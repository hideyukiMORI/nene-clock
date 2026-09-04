package io.github.hideyukimori.neneclock.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;

class ProductIdentityTest {

    @Test
    void carriesTheVersionAndAuthorItWasGiven() {
        ProductIdentity identity = new ProductIdentity("0.2.2", "hideyukiMORI");

        assertThat(identity.version()).isEqualTo("0.2.2");
        assertThat(identity.author()).isEqualTo("hideyukiMORI");
    }

    @Test
    void refusesABlankVersion() {
        assertThatIllegalArgumentException().isThrownBy(() -> new ProductIdentity(" ", "hideyukiMORI"));
    }

    @Test
    void refusesABlankAuthor() {
        assertThatIllegalArgumentException().isThrownBy(() -> new ProductIdentity("0.2.2", ""));
    }
}
