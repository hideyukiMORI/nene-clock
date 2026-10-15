package io.github.hideyukimori.neneclock.adapter.preferences;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.AbstractPreferences;
import org.jspecify.annotations.Nullable;

/**
 * 書き込みの途中で「プロセスが落ちる」ことを再現する保存領域。
 *
 * <p>Windows の {@code Preferences} は put のたびにレジストリへ書く。だから保存の途中で落ちると、
 * そこまでの put だけが残る。この型は指定した回数の put を受けたあと、次の put で止まる。
 * 止まった後も、それまでに書かれた値は読める（＝次の起動で見える状態）。
 */
final class InterruptedPreferences extends AbstractPreferences {

    private final Map<String, String> values = new HashMap<>();
    private int putsBeforeStopping = Integer.MAX_VALUE;
    private int puts;

    InterruptedPreferences() {
        super(null, "");
    }

    /** これから {@code puts} 回の put を受けたあと、次の put で止まる。 */
    void stopAfter(int puts) {
        putsBeforeStopping = puts;
    }

    /** これまでに受け付けた put の回数。 */
    int puts() {
        return puts;
    }

    /** 止まらない状態へ戻す。次の起動に当たる。 */
    void restart() {
        putsBeforeStopping = Integer.MAX_VALUE;
    }

    @Override
    protected void putSpi(String key, String value) {
        if (putsBeforeStopping == 0) {
            throw new ProcessStopped();
        }
        putsBeforeStopping--;
        puts++;
        values.put(key, value);
    }

    @Override
    protected @Nullable String getSpi(String key) {
        return values.get(key);
    }

    @Override
    protected void removeSpi(String key) {
        values.remove(key);
    }

    @Override
    protected void removeNodeSpi() {
        values.clear();
    }

    @Override
    protected String[] keysSpi() {
        return values.keySet().toArray(String[]::new);
    }

    @Override
    protected String[] childrenNamesSpi() {
        return new String[0];
    }

    @Override
    protected AbstractPreferences childSpi(String name) {
        throw new UnsupportedOperationException("子ノードは使わない: " + name);
    }

    @Override
    protected void syncSpi() {
        // 保存先はこのオブジェクトそのもの。
    }

    @Override
    protected void flushSpi() {
        // 保存先はこのオブジェクトそのもの。
    }

    /** 書き込みの途中でプロセスが止まったこと。 */
    static final class ProcessStopped extends RuntimeException {
        private static final long serialVersionUID = 1L;

        ProcessStopped() {
            super("書き込みの途中で止まった");
        }
    }
}
