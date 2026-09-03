package io.github.hideyukimori.neneclock.domain;

import java.util.Objects;

/**
 * 利用者設定。すべて不変で、部分的に組み立てられない（JAV-003 / JAV-007）。
 *
 * <p>同じ事実を 2 か所に持たないため、表示に関する設定はこの 1 つの型だけが所有する（ARC-004）。
 */
public record UserSettings(
        ClockFormat clockFormat,
        SecondsVisibility secondsVisibility,
        DateVisibility dateVisibility,
        WindowTopmost windowTopmost,
        FontSize fontSize) {

    public UserSettings {
        Objects.requireNonNull(clockFormat, "clockFormat");
        Objects.requireNonNull(secondsVisibility, "secondsVisibility");
        Objects.requireNonNull(dateVisibility, "dateVisibility");
        Objects.requireNonNull(windowTopmost, "windowTopmost");
        Objects.requireNonNull(fontSize, "fontSize");
    }

    /** 既定値。仕様 FR-040 と一致する。 */
    public static UserSettings defaults() {
        return new UserSettings(
                ClockFormat.HOUR_24,
                SecondsVisibility.SHOWN,
                DateVisibility.SHOWN,
                WindowTopmost.DISABLED,
                FontSize.DEFAULT);
    }
}
