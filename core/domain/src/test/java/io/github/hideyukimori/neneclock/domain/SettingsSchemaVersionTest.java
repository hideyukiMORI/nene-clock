package io.github.hideyukimori.neneclock.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;

class SettingsSchemaVersionTest {

    @Test
    void theCurrentVersionIsSupported() {
        assertThat(SettingsSchemaVersion.CURRENT.isSupported()).isTrue();
    }

    @Test
    void anUnknownFutureVersionIsNotSupported() {
        SettingsSchemaVersion future = new SettingsSchemaVersion(SettingsSchemaVersion.CURRENT.value() + 1);

        assertThat(future.isSupported()).isFalse();
    }

    @Test
    void rejectsAVersionBelowOne() {
        assertThatIllegalArgumentException().isThrownBy(() -> new SettingsSchemaVersion(0));
    }
}
