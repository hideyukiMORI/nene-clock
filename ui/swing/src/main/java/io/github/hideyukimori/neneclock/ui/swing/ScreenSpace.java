package io.github.hideyukimori.neneclock.ui.swing;

import java.awt.Dimension;
import java.awt.Point;

/**
 * 1 つの画面の座標の読み方（ADR 0019）。原点と尺度だけを持つ値であり、副作用を持たない。
 *
 * <p>🔑 Java の仮想デスクトップは単位が混ざっている。<b>画面の原点は実ピクセル、画面の大きさは
 * 論理ピクセル</b>で報告される（施主の 4 画面で実測）。だから画面をまたぐ計算は、どの画面にも
 * 属さない唯一の共通の物差しである<b>実ピクセル</b>で行う。
 *
 * <pre>
 * 実ピクセル = 原点 + (Java 座標 − 原点) × 尺度
 * Java 座標  = 原点 + (実ピクセル − 原点) ÷ 尺度
 * </pre>
 *
 * <p>大きさには原点が効かない（両端が同じ原点で写るので差し引かれる）。したがって
 * 実ピクセルの大きさは論理の大きさに尺度を掛けただけである。
 *
 * <p>値として渡すので、混在尺度の 4 画面を headless で再現できる。画面の一覧は尋ねない
 * （{@code GraphicsEnvironment} の禁止は維持する・ARC-007）。窓とポインタが、それぞれ自分の
 * 画面について答える。
 */
final class ScreenSpace {

    private final int originX;
    private final int originY;
    private final double scaleX;
    private final double scaleY;

    private ScreenSpace(int originX, int originY, double scaleX, double scaleY) {
        this.originX = originX;
        this.originY = originY;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }

    /**
     * 画面の原点（実ピクセル）と尺度から作る。
     *
     * <p>尺度は正でなければならない。0 や NaN を受け取ると逆変換が定義できない。
     */
    static ScreenSpace of(int originX, int originY, double scaleX, double scaleY) {
        if (!(scaleX > 0) || !(scaleY > 0)) {
            throw new IllegalArgumentException("a screen scale must be positive: " + scaleX + " x " + scaleY);
        }
        return new ScreenSpace(originX, originY, scaleX, scaleY);
    }

    /** Java 座標の点を実ピクセルへ。 */
    Point toDevice(Point javaPoint) {
        return new Point(
                originX + rounded((javaPoint.x - originX) * scaleX),
                originY + rounded((javaPoint.y - originY) * scaleY));
    }

    /** 実ピクセルの点を Java 座標へ。{@code setLocation} に渡す値はこちらで作る。 */
    Point toJava(Point devicePoint) {
        return new Point(
                originX + rounded((devicePoint.x - originX) / scaleX),
                originY + rounded((devicePoint.y - originY) / scaleY));
    }

    /** 論理ピクセルの大きさを実ピクセルへ。原点は効かない。 */
    Dimension toDeviceSize(Dimension javaSize) {
        return new Dimension(rounded(javaSize.width * scaleX), rounded(javaSize.height * scaleY));
    }

    private static int rounded(double value) {
        return (int) Math.round(value);
    }
}
