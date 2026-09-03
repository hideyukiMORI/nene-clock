package io.github.hideyukimori.neneclock.domain;

import java.util.Objects;

/**
 * {@link FontColor} 生成の結果。
 *
 * <p>期待される失敗を例外ではなく型で表す（JAV-005）。
 */
public sealed interface FontColorOutcome {

    /** 検証を通った色。 */
    record Accepted(FontColor value) implements FontColorOutcome {
        public Accepted {
            Objects.requireNonNull(value, "value");
        }
    }

    /** 検証で拒否された。理由は閉じた集合で示す。 */
    record Rejected(FontColorRejection reason) implements FontColorOutcome {
        public Rejected {
            Objects.requireNonNull(reason, "reason");
        }
    }
}
