# Microsoft Store の掲載文と年齢区分の根拠 — NeNe Clock

> Issue #120 / ADR 0020。Partner Center への入力は施主が行う。ここは入力する文面の正本である。
> 形は NeNe Loupe の `docs/release/store-listing.md` に合わせた。入力欄の上限は Loupe が 2026-10-03 に確かめた値を使っている
> （製品の機能は最大 20 件、キーワードは最大 7 個・各 40 文字以内、短い説明は 270 文字以下を推奨、`runFullTrust` の理由は 500 文字まで）。
> 🔴 **Partner Center の画面の操作は、施主の画面を見ずに言い切らない**（Loupe で案内が 3 か所違った）。

掲載の言語は英語と日本語の 2 つ（施主の決定・2026-10-04）。アプリの表示も日英を持つ（FR-048）。
各文は README・SPECIFICATION・実装と突き合わせた。できないことは書いていない。

## 英語（en-US）

### Product name

```text
NeNe Clock
```

### Short description

```text
A quiet desktop clock. No frame, no buttons, no tray icon: just the time, in the typeface and colours you choose.
```

### Description

```text
NeNe Clock is a quiet desktop clock for Windows. The window is the clock: there is no title bar, no frame and no tray icon. You see the time, in the typeface and colours you chose, and nothing else.

Drag it anywhere on your screen. Hover over it and two small icons appear: a gear for the settings and a cross to quit. Move the pointer away and they fade out again.

• 30 bundled typefaces. Sans, serif, mono, display, retro and handwritten faces, so the clock looks the same on every PC and never depends on the fonts you have installed.
• Your colours. Pick the text colour and the background from 24 presets, a colour picker or a hex code. A contrast readout tells you when the two become hard to read.
• 12-hour or 24-hour time, with or without seconds, with or without the date.
• Size and padding. Set the text from 24 to 160 pt and choose how much space surrounds it. The window sizes itself to fit; you never resize it by hand.
• Always on top, if you want it.
• Settings apply as you change them and are remembered. There is no OK or Apply button.
• English and Japanese.
• Private. No network connections, no account, no telemetry. The only thing saved is your display settings, on your own PC.

Good to know

• NeNe Clock shows the time. It has no alarm, timer or stopwatch.
• Nothing keeps running in the background after you close it.
• Windows 10 version 2004 or later, 64-bit.
• Open source under the MIT License: https://github.com/hideyukiMORI/nene-clock
```

### Product features

```text
A frameless clock: the window is the time
30 bundled typefaces in six moods
Text and background colours from presets, a picker or a hex code
12-hour or 24-hour, seconds and date optional
Text size from 24 to 160 pt, adjustable padding
Always on top, switchable
English and Japanese
No network connections, no account, no telemetry
```

### Search terms

```text
desktop clock
clock
digital clock
always on top clock
minimal clock
clock widget
time
```

### Screenshot captions

| 画像 | Caption |
| --- | --- |
| `docs/images/store/store-1-clocks.png` | `Your clock, your way: 30 typefaces, any colours, any size.` |
| `docs/images/store/store-2-typefaces-en.png` | `Thirty bundled typefaces.` |
| `docs/images/store/store-3-colours-en.png` | `Pick the text and background colours.` |
| `docs/images/store/store-4-settings-en.png` | `Settings apply as you change them.` |

## 日本語（ja-JP）

### 製品名

```text
NeNe Clock
```

### 短い説明

```text
静かなデスクトップ時計。枠もボタンもトレイアイコンも無く、選んだ書体と色で時刻だけがそこにあります。
```

### 説明

