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

package megamek.server.heavyGear;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import megamek.common.Player;
import megamek.common.heavyGear.Dice;
import megamek.common.heavyGear.HGGame;
import megamek.common.heavyGear.HGPlayerTurn;
import megamek.common.heavyGear.HGUnit;
import megamek.common.heavyGear.Skill;
import megamek.common.heavyGear.SkillRoll;

/**
 * Who goes first: the set-up roll before the battle and the initiative roll at the start of every round
 * (notes 01 sections 3.2 and 3.5; B pp.128-129). For now every player is one side and every unit is its own combat
 * group; combat groups come in Stage 4.
 *
 * <p><b>Set-up:</b> each side rolls its commander's skill level; a fumble counts as 1 and ties are rolled again.
 * The winner chooses who places a unit first. The sides then take turns placing one unit at a time.</p>
 *
 * <p><b>Each round:</b> each side rolls its commander's Leadership, or Piloting when every side has exactly one
 * unit left. The highest total wins; ties are rolled again; a fumble loses. The side with fewer units chooses who
 * activates first, or the winner when both have as many. Then the sides take turns activating one unit at a time;
 * a side that runs out of units drops out and the other carries on.</p>
 *
 * <p>Until the game can ask, a side that gets to choose always lets the other side go first, which is usually the
 * better choice: moving second means seeing the enemy's move before making yours. The commander is a side's first
 * unit still in the fight.</p>
 */
final class HGInitiative {

    private final HGGame game;
    private final Dice dice;
    private final List<String> log;

    HGInitiative(HGGame game, Dice dice, List<String> log) {
        this.game = game;
        this.dice = dice;
        this.log = log;
    }

    /**
     * Rolls set-up initiative and builds the placement turns for every unit that isn't on the map yet.
     *
     * @return the turns, in order
     */
    List<HGPlayerTurn> setupTurns() {
        Map<Integer, Integer> unitsToPlace = new HashMap<>();
        for (HGUnit unit : game.units()) {
            if (!unit.isDeployed()) {
                unitsToPlace.merge(unit.getOwnerId(), 1, Integer::sum);
            }
        }
        List<Integer> sides = new ArrayList<>(unitsToPlace.keySet());
        sides.sort(Comparator.naturalOrder());
        if (sides.isEmpty()) {
            return new ArrayList<>();
        }
        log.add("Set-up: the sides roll to see who places a unit first.");
        List<Integer> order = rankSides(sides, true);
        // the winner (first in the ranking) chooses, and lets the others place first
        List<Integer> placingOrder = new ArrayList<>(order.reversed());
        if (placingOrder.size() > 1) {
            log.add(name(order.get(0)) + " wins and lets " + name(placingOrder.get(0)) + " place first.");
        }
        return interleave(placingOrder, unitsToPlace);
    }

    /**
     * Rolls initiative for a new round and builds the activation turns.
     *
     * @return the turns, in order
     */
    List<HGPlayerTurn> roundTurns() {
        Map<Integer, Integer> fighting = new HashMap<>();
        for (HGUnit unit : game.units()) {
            if (unit.isDeployed() && !unit.isOutOfFight()) {
                fighting.merge(unit.getOwnerId(), 1, Integer::sum);
            }
        }
        List<Integer> sides = new ArrayList<>(fighting.keySet());
        sides.sort(Comparator.naturalOrder());
        if (sides.size() < 2) {
            return interleave(sides, fighting);
        }
        List<Integer> ranking = rankSides(sides, false);
        int winner = ranking.get(0);
        List<Integer> activationOrder;
        if (sides.size() == 2) {
            int loser = ranking.get(1);
            // the side with fewer units chooses; with equal numbers the winner does
            int chooser = (fighting.get(loser) < fighting.get(winner)) ? loser : winner;
            int other = (chooser == winner) ? loser : winner;
            activationOrder = List.of(other, chooser);
            log.add(name(chooser) + " chooses and lets " + name(other) + " activate first.");
        } else {
            activationOrder = new ArrayList<>(ranking.reversed());
            log.add("The lowest roll activates first.");
        }
        return interleave(activationOrder, fighting);
    }

    /**
     * Rolls for every side and ranks them, best first. Ties for a place are rolled again.
     *
     * @param setup true for the set-up roll, where a fumble counts as 1
     */
    private List<Integer> rankSides(List<Integer> sides, boolean setup) {
        Map<Integer, Integer> results = new HashMap<>();
        for (int side : sides) {
            results.put(side, rollFor(side, setup));
        }
        List<Integer> ranked = new ArrayList<>(sides);
        ranked.sort(Comparator.comparing((Integer side) -> results.get(side)).reversed());
        // roll off any ties, a few times at most; a tie that survives keeps the order above
        for (int attempt = 0; attempt < 10; attempt++) {
            boolean tied = false;
            for (int i = 0; i + 1 < ranked.size(); i++) {
                if (results.get(ranked.get(i)).equals(results.get(ranked.get(i + 1)))) {
                    tied = true;
                    break;
                }
            }
            if (!tied) {
                break;
            }
            log.add("A tie: roll again.");
            for (int side : sides) {
                results.put(side, rollFor(side, setup));
            }
            ranked.sort(Comparator.comparing((Integer side) -> results.get(side)).reversed());
        }
        return ranked;
    }

    /**
     * @return the side's result; a fumbled round initiative roll counts as -1 so it always loses (two fumbles tie
     *       and are rolled again)
     */
    private int rollFor(int side, boolean setup) {
        List<HGUnit> units = game.fightingUnitsOf(side);
        if (setup) {
            units = game.units().stream().filter(unit -> unit.getOwnerId() == side).toList();
        }
        if (units.isEmpty()) {
            return -1;
        }
        HGUnit commander = units.get(0);
        boolean duel = !setup && everySideHasOneUnit();
        Skill skill = duel ? commander.crewLevel().piloting() : commander.crewLevel().leadership();
        String skillName = duel ? "Piloting" : (setup ? "Set-up (" + commander.crewLevel() + ")" : "Leadership");
        SkillRoll roll = SkillRoll.roll(skillName, skill, List.of(), dice);
        int result = roll.fumble() ? (setup ? 1 : -1) : roll.total();
        log.add("  " + name(side) + " (" + commander.shortName() + "): " + roll.describe()
              + (roll.fumble() ? (setup ? ", counts as 1" : ", loses") : ""));
        return result;
    }

    private boolean everySideHasOneUnit() {
        Map<Integer, Integer> fighting = new HashMap<>();
        game.units().stream().filter(unit -> unit.isDeployed() && !unit.isOutOfFight())
              .forEach(unit -> fighting.merge(unit.getOwnerId(), 1, Integer::sum));
        return fighting.values().stream().allMatch(count -> count == 1);
    }

    /** Takes turns between the sides in the given order until every side has used up its units. */
    static List<HGPlayerTurn> interleave(List<Integer> order, Map<Integer, Integer> unitCounts) {
        Map<Integer, Integer> left = new HashMap<>(unitCounts);
        List<HGPlayerTurn> turns = new ArrayList<>();
        boolean added = true;
        while (added) {
            added = false;
            for (int side : order) {
                if (left.getOrDefault(side, 0) > 0) {
                    turns.add(new HGPlayerTurn(side));
                    left.merge(side, -1, Integer::sum);
                    added = true;
                }
            }
        }
        return turns;
    }

    private String name(int playerId) {
        Player player = game.getPlayer(playerId);
        return (player == null) ? "Player " + playerId : player.getName();
    }
}
