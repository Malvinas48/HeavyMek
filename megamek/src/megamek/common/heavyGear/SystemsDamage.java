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
import java.util.Comparator;
import java.util.List;

/**
 * Applies a damaging hit to a unit: the armor loss and the systems damage tables (notes 02 sections 11-13;
 * B pp.145-147).
 *
 * <p>The 1997 rulebook's tables are used throughout. Where a result could hit one of several parts (which weapon,
 * which movement system) the rules roll a die: odd, the defender picks; even, the attacker picks. HeavyMek has no
 * pop-up questions for that yet, so the picks are made for the players: the attacker always picks the part that
 * hurts most, the defender the part that hurts least.</p>
 *
 * <p>Rulings for things the books leave open, all from the notes:</p>
 * <ul>
 *     <li>A 6 ("roll twice more") rolled during the extra rolls keeps chaining.</li>
 *     <li>The Heavy +1 on subtables A and B also applies to the extra rolls a 6 there calls for.</li>
 *     <li>Casualties are rounded down, with the rulebook's minimum of 1.</li>
 *     <li>Destroyed Fire Control gives -5 in place of the Fire Control rating.</li>
 * </ul>
 */
public final class SystemsDamage {

    private static final int MAX_ROLLS = 50;

    private final Dice dice;
    private final List<String> log;
    private int rollsMade;

    /**
     * @param dice where the dice come from
     * @param log  the battle log; this class adds a line for every roll and effect
     */
    public SystemsDamage(Dice dice, List<String> log) {
        this.dice = dice;
        this.log = log;
    }

    /**
     * Applies the result of a hit: Light and Heavy damage cost armor and roll on the systems damage table, Overkill
     * destroys the unit.
     */
    public void apply(HGUnit unit, DamageResult result) {
        switch (result) {
            case NONE -> note(unit.shortName() + " shrugs off the hit.");
            case OVERKILL -> {
                unit.destroy("overkill");
                note(unit.shortName() + " is destroyed outright (overkill).");
            }
            case LIGHT, HEAVY -> {
                boolean heavy = (result == DamageResult.HEAVY);
                unit.loseArmor(heavy ? 2 : 1);
                note(unit.shortName() + " takes " + result + ". Armor is now " + unit.armorThresholds() + ".");
                if (unit.isOutOfFight()) {
                    note(unit.shortName() + " is destroyed: " + unit.fateReason() + ".");
                    return;
                }
                rollMainTable(unit, heavy);
            }
        }
    }

    /**
     * Applies Light or Heavy damage that has no damage number, such as a crash from a failed sharp turn.
     */
    public void applyCrash(HGUnit unit, boolean heavy) {
        apply(unit, heavy ? DamageResult.HEAVY : DamageResult.LIGHT);
    }

    private void rollMainTable(HGUnit unit, boolean heavy) {
        String table = heavy ? "Heavy" : "Light";
        int pending = 1;
        while ((pending > 0) && !unit.isOutOfFight() && canRollAgain()) {
            pending--;
            int roll = roll();
            switch (roll) {
                case 1 -> {
                    note(table + " systems damage roll " + roll + ": Fire Control.");
                    fireControlSubtable(unit, heavy ? 1 : 0);
                }
                case 2 -> {
                    note(table + " systems damage roll " + roll + ": Structure.");
                    structuralSubtable(unit, heavy ? 1 : 0);
                }
                case 3 -> {
                    note(table + " systems damage roll " + roll + ": Crew.");
                    crewHit(unit, heavy);
                }
                case 4 -> {
                    note(table + " systems damage roll " + roll + ": Movement.");
                    movementHit(unit, heavy);
                }
                case 5 -> {
                    note(table + " systems damage roll " + roll + ": Auxiliary systems.");
                    auxiliaryHit(unit, heavy);
                }
                default -> {
                    note(table + " systems damage roll " + roll + ": roll twice more.");
                    pending += 2;
                }
            }
        }
    }

