package io.github.hideyukimori.neneclock.application;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.hideyukimori.neneclock.domain.ClockFormat;
import io.github.hideyukimori.neneclock.domain.DateVisibility;
import io.github.hideyukimori.neneclock.domain.FontSize;
import io.github.hideyukimori.neneclock.domain.Language;
import io.github.hideyukimori.neneclock.domain.RgbColor;
import io.github.hideyukimori.neneclock.domain.SecondsVisibility;
import io.github.hideyukimori.neneclock.domain.Typeface;
import io.github.hideyukimori.neneclock.domain.UserSettings;
import io.github.hideyukimori.neneclock.domain.WindowPadding;
import io.github.hideyukimori.neneclock.domain.WindowTopmost;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ClockFaceQueryTest {

    private static final LocalDateTime AFTERNOON = LocalDateTime.of(2026, 9, 3, 13, 45, 9);

    private final ClockFaceQuery query = new ClockFaceQuery(() -> AFTERNOON);

    @Test
    void formatsTwentyFourHourTimeWithSeconds() {
        ClockFace face =
                query.currentFace(settings(ClockFormat.HOUR_24, SecondsVisibility.SHOWN, DateVisibility.SHOWN));

        assertThat(face.time()).isEqualTo("13:45:09");
        assertThat(face.date()).isEqualTo(new DateLine.Shown("2026-09-03"));
    }

    @Test
    void formatsTwentyFourHourTimeWithoutSeconds() {
        ClockFace face =
                query.currentFace(settings(ClockFormat.HOUR_24, SecondsVisibility.HIDDEN, DateVisibility.SHOWN));

        assertThat(face.time()).isEqualTo("13:45");
    }

    @Test
    void formatsTwelveHourTimeWithSeconds() {
        ClockFace face =
                query.currentFace(settings(ClockFormat.HOUR_12, SecondsVisibility.SHOWN, DateVisibility.SHOWN));

        assertThat(face.time()).isEqualTo("01:45:09 PM");
    }

    @Test
    void formatsTwelveHourTimeWithoutSeconds() {
        ClockFace face =
                query.currentFace(settings(ClockFormat.HOUR_12, SecondsVisibility.HIDDEN, DateVisibility.SHOWN));

        assertThat(face.time()).isEqualTo("01:45 PM");
    }

    @Test
    void hidesTheDateLineWhenTheSettingSaysSo() {
        ClockFace face =
                query.currentFace(settings(ClockFormat.HOUR_24, SecondsVisibility.SHOWN, DateVisibility.HIDDEN));

        assertThat(face.date()).isEqualTo(new DateLine.Hidden());
    }

    @Test
    void readsTheClockOnEveryCall() {
        LocalDateTime[] moments = {AFTERNOON, AFTERNOON.plusSeconds(1)};
        int[] calls = {0};
        ClockFaceQuery advancing = new ClockFaceQuery(() -> moments[calls[0]++]);
        UserSettings settings = settings(ClockFormat.HOUR_24, SecondsVisibility.SHOWN, DateVisibility.SHOWN);

        assertThat(advancing.currentFace(settings).time()).isEqualTo("13:45:09");
        assertThat(advancing.currentFace(settings).time()).isEqualTo("13:45:10");
    }

    @Test
    void boundsTheWidestFaceWithEveryDigitInsteadOfTheDigitsShownNow() {
        ClockFaceExtent extent =
                query.widestFace(settings(ClockFormat.HOUR_24, SecondsVisibility.SHOWN, DateVisibility.SHOWN));

        // 数字の字幅が揃っていない書体でも最大を取り逃がさないよう、0〜9 のすべてで埋めた形を渡す。
        assertThat(extent.timeCandidates()).hasSize(10).contains("00:00:00", "99:99:99");
        assertThat(extent.date()).isEqualTo(new DateExtent.Measured(dateCandidates()));
    }

    @Test
    void keepsBothMarkersInTheWidestTwelveHourFace() {
        ClockFaceExtent extent =
                query.widestFace(settings(ClockFormat.HOUR_12, SecondsVisibility.SHOWN, DateVisibility.SHOWN));

        // AM と PM のどちらが広いかは書体が決める。両方渡さないと窓が正午に震える。
        assertThat(extent.timeCandidates()).hasSize(20).contains("00:00:00 AM", "00:00:00 PM", "99:99:99 PM");
    }

    @Test
    void dropsTheMarkerFromTheWidestFaceInTwentyFourHourTime() {
        ClockFaceExtent extent =
                query.widestFace(settings(ClockFormat.HOUR_24, SecondsVisibility.SHOWN, DateVisibility.SHOWN));

        assertThat(extent.timeCandidates())
                .allSatisfy(
                        candidate -> assertThat(candidate).doesNotContain("AM").doesNotContain("PM"));
    }

    @Test
    void dropsTheSecondsFromTheWidestFaceWhenTheyAreHidden() {
        ClockFaceExtent extent =
                query.widestFace(settings(ClockFormat.HOUR_24, SecondsVisibility.HIDDEN, DateVisibility.SHOWN));

        assertThat(extent.timeCandidates()).hasSize(10).contains("99:99");
    }

    @Test
    void dropsTheDateLineFromTheWidestFaceWhenItIsHidden() {
        ClockFaceExtent extent =
                query.widestFace(settings(ClockFormat.HOUR_24, SecondsVisibility.SHOWN, DateVisibility.HIDDEN));

        assertThat(extent.date()).isEqualTo(new DateExtent.Absent());
    }

    @Test
    void theWidestFaceHasTheSameShapeAsEveryFaceThatCanBeShown() {
        // 🔑 これが「窓が毎秒震えない」ことの根拠である。いま出ている文字列は、候補のどれかと
        //    同じ長さで、数字でない位置も同じである。だから候補の最大幅に必ず収まる。
        for (ClockFormat format : ClockFormat.values()) {
            for (SecondsVisibility seconds : SecondsVisibility.values()) {
                UserSettings settings = settings(format, seconds, DateVisibility.SHOWN);
                assertThat(query.widestFace(settings).timeCandidates())
                        .anySatisfy(candidate -> assertThat(sameShape(
                                        candidate, query.currentFace(settings).time()))
                                .isTrue());
            }
        }
    }

    @Test
    void doesNotReadTheClockToMeasureTheWidestFace() {
        int[] calls = {0};
        ClockFaceQuery counting = new ClockFaceQuery(() -> {
            calls[0]++;
            return AFTERNOON;
        });

        counting.widestFace(settings(ClockFormat.HOUR_12, SecondsVisibility.SHOWN, DateVisibility.SHOWN));

        assertThat(calls[0]).isZero();
    }

    /** 長さが同じで、数字でない位置がすべて一致するか。 */
    private static boolean sameShape(String candidate, String shown) {
        if (candidate.length() != shown.length()) {
            return false;
        }
        for (int index = 0; index < candidate.length(); index++) {
            boolean digit = Character.isDigit(candidate.charAt(index));
            if (digit != Character.isDigit(shown.charAt(index))) {
                return false;
            }
            if (!digit && candidate.charAt(index) != shown.charAt(index)) {
                return false;
            }
        }
        return true;
    }

    private static List<String> dateCandidates() {
        List<String> candidates = new ArrayList<>();
        for (int digit = 0; digit < 10; digit++) {
            String repeated = String.valueOf(digit).repeat(2);
            candidates.add(repeated + repeated + "-" + repeated + "-" + repeated);
        }
        return candidates;
    }

    private static UserSettings settings(ClockFormat format, SecondsVisibility seconds, DateVisibility date) {
        return new UserSettings(
                format,
                seconds,
                date,
                WindowTopmost.DISABLED,
                Typeface.DEFAULT,
                FontSize.DEFAULT,
                RgbColor.DEFAULT_FONT,
                RgbColor.DEFAULT_BACKGROUND,
                Language.DEFAULT,
                WindowPadding.DEFAULT);
    }
}
