package io.github.hideyukimori.neneclock.ui.swing;

import io.github.hideyukimori.neneclock.domain.RgbColor;
import io.github.hideyukimori.neneclock.domain.RgbColorOutcome;
import io.github.hideyukimori.neneclock.domain.UserSettings;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * 色を選ぶ画面（FR-044 / FR-046 / FR-045）。
 *
 * <p>厳選プリセットと HEX の直接指定を並べる。既製の {@code JColorChooser} を使わないのは、
 * タブと HSB スライダが並ぶ画面が、時計の設定に対して重すぎるためである。
 *
 * <p>🔑 読めない組み合わせを**拒否しない**。警告して、直す手段を隣に置くだけである（ADR 0007）。
 */
public final class ColourPickerPanel {

    private static final int SIDE = 18;
    private static final int GAP = 8;
    private static final int HEX_LENGTH = 6;
    private static final float SMALL_POINTS = 11f;
    private static final int RADIX = 16;
    private static final int TOP_INSET = 14;
    private static final int HEX_FIELD_WIDTH = 90;
    private static final int HEX_FIELD_HEIGHT = 26;
    private static final int CONTRAST_ROW_HEIGHT = 28;
    private static final int HEX_GAP = 4;
    private static final int RED_SHIFT = 16;
    private static final int GREEN_SHIFT = 8;
    private static final int BYTE_MASK = 0xFF;
    private static final int SWATCH_ROWS = 3;
    private static final int SWATCH_HEIGHT = 34;

    private final JPanel surface = new JPanel();
    private final ClockPreview preview;
    private final JPanel swatches = new JPanel(new GridLayout(0, SwatchPresets.PER_ROW, GAP, GAP));
    private final List<ColourSwatch> presets = new ArrayList<>();
    private final JTextField hex = new JTextField(HEX_LENGTH);
    private final JLabel hexPrefix = new JLabel("#");
    private final JLabel freeform = new JLabel("自由指定");
    private final JLabel contrast = new JLabel();
    private final MoodChip repair = new MoodChip("読める色にする");

    private Consumer<RgbColor> chosen = colour -> {};
    private Palette palette = Palette.from(RgbColor.DEFAULT_BACKGROUND);
    private ColourEditing editing = new ColourEditing(UserSettings.defaults(), SettingsDestination.FONT_COLOUR);

    /** 部品を組み立てる。中身は {@code render*} が決める。 */
    public ColourPickerPanel(TypefaceFontLoader typefaces) {
        this.preview = new ClockPreview(typefaces);
        for (RgbColor value : SwatchPresets.all()) {
            ColourSwatch swatch = new ColourSwatch(value);
            swatch.onChosen(picked -> chosen.accept(picked));
            presets.add(swatch);
            swatches.add(swatch.component());
        }
        repair.onPressed(() -> chosen.accept(readableAgainst(editing.counterpart())));
        layOut();
        listen();
    }

    /** 画面に載せるための Swing 部品。 */
    public JComponent component() {
        return surface;
    }

    /** 色が選ばれたことの宛先を 1 度だけ結ぶ。 */
    public void onColourChosen(Consumer<RgbColor> action) {
        this.chosen = Objects.requireNonNull(action, "action");
    }

    /** 編集中の色・見本・コントラストを反映する。 */
    public void renderColour(ColourEditing shown, ClockPreviewText text) {
        editing = Objects.requireNonNull(shown, "shown");
        UserSettings settings = shown.settings();
        palette = Palette.from(settings.backgroundColor());
        preview.renderSettings(settings, text.time(), text.date());
        if (!hex.isFocusOwner()) {
            hex.setText(SettingsFormPanel.hexOf(shown.editing()).substring(1));
        }
        renderContrast(ContrastReading.between(settings.fontColor(), settings.backgroundColor()));
        for (ColourSwatch swatch : presets) {
            swatch.renderSelection(swatch.holds(shown.editing()), palette);
        }
        renderChrome();
    }

    /**
     * 相手の色に対して読める色を選ぶ。黒か白のうち、対比が大きいほうを返す。
     *
     * <p>ここで凝った色を提案しないのは、これが「好みの提案」ではなく「読めない状態からの脱出」だからである。
     */
    private static RgbColor readableAgainst(RgbColor other) {
        ContrastReading darker = ContrastReading.between(RgbColor.DEFAULT_FONT, other);
        return darker.ratio() >= ContrastReading.between(white(), other).ratio() ? RgbColor.DEFAULT_FONT : white();
    }

    private static RgbColor white() {
        return switch (RgbColor.of(
                RgbColor.MAXIMUM_COMPONENT, RgbColor.MAXIMUM_COMPONENT, RgbColor.MAXIMUM_COMPONENT)) {
            case RgbColorOutcome.Accepted accepted -> accepted.value();
            // 上限そのものなので、ここへは来ない。
            case RgbColorOutcome.Rejected outOfRange -> RgbColor.DEFAULT_FONT;
        };
    }

