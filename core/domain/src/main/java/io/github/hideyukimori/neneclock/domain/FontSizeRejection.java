package io.github.hideyukimori.neneclock.domain;

/** フォントサイズが拒否された理由。呼び出し側が分岐できるよう、閉じた集合にする（ARC-010）。 */
public enum FontSizeRejection {
    /** 下限未満だった。 */
    BELOW_MINIMUM,
    /** 上限を超えていた。 */
    ABOVE_MAXIMUM
}
