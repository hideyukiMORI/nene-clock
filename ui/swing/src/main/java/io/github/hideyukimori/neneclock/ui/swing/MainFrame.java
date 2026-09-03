package io.github.hideyukimori.neneclock.ui.swing;

import io.github.hideyukimori.neneclock.application.SettingsSaveOutcome;
import io.github.hideyukimori.neneclock.application.SettingsView;
import java.awt.Dimension;
import java.util.Objects;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

/**
 * ウィンドウの合成と生存期間だけを持つ（ARC-011）。
 *
 * <p>時計・ストップウォッチ・タイマーの計算をここに置かない。
 */
public final class MainFrame {

    private static final int INITIAL_WIDTH = 480;
    private static final int INITIAL_HEIGHT = 320;
    private static final int MINIMUM_WIDTH = 320;
    private static final int MINIMUM_HEIGHT = 240;

    private final JFrame frame = new JFrame("NeNe Clock");
    private final ClockPanel clockPanel;
    private final SettingsPanel settingsPanel;

    public MainFrame(ClockPanel clockPanel, SettingsPanel settingsPanel) {
        this.clockPanel = Objects.requireNonNull(clockPanel, "clockPanel");
        this.settingsPanel = Objects.requireNonNull(settingsPanel, "settingsPanel");
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Clock", clockPanel.component());
        tabs.addTab("Settings", settingsPanel.component());
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setContentPane(tabs);
        frame.setSize(new Dimension(INITIAL_WIDTH, INITIAL_HEIGHT));
        frame.setMinimumSize(new Dimension(MINIMUM_WIDTH, MINIMUM_HEIGHT));
        frame.setLocationRelativeTo(null);
    }

    /** 設定を窓と各タブへ反映する。UI 状態の反映経路はここ 1 本（CNF-004）。 */
    public void renderSettings(SettingsView view) {
        Objects.requireNonNull(view, "view");
        boolean topmost =
                switch (view.settings().windowTopmost()) {
                    case ENABLED -> true;
                    case DISABLED -> false;
                };
        frame.setAlwaysOnTop(topmost);
        clockPanel.renderSettings(view.settings());
        settingsPanel.renderSettings(view);
    }

    /** 保存の結果を設定タブへ伝える。 */
    public void renderSaveOutcome(SettingsSaveOutcome outcome) {
        settingsPanel.renderSaveOutcome(outcome);
    }

    /** 窓を表示する。EDT から呼ぶこと（SWG-001）。 */
    public void display() {
        frame.setVisible(true);
    }
}
