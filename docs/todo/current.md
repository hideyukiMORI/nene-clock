# いまのタスク — NeNe Clock

> タスク状態の正本は GitHub Issue。このファイルはリポジトリ単体で読めるようにするための写しであり、
> Issue と食い違ったら Issue が正しい。

## M0（進行中）

- [x] Gradle マルチモジュール骨格と規範文書一式
- [x] `validateConformance`（CNF-001..010）と単体テスト
- [x] forbidden-apis による決定性の強制（ARC-007）
- [x] ArchUnit によるレイヤ・プラットフォーム隔離（ARC-002 / ARC-003）
- [x] 時計表示（FR-001..FR-006）と設定永続化（FR-040..FR-042）
- [x] CI（`quality` ジョブが `./gradlew check` を実行）
- [x] GitHub へ push し、`main` の ruleset を設定して [quality/gate-proofs.md](../quality/gate-proofs.md) に記録する
- [ ] WSLg での表示確認を記録する（QLT-012）— #14

## M0.1（設定 GUI・完了）

- [x] FR-043 書体・FR-044 文字色を値型として持つ — #23
- [x] FR-042 保存形式 v2 と v1 からの移行（既存の設定を失わない）— #23
- [x] FR-045 設定画面から 7 項目すべてを変更でき、即座に反映され保存される — #24
- [x] 実行環境の書体一覧を読める場所を `:adapters:font-catalog` 1 つに限定 — #24

## M0.2（見た目の作り直し・進行中）

- [ ] FR-043 Google Fonts 30 書体を同梱し、OS の書体をやめる — #34
- [ ] 背景色を設定できるようにし、保存形式を上げる — #35（仕様 ID は同 PR で新設する）
- [ ] 枠なしウィンドウ・ホバークローム・設定モーダル — #36（仕様 ID は同 PR で新設する）

## M1（未着手）

- [ ] FR-010 ストップウォッチ（単調時刻・状態機械）— #15
- [ ] FR-020 カウントダウンタイマー（目標時刻からの計算）— #16
- [ ] FR-050 残り 2 タブ（Stopwatch / Timer）— #17
- [ ] SWG-004 / SWG-005 の機械強制を `planned` → `active` — #18
- [ ] QLT-011 の SHA-256 dependency verification を `planned` → `active` — #19
