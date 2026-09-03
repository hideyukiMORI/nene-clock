package io.github.hideyukimori.neneclock.ui.swing;

import io.github.hideyukimori.neneclock.domain.RgbColor;
import java.awt.Color;
import java.util.Objects;

/**
 * domain の色を描画の色にする唯一の場所。
 *
 * <p>変換が散らばると、同じ変換が少しずつ違う形で 4 か所に写る。実際にそうなっていた
 * （FR-044 / FR-046）。
 */
public final class AwtColour {

    private AwtColour() {}

    /** 描画の色にする。 */
    public static Color of(RgbColor colour) {
        Objects.requireNonNull(colour, "colour");
        return new Color(colour.red(), colour.green(), colour.blue());
    }
}
