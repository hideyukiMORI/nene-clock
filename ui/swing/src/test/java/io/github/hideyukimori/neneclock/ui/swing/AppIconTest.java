package io.github.hideyukimori.neneclock.ui.swing;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * アイコンが「描かれている」ことを見る。
 *
 * <p>見た目が良いかは機械では見られない。ここで見るのは、**空の画像を渡していないこと**と、
 * 小さくしても 2 点が残っていることである。16px で潰れたら、タスクバーでは何も見えない。
 */
class AppIconTest {

    /** タスクバーに出る最小の大きさ。ここで壊れたら意味が無い。 */
    private static final int SMALLEST = 16;

    @ParameterizedTest
    @ValueSource(ints = {16, 20, 24, 32, 48, 64, 128, 256})
    void isDrawnAtEverySize(int size) {
        BufferedImage image = AppIcon.at(size);

        assertThat(image.getWidth()).isEqualTo(size);
        assertThat(opaquePixels(image)).isGreaterThan(size * size / 2);
    }

    @Test
    void keepsBothDotsEvenAtTheSmallestSize() {
        // 上の点と下の点は色が違う。両方が残っていることを、色の種類で見る。
        BufferedImage image = AppIcon.at(SMALLEST);

        assertThat(distinctOpaqueColours(image)).isGreaterThanOrEqualTo(3);
    }

    @Test
    void offersSeveralSizesSoTheWindowManagerCanChoose() {
        List<Image> images = AppIcon.images();

        assertThat(images).hasSize(8);
    }

    private static int opaquePixels(BufferedImage image) {
        int opaque = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if ((image.getRGB(x, y) >>> 24) > 0) {
                    opaque++;
                }
            }
        }
        return opaque;
    }

    private static int distinctOpaqueColours(BufferedImage image) {
        Set<Integer> seen = new HashSet<>();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRGB(x, y);
                if ((pixel >>> 24) == 255) {
                    seen.add(pixel & 0xFFFFFF);
                }
            }
        }
        return seen.size();
    }
}
