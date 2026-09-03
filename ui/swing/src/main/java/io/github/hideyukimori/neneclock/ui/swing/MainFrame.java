package io.github.hideyukimori.neneclock.ui.swing;

import io.github.hideyukimori.neneclock.domain.UserSettings;
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
    private static final int INITIAL_HEIGHT = 240;
    private static final int MINIMUM_WIDTH = 320;
    private static final int MINIMUM_HEIGHT = 160;

    private final JFrame frame = new JFrame("NeNe Clock");
    private final ClockPanel clockPanel;

    public MainFrame(ClockPanel clockPanel) {
        this.clockPanel = Objects.requireNonNull(clockPanel, "clockPanel");
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Clock", clockPanel.component());
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setContentPane(tabs);
        frame.setSize(new Dimension(INITIAL_WIDTH, INITIAL_HEIGHT));
        frame.setMinimumSize(new Dimension(MINIMUM_WIDTH, MINIMUM_HEIGHT));
        frame.setLocationRelativeTo(null);
    }

    /** 設定を窓と各タブへ反映する。UI 状態の反映経路はここ 1 本（CNF-004）。 */
    public void renderSettings(UserSettings settings) {
        Objects.requireNonNull(settings, "settings");
        boolean topmost =
                switch (settings.windowTopmost()) {
                    case ENABLED -> true;
                    case DISABLED -> false;
                };
        frame.setAlwaysOnTop(topmost);
        clockPanel.renderSettings(settings);
    }

    /** 窓を表示する。EDT から呼ぶこと（SWG-001）。 */
    public void display() {
        frame.setVisible(true);
    }
}
