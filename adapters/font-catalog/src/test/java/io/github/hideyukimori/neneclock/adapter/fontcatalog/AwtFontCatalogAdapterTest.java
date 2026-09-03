package io.github.hideyukimori.neneclock.adapter.fontcatalog;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.hideyukimori.neneclock.domain.FontFamily;
import io.github.hideyukimori.neneclock.domain.FontFamilyOutcome;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;

class AwtFontCatalogAdapterTest {

    private final List<FontFamily> families = AwtFontCatalogAdapter.system().availableFamilies();

    @Test
    void alwaysOffersTheDefaultFamily() {
        // 論理フォントはどの環境にも存在するが、環境が返さなかった場合でも選択肢から消えないこと。
        assertThat(families).contains(FontFamily.DEFAULT);
    }

    @Test
    void isSortedByName() {
        assertThat(families).isSortedAccordingTo(Comparator.comparing(FontFamily::name));
    }

    @Test
    void containsNoDuplicates() {
        assertThat(families).doesNotHaveDuplicates();
    }

    @Test
    void everyOfferedFamilySurvivesItsOwnValidation() {
        assertThat(families)
                .allSatisfy(family ->
                        assertThat(FontFamily.of(family.name())).isInstanceOf(FontFamilyOutcome.Accepted.class));
    }
}
