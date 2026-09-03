package io.github.hideyukimori.neneclock.gradle.conformance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/** CNF-011 の negative proof（QLT-007）。 */
class ConfigurationWiringRulesTest {

    private static final SourceFile CONVENTIONS = SourceFile.of(
            "build-logic/src/main/kotlin/neneclock.java-conventions.gradle.kts",
            """
            forbiddenApis {
                signaturesFiles = files(rootProject.file("config/forbiddenapis/base.txt"))
            }
            """);

    @Test
    void acceptsAConfigurationFileThatSomeBuildScriptReads() {
        List<Violation> violations =
                ConfigurationWiringRules.check(List.of("config/forbiddenapis/base.txt"), List.of(CONVENTIONS));

        assertEquals(List.of(), violations);
    }

    @Test
    void rejectsAConfigurationFileThatNothingReads() {
        List<Violation> violations = ConfigurationWiringRules.check(
                List.of("config/forbiddenapis/base.txt", "config/forbiddenapis/determinism.txt"),
                List.of(CONVENTIONS));

        assertEquals(1, violations.size());
        assertEquals("CNF-011", violations.get(0).ruleId());
        assertTrue(violations.get(0).path().endsWith("determinism.txt"));
    }

    @Test
    void ignoresPathsOutsideTheConfigurationDirectory() {
        List<Violation> violations =
                ConfigurationWiringRules.check(List.of("docs/QUALITY_GATES.md"), List.of(CONVENTIONS));

        assertEquals(List.of(), violations);
    }
}
