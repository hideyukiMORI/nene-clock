package io.github.hideyukimori.neneclock.application;

import java.util.List;

/**
 * 大きさを測るときの日付行。
 *
 * <p>「日付行が無い」を空のリストで表すと意味が二重化するため、型で表す（JAV-004）。
 * {@link DateLine} と同じ形にしてあるのは、同じ事実の別の側面だからである。
 */
public sealed interface DateExtent {

    /** 日付行がある。候補のうち最も広いものが幅を決め、行の高さも場所を取る。 */
    record Measured(List<String> candidates) implements DateExtent {
        public Measured {
            candidates = List.copyOf(candidates);
        }
    }

    /** 日付行が無い。幅にも高さにも寄与しない。 */
    record Absent() implements DateExtent {}
}