    /** Subtable A, Fire Control (B p.147). */
    private void fireControlSubtable(HGUnit unit, int modifier) {
        int pending = 1;
        while ((pending > 0) && !unit.isOutOfFight() && canRollAgain()) {
            pending--;
            int die = roll();
            int result = die + modifier;
            String prefix = "  Fire Control roll " + die + ((modifier > 0) ? " +" + modifier : "") + ": ";
            switch (result) {
                case 1 -> damageOneWeapon(unit, -1, prefix);
                case 2 -> damageOneWeapon(unit, -2, prefix);
                case 3 -> {
                    unit.weapons().stream().filter(weapon -> !weapon.isDestroyed())
                          .forEach(weapon -> weapon.addDamagePenalty(-1));
                    note(prefix + "-1 to every weapon.");
                }
                case 4 -> destroyOneWeapon(unit, prefix);
                case 5 -> {
                    unit.destroyFireControl();
                    note(prefix + "Fire Control destroyed, -5 to attacks from now on.");
                }
                case 6 -> {
                    note(prefix + "roll twice more on the Fire Control table.");
                    pending += 2;
                }
                default -> ammunitionHit(unit, prefix);
            }
        }
    }

    private void ammunitionHit(HGUnit unit, String prefix) {
        int die = roll();
        if (die <= 3) {
            unit.ruptureAmmoStore();
            note(prefix + "ammunition or fuel hit (" + die + "): the store ruptures, " + unit.shortName()
                  + " can no longer move or fire.");
        } else {
            unit.destroy("its ammunition exploded");
            note(prefix + "ammunition or fuel hit (" + die + "): chain reaction, " + unit.shortName()
                  + " explodes and its crew is killed.");
        }
    }

    /** Subtable B, Structural, in the 1997 rulebook's version (B p.147). */
    private void structuralSubtable(HGUnit unit, int modifier) {
        int die = roll();
        int result = die + modifier;
        String prefix = "  Structure roll " + die + ((modifier > 0) ? " +" + modifier : "") + ": ";
        switch (result) {
            case 1 -> loseMP(unit, false, prefix);
            case 2 -> loseMP(unit, true, prefix);
            case 3 -> {
                unit.addManeuverPenalty(-1);
                note(prefix + "-1 Maneuver (now " + unit.maneuver() + ").");
            }
            case 4 -> {
                unit.addManeuverPenalty(-2);
                note(prefix + "-2 Maneuver (now " + unit.maneuver() + ").");
            }
            case 5 -> {
                unit.failPowerTransfer();
                note(prefix + "power transfer failure: " + unit.shortName()
                      + " cannot move and defends at 0, but can still fire.");
            }
            case 6 -> {
                int lost = casualties(unit.crew(), 0.75);
                unit.loseCrew(lost);
                note(prefix + "crew compartment failure, " + lost + " crew lost." + crewStatus(unit));
            }
            default -> {
                unit.destroy("its structure failed");
                note(prefix + "complete structural failure: " + unit.shortName()
                      + " is destroyed, but the crew survives.");
            }
        }
    }

    private void crewHit(HGUnit unit, boolean heavy) {
        if (heavy) {
            int lost = casualties(unit.crew(), 0.1);
            unit.loseCrew(lost);
            note("  " + lost + " crew lost." + crewStatus(unit));
        } else {
            unit.stunCrew();
            note("  Crew stunned: " + unit.shortName() + " loses its next action.");
        }
    }

    private void movementHit(HGUnit unit, boolean heavy) {
        loseMP(unit, heavy, "  ");
        if (heavy) {
            unit.addManeuverPenalty(-2);
            note("  -2 Maneuver (now " + unit.maneuver() + ").");
        }
    }

    private void loseMP(HGUnit unit, boolean halve, String prefix) {
        List<HGMovementMode> working = unit.movementModes().stream().filter(mode -> !mode.isKnockedOut()).toList();
        if (working.isEmpty()) {
            note(prefix + "no movement system left to damage.");
            return;
        }
        boolean defenderPicks = defenderPicks();
        HGMovementMode inUse = unit.currentMovementMode();
        HGMovementMode picked;
        if (working.size() == 1) {
            picked = working.get(0);
        } else if (defenderPicks) {
            picked = working.stream().filter(mode -> mode != inUse).findFirst().orElse(working.get(0));
        } else {
            picked = working.contains(inUse) ? inUse : working.get(0);
        }
        if (halve) {
            picked.halveMP();
        } else {
            picked.loseMP(1);
        }
        note(prefix + (halve ? "half the MP" : "-1 MP") + " on " + picked.system() + " (picked by the "
              + (defenderPicks ? "defender" : "attacker") + "), now Combat " + picked.combat() + " / Top "
              + picked.top() + "." + (picked.isKnockedOut() ? " That system is out." : ""));
        if (unit.isImmobile()) {
            note("  " + unit.shortName() + " can no longer move and defends at 0.");
        }
    }

