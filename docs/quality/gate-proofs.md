# ゲート発火の証明

> Status: 記録 / 最終実測 2026-09-22
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

### 9.4 🔴 「読み込める」は「正しく描かれる」ではなかった（Issue #36 で発覚）

`everyBundledFileIsAFontAwtCanCreate` は 30 件すべて緑だった。しかし**実機で窓を出して初めて**、
4 書体が細すぎることが分かった。Java 21 には可変フォントの軸を選ぶ API が無く、
`Font[wght].ttf` は既定のアウトラインで描かれる。Google の可変フォントはその既定が
最も細いマスタであることがある。実測（`Font#getFontName`）:

```text
bitter                 Bitter Thin
manrope                Manrope ExtraLight
montserrat             Montserrat Thin
source-code-pro        Source Code Pro ExtraLight Regular
```

🔑 **検査は「落ちなかった」が、見ているものが足りなかった。**
この 4 書体を Lato / Work Sans / Fira Mono / Zilla Slab へ差し替え、
**既定インスタンス名に太さの語が入っていたら落とす検査**を足した。
negative proof として Lato の中身を Rubik（既定 Light）に差し替えたところ、狙いどおり落ちた（実測）。

```text
BundledTypefaceAdapterTest > everyTypefaceRendersAtRegularWeight(Typeface) > [3] typeface = LATO FAILED
TypefaceProvenanceTest > everyRecordedChecksumMatchesTheBundledFile() FAILED
```

⚠️ この差し替えの最中に**私自身が復元を間違え**、Lato の中身が Rubik のまま残った。
それを見つけたのは SHA-256 の検査である。記録と実体を突き合わせる検査は、
攻撃者ではなく**書いた本人の手違い**を捕まえるために効く。

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

## 11. 枠なしウィンドウと設定モーダル（Issue #36 / ADR 0008）

🔑 **ここは目視でしか確かめられない。** 単体テストは全部緑のまま、画面は 4 か所壊れていた。
WSLg 上で実際に窓を出し、Robot でポインタを動かして確かめた（2026-09-04・実測）。

| 確かめたこと | 結果 |
| --- | --- |
| 枠の無い窓が出る | ✅ 480x240 の窓が、タイトルバー無しで出る |
| 角丸 | ✅ `setShape` が受け付けられた（対応していない環境では角のまま） |
| ホバーでクロームが出る | ✅ 移動 / 設定 / 終了 の 3 つ。時刻に重ならない |
| 同梱書体で描かれる | ✅ JetBrains Mono で描かれている |
| 設定モーダルが開く | ✅ ギアから 600x580 のモーダルが出る |
| 書体ピッカー | ✅ 30 書体がそれぞれ自分の書体で `12:34` を描く |
| 色ピッカー | ✅ プリセット 24 色・HEX・コントラスト比 18.8:1 の表示 |
| 保存済み設定の移行 | ✅ v3 以前の保存から 12 時間表記・100pt・最前面が引き継がれた |
| 終了アイコン | ✅ プロセスが終了コード 0 で終わる。常駐スレッドは残らない（FR-030） |

### 11.1 単体テストが緑のまま壊れていた 4 件

| 何が | 単体テストで見えたか |
| --- | --- |
| 100pt にすると時刻が「05:14:..」と切れる | ❌ 見えない。窓の大きさと文字の幅の関係は描いて初めて分かる |
| ポインタが既にある位置に窓が出るとクロームが永久に出ない | ❌ 見えない。イベントの順序は実機の窓マネージャが決める |
| 色見本が右へ寄る（`BoxLayout` の alignmentX 混在） | ❌ 見えない。レイアウトの結果は描画してからでないと分からない |
| 分類チップが縦に潰れる | ❌ 見えない。同上 |

⚠️ **「テストが緑だから動く」と書かないこと。** 見た目については、テストは何も言っていない。

---

## 12. 文字のアンチエイリアス（Issue #41 / SWG-006 / CNF-012）

### 12.1 効いていなかったことの実測

Swing の文字描画ヒントはデスクトップ環境から渡される。この作業環境では渡されていない（実測）。

```text
awt.font.desktophints = null
awt.useSystemAAFontSettings = null
Graphics2D default TEXT_ANTIALIASING = Default antialiasing text mode
```

同じ文字列を描いて、画像に現れた**階調数**を数えた（実測）。

| 描き方 | 階調数 |
| --- | --- |
| 素の `JLabel`（修正前の実装） | **2**（白と黒だけ＝アンチエイリアス無し） |
| `JLabel` ＋ クライアントプロパティ `KEY_TEXT_ANTIALIASING` | **249** |
| 自前描画で `TEXT_ANTIALIASING = ON` | **249** |

🔴 **時計の数字も設定モーダルの文言も、丸一日ギザギザで描かれていた。**
自前描画の部品（クローム・分節・札）だけはヒントを立てていたので、
**同じ画面の中に描き方が 2 通り**あった。

### 12.2 「大きく描いて縮小する」は Java では不要（実測）

4 倍の大きさで描いて縮小する方式と比べた（実測）。

| 方式 | 階調数 | 代償 |
| --- | --- | --- |
| Java2D の AA | **234** | なし |
| 4 倍で描いて面積平均で縮小 | **17** | 16 倍の面積を描く。ヒンティングが失われる |

4×4 の supersampling は被覆率が 17 段階しか作れない。Java2D のラスタライザは
被覆率をもっと細かく計算するので、**大きく描くほうが原理的に粗い**。

### 12.3 付け忘れられない形になっているか（negative proof）

`ClockPanel` で `TextRendering` を通さずにラベルを作ってみた（実測）。

```text
CNF-012 ui/swing/.../ClockPanel.java:43 — new JLabel( を直接書かない。TextRendering を通す（SWG-006）
```

---

## 13. 言語の切り替え（Issue #42 / ADR 0009）

### 13.1 🔴 英語 UI に日本語を書いて豆腐になった

言語の選択肢を、英語 UI でも「日本語」と表示していた。英語 UI の書体（Arimo）は
**日本語の字形を持たない**ので、実機では **□□□** で描かれた。

- 文言だけ見ても分からない（「日本語」は正しい表記である）
- 書体だけ見ても分からない（Arimo は壊れていない）
- **組で見て初めて分かる**

だから組で見る検査を足した。`UiText` の全定数 × 全言語について、
その言語の同梱書体が文字列を描けること（`Font#canDisplayUpTo` が -1）を確かめる。

negative proof（実測）— 英語側をわざと「日本語」に戻した:

```text
UiTextTest > everyTextCanBeDrawnInEveryLanguage(UiText) > [15] text = LANGUAGE_JAPANESE FAILED
```

### 13.2 🔴 足したばかりの検査を、自分で迂回していた

13.1 の検査（`UiText` の全定数を全言語の書体で描けるか）を足した**あと**に実機を見たら、
まだ豆腐のままだった。原因は、言語の選択肢だけ `UiText` を通さず
`List.of("日本語", "English")` とリテラルで書いていたことである。

🔑 **検査は「集めた場所」しか見ていなかった。集めていない経路が残っていた。**
だから「`ui/` に日本語のリテラルを書けない」検査（CNF-013）を足した。
迂回できる場所そのものを塞ぐ。

```text
CNF-013 ui/swing/.../SettingsFormPanel.java:106 — 画面に出す文言をリテラルで書かない。UiText を通す（FR-048）
```

### 13.3 UI 書体も同梱書体の検査に入っている

`Typeface`（時計・30）と `InterfaceTypeface`（UI・2）は `BundledTypeface` として
ひとつづきに検査する。存在・SHA-256・既定インスタンスが Regular であること。
検査を 2 本に分けると、片方だけ緩む余地が残る。

---

## 14. アプリアイコン（Issue #46）

