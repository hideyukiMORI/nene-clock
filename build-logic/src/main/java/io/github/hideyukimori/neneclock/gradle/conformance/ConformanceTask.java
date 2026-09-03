package io.github.hideyukimori.neneclock.gradle.conformance;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

/** `validateConformance`: NeNe Clock 固有の規約検査（CNF-001..010）を実行する。 */
public abstract class ConformanceTask extends DefaultTask {

    @Internal
    public abstract DirectoryProperty getRepositoryRoot();

    @Input
    public abstract MapProperty<String, Set<String>> getModuleGraph();

    @Input
    public abstract SetProperty<String> getApprovedModules();

    @Input
    public abstract SetProperty<String> getApprovedEdges();

    @Input
    public abstract Property<String> getToday();

    @OutputFile
    public abstract RegularFileProperty getReportFile();

    @TaskAction
    public void validate() {
        Path root = getRepositoryRoot().get().getAsFile().toPath();
        LocalDate today = LocalDate.parse(getToday().get());
        Map<String, Set<String>> graph = getModuleGraph().get();
        List<Violation> violations = new ConformanceCheck(root, today)
                .run(graph, getApprovedModules().get(), getApprovedEdges().get());

        String report = violations.isEmpty()
                ? "conformance: 違反なし\n"
                : violations.stream().map(Violation::render).collect(Collectors.joining("\n", "", "\n"));
        writeReport(report);

        if (!violations.isEmpty()) {
            throw new GradleException("規約検査に失敗（" + violations.size() + " 件）:\n" + report);
        }
        getLogger().lifecycle("conformance: 違反なし（{} モジュール）", graph.size());
    }

    private void writeReport(String report) {
        Path target = getReportFile().get().getAsFile().toPath();
        try {
            Files.createDirectories(target.getParent());
            Files.writeString(target, report, StandardCharsets.UTF_8);
        } catch (IOException failure) {
            throw new UncheckedIOException("レポートを書けない: " + target, failure);
        }
    }
}
