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

import megamek.common.board.Coords;

/**
 * The directions a weapon can fire in (notes 01 section 6.9; notes 02 section 2.1; B p.128, p.136).
 *
 * <p>Each arc is tested by bearing from the shooter's hex centre (see {@link HexGeometry}). Hexes exactly on an
 * arc's edge count as inside it. A target in the shooter's own hex is inside every arc; neither book says
 * otherwise and it is the usual hex-game convention.</p>
 */
public enum FiringArc {
    /** Forward 180 degrees. */
    F("Forward"),
    /** The 120 degree fixed-forward cone, which starts one hex in front of the unit. */
    FF("Fixed Forward"),
    /** Right 180 degrees, including dead ahead and dead astern. */
    RT("Right"),
    /** Left 180 degrees, including dead ahead and dead astern. */
    L("Left"),
    /** Rear 180 degrees. */
    RR("Rear"),
    /** Turret: all round. */
    T("Turret");

    private final String displayName;

    FiringArc(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Checks whether a target hex lies in this arc.
     *
     * @param shooter the shooter's hex
     * @param facing  the shooter's facing, 0 to 5
     * @param target  the target's hex
     */
    public boolean contains(Coords shooter, int facing, Coords target) {
        if (shooter.equals(target)) {
            return true;
        }
        double bearing = HexGeometry.relativeBearing(shooter, facing, target);
        return switch (this) {
            case F -> HexGeometry.insideArc(bearing, 270, 180);
            case RR -> HexGeometry.insideArc(bearing, 90, 180);
            case RT -> HexGeometry.insideArc(bearing, 0, 180);
            case L -> HexGeometry.insideArc(bearing, 180, 180);
            case T -> true;
            case FF -> containsFixedForward(shooter, facing, target);
        };
    }

    /**
     * The fixed-forward arc starts one hex ahead of the unit (B p.128): it holds the hex straight ahead and every
     * hex within 60 degrees either side of the facing, seen from that hex's centre. The shooter's own front-left and
     * front-right neighbours are therefore outside it.
     */
    private static boolean containsFixedForward(Coords shooter, int facing, Coords target) {
        Coords apex = shooter.translated(facing);
        if (apex.equals(target)) {
            return true;
        }
        double bearingFromApex = HexGeometry.relativeBearing(apex, facing, target);
        return HexGeometry.insideArc(bearingFromApex, 300, 120);
    }

    /**
     * Reads an arc code from a unit file: F, FF, Rt, L, Rr or T. Upper or lower case both work.
     *
     * @throws IllegalArgumentException for an unknown code
     */
    public static FiringArc parse(String code) {
        String wanted = code.trim().toUpperCase(Locale.ROOT);
        for (FiringArc arc : values()) {
            if (arc.name().equals(wanted)) {
                return arc;
            }
        }
        throw new IllegalArgumentException("Unknown firing arc \"" + code + "\"; use F, FF, Rt, L, Rr or T");
    }

    @Override
    public String toString() {
        return displayName;
    }
}