    private void auxiliaryHit(HGUnit unit, boolean heavy) {
        int count = roll();
        List<String> working = new ArrayList<>();
        if (unit.sensorsPenalty() > -5) {
            working.add("Sensors");
        }
        if (unit.communicationsPenalty() > -5) {
            working.add("Communications");
        }
        if (working.isEmpty()) {
            note("  " + count + " auxiliary systems hit, but none are left to damage.");
            return;
        }
        List<String> hit = new ArrayList<>(working.subList(0, Math.min(count, working.size())));
        if ((hit.size() == 1) && (working.size() > 1) && defenderPicks()) {
            // with one hit to place the defender gives up Communications before Sensors
            hit.set(0, working.get(working.size() - 1));
        }
        for (String system : hit) {
            int penalty = heavy ? -5 : -1;
            if (system.equals("Sensors")) {
                unit.addSensorsPenalty(heavy ? -5 - unit.sensorsPenalty() : penalty);
            } else {
                unit.addCommunicationsPenalty(heavy ? -5 - unit.communicationsPenalty() : penalty);
            }
        }
        note("  " + count + " auxiliary system(s) hit: " + String.join(" and ", hit)
              + (heavy ? " destroyed." : " -1."));
    }

    private void damageOneWeapon(HGUnit unit, int penalty, String prefix) {
        HGWeapon weapon = pickWeapon(unit);
        if (weapon == null) {
            note(prefix + penalty + " to a weapon, but none are left.");
            return;
        }
        weapon.addDamagePenalty(penalty);
        note(prefix + penalty + " to " + weapon.code() + " (now " + weapon.damagePenalty() + ")"
              + (weapon.isDestroyed() ? ", destroyed." : (weapon.isOutOfAction() ? ", out of action." : ".")));
    }

    private void destroyOneWeapon(HGUnit unit, String prefix) {
        HGWeapon weapon = pickWeapon(unit);
        if (weapon == null) {
            note(prefix + "a weapon would be destroyed, but none are left.");
            return;
        }
        weapon.destroy();
        note(prefix + weapon.code() + " destroyed.");
    }

    /**
     * Odd-even pick between the working weapons: the attacker takes the strongest, the defender gives up the
     * weakest. "Strongest" is the Damage Multiplier times (1 + Rate of Fire), a rough measure of the damage a weapon
     * can deal in one burst.
     */
    private HGWeapon pickWeapon(HGUnit unit) {
        List<HGWeapon> working = unit.weapons().stream().filter(weapon -> !weapon.isDestroyed()).toList();
        if (working.isEmpty()) {
            return null;
        }
        Comparator<HGWeapon> byValue = Comparator.comparingInt(
              (HGWeapon weapon) -> weapon.damageMultiplier() * (1 + weapon.rateOfFire()));
        if (working.size() == 1) {
            return working.get(0);
        } else if (defenderPicks()) {
            return working.stream().min(byValue).orElseThrow();
        } else {
            return working.stream().max(byValue).orElseThrow();
        }
    }

    private boolean defenderPicks() {
        return roll() % 2 == 1;
    }

    private static int casualties(int crew, double fraction) {
        return Math.max(1, (int) Math.floor(crew * fraction));
    }

    private static String crewStatus(HGUnit unit) {
        return (unit.crew() == 0) ? " " + unit.shortName() + " has no crew left and is out of the fight." : "";
    }

    /** Adds a line to the log, indented under the attack that caused it. */
    private void note(String line) {
        log.add("  " + line);
    }

    private boolean canRollAgain() {
        return rollsMade < MAX_ROLLS;
    }

    private int roll() {
        rollsMade++;
        return dice.d6();
    }
}
