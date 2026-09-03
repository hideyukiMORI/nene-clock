package io.github.hideyukimori.neneclock.domain;

/**
 * 表示フォントの大きさ（ポイント）。
 *
 * <p>コンストラクタは非公開で、生成経路は {@link #of(int)} ただ 1 本（JAV-007）。
 * 不正な値は例外ではなく {@link FontSizeOutcome} で返す。
 */
public final class FontSize {

    /** 許容する下限。仕様 FR-041 の範囲と一致する。 */
    public static final int MINIMUM_POINTS = 24;

    /** 許容する上限。仕様 FR-041 の範囲と一致する。 */
    public static final int MAXIMUM_POINTS = 160;

    private static final int DEFAULT_POINTS = 64;

    /**
     * 既定値。
     *
     * <p>ここで {@link #of(int)} を通して結果型を開くと、到達し得ない「拒否側」の分岐が残る。
     * 到達しない防御コードを置くより、範囲内であることをテストで示すほうがこのリポジトリの
     * 規律に合う（QLT-008 / QLT-009）。不変条件は {@code FontSizeTest} が保証している。
     */
    public static final FontSize DEFAULT = new FontSize(DEFAULT_POINTS);

    private final int points;

    private FontSize(int points) {
        this.points = points;
    }

    /** 範囲を検証して生成する。ここが唯一の生成経路。 */
    public static FontSizeOutcome of(int points) {
        if (points < MINIMUM_POINTS) {
            return new FontSizeOutcome.Rejected(FontSizeRejection.BELOW_MINIMUM);
        }
        if (points > MAXIMUM_POINTS) {
            return new FontSizeOutcome.Rejected(FontSizeRejection.ABOVE_MAXIMUM);
        }
        return new FontSizeOutcome.Accepted(new FontSize(points));
    }

    /** ポイント数。 */
    public int points() {
        return points;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof FontSize size && size.points == points;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(points);
    }

    @Override
    public String toString() {
        return "FontSize[" + points + "]";
    }
}
