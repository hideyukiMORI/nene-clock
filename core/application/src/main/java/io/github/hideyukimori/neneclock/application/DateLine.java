package io.github.hideyukimori.neneclock.application;

import java.util.Objects;

/**
 * 時計表示の日付行。
 *
 * <p>「非表示」を {@code null} や空文字で表すと意味が二重化するため、型で表す（JAV-004）。
 */
public sealed interface DateLine {

    /** 日付を表示する。 */
    record Shown(String text) implements DateLine {
        public Shown {
            Objects.requireNonNull(text, "text");
        }
    }

    /** 日付を表示しない。 */
    record Hidden() implements DateLine {}
}
