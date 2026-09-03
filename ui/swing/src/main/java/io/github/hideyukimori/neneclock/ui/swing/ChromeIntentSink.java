package io.github.hideyukimori.neneclock.ui.swing;

/**
 * ウィンドウ操作の意図の宛先。
 *
 * <p>クローム自身は何も決めない。押されたことだけを伝え、何が起きるかは合成ルートが決める（ARC-011）。
 */
@FunctionalInterface
public interface ChromeIntentSink {

    /** アイコンが押された。 */
    void triggered(ChromeIcon icon);
}
