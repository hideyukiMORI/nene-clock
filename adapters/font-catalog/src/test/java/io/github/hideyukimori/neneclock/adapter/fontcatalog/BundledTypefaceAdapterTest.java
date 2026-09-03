package io.github.hideyukimori.neneclock.adapter.fontcatalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.hideyukimori.neneclock.application.TypefaceBinaryPort;
import io.github.hideyukimori.neneclock.domain.InterfaceTypeface;
import io.github.hideyukimori.neneclock.domain.Typeface;
import java.awt.Font;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Locale;
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

    /**
     * 太さの名前。既定インスタンスがこれらであってはならない。
     *
     * <p>🔴 この検査は実測から生まれた。可変フォント（`Font[wght].ttf`）を同梱すると、
     * Java 21 は軸を選べないので**既定のアウトライン**で描く。Google の可変フォントの多くは
     * 既定が最も細いマスタなので、Montserrat が Thin で、Source Code Pro が ExtraLight で描かれていた。
     * 「読み込める」ことしか見ていなかったので、画面を見るまで気づけなかった。
     */
    private static final List<String> WEIGHT_WORDS =
            List.of("thin", "extralight", "light", "medium", "semibold", "bold", "black", "italic", "oblique");

    @ParameterizedTest
    @EnumSource(Typeface.class)
    void everyTypefaceRendersAtRegularWeight(Typeface typeface) throws Exception {
        Font created = Font.createFont(Font.TRUETYPE_FONT, new ByteArrayInputStream(typefaces.read(typeface)));
        String name = created.getFontName().toLowerCase(Locale.ROOT).replace(" ", "");

        assertThat(WEIGHT_WORDS)
                .describedAs("%s は %s として描かれる", typeface.name(), created.getFontName())
                .noneMatch(name::contains);
    }

    @ParameterizedTest
    @EnumSource(InterfaceTypeface.class)
    void everyInterfaceTypefaceIsBundledAndRendersAtRegularWeight(InterfaceTypeface typeface) throws Exception {
        Font created = Font.createFont(Font.TRUETYPE_FONT, new ByteArrayInputStream(typefaces.read(typeface)));
        String name = created.getFontName().toLowerCase(Locale.ROOT).replace(" ", "");

        assertThat(WEIGHT_WORDS)
                .describedAs("%s は %s として描かれる", typeface.name(), created.getFontName())
                .noneMatch(name::contains);
    }

    @Test
    void theJapaneseInterfaceTypefaceCanDrawJapanese() {
        // 🔴 「読み込める」だけでは足りない。日本語の字形を持たない書体を選んでも読み込めてしまう。
        Font created = fontOf(InterfaceTypeface.ZEN_KAKU_GOTHIC_NEW);

        assertThat(created.canDisplayUpTo("設定 書体 文字色 背景色 言語")).isEqualTo(-1);
    }

    @Test
    void theLatinInterfaceTypefaceCoversTheEnglishWords() {
        Font created = fontOf(InterfaceTypeface.ARIMO);

        assertThat(created.canDisplayUpTo("Settings Typeface Background Language"))
                .isEqualTo(-1);
    }

    private Font fontOf(InterfaceTypeface typeface) {
        try {
            return Font.createFont(Font.TRUETYPE_FONT, new ByteArrayInputStream(typefaces.read(typeface)));
        } catch (java.awt.FontFormatException | java.io.IOException failure) {
            throw new IllegalStateException("同梱書体を読めない: " + typeface.name(), failure);
        }
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
