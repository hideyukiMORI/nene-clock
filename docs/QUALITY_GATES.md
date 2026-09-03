# 品質ゲート — NeNe Clock

> Status: normative（規範）/ 2026-09-03 初版
> 本書は「いま何が機械で守られているか」の**正本**である。
> 規範の本文は [ARCHITECTURE_CONSTITUTION.md](ARCHITECTURE_CONSTITUTION.md) と
> [CODING_RULES.md](CODING_RULES.md)、機械側の実体はこの文書に対応する。

`./gradlew check` がローカルと CI の**唯一の完了定義**である。
個別の道具は診断のために単独で回してよいが、その成功は完了の代わりにならない。

---

## 1. ゲートの整合性規則（QLT-0xx）

### QLT-001 — ゲートは一つ

ローカルの検証と CI は**同じ 1 つのタスク**（`./gradlew check`）を呼ぶ。
CI のワークフローに品質判断のシェルロジックを書かない。第 2 の完了定義を文書化しない。

- 機械強制: **active**（`.github/workflows/ci.yml` は `./gradlew check` を呼ぶだけ）
- 機械強制: **planned**（CI がゲートを再構成していないことの機械検査）

### QLT-002 — 警告は失敗する

`javac` の警告は `-Xlint:all -Werror` で失敗にする。Error Prone / NullAway / Checkstyle /
forbidden-apis の指摘も失敗にする。重大度の引き下げは禁止。

- 機械強制: **active**（`neneclock.java-conventions` の `compilerArgs`、NullAway を `error` で実行）

### QLT-003 — baseline を作らない

NeNe Clock は lint・静的解析・アーキテクチャ・依存・テストのいずれについても baseline を持たない。
既存の違反を「無かったこと」にする経路を作らない。**唯一の例外機構は期限付きの狭い waiver である。**

- 機械強制: **active**（CNF-008 が baseline / suppressions ファイルとゲート無力化フラグを拒否）

### QLT-004 — 整形は検査であって修復ではない

整形は `config/` の設定から決定的に決まる。CI がソースを書き換えて成功させることはしない。
整形の正本は 1 つ（Spotless ＋ palantir-java-format）。

- 機械強制: **active**（`spotlessCheck` が `check` に入る）

### QLT-005 — ローカルと CI は同一

CI で必要なゲートは、すべてローカルで Gradle から実行できる。
Gradle のタスクやテストとして書ける規則を CI 専用のシェルとして書かない。

- 機械強制: **active**（CI は `./gradlew check` のみを実行する）

### QLT-006 — アーキテクチャは実行可能

モジュール境界・import 規律・状態の経路は機械が検査する。
散文だけのアーキテクチャ規則は、**「不能」と明記されない限り未完成**として扱う。

- 機械強制: **active**（`validateConformance` の CNF-010、ArchUnit の各テスト）

### QLT-007 — カスタムゲートには negative proof が要る

自作の検査は、「わざと違反させた入力で、意図した規則によって落ちること」と
「正しい入力では通ること」の両方を示すまで信用しない。
証拠は [quality/gate-proofs.md](quality/gate-proofs.md) に記録する。ゲートを変えたら証拠も同じ変更で更新する。

- 機械強制: **active**（build-logic の単体テストが CNF 各規則の正例・反例を検証する）

### QLT-008 — 振る舞いの変更はテストを伴う

振る舞いを変えたら、いちばん狭い安定した境界にテストを足す。
不具合の修正は、可能なら**先に落ちる回帰テスト**を書いてから直す。

- 機械強制: **planned**（レビュー事項。カバレッジ下限＝QLT-009 が部分的に代替する）

### QLT-009 — カバレッジは下げられない

`:core:domain` と `:core:application` は分岐カバレッジ **90%** を下限とする。
閾値は上げてよいが下げてはならない。下げるには ADR が要る。

UI・アダプタ・合成ルートには現時点でカバレッジのゲートを置いていない。
**「置いた」と書かないために、置いていないことをここに書く。**

- 機械強制: **active**（`jacocoTestCoverageVerification` が core 2 モジュールの `check` に入る）
- 機械強制: **planned**（UI・アダプタ層の閾値）

### QLT-010 — ゲートの弱体化はアーキテクチャ変更

重大度・除外・閾値・モジュール境界・必須 CI ジョブを変えるには明示的な根拠が要る。
MUST 規則を弱める変更には ADR か waiver が要る。**無関係な作業を通すために検査を切ることは禁止。**

