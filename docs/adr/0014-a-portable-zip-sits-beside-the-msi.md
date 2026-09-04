# ADR 0014 — MSI の隣にポータブル zip を置く。1 ファイルの exe は作らない

- 状態: 受理
- 日付: 2026-09-04
- Issue: #68
- 影響する規則: QLT-005 / ARC-012 / ADR 0013

## 文脈

施主要件: インストーラーではなく、入れずに使える形も欲しい（「スタンドアロンの exe」）。

## 決定

1. **`jpackage` の app-image を zip にして配る。** 展開して `NeNe Clock.exe` を押すだけで動き、
   レジストリにもスタートメニューにも何も書かない
2. **同じ task（`packageInstaller`）が app-image → zip → MSI の順に作る。** MSI は app-image から作るので、
   結線（jar・main クラス・アイコン・モジュール集合）は 1 か所で決まる
3. Release には MSI と zip と SHA-256 が並ぶ。入れたい人は MSI、入れたくない人は zip
4. **1 ファイルの exe は作らない。** 下の表のとおり、いまの Java に素直な道が無い

## 却下した選択肢

| 選択肢 | 却下の理由 |
| --- | --- |
| GraalVM Native Image で 1 ファイルの exe | Swing / AWT の対応は Windows ではまだ実験的。同梱書体・Preferences・Java2D に個別の設定が要り、動くかは試すまで分からない。ツールチェーンの変更でもある。安定してから別 Issue で試す |
| Launch4j 等で jar を exe に包む | 利用者の PC に Java が入っている前提。要件に反する |
| 自己展開 zip（7-Zip SFX） | 起動のたびに一時フォルダへ展開する。SmartScreen の印象も悪い |
| zip を別の Gradle task で作る | 配布物を作る経路が 2 つになる（ARC-012） |

## 強制

- **active**: Linux で `packageInstaller` が zip を作り、中に `bin/NeNe Clock` と `lib/runtime` がある（gate-proofs 第 20 節）
- **planned**: Windows ランナーで zip と MSI の両方ができる（PR の workflow で走る）

## 結果

- zip は 30 MB 台、展開すると約 90 MB
- 設定の保存先は MSI 版と同じ（Windows のレジストリ）。zip 版と MSI 版で設定は共有される
- 「exe 単体」では動かない。隣の `runtime/` と `app/` が要る
