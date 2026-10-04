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
| （未作成） | `The window is the clock. No frame, no buttons until you hover.` |
| （未作成） | `Thirty bundled typefaces.` |
| （未作成） | `Pick the text and background colours.` |
| （未作成） | `Settings apply as you change them.` |

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
| （未作成） | `窓そのものが時計です。ポインタを載せるまで、枠もボタンもありません。` |
| （未作成） | `30 種類の同梱書体。` |
| （未作成） | `文字色と背景色を選べます。` |
| （未作成） | `設定は変えたその場で反映されます。` |

## スクリーンショット（未作成・決めること）

Store は 1 枚以上を求め、4 枚以上・1366×768 以上の PNG を推奨する。Clock の窓は小さい（施主の常用の設定で 212×133）ので、
窓だけを撮っても寸法に届かない。README の画像（`docs/images/`）は幅 478〜600 で、そのままでは使えない。

決めること:

| 点 | 案 |
| --- | --- |
| どう寸法に届かせるか | 1920×1080 の単色の地に、時計の窓と設定モーダルを実寸で置いた 1 枚にする。地の色は時計の背景と対になる色にする |
| 何で描くか | 製品の描画をそのまま使う。README の画像を撮った経路（ADR と gate-proofs を確認してから）に、地を足す。**絵を手で描き直さない** |
| 言語 | 英語の掲載には英語の UI、日本語の掲載には日本語の UI で撮る。Loupe は UI が英語だけだったので 1 組だったが、Clock は 2 組になる |
| 枚数 | 4 枚 × 2 言語。時計だけ／書体の画面／色の画面／設定モーダル |

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
- スクリーンショットは 1 枚も作っていない
