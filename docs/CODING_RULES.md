# Java コーディング規約 — NeNe Clock

> Status: normative（規範）/ 2026-09-03 初版
> 判断の根拠は [ADR 0001](adr/0001-strictness-is-mechanically-enforced.md)。
> 本書は Java を本リポジトリで承認された部分集合に狭めるためのものであり、
> ここが沈黙している領域は公式の Java 標準（Java Language Specification / 一般的な Java 慣行）に従う。

読み方（**active / planned / 不能 / 不採用**）と強制の 5 層は
[ARCHITECTURE_CONSTITUTION.md](ARCHITECTURE_CONSTITUTION.md) 第 0 節と同じ。

---

## 1. 型と状態（JAV-0xx）

### JAV-001 — 境界でのプリミティブ執着を禁じる

識別子・寸法・ポイント数・版番号のように**単位や不変条件を持つ値**は専用の型にする。
`int fontSize` を引き回さず `FontSize` を渡す。検証は生成時に行い、生の値を深いところまで運ばない。

```java
public static FontSizeOutcome of(int points) { ... }   // ✅ 生成時に検証する唯一の経路
void apply(int points)                                  // ❌ どこで検証されたか型から分からない
```

- 機械強制: **planned**（「専用型にすべき値かどうか」はレビュー事項）
- 補助: **active**（JAV-007 のファクトリ強制により、型を作れば必ず検証される）

### JAV-002 — 閉じた選択肢は enum / sealed で表し、`default` を書かない

モードや状態機械を boolean の組み合わせ・マジック整数・裸の文字列で表さない。
選択肢が閉じているなら `enum` か `sealed interface` にする。

`switch` の網羅性は Java 21 のコンパイラが見る。**したがって規約が守るべきなのは
「網羅性検査を無効化しないこと」**である。`default` を書いた瞬間に検査は死ぬ。

```java
return switch (settings.clockFormat()) {
    case HOUR_24 -> ...;
    case HOUR_12 -> ...;
};                                   // ✅ 選択肢が増えたらコンパイルが落ちる

return switch (settings.clockFormat()) {
    case HOUR_24 -> ...;
    default -> ...;                  // ❌ 将来の選択肢を黙って飲む
};
```

- 機械強制: **active**（網羅性はコンパイラ、`default:` / `default ->` の混入は CNF-003）
- 補足: 本リポジトリは Checkstyle の `MissingSwitchDefault` を**採用しない**。
  この規約と正面から矛盾するため（不採用の理由をここに残す）

### JAV-003 — 公開状態は不変

- 公開フィールドを持たない。値は `record` か `final` フィールドで表す
- domain / application の型は可変コレクション・配列・可変ビルダを公開しない
- 外から受け取った可変データは、必要なら防御的に複製して所有する

- 機械強制: **active**（Checkstyle `VisibilityModifier`、ArchUnit の static final 検査）
- 機械強制: **planned**（可変コレクション返却の検出）

### JAV-004 — null の意味は一つ

`null` が意味してよいのは「省略可能な値が無い」だけである。
無効・未読込・失敗・未知・削除済みを `null` で表さない。それらは専用の結果型を作る。

- 公開 API は `null` を返さない
- 内部で `null` を使うときは `@Nullable`（JSpecify）を必ず付ける
- パッケージは `@NullMarked` を宣言する

- 機械強制: **active**（Error Prone の NullAway を error で実行。`@NullMarked` されたパッケージで
  未注釈の `null` 代入・逆参照を拒否）
- 機械強制: **不能**（`null` そのものを言語から消せない。Rust との差はここ）

### JAV-005 — 期待される失敗は例外で表さない

検証エラー・見つからない・拒否・非互換は `sealed interface` の結果型で返す。

```java
public sealed interface SettingsLoadOutcome {
    record Restored(UserSettings settings) implements SettingsLoadOutcome { ... }
    record Defaulted(SettingsLoadFailure failure) implements SettingsLoadOutcome { ... }
}
```

- `catch (Exception | Throwable | RuntimeException | Error)` を書かない
- 例外を握り潰さない。`printStackTrace()` を呼ばない
- 保存の失敗を戻り値の無い `void` で消さない

- 機械強制: **active**（Checkstyle `IllegalCatch` / `IllegalThrows`、
  forbidden-apis が `Throwable#printStackTrace()` を拒否）

### JAV-006 — 汎用データバッグを禁じる

`Object`・`Map<String, Object>`・意味を持つ値の `Object[]`・文字列キーのメタデータで型を代用しない。
名前付きの型を作る。

- 機械強制: **planned**（検出規則は未実装。レビュー事項）

