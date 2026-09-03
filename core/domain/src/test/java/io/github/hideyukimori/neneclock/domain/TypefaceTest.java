package io.github.hideyukimori.neneclock.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class TypefaceTest {

    @Test
    void offersThirtyTypefaces() {
        // 数そのものが仕様（FR-043）。減らすときは仕様を先に変える。
        assertThat(Typeface.values()).hasSize(30);
    }

    @Test
    void hasNoDuplicateDisplayNames() {
        List<String> names = new ArrayList<>();
        for (Typeface typeface : Typeface.values()) {
            names.add(typeface.displayName());
        }

        assertThat(names).doesNotHaveDuplicates();
    }

    @ParameterizedTest
    @EnumSource(Typeface.class)
    void everyTypefaceHasANameToShow(Typeface typeface) {
        assertThat(typeface.displayName()).isNotBlank();
    }

    @ParameterizedTest
    @EnumSource(TypefaceMood.class)
    void everyMoodHasAtLeastOneTypeface(TypefaceMood mood) {
        // 空の分類が設定画面に並ぶことを防ぐ。
        assertThat(Typeface.values()).anyMatch(typeface -> typeface.mood() == mood);
    }

    @Test
    void defaultsToAMonospacedTypefaceSoDigitsDoNotShift() {
        assertThat(Typeface.DEFAULT.mood()).isEqualTo(TypefaceMood.MONO);
    }
}
