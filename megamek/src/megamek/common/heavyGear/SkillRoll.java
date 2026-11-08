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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * One Heavy Gear skill roll, read the Silhouette way (notes 01 section 1.1):
 * <ol>
 *     <li>Roll as many dice as the skill level and take the single highest die. Dice are never added up.</li>
 *     <li>Add 1 for each six beyond the first.</li>
 *     <li>If every die shows 1 the roll is a <b>fumble</b>: the total is 0 and no modifier can change it.</li>
 *     <li>Otherwise add the skill bonus and every situation modifier. A total below 0 counts as 0.</li>
 * </ol>
 * Example: Gunnery 2/+1 rolls 4 and 6, reads 6, adds the +1 bonus and a -1 range penalty: total 6.
 *
 * @param skillName the skill rolled, for the battle log ("Gunnery", "Piloting"...)
 * @param skill     the skill as "dice/bonus"
 * @param dice      the faces rolled
 * @param modifiers every modifier applied, the skill bonus first
 * @param fumble    true when every die showed 1
 * @param total     the final result, never below 0
 */
public record SkillRoll(String skillName, Skill skill, int[] dice, List<RollModifier> modifiers, boolean fumble,
      int total) {

    /**
     * Rolls a skill with the given situation modifiers.
     *
     * @param skillName the skill's name for the log
     * @param skill     the skill to roll
     * @param situation the modifiers besides the skill bonus (range, movement, ...)
     * @param cup       where the dice come from
     *
     * @return the finished roll
     */
    public static SkillRoll roll(String skillName, Skill skill, List<RollModifier> situation, Dice cup) {
        int count = Math.max(1, skill.dice());
        int[] faces = new int[count];
        for (int i = 0; i < count; i++) {
            faces[i] = cup.d6();
        }
        return of(skillName, skill, faces, situation);
    }

    /**
     * Reads dice that have already been rolled. The tests use this to check the arithmetic with known dice.
     */
    public static SkillRoll of(String skillName, Skill skill, int[] faces, List<RollModifier> situation) {
        List<RollModifier> allModifiers = new ArrayList<>();
        if (skill.bonus() != 0) {
            allModifiers.add(new RollModifier("skill bonus", skill.bonus()));
        }
        allModifiers.addAll(situation);
        boolean fumble = isFumble(faces);
        int total = fumble ? 0 : Math.max(0, readDice(faces) + RollModifier.sum(allModifiers));
        return new SkillRoll(skillName, skill, faces.clone(), List.copyOf(allModifiers), fumble, total);
    }

    /**
     * Reads a set of dice: the highest die, plus 1 for each six beyond the first. An empty set reads 0.
     */
    public static int readDice(int... faces) {
        int highest = 0;
        int sixes = 0;
        for (int face : faces) {
            highest = Math.max(highest, face);
            if (face == 6) {
                sixes++;
            }
        }
        return highest + Math.max(0, sixes - 1);
    }

    /** @return true when every die shows 1 */
    public static boolean isFumble(int... faces) {
        return (faces.length > 0) && Arrays.stream(faces).allMatch(face -> face == 1);
    }

    /** @return the dice read before modifiers (highest die plus extra sixes) */
    public int diceResult() {
        return readDice(dice);
    }

    /**
     * @return the roll as one readable line for the battle log, for example
     *       "Gunnery 2d6 [4, 6] = 6, skill bonus +1, Medium range -1: total 6"
     */
    public String describe() {
        String faces = Arrays.stream(dice).mapToObj(Integer::toString).collect(Collectors.joining(", "));
        StringBuilder line = new StringBuilder();
        line.append(skillName).append(' ').append(dice.length).append("d6 [").append(faces).append("]");
        if (fumble) {
            return line.append(": FUMBLE, total 0").toString();
        }
        line.append(" = ").append(diceResult());
        for (RollModifier modifier : modifiers) {
            line.append(", ").append(modifier);
        }
        return line.append(": total ").append(total).toString();
    }

    @Override
    public String toString() {
        return describe();
    }
}
