package io.github.hideyukimori.neneclock.domain;

/** 常に最前面に表示するかどうか。boolean ではなく列挙で表す（JAV-002）。 */
public enum WindowTopmost {
    /** 常に最前面に置く。 */
    ENABLED,
    /** 通常の重なり順に従う。 */
    DISABLED
}
