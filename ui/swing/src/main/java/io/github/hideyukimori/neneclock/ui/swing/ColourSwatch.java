package io.github.hideyukimori.neneclock.ui.swing;

import io.github.hideyukimori.neneclock.domain.RgbColor;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.JComponent;
import javax.swing.JPanel;

/** 色見本 1 つ。選ばれているものだけが縁取りを持つ。 */
public final class ColourSwatch {

    private static final int HEIGHT = 34;
    private static final int ARC = 7;
    private static final float RING = 2f;
    private static final double INSET = 2.0;
    private static final double DOUBLE_INSET = 4.0;

    private final RgbColor value;

    private final JPanel surface = new JPanel(null) {
        @Override
        protected void paintComponent(Graphics graphics) {
            paintSwatch(graphics);
        }
    };

    private Palette palette = Palette.from(RgbColor.DEFAULT_BACKGROUND);
    private boolean selected;

    /** 1 色に対する見本を作る。 */
    public ColourSwatch(RgbColor value) {
        this.value = Objects.requireNonNull(value, "value");
        surface.setOpaque(false);
        surface.setPreferredSize(new Dimension(0, HEIGHT));
    }

    /** 画面に載せるための Swing 部品。 */
    public JComponent component() {
        return surface;
    }

    /** 選ばれたことの宛先を 1 度だけ結ぶ。 */
    public void onChosen(Consumer<RgbColor> action) {
        Objects.requireNonNull(action, "action");
        surface.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                action.accept(value);
            }
        });
    }

    /** この見本がその色かどうか。 */
    public boolean holds(RgbColor other) {
        return value.equals(other);
    }

    /** 選択状態と配色を反映する。 */
    public void renderSelection(boolean chosen, Palette colours) {
        this.selected = chosen;
        this.palette = Objects.requireNonNull(colours, "colours");
        surface.repaint();
    }

    private void paintSwatch(Graphics graphics) {
        Graphics2D canvas = (Graphics2D) graphics.create();
        canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int width = surface.getWidth();
        canvas.setColor(new Color(value.red(), value.green(), value.blue()));
        canvas.fill(new RoundRectangle2D.Double(INSET, INSET, width - DOUBLE_INSET, HEIGHT - DOUBLE_INSET, ARC, ARC));
        canvas.setStroke(new BasicStroke(RING));
        canvas.setColor(selected ? palette.accent() : palette.hairline());
        canvas.draw(new RoundRectangle2D.Double(INSET, INSET, width - DOUBLE_INSET, HEIGHT - DOUBLE_INSET, ARC, ARC));
        canvas.dispose();
    }
}