### 14.1 差し替わっていることの実測

`setIconImages` を呼ぶ前は、JDK の既定アイコンが 1 枚だけ載っていた（実測）。

```text
_NET_WM_ICON(CARDINAL) = Icon (16 x 16)
```

呼んだあと（実測）:

```text
Icon (16 x 16)  Icon (20 x 20)  Icon (24 x 24)  Icon (32 x 32)
Icon (48 x 48)  Icon (64 x 64)  Icon (128 x 128)
```

⚠️ **Windows のタスクバーにどう出るかは、この作業環境からは撮れない。**
WSLg は rootless で、Windows 側の画面を取得する手段が無い。X の性質までしか示せない。

### 14.2 比率は最小の大きさで決めた

16px で描いて選んだ。r13（最初の案）では点が地に沈み、r20 では 2 点がひと塊に見えた。
採ったのは r18 / 中心間 44。**大きい絵で決めて縮めると、必ず小さい側が壊れる。**

---

## 15. 透明度（Issue #49 / ADR 0011）

### 15.1 この環境で使える半透明の種類（実測）

```text
PERPIXEL_TRANSPARENT  = true
TRANSLUCENT           = false     ← 窓全体の一様な不透明度は使えない
PERPIXEL_TRANSLUCENT  = true      ← 画素ごとの半透明は使える
```

`setBackground(アルファつき)` と `setShape` の両方が受け入れられることも確かめた。

### 15.2 実際に透けていることの実測

不透明度を 56% にして、窓の画素を読んだ（実測）。

```text
不透明のとき  中央の画素 = (245, 242, 235)   ← 地の色そのもの
56% のとき    中央の画素 = (137, 136, 132)   ← 0.56 × 245 ≒ 137
```

**地の色がそのまま出ていない**＝下にあるものと混ざっている。

### 15.3 🔴 塗り直されなかった帯が不透明のまま残った

最初の実装では、不透明度を下げたときに**窓の一部が不透明のまま残った**。
`AlphaComposite.Src` は塗った範囲の画素を（透明度ごと）置き換えるが、
Swing は変化した部品の周りしか塗り直さない。地の色だけが変わったときは面全体が塗り直されない。

`renderSettings` の最後で面全体の再描画を明示して直した。
**「色を変えた」と「塗り直した」は別のことである。**

⚠️ **半透明が使えない環境での振る舞いは、この環境からは確かめられない。**
`PERPIXEL_TRANSLUCENT` が使える環境なので、不透明側の経路が走らない。
ADR 0011 に `planned` として書いた。

---

## 16. Windows のショートカットから起動する（Issue #50）

### 16.1 WSLg が .desktop から .lnk を作ることの実測

`/usr/share/applications/nene-clock.desktop` を置いた**約 1 分後**、Windows の Start Menu に
ショートカットが現れた（実測）。

```text
/mnt/c/Users/<user>/AppData/Roaming/Microsoft/Windows/Start Menu/Programs/Ubuntu-22.04/
    ImageMagick (color depth=q16) (Ubuntu-22.04).lnk
    Install RELEASE (Ubuntu-22.04).lnk
    NeNe Clock (Ubuntu-22.04).lnk        ← 07:02 に生成された
```

中身（実測）:

```text
target = C:\Program Files\WSL\wslg.exe
args   = -d Ubuntu-22.04 --cd "~" -- /opt/nene-clock/bin/app
```

`wslg.exe` なので**端末の窓が出ない**。Gradle も通らない。

### 16.2 🔴 WSLg のアイコンにはペンギンが合成される

WSLg が `.desktop` の `Icon=` から作る `.ico` には、**Linux アプリの目印としてペンギンが重なる**（実測）。
製品のアイコンをそのまま出したいので、同じ PNG から自前で `.ico` を作り、
デスクトップのショートカットの `IconLocation` をそちらへ向けた。

### 16.3 🔴 日本語のフォルダ名で文字化けした

`powershell.exe` の出力を bash で受けると、`デスクトップ` が Shift-JIS のまま届いて壊れた（実測）。

```text
cp: cannot create regular file '/mnt/c/Users/info/OneDrive/'$'\203''f'…
```

Windows 側のパス操作は **PowerShell の中に閉じる**ことで直した（`Copy-Item` まで PowerShell が行う）。
bash へ返す文字列には `[Console]::OutputEncoding = [Text.Encoding]::UTF8` を立てる。

### 16.4 アイコンの絵は 1 つのまま

配布物のアイコンは `./gradlew writeAppIcons` が `AppIcon` から書き出す。
**画像をリポジトリに置かない。** 置くと「描いている絵」と「置いた絵」が別々に存在し、片方だけ古くなる。

---

## 17. ちらつきの調査（Issue #55 / #57 / #58 / ADR 0012）

### 17.1 🔴 4 回誤診した

| 回 | 見立て | 入れた直し | 結果 |
| --- | --- | --- | --- |
| 1 | 部分再描画と `Src` の噛み合わせ | 毎秒、面全体を描き直す | **悪化**（窓全体がちらつくようになった） |
| 2 | Swing が下を消している | 面が「不透明」と名乗る | **悪化**（壊れた矩形が残った） |
| 3 | 透明を経由する 2 手描き | 角の外側だけ透明にする | 変わらず |
| 4 | ルートペインが白で塗る | ルートまで不透明・裏画面を降ろす | 変わらず |

2 回目の悪化は、それ自体が実測になった。撮った画像の色を数えると:

```text
(60, 25, 38)   88,698 px  ← 36% が正しく合成された色
(168, 70, 106) 24,408 px  ← 完全不透明＝壊れた矩形
```

**Swing の裏画面はアルファを持たない。** 「不透明」と名乗った部品の描画はそこへ回され、半透明が潰れる。

### 17.2 決め手は「観測できる状態を作った」こと

推測をやめ、**刻みを 200ms から 30ms へ上げた**（毎秒 33 回の描き直し）。
稀な事象を、撮れば当たる頻度にした。40 枚撮った結果:

```text
明るい（白い）画素を含むフレーム: 0 / 40
壊れた不透明画素: 0
支配色: (60, 25, 38)
```

🔑 **アプリが描いている絵は、最初から一度も壊れていなかった。**
`import` が読むのは窓自身の画素である。そこに白が無いなら、白は**窓を画面へ合成する層**で入っている。
アプリからは触れない。

ちらつく範囲が「文字の矩形」「クロームの矩形」「窓全体」と、
そのとき更新を通知した範囲に一致していたことも、合成側の挙動として筋が通る。

### 17.3 残す教訓

1. **直して悪化したら、それは診断である。** 見立てを疑う番であって、直し方を変える番ではない
2. **観測できないものを直そうとしない。** 誤診 4 回はすべて「見えないまま直した」結果である。
   撮れる状態を作ったのは 5 回目で、そこから 10 分で決着した
3. **「アプリの中が壊れている」と決めてかからない。** 壊れていないことを示せれば、探す場所が外へ移る

---

## 18. カラーピッカー（Issue #62 / FR-045）

🔑 **ここも目視でしか確かめられない。** 単体テスト（`HsbCoordinateTest`）が見ているのは
「座標系を通しても色が壊れないか」だけであって、**つまみが正しい所に描かれるか**については何も言っていない。
WSLg 上で窓を出し、`java.awt.Robot` で面と帯をドラッグして確かめた（2026-09-04・実測）。

### 18.1 実機で見たこと

