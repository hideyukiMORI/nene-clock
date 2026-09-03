package io.github.hideyukimori.neneclock.domain;

/**
 * 画面の文言の言語（FR-048）。閉じた選択肢なので列挙で表す（JAV-002）。
 *
 * <p>🔑 実行環境のロケールから決めない。ロケールは環境依存の事実であり、
 * 保存された設定より優先されるべきものではない（ARC-007 と同じ考え方）。
 *
 * <p>時計の表示（{@code HH:mm:ss} / {@code yyyy-MM-dd}）はこの設定に影響されない。
 * 時刻の整形はロケールに依存しない（FR-006）。
 */
public enum Language {
    /** 日本語（既定）。 */
    JAPANESE(InterfaceTypeface.ZEN_KAKU_GOTHIC_NEW),
    /** English。 */
    ENGLISH(InterfaceTypeface.ARIMO);

    /** 既定の言語。仕様 FR-040 と一致する。 */
    public static final Language DEFAULT = JAPANESE;

    private final InterfaceTypeface typeface;

    Language(InterfaceTypeface typeface) {
        this.typeface = typeface;
    }

    /** この言語を描ける同梱書体。 */
    public InterfaceTypeface typeface() {
        return typeface;
    }
}
