package io.github.hideyukimori.neneclock.application;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.hideyukimori.neneclock.domain.ClockFormat;
import io.github.hideyukimori.neneclock.domain.DateVisibility;
import io.github.hideyukimori.neneclock.domain.FontSize;
import io.github.hideyukimori.neneclock.domain.RgbColor;
import io.github.hideyukimori.neneclock.domain.SecondsVisibility;
import io.github.hideyukimori.neneclock.domain.Typeface;
import io.github.hideyukimori.neneclock.domain.UserSettings;
import io.github.hideyukimori.neneclock.domain.WindowTopmost;
import org.junit.jupiter.api.Test;

class SettingsLoadOutcomeTest {

    @Test
    void restoredOutcomeKeepsTheStoredSettings() {
        UserSettings stored = new UserSettings(
                ClockFormat.HOUR_12,
                SecondsVisibility.HIDDEN,
                DateVisibility.HIDDEN,
                WindowTopmost.ENABLED,
                Typeface.DEFAULT,
                FontSize.DEFAULT,
                RgbColor.DEFAULT_FONT,
                RgbColor.DEFAULT_BACKGROUND);

        SettingsLoadOutcome outcome = new SettingsLoadOutcome.Restored(stored);

        assertThat(outcome.settingsOrDefaults()).isEqualTo(stored);
    }

    @Test
    void defaultedOutcomeFallsBackWithoutLosingTheReason() {
        SettingsLoadOutcome outcome = new SettingsLoadOutcome.Defaulted(SettingsLoadFailure.UNSUPPORTED_SCHEMA);

        assertThat(outcome.settingsOrDefaults()).isEqualTo(UserSettings.defaults());
        assertThat(((SettingsLoadOutcome.Defaulted) outcome).failure())
                .isEqualTo(SettingsLoadFailure.UNSUPPORTED_SCHEMA);
    }

    @Test
    void saveOutcomeDistinguishesSuccessFromFailure() {
        assertThat(new SettingsSaveOutcome.Saved()).isEqualTo(new SettingsSaveOutcome.Saved());
        assertThat(new SettingsSaveOutcome.Failed(SettingsSaveFailure.UNWRITABLE))
                .isNotEqualTo(new SettingsSaveOutcome.Saved());
    }
}
