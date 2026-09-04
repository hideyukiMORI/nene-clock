package io.github.hideyukimori.neneclock.ui.swing;

import io.github.hideyukimori.neneclock.domain.RgbColor;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.jspecify.annotations.Nullable;

/**
 * 色を掴んで選ぶ面（FR-045）。左が彩度／明度の面、右が色相の帯。
 *
 * <p>既製の {@code JColorChooser} は採らない（ADR 0008）。タブと数値欄の並ぶ既製の見た目が、
 * 自分で描いてきたモーダルの見た目を上書きしてしまうためである。つまみ・帯と同じく自分で描く。
 *
 * <p>🔑 HSB を持っているのはこの部品だけである。外へ出ていくのは常に {@link RgbColor}
 * （FR-044 / FR-046）。座標系は画面の都合であって、保存される値ではない。
 *
 * <p>🔑 掴んでいる間、位置の正本はこの部品にある。自分が出した色が戻ってきたときは座標を計算し直さない。
 * 8bit へ丸めた色から座標を作り直すと、掴んだままつまみが少しずつ動く。
 */
public final class ColourField {

    /**
     * 面の上下に空ける余白。
     *
     * <p>🔴 つまみは面の縁の上にも載る（明度 1.0 は上端そのもの）。余白が無いと、
     * いちばん使う「明るい側」でつまみが半分切れて描かれる。実機で見て足した。
     * 横は空けない。上のプリセットの並びと左右の端が揃っているほうを優先する。
     */
    private static final int PAD = 8;

    private static final int PLANE_HEIGHT = 112;
    private static final int HEIGHT = PLANE_HEIGHT + PAD * 2;
    private static final int BAND_WIDTH = 14;
    private static final int BAND_GAP = 12;
    private static final int PLANE_ARC = 8;
    private static final int BAND_ARC = 7;
    private static final int HUE_STOPS = 7;

    private static final float FULL = 1f;
    private static final float HAIRLINE = 1f;
    private static final float KNOB_RING = 2f;

    private static final double KNOB_RADIUS = 5.5;
    private static final double KNOB_EDGE = 1.5;
    private static final double BAND_KNOB_HEIGHT = 7.0;
    private static final double BAND_KNOB_BLEED = 2.0;

    /** 面の白と黒のグラデーションの、透明な側。 */
    private static final Color CLEAR_WHITE = new Color(255, 255, 255, 0);

    private static final Color CLEAR_BLACK = new Color(0, 0, 0, 0);

    /**
     * つまみの縁。
     *
     * <p>🔑 ここだけはアンバーの強調色を使わない。つまみは**選んでいる色の上に載る**ので、
     * 配色から決まる 1 色では地に溶ける瞬間がある。白い輪と暗い縁の組にして、どの色の上でも見えるようにする。
     */
    private static final Color KNOB_EDGE_COLOUR = new Color(0, 0, 0, 90);

    private final JPanel surface = new JPanel(null) {
        @Override
        protected void paintComponent(Graphics graphics) {
            paintField(graphics);
        }
    };

    private Consumer<RgbColor> chosen = colour -> {};
    private HsbCoordinate position = HsbCoordinate.of(RgbColor.DEFAULT_FONT, 0f);
    private Grip grip = Grip.PLANE;
    private @Nullable Palette palette;
    private @Nullable RgbColor emitted;

    /** 面を組み立てる。色と配色は {@code render*} が持ち込む。 */
    public ColourField() {
        surface.setOpaque(false);
        surface.setPreferredSize(new Dimension(0, HEIGHT));
        surface.setMaximumSize(new Dimension(Integer.MAX_VALUE, HEIGHT));
        MouseAdapter pointer = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                // 掴んだ場所で、面と帯のどちらを動かすかが決まる。離すまで変わらない。
                grab(event.getX(), event.getY());
            }

