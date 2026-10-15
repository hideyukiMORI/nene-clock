package io.github.hideyukimori.neneclock.ui.swing;

import io.github.hideyukimori.neneclock.application.TypefaceBinaryPort;
import io.github.hideyukimori.neneclock.domain.BundledTypeface;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 同梱書体のバイト列を Swing が描ける {@link Font} にする。
 *
 * <p>描画の型（{@code java.awt.Font}）を知ってよいのは UI だけなので、変換はここに置く。
 * 実行環境へ書体を登録（{@code GraphicsEnvironment#registerFont}）はしない。登録すると
 * 「同梱書体を名前で引ける」経路が生まれ、書体の指定経路が 2 本になる（ARC-001 / ADR 0006）。
 *
 * <p>🔑 解析は書体ごとに 1 回だけ行い、大きさは {@code deriveFont} で変える。{@code Font.createFont} は
 * 1 回 0.5〜15ms かかり、そのたびに一時ファイルを 1 枚書く。{@code deriveFont} は 0.01ms に満たない。
 * 設定ダイアログを開いている間、刻みごとに解析し直して毎秒 25 回になっていた（Issue #141）。
 * EDT からしか呼ばれないので、覚えておく表に排他は要らない（SWG-001）。
 */
public final class TypefaceFontLoader {

    private final TypefaceBinaryPort typefaces;
    private final Map<BundledTypeface, Font> parsed = new HashMap<>();

    public TypefaceFontLoader(TypefaceBinaryPort typefaces) {
        this.typefaces = Objects.requireNonNull(typefaces, "typefaces");
    }

    /** 指定の大きさの書体を作る。同梱が壊れていれば梱包の誤りとして落とす。 */
    public Font load(BundledTypeface typeface, int points) {
        Objects.requireNonNull(typeface, "typeface");
        return parsed.computeIfAbsent(typeface, this::parse).deriveFont(Font.PLAIN, (float) points);
    }

    private Font parse(BundledTypeface typeface) {
        try {
            return Font.createFont(Font.TRUETYPE_FONT, new ByteArrayInputStream(typefaces.read(typeface)));
        } catch (FontFormatException | IOException failure) {
            throw new IllegalStateException("同梱書体を Font にできない: " + typeface.constantName(), failure);
        }
    }
}
