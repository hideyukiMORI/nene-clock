package io.github.hideyukimori.neneclock.ui.swing;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.Objects;
import java.util.function.IntConsumer;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * 2 つ以上の選択肢を横並びにした分節。選ばれているものが反転した面になる。
 *
 * <p>{@code JComboBox} を使わないのは、閉じた 2 択のために一覧を開かせるのが操作として重いのと、
 * 既製の見た目が出るためである。
 */
public final class SegmentedChoice {

    private static final int HEIGHT = 27;
    private static final int PAD = 2;
    private static final int SEGMENT_ARC = 6;
    private static final int OUTER_ARC = 8;
    private static final int SIDE_PAD = 12;
    private static final float LABEL_POINTS = 12f;
    private static final int SEGMENT_TEXT_ROOM = 44;

    private final JPanel surface = new JPanel(null) {
        @Override
        protected void paintComponent(Graphics graphics) {
            paintSegments(graphics);
        }
    };

    private final List<String> labels;

    private IntConsumer chosen = index -> {};
    private Palette palette = Palette.from(io.github.hideyukimori.neneclock.domain.RgbColor.DEFAULT_BACKGROUND);
    private int selected;

    /** 選択肢の並び順を決めて組み立てる。並びは変わらない。 */
    public SegmentedChoice(List<String> segments) {
        this.labels = List.copyOf(Objects.requireNonNull(segments, "segments"));
        surface.setOpaque(false);
        surface.setPreferredSize(new Dimension(widthOf(), HEIGHT));
        surface.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                chosen.accept(Math.min(labels.size() - 1, event.getX() / segmentWidth()));
            }
        });
    }

    /** 画面に載せるための Swing 部品。 */
    public JComponent component() {
        return surface;
    }

    /** 選ばれたことの宛先を 1 度だけ結ぶ。 */
    public void onChosen(IntConsumer action) {
        this.chosen = Objects.requireNonNull(action, "action");
    }

    /** 選択と配色を反映する。 */
    public void renderSelection(int index, Palette colours) {
        this.selected = index;
        this.palette = Objects.requireNonNull(colours, "colours");
        surface.repaint();
    }

    private int segmentWidth() {
        return Math.max(1, (widthOf() - PAD * 2) / labels.size());
    }

    private int widthOf() {
        return labels.size() * (SIDE_PAD * 2 + SEGMENT_TEXT_ROOM) + PAD * 2;
    }

    private void paintSegments(Graphics graphics) {
        Graphics2D canvas = (Graphics2D) graphics.create();
        canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        canvas.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        canvas.setColor(palette.wash());
        canvas.fill(new RoundRectangle2D.Double(0, 0, surface.getWidth(), HEIGHT, OUTER_ARC, OUTER_ARC));
        Font label = surface.getFont().deriveFont(LABEL_POINTS);
        canvas.setFont(label);
        FontMetrics metrics = canvas.getFontMetrics(label);
        int width = segmentWidth();
        for (int index = 0; index < labels.size(); index++) {
            int left = PAD + index * width;
            if (index == selected) {
                canvas.setColor(palette.inverted());
                canvas.fill(
                        new RoundRectangle2D.Double(left, PAD, width, HEIGHT - PAD * 2.0, SEGMENT_ARC, SEGMENT_ARC));
            }
            canvas.setColor(index == selected ? palette.onInverted() : palette.textMuted());
            String text = labels.get(index);
            canvas.drawString(
                    text,
                    left + (width - metrics.stringWidth(text)) / 2,
                    (HEIGHT + metrics.getAscent() - metrics.getDescent()) / 2);
        }
        canvas.dispose();
    }
}
