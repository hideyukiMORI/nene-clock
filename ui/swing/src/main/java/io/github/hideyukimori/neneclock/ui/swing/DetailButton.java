package io.github.hideyukimori.neneclock.ui.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Objects;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.jspecify.annotations.Nullable;

/**
 * 「押すと別の画面へ進む」行の右側。値と、色見本と、進む向きの山形を描く。
 *
 * <p>書体・文字色・背景色は選択肢が多いので、行の中で選ばせずに専用の画面へ進む。
 */
public final class DetailButton {

    private static final int HEIGHT = 30;
    private static final int WIDTH = 250;
    private static final int SWATCH = 20;
    private static final int SWATCH_ARC = 6;
    private static final int CHEVRON = 12;
    private static final int GAP = 10;
    private static final float VALUE_POINTS = 12f;
    private static final float CHEVRON_STROKE = 1.8f;

    private final JPanel surface = new JPanel(null) {
        @Override
        protected void paintComponent(Graphics graphics) {
            paintRow(graphics);
        }
    };

    private Runnable pressed = () -> {};
    private @Nullable UiTheme theme;
    private String value = "";
    private @Nullable Color swatch;
    private @Nullable Font sample;

    /** 部品を組み立てる。 */
    public DetailButton() {
        surface.setOpaque(false);
        surface.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        surface.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                pressed.run();
            }
        });
    }

    /** 画面に載せるための Swing 部品。 */
    public JComponent component() {
        return surface;
    }

    /** 押されたことの宛先を 1 度だけ結ぶ。 */
    public void onPressed(Runnable action) {
        this.pressed = Objects.requireNonNull(action, "action");
    }

    /** 値・色見本・見本書体・配色を反映する。色見本と見本書体は無くてよい。 */
    public void renderValue(DetailValue shown, UiTheme colours) {
        this.value = shown.text();
        this.swatch = shown.swatch();
        this.sample = shown.sample();
        this.theme = Objects.requireNonNull(colours, "colours");
        surface.repaint();
    }

    private void paintRow(Graphics graphics) {
        UiTheme theme = this.theme;
        if (theme == null) {
            return;
        }
        Palette palette = theme.palette();
        Graphics2D canvas = (Graphics2D) graphics.create();
        TextRendering.smooth(canvas);
        int right = surface.getWidth();
        paintChevron(canvas, right - CHEVRON, palette);
        right -= CHEVRON + GAP;
        canvas.setFont(theme.font(VALUE_POINTS));
        FontMetrics metrics = canvas.getFontMetrics();
        canvas.setColor(palette.textMuted());
        int baseline = (HEIGHT + metrics.getAscent() - metrics.getDescent()) / 2;
        canvas.drawString(value, right - metrics.stringWidth(value), baseline);
        right -= metrics.stringWidth(value) + GAP;
        right = paintSwatch(canvas, right, palette);
        paintSample(canvas, right, palette);
        canvas.dispose();
    }

    private int paintSwatch(Graphics2D canvas, int right, Palette palette) {
        Color shown = swatch;
        if (shown == null) {
            return right;
        }
        canvas.setColor(shown);
        double top = (HEIGHT - SWATCH) / 2.0;
        canvas.fill(new RoundRectangle2D.Double(right - SWATCH, top, SWATCH, SWATCH, SWATCH_ARC, SWATCH_ARC));
        canvas.setColor(palette.hairline());
        canvas.draw(
                new RoundRectangle2D.Double(right - SWATCH, top, SWATCH - 1.0, SWATCH - 1.0, SWATCH_ARC, SWATCH_ARC));
        return right - SWATCH - GAP;
    }

    private void paintSample(Graphics2D canvas, int right, Palette palette) {
        Font shown = sample;
        if (shown == null) {
            return;
        }
        canvas.setFont(shown);
        FontMetrics metrics = canvas.getFontMetrics();
        canvas.setColor(palette.text());
        canvas.drawString(
                "12:34",
                right - metrics.stringWidth("12:34"),
                (HEIGHT + metrics.getAscent() - metrics.getDescent()) / 2);
    }

    private void paintChevron(Graphics2D canvas, int left, Palette palette) {
        Path2D chevron = new Path2D.Double();
        double middle = HEIGHT / 2.0;
        chevron.moveTo(left, middle - CHEVRON / 2.0);
        chevron.lineTo(left + CHEVRON / 2.0, middle);
        chevron.lineTo(left, middle + CHEVRON / 2.0);
        canvas.setColor(palette.textFaint());
        canvas.setStroke(new java.awt.BasicStroke(
                CHEVRON_STROKE, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
        canvas.draw(chevron);
    }
}
