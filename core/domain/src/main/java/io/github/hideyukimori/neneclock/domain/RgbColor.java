package io.github.hideyukimori.neneclock.domain;

/**
 * 色。RGB の 3 成分だけを持ち、透明度は持たない（FR-044 / FR-046）。
 *
 * <p>コンストラクタは非公開で、生成経路は {@link #of(int, int, int)} ただ 1 本（JAV-007）。
 * 描画に使う色型（{@code java.awt.Color}）への変換は UI の仕事であり、domain は知らない。
 *
 * <p>🔑 文字色と背景色は**同じ型**で表す。「各成分が 0..255」という規則は 1 つであり、
 * それを 2 つの型に写すと同じ規則が 2 か所に存在することになる（ADR 0007）。
 * どちらの色かを語るのは、この型ではなく {@link UserSettings} の成分名である（ARC-004）。
 */
public final class RgbColor {

    /** 各成分の下限。 */
    public static final int MINIMUM_COMPONENT = 0;

    /** 各成分の上限。 */
    public static final int MAXIMUM_COMPONENT = 255;

    /** 既定の文字色（黒）。仕様 FR-040 と一致する。 */
    public static final RgbColor DEFAULT_FONT = new RgbColor(MINIMUM_COMPONENT, MINIMUM_COMPONENT, MINIMUM_COMPONENT);

    /** 既定の背景色。仕様 FR-040 と一致する。 */
    public static final RgbColor DEFAULT_BACKGROUND = new RgbColor(0xF5, 0xF2, 0xEB);

    private final int red;
    private final int green;
    private final int blue;

    private RgbColor(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    /** 各成分を検証して生成する。ここが唯一の生成経路。 */
    public static RgbColorOutcome of(int red, int green, int blue) {
        if (isOutOfRange(red) || isOutOfRange(green) || isOutOfRange(blue)) {
            return new RgbColorOutcome.Rejected(RgbColorRejection.COMPONENT_OUT_OF_RANGE);
        }
        return new RgbColorOutcome.Accepted(new RgbColor(red, green, blue));
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
        return other instanceof RgbColor color && color.red == red && color.green == green && color.blue == blue;
    }

    @Override
    public int hashCode() {
        return (red * (MAXIMUM_COMPONENT + 1) + green) * (MAXIMUM_COMPONENT + 1) + blue;
    }

    @Override
    public String toString() {
        return "RgbColor[" + red + "," + green + "," + blue + "]";
    }

    private static boolean isOutOfRange(int component) {
        return component < MINIMUM_COMPONENT || component > MAXIMUM_COMPONENT;
    }
}
