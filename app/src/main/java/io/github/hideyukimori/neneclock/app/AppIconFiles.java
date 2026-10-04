package io.github.hideyukimori.neneclock.app;

import io.github.hideyukimori.neneclock.ui.swing.AppIcon;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * アイコンを PNG と ICO として書き出す。**配布物を作るときだけ**使う入口。
 *
 * <p>デスクトップエントリ（{@code .desktop}）はアイコンをファイルで指すので、画像が要る。
 * だからといって画像をリポジトリに置くと、**描いている絵と置いた絵が別々に存在する**ことになり、
 * 片方だけ古くなる。ここで {@link AppIcon} から書き出せば、絵の正本は 1 つのままである。
 *
 * <p>PNG は {@code .desktop} が指す。ICO は Windows のインストーラー（{@code jpackage}）と
 * デスクトップのショートカットが指す。どちらも同じ {@link AppIcon} から出る。
 *
 * <p>{@code msix/} の下には、Microsoft Store のパッケージ（MSIX）のマニフェストが指すロゴを書く（ADR 0020）。
 *
 * <p>アプリの入口ではない。{@code ./gradlew writeAppIcons} からだけ呼ばれる。
 */
public final class AppIconFiles {

    private static final int EXIT_FAILED = 1;

    /** MSIX のマニフェストが求める 3 つのロゴと、100% 表示での一辺。名前はマニフェストの属性名と同じ。 */
    private static final Map<String, Integer> MSIX_LOGOS =
            Map.of("StoreLogo", 50, "Square44x44Logo", 44, "Square150x150Logo", 150);

    /** Windows が持つ表示倍率（%）。倍率ごとに 1 枚ずつ描く。縮めた絵を使い回さない。 */
    private static final List<Integer> MSIX_SCALES = List.of(100, 125, 150, 200, 400);

    /** タスクバーとスタートメニューの一覧が、倍率ではなく画素の大きさで選ぶ寸法。 */
    private static final List<Integer> MSIX_TARGET_SIZES = List.of(16, 24, 32, 48, 256);

    private static final String MSIX_LIST_LOGO = "Square44x44Logo";
    private static final int PERCENT = 100;

    /** Store の掲載ページに出すアイコンの一辺。パッケージには入れない。 */
    private static final int STORE_LISTING_SIZE = 300;

    private AppIconFiles() {}

    /** 第 1 引数の場所へ PNG と ICO を書き出す。 */
    public static void main(String[] arguments) {
        if (arguments.length != 1) {
            System.err.println("usage: AppIconFiles <出力先ディレクトリ>");
            System.exit(EXIT_FAILED);
            return;
        }
        File directory = new File(arguments[0]);
        if (!directory.isDirectory() && !directory.mkdirs()) {
            System.err.println("出力先を作れない: " + directory);
            System.exit(EXIT_FAILED);
            return;
        }
        try {
            write(directory);
        } catch (IOException failure) {
            System.err.println("アイコンを書き出せない: " + failure.getMessage());
            System.exit(EXIT_FAILED);
        }
    }

    private static void write(File directory) throws IOException {
        List<BufferedImage> drawn = new ArrayList<>();
        for (Image image : AppIcon.images()) {
            drawn.add((BufferedImage) image);
        }
        for (BufferedImage image : drawn) {
            File file = new File(directory, "nene-clock-" + image.getWidth() + ".png");
            ImageIO.write(image, "png", file);
            System.out.println(file.getPath());
        }
        File ico = new File(directory, "nene-clock.ico");
        try (OutputStream out = new FileOutputStream(ico)) {
            IcoFile.write(drawn, out);
        }
        System.out.println(ico.getPath());
        File msix = new File(directory, "msix");
        if (!msix.isDirectory() && !msix.mkdirs()) {
            throw new IOException("出力先を作れない: " + msix);
        }
        clear(msix);
        for (Map.Entry<String, Integer> logo : MSIX_LOGOS.entrySet()) {
            for (int scale : MSIX_SCALES) {
                int side = Math.round(logo.getValue() * scale / (float) PERCENT);
                writePng(side, new File(msix, logo.getKey() + ".scale-" + scale + ".png"));
            }
        }
        for (int side : MSIX_TARGET_SIZES) {
            writePng(side, new File(msix, MSIX_LIST_LOGO + ".targetsize-" + side + ".png"));
            writePng(side, new File(msix, MSIX_LIST_LOGO + ".targetsize-" + side + "_altform-unplated.png"));
        }
        writePng(STORE_LISTING_SIZE, new File(directory, "store-listing-" + STORE_LISTING_SIZE + ".png"));
    }

    private static void writePng(int side, File file) throws IOException {
        ImageIO.write(AppIcon.at(side), "png", file);
        System.out.println(file.getPath());
    }

    /** 前に書いた名前のロゴを残さない。残すと、古い名前の絵がパッケージに混ざる。 */
    private static void clear(File directory) throws IOException {
        File[] stale = directory.listFiles();
        if (stale == null) {
            throw new IOException("出力先を読めない: " + directory);
        }
        for (File file : stale) {
            if (!file.delete()) {
                throw new IOException("古いロゴを消せない: " + file);
            }
        }
    }
}
