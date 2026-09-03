package io.github.hideyukimori.neneclock.domain;

import java.util.Objects;

/**
 * {@link FontSize} 生成の結果。
 *
 * <p>期待される失敗を例外ではなく型で表す（JAV-005）。呼び出し側は switch で網羅する。
 */
public sealed interface FontSizeOutcome {

    /** 検証を通った値。 */
    record Accepted(FontSize value) implements FontSizeOutcome {
        public Accepted {
            Objects.requireNonNull(value, "value");
        }
    }

    /** 検証で拒否された。理由は閉じた集合で示す。 */
    record Rejected(FontSizeRejection reason) implements FontSizeOutcome {
        public Rejected {
            Objects.requireNonNull(reason, "reason");
        }
    }
}
