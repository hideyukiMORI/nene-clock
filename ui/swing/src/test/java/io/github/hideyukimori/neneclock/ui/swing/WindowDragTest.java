package io.github.hideyukimori.neneclock.ui.swing;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import org.junit.jupiter.api.Test;

/**
 * 画面をまたぐドラッグの座標計算（ADR 0019 / Issue #100）。
 *
 * <p>画面は施主の実機で測った 4 面をそのまま値にしてある。**ピアの変換も同じ規則で書き下して
 * いる**ので、「setLocation に渡した値が、変換されたあとに狙った実ピクセルになる」ことを
 * headless で確かめられる（{@link #wherePeerPutsIt}）。
 *
 * <p>🔴 ここで見られるのは算術だけである。実機の per-monitor DPI の挙動そのものは、この環境
 * （WSLg・画面 1 枚・拡大率 1 倍）では一度も再現できない（QLT-012）。記録は
 * quality/gate-proofs.md 第 24 節。
 */
class WindowDragTest {

    private static final ScreenSpace PRIMARY = ScreenSpace.of(0, 0, 1.25, 1.25);
    private static final ScreenSpace LEFT = ScreenSpace.of(-3840, 0, 1.5, 1.5);
    private static final ScreenSpace RIGHT = ScreenSpace.of(3840, 0, 1.75, 1.75);
    private static final ScreenSpace TOP_RIGHT = ScreenSpace.of(3818, -1440, 1.5, 1.5);
    private static final ScreenSpace UNSCALED = ScreenSpace.of(0, 0, 1.0, 1.0);

    /** 実機で測った論理サイズ 765x208 の窓が、主画面の (100,500) に居る。 */
    private static final Rectangle WINDOW = new Rectangle(100, 500, 765, 208);

    private static final Dimension WINDOW_SIZE = new Dimension(765, 208);

    /** ピアが setLocation に掛ける変換。窓が載っていると Java が思っている画面で写す。 */
    private static Point wherePeerPutsIt(Point location, ScreenSpace windowScreen) {
        return windowScreen.toDevice(location);
    }

    @Test
    void theGrabbedPointStaysUnderThePointerWhenItCrossesToTheLeftScreen() {
        WindowDrag drag = WindowDrag.grabbedAt(new Point(150, 540), PRIMARY, WINDOW, PRIMARY);
        assertThat(drag.grabbedPoint()).isEqualTo(new Point(63, 50));

        // ポインタは左画面（150%）に居る。窓は主画面（125%）に居ると Java は思ったままである。
        Point location = drag.locationFor(new Point(-1284, 1123), LEFT, WINDOW_SIZE, PRIMARY);

        assertThat(location).isEqualTo(new Point(-55, 1308));
        Point placed = wherePeerPutsIt(location, PRIMARY);
        Point pointerDevice = LEFT.toDevice(new Point(-1284, 1123));
        assertThat(new Point(pointerDevice.x - placed.x, pointerDevice.y - placed.y))
                .isEqualTo(drag.grabbedPoint());
    }

    /**
     * 実機の step50（Issue #100 の harness）をそのまま再現する。
     *
     * <p>掴んだあと、ポインタが左画面（150%）に入り、窓は主画面（125%）に居ると Java が思ったまま。
     * 窓の GC を鍵にすると -480 を渡すことになり、ピアは<b>左画面の変換</b>を掛けて +1200 に置く
     * （実機の実測値）。ポインタの画面を鍵にすると -1689 を渡し、同じピアの変換が狙いどおり
     * -613 に戻す。**同じ算術で、鍵にする画面だけが違う。**
     */
    @Test
    void theHarnessStepFiftyIsReproducedByBothKeyings() {
        WindowDrag drag = WindowDrag.grabbedAt(new Point(150, 540), PRIMARY, WINDOW, PRIMARY);
        Point pointer = new Point(-1638, 1123);

        Point keyedOnTheWindow = drag.locationFor(pointer, LEFT, WINDOW_SIZE, PRIMARY);
        Point keyedOnThePointer = drag.locationFor(pointer, LEFT, WINDOW_SIZE, LEFT);

        assertThat(keyedOnTheWindow.x).isEqualTo(-480);
        assertThat(LEFT.toDevice(keyedOnTheWindow).x).isEqualTo(1200);
        assertThat(keyedOnThePointer.x).isEqualTo(-1689);
        assertThat(LEFT.toDevice(keyedOnThePointer).x).isEqualTo(-613);
    }

    @Test
    void keyingOnThePointerPutsTheGrabUnderThePointerWhenThePeerUsesThePointersScreen() {
        WindowDrag drag = WindowDrag.grabbedAt(new Point(150, 540), PRIMARY, WINDOW, PRIMARY);
        Point pointer = new Point(-1638, 1123);

        Point placed = wherePeerPutsIt(drag.locationFor(pointer, LEFT, WINDOW_SIZE, LEFT), LEFT);

        Point pointerDevice = LEFT.toDevice(pointer);
        Point grab = drag.grabbedPointIn(LEFT.toDeviceSize(WINDOW_SIZE));
        assertThat(new Point(pointerDevice.x - placed.x, pointerDevice.y - placed.y))
                .isEqualTo(grab);
    }

    @Test
    void theGrabbedPointStaysUnderThePointerOnTheRightAndTopScreensToo() {
        WindowDrag drag = WindowDrag.grabbedAt(new Point(150, 540), PRIMARY, WINDOW, PRIMARY);

        assertThatTheGrabIsUnderThePointer(drag, new Point(4100, 700), RIGHT);
        assertThatTheGrabIsUnderThePointer(drag, new Point(4200, -900), TOP_RIGHT);
    }

