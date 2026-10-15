package io.github.hideyukimori.neneclock.ui.swing;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Supplier;
import javax.swing.Timer;

/**
 * 再描画のきっかけを作るだけの部品（SWG-005）。
 *
 * <p>刻みは時刻の正本ではない。表示する値は必ず
 * {@link io.github.hideyukimori.neneclock.application.ClockFaceQuery} から取り直す。
 *
 * <p>🔑 刻みは**次の秒の境界の直後**に打つ。以前は 200ms 固定で、秒の切り替わりが最大約 200ms 遅れていた
 * （Issue #148）。待ち時間は刻みのたびに残りから計算し直すので、早く着いても遅れても次の刻みで戻る。
 */
public final class ClockTicker {

    /** 境界を越えてから描くための余裕。Windows のタイマーの粒度（約 15.6ms）より小さいと、境界の手前に着きやすい。 */
    private static final long PAST_THE_BOUNDARY_MILLIS = 15;

    /** 壁時計がどう答えても、これより長くは待たない。刻みが止まったように見せない。 */
    private static final long LONGEST_WAIT_MILLIS = 1000;

    private final Timer timer;
    private final Supplier<Duration> untilNextSecond;

    /** 刻みのたびに {@code onTick} を呼ぶ。次の秒までの残りは {@code untilNextSecond} に聞く。 */
    public ClockTicker(Runnable onTick, Supplier<Duration> untilNextSecond) {
        Objects.requireNonNull(onTick, "onTick");
        this.untilNextSecond = Objects.requireNonNull(untilNextSecond, "untilNextSecond");
        this.timer = new Timer(0, event -> {
            onTick.run();
            armForTheNextSecond();
        });
        timer.setRepeats(false);
    }

    /** 刻みを開始する。EDT から呼ぶこと（SWG-001）。 */
    public void start() {
        armForTheNextSecond();
    }

    /** 刻みを止める。 */
    public void stop() {
        timer.stop();
    }

    /** 次の秒までの残りから、次の刻みまでの待ち時間を決める。 */
    static long nextDelayMillis(Duration untilNextSecond) {
        long remaining = Math.min(Math.max(0, untilNextSecond.toMillis()), LONGEST_WAIT_MILLIS);
        return remaining + PAST_THE_BOUNDARY_MILLIS;
    }

    private void armForTheNextSecond() {
        timer.setInitialDelay((int) nextDelayMillis(untilNextSecond.get()));
        timer.restart();
    }
}
