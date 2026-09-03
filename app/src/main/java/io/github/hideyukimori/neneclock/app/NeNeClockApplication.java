package io.github.hideyukimori.neneclock.app;

import io.github.hideyukimori.neneclock.adapter.fontcatalog.AwtFontCatalogAdapter;
import io.github.hideyukimori.neneclock.adapter.preferences.PreferencesSettingsAdapter;
import io.github.hideyukimori.neneclock.adapter.systemtime.SystemWallClockAdapter;
import io.github.hideyukimori.neneclock.application.ClockFaceQuery;
import io.github.hideyukimori.neneclock.application.FontCatalogPort;
import io.github.hideyukimori.neneclock.application.SettingsHandler;
import io.github.hideyukimori.neneclock.application.SettingsSaveOutcome;
import io.github.hideyukimori.neneclock.application.SettingsStorePort;
import io.github.hideyukimori.neneclock.application.SettingsView;
import io.github.hideyukimori.neneclock.ui.swing.ClockPanel;
import io.github.hideyukimori.neneclock.ui.swing.ClockTicker;
import io.github.hideyukimori.neneclock.ui.swing.MainFrame;
import io.github.hideyukimori.neneclock.ui.swing.SettingsPanel;
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
        FontCatalogPort fontCatalog = AwtFontCatalogAdapter.system();
        SettingsHandler settings = SettingsHandler.restoredFrom(settingsStore);
        ClockFaceQuery clockFace = new ClockFaceQuery(SystemWallClockAdapter.system());

        ClockPanel clockPanel = new ClockPanel();
        SettingsPanel settingsPanel = new SettingsPanel();
        MainFrame frame = new MainFrame(clockPanel, settingsPanel);

        settingsPanel.onSettingsRequested(requested -> {
            SettingsSaveOutcome outcome = settings.apply(requested);
            frame.renderSettings(new SettingsView(fontCatalog.availableFamilies(), settings.current()));
            frame.renderSaveOutcome(outcome);
            clockPanel.renderFace(clockFace.currentFace(settings.current()));
        });

        frame.renderSettings(new SettingsView(fontCatalog.availableFamilies(), settings.current()));
        clockPanel.renderFace(clockFace.currentFace(settings.current()));

        ClockTicker ticker = new ClockTicker(() -> clockPanel.renderFace(clockFace.currentFace(settings.current())));
        ticker.start();
        frame.display();
    }
}
