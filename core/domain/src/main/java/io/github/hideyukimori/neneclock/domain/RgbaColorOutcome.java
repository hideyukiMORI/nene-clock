package io.github.hideyukimori.neneclock.domain;

import java.util.Objects;

/**
 * {@link RgbaColor} 生成の結果。
 *
 * <p>期待される失敗を例外ではなく型で表す（JAV-005）。
 */
public sealed interface RgbaColorOutcome {

    /** 検証を通った色。 */
    record Accepted(RgbaColor value) implements RgbaColorOutcome {
        public Accepted {
            Objects.requireNonNull(value, "value");
        }
    }

    /** 検証で拒否された。理由は閉じた集合で示す。 */
    record Rejected(RgbaColorRejection reason) implements RgbaColorOutcome {
        public Rejected {
            Objects.requireNonNull(reason, "reason");
        }
    }
}
