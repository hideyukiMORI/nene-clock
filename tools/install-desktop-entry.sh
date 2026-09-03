#!/usr/bin/env bash
# NeNe Clock を「Gradle 無しで起動できる形」にして、デスクトップエントリを置く。
#
# WSLg はここで置く .desktop から Windows の Start Menu へショートカット（.lnk）を作る。
# 生成された .lnk は、タスクバーへのピン留めにも、デスクトップへのコピーにも使える（FR-030 / #50）。
#
# 何度実行しても同じ結果になる（冪等）。消すときは tools/uninstall-desktop-entry.sh。
set -euo pipefail

PREFIX="${PREFIX:-/opt/nene-clock}"
ENTRY_DIR="${ENTRY_DIR:-/usr/share/applications}"
ENTRY="$ENTRY_DIR/nene-clock.desktop"
REPO="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
JAVA_HOME="${JAVA_HOME:-/usr/lib/jvm/java-21-openjdk-amd64}"
export JAVA_HOME

if [ "$(id -u)" -ne 0 ] && [ ! -w "$ENTRY_DIR" ]; then
  echo "  $ENTRY_DIR へ書けない。sudo で実行するか ENTRY_DIR= を指定すること。" >&2
  exit 1
fi

echo "==> 起動できる形に組み立てる（installDist）"
"$REPO/gradlew" -p "$REPO" --quiet installDist writeAppIcons

echo "==> $PREFIX へ置く"
rm -rf "$PREFIX"
mkdir -p "$PREFIX"
cp -r "$REPO/app/build/install/app/." "$PREFIX/"
cp "$REPO/app/build/icons/"*.png "$PREFIX/"
chmod -R a+rX "$PREFIX"
chmod a+x "$PREFIX/bin/app"

echo "==> $ENTRY を書く"
cat > "$ENTRY" <<DESKTOP
[Desktop Entry]
Type=Application
Version=1.0
Name=NeNe Clock
Comment=A frameless desktop clock
Comment[ja]=枠の無いデスクトップ時計
Exec=$PREFIX/bin/app
Icon=$PREFIX/nene-clock-256.png
Terminal=false
Categories=Utility;Clock;
StartupWMClass=io-github-hideyukimori-neneclock-app-NeNeClockApplication
DESKTOP
chmod a+r "$ENTRY"

echo
echo "置いた:"
echo "  アプリ    $PREFIX/bin/app"
echo "  エントリ  $ENTRY"
echo
echo "WSLg が Windows の Start Menu にショートカットを作るまで少し待つ。"
echo "できたショートカットは、タスクバーへのピン留めにもデスクトップへのコピーにも使える。"
