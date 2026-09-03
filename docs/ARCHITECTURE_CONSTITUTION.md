# アーキテクチャ憲章 — NeNe Clock

> Status: normative（規範）/ 2026-09-03 初版
> 判断の根拠は [ADR 0001](adr/0001-strictness-is-mechanically-enforced.md)。
> 本書が沈黙している領域は [CODING_RULES.md](CODING_RULES.md) と公式の Java 標準に従う。

---

## 0. この文書の読み方

**すべての規則は「機械強制」の状態を持つ。**

| 表記 | 意味 |
| --- | --- |
| **active** | 違反すると `./gradlew check` が落ちる。人の記憶に依存しない |
| **planned** | 規範だが、まだ機械が見ていない。**この行に触れる変更は PR で明示的に自己レビューすること** |
| **不能** | 言語・道具の仕様上、機械では塞げない。塞げないことを明記して残す |
| **不採用** | 検討して採らなかった。理由を必ず併記する（再提案は同じ理由への反論から始める） |

🔴 **planned を active と書き換えないこと。** 未実装の強制を実装済みに見せるのは、
規約全体の信頼を壊す唯一の行為である。実装してから書き換える。
どの規則がいまどの状態かの正本は [QUALITY_GATES.md](QUALITY_GATES.md) の強制マトリクスであり、
`validateConformance`（CNF-005）が「本書に定義があるのにマトリクスに行が無い」状態を拒否する。

### 強制の実体は 5 層ある

| 層 | 実体 | 守るもの |
| --- | --- | --- |
| コンパイラ | `javac -Xlint:all -Werror` / `sealed` / アクセス制御 | **不正な状態を「書けなく」する** |
| Gradle モジュールグラフ | `settings.gradle.kts` ＋ `config/architecture/module-graph.txt` | 層の依存方向 |
| API 単位の禁止 | `forbidden-apis`（`config/forbiddenapis/*.txt`） | **メソッド単位**で「呼べなくする」 |
| 静的解析 | Error Prone / NullAway / Checkstyle / ArchUnit | 書けてしまうが書くべきでないこと・**パッケージ単位**の境界 |
| 規約検査 | `validateConformance`（依存ゼロの自作 Java） | **NeNe Clock として守るべきこと** |

道具が重なっても責務は重ねない。**forbidden-apis は「メソッド」、ArchUnit は「パッケージ／レイヤ」**
という分担を崩さない（同じ規則を 2 か所で設定すると、片方を緩めても誰も気づかなくなる）。

### Java がどこまで届いたか

先行事例（NENE-PIXEL＝Kotlin / nene-recall＝Go / xi-tools＝Rust）が到達した点との比較。

| 規則 | Kotlin | Go | Rust | **Java 21（本リポジトリの実測）** |
| --- | --- | --- | --- | --- |
| 不正状態を表現不能に・網羅性 | ある | 無い | ある | **ある**（`sealed` + switch は `default` が無ければ網羅をコンパイラが検査。`default` の混入は CNF-003 が拒否） |
| 公開状態は不変 | ある | 無い | ある | **ほぼある**（`record` / `final`。配列・可変コレクションの漏れは ArchUnit と Checkstyle が見る） |
| `null` の意味は一つ | 不十分 | 不十分 | ある | **不十分**（NullAway で大半を塞ぐが、`null` そのものは言語から消せない） |
| private コンストラクタ＋唯一のファクトリ | ある | 不十分 | ある | **ある**（アクセス制御をコンパイラが強制する） |
| **決定性（時刻・乱数・既定ゾーンを読ませない）** | planned | planned | planned | 🔑 **ある**（`forbidden-apis` が JDK のシグネチャ単位で拒否する） |
| 抑制は例外であって道具ではない | waiver 台帳（人手） | 理由必須どまり | forbid 層（抑制不能） | **弱い**（`@SuppressWarnings` は言語で塞げない。CNF-002 の waiver 台帳で補う） |

🔑 **決定性は Java の方が強い。** Kotlin/Go/Rust の先行 3 本が `planned` のまま残した
「core で現在時刻を読ませない」を、Java は `forbidden-apis` で **active** にできた。
理由は単純で、対象が JDK の具体的なメソッドシグネチャだからである。

