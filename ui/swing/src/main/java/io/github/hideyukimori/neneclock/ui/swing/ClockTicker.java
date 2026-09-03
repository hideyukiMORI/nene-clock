package io.github.hideyukimori.neneclock.ui.swing;

import java.util.Objects;
import javax.swing.Timer;

/**
 * 再描画のきっかけを作るだけの部品（SWG-005）。
 *
 * <p>刻みは時刻の正本ではない。表示する値は必ず
 * {@link io.github.hideyukimori.neneclock.application.ClockFaceQuery} から取り直す。
 */
public final class ClockTicker {

    private static final int TICK_MILLIS = 200;

    private final Timer timer;

    public ClockTicker(Runnable onTick) {
        Objects.requireNonNull(onTick, "onTick");
        this.timer = new Timer(TICK_MILLIS, event -> onTick.run());
    }

    /** 刻みを開始する。EDT から呼ぶこと（SWG-001）。 */
    public void start() {
        timer.start();
    }

    /** 刻みを止める。 */
    public void stop() {
        timer.stop();
    }
}
