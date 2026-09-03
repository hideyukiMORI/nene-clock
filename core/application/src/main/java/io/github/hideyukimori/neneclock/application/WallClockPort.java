package io.github.hideyukimori.neneclock.application;

import java.time.LocalDateTime;

/**
 * 壁時計の唯一の窓口（ARC-007）。
 *
 * <p>production の実装は {@code :adapters:system-time} にただ 1 つ。テストは固定値を返す実装を渡す。
 * これ以外の経路で現在時刻を読むことは forbidden-apis が拒否する。
 */
@FunctionalInterface
public interface WallClockPort {

    /** 現在のローカル日時。 */
    LocalDateTime currentDateTime();
}
