# ADR 0013 — Windows へは jpackage の MSI で配る。作るのは CI の Windows ランナー

- 状態: 受理
- 日付: 2026-09-04
- Issue: #66
- 影響する規則: QLT-001 / QLT-005 / ARC-012 / ADR 0005 / FR-030

## 文脈

施主要件: Windows からインストーラーで入れたい。GitHub で配りたい。

いまの配布経路は WSL の中に `/opt/nene-clock` を置き、WSLg がショートカットを作るもの（#50）だけである。
利用者に WSL と JDK を要求するので、配布とは呼べない。
さらに、ちらつき（#64）は WSLg の合成層の事象であり、ネイティブの Windows 窓なら起きない可能性が高い。

## 決定

1. **JDK 同梱の `jpackage` で MSI を作る。** 実行環境は `jlink` で刻んで同梱し、利用者に Java を要求しない
2. **作るのは GitHub Actions の Windows ランナー。** `jpackage` は動いている OS 向けしか作れない（クロスビルド不可）
3. **手順は Gradle task `packageInstaller` に閉じる**（QLT-005）。ワークフローはその task を呼び、できたものを置くだけ。
   Windows 以外では同じ task が app-image を作り、結線（jar・main クラス・アイコン・モジュール集合）が正しいことを
   起動して証明できる
4. **モジュール集合は `jdeps` から機械的に求める。** 手で書くと、足りないモジュールは実行時まで分からない
5. **`.ico` は実装（`AppIcon`）から書き出す**（#46 と同じ原則）。ImageMagick への依存は消す
6. **MSI の upgrade UUID を固定する。** 入れ直しが上書きになる。変えると別製品として並んで入る
7. **配布は GitHub Release に MSI と SHA-256 を添付する。** `v*` タグを打つと自動で添付される。
   手動起動（workflow_dispatch）では run の成果物として落とせる（施主の実機確認用）
8. 版は `gradle.properties` の `version` 1 か所。MSI は数字 3 つの形しか受けない

## 却下した選択肢

| 選択肢 | 却下の理由 |
| --- | --- |
| Inno Setup / NSIS | 外の道具と別のスクリプト言語が増える。`jpackage` は JDK にあり、方法が 1 つで済む |
| MSIX / winget / Microsoft Store | 署名が前提。署名しないと決めたので採れない |
| zip を配って「JDK を入れて `bin/app` を叩け」 | 利用者に JDK を要求する。インストーラーという要件に応えていない |
| WSL から Windows 向けをクロスビルド | `jpackage` にその機能が無い |
| ワークフローの中で `jpackage` を直接叩く | 手順が CI 専用のシェルになり、ローカルで再現できない（QLT-005） |
| `--add-modules` を手で書く | 足りないときに実行時の `ClassNotFoundException` でしか分からない |
| ImageMagick で `.ico` を作り続ける | 作り方が 2 つになり、片方だけ古くなる（ARC-012）。ランナーにあるとも限らない |

## 強制

- **active**: `packageInstaller` が Linux で app-image を作り、起動できる（gate-proofs 第 20 節）
- **active（2026-09-05・施主の実機）**: Windows ランナーで MSI ができ、施主の Windows 実機に問題なく入った。
  ネイティブの Windows 窓でちらつきが起きないことは zip 版で先に確認済み（gate-proofs 19.3）。
  MSI は検証済みになったが、**Release に載せるかは別の判断**（いまは zip と .deb だけ・#70）
- **署名はしない（施主決定 2026-09-04）。** SmartScreen の警告は出るが、「詳細情報」→「実行」で通る。
  コード署名証明書は認証局から買う（OV で年数万円・鍵はハードウェアかクラウド署名）か、
  Microsoft の Azure Trusted Signing（月額制・法人は設立 3 年以上の条件）か、Certum の OSS 向けかになる。
  署名しても評判が溜まるまで警告は消えない。いまの配布規模ではその費用と手間に見合わない。
  再検討するなら、CI から署名できるクラウド方式（Trusted Signing / eSigner / KeyLocker）を前提にする

## 結果

得られるもの:

- 利用者は MSI をダブルクリックするだけ。JDK も WSL も要らない
- Windows 側の設定はレジストリ（`HKCU\Software\JavaSoft\Prefs`）に入る。WSL 側の設定とは別になる
- ちらつきの原因だった合成層（WSLg）を通らない

失うもの:

- 配布物は 40〜60 MB 程度になる（実行環境を同梱するため）
- 初回起動で SmartScreen に止められる（署名しないと決めた）
- WSL 側と Windows 側で設定が共有されない
