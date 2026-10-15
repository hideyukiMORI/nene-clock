package io.github.hideyukimori.neneclock.ui.swing;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.hideyukimori.neneclock.adapter.fontcatalog.BundledTypefaceAdapter;
import io.github.hideyukimori.neneclock.application.TypefaceBinaryPort;
import io.github.hideyukimori.neneclock.domain.BundledTypeface;
import io.github.hideyukimori.neneclock.domain.Typeface;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TypefaceFontLoaderTest {

    /** 書体ファイルを何回読まれたかを数える。読んだ回数が、TrueType を解析した回数である。 */
    private static final class CountingTypefaces implements TypefaceBinaryPort {
        private final TypefaceBinaryPort bundled = BundledTypefaceAdapter.bundled();
        private final Map<BundledTypeface, Integer> reads = new HashMap<>();

        @Override
        public byte[] read(BundledTypeface typeface) {
            reads.merge(typeface, 1, Integer::sum);
            return bundled.read(typeface);
        }

        int readsOf(BundledTypeface typeface) {
            return reads.getOrDefault(typeface, 0);
        }
    }

    @Test
    void parsesEachTypefaceOnlyOnceWhateverTheSize() {
        // 🔴 設定ダイアログを開いている間、200ms の刻みごとに 5 回、書体を解析し直していた（毎秒 25 回）。
        //    1 回 0.5〜15ms、そのたびに一時ファイルを 1 枚書く（docs/quality/gate-proofs.md 第 31 節）。
        CountingTypefaces typefaces = new CountingTypefaces();
        TypefaceFontLoader loader = new TypefaceFontLoader(typefaces);

        for (int tick = 0; tick < 5; tick++) {
            loader.load(Typeface.ORBITRON, 40);
            loader.load(Typeface.ORBITRON, 14);
            loader.load(Typeface.ANTON, 40);
        }

        assertThat(typefaces.readsOf(Typeface.ORBITRON)).isEqualTo(1);
        assertThat(typefaces.readsOf(Typeface.ANTON)).isEqualTo(1);
    }

    @Test
    void stillGivesTheSizeThatWasAskedFor() {
        TypefaceFontLoader loader = new TypefaceFontLoader(new CountingTypefaces());

        Font large = loader.load(Typeface.ORBITRON, 40);
        Font small = loader.load(Typeface.ORBITRON, 14);

        assertThat(large.getSize()).isEqualTo(40);
        assertThat(small.getSize()).isEqualTo(14);
        assertThat(small.getFontName()).isEqualTo(large.getFontName());
        assertThat(small.getStyle()).isEqualTo(Font.PLAIN);
    }
}