    private void renderContrast(ContrastReading reading) {
        contrast.setText(String.format(Locale.ROOT, "コントラスト比 %.1f : 1", reading.ratio()));
        contrast.setForeground(reading.isTooLow() ? palette.warning() : palette.textMuted());
        repair.component().setVisible(reading.isTooLow());
        repair.renderSelection(false, palette);
    }

    private void renderChrome() {
        surface.setBackground(palette.surface());
        swatches.setBackground(palette.surface());
        freeform.setForeground(palette.textMuted());
        hexPrefix.setForeground(palette.textFaint());
        hex.setBackground(palette.surface());
        hex.setForeground(palette.text());
        hex.setCaretColor(palette.text());
        hex.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, palette.hairline()),
                BorderFactory.createEmptyBorder(HEX_GAP / 2, HEX_GAP / 2, HEX_GAP / 2, HEX_GAP / 2)));
    }

    /**
     * 縦に積む。
     *
     * <p>🔴 BoxLayout は子の {@code alignmentX} が混ざると横位置が崩れる。既定は中央で、
     * 左寄せの子と混ぜると全体が右へずれた（実機で踏んだ）。だから**全部の子に左寄せを言う**。
     */
    private void layOut() {
        surface.setLayout(new BoxLayout(surface, BoxLayout.Y_AXIS));
        surface.setBorder(BorderFactory.createEmptyBorder(TOP_INSET, SIDE, SIDE, SIDE));
        preview.component().setAlignmentX(Component.LEFT_ALIGNMENT);
        surface.add(preview.component());
        surface.add(Box.createVerticalStrut(TOP_INSET));
        swatches.setOpaque(true);
        swatches.setAlignmentX(Component.LEFT_ALIGNMENT);
        // 🔴 最大の大きさを言わないと、BoxLayout は推奨の幅で中央へ寄せる（実機で右寄りになった）。
        int gridHeight = SWATCH_ROWS * SWATCH_HEIGHT + (SWATCH_ROWS - 1) * GAP;
        swatches.setMaximumSize(new Dimension(Integer.MAX_VALUE, gridHeight));
        swatches.setPreferredSize(new Dimension(0, gridHeight));
        surface.add(swatches);
        surface.add(Box.createVerticalStrut(SIDE));
        JComponent hexRow = hexRow();
        hexRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        surface.add(hexRow);
        surface.add(Box.createVerticalStrut(GAP + HEX_GAP));
        JComponent contrastRow = contrastRow();
        contrastRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        surface.add(contrastRow);
        surface.add(Box.createVerticalGlue());
    }

    private JComponent hexRow() {
        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        freeform.setFont(freeform.getFont().deriveFont(Font.PLAIN, SMALL_POINTS));
        hexPrefix.setFont(hexPrefix.getFont().deriveFont(Font.PLAIN, SMALL_POINTS));
        hex.setMaximumSize(new Dimension(HEX_FIELD_WIDTH, HEX_FIELD_HEIGHT));
        row.add(freeform);
        row.add(Box.createHorizontalStrut(GAP + HEX_GAP / 2));
        row.add(hexPrefix);
        row.add(Box.createHorizontalStrut(HEX_GAP));
        row.add(hex);
        row.add(Box.createHorizontalGlue());
        return row;
    }

    private JComponent contrastRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        contrast.setFont(contrast.getFont().deriveFont(Font.PLAIN, SMALL_POINTS));
        row.add(contrast, BorderLayout.WEST);
        row.add(repair.component(), BorderLayout.EAST);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, CONTRAST_ROW_HEIGHT));
        return row;
    }

    private void listen() {
        hex.addActionListener(event -> submitTypedHex());
        hex.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent event) {
                submitTypedHex();
            }
        });
    }

    /** 打ち込まれた HEX を読む。読めない字が来たら、直前の色に戻すだけで何も言わない。 */
    private void submitTypedHex() {
        String typed = hex.getText().trim().replace("#", "");
        if (typed.length() != HEX_LENGTH) {
            hex.setText(SettingsFormPanel.hexOf(editing.editing()).substring(1));
            return;
        }
        try {
            int packed = Integer.parseInt(typed, RADIX);
            switch (RgbColor.of(
                    (packed >> RED_SHIFT) & BYTE_MASK, (packed >> GREEN_SHIFT) & BYTE_MASK, packed & BYTE_MASK)) {
                case RgbColorOutcome.Accepted accepted -> chosen.accept(accepted.value());
                case RgbColorOutcome.Rejected outOfRange ->
                    hex.setText(SettingsFormPanel.hexOf(editing.editing()).substring(1));
            }
        } catch (NumberFormatException notHex) {
            hex.setText(SettingsFormPanel.hexOf(editing.editing()).substring(1));
        }
    }
}