🔴 **逆に、抑制の封じ込めは Rust より弱い。** `#[forbid]` に相当するものが Java には無く、
`@SuppressWarnings` はコンパイラの機能なので禁止できない。本リポジトリは
NENE-PIXEL と同じ waiver 台帳（人手の手続き）で補っている。**穴が残ることを書かずに厳格さを主張しない。**

---

## 1. 憲章規則

### ARC-001 — 一つの意味に一つの正典経路

一つの意味には、正典となる型・所有者・振る舞いの経路が **ちょうど 1 つ**存在する。
並行するサービス、同義の別モデル、アダプタ固有の業務ロジック、同じ概念の別名を作らない。

置き換えが必要なら、呼び出し側の移行と旧経路の削除を**同じ変更で**行う。段階移行が要るときは
期限付きの waiver に書く。

- 機械強制: **planned**（レビュー事項。「意味が同じか」は機械が判定できない）
- 補助: CNF-001（総称名の禁止）が「役割の分からない第 2 経路」の温床を減らす

### ARC-002 — 依存方向は物理的である

依存は [PROJECT_LAYOUT.md](PROJECT_LAYOUT.md) のモジュールグラフに従う。禁じた依存は
「レビューで気をつけること」ではなく **import できないこと**でなければならない。
モジュール間の循環は許さない。

- 機械強制: **active**（`validateConformance` の CNF-010 が承認外モジュール・許可外の依存辺・循環を拒否）
- 機械強制: **active**（`:core:domain` は production 依存の追加そのものをビルドが拒否する）

### ARC-003 — 中核はプラットフォームから独立している

`:core:domain` と `:core:application` は Swing・AWT・`java.util.prefs`・ファイル・ネットワーク・
現在時刻・既定ロケール・既定タイムゾーンを知らない。プラットフォームは**型のあるポート**から入る。

🔑 **Swing も Preferences も JDK 同梱である。** したがって Kotlin/Android の先行事例と違い、
Gradle のモジュールグラフでは塞げない（依存を宣言しなくても import できてしまう）。
この規則はパッケージ単位の検査層が塞ぐ。

- 機械強制: **active**（ArchUnit `PlatformIsolationRulesTest`）

### ARC-004 — 状態には唯一の所有者がいる

| 状態 | 意味 | 所有者 | 変更経路 |
| --- | --- | --- | --- |
| `UserSettings` | 保存される利用者設定 | `:core:domain` の値型 | `SettingsStorePort` 経由の保存のみ |
| 表示文字列（`ClockFace`） | 画面に出す整形済みの値 | `:core:application` の `ClockFaceQuery` | 生成のみ。UI は保持しない |
| Swing 部品の状態 | 描画のための一時状態 | `:ui:swing` の各部品 | `render*` メソッドのみ |

同じ事実を 2 つの区分に独立して持たない。派生値は再計算するか、無効化を明示したキャッシュにする。

- 機械強制: **active**（UI 状態の反映経路のみ。CNF-004）
- 機械強制: **planned**（「同じ事実の二重保持」の検出）

### ARC-005 — 可変性は隔離区画にのみ存在する

外から見える domain / application の状態は不変とする。可変な状態を持ってよいのは
`:ui:swing` の Swing 部品の内部だけである（Swing の API がそう出来ている）。

隔離区画は次を守る。

- 可変なコレクション・配列・ビルダを外へ返さない
- 受け取るのは検証済みの値型だけ
- 同じ入力に対して同じ描画結果になる

- 機械強制: **active**（`fields().that().areStatic().should().beFinal()` ＝ ArchUnit）
- 機械強制: **planned**（可変コレクションの返却検出）

### ARC-006 — 合成は明示的である

依存は合成ルート（`:app`）で与える。サービスロケータ・リフレクションによる発見・
クラスパス走査・暗黙のシングルトン・可変なグローバル登録簿は使わない。
DI フレームワークを入れるには ADR が要る。