| 確かめたこと | どうやって | 結果 |
| --- | --- | --- |
| 色の画面にピッカーが載っている | 設定 → 文字色 | ✅ 面（彩度／明度）と帯（色相）が自前描画で出る。`JColorChooser` の見た目は出ない |
| ドラッグで HEX が追う | 面を (150,400)→(480,320) へドラッグ | ✅ `#241E28` → `#8A1ED1` |
| ドラッグで見本が追う | 同上 | ✅ 見本の時刻が紫になった |
| ドラッグでコントラストが追う | 同上 | ✅ `5.8 : 1` → `2.4 : 1`。警告色に変わり「読める色にする」が現れた |
| ドラッグで時計本体が追う | 同上（時計の窓を撮る） | ✅ 窓の文字色がその場で変わる |
| 帯を掴むと色相だけが変わる | 帯を縦にドラッグ | ✅ 面が別の色相に塗り替わり、つまみの位置（彩度／明度）は動かない |
| 掴んだ場所で役割が決まる | 面から掴んで帯の上まで引く | ✅ 面のまま。離すまで帯へ移らない |
| HEX を打つとつまみが追う | HEX 欄へ `1E90FF` を入れて Enter | ✅ 帯のつまみが青へ、面のつまみが右上（彩度 1.0 / 明度 1.0）へ跳ぶ |
| 無彩色を打っても色相が飛ばない | 続けて `7CFC00` → `808080` | ✅ 面は緑の色相のまま、つまみだけが左端（彩度 0）へ移った |
| プリセットを押してもつまみが追う | 見本 `#2F6F7E` を押す | ✅ 帯とつまみがその色へ移り、見本に選択の輪が付く |
| 背景色の役でも同じ画面が動く | 設定 → 背景 → 帯をドラッグ | ✅ `#D08C3F` → `#3F63D0`。モーダルの配色が明→暗へ追従した |

### 18.2 🔴 キーボードだけは WSLg の外にあった

HEX の打ち込みを Robot で試したが、**キーが 1 つも届かなかった。**

```text
xprop -root _NET_ACTIVE_WINDOW → window id # 0x0
xwininfo … focus: None
```

X のキーボードフォーカスが誰にも無い。XTEST の合成キーは「フォーカスされた窓」へ配られるので、
どこにも届かない。`XSetInputFocus` も `_NET_ACTIVE_WINDOW` も効かなかった（Xwayland のフォーカスは
Wayland 側の活性化が決めるため、X クライアントからは動かせない）。**ポインタは届くのにキーは届かない。**

⚠️ **これは「HEX 入力が壊れている」ではない。試験の道具が届いていない。**
そこで、production の `ColourPickerPanel` をそのまま組み立て、HEX 欄へ文字を入れて
`postActionEvent()`（Enter と同じ経路）を叩く小さな道具で確かめた。
迂回したのは **OS のキーボードだけ**で、`submitTypedHex` → `RgbColor` → `renderColour` は本物を通っている。

### 18.3 単体テストが緑のまま見えなかったこと

| 何が | 単体テストで見えたか |
| --- | --- |
| 明度 1.0 のときつまみが上端で半分切れる | ❌ 見えない。座標は正しく、**描ける場所が足りない**だけだった。面の上下に 8px の余白を足して直した |

---
## 19. 窓を本当に不透明にする（Issue #64）

### 19.1 🔴 ADR 0012 の決定はコードに届いていなかった

施主報告: 設定モーダルを閉じると、設定アイコンが**半透明のときのように**チカチカすることがある。

ADR 0012 は「窓は常に不透明。角丸は `setShape` の切り抜きへ戻る」と決めたが、#58 が消したのは
色の透明度と半透明の描画だけで、`ClockWindow.askForTranslucency()` は残っていた。
起動時に `setBackground(new Color(0, 0, 0, 0))` を頼み、WSLg は受け入れる（第 15 節・PERPIXEL_TRANSLUCENT = true）。
受け入れられると `roundTheCorners()` は何もせず戻り、`setShape` は呼ばれない。

つまり**色が不透明でも、合成層（WSLg / Windows）から見た窓はアルファ付きのまま**だった。
第 17 節が白の混入場所と特定した条件が、そっくり残っていたことになる。

⚠️ ADR 0012 の「不透明な窓では一度も観測されなかった」は、不透明な窓を**一度も出していない**状態で
書かれていた。規約が自分について嘘をついていた形の 1 つである。

### 19.2 実測（2026-09-04・WSLg・`DISPLAY=:0`）

窓の深さを `xwininfo -id <id>` で読み、`import -window <id>` で撮った画像の角の画素を読んだ。

| | `main`（`/opt/nene-clock/bin/app`） | #64 の作業木（`./gradlew run`） |
| --- | --- | --- |
| `Depth` | **32**（ARGB・アルファ付きの窓） | **24**（TrueColor・不透明） |
| 左上 (0, 0) の画素 | (208, 140, 63) = 地の色。**角が四角** | (0, 0, 0)。切り抜きの外＝塗られていない。**角が丸い** |
| 中央の画素 | (181, 124, 59) = 文字の縁 | (208, 140, 63) = 地の色 |

`Depth: 32` が「合成層から見てアルファ付き」の直接の証拠であり、変更後に 24 へ落ちたことが
「不透明な窓になった」の証拠である。角丸は副作用として戻った（PR #63 の目視で角が四角だったのは、この経路のため）。

### 19.3 施主の実機での確認（2026-09-04）

ちらつきそのものは `import` では撮れない（窓自身の画素ではなく、合成層の事象。第 17 節）。
施主が **ネイティブの Windows 窓（ポータブル zip 版・#68）で設定モーダルを開閉し、ちらつきは出なかった**と確認した。
WSLg 経由の窓については、この PR の時点で未確認のまま（合成層を通らない配布形態ができたので、優先度は下がった）。

## 20. Windows インストーラーの結線（Issue #66 / ADR 0013）

### 20.1 Linux で app-image を作り、起動した（2026-09-04・WSLg・`DISPLAY=:0`）

`jpackage` は動いている OS 向けしか作れないので、MSI そのものは Windows ランナーでしか作れない。
その代わり**同じ task**（`packageInstaller`）が Linux では app-image を作る。jar・main クラス・アイコン・
モジュール集合の結線はここで証明できる。

```text
$ ./gradlew packageInstaller
modules: java.base,java.desktop,java.prefs            ← jdeps が求めた集合（手で書いていない）
installer: NeNe Clock                                  ← app/build/installer/NeNe Clock/
$ grep MODULES "app/build/installer/NeNe Clock/lib/runtime/release"
MODULES="java.base java.datatransfer java.xml java.prefs java.desktop"
$ "app/build/installer/NeNe Clock/bin/NeNe Clock" &
$ xwininfo -id 0x800004 | grep Depth
  Depth: 24
```

窓が出た。同梱書体で描かれ、保存済みの設定（背景 `#1A1917` 系・文字色ピンク）が読めている。
つまり刻んだ実行環境に `java.desktop` と `java.prefs` が入っており、フォントの読み込みも通っている。
大きさは 88 MB（Linux・非圧縮。MSI は圧縮される）。

### 20.2 `.ico` は実装から出る

`writeAppIcons` が PNG 8 枚に加えて `nene-clock.ico` を書く。単体テスト（`IcoFileTest`）が
目次の先頭 6 バイト・各エントリの幅と高さ・各ポインタの先が PNG 署名であることを見る。
`tools/place-windows-shortcut.sh` は ImageMagick をやめ、この `.ico` を使う。

### 20.3 ポータブル zip は同じ app-image から出る（Issue #68 / ADR 0014）

```text
$ ./gradlew packageInstaller
$ ls app/build/installer/
NeNe Clock-0.2.0-linux-portable.zip          33 MB
NeNe Clock-0.2.0-linux-portable.zip.sha256
image/NeNe Clock/                            ← MSI はここから作る（Windows だけ）
$ unzip -l "NeNe Clock-0.2.0-linux-portable.zip" | grep -E "bin/NeNe Clock$|lib/runtime/release$"
    21864  NeNe Clock/bin/NeNe Clock
       94  NeNe Clock/lib/runtime/release
```

