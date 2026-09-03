package io.github.hideyukimori.neneclock.domain;

/**
 * 永続化された設定のスキーマ版（ARC-009）。
 *
 * <p>版が上がるときは移行規則を同じ変更で持ち込む。読めない版は既定値へ落とすのではなく、
 * 型のある失敗として上へ返す。移行の方針は ADR 0003。
 */
public record SettingsSchemaVersion(int value) {

    /** 現在アプリが書き出す版。 */
    public static final SettingsSchemaVersion CURRENT = new SettingsSchemaVersion(5);

    /** 移行して読める最も古い版。これより古い版は存在しない。 */
    public static final SettingsSchemaVersion EARLIEST_MIGRATABLE = new SettingsSchemaVersion(1);

    public SettingsSchemaVersion {
        if (value < 1) {
            throw new IllegalArgumentException("スキーマ版は 1 以上: " + value);
        }
    }

    /** そのまま読める版か（移行が不要か）どうか。 */
    public boolean isSupported() {
        return value == CURRENT.value;
    }

    /**
     * 移行して読める版かどうか（判断の根拠は ADR 0003）。
     *
     * <p>未来の版は移行できない。推測して読むより、読めないと返すほうが安全である。
     */
    public boolean isMigratable() {
        return value >= EARLIEST_MIGRATABLE.value && value <= CURRENT.value;
    }
}
