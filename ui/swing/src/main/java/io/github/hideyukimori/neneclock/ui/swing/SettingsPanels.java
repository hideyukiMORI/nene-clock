package io.github.hideyukimori.neneclock.ui.swing;

import java.util.Objects;

/**
 * 設定モーダルが束ねる 3 つの画面。
 *
 * <p>引数を 3 つ並べる代わりに名前付きの組で渡す（JAV-012）。
 */
public record SettingsPanels(SettingsFormPanel form, TypefacePickerPanel typefaces, ColourPickerPanel colours) {

    public SettingsPanels {
        Objects.requireNonNull(form, "form");
        Objects.requireNonNull(typefaces, "typefaces");
        Objects.requireNonNull(colours, "colours");
    }
}
