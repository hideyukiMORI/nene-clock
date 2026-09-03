package io.github.hideyukimori.neneclock.ui.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.Objects;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.Timer;
import org.jspecify.annotations.Nullable;

/**
 * ホバーしたときだけ現れる、ウィンドウ操作のレール（FR-047）。
 *
 * <p>設定と終了の 2 つを右上に横並びで持つ。移動は置かない——窓のどこを掴んでも動くので、
 * 置くと「押しても何も起きないボタン」になる（ADR 0010）。時計の文字に重ならない位置に置くのは
 * このクラスの外（{@link ClockWindow}）の仕事で、ここは自分の中だけを描く。
 *
 * <p>可変なのは「いま見えている度合い」と「どのアイコンの上にいるか」の 2 つだけで、
 * どちらも描画状態である（ARC-005 の Swing 区画）。
 */
public final class WindowChrome {

    /** アイコンの当たり判定の一辺。 */
    static final int HIT = 32;

    private static final int ICON = 18;
    private static final int GAP = 2;
    private static final int PAD = 3;
    private static final int RAIL_ARC = 11;
    private static final int BUTTON_ARC = 8;
    private static final int FADE_INTERVAL_MILLIS = 20;
    private static final float FADE_IN_STEP = 1.0f / 7;
    private static final float FADE_OUT_STEP = 1.0f / 12;
    private static final float HIDDEN = 0.0f;
    private static final float SHOWN = 1.0f;
    private static final int RAIL_ALPHA = 18;
    private static final int BUTTON_ALPHA = 30;
    private static final int ICON_ALPHA_HOVERED = 255;
    private static final int ICON_ALPHA_RESTING = 190;

    private static final List<ChromeIcon> ICONS = List.of(ChromeIcon.SETTINGS, ChromeIcon.CLOSE);

    private final Timer fade = new Timer(FADE_INTERVAL_MILLIS, event -> stepFade());

    private final JPanel surface = new JPanel(null) {
        @Override
        protected void paintComponent(Graphics graphics) {
            paintRail(graphics);
        }
    };

    private ChromeIntentSink sink = icon -> {};
    private Color ink = Color.WHITE;
    private Color danger = Color.RED;
    private float visibility = HIDDEN;
    private boolean wanted;
    private @Nullable ChromeIcon hovered;

    /** 部品を組み立てる。見た目は {@code render*} が決める。 */
    public WindowChrome() {
        surface.setOpaque(false);
        surface.setSize(new Dimension(ICONS.size() * HIT + (ICONS.size() - 1) * GAP + PAD * 2, HIT + PAD * 2));
        listen();
    }

    /** 画面に載せるための Swing 部品。 */
    public JComponent component() {
        return surface;
    }

    /** 意図の宛先を 1 度だけ結ぶ。合成ルートが呼ぶ。 */
    public void onChromeTriggered(ChromeIntentSink triggered) {
        this.sink = Objects.requireNonNull(triggered, "triggered");
    }

    /** 時計の文字色に合わせる。クロームは時計の一部であって、別の世界の部品ではない。 */
    public void renderColours(Color foreground, Color warning) {
        this.ink = Objects.requireNonNull(foreground, "foreground");
        this.danger = Objects.requireNonNull(warning, "warning");
        surface.repaint();
    }

    /** 見えるべきかどうかを伝える。実際の増減はフェードが行う。 */
    public void renderVisibility(boolean visible) {
        wanted = visible;
        if (!visible) {
            hovered = null;
        }
        fade.start();
    }

    /** いま完全に隠れているか。隠れている間はクリックを受け取らない。 */
    boolean isHidden() {
        return visibility <= HIDDEN;
    }

    private void paintRail(Graphics graphics) {
        if (isHidden()) {
            return;
        }
        Graphics2D canvas = (Graphics2D) graphics.create();
        TextRendering.smooth(canvas);
        canvas.setColor(withVisibility(ink, RAIL_ALPHA));
        canvas.fill(new RoundRectangle2D.Double(0, 0, surface.getWidth(), surface.getHeight(), RAIL_ARC, RAIL_ARC));
        for (int index = 0; index < ICONS.size(); index++) {
            paintButton(canvas, ICONS.get(index), PAD + index * (HIT + GAP));
        }
        canvas.dispose();
    }

    private void paintButton(Graphics2D canvas, ChromeIcon icon, int left) {
        boolean under = icon == hovered;
        if (under) {
            canvas.setColor(withVisibility(icon == ChromeIcon.CLOSE ? danger : ink, BUTTON_ALPHA));
            canvas.fill(new RoundRectangle2D.Double(left, PAD, HIT, HIT, BUTTON_ARC, BUTTON_ARC));
        }
        Color stroke = under && icon == ChromeIcon.CLOSE ? danger : ink;
        int inset = (HIT - ICON) / 2;
        ChromeIconPainter.paint(
                canvas,
                icon,
                withVisibility(stroke, under ? ICON_ALPHA_HOVERED : ICON_ALPHA_RESTING),
                new IconBox(left + inset, PAD + inset, ICON));
    }

    private Color withVisibility(Color base, int alpha) {
        return new Color(base.getRed(), base.getGreen(), base.getBlue(), Math.round(alpha * visibility));
    }

    private void listen() {
        MouseAdapter pointer = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent event) {
                // 🔴 レールの上にポインタが乗ったまま窓が出てくると、窓の側の enter / moved が
                //    一度も飛ばない。そのときクロームは隠れたままで、押しても何も起きない（実機で踏んだ）。
                renderVisibility(true);
                ChromeIcon under = iconAt(event.getX(), event.getY());
                if (under != hovered) {
                    hovered = under;
                    surface.repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent event) {
                hovered = null;
                surface.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent event) {
                ChromeIcon under = iconAt(event.getX(), event.getY());
                if (under != null && !isHidden()) {
                    sink.triggered(under);
                }
            }
        };
        surface.addMouseListener(pointer);
        surface.addMouseMotionListener(pointer);
    }

    private @Nullable ChromeIcon iconAt(int x, int y) {
        if (y < PAD || y > PAD + HIT) {
            return null;
        }
        for (int index = 0; index < ICONS.size(); index++) {
            int left = PAD + index * (HIT + GAP);
            if (x >= left && x < left + HIT) {
                return ICONS.get(index);
            }
        }
        return null;
    }

    private void stepFade() {
        float target = wanted ? SHOWN : HIDDEN;
        float step = wanted ? FADE_IN_STEP : FADE_OUT_STEP;
        visibility = wanted ? Math.min(target, visibility + step) : Math.max(target, visibility - step);
        if (visibility == target) {
            fade.stop();
        }
        surface.repaint();
    }
}
