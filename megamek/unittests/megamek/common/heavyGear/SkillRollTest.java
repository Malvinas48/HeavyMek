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

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * The dice-reading test vectors from the rules notes (notes 01 section 1.1, notes 02 section 1.1).
 */
class SkillRollTest {

    private static final Skill NO_BONUS = new Skill(2, 0);

    private static SkillRoll read(int modifier, int... faces) {
        return SkillRoll.of("Test", NO_BONUS, faces, List.of(new RollModifier("situation", modifier)));
    }

    @Test
    void highestDieCounts() {
        assertEquals(5, read(0, 2, 5).total());
    }

    @Test
    void extraSixesAddOne() {
        assertEquals(8, read(0, 6, 2, 6, 6).total());
        assertEquals(7, read(0, 6, 6, 3).total());
        assertEquals(8, read(0, 6, 6, 6).total());
    }

    @Test
    void allOnesIsAFumbleAndIgnoresModifiers() {
        SkillRoll roll = read(3, 1, 1);
        assertTrue(roll.fumble());
        assertEquals(0, roll.total());
        assertTrue(read(2, 1).fumble());
    }

    @Test
    void modifiersAreAdded() {
        assertEquals(6, read(2, 1, 4).total());
        assertEquals(7, read(2, 1, 5).total());
    }

    @Test
    void totalIsFlooredAtZeroWithoutBeingAFumble() {
        SkillRoll roll = read(-5, 3, 2);
        assertEquals(0, roll.total());
        assertFalse(roll.fumble());
        assertEquals(0, read(-4, 1, 2).total());
    }

    @Test
    void skillBonusIsAdded() {
        SkillRoll roll = SkillRoll.of("Gunnery", new Skill(2, 1), new int[] { 4, 6 }, List.of());
        assertEquals(7, roll.total());
        assertEquals("skill bonus", roll.modifiers().get(0).label());
    }

    @Test
    void rollsAsManyDiceAsTheSkillLevel() {
        ScriptedDice dice = new ScriptedDice(2, 3, 4);
        SkillRoll roll = SkillRoll.roll("Piloting", new Skill(3, 1), List.of(), dice);
        assertEquals(3, roll.dice().length);
        assertEquals(5, roll.total());
        assertEquals(0, dice.unused());
    }

    @Test
    void describeShowsTheWorking() {
        SkillRoll roll = SkillRoll.of("Gunnery", new Skill(2, 1), new int[] { 4, 6 },
              List.of(new RollModifier("Medium range", -1)));
        assertEquals("Gunnery 2d6 [4, 6] = 6, skill bonus +1, Medium range -1: total 6", roll.describe());
        assertEquals("Gunnery 2d6 [1, 1]: FUMBLE, total 0",
              SkillRoll.of("Gunnery", new Skill(2, 1), new int[] { 1, 1 }, List.of()).describe());
    }

    @Test
    void crewTableFollowsTheRulebook() {
        assertEquals(new Skill(1, 1), CrewLevel.ROOKIE.gunnery());
        assertEquals(new Skill(2, 0), CrewLevel.QUALIFIED.piloting());
        assertEquals(new Skill(2, 1), CrewLevel.QUALIFIED.gunnery());
        assertEquals(new Skill(3, 1), CrewLevel.VETERAN.piloting());
        assertEquals(new Skill(4, 2), CrewLevel.ELITE.gunnery());
        assertEquals(CrewLevel.VETERAN, CrewLevel.parse("veteran"));
        assertEquals("2/+1", CrewLevel.QUALIFIED.gunnery().toString());
        assertEquals("2/0", CrewLevel.QUALIFIED.piloting().toString());
    }
}
