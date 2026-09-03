package io.github.hideyukimori.neneclock.domain;

/**
 * 永続化された設定のスキーマ版（ARC-009）。
 *
 * <p>版が上がるときは移行規則を同じ変更で持ち込む。読めない版は既定値へ落とすのではなく、
 * 型のある失敗として上へ返す。
 */
public record SettingsSchemaVersion(int value) {

    /** 現在アプリが書き出す版。 */
    public static final SettingsSchemaVersion CURRENT = new SettingsSchemaVersion(1);

    public SettingsSchemaVersion {
        if (value < 1) {
            throw new IllegalArgumentException("スキーマ版は 1 以上: " + value);
        }
    }

    /** このアプリが読める版かどうか。 */
    public boolean isSupported() {
        return value == CURRENT.value;
    }
}
