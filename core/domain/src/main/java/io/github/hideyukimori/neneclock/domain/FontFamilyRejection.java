package io.github.hideyukimori.neneclock.domain;

/** 書体名が拒否された理由。呼び出し側が分岐できるよう、閉じた集合にする（ARC-010）。 */
public enum FontFamilyRejection {
    /** 空、または空白だけだった。 */
    BLANK,
    /** 上限より長かった。 */
    TOO_LONG,
    /** 書体名に使えない文字（制御文字）を含んでいた。 */
    UNSUPPORTED_CHARACTER
}
