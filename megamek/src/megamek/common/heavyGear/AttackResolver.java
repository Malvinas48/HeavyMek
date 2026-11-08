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

import megamek.common.board.Coords;

/**
 * Rolls the dice for an attack and applies the damage (notes 02 sections 0-1 and 10-11; B pp.137-145).
 *
 * <ol>
 *     <li>The attacker rolls Gunnery with the attack modifiers; each defender rolls Piloting with its defense
 *     modifiers.</li>
 *     <li>The attack hits only if its total is higher than the defense total; a tie misses. A fumbled attack always
 *     misses. A fumbled defense is hit anyway (unless the attack fumbled too) and counts as 0.</li>
 *     <li>Damage = Margin of Success x Damage Multiplier. A burst adds the Rate of Fire used to the multiplier.</li>
 *     <li>The damage is compared with the defender's armor ({@link DamageResult}) and applied by
 *     {@link SystemsDamage}.</li>
 * </ol>
 * An Area Effect weapon makes one attack roll and every unit in the blast, friends included, defends against it
 * separately. A unit that isn't beaten still takes half the Damage Multiplier, rounded down, as concussion
 * (notes 02 section 10.5). A unit that can't move at all defends with a total of 0 and doesn't roll.
 */
public final class AttackResolver {

    private final HGGame game;
    private final Dice dice;
    private final List<String> log;

    /**
     * @param game the game, for the units in a blast
     * @param dice where the dice come from
     * @param log  the battle log to write to
     */
    public AttackResolver(HGGame game, Dice dice, List<String> log) {
        this.game = game;
        this.dice = dice;
        this.log = log;
    }

    /**
     * Resolves one ranged attack. The caller has already checked it with
     * {@link AttackRules#checkAttack}. Uses the weapon's ammunition.
     *
     * @param attacker the attacking unit
     * @param from     the hex it fires from
     * @param weapon   the weapon
     * @param rof      the Rate of Fire used; 0 is a single shot
     * @param target   the unit aimed at
     * @param band     the attacker's speed this activation
     */
    public void resolve(HGUnit attacker, Coords from, HGWeapon weapon, int rof, HGUnit target, MovementBand band) {
        int distance = from.distance(target.position());
        RangeBand range = AttackRules.rangeBand(weapon, from, target);
        int damageMultiplier = AttackRules.damageMultiplier(weapon, rof);
        weapon.useAmmo(rof);
        log.add(attacker.shortName() + " fires its " + weapon.code() + " at " + target.shortName() + ": "
              + distance + ((distance == 1) ? " hex, " : " hexes, ") + range + " range"
              + ((rof > 0) ? ", burst using ROF " + rof + " (damage x" + damageMultiplier + ")" : "")
              + ((weapon.ammo() != null) ? "; " + weapon.ammo()
              + ((weapon.category() == HGWeapon.Category.ROCKET) ? " rockets" : " rounds") + " left." : "."));

        List<RollModifier> attackModifiers = AttackRules.attackModifiers(attacker, weapon, from, target, band);
        SkillRoll attack = SkillRoll.roll("Gunnery", attacker.crewLevel().gunnery(), attackModifiers, dice);
        log.add("  Attack: " + attack.describe());

        List<HGUnit> defenders = defenders(weapon, target);
        for (HGUnit defender : defenders) {
            defend(attack, defender, from, damageMultiplier, weapon.areaEffectRadius() >= 0);
        }
    }

    private List<HGUnit> defenders(HGWeapon weapon, HGUnit target) {
        int radius = weapon.areaEffectRadius();
        if (radius < 0) {
            return List.of(target);
        }
        List<HGUnit> defenders = new ArrayList<>();
        defenders.add(target);
        game.units().stream()
              .filter(unit -> unit != target)
              .filter(unit -> unit.isDeployed() && !unit.isOutOfFight())
              .filter(unit -> unit.position().distance(target.position()) <= radius)
              .forEach(defenders::add);
        if (defenders.size() > 1) {
            log.add("  Area effect: " + (defenders.size() - 1) + " more unit(s) caught in the blast.");
        }
        return defenders;
    }

    private void defend(SkillRoll attack, HGUnit defender, Coords from, int damageMultiplier, boolean areaEffect) {
        int defenseTotal;
        boolean defenseFumbled;
        if (defender.isImmobile()) {
            defenseTotal = 0;
            defenseFumbled = false;
            log.add("  Defense: " + defender.shortName() + " cannot move, so its defense total is 0.");
        } else {
            List<RollModifier> defenseModifiers = AttackRules.defenseModifiers(defender, from);
            SkillRoll defense = SkillRoll.roll("Piloting", defender.crewLevel().piloting(), defenseModifiers, dice);
            log.add("  Defense (" + defender.shortName() + "): " + defense.describe());
            defenseTotal = defense.total();
            defenseFumbled = defense.fumble();
        }

        int margin;
        boolean hit;
        if (attack.fumble()) {
            hit = false;
            margin = 0;
            log.add("  The attack fumbled: an automatic miss.");
        } else if (defenseFumbled) {
            hit = true;
            margin = attack.total();
            log.add("  The defense fumbled: an automatic hit, Margin of Success " + margin + ".");
        } else {
            margin = attack.total() - defenseTotal;
            hit = margin > 0;
            log.add("  " + attack.total() + " against " + defenseTotal + ": "
                  + (hit ? "hit, Margin of Success " + margin + "." : ((margin == 0) ? "a tie misses." : "miss.")));
        }

        int damage;
        if (hit) {
            damage = margin * damageMultiplier;
            log.add("  Damage " + margin + " x " + damageMultiplier + " = " + damage + " against armor "
                  + defender.armorThresholds() + ".");
        } else if (areaEffect && !attack.fumble()) {
            damage = damageMultiplier / 2;
            log.add("  Concussion from the blast: " + damage + " damage against armor "
                  + defender.armorThresholds() + ".");
        } else {
            return;
        }
        new SystemsDamage(dice, log).apply(defender, DamageResult.of(damage, defender.armor()));
    }
}
