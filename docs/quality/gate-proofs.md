# ゲート発火の証明

> Status: 記録 / 最終実測 2026-09-03
> 根拠となる規則: QLT-007（カスタムゲートには negative proof が要る）

**検査は「落ちること」を見るまで信用しない。** 各ゲートについて、最小の違反を仕込んだ状態で
意図した規則 ID によって失敗すること、そして元に戻すと `./gradlew check` が緑に戻ることを実測する。
ゲートを変えたら、この記録も同じ変更で更新する。

環境: Ubuntu 22.04（WSL2）/ OpenJDK 21.0.12 / Gradle 9.7.1
CI: ubuntu-24.04 / Temurin 21 / 同梱 Wrapper

---

## 1. 実測結果

| # | 規則 | 仕込んだ違反 | 実行したタスク | 結果 |
| --- | --- | --- | --- | --- |
| P1 | ARC-007 | `:core:application` で `LocalDateTime.now()` を呼ぶ | `:core:application:forbiddenApisMain` | **失敗** |
| P2 | ARC-003 | `:core:domain` から `javax.swing.JLabel` を参照する | `:quality:architecture-tests:test` | **失敗** |
| P3 | CNF-003 | `switch` に `default ->` を書く | `validateConformance` | **失敗** |
| P4 | CNF-010 | `:ui:swing` から `:adapters:preferences` へ依存を足す | `validateConformance` | **失敗** |
| P5 | QLT-002 | 生の `java.util.List` を使う（`[rawtypes]` 警告） | `:core:domain:compileJava` | **失敗** |
| P6 | CNF-002 | waiver 無しの `@SuppressWarnings` を書く | `validateConformance` | **失敗** |
| P7 | CNF-009 | 期限切れ（2026-01-01）の waiver を置く | `validateConformance` | **失敗** |
| P8 | QLT-004 | 整形を崩す | `:core:domain:spotlessCheck` | **失敗** |
| P9 | CNF-001 | 入れ子クラスを `TimeHelper` と命名する | `validateConformance` | **失敗** |
| P10 | CNF-004 | `render*` 以外のメソッドで `setEnabled(...)` を呼ぶ | `validateConformance` | **失敗** |
| P11 | JAV-009 | 未使用の import を残す | `:core:domain:checkstyleMain` | **失敗** |

**復帰の確認**: 11 件すべてについて、仕込みを戻したあと `./gradlew check` が終了コード 0 で成功した。

---

## 2. 出力の抜粋

```text
P1  Forbidden method invocation: java.time.LocalDateTime#now()
      [現在時刻は WallClockPort / TickSource からのみ得る。ここで読むと決定性が壊れる（ARC-007）]

P2  Architecture Violation — Rule 'no classes that reside in any package
      ['...domain..', '...application..'] should depend on classes that reside in any package
      ['javax.swing..', 'java.awt..'], because ARC-003: 中核は Swing を知らない'

P3  CNF-003 core/application/.../ClockFaceQuery.java:43
      — switch に default を書かない。網羅性検査を無効化する

P4  CNF-010 docs/PROJECT_LAYOUT.md
      — 許可されていない依存: :ui:swing -> :adapters:preferences

P5  SettingsSchemaVersion.java:20: warning: [rawtypes] found raw type: List
    error: warnings found and -Werror specified

P6  CNF-002 core/domain/.../SettingsSchemaVersion.java:20
      — @SuppressWarnings の直前行に // Waiver: WVR-NNNN が必要

P7  CNF-009 docs/waivers/WVR-0001-expired-proof.md:6
      — 期限切れの waiver（2026-01-01）。コードを直すか ADR にする

P8  The following files had format violations: ... Run './gradlew spotlessApply' to fix all violations.

P9  CNF-001 core/domain/.../SettingsSchemaVersion.java:20
      — 禁止された総称型名: TimeHelper（役割を名前で語る）

P10 CNF-004 ui/swing/.../MainFrame.java:34
      — setEnabled( を MainFrame で呼んでいる。UI 状態の反映は render* からのみ

P11 [ERROR] core/domain/.../SettingsSchemaVersion.java:3:8:
      Unused import - java.util.List. [UnusedImports]
```

---

## 3. 証明の途中で分かったこと

