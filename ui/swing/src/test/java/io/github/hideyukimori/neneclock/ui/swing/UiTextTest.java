package io.github.hideyukimori.neneclock.ui.swing;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.hideyukimori.neneclock.adapter.fontcatalog.BundledTypefaceAdapter;
import io.github.hideyukimori.neneclock.domain.Language;
import java.awt.Font;
import java.io.ByteArrayInputStream;
import java.util.EnumMap;
import java.util.Map;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * 文言が、その言語の書体で**描けるか**を見る。
 *
 * <p>🔴 この検査は実機で踏んだ不具合から生まれた。英語 UI の書体（Arimo）に日本語の字形が
 * 無いのに、言語の選択肢へ「日本語」と書いていたので、豆腐（□□□）で描かれていた。
 * 文言だけを見ても、書体だけを見ても気づけない。**組で見る必要がある。**
 */
class UiTextTest {

    private static final Map<Language, Font> FONTS = interfaceFonts();

    @ParameterizedTest
    @EnumSource(UiText.class)
    void everyTextCanBeDrawnInEveryLanguage(UiText text) {
        for (Language language : Language.values()) {
            String shown = text.in(language);

            Font font = fontOf(language);

            assertThat(font.canDisplayUpTo(shown))
                    .describedAs("%s の %s（%s）が %s で描けない", text.name(), language, shown, font.getFontName())
                    .isEqualTo(-1);
        }
    }

    @ParameterizedTest
    @EnumSource(UiText.class)
    void everyTextIsWrittenInBothLanguages(UiText text) {
        for (Language language : Language.values()) {
            assertThat(text.in(language))
                    .describedAs("%s の %s", text.name(), language)
                    .isNotBlank();
        }
    }

    private static Font fontOf(Language language) {
        Font font = FONTS.get(language);
        if (font == null) {
            throw new IllegalStateException("UI 書体が無い: " + language);
        }
        return font;
    }

    private static Map<Language, Font> interfaceFonts() {
        Map<Language, Font> fonts = new EnumMap<>(Language.class);
        for (Language language : Language.values()) {
            fonts.put(language, load(language));
        }
        return fonts;
    }

    private static Font load(Language language) {
        try {
            return Font.createFont(
                    Font.TRUETYPE_FONT,
                    new ByteArrayInputStream(BundledTypefaceAdapter.bundled().read(language.typeface())));
        } catch (java.awt.FontFormatException | java.io.IOException failure) {
            throw new IllegalStateException("UI 書体を読めない: " + language, failure);
        }
    }
}
