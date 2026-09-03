package io.github.hideyukimori.neneclock.domain;

/**
 * アプリが同梱している書体（ADR 0006）。
 *
 * <p>時計に使う {@link Typeface} と、画面の文言に使う {@link InterfaceTypeface} の 2 種類がある。
 * 用途は違うが、「同梱されたファイルがある」という点は同じなので、
 * 実体を取り出す経路（{@code TypefaceBinaryPort}）と検査は 1 本にまとめる。
 */
public sealed interface BundledTypeface permits Typeface, InterfaceTypeface {

    /** 列挙定数の名前。同梱ファイルの名前はここから機械的に決まる。 */
    String constantName();
}
