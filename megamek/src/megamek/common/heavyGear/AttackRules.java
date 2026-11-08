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
import java.util.List;

import jakarta.annotation.Nullable;
import megamek.common.board.Coords;

/**
 * Whether an attack is allowed, and the modifiers for the attack and defense rolls (notes 02 sections 2-4;
 * B pp.137-139). Nothing here rolls dice, so the player's screen uses the same code to preview a shot that the
 * server uses to resolve it.
 *
 * <p>Not in yet (later stages): line of sight and obscurement (Stage 3, the board is open ground for now), extra
 * declared actions, called shots, indirect fire and attacks on infantry.</p>
 */
public final class AttackRules {

    private AttackRules() {
        // only static methods
    }

    /**
     * Checks a ranged attack.
     *
     * @param game     the game
     * @param attacker the attacking unit
     * @param from     the hex it fires from
     * @param facing   its facing when it fires
     * @param weapon   the weapon, or null when the weapon index was bad
     * @param rof      the Rate of Fire to use; 0 is a single shot
     * @param target   the unit shot at, or null when the target id was bad
     *
     * @return why the attack is not allowed, or null when it is
     */
    public static @Nullable String checkAttack(HGGame game, HGUnit attacker, Coords from, int facing,
          @Nullable HGWeapon weapon, int rof, @Nullable HGUnit target) {
        if (!attacker.canFire()) {
            return attacker.shortName() + " cannot fire.";
        } else if (weapon == null) {
            return "There is no such weapon.";
        } else if (!weapon.isRanged()) {
            return "The " + weapon.code() + " needs the close combat rules, which come in a later stage.";
        } else if (weapon.isOutOfAction()) {
            return "The " + weapon.code() + " is out of action.";
        } else if ((rof < 0) || (rof > weapon.rateOfFire())) {
            return "The " + weapon.code() + " has a Rate of Fire of " + weapon.rateOfFire() + ".";
        } else if (!weapon.hasAmmoFor(rof)) {
            return "Not enough ammunition: that needs " + weapon.ammoNeeded(rof) + ", the " + weapon.code()
                  + " has " + weapon.ammo() + " left.";
        } else if ((target == null) || !target.isDeployed()) {
            return "Pick a target.";
        } else if (target == attacker) {
            return "A unit cannot shoot at itself.";
        } else if (target.isOutOfFight()) {
            return target.shortName() + " is already out of the fight.";
        } else if (!game.areEnemies(attacker, target)) {
            return target.shortName() + " is not an enemy.";
        }
        int distance = from.distance(target.position());
        if (distance > weapon.maxRange()) {
            return target.shortName() + " is out of range: " + distance + " hexes, the " + weapon.code()
                  + " reaches " + weapon.maxRange() + ".";
        } else if (!weapon.arc().contains(from, facing, target.position())) {
            return target.shortName() + " is outside the " + weapon.code() + "'s " + weapon.arc() + " arc.";
        }
        return null;
    }

    /**
     * The attack roll's modifiers besides the crew's Gunnery bonus (notes 02 section 3). Range and movement are
     * always listed so the log shows them; the others only when they aren't zero.
     *
     * @param attacker the attacking unit
     * @param weapon   the weapon
     * @param from     the hex it fires from
     * @param target   the target
     * @param band     the attacker's speed this activation
     */
    public static List<RollModifier> attackModifiers(HGUnit attacker, HGWeapon weapon, Coords from, HGUnit target,
          MovementBand band) {
        List<RollModifier> modifiers = new ArrayList<>();
        if (attacker.isFireControlDestroyed()) {
            modifiers.add(new RollModifier("Fire Control destroyed", attacker.fireControl()));
        } else if (attacker.fireControl() != 0) {
            modifiers.add(new RollModifier("Fire Control", attacker.fireControl()));
        }
        if (weapon.accuracy() != 0) {
            modifiers.add(new RollModifier(weapon.code() + " accuracy", weapon.accuracy()));
        }
        if (weapon.damagePenalty() != 0) {
            modifiers.add(new RollModifier(weapon.code() + " damaged", weapon.damagePenalty()));
        }
        RangeBand range = rangeBand(weapon, from, target);
        modifiers.add(new RollModifier(range + " range", range.attackModifier()));
        modifiers.add(new RollModifier(band.toString(), band.attackModifier()));
        return modifiers;
    }

    /**
     * The defense roll's modifiers besides the crew's Piloting bonus (notes 02 section 4).
     *
     * @param defender the unit attacked
     * @param from     the hex the attack comes from
     */
    public static List<RollModifier> defenseModifiers(HGUnit defender, Coords from) {
        List<RollModifier> modifiers = new ArrayList<>();
        if (defender.maneuver() != 0) {
            modifiers.add(new RollModifier("Maneuver", defender.maneuver()));
        }
        int hexes = defender.hexesForDefense();
        modifiers.add(new RollModifier("moved " + hexes + ((hexes == 1) ? " hex" : " hexes"),
              CombatTables.targetSpeedModifier(hexes)));
        DefenseArc arc = DefenseArc.of(defender.position(), defender.facing(), from);
        modifiers.add(new RollModifier("attacked from the " + arc, arc.defenseModifier()));
        return modifiers;
    }

    /** @return the range band from the firing hex to the target */
    public static RangeBand rangeBand(HGWeapon weapon, Coords from, HGUnit target) {
        return RangeBand.of(from.distance(target.position()), weapon.rangeBands());
    }

    /**
     * @return the Damage Multiplier for an attack: a burst against a vehicle adds the Rate of Fire used
     *       (notes 02 section 10.2; B p.143)
     */
    public static int damageMultiplier(HGWeapon weapon, int rof) {
        return weapon.damageMultiplier() + Math.max(0, rof);
    }
}
