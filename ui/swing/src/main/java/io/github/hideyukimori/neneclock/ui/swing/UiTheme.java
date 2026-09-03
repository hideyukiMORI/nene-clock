package io.github.hideyukimori.neneclock.ui.swing;

import io.github.hideyukimori.neneclock.domain.Language;
import io.github.hideyukimori.neneclock.domain.UserSettings;
import java.awt.Font;
import java.util.Objects;

/**
 * 画面を描くために要る「設定から決まるもの」一式。
 *
 * <p>色（{@link Palette}）と言語と UI 書体は、どれも利用者が直接選ぶものではなく、
 * 選んだ設定から決まる従属的な値である。部品ごとに導出すると、導出の仕方が散らばる。
 *
 * <p>書体は**組み立て済みのものを受け取る**。`Font` を作るのは重い処理なので、
 * 言語ごとに 1 度だけ作って使い回す（{@link ClockScreen} が持つ）。
 */
public final class UiTheme {

    private final Palette palette;
    private final Language language;
    private final Font base;

    private UiTheme(Palette palette, Language language, Font base) {
        this.palette = palette;
        this.language = language;
        this.base = base;
    }

    /** 設定と、その言語の UI 書体から組み立てる。ここが唯一の生成経路。 */
    public static UiTheme of(UserSettings settings, Font interfaceFont) {
        Objects.requireNonNull(settings, "settings");
        Objects.requireNonNull(interfaceFont, "interfaceFont");
        return new UiTheme(Palette.from(settings.backgroundColor()), settings.language(), interfaceFont);
    }

    /** 配色。 */
    public Palette palette() {
        return palette;
    }

    /** 言語。 */
    public Language language() {
        return language;
    }

    /** その大きさの UI 書体。{@code deriveFont} は軽いので、呼ぶたびに作ってよい。 */
    public Font font(float points) {
        return base.deriveFont(points);
    }

    /** 文言。 */
    public String text(UiText text) {
        return text.in(language);
    }
}
