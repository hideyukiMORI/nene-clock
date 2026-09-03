package io.github.hideyukimori.neneclock.gradle.conformance;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * waiver 台帳の整合（CNF-009）。
 *
 * <p>「期限切れの waiver は CI を落とす」を成立させるための検査。今日の日付は引数で受ける
 * （この検査自身も ARC-007 の対象であり、内部で現在時刻を読まない）。
 */
public final class WaiverLedger {

    private static final Pattern FILE_NAME = Pattern.compile("^WVR-(\\d{4})-[a-z0-9-]+\\.md$");

    private static final Pattern EXPIRES = Pattern.compile("^-\\s*Expires:\\s*(\\S+)");

    private static final List<String> REQUIRED_FIELDS = List.of("- Rule:", "- Scope:", "- Issue:", "- Expires:");

    private WaiverLedger() {}

    public static List<Violation> check(
            List<SourceFile> waiverFiles, SourceFile index, List<String> referencedFromSources, LocalDate today) {
        List<Violation> violations = new ArrayList<>();
        Map<String, SourceFile> active = new LinkedHashMap<>();

        for (SourceFile file : waiverFiles) {
            String name = file.fileName();
            if (name.equals("README.md") || name.equals("0000-template.md")) {
                continue;
            }
            Matcher matcher = FILE_NAME.matcher(name);
            if (!matcher.matches()) {
                violations.add(Violation.atFile(
                        "CNF-009", file.path(), "waiver のファイル名は WVR-NNNN-short-kebab-title.md にする"));
                continue;
            }
            String waiverId = "WVR-" + matcher.group(1);
            active.put(waiverId, file);
            checkFields(violations, file);
            checkExpiry(violations, file, today);
            if (index != null && !containsToken(index, waiverId)) {
                violations.add(Violation.atFile(
                        "CNF-009", index.path(), waiverId + " が有効 waiver 索引に載っていない"));
            }
        }

        for (String referenced : referencedFromSources) {
            if (!active.containsKey(referenced)) {
                violations.add(Violation.atFile(
                        "CNF-009", "docs/waivers", "ソースが参照する " + referenced + " の台帳ファイルが無い"));
            }
        }
        return List.copyOf(violations);
    }

    private static void checkFields(List<Violation> violations, SourceFile file) {
        for (String field : REQUIRED_FIELDS) {
            boolean present = file.lines().stream().anyMatch(line -> line.startsWith(field));
            if (!present) {
                violations.add(Violation.atFile("CNF-009", file.path(), "waiver に必須項目が無い: " + field));
            }
        }
    }

    private static void checkExpiry(List<Violation> violations, SourceFile file, LocalDate today) {
        for (int index = 0; index < file.lines().size(); index++) {
            Matcher matcher = EXPIRES.matcher(file.lines().get(index));
            if (!matcher.find()) {
                continue;
            }
            String raw = matcher.group(1);
            LocalDate expiry;
            try {
                expiry = LocalDate.parse(raw);
            } catch (DateTimeParseException failure) {
                violations.add(new Violation(
                        "CNF-009", file.path(), index + 1, "Expires は YYYY-MM-DD で書く: " + raw));
                return;
            }
            if (expiry.isBefore(today)) {
                violations.add(new Violation(
                        "CNF-009", file.path(), index + 1, "期限切れの waiver（" + raw + "）。コードを直すか ADR にする"));
            }
            return;
        }
    }

    private static boolean containsToken(SourceFile file, String token) {
        return file.lines().stream().anyMatch(line -> line.contains(token));
    }
}
