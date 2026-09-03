package io.github.hideyukimori.neneclock.domain;

/**
 * 色。RGB の 3 成分と透明度（FR-044 / FR-046）。
 *
 * <p>コンストラクタは非公開で、生成経路は {@link #of(int, int, int, int)} ただ 1 本（JAV-007）。
 * 描画に使う色型（{@code java.awt.Color}）への変換は UI の仕事であり、domain は知らない。
 *
 * <p>🔑 文字色と背景色は**同じ型**で表す。「各成分が 0..255」という規則は 1 つであり、
 * それを 2 つの型に写すと同じ規則が 2 か所に存在することになる（ADR 0007）。
 * どちらの色かを語るのは、この型ではなく {@link UserSettings} の成分名である（ARC-004）。
 *
 * <p>透明度も同じ 0..255 の規則に従う。**半透明で描けるかどうかは環境依存の事実**であり、
 * この型の関心ではない（ADR 0011）。描けない環境では不透明として描かれる。
 */
public final class RgbaColor {

    /** 各成分の下限。 */
    public static final int MINIMUM_COMPONENT = 0;

    /** 各成分の上限。 */
    public static final int MAXIMUM_COMPONENT = 255;

    /** 完全に不透明。 */
    public static final int OPAQUE = MAXIMUM_COMPONENT;

    /** 既定の文字色（黒・不透明）。仕様 FR-040 と一致する。 */
    public static final RgbaColor DEFAULT_FONT =
            new RgbaColor(MINIMUM_COMPONENT, MINIMUM_COMPONENT, MINIMUM_COMPONENT, OPAQUE);

    /** 既定の背景色（不透明）。仕様 FR-040 と一致する。 */
    public static final RgbaColor DEFAULT_BACKGROUND = new RgbaColor(0xF5, 0xF2, 0xEB, OPAQUE);

    private final int red;
    private final int green;
    private final int blue;
    private final int alpha;

    private RgbaColor(int red, int green, int blue, int alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    /** 各成分を検証して生成する。ここが唯一の生成経路。 */
    public static RgbaColorOutcome of(int red, int green, int blue, int alpha) {
        if (isOutOfRange(red) || isOutOfRange(green) || isOutOfRange(blue) || isOutOfRange(alpha)) {
            return new RgbaColorOutcome.Rejected(RgbaColorRejection.COMPONENT_OUT_OF_RANGE);
        }
        return new RgbaColorOutcome.Accepted(new RgbaColor(red, green, blue, alpha));
    }

    /** 不透明な色として生成する。検証は {@link #of(int, int, int, int)} に任せる。 */
    public static RgbaColorOutcome opaque(int red, int green, int blue) {
        return of(red, green, blue, OPAQUE);
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

    /** 透明度。0 が完全に透明、255 が完全に不透明。 */
    public int alpha() {
        return alpha;
    }

    /** 透明度だけを差し替える。ほかの成分は保つ。 */
    public RgbaColor withAlpha(int replacement) {
        return switch (of(red, green, blue, replacement)) {
            case RgbaColorOutcome.Accepted accepted -> accepted.value();
            // 範囲外なら変えない。値を勝手に丸めない。
            case RgbaColorOutcome.Rejected outOfRange -> this;
        };
    }

    /** 不透明にしたもの。コントラストの計算など、透け方を考えない場面で使う。 */
    public RgbaColor asOpaque() {
        return alpha == OPAQUE ? this : withAlpha(OPAQUE);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof RgbaColor color
                && color.red == red
                && color.green == green
                && color.blue == blue
                && color.alpha == alpha;
    }

    @Override
    public int hashCode() {
        int packed = (red * (MAXIMUM_COMPONENT + 1) + green) * (MAXIMUM_COMPONENT + 1) + blue;
        return packed * (MAXIMUM_COMPONENT + 1) + alpha;
    }

    @Override
    public String toString() {
        return "RgbaColor[" + red + "," + green + "," + blue + "," + alpha + "]";
    }

    private static boolean isOutOfRange(int component) {
        return component < MINIMUM_COMPONENT || component > MAXIMUM_COMPONENT;
    }
}
