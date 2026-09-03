package io.github.hideyukimori.neneclock.application;

import io.github.hideyukimori.neneclock.domain.BundledTypeface;

/**
 * 同梱書体の実体（TrueType のバイト列）を得る唯一の窓口（ARC-007 / ADR 0006）。
 *
 * <p>どの書体があるかは domain の {@link BundledTypeface} が知っている。このポートが答えるのは
 * 「その書体のファイルの中身」だけである。バイト列を {@code java.awt.Font} にするのは UI の仕事で、
 * core は描画の型を知らない（ARC-003）。
 *
 * <p>すべての {@link Typeface} に対応するファイルが同梱されていることは実装側の不変条件であり、
 * 満たされないのは利用者に伝える失敗ではなく**梱包の誤り**である。したがって結果型ではなく
 * 例外で落とす（JAV-005 の「期待される失敗」に当たらない）。
 */
@FunctionalInterface
public interface TypefaceBinaryPort {

    /**
     * 書体のファイルの中身を返す。呼び出し側が書き換えても影響しない複製を返す。
     *
     * @throws IllegalStateException 同梱されているはずのファイルが読めないとき
     */
    byte[] read(BundledTypeface typeface);
}
