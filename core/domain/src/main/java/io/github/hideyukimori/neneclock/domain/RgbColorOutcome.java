package io.github.hideyukimori.neneclock.domain;

import java.util.Objects;

/**
 * {@link RgbColor} 生成の結果。
 *
 * <p>期待される失敗を例外ではなく型で表す（JAV-005）。
 */
public sealed interface RgbColorOutcome {

    /** 検証を通った色。 */
    record Accepted(RgbColor value) implements RgbColorOutcome {
        public Accepted {
            Objects.requireNonNull(value, "value");
        }
    }

    /** 検証で拒否された。理由は閉じた集合で示す。 */
    record Rejected(RgbColorRejection reason) implements RgbColorOutcome {
        public Rejected {
            Objects.requireNonNull(reason, "reason");
        }
    }
}
