package io.github.hideyukimori.neneclock.ui.swing;

import io.github.hideyukimori.neneclock.domain.UserSettings;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.IllegalComponentStateException;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.Objects;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import org.jspecify.annotations.Nullable;

/**
 * 枠の無いウィンドウ（FR-047）。合成と生存期間だけを持つ（ARC-011）。
 *
 * <p>OS のタイトルバーを持たないので、移動と終了の手段は自分で持つ。窓のどこを掴んでも動き、
 * ホバーしたときだけ操作用のクローム（{@link WindowChrome}）が現れる。
 *
 * <p>半透明と角丸は環境依存である。可否を読む手段（{@code GraphicsEnvironment}）はこのアプリに
 * 無い（ADR 0006）ので、**試して、断られたら諦める**形にしてある（ADR 0011）。
 *
 * <p>半透明が使えるなら、地も角丸も {@link ClockPanel} が描く（縁が滑らかになる）。
 * 使えないなら、地は不透明で描き、角丸は {@code setShape} の切り抜きで作る。
 */
public final class ClockWindow {

    private static final int INITIAL_WIDTH = 480;
    private static final int INITIAL_HEIGHT = 240;
    private static final int MINIMUM_WIDTH = 320;
    private static final int MINIMUM_HEIGHT = 160;
    private static final int CORNER = ClockPanel.CORNER;
    private static final int CHROME_MARGIN = 10;

    private final JFrame frame = new JFrame("NeNe Clock");
    private final ClockPanel clockPanel;
    private final WindowChrome chrome;

    private final boolean translucent;

    private @Nullable Point grabbedAt;

    /** 窓を組み立てる。表示内容は {@code render*} が決める。 */
    public ClockWindow(ClockPanel clockPanel, WindowChrome chrome) {
        this.clockPanel = Objects.requireNonNull(clockPanel, "clockPanel");
        this.chrome = Objects.requireNonNull(chrome, "chrome");
        frame.setUndecorated(true);
        frame.setIconImages(AppIcon.images());
        this.translucent = askForTranslucency();
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setContentPane(clockPanel.component());
        frame.setSize(new Dimension(INITIAL_WIDTH, INITIAL_HEIGHT));
        frame.setMinimumSize(new Dimension(MINIMUM_WIDTH, MINIMUM_HEIGHT));
        frame.setLocationRelativeTo(null);
        frame.getLayeredPane().add(chrome.component(), JLayeredPane.PALETTE_LAYER);
        listen();
        layOutChrome();
        roundTheCorners();
    }

    /**
     * 画素ごとの半透明を頼んでみる。
     *
     * <p>断られたら不透明で描く。可否を先に読む手段は無いので、頼んで返事を見るしかない。
     */
    private boolean askForTranslucency() {
        try {
            frame.setBackground(new Color(0, 0, 0, 0));
            return true;
        } catch (UnsupportedOperationException | IllegalComponentStateException refused) {
            return false;
        }
    }

    /** 設定を窓へ反映する。UI 状態の反映経路はここ 1 本（CNF-004）。 */
    public void renderSettings(UserSettings settings) {
        Objects.requireNonNull(settings, "settings");
        boolean topmost =
                switch (settings.windowTopmost()) {
                    case ENABLED -> true;
                    case DISABLED -> false;
                };
        frame.setAlwaysOnTop(topmost);
        clockPanel.renderSettings(settings);
        fitToClock();
        chrome.renderColours(
                AwtColour.of(settings.fontColor()),
                Palette.from(settings.backgroundColor()).warning());
    }

    /**
     * 時刻が収まる大きさに合わせる。
     *
     * <p>枠が無いので、利用者が窓の端を掴んで広げることができない。文字を大きくしたときに
     * 「05:14:..」と切れて出るのは、窓が時計そのものであるという前提と噛み合わない（FR-047）。
     * だから大きさは設定に従う。下限は FR-030 の最小サイズである。
     */
    private void fitToClock() {
        Dimension wanted = frame.getContentPane().getPreferredSize();
        frame.setSize(Math.max(MINIMUM_WIDTH, wanted.width), Math.max(MINIMUM_HEIGHT, wanted.height));
        layOutChrome();
        roundTheCorners();
    }

    /** 窓を表示する。EDT から呼ぶこと（SWG-001）。 */
    public void display() {
        frame.setVisible(true);
    }

    /** 窓を閉じる。常駐スレッドを残さないのは合成ルートの仕事（FR-030）。 */
    public void close() {
        frame.dispose();
    }

    /** モーダルを出すときの親。 */
    public JFrame owner() {
        return frame;
    }

    private void listen() {
        MouseAdapter pointer = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                grabbedAt = event.getPoint();
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                grabbedAt = null;
            }

            @Override
            public void mouseDragged(MouseEvent event) {
                dragTo(event);
            }

            @Override
            public void mouseEntered(MouseEvent event) {
                chrome.renderVisibility(true);
            }

            // 動いただけでもクロームを出す。
            // 🔴 entered だけに頼ると、窓が「すでにポインタのある位置に」出てきたときに
            //    一度も enter が飛ばず、クロームが永久に出てこない（実機で踏んだ）。
            @Override
            public void mouseMoved(MouseEvent event) {
                chrome.renderVisibility(true);
            }

            @Override
            public void mouseExited(MouseEvent event) {
                chrome.renderVisibility(stillInside(event));
            }
        };
        frame.getContentPane().addMouseListener(pointer);
        frame.getContentPane().addMouseMotionListener(pointer);
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                layOutChrome();
                roundTheCorners();
            }
        });
    }

    /**
     * 掴んだ点を保ったまま窓を動かす。
     *
     * <p>掴んだ点は「押した瞬間の、窓の中での座標」である。動かすたびに窓の位置を足すのは、
     * ドラッグ中のイベント座標が動いた後の窓を基準にしているためである。
     */
    private void dragTo(MouseEvent event) {
        Point origin = grabbedAt;
        if (origin == null) {
            return;
        }
        Point where = frame.getLocation();
        frame.setLocation(where.x + event.getX() - origin.x, where.y + event.getY() - origin.y);
    }

    /** 子部品へ入ったときも exit が飛ぶので、本当に窓の外へ出たのかを見る。 */
    private boolean stillInside(MouseEvent event) {
        Point onFrame = SwingUtilities.convertPoint((java.awt.Component) event.getSource(), event.getPoint(), frame);
        return onFrame.x >= 0 && onFrame.y >= 0 && onFrame.x < frame.getWidth() && onFrame.y < frame.getHeight();
    }

    private void layOutChrome() {
        Dimension size = chrome.component().getSize();
        chrome.component()
                .setBounds(frame.getWidth() - size.width - CHROME_MARGIN, CHROME_MARGIN, size.width, size.height);
    }

    /** 角を丸める。半透明が使えるなら {@link ClockPanel} が描くので、切り抜きは要らない。 */
    private void roundTheCorners() {
        if (translucent) {
            return;
        }
        try {
            frame.setShape(new RoundRectangle2D.Double(0, 0, frame.getWidth(), frame.getHeight(), CORNER, CORNER));
        } catch (UnsupportedOperationException unsupported) {
            // 角丸に対応していない環境。角のまま描く。デザインはそれで成立するようにしてある。
        }
    }
}
