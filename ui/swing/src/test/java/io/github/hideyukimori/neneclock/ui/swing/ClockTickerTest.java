package io.github.hideyukimori.neneclock.ui.swing;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class ClockTickerTest {

    /** 秒の境界から、どれだけ遅れて表示が変わってよいか。Windows のタイマーの粒度（約 15.6ms）に余裕を見た値。 */
    private static final long ALLOWED_LATENESS_MILLIS = 20;

    @Test
    void landsJustAfterTheNextSecondWhereverTheTickCame() {
        // 🔴 200ms 固定の刻みでは、秒の境界の直前に刻みが来ると、次の秒の表示が最大約 200ms 遅れた（Issue #148）。
        //    秒の中のどの位置で刻みが来ても、次の刻みは次の境界の直後に来ること。
        for (long remaining = 1; remaining <= 1000; remaining++) {
            long delay = ClockTicker.nextDelayMillis(Duration.ofMillis(remaining));

            assertThat(delay).as("次の秒まで %dms のとき", remaining).isBetween(remaining, remaining + ALLOWED_LATENESS_MILLIS);
        }
    }

    @Test
    void waitsAtMostOneSecondEvenIfTheClockSaysOtherwise() {
        // 壁時計が戻った・飛んだときでも、刻みが止まったように見えてはいけない。
        assertThat(ClockTicker.nextDelayMillis(Duration.ofSeconds(5)))
                .isLessThanOrEqualTo(1000 + ALLOWED_LATENESS_MILLIS);
        assertThat(ClockTicker.nextDelayMillis(Duration.ZERO)).isPositive();
        assertThat(ClockTicker.nextDelayMillis(Duration.ofMillis(-3))).isPositive();
    }
}
