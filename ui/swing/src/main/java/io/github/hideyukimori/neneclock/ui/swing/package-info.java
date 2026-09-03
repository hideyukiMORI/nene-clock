/**
 * Swing 描画。状態を描き、意図を発行するだけ（ARC-011 / SWG-002）。
 *
 * <p>整形・検証・時刻の入手はここに置かない。UI 状態の反映は {@code render*} メソッドからのみ
 * 行う（CNF-004）。
 */
@NullMarked
package io.github.hideyukimori.neneclock.ui.swing;

import org.jspecify.annotations.NullMarked;
