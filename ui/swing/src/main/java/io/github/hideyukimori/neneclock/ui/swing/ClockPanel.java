package io.github.hideyukimori.neneclock.ui.swing;

import io.github.hideyukimori.neneclock.application.ClockFace;
import io.github.hideyukimori.neneclock.application.DateLine;
import io.github.hideyukimori.neneclock.domain.UserSettings;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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

    /** 角丸の半径。窓の切り抜きに使う。 */
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
        // 地は自分で描く。色は設定から来るので、Swing の背景色に預けない。
        panel.setOpaque(true);
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

    /**
     * 表示文字列を反映する。
     *
     * <p>🔴 面**全体**を描き直す。文字を変えるだけだと、Swing はラベルの矩形しか塗り直さない。
     * 地は {@link AlphaComposite#Src} で描いており、これは塗った範囲の画素を**透明度ごと
     * 置き換える**ので、その長方形だけが消えて描き直され、境目が毎秒ちらついた（FR-002 / Issue #55）。
     */
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
        panel.repaint();
    }

    /** 設定を反映する。 */
    public void renderSettings(UserSettings settings) {
        Objects.requireNonNull(settings, "settings");
        int points = settings.fontSize().points();
        Color foreground = AwtColour.of(settings.fontColor());
        fill = AwtColour.of(settings.backgroundColor());
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

    /** 地を描く。角丸は窓の切り抜き（{@code setShape}）が作る。 */
    private void paintGround(Graphics graphics) {
        graphics.setColor(fill);
        graphics.fillRect(0, 0, panel.getWidth(), panel.getHeight());
    }
}
