# Privacy Policy — NeNe Clock

Last updated: 2026-10-04 · Publisher: Hideyuki Mori

**NeNe Clock does not collect, store or transmit any personal information. It never connects to
a network.**

日本語は[このページの後半](#プライバシーポリシー日本語)にあります。

## What the application does with data

| Data | What happens | Where it goes |
| --- | --- | --- |
| Date, time and time zone | The application reads the current date, time and time zone from your device to draw the clock. | Shown on screen. Never written to disk. Never sent anywhere. |
| Settings | Ten display choices are saved: time format, seconds, date, always on top, language, typeface, size, text colour, background colour and padding. | Your device only (see below). |

The application has no accounts, no telemetry, no analytics, no advertising, no crash reporting,
no automatic update check of its own, and no third-party services. It does not read your files,
your clipboard, your screen or your location. The typefaces are bundled inside the application;
nothing is downloaded.

## Where the settings are kept

The saved settings contain only a schema version and the ten choices above. They contain nothing
about you.

- Installer (MSI) and portable ZIP versions on Windows: the registry key
  `HKEY_CURRENT_USER\Software\JavaSoft\Prefs\io\github\hideyukimori\neneclock`. It stays on your
  device until you delete it.
- Microsoft Store version: Windows keeps the settings inside the application's own package
  storage and removes them when you uninstall the application. If settings from the installer or
  ZIP version already exist on the device, the Store version starts from them, but it saves its
  own changes to the package storage and does not modify the existing ones.
- Linux (.deb): the same settings under `~/.java/.userPrefs` in your home directory.

## Network

The application makes no network connections. Its code uses no networking functions.

If you install the application from the Microsoft Store, the Store itself — not this
application — handles download, installation and updates. That is covered by the
[Microsoft Privacy Statement](https://privacy.microsoft.com/privacystatement).

## Changes and contact

Changes to this policy are made in this file, and its history is public in the repository.
Questions: open an issue at <https://github.com/hideyukiMORI/nene-clock/issues>.

---

# プライバシーポリシー（日本語）

最終更新: 2026-10-04 · 発行者: Hideyuki Mori

**NeNe Clock は個人情報を収集・保存・送信しません。ネットワークには一切接続しません。**

## アプリが扱うデータ

| データ | 何をするか | どこへ行くか |
| --- | --- | --- |
| 日付・時刻・タイムゾーン | 時計を描くために、お使いの端末の現在の日付・時刻・タイムゾーンを読みます。 | 画面に表示するだけです。ディスクに書きません。どこにも送りません。 |
| 設定 | 表示についての 10 個の選択を保存します: 時刻の形式・秒の表示・日付の表示・常に最前面・言語・書体・文字の大きさ・文字色・背景色・余白。 | お使いの端末の中だけ（下記）。 |

アカウント、利用状況の送信、解析、広告、クラッシュ報告、アプリ独自の更新確認、第三者のサービスはありません。
ファイル、クリップボード、画面、位置情報は読みません。書体はアプリに同梱しており、何もダウンロードしません。

## 設定の保存先

保存される設定に入るのは、形式の版と上の 10 個の選択だけです。利用者についての情報は入りません。

- Windows のインストーラ（MSI）版と ZIP 版: レジストリの
  `HKEY_CURRENT_USER\Software\JavaSoft\Prefs\io\github\hideyukimori\neneclock`。削除するまで端末に残ります。
- Microsoft Store 版: Windows がアプリ専用の保存場所に置き、アンインストールすると消えます。
  インストーラ版や ZIP 版の設定がすでに端末にある場合、Store 版はその設定から始まりますが、
  変更はアプリ専用の保存場所に保存し、もとの設定は書き換えません。
- Linux（.deb）版: 同じ設定が、ホームディレクトリの `~/.java/.userPrefs` の下に入ります。

## ネットワーク

アプリはネットワークに接続しません。アプリのコードは通信の機能を使っていません。

Microsoft Store から入れた場合、ダウンロード・インストール・更新を行うのは Store であり、このアプリではありません。
それについては [Microsoft のプライバシーに関する声明](https://privacy.microsoft.com/privacystatement)が適用されます。

## 変更と連絡先

このポリシーの変更はこのファイルで行い、履歴はリポジトリで公開されます。
ご質問は <https://github.com/hideyukiMORI/nene-clock/issues> に Issue を立ててください。
