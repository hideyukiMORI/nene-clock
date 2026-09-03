package io.github.hideyukimori.neneclock.quality.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

/**
 * ARC-003: 中核はプラットフォームから独立している。
 *
 * <p>🔑 Swing も {@code java.util.prefs} も JDK 同梱なので、Gradle のモジュールグラフでは
 * 「core が Swing を import できない」を作れない。そこはこの層が塞ぐ（QLT-006）。
 */
class PlatformIsolationRulesTest {

    private static final String DOMAIN = "io.github.hideyukimori.neneclock.domain..";
    private static final String APPLICATION = "io.github.hideyukimori.neneclock.application..";
    private static final String PREFERENCES_ADAPTER = "io.github.hideyukimori.neneclock.adapter.preferences..";

    private static final JavaClasses PRODUCTION = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("io.github.hideyukimori.neneclock");

    @Test
    void coreDoesNotKnowAboutTheDesktopToolkit() {
        noClasses()
                .that()
                .resideInAnyPackage(DOMAIN, APPLICATION)
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("javax.swing..", "java.awt..")
                .because("ARC-003: 中核は Swing を知らない")
                .check(PRODUCTION);
    }

    @Test
    void onlyThePreferencesAdapterTouchesPreferences() {
        noClasses()
                .that()
                .resideOutsideOfPackage(PREFERENCES_ADAPTER)
                .should()
                .dependOnClassesThat()
                .resideInAPackage("java.util.prefs..")
                .because("ARC-002: 設定の永続化に触れる場所は 1 つ")
                .check(PRODUCTION);
    }

    @Test
    void onlyTheApplicationLayerFormatsTime() {
        noClasses()
                .that()
                .resideOutsideOfPackage(APPLICATION)
                .should()
                .dependOnClassesThat()
                .resideInAPackage("java.time.format..")
                .because("ARC-001: 表示文字列を作る経路は 1 本")
                .check(PRODUCTION);
    }
}
