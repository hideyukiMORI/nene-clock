package io.github.hideyukimori.neneclock.app;

import io.github.hideyukimori.neneclock.adapter.fontcatalog.BundledTypefaceAdapter;
import io.github.hideyukimori.neneclock.adapter.preferences.PreferencesSettingsAdapter;
import io.github.hideyukimori.neneclock.adapter.systemtime.SystemWallClockAdapter;
import io.github.hideyukimori.neneclock.application.ClockFaceQuery;
import io.github.hideyukimori.neneclock.application.SettingsHandler;
import io.github.hideyukimori.neneclock.application.SettingsSaveOutcome;
import io.github.hideyukimori.neneclock.application.SettingsStorePort;
import io.github.hideyukimori.neneclock.application.TypefaceBinaryPort;
import io.github.hideyukimori.neneclock.ui.swing.ClockScreen;
import io.github.hideyukimori.neneclock.ui.swing.ClockTicker;
import io.github.hideyukimori.neneclock.ui.swing.TypefaceFontLoader;
import java.awt.HeadlessException;
import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;

/**
 * アプリケーションの入口。配線と、起動できなかったことの報告だけを行う。
 *
 * <p>🔑 端末への出力とプロセスの終了コードを扱ってよいのは、このクラスだけである（ADR 0005）。
 * 窓が出る前に失敗したとき、利用者へ届く経路は端末しか無い。
 */
public final class NeNeClockApplication {

    private static final int EXIT_STARTUP_FAILED = 1;

    private NeNeClockApplication() {}

    /**
     * 起動する。すべての Swing 操作は EDT 上で行う（SWG-001）。
     *
     * <p>組み立てを {@code invokeAndWait} で待つのは、EDT で起きた失敗を握り潰さないためである。
     * 投げっぱなしにすると、窓が出ていないのに終了コード 0 で成功したように見える。
     */
    public static void main(String[] arguments) {
        try {
            SwingUtilities.invokeAndWait(NeNeClockApplication::start);
        } catch (InterruptedException | InvocationTargetException failure) {
            report(failure);
            System.exit(EXIT_STARTUP_FAILED);
        }
    }

    /** 起動できなかった理由を 1 行で伝える。スタックトレースは見せない（FR-030）。 */
    private static void report(Exception failure) {
        Throwable cause = failure.getCause() == null ? failure : failure.getCause();
        if (cause instanceof HeadlessException) {
            System.err.println("NeNe Clock needs a graphical display, but none is available.");
            System.err.println("On WSL: set guiApplications=true in .wslconfig, then run 'wsl --shutdown' on Windows.");
            return;
        }
        System.err.println("NeNe Clock could not start: " + cause);
    }

    private static void start() {
        SettingsStorePort settingsStore = PreferencesSettingsAdapter.userScoped();
        TypefaceBinaryPort typefaceBinaries = BundledTypefaceAdapter.bundled();
        SettingsHandler settings = SettingsHandler.restoredFrom(settingsStore);
        ClockFaceQuery clockFace = new ClockFaceQuery(SystemWallClockAdapter.system());

        ClockScreen screen = new ClockScreen(new TypefaceFontLoader(typefaceBinaries), ProductIdentityFile.read());
        ClockTicker ticker = new ClockTicker(() -> screen.renderFace(clockFace.currentFace(settings.current())));

        screen.onSettingsRequested(requested -> {
            SettingsSaveOutcome outcome = settings.apply(requested);
            screen.renderSettings(settings.current(), clockFace.currentFace(settings.current()));
            screen.renderSaveOutcome(outcome);
        });
        // 常駐スレッドを残さないのは合成ルートの責務である（FR-030 / ADR 0005）。
        screen.onQuitRequested(() -> {
            ticker.stop();
            screen.close();
        });

        screen.renderSettings(settings.current(), clockFace.currentFace(settings.current()));
        ticker.start();
        screen.display();
    }
}
