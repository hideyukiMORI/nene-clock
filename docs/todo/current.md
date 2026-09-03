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
- [ ] WSLg での表示確認を記録する（QLT-012）

## M1（未着手）

- [ ] FR-010 ストップウォッチ（単調時刻・状態機械）
- [ ] FR-020 カウントダウンタイマー（目標時刻からの計算）
- [ ] FR-050 残り 3 タブ（設定 UI を含む）
- [ ] SWG-004 の機械強制（`setLayout(null)` / `setBounds` の検出）を `planned` → `active`
- [ ] SWG-005 の機械強制（減算カウンタの検出）を `planned` → `active`
- [ ] QLT-011 の SHA-256 dependency verification を `planned` → `active`
