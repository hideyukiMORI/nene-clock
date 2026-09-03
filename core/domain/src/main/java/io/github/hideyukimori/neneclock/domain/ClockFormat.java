package io.github.hideyukimori.neneclock.domain;

/** 時刻表記。12 時間制と 24 時間制の 2 つしかない閉じた選択肢（JAV-002）。 */
public enum ClockFormat {
    /** 24 時間表記（既定）。 */
    HOUR_24,
    /** 12 時間表記。午前・午後の表示を伴う。 */
    HOUR_12
}
