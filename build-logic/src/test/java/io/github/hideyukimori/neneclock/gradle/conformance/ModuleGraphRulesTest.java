package io.github.hideyukimori.neneclock.gradle.conformance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** CNF-010 の negative proof（QLT-007）。 */
class ModuleGraphRulesTest {

    private static final Set<String> APPROVED_MODULES = Set.of(":core:domain", ":core:application", ":ui:swing");

    private static final Set<String> APPROVED_EDGES =
            Set.of(":core:application -> :core:domain", ":ui:swing -> :core:application");

    @Test
    void acceptsTheApprovedGraph() {
        Map<String, Set<String>> graph = new LinkedHashMap<>();
        graph.put(":core:domain", Set.of());
        graph.put(":core:application", Set.of(":core:domain"));
        graph.put(":ui:swing", Set.of(":core:application"));

        assertEquals(List.of(), ModuleGraphRules.check(graph, APPROVED_MODULES, APPROVED_EDGES));
    }

    @Test
    void rejectsAnUnapprovedModule() {
        Map<String, Set<String>> graph = Map.of(":core:sneaky", Set.of());

        List<Violation> violations = ModuleGraphRules.check(graph, APPROVED_MODULES, APPROVED_EDGES);

        assertEquals(1, violations.size());
        assertEquals("CNF-010", violations.get(0).ruleId());
    }

    @Test
    void rejectsAnInvertedDependency() {
        Map<String, Set<String>> graph = Map.of(":core:domain", Set.of(":core:application"));

        List<Violation> violations = ModuleGraphRules.check(graph, APPROVED_MODULES, APPROVED_EDGES);

        assertTrue(violations.stream().anyMatch(violation -> violation.message().contains("許可されていない依存")));
    }

    @Test
    void rejectsACycle() {
        Map<String, Set<String>> graph = new LinkedHashMap<>();
        graph.put(":core:domain", Set.of(":core:application"));
        graph.put(":core:application", Set.of(":core:domain"));

        List<Violation> violations = ModuleGraphRules.check(
                graph,
                APPROVED_MODULES,
                Set.of(":core:domain -> :core:application", ":core:application -> :core:domain"));

        assertTrue(violations.stream().anyMatch(violation -> violation.message().contains("循環")));
    }
}
