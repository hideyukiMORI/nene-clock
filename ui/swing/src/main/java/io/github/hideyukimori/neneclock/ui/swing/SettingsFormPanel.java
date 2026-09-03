package io.github.hideyukimori.neneclock.ui.swing;

import io.github.hideyukimori.neneclock.application.SettingsIntentSink;
import io.github.hideyukimori.neneclock.domain.ClockFormat;
import io.github.hideyukimori.neneclock.domain.DateVisibility;
import io.github.hideyukimori.neneclock.domain.FontSize;
import io.github.hideyukimori.neneclock.domain.FontSizeOutcome;
import io.github.hideyukimori.neneclock.domain.SecondsVisibility;
import io.github.hideyukimori.neneclock.domain.UserSettings;
import io.github.hideyukimori.neneclock.domain.WindowTopmost;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * 設定モーダルの最初の画面。8 項目のうち、その場で決まる 5 つを持ち、
 * 選択肢の多い 3 つ（書体・文字色・背景色）は専用の画面へ渡す（FR-045）。
 *
 * <p>この部品は判断をしない。触った結果の {@link UserSettings} を丸ごと送るだけである（ARC-011）。
 */
public final class SettingsFormPanel {

    private static final int SIDE = 18;
    private static final float SECTION_POINTS = 10f;
    private static final float STATUS_POINTS = 11f;
    private static final int SAMPLE_POINTS = 15;
    private static final int DISPLAY_ROWS = 4;
    private static final int SECTION_TOP = 14;
    private static final int SECTION_BOTTOM = 6;
    private static final int PREVIEW_TOP = 14;

    private final JPanel surface = new JPanel();
    private final ClockPreview preview;
    private final SegmentedChoice format = new SegmentedChoice(List.of("12 時間", "24 時間"));
    private final ToggleSwitch seconds = new ToggleSwitch();
    private final ToggleSwitch date = new ToggleSwitch();
    private final ToggleSwitch topmost = new ToggleSwitch();
    private final DetailButton typeface = new DetailButton();
    private final SizeSlider size = new SizeSlider();
    private final JLabel sizeValue = new JLabel();
    private final DetailButton fontColour = new DetailButton();
    private final DetailButton background = new DetailButton();
    private final JLabel displaySection = new JLabel("表示");
    private final JLabel appearanceSection = new JLabel("見た目");
    private final List<SettingsRow> rows;
    private final TypefaceFontLoader typefaces;

    private SettingsIntentSink sink = requested -> {};
    private Consumer<SettingsDestination> navigate = destination -> {};
    private UserSettings shown = UserSettings.defaults();

    /** 部品を組み立てる。表示内容は {@code render*} が決める。 */
    public SettingsFormPanel(TypefaceFontLoader typefaces) {
        this.typefaces = Objects.requireNonNull(typefaces, "typefaces");
        this.preview = new ClockPreview(typefaces);
        this.rows = List.of(
                new SettingsRow("時刻表記", format.component(), true),
                new SettingsRow("秒を表示", seconds.component(), true),
                new SettingsRow("日付を表示", date.component(), true),
                new SettingsRow("常に最前面", topmost.component(), false),
                new SettingsRow("書体", typeface.component(), true),
                new SettingsRow("大きさ", sizePanel(), true),
                new SettingsRow("文字色", fontColour.component(), true),
                new SettingsRow("背景色", background.component(), false));
        layOut();
        listen();
    }

    /** 画面に載せるための Swing 部品。 */
    public JComponent component() {
        return surface;
    }

    /** 意図の宛先を 1 度だけ結ぶ。合成ルートが呼ぶ。 */
    public void onSettingsRequested(SettingsIntentSink requested) {
        this.sink = Objects.requireNonNull(requested, "requested");
    }

    /** 別の画面へ進みたいことの宛先を 1 度だけ結ぶ。 */
    public void onNavigationRequested(Consumer<SettingsDestination> requested) {
        this.navigate = Objects.requireNonNull(requested, "requested");
    }

    /** 現在の設定を反映する。 */
    public void renderSettings(UserSettings settings, ClockPreviewText text) {
        shown = Objects.requireNonNull(settings, "settings");
        Palette palette = Palette.from(settings.backgroundColor());
        preview.renderSettings(settings, text.time(), text.date());
        format.renderSelection(settings.clockFormat() == ClockFormat.HOUR_12 ? 0 : 1, palette);
        seconds.renderState(settings.secondsVisibility() == SecondsVisibility.SHOWN, palette);
        date.renderState(settings.dateVisibility() == DateVisibility.SHOWN, palette);
        topmost.renderState(settings.windowTopmost() == WindowTopmost.ENABLED, palette);
        size.renderPoints(settings.fontSize().points(), palette);
        sizeValue.setText(String.valueOf(settings.fontSize().points()));
        sizeValue.setForeground(palette.text());
        typeface.renderValue(
                new DetailValue(
                        settings.typeface().displayName(), null, typefaces.load(settings.typeface(), SAMPLE_POINTS)),
                palette);
        fontColour.renderValue(
                new DetailValue(hexOf(settings.fontColor()), awtColour(settings.fontColor()), null), palette);
        background.renderValue(
                new DetailValue(hexOf(settings.backgroundColor()), awtColour(settings.backgroundColor()), null),
                palette);
        renderColours(palette);
    }

