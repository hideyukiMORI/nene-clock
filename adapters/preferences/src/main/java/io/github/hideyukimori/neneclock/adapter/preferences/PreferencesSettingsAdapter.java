package io.github.hideyukimori.neneclock.adapter.preferences;

import io.github.hideyukimori.neneclock.application.SettingsLoadFailure;
import io.github.hideyukimori.neneclock.application.SettingsLoadOutcome;
import io.github.hideyukimori.neneclock.application.SettingsSaveFailure;
import io.github.hideyukimori.neneclock.application.SettingsSaveOutcome;
import io.github.hideyukimori.neneclock.application.SettingsStorePort;
import io.github.hideyukimori.neneclock.domain.ClockFormat;
import io.github.hideyukimori.neneclock.domain.DateVisibility;
import io.github.hideyukimori.neneclock.domain.FontColor;
import io.github.hideyukimori.neneclock.domain.FontColorOutcome;
import io.github.hideyukimori.neneclock.domain.FontFamily;
import io.github.hideyukimori.neneclock.domain.FontFamilyOutcome;
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

/**
 * {@link Preferences} に設定を保存する {@link SettingsStorePort} の実装。
 *
 * <p>保存形式を知るのはこのクラスだけである。版の移行もここに閉じる（ADR 0003）。
 * {@link #load()} は保存領域を書き換えない。v2 として書き戻されるのは次の保存のときである。
 */
public final class PreferencesSettingsAdapter implements SettingsStorePort {

    private static final String KEY_SCHEMA = "schemaVersion";
    private static final String KEY_CLOCK_FORMAT = "clockFormat";
    private static final String KEY_SECONDS = "secondsVisibility";
    private static final String KEY_DATE = "dateVisibility";
    private static final String KEY_TOPMOST = "windowTopmost";
    private static final String KEY_FONT_POINTS = "fontPoints";
    private static final String KEY_FONT_FAMILY = "fontFamily";
    private static final String KEY_FONT_RED = "fontRed";
    private static final String KEY_FONT_GREEN = "fontGreen";
    private static final String KEY_FONT_BLUE = "fontBlue";

    private static final int SCHEMA_ABSENT = 0;
    private static final int INTEGER_ABSENT = -1;

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
        if (storedSchema < SettingsSchemaVersion.EARLIEST_MIGRATABLE.value()) {
            return new SettingsLoadOutcome.Defaulted(SettingsLoadFailure.UNSUPPORTED_SCHEMA);
        }
        SettingsSchemaVersion stored = new SettingsSchemaVersion(storedSchema);
        if (!stored.isMigratable()) {
            return new SettingsLoadOutcome.Defaulted(SettingsLoadFailure.UNSUPPORTED_SCHEMA);
        }
        return restore(stored);
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
        node.put(KEY_FONT_FAMILY, settings.fontFamily().name());
        node.putInt(KEY_FONT_RED, settings.fontColor().red());
        node.putInt(KEY_FONT_GREEN, settings.fontColor().green());
        node.putInt(KEY_FONT_BLUE, settings.fontColor().blue());
        try {
            node.flush();
        } catch (BackingStoreException failure) {
            return new SettingsSaveOutcome.Failed(SettingsSaveFailure.UNWRITABLE);
        }
        return new SettingsSaveOutcome.Saved();
    }

    /**
     * 版に応じて読む。
     *
     * <p>v1 は書体と文字色を持たない。欠けている分は domain の既定値で埋める。推測はしない。
     */
    private SettingsLoadOutcome restore(SettingsSchemaVersion stored) {
        UserSettings carried = readSettingsSharedByEveryVersion();
        if (carried == null) {
            return invalidValue();
        }
        if (!stored.isSupported()) {
            return new SettingsLoadOutcome.Restored(carried);
        }
        FontFamily family = readFontFamily();
        if (family == null) {
            return invalidValue();
        }
        FontColor color = readFontColor();
        if (color == null) {
            return invalidValue();
        }
        return new SettingsLoadOutcome.Restored(new UserSettings(
                carried.clockFormat(),
                carried.secondsVisibility(),
                carried.dateVisibility(),
                carried.windowTopmost(),
                family,
                carried.fontSize(),
                color));
    }

    /** v1 から変わっていない 5 項目。書体と文字色は既定値のまま返す。 */
    private @Nullable UserSettings readSettingsSharedByEveryVersion() {
        ClockFormat clockFormat = lookup(ClockFormat.values(), node.get(KEY_CLOCK_FORMAT, null));
        SecondsVisibility seconds = lookup(SecondsVisibility.values(), node.get(KEY_SECONDS, null));
        DateVisibility date = lookup(DateVisibility.values(), node.get(KEY_DATE, null));
        WindowTopmost topmost = lookup(WindowTopmost.values(), node.get(KEY_TOPMOST, null));
        FontSize fontSize = readFontSize();
        if (clockFormat == null || seconds == null) {
            return null;
        }
        if (date == null || topmost == null || fontSize == null) {
            return null;
        }
        return new UserSettings(clockFormat, seconds, date, topmost, FontFamily.DEFAULT, fontSize, FontColor.DEFAULT);
    }

    private static SettingsLoadOutcome invalidValue() {
        return new SettingsLoadOutcome.Defaulted(SettingsLoadFailure.INVALID_VALUE);
    }

    private @Nullable FontSize readFontSize() {
        return switch (FontSize.of(node.getInt(KEY_FONT_POINTS, INTEGER_ABSENT))) {
            case FontSizeOutcome.Accepted accepted -> accepted.value();
            case FontSizeOutcome.Rejected outOfRange -> null;
        };
    }

    private @Nullable FontFamily readFontFamily() {
        String stored = node.get(KEY_FONT_FAMILY, null);
        if (stored == null) {
            return null;
        }
        return switch (FontFamily.of(stored)) {
            case FontFamilyOutcome.Accepted accepted -> accepted.value();
            case FontFamilyOutcome.Rejected rejected -> null;
        };
    }

    private @Nullable FontColor readFontColor() {
        return switch (FontColor.of(
                node.getInt(KEY_FONT_RED, INTEGER_ABSENT),
                node.getInt(KEY_FONT_GREEN, INTEGER_ABSENT),
                node.getInt(KEY_FONT_BLUE, INTEGER_ABSENT))) {
            case FontColorOutcome.Accepted accepted -> accepted.value();
            case FontColorOutcome.Rejected outOfRange -> null;
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