🔑 **最初の P5 は「未使用 import を残す」で試したが、ビルドは通ってしまった。**
`javac -Xlint:all` に未使用 import の検査は無い（`-Xlint` のカテゴリに存在しない）。
つまり **QLT-002（警告は失敗する）は未使用 import を守っていない**。
守っているのは Checkstyle の `UnusedImports` であり、それが P11 である。

これは「ゲートがあると思っていた場所に無い」典型例であり、
negative proof を取らなければ気づかないまま
「コンパイラが見ている」と書き続けていた。**QLT-007 が要る理由そのものである。**

---

## 4. 規則単位のテスト（`validateConformance` の内部）

上の end-to-end の証明とは別に、`build-logic` の単体テストが各 CNF 規則について
正例（通る入力）と反例（落ちる入力）の両方を持つ。

```bash
./gradlew -p build-logic test
```

| テストクラス | 対象 |
| --- | --- |
| `JavaSourceRulesTest` | CNF-001 / CNF-002 / CNF-003 / CNF-004 / CNF-006 / CNF-007 |
| `DocumentationRulesTest` | CNF-005 |
| `WaiverLedgerTest` | CNF-009 |
| `BaselineRulesTest` | CNF-008 |
| `ModuleGraphRulesTest` | CNF-010 |

`JavaSourceRulesTest` は「文字列リテラル中の `default ->` を誤検知しないこと」のような
**偽陽性側**の反例も持つ。検査が正しく落ちることと、正しく落ちないことの両方を見る。

---

## 5. クリーンな作業木での証明（fresh clone）

**手元の作業木が緑であることは、クローンした人の手元が緑であることの証拠ではない。**
ブートストラップの各コミットについて、`git worktree add` で切り出した作業木で
`./gradlew check` を実行した。

| コミット | 内容 | 結果 |
| --- | --- | --- |
| #3 | ビルド骨格と品質ゲート | 成功（31 タスク） |
| #4 | 時計表示と設定の永続化 | 成功（71 タスク） |
| #5 | ArchUnit のレイヤ規則 | 成功（76 タスク） |

🔑 **この検証で、手元では出ない壊れ方を 2 件見つけた。**

1. `neneclock.conformance` を Kotlin の precompiled script plugin として書いていた。
   precompiled script は同じソースセットの Java クラスを解決できず、fresh clone では
   `Unresolved reference 'io'` で落ちた。手元では過去のビルド成果物が残っていたため通っていた。
   → Java の `Plugin<Project>` 実装に置き換えた。
2. `.gitignore` の `build/` が、パッケージ名 `io.github.hideyukimori.neneclock.build.conformance`
   の `build` セグメントに一致し、**規約検査エンジンのソースが丸ごとコミットから抜けていた**。
   → ignore パターンをアンカーし（`/build/` `/*/build/` `/*/*/build/`）、
   パッケージを `...neneclock.gradle.conformance` へ改名した。

どちらも「動いているように見えるゲートが、実は配布物に入っていない」種類の事故である。

---

## 6. CI と main 保護の証明

| # | 対象 | 実測 |
| --- | --- | --- |
| P12 | `quality` ジョブ | PR #12 で成功（2 分 5 秒）。CI は `./gradlew check` のみを実行する |
| P13 | `main` への直接 push | **拒否**。`remote rejected ... push declined due to repository rule violations` / `Required status check "quality" is expected.` |

ruleset の内容は GitHub API で読み戻して確認した。

```json
{
  "name": "main-protection",
  "enforcement": "active",
  "rules": ["deletion", "non_fast_forward", "pull_request", "required_status_checks"],
  "checks": ["quality"],
  "merge": ["squash"]
}
```

---

## 7. まだ証明していないもの

🔴 **ここに書いていないものは、証明されていない。**

| 対象 | 状態 |
| --- | --- |
| WSLg での表示（`./gradlew run`） | **未確認**。この作業環境からは X サーバへ到達できない（実測: `java.awt.AWTError: Can't connect to X11 window server using ':0'`。`/tmp/.X11-unix` が存在しない）。単体テストが headless で通っていることは表示の証拠ではない（QLT-012） |
| SHA-256 dependency verification | **未導入**（QLT-011 の planned 部分） |
| `planned` と書いた規則の強制 | 未実装であることを強制マトリクスに明記している。実装したときに状態を書き換える |
