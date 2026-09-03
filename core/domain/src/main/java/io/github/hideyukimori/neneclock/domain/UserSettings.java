package io.github.hideyukimori.neneclock.domain;

import java.util.Objects;

/**
 * 利用者設定。すべて不変で、部分的に組み立てられない（JAV-003 / JAV-007）。
 *
 * <p>表示に関する事実の唯一の所有者はこの型である（ARC-004）。同じ事実を 2 か所に持たない。
 * 成分が 8 つあるのは、それらが「表示設定」という 1 つの概念だからであり、
 * 名前付きの型にまとめたものが JAV-012 の言う「引数を減らす手段」そのものである。
 *
 * <p>🔑 文字色と背景色は同じ {@link RgbColor} 型である。どちらであるかを語るのは成分名であり、
 * 型ではない（ADR 0007）。組み立てるときに 2 つを取り違えないことは、この型の利用者の責任になる。
 */
public record UserSettings(
        ClockFormat clockFormat,
        SecondsVisibility secondsVisibility,
        DateVisibility dateVisibility,
        WindowTopmost windowTopmost,
        Typeface typeface,
        FontSize fontSize,
        RgbColor fontColor,
        RgbColor backgroundColor) {

    public UserSettings {
        Objects.requireNonNull(clockFormat, "clockFormat");
        Objects.requireNonNull(secondsVisibility, "secondsVisibility");
        Objects.requireNonNull(dateVisibility, "dateVisibility");
        Objects.requireNonNull(windowTopmost, "windowTopmost");
        Objects.requireNonNull(typeface, "typeface");
        Objects.requireNonNull(fontSize, "fontSize");
        Objects.requireNonNull(fontColor, "fontColor");
        Objects.requireNonNull(backgroundColor, "backgroundColor");
    }

    /** 既定値。仕様 FR-040 と一致する。 */
    public static UserSettings defaults() {
        return new UserSettings(
                ClockFormat.HOUR_24,
                SecondsVisibility.SHOWN,
                DateVisibility.SHOWN,
                WindowTopmost.DISABLED,
                Typeface.DEFAULT,
                FontSize.DEFAULT,
                RgbColor.DEFAULT_FONT,
                RgbColor.DEFAULT_BACKGROUND);
    }
}
