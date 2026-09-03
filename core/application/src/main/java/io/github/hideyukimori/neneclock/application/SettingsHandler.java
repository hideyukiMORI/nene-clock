package io.github.hideyukimori.neneclock.application;

import io.github.hideyukimori.neneclock.domain.UserSettings;
import java.util.Objects;

/**
 * 「現在の設定」の唯一の所有者（ARC-004 / ADR 0004）。
 *
 * <p>🔑 このクラスは application 層で唯一、可変な参照を持つ（ARC-005 の第 2 の隔離区画）。
 * 可変なのは「どの {@link UserSettings} を指しているか」だけで、値そのものは不変である。
 * 参照の差し替えは {@link #apply(UserSettings)} ただ 1 本で行う。
 */
public final class SettingsHandler {

    private final SettingsStorePort store;

    private UserSettings current;

    private SettingsHandler(SettingsStorePort store, UserSettings current) {
        this.store = store;
        this.current = current;
    }

    /** 保存済みの設定から組み立てる。読めなければ既定値から始まる。 */
    public static SettingsHandler restoredFrom(SettingsStorePort store) {
        Objects.requireNonNull(store, "store");
        return new SettingsHandler(store, store.load().settingsOrDefaults());
    }

    /** いま使っている設定。 */
    public UserSettings current() {
        return current;
    }

    /**
     * 設定を差し替えて保存する。
     *
     * <p>保存に失敗しても現在値は差し替える。利用者の操作を無かったことにするより、
     * 「表示は変わったが保存できなかった」と伝えるほうが正直である（FR-045）。
     * 保存できなかったことは戻り値で返し、握り潰さない。
     */
    public SettingsSaveOutcome apply(UserSettings requested) {
        Objects.requireNonNull(requested, "requested");
        current = requested;
        return store.save(requested);
    }
}
