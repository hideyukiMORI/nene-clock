package io.github.hideyukimori.neneclock.application;

import io.github.hideyukimori.neneclock.domain.FontFamily;
import java.util.List;

/**
 * 実行環境で利用可能な書体を知る唯一の窓口（ARC-007）。
 *
 * <p>利用可能かどうかは環境依存の事実であって、保存された設定の正しさではない（FR-043）。
 * 一覧は「設定画面で選べるもの」を決めるためだけに使う。
 *
 * <p>production の実装は {@code :adapters:font-catalog} にただ 1 つ。これ以外の場所で
 * {@code java.awt.GraphicsEnvironment} を呼ぶことは forbidden-apis が拒否する。
 */
@FunctionalInterface
public interface FontCatalogPort {

    /** 利用可能な書体。名前順で、重複を含まない。 */
    List<FontFamily> availableFamilies();
}
