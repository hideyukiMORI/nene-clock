package io.github.hideyukimori.neneclock.ui.swing;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Objects;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * 入切のつまみ。既製の {@code JCheckBox} を使わないのは、Metal / Nimbus の見た目が出るためである。
 *
 * <p>この部品は判断をしない。押されたことを伝えるだけで、状態は外から {@code render*} で入る。
 */
public final class ToggleSwitch {

    private static final int WIDTH = 38;
    private static final int HEIGHT = 21;
    private static final int KNOB = 15;
    private static final int INSET = 3;

    private final JPanel surface = new JPanel(null) {
        @Override
        protected void paintComponent(Graphics graphics) {
            paintSwitch(graphics);
        }
    };

    private Runnable toggled = () -> {};
    private Palette palette = Palette.from(io.github.hideyukimori.neneclock.domain.RgbColor.DEFAULT_BACKGROUND);
    private boolean on;

    /** 部品を組み立てる。 */
    public ToggleSwitch() {
        surface.setOpaque(false);
        surface.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        surface.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                toggled.run();
            }
        });
    }

    /** 画面に載せるための Swing 部品。 */
    public JComponent component() {
        return surface;
    }

    /** 押されたことの宛先を 1 度だけ結ぶ。 */
    public void onToggled(Runnable action) {
        this.toggled = Objects.requireNonNull(action, "action");
    }

    /** 状態と配色を反映する。 */
    public void renderState(boolean enabled, Palette colours) {
        this.on = enabled;
        this.palette = Objects.requireNonNull(colours, "colours");
        surface.repaint();
    }

    private void paintSwitch(Graphics graphics) {
        Graphics2D canvas = (Graphics2D) graphics.create();
        canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        canvas.setColor(on ? palette.accent() : palette.wash());
        canvas.fill(new RoundRectangle2D.Double(0, 0, WIDTH, HEIGHT, HEIGHT, HEIGHT));
        canvas.setColor(on ? palette.surface() : palette.textMuted());
        double left = on ? WIDTH - KNOB - INSET : INSET;
        canvas.fill(new Ellipse2D.Double(left, (HEIGHT - KNOB) / 2.0, KNOB, KNOB));
        canvas.dispose();
    }
}
