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

最終実測 2026-09-03（Issue #26 の修正後に全件を取り直した）。

| # | 規則 | 仕込んだ違反 | 実行したタスク | 結果 |
| --- | --- | --- | --- | --- |
| P1 | ARC-007 | `:core:application` で `System.nanoTime()` と `Instant.now()` を呼ぶ | `:core:application:forbiddenApisMain` | **失敗** |
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
| P12 | CNF-011 | どこからも読み込まれない設定ファイルを `config/` に置く | `validateConformance` | **失敗** |
| P13 | ARC-007 | `:ui:swing` で `GraphicsEnvironment.getLocalGraphicsEnvironment()` を呼ぶ | `:ui:swing:forbiddenApisMain` | **失敗** |
| P14 | ARC-006 | `:ui:swing` で `System.err.println(...)` を呼ぶ | `:ui:swing:forbiddenApisMain` / `:quality:architecture-tests:test` | **失敗**（2 層とも） |

**復帰の確認**: 14 件すべてについて、仕込みを戻したあと `./gradlew check` が終了コード 0 で成功した。

**除外側の確認**: 例外区画が 2 つある。どちらも「禁止が効いていること」と
「唯一の窓口が通ること」の両方を見ている。

| 区画 | 適用しない署名 | 呼んでいる禁止 API | 結果 |
| --- | --- | --- | --- |
| `:adapters:system-time` | `determinism.txt` | `LocalDateTime.now(Clock)` / `Clock.system(ZoneId)` / `ZoneId.systemDefault()` | `check` 成功 |
| `:adapters:font-catalog` | `platform.txt` | `GraphicsEnvironment.getLocalGraphicsEnvironment()` | `check` 成功 |
| `:app` | `process-control.txt` ＋ bundled `jdk-system-out` | `System.err.println` / `System.exit` | `check` 成功 |

`:adapters:font-catalog` は `determinism.txt` を適用したままなので、書体は読めても時計は読めない。

---

## 2. 出力の抜粋（実行結果からの引用）

```text
P1  Forbidden method invocation: java.lang.System#nanoTime() [現在時刻は WallClockPort / TickSource からのみ得る。ここで読むと決定性が壊れる（ARC-007）]
    Forbidden method invocation: java.time.Instant#now() [現在時刻は WallClockPort / TickSource からのみ得る。ここで読むと決定性が壊れる（ARC-007）]
    Scanned 17 class file(s) for forbidden API invocations (in 0.02s), 2 error(s).

P2  java.lang.AssertionError: Architecture Violation [Priority: MEDIUM] - Rule 'no classes that
    reside in any package ['io.github.hideyukimori.neneclock.domain..',
    'io.github.hideyukimori.neneclock.application..'] should depend on classes that reside in any
    package ['javax.swing..', 'java.awt..'], because ARC-003: 中核は Swing を知らない' was violated (1 times):
    Method <io.github.hideyukimori.neneclock.domain.SettingsSchemaVersion.toolkit()> references
    class object <javax.swing.JLabel> in (SettingsSchemaVersion.java:21)

P3  CNF-003 core/application/src/main/java/io/github/hideyukimori/neneclock/application/ClockFaceQuery.java:43
      — switch に default を書かない。網羅性検査を無効化する

P4  CNF-010 docs/PROJECT_LAYOUT.md — 許可されていない依存: :ui:swing -> :adapters:preferences

P5  core/domain/src/main/java/io/github/hideyukimori/neneclock/domain/SettingsSchemaVersion.java:20:
      warning: [rawtypes] found raw type: List
    error: warnings found and -Werror specified

P6  CNF-002 core/domain/src/main/java/io/github/hideyukimori/neneclock/domain/SettingsSchemaVersion.java:20
      — @SuppressWarnings の直前行に // Waiver: WVR-NNNN が必要

P7  CNF-009 docs/waivers/WVR-0001-expired-proof.md:6
      — 期限切れの waiver（2026-01-01）。コードを直すか ADR にする

P8  The following files had format violations: ... Run './gradlew spotlessApply' to fix all violations.

P9  CNF-001 core/domain/src/main/java/io/github/hideyukimori/neneclock/domain/SettingsSchemaVersion.java:20
      — 禁止された総称型名: TimeHelper（役割を名前で語る）

P10 CNF-004 ui/swing/src/main/java/io/github/hideyukimori/neneclock/ui/swing/MainFrame.java:34
      — setEnabled( を MainFrame で呼んでいる。UI 状態の反映は render* からのみ

P11 [ant:checkstyle] [ERROR] core/domain/.../SettingsSchemaVersion.java:3:8:
      Unused import - java.util.List. [UnusedImports]

P12 CNF-011 config/forbiddenapis/orphan.txt
      — この設定ファイルを読み込むビルドスクリプトが無い。置いても効かない

P13 Forbidden method invocation: java.awt.GraphicsEnvironment#getLocalGraphicsEnvironment()
      [実行環境そのものの情報（利用可能な書体など）は :adapters:font-catalog からのみ読む（ARC-007）]

P14 Forbidden field access: java.lang.System#err
      [prints to System.err; should only be used for debugging, not in production code]
    java.lang.AssertionError: Architecture Violation [Priority: MEDIUM] - Rule 'no classes that
    reside outside of package 'io.github.hideyukimori.neneclock.app..' should access standard
    streams, because ARC-006: 端末へ出せるのは合成ルートだけ（ADR 0005）' was violated (1 times):
    Method <io.github.hideyukimori.neneclock.ui.swing.ClockTicker.start()> gets field
    <java.lang.System.err> in (ClockTicker.java:25)
```

