/**
 * {@code java.util.prefs} に触れる唯一の場所（ARC-002 / ARC-009）。
 *
 * <p>保存形式は版を持つ。読めない版は既定値へ黙って落とすのではなく、
 * {@link io.github.hideyukimori.neneclock.application.SettingsLoadFailure} として上へ返す。
 */
@NullMarked
package io.github.hideyukimori.neneclock.adapter.preferences;

import org.jspecify.annotations.NullMarked;
