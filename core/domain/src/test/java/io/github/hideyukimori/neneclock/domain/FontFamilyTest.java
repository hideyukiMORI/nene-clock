package io.github.hideyukimori.neneclock.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FontFamilyTest {

    @Test
    void acceptsAnOrdinaryFamilyName() {
        FontFamilyOutcome outcome = FontFamily.of("Noto Sans Mono");

        assertThat(outcome).isInstanceOf(FontFamilyOutcome.Accepted.class);
        assertThat(((FontFamilyOutcome.Accepted) outcome).value().name()).isEqualTo("Noto Sans Mono");
    }

    @Test
    void rejectsABlankName() {
        assertThat(FontFamily.of("   ")).isEqualTo(new FontFamilyOutcome.Rejected(FontFamilyRejection.BLANK));
    }

    @Test
    void rejectsAnOverlongName() {
        String tooLong = "a".repeat(FontFamily.MAXIMUM_LENGTH + 1);

        assertThat(FontFamily.of(tooLong)).isEqualTo(new FontFamilyOutcome.Rejected(FontFamilyRejection.TOO_LONG));
    }

    @Test
    void acceptsANameExactlyAtTheLengthLimit() {
        String atLimit = "a".repeat(FontFamily.MAXIMUM_LENGTH);

        assertThat(FontFamily.of(atLimit)).isInstanceOf(FontFamilyOutcome.Accepted.class);
    }

    @Test
    void rejectsAControlCharacter() {
        String withBell = "Serif" + (char) 7;

        assertThat(FontFamily.of(withBell))
                .isEqualTo(new FontFamilyOutcome.Rejected(FontFamilyRejection.UNSUPPORTED_CHARACTER));
    }

    @Test
    void theDefaultIsAcceptedByItsOwnFactory() {
        // FontFamily.DEFAULT は非公開コンストラクタで直接組み立てられている。
        // 既定値が自分の検証を通ることを保証しているのはこのテストである（JAV-007 の補完）。
        assertThat(FontFamily.of(FontFamily.DEFAULT.name())).isInstanceOf(FontFamilyOutcome.Accepted.class);
    }

    @Test
    void comparesByValue() {
        FontFamilyOutcome first = FontFamily.of("Serif");
        FontFamilyOutcome second = FontFamily.of("Serif");

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
        assertThat(first).isNotEqualTo(FontFamily.of("SansSerif"));
        assertThat(FontFamily.DEFAULT).isNotEqualTo(FontFamily.DEFAULT.name());
        assertThat(FontFamily.DEFAULT.toString()).contains(FontFamily.DEFAULT.name());
    }
}
