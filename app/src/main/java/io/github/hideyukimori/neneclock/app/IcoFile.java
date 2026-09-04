package io.github.hideyukimori.neneclock.app;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Windows のアイコン（{@code .ico}）を書く。
 *
 * <p>ICO は「目次 ＋ 画像の列」だけの形式で、Vista 以降は各画像を PNG のまま埋め込める。
 * だから PNG を書ける {@link ImageIO} があれば、外の道具（ImageMagick）は要らない。
 * 目次はリトルエンディアンで書く。{@link DataOutputStream} はビッグエンディアンなので、
 * 数値はここでバイトに割る。
 *
 * <p>256px は幅・高さの欄に 0 と書く決まりである（1 バイトに 256 が入らないため）。
 */
final class IcoFile {

    private static final int RESERVED = 0;
    private static final int TYPE_ICON = 1;
    private static final int COLOUR_PLANES = 1;
    private static final int BITS_PER_PIXEL = 32;
    private static final int HEADER_BYTES = 6;
    private static final int ENTRY_BYTES = 16;
    private static final int LARGEST_ENCODABLE_SIDE = 255;
    private static final int BYTE_MASK = 0xFF;
    private static final int BITS_PER_BYTE = 8;

    private IcoFile() {}

    /** 与えられた画像を 1 つの ICO として書く。順序はそのまま保つ。 */
    static void write(List<BufferedImage> images, OutputStream destination) throws IOException {
        List<byte[]> encoded = new ArrayList<>();
        for (BufferedImage image : images) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ImageIO.write(image, "png", buffer);
            encoded.add(buffer.toByteArray());
        }
        DataOutputStream out = new DataOutputStream(destination);
        writeShort(out, RESERVED);
        writeShort(out, TYPE_ICON);
        writeShort(out, images.size());
        int offset = HEADER_BYTES + ENTRY_BYTES * images.size();
        for (int index = 0; index < images.size(); index++) {
            BufferedImage image = images.get(index);
            byte[] bytes = encoded.get(index);
            out.writeByte(sideByte(image.getWidth()));
            out.writeByte(sideByte(image.getHeight()));
            out.writeByte(RESERVED);
            out.writeByte(RESERVED);
            writeShort(out, COLOUR_PLANES);
            writeShort(out, BITS_PER_PIXEL);
            writeInt(out, bytes.length);
            writeInt(out, offset);
            offset += bytes.length;
        }
        for (byte[] bytes : encoded) {
            out.write(bytes);
        }
        out.flush();
    }

    private static int sideByte(int side) {
        return side > LARGEST_ENCODABLE_SIDE ? 0 : side;
    }

    private static void writeShort(DataOutputStream out, int value) throws IOException {
        out.writeByte(value & BYTE_MASK);
        out.writeByte((value >>> BITS_PER_BYTE) & BYTE_MASK);
    }

    private static void writeInt(DataOutputStream out, int value) throws IOException {
        for (int shift = 0; shift < Integer.SIZE; shift += BITS_PER_BYTE) {
            out.writeByte((value >>> shift) & BYTE_MASK);
        }
    }
}