zip の中は app-image そのもの（93 ファイル・展開で約 90 MB）。起動できることは 20.1 で示した app-image と同一物である。

### 20.4 `.deb` は WSL の Ubuntu 22.04 に入り、起動し、消える（Issue #74 / ADR 0015・2026-09-05）

```text
$ ./gradlew packageInstaller                       ← Linux では app-image → zip → .deb
$ dpkg-deb -f nene-clock_0.2.0_amd64.deb Package Version Architecture Depends
Package: nene-clock / Version: 0.2.0 / Architecture: amd64
Depends: libasound2, libfreetype6, libx11-6, libxext6, libxi6, libxrender1, libxtst6, xdg-utils, ...（26 個）
$ sudo apt install ./nene-clock_0.2.0_amd64.deb
Setting up nene-clock (0.2.0) ...                  ← Status: install ok installed
$ ls /usr/share/applications | grep nene
nene-clock-NeNe_Clock.desktop                      ← メニュー項目
$ "/opt/nene-clock/bin/NeNe Clock" &               → 窓が出た（Depth 24）
$ sudo apt remove nene-clock
Removing nene-clock (0.2.0) ...                    ← /opt/nene-clock もメニュー項目も消えた
```

🔴 **最初の版は WSL で postinst が落ちた。** `xdg-desktop-menu install` が
「No writable system menu directory found」（終了コード 3）を返す。
原因は `/usr/share/desktop-directories` が無いこと（`xdg-desktop-menu` は `.directory` の置き場も要求する）。
デスクトップ版の Ubuntu には最初からあるが、WSL のような最小構成には無い。
jpackage の `--resource-dir` で postinst だけ差し替え（`app/src/deb/postinst`）、`mkdir -p` を 1 行足した。
差し替え後は上のとおり一度で通る。**アプリ本体は postinst が落ちていても起動できていた**——
落ちていたのはメニュー登録だけで、そこを見ないと「入った」と誤読する種類の壊れ方だった。

### 20.5 MSI は施主の Windows 実機に入った（2026-09-05）

施主が `NeNe Clock-0.2.0.msi`（PR #63 時点の workflow の成果物）を Windows 実機に入れ、**問題なくインストールできた**と確認した。
アンインストールと上書きインストールは未確認。Release には引き続き zip と `.deb` だけを載せている（#70）。

### 20.6 `.deb` は施主の Ubuntu 実機に入った。ドックのアイコンは `StartupWMClass` で結びつける（2026-09-05・#80）

施主が Ubuntu の実機で `sudo apt install ./nene-clock_0.2.1_amd64.deb` を実行し、**入って起動した**。
ただし**アイコンが既定の絵**だった。このとき原因を `StartupWMClass` の不在だと書いたが、
🔴 **それは誤診である**（真の原因は 20.8。この節の以下は `StartupWMClass` 自体の記録として残す）。

```text
$ grep StartupWMClass /opt/nene-clock/lib/nene-clock-NeNe_Clock.desktop
StartupWMClass=io-github-hideyukimori-neneclock-app-NeNeClockApplication
$ xprop -id <窓> WM_CLASS
WM_CLASS(STRING) = "io-github-hideyukimori-neneclock-app-NeNeClockApplication", "io-github-hideyukimori-neneclock-app-NeNeClockApplication"
```

一致する。Java（AWT/X11）はメインクラスの完全修飾名の「.」を「-」に替えて `WM_CLASS` にする。
**メインクラスを改名したら `.desktop` の行も変える**（機械では突き合わせていない）。
同じ `.deb` で 0.2.0 → 0.2.2 の上書きインストールも通った（`Unpacking nene-clock (0.2.2) over (0.2.0)`）。

### 20.7 版とシグネチャがフッターに出る（Issue #82・2026-09-05）

`./gradlew run`（作業木・`version=0.2.2`）で設定モーダルを開き、フッター右端に
`NeNe Clock 0.2.2 · hideyukiMORI` が薄い文字で出ることを目視した。版は `product.properties` 経由で
`gradle.properties` から来ており、コードには書いていない（`ProductIdentityFileTest` が `${` の残留を拒否する）。

## 20.8 🔴 アイコンが既定のままだった本当の理由（Issue #84・2026-09-05）

### 20.8.1 誤診をもう一度した

施主の報告は「アイコンが既定のまま」。私は**アイコンのファイルを一度も見ないまま**、
「窓とメニュー項目が結びついていないからだ」と決めて `StartupWMClass` を足した（#80・v0.2.2）。
施主が入れ直し、ログアウトまでしても直らなかった。

2 回目に**見るほうを先にした**。`.desktop` が指す PNG を突き合わせただけで 1 分で決着した。

```text
$ grep ^Icon= /opt/nene-clock/lib/nene-clock-NeNe_Clock.desktop
Icon=/opt/nene-clock/lib/NeNe_Clock.png
$ identify -format '%wx%h' その PNG        →  32x32          ← 我々の絵は 256x256
$ sha256sum その PNG
8b2491d0b5cbc67075dcae4d29c8a92b9ab813d9eca05a2f16ee3b3efb970e65
$ sha256sum <jdk.jpackage.jmod>/resources/JavaApp.png
8b2491d0b5cbc67075dcae4d29c8a92b9ab813d9eca05a2f16ee3b3efb970e65   ← バイト単位で同一
```

**表示されていたのは jpackage の既定アイコンそのものだった。**
`.deb` を `--app-image` から作るとき、jpackage は app-image の中のアイコンを**流用しない**。
デスクトップ統合用に `--icon` を別途要求し、無ければ自前の既定を黙って使う。

⚠️ ここでも [ADR 0012](../adr/0012-transparency-is-dropped-the-artefact-is-not-ours.md) の教訓が繰り返された。
**「直して直らなかったら、見立てを疑う番である。」** 直し方（ログアウト・入れ直し）を変える番ではなかった。
`StartupWMClass` は窓の結びつけには要るので残したが、**症状の原因ではなかった**。

### 20.8.2 直したあと

```text
$ sha256sum /opt/nene-clock/lib/NeNe_Clock.png      ← .deb v0.2.3 を入れたあと
4103f880d9ad2b31d501b607f567f5627382af54dc79a869c705e42cb150e635   256x256
$ sha256sum app/build/icons/nene-clock-256.png
4103f880d9ad2b31d501b607f567f5627382af54dc79a869c705e42cb150e635   ← AppIcon の出力と一致
```

### 20.8.3 機械に守らせた（negative proof つき）

「`--icon` を渡し忘れる」を人の記憶に頼らせない。`packageInstaller` は作った `.deb` を `dpkg-deb -x` で開き、
デスクトップ項目が指すアイコンが `AppIcon` の 256px 出力と**バイト単位で一致**しなければビルドを落とす。

わざと `--icon` を外して、落ちることと**落ちた理由**を確かめた:

```text
> the .deb desktop entry points at an icon that is not ours: /opt/nene-clock/lib/NeNe_Clock.png
  (jpackage falls back to its own default when --icon is missing)
BUILD FAILED
```

戻すと通る。

### 20.9 まだ証明していないこと

- MSI のアンインストールと上書きインストール
- 施主の Ubuntu 実機で、`StartupWMClass` を入れた版のドックのアイコンが製品のものになること
- 施主の Windows 実機で入って起動すること。**ネイティブの窓でちらつきが起きないか**もそこで見る
- 署名は無い。SmartScreen の警告が出る

## 21. 画面をまたぐドラッグ（Issue #95 / #96 / ADR 0018・2026-09-22）

