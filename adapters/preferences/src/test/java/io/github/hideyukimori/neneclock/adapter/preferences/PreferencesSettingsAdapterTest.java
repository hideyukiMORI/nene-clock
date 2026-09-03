package io.github.hideyukimori.neneclock.adapter.preferences;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.hideyukimori.neneclock.application.SettingsLoadFailure;
import io.github.hideyukimori.neneclock.application.SettingsLoadOutcome;
import io.github.hideyukimori.neneclock.application.SettingsSaveOutcome;
import io.github.hideyukimori.neneclock.domain.ClockFormat;
import io.github.hideyukimori.neneclock.domain.DateVisibility;
import io.github.hideyukimori.neneclock.domain.FontColor;
import io.github.hideyukimori.neneclock.domain.FontColorOutcome;
import io.github.hideyukimori.neneclock.domain.FontFamily;
import io.github.hideyukimori.neneclock.domain.FontFamilyOutcome;
import io.github.hideyukimori.neneclock.domain.FontSize;
import io.github.hideyukimori.neneclock.domain.FontSizeOutcome;
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
                family("Serif"),
                FontSize.DEFAULT,
                color(10, 20, 30));

        SettingsSaveOutcome saveOutcome = adapter.save(stored);

        assertThat(saveOutcome).isEqualTo(new SettingsSaveOutcome.Saved());
        assertThat(adapter.load()).isEqualTo(new SettingsLoadOutcome.Restored(stored));
    }

    @Test
    void refusesAFutureSchemaInsteadOfGuessing() {
        node.putInt("schemaVersion", 99);

        SettingsLoadOutcome outcome = PreferencesSettingsAdapter.at(node).load();

        assertThat(outcome).isEqualTo(new SettingsLoadOutcome.Defaulted(SettingsLoadFailure.UNSUPPORTED_SCHEMA));
    }

    @Test
    void migratesVersionOneWithoutLosingTheSettingsItHad() {
        writeVersionOne(versionOneSettings());

        SettingsLoadOutcome outcome = PreferencesSettingsAdapter.at(node).load();

        assertThat(outcome).isEqualTo(new SettingsLoadOutcome.Restored(versionOneSettings()));
    }

    @Test
    void refusesABrokenValueEvenWhenMigratingFromVersionOne() {
        writeVersionOne(versionOneSettings());
        node.put("clockFormat", "HOUR_13");

        SettingsLoadOutcome outcome = PreferencesSettingsAdapter.at(node).load();

        assertThat(outcome).isEqualTo(new SettingsLoadOutcome.Defaulted(SettingsLoadFailure.INVALID_VALUE));
    }

    @Test
    void doesNotRewriteTheStoreWhileReading() {
        writeVersionOne(versionOneSettings());

        PreferencesSettingsAdapter.at(node).load();

        assertThat(node.getInt("schemaVersion", 0)).isEqualTo(1);
    }

    /** v1 で保存されていた設定を v2 の型で表したもの。書体と文字色は既定値になる。 */
    private static UserSettings versionOneSettings() {
        return new UserSettings(
                ClockFormat.HOUR_12,
                SecondsVisibility.HIDDEN,
                DateVisibility.HIDDEN,
                WindowTopmost.ENABLED,
                FontFamily.DEFAULT,
                size(96),
                FontColor.DEFAULT);
    }

    @Test
    void refusesAnUnknownFontFamily() {
        PreferencesSettingsAdapter.at(node).save(UserSettings.defaults());
        node.put("fontFamily", " ");

        SettingsLoadOutcome outcome = PreferencesSettingsAdapter.at(node).load();

        assertThat(outcome).isEqualTo(new SettingsLoadOutcome.Defaulted(SettingsLoadFailure.INVALID_VALUE));
    }

    @Test
    void refusesAColourComponentOutsideTheAllowedRange() {
        PreferencesSettingsAdapter.at(node).save(UserSettings.defaults());
        node.putInt("fontGreen", FontColor.MAXIMUM_COMPONENT + 1);

        SettingsLoadOutcome outcome = PreferencesSettingsAdapter.at(node).load();

        assertThat(outcome).isEqualTo(new SettingsLoadOutcome.Defaulted(SettingsLoadFailure.INVALID_VALUE));
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

    /**
     * v1 の保存値を書く。v1 は書体と文字色のキーを持たないので、渡された設定のうち
     * 当時からある 5 項目だけを書く。
     */
    private void writeVersionOne(UserSettings settings) {
        node.putInt("schemaVersion", 1);
        node.put("clockFormat", settings.clockFormat().name());
        node.put("secondsVisibility", settings.secondsVisibility().name());
        node.put("dateVisibility", settings.dateVisibility().name());
        node.put("windowTopmost", settings.windowTopmost().name());
        node.putInt("fontPoints", settings.fontSize().points());
    }

    private static FontFamily family(String name) {
        return ((FontFamilyOutcome.Accepted) FontFamily.of(name)).value();
    }

    private static FontColor color(int red, int green, int blue) {
        return ((FontColorOutcome.Accepted) FontColor.of(red, green, blue)).value();
    }

    private static FontSize size(int points) {
        return ((FontSizeOutcome.Accepted) FontSize.of(points)).value();
    }
}
