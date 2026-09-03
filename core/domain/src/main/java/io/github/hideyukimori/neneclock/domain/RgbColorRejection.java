package io.github.hideyukimori.neneclock.domain;

/** 文字色が拒否された理由。呼び出し側が分岐できるよう、閉じた集合にする（ARC-010）。 */
public enum RgbColorRejection {
    /** RGB のいずれかの成分が 0..255 の外だった。 */
    COMPONENT_OUT_OF_RANGE
}
