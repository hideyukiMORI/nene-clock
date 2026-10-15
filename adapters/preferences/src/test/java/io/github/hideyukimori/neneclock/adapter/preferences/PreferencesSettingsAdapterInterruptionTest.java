package io.github.hideyukimori.neneclock.adapter.preferences;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.hideyukimori.neneclock.application.SettingsLoadOutcome;
import io.github.hideyukimori.neneclock.domain.Typeface;
import io.github.hideyukimori.neneclock.domain.UserSettings;
import io.github.hideyukimori.neneclock.domain.WindowPadding;
import io.github.hideyukimori.neneclock.domain.WindowPaddingOutcome;
import org.junit.jupiter.api.Test;

/** 保存の途中でプロセスが落ちたときに、次の起動で何が読めるか（Issue #139）。 */
class PreferencesSettingsAdapterInterruptionTest {

    @Test
    void keepsTheSettingsWhenTheFirstSaveAfterAnUpgradeStopsHalfway() {
        // 🔴 版番号を先に書くと、v7 → v8 の最初の保存の途中で落ちたとき
        //    「版は 8 なのに余白が無い」が残り、次の起動で全部が既定値へ戻った。
        //    put 1 回ごとに落ちる地点を全部試す。どこで落ちても、既定値へ落ちてはいけない。
        UserSettings changed = UserSettings.defaults().withWindowPadding(padding(WindowPadding.MINIMUM_PIXELS));
        int putsInOneSave = countPutsInOneSave(changed);

        for (int stopAt = 0; stopAt < putsInOneSave; stopAt++) {
            InterruptedPreferences store = new InterruptedPreferences();
            writeVersionSeven(store);
            store.stopAfter(stopAt);

            assertThatThrownBy(() -> PreferencesSettingsAdapter.at(store).save(changed))
                    .isInstanceOf(InterruptedPreferences.ProcessStopped.class);
            store.restart();

            assertThat(PreferencesSettingsAdapter.at(store).load())
                    .as("put %d 回目で落ちた", stopAt + 1)
                    .isInstanceOf(SettingsLoadOutcome.Restored.class);
        }
    }

    /** 1 回の保存で put が何回走るか。落ちる地点を数え漏らさないために、実物から数える。 */
    private static int countPutsInOneSave(UserSettings settings) {
        InterruptedPreferences counter = new InterruptedPreferences();
        PreferencesSettingsAdapter.at(counter).save(settings);
        return counter.puts();
    }

    /** v7 の保存値を書く。v7 は余白のキーを持たないので、現在の形で保存してから外す。 */
    private static void writeVersionSeven(InterruptedPreferences store) {
        PreferencesSettingsAdapter.at(store).save(UserSettings.defaults().withTypeface(Typeface.ORBITRON));
        store.putInt("schemaVersion", 7);
        store.remove("windowPadding");
    }

    private static WindowPadding padding(int pixels) {
        return ((WindowPaddingOutcome.Accepted) WindowPadding.of(pixels)).value();
    }
}
