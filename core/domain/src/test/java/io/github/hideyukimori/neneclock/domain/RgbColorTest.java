package io.github.hideyukimori.neneclock.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RgbColorTest {

    @Test
    void theTwoDefaultsAreReadableAgainstEachOther() {
        // 既定のまま起動して時刻が読めないことが起きないようにする（FR-046）。
        assertThat(RgbColor.DEFAULT_FONT).isNotEqualTo(RgbColor.DEFAULT_BACKGROUND);
        assertThat(RgbColor.DEFAULT_FONT.red() + RgbColor.DEFAULT_FONT.green() + RgbColor.DEFAULT_FONT.blue())
                .isLessThan(RgbColor.DEFAULT_BACKGROUND.red()
                        + RgbColor.DEFAULT_BACKGROUND.green()
                        + RgbColor.DEFAULT_BACKGROUND.blue());
    }

    private static final RgbColorOutcome.Rejected OUT_OF_RANGE =
            new RgbColorOutcome.Rejected(RgbColorRejection.COMPONENT_OUT_OF_RANGE);

    @Test
    void acceptsComponentsInsideTheAllowedRange() {
        RgbColorOutcome outcome = RgbColor.of(10, 20, 30);

        assertThat(outcome).isInstanceOf(RgbColorOutcome.Accepted.class);
        RgbColor color = ((RgbColorOutcome.Accepted) outcome).value();
        assertThat(color.red()).isEqualTo(10);
        assertThat(color.green()).isEqualTo(20);
        assertThat(color.blue()).isEqualTo(30);
    }

    @Test
    void acceptsBothBounds() {
        int low = RgbColor.MINIMUM_COMPONENT;
        int high = RgbColor.MAXIMUM_COMPONENT;

        assertThat(RgbColor.of(low, low, low)).isInstanceOf(RgbColorOutcome.Accepted.class);
        assertThat(RgbColor.of(high, high, high)).isInstanceOf(RgbColorOutcome.Accepted.class);
    }

    @Test
    void rejectsARedComponentBelowTheMinimum() {
        assertThat(RgbColor.of(RgbColor.MINIMUM_COMPONENT - 1, 0, 0)).isEqualTo(OUT_OF_RANGE);
    }

    @Test
    void rejectsAGreenComponentAboveTheMaximum() {
        assertThat(RgbColor.of(0, RgbColor.MAXIMUM_COMPONENT + 1, 0)).isEqualTo(OUT_OF_RANGE);
    }

    @Test
    void rejectsABlueComponentAboveTheMaximum() {
        assertThat(RgbColor.of(0, 0, RgbColor.MAXIMUM_COMPONENT + 1)).isEqualTo(OUT_OF_RANGE);
    }

    @Test
    void theDefaultIsAcceptedByItsOwnFactory() {
        // RgbColor.DEFAULT_FONT は非公開コンストラクタで直接組み立てられている。
        // 既定値が自分の検証を通ることを保証しているのはこのテストである（JAV-007 の補完）。
        assertThat(RgbColor.of(
                        RgbColor.DEFAULT_FONT.red(), RgbColor.DEFAULT_FONT.green(), RgbColor.DEFAULT_FONT.blue()))
                .isInstanceOf(RgbColorOutcome.Accepted.class);
    }

    @Test
    void comparesByValue() {
        RgbColorOutcome first = RgbColor.of(1, 2, 3);

        assertThat(first).isEqualTo(RgbColor.of(1, 2, 3));
        assertThat(first.hashCode()).isEqualTo(RgbColor.of(1, 2, 3).hashCode());
        assertThat(first).isNotEqualTo(RgbColor.of(1, 2, 4));
        assertThat(first).isNotEqualTo(RgbColor.of(1, 9, 3));
        assertThat(first).isNotEqualTo(RgbColor.of(9, 2, 3));
        assertThat(RgbColor.DEFAULT_FONT).isNotEqualTo("black");
        assertThat(RgbColor.DEFAULT_FONT.toString()).contains("0,0,0");
    }
}
