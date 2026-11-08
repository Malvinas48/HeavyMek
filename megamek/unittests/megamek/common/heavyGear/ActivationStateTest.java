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

import static megamek.common.heavyGear.HGTestGames.NORTH;
import static megamek.common.heavyGear.HGTestGames.SOUTH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import megamek.common.board.Coords;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The movement rules of one activation (notes 01 sections 5.1-5.7).
 */
class ActivationStateTest {

    private HGGame game;
    private HGUnit hunter;
    private HGUnit jager;

    @BeforeEach
    void setUp() {
        game = HGTestGames.twoPlayerGame();
        // a Hunter on its walker system (Combat 4 / Top 7) facing north, a Jager five hexes ahead facing south
        hunter = HGTestGames.place(game, "hunter", NORTH, 5, 8, 0);
        jager = HGTestGames.place(game, "jager", SOUTH, 5, 3, 3);
    }

    private ActivationState start() {
        return new ActivationState(game, hunter);
    }

    @Test
    void movingCostsOneMPPerHex() {
        ActivationState state = start();
        for (int i = 0; i < 4; i++) {
            assertNull(state.tryApply(HGStep.forward()));
        }
        assertEquals(4, state.mpSpent());
        assertEquals(4, state.hexesMoved());
        assertEquals(new Coords(5, 4), state.position());
        assertNotNull(state.tryApply(HGStep.forward()), "the walker's 4 Combat MP are spent");
        assertEquals(MovementBand.COMBAT, state.band());
    }

    @Test
    void turningInOneSpotIsPricedByTheTotalTurn() {
        ActivationState state = start();
        assertNull(state.tryApply(HGStep.turnRight()));
        assertEquals(0, state.mpSpent(), "one hexside is free");
        assertNull(state.tryApply(HGStep.turnRight()));
        assertEquals(1, state.mpSpent(), "two hexsides cost 1 MP");
        assertNull(state.tryApply(HGStep.turnRight()));
        assertEquals(1, state.mpSpent(), "three hexsides still cost 1 MP");
        assertNull(state.tryApply(HGStep.turnLeft()));
        assertNull(state.tryApply(HGStep.turnLeft()));
        assertEquals(0, state.mpSpent(), "turning back to one hexside is free again");
        assertEquals(1, state.facing());
        assertEquals(MovementBand.STATIONARY, state.band(), "a free turn keeps the unit stationary");
    }

    @Test
    void enteringAHexStartsANewTurn() {
        ActivationState state = start();
        state.tryApply(HGStep.turnRight());
        state.tryApply(HGStep.forward());
        assertNull(state.tryApply(HGStep.turnRight()));
        assertEquals(1, state.mpSpent(), "a second one-hexside turn after moving is free too");
    }

    @Test
    void reversingIsLimitedToHalfCombatSpeed() {
        ActivationState state = start();
        assertNull(state.tryApply(HGStep.backward()));
        assertNull(state.tryApply(HGStep.backward()));
        assertEquals(new Coords(5, 10), state.position());
        assertEquals(0, state.facing(), "reversing keeps the facing");
        assertEquals(MovementBand.HALF_COMBAT, state.band());
        assertNotNull(state.tryApply(HGStep.backward()));
        assertNotNull(state.tryApply(HGStep.forward()), "after reversing the whole move stays at half speed");
    }

    @Test
    void changingToGroundKeepsTheSmallerPool() {
        ActivationState state = start();
        assertNull(state.tryApply(HGStep.switchTo(MovementSystem.GROUND)));
        assertEquals(MovementSystem.GROUND, state.system());
        assertEquals(4, state.mpPool(), "the walker's 4 MP, not the ground system's 6");
        assertNotNull(state.tryApply(HGStep.switchTo(MovementSystem.WALKER)), "only one change per round");
    }

    @Test
    void changingSystemAfterSpendingTheNewAllowanceStops() {
        hunter.setMovementSystem(MovementSystem.GROUND);
        ActivationState state = start();
        for (int i = 0; i < 4; i++) {
            state.tryApply(HGStep.turnRight());
            state.tryApply(HGStep.forward());
        }
        assertEquals(4, state.mpSpent());
        assertNull(state.tryApply(HGStep.switchTo(MovementSystem.WALKER)));
        assertEquals(0, state.mpLeft(), "the walker allows 4 MP and 4 are spent");
    }