> 🔴 **この節の実装は置き換えられた（ADR 0018 → [ADR 0019](../adr/0019-the-desktop-is-not-one-coordinate-space.md)）。**
> 実機で測ったところ、ここに書いた単体テストは緑のままだったが、**症状は 1 つも直っていなかった**。
> 何が間違っていたか、いま何を証明しているかは第 24 節にある。この節は「緑のゲートが、直って
> いないものを直ったと言いうる」ことの記録として残す。

施主の Windows 実機（4 画面・125% / 150% / 175% / 150%）で、拡大率の違うモニタへドラッグすると
窓が 7.43 倍（956×260 → 5642×1546）まで膨らみ、ポインタから大きく外れた。原因の実測は
[ADR 0018](../adr/0018-the-window-knows-which-screen-it-is-on.md) にある。

### 21.1 単体テストで見たもの（`:ui:swing:test` / `WindowDragTest`・8 件）

ドラッグの座標計算を副作用の無い `WindowDrag` に切り出した。**機械が見られるのはここだけである。**

| テスト | 見ていること |
| --- | --- |
| `theGrabbedPointStaysUnderThePointer` | 掴んだ点がポインタの下に留まる |
| `theLocationIsDecidedByThePointerAloneSoErrorCannotAccumulate` | 500 回の中間計算が結果に残らない |
| `theSameJourneyInOneJumpEndsAtTheSamePlace` | 951 歩で這わせても 1 跳びでも同じ位置に着く |
| `aSizeChangeDuringTheDragIsNoticed` | 765×208 → 637×173（ADR 0018 の過渡状態の実測値）を「変わった」と判定する |
| `theGrabbedPointComesBackInsideAWindowThatShrank` | 窓が縮んだら掴み点を窓の中へ収め直す（700,190 → 636,172） |
| `aPointerOutsideTheWindowIsPulledBackToTheEdge` | 窓の外で掴み直しても掴み点は縁に留まる |
| `theWindowFollowsThePointerAgainAfterTheGrabIsTakenAnew` | 掴み直したあとも窓はポインタに追従する |
| `aWindowWithNoExtentStillGivesAGrabPointInside` | 幅・高さ 0 の窓でも掴み点が負にならない |

**negative proof**（QLT-007・落ちることと、**落ちた理由**まで見た）:

```text
① 掴み点の切り詰めを外す（inside(...) が素の offset を返す）
   theGrabbedPointComesBackInsideAWindowThatShrank  FAILED  (WindowDragTest.java:70)
   aPointerOutsideTheWindowIsPulledBackToTheEdge    FAILED  (WindowDragTest.java:77)

② locationFor に 1px のずれを入れる
   theGrabbedPointStaysUnderThePointer                            FAILED  (WindowDragTest.java:28)
   theWindowFollowsThePointerAgainAfterTheGrabIsTakenAnew         FAILED  (WindowDragTest.java:86)
   theLocationIsDecidedByThePointerAloneSoErrorCannotAccumulate   FAILED  (WindowDragTest.java:41)
```

どちらも戻すと `:ui:swing:test` は緑に戻る。

⚠️ 「誤差が累積しない」は**構造の性質**である。`locationFor` は掴み点以外の状態を読まないので、
累積しようがない。上の 2 件のテストはその性質を**目撃**しているのであって、累積する実装との
差を見張っているわけではない。**そこは型と可視性（`final` な値型・掴み点は不変）が担保している。**

### 21.2 WSLg で目で見たもの（2026-09-22・`DISPLAY=:0 ./gradlew run`）

ドラッグは `java.awt.Robot` の別プロセスで実際のポインタを動かして行った（**合成キーは WSLg に
届かないが、マウスは届く**——第 18.2 節で届かなかったのはキーボードである）。

```text
$ xwininfo -root -tree | grep "NeNe Clock"
  0x600004 "NeNe Clock": ... 527x250+32+32  +5520+2400

# 掴み点 (200,120)、5px×3px を 40 歩 ＝ ポインタを (+200,+120) 動かす
$ java DragProbe.java 5720 2520
$ xwininfo -id 0x600004
  Absolute upper-left X:  5720   Y:  2520   Width: 527   Height: 250
```

往復を 3 回:

```text
戻り  +5520+2400  527x250
行き  +5720+2520  527x250
戻り  +5520+2400  527x250
行き  +5720+2520  527x250
戻り  +5520+2400  527x250
行き  +5720+2520  527x250
```

- **窓はポインタと同じ量だけ動いた**（+200,+120）。掴み点は外れない
- **大きさは 6 回とも 527×250 のまま**。往復しても元の位置にぴったり戻る（累積が無い）
- 画面写真（`import -window`）を前後で見比べた。**時刻以外は同一**——角丸も、右上のクロームの
  位置も、文字の位置も変わっていない

### 21.3 🔴 まだ証明していないこと（QLT-012）

**この修正が直そうとしている症状そのものは、この環境では一度も再現していないし、確認もできない。**

| 主張 | 状態 |
| --- | --- |
| 単一画面でのドラッグが壊れていない | **確認済み**（21.2・WSLg・Robot による実測） |
| 座標計算が累積しない・掴み点が窓の外へ出ない | **確認済み**（21.1・単体テスト＋ negative proof） |
| 拡大率の違うモニタへまたいでも窓が膨らまない | 🔴 **未確認。** WSLg に per-monitor DPI が無い |
| またいだ直後に窓がポインタから外れない | 🔴 **未確認。** 同上 |
| `componentMoved` が画面の載り換えで発火すること | 🔴 **未確認。** 画面が 1 つしか無いので、載り換えが起きない |

🔴 **検証の手段は 1 つしかない: MSI を作り直して施主の Windows 実機に入れ、ADR 0018 と同じ測定
（per-monitor 対応を宣言した測定プロセスで矩形を読む）をやり直す。** その数字が出るまで、
この修正は「直ったと信じている」であって「直った」ではない。
**測る側の DPI 認識を宣言していない測定値は、ここに書かない。**

## 22. 余白の設定と「いまの設定で起こりうる最大」（Issue #94 / ADR 0017 / FR-049）

🔑 **ここも目視でしか確かめられない。** 「窓が縮んだか」「毎秒震えないか」は画素でしか見えない。
WSLg 上で窓を出し、`xwininfo` で画素を読み、`import` で撮って確かめた
（2026-09-22・`DISPLAY=:0`・`JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew run`）。
操作は `java.awt.Robot`（使い捨ての 1 ファイル）で行った。キーボードは使っていない（第 18.2 節）。

### 22.1 実機に残っていた v7 の保存から、実際に移行した

テストで作った v7 ではなく、**この機械に前から残っていた本物の v7 の保存**（`schemaVersion=7` /
Anton 95pt / `#A8456B` / 黒背景 / English / 最前面）で起動した。

| 確かめたこと | 結果 |
| --- | --- |
| v7 → v8 の移行 | ✅ 9 項目すべてそのまま出た。余白だけ既定の 30 になった |
| 保存し直された形式 | ✅ `schemaVersion=8` と `windowPadding` が書かれた（`~/.java/.userPrefs/.../prefs.xml`） |

（確認後、この機械の保存は v7 のバックアップへ戻してある。配布版で改めて移行が走る。）

### 22.2 窓の画素（Anton 95pt・秒あり・日付あり）

| 設定 | 窓の大きさ（実測） |
| --- | --- |
| 24 時間・余白 30（移行直後） | **388 × 250** |
| 24 時間・余白 160 | **648 × 510**（＝ 388−60+320 × 250−60+320） |
| 24 時間・余白 8 | **344 × 206** |
| **12 時間**・余白 8 | **483 × 206** |
| 12 時間・余白 8・秒なし | 320 × 206（下限で止まる） |
| 12 時間・余白 8・秒なし・日付なし | 320 × 160（下限で止まる） |

