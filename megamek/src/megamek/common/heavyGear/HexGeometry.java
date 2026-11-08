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
 * Angles between hexes, needed for firing arcs and defense arcs.
 *
 * <p>MegaMek's hexes are flat-topped, and every odd column sits half a hex lower than the even columns. Facing 0
 * is north (up the map) and the facings go clockwise: 1 north-east, 2 south-east, 3 south, 4 south-west,
 * 5 north-west. A <b>bearing</b> here is the angle from the centre of one hex to the centre of another, measured
 * clockwise from a unit's facing: 0 is dead ahead, 90 is to the right, 180 is dead astern and 270 is to the left
 * (notes 02, conventions).</p>
 *
 * <p>Some hexes sit exactly on an arc's edge, for example two columns to the side ("abeam", exactly 90 degrees).
 * Computers store such angles with tiny rounding errors, so every comparison here allows a margin of
 * {@link #EPSILON} degrees. No two hexes on a real map are anywhere near that close in angle.</p>
 */
public final class HexGeometry {

    /** The rounding margin for angle comparisons, in degrees. */
    public static final double EPSILON = 1e-6;

    private static final double SQRT_3 = Math.sqrt(3);

    private HexGeometry() {
        // only static methods
    }

    /**
     * @return the compass bearing from one hex centre to another: 0 is north, 90 east, clockwise, from 0 up to
     *       (but not including) 360. Returns 0 for the same hex.
     */
    public static double bearing(Coords from, Coords to) {
        double dx = centerX(to) - centerX(from);
        double dy = centerY(to) - centerY(from);
        if ((Math.abs(dx) < EPSILON) && (Math.abs(dy) < EPSILON)) {
            return 0;
        }
        // atan2(dx, -dy) measures clockwise from "up" because the screen's y axis points down
        return normalize(Math.toDegrees(Math.atan2(dx, -dy)));
    }

    /**
     * @return the bearing from {@code from} to {@code to}, measured from a unit in {@code from} that faces
     *       {@code facing}: 0 dead ahead, 90 right, 180 astern, 270 left
     */
    public static double relativeBearing(Coords from, int facing, Coords to) {
        return normalize(bearing(from, to) - 60.0 * facing);
    }

    /**
     * Checks whether an angle lies inside an arc, edges included.
     *
     * @param angle the angle to test, in degrees
     * @param start where the arc starts
     * @param width how far the arc runs clockwise from its start
     */
    public static boolean insideArc(double angle, double start, double width) {
        return normalize(angle - start) <= width + EPSILON;
    }

    /**
     * Checks whether an angle lies strictly inside an arc: an angle on either edge does not count.
     */
    public static boolean strictlyInsideArc(double angle, double start, double width) {
        double offset = normalize(angle - start);
        return (offset > EPSILON) && (offset < width - EPSILON);
    }

    /** @return the angle brought into the range 0 up to 360; angles a hair below 360 become 0 */
    public static double normalize(double degrees) {
        double result = degrees % 360;
        if (result < 0) {
            result += 360;
        }
        return (result > 360 - EPSILON) ? 0 : result;
    }

    /** @return how many hexsides apart two facings are, the short way round (0 to 3) */
    public static int hexsideDifference(int facing1, int facing2) {
        int difference = Math.floorMod(facing2 - facing1, 6);
        return Math.min(difference, 6 - difference);
    }

    private static double centerX(Coords coords) {
        return 1.5 * coords.getX();
    }

    private static double centerY(Coords coords) {
        return SQRT_3 * (coords.getY() + (coords.isXOdd() ? 0.5 : 0));
    }
}
