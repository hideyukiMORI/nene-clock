package io.github.hideyukimori.neneclock.ui.swing;

import io.github.hideyukimori.neneclock.domain.FontSize;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Objects;
import java.util.function.IntConsumer;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * 文字の大きさを選ぶ帯。範囲は {@link FontSize} の下限と上限そのものなので、
 * この部品からは範囲外の値が出てこない（FR-041）。
 */
public final class SizeSlider {

    private static final int HEIGHT = 22;
    private static final int WIDTH = 220;
    private static final int TRACK = 4;
    private static final int KNOB = 14;

    private final JPanel surface = new JPanel(null) {
        @Override
        protected void paintComponent(Graphics graphics) {
            paintTrack(graphics);
        }
    };

    private IntConsumer moved = points -> {};
    private Palette palette = Palette.from(io.github.hideyukimori.neneclock.domain.RgbColor.DEFAULT_BACKGROUND);
    private int points = FontSize.DEFAULT.points();

    /** 部品を組み立てる。 */
    public SizeSlider() {
        surface.setOpaque(false);
        surface.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        MouseAdapter pointer = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                moved.accept(pointsAt(event.getX()));
            }

            @Override
            public void mouseDragged(MouseEvent event) {
                moved.accept(pointsAt(event.getX()));
            }
        };
        surface.addMouseListener(pointer);
        surface.addMouseMotionListener(pointer);
    }

    /** 画面に載せるための Swing 部品。 */
    public JComponent component() {
        return surface;
    }

    /** 動かされたことの宛先を 1 度だけ結ぶ。 */
    public void onMoved(IntConsumer action) {
        this.moved = Objects.requireNonNull(action, "action");
    }

    /** 現在値と配色を反映する。 */
    public void renderPoints(int current, Palette colours) {
        this.points = current;
        this.palette = Objects.requireNonNull(colours, "colours");
        surface.repaint();
    }

    private int pointsAt(int x) {
        int usable = Math.max(1, surface.getWidth() - KNOB);
        double ratio = Math.min(1.0, Math.max(0.0, (x - KNOB / 2.0) / usable));
        int span = FontSize.MAXIMUM_POINTS - FontSize.MINIMUM_POINTS;
        return FontSize.MINIMUM_POINTS + (int) Math.round(ratio * span);
    }

    private double ratioOfCurrent() {
        int span = FontSize.MAXIMUM_POINTS - FontSize.MINIMUM_POINTS;
        return (points - FontSize.MINIMUM_POINTS) / (double) span;
    }

    private void paintTrack(Graphics graphics) {
        Graphics2D canvas = (Graphics2D) graphics.create();
        TextRendering.smooth(canvas);
        double top = (HEIGHT - TRACK) / 2.0;
        double usable = Math.max(1, surface.getWidth() - KNOB);
        double filled = ratioOfCurrent() * usable;
        canvas.setColor(palette.wash());
        canvas.fill(new RoundRectangle2D.Double(KNOB / 2.0, top, usable, TRACK, TRACK, TRACK));
        canvas.setColor(palette.accent());
        canvas.fill(new RoundRectangle2D.Double(KNOB / 2.0, top, Math.max(TRACK, filled), TRACK, TRACK, TRACK));
        canvas.setColor(palette.text());
        canvas.fill(new Ellipse2D.Double(filled, (HEIGHT - KNOB) / 2.0, KNOB, KNOB));
        canvas.dispose();
    }
}
