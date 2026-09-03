package io.github.hideyukimori.neneclock.ui.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.Objects;
import javax.swing.JComponent;
import javax.swing.JPanel;

/** アイコン 1 つの押しどころ。設定モーダルのヘッダで使う。 */
public final class IconButton {

    private static final int HIT = 30;
    private static final int ICON = 16;
    private static final int ARC = 8;

    private final ChromeIcon icon;

    private final JPanel surface = new JPanel(null) {
        @Override
        protected void paintComponent(Graphics graphics) {
            paintButton(graphics);
        }
    };

    private Palette palette = Palette.from(io.github.hideyukimori.neneclock.domain.RgbColor.DEFAULT_BACKGROUND);
    private boolean hovered;

    /** 描くアイコンを決めて組み立てる。 */
    public IconButton(ChromeIcon icon) {
        this.icon = Objects.requireNonNull(icon, "icon");
        surface.setOpaque(false);
        surface.setPreferredSize(new Dimension(HIT, HIT));
        surface.setMaximumSize(new Dimension(HIT, HIT));
        surface.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                hovered = true;
                surface.repaint();
            }

            @Override
            public void mouseExited(MouseEvent event) {
                hovered = false;
                surface.repaint();
            }
        });
    }

    /** 画面に載せるための Swing 部品。 */
    public JComponent component() {
        return surface;
    }

    /** 押されたことの宛先を 1 度だけ結ぶ。 */
    public void onPressed(Runnable action) {
        Objects.requireNonNull(action, "action");
        surface.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                action.run();
            }
        });
    }

    /** 配色を反映する。 */
    public void renderColours(Palette colours) {
        this.palette = Objects.requireNonNull(colours, "colours");
        surface.repaint();
    }

    private void paintButton(Graphics graphics) {
        Graphics2D canvas = (Graphics2D) graphics.create();
        canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (hovered) {
            canvas.setColor(palette.wash());
            canvas.fill(new RoundRectangle2D.Double(0, 0, HIT, HIT, ARC, ARC));
        }
        Color ink = hovered && icon == ChromeIcon.CLOSE ? palette.warning() : palette.textMuted();
        ChromeIconPainter.paint(canvas, icon, ink, new IconBox((HIT - ICON) / 2, (HIT - ICON) / 2, ICON));
        canvas.dispose();
    }
}
