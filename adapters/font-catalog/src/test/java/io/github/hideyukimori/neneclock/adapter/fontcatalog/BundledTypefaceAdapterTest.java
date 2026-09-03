package io.github.hideyukimori.neneclock.adapter.fontcatalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.hideyukimori.neneclock.application.TypefaceBinaryPort;
import io.github.hideyukimori.neneclock.domain.Typeface;
import java.awt.Font;
import java.io.ByteArrayInputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class BundledTypefaceAdapterTest {

    private final TypefaceBinaryPort typefaces = BundledTypefaceAdapter.bundled();

    @ParameterizedTest
    @EnumSource(Typeface.class)
    void everyTypefaceIsBundled(Typeface typeface) {
        assertThat(typefaces.read(typeface)).isNotEmpty();
    }

    @ParameterizedTest
    @EnumSource(Typeface.class)
    void everyBundledFileIsAFontAwtCanCreate(Typeface typeface) throws Exception {
        // 「ファイルがある」では足りない。壊れた TTF でもバイト列は返るので、描ける形かまで見る。
        Font created = Font.createFont(Font.TRUETYPE_FONT, new ByteArrayInputStream(typefaces.read(typeface)));

        assertThat(created.getFontName()).isNotBlank();
    }

    @Test
    void resourceNamesAreDerivedFromTheConstantName() {
        assertThat(BundledTypefaceAdapter.resourceNameOf(Typeface.DM_SANS)).isEqualTo("typefaces/dm-sans.ttf");
        assertThat(BundledTypefaceAdapter.resourceNameOf(Typeface.VT323)).isEqualTo("typefaces/vt323.ttf");
    }

    @Test
    void readingIsIndependentOfTheCallersCopy() {
        byte[] first = typefaces.read(Typeface.DEFAULT);
        first[1] = 0;

        assertThat(typefaces.read(Typeface.DEFAULT)[1]).isNotEqualTo((byte) 0);
    }

    @Test
    void aMissingFileIsAPackagingFaultNotAUserFacingFailure() {
        // 梱包の誤りは結果型で返さない（JAV-005）。落ちることそのものがここでの正しい振る舞い。
        assertThatThrownBy(() -> BundledTypefaceAdapter.readResource("typefaces/not-bundled.ttf"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("typefaces/not-bundled.ttf");
    }
}
