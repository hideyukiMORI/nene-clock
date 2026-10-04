# 調査報告 — MSIX 化と Microsoft Store 申請の下調べ（2026-10-04）/ NeNe Clock

記録者: Clock リナ。[Issue #107](https://github.com/hideyukiMORI/nene-clock/issues/107) の下調べ。
申請・Partner Center の操作・証明書の購入は行っていない。版も上げていない。判断の案は [ADR 0020](../adr/0020-the-store-is-a-fourth-channel-wrapped-from-the-same-app-image.md)（提案）。

Store の制度（Microsoft が署名する・個人登録は無料・版の先頭は 0 にできない・プライバシーポリシーが必須）は
NeNe Loupe が一次資料で裏を取っており（同リポの `docs/reports/2026-10-03-store-msix-research.md`）、ここでは調べ直していない。

## 結論

- **包めた。** 配布済みの v0.2.6 の app-image を、製品コードを変えずに MSIX にできて、パッケージの身元を持って起動した。
- **設定は読めて、書ける。行き先は変わる。** 読むときは本物のレジストリが見え、書くときはパッケージ専用の場所へ行く。
  MSI 版の設定は Store 版に引き継がれるが、Store 版の変更は MSI 版に戻らない。
- **CI の Windows ランナーで作れた。** `makeappx` / `makepri` はランナーにある。
- **製品コードを変えないと包めない箇所は、測った範囲では無かった。**
- 🔴 **測っていないことが残っている。** `C:\Program Files\WindowsApps\` に本当に入れた状態（読み取り専用）での起動と、
  包んだ状態での目視（同梱書体・設定モーダル・複数モニタ）は測っていない。管理者権限と施主の画面が要るため。下の「測っていないこと」を読むこと。

## A. 実測

### 環境と材料

- Windows 11 Pro 10.0.26200。Windows SDK 10.0.26100.0 の `makeappx.exe`。モニタ 4 枚。開発者モードは有効だった。
- app-image は **GitHub Release v0.2.6 の `NeNe-Clock-windows-portable.zip`** の中身そのまま
  （SHA-256 `49077aee…119b2`、添付の `.sha256` と一致）。CI が `packageInstaller` で作ったもので、手元では作り直していない。
  148 ファイル・83.1 MB。同梱の実行環境は `21.0.12.1`、モジュールは `java.base java.datatransfer java.xml java.prefs java.desktop`。
- 作業の置き場はリポジトリの外（`%LOCALAPPDATA%\NeNeClockProbe\`）。リポジトリには何も足していない。
- 施主は MSI 版 0.2.6 を常用しており、実測のあいだも動いていた。それには触れていない。
- **実測の前に施主の設定を控えた**（`reg export HKCU\Software\JavaSoft\Prefs\io\github\hideyukimori\neneclock`。
  控えは `%LOCALAPPDATA%\NeNeClockProbe\neneclock-prefs-backup-2026-10-04.reg`）。
  実測のあとにもう一度書き出して、**SHA-256 が同じ**（`FEC9410D…7A07`）であることを確かめた。

### A-1. jpackage の app-image を MSIX に包めるか

| 試したこと | 結果 |
| --- | --- |
| app-image のフォルダに `AppxManifest.xml` とロゴ 3 枚を足して `makeappx pack` | **成功。** 2.8 秒、32,275,903 バイト（MSI は 32.4 MB、zip は 31.1 MB） |
| `Executable="NeNe Clock.exe"`（名前に空白）・`EntryPoint="Windows.FullTrustApplication"`・`runFullTrust` | そのまま通った。空白は問題にならなかった |
| 開発者モードで登録（`Add-AppxPackage -Register`）して AUMID から起動 | **起動した。** 窓の題は `NeNe Clock`。プロセスは 2 つ（jpackage のランチャーと本体）で、どちらも `GetPackageFullName` が成功（`NeNeClock.LocalTest_0.2.6.0_x64__d5f82krk61nwp`）。パッケージの身元を持って動いている |
| MSI 版と同時に動かす | 両方とも動いた。互いに止めない |
| 窓の大きさ | 320×160（同じ設定で動いている MSI 版の窓と同じ） |

自動起動やスタートメニュー以外の入口は、製品に無い（コードに登録の処理が無い）。MSI が作るのはスタートメニューとデスクトップのショートカットだけである。

### A-2. 設定の保存先（`java.util.prefs`）

測り方: パッケージの文脈の中で（`Invoke-CommandInDesktopPackage`）、`java.util.prefs.Preferences` を読み書きする小さな Java を動かし、
外から `reg query` で本物のレジストリを見た。**書き込みは施主の本物の設定キーには行わず、隣に作った検証用のキー
（`…\hideyukimori\neneclockprobe1`〜`3`）で測った。** 本物のキーに対して行ったのは読み取りだけである。

| 場面 | パッケージの中から見えたもの | 本物のレジストリ（外から） |
| --- | --- | --- |
| MSI 版の設定がある（施主の本物の設定を読むだけ） | **読めた**（`typeface=ANTON`・`schemaVersion=8`） | 変わらない |
| ① 何も無い状態で、中から書く | 書いた値が読める | **何も作られない** |
| ② 本物にキーがある状態で、中から読む | 本物の値（`from-msi`）が読める | — |
| ② 続けて、中から書く | 書いた値（`from-package`）が読める | **元の値（`from-msi`）のまま** |
| ④ そのあと外（MSI 版の側）で値を変える | **中からは古い自分の値（`from-package`）のまま**。外の変更は見えない | 外で書いた値 |
| ③ パッケージを消す | — | 本物のキーは残る。中から書いた値はどこにも残らない |

パッケージ専用の場所は `%LOCALAPPDATA%\Packages\<PackageFamilyName>\SystemAppData\Helium\User.dat`（パッケージごとのレジストリのファイル）。
パッケージを消すと `Packages\<PackageFamilyName>\` ごと消えた。

**ファイルの場合（Loupe）とは動きが違う。** Loupe では、本物の場所にフォルダが既にあると包んだアプリは本物の場所へ書いた。
レジストリでは、本物にキーがあっても書き込みは必ず専用の場所へ行った。

利用者から見ると次のようになる。

| 利用者 | 起きること |
| --- | --- |
| Store から初めて入れた人 | 既定の設定で始まる。設定は専用の場所に入り、アンインストールで消える。`HKCU\Software\JavaSoft\Prefs` には何も残らない |
| MSI 版（または zip 版）を使っていた人 | **Store 版は最初、MSI 版の設定のまま始まる**（引き継ぐ）。Store 版で設定を変えると、そこから先は別々になる。MSI 版には書き戻されない |
| 両方を使い続ける人 | Store 版で一度でも設定を変えたあとは、MSI 版で変えた設定が Store 版に見えなくなる |
| Store 版を消した人 | Store 版の設定は消える。MSI 版の設定は残る |

「Store 版で設定を変えるまでは MSI 版の設定が見え続ける」は、製品が設定を書くのは設定を変えたときだけ（`SettingsHandler` → `store.save`）で、
起動では書かないというコードの事実と、上の実測を合わせた推論である。**包んだ時計の設定モーダルを実際に操作しては確かめていない。**

### A-3. CI の Windows ランナーで MSIX を作れるか

PR #108 に一時的な手順を置いて測り、測ったあとに消した（最終の差分にワークフローの変更は無い）。run は
[37189417490](https://github.com/hideyukiMORI/nene-clock/actions/runs/37189417490)。

| 見たこと | 結果 |
| --- | --- |
| ランナー | `windows-2025`（`ImageOS=win25-vs2026`・`ImageVersion=20260925.250.1`） |
| `makeappx` / `makepri` / `signtool` | **ある。** `C:\Program Files (x86)\Windows Kits\10\bin\10.0.26100.0\x64\`。ほかの版のフォルダ（14393 / 15063 / 16299 / 17134）には無い |
| PATH | **通っていない。** 場所を自分で決める必要がある |
| `packageInstaller` が作った app-image をそのまま包む | **成功。** 32,280,368 バイト |
| `makepri createconfig` | 動いた（終了 0）。`resources.pri` を作るところまでは測っていない |

版の固定: 道具は SDK の版のフォルダの下にあるので、task が「10.0.26100.0 のフォルダの `makeappx`」を名指しすれば、
ランナーの SDK が変わったときに黙って別の版を使わず、落ちる。Loupe はランナーの Visual Studio が上がって CI が落ちた経験がある
（同じことが SDK でも起きうる。落ちるのが正しい）。

「検査を終えた app-image だけを入力にし、MSIX のために作り直さない」形にできる。`packageInstaller` はすでに app-image を
`app/build/installer/image/NeNe Clock` に作り、そこから zip と MSI を出している。MSIX も同じフォルダを入力にすればよい。

### 測っていないこと

| 測っていないこと | 理由 |
| --- | --- |
| **`C:\Program Files\WindowsApps\`（読み取り専用）に入れた状態での起動** | 開発者モードの登録では、アプリは元のフォルダ（書ける場所）から動く。本当に入れるには自己署名の証明書を「ローカル コンピューター」に入れる必要があり、管理者権限（UAC）が要る。施主が PC の前に居るときに行う |
| 包んだ状態での目視: 同梱書体の描画・設定モーダルの操作・複数モニタでのドラッグ | 施主の画面を撮る・ポインタを動かすことになる。起動して窓が出たことと、その大きさだけを見た |
| 包んだ時計の設定モーダルから実際に保存すること | 同上。Java の同じ API をパッケージの文脈で動かして測った |
| Store が署名したパッケージの挙動 | 提出しないと手に入らない |
| Windows App Certification Kit | 管理者権限が要る。申請の前に必ず回す |
| 版を上げたときに設定が引き継がれること | Store の更新でしか測れない |
| `resources.pri` と倍率別のロゴ | ロゴは 256px を縮めた 3 枚で測った |
| Windows 10 | 手元は Windows 11 だけ |

読み取り専用の場所で動くかについて、コードから言えることだけを書く（**実測ではない**）。
製品が書くのは `java.util.prefs` だけで、ファイルは書かない。通信もしない。ランチャーは `app\NeNe Clock.cfg` を読むだけである。
ポータブル zip と MSI は、書けない場所に置いても動く作りのはずだが、確かめたわけではない。

### 後片付け

- 検証用のパッケージは消した（`Get-AppxPackage NeNeClock.LocalTest` が空・`Packages\` の下のフォルダも無い）。
- 検証用のレジストリキー 3 つは消した。`…\hideyukimori` の下には `neneclock` だけが残っている。
- 証明書は作っていない。鍵もファイルも無い。
- `%LOCALAPPDATA%\NeNeClockProbe\` に、設定の控え・展開した app-image・検証用の MSIX・探針のスクリプトが残っている。
  **消してよいかは施主が決める**（控えが入っているので勝手に消していない）。

## B. 申請までの作業（Issue の切り方の案）

実装はしていない。順番は上から。

| # | Issue の案 | 中身 | 施主の判断 |
| --- | --- | --- | --- |
| 1 | 読み取り専用の場所に入れた状態の実測 | 上の「測っていないこと」の先頭 2 つ。施主が PC の前に居るときに、UAC を 1 回通して行う | **居る時間を決める** |
| 2 | ADR 0020 を受理するか | この報告を読んで決める | **する / しない** |
| 3 | MSIX を作る Gradle task | `packageInstaller` の app-image を入力に、Windows でだけ MSIX を作る。身元は 1 つのファイル。SDK の版を名指しする | 身元の値（アプリ名を予約すると決まる） |
| 4 | ロゴ一式と `resources.pri` | `AppIcon` から書き出す。倍率別・`targetsize` 別 | — |
| 5 | `PRIVACY.md` | 事実は「通信しない。保存するのは表示の設定だけで、PC の中にある」。日英 | 文面を読む |
| 6 | 版を 1.0.0 へ | `gradle.properties` の 1 か所。MSI の upgrade UUID は変えない | **1.0.0 に何を含めるか**（#97 / #101 を先に直すか） |
| 7 | 掲載文とスクリーンショット | Clock は日英の UI を持つ（ADR 0009）。掲載ページを日英の 2 言語で作るか、英語だけにするか。画像は 1366×768 以上が要るが、窓は小さい | **言語をどうするか** |
| 8 | WACK | 申請の前に通す。管理者権限 | 居る時間 |
| 9 | README | Store 版の入れ方と、設定の場所が入れ方で違うこと | 文面を読む |

施主の手番（リナはやらない）: Partner Center でのアプリ名の予約（候補 `NeNe Clock`）と、申請。

## 厳格規約の雛形へ還流すべき点

- **HKCU のレジストリに設定を置く製品は、包むと「読むのは本物・書くのは専用」になる。** ファイルに置く製品（フォルダがあれば本物へ書く）とは
  動きが違う。設定の置き場を決めるときに、包んだ場合の行き先を表にしておく。
- **「引き継ぐが、書き戻さない」は利用者に見えない分岐になる。** 乗り換えた人の設定は最初だけ同じで、あとから黙って別れる。README に書く。
- **実機の実測は、本物の設定の隣に検証用のキーを作って測る。** 仕組みを知るのに、常用している設定を動かす必要は無い。控えは取る。
- **配布物は、検査を終えた 1 つの中身から全部の経路へ出す形にしておく。** Clock は app-image から zip と MSI を出していたので、
  MSIX を足すのに作り直しが要らなかった。
- **ランナーにある道具は PATH に無いことがある。** 場所と版を task が名指しする。黙って別の版を使わせない。
- **開発者モードの登録で測れることと、測れないことを分けて書く。** 身元とレジストリは測れる。読み取り専用の場所は測れない。
