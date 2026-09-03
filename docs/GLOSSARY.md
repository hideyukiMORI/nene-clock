# 用語集 — NeNe Clock

> Status: normative（規範）/ 2026-09-03 初版
> ここに載っている語は、コード・ドキュメント・Issue で**同じ意味**で使う。

| 語 | 意味 | 型／場所 |
| --- | --- | --- |
| 時計表示（clock face） | いま画面に出す整形済みの文字列の組 | `ClockFace`（`:core:application`） |
| 日付行（date line） | 時刻の下に出す日付。表示しない状態も型で表す | `DateLine`（`:core:application`） |
| 利用者設定（user settings） | 保存される表示設定の全体。表示に関する事実の唯一の所有者 | `UserSettings`（`:core:domain`） |
| 壁時計ポート（wall clock port） | 現在時刻を得る唯一の窓口 | `WallClockPort`（`:core:application`） |
| 設定ストアポート | 設定の永続化の唯一の窓口 | `SettingsStorePort`（`:core:application`） |
| 結果（outcome） | 期待される成功・失敗を表す閉じた型 | `*Outcome`（`sealed interface`） |
| 拒否理由（rejection / failure） | 結果に添える閉じた理由の集合 | `*Rejection` / `*Failure`（`enum`） |
| 反映（render） | 決まった値を Swing 部品へ写す操作。UI 状態を変えてよい唯一の場所 | `render*` メソッド（`:ui:swing`） |
| 刻み（tick） | 再描画のきっかけ。時刻の正本ではない | `ClockTicker`（`:ui:swing`） |
| 隔離区画 | 可変性を許した唯一の場所 | Swing 部品の内部（ARC-005） |
| 規約検査（conformance） | NeNe Clock 固有の自作ゲート | `validateConformance`（CNF-001..010） |
| waiver | 1 つの規則に対する期限付きの狭い例外 | `docs/waivers/WVR-NNNN-*.md` |
| 機械強制の状態 | active / planned / 不能 / 不採用 | `docs/QUALITY_GATES.md` の強制マトリクス |

## 使ってはいけない語

`Manager` / `Helper` / `Util` / `Utils` / `Common` を型名の語尾に使わない（JAV-010）。
役割を語る名前が思いつかないときは、その型が 2 つの責務を持っている可能性が高い。
