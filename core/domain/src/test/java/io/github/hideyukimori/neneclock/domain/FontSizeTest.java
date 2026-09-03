package io.github.hideyukimori.neneclock.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FontSizeTest {

    @Test
    void acceptsAValueInsideTheAllowedRange() {
        FontSizeOutcome outcome = FontSize.of(FontSize.MINIMUM_POINTS);

        assertThat(outcome).isInstanceOf(FontSizeOutcome.Accepted.class);
        assertThat(((FontSizeOutcome.Accepted) outcome).value().points()).isEqualTo(FontSize.MINIMUM_POINTS);
    }

    @Test
    void acceptsTheUpperBound() {
        FontSizeOutcome outcome = FontSize.of(FontSize.MAXIMUM_POINTS);

        assertThat(outcome).isInstanceOf(FontSizeOutcome.Accepted.class);
    }

    @Test
    void rejectsAValueBelowTheMinimum() {
        FontSizeOutcome outcome = FontSize.of(FontSize.MINIMUM_POINTS - 1);

        assertThat(outcome).isEqualTo(new FontSizeOutcome.Rejected(FontSizeRejection.BELOW_MINIMUM));
    }

    @Test
    void rejectsAValueAboveTheMaximum() {
        FontSizeOutcome outcome = FontSize.of(FontSize.MAXIMUM_POINTS + 1);

        assertThat(outcome).isEqualTo(new FontSizeOutcome.Rejected(FontSizeRejection.ABOVE_MAXIMUM));
    }

    @Test
    void theDefaultIsInsideItsOwnRange() {
        // FontSize.DEFAULT は非公開コンストラクタで直接組み立てられている。
        // 範囲内であることを保証しているのはこのテストである（JAV-007 の補完）。
        assertThat(FontSize.of(FontSize.DEFAULT.points())).isInstanceOf(FontSizeOutcome.Accepted.class);
        assertThat(FontSize.DEFAULT.points()).isBetween(FontSize.MINIMUM_POINTS, FontSize.MAXIMUM_POINTS);
    }

    @Test
    void comparesByValue() {
        FontSizeOutcome first = FontSize.of(FontSize.MINIMUM_POINTS);
        FontSizeOutcome second = FontSize.of(FontSize.MINIMUM_POINTS);

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
        assertThat(first).isNotEqualTo(FontSize.of(FontSize.MAXIMUM_POINTS));
        assertThat(FontSize.DEFAULT).isNotEqualTo(FontSize.MINIMUM_POINTS);
        assertThat(FontSize.DEFAULT.toString()).contains(String.valueOf(FontSize.DEFAULT.points()));
    }
}