- 機械強制: **不能**（判断そのものが対象。PR の手続きで担保する）

### QLT-011 — 依存は再現可能

依存の版は `gradle/libs.versions.toml` にだけ書く。Gradle の dependency locking を全設定に適用し、
lock ファイルのドリフトはビルドを落とす。

- 機械強制: **active**（`dependencyLocking { lockAllConfigurations() }` ＋ 各モジュールの `gradle.lockfile`）
- 機械強制: **planned**（SHA-256 の dependency verification）

### QLT-012 — 環境依存の主張は正直に名付ける

単体テストは表示サーバ（X11 / Wayland / WSLg）を必要としない。
単体テストが通ったことを「WSLg で動く」の証拠として扱わない。表示を伴う確認は
[quality/gate-proofs.md](quality/gate-proofs.md) に環境と手順を書いて別に記録する。

- 機械強制: **active**（テストは `java.awt.headless=true` で実行する）

---

## 2. 規約検査の規則（CNF-0xx）

`validateConformance` が実行する自作検査。実体は `build-logic/src/main/java/.../conformance/`
（依存ゼロの Java）で、各規則には正例・反例の単体テストがある（QLT-007）。

### CNF-001 — 禁止された総称名

型名の語尾 `Manager` / `Helper` / `Util` / `Utils` / `Common` と、
パッケージ名の構成要素 `utils` / `helpers` / `managers` / `misc` / `common` を拒否する。
判断が要る語（`Processor`・`Data` 等）は機械では拒否しない（JAV-010）。

### CNF-002 — 抑制には waiver ID が要る

`@SuppressWarnings` は直前行に `// Waiver: WVR-NNNN` が無ければ拒否する。
`@SuppressWarnings("all")` は waiver があっても拒否する（JAV-015）。

### CNF-003 — 網羅性を殺す `default` の禁止

production ソースの `switch` に `default:` / `default ->` が現れたら拒否する（JAV-002）。

### CNF-004 — UI 状態の反映は render 経路だけ

`ui/` 配下で `setEnabled(` / `setAlwaysOnTop(` を呼べるのは、名前が `render` で始まるメソッドの中だけ。
囲むメソッドを特定できない場合も拒否する（安全側に倒す）（SWG-003）。

### CNF-005 — ドキュメント整合

規則 ID が二重に定義されていないこと、参照された ID がすべて定義済みであること、
ARC / JAV / SWG / CNF のすべての ID が本書の強制マトリクスに行を持つことを検査する。
**規約が自分自身について嘘をつく経路を塞ぐ。**

### CNF-006 — TODO / FIXME には Issue 番号

コメント中の `TODO` / `FIXME` は `(#123)` 形式の Issue 番号を伴わなければ拒否する。

### CNF-007 — 1 ファイル 1 主要宣言

トップレベル型が 2 つ以上あるファイル、ファイル名と主要型名が一致しないファイルを拒否する（JAV-011）。

### CNF-008 — baseline とゲート無力化の禁止

`baseline` / `suppressions` を名前に含む設定ファイルの存在、および設定・ビルドスクリプト中の
`ratchetFrom` / `ignoreFailures = true` / `failOnError = false` / `-Xlint:none` / `-nowarn` を拒否する（QLT-003）。

### CNF-009 — waiver 台帳の整合

waiver ファイルの命名・必須項目（Rule / Scope / Issue / Expires）・索引への掲載を検査し、
**期限切れの waiver でビルドを落とす**。ソースが参照する waiver の実在も検査する。

### CNF-010 — モジュールグラフ

`config/architecture/module-graph.txt` に無いモジュール、許可されていない依存辺、
モジュール間の循環を拒否する（ARC-002）。

---

## 3. 強制マトリクス

規範の各規則が、いまどの層で守られているか。**この表が実装と食い違ったら merge を止める。**

