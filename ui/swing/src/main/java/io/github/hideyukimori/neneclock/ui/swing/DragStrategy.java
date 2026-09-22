package io.github.hideyukimori.neneclock.ui.swing;

/**
 * ドラッグ中に窓を動かす方法（Issue #100）。<b>起動時に 1 度だけ決まる。</b>
 *
 * <p>🔴 これは「決め打ちしない」ための一時的な分岐である。実機の自動ドラッグ試験台で 3 つを
 * 走らせ、**測定で選ぶ**。選ばれたら残りは消す。ここに分岐が残り続けることは、ARC-001
 * （一つのことを実現する方法は 1 つ）に照らして正しい状態ではない。
 *
 * <p>分岐が要るのは、実機の測定が次を示したからである（2026-09-22・施主の実機）:
 *
 * <pre>
 * 左へのドラッグ step50: 狙った実ピクセル -600
 *   窓の GC（主・原点 0・尺度 1.25）で逆変換 → setLocation(-480)
 *   ピアが実際に置いた場所 → 実ピクセル +1200 = 左画面(-3840, 1.5) の変換をそのまま掛けた値
 * </pre>
 *
 * 🔑 **ピアは既に左画面の変換を掛けているのに、{@code getGraphicsConfiguration()} はまだ主画面を
 * 返している。** 窓の GC は「大きさの経路で古い」だけでなく、**ピア自身の {@code setLocation}
 * 変換に対しても古い**。事前補償の算術は正しいが、鍵にしている画面が信用できない。
 *
 * <p>選び方: {@code -Dneneclock.dragStrategy=window|pointer|delta|verify}。読めない値は既定に落ちる。
 */
enum DragStrategy {

    /**
     * 窓が載っていると Java が思っている画面で事前補償する（既定・ADR 0019 の決定 4）。
     *
     * <p>実測: 右（125%→175%）は追従する（ずれ 30px）が、左（125%→150%）で 1842px 外れる。
     */
    WINDOW,

    /**
     * ポインタが載っている画面で事前補償する。逆変換も、窓の実寸と掴み点の写し直しも、
     * すべてポインタの画面で行う。
     *
     * <p>根拠: ドラッグ中、ポインタは常に窓の上にある。だから<b>ピアが使う画面</b>の推定としては、
     * 窓の GC よりポインタの画面のほうが確からしい。step50 の数に当てはめると
     * {@code -3840 + (-600+3840)/1.5 = -1680} で、ピアの左画面の変換はこれをちょうど -600 に戻す。
     */
    POINTER,

    /**
     * 絶対位置を使わない。ポインタの<b>移動量</b>だけ窓を動かす。
     *
     * <p>どの画面の変換も推定しないので、変換を間違えようがない。代わりに、座標の写像が
     * 切り替わった瞬間の見かけの跳びを「移動ではない」と見抜く必要がある（{@link PointerStep}）。
     * 累積するので、取りこぼすとその分だけ掴み点がずれたまま戻らない。
     */
    DELTA,

    /**
     * ポインタの画面で事前補償し、そのうえで<b>ピアが置き直したことに気づいたら掴み直す</b>。
     *
     * <p>実測: {@link #POINTER} は 70 回のドラッグのうち 2 回だけ大きく外れ、すぐ戻る。
     * 外れるのは<b>窓が境界を越えてピアの写像が切り替わった瞬間</b>である。
     *
     * <p>🔑 その瞬間は予測しなくても<b>検出できる</b>。こちらが {@code setLocation} に渡した値と、
     * 次に {@code getLocation()} が返す値が食い違ったら、ピアが置き直したということである
     * （診断ビルドで実測: 渡した -290 に対し、次のイベントで 1181 が返った）。
     * そのイベントでは<b>動かさず掴み直す</b>。予測ではなく、起きたことを見る。
     */
    VERIFY;

    private static final String PROPERTY = "neneclock.dragStrategy";

    private static final DragStrategy CHOSEN = named(System.getProperty(PROPERTY, WINDOW.name()));

    /** 起動時に決まった方法。実行中には変わらない。 */
    static DragStrategy chosen() {
        return CHOSEN;
    }

    /** 名前から選ぶ。知らない名前は既定（{@link #WINDOW}）に落とす。 */
    static DragStrategy named(String name) {
        for (DragStrategy candidate : values()) {
            if (candidate.name().equalsIgnoreCase(name)) {
                return candidate;
            }
        }
        return WINDOW;
    }
}
