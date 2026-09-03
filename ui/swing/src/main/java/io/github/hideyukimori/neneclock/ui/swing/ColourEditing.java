package io.github.hideyukimori.neneclock.ui.swing;

import io.github.hideyukimori.neneclock.domain.RgbColor;
import io.github.hideyukimori.neneclock.domain.UserSettings;
import java.util.Objects;

/**
 * いま色ピッカーが編集しているもの。
 *
 * <p>🔑 色を 2 つ渡すのではなく、**設定一式とどちらの役かを**渡す。文字色と背景色は同じ型なので、
 * 2 つ並べて渡すと取り違えてもコンパイルが通る（ADR 0007）。役から引く形にすれば取り違えようがない。
 */
public record ColourEditing(UserSettings settings, SettingsDestination role) {

    public ColourEditing {
        Objects.requireNonNull(settings, "settings");
        Objects.requireNonNull(role, "role");
    }

    /** いじっている色。 */
    public RgbColor editing() {
        return switch (role) {
            case BACKGROUND_COLOUR -> settings.backgroundColor();
            case FONT_COLOUR, FORM, TYPEFACE -> settings.fontColor();
        };
    }

    /** いじっていないほうの色。読める色を選び直すときの相手になる。 */
    public RgbColor counterpart() {
        return switch (role) {
            case BACKGROUND_COLOUR -> settings.fontColor();
            case FONT_COLOUR, FORM, TYPEFACE -> settings.backgroundColor();
        };
    }
}
