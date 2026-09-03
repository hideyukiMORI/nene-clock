package io.github.hideyukimori.neneclock.gradle.conformance;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/** リポジトリ全体に対して規約検査を走らせる入口。Gradle からも単体テストからも同じ経路で呼ぶ。 */
public final class ConformanceCheck {

    private static final List<String> IGNORED_DIRECTORIES =
            List.of(".git", "build", ".gradle", ".kotlin", "node_modules", ".idea");

    private static final String MATRIX_PATH = "docs/QUALITY_GATES.md";

    private final Path root;
    private final LocalDate today;

    public ConformanceCheck(Path root, LocalDate today) {
        this.root = root;
        this.today = today;
    }

    public List<Violation> run(Map<String, Set<String>> moduleGraph, Set<String> approvedModules, Set<String> approvedEdges) {
        List<Violation> violations = new ArrayList<>();
        List<String> allPaths = listPaths();

        List<String> referencedWaivers = new ArrayList<>();
        List<SourceFile> javaSources = read(allPaths, path -> path.endsWith(".java") && isProductionSource(path));
        for (SourceFile source : javaSources) {
            JavaSourceRules.Result result = JavaSourceRules.check(source);
            violations.addAll(result.violations());
            referencedWaivers.addAll(result.referencedWaivers());
        }

        List<SourceFile> documents = read(allPaths, path -> path.endsWith(".md") && !path.startsWith("docs/waivers/"));
        List<SourceFile> configurationFiles =
                read(allPaths, path -> path.endsWith(".gradle.kts") || path.startsWith("config/") || path.startsWith(".github/"));
        violations.addAll(DocumentationRules.check(documents, javaSources, MATRIX_PATH));

        List<SourceFile> waiverFiles = read(allPaths, path -> path.startsWith("docs/waivers/") && path.endsWith(".md"));
        SourceFile waiverIndex = waiverFiles.stream()
                .filter(file -> file.path().equals("docs/waivers/README.md"))
                .findFirst()
                .orElse(null);
        violations.addAll(WaiverLedger.check(waiverFiles, waiverIndex, referencedWaivers, today));

        violations.addAll(BaselineRules.check(allPaths, configurationFiles));

        List<SourceFile> wiringSources =
                read(allPaths, path -> path.endsWith(".gradle.kts") || path.startsWith("build-logic/src/main/java/"));
        violations.addAll(ConfigurationWiringRules.check(allPaths, wiringSources));
        violations.addAll(ModuleGraphRules.check(moduleGraph, approvedModules, approvedEdges));
        return List.copyOf(violations);
    }

    private static boolean isProductionSource(String path) {
        return path.contains("/src/main/java/") && !path.startsWith("build-logic/");
    }

    private List<String> listPaths() {
        try (Stream<Path> walk = Files.walk(root)) {
            return walk.filter(Files::isRegularFile)
                    .map(path -> root.relativize(path).toString().replace('\\', '/'))
                    .filter(ConformanceCheck::isVisible)
                    .sorted()
                    .toList();
        } catch (IOException failure) {
            throw new UncheckedIOException("リポジトリを走査できない: " + root, failure);
        }
    }

    private static boolean isVisible(String path) {
        for (String ignored : IGNORED_DIRECTORIES) {
            if (path.equals(ignored) || path.startsWith(ignored + "/") || path.contains("/" + ignored + "/")) {
                return false;
            }
        }
        return true;
    }

    private List<SourceFile> read(List<String> paths, java.util.function.Predicate<String> filter) {
        List<SourceFile> files = new ArrayList<>();
        for (String path : paths) {
            if (!filter.test(path)) {
                continue;
            }
            try {
                files.add(new SourceFile(path, Files.readAllLines(root.resolve(path), StandardCharsets.UTF_8)));
            } catch (IOException failure) {
                throw new UncheckedIOException("ファイルを読めない: " + path, failure);
            }
        }
        return files;
    }
}
