package io.github.hideyukimori.neneclock.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RgbaColorTest {

    @Test
    void theTwoDefaultsAreReadableAgainstEachOther() {
        // 既定のまま起動して時刻が読めないことが起きないようにする（FR-046）。
        assertThat(RgbaColor.DEFAULT_FONT).isNotEqualTo(RgbaColor.DEFAULT_BACKGROUND);
        assertThat(RgbaColor.DEFAULT_FONT.red() + RgbaColor.DEFAULT_FONT.green() + RgbaColor.DEFAULT_FONT.blue())
                .isLessThan(RgbaColor.DEFAULT_BACKGROUND.red()
                        + RgbaColor.DEFAULT_BACKGROUND.green()
                        + RgbaColor.DEFAULT_BACKGROUND.blue());
    }

    private static final RgbaColorOutcome.Rejected OUT_OF_RANGE =
            new RgbaColorOutcome.Rejected(RgbaColorRejection.COMPONENT_OUT_OF_RANGE);

    @Test
    void acceptsComponentsInsideTheAllowedRange() {
        RgbaColorOutcome outcome = RgbaColor.opaque(10, 20, 30);

        assertThat(outcome).isInstanceOf(RgbaColorOutcome.Accepted.class);
        RgbaColor color = ((RgbaColorOutcome.Accepted) outcome).value();
        assertThat(color.red()).isEqualTo(10);
        assertThat(color.green()).isEqualTo(20);
        assertThat(color.blue()).isEqualTo(30);
    }

    @Test
    void acceptsBothBounds() {
        int low = RgbaColor.MINIMUM_COMPONENT;
        int high = RgbaColor.MAXIMUM_COMPONENT;

        assertThat(RgbaColor.opaque(low, low, low)).isInstanceOf(RgbaColorOutcome.Accepted.class);
        assertThat(RgbaColor.opaque(high, high, high)).isInstanceOf(RgbaColorOutcome.Accepted.class);
    }

    @Test
    void rejectsARedComponentBelowTheMinimum() {
        assertThat(RgbaColor.opaque(RgbaColor.MINIMUM_COMPONENT - 1, 0, 0)).isEqualTo(OUT_OF_RANGE);
    }

    @Test
    void rejectsAGreenComponentAboveTheMaximum() {
        assertThat(RgbaColor.opaque(0, RgbaColor.MAXIMUM_COMPONENT + 1, 0)).isEqualTo(OUT_OF_RANGE);
    }

    @Test
    void rejectsABlueComponentAboveTheMaximum() {
        assertThat(RgbaColor.opaque(0, 0, RgbaColor.MAXIMUM_COMPONENT + 1)).isEqualTo(OUT_OF_RANGE);
    }

    @Test
    void theDefaultIsAcceptedByItsOwnFactory() {
        // RgbaColor.DEFAULT_FONT は非公開コンストラクタで直接組み立てられている。
        // 既定値が自分の検証を通ることを保証しているのはこのテストである（JAV-007 の補完）。
        assertThat(RgbaColor.opaque(
                        RgbaColor.DEFAULT_FONT.red(), RgbaColor.DEFAULT_FONT.green(), RgbaColor.DEFAULT_FONT.blue()))
                .isInstanceOf(RgbaColorOutcome.Accepted.class);
    }

    @Test
    void defaultsToOpaque() {
        assertThat(RgbaColor.DEFAULT_FONT.alpha()).isEqualTo(RgbaColor.OPAQUE);
        assertThat(RgbaColor.DEFAULT_BACKGROUND.alpha()).isEqualTo(RgbaColor.OPAQUE);
    }

    @Test
    void refusesAnAlphaOutsideTheAllowedRange() {
        assertThat(RgbaColor.of(1, 2, 3, RgbaColor.MAXIMUM_COMPONENT + 1)).isEqualTo(OUT_OF_RANGE);
        assertThat(RgbaColor.of(1, 2, 3, RgbaColor.MINIMUM_COMPONENT - 1)).isEqualTo(OUT_OF_RANGE);
    }

    @Test
    void keepsTheOtherComponentsWhenTheAlphaChanges() {
        RgbaColor opaque = ((RgbaColorOutcome.Accepted) RgbaColor.opaque(1, 2, 3)).value();

        RgbaColor faded = opaque.withAlpha(128);

        assertThat(faded.red()).isEqualTo(1);
        assertThat(faded.green()).isEqualTo(2);
        assertThat(faded.blue()).isEqualTo(3);
        assertThat(faded.alpha()).isEqualTo(128);
        assertThat(faded.asOpaque()).isEqualTo(opaque);
    }

    @Test
    void doesNotChangeTheAlphaWhenTheNewOneIsOutOfRange() {
        // 値を勝手に丸めない。範囲外なら何も起きない。
        RgbaColor opaque = ((RgbaColorOutcome.Accepted) RgbaColor.opaque(1, 2, 3)).value();

        assertThat(opaque.withAlpha(-1)).isEqualTo(opaque);
    }

    @Test
    void tellsTwoColoursApartByTheirAlpha() {
        RgbaColor opaque = ((RgbaColorOutcome.Accepted) RgbaColor.opaque(1, 2, 3)).value();

        assertThat(opaque).isNotEqualTo(opaque.withAlpha(128));
        assertThat(opaque.hashCode()).isNotEqualTo(opaque.withAlpha(128).hashCode());
    }

    @Test
    void comparesByValue() {
        RgbaColorOutcome first = RgbaColor.opaque(1, 2, 3);

        assertThat(first).isEqualTo(RgbaColor.opaque(1, 2, 3));
        assertThat(first.hashCode()).isEqualTo(RgbaColor.opaque(1, 2, 3).hashCode());
        assertThat(first).isNotEqualTo(RgbaColor.opaque(1, 2, 4));
        assertThat(first).isNotEqualTo(RgbaColor.opaque(1, 9, 3));
        assertThat(first).isNotEqualTo(RgbaColor.opaque(9, 2, 3));
        assertThat(RgbaColor.DEFAULT_FONT).isNotEqualTo("black");
        assertThat(RgbaColor.DEFAULT_FONT.toString()).contains("0,0,0");
    }
}
