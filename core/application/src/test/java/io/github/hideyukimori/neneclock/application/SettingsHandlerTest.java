package io.github.hideyukimori.neneclock.application;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.hideyukimori.neneclock.domain.ClockFormat;
import io.github.hideyukimori.neneclock.domain.DateVisibility;
import io.github.hideyukimori.neneclock.domain.FontSize;
import io.github.hideyukimori.neneclock.domain.Language;
import io.github.hideyukimori.neneclock.domain.RgbaColor;
import io.github.hideyukimori.neneclock.domain.SecondsVisibility;
import io.github.hideyukimori.neneclock.domain.Typeface;
import io.github.hideyukimori.neneclock.domain.UserSettings;
import io.github.hideyukimori.neneclock.domain.WindowTopmost;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class SettingsHandlerTest {

    private static final UserSettings CHANGED = new UserSettings(
            ClockFormat.HOUR_12,
            SecondsVisibility.HIDDEN,
            DateVisibility.HIDDEN,
            WindowTopmost.ENABLED,
            Typeface.DEFAULT,
            FontSize.DEFAULT,
            RgbaColor.DEFAULT_FONT,
            RgbaColor.DEFAULT_BACKGROUND,
            Language.DEFAULT);

    @Test
    void startsFromWhateverTheStoreCouldRestore() {
        SettingsHandler handler = SettingsHandler.restoredFrom(
                new RecordingStore(new SettingsLoadOutcome.Restored(CHANGED), new SettingsSaveOutcome.Saved()));

        assertThat(handler.current()).isEqualTo(CHANGED);
    }

    @Test
    void startsFromDefaultsWhenNothingCouldBeRestored() {
        SettingsHandler handler = SettingsHandler.restoredFrom(new RecordingStore(
                new SettingsLoadOutcome.Defaulted(SettingsLoadFailure.ABSENT), new SettingsSaveOutcome.Saved()));

        assertThat(handler.current()).isEqualTo(UserSettings.defaults());
    }

    @Test
    void applyingSwapsTheCurrentValueAndSavesIt() {
        RecordingStore store = new RecordingStore(
                new SettingsLoadOutcome.Defaulted(SettingsLoadFailure.ABSENT), new SettingsSaveOutcome.Saved());
        SettingsHandler handler = SettingsHandler.restoredFrom(store);

        SettingsSaveOutcome outcome = handler.apply(CHANGED);

        assertThat(outcome).isEqualTo(new SettingsSaveOutcome.Saved());
        assertThat(handler.current()).isEqualTo(CHANGED);
        assertThat(store.saved).containsExactly(CHANGED);
    }

    @Test
    void aFailedSaveStillChangesTheCurrentValueAndReportsTheFailure() {
        SettingsSaveOutcome failure = new SettingsSaveOutcome.Failed(SettingsSaveFailure.UNWRITABLE);
        SettingsHandler handler = SettingsHandler.restoredFrom(
                new RecordingStore(new SettingsLoadOutcome.Defaulted(SettingsLoadFailure.ABSENT), failure));

        SettingsSaveOutcome outcome = handler.apply(CHANGED);

        assertThat(outcome).isEqualTo(failure);
        assertThat(handler.current()).isEqualTo(CHANGED);
    }

    /** 保存の呼び出しを覚えておくだけの代役。実際の保存先は持たない。 */
    private static final class RecordingStore implements SettingsStorePort {

        private final SettingsLoadOutcome loadOutcome;
        private final SettingsSaveOutcome saveOutcome;
        private final List<UserSettings> saved = new ArrayList<>();

        private RecordingStore(SettingsLoadOutcome loadOutcome, SettingsSaveOutcome saveOutcome) {
            this.loadOutcome = loadOutcome;
            this.saveOutcome = saveOutcome;
        }

        @Override
        public SettingsLoadOutcome load() {
            return loadOutcome;
        }

        @Override
        public SettingsSaveOutcome save(UserSettings settings) {
            saved.add(settings);
            return saveOutcome;
        }
    }
}
