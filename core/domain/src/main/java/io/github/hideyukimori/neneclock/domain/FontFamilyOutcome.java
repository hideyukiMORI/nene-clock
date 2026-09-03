package io.github.hideyukimori.neneclock.domain;

import java.util.Objects;

/**
 * {@link FontFamily} 生成の結果。
 *
 * <p>期待される失敗を例外ではなく型で表す（JAV-005）。
 */
public sealed interface FontFamilyOutcome {

    /** 検証を通った書体名。 */
    record Accepted(FontFamily value) implements FontFamilyOutcome {
        public Accepted {
            Objects.requireNonNull(value, "value");
        }
    }

    /** 検証で拒否された。理由は閉じた集合で示す。 */
    record Rejected(FontFamilyRejection reason) implements FontFamilyOutcome {
        public Rejected {
            Objects.requireNonNull(reason, "reason");
        }
    }
}
