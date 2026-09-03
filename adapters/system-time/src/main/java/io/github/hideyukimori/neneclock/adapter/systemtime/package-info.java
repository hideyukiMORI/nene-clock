/**
 * 現在時刻を JDK から読める唯一の場所（ARC-007 の例外区画）。
 *
 * <p>このモジュールだけ {@code config/forbiddenapis/determinism.txt} を適用しない。
 * ほかのモジュールで {@code now()} を書くとビルドが落ちる。
 */
@NullMarked
package io.github.hideyukimori.neneclock.adapter.systemtime;

import org.jspecify.annotations.NullMarked;
