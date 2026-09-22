package io.github.hideyukimori.neneclock.application;

import io.github.hideyukimori.neneclock.domain.UserSettings;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * 設定から表示文字列を作る唯一の経路（ARC-001）。
 *
 * <p>UI 側で {@code DateTimeFormatter} を組み立てることは ArchUnit が拒否する。整形規則が
 * 2 か所に分かれると、表示のずれが「どちらが正しいか分からない」形で残るため。
 */
public final class ClockFaceQuery {

    private static final DateTimeFormatter TIME_24_WITH_SECONDS = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ROOT);

    private static final DateTimeFormatter TIME_24_WITHOUT_SECONDS = DateTimeFormatter.ofPattern("HH:mm", Locale.ROOT);

    private static final DateTimeFormatter TIME_12_WITH_SECONDS =
            DateTimeFormatter.ofPattern("hh:mm:ss a", Locale.ROOT);

    private static final DateTimeFormatter TIME_12_WITHOUT_SECONDS =
            DateTimeFormatter.ofPattern("hh:mm a", Locale.ROOT);

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ROOT);

    /**
     * 形だけを取り出すための時刻。値そのものには意味が無い。
     *
     * <p>午前と午後の 2 つを持つのは、12 時間表記の AM / PM がここでしか現れないためである。
     * 現在時刻は読まない（ARC-007）。整形の形は時刻に依らず同じ長さなので、定数で足りる。
     */
    private static final LocalDateTime SHAPE_BEFORE_NOON = LocalDateTime.of(2000, 11, 22, 10, 33, 44);

    private static final LocalDateTime SHAPE_AFTER_NOON = LocalDateTime.of(2000, 11, 22, 22, 33, 44);

    private static final int DIGIT_COUNT = 10;

    private final WallClockPort clock;

    public ClockFaceQuery(WallClockPort clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /** 現在時刻を設定に従って整形する。 */
    public ClockFace currentFace(UserSettings settings) {
        Objects.requireNonNull(settings, "settings");
        LocalDateTime moment = clock.currentDateTime();
        String time = timeFormatter(settings).format(moment);
        DateLine date =
                switch (settings.dateVisibility()) {
                    case SHOWN -> new DateLine.Shown(DATE.format(moment));
                    case HIDDEN -> new DateLine.Hidden();
                };
        return new ClockFace(time, date);
    }

    /**
     * いまの設定で起こりうる、最も大きい面の候補を作る（FR-049）。
     *
     * <p>窓の大きさはこれで決まる。**「どの設定でも起こりうる最大」ではなく「この設定で
     * 起こりうる最大」**を返すので、24 時間表記にすれば AM / PM の幅は含まれず、
     * 秒を隠せば秒の幅も含まれず、日付を隠せば日付行は場所を取らない（ADR 0017）。
     *
     * <p>候補は「整形した形の数字をすべて同じ数字で埋めたもの」を 0〜9 の 10 通り作る。
     * 文字列の幅は各文字の送り幅の和なので、最も広い数字ですべての桁を埋めたものが最大を上から抑える。
     * 数字の字幅が揃っている書体（既定の JetBrains Mono を含む）では、この上界は実際の最大と一致する。
     * 時刻も日付も桁数が固定なので、候補の長さは実際に表示される文字列と同じである。
     */
    public ClockFaceExtent widestFace(UserSettings settings) {
        Objects.requireNonNull(settings, "settings");
        DateTimeFormatter formatter = timeFormatter(settings);
        // 24 時間表記では午前と午後の形が同じになる。同じ候補を 2 度測る意味は無い。
        Set<String> times = new LinkedHashSet<>(everyDigitFilling(formatter.format(SHAPE_BEFORE_NOON)));
        times.addAll(everyDigitFilling(formatter.format(SHAPE_AFTER_NOON)));
        DateExtent date =
                switch (settings.dateVisibility()) {
                    case SHOWN -> new DateExtent.Measured(everyDigitFilling(DATE.format(SHAPE_BEFORE_NOON)));
                    case HIDDEN -> new DateExtent.Absent();
                };
        return new ClockFaceExtent(List.copyOf(times), date);
    }

    /** 整形済みの形の数字をすべて同じ数字に置き換えた 10 通り。区切り文字と AM / PM はそのまま残る。 */
    private static List<String> everyDigitFilling(String shape) {
        List<String> candidates = new ArrayList<>(DIGIT_COUNT);
        for (int digit = 0; digit < DIGIT_COUNT; digit++) {
            StringBuilder filled = new StringBuilder(shape.length());
            for (int index = 0; index < shape.length(); index++) {
                char character = shape.charAt(index);
                filled.append(character >= '0' && character <= '9' ? (char) ('0' + digit) : character);
            }
            candidates.add(filled.toString());
        }
        return candidates;
    }

    private static DateTimeFormatter timeFormatter(UserSettings settings) {
        return switch (settings.clockFormat()) {
            case HOUR_24 ->
                switch (settings.secondsVisibility()) {
                    case SHOWN -> TIME_24_WITH_SECONDS;
                    case HIDDEN -> TIME_24_WITHOUT_SECONDS;
                };
            case HOUR_12 ->
                switch (settings.secondsVisibility()) {
                    case SHOWN -> TIME_12_WITH_SECONDS;
                    case HIDDEN -> TIME_12_WITHOUT_SECONDS;
                };
        };
    }
}
