package io.github.hideyukimori.neneclock.ui.swing;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import org.junit.jupiter.api.Test;

/**
 * ドラッグの座標計算（ADR 0018 / Issue #95・#96）。
 *
 * <p>🔴 ここで見られるのは算術だけである。「拡大率の違うモニタをまたいでも窓が膨らまない」こと
 * そのものは、per-monitor DPI を持つ実機でしか見られない（QLT-012）。この環境（WSLg）には
 * per-monitor DPI が無い。記録は quality/gate-proofs.md 第 21 節。
 */
class WindowDragTest {

    /** 実機で測った論理サイズ 765x208 の窓（ADR 0018 の表）。 */
    private static final Rectangle WINDOW = new Rectangle(100, 50, 765, 208);

    @Test
    void theGrabbedPointStaysUnderThePointer() {
        WindowDrag drag = WindowDrag.grabbedAt(new Point(150, 80), WINDOW);

        Point location = drag.locationFor(new Point(1500, 900));

        assertThat(new Point(1500 - location.x, 900 - location.y)).isEqualTo(drag.grabbedPoint());
    }

    @Test
    void theLocationIsDecidedByThePointerAloneSoErrorCannotAccumulate() {
        WindowDrag drag = WindowDrag.grabbedAt(new Point(150, 80), WINDOW);
        Point last = new Point(150, 80);

        for (int step = 1; step <= 500; step++) {
            last = new Point(150 + step * 13, 80 + step * 7);
            drag.locationFor(last); // 途中の計算は何も残さない
        }

        assertThat(drag.locationFor(last)).isEqualTo(new Point(last.x - 50, last.y - 30));
    }

    @Test
    void theSameJourneyInOneJumpEndsAtTheSamePlace() {
        WindowDrag crept = WindowDrag.grabbedAt(new Point(150, 80), WINDOW);
        WindowDrag jumped = WindowDrag.grabbedAt(new Point(150, 80), WINDOW);
        for (int x = 150; x <= 3000; x += 3) {
            crept.locationFor(new Point(x, 80));
        }

        assertThat(crept.locationFor(new Point(3000, 80))).isEqualTo(jumped.locationFor(new Point(3000, 80)));
    }

    @Test
    void aSizeChangeDuringTheDragIsNoticed() {
        WindowDrag drag = WindowDrag.grabbedAt(new Point(150, 80), WINDOW);

        assertThat(drag.stillFits(new Dimension(765, 208))).isTrue();
        assertThat(drag.stillFits(new Dimension(637, 173))).isFalse(); // 過渡状態の実測値（ADR 0018）
    }

    @Test
    void theGrabbedPointComesBackInsideAWindowThatShrank() {
        WindowDrag wide = WindowDrag.grabbedAt(new Point(800, 240), WINDOW);

        WindowDrag narrow = WindowDrag.grabbedAt(new Point(800, 240), new Rectangle(100, 50, 637, 173));

        assertThat(wide.grabbedPoint()).isEqualTo(new Point(700, 190));
        assertThat(narrow.grabbedPoint()).isEqualTo(new Point(636, 172));
    }

    @Test
    void aPointerOutsideTheWindowIsPulledBackToTheEdge() {
        WindowDrag before = WindowDrag.grabbedAt(new Point(50, 20), WINDOW);

        assertThat(before.grabbedPoint()).isEqualTo(new Point(0, 0));
    }

    @Test
    void theWindowFollowsThePointerAgainAfterTheGrabIsTakenAnew() {
        WindowDrag taken = WindowDrag.grabbedAt(new Point(1200, 700), new Rectangle(900, 600, 637, 173));

        Point location = taken.locationFor(new Point(1260, 740));

        assertThat(location).isEqualTo(new Point(960, 640));
        assertThat(new Point(1260 - location.x, 740 - location.y)).isEqualTo(taken.grabbedPoint());
    }

    @Test
    void aWindowWithNoExtentStillGivesAGrabPointInside() {
        WindowDrag drag = WindowDrag.grabbedAt(new Point(100, 50), new Rectangle(100, 50, 0, 0));

        assertThat(drag.grabbedPoint()).isEqualTo(new Point(0, 0));
    }
}
