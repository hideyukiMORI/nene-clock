package io.github.hideyukimori.neneclock.application;

import java.util.List;
import java.util.Objects;

/**
 * いまの設定で起こりうる、最も大きい面を測るための候補（FR-049）。
 *
 * <p>🔑 **幅を測るのは書体を知っている UI の仕事だが、「何を測れば最大になるか」を決めるのは
 * 文字列の組み立てを知っているこの層である**（ARC-001 / ARC-012）。UI がここで
 * {@code "00:00:00 AM"} のような文字列を自前で持つと、整形の知識が 2 か所になる。
 *
 * <p>候補が複数あるのは、最大が 1 つの文字列では決まらないからである。
 * 数字の字幅が揃っていない書体では最も広い数字が分からず、12 時間表記では AM と PM の
 * どちらが広いかも分からない。**起こりうる形をすべて渡し、UI が測って最大を取る。**
 * こうすれば毎秒・毎分で幅が変わらない（ADR 0008 / ADR 0017）。
 */
public record ClockFaceExtent(List<String> timeCandidates, DateExtent date) {

    public ClockFaceExtent {
        timeCandidates = List.copyOf(timeCandidates);
        Objects.requireNonNull(date, "date");
    }
}
