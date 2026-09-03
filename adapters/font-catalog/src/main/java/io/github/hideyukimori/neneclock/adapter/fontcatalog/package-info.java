/**
 * 実行環境の書体一覧を読める唯一の場所（ARC-007 の例外区画）。
 *
 * <p>このモジュールだけ {@code config/forbiddenapis/platform.txt} を適用しない。
 * ほかのモジュールで {@code GraphicsEnvironment} を触るとビルドが落ちる。
 * 時計は読めないままなので {@code determinism.txt} は適用したままである。
 */
@NullMarked
package io.github.hideyukimori.neneclock.adapter.fontcatalog;

import org.jspecify.annotations.NullMarked;
