# 規則の waiver

waiver は、**1 つの**名前のある機械規則に対する、**期限付き**で**最小範囲**の例外である。
別のアーキテクチャではないし、プロジェクトの方針を変えるものでもない。

## 原則

- waiver は最後の手段である。先に正典の実装経路を試すこと
- 1 つの規則・1 つの理由・可能な限り小さい範囲（ファイルと宣言）に閉じる
- Issue・担当・作成日・**期限**（または客観的な解除条件）を持つ
- リスクに見合うテストか封じ込めを添える
- コードの抑制の**直前行**と PR 本文の両方から参照する
- **期限切れの waiver は CI を落とす**（CNF-009）
- 恒久化した waiver は、コードを直して消すか、規則そのものを変える ADR に置き換える

lint / 静的解析の baseline、ディレクトリ単位の除外、`@SuppressWarnings("all")`、
説明の無い生成コード除外は禁止であり、waiver でも許可できない（QLT-003 / CNF-008）。

## 命名

```text
WVR-NNNN-short-kebab-title.md
```

`0000-template.md` を複製し、下の索引に追加する。

`Scope` はリポジトリ相対のパスと宣言名で書く。例:

```text
ui/swing/src/main/java/io/github/hideyukimori/neneclock/ui/swing/ClockPanel.java#ClockPanel
```

抑制の直前行には `// Waiver: WVR-NNNN` を置く。ファイル単位の抑制は waiver があっても禁止。

## 有効な waiver の索引

| Waiver | 規則 | Scope | 解除条件 | 期限 |
| --- | --- | --- | --- | --- |
| — | — | — | 有効な waiver は無い | — |
