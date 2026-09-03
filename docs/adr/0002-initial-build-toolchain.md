# ADR 0002 — 初期ビルドツールチェーン

- 状態: 受理
- 日付: 2026-09-03
- Issue: #2
- 影響する規則: ARC-002 / ARC-007 / JAV-004 / JAV-012 / QLT-002 / QLT-004 / QLT-011

## 文脈

Java 21 / Swing / Gradle という土台は製品仕様（[SPECIFICATION.md](../../SPECIFICATION.md)）で決まっている。
決める必要があるのは、**どの道具にどの規則を担当させるか**である。
道具が重なると、片方を緩めても誰も気づかない状態が生まれる（QLT-010 が想定する事故）。

## 決定

パッケージルートを `io.github.hideyukimori.neneclock` とし、次の分担で構成する。

| 道具 | 版 | 担当（重ねない） |
| --- | --- | --- |
| Gradle（Kotlin DSL・Wrapper 同梱） | 9.7.1 | モジュールグラフ・タスクグラフ・依存の固定 |
| javac | 21（toolchain） | 型・網羅性・`-Xlint:all -Werror` |
| Error Prone ＋ NullAway | 2.50.0 / 0.14.1 | バグパターンと **`null` の意味**（JAV-004） |
| Spotless ＋ palantir-java-format | 8.10.1 / 2.97.0 | **整形だけ**（唯一の正本） |
| forbidden-apis | 3.10 | **メソッド単位**の禁止（ARC-007 の決定性がここ） |
| Checkstyle | 14.1.0 | **構造と複雑度**（JAV-012） |
| ArchUnit | 1.5.0 | **パッケージ・レイヤ単位**の境界 |
| JUnit 5 ＋ AssertJ | 6.1.3 / 3.27.7 | 振る舞い |
| JaCoCo | 0.8.13 | 中核のカバレッジ下限 |
| `validateConformance`（自作・依存ゼロ） | — | NeNe Clock 固有（CNF-001..010） |

依存の版は `gradle/libs.versions.toml` にだけ書く（QLT-011）。

## 強制

- **active**: 上表の全ツールが `./gradlew check` に入っている
- **active**: `:core:domain` は production 依存の**追加そのもの**をビルドが拒否する
- **planned**: SHA-256 の dependency verification

## 結果

- 「決定性」（現在時刻・乱数・既定ゾーンを読ませない）が **active** にできた。
  🔑 これは先行 3 言語（Kotlin / Go / Rust）がいずれも `planned` のまま残した規則である。
  forbidden-apis が JDK のシグネチャを直接名指しできることによる
- Swing と `java.util.prefs` は JDK 同梱なので、Gradle のモジュールグラフでは
  「core が import できない」を作れない。そこは ArchUnit が担当する（責務の重複ではなく分担）
- 道具が 9 つあるので、初回のビルドは依存の取得に時間がかかる

## 却下した選択肢

| 選択肢 | 却下の理由 |
| --- | --- |
| Maven | モジュールグラフの検査と自作タスクの実装が Gradle より書きにくい。仕様側も Gradle 指定 |
| Groovy DSL | 型が効かない。ビルドスクリプトもコードとして検査したい |
| SpotBugs / PMD の追加 | Error Prone ＋ Checkstyle ＋ ArchUnit と担当が重なる。重なった道具は「片方を緩めても気づかない」経路を作る（QLT-010）。必要になったら ADR で足す |
| google-java-format | palantir 版のほうが行折り返しが読みやすく、`record` と switch 式の整形が安定している。**どちらか 1 つだけを入れる**ことが要件 |
| Checkstyle の `MissingSwitchDefault` を有効化 | JAV-002 と正面から矛盾する。`default` は網羅性検査を殺すので、逆に CNF-003 で禁止する |
| JUnit 5 系（5.x）に固定 | ArchUnit は core API のみを使うため JUnit 6 と併用できる。新規リポジトリで旧系に固定する理由が無い |
| lint の baseline / suppressions ファイル | QLT-003。新規リポジトリに既存違反は無い |
