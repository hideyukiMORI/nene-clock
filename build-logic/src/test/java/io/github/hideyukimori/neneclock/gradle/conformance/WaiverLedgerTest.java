package io.github.hideyukimori.neneclock.gradle.conformance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

/** CNF-009 の negative proof（QLT-007）。 */
class WaiverLedgerTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 9, 3);

    private static SourceFile waiver(String expires) {
        return SourceFile.of(
                "docs/waivers/WVR-0007-sample-exception.md",
                """
                # WVR-0007 — 例

                - Rule: JAV-015
                - Scope: core/domain/src/main/java/Sample.java#Sample
                - Issue: #7
                - Expires: %s
                """
                        .formatted(expires));
    }

    private static SourceFile index() {
        return SourceFile.of("docs/waivers/README.md", "| WVR-0007 | JAV-015 | Sample | 直す | 2026-12-31 |\n");
    }

    @Test
    void acceptsAWellFormedActiveWaiver() {
        List<Violation> violations =
                WaiverLedger.check(List.of(waiver("2026-12-31")), index(), List.of("WVR-0007"), TODAY);

        assertEquals(List.of(), violations);
    }

    @Test
    void rejectsAnExpiredWaiver() {
        List<Violation> violations =
                WaiverLedger.check(List.of(waiver("2026-09-02")), index(), List.of("WVR-0007"), TODAY);

        assertEquals(1, violations.size());
        assertEquals("CNF-009", violations.get(0).ruleId());
        assertTrue(violations.get(0).message().contains("期限切れ"));
    }

    @Test
    void rejectsAWaiverReferencedFromSourceButMissingFromTheLedger() {
        List<Violation> violations = WaiverLedger.check(List.of(), null, List.of("WVR-0042"), TODAY);

        assertEquals(1, violations.size());
        assertTrue(violations.get(0).message().contains("WVR-0042"));
    }

    @Test
    void rejectsAWaiverThatIsMissingARequiredField() {
        SourceFile incomplete = SourceFile.of(
                "docs/waivers/WVR-0007-sample-exception.md",
                """
                # WVR-0007 — 例

                - Rule: JAV-015
                - Expires: 2026-12-31
                """);

        List<Violation> violations = WaiverLedger.check(List.of(incomplete), index(), List.of(), TODAY);

        assertTrue(violations.stream().anyMatch(violation -> violation.message().contains("- Scope:")));
    }

    @Test
    void rejectsAWaiverMissingFromTheIndex() {
        SourceFile emptyIndex = SourceFile.of("docs/waivers/README.md", "有効な waiver は無い\n");

        List<Violation> violations =
                WaiverLedger.check(List.of(waiver("2026-12-31")), emptyIndex, List.of(), TODAY);

        assertTrue(violations.stream().anyMatch(violation -> violation.message().contains("索引")));
    }
}
