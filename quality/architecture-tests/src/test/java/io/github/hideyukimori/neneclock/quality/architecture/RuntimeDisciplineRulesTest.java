package io.github.hideyukimori.neneclock.quality.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.library.GeneralCodingRules;
import io.github.hideyukimori.neneclock.application.SettingsHandler;
import org.junit.jupiter.api.Test;

/** ARC-005 / ARC-006 / JAV-013: 可変な場所を数えられる状態に保ち、並行性は 1 種類だけにする。 */
class RuntimeDisciplineRulesTest {

    private static final String DOMAIN = "io.github.hideyukimori.neneclock.domain..";
    private static final String APPLICATION = "io.github.hideyukimori.neneclock.application..";
    private static final String COMPOSITION_ROOT = "io.github.hideyukimori.neneclock.app..";

    private static final JavaClasses PRODUCTION = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("io.github.hideyukimori.neneclock");

    @Test
    void staticStateIsImmutable() {
        fields().that()
                .areStatic()
                .should()
                .beFinal()
                .because("ARC-006: 可変なグローバル状態を持たない")
                .check(PRODUCTION);
    }

    @Test
    void domainStateIsImmutable() {
        fields().that()
                .areDeclaredInClassesThat()
                .resideInAPackage(DOMAIN)
                .should()
                .beFinal()
                .because("JAV-003: domain の状態は不変")
                .check(PRODUCTION);
    }

    @Test
    void theOnlyMutableApplicationStateIsTheSettingsOwner() {
        fields().that()
                .areDeclaredInClassesThat()
                .resideInAPackage(APPLICATION)
                .and()
                .areNotDeclaredIn(SettingsHandler.class)
                .should()
                .beFinal()
                .because("ARC-005: application の可変な隔離区画は SettingsHandler だけ（ADR 0004）")
                .check(PRODUCTION);
    }

    @Test
    void concurrencyIsSwingTimersOnly() {
        noClasses()
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("java.util.concurrent..")
                .because("JAV-013: 並行性の道具は javax.swing.Timer だけにする")
                .check(PRODUCTION);
    }

    @Test
    void noRawThreadsOrLegacyTimers() {
        noClasses()
                .should()
                .dependOnClassesThat()
                .haveFullyQualifiedName("java.lang.Thread")
                .orShould()
                .dependOnClassesThat()
                .haveFullyQualifiedName("java.util.Timer")
                .because("JAV-013 / SWG-001: EDT 以外のスレッドを作らない")
                .check(PRODUCTION);
    }

    @Test
    void onlyTheCompositionRootTalksToTheTerminal() {
        noClasses()
                .that()
                .resideOutsideOfPackage(COMPOSITION_ROOT)
                .should(GeneralCodingRules.ACCESS_STANDARD_STREAMS)
                .because("ARC-006: 端末へ出せるのは合成ルートだけ（ADR 0005）")
                .check(PRODUCTION);
    }

    @Test
    void noGenericExceptionsAndNoHiddenWiring() {
        GeneralCodingRules.NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS.check(PRODUCTION);
        GeneralCodingRules.NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING.check(PRODUCTION);
        GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION.check(PRODUCTION);
    }
}
