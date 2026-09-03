# モジュール構成と依存規則 — NeNe Clock

> Status: normative（規範）/ 2026-09-03 初版
> パッケージルート: `io.github.hideyukimori.neneclock`（[ADR 0002](adr/0002-initial-build-toolchain.md)）

モジュールグラフはアーキテクチャの一部である。**パッケージの命名規約だけでは依存の境界にならない。**
承認された一覧の機械可読な正本は `config/architecture/module-graph.txt` で、
`validateConformance`（CNF-010）がそれと実際の Gradle グラフを突き合わせる。

---

## 1. 承認されたモジュール

```text
:app
    合成ルート。ポートに実装を結び、UI を起動する

:ui:swing
    Swing の画面。状態を描き、意図を発行する

:adapters:system-time
    現在時刻を JDK から読める唯一のモジュール

:adapters:preferences
    java.util.prefs に触れる唯一のモジュール

:adapters:font-catalog
    実行環境の書体一覧を読める唯一のモジュール

:core:application
    ポートの宣言、表示値の生成、結果型

:core:domain
    値・不変条件・閉じた選択肢。JDK 標準ライブラリ以外に依存しない

:quality:architecture-tests
    ArchUnit によるアーキテクチャ検査。テスト専用で production 依存を持たない
```

空の「将来用モジュール」を作らない。上の名前は予約された行き先であり、
最初の具体的な責務が発生したときにだけ作る。

---

## 2. 許可された依存グラフ

```text
:core:domain
    -> JDK 標準ライブラリのみ

:core:application
    -> :core:domain

:adapters:system-time
    -> :core:application, :core:domain

:adapters:preferences
    -> :core:application, :core:domain

:adapters:font-catalog
    -> :core:application, :core:domain

:ui:swing
    -> :core:application, :core:domain

:app
    -> すべて（明示的な合成のためだけに）
```

ここに無い依存はすべて禁止である。とくに:

- core -> Swing / AWT: **禁止**（ArchUnit。JDK 同梱なのでモジュールグラフでは塞げない）
- domain -> application: 禁止
- application -> アダプタ: 禁止
- ui -> アダプタ: 禁止
- アダプタ -> 別のアダプタ: 禁止
- 何か -> :app: 禁止

`:quality:architecture-tests` は**ビルド専用の例外**である。テスト依存としてすべてのモジュールを
読むが、production コードがこのモジュールに依存することはできない。

---

## 3. 各モジュールの責務

### `:core:domain`

意味の正本を持つ。

- 表示に関わる値型（`FontFamily`・`FontSize`・`FontColor`・`SettingsSchemaVersion`）
- 閉じた選択肢（`ClockFormat`・`SecondsVisibility`・`DateVisibility`・`WindowTopmost`）
- 不変の設定値（`UserSettings`）
- 拒否理由と結果型（`FontFamilyRejection` / `FontSizeRejection` / `FontColorRejection` と対応する `*Outcome`）

不変条件を持つ値型は `record` ではなく `final class` にする。`record` の正準コンストラクタは
公開度を下げられず、生成経路が 2 本になるため（JAV-007）。

UI 状態・シリアライズ注釈・永続化・現在時刻を持たない。
**production 依存の追加そのものをビルドが拒否する**（ARC-003）。

### `:core:application`

振る舞いの調整を持つ。

- ポート（`WallClockPort`・`SettingsStorePort`）
- 表示値の生成（`ClockFaceQuery` → `ClockFace` / `DateLine`）
- 結果型（`SettingsLoadOutcome`・`SettingsSaveOutcome` とその失敗列挙）

Swing・`java.util.prefs`・ファイル・ネットワークを知らない。
**時刻の整形（`java.time.format`）を行ってよいのはこの層だけ**である（ARC-011）。

### `:adapters:system-time`

`WallClockPort` の実装をただ 1 つ持つ。
🔑 **`config/forbiddenapis/determinism.txt` を適用しない唯一のモジュール**であり、
その差分は `adapters/system-time/build.gradle.kts` に明示的に書かれている。
既定タイムゾーンを読むのもこの区画に閉じる（ARC-007）。

### `:adapters:preferences`

`SettingsStorePort` の実装をただ 1 つ持つ。保存形式は版を持ち（ARC-009）、
読めない値は既定値へ黙って落とさず、型のある失敗として返す。
版の移行もこのモジュールに閉じる（ADR 0003）。`load()` は保存領域を書き換えない。

### `:adapters:font-catalog`

`FontCatalogPort` の実装をただ 1 つ持つ。
🔑 **`config/forbiddenapis/platform.txt` を適用しない唯一のモジュール**であり、
`GraphicsEnvironment` を触れるのはここだけである。時計は読めないままなので
`determinism.txt` は適用したままにしてある。

### `:ui:swing`

画面を組み立てて描く。`ClockPanel` / `MainFrame` / `ClockTicker`。
Swing 部品は**継承せず内包する**（構築中に自分のメソッドが呼ばれる形を作らないため）。
UI 状態の反映は `render*` メソッドからのみ行う（SWG-003）。

### `:app`

合成ルート。実装をポートへ結び、EDT 上で UI を起動する。業務判断を置かない（ARC-006）。

---

## 4. パッケージ規則

モジュール内のパッケージは、まず領域の意味で分け、次に技術的な役割で分ける。
汎用の捨て場を作らない。

禁止するパッケージ名の構成要素: `utils` / `helpers` / `managers` / `misc` / `common`（CNF-001）。

他モジュールの内部実装パッケージを import しない。公開された API だけを使う。
