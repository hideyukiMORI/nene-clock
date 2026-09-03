package io.github.hideyukimori.neneclock.ui.swing;

import io.github.hideyukimori.neneclock.application.ClockFace;
import io.github.hideyukimori.neneclock.application.DateLine;
import io.github.hideyukimori.neneclock.domain.UserSettings;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Objects;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * 時計タブ。{@link ClockFace} をそのまま描く。
 *
 * <p>JPanel を継承せず内包する（構築中に自分のメソッドが呼ばれる形を作らないため）。
 */
public final class ClockPanel {

    private static final int DATE_FONT_DIVISOR = 4;
    private static final int MINIMUM_DATE_POINTS = 12;

    private final TypefaceFontLoader typefaces;
    private final JPanel panel = new JPanel(new GridBagLayout());
    private final JLabel time = new JLabel("", SwingConstants.CENTER);
    private final JLabel date = new JLabel("", SwingConstants.CENTER);

    /** 部品を組み立てる。表示内容は {@code render*} が決める。 */
    public ClockPanel(TypefaceFontLoader typefaces) {
        this.typefaces = Objects.requireNonNull(typefaces, "typefaces");
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add(time, constraints);
        constraints.gridy = 1;
        panel.add(date, constraints);
    }

    /** 画面に載せるための Swing 部品。 */
    public JComponent component() {
        return panel;
    }

    /** 表示文字列を反映する。 */
    public void renderFace(ClockFace face) {
        Objects.requireNonNull(face, "face");
        time.setText(face.time());
        switch (face.date()) {
            case DateLine.Shown shown -> {
                date.setText(shown.text());
                date.setVisible(true);
            }
            case DateLine.Hidden hidden -> date.setVisible(false);
        }
    }

    /** 設定を反映する。 */
    public void renderSettings(UserSettings settings) {
        Objects.requireNonNull(settings, "settings");
        int points = settings.fontSize().points();
        Color foreground = new Color(
                settings.fontColor().red(),
                settings.fontColor().green(),
                settings.fontColor().blue());
        time.setFont(typefaces.load(settings.typeface(), points));
        time.setForeground(foreground);
        date.setFont(typefaces.load(settings.typeface(), Math.max(MINIMUM_DATE_POINTS, points / DATE_FONT_DIVISOR)));
        date.setForeground(foreground);
    }
}
