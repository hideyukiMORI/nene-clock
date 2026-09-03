package io.github.hideyukimori.neneclock.ui.swing;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.Objects;
import java.util.function.IntConsumer;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.jspecify.annotations.Nullable;

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

    private final int segments;

    private IntConsumer chosen = index -> {};
    private List<String> labels = List.of();
    private @Nullable UiTheme theme;
    private int selected;

    /** 分節の数を決めて組み立てる。文言は言語で変わるので {@code render*} が持ち込む。 */
    public SegmentedChoice(int segments) {
        this.segments = segments;
        surface.setOpaque(false);
        surface.setPreferredSize(new Dimension(widthOf(), HEIGHT));
        surface.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                chosen.accept(Math.min(segments - 1, event.getX() / segmentWidth()));
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

    /** 選択・文言・配色を反映する。 */
    public void renderSelection(int index, List<String> texts, UiTheme shown) {
        this.selected = index;
        this.labels = List.copyOf(Objects.requireNonNull(texts, "texts"));
        this.theme = Objects.requireNonNull(shown, "shown");
        surface.repaint();
    }

    private int segmentWidth() {
        return Math.max(1, (widthOf() - PAD * 2) / segments);
    }

    private int widthOf() {
        return segments * (SIDE_PAD * 2 + SEGMENT_TEXT_ROOM) + PAD * 2;
    }

    private void paintSegments(Graphics graphics) {
        UiTheme theme = this.theme;
        if (theme == null) {
            return;
        }
        Palette palette = theme.palette();
        Graphics2D canvas = (Graphics2D) graphics.create();
        TextRendering.smooth(canvas);
        canvas.setColor(palette.wash());
        canvas.fill(new RoundRectangle2D.Double(0, 0, surface.getWidth(), HEIGHT, OUTER_ARC, OUTER_ARC));
        Font label = theme.font(LABEL_POINTS);
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
