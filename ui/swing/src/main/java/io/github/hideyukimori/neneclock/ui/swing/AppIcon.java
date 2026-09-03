package io.github.hideyukimori.neneclock.ui.swing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * タスクバーと窓マネージャに出すアイコン。
 *
 * <p>デジタル時計のコロン（{@code :}）だけを描く。タスクバーに出るのはほとんど 16〜32px で、
 * その大きさで残らない形は意味を持たない。点が 2 つなら、縮めても壊れない。
 *
 * <p>下の点だけ色を変えてあるのは、コロンが秒ごとに点滅するからである。
 * 2 点を同じ色にすると記号に見え、色を分けると動いているものに見える。**動きを描かずに動きを示す。**
 *
 * <p>🔑 画像ファイルを同梱しない。角丸矩形 1 つと円 2 つなので、要る大きさをその場で描く。
 * どの拡大率でも輪郭が崩れず、書き出した画像と実物がずれることもない。
 *
 * <p>点の大きさ（半径 18 / 中心間 44）は 16px で描いて選んだ。もっと小さいと点が沈み、
 * もっと大きいと 16px で 2 点がひと塊に見える。**比率は最小の大きさで決まる。**
 */
public final class AppIcon {

    /** 設計した格子の大きさ。座標はすべてこの中の比で決めてある。 */
    private static final double GRID = 128.0;

    private static final double CORNER = 26.0;
    private static final double DOT_RADIUS = 18.0;
    private static final double CENTRE_X = 64.0;
    private static final double UPPER_DOT_Y = 42.0;
    private static final double LOWER_DOT_Y = 86.0;

    private static final Color GROUND = new Color(0x1A1917);
    private static final Color UPPER_DOT = new Color(0xF5F2EB);
    private static final Color LOWER_DOT = new Color(0xD08C3F);

    /** 窓マネージャへ渡す大きさ。小さいほうから並べる。 */
    private static final List<Integer> SIZES = List.of(16, 20, 24, 32, 48, 64, 128, 256);

    private AppIcon() {}

    /** 窓に渡す一式。大きさの選択は窓マネージャに任せる。 */
    public static List<Image> images() {
        List<Image> images = new ArrayList<>();
        for (int size : SIZES) {
            images.add(at(size));
        }
        return List.copyOf(images);
    }

    /** 指定の大きさで描く。 */
    static BufferedImage at(int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D canvas = image.createGraphics();
        canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        canvas.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        double scale = size / GRID;
        canvas.scale(scale, scale);
        canvas.setColor(GROUND);
        canvas.fill(new RoundRectangle2D.Double(0, 0, GRID, GRID, CORNER, CORNER));
        canvas.setColor(UPPER_DOT);
        canvas.fill(dotAt(UPPER_DOT_Y));
        canvas.setColor(LOWER_DOT);
        canvas.fill(dotAt(LOWER_DOT_Y));
        canvas.dispose();
        return image;
    }

    private static Ellipse2D dotAt(double centreY) {
        return new Ellipse2D.Double(CENTRE_X - DOT_RADIUS, centreY - DOT_RADIUS, DOT_RADIUS * 2, DOT_RADIUS * 2);
    }
}
