/**
 * 振る舞いの調整層。ポートを宣言し、その実装は知らない（ARC-002）。
 *
 * <p>時刻の入手は {@link io.github.hideyukimori.neneclock.application.WallClockPort} 経由のみ。
 * Swing・java.util.prefs・ファイル・ネットワークをこのパッケージから触らない。
 */
@NullMarked
package io.github.hideyukimori.neneclock.application;

import org.jspecify.annotations.NullMarked;
