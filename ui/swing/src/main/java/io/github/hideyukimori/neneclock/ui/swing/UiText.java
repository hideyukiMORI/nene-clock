package io.github.hideyukimori.neneclock.ui.swing;

import io.github.hideyukimori.neneclock.domain.Language;

/**
 * 画面に出す文言（FR-048）。
 *
 * <p>🔑 **両方の言語を 1 つの定数が持つ。** 言語ごとにファイルを分けると、片方だけ足して
 * もう片方を忘れることができてしまう。この形なら、定数を足した時点で両方書かないとコンパイルが通らない。
 *
 * <p>{@code ResourceBundle} を使わないのは、既定ロケール（＝実行環境の状態）を読む経路が
 * 生まれるためである。言語は保存された設定から決まる（ARC-007 と同じ考え方）。
 */
public enum UiText {
    /** 設定モーダルの見出し。 */
    SETTINGS("設定", "Settings"),
    /** 区分けの見出し。 */
    SECTION_DISPLAY("表示", "Display"),
    /** 区分けの見出し。 */
    SECTION_APPEARANCE("見た目", "Appearance"),
    /** 設定項目。 */
    CLOCK_FORMAT("時刻表記", "Time format"),
    /** 時刻表記の選択肢。 */
    HOUR_12("12 時間", "12-hour"),
    /** 時刻表記の選択肢。 */
    HOUR_24("24 時間", "24-hour"),
    /** 設定項目。 */
    SHOW_SECONDS("秒を表示", "Show seconds"),
    /** 設定項目。 */
    SHOW_DATE("日付を表示", "Show date"),
    /** 設定項目。 */
    ALWAYS_ON_TOP("常に最前面", "Always on top"),
    /** 設定項目。 */
    TYPEFACE("書体", "Typeface"),
    /** 設定項目。 */
    SIZE("大きさ", "Size"),
    /** 設定項目。 */
    FONT_COLOUR("文字色", "Text colour"),
    /** 設定項目。 */
    BACKGROUND_COLOUR("背景色", "Background"),
    /** 設定項目。 */
    LANGUAGE("言語", "Language"),
    /**
     * 言語の選択肢（日本語）。
     *
     * <p>🔴 英語の側を「日本語」にしない。英語 UI の書体（Arimo）は日本語の字形を持たないので、
     * 豆腐（□□□）になる。実機で踏んだ。文言と書体は組で決まる。
     */
    LANGUAGE_JAPANESE("日本語", "Japanese"),
    /** 言語の選択肢（英語）。 */
    LANGUAGE_ENGLISH("English", "English"),
    /** 書体ピッカーの絞り込み。 */
    ALL_MOODS("すべて", "All"),
    /** 色ピッカーの自由入力。 */
    CUSTOM_COLOUR("自由指定", "Custom"),
    /** 読めない配色を直す提案。 */
    MAKE_READABLE("読める色にする", "Make it readable"),
    /** コントラスト比の表示。書式引数を 1 つ取る。 */
    CONTRAST_RATIO("コントラスト比 %.1f : 1", "Contrast %.1f : 1"),
    /** 保存できたときの状態行。 */
    SAVED("変更はすぐに反映され、そのまま保存されます", "Changes apply and are saved right away"),
    /** 保存できなかったときの状態行。 */
    NOT_SAVED(
            "保存できませんでした。いまは反映されていますが、再起動すると元に戻ります",
            "Could not save. The change applies now but will be lost on restart"),
    /** フッター右端の名乗り。書式引数は版と作者の 2 つ。 */
    SIGNATURE("NeNe Clock %s · %s", "NeNe Clock %s · %s");

    private final String japanese;
    private final String english;

    UiText(String japanese, String english) {
        this.japanese = japanese;
        this.english = english;
    }

    /** その言語での文言。 */
    public String in(Language language) {
        return switch (language) {
            case JAPANESE -> japanese;
            case ENGLISH -> english;
        };
    }
}
