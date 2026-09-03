package io.github.hideyukimori.neneclock.ui.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** 設定 1 行。左に日本語のラベル、右に操作、下に 1px のヘアライン。 */
public final class SettingsRow {

    private static final int HEIGHT = 42;
    private static final float LABEL_POINTS = 13f;

    private final JPanel surface = new JPanel(new BorderLayout());
    private final JLabel caption = TextRendering.label("");
    private final UiText label;
    private final boolean ruled;

    /** 文言と操作を組にして 1 行にする。文言は言語で変わるので、何を出すかだけを持つ。 */
    public SettingsRow(UiText label, JComponent control, boolean withRule) {
        this.label = Objects.requireNonNull(label, "label");
        this.ruled = withRule;
        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        right.add(Objects.requireNonNull(control, "control"), BorderLayout.EAST);
        surface.setOpaque(false);
        surface.setPreferredSize(new Dimension(0, HEIGHT));
        surface.setMaximumSize(new Dimension(Integer.MAX_VALUE, HEIGHT));
        surface.add(caption, BorderLayout.WEST);
        surface.add(right, BorderLayout.CENTER);
    }

    /** 画面に載せるための Swing 部品。 */
    public JComponent component() {
        return surface;
    }

    /** 文言と配色を反映する。 */
    public void renderRow(UiTheme theme) {
        caption.setText(theme.text(label));
        caption.setFont(theme.font(LABEL_POINTS));
        caption.setForeground(theme.palette().text());
        surface.setBorder(
                ruled
                        ? BorderFactory.createMatteBorder(
                                0, 0, 1, 0, theme.palette().hairline())
                        : null);
    }
}
