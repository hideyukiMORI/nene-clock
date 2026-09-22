package io.github.hideyukimori.neneclock.ui.swing;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Point;
import org.junit.jupiter.api.Test;

/**
 * 移動量でのドラッグと、写像が切り替わった瞬間の跳びの捨て方（Issue #100 / {@link DragStrategy#DELTA}）。
 */
class PointerStepTest {

    @Test
    void anOrdinaryStepMovesTheWindowByTheSameAmount() {
        PointerStep step = PointerStep.between(new Point(100, 100), new Point(130, 140));

        assertThat(step.isPlausible()).isTrue();
        assertThat(step.appliedTo(new Point(10, 20))).isEqualTo(new Point(40, 60));
    }

    @Test
    void theJumpMeasuredAtAScreenBoundaryIsRefused() {
        // ADR 0019 の実測: 同じ瞬間の 2 つの座標源が 1282px 食い違った。
        PointerStep crossing = PointerStep.between(new Point(1, 1348), new Point(-1284, 1123));

        assertThat(crossing.isPlausible()).isFalse();
    }

    @Test
    void theThresholdIsInclusiveSoAFastButRealDragStillCounts() {
        assertThat(PointerStep.between(new Point(0, 0), new Point(300, -300)).isPlausible())
                .isTrue();
        assertThat(PointerStep.between(new Point(0, 0), new Point(301, 0)).isPlausible())
                .isFalse();
        assertThat(PointerStep.between(new Point(0, 0), new Point(0, -301)).isPlausible())
                .isFalse();
    }

    @Test
    void standingStillIsAStepOfNoSize() {
        PointerStep still = PointerStep.between(new Point(742, 91), new Point(742, 91));

        assertThat(still.isPlausible()).isTrue();
        assertThat(still.appliedTo(new Point(-300, 901))).isEqualTo(new Point(-300, 901));
    }

    @Test
    void aRefusedStepIsNotAppliedByTheCallerSoTheWindowDoesNotFollowTheJump() {
        // 捨てる判断は isPlausible() が持つ。呼び出し側（ClockWindow）は false のとき動かさない。
        PointerStep crossing = PointerStep.between(new Point(1, 1348), new Point(-1284, 1123));

        assertThat(crossing.isPlausible()).isFalse();
        assertThat(crossing.appliedTo(new Point(0, 0))).isEqualTo(new Point(-1285, -225));
    }
}
