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
 * The speed a unit is travelling at between rounds (notes 01 sections 3.2 and 5.2-5.5).
 *
 * <p>A unit at Combat Speed gets its Combat MP each round and may spend any of them. A unit at Top Speed gets its
 * Top MP and must spend more than its Combat MP. A unit shifts up to Top by spending exactly all its Combat MP and
 * declaring the shift, and may shift back down after any Top Speed move; either change applies from the next
 * round.</p>
 *
 * <p>{@link #STATIONARY} only exists at set-up: a unit placed as Stationary defends in the first round as if it
 * had not moved, and otherwise behaves as a Combat Speed unit.</p>
 */
public enum SpeedState {
    STATIONARY("Stationary"),
    COMBAT("Combat Speed"),
    TOP("Top Speed");

    private final String displayName;

    SpeedState(String displayName) {
        this.displayName = displayName;
    }

    /** @return true at Top Speed */
    public boolean isTop() {
        return this == TOP;
    }

    /**
     * Reads a speed from a scenario file: stationary, combat or top.
     *
     * @throws IllegalArgumentException for an unknown speed
     */
    public static SpeedState parse(String text) {
        String wanted = text.trim().toUpperCase(Locale.ROOT);
        for (SpeedState state : values()) {
            if (state.name().equals(wanted)) {
                return state;
            }
        }
        throw new IllegalArgumentException("Unknown speed \"" + text + "\"; use Stationary, Combat or Top");
    }

    @Override
    public String toString() {
        return displayName;
    }
}
