package io.github.hideyukimori.neneclock.adapter.preferences;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.hideyukimori.neneclock.application.SettingsLoadFailure;
import io.github.hideyukimori.neneclock.application.SettingsLoadOutcome;
import io.github.hideyukimori.neneclock.application.SettingsSaveOutcome;
import io.github.hideyukimori.neneclock.domain.ClockFormat;
import io.github.hideyukimori.neneclock.domain.DateVisibility;
import io.github.hideyukimori.neneclock.domain.FontSize;
import io.github.hideyukimori.neneclock.domain.FontSizeOutcome;
import io.github.hideyukimori.neneclock.domain.Language;
import io.github.hideyukimori.neneclock.domain.RgbaColor;
import io.github.hideyukimori.neneclock.domain.RgbaColorOutcome;
import io.github.hideyukimori.neneclock.domain.SecondsVisibility;
import io.github.hideyukimori.neneclock.domain.Typeface;
import io.github.hideyukimori.neneclock.domain.UserSettings;
import io.github.hideyukimori.neneclock.domain.WindowTopmost;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PreferencesSettingsAdapterTest {

    private static final String TEST_NODE = "io/github/hideyukimori/neneclock/test";

    private Preferences node = Preferences.userRoot().node(TEST_NODE);

    @BeforeEach
    void createNode() throws BackingStoreException {
        node = Preferences.userRoot().node(TEST_NODE);
        node.clear();
    }

    @AfterEach
    void removeNode() throws BackingStoreException {
        node.removeNode();
    }

    @Test
    void reportsAbsentSettingsOnFirstRun() {
        SettingsLoadOutcome outcome = PreferencesSettingsAdapter.at(node).load();

        assertThat(outcome).isEqualTo(new SettingsLoadOutcome.Defaulted(SettingsLoadFailure.ABSENT));
        assertThat(outcome.settingsOrDefaults()).isEqualTo(UserSettings.defaults());
    }

    @Test
    void roundTripsSavedSettings() {
        PreferencesSettingsAdapter adapter = PreferencesSettingsAdapter.at(node);
        UserSettings stored = new UserSettings(
                ClockFormat.HOUR_12,
                SecondsVisibility.HIDDEN,
                DateVisibility.HIDDEN,
                WindowTopmost.ENABLED,
                Typeface.ORBITRON,
                FontSize.DEFAULT,
                color(10, 20, 30),
                color(40, 50, 60),
                Language.DEFAULT);

        SettingsSaveOutcome saveOutcome = adapter.save(stored);

        assertThat(saveOutcome).isEqualTo(new SettingsSaveOutcome.Saved());
        assertThat(adapter.load()).isEqualTo(new SettingsLoadOutcome.Restored(stored));
    }

    @Test
    void refusesAFutureSchemaInsteadOfGuessing() {
        node.putInt("schemaVersion", 99);

        SettingsLoadOutcome outcome = PreferencesSettingsAdapter.at(node).load();

        assertThat(outcome).isEqualTo(new SettingsLoadOutcome.Defaulted(SettingsLoadFailure.UNSUPPORTED_SCHEMA));
    }

    @Test
    void migratesVersionOneWithoutLosingTheSettingsItHad() {
        writeVersionOne(versionOneSettings());

        SettingsLoadOutcome outcome = PreferencesSettingsAdapter.at(node).load();

        assertThat(outcome).isEqualTo(new SettingsLoadOutcome.Restored(versionOneSettings()));
    }

    @Test
    void refusesABrokenValueEvenWhenMigratingFromVersionOne() {
        writeVersionOne(versionOneSettings());
        node.put("clockFormat", "HOUR_13");

        SettingsLoadOutcome outcome = PreferencesSettingsAdapter.at(node).load();

        assertThat(outcome).isEqualTo(new SettingsLoadOutcome.Defaulted(SettingsLoadFailure.INVALID_VALUE));
    }

    @Test
    void doesNotRewriteTheStoreWhileReading() {
        writeVersionOne(versionOneSettings());

        PreferencesSettingsAdapter.at(node).load();

        assertThat(node.getInt("schemaVersion", 0)).isEqualTo(1);
    }

    /** v1 で保存されていた設定を現在の型で表したもの。書体と文字色は既定値になる。 */
    private static UserSettings versionOneSettings() {
        return new UserSettings(
                ClockFormat.HOUR_12,
                SecondsVisibility.HIDDEN,
                DateVisibility.HIDDEN,
                WindowTopmost.ENABLED,
                Typeface.DEFAULT,
                size(96),
                RgbaColor.DEFAULT_FONT,
                RgbaColor.DEFAULT_BACKGROUND,
                Language.DEFAULT);
    }

    @Test
    void refusesAnUnknownTypeface() {
        PreferencesSettingsAdapter.at(node).save(UserSettings.defaults());
        node.put("typeface", "COMIC_SANS");

        SettingsLoadOutcome outcome = PreferencesSettingsAdapter.at(node).load();

        assertThat(outcome).isEqualTo(new SettingsLoadOutcome.Defaulted(SettingsLoadFailure.INVALID_VALUE));
    }

    @Test
    void migratesVersionTwoAndKeepsEverythingButTheEnvironmentFont() {
        writeVersionTwo("DejaVu Sans");

        SettingsLoadOutcome outcome = PreferencesSettingsAdapter.at(node).load();

        // 実行環境の書体名は同梱書体の集合に無い。設定を捨てずに、書体だけ既定へ落とす。
        assertThat(outcome)
                .isEqualTo(new SettingsLoadOutcome.Restored(new UserSettings(
                        ClockFormat.HOUR_12,
                        SecondsVisibility.HIDDEN,
                        DateVisibility.HIDDEN,
                        WindowTopmost.ENABLED,
                        Typeface.DEFAULT,
                        size(96),
                        color(10, 20, 30),
                        RgbaColor.DEFAULT_BACKGROUND,
                        Language.DEFAULT)));
    }

    @Test
    void keepsAVersionTwoChoiceWhenTheSameTypefaceIsBundled() {
        writeVersionTwo(Typeface.ORBITRON.displayName());

        SettingsLoadOutcome outcome = PreferencesSettingsAdapter.at(node).load();

        assertThat(((SettingsLoadOutcome.Restored) outcome).settings().typeface())
                .isEqualTo(Typeface.ORBITRON);
    }

    @Test
    void migratesVersionThreeByFillingInTheBackgroundColour() {
        writeVersionThree();

        SettingsLoadOutcome outcome = PreferencesSettingsAdapter.at(node).load();

        assertThat(((SettingsLoadOutcome.Restored) outcome).settings())
                .isEqualTo(new UserSettings(
                        ClockFormat.HOUR_12,
                        SecondsVisibility.HIDDEN,
                        DateVisibility.HIDDEN,
                        WindowTopmost.ENABLED,
                        Typeface.ORBITRON,
                        size(96),
                        color(10, 20, 30),
                        RgbaColor.DEFAULT_BACKGROUND,
                        Language.DEFAULT));
    }

    @Test
    void migratesVersionFourByFillingInTheLanguage() {
        writeVersionThree();
        node.putInt("schemaVersion", 4);
        node.putInt("backgroundRed", 1);
        node.putInt("backgroundGreen", 2);
        node.putInt("backgroundBlue", 3);

        SettingsLoadOutcome outcome = PreferencesSettingsAdapter.at(node).load();

        UserSettings restored = ((SettingsLoadOutcome.Restored) outcome).settings();
        assertThat(restored.backgroundColor()).isEqualTo(color(1, 2, 3));
        assertThat(restored.language()).isEqualTo(Language.DEFAULT);
        assertThat(restored.typeface()).isEqualTo(Typeface.ORBITRON);
    }

    @Test
    void migratesVersionFiveByTreatingTheColoursAsOpaque() {
        writeVersionThree();
        node.putInt("schemaVersion", 5);
        node.putInt("backgroundRed", 1);
        node.putInt("backgroundGreen", 2);
        node.putInt("backgroundBlue", 3);
        node.put("language", Language.ENGLISH.name());

        SettingsLoadOutcome outcome = PreferencesSettingsAdapter.at(node).load();

        UserSettings restored = ((SettingsLoadOutcome.Restored) outcome).settings();
        assertThat(restored.fontColor().alpha()).isEqualTo(RgbaColor.OPAQUE);
        assertThat(restored.backgroundColor().alpha()).isEqualTo(RgbaColor.OPAQUE);
        assertThat(restored.language()).isEqualTo(Language.ENGLISH);
    }

    @Test
    void roundTripsTheAlphaOfBothColours() {
        PreferencesSettingsAdapter adapter = PreferencesSettingsAdapter.at(node);
        UserSettings stored = UserSettings.defaults()
                .withFontColor(RgbaColor.DEFAULT_FONT.withAlpha(200))
                .withBackgroundColor(RgbaColor.DEFAULT_BACKGROUND.withAlpha(120));

        adapter.save(stored);

        UserSettings restored = ((SettingsLoadOutcome.Restored) adapter.load()).settings();
        assertThat(restored.fontColor().alpha()).isEqualTo(200);
        assertThat(restored.backgroundColor().alpha()).isEqualTo(120);
    }

    @Test
    void refusesAnAlphaOutsideTheAllowedRange() {
        PreferencesSettingsAdapter.at(node).save(UserSettings.defaults());
        node.putInt("fontAlpha", RgbaColor.MAXIMUM_COMPONENT + 1);

        SettingsLoadOutcome outcome = PreferencesSettingsAdapter.at(node).load();

        assertThat(outcome).isEqualTo(new SettingsLoadOutcome.Defaulted(SettingsLoadFailure.INVALID_VALUE));
    }

    @Test
    void refusesAnUnknownLanguage() {
        PreferencesSettingsAdapter.at(node).save(UserSettings.defaults());
        node.put("language", "KLINGON");

        SettingsLoadOutcome outcome = PreferencesSettingsAdapter.at(node).load();

        assertThat(outcome).isEqualTo(new SettingsLoadOutcome.Defaulted(SettingsLoadFailure.INVALID_VALUE));
    }

    @Test
    void refusesABackgroundComponentOutsideTheAllowedRange() {
        PreferencesSettingsAdapter.at(node).save(UserSettings.defaults());
        node.putInt("backgroundBlue", RgbaColor.MAXIMUM_COMPONENT + 1);

        SettingsLoadOutcome outcome = PreferencesSettingsAdapter.at(node).load();

        assertThat(outcome).isEqualTo(new SettingsLoadOutcome.Defaulted(SettingsLoadFailure.INVALID_VALUE));
    }

    @Test
    void keepsTheFontAndBackgroundColoursApart() {
        // 同じ型の成分が 2 つ並ぶので、取り違えると気づきにくい（ADR 0007）。
        PreferencesSettingsAdapter adapter = PreferencesSettingsAdapter.at(node);
        UserSettings stored = new UserSettings(
                ClockFormat.HOUR_24,
                SecondsVisibility.SHOWN,
                DateVisibility.SHOWN,
                WindowTopmost.DISABLED,
                Typeface.DEFAULT,
                FontSize.DEFAULT,
                color(1, 2, 3),
                color(250, 251, 252),
                Language.DEFAULT);

        adapter.save(stored);
        UserSettings restored = ((SettingsLoadOutcome.Restored) adapter.load()).settings();

        assertThat(restored.fontColor()).isEqualTo(color(1, 2, 3));
        assertThat(restored.backgroundColor()).isEqualTo(color(250, 251, 252));
    }

    @Test
    void refusesAColourComponentOutsideTheAllowedRange() {
        PreferencesSettingsAdapter.at(node).save(UserSettings.defaults());
        node.putInt("fontGreen", RgbaColor.MAXIMUM_COMPONENT + 1);

        SettingsLoadOutcome outcome = PreferencesSettingsAdapter.at(node).load();

        assertThat(outcome).isEqualTo(new SettingsLoadOutcome.Defaulted(SettingsLoadFailure.INVALID_VALUE));
    }

    @Test
    void refusesAnUnknownEnumValue() {
        PreferencesSettingsAdapter.at(node).save(UserSettings.defaults());
        node.put("clockFormat", "HOUR_13");

        SettingsLoadOutcome outcome = PreferencesSettingsAdapter.at(node).load();

        assertThat(outcome).isEqualTo(new SettingsLoadOutcome.Defaulted(SettingsLoadFailure.INVALID_VALUE));
    }

    @Test
    void refusesAFontSizeOutsideTheAllowedRange() {
        PreferencesSettingsAdapter.at(node).save(UserSettings.defaults());
        node.putInt("fontPoints", FontSize.MAXIMUM_POINTS + 1);

        SettingsLoadOutcome outcome = PreferencesSettingsAdapter.at(node).load();

        assertThat(outcome).isEqualTo(new SettingsLoadOutcome.Defaulted(SettingsLoadFailure.INVALID_VALUE));
    }

    /**
     * v1 の保存値を書く。v1 は書体と文字色のキーを持たないので、渡された設定のうち
     * 当時からある 5 項目だけを書く。
     */
    private void writeVersionOne(UserSettings settings) {
        node.putInt("schemaVersion", 1);
        node.put("clockFormat", settings.clockFormat().name());
        node.put("secondsVisibility", settings.secondsVisibility().name());
        node.put("dateVisibility", settings.dateVisibility().name());
        node.put("windowTopmost", settings.windowTopmost().name());
        node.putInt("fontPoints", settings.fontSize().points());
    }

    /** v3 の保存値を書く。v3 は同梱書体の識別子を持つが、背景色を持たない。 */
    private void writeVersionThree() {
        writeVersionTwo("ignored");
        node.putInt("schemaVersion", 3);
        node.put("typeface", Typeface.ORBITRON.name());
    }

    /** v2 の保存値を書く。v2 は「実行環境の書体名」を持ち、同梱書体の識別子は持たない。 */
    private void writeVersionTwo(String environmentFontName) {
        node.putInt("schemaVersion", 2);
        node.put("clockFormat", ClockFormat.HOUR_12.name());
        node.put("secondsVisibility", SecondsVisibility.HIDDEN.name());
        node.put("dateVisibility", DateVisibility.HIDDEN.name());
        node.put("windowTopmost", WindowTopmost.ENABLED.name());
        node.putInt("fontPoints", 96);
        node.put("fontFamily", environmentFontName);
        node.putInt("fontRed", 10);
        node.putInt("fontGreen", 20);
        node.putInt("fontBlue", 30);
    }

    private static RgbaColor color(int red, int green, int blue) {
        return ((RgbaColorOutcome.Accepted) RgbaColor.opaque(red, green, blue)).value();
    }

    private static FontSize size(int points) {
        return ((FontSizeOutcome.Accepted) FontSize.of(points)).value();
    }
}
