package io.github.hideyukimori.neneclock.ui.swing;

import io.github.hideyukimori.neneclock.application.SettingsSaveFailure;
import io.github.hideyukimori.neneclock.application.SettingsSaveOutcome;
import io.github.hideyukimori.neneclock.domain.RgbColor;
import io.github.hideyukimori.neneclock.domain.UserSettings;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.geom.RoundRectangle2D;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * 設定モーダル（FR-045 / FR-047）。ギアから開き、3 つの画面を持つ。
 *
 * <p>タブをやめてモーダルにしたのは、時計が「置いてあるもの」であって「操作するもの」ではないからである。
 * 設定はいじるときだけ前に出てくればよい。
 *
 * <p>🔑 モーダルだが**入力は塞がない**（modeless）。設定の変更は時計本体へ即座に出るので、
 * 見えるように窓を動かせるほうがよい。塞ぐと「変えた結果を見る」ことができなくなる。
 */
public final class SettingsDialog {

    private static final int WIDTH = 600;
    private static final int HEIGHT = 580;
    private static final int CORNER = 14;
    private static final int HEADER_HEIGHT = 52;
    private static final int FOOTER_HEIGHT = 40;
    private static final int SIDE = 18;
    private static final float TITLE_POINTS = 15f;
    private static final float STATUS_POINTS = 11f;

    private final JDialog dialog;
    private final CardLayout cards = new CardLayout();
    private final JPanel body = new JPanel(cards);
    private final JPanel header = new JPanel(new BorderLayout());
    private final JPanel footer = new JPanel(new BorderLayout());
    private final JLabel title = new JLabel("設定");
    private final JLabel status = new JLabel("変更はすぐに反映され、そのまま保存されます");
    private final IconButton back = new IconButton(ChromeIcon.BACK);
    private final IconButton close = new IconButton(ChromeIcon.CLOSE);

    private final SettingsFormPanel form;
    private final TypefacePickerPanel typefaces;
    private final ColourPickerPanel colours;

    private SettingsDestination showing = SettingsDestination.FORM;
    private Palette palette = Palette.from(RgbColor.DEFAULT_BACKGROUND);

    /** 3 つの画面を束ねて 1 つのモーダルにする。 */
    public SettingsDialog(Frame owner, SettingsPanels panels) {
        this.dialog = new JDialog(Objects.requireNonNull(owner, "owner"));
        this.form = panels.form();
        this.typefaces = panels.typefaces();
        this.colours = panels.colours();
        dialog.setUndecorated(true);
        dialog.setSize(new Dimension(WIDTH, HEIGHT));
        dialog.setLocationRelativeTo(owner);
        layOut();
        listen();
        roundTheCorners();
    }

    /** 開く。開くたびに一覧の画面から始める。 */
    public void display() {
        show(SettingsDestination.FORM);
        dialog.setVisible(true);
    }

    /** 閉じる。 */
    public void hide() {
        dialog.setVisible(false);
    }

    /** 開いているかどうか。 */
    public boolean isShowing() {
        return dialog.isVisible();
    }

    /** どの画面を出すかを決める。 */
    public void show(SettingsDestination destination) {
        showing = Objects.requireNonNull(destination, "destination");
        cards.show(body, cardNameOf(destination));
        title.setText(titleOf(destination));
        back.component().setVisible(destination != SettingsDestination.FORM);
    }

    /** いま出ている画面。 */
    public SettingsDestination showingNow() {
        return showing;
    }

    /** 配色を反映する。時計の背景色の明るさに追従する。 */
    public void renderColours(UserSettings settings) {
        palette = Palette.from(settings.backgroundColor());
        dialog.getContentPane().setBackground(palette.surface());
        header.setBackground(palette.surface());
        footer.setBackground(palette.surface());
        body.setBackground(palette.surface());
        title.setForeground(palette.text());
        status.setForeground(palette.textMuted());
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, palette.hairline()),
                BorderFactory.createEmptyBorder(0, SIDE, 0, SIDE)));
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, palette.hairline()),
                BorderFactory.createEmptyBorder(0, SIDE, 0, SIDE)));
        back.renderColours(palette);
        close.renderColours(palette);
    }

    /** 保存の結果を伝える。失敗を握り潰さない（FR-045）。 */
    public void renderSaveOutcome(SettingsSaveOutcome outcome) {
        Objects.requireNonNull(outcome, "outcome");
        switch (outcome) {
            case SettingsSaveOutcome.Saved saved -> {
                status.setText("変更はすぐに反映され、そのまま保存されます");
                status.setForeground(palette.textMuted());
            }
            case SettingsSaveOutcome.Failed failed -> {
                status.setText(describe(failed.reason()));
                status.setForeground(palette.warning());
            }
        }
    }

    private static String describe(SettingsSaveFailure reason) {
        return switch (reason) {
            case UNWRITABLE -> "保存できませんでした。いまは反映されていますが、再起動すると元に戻ります";
        };
    }

    private static String titleOf(SettingsDestination destination) {
        return switch (destination) {
            case FORM -> "設定";
            case TYPEFACE -> "書体";
            case FONT_COLOUR -> "文字色";
            case BACKGROUND_COLOUR -> "背景色";
        };
    }

    private static String cardNameOf(SettingsDestination destination) {
        return switch (destination) {
            case FORM -> "form";
            case TYPEFACE -> "typeface";
            case FONT_COLOUR, BACKGROUND_COLOUR -> "colour";
        };
    }

    private void layOut() {
        title.setFont(title.getFont().deriveFont(Font.BOLD, TITLE_POINTS));
        status.setFont(status.getFont().deriveFont(Font.PLAIN, STATUS_POINTS));
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.X_AXIS));
        left.add(back.component());
        left.add(Box.createHorizontalStrut(SIDE / 2));
        left.add(title);
        header.setPreferredSize(new Dimension(0, HEADER_HEIGHT));
        header.add(left, BorderLayout.WEST);
        header.add(close.component(), BorderLayout.EAST);
        footer.setPreferredSize(new Dimension(0, FOOTER_HEIGHT));
        footer.add(status, BorderLayout.WEST);
        body.add(form.component(), "form");
        body.add(typefaces.component(), "typeface");
        body.add(colours.component(), "colour");
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(header, BorderLayout.NORTH);
        dialog.getContentPane().add(body, BorderLayout.CENTER);
        dialog.getContentPane().add(footer, BorderLayout.SOUTH);
    }

    private void listen() {
        back.onPressed(() -> show(SettingsDestination.FORM));
        close.onPressed(this::hide);
    }

    private void roundTheCorners() {
        try {
            dialog.setShape(new RoundRectangle2D.Double(0, 0, WIDTH, HEIGHT, CORNER, CORNER));
        } catch (UnsupportedOperationException unsupported) {
            // 角丸に対応していない環境。角のまま出す。
        }
    }
}
