package io.github.hideyukimori.neneclock.ui.swing;

import io.github.hideyukimori.neneclock.application.ClockFaceExtent;
import io.github.hideyukimori.neneclock.domain.UserSettings;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
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
 *
 * <p>🔴 ドラッグは実ピクセルで決める（ADR 0019）。ポインタは {@link MouseInfo} から取り、位置は
 * {@link WindowDrag} が決める。**窓が別の画面へ移ったことを知る手段は無い**（実測: JDK は窓の
 * {@code GraphicsConfiguration} を差し替えない）。だから画面の載り換えを検出する経路は持たず、
 * 膨らみは「大きさが設定と違っていたら戻す」で潰す。
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

    /** 動かし方は起動時に 1 度だけ決まる（Issue #100・測定で選ぶための一時的な分岐）。 */
    private final DragStrategy strategy = DragStrategy.chosen();

    private @Nullable WindowDrag drag;
    private @Nullable Point lastPointer;

    /** 直前に {@code setLocation} へ渡した値。ピアが置き直したことに気づくために覚える。 */
    private @Nullable Point lastSetLocation;

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
        restoreTheSize();
        layOutChrome();
        roundTheCorners();
    }

    /**
     * 大きさが設定から決まる値と違っていたら戻す（ADR 0019 の決定 5）。
     *
     * <p>🔴 ここが ×1.2 の膨らみを潰す唯一の場所である。窓が尺度の違う画面へ入ると Windows が
     * 実寸を直し、JDK が古い尺度で割り戻すので、論理の大きさが 582 → 698 → 838 …と育つ。
     * **画面が変わったことに気づく手段は無い**が、この膨らみは {@code componentResized} として届く。
     *
     * <p>戻す先（content pane の preferred size）は<b>いまの大きさに依らない</b>ので、同じ値を
     * 置き直しても動かない。だから再入しても収束する。
     */
    private void restoreTheSize() {
        Dimension wanted = wantedSize();
        if (!frame.getSize().equals(wanted)) {
            frame.setSize(wanted);
        }
    }

    /** 設定から決まる窓の大きさ。下限は広げる向きにだけ効く。 */
    private Dimension wantedSize() {
        Dimension wanted = frame.getContentPane().getPreferredSize();
        return new Dimension(Math.max(MINIMUM_WIDTH, wanted.width), Math.max(MINIMUM_HEIGHT, wanted.height));
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

    private void listenToThePointer() {
        MouseAdapter pointer = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                grabTheWindow();
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                drag = null;
                lastPointer = null;
                lastSetLocation = null;
            }

            @Override
            public void mouseDragged(MouseEvent event) {
                dragTheWindow();
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

    /**
     * 窓そのものの動きを聞く。
     *
     * <p>🔴 聞けるのは寸法の変化だけである。{@code componentMoved} で
     * {@code getGraphicsConfiguration()} を見る経路は<b>持たない</b>。実機で測ったところ、窓が
     * 別の画面へ深く入り込んでも JDK はこれを差し替えず、その枝は一度も実行されなかった（ADR 0019）。
     */
    private void listenToTheFrame() {
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                restoreTheSize();
                layOutChrome();
                roundTheCorners();
            }
        });
    }

    /**
     * 掴み点を覚える。ポインタと窓は、それぞれ<b>自分が載っている画面</b>で読む。
     *
     * <p>ポインタを読めないことがある（{@link MouseInfo#getPointerInfo()} は null を返しうる）。
     * そのときは掴まない。掴んでいなければ動かさないので、窓は静かに留まる。
     *
     * <p>{@link DragStrategy#POINTER} では窓の側もポインタの画面で読む。{@link DragStrategy#DELTA}
     * は掴み点を使わないが、最初のポインタだけはここで覚える。
     */
    private void grabTheWindow() {
        grabTheWindowAt(MouseInfo.getPointerInfo());
    }

    /** いまのポインタと窓の実物で掴み直す。押した瞬間と、ピアが置き直したときに通る。 */
    private void grabTheWindowAt(@Nullable PointerInfo pointer) {
        GraphicsConfiguration windowScreen = frame.getGraphicsConfiguration();
        if (pointer == null || windowScreen == null) {
            drag = null;
            lastPointer = null;
            return;
        }
        ScreenSpace pointerSpace = spaceOf(pointer.getDevice().getDefaultConfiguration());
        lastPointer = new Point(pointer.getLocation());
        drag = WindowDrag.grabbedAt(
                pointer.getLocation(), pointerSpace, frame.getBounds(), peerSpace(pointerSpace, windowScreen));
    }

    /**
     * ピアが {@code setLocation} に掛けると思われる変換。ここが測定で選ぶ 1 点である。
     *
     * <p>実測では、窓の GC がまだ主画面を返している瞬間に、ピアは既に左画面の変換を掛けていた
     * （Issue #100 の step50・{@link DragStrategy}）。どちらを信じるかは機械では決められないので、
     * 起動時のプロパティで選べるようにしてある。
     */
    private ScreenSpace peerSpace(ScreenSpace pointerSpace, GraphicsConfiguration windowScreen) {
        return switch (strategy) {
            case WINDOW, DELTA -> spaceOf(windowScreen);
            case POINTER, VERIFY -> pointerSpace;
        };
    }

    /**
     * 掴んだ点を保ったまま窓を動かす（ADR 0019）。
     *
     * <p>🔴 ポインタは {@link MouseInfo#getPointerInfo()} から取る。{@code getXOnScreen()} は
     * <b>イベント源（窓）が載っている画面</b>の尺度で返すので、ポインタが別の画面に入った瞬間から
     * 嘘になる（実測 1282px の食い違い）。{@code getPointerInfo()} は<b>ポインタ自身の画面</b>の
     * 尺度で返し、その画面も一緒に答える。
     *
     * <p>読めないイベントは黙って飛ばす。
     */
    private void dragTheWindow() {
        PointerInfo pointer = MouseInfo.getPointerInfo();
        if (pointer == null) {
            return;
        }
        switch (strategy) {
            case WINDOW, POINTER -> moveByCompensation(pointer);
            case DELTA -> moveByDelta(pointer.getLocation());
            case VERIFY -> moveAfterCheckingThePeer(pointer);
        }
    }

    /**
     * 狙った実ピクセルを、ピアの変換を見越して打ち消した Java 座標へ直して渡す。
     *
     * <p>渡すのは窓の<b>大きさ</b>だけで、現在位置は渡さない。位置を足し込まないので誤差が
     * 累積しない。
     */
    private void moveByCompensation(PointerInfo pointer) {
        WindowDrag grabbed = drag;
        GraphicsConfiguration windowScreen = frame.getGraphicsConfiguration();
        if (grabbed == null || windowScreen == null) {
            return;
        }
        ScreenSpace pointerSpace = spaceOf(pointer.getDevice().getDefaultConfiguration());
        Point wanted = grabbed.locationFor(
                pointer.getLocation(), pointerSpace, frame.getSize(), peerSpace(pointerSpace, windowScreen));
        lastSetLocation = new Point(wanted);
        frame.setLocation(wanted);
    }

    /**
     * ピアが窓を置き直していないか確かめてから動かす（{@link DragStrategy#VERIFY}）。
     *
     * <p>前のイベントで渡した値と、いま {@code getLocation()} が返す値が違うなら、
     * その間にピアが自分の都合で置き直している＝写像が切り替わった瞬間である。
     * そのイベントでは<b>動かさず、いまの実物で掴み直す</b>。予測を当てにいかない。
     */
    private void moveAfterCheckingThePeer(PointerInfo pointer) {
        Point promised = lastSetLocation;
        if (promised != null && !promised.equals(frame.getLocation())) {
            lastSetLocation = null;
            grabTheWindowAt(pointer);
            return;
        }
        moveByCompensation(pointer);
    }

    /**
     * ポインタの移動量だけ窓を動かす（{@link DragStrategy#DELTA}）。
     *
     * <p>どの画面の変換も推定しない代わりに、写像が切り替わった瞬間の見かけの跳びを捨てる
     * （{@link PointerStep#isPlausible()}）。捨てたイベントでは<b>窓を動かさず、覚えている
     * ポインタだけを更新する</b>。次のイベントからは新しい写像の上で続きを歩ける。
     */
    private void moveByDelta(Point now) {
        Point previous = lastPointer;
        lastPointer = new Point(now);
        if (previous == null) {
            return;
        }
        PointerStep step = PointerStep.between(previous, now);
        if (step.isPlausible()) {
            frame.setLocation(step.appliedTo(frame.getLocation()));
        }
    }

    /**
     * 画面の記述（原点と尺度）を読む。
     *
     * <p>原点は {@code getBounds()}（実ピクセル）、尺度は既定の変換から取る。**画面の一覧は
     * 尋ねない**（{@code GraphicsEnvironment} の禁止は維持する・ARC-007）。窓とポインタが、
     * それぞれ自分の画面について答える。
     */
    private static ScreenSpace spaceOf(GraphicsConfiguration screen) {
        Rectangle bounds = screen.getBounds();
        AffineTransform transform = screen.getDefaultTransform();
        return ScreenSpace.of(bounds.x, bounds.y, transform.getScaleX(), transform.getScaleY());
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
