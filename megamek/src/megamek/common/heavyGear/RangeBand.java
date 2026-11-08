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

/**
 * The range bands and their attack modifiers (notes 02 section 2.2; B p.138).
 *
 * <p>Range is counted from hex centre to hex centre. A weapon's datacard gives the outer limit of each band, for
 * example 2/4/8/16 for the Light Autocannon: Short out to 2 hexes, Medium to 4, Long to 8, Extreme to 16. Point
 * Blank is the same hex only, as in the 1997 rulebook.</p>
 */
public enum RangeBand {
    POINT_BLANK("Point Blank", 1),
    SHORT("Short", 0),
    MEDIUM("Medium", -1),
    LONG("Long", -2),
    EXTREME("Extreme", -3),
    OUT_OF_RANGE("out of range", 0);

    private final String displayName;
    private final int attackModifier;

    RangeBand(String displayName, int attackModifier) {
        this.displayName = displayName;
        this.attackModifier = attackModifier;
    }

    /**
     * Finds the band for a distance.
     *
     * @param distance   the range in hexes
     * @param bandLimits the outer limits of Short, Medium, Long and Extreme, such as {2, 4, 8, 16}
     *
     * @return the band, or {@link #OUT_OF_RANGE} beyond Extreme
     */
    public static RangeBand of(int distance, int[] bandLimits) {
        if (distance <= 0) {
            return POINT_BLANK;
        } else if (bandLimits.length < 4) {
            return OUT_OF_RANGE;
        } else if (distance <= bandLimits[0]) {
            return SHORT;
        } else if (distance <= bandLimits[1]) {
            return MEDIUM;
        } else if (distance <= bandLimits[2]) {
            return LONG;
        } else if (distance <= bandLimits[3]) {
            return EXTREME;
        } else {
            return OUT_OF_RANGE;
        }
    }

    /** @return the standard band limits for a Base Range: x1, x2, x4 and x8 */
    public static int[] limitsFor(int baseRange) {
        return new int[] { baseRange, 2 * baseRange, 4 * baseRange, 8 * baseRange };
    }

    /** @return the modifier this band gives to the attack roll */
    public int attackModifier() {
        return attackModifier;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
