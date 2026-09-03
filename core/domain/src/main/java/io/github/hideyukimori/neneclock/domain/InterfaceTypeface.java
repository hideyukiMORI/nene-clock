package io.github.hideyukimori.neneclock.domain;

/**
 * 画面の文言に使う書体（FR-048）。
 *
 * <p>時計の書体（{@link Typeface}）と違い、利用者は直接選ばない。**言語から決まる。**
 * 日本語の字形を持つ書体と持たない書体を混ぜて選ばせても、選べない組み合わせが増えるだけである。
 *
 * <p>🔴 Noto Sans JP は採れなかった。google/fonts には可変版しか無く、9.6 MB あって
 * 既定インスタンスが Thin である（Java 21 は可変フォントの軸を選べない・ADR 0006）。
 */
public enum InterfaceTypeface implements BundledTypeface {
    /** 日本語のゴシック。 */
    ZEN_KAKU_GOTHIC_NEW,
    /** ラテン文字。Arial とメトリック互換の書体。 */
    ARIMO;

    @Override
    public String constantName() {
        return name();
    }
}
