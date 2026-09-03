package io.github.hideyukimori.neneclock.gradle.conformance;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * モジュールグラフ（CNF-010 / ARC-002）。
 *
 * <p>依存方向を「レビューで気をつけること」ではなく「Gradle が拒否すること」にする。
 * 承認外モジュール・許可されていない依存辺・循環の 3 つを見る。
 */
public final class ModuleGraphRules {

    private ModuleGraphRules() {}

    public static List<Violation> check(
            Map<String, Set<String>> graph, Set<String> approvedModules, Set<String> approvedEdges) {
        List<Violation> violations = new ArrayList<>();

        for (String module : graph.keySet()) {
            if (!approvedModules.contains(module)) {
                violations.add(Violation.atFile(
                        "CNF-010", "settings.gradle.kts", "承認されていないモジュール: " + module));
            }
        }
        for (Map.Entry<String, Set<String>> entry : graph.entrySet()) {
            for (String target : entry.getValue()) {
                String edge = entry.getKey() + " -> " + target;
                if (!approvedEdges.contains(edge)) {
                    violations.add(Violation.atFile(
                            "CNF-010", "docs/PROJECT_LAYOUT.md", "許可されていない依存: " + edge));
                }
            }
        }
        List<String> cycle = findCycle(graph);
        if (!cycle.isEmpty()) {
            violations.add(Violation.atFile(
                    "CNF-010", "settings.gradle.kts", "モジュール依存に循環がある: " + String.join(" -> ", cycle)));
        }
        return List.copyOf(violations);
    }

    private static List<String> findCycle(Map<String, Set<String>> graph) {
        Set<String> settled = new LinkedHashSet<>();
        for (String start : graph.keySet()) {
            if (settled.contains(start)) {
                continue;
            }
            Deque<String> path = new ArrayDeque<>();
            Set<String> onPath = new LinkedHashSet<>();
            if (visit(graph, start, settled, onPath, path)) {
                List<String> cycle = new ArrayList<>(path);
                java.util.Collections.reverse(cycle);
                return cycle;
            }
        }
        return List.of();
    }

    private static boolean visit(
            Map<String, Set<String>> graph,
            String module,
            Set<String> settled,
            Set<String> onPath,
            Deque<String> path) {
        if (onPath.contains(module)) {
            path.push(module);
            return true;
        }
        if (settled.contains(module)) {
            return false;
        }
        onPath.add(module);
        for (String target : graph.getOrDefault(module, Set.of())) {
            if (visit(graph, target, settled, onPath, path)) {
                path.push(module);
                return true;
            }
        }
        onPath.remove(module);
        settled.add(module);
        return false;
    }
}
