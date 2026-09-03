package io.github.hideyukimori.neneclock.ui.swing;

import io.github.hideyukimori.neneclock.application.SettingsIntentSink;
import io.github.hideyukimori.neneclock.application.SettingsSaveFailure;
import io.github.hideyukimori.neneclock.application.SettingsSaveOutcome;
import io.github.hideyukimori.neneclock.domain.ClockFormat;
import io.github.hideyukimori.neneclock.domain.DateVisibility;
import io.github.hideyukimori.neneclock.domain.FontSize;
import io.github.hideyukimori.neneclock.domain.FontSizeOutcome;
import io.github.hideyukimori.neneclock.domain.RgbColor;
import io.github.hideyukimori.neneclock.domain.RgbColorOutcome;
import io.github.hideyukimori.neneclock.domain.SecondsVisibility;
import io.github.hideyukimori.neneclock.domain.Typeface;
import io.github.hideyukimori.neneclock.domain.UserSettings;
import io.github.hideyukimori.neneclock.domain.WindowTopmost;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Locale;
import java.util.Objects;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import org.jspecify.annotations.Nullable;

/**
 * 設定タブ（FR-045）。
 *
 * <p>この部品は判断をしない。利用者が触った結果として「こうしたい」という
 * {@link UserSettings} を丸ごと {@link SettingsIntentSink} へ渡すだけである（ARC-011）。
 * 差し替えて保存するかどうかは application が決める。
 */
public final class SettingsPanel {

    private static final int SIZE_STEP = 1;
    private static final int CELL_INSET = 4;

    private final JPanel panel = new JPanel(new GridBagLayout());
    private final JComboBox<ClockFormat> format = new JComboBox<>(ClockFormat.values());
    private final JCheckBox seconds = new JCheckBox();
    private final JCheckBox date = new JCheckBox();
    private final JCheckBox topmost = new JCheckBox();
    private final JComboBox<Typeface> typeface = new JComboBox<>(Typeface.values());
    private final JSpinner size = new JSpinner(new SpinnerNumberModel(
            FontSize.DEFAULT.points(), FontSize.MINIMUM_POINTS, FontSize.MAXIMUM_POINTS, SIZE_STEP));
    private final JButton colour = new JButton();
    private final JButton background = new JButton();
    private final JLabel status = new JLabel(" ");

    private SettingsIntentSink sink = requested -> {};
    private UserSettings shown = UserSettings.defaults();
    private RgbColor chosenColour = RgbColor.DEFAULT_FONT;
    private RgbColor chosenBackground = RgbColor.DEFAULT_BACKGROUND;

    /** 部品を組み立てる。表示内容は {@code render*} が決める。 */
    public SettingsPanel() {
        layOut();
        listen();
    }

    /** 画面に載せるための Swing 部品。 */
    public JComponent component() {
        return panel;
    }

    /** 意図の宛先を 1 度だけ結ぶ。合成ルートが呼ぶ。 */
    public void onSettingsRequested(SettingsIntentSink requested) {
        this.sink = Objects.requireNonNull(requested, "requested");
    }

    /** 現在の設定を反映する。選べる書体は同梱の 30 種で固定なので、ここでは差し替えない。 */
    public void renderSettings(UserSettings settings) {
        shown = Objects.requireNonNull(settings, "settings");
        chosenColour = shown.fontColor();
        chosenBackground = shown.backgroundColor();
        typeface.setSelectedItem(shown.typeface());
        format.setSelectedItem(shown.clockFormat());
        size.setValue(shown.fontSize().points());
        seconds.setSelected(shown.secondsVisibility() == SecondsVisibility.SHOWN);
        date.setSelected(shown.dateVisibility() == DateVisibility.SHOWN);
        topmost.setSelected(shown.windowTopmost() == WindowTopmost.ENABLED);
        renderColour();
    }

    /** 保存の結果を伝える。失敗を握り潰さない（FR-045）。 */
    public void renderSaveOutcome(SettingsSaveOutcome outcome) {
        Objects.requireNonNull(outcome, "outcome");
        status.setText(
                switch (outcome) {
                    case SettingsSaveOutcome.Saved saved -> " ";
                    case SettingsSaveOutcome.Failed failed -> describe(failed.reason());
                });
    }

