package io.github.hideyukimori.neneclock.application;

import io.github.hideyukimori.neneclock.domain.UserSettings;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

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
