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

import megamek.common.board.Coords;

/**
 * Where an attack comes from, seen by the defender, and the defense modifier that gives (notes 02 section 4.3;
 * B p.139). Only the defender's facing matters; the attacker's facing does not.
 *
 * <ul>
 *     <li><b>Front</b> (0): bearings from 270 through 0 to 90, edges included. Hexes exactly abeam are Front.</li>
 *     <li><b>Rear</b> (-2): the 60 degree wedge straight back, strictly between 150 and 210 degrees.</li>
 *     <li><b>Rear Flank</b> (-1): everything else, the two back corners.</li>
 * </ul>
 * An attack from inside the defender's own hex counts as Front; the 1997 rulebook gives no arc for it.
 */
public enum DefenseArc {
    FRONT("front", 0),
    REAR_FLANK("rear flank", -1),
    REAR("rear", -2);

    private final String displayName;
    private final int defenseModifier;

    DefenseArc(String displayName, int defenseModifier) {
        this.displayName = displayName;
        this.defenseModifier = defenseModifier;
    }

    /**
     * @param defender       the defender's hex
     * @param defenderFacing the defender's facing, 0 to 5
     * @param attacker       the attacker's hex
     */
    public static DefenseArc of(Coords defender, int defenderFacing, Coords attacker) {
        if (defender.equals(attacker)) {
            return FRONT;
        }
        double bearing = HexGeometry.relativeBearing(defender, defenderFacing, attacker);
        if (HexGeometry.insideArc(bearing, 270, 180)) {
            return FRONT;
        } else if (HexGeometry.strictlyInsideArc(bearing, 150, 60)) {
            return REAR;
        } else {
            return REAR_FLANK;
        }
    }

    /** @return the modifier for the defense roll */
    public int defenseModifier() {
        return defenseModifier;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
