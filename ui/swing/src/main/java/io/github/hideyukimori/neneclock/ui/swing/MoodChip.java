package io.github.hideyukimori.neneclock.ui.swing;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.Objects;
import javax.swing.JComponent;
import javax.swing.JPanel;

/** 書体を雰囲気で絞り込む札。選ばれているものが反転した面になる。 */
public final class MoodChip {

    private static final int HEIGHT = 24;
    private static final int ARC = 7;
    private static final int SIDE_PAD = 11;
    private static final float LABEL_POINTS = 11f;

    private final String label;

    private final JPanel surface = new JPanel(null) {
        @Override
        protected void paintComponent(Graphics graphics) {
            paintChip(graphics);
        }
    };

    private Palette palette = Palette.from(io.github.hideyukimori.neneclock.domain.RgbColor.DEFAULT_BACKGROUND);
    private boolean selected;

    /** 表示名を決めて組み立てる。 */
    public MoodChip(String label) {
        this.label = Objects.requireNonNull(label, "label");
        surface.setOpaque(false);
        surface.setPreferredSize(new Dimension(measure(), HEIGHT));
        surface.setMaximumSize(new Dimension(measure(), HEIGHT));
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

    /** 選択状態と配色を反映する。 */
    public void renderSelection(boolean chosen, Palette colours) {
        this.selected = chosen;
        this.palette = Objects.requireNonNull(colours, "colours");
        surface.repaint();
    }

    private int measure() {
        FontMetrics metrics = surface.getFontMetrics(surface.getFont().deriveFont(LABEL_POINTS));
        return metrics.stringWidth(label) + SIDE_PAD * 2;
    }

    private void paintChip(Graphics graphics) {
        Graphics2D canvas = (Graphics2D) graphics.create();
        canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        canvas.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        canvas.setColor(selected ? palette.inverted() : palette.wash());
        canvas.fill(new RoundRectangle2D.Double(0, 0, surface.getWidth(), HEIGHT, ARC, ARC));
        canvas.setFont(surface.getFont().deriveFont(LABEL_POINTS));
        FontMetrics metrics = canvas.getFontMetrics();
        canvas.setColor(selected ? palette.onInverted() : palette.textMuted());
        canvas.drawString(
                label,
                (surface.getWidth() - metrics.stringWidth(label)) / 2,
                (HEIGHT + metrics.getAscent() - metrics.getDescent()) / 2);
        canvas.dispose();
    }
}
