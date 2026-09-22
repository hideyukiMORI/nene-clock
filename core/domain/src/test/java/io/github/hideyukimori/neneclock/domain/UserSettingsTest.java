package io.github.hideyukimori.neneclock.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UserSettingsTest {

    @Test
    void defaultsMatchTheSpecification() {
        UserSettings settings = UserSettings.defaults();

        assertThat(settings.clockFormat()).isEqualTo(ClockFormat.HOUR_24);
        assertThat(settings.secondsVisibility()).isEqualTo(SecondsVisibility.SHOWN);
        assertThat(settings.dateVisibility()).isEqualTo(DateVisibility.SHOWN);
        assertThat(settings.windowTopmost()).isEqualTo(WindowTopmost.DISABLED);
        assertThat(settings.typeface()).isEqualTo(Typeface.DEFAULT);
        assertThat(settings.fontSize()).isEqualTo(FontSize.DEFAULT);
        assertThat(settings.fontColor()).isEqualTo(RgbColor.DEFAULT_FONT);
        assertThat(settings.backgroundColor()).isEqualTo(RgbColor.DEFAULT_BACKGROUND);
        assertThat(settings.language()).isEqualTo(Language.DEFAULT);
        assertThat(settings.windowPadding()).isEqualTo(WindowPadding.DEFAULT);
    }

    @Test
    void replacingThePaddingKeepsEverythingElse() {
        WindowPadding wide = ((WindowPaddingOutcome.Accepted) WindowPadding.of(WindowPadding.MAXIMUM_PIXELS)).value();

        UserSettings settings = UserSettings.defaults().withWindowPadding(wide);

        assertThat(settings.windowPadding()).isEqualTo(wide);
        assertThat(settings).isEqualTo(UserSettings.defaults().withWindowPadding(wide));
        assertThat(settings.withWindowPadding(WindowPadding.DEFAULT)).isEqualTo(UserSettings.defaults());
    }

    // 「成分が null の UserSettings を作れない」ことは NullAway が
    // コンパイル時に落とすため、テストとして書くことができない（JAV-004）。
    // 実行時の requireNonNull は、注釈の外から呼ばれた場合の防御として残している。

    @Test
    void defaultsAreStable() {
        assertThat(UserSettings.defaults()).isEqualTo(UserSettings.defaults());
    }
}
