package io.github.hideyukimori.neneclock.domain;

import java.util.Objects;

/**
 * 製品の名乗り（版と作者）。設定モーダルのフッターに出す。
 *
 * <p>版の正本はビルド（{@code gradle.properties} の {@code version}）にあり、ここは運ばれてきた値を
 * 持つだけである。検証済みの値の組なので {@code record} でよい（JAV-007）。
 * 年は持たない。現在時刻を読めない（ARC-007）ので、著作権表示の年を自動で作る経路が無い。
 *
 * @param version 版。空でない
 * @param author 作者。空でない
 */
public record ProductIdentity(String version, String author) {

    public ProductIdentity {
        Objects.requireNonNull(version, "version");
        Objects.requireNonNull(author, "author");
        if (version.isBlank() || author.isBlank()) {
            throw new IllegalArgumentException("version and author must not be blank");
        }
    }
}
