package io.github.hideyukimori.neneclock.gradle.conformance;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * QLT-007 の negative proof。
 *
 * <p>各規則について「わざと違反させた入力が、意図した規則 ID で落ちること」と
 * 「正しい入力では落ちないこと」の両方を確かめる。
 */
class JavaSourceRulesTest {

    private static List<String> ruleIds(String path, String content) {
        return JavaSourceRules.check(SourceFile.of(path, content)).violations().stream()
                .map(Violation::ruleId)
                .distinct()
                .sorted()
                .toList();
    }

    @Nested
    @DisplayName("CNF-001 禁止された総称名")
    class ForbiddenNames {

        @Test
        void rejectsAForbiddenTypeSuffix() {
            String source =
                    """
                    package io.github.hideyukimori.neneclock.domain;

                    final class ClockManager {}
                    """;

            assertTrue(ruleIds("core/domain/src/main/java/ClockManager.java", source).contains("CNF-001"));
        }

        @Test
        void rejectsAForbiddenPackageSegment() {
            String source =
                    """
                    package io.github.hideyukimori.neneclock.utils;

                    final class Sample {}
                    """;

            assertTrue(ruleIds("core/domain/src/main/java/Sample.java", source).contains("CNF-001"));
        }

        @Test
        void acceptsARoleName() {
            String source =
                    """
                    package io.github.hideyukimori.neneclock.application;

                    final class ClockFaceQuery {}
                    """;

            assertEquals(List.of(), ruleIds("core/application/src/main/java/ClockFaceQuery.java", source));
        }
    }

    @Nested
    @DisplayName("CNF-002 抑制には waiver ID が要る")
    class Suppressions {

        @Test
        void rejectsASuppressionWithoutAWaiver() {
            String source =
                    """
                    package io.github.hideyukimori.neneclock.domain;

                    final class Sample {
                        @SuppressWarnings("unchecked")
                        void run() {}
                    }
                    """;

            assertTrue(ruleIds("core/domain/src/main/java/Sample.java", source).contains("CNF-002"));
        }

        @Test
        void rejectsBlanketSuppressionEvenWithAWaiver() {
            String source =
                    """
                    package io.github.hideyukimori.neneclock.domain;

                    final class Sample {
                        // Waiver: WVR-0001
                        @SuppressWarnings("all")
                        void run() {}
                    }
                    """;

            assertTrue(ruleIds("core/domain/src/main/java/Sample.java", source).contains("CNF-002"));
        }

        @Test
        void acceptsASuppressionCarryingAWaiverAndReportsIt() {
            String source =
                    """
                    package io.github.hideyukimori.neneclock.domain;

                    final class Sample {
                        // Waiver: WVR-0007
                        @SuppressWarnings("unchecked")
                        void run() {}
                    }
                    """;

            JavaSourceRules.Result result = JavaSourceRules.check(
                    SourceFile.of("core/domain/src/main/java/Sample.java", source));

            assertEquals(List.of(), result.violations());
            assertEquals(List.of("WVR-0007"), result.referencedWaivers());
        }
    }

    @Nested
    @DisplayName("CNF-003 網羅性を殺す default の禁止")
    class SwitchDefault {

        @Test
        void rejectsAnArrowDefault() {
            String source =
                    """
                    package io.github.hideyukimori.neneclock.domain;

                    final class Sample {
                        int run(ClockFormat format) {
                            return switch (format) {
                                case HOUR_24 -> 1;
                                default -> 0;
                            };
                        }
                    }
                    """;

            assertTrue(ruleIds("core/domain/src/main/java/Sample.java", source).contains("CNF-003"));
        }

        @Test
        void ignoresTheWordDefaultInsideAStringLiteral() {
            String source =
                    """
                    package io.github.hideyukimori.neneclock.domain;

                    final class Sample {
                        String label() {
                            return "default -> fallback";
                        }
                    }
                    """;

            assertEquals(List.of(), ruleIds("core/domain/src/main/java/Sample.java", source));
        }
    }

    @Nested
    @DisplayName("CNF-013 画面に出す文言は UiText に集める")
    class DisplayText {

        @Test
        void rejectsJapaneseWrittenStraightIntoAPanel() {
            String source =
                    """
                    package io.github.hideyukimori.neneclock.ui.swing;

                    final class Sample {
                        void render() {
                            choice.renderSelection(0, List.of("日本語", "English"), theme);
                        }
                    }
                    """;

            assertTrue(ruleIds("ui/swing/src/main/java/Sample.java", source).contains("CNF-013"));
        }

        @Test
        void allowsTheOneFileThatOwnsTheWording() {
            String source =
                    """
                    package io.github.hideyukimori.neneclock.ui.swing;

                    enum UiText {
                        SETTINGS("設定", "Settings");
                    }
                    """;

            assertFalse(ruleIds("ui/swing/src/main/java/UiText.java", source).contains("CNF-013"));
        }