    private void renderColours(Palette palette) {
        surface.setBackground(palette.surface());
        displaySection.setForeground(palette.textFaint());
        appearanceSection.setForeground(palette.textFaint());
        for (SettingsRow row : rows) {
            row.renderColours(palette);
        }
    }

    private JComponent sizePanel() {
        JPanel holder = new JPanel();
        holder.setOpaque(false);
        holder.setLayout(new BoxLayout(holder, BoxLayout.X_AXIS));
        sizeValue.setFont(sizeValue.getFont().deriveFont(Font.PLAIN, STATUS_POINTS));
        holder.add(size.component());
        holder.add(javax.swing.Box.createHorizontalStrut(SIDE / 2));
        holder.add(sizeValue);
        return holder;
    }

    private void layOut() {
        surface.setLayout(new BoxLayout(surface, BoxLayout.Y_AXIS));
        surface.setBorder(BorderFactory.createEmptyBorder(0, SIDE, 0, SIDE));
        surface.add(javax.swing.Box.createVerticalStrut(PREVIEW_TOP));
        surface.add(preview.component());
        surface.add(section(displaySection));
        for (int index = 0; index < DISPLAY_ROWS; index++) {
            surface.add(rows.get(index).component());
        }
        surface.add(section(appearanceSection));
        for (int index = DISPLAY_ROWS; index < rows.size(); index++) {
            surface.add(rows.get(index).component());
        }
        surface.add(javax.swing.Box.createVerticalGlue());
    }

    private JComponent section(JLabel label) {
        label.setFont(label.getFont().deriveFont(Font.BOLD, SECTION_POINTS));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(SECTION_TOP, 0, SECTION_BOTTOM, 0));
        JPanel holder = new JPanel();
        holder.setOpaque(false);
        holder.setLayout(new BoxLayout(holder, BoxLayout.X_AXIS));
        holder.add(label);
        holder.add(javax.swing.Box.createHorizontalGlue());
        return holder;
    }

    private void listen() {
        format.onChosen(index -> submit(with(index == 0 ? ClockFormat.HOUR_12 : ClockFormat.HOUR_24)));
        seconds.onToggled(() -> submit(with(
                shown.secondsVisibility() == SecondsVisibility.SHOWN
                        ? SecondsVisibility.HIDDEN
                        : SecondsVisibility.SHOWN)));
        date.onToggled(() -> submit(
                with(shown.dateVisibility() == DateVisibility.SHOWN ? DateVisibility.HIDDEN : DateVisibility.SHOWN)));
        topmost.onToggled(() -> submit(
                with(shown.windowTopmost() == WindowTopmost.ENABLED ? WindowTopmost.DISABLED : WindowTopmost.ENABLED)));
        size.onMoved(points -> submit(withSize(points)));
        typeface.onPressed(() -> navigate.accept(SettingsDestination.TYPEFACE));
        fontColour.onPressed(() -> navigate.accept(SettingsDestination.FONT_COLOUR));
        background.onPressed(() -> navigate.accept(SettingsDestination.BACKGROUND_COLOUR));
    }

    private void submit(UserSettings requested) {
        if (!requested.equals(shown)) {
            sink.submit(requested);
        }
    }

    private UserSettings with(ClockFormat clockFormat) {
        return new UserSettings(
                clockFormat,
                shown.secondsVisibility(),
                shown.dateVisibility(),
                shown.windowTopmost(),
                shown.typeface(),
                shown.fontSize(),
                shown.fontColor(),
                shown.backgroundColor());
    }

    private UserSettings with(SecondsVisibility visibility) {
        return new UserSettings(
                shown.clockFormat(),
                visibility,
                shown.dateVisibility(),
                shown.windowTopmost(),
                shown.typeface(),
                shown.fontSize(),
                shown.fontColor(),
                shown.backgroundColor());
    }

    private UserSettings with(DateVisibility visibility) {
        return new UserSettings(
                shown.clockFormat(),
                shown.secondsVisibility(),
                visibility,
                shown.windowTopmost(),
                shown.typeface(),
                shown.fontSize(),
                shown.fontColor(),
                shown.backgroundColor());
    }

    private UserSettings with(WindowTopmost value) {
        return new UserSettings(
                shown.clockFormat(),
                shown.secondsVisibility(),
                shown.dateVisibility(),
                value,
                shown.typeface(),
                shown.fontSize(),
                shown.fontColor(),
                shown.backgroundColor());
    }

    private UserSettings withSize(int points) {
        return switch (FontSize.of(points)) {
            case FontSizeOutcome.Accepted accepted ->
                new UserSettings(
                        shown.clockFormat(),
                        shown.secondsVisibility(),
                        shown.dateVisibility(),
                        shown.windowTopmost(),
                        shown.typeface(),
                        accepted.value(),
                        shown.fontColor(),
                        shown.backgroundColor());
            // 帯は FontSize の範囲そのものなので、ここへは来ない。来たら動かさない。
            case FontSizeOutcome.Rejected outOfRange -> shown;
        };
    }

    private static Color awtColour(io.github.hideyukimori.neneclock.domain.RgbColor colour) {
        return new Color(colour.red(), colour.green(), colour.blue());
    }

    static String hexOf(io.github.hideyukimori.neneclock.domain.RgbColor colour) {
        return String.format(Locale.ROOT, "#%02X%02X%02X", colour.red(), colour.green(), colour.blue());
    }
}