🔑 **24 時間表記と 12 時間表記の差は 139px あった。** これまではこの 139px を
**24 時間表記でも常に確保していた**。「窓が無駄に広い」の実体はここにあった（ADR 0017 の文脈どおり）。
日付を隠すと高さは 206 → 160 になり、日付行は場所を取らなくなった。

### 22.3 窓は毎秒震えない

`xwininfo` を 2 秒ごとに読み、秒の桁が何度も変わる間の幅を見た。

| 表記 | 観測 | 結果 |
| --- | --- | --- |
| 24 時間 | 12:22:13 〜 12:22:23（6 回） | ✅ 388 × 250 のまま動かない |
| 12 時間 | 12:25:55 〜 12:26:07（7 回） | ✅ 483 × 206 のまま動かない |

AM ↔ PM の切り替わりそのものは**この時間帯では観測できない**。候補に AM と PM の両方を入れて
最大を取っていることは `ClockFaceQueryTest` が見ている（`keepsBothMarkersInTheWidestTwelveHourFace`）。

### 22.4 設定モーダルは 10 項目でも溢れない

| 確かめたこと | 結果 |
| --- | --- |
| 「余白」の行が「大きさ」の下に出る | ✅ 帯と数値（30 / 160 / 8）が出る |
| 600 × 580 のモーダルに 10 行が収まる | ✅ 背景色の行とフッターが切れずに出る |
| 帯を動かすと即座に窓が変わる | ✅ 離す前から追従する。保存も同時に行われる |

### 22.5 気づいたこと

- **窓の最小サイズ（320 × 160）は広げる向きにしか効かない。** 文字が切れることはないが、
  余白 8 ＋ 秒なしのような小さい組み合わせでは窓がそれ以上縮まない。ADR 0017 の決定の範囲外なので、
  下限そのものは動かしていない
- 最前面を有効にしていると、**時計の窓がモーダルの上に来る**ので、Robot で操作するときは
  先に時計を横へ退かす必要があった（人が触るぶんには窓を動かせばよい）

---

## 25. 画面をまたぐドラッグを、実機の合成ドラッグで選んだ（Issue #100 / #101 / ADR 0019）

🔴 **ここは WSLg では一切証明できない。** 複数画面も per-monitor DPI も無い（QLT-012）。
証拠はすべて施主の Windows 実機で取った。

### 25.1 測り方

- 合成ドラッグ: `SendInput`（`mouse_event`）で押下・移動・解放を作る。人は触らない
- 測定: `SetProcessDpiAwarenessContext(PER_MONITOR_AWARE_V2)` を宣言したプロセスから
  `GetWindowRect` と `GetCursorPos` を 30ms ごとに読む
- ⚠️ **測る側が DPI 認識を宣言していないと、拡大率について何も言っていない値が返る。**
  最初の測定でこれを踏み、「全モニタ 96dpi」という誤った像を得た
- ⚠️ 画面がロックされていると合成クリックはロック画面に吸われる。窓は動かず、試験は成立しない。
  押下点に本当に対象の窓が居るかを `WindowFromPoint` で確かめてから始める

### 25.2 4 案を走らせて選んだ

主（125%）→ 左（150%）へ 70 歩。掴み点とポインタの実ピクセル距離を毎標本で測った。

| 案 | 逆変換に使う画面 | 外れた標本 | 最大のずれ | 判定 |
| --- | --- | --- | --- | --- |
| `window` | 窓の `GraphicsConfiguration` | 持続 | **1842px** | ❌ |
| **`pointer`** | **ポインタの画面** | **2 / 70** | 1827px（直後に復帰） | ✅ 採用 |
| `delta` | 使わない（移動量だけ） | 窓が動かない | 2057px | ❌ |
| `verify` | ポインタの画面 ＋ 読み戻しで検出 | 2 / 70 | 1932px | ❌（同着） |

右（125%→175%）はどの案でも追従（ずれ 30px ＝標本の遅れ）。

### 25.3 膨らみは消えた

| | 旧 v0.2.5 | ADR 0018 版 | 採用版 |
| --- | --- | --- | --- |
| 左へドラッグ | ×1.2 が累積（実測 7.43 倍・5642×1546 まで） | 同左 | **727px のまま変わらず** |
| 右へ往復して戻す | 論理サイズが 765→637 に壊れる | 正常 | **正常（727 に戻る）** |

### 25.4 まだ直っていないこと（Issue #101）

- 境界を越える瞬間の **1〜2 フレーム（約 60ms）の跳ね**。ピアが窓を置き直す一手であり、
  Java からは事後にしか観測できない。`verify` 案で「気づく」ことはできたが、防げなかった
- **消せないものを消したと書かない。** 直ったのは「飛んだまま戻らない」であって「一度も跳ねない」ではない

---

## 23. まだ証明していないもの

🔴 **ここに書いていないものは、証明されていない。**

| 対象 | 状態 |
| --- | --- |
| WSLg での表示（`./gradlew run`） | **確認済み（2026-09-04）。** 第 11 節に実測を記録した（QLT-012 / #14） |
| 同梱書体が**実際に描かれる**こと | **確認済み（2026-09-04）。** 30 書体すべてがピッカー上で自分の書体で描かれた（第 11 節） |
| 窓の見た目そのものの機械検査 | **不能**。目視と記録でしか担保できない。だから第 11 節と第 18 節を残す |
| カラーピッカーのつまみの位置 | **確認済み（2026-09-04）。** 第 18 節に実測を記録した（FR-045 / #62） |
| WSLg 上でのキーボード入力の自動化 | **不能**。X のフォーカスが Wayland 側にあり、合成キーが届かない（第 18.2 節） |
| 拡大率の違う画面をまたぐドラッグ（ADR 0019 の実装） | 🔴 **未確認。** 実機の自動ドラッグ試験台が回るまで（第 24 節） |
| SHA-256 dependency verification | **未導入**（QLT-011 の planned 部分） |
| `planned` と書いた規則の強制 | 未実装であることを強制マトリクスに明記している。実装したときに状態を書き換える |

---

## 24. 実ピクセルで決めるドラッグ（Issue #100 / ADR 0019・2026-09-22）

第 21 節（ADR 0018）の修正は、**単体テストが緑のまま、実機の症状を 1 つも直さなかった**。
理由は [ADR 0019](../adr/0019-the-desktop-is-not-one-coordinate-space.md) にある実測のとおりで、
①窓の `GraphicsConfiguration` は載り換えても差し替わらない（画面の変化を知る手段が無い）
②Java の仮想デスクトップは**原点が実ピクセル・大きさが論理ピクセル**で単位が混ざっている
③ポインタの座標源が 2 つあって、境界では 1282px 食い違う。

この節は、**やり直した実装で機械が何を見ているか**と、**それでもまだ見えていないもの**を書く。

### 24.1 単体テストで見たもの（`:ui:swing:test`・`ScreenSpaceTest` 7 件 / `WindowDragTest` 10 件）

変換は副作用の無い 2 つの値型に置いた。画面は**値として渡す**ので、施主の 4 画面（混在尺度）を
headless で再現できる。`ScreenSpace` が画面 1 枚の読み方、`WindowDrag` がドラッグの算術である。

