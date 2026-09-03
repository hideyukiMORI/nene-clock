package io.github.hideyukimori.neneclock.domain;

/**
 * 時計の表示に使う書体（FR-043）。
 *
 * <p>🔑 ここに並ぶのは「アプリが同梱している書体」であって、「実行環境が持っている書体」ではない。
 * 同梱したことで利用可能性が環境依存の事実でなくなり、選択肢が閉じたので列挙で表せる（JAV-002 / ADR 0006）。
 *
 * <p>書体の実体（TTF）を持つのは {@code :adapters:font-catalog} ただ 1 つで、domain は
 * ファイル名も置き場所も知らない。ここが知っているのは「どの書体があるか」だけである。
 */
public enum Typeface {
    INTER("Inter", TypefaceMood.SANS),
    ROBOTO("Roboto", TypefaceMood.SANS),
    MONTSERRAT("Montserrat", TypefaceMood.SANS),
    POPPINS("Poppins", TypefaceMood.SANS),
    DM_SANS("DM Sans", TypefaceMood.SANS),
    MANROPE("Manrope", TypefaceMood.SANS),
    PLAYFAIR_DISPLAY("Playfair Display", TypefaceMood.SERIF),
    LORA("Lora", TypefaceMood.SERIF),
    EB_GARAMOND("EB Garamond", TypefaceMood.SERIF),
    BITTER("Bitter", TypefaceMood.SERIF),
    CRIMSON_TEXT("Crimson Text", TypefaceMood.SERIF),
    JETBRAINS_MONO("JetBrains Mono", TypefaceMood.MONO),
    ROBOTO_MONO("Roboto Mono", TypefaceMood.MONO),
    IBM_PLEX_MONO("IBM Plex Mono", TypefaceMood.MONO),
    SPACE_MONO("Space Mono", TypefaceMood.MONO),
    SOURCE_CODE_PRO("Source Code Pro", TypefaceMood.MONO),
    BEBAS_NEUE("Bebas Neue", TypefaceMood.DISPLAY),
    ANTON("Anton", TypefaceMood.DISPLAY),
    OSWALD("Oswald", TypefaceMood.DISPLAY),
    RIGHTEOUS("Righteous", TypefaceMood.DISPLAY),
    CINZEL("Cinzel", TypefaceMood.DISPLAY),
    ABRIL_FATFACE("Abril Fatface", TypefaceMood.DISPLAY),
    ORBITRON("Orbitron", TypefaceMood.RETRO),
    AUDIOWIDE("Audiowide", TypefaceMood.RETRO),
    SHARE_TECH_MONO("Share Tech Mono", TypefaceMood.RETRO),
    VT323("VT323", TypefaceMood.RETRO),
    MICHROMA("Michroma", TypefaceMood.RETRO),
    CAVEAT("Caveat", TypefaceMood.HAND),
    PACIFICO("Pacifico", TypefaceMood.HAND),
    DANCING_SCRIPT("Dancing Script", TypefaceMood.HAND);

    /** 既定の書体。等幅なので秒が変わっても桁が動かない（FR-040）。 */
    public static final Typeface DEFAULT = JETBRAINS_MONO;

    private final String displayName;
    private final TypefaceMood mood;

    Typeface(String displayName, TypefaceMood mood) {
        this.displayName = displayName;
        this.mood = mood;
    }

    /** 利用者に見せる名前。書体そのものの名前であり、翻訳しない。 */
    public String displayName() {
        return displayName;
    }

    /** この書体の雰囲気。設定画面の絞り込みに使う。 */
    public TypefaceMood mood() {
        return mood;
    }
}
