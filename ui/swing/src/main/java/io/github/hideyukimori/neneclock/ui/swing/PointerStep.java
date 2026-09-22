package io.github.hideyukimori.neneclock.ui.swing;

import java.awt.Point;

/**
 * ポインタの 1 歩（{@link DragStrategy#DELTA} 用）。副作用を持たない。
 *
 * <p>🔴 画面をまたぐ瞬間、ポインタの座標は<b>写像が切り替わったせいで</b>大きく跳ぶ。実測では
 * 同じ瞬間の 2 つの座標源が 1282px 食い違った（ADR 0019）。これは「利用者が手を 1282px 動かした」
 * ではない。移動量で窓を動かすなら、この跳びを移動として足してはいけない。
 *
 * <p>見分け方は閾値 1 つである。人の手は 1 イベントで {@value #LIMIT} 論理ピクセルも動かない
 * （ドラッグのイベントは数ミリ秒おきに届く）。超えていたら写像の切り替わりとみなして捨て、
 * 覚えているポインタだけを更新する。**窓は動かさない。**
 */
final class PointerStep {

    /** 1 イベントで動きうる論理ピクセルの上限。これを超えたら移動ではない。 */
    static final int LIMIT = 300;

    private final int dx;
    private final int dy;

    private PointerStep(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    /** 前のポインタからいまのポインタまでの 1 歩。 */
    static PointerStep between(Point previous, Point now) {
        return new PointerStep(now.x - previous.x, now.y - previous.y);
    }

    /** 人の手の動きとして説明できる大きさか。説明できないなら写像の切り替わりである。 */
    boolean isPlausible() {
        return Math.abs(dx) <= LIMIT && Math.abs(dy) <= LIMIT;
    }

    /** この 1 歩を窓の左上に足した位置。 */
    Point appliedTo(Point windowLocation) {
        return new Point(windowLocation.x + dx, windowLocation.y + dy);
    }
}