---

## 3. 証明の途中で分かったこと

### 3.1 QLT-002 の担当を取り違えていた

最初の P5 は「未使用 import を残す」で試したが、ビルドは通ってしまった。
`javac -Xlint:all` に未使用 import の検査は無い（`-Xlint` のカテゴリに存在しない）。
守っているのは Checkstyle の `UnusedImports` であり、それが P11 である。

### 3.2 🔴 ARC-007 のゲートは、実は半分しか繋がっていなかった（Issue #26）

**最初にこの文書を書いた時点で、`config/forbiddenapis/determinism.txt` は
どのビルドスクリプトからも読み込まれていなかった。** `neneclock.java-conventions` の
`signaturesFiles` が `base.txt` だけを指していたためである。

それでも P1 が「落ちた」のは、bundled signature の `jdk-unsafe` が
**既定タイムゾーンを使うメソッド**を禁じており、最初の証明に使った `LocalDateTime.now()` が
たまたまそれに当たったからだった。実際には次が素通りしていた。

```java
long probe = System.nanoTime() + java.time.Instant.now().toEpochMilli();   // 修正前は通った
```

さらに `:adapters:system-time/build.gradle.kts` の「このモジュールだけ determinism.txt を外す」
という上書きも、外す対象が最初から入っていないため **no-op** だった。

🔴 **加えて、この文書の初版に載せた P1 の出力抜粋は、実際の出力ではなく期待した内容だった。**
証明の記録として誤りであり、`docs/QUALITY_GATES.md` の ARC-007 の `active` も過大な主張だった。
Issue #26 で署名を実際に読み込ませ、**全 11 件を実行結果からの引用で取り直した**のが第 2 節である。

**教訓**: negative proof は「落ちること」だけでなく **「何によって落ちたか」**まで確かめないと、
別の道具がたまたま拾っているだけの状態を「このゲートが効いている」と誤読する。
出力は必ず実行結果から引用する。

**再発防止**: CNF-011 を新設した。`config/` に置いた設定が、どのビルドスクリプトからも
読み込まれていなければ `validateConformance` が落ちる。

### 3.3 🔴 規約検査そのもののテストが、ゲートから呼ばれていなかった（Issue #26）

`build-logic` は included build なので、**そのタスクはルートの `check` からは自動で呼ばれない**。
「各 CNF 規則に正例・反例の単体テストがある」と書いていたが、`./gradlew check` も CI も
そのテストを一度も実行していなかった。

初めて `./gradlew -p build-logic test` を回したところ、**2 件が落ちた**。どちらも検査側の欠陥である。

1. CNF-002 が waiver コメントを見つけられなかった。「直前行」を探すのに**コード行**だけを
   覚えていたため、`// Waiver: WVR-NNNN` というコメント行は候補にならなかった。
   つまり **waiver を正しく書いても通らない**状態だった
2. `@SuppressWarnings("all")` を検出できていなかった。`CodeText` が文字列リテラルの中身を
   空白へ潰すため、潰したあとの文字列から `"all"` を探していた

🔑 1 の欠陥は、テストの側でも見えていなかった。waiver 付きの抑制が
「waiver が無い」という**別の理由**で拒否されており、テストは規則 ID だけを見ていたので通っていた。
**正しい理由で落ちているかまで見ないと、テストも証明にならない。**

対策として `check` に `:build-logic:test` を依存させた。

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

## 7. 表示が無い環境での起動（Issue #30）

表示が無い状態で `./gradlew run` を実行したときの実測。

修正前:

```text
Exception in thread "AWT-EventQueue-0" java.awt.HeadlessException: ...（20 行のスタックトレース）
BUILD SUCCESSFUL in 2s
```

