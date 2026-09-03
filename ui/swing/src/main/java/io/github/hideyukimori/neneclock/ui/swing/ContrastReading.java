package io.github.hideyukimori.neneclock.ui.swing;

import io.github.hideyukimori.neneclock.domain.RgbColor;

/**
 * 文字色と背景色の読みやすさ（FR-046）。
 *
 * <p>🔑 この値は**警告のためだけ**に使う。設定を拒否する根拠には使わない。
 * どこからが読めないかはアプリが決められない（利用者の視力・画面・環境光を知らない）ので、
 * しきい値は「明らかに読めない」側にだけ置く。
 */
public record ContrastReading(double ratio) {

    /** これを下回ったら警告する。W3C の最低基準（4.5）ではなく、明らかに読めない側に置く。 */
    private static final double READABLE_ENOUGH = 2.5;

    private static final double FULL_COMPONENT = 255.0;
    private static final double LOW_SLOPE = 12.92;
    private static final double LOW_EDGE = 0.03928;
    private static final double OFFSET = 0.055;
    private static final double GAMMA = 2.4;
    private static final double RED_WEIGHT = 0.2126;
    private static final double GREEN_WEIGHT = 0.7152;
    private static final double BLUE_WEIGHT = 0.0722;
    private static final double GLARE = 0.05;

    /** 2 色から読み取る。 */
    public static ContrastReading between(RgbColor foreground, RgbColor background) {
        double first = relativeLuminance(foreground);
        double second = relativeLuminance(background);
        double lighter = Math.max(first, second);
        double darker = Math.min(first, second);
        return new ContrastReading((lighter + GLARE) / (darker + GLARE));
    }

    /** 明らかに読めない組み合わせかどうか。 */
    public boolean isTooLow() {
        return ratio < READABLE_ENOUGH;
    }

    private static double relativeLuminance(RgbColor colour) {
        return RED_WEIGHT * channel(colour.red())
                + GREEN_WEIGHT * channel(colour.green())
                + BLUE_WEIGHT * channel(colour.blue());
    }

    private static double channel(int component) {
        double value = component / FULL_COMPONENT;
        return value <= LOW_EDGE ? value / LOW_SLOPE : Math.pow((value + OFFSET) / (1 + OFFSET), GAMMA);
    }
}
