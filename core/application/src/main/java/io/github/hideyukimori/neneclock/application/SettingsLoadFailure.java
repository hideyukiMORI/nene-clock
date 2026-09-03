package io.github.hideyukimori.neneclock.application;

/** 設定を復元できなかった理由。閉じた集合として扱う（ARC-010）。 */
public enum SettingsLoadFailure {
    /** 保存された設定がまだ無い（初回起動）。 */
    ABSENT,
    /** 保存領域を読めない。 */
    UNREADABLE,
    /** 保存された版をこのアプリが読めない（ARC-009）。 */
    UNSUPPORTED_SCHEMA,
    /** 保存された値が現在の不変条件を満たさない。 */
    INVALID_VALUE
}
