/*
 * Copyright (C) 2026 The HeavyMek contributors.
 *
 * This file is part of HeavyMek, a fork of MegaMek.
 *
 * HeavyMek is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License (GPL), version 3 or (at your option) any later version, as published by the Free Software Foundation.
 *
 * HeavyMek is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * A copy of the GPL should have been included with this project; if not, see <https://www.gnu.org/licenses/>.
 *
 * Heavy Gear is a trademark of Dream Pod 9, Inc. HeavyMek is an unofficial fan project, not endorsed by Dream Pod 9.
 */

package megamek.common.heavyGear;

import java.util.Locale;

/**
 * A way of moving. Gears have two: walking on their legs (primary) and rolling on the wheels or tracks in their feet,
 * the Secondary Movement System, written "ground" in the unit files (notes 01 section 5.6).
 *
 * <p>Each system has its own Combat and Top speeds. In later stages each will also pay its own terrain costs.</p>
 */
public enum MovementSystem {
    WALKER("Walker"),
    GROUND("Ground"),
    HOVER("Hover"),
    TRACKED("Tracked"),
    WHEELED("Wheeled"),
    NAVAL("Naval");

    private final String displayName;

    MovementSystem(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Reads a movement system from a unit or scenario file. Upper or lower case both work.
     *
     * @throws IllegalArgumentException for an unknown system
     */
    public static MovementSystem parse(String text) {
        String wanted = text.trim().toUpperCase(Locale.ROOT);
        for (MovementSystem system : values()) {
            if (system.name().equals(wanted)) {
                return system;
            }
        }
        throw new IllegalArgumentException("Unknown movement system \"" + text + "\"");
    }

    @Override
    public String toString() {
        return displayName;
    }
}
