#!/bin/bash
# Starts an installed HeavyMek, waits, performs clicks/keys with RobotClick, and takes screenshots.
# Meant to run inside a virtual display, for example:
#   xvfb-run -a -s "-screen 0 1600x1000x24" tools/ui-check/ui-session.sh \
#       megamek/build/install/HeavyMek 40 /tmp/shot click:804:722 sleep:1500 click:1400:626 sleep:15000
# Writes <prefix>_0.png (before the steps) and <prefix>_1.png (after them). Needs ImageMagick's `import`.
dir="$1"; secs="$2"; out="$3"; shift 3
here="$(cd "$(dirname "$0")" && pwd)"
cd "$dir" || exit 1
./HeavyMek.sh > /tmp/heavymek_stdout.txt 2>&1 &
sleep "$secs"
import -window root "${out}_0.png" 2>/dev/null
java "$here/RobotClick.java" "$@"
import -window root "${out}_1.png" 2>/dev/null
pkill -f "megamek.MegaMek" 2>/dev/null
sleep 2
