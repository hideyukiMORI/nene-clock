package io.github.hideyukimori.neneclock.domain;

/** 秒の表示可否。boolean ではなく列挙で表す（JAV-002）。 */
public enum SecondsVisibility {
    /** 秒を表示する。 */
    SHOWN,
    /** 秒を表示しない。 */
    HIDDEN
}
