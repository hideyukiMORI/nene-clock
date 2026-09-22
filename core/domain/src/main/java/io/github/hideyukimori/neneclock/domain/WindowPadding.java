package io.github.hideyukimori.neneclock.domain;

/**
 * 時計の文字の周りに取る余白（画素）。窓の大きさはこれで変わる（ADR 0017）。
 *
 * <p>窓の幅・高さそのものは設定として持たない。文字サイズと窓サイズの両方が大きさを主張すると
 * どちらかが必ず折れるためである（FR-049）。
 *
 * <p>コンストラクタは非公開で、生成経路は {@link #of(int)} ただ 1 本（JAV-007）。
 * 不正な値は例外ではなく {@link WindowPaddingOutcome} で返す。
 */
public final class WindowPadding {

    /** 許容する下限。仕様 FR-049 の範囲と一致する。 */
    public static final int MINIMUM_PIXELS = 8;

    /** 許容する上限。仕様 FR-049 の範囲と一致する。 */
    public static final int MAXIMUM_PIXELS = 160;

    private static final int DEFAULT_PIXELS = 30;

    /**
     * 既定値。v7 までの固定値と同じ大きさである。
     *
     * <p>ここで {@link #of(int)} を通して結果型を開くと、到達し得ない「拒否側」の分岐が残る。
     * 到達しない防御コードを置くより、範囲内であることをテストで示すほうがこのリポジトリの
     * 規律に合う（QLT-008 / QLT-009）。不変条件は {@code WindowPaddingTest} が保証している。
     */
    public static final WindowPadding DEFAULT = new WindowPadding(DEFAULT_PIXELS);

    private final int pixels;

    private WindowPadding(int pixels) {
        this.pixels = pixels;
    }

    /** 範囲を検証して生成する。ここが唯一の生成経路。 */
    public static WindowPaddingOutcome of(int pixels) {
        if (pixels < MINIMUM_PIXELS) {
            return new WindowPaddingOutcome.Rejected(WindowPaddingRejection.BELOW_MINIMUM);
        }
        if (pixels > MAXIMUM_PIXELS) {
            return new WindowPaddingOutcome.Rejected(WindowPaddingRejection.ABOVE_MAXIMUM);
        }
        return new WindowPaddingOutcome.Accepted(new WindowPadding(pixels));
    }

    /** 画素数。文字の周り 4 辺すべてに同じだけ取る。 */
    public int pixels() {
        return pixels;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof WindowPadding padding && padding.pixels == pixels;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(pixels);
    }

    @Override
    public String toString() {
        return "WindowPadding[" + pixels + "]";
    }
}
