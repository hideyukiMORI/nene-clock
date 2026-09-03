package io.github.hideyukimori.neneclock.ui.swing;

/**
 * ウィンドウ操作のアイコン。24 の格子の上で、直線と円だけで描ける形にしてある。
 *
 * <p>形の定義は 1 か所（{@link ChromeIconPainter}）にあり、この列挙はどれを描くかだけを表す。
 */
public enum ChromeIcon {
    /** 移動（四方向の矢印）。窓のどこを掴んでも動くことを教える。 */
    MOVE,
    /** 設定（歯車）。押すと設定モーダルが開く。 */
    SETTINGS,
    /** 終了（×）。押すと窓が閉じる。 */
    CLOSE,
    /** 戻る（左向きの山形）。設定モーダルの中で 1 つ前の画面へ戻る。 */
    BACK
}
