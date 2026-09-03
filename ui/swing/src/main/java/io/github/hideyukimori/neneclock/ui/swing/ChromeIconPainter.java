package io.github.hideyukimori.neneclock.ui.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;

/**
 * {@link ChromeIcon} を描く唯一の場所。
 *
 * <p>24 の格子で設計し、実際の大きさへは拡大縮小で合わせる。画像を持たないのは、
 * 文字色に追従して色を変えるためと、どの拡大率でも輪郭が崩れないためである。
 */
final class ChromeIconPainter {

    /** 設計した格子の大きさ。 */
    private static final double GRID = 24.0;

    private static final double STROKE = 1.6;
    private static final double GEAR_RING = 6.2;
    private static final double GEAR_DOT = 2.0;
    private static final double GEAR_TOOTH_INNER = 7.2;
    private static final double GEAR_TOOTH_OUTER = 9.6;
    private static final int GEAR_TEETH = 8;
    private static final double CENTRE = 12.0;
    private static final double CLOSE_START = 6.5;
    private static final double CLOSE_END = 17.5;
    private static final double HALF_TURN = Math.PI;
    private static final double BACK_REACH = 4.5;

    private ChromeIconPainter() {}

    /** アイコン 1 つを、左上 (x, y) から size 四方に描く。 */
    static void paint(Graphics2D canvas, ChromeIcon icon, Color colour, IconBox box) {
        Graphics2D scaled = (Graphics2D) canvas.create();
        TextRendering.smooth(scaled);
        scaled.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        scaled.translate(box.x(), box.y());
        double factor = box.size() / GRID;
        scaled.scale(factor, factor);
        scaled.setColor(colour);
        scaled.setStroke(new BasicStroke((float) STROKE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (icon) {
            case SETTINGS -> paintSettings(scaled);
            case CLOSE -> paintClose(scaled);
            case BACK -> paintBack(scaled);
        }
        scaled.dispose();
    }

    private static void paintSettings(Graphics2D canvas) {
        canvas.draw(circleAt(GEAR_RING));
        canvas.fill(circleAt(GEAR_DOT));
        for (int tooth = 0; tooth < GEAR_TEETH; tooth++) {
            double angle = HALF_TURN * 2 * tooth / GEAR_TEETH;
            canvas.draw(new Line2D.Double(
                    CENTRE + Math.cos(angle) * GEAR_TOOTH_INNER,
                    CENTRE + Math.sin(angle) * GEAR_TOOTH_INNER,
                    CENTRE + Math.cos(angle) * GEAR_TOOTH_OUTER,
                    CENTRE + Math.sin(angle) * GEAR_TOOTH_OUTER));
        }
    }

    private static Ellipse2D circleAt(double radius) {
        return new Ellipse2D.Double(CENTRE - radius, CENTRE - radius, radius * 2, radius * 2);
    }

    private static void paintBack(Graphics2D canvas) {
        Path2D chevron = new Path2D.Double();
        chevron.moveTo(CENTRE + BACK_REACH, CENTRE - BACK_REACH);
        chevron.lineTo(CENTRE - BACK_REACH, CENTRE);
        chevron.lineTo(CENTRE + BACK_REACH, CENTRE + BACK_REACH);
        canvas.draw(chevron);
    }

    private static void paintClose(Graphics2D canvas) {
        canvas.draw(new Line2D.Double(CLOSE_START, CLOSE_START, CLOSE_END, CLOSE_END));
        canvas.draw(new Line2D.Double(CLOSE_END, CLOSE_START, CLOSE_START, CLOSE_END));
    }
}
