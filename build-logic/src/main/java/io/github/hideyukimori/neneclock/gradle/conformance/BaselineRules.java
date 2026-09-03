package io.github.hideyukimori.neneclock.gradle.conformance;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * baseline 禁止（CNF-008）。
 *
 * <p>既存違反を「無かったこと」にする経路（baseline・suppressions ファイル・ゲートの無効化フラグ）
 * を機械的に拒否する。QLT-003 を人の記憶に頼らせないための検査。
 */
public final class BaselineRules {

    private static final Pattern FORBIDDEN_PATH =
            Pattern.compile("(?i)(^|/)[^/]*(baseline|suppressions)[^/]*\\.(xml|json|txt|toml)$");

    /** ゲートを黙って無力化する書き方。設定ファイル・ビルドスクリプトに現れたら落とす。 */
    private static final List<String> FORBIDDEN_TOKENS = List.of(
            "ratchetFrom",
            "ignoreFailures = true",
            "ignoreFailures=true",
            "failOnError = false",
            "failOnError=false",
            "-Xlint:none",
            "-nowarn",
            "abortOnError false");

    private BaselineRules() {}

    public static List<Violation> check(List<String> repositoryPaths, List<SourceFile> configurationFiles) {
        List<Violation> violations = new ArrayList<>();
        for (String path : repositoryPaths) {
            if (FORBIDDEN_PATH.matcher(path).find()) {
                violations.add(Violation.atFile("CNF-008", path, "baseline / suppressions ファイルは持たない"));
            }
        }
        for (SourceFile file : configurationFiles) {
            for (int index = 0; index < file.lines().size(); index++) {
                String line = file.lines().get(index);
                for (String token : FORBIDDEN_TOKENS) {
                    if (line.contains(token)) {
                        violations.add(new Violation(
                                "CNF-008", file.path(), index + 1, "ゲートを無力化する記述: " + token));
                    }
                }
            }
        }
        return List.copyOf(violations);
    }
}
