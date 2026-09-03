package io.github.hideyukimori.neneclock.ui.swing;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.Objects;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.jspecify.annotations.Nullable;

/** 書体を雰囲気で絞り込む札。選ばれているものが反転した面になる。 */
public final class MoodChip {

    private static final int HEIGHT = 24;
    private static final int ARC = 7;
    private static final int SIDE_PAD = 11;
    private static final float LABEL_POINTS = 11f;

    private String label = "";

    private final JPanel surface = new JPanel(null) {
        @Override
        protected void paintComponent(Graphics graphics) {
            paintChip(graphics);
        }
    };

    private @Nullable UiTheme theme;
    private boolean selected;

    /** 組み立てる。表示名は言語で変わるので {@code render*} が持ち込む。 */
    public MoodChip() {
        surface.setOpaque(false);
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

    /** 表示名・選択状態・配色を反映する。 */
    public void renderSelection(String text, boolean chosen, UiTheme shown) {
        this.label = Objects.requireNonNull(text, "text");
        this.selected = chosen;
        this.theme = Objects.requireNonNull(shown, "shown");
        int width = measure(shown);
        surface.setPreferredSize(new Dimension(width, HEIGHT));
        surface.setMaximumSize(new Dimension(width, HEIGHT));
        surface.revalidate();
        surface.repaint();
    }

    private int measure(UiTheme shown) {
        FontMetrics metrics = surface.getFontMetrics(shown.font(LABEL_POINTS));
        return metrics.stringWidth(label) + SIDE_PAD * 2;
    }

    private void paintChip(Graphics graphics) {
        UiTheme theme = this.theme;
        if (theme == null) {
            return;
        }
        Palette palette = theme.palette();
        Graphics2D canvas = (Graphics2D) graphics.create();
        TextRendering.smooth(canvas);
        canvas.setColor(selected ? palette.inverted() : palette.wash());
        canvas.fill(new RoundRectangle2D.Double(0, 0, surface.getWidth(), HEIGHT, ARC, ARC));
        canvas.setFont(theme.font(LABEL_POINTS));
        FontMetrics metrics = canvas.getFontMetrics();
        canvas.setColor(selected ? palette.onInverted() : palette.textMuted());
        canvas.drawString(
                label,
                (surface.getWidth() - metrics.stringWidth(label)) / 2,
                (HEIGHT + metrics.getAscent() - metrics.getDescent()) / 2);
        canvas.dispose();
    }
}