    private void renderColour() {
        colour.setForeground(awtColour(chosenColour));
        colour.setText(hexOf(chosenColour));
        background.setForeground(awtColour(chosenBackground));
        background.setText(hexOf(chosenBackground));
    }

    private static Color awtColour(RgbColor value) {
        return new Color(value.red(), value.green(), value.blue());
    }

    private static String hexOf(RgbColor value) {
        return String.format(Locale.ROOT, "#%02X%02X%02X", value.red(), value.green(), value.blue());
    }

    private void layOut() {
        int row = 0;
        addRow(row++, "Time format", format);
        addRow(row++, "Show seconds", seconds);
        addRow(row++, "Show date", date);
        addRow(row++, "Always on top", topmost);
        addRow(row++, "Typeface", typeface);
        addRow(row++, "Font size", size);
        addRow(row++, "Font colour", colour);
        addRow(row++, "Background colour", background);
        addRow(row, "", status);
    }

    private void addRow(int row, String label, JComponent field) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(CELL_INSET, CELL_INSET, CELL_INSET, CELL_INSET);
        constraints.gridy = row;
        constraints.gridx = 0;
        constraints.anchor = GridBagConstraints.LINE_END;
        panel.add(new JLabel(label), constraints);
        constraints.gridx = 1;
        constraints.anchor = GridBagConstraints.LINE_START;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, constraints);
    }

    private void listen() {
        format.addActionListener(event -> submitIfChanged());
        seconds.addActionListener(event -> submitIfChanged());
        date.addActionListener(event -> submitIfChanged());
        topmost.addActionListener(event -> submitIfChanged());
        typeface.addActionListener(event -> submitIfChanged());
        size.addChangeListener(event -> submitIfChanged());
        colour.addActionListener(event -> chooseFontColour());
        background.addActionListener(event -> chooseBackgroundColour());
    }

    /**
     * 変化があったときだけ意図を送る。
     *
     * <p>反映（{@code render*}）でも部品のイベントは発火するが、そのときは読み戻した値が
     * 表示中の設定と等しいので何も送られない。再入を止めるためのフラグを持たずに済む。
     */
    private void submitIfChanged() {
        UserSettings requested = readForm();
        if (!requested.equals(shown)) {
            sink.submit(requested);
        }
    }

    private UserSettings readForm() {
        return new UserSettings(
                format.getItemAt(format.getSelectedIndex()),
                seconds.isSelected() ? SecondsVisibility.SHOWN : SecondsVisibility.HIDDEN,
                date.isSelected() ? DateVisibility.SHOWN : DateVisibility.HIDDEN,
                topmost.isSelected() ? WindowTopmost.ENABLED : WindowTopmost.DISABLED,
                selectedTypeface(),
                selectedSize(),
                chosenColour,
                chosenBackground);
    }

    private Typeface selectedTypeface() {
        return typeface.getItemAt(typeface.getSelectedIndex());
    }

    private FontSize selectedSize() {
        int points = ((Number) size.getValue()).intValue();
        return switch (FontSize.of(points)) {
            case FontSizeOutcome.Accepted accepted -> accepted.value();
            case FontSizeOutcome.Rejected outOfRange -> shown.fontSize();
        };
    }

    private void chooseFontColour() {
        RgbColor picked = ask("Font colour", chosenColour);
        if (picked != null) {
            chosenColour = picked;
            renderColour();
            submitIfChanged();
        }
    }

    private void chooseBackgroundColour() {
        RgbColor picked = ask("Background colour", chosenBackground);
        if (picked != null) {
            chosenBackground = picked;
            renderColour();
            submitIfChanged();
        }
    }

    private @Nullable RgbColor ask(String title, RgbColor current) {
        Color picked = JColorChooser.showDialog(panel, title, awtColour(current));
        if (picked == null) {
            return null;
        }
        return switch (RgbColor.of(picked.getRed(), picked.getGreen(), picked.getBlue())) {
            case RgbColorOutcome.Accepted accepted -> accepted.value();
            // 色選択ダイアログは 0..255 の外を返さない。返したなら選ばなかった扱いにする。
            case RgbColorOutcome.Rejected outOfRange -> null;
        };
    }

    private static String describe(SettingsSaveFailure reason) {
        return switch (reason) {
            case UNWRITABLE -> "Could not save the settings. They apply now but will not survive a restart.";
        };
    }
}
