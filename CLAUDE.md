# CLAUDE.md — NeNe Clock

Claude Code / AI エージェントがこのリポジトリで作業するための**中核ハンドブック**。
簡潔な英語版の入口は [AGENTS.md](AGENTS.md)。詳細の正本は `docs/` にあり、ここには複製しない。

---

## 0. まず読むもの（production コードに触れる前に必ず）

1. [SPECIFICATION.md](SPECIFICATION.md) — 何を作るか（FR-NNN）
2. [docs/ARCHITECTURE_CONSTITUTION.md](docs/ARCHITECTURE_CONSTITUTION.md) — 憲章（ARC-NNN）
3. [docs/PROJECT_LAYOUT.md](docs/PROJECT_LAYOUT.md) — モジュールと依存方向
4. [docs/CODING_RULES.md](docs/CODING_RULES.md) — Java / Swing 規約（JAV-NNN / SWG-NNN）
5. [docs/QUALITY_GATES.md](docs/QUALITY_GATES.md) — **いま何が機械で守られているか**（QLT-NNN / CNF-NNN）
6. [docs/DEVELOPMENT_WORKFLOW.md](docs/DEVELOPMENT_WORKFLOW.md) — 手順
7. [docs/GLOSSARY.md](docs/GLOSSARY.md) — 用語
8. 該当する ADR（`docs/adr/`）と有効な waiver（`docs/waivers/`）

---

## 1. このリポジトリの統治原則

> **一つのことを実現する方法を 1 つに固定し、そのことを人の記憶ではなく機械に守らせる。**

その帰結として、次の 3 つを常に守る。

1. **正典の経路を先に特定してから編集する。** 「ここで書いたほうが早いから」で第 2 の経路を作らない（ARC-001 / ARC-012）
2. **ゲートを弱めて通さない。** 検査が落ちたらコードを直す。閾値・除外・重大度を触るのは ADR 相当の判断（QLT-010）
3. **`planned` を `active` と書かない。** 未実装の強制を実装済みに見せるのは、この規約体系で唯一「壊す」行為（[ADR 0001](docs/adr/0001-strictness-is-mechanically-enforced.md)）

---

## 2. このプロジェクトで間違えやすい所

### 現在時刻を読む場所は 1 つしかない

`LocalDateTime.now()` / `System.nanoTime()` / `Clock.systemDefaultZone()` /
`ZoneId.systemDefault()` は **`:adapters:system-time` 以外では書けない**（forbidden-apis が落とす）。
時刻が要るなら `WallClockPort` を注入する。**テストソースにも同じ禁止がかかる。**

### Swing と Preferences は JDK 同梱なので、依存を宣言しなくても import できてしまう

だから ArchUnit（`:quality:architecture-tests`）が塞いでいる。
`build.gradle.kts` に依存が書かれていないことを「使えない証拠」と読まないこと。

### `default` を書いた瞬間に網羅性検査が死ぬ

`switch` に `default:` / `default ->` を書かない。選択肢が増えたらコンパイルが落ちるのが正しい状態（JAV-002 / CNF-003）。

### 検証を伴う値型は `record` にできない

`record` の正準コンストラクタは公開度を下げられない（言語仕様）。
不変条件を持つ型は `final class` ＋ 非公開コンストラクタ ＋ 唯一のファクトリにする（JAV-007）。
`record` は「すでに検証済みの値の組」にだけ使う。

### UI 状態の反映は `render` で始まるメソッドの中だけ

`setEnabled(...)` / `setAlwaysOnTop(...)` をリスナの中に書くと CNF-004 が落とす。

### 期待される失敗は例外にしない

`sealed interface` の結果型で返す。広い `catch` は Checkstyle が拒否する（JAV-005）。

---

## 3. 検証コマンド

```bash
./gradlew check          # 唯一の完了定義（ローカルと CI で同じ）
./gradlew run            # 起動（WSLg なら Windows のデスクトップに窓が出る）
./gradlew spotlessApply  # 整形を直す（検査は spotlessCheck が check の中で行う）
```

開発中は最も狭い検査を使ってよい。

```bash
./gradlew :core:domain:test
./gradlew validateConformance
./gradlew :quality:architecture-tests:test
```

🔴 **`./gradlew check` が通っていないものを「できた」と報告しない。**
実行していないコマンドの結果を書かない。テストの失敗を隠さない。
テストが本当の欠陥を見つけたら、期待値ではなく production コードを直す。

---

## 4. 変更の進め方

[docs/DEVELOPMENT_WORKFLOW.md](docs/DEVELOPMENT_WORKFLOW.md) が正本。要約すると:

Issue → 正典経路の特定 → ブランチ → （設計を変えるなら先に ADR）→ 最小の実装 →
テスト → `./gradlew check` → 規則 ID ごとの自己レビュー → PR。

コミットは Conventional Commits（`type` と `scope` は英語、説明は日本語、末尾に `(#N)`）。

---

## 5. 完了報告の形

作業を終えたら必ず次を報告する。

```text
Issue / 規則 ID:
変更したファイルと振る舞い:
実行した検証コマンドと結果:
ドキュメント・スキーマの変更:
Waivers: none | WVR-NNNN
残るリスク:
```

調査だけを頼まれたときは、編集・コミット・push・PR 作成・外部状態の変更を行わない。

---

## 6. いまのマイルストーン

**M0**（時計と設定永続化、ゲート一式）。ストップウォッチ（FR-010）とタイマー（FR-020）、
残り 3 タブ（FR-050）は **M1** であり、まだ実装しない。
現在のタスクは [docs/todo/current.md](docs/todo/current.md)。