```text
NeNe Clock は、Windows 用の静かなデスクトップ時計です。窓そのものが時計で、タイトルバーも枠もトレイアイコンもありません。選んだ書体と色で、時刻だけが表示されます。

画面の好きな場所へドラッグしてください。ポインタを載せると小さなアイコンが 2 つ現れます。歯車が設定、×が終了です。ポインタを離すと消えます。

• 30 種類の同梱書体。サンセリフ・セリフ・等幅・ディスプレイ・レトロ・手書き。どの PC でも同じ見た目になり、PC に入っているフォントに左右されません。
• 好きな色で。文字色と背景色を、24 色のプリセット・カラーピッカー・16 進のコードから選べます。2 色が読みにくくなると、コントラストの表示が知らせます。
• 12 時間表記と 24 時間表記。秒と日付は、表示するかどうかを選べます。
• 大きさと余白。文字は 24〜160 pt、まわりの余白も選べます。窓の大きさは自動で合うので、手で広げる必要はありません。
• 常に最前面に表示できます。
• 設定は変えたその場で反映され、保存されます。「OK」や「適用」のボタンはありません。
• 日本語と英語に対応しています。
• プライバシー。ネットワークに接続せず、アカウントも利用状況の送信もありません。保存するのは表示の設定だけで、お使いの PC の中に置きます。

ご注意

• NeNe Clock は時刻を表示するアプリです。アラーム・タイマー・ストップウォッチの機能はありません。
• 閉じたあとに、裏で動き続けるものはありません。
• Windows 10 version 2004 以降（64 bit）。
• MIT License のオープンソースです: https://github.com/hideyukiMORI/nene-clock
```

### 製品の特長

```text
枠の無い時計。窓そのものが時刻
6 つの系統・30 種類の同梱書体
文字色と背景色を、プリセット・カラーピッカー・16 進コードで
12 時間／24 時間表記。秒と日付は表示を選べる
文字の大きさは 24〜160 pt。余白も調整できる
常に最前面（切り替え可）
日本語と英語
ネットワーク接続なし、アカウントなし、利用状況の送信なし
```

### 検索語

```text
時計
デスクトップ時計
デジタル時計
最前面
シンプル
ウィジェット
時刻
```

### スクリーンショットの説明

| 画像 | 説明 |
| --- | --- |
| `docs/images/store/store-1-clocks.png` | `書体・色・大きさ・余白を選んで、自分の時計に。` |
| `docs/images/store/store-2-typefaces-ja.png` | `30 種類の同梱書体。` |
| `docs/images/store/store-3-colours-ja.png` | `文字色と背景色を選べます。` |
| `docs/images/store/store-4-settings-ja.png` | `設定は変えたその場で反映されます。` |

## スクリーンショット（2026-10-04 に作成）

Store は 1 枚以上を求め、4 枚以上・1366×768 以上の PNG を推奨する。Clock の窓は小さいので、
1920×1080 の単色の地（`#DCD6CA`）に、**実機で撮った窓を実寸のまま**置いた（施主の了承・2026-10-04）。7 枚ある。

| ファイル | 中身 | 言語 |
| --- | --- | --- |
| `store-1-clocks.png` | 時計 12 個。書体・文字色・背景色・文字の大きさ・余白・表記をそれぞれ変えてある（下の表） | 共通（時計に言語は無い） |
| `store-2-typefaces-{en,ja}.png` | 書体の画面 | 英／日 |
| `store-3-colours-{en,ja}.png` | 文字色の画面 | 英／日 |
| `store-4-settings-{en,ja}.png` | 時計（JetBrains Mono 96pt）と設定モーダル | 英／日 |

1 枚目は「選べる幅」を見せる（施主の指示・2026-10-04）。12 個とも実際の設定で起動して撮った窓で、大きさの違いは設定の違いそのものである。

組み方は、3 つの立場（Store の掲載・紙面の組み・書体と配色）からの批評を受けて決めた。

- **段ごとに高さを揃え、左右の端を合わせた 1 つの塊にする。** 幅 1680（x 120〜1800）、段の高さは 340 / 240 / 190、段の間は 29。
  高さは余白の設定で合わせた（拡大・縮小はしていない。誤差は ±1px）。段の中の間隔は段ごとに等間隔（約 28〜31）
- **主役は左上の 1 つ**（既定の書体 JetBrains Mono を、いちばん大きく、墨の地で）
- **配色はアプリ自身の色（生成り `#F5F2EB`・墨 `#1A1917`・琥珀 `#D08C3F`）を軸にし、色相は 1 つずつ**（深緑・えんじ・煉瓦・藍）。純粋な黒と白は使わない
- 明るい時計と暗い時計を、各段で交互に置く
- どの時計もコントラストは 3:1 を超える組み合わせにした（アプリが警告を出す境目）。比率は手計算で、アプリの表示では確かめていない
- 地は暖かい石の色 `#DCD6CA`。2〜4 枚目も同じ地にした

