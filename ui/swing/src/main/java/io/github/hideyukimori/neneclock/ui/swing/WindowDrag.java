package io.github.hideyukimori.neneclock.ui.swing;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

/**
 * ドラッグ中の窓の左上を決める計算（ADR 0019）。副作用を持たず、窓にも画面にも触れない。
 *
 * <p>計算は<b>実ピクセル</b>で行う。Java の座標空間は画面ごとに尺度が違い、しかも
 * どの画面にも属さない穴が空いている（ADR 0019 の実測）。ポインタと窓を同じ物差しに
 * 載せられるのは実ピクセルだけである。
 *
 * <p>🔑 最後の一手が肝である。{@code setLocation} に渡した値には、ピアが必ず
 * <b>窓が載っていると Java が思っている画面</b>の変換を掛ける。だから正しい Java 座標を
 * 渡しても届かない。渡すのは「変換されたあとに正しくなる値」——すなわち、狙った実ピクセルを
 * <b>窓の画面</b>の尺度で逆変換した値である（{@link #locationFor}）。
 *
 * <p>掴み点は実ピクセルで覚える。窓の実ピクセルの大きさは、尺度の違う画面へ入ると変わる。
 * そのときは掴み点を<b>同じ相対位置へ写し直し</b>、窓の中に収める（{@link #grabbedPointIn}）。
 *
 * <p>窓の<b>現在位置</b>は一度も読まない。読んで足し込む形にすると、尺度の違う数を足した誤差が
 * 累積して戻らなくなる（実測 7.43 倍・ADR 0018）。だからこの型は、同じ入力に同じ答えを返す。
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
     * ポインタと窓を、それぞれが載っている画面の記述で実ピクセルへ直し、掴み点を覚える。
     *
     * <p>掴み点は必ず窓の中へ収める。窓が作り直された直後はポインタが窓の外に居ることがあり、
     * そのまま覚えると次の一手で窓が遠くへ跳ぶ。
     */
    static WindowDrag grabbedAt(Point pointer, ScreenSpace pointerScreen, Rectangle window, ScreenSpace windowScreen) {
        Point pointerDevice = pointerScreen.toDevice(pointer);
        Point windowDevice = windowScreen.toDevice(window.getLocation());
        Dimension windowDeviceSize = windowScreen.toDeviceSize(window.getSize());
        return new WindowDrag(
                inside(pointerDevice.x - windowDevice.x, windowDeviceSize.width),
                inside(pointerDevice.y - windowDevice.y, windowDeviceSize.height),
                windowDeviceSize);
    }

    /**
     * 掴み点をポインタの下に保つために {@code setLocation} へ渡す Java 座標。
     *
     * <p>ポインタは<b>ポインタが載っている画面</b>の記述で実ピクセルへ直す。窓の大きさと
     * 逆変換は<b>窓が載っていると Java が思っている画面</b>の記述で行う。この 2 つが違う画面で
     * あることが、画面をまたぐドラッグの本体である。
     *
     * <p>渡すのは窓の<b>大きさ</b>だけで、位置は渡さない。位置を読まないことが「誤差が累積
     * しない」の中身である。
     */
    Point locationFor(Point pointer, ScreenSpace pointerScreen, Dimension windowSize, ScreenSpace windowScreen) {
        Point pointerDevice = pointerScreen.toDevice(pointer);
        Point grab = grabbedPointIn(windowScreen.toDeviceSize(windowSize));
        return windowScreen.toJava(new Point(pointerDevice.x - grab.x, pointerDevice.y - grab.y));
    }

    /**
     * いまの実ピクセルの大きさにおける掴み点。
     *
     * <p>窓が尺度の違う画面へ入ると実ピクセルの大きさが変わる。掴み点を実ピクセルのまま使うと、
     * 窓が縮んだときに窓の外へ出る。だから<b>同じ相対位置</b>へ写してから窓の中に収める。
     */
    Point grabbedPointIn(Dimension deviceSize) {
        return new Point(
                inside(remapped(grabX, sizeWhenGrabbed.width, deviceSize.width), deviceSize.width),
                inside(remapped(grabY, sizeWhenGrabbed.height, deviceSize.height), deviceSize.height));
    }

    /** 掴んだ瞬間の、窓の左上から見た掴み点（実ピクセル）。 */
    Point grabbedPoint() {
        return new Point(grabX, grabY);
    }

    /** 掴んだときの窓の実ピクセルの大きさ。 */
    Dimension sizeWhenGrabbed() {
        return new Dimension(sizeWhenGrabbed);
    }

    private static int remapped(int offset, int was, int now) {
        if (was == 0) {
            return 0;
        }
        return (int) Math.round(offset * (double) now / was);
    }

    private static int inside(int offset, int extent) {
        return Math.min(Math.max(offset, 0), Math.max(0, extent - 1));
    }
}
