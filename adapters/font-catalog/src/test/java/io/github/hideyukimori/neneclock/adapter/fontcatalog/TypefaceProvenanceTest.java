package io.github.hideyukimori.neneclock.adapter.fontcatalog;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.hideyukimori.neneclock.domain.BundledTypeface;
import io.github.hideyukimori.neneclock.domain.InterfaceTypeface;
import io.github.hideyukimori.neneclock.domain.Typeface;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * 同梱書体の出所（{@code typefaces/provenance.tsv}）が実体と一致していることを見る。
 *
 * <p>記録が実体とずれても誰も気づかない状態を作らないための検査である。ファイルを差し替えて
 * 記録を直し忘れたとき、あるいは記録だけ書き換えたときに落ちる。
 */
class TypefaceProvenanceTest {

    private final Map<String, String> recorded = readProvenance();

    @Test
    void recordsExactlyTheBundledTypefaces() {
        List<String> expected = new ArrayList<>();
        for (BundledTypeface typeface : everyBundledTypeface()) {
            expected.add(BundledTypefaceAdapter.resourceNameOf(typeface));
        }

        assertThat(recorded.keySet()).containsExactlyInAnyOrderElementsOf(expected);
    }

    /** 時計の書体と UI の書体をひとつづきに見る。検査を 2 本に分けない。 */
    private static List<BundledTypeface> everyBundledTypeface() {
        List<BundledTypeface> all = new ArrayList<>(List.of(Typeface.values()));
        all.addAll(List.of(InterfaceTypeface.values()));
        return List.copyOf(all);
    }

    @Test
    void everyRecordedChecksumMatchesTheBundledFile() {
        for (BundledTypeface typeface : everyBundledTypeface()) {
            String resource = BundledTypefaceAdapter.resourceNameOf(typeface);

            assertThat(sha256Of(BundledTypefaceAdapter.readResource(resource)))
                    .describedAs(resource)
                    .isEqualTo(recorded.get(resource));
        }
    }

    @Test
    void everyTypefaceCarriesItsLicence() {
        for (BundledTypeface typeface : everyBundledTypeface()) {
            String licence =
                    new String(BundledTypefaceAdapter.readResource(licenceNameOf(typeface)), StandardCharsets.UTF_8);

            assertThat(licence).describedAs(typeface.constantName()).contains("SIL OPEN FONT LICENSE");
        }
    }

    private static String licenceNameOf(BundledTypeface typeface) {
        return BundledTypefaceAdapter.resourceNameOf(typeface).replace(".ttf", ".license.txt");
    }

    private static String sha256Of(byte[] content) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (java.security.NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 は JDK に必ずある", impossible);
        }
    }

    private static Map<String, String> readProvenance() {
        String table = new String(
                BundledTypefaceAdapter.readResource(BundledTypefaceAdapter.DIRECTORY + "provenance.tsv"),
                StandardCharsets.UTF_8);
        Map<String, String> rows = new LinkedHashMap<>();
        for (String line : table.lines().toList()) {
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            String[] columns = line.split("\t", -1);
            rows.put(BundledTypefaceAdapter.DIRECTORY + columns[0] + ".ttf", columns[2].trim());
        }
        return rows;
    }
}
