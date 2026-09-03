package io.github.hideyukimori.neneclock.gradle.conformance;

import java.util.List;
import java.util.Objects;

/** 走査対象の 1 ファイル。ファイルシステムから切り離してテストできるようにする。 */
public record SourceFile(String path, List<String> lines) {

    public SourceFile {
        Objects.requireNonNull(path, "path");
        lines = List.copyOf(Objects.requireNonNull(lines, "lines"));
    }

    public static SourceFile of(String path, String content) {
        return new SourceFile(path, List.of(content.split("\n", -1)));
    }

    public String fileName() {
        int slash = path.lastIndexOf('/');
        return slash < 0 ? path : path.substring(slash + 1);
    }
}
