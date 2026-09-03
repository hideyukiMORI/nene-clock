package io.github.hideyukimori.neneclock.app;

import io.github.hideyukimori.neneclock.ui.swing.AppIcon;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * アイコンを PNG として書き出す。**配布物を作るときだけ**使う入口。
 *
 * <p>デスクトップエントリ（{@code .desktop}）はアイコンをファイルで指すので、画像が要る。
 * だからといって画像をリポジトリに置くと、**描いている絵と置いた絵が別々に存在する**ことになり、
 * 片方だけ古くなる。ここで {@link AppIcon} から書き出せば、絵の正本は 1 つのままである。
 *
 * <p>アプリの入口ではない。{@code ./gradlew writeAppIcons} からだけ呼ばれる。
 */
public final class AppIconFiles {

    private static final int EXIT_FAILED = 1;

    private AppIconFiles() {}

    /** 第 1 引数の場所へ PNG を書き出す。 */
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
        List<Image> images = AppIcon.images();
        for (Image image : images) {
            BufferedImage drawn = (BufferedImage) image;
            File file = new File(directory, "nene-clock-" + drawn.getWidth() + ".png");
            ImageIO.write(drawn, "png", file);
            System.out.println(file.getPath());
        }
    }
}
