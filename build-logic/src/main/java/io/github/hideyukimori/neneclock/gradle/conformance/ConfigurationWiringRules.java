package io.github.hideyukimori.neneclock.gradle.conformance;

import java.util.ArrayList;
import java.util.List;

/**
 * 設定ファイルの結線（CNF-011）。
 *
 * <p>`config/` に置いた検査設定が、どのビルドスクリプトからも読み込まれていない状態を拒否する。
 * 「署名ファイルは存在するのに誰も読んでいない」ために、規則が守られていると誤読した実害があった
 * （Issue #26。詳細は quality/gate-proofs.md 第 3 節）。
 */
public final class ConfigurationWiringRules {

    private static final String CONFIG_PREFIX = "config/";

    private ConfigurationWiringRules() {}

    /**
     * @param configurationPaths `config/` 配下のファイルパス
     * @param wiringSources ビルドスクリプトと build-logic のソース（読み込み側になりうるもの）
     */
    public static List<Violation> check(List<String> configurationPaths, List<SourceFile> wiringSources) {
        List<Violation> violations = new ArrayList<>();
        for (String path : configurationPaths) {
            if (!path.startsWith(CONFIG_PREFIX)) {
                continue;
            }
            String fileName = fileNameOf(path);
            if (!isReferenced(fileName, wiringSources)) {
                violations.add(Violation.atFile(
                        "CNF-011", path, "この設定ファイルを読み込むビルドスクリプトが無い。置いても効かない"));
            }
        }
        return List.copyOf(violations);
    }

    private static boolean isReferenced(String fileName, List<SourceFile> wiringSources) {
        for (SourceFile source : wiringSources) {
            for (String line : source.lines()) {
                if (line.contains(fileName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String fileNameOf(String path) {
        int slash = path.lastIndexOf('/');
        return slash < 0 ? path : path.substring(slash + 1);
    }
}
