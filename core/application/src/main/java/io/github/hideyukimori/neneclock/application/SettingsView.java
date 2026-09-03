package io.github.hideyukimori.neneclock.application;

import io.github.hideyukimori.neneclock.domain.FontFamily;
import io.github.hideyukimori.neneclock.domain.UserSettings;
import java.util.List;
import java.util.Objects;

/**
 * 設定画面が描くために必要なものすべて。
 *
 * <p>UI はこの値を描くだけで、書体の一覧をどこから得るか（ARC-007）も、現在の設定を誰が持つか
 * （ARC-004）も知らない。一覧は防御的に複製して所有する（JAV-003）。
 */
public record SettingsView(List<FontFamily> availableFamilies, UserSettings settings) {

    public SettingsView {
        availableFamilies = List.copyOf(Objects.requireNonNull(availableFamilies, "availableFamilies"));
        Objects.requireNonNull(settings, "settings");
    }
}
