package io.github.hideyukimori.neneclock.ui.swing;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Objects;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * 文字を滑らかに描くための唯一の入口（SWG-006）。
 *
 * <p>🔴 Swing の文字描画ヒントは**デスクトップ環境から渡される**。渡されない環境
 * （`awt.font.desktophints` が {@code null}）では、{@code JLabel} はアンチエイリアス無しで描かれる。
 * 実測では素の {@code JLabel} が 2 階調（白と黒だけ）で、ヒントを付けると 249 階調になった。
 *
 * <p>だから部品ごとに「付けたり付けなかったり」できないようにする。テキスト部品はここでしか
 * 作れず（CNF-012 が直接の {@code new} を拒否する）、作れば必ずヒントが付く。
 *
 * <p>LCD サブピクセル（{@code VALUE_TEXT_ANTIALIAS_LCD_HRGB}）は採らない。背景色を利用者が
 * 選ぶため、色の付いた地の上で色にじみが出る。グレースケールの AA にする。
 */
public final class TextRendering {

    private TextRendering() {}

    /** 文字を滑らかに描くラベルを作る。ここが唯一の生成経路。 */
    public static JLabel label(String text) {
        JLabel label = new JLabel(Objects.requireNonNull(text, "text"));
        smooth(label);
        return label;
    }

    /** 中央寄せのラベルを作る。 */
    public static JLabel centredLabel(String text, int alignment) {
        JLabel label = new JLabel(Objects.requireNonNull(text, "text"), alignment);
        smooth(label);
        return label;
    }

    /** 文字を滑らかに描く入力欄を作る。 */
    public static JTextField field(int columns) {
        JTextField field = new JTextField(columns);
        smooth(field);
        return field;
    }

    /** 自前描画の側で同じヒントを立てる。描き方を 2 通りにしないため。 */
    public static void smooth(Graphics2D canvas) {
        Objects.requireNonNull(canvas, "canvas");
        canvas.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        canvas.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    private static void smooth(javax.swing.JComponent component) {
        component.putClientProperty(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }
}
