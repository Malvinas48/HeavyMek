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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import megamek.common.board.Coords;
import org.junit.jupiter.api.Test;

/**
 * Firing arcs and defense arcs against the hex diagrams in the notes (notes 01 section 6.9, notes 02 section 4.3).
 * Every check runs for all six facings and for a unit in an even and an odd column, because odd columns sit half a
 * hex lower.
 */
class ArcsTest {

    private static final Coords[] UNITS = { new Coords(4, 4), new Coords(5, 5) };

    /** Neighbour "rel" steps clockwise from the unit's facing: 0 front, 1 front-right ... 5 front-left. */
    private static Coords neighbour(Coords unit, int facing, int rel) {
        return unit.translated((facing + rel) % 6);
    }

    private static void assertArcNeighbours(FiringArc arc, boolean... inArc) {
        for (Coords unit : UNITS) {
            for (int facing = 0; facing < 6; facing++) {
                for (int rel = 0; rel < 6; rel++) {
                    assertEquals(inArc[rel], arc.contains(unit, facing, neighbour(unit, facing, rel)),
                          arc.name() + " unit " + unit + " facing " + facing + " neighbour rel" + rel);
                }
            }
        }
    }

    @Test
    void forwardArcNeighbours() {
        assertArcNeighbours(FiringArc.F, true, true, false, false, false, true);
    }

    @Test
    void sideArcsIncludeDeadAheadAndAstern() {
        assertArcNeighbours(FiringArc.RT, true, true, true, true, false, false);
        assertArcNeighbours(FiringArc.L, true, false, false, true, true, true);
    }

    @Test
    void rearArcNeighbours() {
        assertArcNeighbours(FiringArc.RR, false, false, true, true, true, false);
    }

    @Test
    void turretSeesEverything() {
        assertArcNeighbours(FiringArc.T, true, true, true, true, true, true);
    }

    @Test
    void fixedForwardStartsOneHexAhead() {
        assertArcNeighbours(FiringArc.FF, true, false, false, false, false, false);
        for (Coords unit : UNITS) {
            for (int facing = 0; facing < 6; facing++) {
                Coords apex = unit.translated(facing);
                // straight ahead, and the rows running out from the apex at 60 degrees, are inside
                assertTrue(FiringArc.FF.contains(unit, facing, apex.translated(facing, 3)));
                assertTrue(FiringArc.FF.contains(unit, facing, apex.translated((facing + 1) % 6, 2)));
                assertTrue(FiringArc.FF.contains(unit, facing, apex.translated((facing + 5) % 6, 2)));
                // a hex further round, beside the cone, is not
                Coords besideCone = neighbour(unit, facing, 1).translated((facing + 1) % 6);
                assertFalse(FiringArc.FF.contains(unit, facing, besideCone));
            }
        }
    }

    @Test
    void abeamHexIsInBothForwardAndRear() {
        for (Coords unit : UNITS) {
            for (int facing = 0; facing < 6; facing++) {
                Coords abeamRight = neighbour(unit, facing, 1).translated((facing + 2) % 6);
                assertEquals(90, HexGeometry.relativeBearing(unit, facing, abeamRight), 1e-9);
                assertTrue(FiringArc.F.contains(unit, facing, abeamRight));
                assertTrue(FiringArc.RR.contains(unit, facing, abeamRight));
                assertEquals(DefenseArc.FRONT, DefenseArc.of(unit, facing, abeamRight));
            }
        }
    }

    @Test
    void sameHexIsInEveryArc() {
        for (FiringArc arc : FiringArc.values()) {
            assertTrue(arc.contains(UNITS[0], 2, UNITS[0]));
        }
        assertEquals(DefenseArc.FRONT, DefenseArc.of(UNITS[0], 2, UNITS[0]));
    }

    @Test
    void defenseArcNeighbours() {
        DefenseArc[] expected = { DefenseArc.FRONT, DefenseArc.FRONT, DefenseArc.REAR_FLANK, DefenseArc.REAR,
                                  DefenseArc.REAR_FLANK, DefenseArc.FRONT };
        for (Coords unit : UNITS) {
            for (int facing = 0; facing < 6; facing++) {
                for (int rel = 0; rel < 6; rel++) {
                    assertEquals(expected[rel], DefenseArc.of(unit, facing, neighbour(unit, facing, rel)),
                          "unit " + unit + " facing " + facing + " rel" + rel);
                }
            }
        }
    }

    @Test
    void rearWedgeFurtherOut() {
        for (Coords unit : UNITS) {
            for (int facing = 0; facing < 6; facing++) {
                int rear = (facing + 3) % 6;
                Coords rearNeighbour = unit.translated(rear);
                // range 2: only the hex straight back is Rear; the hexes on the 150 and 210 degree lines are not
                assertEquals(DefenseArc.REAR, DefenseArc.of(unit, facing, unit.translated(rear, 2)));
                assertEquals(DefenseArc.REAR_FLANK,
                      DefenseArc.of(unit, facing, rearNeighbour.translated((facing + 2) % 6)));
                assertEquals(DefenseArc.REAR_FLANK,
                      DefenseArc.of(unit, facing, rearNeighbour.translated((facing + 4) % 6)));
                // range 3: the hex three straight back and the two beside it are Rear
                Coords twoBack = unit.translated(rear, 2);
                assertEquals(DefenseArc.REAR, DefenseArc.of(unit, facing, unit.translated(rear, 3)));
                assertEquals(DefenseArc.REAR, DefenseArc.of(unit, facing, twoBack.translated((facing + 2) % 6)));
                assertEquals(DefenseArc.REAR, DefenseArc.of(unit, facing, twoBack.translated((facing + 4) % 6)));
            }
        }
    }

    @Test
    void hexsideDifferenceGoesTheShortWay() {
        assertEquals(0, HexGeometry.hexsideDifference(2, 2));
        assertEquals(1, HexGeometry.hexsideDifference(0, 5));
        assertEquals(2, HexGeometry.hexsideDifference(1, 5));
        assertEquals(3, HexGeometry.hexsideDifference(0, 3));
    }
}
