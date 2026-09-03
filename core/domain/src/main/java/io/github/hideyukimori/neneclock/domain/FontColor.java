package io.github.hideyukimori.neneclock.domain;

/**
 * 文字色。RGB の 3 成分だけを持ち、透明度は持たない（FR-044）。
 *
 * <p>コンストラクタは非公開で、生成経路は {@link #of(int, int, int)} ただ 1 本（JAV-007）。
 * 描画に使う色型（{@code java.awt.Color}）への変換は UI の仕事であり、domain は知らない。
 */
public final class FontColor {

    /** 各成分の下限。 */
    public static final int MINIMUM_COMPONENT = 0;

    /** 各成分の上限。 */
    public static final int MAXIMUM_COMPONENT = 255;

    /** 既定の文字色（黒）。仕様 FR-040 と一致する。 */
    public static final FontColor DEFAULT = new FontColor(MINIMUM_COMPONENT, MINIMUM_COMPONENT, MINIMUM_COMPONENT);

    private final int red;
    private final int green;
    private final int blue;

    private FontColor(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    /** 各成分を検証して生成する。ここが唯一の生成経路。 */
    public static FontColorOutcome of(int red, int green, int blue) {
        if (isOutOfRange(red) || isOutOfRange(green) || isOutOfRange(blue)) {
            return new FontColorOutcome.Rejected(FontColorRejection.COMPONENT_OUT_OF_RANGE);
        }
        return new FontColorOutcome.Accepted(new FontColor(red, green, blue));
    }

    /** 赤成分。 */
    public int red() {
        return red;
    }

    /** 緑成分。 */
    public int green() {
        return green;
    }

    /** 青成分。 */
    public int blue() {
        return blue;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof FontColor color && color.red == red && color.green == green && color.blue == blue;
    }

    @Override
    public int hashCode() {
        return (red * (MAXIMUM_COMPONENT + 1) + green) * (MAXIMUM_COMPONENT + 1) + blue;
    }

    @Override
    public String toString() {
        return "FontColor[" + red + "," + green + "," + blue + "]";
    }

    private static boolean isOutOfRange(int component) {
        return component < MINIMUM_COMPONENT || component > MAXIMUM_COMPONENT;
    }
}
