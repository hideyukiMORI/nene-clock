package io.github.hideyukimori.neneclock.gradle.conformance;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ドキュメント整合（CNF-005）。
 *
 * <p>規則 ID が「定義されずに参照される」「二重に定義される」「強制マトリクスに載っていない」
 * ことを拒否する。規約が自分自身について嘘をつく経路を塞ぐための検査である。
 */
public final class DocumentationRules {

    private static final Pattern DEFINITION =
            Pattern.compile("^###\\s+((?:ARC|JAV|SWG|QLT|CNF|FR)-\\d{3})\\s+—");

    private static final Pattern REFERENCE = Pattern.compile("\\b((?:ARC|JAV|SWG|QLT|CNF|FR)-\\d{3})\\b");

    /** 強制マトリクスに行を持たねばならない規則の接頭辞。 */
    private static final Set<String> MATRIX_REQUIRED_PREFIXES = Set.of("ARC", "JAV", "SWG", "CNF");

    private DocumentationRules() {}

    public static List<Violation> check(List<SourceFile> documents, List<SourceFile> otherReferences, String matrixPath) {
        List<Violation> violations = new ArrayList<>();
        Map<String, String> definedIn = new LinkedHashMap<>();

        for (SourceFile document : documents) {
            for (int index = 0; index < document.lines().size(); index++) {
                Matcher matcher = DEFINITION.matcher(document.lines().get(index));
                if (!matcher.find()) {
                    continue;
                }
                String ruleId = matcher.group(1);
                String previous = definedIn.put(ruleId, document.path());
                if (previous != null) {
                    violations.add(new Violation(
                            "CNF-005",
                            document.path(),
                            index + 1,
                            ruleId + " が二重に定義されている（既出: " + previous + "）"));
                }
            }
        }

        Set<String> matrixIds = matrixIds(documents, matrixPath);
        Set<String> unknown = new TreeSet<>();
        for (SourceFile file : concat(documents, otherReferences)) {
            for (String line : file.lines()) {
                Matcher matcher = REFERENCE.matcher(line);
                while (matcher.find()) {
                    String ruleId = matcher.group(1);
                    if (!definedIn.containsKey(ruleId) && unknown.add(ruleId)) {
                        violations.add(Violation.atFile(
                                "CNF-005", file.path(), "未定義の規則 ID を参照している: " + ruleId));
                    }
                }
            }
        }

        for (Map.Entry<String, String> entry : definedIn.entrySet()) {
            String ruleId = entry.getKey();
            if (!MATRIX_REQUIRED_PREFIXES.contains(ruleId.substring(0, 3))) {
                continue;
            }
            if (!matrixIds.contains(ruleId)) {
                violations.add(Violation.atFile(
                        "CNF-005",
                        entry.getValue(),
                        ruleId + " が " + matrixPath + " の強制マトリクスに載っていない"));
            }
        }
        return List.copyOf(violations);
    }

    private static Set<String> matrixIds(List<SourceFile> documents, String matrixPath) {
        Set<String> ids = new LinkedHashSet<>();
        for (SourceFile document : documents) {
            if (!document.path().equals(matrixPath)) {
                continue;
            }
            for (String line : document.lines()) {
                if (!line.startsWith("|")) {
                    continue;
                }
                Matcher matcher = REFERENCE.matcher(line);
                while (matcher.find()) {
                    ids.add(matcher.group(1));
                }
            }
        }
        return ids;
    }

    private static List<SourceFile> concat(List<SourceFile> first, List<SourceFile> second) {
        List<SourceFile> all = new ArrayList<>(first);
        all.addAll(second);
        return all;
    }
}
