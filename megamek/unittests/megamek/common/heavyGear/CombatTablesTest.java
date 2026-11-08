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

import org.junit.jupiter.api.Test;

/**
 * The plain number tables: target speed, damage against armor, actions, range bands and speed bands.
 */
class CombatTablesTest {

    @Test
    void targetSpeedTable() {
        int[][] expected = { { 0, -3 }, { 1, -2 }, { 2, -2 }, { 3, -1 }, { 4, -1 }, { 5, 0 }, { 6, 0 }, { 7, 1 },
                             { 9, 1 }, { 10, 2 }, { 19, 2 }, { 20, 3 }, { 99, 3 }, { 100, 4 } };
        for (int[] row : expected) {
            assertEquals(row[1], CombatTables.targetSpeedModifier(row[0]), "hexes moved " + row[0]);
        }
    }

    @Test
    void damageAgainstArmor15() {
        assertEquals(DamageResult.NONE, DamageResult.of(14, 15));
        assertEquals(DamageResult.LIGHT, DamageResult.of(15, 15));
        assertEquals(DamageResult.LIGHT, DamageResult.of(29, 15));
        assertEquals(DamageResult.HEAVY, DamageResult.of(30, 15));
        assertEquals(DamageResult.HEAVY, DamageResult.of(44, 15));
        assertEquals(DamageResult.OVERKILL, DamageResult.of(45, 15));
    }

    @Test
    void freeActionsDoubleWithCrew() {
        assertEquals(0, CombatTables.freeActionsForCrew(0));
        assertEquals(1, CombatTables.freeActionsForCrew(1));
        assertEquals(2, CombatTables.freeActionsForCrew(2));
        assertEquals(2, CombatTables.freeActionsForCrew(3));
        assertEquals(3, CombatTables.freeActionsForCrew(4));
        assertEquals(4, CombatTables.freeActionsForCrew(8));
        assertEquals(10, CombatTables.freeActionsForCrew(512));
    }

    @Test
    void lightAutocannonBands() {
        int[] bands = { 2, 4, 8, 16 };
        assertEquals(RangeBand.POINT_BLANK, RangeBand.of(0, bands));
        assertEquals(RangeBand.SHORT, RangeBand.of(1, bands));
        assertEquals(RangeBand.SHORT, RangeBand.of(2, bands));
        assertEquals(RangeBand.MEDIUM, RangeBand.of(3, bands));
        assertEquals(RangeBand.MEDIUM, RangeBand.of(4, bands));
        assertEquals(RangeBand.LONG, RangeBand.of(5, bands));
        assertEquals(RangeBand.LONG, RangeBand.of(8, bands));
        assertEquals(RangeBand.EXTREME, RangeBand.of(9, bands));
        assertEquals(RangeBand.EXTREME, RangeBand.of(16, bands));
        assertEquals(RangeBand.OUT_OF_RANGE, RangeBand.of(17, bands));
    }

    @Test
    void baseRangeOneBands() {
        int[] bands = RangeBand.limitsFor(1);
        assertEquals(RangeBand.SHORT, RangeBand.of(1, bands));
        assertEquals(RangeBand.MEDIUM, RangeBand.of(2, bands));
        assertEquals(RangeBand.LONG, RangeBand.of(4, bands));
        assertEquals(RangeBand.EXTREME, RangeBand.of(8, bands));
        assertEquals(RangeBand.OUT_OF_RANGE, RangeBand.of(9, bands));
    }

    @Test
    void movementBands() {
        assertEquals(MovementBand.STATIONARY, MovementBand.of(false, 0, 6));
        assertEquals(MovementBand.HALF_COMBAT, MovementBand.of(false, 3, 6));
        assertEquals(MovementBand.COMBAT, MovementBand.of(false, 4, 6));
        assertEquals(MovementBand.HALF_COMBAT, MovementBand.of(false, 2, 5));
        assertEquals(MovementBand.COMBAT, MovementBand.of(false, 3, 5));
        assertEquals(MovementBand.TOP, MovementBand.of(true, 9, 6));
        assertEquals(2, MovementBand.STATIONARY.attackModifier());
        assertEquals(-3, MovementBand.TOP.attackModifier());
    }

    @Test
    void speedLossRecalculatesCombatSpeed() {
        HGMovementMode ground = new HGMovementMode(MovementSystem.GROUND, 6, 12);
        ground.loseMP(1);
        assertEquals(11, ground.top());
        assertEquals(6, ground.combat());
        ground.halveMP();
        assertEquals(5, ground.top());
        assertEquals(3, ground.combat());
        ground.loseMP(5);
        assertEquals(0, ground.top());
        assertEquals(true, ground.isKnockedOut());
    }
}
