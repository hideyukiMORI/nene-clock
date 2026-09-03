package io.github.hideyukimori.neneclock.application;

import io.github.hideyukimori.neneclock.domain.UserSettings;
import java.util.Objects;

/**
 * 設定復元の結果。
 *
 * <p>「読めなかったら既定値へ落ちる」判断をこの型が 1 か所で持つ（ARC-001）。呼び出し側が
 * 場当たりに {@code null} 判定や try/catch で代替値を作ることを防ぐ。
 */
public sealed interface SettingsLoadOutcome {

    /** 復元できたか既定値かに関わらず、実際に使う設定。 */
    UserSettings settingsOrDefaults();

    /** 保存された設定を復元できた。 */
    record Restored(UserSettings settings) implements SettingsLoadOutcome {
        public Restored {
            Objects.requireNonNull(settings, "settings");
        }

        @Override
        public UserSettings settingsOrDefaults() {
            return settings;
        }
    }

    /** 復元できなかったので既定値を使う。理由は失われない。 */
    record Defaulted(SettingsLoadFailure failure) implements SettingsLoadOutcome {
        public Defaulted {
            Objects.requireNonNull(failure, "failure");
        }

        @Override
        public UserSettings settingsOrDefaults() {
            return UserSettings.defaults();
        }
    }
}
