package io.github.hideyukimori.neneclock.ui.swing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.awt.Dimension;
import java.awt.Point;
import org.junit.jupiter.api.Test;

/**
 * 画面ごとの座標変換（ADR 0019 / Issue #100）。
 *
 * <p>数値はすべて施主の実機で測った 4 画面のものである（ADR 0019 の表）。原点は実ピクセル、
 * 大きさは論理ピクセルで報告される、という観測された性質をそのまま検算している。
 */
class ScreenSpaceTest {

    /** 主（3840x2160 @125%・Java は (0,0) 3072x1728 と報告する）。 */
    private static final ScreenSpace PRIMARY = ScreenSpace.of(0, 0, 1.25, 1.25);

    /** 左（3840x2160 @150%・Java は (-3840,0) 2560x1440）。 */
    private static final ScreenSpace LEFT = ScreenSpace.of(-3840, 0, 1.5, 1.5);

    /** 右（3840x2160 @175%・Java は (3840,0) 2194x1234）。 */
    private static final ScreenSpace RIGHT = ScreenSpace.of(3840, 0, 1.75, 1.75);

    /** 右上（2560x1440 @150%・Java は (3818,-1440) 1707x960）。 */
    private static final ScreenSpace TOP_RIGHT = ScreenSpace.of(3818, -1440, 1.5, 1.5);

    @Test
    void theWindowMeasurementFromTheRealMachineIsReproduced() {
        // ADR 0019: 窓 Java -290 → 0 + (-290)x1.25 = -362
        assertThat(PRIMARY.toDevice(new Point(-290, 0)).x).isEqualTo(-362);
    }

    @Test
    void thePointerMeasurementFromTheRealMachineIsReproduced() {
        // ADR 0019: ポインタ Java -1284 → -3840 + (2556)x1.5 = -6
        assertThat(LEFT.toDevice(new Point(-1284, 1123))).isEqualTo(new Point(-6, 1685));
    }

    @Test
    void theOriginOfAScreenIsTheSamePointInBothUnits() {
        assertThat(RIGHT.toDevice(new Point(3840, 0))).isEqualTo(new Point(3840, 0));
        assertThat(TOP_RIGHT.toDevice(new Point(3818, -1440))).isEqualTo(new Point(3818, -1440));
    }

    @Test
    void aNegativeOriginDoesNotBreakTheRoundTrip() {
        Point javaPoint = new Point(4200, -900);

        Point device = TOP_RIGHT.toDevice(javaPoint);

        assertThat(device).isEqualTo(new Point(3818 + 573, -1440 + 810));
        assertThat(TOP_RIGHT.toJava(device)).isEqualTo(javaPoint);
    }

    @Test
    void theExtentOfAScreenIsLogicalSoItScalesWithoutTheOrigin() {
        assertThat(LEFT.toDeviceSize(new Dimension(765, 208))).isEqualTo(new Dimension(1148, 312));
        assertThat(RIGHT.toDeviceSize(new Dimension(765, 208))).isEqualTo(new Dimension(1339, 364));
        assertThat(PRIMARY.toDeviceSize(new Dimension(765, 208))).isEqualTo(new Dimension(956, 260));
    }

    @Test
    void aScreenWithoutScalingIsTheIdentity() {
        ScreenSpace plain = ScreenSpace.of(0, 0, 1.0, 1.0);

        assertThat(plain.toDevice(new Point(1234, -567))).isEqualTo(new Point(1234, -567));
        assertThat(plain.toJava(new Point(1234, -567))).isEqualTo(new Point(1234, -567));
        assertThat(plain.toDeviceSize(new Dimension(765, 208))).isEqualTo(new Dimension(765, 208));
    }

    /**
     * 実機が測った跳びは、2 つの写像の差そのものである（Issue #100・harness の step50）。
     *
     * <p>狙った実ピクセルは -600 だった。窓の GC（主）で逆変換して -480 を渡したところ、窓は
     * 実ピクセル +1200 に着いた。+1200 は<b>左画面の写像を -480 に掛けた値</b>である。
     * ⇒ ピアは既に左画面の写像を使っていて、{@code getGraphicsConfiguration()} だけが古かった。
     */
    @Test
    void theJumpTheHarnessMeasuredIsExactlyTheDifferenceBetweenTwoMappings() {
        assertThat(PRIMARY.toJava(new Point(-600, 0)).x).isEqualTo(-480);
        assertThat(LEFT.toDevice(new Point(-480, 0)).x).isEqualTo(1200);

        assertThat(LEFT.toJava(new Point(-600, 0)).x).isEqualTo(-1680);
        assertThat(LEFT.toDevice(new Point(-1680, 0)).x).isEqualTo(-600);
    }

    @Test
    void aScaleThatCannotBeInvertedIsRefused() {
        assertThatIllegalArgumentException().isThrownBy(() -> ScreenSpace.of(0, 0, 0.0, 1.5));
        assertThatIllegalArgumentException().isThrownBy(() -> ScreenSpace.of(0, 0, 1.5, Double.NaN));
    }
}
