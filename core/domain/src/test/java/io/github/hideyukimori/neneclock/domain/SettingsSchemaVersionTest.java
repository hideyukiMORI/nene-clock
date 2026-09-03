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
    void anUnknownFutureVersionIsNeitherSupportedNorMigratable() {
        SettingsSchemaVersion future = new SettingsSchemaVersion(SettingsSchemaVersion.CURRENT.value() + 1);

        assertThat(future.isSupported()).isFalse();
        assertThat(future.isMigratable()).isFalse();
    }

    @Test
    void theEarliestVersionIsMigratableButNotSupported() {
        assertThat(SettingsSchemaVersion.EARLIEST_MIGRATABLE.isMigratable()).isTrue();
        assertThat(SettingsSchemaVersion.EARLIEST_MIGRATABLE.isSupported()).isFalse();
    }

    @Test
    void theCurrentVersionIsMigratable() {
        assertThat(SettingsSchemaVersion.CURRENT.isMigratable()).isTrue();
    }

    @Test
    void rejectsAVersionBelowOne() {
        assertThatIllegalArgumentException().isThrownBy(() -> new SettingsSchemaVersion(0));
    }
}