| 段 | 書体 | 大きさ | 余白 | 文字色 / 背景色 | 表記 | 窓の大きさ |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | JetBrains Mono | 150 | 41 | `#F5F2EB` / `#1A1917` | 24 時間・秒・日付 | 802 × 340 |
| 1 | Bebas Neue | 160 | 45 | `#1A1917` / `#D08C3F` | 24 時間・日付 | 376 × 340 |
| 1 | Cinzel | 130 | 55 | `#D9C28A` / `#1F3A34` | 24 時間・日付 | 441 × 340 |
| 2 | Caveat | 110 | 50 | `#24406E` / `#F4EBCF` | 12 時間 | 483 × 239 |
| 2 | Playfair Display | 110 | 23 | `#F3E8D2` / `#5A1E24` | 24 時間・日付 | 339 × 240 |
| 2 | Abril Fatface | 90 | 38 | `#1F3A5C` / `#D6DEE2` | 24 時間・日付 | 340 × 240 |
| 2 | VT323 | 88 | 75 | `#FFB000` / `#1B1200` | 24 時間・秒 | 430 × 239 |
| 3 | Orbitron | 64 | 54 | `#8FE3CF` / `#10242B` | 24 時間・秒 | 454 × 189 |
| 3 | Inter | 48 | 65 | `#1A1917` / `#FBF9F4` | 24 時間 | 268 × 189 |
| 3 | Anton | 84 | 31 | `#F5F2EB` / `#B5482A` | 24 時間・秒 | 354 × 189 |
| 3 | Space Mono | 64 | 30 | `#1E2F5C` / `#E9EEF2` | 24 時間・日付 | 255 × 190 |
| 3 | Oswald | 64 | 47 | `#D08C3F` / `#1A1917` | 24 時間 | 239 × 190 |

採らなかった案: 見出しの文字を絵に入れる（掲載の立場からは推されたが、塊を縮めることになり、主役の時計が見出しの役をする）／
時計を重ねる・傾ける（角丸と余白が隠れる）／時計ごとに鮮やかな色を割り当てる（「静かな時計」と合わない）。
Playfair Display は 12 時間表記にしない（数字がオールドスタイルで、先頭の 0 が小文字の o に見える）。

撮り方（README の画像と同じ経路・PR #71）: WSLg（`DISPLAY=:0`）で main `e745b77`（版 1.0.0）の `installDist` を起動し、
Linux 側の保存を書き換えて設定を与え、`import -window` で窓を撮った。モーダルは XTest でポインタを載せ、歯車と行を押して開いた。
撮ったあと、保存は元へ戻した。

手を入れた所（絵は描き直していない）:

- 窓を単色の地の上に並べた。拡大・縮小はしていない
- `import -window` は角丸の外側を黒で返すので、角を丸く切り抜いて地の色が見えるようにした（時計は半径 13、モーダルは半径 10 で切った。
  製品の角丸の値と厳密には合わせていない）
- 4 枚目は、時計とモーダルを別々に撮って並べた。**モーダルの中のプレビューと時計の時刻が 1 分ほどずれている**（19:35:59 と 19:36:50）。
  実際の画面では同じ時刻を示す

確かめていないこと: Windows での見た目との差（WSLg で撮った。書体は同梱なので同じはずだが、比べていない）。
UI を変えたら撮り直す必要がある（画像はコードから生成できない）。

## 提出の控え

提出する MSIX は、次の実行の成果物である。**認定のあと、GitHub Release にはこの実行の MSI・zip・`.deb` をそのまま出す**（ADR 0022）。
提出の前に main が進んで作り直したら、この表を書き換える。

