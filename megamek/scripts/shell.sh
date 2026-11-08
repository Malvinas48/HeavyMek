#!/bin/sh

# Copyright (C) 2025 The MegaMek Team. All Rights Reserved.
#
# This file is part of MegaMek.
#
# MegaMek is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License (GPL),
# version 3 or (at your option) any later version,
# as published by the Free Software Foundation.
#
# MegaMek is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty
# of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
# See the GNU General Public License for more details.
#
# A copy of the GPL should have been included with this project;
# if not, see <https://www.gnu.org/licenses/>.
#
# NOTICE: The MegaMek organization is a non-profit group of volunteers
# creating free software for the BattleTech community.
#
# MechWarrior, BattleMech, `Mech and AeroTech are registered trademarks
# of The Topps Company, Inc. All Rights Reserved.
#
# Catalyst Game Labs and the Catalyst Game Labs logo are trademarks of
# InMediaRes Productions, LLC.
#
# MechWarrior Copyright Microsoft Corporation. MegaMek was created under
# Microsoft's "Game Content Usage Rules"
# <https://www.xbox.com/en-US/developers/rules> and it is not endorsed by or
# affiliated with Microsoft.

CDPATH= cd -- "$(dirname -- "$0")/" || exit 1

# HeavyMek needs Java 21 or newer. If JAVA_HOME isn't set and the default `java` is older (for example Java 17,
# kept as the default for other games), look for an installed Java 21+ and use that, without changing the default.
java_version() {
    "$1" -XshowSettings:properties -version 2>&1 | sed -n 's/^ *java\.specification\.version = //p'
}
if [ -z "$JAVA_HOME" ]; then
    current=""
    if command -v java >/dev/null 2>&1; then
        current=$(java_version java)
    fi
    if [ -z "$current" ] || [ "${current%%.*}" -lt 21 ]; then
        for candidate in /usr/lib/jvm/java-2[1-9]-openjdk* /usr/lib/jvm/*-2[1-9]* /usr/lib/jvm/java-[3-9][0-9]-openjdk*; do
            if [ -x "$candidate/bin/java" ]; then
                JAVA_HOME="$candidate"
                export JAVA_HOME
                break
            fi
        done
        if [ -z "$JAVA_HOME" ]; then
            echo "HeavyMek needs Java 21 or newer, but only found Java ${current:-none}." >&2
            echo "On Arch or CachyOS, install it with: sudo pacman -S jdk21-openjdk" >&2
            exit 1
        fi
    fi
fi

# Start the game from its own folder so it finds data/, mmconf/ and docs/.
exec ./bin/HeavyMek "$@"
