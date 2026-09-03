package io.github.hideyukimori.neneclock.ui.swing;

import io.github.hideyukimori.neneclock.domain.UserSettings;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.geom.RoundRectangle2D;
import java.util.Objects;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * 設定モーダルの上に置く、いまの設定そのままの見本。
 *
 * <p>設定を変えた結果は時計本体にも即座に出るが、モーダルが時計に重なっていると見えない。
 * 「いじっている場所のすぐ上で結果が見える」ことのために置いている。
 */
public final class ClockPreview {

    private static final int HEIGHT = 76;
    private static final int ARC = 10;
    private static final float TIME_POINTS = 34f;
    private static final float DATE_POINTS = 9f;
    private static final int GAP = 7;

    private final TypefaceFontLoader typefaces;
    private final JLabel time = TextRendering.centredLabel("", SwingConstants.CENTER);
    private final JLabel date = TextRendering.centredLabel("", SwingConstants.CENTER);

    private Color fill = Color.WHITE;
    private Color edge = Color.GRAY;

    private final JPanel surface = new JPanel(new GridBagLayout()) {
        @Override
        protected void paintComponent(Graphics graphics) {
            paintCard(graphics);
        }
    };

    /** 見本を組み立てる。中身は {@code render*} が決める。 */
    public ClockPreview(TypefaceFontLoader typefaces) {
        this.typefaces = Objects.requireNonNull(typefaces, "typefaces");
        surface.setOpaque(false);
        surface.setPreferredSize(new Dimension(0, HEIGHT));
        surface.setMaximumSize(new Dimension(Integer.MAX_VALUE, HEIGHT));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        surface.add(time, constraints);
        constraints.gridy = 1;
        constraints.insets = new java.awt.Insets(GAP, 0, 0, 0);
        surface.add(date, constraints);
    }

    /** 画面に載せるための Swing 部品。 */
    public JComponent component() {
        return surface;
    }

    /** 設定と、いま表示している文字列を反映する。 */
    public void renderSettings(UserSettings settings, String timeText, String dateText) {
        Objects.requireNonNull(settings, "settings");
        Color foreground = new Color(
                settings.fontColor().red(),
                settings.fontColor().green(),
                settings.fontColor().blue());
        fill = new Color(
                settings.backgroundColor().red(),
                settings.backgroundColor().green(),
                settings.backgroundColor().blue());
        edge = Palette.from(settings.backgroundColor()).hairline();
        time.setFont(typefaces.load(settings.typeface(), Math.round(TIME_POINTS)));
        time.setForeground(foreground);
        time.setText(timeText);
        date.setFont(typefaces.load(settings.typeface(), Math.round(DATE_POINTS)));
        date.setForeground(foreground);
        date.setText(dateText);
        surface.repaint();
    }

    private void paintCard(Graphics graphics) {
        Graphics2D canvas = (Graphics2D) graphics.create();
        TextRendering.smooth(canvas);
        canvas.setColor(fill);
        canvas.fill(new RoundRectangle2D.Double(0, 0, surface.getWidth(), surface.getHeight(), ARC, ARC));
        canvas.setColor(edge);
        canvas.draw(new RoundRectangle2D.Double(0, 0, surface.getWidth() - 1.0, surface.getHeight() - 1.0, ARC, ARC));
        canvas.dispose();
    }
}
