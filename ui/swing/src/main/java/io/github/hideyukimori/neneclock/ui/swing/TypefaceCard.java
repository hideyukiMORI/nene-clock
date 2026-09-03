package io.github.hideyukimori.neneclock.ui.swing;

import io.github.hideyukimori.neneclock.domain.Typeface;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
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

/**
 * 書体ピッカーの札 1 枚。**その書体自身で** {@code 12:34} を描く。
 *
 * <p>名前の一覧では雰囲気が分からない。選ぶために要るのは名前ではなく見た目である。
 */
public final class TypefaceCard {

    private static final int HEIGHT = 62;
    private static final int ARC = 9;
    private static final float SAMPLE_POINTS = 21f;
    private static final float NAME_POINTS = 9f;
    private static final int SAMPLE_BASELINE = 32;
    private static final int NAME_BASELINE = 50;
    private static final String SAMPLE = "12:34";

    private final Typeface typeface;
    private final Font sample;

    private final JPanel surface = new JPanel(null) {
        @Override
        protected void paintComponent(Graphics graphics) {
            paintCard(graphics);
        }
    };

    private Palette palette = Palette.from(io.github.hideyukimori.neneclock.domain.RgbColor.DEFAULT_BACKGROUND);
    private boolean selected;

    /** 1 つの書体に対する札を作る。 */
    public TypefaceCard(Typeface typeface, TypefaceFontLoader typefaces) {
        this.typeface = Objects.requireNonNull(typeface, "typeface");
        this.sample = typefaces.load(typeface, Math.round(SAMPLE_POINTS));
        surface.setOpaque(false);
        surface.setPreferredSize(new Dimension(0, HEIGHT));
    }

    /** 画面に載せるための Swing 部品。 */
    public JComponent component() {
        return surface;
    }

    /** 選ばれたことの宛先を 1 度だけ結ぶ。 */
    public void onChosen(Consumer<Typeface> action) {
        Objects.requireNonNull(action, "action");
        surface.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                action.accept(typeface);
            }
        });
    }

    /** 選択状態と配色を反映する。 */
    public void renderSelection(boolean chosen, Palette colours) {
        this.selected = chosen;
        this.palette = Objects.requireNonNull(colours, "colours");
        surface.repaint();
    }

    private void paintCard(Graphics graphics) {
        Graphics2D canvas = (Graphics2D) graphics.create();
        canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        canvas.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int width = surface.getWidth();
        canvas.setColor(selected ? palette.accent() : palette.wash());
        canvas.fill(new RoundRectangle2D.Double(0, 0, width, HEIGHT, ARC, ARC));
        canvas.setColor(selected ? palette.accent() : palette.hairline());
        canvas.draw(new RoundRectangle2D.Double(0, 0, width - 1.0, HEIGHT - 1.0, ARC, ARC));
        canvas.setColor(selected ? palette.onInverted() : palette.text());
        canvas.setFont(sample);
        canvas.drawString(SAMPLE, centred(canvas, sample, SAMPLE, width), SAMPLE_BASELINE);
        Font name = surface.getFont().deriveFont(NAME_POINTS);
        canvas.setFont(name);
        canvas.setColor(selected ? palette.onInverted() : palette.textMuted());
        canvas.drawString(typeface.displayName(), centred(canvas, name, typeface.displayName(), width), NAME_BASELINE);
        canvas.dispose();
    }

    private static int centred(Graphics2D canvas, Font font, String text, int width) {
        FontMetrics metrics = canvas.getFontMetrics(font);
        return (width - metrics.stringWidth(text)) / 2;
    }
}
