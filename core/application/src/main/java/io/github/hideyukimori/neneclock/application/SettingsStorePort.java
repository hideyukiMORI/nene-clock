package io.github.hideyukimori.neneclock.application;

import io.github.hideyukimori.neneclock.domain.UserSettings;

/**
 * 設定の永続化の唯一の窓口（ARC-002）。
 *
 * <p>production の実装は {@code :adapters:preferences} にただ 1 つ。UI から
 * {@code java.util.prefs} を直接触ることは ArchUnit が拒否する。
 */
public interface SettingsStorePort {

    /** 保存された設定を読む。失敗は例外ではなく結果型で返る（JAV-005）。 */
    SettingsLoadOutcome load();

    /** 設定を保存する。失敗は例外ではなく結果型で返る（JAV-005）。 */
    SettingsSaveOutcome save(UserSettings settings);
}
