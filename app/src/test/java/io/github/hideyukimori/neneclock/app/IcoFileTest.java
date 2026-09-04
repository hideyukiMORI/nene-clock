package io.github.hideyukimori.neneclock.app;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.hideyukimori.neneclock.ui.swing.AppIcon;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

class IcoFileTest {

    private static final byte[] PNG_SIGNATURE = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};

    @Test
    void writesAnIcoDirectoryWhosePointersLandOnPngImages() throws IOException {
        List<BufferedImage> images =
                AppIcon.images().stream().map(image -> (BufferedImage) image).toList();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        IcoFile.write(images, out);
        byte[] ico = out.toByteArray();

        assertThat(ico).startsWith((byte) 0, (byte) 0, (byte) 1, (byte) 0, (byte) images.size(), (byte) 0);
        for (int index = 0; index < images.size(); index++) {
            int entry = 6 + 16 * index;
            int side = images.get(index).getWidth();
            assertThat(Byte.toUnsignedInt(ico[entry])).isEqualTo(side == 256 ? 0 : side);
            assertThat(Byte.toUnsignedInt(ico[entry + 1])).isEqualTo(side == 256 ? 0 : side);
            int size = littleEndianInt(ico, entry + 8);
            int offset = littleEndianInt(ico, entry + 12);
            assertThat(offset + size).isLessThanOrEqualTo(ico.length);
            byte[] head = new byte[PNG_SIGNATURE.length];
            System.arraycopy(ico, offset, head, 0, head.length);
            assertThat(head).isEqualTo(PNG_SIGNATURE);
        }
    }

    @Test
    void theBundledIconHasEverySizeWindowsAsksFor() {
        List<Integer> sides = AppIcon.images().stream()
                .map(image -> ((BufferedImage) image).getWidth())
                .toList();
        assertThat(sides).contains(16, 32, 48, 256);
    }

    private static int littleEndianInt(byte[] bytes, int at) {
        return Byte.toUnsignedInt(bytes[at])
                | Byte.toUnsignedInt(bytes[at + 1]) << 8
                | Byte.toUnsignedInt(bytes[at + 2]) << 16
                | Byte.toUnsignedInt(bytes[at + 3]) << 24;
    }
}
