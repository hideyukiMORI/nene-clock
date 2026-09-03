package io.github.hideyukimori.neneclock.ui.swing;

import java.awt.Color;
import java.awt.Font;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * {@link DetailButton} に出すもの。
 *
 * <p>4 つの引数を並べる代わりに名前付きの組で渡す（JAV-012）。色見本と見本書体は
 * 「その行に無い」ことがあるので {@code null} を許す。
 */
public record DetailValue(
        String text, @Nullable Color swatch, @Nullable Font sample) {

    public DetailValue {
        Objects.requireNonNull(text, "text");
    }

    /** 文字だけの行。 */
    public static DetailValue of(String text) {
        return new DetailValue(text, null, null);
    }
}
