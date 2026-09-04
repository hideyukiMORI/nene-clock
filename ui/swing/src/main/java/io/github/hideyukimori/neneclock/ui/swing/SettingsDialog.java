package io.github.hideyukimori.neneclock.ui.swing;

import io.github.hideyukimori.neneclock.application.SettingsSaveFailure;
import io.github.hideyukimori.neneclock.application.SettingsSaveOutcome;
import io.github.hideyukimori.neneclock.domain.ProductIdentity;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.geom.RoundRectangle2D;
import java.util.Locale;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.jspecify.annotations.Nullable;

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
    private final JLabel title = TextRendering.label("");
    private final JLabel status = TextRendering.label("");
    private final JLabel signature = TextRendering.label("");
    private final ProductIdentity identity;
    private final IconButton back = new IconButton(ChromeIcon.BACK);
    private final IconButton close = new IconButton(ChromeIcon.CLOSE);

    private final SettingsFormPanel form;
    private final TypefacePickerPanel typefaces;
    private final ColourPickerPanel colours;

    private SettingsDestination showing = SettingsDestination.FORM;
    private boolean saved = true;
    private @Nullable UiTheme theme;

    /** 3 つの画面を束ねて 1 つのモーダルにする。 */
    public SettingsDialog(Frame owner, SettingsPanels panels, ProductIdentity identity) {
        this.dialog = new JDialog(Objects.requireNonNull(owner, "owner"));
        this.identity = Objects.requireNonNull(identity, "identity");
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

    /** いま出ている画面の見出しを描き直す。言語が変わったときに要る。 */
    private void renderTitle(UiTheme shownTheme) {
        title.setText(titleOf(showing, shownTheme));
        title.setFont(shownTheme.font(TITLE_POINTS));
        status.setFont(shownTheme.font(STATUS_POINTS));
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
        UiTheme shownTheme = theme;
        if (shownTheme != null) {
            renderTitle(shownTheme);
        }
        back.component().setVisible(destination != SettingsDestination.FORM);
    }

    /** いま出ている画面。 */
    public SettingsDestination showingNow() {
        return showing;
    }

    /** 配色と言語を反映する。配色は時計の背景色の明るさに追従する。 */
    public void renderTheme(UiTheme shownTheme) {
        theme = Objects.requireNonNull(shownTheme, "shownTheme");
        Palette palette = shownTheme.palette();
        renderTitle(shownTheme);
        renderStatusText(shownTheme);
        renderSignature(shownTheme);
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
        back.renderColours(shownTheme);
        close.renderColours(shownTheme);
    }

    /** 版と作者。フッターの右端に、状態行より薄く出す。 */
    private void renderSignature(UiTheme shownTheme) {
        signature.setText(
                String.format(Locale.ROOT, shownTheme.text(UiText.SIGNATURE), identity.version(), identity.author()));
        signature.setFont(shownTheme.font(STATUS_POINTS));
        signature.setForeground(shownTheme.palette().textFaint());
    }

    /** 状態行を、いまの言語で書き直す。 */
    private void renderStatusText(UiTheme shownTheme) {
        status.setText(shownTheme.text(saved ? UiText.SAVED : UiText.NOT_SAVED));
        status.setForeground(
                saved ? shownTheme.palette().textMuted() : shownTheme.palette().warning());
    }

    /** 保存の結果を伝える。失敗を握り潰さない（FR-045）。 */
    public void renderSaveOutcome(SettingsSaveOutcome outcome) {
        Objects.requireNonNull(outcome, "outcome");
        saved = switch (outcome) {
            case SettingsSaveOutcome.Saved success -> true;
            case SettingsSaveOutcome.Failed failed -> describe(failed.reason());
        };
        UiTheme shownTheme = theme;
        if (shownTheme != null) {
            renderStatusText(shownTheme);
        }
    }

    /** 失敗の種類を、伝えるべきかどうかへ落とす。種類が増えたらここが落ちる（JAV-002）。 */
    private static boolean describe(SettingsSaveFailure reason) {
        return switch (reason) {
            case UNWRITABLE -> false;
        };
    }

    private static String titleOf(SettingsDestination destination, UiTheme shownTheme) {
        return shownTheme.text(
                switch (destination) {
                    case FORM -> UiText.SETTINGS;
                    case TYPEFACE -> UiText.TYPEFACE;
                    case FONT_COLOUR -> UiText.FONT_COLOUR;
                    case BACKGROUND_COLOUR -> UiText.BACKGROUND_COLOUR;
                });
    }

    private static String cardNameOf(SettingsDestination destination) {
        return switch (destination) {
            case FORM -> "form";
            case TYPEFACE -> "typeface";
            case FONT_COLOUR, BACKGROUND_COLOUR -> "colour";
        };
    }

    private void layOut() {
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
        footer.add(signature, BorderLayout.EAST);
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
