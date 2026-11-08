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

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * The systems damage tables with known dice (notes 02 sections 12-13; B pp.146-147).
 */
class SystemsDamageTest {

    private final HGUnit hunter = HGTestGames.gear("hunter");
    private final List<String> log = new ArrayList<>();

    private void damage(DamageResult result, int... dice) {
        ScriptedDice scripted = new ScriptedDice(dice);
        new SystemsDamage(scripted, log).apply(hunter, result);
        assertEquals(0, scripted.unused(), "every scripted die should be used: " + String.join("\n", log));
    }

    @Test
    void lightDamageCostsOneArmorPoint() {
        damage(DamageResult.LIGHT, 3);
        assertEquals(14, hunter.armor());
    }

    @Test
    void heavyDamageCostsTwoArmorPoints() {
        damage(DamageResult.HEAVY, 3);
        assertEquals(13, hunter.armor());
    }

    @Test
    void overkillDestroys() {
        damage(DamageResult.OVERKILL);
        assertEquals(HGUnit.Fate.DESTROYED, hunter.fate());
    }

    @Test
    void armorReachingZeroDestroys() {
        hunter.loseArmor(14);
        damage(DamageResult.LIGHT);
        assertEquals(0, hunter.armor());
        assertTrue(hunter.isOutOfFight());
    }

    @Test
    void fireControlDestroyed() {
        damage(DamageResult.LIGHT, 1, 5);
        assertEquals(-5, hunter.fireControl());
    }

    @Test
    void heavyFireControlHitCanReachTheAmmunition() {
        damage(DamageResult.HEAVY, 1, 6, 4);
        assertEquals(HGUnit.Fate.DESTROYED, hunter.fate(), "6 + 1 = 7, then 4-6 on the ammunition roll explodes");
    }

    @Test
    void rupturedAmmunitionStopsMovingAndFiring() {
        damage(DamageResult.HEAVY, 1, 6, 2);
        assertFalse(hunter.isOutOfFight());
        assertTrue(hunter.isImmobile());
        assertFalse(hunter.canFire());
    }

    @Test
    void attackerPicksTheStrongestWeapon() {
        damage(DamageResult.LIGHT, 1, 1, 2);
        assertEquals(-1, hunter.weapon(1).damagePenalty(), "even pick roll: the attacker takes the LRP/24");
    }

    @Test
    void defenderGivesUpTheWeakestWeapon() {
        damage(DamageResult.LIGHT, 1, 4, 1);
        assertTrue(hunter.weapon(2).isDestroyed(), "odd pick roll: the defender gives up the APGL");
    }

    @Test
    void minusOneToEveryWeapon() {
        damage(DamageResult.LIGHT, 1, 3);
        assertTrue(hunter.weapons().stream().allMatch(weapon -> weapon.damagePenalty() == -1));
    }

    @Test
    void powerTransferFailure() {
        damage(DamageResult.LIGHT, 2, 5);
        assertTrue(hunter.isImmobile());
        assertTrue(hunter.canFire());
    }

    @Test
    void crewCompartmentFailureOnALightHit() {
        damage(DamageResult.LIGHT, 2, 6);
        assertEquals(HGUnit.Fate.CREW_LOST, hunter.fate(), "the 1997 table allows this on a Light hit");
    }

    @Test
    void heavyStructureHitCanDestroy() {
        damage(DamageResult.HEAVY, 2, 6);
        assertEquals(HGUnit.Fate.DESTROYED, hunter.fate());
    }

    @Test
    void maneuverLoss() {
        damage(DamageResult.LIGHT, 2, 4);
        assertEquals(-2, hunter.maneuver());
    }

    @Test
    void stunnedCrewLosesItsNextAction() {
        damage(DamageResult.LIGHT, 3);
        assertEquals(0, hunter.beginActivationActions());
        assertEquals(1, hunter.beginActivationActions());
    }

    @Test
    void heavyMovementHitHalvesSpeedAndManeuver() {
        damage(DamageResult.HEAVY, 4, 2);
        assertEquals(3, hunter.movementMode(MovementSystem.WALKER).top());
        assertEquals(2, hunter.movementMode(MovementSystem.WALKER).combat());
        assertEquals(-2, hunter.maneuver());
    }

    @Test
    void defenderMovesMovementDamageToTheOtherSystem() {
        damage(DamageResult.LIGHT, 4, 1);
        assertEquals(7, hunter.movementMode(MovementSystem.WALKER).top());
        assertEquals(11, hunter.movementMode(MovementSystem.GROUND).top());
    }

    @Test
    void auxiliarySystems() {
        damage(DamageResult.LIGHT, 5, 4);
        assertEquals(-1, hunter.sensorsPenalty());
        assertEquals(-1, hunter.communicationsPenalty());
    }

    @Test
    void sixesKeepChaining() {
        damage(DamageResult.LIGHT, 6, 6, 3, 3, 3);
        assertEquals(3, hunter.stunnedActions());
    }
}
