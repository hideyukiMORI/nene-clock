package io.github.hideyukimori.neneclock.domain;

import java.util.Objects;

/**
 * {@link WindowPadding} 生成の結果。
 *
 * <p>期待される失敗を例外ではなく型で表す（JAV-005）。呼び出し側は switch で網羅する。
 */
public sealed interface WindowPaddingOutcome {

    /** 検証を通った値。 */
    record Accepted(WindowPadding value) implements WindowPaddingOutcome {
        public Accepted {
            Objects.requireNonNull(value, "value");
        }
    }

    /** 検証で拒否された。理由は閉じた集合で示す。 */
    record Rejected(WindowPaddingRejection reason) implements WindowPaddingOutcome {
        public Rejected {
            Objects.requireNonNull(reason, "reason");
        }
    }
}
