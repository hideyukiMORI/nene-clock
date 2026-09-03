package io.github.hideyukimori.neneclock.adapter.fontcatalog;

import io.github.hideyukimori.neneclock.application.TypefaceBinaryPort;
import io.github.hideyukimori.neneclock.domain.Typeface;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Objects;

/**
 * 同梱書体のファイルを返す {@link TypefaceBinaryPort} の実装。
 *
 * <p>アプリの中で書体の実体を持つのはこのモジュールだけである。ファイルは無改変で同梱してあり、
 * 出所と SHA-256 は {@code typefaces/provenance.tsv} に記録してある（ADR 0006）。
 */
public final class BundledTypefaceAdapter implements TypefaceBinaryPort {

    /** 資源の置き場所。このクラスのパッケージからの相対で解決する。 */
    static final String DIRECTORY = "typefaces/";

    private static final String EXTENSION = ".ttf";

    private BundledTypefaceAdapter() {}

    /** production の合成ルートが使う生成経路。 */
    public static BundledTypefaceAdapter bundled() {
        return new BundledTypefaceAdapter();
    }

    @Override
    public byte[] read(Typeface typeface) {
        Objects.requireNonNull(typeface, "typeface");
        return readResource(resourceNameOf(typeface));
    }

    /** 資源 1 つを読む。存在しない資源で落ちることをテストから示せるように分けてある。 */
    static byte[] readResource(String resource) {
        try (InputStream stream = BundledTypefaceAdapter.class.getResourceAsStream(resource)) {
            if (stream == null) {
                throw new IllegalStateException("同梱されているはずの書体が無い: " + resource);
            }
            return stream.readAllBytes();
        } catch (IOException failure) {
            throw new IllegalStateException("同梱書体を読めない: " + resource, failure);
        }
    }

    /**
     * 資源名を定数名から機械的に決める。
     *
     * <p>対応表を人が書くと、表とファイルがずれても誰も気づかない。導出にしておくと
     * 「定数はあるがファイルが無い」形でしかずれられず、それは起動時に必ず落ちる。
     */
    static String resourceNameOf(Typeface typeface) {
        return DIRECTORY + typeface.name().toLowerCase(Locale.ROOT).replace('_', '-') + EXTENSION;
    }
}
