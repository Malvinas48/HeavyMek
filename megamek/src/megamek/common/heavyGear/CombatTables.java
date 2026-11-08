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
 * Small rule tables that are just numbers.
 */
public final class CombatTables {

    private CombatTables() {
        // only static methods
    }

    /**
     * The defense modifier for how far the target moved (notes 02 section 4.2; B p.139). Moving fast makes a unit
     * harder to hit; standing still makes it easy.
     *
     * <pre>
     * hexes moved:  0    1-2  3-4  5-6  7-9  10-19  20-99  100+
     * modifier:    -3    -2   -1    0   +1    +2     +3     +4
     * </pre>
     *
     * @param hexesMoved the hexes the target entered in its last move (turning in place does not count)
     */
    public static int targetSpeedModifier(int hexesMoved) {
        if (hexesMoved <= 0) {
            return -3;
        } else if (hexesMoved <= 2) {
            return -2;
        } else if (hexesMoved <= 4) {
            return -1;
        } else if (hexesMoved <= 6) {
            return 0;
        } else if (hexesMoved <= 9) {
            return 1;
        } else if (hexesMoved <= 19) {
            return 2;
        } else if (hexesMoved <= 99) {
            return 3;
        } else {
            return 4;
        }
    }

    /**
     * Free actions for a crew (notes 01 section 4; B p.130): one action, plus one more each time the crew doubles
     * (1 crew: 1 action, 2: 2, 4: 3, 8: 4 ...). A unit with no crew takes no actions.
     */
    public static int freeActionsForCrew(int crew) {
        if (crew <= 0) {
            return 0;
        }
        return 1 + (31 - Integer.numberOfLeadingZeros(crew));
    }
}
