# アーキテクチャ決定記録（ADR）

トレードオフのある判断を、**理由ごと**残す場所。正本ドキュメントが「いま何が規範か」を書くのに対し、
ADR は「なぜそう決めたか」「何を却下したか」を書く。

## 書き方

- 連番 4 桁 ＋ kebab の題名: `NNNN-short-kebab-title.md`
- `0000-template.md` を複製して書く
- 状態は `提案` / `受理` / `却下` / `置換（→ NNNN）`
- **却下した選択肢を必ず書く。** 再提案は、その理由への反論から始めること
- 受理された ADR を後から書き換えない。変えるときは新しい ADR で置き換える

## 一覧

| ADR | 題名 | 状態 |
| --- | --- | --- |
| [0001](0001-strictness-is-mechanically-enforced.md) | 厳格さは機械で強制する | 受理 |
| [0002](0002-initial-build-toolchain.md) | 初期ビルドツールチェーン | 受理 |
| [0003](0003-settings-schema-migration.md) | 設定スキーマの移行方針 | 受理 |
| [0004](0004-settings-state-owner.md) | 現在の設定の所有者を application に置く | 受理 |
| [0005](0005-composition-root-owns-the-terminal.md) | 端末とプロセス終了コードは合成ルートが持つ | 受理 |
| [0006](0006-typefaces-are-bundled-not-discovered.md) | 書体は同梱する。実行環境からは読まない | 受理 |
| [0007](0007-one-colour-type-two-roles.md) | 色の型は 1 つにし、役割は成分名で語る | 受理 |
| [0008](0008-the-window-is-the-clock.md) | 窓そのものを時計にする。枠と設定タブをやめる | 受理 |
