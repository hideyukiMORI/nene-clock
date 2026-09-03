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
import io.github.hideyukimori.neneclock.domain.WindowTopmost;
import java.time.LocalDateTime;
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
                Language.DEFAULT);
    }
}
