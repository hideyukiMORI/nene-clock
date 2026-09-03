package io.github.hideyukimori.neneclock.application;

/** 設定を保存できなかった理由。閉じた集合として扱う（ARC-010）。 */
public enum SettingsSaveFailure {
    /** 保存領域へ書けない。 */
    UNWRITABLE
}
