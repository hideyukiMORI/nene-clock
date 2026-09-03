package io.github.hideyukimori.neneclock.app;

import io.github.hideyukimori.neneclock.adapter.preferences.PreferencesSettingsAdapter;
import io.github.hideyukimori.neneclock.adapter.systemtime.SystemWallClockAdapter;
import io.github.hideyukimori.neneclock.application.ClockFaceQuery;
import io.github.hideyukimori.neneclock.application.SettingsStorePort;
import io.github.hideyukimori.neneclock.domain.UserSettings;
import io.github.hideyukimori.neneclock.ui.swing.ClockPanel;
import io.github.hideyukimori.neneclock.ui.swing.ClockTicker;
import io.github.hideyukimori.neneclock.ui.swing.MainFrame;
import javax.swing.SwingUtilities;

/** アプリケーションの入口。配線だけを行う。 */
public final class NeNeClockApplication {

    private NeNeClockApplication() {}

    /** 起動する。すべての Swing 操作は EDT 上で行う（SWG-001）。 */
    public static void main(String[] arguments) {
        SwingUtilities.invokeLater(NeNeClockApplication::start);
    }

    private static void start() {
        SettingsStorePort settingsStore = PreferencesSettingsAdapter.userScoped();
        UserSettings settings = settingsStore.load().settingsOrDefaults();

        ClockFaceQuery clockFace = new ClockFaceQuery(SystemWallClockAdapter.system());
        ClockPanel clockPanel = new ClockPanel();
        MainFrame frame = new MainFrame(clockPanel);

        frame.renderSettings(settings);
        clockPanel.renderFace(clockFace.currentFace(settings));

        ClockTicker ticker = new ClockTicker(() -> clockPanel.renderFace(clockFace.currentFace(settings)));
        ticker.start();
        frame.display();
    }
}
