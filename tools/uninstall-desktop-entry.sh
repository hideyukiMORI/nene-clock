#!/usr/bin/env bash
# install-desktop-entry.sh が置いたものを消す。
set -euo pipefail

PREFIX="${PREFIX:-/opt/nene-clock}"
ENTRY="${ENTRY_DIR:-/usr/share/applications}/nene-clock.desktop"

rm -rf "$PREFIX"
rm -f "$ENTRY"
echo "消した: $PREFIX と $ENTRY"
echo "Windows 側のショートカットは WSLg が追って消す。デスクトップへコピーした分は手で消すこと。"
