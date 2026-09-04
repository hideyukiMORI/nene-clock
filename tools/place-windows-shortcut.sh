#!/usr/bin/env bash
# WSLg が作った Windows のショートカットを、デスクトップへ置く（#50）。
#
# 前提: tools/install-desktop-entry.sh を先に実行していること。
#   WSLg は /usr/share/applications の .desktop を見て、Windows の Start Menu に .lnk を作る。
#   ここでやるのは「それをデスクトップへ複製し、アイコンを差し替える」ことだけである。
#
# 🔴 WSLg が作るアイコンには**ペンギンが合成される**（Linux アプリの目印）。
#    製品のアイコンをそのまま出したいので、実装から書き出した .ico を指し直す。
#
# 🔴 Windows 側のパス操作は PowerShell に閉じる。日本語のフォルダ名（「デスクトップ」など）は、
#    powershell.exe の出力を bash 側で受けると文字化けするため（実測）。
set -euo pipefail

PREFIX="${PREFIX:-/opt/nene-clock}"
DISTRO="${WSL_DISTRO_NAME:-}"
POWERSHELL="${POWERSHELL:-/mnt/c/Windows/System32/WindowsPowerShell/v1.0/powershell.exe}"

if [ -z "$DISTRO" ]; then
  echo "  WSL の中でしか使えない（WSL_DISTRO_NAME が無い）。" >&2
  exit 1
fi
if [ ! -x "$POWERSHELL" ]; then
  echo "  PowerShell が見つからない: $POWERSHELL" >&2
  exit 1
fi
if [ ! -d "$PREFIX" ]; then
  echo "  $PREFIX が無い。先に tools/install-desktop-entry.sh を実行すること。" >&2
  exit 1
fi

echo "==> アイコン（.ico）を Windows 側へ置く"
# .ico は AppIconFiles が実装から書き出したもの（install-desktop-entry.sh が $PREFIX へ置く）。
# 以前は ImageMagick で PNG から作っていたが、作り方が 2 つあると片方だけ古くなる（#66）。
LOCAL_APPDATA="$("$POWERSHELL" -NoProfile -Command '$env:LOCALAPPDATA' | tr -d '\r')"
ICON_DIR="$(wslpath -u "$LOCAL_APPDATA")/NeNeClock"
mkdir -p "$ICON_DIR"
if [ -f "$PREFIX/nene-clock.ico" ]; then
  cp "$PREFIX/nene-clock.ico" "$ICON_DIR/nene-clock.ico"
else
  echo "  $PREFIX/nene-clock.ico が無い（古い install）。install-desktop-entry.sh を入れ直すこと。アイコンは WSLg のもの（ペンギンつき）のままにする。" >&2
fi

echo "==> デスクトップへ置き、アイコンを指し直す"
"$POWERSHELL" -NoProfile -Command "
  [Console]::OutputEncoding = [Text.Encoding]::UTF8
  \$distro = '$DISTRO'
  \$source = [Environment]::GetFolderPath('Programs') + '\' + \$distro + '\NeNe Clock (' + \$distro + ').lnk'
  if (-not (Test-Path -LiteralPath \$source)) {
    Write-Error ('WSLg のショートカットがまだ無い: ' + \$source)
    exit 1
  }
  \$target = [Environment]::GetFolderPath('Desktop') + '\NeNe Clock.lnk'
  Copy-Item -LiteralPath \$source -Destination \$target -Force
  \$icon = \$env:LOCALAPPDATA + '\NeNeClock\nene-clock.ico'
  if (Test-Path -LiteralPath \$icon) {
    \$shortcut = (New-Object -ComObject WScript.Shell).CreateShortcut(\$target)
    \$shortcut.IconLocation = \$icon
    \$shortcut.Save()
  }
  Write-Output ('置いた: ' + \$target)
  Write-Output ('起動:   ' + \$shortcut.TargetPath + ' ' + \$shortcut.Arguments)
"
echo
echo "Start Menu の元は残してある（タスクバーへのピン留めはそちらからでもよい）。"
