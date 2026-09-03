package io.github.hideyukimori.neneclock.ui.swing;

import io.github.hideyukimori.neneclock.domain.RgbColor;
import java.awt.Color;

/**
 * 画面の色。時計の背景色の明るさに追従して、明るい面と暗い面を切り替える。
 *
 * <p>利用者が選ぶのは時計の 2 色だけである（FR-044 / FR-046）。設定モーダルの色は
 * そこから決まる従属的な値であって、独立した設定ではない。だから「設定」ではなくここに置く。
 */
public final class Palette {

    /** 明暗を分ける知覚輝度のしきい値。 */
    private static final double LIGHT_THRESHOLD = 0.55;

    private static final double RED_WEIGHT = 0.299;
    private static final double GREEN_WEIGHT = 0.587;
    private static final double BLUE_WEIGHT = 0.114;
    private static final double FULL_COMPONENT = 255.0;

    private static final Color LIGHT_SURFACE = new Color(0xFBF9F4);
    private static final Color DARK_SURFACE = new Color(0x201E1C);
    private static final Color LIGHT_SUNKEN = new Color(0xF5F2EB);
    private static final Color DARK_SUNKEN = new Color(0x1A1917);
    private static final Color LIGHT_HAIRLINE = new Color(27, 25, 23, 24);
    private static final Color DARK_HAIRLINE = new Color(239, 234, 225, 22);
    private static final Color LIGHT_WASH = new Color(27, 25, 23, 16);
    private static final Color DARK_WASH = new Color(239, 234, 225, 16);
    private static final Color LIGHT_TEXT = new Color(0x1B1917);
    private static final Color DARK_TEXT = new Color(0xEFEAE1);
    private static final Color LIGHT_MUTED = new Color(0x857E76);
    private static final Color DARK_MUTED = new Color(0x8E877D);
    private static final Color LIGHT_FAINT = new Color(0x928B82);
    private static final Color DARK_FAINT = new Color(0x746D64);
    private static final Color LIGHT_ACCENT = new Color(0xB4762C);
    private static final Color DARK_ACCENT = new Color(0xD08C3F);
    private static final Color LIGHT_WARNING = new Color(0xB8462F);
    private static final Color DARK_WARNING = new Color(0xC8703A);

    private final boolean light;

    private Palette(boolean light) {
        this.light = light;
    }

    /** 時計の背景色から決める。ここが唯一の生成経路。 */
    public static Palette from(RgbColor background) {
        return new Palette(luminanceOf(background) >= LIGHT_THRESHOLD);
    }

    /** 面の色（モーダルの地）。 */
    public Color surface() {
        return light ? LIGHT_SURFACE : DARK_SURFACE;
    }

    /** 面より一段沈んだ色（見本の枠の地など）。 */
    public Color surfaceSunken() {
        return light ? LIGHT_SUNKEN : DARK_SUNKEN;
    }

    /** 罫線。1px のヘアラインだけに使う。 */
    public Color hairline() {
        return light ? LIGHT_HAIRLINE : DARK_HAIRLINE;
    }

    /** 押せる面の弱い塗り。 */
    public Color wash() {
        return light ? LIGHT_WASH : DARK_WASH;
    }

    /** 主たる文字色。 */
    public Color text() {
        return light ? LIGHT_TEXT : DARK_TEXT;
    }

    /** 補助の文字色。 */
    public Color textMuted() {
        return light ? LIGHT_MUTED : DARK_MUTED;
    }

    /** 見出し・分類名の色。 */
    public Color textFaint() {
        return light ? LIGHT_FAINT : DARK_FAINT;
    }

    /** 選択・強調。1 色だけ持つ。 */
    public Color accent() {
        return light ? LIGHT_ACCENT : DARK_ACCENT;
    }

    /** 反転した面（選ばれている分節の地）。 */
    public Color inverted() {
        return light ? LIGHT_TEXT : DARK_TEXT;
    }

    /** 反転した面の上の文字。 */
    public Color onInverted() {
        return light ? LIGHT_SURFACE : DARK_SUNKEN;
    }

    /** 警告。読めない配色や保存の失敗に使う。 */
    public Color warning() {
        return light ? LIGHT_WARNING : DARK_WARNING;
    }

    /** 明るい面かどうか。 */
    public boolean isLight() {
        return light;
    }

    private static double luminanceOf(RgbColor colour) {
        return (RED_WEIGHT * colour.red() + GREEN_WEIGHT * colour.green() + BLUE_WEIGHT * colour.blue())
                / FULL_COMPONENT;
    }
}
