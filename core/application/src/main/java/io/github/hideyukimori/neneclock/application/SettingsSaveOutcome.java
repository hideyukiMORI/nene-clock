package io.github.hideyukimori.neneclock.application;

import java.util.Objects;

/**
 * 設定保存の結果。
 *
 * <p>保存の失敗を戻り値の無い {@code void} で消さない。呼び出し側が利用者へ知らせる判断を
 * できるよう、型で返す（JAV-005）。
 */
public sealed interface SettingsSaveOutcome {

    /** 保存できた。 */
    record Saved() implements SettingsSaveOutcome {}

    /** 保存できなかった。 */
    record Failed(SettingsSaveFailure reason) implements SettingsSaveOutcome {
        public Failed {
            Objects.requireNonNull(reason, "reason");
        }
    }
}
