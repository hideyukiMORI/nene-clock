package io.github.hideyukimori.neneclock.gradle.conformance;

import java.util.Objects;

/** 規約違反 1 件。{@code ruleId} は必ず docs 側の規則 ID と一致する。 */
public record Violation(String ruleId, String path, int line, String message) {

    public Violation {
        Objects.requireNonNull(ruleId, "ruleId");
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(message, "message");
    }

    /** 行番号を持たない（ファイル単位・リポジトリ単位の）違反。 */
    public static Violation atFile(String ruleId, String path, String message) {
        return new Violation(ruleId, path, 0, message);
    }

    public String render() {
        String location = line > 0 ? path + ":" + line : path;
        return ruleId + " " + location + " — " + message;
    }
}
