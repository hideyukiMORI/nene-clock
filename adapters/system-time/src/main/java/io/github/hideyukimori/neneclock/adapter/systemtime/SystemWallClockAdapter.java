package io.github.hideyukimori.neneclock.adapter.systemtime;

import io.github.hideyukimori.neneclock.application.WallClockPort;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

/**
 * システム時計から現在時刻を読む {@link WallClockPort} の実装。
 *
 * <p>アプリ全体で「実時刻を読む」のはこのクラスだけである。
 */
public final class SystemWallClockAdapter implements WallClockPort {

    private final Clock clock;

    private SystemWallClockAdapter(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /**
     * production の合成ルートが使う生成経路。
     *
     * <p>タイムゾーンの既定値を読むのもこの区画に閉じる。合成ルートへ漏らすと、
     * 「環境を読む場所」が 2 つになる（ARC-007）。
     */
    public static SystemWallClockAdapter system() {
        return new SystemWallClockAdapter(Clock.system(ZoneId.systemDefault()));
    }

    /** 任意の {@link Clock} で組み立てる。テストが固定時計を渡すための経路。 */
    public static SystemWallClockAdapter using(Clock clock) {
        return new SystemWallClockAdapter(clock);
    }

    @Override
    public LocalDateTime currentDateTime() {
        return LocalDateTime.now(clock);
    }
}