- 機械強制: **active**（static な可変フィールドの禁止＝ArchUnit、`Class#newInstance()` の禁止＝forbidden-apis）
- 機械強制: **planned**（リフレクション API 全般の禁止）

### ARC-007 — 既定で決定的である

中核は、現在時刻・乱数・既定ロケール・既定タイムゾーン・環境変数・プロセス状態を**直接読まない**。
これらはポートか明示的な引数から入る。

現在時刻を JDK から読んでよいのは `:adapters:system-time` **ただ 1 モジュール**である。
そのことは「レビューの約束」ではなく、**そのモジュールだけ `determinism.txt` を適用しない**という
ビルドファイルの差分として残る。

- 機械強制: **active**（`config/forbiddenapis/determinism.txt`。`now()` / `nanoTime()` / `currentTimeMillis()` /
  `Math.random()` / `ZoneId.systemDefault()` などをメソッド単位で拒否）
- 機械強制: **active**（既定ロケール・既定文字集合は forbidden-apis の `jdk-unsafe` が拒否）
- 補足: テストソースにも同じ署名を適用する。**テストが実時刻を読むことも決定性の破壊である。**

### ARC-008 — 境界は一度だけ検証する

外から来た値（保存済み設定・利用者入力）は、最初の自前の境界で検証し、その場で domain の型へ変換する。
検証前の生の値を中核へ流さない。UI と永続化で**違う規則の検証を二重に書かない**。

- 機械強制: **planned**（レビュー事項）
- 補助: JAV-007（唯一のファクトリ）が、検証を通らない値の生成を難しくする

### ARC-009 — 永続化の契約は版を持つ

保存形式は明示的な版（`SettingsSchemaVersion`）を持ち、移行規則を決める。
読めない版は既定値へ黙って落とさず、型のある失敗（`SettingsLoadFailure.UNSUPPORTED_SCHEMA`）として返す。

- 機械強制: **planned**（版の存在は型で保証されるが、移行の網羅性はテストの仕事）
- 補助: QLT-008 が「振る舞いの変更にテストを伴わせる」

### ARC-010 — 期待される結果は型で表す

期待される失敗・検証エラー・非互換は、閉じた結果型（`sealed interface` ＋ `record`）で返す。
例外はプログラム上の欠陥・キャンセル・下位層の想定外に限る。
`null`・boolean・文字列・汎用例外で複数の業務的な結果を表さない。

- 機械強制: **active**（`sealed` の網羅性はコンパイラ、`default` の混入は CNF-003、
  広い catch は Checkstyle `IllegalCatch` が拒否）

### ARC-011 — UI は状態を描き、意図を発行する

`:ui:swing` は、application が作った値を描き、利用者の操作を意図として渡すだけを行う。
整形の判断・状態遷移・永続化の呼び出しを UI に置かない。

- 機械強制: **active**（`java.time.format` の使用を application 以外で禁止＝ArchUnit、
  `java.util.prefs` を preferences アダプタ以外で禁止＝ArchUnit、UI 状態の反映経路＝CNF-004）

### ARC-012 — アーキテクチャは利便性に優先する

道具の近道・ライブラリの便利機能・最適化が、状態の所有者・モジュールグラフ・契約の境界を
迂回することを許さない。アーキテクチャが間違っているなら ADR で明示的に変える。
ユーティリティを装った局所的な例外を作らない。

- 機械強制: **不能**（判断そのものが対象。QLT-010 の手続きで担保する）

---

## 2. 強制を置く順番

規則は、確実に効く**いちばん早い層**に置く。

1. Java の型・可視性・`sealed`
2. Gradle のモジュールグラフとソースセット
3. コンパイラオプション（`-Xlint:all -Werror`）
4. forbidden-apis（メソッド単位）
5. 静的解析（Error Prone / NullAway / Checkstyle）と ArchUnit（パッケージ・レイヤ単位）
6. 規約検査（`validateConformance`）
7. CI のマージゲート
8. まだ自動化できない判断だけをレビューへ

レビューだけの強制は一時的な状態であり、[QUALITY_GATES.md](QUALITY_GATES.md) に `planned` として残す。
