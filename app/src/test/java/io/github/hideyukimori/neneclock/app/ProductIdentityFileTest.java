package io.github.hideyukimori.neneclock.app;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.hideyukimori.neneclock.domain.ProductIdentity;
import org.junit.jupiter.api.Test;

class ProductIdentityFileTest {

    @Test
    void theBuildFillsInARealVersionAndAnAuthor() {
        ProductIdentity identity = ProductIdentityFile.read();

        assertThat(identity.version()).matches("\\d+\\.\\d+\\.\\d+");
        assertThat(identity.version()).doesNotContain("${");
        assertThat(identity.author()).isEqualTo("hideyukiMORI");
    }
}
