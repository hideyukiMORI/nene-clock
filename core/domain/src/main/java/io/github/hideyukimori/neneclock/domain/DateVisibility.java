package io.github.hideyukimori.neneclock.domain;

/** 日付行の表示可否。boolean ではなく列挙で表す（JAV-002）。 */
public enum DateVisibility {
    /** 日付を表示する。 */
    SHOWN,
    /** 日付を表示しない。 */
    HIDDEN
}
