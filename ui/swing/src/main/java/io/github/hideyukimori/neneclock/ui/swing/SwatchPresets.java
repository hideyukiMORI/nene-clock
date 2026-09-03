package io.github.hideyukimori.neneclock.ui.swing;

import io.github.hideyukimori.neneclock.domain.RgbColor;
import io.github.hideyukimori.neneclock.domain.RgbColorOutcome;
import java.util.ArrayList;
import java.util.List;

/**
 * 色ピッカーに並べる厳選プリセット。
 *
 * <p>色は無数にあるので、選ばせるのは「よく使う 24 色」に絞る。それ以外は HEX で直接指定する。
 * 3 段（無彩色・原色寄り・淡色）に分けてあり、順序そのものが並びの意味である。
 */
public final class SwatchPresets {

    /** 1 段に並べる数。 */
    public static final int PER_ROW = 8;

    private static final int[] VALUES = {
        0x000000, 0x1A1917, 0x33302B, 0x6E6862, 0xA8A29A, 0xD8D2C8, 0xF5F2EB, 0xFFFFFF,
        0xB8462F, 0xD08C3F, 0xC9A227, 0x4E7A51, 0x2F6F7E, 0x3C5A8A, 0x6B4E8A, 0xA8456B,
        0xE9A08D, 0xEFC98A, 0xE7DA9B, 0xA9C6A6, 0x9BC3CC, 0xA6B6D6, 0xC0AAD8, 0xDDA8BE
    };

    private static final int BYTE_MASK = 0xFF;
    private static final int RED_SHIFT = 16;
    private static final int GREEN_SHIFT = 8;

    private SwatchPresets() {}

    /** 並び順のままのプリセット。 */
    public static List<RgbColor> all() {
        List<RgbColor> colours = new ArrayList<>();
        for (int packed : VALUES) {
            colours.add(unpack(packed));
        }
        return List.copyOf(colours);
    }

    private static RgbColor unpack(int packed) {
        return switch (RgbColor.of(
                (packed >> RED_SHIFT) & BYTE_MASK, (packed >> GREEN_SHIFT) & BYTE_MASK, packed & BYTE_MASK)) {
            case RgbColorOutcome.Accepted accepted -> accepted.value();
            // 定数は 24 bit に収めてあるので、ここへは来ない。
            case RgbColorOutcome.Rejected outOfRange -> RgbColor.DEFAULT_FONT;
        };
    }
}
