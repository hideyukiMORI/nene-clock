package io.github.hideyukimori.neneclock.adapter.fontcatalog;

import io.github.hideyukimori.neneclock.application.FontCatalogPort;
import io.github.hideyukimori.neneclock.domain.FontFamily;
import io.github.hideyukimori.neneclock.domain.FontFamilyOutcome;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * AWT が知っている書体一覧を返す {@link FontCatalogPort} の実装。
 *
 * <p>アプリ全体で実行環境の書体を覗くのはこのクラスだけである。
 * 環境が返す名前のうち、{@link FontFamily} の不変条件を満たさないものは黙って捨てる
 * （選ばせないだけで、利用者への通知が要る失敗ではない）。
 */
public final class AwtFontCatalogAdapter implements FontCatalogPort {

    private AwtFontCatalogAdapter() {}

    /** production の合成ルートが使う生成経路。 */
    public static AwtFontCatalogAdapter system() {
        return new AwtFontCatalogAdapter();
    }

    @Override
    public List<FontFamily> availableFamilies() {
        List<FontFamily> families = new ArrayList<>();
        for (String name : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames(Locale.ROOT)) {
            switch (FontFamily.of(name)) {
                case FontFamilyOutcome.Accepted accepted -> families.add(accepted.value());
                case FontFamilyOutcome.Rejected unusable -> {
                    // 環境が返した名前がこのアプリの不変条件を満たさないだけ。選択肢から外す。
                }
            }
        }
        if (!families.contains(FontFamily.DEFAULT)) {
            families.add(FontFamily.DEFAULT);
        }
        families.sort(Comparator.comparing(FontFamily::name));
        return List.copyOf(families);
    }
}