| テスト | 見ていること |
| --- | --- |
| `ScreenSpaceTest.theWindowMeasurementFromTheRealMachineIsReproduced` | 実機の実測 Java -290 → 実 -362（主・125%） |
| `ScreenSpaceTest.thePointerMeasurementFromTheRealMachineIsReproduced` | 実機の実測 Java -1284 → 実 -6（左・150%・原点 -3840） |
| `ScreenSpaceTest.theOriginOfAScreenIsTheSamePointInBothUnits` | 原点は両方の単位で同じ点（右・右上） |
| `ScreenSpaceTest.aNegativeOriginDoesNotBreakTheRoundTrip` | 右上（3818,-1440）で往復して元の値に戻る |
| `ScreenSpaceTest.theExtentOfAScreenIsLogicalSoItScalesWithoutTheOrigin` | 765×208 → 1148×312（150%）／1339×364（175%）／956×260（125%） |
| `ScreenSpaceTest.aScreenWithoutScalingIsTheIdentity` | 尺度 1 の画面では恒等 |
| `ScreenSpaceTest.aScaleThatCannotBeInvertedIsRefused` | 尺度 0・NaN を作らせない（逆変換が定義できない） |
| `WindowDragTest.theGrabbedPointStaysUnderThePointerWhenItCrossesToTheLeftScreen` | **本体**。ポインタが左（150%）、窓は主（125%）に居ると Java が思ったまま → `setLocation` に渡す値は (-55,1308)。ピアの変換を掛け直すと掴み点が**ぴったり**ポインタの下（誤差 0） |
| `WindowDragTest.theGrabbedPointStaysUnderThePointerOnTheRightAndTopScreensToo` | 右（175%）・右上（150%・原点 -1440）でも誤差 1px 以内 |
| `WindowDragTest.draggingInsideOneUnscaledScreenIsExactlyThePointerMinusTheGrab` | 尺度 1 の画面では「ポインタ − 掴み点」そのもの（＝従来の挙動） |
| `WindowDragTest.draggingInsideOneScaledScreenKeepsTheGrabUnderThePointer` | 同一画面（125%）内でも誤差 1px 以内 |
| `WindowDragTest.theGrabbedPointIsRemappedAndStaysInsideAWindowWhoseDeviceSizeChanged` | 窓の実寸が変わったら掴み点を同じ相対位置へ写し、窓の中に収める（875 → 1051 / 572） |
| `WindowDragTest.theGrabbedPointNeverLeavesAWindowThatLostAllItsExtent` | 大きさ 0 でも掴み点が負にならない |
| `WindowDragTest.feedingTheSamePointerTwiceGivesTheSameLocation` | 同じ入力に同じ答え（累積しない） |
| `WindowDragTest.theLocationIsDecidedByThePointerAloneSoErrorCannotAccumulate` | 478 歩で這わせても 1 跳びでも同じ位置 |
| `WindowDragTest.aPointerOutsideTheWindowIsPulledBackToTheEdge` | 窓の外で掴んでも掴み点は縁に留まる |
| `WindowDragTest.aWindowWithNoExtentStillGivesAGrabPointInside` | 幅・高さ 0 の窓でも掴み点が窓の中 |

🔑 **ピアの変換をテストの中に書き下した**のが第 21 節との違いである。
`setLocation` に渡した値へ**窓の画面の尺度**を掛け直し、その結果と実ピクセルのポインタを比べる。
「渡した値」ではなく「**届く値**」を見ているので、事前補償が正しいかを機械が言える。

**negative proof**（QLT-007・落ちることと、**落ちた理由**まで見た）:

```text
① 事前補償を壊す（逆変換を「窓の画面」ではなく「ポインタの画面」で行う）
   WindowDragTest.theGrabbedPointStaysUnderThePointerWhenItCrossesToTheLeftScreen  FAILED
     expected: java.awt.Point[x=-55,y=1308]
      but was: java.awt.Point[x=-1326,y=1090]        ← 手計算と一致（1271px ずれる）
   WindowDragTest.theGrabbedPointStaysUnderThePointerOnTheRightAndTopScreensToo    FAILED
     Expecting actual: -848  to be between: [-1, 1]  ← 掴み点が 848px ポインタから外れる
   94 tests completed, 2 failed

② 掴み点の写し直しの比を逆にする（now/was → was/now）
   WindowDragTest.theGrabbedPointIsRemappedAndStaysInsideAWindowWhoseDeviceSizeChanged  FAILED
   94 tests completed, 1 failed

③ 写し直しそのものを消す（offset をそのまま返す）
   → テストまで届かない。**コンパイルが落ちる**:
     [UnusedVariable] The parameter 'now' is never read. / error: warnings found and -Werror specified
```

どれも戻すと `:ui:swing:test` は緑（94 件）に戻る。

### 24.2 WSLg で目で見たもの（2026-09-22・`DISPLAY=:0 ./gradlew run`）

ドラッグは `java.awt.Robot` の別プロセスで実際のポインタを動かして行った（掴み点 (200,120)・
5px×3px を 40 歩）。窓の矩形は `xwininfo` で読んだ。

```text
before  +5520+2400  388x250
out     +5720+2520  388x250     ← ポインタと同じ +200,+120
back    +5520+2400  388x250
out     +5720+2520  388x250
back    +5520+2400  388x250
out     +5720+2520  388x250
back    +5520+2400  388x250
```

- **窓はポインタと同じ量だけ動く**。掴み点は外れない
- **大きさは 7 回とも 388×250 のまま**。往復してもぴったり同じ位置に戻る（累積が無い）
- `import -window` で撮って確認。角丸・右上のクローム・文字の位置は変わっていない

⚠️ **この環境の画面はこう見えている**（使い捨ての probe で読んだ・`GraphicsEnvironment` は
repo の外の 1 ファイルで、production は使っていない）:

```text
:0.0 bounds=(3840,1440 3840x2160) scale=1.0x1.0
:0.1 bounds=(7658,0    2560x1440) scale=1.0x1.0
:0.2 bounds=(0,1440    3840x2160) scale=1.0x1.0
:0.3 bounds=(7680,1440 3840x2160) scale=1.0x1.0
```

🔑 **画面は 4 枚あるが、尺度はすべて 1.0 で、原点は Java の大きさと矛盾していない。**
だから WSLg で通ったのは `ScreenSpace` が**恒等になる経路**である。混ざった単位も、穴も、
尺度の食い違いも、ここには存在しない。

それでも 1 つだけ実機に近いことができた。**画面の境界（x=3840）をまたぐドラッグ**である。

```text
before   +5520+2400  388x250   ← :0.0 の上
crossed  +2800+2400  388x250   ← ポインタを -2720 動かし、窓も -2720。:0.2 へ入った
back     +5520+2400  388x250
```

- またいでも**窓は 1px もずれず、大きさも変わらない**
- すなわち「ポインタの画面 ≠ 窓の画面」になりうる経路（`MouseInfo#getPointerInfo()` と
  `PointerInfo#getDevice()`）が**実際に走って、壊れていない**。ただし尺度が同じなので、
  **この試験は事前補償の正しさについては何も言っていない**

### 24.3 🔴 まだ証明していないこと（QLT-012）

| 主張 | 状態 |
| --- | --- |
| 変換の算術（4 画面・混在尺度・事前補償） | **確認済み**（24.1・単体テスト＋ negative proof 3 種） |
| 単一画面のドラッグが壊れていない | **確認済み**（24.2・WSLg・Robot による実測） |
| 画面の境界をまたいでも窓がずれない（**尺度が同じ場合**） | **確認済み**（24.2） |
| 拡大率の違う画面をまたいでも掴み点がポインタの下に留まる | 🔴 **実機で反証された（24.4）。** 左へのドラッグで 1842px 外れたまま |
| 窓が ×1.2 ずつ膨らまない（`componentResized` での復元が効く） | ✅ **実機で確認された（24.4）。** min=max=727 |
| 戻ってきたときに窓の大きさが元どおりになる | ✅ **実機で確認された（24.4）。** 可逆 |

🔴 **検証の手段は 1 つしかない: 施主の実機の自動ドラッグ試験台（`SendInput` で合成したドラッグ＋
DPI 対応の測定プロセス）を、このビルドに対して回すことである。** 合格条件を先に数値で書いておく。

