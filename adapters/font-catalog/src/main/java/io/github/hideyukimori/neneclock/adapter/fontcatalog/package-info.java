/**
 * 同梱書体（TrueType ファイル）を持つ唯一の場所。
 *
 * <p>🔑 かつてここは実行環境の書体一覧を読む例外区画だったが、書体を同梱したことで
 * {@code GraphicsEnvironment} を読む必要が無くなり、{@code platform.txt} の適用除外を畳んだ（ADR 0006）。
 * いまはこのモジュールもほかと同じ署名で検査される。
 */
@NullMarked
package io.github.hideyukimori.neneclock.adapter.fontcatalog;

import org.jspecify.annotations.NullMarked;
