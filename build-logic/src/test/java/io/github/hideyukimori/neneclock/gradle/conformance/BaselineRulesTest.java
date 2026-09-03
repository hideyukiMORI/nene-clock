package io.github.hideyukimori.neneclock.gradle.conformance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/** CNF-008 の negative proof（QLT-007）。 */
class BaselineRulesTest {

    @Test
    void acceptsARepositoryWithoutBaselines() {
        List<Violation> violations = BaselineRules.check(
                List.of("config/checkstyle/checkstyle.xml"),
                List.of(SourceFile.of("build.gradle.kts", "plugins {}\n")));

        assertEquals(List.of(), violations);
    }

    @Test
    void rejectsABaselineFile() {
        List<Violation> violations = BaselineRules.check(List.of("config/checkstyle/baseline.xml"), List.of());

        assertEquals(1, violations.size());
        assertEquals("CNF-008", violations.get(0).ruleId());
    }

    @Test
    void rejectsASuppressionsFile() {
        List<Violation> violations =
                BaselineRules.check(List.of("config/checkstyle/checkstyle-suppressions.xml"), List.of());

        assertEquals(1, violations.size());
    }

    @Test
    void rejectsAGateDisablingFlag() {
        List<Violation> violations = BaselineRules.check(
                List.of(), List.of(SourceFile.of("build.gradle.kts", "checkstyle { ignoreFailures = true }\n")));

        assertEquals(1, violations.size());
        assertTrue(violations.get(0).message().contains("ignoreFailures"));
    }
}
