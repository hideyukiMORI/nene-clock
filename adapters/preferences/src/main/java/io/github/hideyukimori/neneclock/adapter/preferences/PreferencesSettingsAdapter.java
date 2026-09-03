package io.github.hideyukimori.neneclock.adapter.preferences;

import io.github.hideyukimori.neneclock.application.SettingsLoadFailure;
import io.github.hideyukimori.neneclock.application.SettingsLoadOutcome;
import io.github.hideyukimori.neneclock.application.SettingsSaveFailure;
import io.github.hideyukimori.neneclock.application.SettingsSaveOutcome;
import io.github.hideyukimori.neneclock.application.SettingsStorePort;
import io.github.hideyukimori.neneclock.domain.ClockFormat;
import io.github.hideyukimori.neneclock.domain.DateVisibility;
import io.github.hideyukimori.neneclock.domain.FontSize;
import io.github.hideyukimori.neneclock.domain.FontSizeOutcome;
import io.github.hideyukimori.neneclock.domain.SecondsVisibility;
import io.github.hideyukimori.neneclock.domain.SettingsSchemaVersion;
import io.github.hideyukimori.neneclock.domain.UserSettings;
import io.github.hideyukimori.neneclock.domain.WindowTopmost;
import java.util.Objects;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.jspecify.annotations.Nullable;

/** {@link Preferences} に設定を保存する {@link SettingsStorePort} の実装。 */
public final class PreferencesSettingsAdapter implements SettingsStorePort {

    private static final String KEY_SCHEMA = "schemaVersion";
    private static final String KEY_CLOCK_FORMAT = "clockFormat";
    private static final String KEY_SECONDS = "secondsVisibility";
    private static final String KEY_DATE = "dateVisibility";
    private static final String KEY_TOPMOST = "windowTopmost";
    private static final String KEY_FONT_POINTS = "fontPoints";

    private static final int SCHEMA_ABSENT = 0;
    private static final int FONT_POINTS_ABSENT = -1;

    private final Preferences node;

    private PreferencesSettingsAdapter(Preferences node) {
        this.node = Objects.requireNonNull(node, "node");
    }

    /** production の合成ルートが使う生成経路。 */
    public static PreferencesSettingsAdapter userScoped() {
        return new PreferencesSettingsAdapter(Preferences.userNodeForPackage(PreferencesSettingsAdapter.class));
    }

    /** 任意のノードで組み立てる。テストが専用ノードを渡すための経路。 */
    public static PreferencesSettingsAdapter at(Preferences node) {
        return new PreferencesSettingsAdapter(node);
    }

    @Override
    public SettingsLoadOutcome load() {
        int storedSchema = node.getInt(KEY_SCHEMA, SCHEMA_ABSENT);
        if (storedSchema == SCHEMA_ABSENT) {
            return new SettingsLoadOutcome.Defaulted(SettingsLoadFailure.ABSENT);
        }
        if (storedSchema < 1 || !new SettingsSchemaVersion(storedSchema).isSupported()) {
            return new SettingsLoadOutcome.Defaulted(SettingsLoadFailure.UNSUPPORTED_SCHEMA);
        }
        return restore();
    }

    @Override
    public SettingsSaveOutcome save(UserSettings settings) {
        Objects.requireNonNull(settings, "settings");
        node.putInt(KEY_SCHEMA, SettingsSchemaVersion.CURRENT.value());
        node.put(KEY_CLOCK_FORMAT, settings.clockFormat().name());
        node.put(KEY_SECONDS, settings.secondsVisibility().name());
        node.put(KEY_DATE, settings.dateVisibility().name());
        node.put(KEY_TOPMOST, settings.windowTopmost().name());
        node.putInt(KEY_FONT_POINTS, settings.fontSize().points());
        try {
            node.flush();
        } catch (BackingStoreException failure) {
            return new SettingsSaveOutcome.Failed(SettingsSaveFailure.UNWRITABLE);
        }
        return new SettingsSaveOutcome.Saved();
    }

    private SettingsLoadOutcome restore() {
        ClockFormat clockFormat = lookup(ClockFormat.values(), node.get(KEY_CLOCK_FORMAT, null));
        if (clockFormat == null) {
            return invalidValue();
        }
        SecondsVisibility seconds = lookup(SecondsVisibility.values(), node.get(KEY_SECONDS, null));
        if (seconds == null) {
            return invalidValue();
        }
        DateVisibility date = lookup(DateVisibility.values(), node.get(KEY_DATE, null));
        if (date == null) {
            return invalidValue();
        }
        WindowTopmost topmost = lookup(WindowTopmost.values(), node.get(KEY_TOPMOST, null));
        if (topmost == null) {
            return invalidValue();
        }
        FontSize fontSize = readFontSize();
        if (fontSize == null) {
            return invalidValue();
        }
        return new SettingsLoadOutcome.Restored(new UserSettings(clockFormat, seconds, date, topmost, fontSize));
    }

    private static SettingsLoadOutcome invalidValue() {
        return new SettingsLoadOutcome.Defaulted(SettingsLoadFailure.INVALID_VALUE);
    }

    private @Nullable FontSize readFontSize() {
        int points = node.getInt(KEY_FONT_POINTS, FONT_POINTS_ABSENT);
        return switch (FontSize.of(points)) {
            case FontSizeOutcome.Accepted accepted -> accepted.value();
            case FontSizeOutcome.Rejected outOfRange -> null;
        };
    }

    private static <T extends Enum<T>> @Nullable T lookup(T[] candidates, @Nullable String stored) {
        for (T candidate : candidates) {
            if (candidate.name().equals(stored)) {
                return candidate;
            }
        }
        return null;
    }
}
