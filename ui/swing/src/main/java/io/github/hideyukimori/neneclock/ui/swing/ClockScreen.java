package io.github.hideyukimori.neneclock.ui.swing;

import io.github.hideyukimori.neneclock.application.ClockFace;
import io.github.hideyukimori.neneclock.application.DateLine;
import io.github.hideyukimori.neneclock.application.SettingsIntentSink;
import io.github.hideyukimori.neneclock.application.SettingsSaveOutcome;
import io.github.hideyukimori.neneclock.domain.Language;
import io.github.hideyukimori.neneclock.domain.RgbColor;
import io.github.hideyukimori.neneclock.domain.Typeface;
import io.github.hideyukimori.neneclock.domain.UserSettings;
import java.awt.Font;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * 画面ぜんぶ。窓・クローム・設定モーダルを 1 つの面として束ねる（ARC-011）。
 *
 * <p>合成ルートはポートを結ぶだけで済むように、部品どうしの結線はここに閉じる。
 * この型は判断をしない。「こうしたい」を外へ渡し、渡された状態を描くだけである。
 */
public final class ClockScreen {

    /** UI 書体を組み立てるときの大きさ。実際の大きさは {@link UiTheme#font(float)} が derive する。 */
    private static final int INTERFACE_POINTS = 13;

    private final ClockPanel clockPanel;
    private final WindowChrome chrome;
    private final ClockWindow window;
    private final SettingsFormPanel form;
    private final TypefacePickerPanel typefacePicker;
    private final ColourPickerPanel colourPicker;
    private final SettingsDialog dialog;
    private final Map<Language, Font> interfaceFonts = new EnumMap<>(Language.class);

    private SettingsIntentSink sink = requested -> {};
    private Runnable quit = () -> {};
    private UserSettings shown = UserSettings.defaults();

    /** 同梱書体の読み手だけを受け取り、画面を組み立てる。 */
    public ClockScreen(TypefaceFontLoader typefaces) {
        Objects.requireNonNull(typefaces, "typefaces");
        // 🔑 UI 書体はここで 1 度だけ組み立てる。Font.createFont は重く、deriveFont は軽い。
        for (Language language : Language.values()) {
            interfaceFonts.put(language, typefaces.load(language.typeface(), INTERFACE_POINTS));
        }
        this.clockPanel = new ClockPanel(typefaces);
        this.chrome = new WindowChrome();
        this.window = new ClockWindow(clockPanel, chrome);
        this.form = new SettingsFormPanel(typefaces);
        this.typefacePicker = new TypefacePickerPanel(typefaces);
        this.colourPicker = new ColourPickerPanel(typefaces);
        this.dialog = new SettingsDialog(window.owner(), new SettingsPanels(form, typefacePicker, colourPicker));
        connect();
    }

    /** 設定を変えたいという意図の宛先を 1 度だけ結ぶ。 */
    public void onSettingsRequested(SettingsIntentSink requested) {
        this.sink = Objects.requireNonNull(requested, "requested");
    }

    /** 終わりたいという意図の宛先を 1 度だけ結ぶ。窓を閉じるのも止めるのも合成ルートの仕事。 */
    public void onQuitRequested(Runnable requested) {
        this.quit = Objects.requireNonNull(requested, "requested");
    }

    /** 設定を画面全体へ反映する。反映経路はここ 1 本（CNF-004）。 */
    public void renderSettings(UserSettings settings, ClockFace face) {
        shown = Objects.requireNonNull(settings, "settings");
        UiTheme theme = themeOf(settings);
        ClockPreviewText text = textOf(face);
        // 🔴 先に文字を入れる。空のまま大きさを決めると、窓が最小の大きさで固まる（実機で踏んだ）。
        clockPanel.renderFace(face);
        window.renderSettings(settings);
        dialog.renderTheme(theme);
        form.renderTheme(theme);
        form.renderSettings(settings, text);
        typefacePicker.renderSelection(settings.typeface(), theme);
        colourPicker.renderColour(new ColourEditing(settings, colourRole()), text, theme);
    }

    private UiTheme themeOf(UserSettings settings) {
        return UiTheme.of(settings, Objects.requireNonNull(interfaceFonts.get(settings.language())));
    }

    /** 時刻の表示だけを更新する。1 秒ごとに呼ばれる経路（FR-002）。 */
    public void renderFace(ClockFace face) {
        clockPanel.renderFace(face);
        ClockPreviewText text = textOf(face);
        if (dialog.isShowing()) {
            form.renderSettings(shown, text);
            colourPicker.renderColour(new ColourEditing(shown, colourRole()), text, themeOf(shown));
        }
    }

    /** 保存の結果を伝える。 */
    public void renderSaveOutcome(SettingsSaveOutcome outcome) {
        dialog.renderSaveOutcome(outcome);
    }

    /** 窓を表示する。EDT から呼ぶこと（SWG-001）。 */
    public void display() {
        window.display();
    }

    /** 画面を畳む。 */
    public void close() {
        dialog.hide();
        window.close();
    }

    private SettingsDestination colourRole() {
        return dialog.showingNow() == SettingsDestination.BACKGROUND_COLOUR
                ? SettingsDestination.BACKGROUND_COLOUR
                : SettingsDestination.FONT_COLOUR;
    }

    private static ClockPreviewText textOf(ClockFace face) {
        return new ClockPreviewText(
                face.time(),
                switch (face.date()) {
                    case DateLine.Shown shown -> shown.text();
                    case DateLine.Hidden hidden -> " ";
                });
    }

    private void connect() {
        chrome.onChromeTriggered(this::actOn);
        form.onSettingsRequested(requested -> sink.submit(requested));
        form.onNavigationRequested(dialog::show);
        typefacePicker.onTypefaceChosen(this::chooseTypeface);
        colourPicker.onColourChosen(this::chooseColour);
    }

    private void actOn(ChromeIcon icon) {
        switch (icon) {
            case SETTINGS -> dialog.display();
            case CLOSE -> quit.run();
            // 戻るはモーダルの中の操作で、クロームからは飛んでこない。
            case BACK -> {
                // 何もしない。
            }
        }
    }

    private void chooseTypeface(Typeface typeface) {
        sink.submit(shown.withTypeface(typeface));
    }

    private void chooseColour(RgbColor colour) {
        boolean background = dialog.showingNow() == SettingsDestination.BACKGROUND_COLOUR;
        sink.submit(background ? shown.withBackgroundColor(colour) : shown.withFontColor(colour));
    }
}