            @Override
            public void mouseDragged(MouseEvent event) {
                moveTo(event.getX(), event.getY());
            }
        };
        surface.addMouseListener(pointer);
        surface.addMouseMotionListener(pointer);
    }

    /** 画面に載せるための Swing 部品。 */
    public JComponent component() {
        return surface;
    }

    /** 色が選ばれたことの宛先を 1 度だけ結ぶ。 */
    public void onColourChosen(Consumer<RgbColor> action) {
        this.chosen = Objects.requireNonNull(action, "action");
    }

    /**
     * 編集中の色と配色を反映する。
     *
     * <p>外から来た色（プリセット・HEX の打ち込み）のときだけ座標を計算し直す。自分が出した色なら、
     * つまみは動かさない。
     */
    public void renderColour(RgbColor colour, UiTheme theme) {
        Objects.requireNonNull(colour, "colour");
        this.palette = Objects.requireNonNull(theme, "theme").palette();
        if (!Objects.equals(emitted, colour)) {
            position = HsbCoordinate.of(colour, position.hue());
            emitted = colour;
        }
        surface.repaint();
    }

    private void grab(int x, int y) {
        grip = x < planeWidth() + BAND_GAP / 2 ? Grip.PLANE : Grip.BAND;
        moveTo(x, y);
    }

    private void moveTo(int x, int y) {
        int planeWidth = planeWidth();
        emit(
                switch (grip) {
                    case PLANE -> position.withPlane(ratio(x, planeWidth), FULL - ratio(y - PAD, PLANE_HEIGHT));
                    case BAND -> position.withHue(ratio(y - PAD, PLANE_HEIGHT));
                });
    }

    private void emit(HsbCoordinate moved) {
        position = moved;
        RgbColor picked = moved.toColour();
        emitted = picked;
        chosen.accept(picked);
    }

    private int planeWidth() {
        return Math.max(1, surface.getWidth() - BAND_WIDTH - BAND_GAP);
    }

    private static float ratio(int value, int span) {
        return Math.min(FULL, Math.max(0f, value / (float) Math.max(1, span)));
    }

    private void paintField(Graphics graphics) {
        Palette palette = this.palette;
        if (palette == null) {
            return;
        }
        Graphics2D canvas = (Graphics2D) graphics.create();
        TextRendering.smooth(canvas);
        int planeWidth = planeWidth();
        paintPlane(canvas, palette, planeWidth);
        paintBand(canvas, palette, planeWidth);
        canvas.dispose();
    }

    private void paintPlane(Graphics2D canvas, Palette palette, int planeWidth) {
        RoundRectangle2D plane = new RoundRectangle2D.Double(0, PAD, planeWidth, PLANE_HEIGHT, PLANE_ARC, PLANE_ARC);
        Graphics2D area = (Graphics2D) canvas.create();
        area.clip(plane);
        area.setColor(AwtColour.of(HsbCoordinate.at(position.hue(), FULL, FULL).toColour()));
        area.fillRect(0, PAD, planeWidth, PLANE_HEIGHT);
        area.setPaint(new GradientPaint(0f, 0f, Color.WHITE, planeWidth, 0f, CLEAR_WHITE));
        area.fillRect(0, PAD, planeWidth, PLANE_HEIGHT);
        area.setPaint(new GradientPaint(0f, PAD, CLEAR_BLACK, 0f, PAD + PLANE_HEIGHT, Color.BLACK));
        area.fillRect(0, PAD, planeWidth, PLANE_HEIGHT);
        area.dispose();
        canvas.setStroke(new BasicStroke(HAIRLINE));
        canvas.setColor(palette.hairline());
        canvas.draw(plane);
        paintPlaneKnob(canvas, planeWidth);
    }

    private void paintPlaneKnob(Graphics2D canvas, int planeWidth) {
        double centreX = position.saturation() * planeWidth;
        double centreY = PAD + (FULL - position.brightness()) * PLANE_HEIGHT;
        canvas.setStroke(new BasicStroke(KNOB_RING));
        canvas.setColor(Color.WHITE);
        canvas.draw(ringAt(centreX, centreY, KNOB_RADIUS));
        canvas.setStroke(new BasicStroke(HAIRLINE));
        canvas.setColor(KNOB_EDGE_COLOUR);
        canvas.draw(ringAt(centreX, centreY, KNOB_RADIUS + KNOB_EDGE));
    }

    private static Ellipse2D ringAt(double centreX, double centreY, double radius) {
        return new Ellipse2D.Double(centreX - radius, centreY - radius, radius * 2, radius * 2);
    }

    private void paintBand(Graphics2D canvas, Palette palette, int planeWidth) {
        double left = planeWidth + (double) BAND_GAP;
        RoundRectangle2D band = new RoundRectangle2D.Double(left, PAD, BAND_WIDTH, PLANE_HEIGHT, BAND_ARC, BAND_ARC);
        Graphics2D area = (Graphics2D) canvas.create();
        area.clip(band);
        area.setPaint(hueGradient());
        area.fill(band);
        area.dispose();
        canvas.setStroke(new BasicStroke(HAIRLINE));
        canvas.setColor(palette.hairline());
        canvas.draw(band);
        paintBandKnob(canvas, left);
    }

    /** 色相の帯。6 分割の頂点をそのまま繋ぐ。中間は Java2D が線形に埋める。 */
    private static LinearGradientPaint hueGradient() {
        float[] stops = new float[HUE_STOPS];
        Color[] colours = new Color[HUE_STOPS];
        for (int index = 0; index < HUE_STOPS; index++) {
            float hue = index / (float) (HUE_STOPS - 1);
            stops[index] = hue;
            colours[index] = AwtColour.of(HsbCoordinate.at(hue, FULL, FULL).toColour());
        }
        return new LinearGradientPaint(
                new Point2D.Double(0, PAD), new Point2D.Double(0, PAD + PLANE_HEIGHT), stops, colours);
    }

    private void paintBandKnob(Graphics2D canvas, double left) {
        double centreY = PAD + position.hue() * PLANE_HEIGHT;
        double top = Math.min(PAD + PLANE_HEIGHT - BAND_KNOB_HEIGHT, Math.max(PAD, centreY - BAND_KNOB_HEIGHT / 2));
        canvas.setStroke(new BasicStroke(KNOB_RING));
        canvas.setColor(Color.WHITE);
        canvas.draw(bandKnobAt(left, top, 0));
        canvas.setStroke(new BasicStroke(HAIRLINE));
        canvas.setColor(KNOB_EDGE_COLOUR);
        canvas.draw(bandKnobAt(left, top, KNOB_EDGE));
    }

    private static RoundRectangle2D bandKnobAt(double left, double top, double grown) {
        return new RoundRectangle2D.Double(
                left - BAND_KNOB_BLEED - grown,
                top - grown,
                BAND_WIDTH + (BAND_KNOB_BLEED + grown) * 2,
                BAND_KNOB_HEIGHT + grown * 2,
                BAND_ARC,
                BAND_ARC);
    }

    /** 掴んでいる場所。押した瞬間に決まり、離すまで変わらない。 */
    private enum Grip {
        /** 彩度／明度の面。 */
        PLANE,
        /** 色相の帯。 */
        BAND
    }
}
