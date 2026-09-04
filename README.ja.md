# NeNe Clock

<p align="center">
  <img src="docs/images/clock-default.png" width="478" alt="JetBrains Mono で 23:36:52 と日付を表示する NeNe Clock">
</p>

<p align="center">
  静かなデスクトップ時計。枠もボタンもトレイアイコンも無く、選んだ書体と色で時刻だけがそこにある。<br>
  Windows · ポータブル · インストール不要 · <a href="README.md">English</a>
</p>

<p align="center">
  <a href="#ダウンロード">ダウンロード</a> ·
  <a href="#使い方">使い方</a> ·
  <a href="#設定">設定</a> ·
  <a href="#ギャラリー">ギャラリー</a> ·
  <a href="#ソースからビルドする">ソースからビルドする</a> ·
  <a href="#このリポジトリの運営">このリポジトリの運営</a>
</p>

---

## ダウンロード

1. [**Releases ページ**](https://github.com/hideyukiMORI/nene-clock/releases/latest) から
   名前が **`-windows-portable.zip`** で終わるファイルを落とす。
2. 好きな場所に展開する。デスクトップでも、ドキュメントでも、USB メモリでもよい。`NeNe Clock` というフォルダが 1 つできる。
3. そのフォルダを開いて **`NeNe Clock.exe`** をダブルクリックする。

これだけ。インストーラーも管理者権限も要らず、**Java も要らない**。刻んだ実行環境がフォルダの中に入っている。
フォルダはばらさないこと。`.exe` は隣の `runtime/` と `app/` を使う。

| | |
| --- | --- |
| 動く環境 | Windows 10 / 11（64 ビット）。Ubuntu は[下記](#ubuntu) |
| 大きさ | zip で約 31 MB、展開して約 90 MB |
| 必要なもの | なし。Java も .NET もインターネットも要らない |
| 残すもの | 設定だけ。`HKEY_CURRENT_USER\Software\JavaSoft\Prefs\io\github\hideyukimori\neneclock` |
| 消し方 | フォルダを消す（まっさらにしたければ上のレジストリキーも） |

**初回起動時の SmartScreen。** zip はコード署名していないので、最初の 1 回だけ
「Windows によって PC が保護されました」と出る。**「詳細情報」→「実行」**で通る。次からは出ない。

**ダウンロードの検証（任意）。** Release には `.sha256` ファイルも付いている。PowerShell で:

```powershell
Get-FileHash (Get-Item '.\NeNe*-windows-portable.zip') -Algorithm SHA256
```

出たハッシュを `.sha256` の中身と見比べる。

### Ubuntu

同じ Release に Ubuntu（と Debian 系・amd64）向けの `.deb` も付いている。

```bash
sudo apt install ./nene-clock_0.2.0_amd64.deb   # /opt/nene-clock に入り、メニューに出る
"/opt/nene-clock/bin/NeNe Clock"                  # またはアプリメニューの「NeNe Clock」
sudo apt remove nene-clock                        # 消すとき
```

こちらも Java は要らない。パッケージは一般的な X11 ライブラリに依存するが、Ubuntu のデスクトップには最初から入っている。
Wayland のセッションでは XWayland を通って動く。WSL にもそのまま入り、WSLg がメニュー項目を Windows のスタートメニューに出す。

macOS のバイナリは配っていない。同じビルド task で実行イメージは作れる（[ソースからビルドする](#ソースからビルドする)）。

---

## 使い方

<p align="center">
  <img src="docs/images/clock-hover.png" width="478" alt="ポインタを乗せた状態。右上に歯車と × の小さなアイコンが 2 つ現れている">
</p>

窓そのものが時計である。タイトルバーは無く、ホバーするまで押すものは何も出ない。

| したいこと | 操作 |
| --- | --- |
| **動かす** | 窓のどこを掴んでもドラッグで動く。移動用のつまみは無い。要らないから |
| **設定を開く** | ホバーして、右上の**歯車**を押す |
| **終了する** | ホバーして、歯車の隣の **×** を押す。裏で動き続けるものは無い |
| **ほかの窓より前に出しておく** | 設定 → *常に最前面* |
| **次に使うとき** | `NeNe Clock.exe` をまた起動する。変えた設定は全部覚えている |

アイコンは 140 ms で現れ 240 ms で消え、文字色に合わせた色で、数字には重ならない。
窓は「その書体・その大きさで最も幅の広い時刻」に合わせて自分で大きさを決めるので、
`05:14:59` が途中で切れることはない。手でリサイズはしない。

---

## 設定

<p align="center">
  <img src="docs/images/settings.png" width="600" alt="設定モーダル。上に時計のプレビュー、続いて時刻表記・秒・日付・最前面・言語・書体・大きさ・文字色・背景色">
</p>

変更は**その場で**後ろの時計に反映され、そのまま保存される。OK もキャンセルも適用も無い。

| 設定 | 何が変わるか | 既定 |
| --- | --- | --- |
| 時刻表記 | `24 時間`（`23:36:52`）か `12 時間`（`11:36:52 PM`） | 24 時間 |
| 秒を表示 | 秒の表示・非表示 | 表示 |
| 日付を表示 | `2026-09-04` の行の表示・非表示 | 表示 |
| 常に最前面 | ほかの窓より前に置く | オフ |
| 言語 | 設定画面の言語（**日本語** / **English**）。時計の表示は言語に依存しない | 日本語 |
| 書体 | 同梱 30 書体から選ぶ（下記） | JetBrains Mono |
| 大きさ | 24〜160 pt。日付の行も連動する | 64 |
| 文字色 | プリセット 24 色・カラーピッカー・HEX 入力 | `#000000` |
| 背景色 | 文字色と同じ操作 | `#F5F2EB` |

モーダルの配色は時計に追従する。背景を暗くすれば、設定画面も暗くなる。

<p align="center">
  <img src="docs/images/settings-dark.png" width="600" alt="背景がほぼ黒の時計に合わせて暗くなった設定モーダル">
</p>

### 書体

<p align="center">
  <img src="docs/images/settings-typeface.png" width="600" alt="書体の画面。各カードがその書体自身で 12:34 を描いている。Sans / Serif / Mono / Display / Retro / Hand で絞り込める">
</p>

30 書体を**アプリの中に同梱**しているので、どの PC でも同じ見た目になり、Windows に入っているフォントに左右されない。
各カードは自分の書体で `12:34` を描き、チップで雰囲気ごとに絞り込める。

| 雰囲気 | 書体 |
| --- | --- |
| Sans | Inter · Roboto · Lato · Poppins · DM Sans · Work Sans |
| Serif | Playfair Display · Lora · EB Garamond · Zilla Slab · Crimson Text |
| Mono | JetBrains Mono · Roboto Mono · IBM Plex Mono · Space Mono · Fira Mono |
| Display | Bebas Neue · Anton · Oswald · Righteous · Cinzel · Abril Fatface |
| Retro | Orbitron · Audiowide · Share Tech Mono · VT323 · Michroma |
| Hand | Caveat · Pacifico · Dancing Script |

すべて Google Fonts の SIL Open Font License 1.1 で、無改変のまま同梱している。出所とライセンスは
[docs/licenses/typefaces.md](docs/licenses/typefaces.md)。
等幅の書体は秒が進んでも桁の位置が動かない。既定が等幅なのはそのためである。

### 色

<p align="center">
  <img src="docs/images/settings-colour.png" width="600" alt="色の画面。プレビュー、プリセット 24 色、彩度と明度の面と色相の帯、HEX 入力、コントラストの表示">
</p>

文字色も背景色も同じ画面で選ぶ。

- **プリセット 24 色** — 黒から白までの無彩色 8 つ、鮮やかな 8 つ、淡い 8 つ。
- **ピッカー** — 面をドラッグすると彩度と明度、帯をドラッグすると色相。HEX とプレビューと時計がドラッグに追従し、
  HEX を打てばピッカーの位置が動く。
- **コントラストの表示。** 2 色が読みにくくなると（3 : 1 未満）数字が琥珀色になり、*読める色にする* ボタンが出る。
  提案であって強制ではない。アプリが勝手に色を変えることはない。

透明度の設定は、意図して無い。半透明の窓は環境によって更新のたびにちらつき、
「使うと壊れて見える設定」は無いほうがよい（[ADR 0012](docs/adr/0012-transparency-is-dropped-the-artefact-is-not-ours.md)）。

### 言語

<p align="center">
  <img src="docs/images/settings-japanese.png" width="600" alt="日本語表示の設定モーダル">
</p>

UI は日本語と英語を持ち、UI 用の書体（Zen Kaku Gothic New と Arimo）も同梱している。
言語は Windows のロケールから推測せず、設定した値のままである。

---

## ギャラリー

設定を変えただけの同じアプリ。どれも動いている時計の実物のスクリーンショット。

| | |
| --- | --- |
| <img src="docs/images/clock-dark-bebas-neue.png" width="420" alt="Bebas Neue・120 pt・黒地に生成り"> | **Bebas Neue** · 120 pt · `#1A1917` に `#EFEAE1` |
| <img src="docs/images/clock-playfair-display.png" width="420" alt="Playfair Display・96 pt・生成りに濃灰"> | **Playfair Display** · 96 pt · `#F5F2EB` に `#2B2B2B` |
| <img src="docs/images/clock-orbitron.png" width="420" alt="Orbitron・88 pt・紺にシアン"> | **Orbitron** · 88 pt · `#0B1020` に `#5EE7FF` |
| <img src="docs/images/clock-caveat-12h.png" width="420" alt="Caveat・110 pt・12 時間表記・クリームに茶"> | **Caveat** · 110 pt · 12 時間 · `#FFF7E6` に `#7A3E1D` |

---

## ソースからビルドする

JDK 21 だけ要る。Gradle Wrapper はリポジトリに入っている。

```bash
./gradlew check              # 唯一の完了定義。CI もこれだけを走らせる
./gradlew run                # ソースから時計を起動する
./gradlew packageInstaller   # 配布物を作る（下記）
```

Windows では `.\gradlew.bat` に同じ task 名を渡す。WSLg なら `DISPLAY` を設定しなくても Windows のデスクトップに窓が出る。
単体テストは画面を必要としない。

### 配布物

`packageInstaller` が、配るものを作る唯一の経路である
（[ADR 0013](docs/adr/0013-windows-is-distributed-as-a-jpackage-msi.md)・
[ADR 0014](docs/adr/0014-a-portable-zip-sits-beside-the-msi.md)）。

1. `jpackage` が **app-image** を作る。起動ファイル・jar・`jdeps` が必要と言ったモジュールだけに刻んだ実行環境。
2. そのフォルダを zip にしたものが**ポータブル zip**。Release に付くのはこれ。
3. Windows では同じ app-image から **MSI** も作る。タグを打つたびに作られ workflow の成果物には残るが、
   検証するまで公開しない。
4. Linux では同じ app-image から **`.deb`** を作る（[ADR 0015](docs/adr/0015-linux-is-distributed-as-a-deb.md)）。

`jpackage` は動いている OS 向けにしか作れないので、GitHub Actions の *Release* workflow が
Windows ランナーと Ubuntu ランナーで同じ task を 1 回ずつ走らせ、`v*` タグのときに両方を 1 つの Release に集める。
macOS でも同じ task が app-image と zip を作る。

アプリのアイコン（タスクバーの絵・PNG・`.ico`）は**アプリ自身がビルド時に描く**（`./gradlew writeAppIcons`）。
画像ファイルはリポジトリに置かないので、絵がコードより古くなることがない。

### WSL に入れる（開発者向け）

```bash
sudo tools/install-desktop-entry.sh   # ビルドして /opt/nene-clock へ置き、.desktop を登録する
tools/place-windows-shortcut.sh       # WSLg が作ったショートカットを Windows のデスクトップへ複製する
```

WSLg は `.desktop` からスタートメニューのショートカットを自分で作る。どちらのスクリプトも冪等で、
`tools/uninstall-desktop-entry.sh` が置いたものを消す。

---

## このリポジトリの運営

時計は小さい。変わっているのはその周りの規約体系で、**一つのことを実現する方法を 1 つに固定し、
それを人の記憶ではなく機械に守らせる。**

```text
:app                        合成ルート
:ui:swing                   Swing の部品と窓
:adapters:system-time       現在時刻を読んでよい唯一のモジュール
:adapters:preferences       java.util.prefs に触ってよい唯一のモジュール
:adapters:font-catalog      同梱書体とその出所
:core:application           ポート・整形・型のある結果
:core:domain                値・不変条件・閉じた選択肢（JDK のみ）
:quality:architecture-tests ArchUnit — 実行できるテストとしてのアーキテクチャ
```

| 層 | 守らせているもの |
|---|---|
| `javac -Xlint:all -Werror` | 警告はエラー。`sealed` ＋ `switch` の網羅性 |
| Gradle のモジュールグラフ | 依存の向き・循環なし・未承認モジュールなし |
| forbidden-apis | **メソッド単位**: `now()` / `nanoTime()` / `Math.random()` / 既定ゾーン・ロケール禁止 |
| Error Prone + NullAway | `null` の意味は 1 つ |
| Checkstyle | 構造と複雑度の上限 |
| ArchUnit | **パッケージ単位**: core は Swing も Preferences も知らない |
| `validateConformance` | 固有規約（命名・抑制の waiver・`default`・文書整合） |
| JaCoCo | core 2 モジュールの分岐カバレッジ下限 |
| font-catalog のテスト | 同梱書体の存在・SHA-256 の一致・Regular で描けること |

面白いのは決定性の規則で、`forbidden-apis` が JDK のメソッドシグネチャを直接名指しできるため、
「core は現在時刻を読まない」がレビューの約束ではなくビルドの失敗になっている。

| 文書 | 内容 |
|---|---|
| [SPECIFICATION.md](SPECIFICATION.md) | 何を作るか（FR-NNN） |
| [docs/ARCHITECTURE_CONSTITUTION.md](docs/ARCHITECTURE_CONSTITUTION.md) | 憲章（ARC-NNN） |
| [docs/CODING_RULES.md](docs/CODING_RULES.md) | Java / Swing 規約（JAV-NNN / SWG-NNN） |
| [docs/QUALITY_GATES.md](docs/QUALITY_GATES.md) | *いま*何が機械で守られているか |
| [docs/quality/gate-proofs.md](docs/quality/gate-proofs.md) | ゲートが本当に落ちる証拠と、画面で見たことの記録 |
| [docs/PROJECT_LAYOUT.md](docs/PROJECT_LAYOUT.md) | モジュールと許される依存 |
| [docs/DEVELOPMENT_WORKFLOW.md](docs/DEVELOPMENT_WORKFLOW.md) | 変更の進め方 |
| [docs/adr/](docs/adr/) | 決定と、却下した選択肢 |

規範文書は日本語、コード・コメント・英語版 README は英語で書く。

### ロードマップ

済んでいるもの: 時計・設定の永続化・設定モーダル・同梱書体・色・日英切り替え・Windows のポータブル配布。
まだのもの: ストップウォッチとカウントダウンタイマー。仕様（FR-010 / FR-020）はあり、意図して着手していない。
タブの無い窓のどこに置くかが、まだ決まっていない。

## ライセンス

MIT — [LICENSE](LICENSE)。同梱書体は SIL OFL 1.1 — [docs/licenses/typefaces.md](docs/licenses/typefaces.md)。
