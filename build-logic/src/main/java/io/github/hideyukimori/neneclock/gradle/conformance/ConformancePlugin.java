package io.github.hideyukimori.neneclock.gradle.conformance;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.language.base.plugins.LifecycleBasePlugin;

/**
 * `validateConformance` をルートプロジェクトへ登録する。
 *
 * <p>Gradle プラグインを Kotlin の precompiled script ではなく Java の {@link Plugin} として書く。
 * precompiled script は同じソースセットの Java クラスを解決できず、クリーンな作業木で
 * ビルドが落ちるため（実測は docs/quality/gate-proofs.md）。
 */
public final class ConformancePlugin implements Plugin<Project> {

    /** モジュールグラフの検査対象にする設定名。テスト依存は対象にしない。 */
    private static final Set<String> PRODUCTION_CONFIGURATIONS =
            Set.of("api", "implementation", "compileOnly", "runtimeOnly");

    private static final String GRAPH_DECLARATION = "config/architecture/module-graph.txt";

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(LifecycleBasePlugin.class);

        Declaration declaration = readDeclaration(project);
        TaskProvider<ConformanceTask> validate =
                project.getTasks().register("validateConformance", ConformanceTask.class, task -> {
                    task.setGroup(LifecycleBasePlugin.VERIFICATION_GROUP);
                    task.setDescription("NeNe Clock 固有の規約検査（CNF-001..010）");
                    task.getRepositoryRoot().set(project.getLayout().getProjectDirectory());
                    task.getApprovedModules().set(declaration.modules());
                    task.getApprovedEdges().set(declaration.edges());
                    task.getToday().set(LocalDate.now().toString());
                    task.getReportFile()
                            .set(project.getLayout().getBuildDirectory().file("reports/conformance/conformance.txt"));
                    task.getOutputs().upToDateWhen(unused -> false);
                });

        project.getGradle().projectsEvaluated(gradle -> validate.configure(task -> task.getModuleGraph()
                .set(moduleGraph(project))));

        project.getTasks().named(LifecycleBasePlugin.CHECK_TASK_NAME).configure(task -> task.dependsOn(validate));
    }

    private static Map<String, Set<String>> moduleGraph(Project root) {
        Map<String, Set<String>> graph = new LinkedHashMap<>();
        for (Project module : root.getSubprojects()) {
            Set<String> dependencies = new LinkedHashSet<>();
            for (Configuration configuration : module.getConfigurations()) {
                if (!PRODUCTION_CONFIGURATIONS.contains(configuration.getName())) {
                    continue;
                }
                for (Dependency dependency : configuration.getDependencies()) {
                    if (dependency instanceof ProjectDependency projectDependency) {
                        dependencies.add(projectDependency.getPath());
                    }
                }
            }
            graph.put(module.getPath(), dependencies);
        }
        return graph;
    }

    private static Declaration readDeclaration(Project project) {
        Set<String> modules = new LinkedHashSet<>();
        Set<String> edges = new LinkedHashSet<>();
        List<String> lines;
        try {
            lines = Files.readAllLines(
                    project.getRootDir().toPath().resolve(GRAPH_DECLARATION), StandardCharsets.UTF_8);
        } catch (IOException failure) {
            throw new UncheckedIOException("モジュールグラフの宣言を読めない: " + GRAPH_DECLARATION, failure);
        }
        for (String raw : lines) {
            int comment = raw.indexOf('#');
            String line = (comment < 0 ? raw : raw.substring(0, comment)).trim();
            if (line.startsWith("module ")) {
                modules.add(line.substring("module ".length()).trim());
            } else if (line.startsWith("edge ")) {
                edges.add(line.substring("edge ".length()).trim());
            }
        }
        return new Declaration(modules, edges);
    }

    private record Declaration(Set<String> modules, Set<String> edges) {}
}
