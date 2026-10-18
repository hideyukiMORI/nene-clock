package io.github.hideyukimori.neneclock.adapter.preferences;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.hideyukimori.neneclock.application.SettingsLoadFailure;
import io.github.hideyukimori.neneclock.application.SettingsLoadOutcome;
import io.github.hideyukimori.neneclock.application.SettingsSaveFailure;
import io.github.hideyukimori.neneclock.application.SettingsSaveOutcome;
import io.github.hideyukimori.neneclock.domain.SettingsSchemaVersion;
import io.github.hideyukimori.neneclock.domain.UserSettings;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** 読めなかった設定を上書きしない（Issue #150・ADR 0023）。 */
class PreferencesSettingsAdapterUnreadableTest {

    private static final String TEST_NODE = "io/github/hideyukimori/neneclock/test-unreadable";
    private static final String BACKUP = "unreadable-backup";

    private Preferences node = Preferences.userRoot().node(TEST_NODE);

    @BeforeEach
    void createNode() throws BackingStoreException {
        node = Preferences.userRoot().node(TEST_NODE);
        node.removeNode();
        node = Preferences.userRoot().node(TEST_NODE);
    }

    @AfterEach
    void removeNode() throws BackingStoreException {
        node.removeNode();
    }

    @Test
    void keepsAFutureSchemaReadOnly() throws BackingStoreException {
        node.putInt("schemaVersion", 99);
        node.put("clockFormat", "SOMETHING_NEW");

        SettingsSaveOutcome outcome = PreferencesSettingsAdapter.at(node).save(UserSettings.defaults());

        assertThat(outcome).isEqualTo(new SettingsSaveOutcome.Failed(SettingsSaveFailure.NEWER_VERSION_KEPT));
        assertThat(node.getInt("schemaVersion", 0)).isEqualTo(99);
        assertThat(node.get("clockFormat", null)).isEqualTo("SOMETHING_NEW");
        assertThat(node.keys()).containsExactlyInAnyOrder("schemaVersion", "clockFormat");
    }

    @Test
    void backsUpAnInvalidValueBeforeSaving() throws BackingStoreException {
        node.putInt("schemaVersion", SettingsSchemaVersion.CURRENT.value());
        node.put("clockFormat", "NOT_A_FORMAT");
        node.putInt("fontPoints", 7);

        SettingsSaveOutcome outcome = PreferencesSettingsAdapter.at(node).save(UserSettings.defaults());

        assertThat(outcome).isEqualTo(new SettingsSaveOutcome.Saved());
        Preferences backup = node.node(BACKUP);
        assertThat(backup.get("clockFormat", null)).isEqualTo("NOT_A_FORMAT");
        assertThat(backup.getInt("fontPoints", 0)).isEqualTo(7);
        assertThat(backup.getInt("schemaVersion", 0)).isEqualTo(SettingsSchemaVersion.CURRENT.value());
        assertThat(PreferencesSettingsAdapter.at(node).load())
                .isEqualTo(new SettingsLoadOutcome.Restored(UserSettings.defaults()));
    }

    @Test
    void savesNormallyOnceTheInvalidValuesAreBackedUp() {
        node.putInt("schemaVersion", SettingsSchemaVersion.CURRENT.value());
        node.put("clockFormat", "NOT_A_FORMAT");
        PreferencesSettingsAdapter adapter = PreferencesSettingsAdapter.at(node);
        adapter.save(UserSettings.defaults());

        SettingsSaveOutcome second = adapter.save(UserSettings.defaults());

        assertThat(second).isEqualTo(new SettingsSaveOutcome.Saved());
        assertThat(node.node(BACKUP).get("clockFormat", null)).isEqualTo("NOT_A_FORMAT");
    }

    @Test
    void treatsASchemaBelowTheFirstAsInvalidNotAsTheFuture() {
        node.putInt("schemaVersion", -3);

        SettingsLoadOutcome outcome = PreferencesSettingsAdapter.at(node).load();

        assertThat(outcome).isEqualTo(new SettingsLoadOutcome.Defaulted(SettingsLoadFailure.INVALID_VALUE));
    }

    @Test
    void treatsASchemaThatIsNotANumberAsInvalidNotAsAbsent() {
        node.put("schemaVersion", "seven");

        SettingsLoadOutcome outcome = PreferencesSettingsAdapter.at(node).load();
        SettingsSaveOutcome saved = PreferencesSettingsAdapter.at(node).save(UserSettings.defaults());

        assertThat(outcome).isEqualTo(new SettingsLoadOutcome.Defaulted(SettingsLoadFailure.INVALID_VALUE));
        assertThat(saved).isEqualTo(new SettingsSaveOutcome.Saved());
        assertThat(node.node(BACKUP).get("schemaVersion", null)).isEqualTo("seven");
    }

    @Test
    void doesNotBackUpOnTheFirstRun() throws BackingStoreException {
        SettingsSaveOutcome outcome = PreferencesSettingsAdapter.at(node).save(UserSettings.defaults());

        assertThat(outcome).isEqualTo(new SettingsSaveOutcome.Saved());
        assertThat(node.nodeExists(BACKUP)).isFalse();
    }
}
