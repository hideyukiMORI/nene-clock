package io.github.hideyukimori.neneclock.quality.architecture;

import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

/** ARC-002: 依存方向を実行可能な検査にする（QLT-006）。 */
class LayerRulesTest {

    private static final JavaClasses PRODUCTION = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("io.github.hideyukimori.neneclock");

    @Test
    void dependenciesPointInwardOnly() {
        layeredArchitecture()
                .consideringOnlyDependenciesInLayers()
                .layer("Domain")
                .definedBy("io.github.hideyukimori.neneclock.domain..")
                .layer("Application")
                .definedBy("io.github.hideyukimori.neneclock.application..")
                .layer("Adapters")
                .definedBy("io.github.hideyukimori.neneclock.adapter..")
                .layer("Ui")
                .definedBy("io.github.hideyukimori.neneclock.ui..")
                .layer("Composition")
                .definedBy("io.github.hideyukimori.neneclock.app..")
                .whereLayer("Composition")
                .mayNotBeAccessedByAnyLayer()
                .whereLayer("Adapters")
                .mayOnlyBeAccessedByLayers("Composition")
                .whereLayer("Ui")
                .mayOnlyBeAccessedByLayers("Composition")
                .whereLayer("Application")
                .mayOnlyBeAccessedByLayers("Adapters", "Ui", "Composition")
                .whereLayer("Domain")
                .mayOnlyBeAccessedByLayers("Application", "Adapters", "Ui", "Composition")
                .check(PRODUCTION);
    }
}
