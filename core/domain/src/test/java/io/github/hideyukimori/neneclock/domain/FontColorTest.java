package io.github.hideyukimori.neneclock.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FontColorTest {

    private static final FontColorOutcome.Rejected OUT_OF_RANGE =
            new FontColorOutcome.Rejected(FontColorRejection.COMPONENT_OUT_OF_RANGE);

    @Test
    void acceptsComponentsInsideTheAllowedRange() {
        FontColorOutcome outcome = FontColor.of(10, 20, 30);

        assertThat(outcome).isInstanceOf(FontColorOutcome.Accepted.class);
        FontColor color = ((FontColorOutcome.Accepted) outcome).value();
        assertThat(color.red()).isEqualTo(10);
        assertThat(color.green()).isEqualTo(20);
        assertThat(color.blue()).isEqualTo(30);
    }

    @Test
    void acceptsBothBounds() {
        int low = FontColor.MINIMUM_COMPONENT;
        int high = FontColor.MAXIMUM_COMPONENT;

        assertThat(FontColor.of(low, low, low)).isInstanceOf(FontColorOutcome.Accepted.class);
        assertThat(FontColor.of(high, high, high)).isInstanceOf(FontColorOutcome.Accepted.class);
    }

    @Test
    void rejectsARedComponentBelowTheMinimum() {
        assertThat(FontColor.of(FontColor.MINIMUM_COMPONENT - 1, 0, 0)).isEqualTo(OUT_OF_RANGE);
    }

    @Test
    void rejectsAGreenComponentAboveTheMaximum() {
        assertThat(FontColor.of(0, FontColor.MAXIMUM_COMPONENT + 1, 0)).isEqualTo(OUT_OF_RANGE);
    }

    @Test
    void rejectsABlueComponentAboveTheMaximum() {
        assertThat(FontColor.of(0, 0, FontColor.MAXIMUM_COMPONENT + 1)).isEqualTo(OUT_OF_RANGE);
    }

    @Test
    void theDefaultIsAcceptedByItsOwnFactory() {
        // FontColor.DEFAULT は非公開コンストラクタで直接組み立てられている。
        // 既定値が自分の検証を通ることを保証しているのはこのテストである（JAV-007 の補完）。
        assertThat(FontColor.of(FontColor.DEFAULT.red(), FontColor.DEFAULT.green(), FontColor.DEFAULT.blue()))
                .isInstanceOf(FontColorOutcome.Accepted.class);
    }

    @Test
    void comparesByValue() {
        FontColorOutcome first = FontColor.of(1, 2, 3);

        assertThat(first).isEqualTo(FontColor.of(1, 2, 3));
        assertThat(first.hashCode()).isEqualTo(FontColor.of(1, 2, 3).hashCode());
        assertThat(first).isNotEqualTo(FontColor.of(1, 2, 4));
        assertThat(first).isNotEqualTo(FontColor.of(1, 9, 3));
        assertThat(first).isNotEqualTo(FontColor.of(9, 2, 3));
        assertThat(FontColor.DEFAULT).isNotEqualTo("black");
        assertThat(FontColor.DEFAULT.toString()).contains("0,0,0");
    }
}
