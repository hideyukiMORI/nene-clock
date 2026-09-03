package io.github.hideyukimori.neneclock.ui.swing;

import java.util.Objects;

/**
 * 見本に出す文字列。整形するのは application の仕事なので、UI は受け取るだけである（ARC-001）。
 */
public record ClockPreviewText(String time, String date) {

    public ClockPreviewText {
        Objects.requireNonNull(time, "time");
        Objects.requireNonNull(date, "date");
    }
}