    private void assertThatTheGrabIsUnderThePointer(WindowDrag drag, Point pointer, ScreenSpace pointerScreen) {
        Point placed = wherePeerPutsIt(drag.locationFor(pointer, pointerScreen, WINDOW_SIZE, PRIMARY), PRIMARY);
        Point pointerDevice = pointerScreen.toDevice(pointer);
        Point grab = drag.grabbedPointIn(PRIMARY.toDeviceSize(WINDOW_SIZE));

        assertThat(pointerDevice.x - placed.x - grab.x).isBetween(-1, 1);
        assertThat(pointerDevice.y - placed.y - grab.y).isBetween(-1, 1);
    }

    @Test
    void draggingInsideOneUnscaledScreenIsExactlyThePointerMinusTheGrab() {
        Rectangle window = new Rectangle(100, 50, 765, 208);
        WindowDrag drag = WindowDrag.grabbedAt(new Point(150, 80), UNSCALED, window, UNSCALED);

        assertThat(drag.grabbedPoint()).isEqualTo(new Point(50, 30));
        assertThat(drag.locationFor(new Point(1500, 900), UNSCALED, window.getSize(), UNSCALED))
                .isEqualTo(new Point(1450, 870));
    }

    @Test
    void draggingInsideOneScaledScreenKeepsTheGrabUnderThePointer() {
        WindowDrag drag = WindowDrag.grabbedAt(new Point(150, 540), PRIMARY, WINDOW, PRIMARY);

        assertThatTheGrabIsUnderThePointer(drag, new Point(900, 1000), PRIMARY);
        assertThatTheGrabIsUnderThePointer(drag, new Point(2400, 300), PRIMARY);
    }

    @Test
    void theGrabbedPointIsRemappedAndStaysInsideAWindowWhoseDeviceSizeChanged() {
        WindowDrag drag = WindowDrag.grabbedAt(new Point(800, 700), PRIMARY, WINDOW, PRIMARY);
        assertThat(drag.sizeWhenGrabbed()).isEqualTo(new Dimension(956, 260));
        assertThat(drag.grabbedPoint()).isEqualTo(new Point(875, 250));

        // 実機で見た膨らみ（765x208 → 918x250 論理）を、窓の画面はそのままに与える。
        Point grown = drag.grabbedPointIn(PRIMARY.toDeviceSize(new Dimension(918, 250)));
        Point shrunk = drag.grabbedPointIn(PRIMARY.toDeviceSize(new Dimension(500, 120)));

        assertThat(grown).isEqualTo(new Point(1051, 301)); // 875 x 1148/956, 250 x 313/260
        assertThat(shrunk).isEqualTo(new Point(572, 144)); // 875 x 625/956, 250 x 150/260
        assertThat(shrunk.x).isLessThan(625);
        assertThat(shrunk.y).isLessThan(150);
    }

    @Test
    void theGrabbedPointNeverLeavesAWindowThatLostAllItsExtent() {
        WindowDrag drag = WindowDrag.grabbedAt(new Point(800, 700), PRIMARY, WINDOW, PRIMARY);

        assertThat(drag.grabbedPointIn(new Dimension(0, 0))).isEqualTo(new Point(0, 0));
    }

    @Test
    void feedingTheSamePointerTwiceGivesTheSameLocation() {
        WindowDrag drag = WindowDrag.grabbedAt(new Point(150, 540), PRIMARY, WINDOW, PRIMARY);
        Point pointer = new Point(-1284, 1123);

        Point first = drag.locationFor(pointer, LEFT, WINDOW_SIZE, PRIMARY);
        Point second = drag.locationFor(pointer, LEFT, WINDOW_SIZE, PRIMARY);

        assertThat(second).isEqualTo(first);
    }

    @Test
    void theLocationIsDecidedByThePointerAloneSoErrorCannotAccumulate() {
        WindowDrag crept = WindowDrag.grabbedAt(new Point(150, 540), PRIMARY, WINDOW, PRIMARY);
        WindowDrag jumped = WindowDrag.grabbedAt(new Point(150, 540), PRIMARY, WINDOW, PRIMARY);
        for (int x = 150; x >= -1284; x -= 3) {
            crept.locationFor(new Point(x, 1123), x >= 0 ? PRIMARY : LEFT, WINDOW_SIZE, PRIMARY);
        }

        assertThat(crept.locationFor(new Point(-1284, 1123), LEFT, WINDOW_SIZE, PRIMARY))
                .isEqualTo(jumped.locationFor(new Point(-1284, 1123), LEFT, WINDOW_SIZE, PRIMARY));
    }

    @Test
    void aPointerOutsideTheWindowIsPulledBackToTheEdge() {
        WindowDrag before = WindowDrag.grabbedAt(new Point(50, 400), PRIMARY, WINDOW, PRIMARY);

        assertThat(before.grabbedPoint()).isEqualTo(new Point(0, 0));
    }

    @Test
    void aWindowWithNoExtentStillGivesAGrabPointInside() {
        WindowDrag drag = WindowDrag.grabbedAt(new Point(100, 500), PRIMARY, new Rectangle(100, 500, 0, 0), PRIMARY);

        assertThat(drag.grabbedPoint()).isEqualTo(new Point(0, 0));
    }
}
