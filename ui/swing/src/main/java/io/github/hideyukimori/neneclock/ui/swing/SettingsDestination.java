package io.github.hideyukimori.neneclock.ui.swing;

/** 設定モーダルの中の行き先。閉じた選択肢なので列挙で表す（JAV-002）。 */
public enum SettingsDestination {
    /** 設定の一覧（最初に出る画面）。 */
    FORM,
    /** 書体を選ぶ画面。 */
    TYPEFACE,
    /** 文字色を選ぶ画面。 */
    FONT_COLOUR,
    /** 背景色を選ぶ画面。 */
    BACKGROUND_COLOUR
}
