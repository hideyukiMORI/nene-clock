package io.github.hideyukimori.neneclock.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class WindowPaddingTest {

    @Test
    void acceptsAValueInsideTheAllowedRange() {
        WindowPaddingOutcome outcome = WindowPadding.of(WindowPadding.MINIMUM_PIXELS);

        assertThat(outcome).isInstanceOf(WindowPaddingOutcome.Accepted.class);
        assertThat(((WindowPaddingOutcome.Accepted) outcome).value().pixels()).isEqualTo(WindowPadding.MINIMUM_PIXELS);
    }

    @Test
    void acceptsTheUpperBound() {
        WindowPaddingOutcome outcome = WindowPadding.of(WindowPadding.MAXIMUM_PIXELS);

        assertThat(outcome).isInstanceOf(WindowPaddingOutcome.Accepted.class);
    }

    @Test
    void rejectsAValueBelowTheMinimum() {
        WindowPaddingOutcome outcome = WindowPadding.of(WindowPadding.MINIMUM_PIXELS - 1);

        assertThat(outcome).isEqualTo(new WindowPaddingOutcome.Rejected(WindowPaddingRejection.BELOW_MINIMUM));
    }

    @Test
    void rejectsAValueAboveTheMaximum() {
        WindowPaddingOutcome outcome = WindowPadding.of(WindowPadding.MAXIMUM_PIXELS + 1);

        assertThat(outcome).isEqualTo(new WindowPaddingOutcome.Rejected(WindowPaddingRejection.ABOVE_MAXIMUM));
    }

    @Test
    void theDefaultIsInsideItsOwnRange() {
        // WindowPadding.DEFAULT は非公開コンストラクタで直接組み立てられている。
        // 範囲内であることを保証しているのはこのテストである（JAV-007 の補完）。
        assertThat(WindowPadding.of(WindowPadding.DEFAULT.pixels())).isInstanceOf(WindowPaddingOutcome.Accepted.class);
        assertThat(WindowPadding.DEFAULT.pixels())
                .isBetween(WindowPadding.MINIMUM_PIXELS, WindowPadding.MAXIMUM_PIXELS);
    }

    @Test
    void comparesByValue() {
        WindowPaddingOutcome first = WindowPadding.of(WindowPadding.MINIMUM_PIXELS);
        WindowPaddingOutcome second = WindowPadding.of(WindowPadding.MINIMUM_PIXELS);

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
        assertThat(first).isNotEqualTo(WindowPadding.of(WindowPadding.MAXIMUM_PIXELS));
        assertThat(WindowPadding.DEFAULT).isNotEqualTo(WindowPadding.MINIMUM_PIXELS);
        assertThat(WindowPadding.DEFAULT.toString()).contains(String.valueOf(WindowPadding.DEFAULT.pixels()));
    }
}
