package io.github.hideyukimori.neneclock.adapter.systemtime;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class SystemWallClockAdapterTest {

    @Test
    void readsTheInjectedClockRatherThanTheWallClock() {
        Clock fixed = Clock.fixed(Instant.parse("2026-09-03T04:45:09Z"), ZoneId.of("UTC"));

        LocalDateTime moment = SystemWallClockAdapter.using(fixed).currentDateTime();

        assertThat(moment).isEqualTo(LocalDateTime.of(2026, 9, 3, 4, 45, 9));
    }

    @Test
    void theProductionFactoryProducesAUsableAdapter() {
        assertThat(SystemWallClockAdapter.system().currentDateTime()).isNotNull();
    }
}
