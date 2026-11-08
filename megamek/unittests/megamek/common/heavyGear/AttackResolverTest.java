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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Whole attacks with known dice, worked out by hand from the rules (notes 02 sections 1-4, 10-11 and the worked
 * examples in section 21).
 */
class AttackResolverTest {

    private final HGGame game = HGTestGames.twoPlayerGame();
    private final List<String> log = new ArrayList<>();

    private void fire(HGUnit attacker, int weaponIndex, int rof, HGUnit target, MovementBand band, Dice dice) {
        HGWeapon weapon = attacker.weapon(weaponIndex);
        assertEquals(null, AttackRules.checkAttack(game, attacker, attacker.position(), attacker.facing(), weapon,
              rof, target));
        new AttackResolver(game, dice, log).resolve(attacker, attacker.position(), weapon, rof, target, band);
    }

    private boolean logHas(String text) {
        return log.stream().anyMatch(line -> line.contains(text));
    }

    @Test
    void autocannonHitForLightDamage() {
        HGUnit hunter = HGTestGames.place(game, "hunter", NORTH, 5, 8, 0);
        HGUnit jager = HGTestGames.place(game, "jager", SOUTH, 5, 6, 3);
        jager.finishActivation(5);
        // attack: 2d6 [5, 3] = 5, +1 skill, Short range +0, Combat Speed +0 = 6
        // defense: 2d6 [4, 2] = 4, moved 5 hexes +0, front +0 = 4
        // margin 2 x8 = 16 against armor 15: Light. Systems roll 4 = movement, pick roll 2 = attacker's choice
        ScriptedDice dice = new ScriptedDice(5, 3, 4, 2, 4, 2);
        fire(hunter, 0, 0, jager, MovementBand.COMBAT, dice);
        assertEquals(0, dice.unused());
        assertTrue(logHas("6 against 4: hit, Margin of Success 2."), String.join("\n", log));
        assertTrue(logHas("Damage 2 x 8 = 16"));
        assertEquals(14, jager.armor());
        assertEquals(6, jager.movementMode(MovementSystem.WALKER).top(), "the walker in use loses 1 MP");
        assertEquals(3, jager.movementMode(MovementSystem.WALKER).combat());
        assertEquals(59, hunter.weapon(0).ammo());
    }

    @Test
    void burstFromBehindForHeavyDamage() {
        // the rulebook version of worked example 1, without the woodland: a burst from straight behind
        HGUnit hunter = HGTestGames.place(game, "hunter", NORTH, 5, 8, 0);
        HGUnit jager = HGTestGames.place(game, "jager", SOUTH, 5, 5, 0);
        jager.finishActivation(4);
        // attack: [4, 5] = 5, +1 skill, Medium range -1, Half Combat Speed +1 = 6
        // defense: [5, 3] = 5, moved 4 hexes -1, attacked from the rear -2 = 2
        // margin 4 x (8 + ROF 2) = 40: Heavy. Heavy systems roll 3 = 10% crew casualties, at least 1
        ScriptedDice dice = new ScriptedDice(4, 5, 5, 3, 3);
        fire(hunter, 0, 2, jager, MovementBand.HALF_COMBAT, dice);
        assertEquals(0, dice.unused());
        assertTrue(logHas("Damage 4 x 10 = 40"), String.join("\n", log));
        assertEquals(13, jager.armor());
        assertEquals(0, jager.crew());
        assertEquals(HGUnit.Fate.CREW_LOST, jager.fate());
        assertEquals(40, hunter.weapon(0).ammo(), "a burst of ROF 2 uses 20 rounds");
    }

    @Test
    void fumbledAttackMisses() {
        HGUnit hunter = HGTestGames.place(game, "hunter", NORTH, 5, 8, 0);
        HGUnit jager = HGTestGames.place(game, "jager", SOUTH, 5, 6, 3);
        fire(hunter, 0, 0, jager, MovementBand.COMBAT, new ScriptedDice(1, 1, 3, 4));
        assertTrue(logHas("automatic miss"));
        assertEquals(15, jager.armor());
    }

    @Test
    void fumbledDefenseIsHit() {
        HGUnit hunter = HGTestGames.place(game, "hunter", NORTH, 5, 8, 0);
        HGUnit jager = HGTestGames.place(game, "jager", SOUTH, 5, 6, 3);
        // attack [2, 3] = 3 +1 = 4; defense fumbles, so the margin is the full 4: 32 is Heavy.
        // Heavy systems roll 5 = auxiliary, 1 system; pick roll 3 is odd so the defender gives up Communications
        ScriptedDice dice = new ScriptedDice(2, 3, 1, 1, 5, 1, 3);
        fire(hunter, 0, 0, jager, MovementBand.COMBAT, dice);
        assertEquals(0, dice.unused());
        assertTrue(logHas("The defense fumbled"));
        assertEquals(13, jager.armor());
        assertEquals(-5, jager.communicationsPenalty());
        assertEquals(0, jager.sensorsPenalty());
    }

    @Test
    void aTieMisses() {
        HGUnit hunter = HGTestGames.place(game, "hunter", NORTH, 5, 8, 0);
        HGUnit jager = HGTestGames.place(game, "jager", SOUTH, 5, 6, 3);
        jager.finishActivation(5);
        fire(hunter, 0, 0, jager, MovementBand.COMBAT, new ScriptedDice(4, 2, 5, 1));
        assertTrue(logHas("a tie misses"));
        assertEquals(15, jager.armor());
    }

    @Test
    void immobileDefenderDefendsAtZero() {
        HGUnit hunter = HGTestGames.place(game, "hunter", NORTH, 5, 8, 0);
        HGUnit jager = HGTestGames.place(game, "jager", SOUTH, 5, 6, 3);
        jager.failPowerTransfer();
        // attack [2, 1] = 2 +1 = 3, no defense roll: 3 x 8 = 24, Light; systems 6 = roll twice more: 3, 3
        ScriptedDice dice = new ScriptedDice(2, 1, 6, 3, 3);
        fire(hunter, 0, 0, jager, MovementBand.COMBAT, dice);
        assertEquals(0, dice.unused());
        assertEquals(2, jager.stunnedActions());
    }

    @Test
    void grenadeBlastCatchesEveryoneInTheHex() {
        HGUnit hunter = HGTestGames.place(game, "hunter", NORTH, 5, 8, 0);
        HGUnit first = HGTestGames.place(game, "jager", SOUTH, 5, 6, 3);
        HGUnit second = HGTestGames.place(game, "jager", SOUTH, 5, 6, 3);
        // APGL: attack [6, 6] = 7, +1 skill, accuracy -1, Medium range -1 = 6
        // first defends [2, 2] = 2, moved 4 hexes -1 = 1: margin 5 x 3 = 15, Light; systems roll 3 = crew stunned
        // second defends [6, 6] = 7 - 1 = 6: a tie, so only the concussion, 3 / 2 = 1 damage, no effect
        ScriptedDice dice = new ScriptedDice(6, 6, 2, 2, 3, 6, 6);
        fire(hunter, 2, 0, first, MovementBand.COMBAT, dice);
        assertEquals(0, dice.unused());
        assertEquals(14, first.armor());
        assertEquals(1, first.stunnedActions());
        assertEquals(15, second.armor());
        assertTrue(logHas("Concussion"));
        assertEquals(5, hunter.weapon(2).ammo());
    }
}
