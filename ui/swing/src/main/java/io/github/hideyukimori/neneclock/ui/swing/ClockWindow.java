package io.github.hideyukimori.neneclock.ui.swing;

import io.github.hideyukimori.neneclock.application.ClockFaceExtent;
import io.github.hideyukimori.neneclock.domain.UserSettings;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
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
 * <p>🔴 窓は**常に不透明**である（ADR 0012）。アルファ付きの背景を窓に頼まない。頼むと色が不透明でも
 * 合成層（WSLg / Windows）から見てアルファ付きの窓になり、そこで白が混じる（Issue #64・実測は #57）。
 * 角丸は {@code setShape} の切り抜きで作る。可否を読む手段はこのアプリに無い（ADR 0006）ので、
 * **試して、断られたら角のまま描く**。
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

    private @Nullable WindowDrag drag;
    private @Nullable GraphicsConfiguration screen;

    /** 窓を組み立てる。表示内容は {@code render*} が決める。 */
    public ClockWindow(ClockPanel clockPanel, WindowChrome chrome) {
        this.clockPanel = Objects.requireNonNull(clockPanel, "clockPanel");
        this.chrome = Objects.requireNonNull(chrome, "chrome");
        frame.setUndecorated(true);
        frame.setIconImages(AppIcon.images());
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setContentPane(clockPanel.component());
        frame.setSize(new Dimension(INITIAL_WIDTH, INITIAL_HEIGHT));
        frame.setMinimumSize(new Dimension(MINIMUM_WIDTH, MINIMUM_HEIGHT));
        frame.setLocationRelativeTo(null);
        frame.getLayeredPane().add(chrome.component(), JLayeredPane.PALETTE_LAYER);
        listenToThePointer();
        layOutChrome();
        roundTheCorners();
    }

    /** 設定を窓へ反映する。UI 状態の反映経路はここ 1 本（CNF-004）。 */
    public void renderSettings(UserSettings settings, ClockFaceExtent extent) {
        Objects.requireNonNull(settings, "settings");
        Objects.requireNonNull(extent, "extent");
        boolean topmost =
                switch (settings.windowTopmost()) {
                    case ENABLED -> true;
                    case DISABLED -> false;
                };
        frame.setAlwaysOnTop(topmost);
        clockPanel.renderSettings(settings, extent);
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
     * だから大きさは設定に従う。下限は FR-030 の最小サイズである。**下限は広げる向きにしか
     * 効かない**ので、文字が切れることはない。余白と文字を最小にしたときだけ、窓はこの下限で止まる。
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
        screen = frame.getGraphicsConfiguration();
    }

    /** 窓を閉じる。常駐スレッドを残さないのは合成ルートの仕事（FR-030）。 */
    public void close() {
        frame.dispose();
    }

    /** モーダルを出すときの親。 */
    public JFrame owner() {
        return frame;
    }

    private void listenToThePointer() {
        MouseAdapter pointer = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                drag = WindowDrag.grabbedAt(pointerOf(event), frame.getBounds());
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                drag = null;
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
        listenToTheFrame();
    }

    /** 窓そのものの動きを聞く。大きさは寸法が変わったとき、画面は載り換えたときに見る（ADR 0018）。 */
    private void listenToTheFrame() {
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                layOutChrome();
                roundTheCorners();
            }

            @Override
            public void componentMoved(ComponentEvent event) {
                followTheScreen();
            }
        });
    }

    /**
     * 載っている画面が変わったことに気づき、そのとき大きさを設定から決め直す（ADR 0018）。
     *
     * <p>ADR 0008 は「窓の大きさは設定に従う」と決めたが、その約束は設定を変えた瞬間にしか
     * 守られていなかった。画面の拡大率が変わるのも、窓の寸法が設定から外れる瞬間である。
     *
     * <p>読むのは {@code frame.getGraphicsConfiguration()}、すなわち<b>この窓がいまどこに居るか</b>
     * である。画面の一覧は尋ねない（{@code GraphicsEnvironment} の禁止は維持する・ARC-007）。
     */
    private void followTheScreen() {
        GraphicsConfiguration moved = frame.getGraphicsConfiguration();
        if (moved == null || moved.equals(screen)) {
            return;
        }
        screen = moved;
        fitToClock();
    }

    /**
     * 掴んだ点を保ったまま窓を動かす。
     *
     * <p>位置は「ポインタの画面座標 − 掴み点」で<b>毎回決め直す</b>。窓の現在位置を読んで足し込む
     * 形にすると、拡大率の違う画面をまたぐ数百ミリ秒のあいだに尺度の違う数を足し、その誤差が
     * 累積して戻らなくなる（ADR 0018）。
     *
     * <p>画面が変わった直後と、窓の大きさが掴んだときから変わったときは<b>動かさない</b>。
     * 掴み点を取り直して、次のイベントから続ける。
     */
    private void dragTo(MouseEvent event) {
        WindowDrag grabbed = drag;
        if (grabbed == null) {
            return;
        }
        Point pointer = pointerOf(event);
        if (onADifferentScreen() || !grabbed.stillFits(frame.getSize())) {
            drag = WindowDrag.grabbedAt(pointer, frame.getBounds());
            return;
        }
        frame.setLocation(grabbed.locationFor(pointer));
    }

    /** まだ {@link #followTheScreen()} が拾っていない画面の変化が起きていないか。 */
    private boolean onADifferentScreen() {
        GraphicsConfiguration now = frame.getGraphicsConfiguration();
        return now != null && !now.equals(screen);
    }

    /**
     * ポインタの画面座標。
     *
     * <p>🔴 {@code event.getX()} は窓の中の座標であり、<b>その窓の寸法と同じ尺度</b>を持つ。
     * 画面が変わった直後の窓は古い寸法のままなので、新しい尺度の値と混ぜられない。画面座標は
     * どちらの尺度にも属さない唯一の共通の物差しである。
     */
    private static Point pointerOf(MouseEvent event) {
        return new Point(event.getXOnScreen(), event.getYOnScreen());
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

    /** 角を丸める。切り抜きなので窓は不透明のまま。断られたら角のまま描く（ADR 0006）。 */
    private void roundTheCorners() {
        try {
            frame.setShape(new RoundRectangle2D.Double(0, 0, frame.getWidth(), frame.getHeight(), CORNER, CORNER));
        } catch (UnsupportedOperationException unsupported) {
            // 角丸に対応していない環境。角のまま描く。デザインはそれで成立するようにしてある。
        }
    }
}