### JAV-007 — 構築が不変条件を守る

不変条件を持つ型はコンストラクタを非公開にし、生成経路を**唯一のファクトリ**に集約する。
ファクトリは想定内の不正入力に対して例外を投げず、結果型を返す。

```java
public final class FontSize {
    private FontSize(int points) { ... }                 // ✅ 外から呼べない
    public static FontSizeOutcome of(int points) { ... } // ✅ 唯一の生成経路
}
```

🔑 **`record` の正準コンストラクタは公開度を下げられない**（言語仕様）。
不変条件を持つ値型を `record` で作ると生成経路が 2 本になる。したがって
**検証を伴う値型は `final class` にする**。`record` は「すでに検証済みの値の組」にだけ使う。

- 機械強制: **active**（アクセス制御をコンパイラが強制する）
- 機械強制: **planned**（「record にすべきでない型が record になっている」の検出）

### JAV-008 — 可視性は最小

- 既定は package-private。他パッケージが実際に使うものだけ `public` にする
- `protected` を使わない（継承を前提にしない）
- テストのためだけに公開しない

- 機械強制: **active**（Checkstyle `VisibilityModifier` がフィールドを見る）
- 機械強制: **planned**（不要な `public` 型・メソッドの検出）

### JAV-009 — 言語マジックを制限する

リフレクション・動的プロキシ・`Class#newInstance()`・シリアライズ・`sun.*` / `com.sun.*` /
`jdk.internal.*` の使用を禁じる。注釈による実行時の探索も使わない。

- 機械強制: **active**（Checkstyle `IllegalImport`、forbidden-apis の `jdk-internal` /
  `jdk-reflection` / `jdk-non-portable` 束、`Class#newInstance()` の明示禁止）

### JAV-010 — 名前が役割を語る

常に禁止する型名の語尾: `Manager` / `Helper` / `Util` / `Utils` / `Common`。
常に禁止するパッケージ名の構成要素: `utils` / `helpers` / `managers` / `misc` / `common`。

承認された役割の語尾は `Port` / `Adapter` / `Query` / `Command` / `Handler` / `Policy` /
`Factory` / `Codec` / `Mapper` / `Renderer` / `Panel` / `Frame` / `Outcome`。
**その型がその役割そのものであるときだけ**使う。

`Processor` や `Data` のように**文脈次第で妥当な語は機械では拒否しない**。
機械が拒否してよいのは「常に禁止」だけで、判断が要る語はレビューの仕事である。

- 機械強制: **active**（CNF-001）

### JAV-011 — 1 ファイル 1 主要宣言

ファイルは 1 つの主要な型とその周辺に閉じる。ファイル名は主要型名と一致させる。
寄せ集めのファイルを作らない。ネストした `record` は「その型の一部」であるときのみ許す。

- 機械強制: **active**（CNF-007、Checkstyle `OneTopLevelClass` / `OuterTypeFilename`）

### JAV-012 — 複雑度に上限を置く

| 指標 | 上限 |
| --- | --- |
| 循環的複雑度（メソッド） | 10 |
| メソッドの長さ | 40 行 |
| ネストの深さ（if） | 3 |
| ネストの深さ（for） | 2 |
| ネストの深さ（try） | 1 |
| 引数の数（メソッド） | 4 |
| 論理式の項数 | 3 |
| ファイルの長さ | 400 行 |

閾値を満たすためだけに意味のある処理を割るのは目的に反する。超える必要があるときは
**測定可能な理由**を添えて ADR にする。

`record` と コンストラクタの成分数は上限の対象外である。値の組を 1 つの概念として名前付きの型に
まとめたものが `record` であり、それを分割させると JAV-001 と矛盾するため（**不採用**の理由）。

- 機械強制: **active**（Checkstyle）

---

## 2. 実行時の規律（JAV-013 以降）

### JAV-013 — 並行性は一種類

このアプリで動くスレッドは EDT（Event Dispatch Thread）だけである。
`java.lang.Thread`・`java.util.Timer`・`java.util.concurrent.*` を使わない。
周期処理は `javax.swing.Timer` だけを使う。

- 機械強制: **active**（ArchUnit `RuntimeDisciplineRulesTest`）

### JAV-014 — 日時と数値の扱いを一つに固定する

- 日時は `java.time` だけを使う。`java.util.Date` / `Calendar` / `SimpleDateFormat` を使わない
- ロケール依存の API を既定ロケールで呼ばない。`DateTimeFormatter` には必ず `Locale` を渡す
- 文字集合を既定に任せない

