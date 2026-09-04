package io.github.hideyukimori.neneclock.app;

import io.github.hideyukimori.neneclock.domain.ProductIdentity;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * 同梱の {@code product.properties} から製品の名乗りを読む。
 *
 * <p>版はビルドが埋める（{@code gradle.properties} → {@code processResources}）。コードに版を書かないのは、
 * 2 か所に同じ数字があると片方だけ上がるからである。読めないのはビルドの欠陥なので、ここでは例外にする。
 */
final class ProductIdentityFile {

    private static final String RESOURCE = "product.properties";

    private ProductIdentityFile() {}

    static ProductIdentity read() {
        try (InputStream stream = ProductIdentityFile.class.getResourceAsStream(RESOURCE)) {
            if (stream == null) {
                throw new IllegalStateException("product.properties is missing from the build");
            }
            Properties properties = new Properties();
            try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                properties.load(reader);
            }
            return new ProductIdentity(properties.getProperty("version", ""), properties.getProperty("author", ""));
        } catch (IOException failure) {
            throw new UncheckedIOException(failure);
        }
    }
}
