# 同梱書体のライセンスと出所

NeNe Clock は表示用の書体を **30 種類同梱している**（FR-043 / [ADR 0006](../adr/0006-typefaces-are-bundled-not-discovered.md)）。
すべて **SIL Open Font License 1.1** で、**無改変**のまま同梱している。

- 実体: `adapters/font-catalog/src/main/resources/io/github/hideyukimori/neneclock/adapter/fontcatalog/typefaces/`
- ライセンス本文: 同じ場所の `<資源名>.license.txt`（書体ごとに同梱）
- 🔑 **機械が突き合わせる正本は同じ場所の `provenance.tsv`** であり、この表はその写しである。
  SHA-256 が実体と一致することは `:adapters:font-catalog` のテストが毎回確認する

上流はいずれも [google/fonts](https://github.com/google/fonts) の
commit `205859f680703e449fe05dce0f792cc041d6dc89`。

| 定数 | 書体 | 雰囲気 | 上流のパス |
| --- | --- | --- | --- |
| `INTER` | Inter | SANS | `ofl/inter/Inter[opsz,wght].ttf` |
| `ROBOTO` | Roboto | SANS | `ofl/roboto/Roboto[wdth,wght].ttf` |
| `MONTSERRAT` | Montserrat | SANS | `ofl/montserrat/Montserrat[wght].ttf` |
| `POPPINS` | Poppins | SANS | `ofl/poppins/Poppins-Regular.ttf` |
| `DM_SANS` | DM Sans | SANS | `ofl/dmsans/DMSans[opsz,wght].ttf` |
| `MANROPE` | Manrope | SANS | `ofl/manrope/Manrope[wght].ttf` |
| `PLAYFAIR_DISPLAY` | Playfair Display | SERIF | `ofl/playfairdisplay/PlayfairDisplay[wght].ttf` |
| `LORA` | Lora | SERIF | `ofl/lora/Lora[wght].ttf` |
| `EB_GARAMOND` | EB Garamond | SERIF | `ofl/ebgaramond/EBGaramond[wght].ttf` |
| `BITTER` | Bitter | SERIF | `ofl/bitter/Bitter[wght].ttf` |
| `CRIMSON_TEXT` | Crimson Text | SERIF | `ofl/crimsontext/CrimsonText-Regular.ttf` |
| `JETBRAINS_MONO` | JetBrains Mono | MONO | `ofl/jetbrainsmono/JetBrainsMono[wght].ttf` |
| `ROBOTO_MONO` | Roboto Mono | MONO | `ofl/robotomono/RobotoMono[wght].ttf` |
| `IBM_PLEX_MONO` | IBM Plex Mono | MONO | `ofl/ibmplexmono/IBMPlexMono-Regular.ttf` |
| `SPACE_MONO` | Space Mono | MONO | `ofl/spacemono/SpaceMono-Regular.ttf` |
| `SOURCE_CODE_PRO` | Source Code Pro | MONO | `ofl/sourcecodepro/SourceCodePro[wght].ttf` |
| `BEBAS_NEUE` | Bebas Neue | DISPLAY | `ofl/bebasneue/BebasNeue-Regular.ttf` |
| `ANTON` | Anton | DISPLAY | `ofl/anton/Anton-Regular.ttf` |
| `OSWALD` | Oswald | DISPLAY | `ofl/oswald/Oswald[wght].ttf` |
| `RIGHTEOUS` | Righteous | DISPLAY | `ofl/righteous/Righteous-Regular.ttf` |
| `CINZEL` | Cinzel | DISPLAY | `ofl/cinzel/Cinzel[wght].ttf` |
| `ABRIL_FATFACE` | Abril Fatface | DISPLAY | `ofl/abrilfatface/AbrilFatface-Regular.ttf` |
| `ORBITRON` | Orbitron | RETRO | `ofl/orbitron/Orbitron[wght].ttf` |
| `AUDIOWIDE` | Audiowide | RETRO | `ofl/audiowide/Audiowide-Regular.ttf` |
| `SHARE_TECH_MONO` | Share Tech Mono | RETRO | `ofl/sharetechmono/ShareTechMono-Regular.ttf` |
| `VT323` | VT323 | RETRO | `ofl/vt323/VT323-Regular.ttf` |
| `MICHROMA` | Michroma | RETRO | `ofl/michroma/Michroma-Regular.ttf` |
| `CAVEAT` | Caveat | HAND | `ofl/caveat/Caveat[wght].ttf` |
| `PACIFICO` | Pacifico | HAND | `ofl/pacifico/Pacifico-Regular.ttf` |
| `DANCING_SCRIPT` | Dancing Script | HAND | `ofl/dancingscript/DancingScript[wght].ttf` |

## 更新するとき

1. 上流の commit を決め、その commit からファイルを取り直す
2. `provenance.tsv` の SHA-256 を取り直す
3. `./gradlew :adapters:font-catalog:test` を実行する。記録と実体がずれていれば落ちる
4. この表と、上の commit の記述を更新する

書体を**足す / 減らす**ときは仕様（FR-043 の「30 書体」）を先に変える。
`TypefaceTest` が数を見ているので、仕様を変えずに増減させることはできない。