| 規則 | 状態 | 機械強制の実体 |
| --- | --- | --- |
| ARC-001 | planned | レビュー事項（CNF-001 が温床を減らす） |
| ARC-002 | active | CNF-010 ＋ `:core:domain` の production 依存禁止 |
| ARC-003 | active | ArchUnit `PlatformIsolationRulesTest` |
| ARC-004 | active（一部） | CNF-004。二重保持の検出は planned |
| ARC-005 | active（一部） | ArchUnit の static final 検査。可変コレクション返却は planned |
| ARC-006 | active（一部） | ArchUnit ＋ forbidden-apis `Class#newInstance()` |
| ARC-007 | active | forbidden-apis `config/forbiddenapis/determinism.txt` |
| ARC-008 | planned | レビュー事項 |
| ARC-009 | planned | 型としては存在。移行の網羅はテストの仕事 |
| ARC-010 | active | `sealed` の網羅性 ＋ CNF-003 ＋ Checkstyle `IllegalCatch` |
| ARC-011 | active | ArchUnit（`java.time.format` / `java.util.prefs` の限定）＋ CNF-004 |
| ARC-012 | 不能 | QLT-010 の手続き |
| JAV-001 | planned | レビュー事項（JAV-007 が補助） |
| JAV-002 | active | コンパイラの網羅性検査 ＋ CNF-003 |
| JAV-003 | active（一部） | Checkstyle `VisibilityModifier` ＋ ArchUnit |
| JAV-004 | active | NullAway（error）。`null` の存在自体は不能 |
| JAV-005 | active | Checkstyle `IllegalCatch` / `IllegalThrows` ＋ forbidden-apis |
| JAV-006 | planned | 検出規則は未実装 |
| JAV-007 | active | アクセス制御（コンパイラ） |
| JAV-008 | active（一部） | Checkstyle `VisibilityModifier` |
| JAV-009 | active | Checkstyle `IllegalImport` ＋ forbidden-apis の jdk-internal / reflection 束 |
| JAV-010 | active | CNF-001 |
| JAV-011 | active | CNF-007 ＋ Checkstyle `OneTopLevelClass` / `OuterTypeFilename` |
| JAV-012 | active | Checkstyle の複雑度規則 |
| JAV-013 | active | ArchUnit `RuntimeDisciplineRulesTest` |
| JAV-014 | active | forbidden-apis `base.txt` ＋ `jdk-unsafe` 束 |
| JAV-015 | active（一部） | CNF-002 ＋ CNF-009。抑制機能そのものの禁止は不能 |
| SWG-001 | active（一部） | JAV-013 の ArchUnit 規則。EDT 上かの静的判定は不能 |
| SWG-002 | active | ArchUnit の import 限定 |
| SWG-003 | active | CNF-004 |
| SWG-004 | planned | 検出規則は未実装 |
| SWG-005 | active（一部） | ARC-007 の forbidden-apis。減算カウンタの検出は planned |
| CNF-001 | active | `JavaSourceRules` |
| CNF-002 | active | `JavaSourceRules` |
| CNF-003 | active | `JavaSourceRules` |
| CNF-004 | active | `JavaSourceRules` |
| CNF-005 | active | `DocumentationRules` |
| CNF-006 | active | `JavaSourceRules` |
| CNF-007 | active | `JavaSourceRules` |
| CNF-008 | active | `BaselineRules` |
| CNF-009 | active | `WaiverLedger` |
| CNF-010 | active | `ModuleGraphRules` |

---

## 4. `check` に入っている層

| 層 | 目的 | 実体 |
| --- | --- | --- |
| コンパイル | 型安全・網羅性・警告ゼロ | `javac -Xlint:all -Werror --release 21` |
| null 解析 | `null` の意味の固定 | Error Prone ＋ NullAway（error） |
| 整形 | 文字列としての正本 | Spotless ＋ palantir-java-format |
| API 禁止 | メソッド単位の禁止 | forbidden-apis（base / determinism / bundled 束） |
| 静的解析 | 構造と複雑度 | Checkstyle |
| アーキテクチャ | レイヤ・パッケージ境界 | ArchUnit（`:quality:architecture-tests`） |
| 規約検査 | NeNe Clock 固有 | `validateConformance`（CNF-001..010） |
| 単体テスト | 振る舞い | JUnit 5（headless） |
| カバレッジ | 中核の検証密度 | JaCoCo（core 2 モジュールで分岐 90%） |
| 依存 | 再現性 | Gradle dependency locking |

---

## 5. マージゲート

`main` は次を必須とする。

- Pull Request 経由であること
- `./gradlew check` が成功していること（CI の `quality` ジョブ）
- 生成物のドリフトが無いこと（作業ツリーが汚れないこと）
- 期限切れ waiver が無いこと
- 未解決のレビュー指摘が無いこと

`main` への直接 push・force push・ブランチ削除は禁止する。

🔴 **リポジトリ設定（ruleset）はまだ適用していない。** GitHub へ push した時点で設定し、
その事実を [quality/gate-proofs.md](quality/gate-proofs.md) に記録する。**設定していないものを
「必須になっている」と書かない。**
