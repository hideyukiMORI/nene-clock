plugins {
    id("neneclock.conformance")
}

// ルートは合成と検査のみを持つ。production コードを置かない。
description = "NeNe Clock — Java 21 / Swing デスクトップ時計"

// QLT-001 / QLT-007: 検査そのもののテストも唯一のゲートに入れる。
// build-logic は included build なので、明示的に依存させないと `check` から呼ばれない。
// 呼ばれていなかった間、規約検査の単体テストは 1 件落ちたまま気づかれなかった（Issue #26）。
tasks.named("check") {
    dependsOn(gradle.includedBuild("build-logic").task(":test"))
}
