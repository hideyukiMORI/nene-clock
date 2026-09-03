package io.github.hideyukimori.neneclock.gradle.conformance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/** CNF-005 の negative proof（QLT-007）。 */
class DocumentationRulesTest {

    private static final String MATRIX = "docs/QUALITY_GATES.md";

    private static SourceFile matrixWith(String... ruleIds) {
        StringBuilder content = new StringBuilder("| 規則 | 状態 | 実体 |\n| --- | --- | --- |\n");
        for (String ruleId : ruleIds) {
            content.append("| ").append(ruleId).append(" | active | x |\n");
        }
        return SourceFile.of(MATRIX, content.toString());
    }

    @Test
    void acceptsADocumentSetThatDefinesEveryReferencedRule() {
        SourceFile constitution = SourceFile.of("docs/ARCHITECTURE_CONSTITUTION.md", "### ARC-001 — 唯一の経路\n");
        List<Violation> violations =
                DocumentationRules.check(List.of(constitution, matrixWith("ARC-001")), List.of(), MATRIX);

        assertEquals(List.of(), violations);
    }

    @Test
    void rejectsAReferenceToAnUndefinedRule() {
        SourceFile constitution = SourceFile.of("docs/ARCHITECTURE_CONSTITUTION.md", "### ARC-001 — 唯一の経路\n");
        SourceFile source = SourceFile.of("core/domain/src/main/java/Sample.java", "// ARC-099 に従う\n");

        List<Violation> violations =
                DocumentationRules.check(List.of(constitution, matrixWith("ARC-001")), List.of(source), MATRIX);

        assertEquals(1, violations.size());
        assertEquals("CNF-005", violations.get(0).ruleId());
        assertTrue(violations.get(0).message().contains("ARC-099"));
    }

    @Test
    void rejectsADuplicateDefinition() {
        SourceFile first = SourceFile.of("docs/ARCHITECTURE_CONSTITUTION.md", "### ARC-001 — 唯一の経路\n");
        SourceFile second = SourceFile.of("docs/CODING_RULES.md", "### ARC-001 — 別の説明\n");

        List<Violation> violations =
                DocumentationRules.check(List.of(first, second, matrixWith("ARC-001")), List.of(), MATRIX);

        assertTrue(violations.stream().anyMatch(violation -> violation.message().contains("二重に定義")));
    }

    @Test
    void rejectsARuleThatIsMissingFromTheEnforcementMatrix() {
        SourceFile constitution = SourceFile.of("docs/ARCHITECTURE_CONSTITUTION.md", "### ARC-001 — 唯一の経路\n");

        List<Violation> violations =
                DocumentationRules.check(List.of(constitution, matrixWith()), List.of(), MATRIX);

        assertTrue(violations.stream().anyMatch(violation -> violation.message().contains("強制マトリクス")));
    }
}
