package io.github.hideyukimori.neneclock.application;

import io.github.hideyukimori.neneclock.domain.UserSettings;

/**
 * 「この設定にしたい」という利用者の意図を受け取る口。
 *
 * <p>UI は状態を直接変えず、望む設定を丸ごと渡すだけである（ARC-011）。
 * 実際に差し替えて保存するのは {@link SettingsHandler} の仕事。
 */
@FunctionalInterface
public interface SettingsIntentSink {

    /** 利用者が望む設定を渡す。 */
    void submit(UserSettings requested);
}
