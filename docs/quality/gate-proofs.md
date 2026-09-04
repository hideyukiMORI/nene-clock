# ゲート発火の証明

> Status: 記録 / 最終実測 2026-09-04
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

### 20.4 まだ証明していないこと

- Windows ランナーで MSI と zip ができること（PR の workflow で走る）
- 施主の Windows 実機で入って起動すること。**ネイティブの窓でちらつきが起きないか**もそこで見る
- 署名は無い。SmartScreen の警告が出る

## 21. まだ証明していないもの

🔴 **ここに書いていないものは、証明されていない。**

| 対象 | 状態 |
| --- | --- |
| WSLg での表示（`./gradlew run`） | **確認済み（2026-09-04）。** 第 11 節に実測を記録した（QLT-012 / #14） |
| 同梱書体が**実際に描かれる**こと | **確認済み（2026-09-04）。** 30 書体すべてがピッカー上で自分の書体で描かれた（第 11 節） |
| 窓の見た目そのものの機械検査 | **不能**。目視と記録でしか担保できない。だから第 11 節と第 18 節を残す |
| カラーピッカーのつまみの位置 | **確認済み（2026-09-04）。** 第 18 節に実測を記録した（FR-045 / #62） |
| WSLg 上でのキーボード入力の自動化 | **不能**。X のフォーカスが Wayland 側にあり、合成キーが届かない（第 18.2 節） |
| SHA-256 dependency verification | **未導入**（QLT-011 の planned 部分） |
| `planned` と書いた規則の強制 | 未実装であることを強制マトリクスに明記している。実装したときに状態を書き換える |
