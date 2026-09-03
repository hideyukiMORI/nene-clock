package io.github.hideyukimori.neneclock.application;

import java.util.Objects;

/**
 * 画面に出す文字列そのもの。UI はこれを描くだけで、整形の判断を持たない（ARC-011）。
 */
public record ClockFace(String time, DateLine date) {

    public ClockFace {
        Objects.requireNonNull(time, "time");
        Objects.requireNonNull(date, "date");
    }
}
