package io.github.hideyukimori.neneclock.ui.swing;

import io.github.hideyukimori.neneclock.domain.Typeface;
import io.github.hideyukimori.neneclock.domain.TypefaceMood;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import org.jspecify.annotations.Nullable;

/**
 * 書体を選ぶ画面（FR-043 / FR-045）。
 *
 * <p>30 枚の札をその書体自身で描き、雰囲気で絞り込める。名前の一覧にしないのは、
 * 名前からは雰囲気が分からないためである。
 */
public final class TypefacePickerPanel {

    private static final int COLUMNS = 3;
    private static final int GAP = 8;
    private static final int SIDE = 18;
    private static final int SCROLL_UNIT = 16;
    private static final int CHIPS_TOP = 12;
    private static final int CHIPS_BOTTOM = 10;
    private static final int CHIP_GAP = 6;
    private static final int SCROLLBAR_WIDTH = 8;
    private static final int CHIP_ROW_HEIGHT = 46;
    private static final String ALL = "ALL";

    private final JPanel surface = new JPanel(new BorderLayout());
    private final JPanel grid = new JPanel(new GridLayout(0, COLUMNS, GAP, GAP));
    private final JPanel chips = new JPanel();
    private final JScrollPane scroller = new JScrollPane(grid);
    private final Map<Typeface, TypefaceCard> cards = new EnumMap<>(Typeface.class);
    private final Map<String, MoodChip> filters = new LinkedHashMap<>();

    private Consumer<Typeface> chosen = typeface -> {};
    private @Nullable TypefaceMood filter;
    private Typeface selected = Typeface.DEFAULT;
    private @Nullable UiTheme theme;

    /** 30 枚の札を作る。書体の読み込みはここで 1 度だけ行う。 */
    public TypefacePickerPanel(TypefaceFontLoader typefaces) {
        Objects.requireNonNull(typefaces, "typefaces");
        for (Typeface typeface : Typeface.values()) {
            TypefaceCard card = new TypefaceCard(typeface, typefaces);
            card.onChosen(picked -> chosen.accept(picked));
            cards.put(typeface, card);
        }
        layOutChips();
        layOut();
        renderGrid();
    }

    /** 画面に載せるための Swing 部品。 */
    public JComponent component() {
        return surface;
    }

    /** 選ばれたことの宛先を 1 度だけ結ぶ。 */
    public void onTypefaceChosen(Consumer<Typeface> action) {
        this.chosen = Objects.requireNonNull(action, "action");
    }

    /** いま選ばれている書体と配色を反映する。 */
    public void renderSelection(Typeface current, UiTheme shownTheme) {
        this.selected = Objects.requireNonNull(current, "current");
        this.theme = Objects.requireNonNull(shownTheme, "shownTheme");
        Palette palette = shownTheme.palette();
        surface.setBackground(palette.surface());
        chips.setBackground(palette.surface());
        grid.setBackground(palette.surface());
        scroller.getViewport().setBackground(palette.surface());
        for (Map.Entry<Typeface, TypefaceCard> entry : cards.entrySet()) {
            entry.getValue().renderSelection(entry.getKey() == current, shownTheme);
        }
        renderChips(shownTheme);
    }

    private void renderChips(UiTheme shownTheme) {
        for (Map.Entry<String, MoodChip> entry : filters.entrySet()) {
            String key = entry.getKey();
            boolean active = key.equals(filter == null ? ALL : filter.name());
            String label = key.equals(ALL) ? shownTheme.text(UiText.ALL_MOODS) : labelOf(TypefaceMood.valueOf(key));
            entry.getValue().renderSelection(label, active, shownTheme);
        }
    }

    private void layOutChips() {
        // 🔴 BoxLayout だと札が縦に潰れた（実測）。FlowLayout は各札の推奨サイズをそのまま使う。
        chips.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, CHIP_GAP, 0));
        chips.setPreferredSize(new Dimension(0, CHIP_ROW_HEIGHT));
        chips.setBorder(BorderFactory.createEmptyBorder(CHIPS_TOP, SIDE, CHIPS_BOTTOM, SIDE));
        addChip(null);
        for (TypefaceMood mood : TypefaceMood.values()) {
            addChip(mood);
        }
    }

    private void addChip(@Nullable TypefaceMood mood) {
        MoodChip chip = new MoodChip();
        chip.onPressed(() -> {
            filter = mood;
            renderGrid();
            UiTheme shownTheme = theme;
            if (shownTheme != null) {
                renderSelection(selected, shownTheme);
            }
        });
        filters.put(mood == null ? ALL : mood.name(), chip);
        chips.add(chip.component());
    }

    private static String labelOf(TypefaceMood mood) {
        return switch (mood) {
            case SANS -> "Sans";
            case SERIF -> "Serif";
            case MONO -> "Mono";
            case DISPLAY -> "Display";
            case RETRO -> "Retro";
            case HAND -> "Hand";
        };
    }

    private void renderGrid() {
        grid.removeAll();
        for (Map.Entry<Typeface, TypefaceCard> entry : cards.entrySet()) {
            if (filter == null || entry.getKey().mood() == filter) {
                grid.add(entry.getValue().component());
            }
        }
        grid.revalidate();
        grid.repaint();
    }

    private void layOut() {
        grid.setBorder(BorderFactory.createEmptyBorder(0, SIDE, SIDE, SIDE));
        scroller.setBorder(BorderFactory.createEmptyBorder());
        scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroller.getVerticalScrollBar().setUnitIncrement(SCROLL_UNIT);
        scroller.getVerticalScrollBar().setPreferredSize(new Dimension(SCROLLBAR_WIDTH, 0));
        surface.add(chips, BorderLayout.NORTH);
        surface.add(scroller, BorderLayout.CENTER);
    }
}