🔴 **失敗しているのに終了コード 0**。例外が EDT 上で起きるため `main` が知らずに正常終了していた。

修正後（実測）:

```text
NeNe Clock needs a graphical display, but none is available.
On WSL: set guiApplications=true in .wslconfig, then run 'wsl --shutdown' on Windows.

FAILURE: Build failed with an exception.
> Process 'command '.../java'' finished with non-zero exit value 1
```

`grep -c "\tat "` は **0**（スタックトレースなし）。`./gradlew run` の終了コードは **1**。

---

## 9. 同梱書体（Issue #34 / ADR 0006）

### 9.1 `platform.txt` の適用除外が本当に無くなったか

例外区画を畳んだと**書いた**だけでは証明にならない。`:adapters:font-catalog` に
`GraphicsEnvironment` の呼び出しを一時的に足して、落ちることを確かめた（実測）。

```text
> Task :adapters:font-catalog:forbiddenApisMain FAILED
Forbidden method invocation: java.awt.GraphicsEnvironment#getLocalGraphicsEnvironment()
  [実行環境そのものの情報（利用可能な書体・ツールキット）は読まない。書体は同梱してある（ARC-007 / ADR 0006）]
```

🔴 **この実測で規約の嘘が 1 件見つかった。** 最初に落ちたときのメッセージは
「実行環境そのものの情報（利用可能な書体など）は **`:adapters:font-catalog` からのみ読む**」だった。
そのモジュールが読まなくなった以上、**もう誰も読めない**のだから、この文言は事実と違う。
落ちたこと（規則 ID）だけを見ていたら、規則が自分について嘘をついたまま通っていた。
`config/forbiddenapis/platform.txt` の文言を直したうえで取り直したのが上の出力である。

### 9.2 出所の記録が実体とずれたら落ちるか

`typefaces/provenance.tsv` の SHA-256 を 1 文字変えて実行した（実測）。

```text
TypefaceProvenanceTest > everyRecordedChecksumMatchesTheBundledFile() FAILED
> Task :adapters:font-catalog:test FAILED
```

落ちたのは**チェックサムの検査**であって、書体が読めないことによる別の失敗ではない。
記録だけを書き換えても、ファイルだけを差し替えても、同じ検査が落ちる。

### 9.3 30 書体すべてが「描ける形」で入っているか

`everyBundledFileIsAFontAwtCanCreate` は「ファイルがある」で止めず、
`Font.createFont` が通ることまで見ている。壊れた TTF でもバイト列は返るため、
存在の確認だけでは同梱の正しさを示せない。30 件すべて緑（実測）。

---

## 10. 背景色（Issue #35 / ADR 0007）

### 10.1 文字色と背景色の取り違えは、型では止まらない

ADR 0007 は「色の型を 1 つにする」と決めた。その代償として、
`UserSettings` に同じ型の成分が 2 つ並び、**取り違えてもコンパイルが通る**。

止まらないことを実測で確かめた。`PreferencesSettingsAdapter` の復元で
文字色と背景色を入れ替えたところ、**コンパイルは通り**、テストが落ちた（実測）。

```text
PreferencesSettingsAdapterTest > keepsTheFontAndBackgroundColoursApart() FAILED
PreferencesSettingsAdapterTest > roundTripsSavedSettings() FAILED
PreferencesSettingsAdapterTest > migratesVersionTwoAndKeepsEverythingButTheEnvironmentFont() FAILED
PreferencesSettingsAdapterTest > migratesVersionThreeByFillingInTheBackgroundColour() FAILED
```

🔴 **これは「機械で止めた」ではない。テストで捕まえているだけである。**
強制マトリクスにも ADR にも `planned` として書いた。
型で止める道（色の型を 2 つに分ける）は、同じ不変条件が 2 か所に写るため却下した。

---

## 11. まだ証明していないもの

🔴 **ここに書いていないものは、証明されていない。**

| 対象 | 状態 |
| --- | --- |
| WSLg での表示（`./gradlew run`） | **施主の手元で表示を確認済み（2026-09-04）。** 記録の整備は #14 に残る。単体テストが headless で通っていることは表示の証拠ではない（QLT-012） |
| 同梱書体が**実際に描かれる**こと | **未確認**。`Font.createFont` が通ることまでは示したが、窓の中でその書体で描かれていることは目視でしか示せない（#36 と同時に確認する） |
| SHA-256 dependency verification | **未導入**（QLT-011 の planned 部分） |
| `planned` と書いた規則の強制 | 未実装であることを強制マトリクスに明記している。実装したときに状態を書き換える |
