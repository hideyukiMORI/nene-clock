package io.github.hideyukimori.neneclock.ui.swing;

import io.github.hideyukimori.neneclock.domain.RgbColor;
import io.github.hideyukimori.neneclock.domain.RgbColorOutcome;
import java.awt.Color;
import java.util.Objects;

/**
 * 色ピッカーの座標系（FR-045）。色相・彩度・明度を 0..1 で持つ。
 *
 * <p>🔑 HSB は **UI の座標系であって domain の値ではない。** 面の上の位置を色へ翻訳するためだけに要る。
 * 保存されるのも、外へ出ていくのも常に {@link RgbColor} である（FR-044 / FR-046 / ADR 0007）。
 * だからこの型は domain ではなく ui の側にあり、保存形式（v7）は何も変わらない。
 *
 * <p>🔴 **無彩色には色相が無い。** {@code Color.RGBtoHSB} は灰色に対して色相 0（＝赤）を返すが、
 * それは「赤である」ではなく「決まらない」という意味である。そのまま採ると、白や黒を選んだ瞬間に
 * つまみが赤へ飛ぶ。だから決まらないときは**呼ぶ側が持っている直前の色相を保つ**。
 */
public final class HsbCoordinate {

    /** 成分の下限。 */
    private static final float MINIMUM = 0f;

    /** 成分の上限。 */
    private static final float MAXIMUM = 1f;

    private static final int RED_SHIFT = 16;
    private static final int GREEN_SHIFT = 8;
    private static final int BYTE_MASK = 0xFF;

    /** {@code Color.RGBtoHSB} が返す配列の並び。 */
    private static final int HUE_INDEX = 0;

    private static final int SATURATION_INDEX = 1;
    private static final int BRIGHTNESS_INDEX = 2;

    private final float hue;
    private final float saturation;
    private final float brightness;

    private HsbCoordinate(float hue, float saturation, float brightness) {
        this.hue = hue;
        this.saturation = saturation;
        this.brightness = brightness;
    }

    /** 3 成分から作る。範囲外は端へ丸める。ここが唯一の生成経路（JAV-007）。 */
    public static HsbCoordinate at(float hue, float saturation, float brightness) {
        return new HsbCoordinate(clamped(hue), clamped(saturation), clamped(brightness));
    }

    /** 色から座標を求める。色相が決まらない（＝無彩色）ときは {@code whenUndefined} をそのまま保つ。 */
    public static HsbCoordinate of(RgbColor colour, float whenUndefined) {
        Objects.requireNonNull(colour, "colour");
        float[] components = Color.RGBtoHSB(colour.red(), colour.green(), colour.blue(), null);
        boolean undefined = components[SATURATION_INDEX] <= MINIMUM;
        return at(
                undefined ? whenUndefined : components[HUE_INDEX],
                components[SATURATION_INDEX],
                components[BRIGHTNESS_INDEX]);
    }

    /** 色相（0..1）。帯の上の位置。 */
    public float hue() {
        return hue;
    }

    /** 彩度（0..1）。面の横の位置。 */
    public float saturation() {
        return saturation;
    }

    /** 明度（0..1）。面の縦の位置（上が明るい）。 */
    public float brightness() {
        return brightness;
    }

    /** 色相だけを差し替える。帯を掴んだときに使う。 */
    public HsbCoordinate withHue(float value) {
        return at(value, saturation, brightness);
    }

    /** 面の上の位置だけを差し替える。色相は保つ。 */
    public HsbCoordinate withPlane(float newSaturation, float newBrightness) {
        return at(hue, newSaturation, newBrightness);
    }

    /**
     * 保存できる色にする。
     *
     * <p>{@code HSBtoRGB} は 8bit へ丸めた 3 成分を返すので、{@link RgbColor} の不変条件
     * （0..255）は構造的に満たされる。拒否の枝はそのことの表明であって、実行経路ではない。
     */
    public RgbColor toColour() {
        int packed = Color.HSBtoRGB(hue, saturation, brightness);
        return switch (RgbColor.of(
                (packed >> RED_SHIFT) & BYTE_MASK, (packed >> GREEN_SHIFT) & BYTE_MASK, packed & BYTE_MASK)) {
            case RgbColorOutcome.Accepted accepted -> accepted.value();
            case RgbColorOutcome.Rejected outOfRange -> RgbColor.DEFAULT_FONT;
        };
    }

    private static float clamped(float value) {
        return Math.min(MAXIMUM, Math.max(MINIMUM, value));
    }
}