        @Test
        void allowsJapaneseInAnExceptionMeantForDevelopers() {
            String source =
                    """
                    package io.github.hideyukimori.neneclock.ui.swing;

                    final class Sample {
                        void load() {
                            throw new IllegalStateException("同梱書体を読めない");
                        }
                    }
                    """;

            assertFalse(ruleIds("ui/swing/src/main/java/Sample.java", source).contains("CNF-013"));
        }

        @Test
        void ignoresJapaneseInComments() {
            String source =
                    """
                    package io.github.hideyukimori.neneclock.ui.swing;

                    final class Sample {
                        // 日本語のコメントは文言ではない
                        void render() {}
                    }
                    """;

            assertFalse(ruleIds("ui/swing/src/main/java/Sample.java", source).contains("CNF-013"));
        }
    }

    @Nested
    @DisplayName("CNF-012 テキスト部品は TextRendering を通して作る")
    class TextComponents {

        @Test
        void rejectsALabelBuiltDirectlyInTheUi() {
            String source =
                    """
                    package io.github.hideyukimori.neneclock.ui.swing;

                    final class Sample {
                        private final JLabel caption = new JLabel("hello");
                    }
                    """;

            assertTrue(ruleIds("ui/swing/src/main/java/Sample.java", source).contains("CNF-012"));
        }

        @Test
        void rejectsATextFieldBuiltDirectlyInTheUi() {
            String source =
                    """
                    package io.github.hideyukimori.neneclock.ui.swing;

                    final class Sample {
                        private final JTextField hex = new JTextField(6);
                    }
                    """;

            assertTrue(ruleIds("ui/swing/src/main/java/Sample.java", source).contains("CNF-012"));
        }

        @Test
        void allowsTheOneFileThatOwnsTheHints() {
            String source =
                    """
                    package io.github.hideyukimori.neneclock.ui.swing;

                    final class TextRendering {
                        static JLabel label(String text) {
                            return new JLabel(text);
                        }
                    }
                    """;

            assertFalse(ruleIds("ui/swing/src/main/java/TextRendering.java", source)
                    .contains("CNF-012"));
        }

        @Test
        void ignoresTheSameConstructionOutsideTheUi() {
            String source =
                    """
                    package io.github.hideyukimori.neneclock.app;

                    final class Sample {
                        private final JLabel caption = new JLabel("hello");
                    }
                    """;

            assertFalse(ruleIds("app/src/main/java/Sample.java", source).contains("CNF-012"));
        }
    }

    @Nested
    @DisplayName("CNF-004 UI 状態の反映は render 経路だけ")
    class RenderOnlyCalls {

        @Test
        void rejectsSetEnabledInsideAListener() {
            String source =
                    """
                    package io.github.hideyukimori.neneclock.ui.swing;

                    final class Sample {
                        void wire() {
                            start.addActionListener(event -> pause.setEnabled(true));
                        }
                    }
                    """;

            assertTrue(ruleIds("ui/swing/src/main/java/Sample.java", source).contains("CNF-004"));
        }

        @Test
        void acceptsSetEnabledInsideARenderMethod() {
            String source =
                    """
                    package io.github.hideyukimori.neneclock.ui.swing;

                    final class Sample {
                        void renderState() {
                            pause.setEnabled(true);
                        }
                    }
                    """;

            assertEquals(List.of(), ruleIds("ui/swing/src/main/java/Sample.java", source));
        }

        @Test
        void doesNotApplyOutsideTheUiModule() {
            String source =
                    """
                    package io.github.hideyukimori.neneclock.application;

                    final class Sample {
                        void wire() {
                            widget.setEnabled(true);
                        }
                    }
                    """;

            assertEquals(List.of(), ruleIds("core/application/src/main/java/Sample.java", source));
        }
    }

    @Nested
    @DisplayName("CNF-006 / CNF-007 タスク印とファイル構成")
    class MarkersAndStructure {

        @Test
        void rejectsATodoWithoutAnIssueNumber() {
            String source =
                    """
                    package io.github.hideyukimori.neneclock.domain;

                    // TODO fix this later
                    final class Sample {}
                    """;

            assertTrue(ruleIds("core/domain/src/main/java/Sample.java", source).contains("CNF-006"));
        }

        @Test
        void acceptsATodoCarryingAnIssueNumber() {
            String source =
                    """
                    package io.github.hideyukimori.neneclock.domain;

                    // TODO (#42) fix this later
                    final class Sample {}
                    """;

            assertEquals(List.of(), ruleIds("core/domain/src/main/java/Sample.java", source));
        }

        @Test
        void rejectsTwoTopLevelTypesInOneFile() {
            String source =
                    """
                    package io.github.hideyukimori.neneclock.domain;

                    final class Sample {}

                    final class Second {}
                    """;

            assertTrue(ruleIds("core/domain/src/main/java/Sample.java", source).contains("CNF-007"));
        }

        @Test
        void rejectsAFileNameThatDoesNotMatchItsPrimaryType() {
            String source =
                    """
                    package io.github.hideyukimori.neneclock.domain;

                    final class Sample {}
                    """;

            assertTrue(ruleIds("core/domain/src/main/java/Other.java", source).contains("CNF-007"));
        }
    }
}
