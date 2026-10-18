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

    private final SettingsLoadOutcome restoration;

    private UserSettings current;

    private SettingsHandler(SettingsStorePort store, SettingsLoadOutcome restoration) {
        this.store = store;
        this.restoration = restoration;
        this.current = restoration.settingsOrDefaults();
    }

    /** 保存済みの設定から組み立てる。読めなければ既定値から始まり、その理由は {@link #restoration()} に残る。 */
    public static SettingsHandler restoredFrom(SettingsStorePort store) {
        Objects.requireNonNull(store, "store");
        return new SettingsHandler(store, store.load());
    }

    /**
     * 起動時に設定を読んだ結果。読めなかったことを利用者へ伝えるために合成ルートが読む（ARC-009・ADR 0023）。
     * 黙って既定値で動かない。
     */
    public SettingsLoadOutcome restoration() {
        return restoration;
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
