# ADR 0015 — Linux（Ubuntu）へは .deb で配る。同じ app-image から、同じ task で

- 状態: 受理
- 日付: 2026-09-05
- Issue: #74
- 影響する規則: QLT-005 / ARC-012 / ADR 0013 / ADR 0014

## 文脈

施主要件: Ubuntu にインストールするものも配りたい。
Linux の app-image は作れているが、tar や zip ではメニューにもアンインストールにも出ない。
GitHub で配る Linux のデスクトップアプリの一般形は `.deb`（`sudo apt install ./x.deb`）である。

## 決定

1. **`jpackage --type deb` で `.deb` を作る。** Windows の MSI と同じく、**app-image から**作る。結線は 1 か所
2. **`packageInstaller` が Linux では app-image → zip → `.deb` の順に作る。** Windows では → MSI。task は 1 つ
3. パッケージ名は `nene-clock`。`/opt/nene-clock` に入り、メニュー（Utility）に出て、`apt remove nene-clock` で消える
4. workflow は OS ごとの job に分け、**release job が両方の成果物を集めて 1 つの Release** に並べる。
   Windows のポータブル zip と Ubuntu の `.deb`（MSI は検証まで載せない・#70）
5. **amd64 のみ**。ランナーが x86_64 なので、arm64 は作らない（要望が出たら別 ADR）

## 却下した選択肢

| 選択肢 | 却下の理由 |
| --- | --- |
| AppImage | 「入れずに使う」の Linux 版としては良いが、道具（appimagetool）が JDK の外にあり、方法が 2 つになる。要望が出たら問い直す |
| Flatpak（Flathub）/ Snap | 審査とマニフェスト保守が要る。個人の小さなアプリには重い。ストアで見つけてもらう段階ではない |
| PPA / apt リポジトリ | ソースパッケージ化が要り、最も重い |
| tar.gz だけ | 動くが、メニューにもアンインストールにも出ない。配布とは呼びにくい |
| `.rpm` も同時に | 施主の環境が Ubuntu。要望が出てから `--type rpm` を足せばよい（同じ app-image から作れる） |

## 強制

- **active**: Linux で `packageInstaller` が `.deb` を作り、WSL の Ubuntu に `dpkg -i` で入って起動し、`apt remove` で消える（gate-proofs 第 20.4 節）
- **active**: postinst は `app/src/deb/postinst`（jpackage の既定に `mkdir -p /usr/share/desktop-directories` を足したもの）。
  最小構成の Linux で `xdg-desktop-menu` が落ちるのを防ぐ（実測は gate-proofs 20.4）
- **active（2026-09-05・施主の Ubuntu 実機）**: 入って起動した。ドックのアイコンは `.desktop` の `StartupWMClass`（`app/src/deb/NeNe Clock.desktop`）で結びつける（#80・gate-proofs 20.6）

## 結果

- Ubuntu の利用者は `.deb` を 1 つ落として `apt install` するだけ。Java は要らない
- WSL にも同じ `.deb` で入る。`tools/install-desktop-entry.sh` は開発中の近道として残すが、役目は `.deb` が引き継げる（廃止は別途）
- Java 21 の Linux 版 AWT は X11 で描く。Wayland のセッションでは XWayland を通る。**サーバ版のような最小構成では X11 のライブラリが無く、入らない**
