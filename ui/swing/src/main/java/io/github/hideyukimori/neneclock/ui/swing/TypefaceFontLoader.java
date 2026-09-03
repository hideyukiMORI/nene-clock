package io.github.hideyukimori.neneclock.ui.swing;

import io.github.hideyukimori.neneclock.application.TypefaceBinaryPort;
import io.github.hideyukimori.neneclock.domain.Typeface;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Objects;

/**
 * 同梱書体のバイト列を Swing が描ける {@link Font} にする。
 *
 * <p>描画の型（{@code java.awt.Font}）を知ってよいのは UI だけなので、変換はここに置く。
 * 実行環境へ書体を登録（{@code GraphicsEnvironment#registerFont}）はしない。登録すると
 * 「同梱書体を名前で引ける」経路が生まれ、書体の指定経路が 2 本になる（ARC-001 / ADR 0006）。
 */
public final class TypefaceFontLoader {

    private final TypefaceBinaryPort typefaces;

    public TypefaceFontLoader(TypefaceBinaryPort typefaces) {
        this.typefaces = Objects.requireNonNull(typefaces, "typefaces");
    }

    /** 指定の大きさの書体を作る。同梱が壊れていれば梱包の誤りとして落とす。 */
    public Font load(Typeface typeface, int points) {
        Objects.requireNonNull(typeface, "typeface");
        try {
            return Font.createFont(Font.TRUETYPE_FONT, new ByteArrayInputStream(typefaces.read(typeface)))
                    .deriveFont(Font.PLAIN, (float) points);
        } catch (FontFormatException | IOException failure) {
            throw new IllegalStateException("同梱書体を Font にできない: " + typeface.name(), failure);
        }
    }
}