| 項目 | 値 |
| --- | --- |
| 実行 | [37199220858](https://github.com/hideyukiMORI/nene-clock/actions/runs/37199220858)（2026-10-04・main `3b2d13e`・版 1.0.0） |
| 成果物 | `nene-clock-store-msix` の中の `NeNe-Clock-store.msix`（32 MB） |
| MSIX の SHA-256 | `3f14bf8e0f8e1b5c17cd5283a32e428a71616abff29fd6150ccbe8731bfe4a2a` |
| マニフェスト | `HideyukiMori.NeNeClock` / `CN=C37230AA-B52D-403B-9BFD-E7980F088422` / `Hideyuki Mori` / 版 `1.0.0.0` |
| 同じ実行の MSI の SHA-256 | `0d5375c031638af125746f87abc0e73b9185a553df84e9fb130b432ef45a7b84` |
| 同じ実行の zip の SHA-256 | `22dc9c9f47eebb0f6de805ee6739e9f2ba97bf54175a2ca7f0bf65b96f249ce5` |
| WACK | 2026-10-04 に通した: PASS 22・WARNING 1（高 DPI）・任意の検査の FAIL 1（ブロック済みの実行可能ファイル）。`docs/quality/gate-proofs.md` 第 29 節 |
| 状態 | **まだ提出していない** |

成果物の保存期間は 90 日（2027-01-02 ごろまで）。

## そのほかの入力

| 項目 | 入れる内容 | 根拠 |
| --- | --- | --- |
| カテゴリ | 「ユーティリティとツール」を提案する。施主が選ぶ | 時刻を表示する道具 |
| 価格 | 無料を提案する。施主が決める | README は無料で配っている |
| プライバシーポリシーの URL | `https://github.com/hideyukiMORI/nene-clock/blob/main/PRIVACY.md` | `PRIVACY.md`（Issue #118。統合されてから有効になる） |
| サポートの連絡先 | `https://github.com/hideyukiMORI/nene-clock/issues` | `PRIVACY.md` の連絡先と同じ |
| Web サイト | `https://github.com/hideyukiMORI/nene-clock` | |
| 著作権 | `© 2026 Hideyuki Mori` | `LICENSE` の著作権表示と同じ |
| デバイス ファミリ | 「Windows 10/11 Desktop」だけ | 包んだ Win32 のアプリ |
| アプリ タイル アイコン | 300×300 の PNG。`./gradlew writeAppIcons` が `app/build/icons/store-listing-300.png` に書き出す | #117 |

## 申請オプションに貼る文面

どの段落も 1 行で、途中に改行を入れていない。端末の表示からではなく、このファイルをエディタで開いてコピーすること
（端末からコピーすると、折り返しの位置に改行や空白が入る）。

### 認定の注意書き（Notes for certification）

```text
To test: launch the app. A small frameless window shows the current time. Drag it anywhere to move it. Hover over the window: a gear and a cross appear in the top-right corner. The gear opens the settings (typeface, size, colours, 12/24-hour, seconds, date, always on top, language, padding); every change applies immediately. The cross quits the app. No account or sign-in is needed. The app makes no network connections.
```

### `runFullTrust` が必要な理由

上限は 500 文字（Loupe で確かめた値）。下の文面は 394 文字。

```text
NeNe Clock is an existing desktop app (Java 21 / Swing, bundled with its own runtime by jpackage) packaged with MSIX. runFullTrust is required because the packaged executable is a classic Win32 process (EntryPoint "Windows.FullTrustApplication"). It only draws a clock window and saves its display settings to the per-user registry. No elevation, no network connections, no services or drivers.
```

## 年齢区分（IARC）のアンケートに答えるための事実

アンケートの設問そのものは Partner Center の画面で出る。ここには、答えの根拠になる事実だけを置く。

| 問われそうなこと | 事実 | 根拠 |
| --- | --- | --- |
| 暴力・性的な内容・恐怖・賭博・薬物・粗野な言葉 | 無い。表示するのは時刻と日付だけ | `SPECIFICATION.md`、実装 |
| 利用者同士のやり取り（チャット・投稿・共有） | 無い | 通信をしない（下） |
| インターネットへの接続 | しない。production のコードに通信の API の使用が無い | `PRIVACY.md` |
| 個人情報の収集・第三者への提供 | しない | `PRIVACY.md` |
| 位置情報 | 使わない。読むのは端末のタイムゾーンの設定だけ | 実装 |
| アプリ内の購入・デジタル商品の販売・広告 | 無い | 実装に該当の機能が無い |
| Web の内容を制限なく表示する機能（ブラウザ） | 無い | 実装に該当の機能が無い |

## 確かめていないこと

- この文面で審査に通るか。Loupe は同じ形で通ったが、Clock では申請していない
- 入力欄の上限は Loupe のときの値である。変わっているかもしれない
- プライバシーポリシーの設問（「個人情報へのアクセス、収集、または送信を行いますか」）にどう答えるか。
  Loupe は画面の画素を読むので「はい」にした。Clock は画面を読まない。規約 10.5.1 は Win32 を包んだ製品にポリシーを常に求めるので、
  URL は入れる。設問の答えは、施主の画面で文言を見てから決める
