package io.github.hideyukimori.neneclock.ui.swing;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DragStrategyTest {

    @Test
    void eachStrategyIsNamedByItsOwnName() {
        assertThat(DragStrategy.named("window")).isEqualTo(DragStrategy.WINDOW);
        assertThat(DragStrategy.named("pointer")).isEqualTo(DragStrategy.POINTER);
        assertThat(DragStrategy.named("delta")).isEqualTo(DragStrategy.DELTA);
    }

    @Test
    void theNameIsReadWithoutCaringAboutCase() {
        assertThat(DragStrategy.named("POINTER")).isEqualTo(DragStrategy.POINTER);
        assertThat(DragStrategy.named("Delta")).isEqualTo(DragStrategy.DELTA);
    }

    @Test
    void aNameNobodyKnowsFallsBackToTodaysBehaviour() {
        assertThat(DragStrategy.named("nonsense")).isEqualTo(DragStrategy.WINDOW);
        assertThat(DragStrategy.named("")).isEqualTo(DragStrategy.WINDOW);
    }

    @Test
    void withoutThePropertyTheBuildKeepsTodaysBehaviour() {
        assertThat(DragStrategy.chosen()).isEqualTo(DragStrategy.WINDOW);
    }
}
