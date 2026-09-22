package io.github.hideyukimori.neneclock.ui.swing;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

/**
 * ドラッグ中の窓の左上を決める計算（ADR 0018）。副作用を持たず、窓にも画面にも触れない。
 *
 * <p>掴み点は「押した瞬間の、窓の左上からの隔たり」である。窓の位置はこの値とポインタの
 * <b>画面座標</b>だけから毎回決め直す。前の位置に足し込まないので、一度ずれても次のイベントで
 * 自己修正する。
 *
 * <p>🔴 相対の足し込みは、拡大率の違う画面をまたぐ数百ミリ秒のあいだに壊れる。その区間では
 * {@code getGraphicsConfiguration()} が新しい画面を返しているのに窓の寸法はまだ古く、
 * 尺度の違う数を足すことになる。しかも誤差が累積して戻らない（実測 7.43 倍・ADR 0018）。
 *
 * <p>掴んだときの窓の大きさを覚えているのは、途中で大きさが変わったことを呼び出し側が
 * 知るためである。変わっていたら掴み直す（{@link #stillFits(Dimension)}）。
 */
final class WindowDrag {

    private final int grabX;
    private final int grabY;
    private final Dimension sizeWhenGrabbed;

    private WindowDrag(int grabX, int grabY, Dimension sizeWhenGrabbed) {
        this.grabX = grabX;
        this.grabY = grabY;
        this.sizeWhenGrabbed = new Dimension(sizeWhenGrabbed);
    }

    /**
     * ポインタの画面座標と窓の矩形から掴み直す。
     *
     * <p>掴み点は必ず窓の中へ収める。窓が作り直された直後はポインタが窓の外に居ることがあり、
     * そのまま覚えると次の一手で窓が遠くへ跳ぶ。
     */
    static WindowDrag grabbedAt(Point pointerOnScreen, Rectangle window) {
        return new WindowDrag(
                inside(pointerOnScreen.x - window.x, window.width),
                inside(pointerOnScreen.y - window.y, window.height),
                window.getSize());
    }

    /** 掴んだときから窓の大きさが変わっていないか。変わっていたら掴み直す番である。 */
    boolean stillFits(Dimension size) {
        return sizeWhenGrabbed.equals(size);
    }

    /** 掴み点をポインタの下に保つ窓の左上。窓の現在位置を読まないので誤差が積み上がらない。 */
    Point locationFor(Point pointerOnScreen) {
        return new Point(pointerOnScreen.x - grabX, pointerOnScreen.y - grabY);
    }

    /** 窓の左上から見た掴み点。 */
    Point grabbedPoint() {
        return new Point(grabX, grabY);
    }

    private static int inside(int offset, int extent) {
        return Math.min(Math.max(offset, 0), Math.max(0, extent - 1));
    }
}
