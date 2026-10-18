package io.github.hideyukimori.neneclock.application;

/** 設定を保存できなかった理由。閉じた集合として扱う（ARC-010）。 */
public enum SettingsSaveFailure {
    /** 保存領域へ書けない。 */
    UNWRITABLE,
    /** 保存領域に新しい版の設定があるので、上書きせずに守った（読み取り専用・ADR 0023）。 */
    NEWER_VERSION_KEPT
}