- 機械強制: **active**（forbidden-apis の `base.txt` と `jdk-unsafe` 束）

### JAV-015 — 抑制は例外であって道具ではない

`@SuppressWarnings` は、**直前行の `// Waiver: WVR-NNNN`** と、有効な waiver 台帳の項目が
両方そろっているときにだけ書ける。`@SuppressWarnings("all")` は waiver があっても許可しない。
ファイル単位・ディレクトリ単位の抑制、静的解析の除外設定、lint の baseline は禁止。

🔴 **ここが Java の弱いところである。** `@SuppressWarnings` はコンパイラの機能であり、
言語の側で禁止できない（Rust の `forbid` に相当するものが無い）。本リポジトリは
検査と台帳という**人手の手続き**で補っている。**塞げていないことを書かずに厳格さを主張しない。**

- 機械強制: **active**（CNF-002 が waiver ID の無い抑制を拒否、CNF-009 が期限切れ waiver を拒否）
- 機械強制: **不能**（`@SuppressWarnings` という機能そのものの禁止）

---

## 3. Swing 規約（SWG-0xx）

Swing は継承前提・可変・スレッド制約つきの API であり、放っておくと憲章の外側に
第 2 の実装経路が育つ。ここは特に細かく縛る。

### SWG-001 — Swing 操作は EDT の上だけで行う

Swing 部品の生成・変更・表示はすべて EDT 上で行う。起動は
`SwingUtilities.invokeLater(...)` から始める。EDT を長時間ブロックしない。

- 機械強制: **active**（EDT 以外のスレッドを作れないこと＝JAV-013 の ArchUnit 規則が担保）
- 機械強制: **不能**（「いま EDT の上か」の静的判定。実行時の `SwingUtilities.isEventDispatchThread()`
  による表明は導入していない）

### SWG-002 — UI は業務判断を持たない

表示文字列の整形・状態遷移・検証・永続化の呼び出しを UI に置かない。
UI が受け取るのは `ClockFace` のような**すでに決まった値**だけである。

- 機械強制: **active**（`java.time.format` と `java.util.prefs` の使用箇所を ArchUnit が限定する）

### SWG-003 — UI 状態の反映は render 経路だけ

ボタンの有効・無効、最前面表示のような UI 状態の反映は、`render` で始まるメソッドの中でだけ行う。
リスナやイベントハンドラの中に `setEnabled(...)` を散らさない。

```java
private void renderState(StopwatchView view) {   // ✅ 反映は 1 か所
    start.setEnabled(view.canStart());
}

start.addActionListener(event -> pause.setEnabled(true));  // ❌ 反映が散る
```

- 機械強制: **active**（CNF-004）

### SWG-004 — レイアウトはレイアウトマネージャで行う

`setLayout(null)` と `setBounds(...)` による絶対配置を使わない。
`BorderLayout` / `GridBagLayout` / `BoxLayout` / `FlowLayout` を使う。

- 機械強制: **planned**（検出規則は未実装。レビュー事項）

### SWG-006 — 文字は必ずアンチエイリアスで描く

Swing の文字描画ヒントは**デスクトップ環境から渡される**。渡されない環境
（`awt.font.desktophints` が `null`）では、`JLabel` はアンチエイリアス無しで描かれる。

実測（この作業環境）: 素の `JLabel` は **2 階調**（白と黒だけ）、ヒントを付けると **249 階調**。

- テキスト部品（`JLabel` / `JTextField`）は `TextRendering` を通してのみ作る。直接 `new` しない
- 自前描画（`paintComponent`）でも同じヒントを立てる。描き方を 2 通りにしない
- LCD サブピクセルは採らない。背景色を利用者が選ぶので、色の付いた地で色にじみが出る
- 「大きく描いて縮小する」方式は採らない。Java2D の AA は 249 階調を作るのに対し、
  4 倍の supersampling は原理的に 17 階調しか作れない（4×4 の被覆率しか無い）うえ、
  16 倍の面積を描くことになる

- 機械強制: **active**（CNF-012 が直接の `new` を拒否する）

---

### SWG-005 — タイマーの刻みは時刻の正本ではない

`javax.swing.Timer` の刻みは**再描画のきっかけ**であって、時刻の源ではない。
表示する値は毎回 application 層から取り直す。残り時間を「毎回 1 減らす」形で持たない
（UI スレッドが遅れた分だけ静かにずれるため）。

- 機械強制: **active**（現在時刻を UI で読めないこと＝ARC-007 の forbidden-apis が担保）
- 機械強制: **planned**（カウンタ減算そのものの検出。M1 でタイマーを実装するときに再評価する）
