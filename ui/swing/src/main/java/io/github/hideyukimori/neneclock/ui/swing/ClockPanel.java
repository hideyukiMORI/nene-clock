package io.github.hideyukimori.neneclock.ui.swing;

import io.github.hideyukimori.neneclock.application.ClockFace;
import io.github.hideyukimori.neneclock.application.DateLine;
import io.github.hideyukimori.neneclock.domain.UserSettings;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.util.Objects;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * 時計タブ。{@link ClockFace} をそのまま描く。
 *
 * <p>JPanel を継承せず内包する（構築中に自分のメソッドが呼ばれる形を作らないため）。
 */
public final class ClockPanel {

    private static final int DATE_FONT_DIVISOR = 4;
    private static final int MINIMUM_DATE_POINTS = 12;

    /**
     * 幅を決めるための最も広い文字列。
     *
     * <p>いま出ている文字列で大きさを決めると、等幅でない書体では毎秒わずかに幅が変わり、
     * 窓が震える。「起こりうる中で最も広いもの」で決めれば、動かない。
     */
    private static final String WIDEST_TIME = "00:00:00 AM";

    private static final String WIDEST_DATE = "0000-00-00";
    private static final int PADDING = 30;

    /** 角丸の半径。窓の形はこの絵が決める。 */
    static final int CORNER = 16;

    private static final int LINE_GAP = 10;

    private final TypefaceFontLoader typefaces;

    private Color fill = Color.WHITE;

    private final JPanel panel = new JPanel(new GridBagLayout()) {
        @Override
        protected void paintComponent(Graphics graphics) {
            paintGround(graphics);
        }
    };
    private final JLabel time = TextRendering.centredLabel("", SwingConstants.CENTER);
    private final JLabel date = TextRendering.centredLabel("", SwingConstants.CENTER);

    /** 部品を組み立てる。表示内容は {@code render*} が決める。 */
    public ClockPanel(TypefaceFontLoader typefaces) {
        this.typefaces = Objects.requireNonNull(typefaces, "typefaces");
        // 🔑 地を自分で描く。窓に任せると、半透明も角丸も窓の性質に縛られる（ADR 0011）。
        panel.setOpaque(false);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add(time, constraints);
        constraints.gridy = 1;
        panel.add(date, constraints);
    }

    /** 画面に載せるための Swing 部品。 */
    public JComponent component() {
        return panel;
    }

    /** 表示文字列を反映する。 */
    public void renderFace(ClockFace face) {
        Objects.requireNonNull(face, "face");
        time.setText(face.time());
        switch (face.date()) {
            case DateLine.Shown shown -> {
                date.setText(shown.text());
                date.setVisible(true);
            }
            case DateLine.Hidden hidden -> date.setVisible(false);
        }
    }

    /** 設定を反映する。半透明で描けない環境では、地を不透明にして描く。 */
    public void renderSettings(UserSettings settings, boolean translucent) {
        Objects.requireNonNull(settings, "settings");
        int points = settings.fontSize().points();
        Color foreground = AwtColour.of(settings.fontColor());
        fill = translucent ? AwtColour.of(settings.backgroundColor()) : AwtColour.opaque(settings.backgroundColor());
        time.setFont(typefaces.load(settings.typeface(), points));
        time.setForeground(foreground);
        date.setFont(typefaces.load(settings.typeface(), Math.max(MINIMUM_DATE_POINTS, points / DATE_FONT_DIVISOR)));
        date.setForeground(foreground);
        panel.setPreferredSize(roomForTheWidestFace());
        // 🔴 地の色が変わっても、部品の再描画だけでは面全体が塗り直されない。
        //    透明度を下げたとき、塗り直されなかった帯が不透明のまま残った（実機で踏んだ）。
        panel.repaint();
    }

    /** いまの書体と大きさで、時計が確実に収まる大きさ。 */
    private Dimension roomForTheWidestFace() {
        FontMetrics timeMetrics = panel.getFontMetrics(time.getFont());
        FontMetrics dateMetrics = panel.getFontMetrics(date.getFont());
        int width = Math.max(timeMetrics.stringWidth(WIDEST_TIME), dateMetrics.stringWidth(WIDEST_DATE));
        int height = timeMetrics.getHeight() + LINE_GAP + dateMetrics.getHeight();
        return new Dimension(width + PADDING * 2, height + PADDING * 2);
    }

    private void paintGround(Graphics graphics) {
        Graphics2D canvas = (Graphics2D) graphics.create();
        canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        canvas.setComposite(AlphaComposite.Src);
        canvas.setColor(fill);
        canvas.fill(new RoundRectangle2D.Double(0, 0, panel.getWidth(), panel.getHeight(), CORNER, CORNER));
        canvas.dispose();
    }
}
