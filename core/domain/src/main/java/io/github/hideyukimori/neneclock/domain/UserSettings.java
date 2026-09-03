package io.github.hideyukimori.neneclock.domain;

import java.util.Objects;

/**
 * 利用者設定。すべて不変で、部分的に組み立てられない（JAV-003 / JAV-007）。
 *
 * <p>表示に関する事実の唯一の所有者はこの型である（ARC-004）。同じ事実を 2 か所に持たない。
 * 成分が 9 つあるのは、それらが「表示設定」という 1 つの概念だからであり、
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
        RgbColor backgroundColor,
        Language language) {

    public UserSettings {
        Objects.requireNonNull(clockFormat, "clockFormat");
        Objects.requireNonNull(secondsVisibility, "secondsVisibility");
        Objects.requireNonNull(dateVisibility, "dateVisibility");
        Objects.requireNonNull(windowTopmost, "windowTopmost");
        Objects.requireNonNull(typeface, "typeface");
        Objects.requireNonNull(fontSize, "fontSize");
        Objects.requireNonNull(fontColor, "fontColor");
        Objects.requireNonNull(backgroundColor, "backgroundColor");
        Objects.requireNonNull(language, "language");
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
                RgbColor.DEFAULT_BACKGROUND,
                Language.DEFAULT);
    }

    /** 時刻表記だけを差し替える。 */
    public UserSettings withClockFormat(ClockFormat replacement) {
        return new UserSettings(
                replacement,
                secondsVisibility,
                dateVisibility,
                windowTopmost,
                typeface,
                fontSize,
                fontColor,
                backgroundColor,
                language);
    }

    /** 秒の表示だけを差し替える。 */
    public UserSettings withSecondsVisibility(SecondsVisibility replacement) {
        return new UserSettings(
                clockFormat,
                replacement,
                dateVisibility,
                windowTopmost,
                typeface,
                fontSize,
                fontColor,
                backgroundColor,
                language);
    }

    /** 日付の表示だけを差し替える。 */
    public UserSettings withDateVisibility(DateVisibility replacement) {
        return new UserSettings(
                clockFormat,
                secondsVisibility,
                replacement,
                windowTopmost,
                typeface,
                fontSize,
                fontColor,
                backgroundColor,
                language);
    }

    /** 最前面だけを差し替える。 */
    public UserSettings withWindowTopmost(WindowTopmost replacement) {
        return new UserSettings(
                clockFormat,
                secondsVisibility,
                dateVisibility,
                replacement,
                typeface,
                fontSize,
                fontColor,
                backgroundColor,
                language);
    }

    /** 書体だけを差し替える。 */
    public UserSettings withTypeface(Typeface replacement) {
        return new UserSettings(
                clockFormat,
                secondsVisibility,
                dateVisibility,
                windowTopmost,
                replacement,
                fontSize,
                fontColor,
                backgroundColor,
                language);
    }

    /** 大きさだけを差し替える。 */
    public UserSettings withFontSize(FontSize replacement) {
        return new UserSettings(
                clockFormat,
                secondsVisibility,
                dateVisibility,
                windowTopmost,
                typeface,
                replacement,
                fontColor,
                backgroundColor,
                language);
    }

    /** 文字色だけを差し替える。 */
    public UserSettings withFontColor(RgbColor replacement) {
        return new UserSettings(
                clockFormat,
                secondsVisibility,
                dateVisibility,
                windowTopmost,
                typeface,
                fontSize,
                replacement,
                backgroundColor,
                language);
    }

    /** 背景色だけを差し替える。 */
    public UserSettings withBackgroundColor(RgbColor replacement) {
        return new UserSettings(
                clockFormat,
                secondsVisibility,
                dateVisibility,
                windowTopmost,
                typeface,
                fontSize,
                fontColor,
                replacement,
                language);
    }

    /** 言語だけを差し替える。 */
    public UserSettings withLanguage(Language replacement) {
        return new UserSettings(
                clockFormat,
                secondsVisibility,
                dateVisibility,
                windowTopmost,
                typeface,
                fontSize,
                fontColor,
                backgroundColor,
                replacement);
    }
}
