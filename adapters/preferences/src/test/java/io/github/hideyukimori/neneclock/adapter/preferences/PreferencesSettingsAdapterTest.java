package io.github.hideyukimori.neneclock.adapter.preferences;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.hideyukimori.neneclock.application.SettingsLoadFailure;
import io.github.hideyukimori.neneclock.application.SettingsLoadOutcome;
import io.github.hideyukimori.neneclock.application.SettingsSaveOutcome;
import io.github.hideyukimori.neneclock.domain.ClockFormat;
import io.github.hideyukimori.neneclock.domain.DateVisibility;
import io.github.hideyukimori.neneclock.domain.FontSize;
import io.github.hideyukimori.neneclock.domain.SecondsVisibility;
import io.github.hideyukimori.neneclock.domain.UserSettings;
import io.github.hideyukimori.neneclock.domain.WindowTopmost;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PreferencesSettingsAdapterTest {

    private static final String TEST_NODE = "io/github/hideyukimori/neneclock/test";

    private Preferences node = Preferences.userRoot().node(TEST_NODE);

    @BeforeEach
    void createNode() throws BackingStoreException {
        node = Preferences.userRoot().node(TEST_NODE);
        node.clear();
    }

    @AfterEach
    void removeNode() throws BackingStoreException {
        node.removeNode();
    }

    @Test
    void reportsAbsentSettingsOnFirstRun() {
        SettingsLoadOutcome outcome = PreferencesSettingsAdapter.at(node).load();

        assertThat(outcome).isEqualTo(new SettingsLoadOutcome.Defaulted(SettingsLoadFailure.ABSENT));
        assertThat(outcome.settingsOrDefaults()).isEqualTo(UserSettings.defaults());
    }

    @Test
    void roundTripsSavedSettings() {
        PreferencesSettingsAdapter adapter = PreferencesSettingsAdapter.at(node);
        UserSettings stored = new UserSettings(
                ClockFormat.HOUR_12,
                SecondsVisibility.HIDDEN,
                DateVisibility.HIDDEN,
                WindowTopmost.ENABLED,
                FontSize.DEFAULT);

        SettingsSaveOutcome saveOutcome = adapter.save(stored);

        assertThat(saveOutcome).isEqualTo(new SettingsSaveOutcome.Saved());
        assertThat(adapter.load()).isEqualTo(new SettingsLoadOutcome.Restored(stored));
    }

    @Test
    void refusesAnUnsupportedSchemaInsteadOfGuessing() {
        node.putInt("schemaVersion", 99);

        SettingsLoadOutcome outcome = PreferencesSettingsAdapter.at(node).load();

        assertThat(outcome).isEqualTo(new SettingsLoadOutcome.Defaulted(SettingsLoadFailure.UNSUPPORTED_SCHEMA));
    }

    @Test
    void refusesAnUnknownEnumValue() {
        PreferencesSettingsAdapter.at(node).save(UserSettings.defaults());
        node.put("clockFormat", "HOUR_13");

        SettingsLoadOutcome outcome = PreferencesSettingsAdapter.at(node).load();

        assertThat(outcome).isEqualTo(new SettingsLoadOutcome.Defaulted(SettingsLoadFailure.INVALID_VALUE));
    }

    @Test
    void refusesAFontSizeOutsideTheAllowedRange() {
        PreferencesSettingsAdapter.at(node).save(UserSettings.defaults());
        node.putInt("fontPoints", FontSize.MAXIMUM_POINTS + 1);

        SettingsLoadOutcome outcome = PreferencesSettingsAdapter.at(node).load();

        assertThat(outcome).isEqualTo(new SettingsLoadOutcome.Defaulted(SettingsLoadFailure.INVALID_VALUE));
    }
}