    @Test
    void topSpeed() {
        hunter.setMovementSystem(MovementSystem.GROUND);
        hunter.setSpeedState(SpeedState.TOP);
        hunter.setPosition(new Coords(5, 14), 0);
        ActivationState state = start();
        assertEquals(12, state.mpPool());
        assertEquals(MovementBand.TOP, state.band());
        assertNotNull(state.tryApply(HGStep.backward()), "no reversing at Top Speed");
        assertNotNull(state.tryApply(HGStep.switchTo(MovementSystem.WALKER)), "no system change at Top Speed");
        assertNotNull(state.topSpeedWarning(), "at least 7 MP are needed");
        for (int i = 0; i < 7; i++) {
            assertNull(state.tryApply(HGStep.forward()));
        }
        assertNull(state.topSpeedWarning());
        state.tryApply(HGStep.turnRight());
        state.tryApply(HGStep.turnRight());
        assertFalse(state.sharpTurnPending(), "two hexsides need no test in the 1997 rules");
        state.tryApply(HGStep.turnRight());
        assertTrue(state.sharpTurnPending(), "three hexsides at Top Speed need a Piloting test");
        assertEquals(4, state.sharpTurnThreshold());
    }

    @Test
    void shiftingUpNeedsExactlyTheCombatMP() {
        ActivationState state = start();
        state.tryApply(HGStep.forward());
        state.tryApply(HGStep.forward());
        state.tryApply(HGStep.forward());
        assertFalse(state.canChangeSpeed());
        state.tryApply(HGStep.forward());
        assertTrue(state.canChangeSpeed());
    }

    @Test
    void cannotLeaveTheMap() {
        hunter.setPosition(new Coords(0, 0), 0);
        assertNotNull(start().tryApply(HGStep.forward()));
    }

    @Test
    void aHexHoldsThirtySizePoints() {
        Coords crowded = new Coords(5, 7);
        for (int i = 0; i < 4; i++) {
            HGTestGames.place(game, "hunter", NORTH, crowded.getX(), crowded.getY(), 0);
        }
        assertNull(new ActivationState(game, hunter).check(HGStep.forward()), "4 Gears of Size 6 leave room");
        HGTestGames.place(game, "hunter", NORTH, crowded.getX(), crowded.getY(), 0);
        assertNotNull(new ActivationState(game, hunter).check(HGStep.forward()), "5 Gears fill the hex");
    }

    @Test
    void oneShotPerAction() {
        ActivationState state = start();
        assertNull(state.tryApply(HGStep.fire(0, jager.getId(), 2)));
        assertNotNull(state.tryApply(HGStep.fire(1, jager.getId(), 0)), "a Gear has one action");
        assertNotNull(state.tryApply(HGStep.turnRight()), "no turning after firing until the next hex");
        assertNull(state.tryApply(HGStep.forward()));
        assertNull(state.tryApply(HGStep.turnRight()));
    }

    @Test
    void firingChecksArcAndAmmunition() {
        HGWeapon autocannon = hunter.weapon(0);
        autocannon.useAmmo(2);
        autocannon.useAmmo(2);
        autocannon.useAmmo(1);
        assertEquals(10, autocannon.ammo());
        ActivationState state = start();
        assertNotNull(state.check(HGStep.fire(0, jager.getId(), 2)), "a full burst needs 20 rounds");
        assertNull(state.check(HGStep.fire(0, jager.getId(), 1)), "10 rounds are enough for ROF 1");
        assertEquals(1, autocannon.maxUsableRateOfFire());
        state.tryApply(HGStep.turnRight());
        state.tryApply(HGStep.turnRight());
        state.tryApply(HGStep.turnRight());
        assertNotNull(state.check(HGStep.fire(0, jager.getId(), 0)), "the Jager is behind the Hunter now");
    }

    @Test
    void firingRules() {
        ActivationState state = start();
        assertNull(state.check(HGStep.fire(0, jager.getId(), 0)), "LAC, 5 hexes, Long range, forward arc");
        assertNull(state.check(HGStep.fire(1, jager.getId(), 3)), "LRP/24, 5 hexes is within its 8");
        assertNotNull(state.check(HGStep.fire(0, jager.getId(), 3)), "the LAC's Rate of Fire is 2");
        assertNotNull(state.check(HGStep.fire(3, jager.getId(), 0)), "hand grenades are thrown");
        assertNotNull(state.check(HGStep.fire(0, hunter.getId(), 0)), "not at itself");
        assertNotNull(state.check(HGStep.fire(9, jager.getId(), 0)), "no such weapon");
    }
}
