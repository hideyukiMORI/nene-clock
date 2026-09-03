package io.github.hideyukimori.neneclock.quality.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.library.GeneralCodingRules;
import org.junit.jupiter.api.Test;

/** ARC-006 / JAV-013: 可変グローバル状態を持たない、並行性は 1 種類だけ。 */
class RuntimeDisciplineRulesTest {

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
    void noStandardStreamsAndNoGenericExceptions() {
        GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS.check(PRODUCTION);
        GeneralCodingRules.NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS.check(PRODUCTION);
        GeneralCodingRules.NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING.check(PRODUCTION);
        GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION.check(PRODUCTION);
    }
}
