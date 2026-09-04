package io.github.hideyukimori.neneclock.ui.swing;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.hideyukimori.neneclock.domain.RgbColor;
import io.github.hideyukimori.neneclock.domain.RgbColorOutcome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * 座標系（HSB）を通しても色が壊れないこと。
 *
 * <p>🔑 ピッカーは HSB の上を動くが、保存されるのは {@link RgbColor} である（FR-044 / FR-046）。
 * 往復で値がずれると「掴んでいないのに色が変わる」ことになるので、そこを見る。
 */
class HsbCoordinateTest {

    @ParameterizedTest
    @CsvSource({
        "0, 0, 0", // 黒（成分の下限）
        "255, 255, 255", // 白（成分の上限）
        "255, 0, 0", // 原色（色相 0 の境界）
        "0, 255, 0",
        "0, 0, 255",
        "0, 255, 255", // 補色側
        "128, 128, 128", // 無彩色
        "1, 1, 1", // 下限のすぐ上
        "254, 255, 253", // 上限のすぐ下
        "245, 242, 235", // 既定の背景色
        "180, 118, 44" // 強調色
    })
    void colourSurvivesTheRoundTrip(int red, int green, int blue) {
        RgbColor colour = colour(red, green, blue);

        RgbColor returned = HsbCoordinate.of(colour, 0f).toColour();

        assertThat(returned).isEqualTo(colour);
    }

    @Test
    void greyKeepsTheHueItCameWith() {
        HsbCoordinate grey = HsbCoordinate.of(colour(128, 128, 128), 0.5f);

        assertThat(grey.hue()).isEqualTo(0.5f);
        assertThat(grey.saturation()).isZero();
    }

    @Test
    void blackKeepsTheHueItCameWith() {
        HsbCoordinate black = HsbCoordinate.of(RgbColor.DEFAULT_FONT, 0.75f);

        assertThat(black.hue()).isEqualTo(0.75f);
        assertThat(black.brightness()).isZero();
    }

    @Test
    void whiteKeepsTheHueItCameWith() {
        HsbCoordinate white = HsbCoordinate.of(colour(255, 255, 255), 0.25f);

        assertThat(white.hue()).isEqualTo(0.25f);
        assertThat(white.brightness()).isEqualTo(1f);
    }

    @Test
    void componentsOutsideTheRangeAreFoldedToTheEdge() {
        HsbCoordinate below = HsbCoordinate.at(-2f, -0.5f, -1f);
        HsbCoordinate above = HsbCoordinate.at(2f, 1.5f, 9f);

        assertThat(below.hue()).isZero();
        assertThat(below.saturation()).isZero();
        assertThat(below.brightness()).isZero();
        assertThat(above.hue()).isEqualTo(1f);
        assertThat(above.saturation()).isEqualTo(1f);
        assertThat(above.brightness()).isEqualTo(1f);
    }

    @Test
    void zeroBrightnessIsBlackWhateverTheHue() {
        assertThat(HsbCoordinate.at(0.4f, 1f, 0f).toColour()).isEqualTo(RgbColor.DEFAULT_FONT);
    }

    @Test
    void zeroSaturationIsGreyWhateverTheHue() {
        assertThat(HsbCoordinate.at(0.4f, 0f, 1f).toColour()).isEqualTo(colour(255, 255, 255));
    }

    @Test
    void movingOnThePlaneKeepsTheHue() {
        HsbCoordinate red = HsbCoordinate.of(colour(255, 0, 0), 0f);

        HsbCoordinate dimmed = red.withPlane(1f, 0.5f);

        assertThat(dimmed.hue()).isEqualTo(red.hue());
        assertThat(dimmed.toColour().green()).isZero();
        assertThat(dimmed.toColour().blue()).isZero();
    }

    @Test
    void movingOnTheBandKeepsThePlace() {
        HsbCoordinate pale = HsbCoordinate.at(0f, 0.3f, 0.8f);

        HsbCoordinate turned = pale.withHue(0.6f);

        assertThat(turned.saturation()).isEqualTo(pale.saturation());
        assertThat(turned.brightness()).isEqualTo(pale.brightness());
        assertThat(turned.hue()).isEqualTo(0.6f);
    }

    private static RgbColor colour(int red, int green, int blue) {
        return switch (RgbColor.of(red, green, blue)) {
            case RgbColorOutcome.Accepted accepted -> accepted.value();
            case RgbColorOutcome.Rejected outOfRange -> throw new IllegalArgumentException("範囲外: " + outOfRange);
        };
    }
}
