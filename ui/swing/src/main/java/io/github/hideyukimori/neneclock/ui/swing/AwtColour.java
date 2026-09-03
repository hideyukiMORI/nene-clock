package io.github.hideyukimori.neneclock.ui.swing;

import io.github.hideyukimori.neneclock.domain.RgbaColor;
import java.awt.Color;
import java.util.Objects;

/**
 * domain の色を描画の色にする唯一の場所。
 *
 * <p>変換が散らばると、透明度を落とす箇所が必ず出る。実際に、透明度を足す前は
 * 4 か所で {@code new Color(r, g, b)} を書いていた（FR-044 / FR-046）。
 */
public final class AwtColour {

    private AwtColour() {}

    /** 透明度も含めて描画の色にする。 */
    public static Color of(RgbaColor colour) {
        Objects.requireNonNull(colour, "colour");
        return new Color(colour.red(), colour.green(), colour.blue(), colour.alpha());
    }

    /** 透明度を捨てて描画の色にする。半透明で描けない環境と、見本の地に使う。 */
    public static Color opaque(RgbaColor colour) {
        Objects.requireNonNull(colour, "colour");
        return new Color(colour.red(), colour.green(), colour.blue());
    }
}