| 測るもの | 合格 |
| --- | --- |
| 主（125%）→ 左（150%）へ 80px 刻みで這わせる間の「掴み点とポインタの実ピクセル距離」 | **どの標本でも 2px 以内**（1500px 飛ばない） |
| 同じ区間での窓の論理サイズ | **どの標本でも `pref` と同じ**（582 → 698 → 838 …と育たない） |
| 左（150%）に着いたときの窓の実ピクセル幅 | **どちらでもよいが、どちらかに決まること**。ADR 0019 の「代償」は `pref × 1.5` と書いているが、**窓の GC が更新されない以上、復元した論理サイズに掛かるのは古い 1.25 のままのはず**で、実ピクセルは `pref × 1.25` に落ち着くと予想する。⇒ 150% の画面では**相対的に小さく見える**。合格条件は「安定していること・育たないこと」であり、どちらの値が出たかは ADR 0019 の代償の記述を直すための観測である |
| 復元と Windows の綱引き | **起きないこと**。`componentResized` が止まらず窓が震える（毎秒 setSize が往復する）なら、復元の場所を見直す |
| 主へ戻したときの窓の論理サイズ | **出発時と同じ値に戻る**（可逆） |

**この数字が出るまで、この修正は「直ったと信じている」であって「直った」ではない。**
**測る側の DPI 認識を宣言していない測定値は、ここに書かない。**

---

### 24.4 実機が答えた（2026-09-22・自動ドラッグ試験台・DPI 対応の測定プロセス）

24.3 に書いた予測のうち **2 と 3 は当たり、1 は外れた。**

```text
LEFT  125->150: start=727px(125%) end=727px(125%) min=727 max=727  worstPointerGap=1842px
RIGHT 125->175: start=727px(125%) end=1018px(175%) min=727 max=1018 worstPointerGap=30px
BACK  150->125: start=873px(150%) end=727px(125%) min=727 max=873  worstPointerGap=1847px
```

- ✅ **膨らまない**（min=max=727・×1.2 の連鎖が消えた）。`componentResized` での復元は効いている
- ✅ **可逆**（戻すと 727 に戻る）
- ✅ 実ピクセル幅の予測（24.3 の観測点）は **`pref × 1.25` のまま**という私の読みどおりだった
  （左 125→150 で 727 のまま）。**ただし右（125→175）では 1018＝`pref × 1.75` になっている。**
  ⇒ 「窓の GC が更新されない」は**どちらの向きでも同じではない**
- 🔴 **掴み点は左へのドラッグで 1842px 外れたまま**（右は 30px で追従する）

左へのドラッグの 1 歩ずつ（実ピクセル・外部の測定プロセス）:

```text
step40  cursor=-240  win=(-300,901)     ← 追従している
step50  cursor=-540  win=(+1200,1081)   ← 跳ぶ。修正前と同じ
```

🔑 **跳びは近似ではなく、厳密に説明がついた。** step50 で狙った実ピクセルは -600。
`frame.getGraphicsConfiguration()` は主（原点 0・尺度 1.25）を返したので `setLocation(-480)` を渡した。
窓が着いたのは **+1200** ＝ `-3840 + (-480 + 3840) × 1.5`、すなわち**左画面の写像**をそのまま掛けた値である。

> **ピアは既に左画面の写像を使っているのに、`getGraphicsConfiguration()` はまだ主画面を返していた。**
> 窓の GC は「大きさの経路で古い」だけでなく、**ピア自身の `setLocation` 変換に対しても古い**。
> 事前補償の算術は正しい。**鍵にしている画面が信用できない。**

右へのドラッグが壊れないのは、そちらではピアと GC がたまたま一致するからである（gap 30px）。

この数（-600 → -480 → +1200、およびポインタの画面を鍵にしたときの -1689 → -613）は
**単体テストに入れてある**（`WindowDragTest.theHarnessStepFiftyIsReproducedByBothKeyings` /
`ScreenSpaceTest.theJumpTheHarnessMeasuredIsExactlyTheDifferenceBetweenTwoMappings`）。
実機の 1 標本が、これで機械の見張りに変わった。

### 24.5 どの画面を鍵にするかは、測定で決める（`DragStrategy`・一時的な分岐）

🔴 **これは ARC-001（一つのことを実現する方法は 1 つ）に対する一時的な例外である。**
実機でしか答えが出ない問いが 1 つ残っており、それを 1 つのビルドで測れるようにするために置いた。
**選ばれたら残り 2 つは消す。**

| `-Dneneclock.dragStrategy=` | 何をするか |
| --- | --- |
| `window`（既定） | 窓の GC で事前補償する。24.4 で測ったいまの挙動 |
| `pointer` | **ポインタの画面**で事前補償する（逆変換も、窓の実寸も、掴み点の写し直しも）。step50 の数なら `-3840 + (-600+3840)/1.5 = -1680` を渡し、ピアの左画面の写像がちょうど -600 に戻す |
| `delta` | 絶対位置を使わず、ポインタの**移動量**だけ動かす。写像の切り替わりに伴う見かけの跳び（実測 1282px）は「移動ではない」として捨てる（閾値 300 論理px・`PointerStep`） |

単体テスト: `DragStrategyTest`（4 件・既定と名前の解釈）、`PointerStepTest`（5 件・閾値と外れ値の排除）、
`WindowDragTest` の 2 件（上記の実機の数）。

### 24.6 WSLg で 3 つを実際に走らせた（2026-09-22）

`installDist` した配布物を `JAVA_OPTS=-Dneneclock.dragStrategy=…` で起動し、`/proc/<pid>/cmdline` で
**プロパティが JVM に届いていること**を確認したうえで、Robot でドラッグして `xwininfo` で読んだ。

```text
window  before +5520+2400 → out +5720+2520 → back +5520+2400 → crossed +2800+2400 → home +5520+2400   388x250 のまま
pointer 同上（すべて同じ値）
delta   同上（すべて同じ値）
```

尺度がすべて 1.0 の環境なので、**3 つとも同じ答えになるのが正しい**（`ScreenSpace` が恒等になる）。
つまりこれは「3 つとも壊れていない」ことの確認であって、優劣については何も言っていない。

1 つだけ、WSLg でも**戦略の違いが見える**測定ができた。1 イベントで大きく跳ばせる試験である。

```text
掴んでから 1 イベントで +400px 跳ばす:
  window   5520 → 5920   （追従する）
  pointer  5520 → 5920   （追従する）
  delta    5520 → 5520   🔑 動かない（閾値 300 を超えたので「移動ではない」と判定した）
掴んでから 1 イベントで +200px 跳ばす:
  delta    5520 → 5720   （閾値の内側なので追従する）
```

⚠️ **代償がそのまま見えている。** `delta` は**本物の速い一振り（1 イベントで 300px 超）も捨てる**。
そのぶん窓は遅れ、しかも `delta` は累積するので**遅れは自分では戻らない**。閾値 300 は
「人の手は 1 イベントでそこまで動かない」という仮定であり、実機のイベント頻度で確かめていない。

### 24.7 🔴 次に実機が答えるべきこと

| 問い | 見分け方 |
| --- | --- |
| `pointer` は左へのドラッグを直すか | `worstPointerGap` が 30px 台まで落ちるか。**落ちない場合も、per-step の形を見る**（1〜2 標本だけ跳ねて戻るなら「ピアの切り替わりとポインタの画面の切り替わりが数フレームずれている」、+1200 に居座るなら「鍵が間違っている」） |
| `delta` は左へのドラッグを直すか | 跳びを捨てた直後に**ずれが残り続けないか**。`delta` は自己修正しないので、1 度ずれたら最後までずれたままになる |
| `delta` で窓が遅れないか | 試験台の 1 歩（実測 30px 前後）は閾値 300 の内側なので捨てられないはず。捨てられた歩数を数えられるとなお良い |
