package io.github.hideyukimori.neneclock.domain;

import java.util.Objects;

/**
 * 表示に使う書体名（family name）。
 *
 * <p>コンストラクタは非公開で、生成経路は {@link #of(String)} ただ 1 本（JAV-007）。
 *
 * <p>🔑 「この実行環境で利用可能かどうか」はこの型の不変条件では**ない**。利用可能性は
 * 環境依存の事実であり、保存された設定の正しさとは別物である（FR-043）。利用可能な一覧は
 * ポート経由で application へ渡す。
 */
public final class FontFamily {

    /** 書体名の長さの上限。仕様 FR-043 と一致する。 */
    public static final int MAXIMUM_LENGTH = 64;

    /** 既定の書体。Java の論理フォントであり、どの実行環境にも存在する。 */
    public static final FontFamily DEFAULT = new FontFamily("Monospaced");

    private final String name;

    private FontFamily(String name) {
        this.name = name;
    }

    /** 検証して生成する。ここが唯一の生成経路。 */
    public static FontFamilyOutcome of(String name) {
        Objects.requireNonNull(name, "name");
        if (name.isBlank()) {
            return new FontFamilyOutcome.Rejected(FontFamilyRejection.BLANK);
        }
        if (name.length() > MAXIMUM_LENGTH) {
            return new FontFamilyOutcome.Rejected(FontFamilyRejection.TOO_LONG);
        }
        if (containsControlCharacter(name)) {
            return new FontFamilyOutcome.Rejected(FontFamilyRejection.UNSUPPORTED_CHARACTER);
        }
        return new FontFamilyOutcome.Accepted(new FontFamily(name));
    }

    /** 書体名。 */
    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof FontFamily family && family.name.equals(name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "FontFamily[" + name + "]";
    }

    private static boolean containsControlCharacter(String name) {
        for (int index = 0; index < name.length(); index++) {
            if (Character.isISOControl(name.charAt(index))) {
                return true;
            }
        }
        return false;
    }
}
