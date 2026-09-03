package io.github.hideyukimori.neneclock.ui.swing;

import io.github.hideyukimori.neneclock.domain.RgbaColor;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.jspecify.annotations.Nullable;

/** 色見本 1 つ。選ばれているものだけが縁取りを持つ。 */
public final class ColourSwatch {

    private static final int HEIGHT = 34;
    private static final int ARC = 7;
    private static final float RING = 2f;
    private static final int CHECKER = 6;
    private static final double INSET = 2.0;
    private static final double DOUBLE_INSET = 4.0;

    private final RgbaColor value;

    private final JPanel surface = new JPanel(null) {
        @Override
        protected void paintComponent(Graphics graphics) {
            paintSwatch(graphics);
        }
    };

    private @Nullable Palette palette;
    private boolean selected;

    /** 1 色に対する見本を作る。 */
    public ColourSwatch(RgbaColor value) {
        this.value = Objects.requireNonNull(value, "value");
        surface.setOpaque(false);
        surface.setPreferredSize(new Dimension(0, HEIGHT));
    }

    /** 画面に載せるための Swing 部品。 */
    public JComponent component() {
        return surface;
    }

    /** 選ばれたことの宛先を 1 度だけ結ぶ。 */
    public void onChosen(Consumer<RgbaColor> action) {
        Objects.requireNonNull(action, "action");
        surface.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                action.accept(value);
            }
        });
    }

    /** この見本がその色かどうか。 */
    public boolean holds(RgbaColor other) {
        return value.equals(other);
    }

    /** 選択状態と配色を反映する。 */
    public void renderSelection(boolean chosen, UiTheme theme) {
        this.selected = chosen;
        this.palette = Objects.requireNonNull(theme, "theme").palette();
        surface.repaint();
    }

    /** 透けている色の下に市松を敷く。敷かないと、薄い色と白い色の区別がつかない。 */
    private static void paintChecker(Graphics2D canvas, RoundRectangle2D shape, Palette palette) {
        Graphics2D checker = (Graphics2D) canvas.create();
        checker.clip(shape);
        checker.setColor(palette.surface());
        checker.fill(shape);
        checker.setColor(palette.wash());
        for (int y = (int) shape.getY(); y < shape.getMaxY(); y += CHECKER) {
            for (int x = (int) shape.getX(); x < shape.getMaxX(); x += CHECKER) {
                if (((x / CHECKER) + (y / CHECKER)) % 2 == 0) {
                    checker.fillRect(x, y, CHECKER, CHECKER);
                }
            }
        }
        checker.dispose();
    }

    private void paintSwatch(Graphics graphics) {
        Palette palette = this.palette;
        if (palette == null) {
            return;
        }
        Graphics2D canvas = (Graphics2D) graphics.create();
        TextRendering.smooth(canvas);
        int width = surface.getWidth();
        RoundRectangle2D shape =
                new RoundRectangle2D.Double(INSET, INSET, width - DOUBLE_INSET, HEIGHT - DOUBLE_INSET, ARC, ARC);
        paintChecker(canvas, shape, palette);
        canvas.setColor(AwtColour.of(value));
        canvas.fill(shape);
        canvas.setStroke(new BasicStroke(RING));
        canvas.setColor(selected ? palette.accent() : palette.hairline());
        canvas.draw(new RoundRectangle2D.Double(INSET, INSET, width - DOUBLE_INSET, HEIGHT - DOUBLE_INSET, ARC, ARC));
        canvas.dispose();
    }
}
