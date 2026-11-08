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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jakarta.annotation.Nullable;

/**
 * One weapon on one unit: the numbers from the unit file plus what has happened to it in the battle (ammunition
 * used, damage taken).
 *
 * <p>Damage to a weapon is an accuracy penalty that adds up: at -5 or worse the weapon is out of action, at -6 or
 * worse it is destroyed (notes 02 section 13.1).</p>
 *
 * <p>Ammunition is counted round by round, as the 1997 rulebook does (notes 02 section 10.2): a single shot uses
 * one round; a burst uses 10 rounds per point of Rate of Fire used, or for rockets and missiles 2, 4, 8 or 16
 * missiles for ROF 1, 2, 3 or 4.</p>
 */
public class HGWeapon implements Serializable {

    /** The broad weapon families. Rockets and missiles count their ammunition differently. */
    public enum Category {
        GUN, ROCKET, SUPPORT, ADVANCED, MELEE;

        /** Reads a category from a unit file; upper or lower case both work. */
        public static Category parse(String text) {
            return valueOf(text.trim().toUpperCase(Locale.ROOT));
        }
    }

    /** The penalty at which a weapon stops working. */
    public static final int OUT_OF_ACTION_PENALTY = -5;

    private final String name;
    private final String code;
    private final String model;
    private final FiringArc arc;
    private final int accuracy;
    private final int damageMultiplier;
    private final int baseRange;
    private final int[] rangeBands;
    private final int rateOfFire;
    private final Integer maxAmmo;
    private final Category category;
    private final ArrayList<String> special;

    private Integer ammo;
    private int damagePenalty;
    private boolean destroyed;

    /**
     * @param name             the full name, such as "Light Autocannon"
     * @param code             the short code, such as "LAC"
     * @param model            the in-world model name, may be empty
     * @param arc              the firing arc
     * @param accuracy         the Accuracy modifier
     * @param damageMultiplier the Damage Multiplier (the "x8" on the card)
     * @param baseRange        the Base Range, the top of Short range
     * @param rangeBands       the four band limits; when null they follow from the Base Range
     * @param rateOfFire       the Rate of Fire
     * @param ammo             the rounds carried, or null for weapons that use no ammunition
     * @param category         the weapon family
     * @param special          the special characteristics, such as "IF" or "AE0"
     */
    public HGWeapon(String name, String code, String model, FiringArc arc, int accuracy, int damageMultiplier,
          int baseRange, @Nullable int[] rangeBands, int rateOfFire, @Nullable Integer ammo, Category category,
          List<String> special) {
        this.name = name;
        this.code = code;
        this.model = model;
        this.arc = arc;
        this.accuracy = accuracy;
        this.damageMultiplier = damageMultiplier;
        this.baseRange = baseRange;
        this.rangeBands = (rangeBands == null) ? RangeBand.limitsFor(baseRange) : rangeBands.clone();
        this.rateOfFire = rateOfFire;
        this.maxAmmo = ammo;
        this.ammo = ammo;
        this.category = category;
        this.special = new ArrayList<>();
        special.forEach(tag -> this.special.add(tag.trim().toUpperCase(Locale.ROOT)));
    }

    /** @return a fresh, undamaged copy with full ammunition */
    public HGWeapon copy() {
        return new HGWeapon(name, code, model, arc, accuracy, damageMultiplier, baseRange, rangeBands, rateOfFire,
              maxAmmo, category, special);
    }

    public String name() {
        return name;
    }

    public String code() {
        return code;
    }

    public String model() {
        return model;
    }

    public FiringArc arc() {
        return arc;
    }

    public int accuracy() {
        return accuracy;
    }

    public int damageMultiplier() {
        return damageMultiplier;
    }

    public int baseRange() {
        return baseRange;
    }

    /** @return the outer limits of Short, Medium, Long and Extreme range */
    public int[] rangeBands() {
        return rangeBands.clone();
    }

    /** @return the Extreme range limit, the farthest this weapon can shoot */
    public int maxRange() {
        return rangeBands[3];
    }

    public int rateOfFire() {
        return rateOfFire;
    }

    public Category category() {
        return category;
    }

    /** @return the rounds left, or null when the weapon uses no ammunition */
    public @Nullable Integer ammo() {
        return ammo;
    }

    /** @return the special characteristics, upper case, such as "IF" */
    public List<String> special() {
        return List.copyOf(special);
    }

    public boolean hasSpecial(String tag) {
        return special.contains(tag.toUpperCase(Locale.ROOT));
    }

    /**
     * @return the blast radius of an Area Effect weapon ("AE0" is 0, the target hex only; "AE1" adds the six
     *       neighbours), or -1 when the weapon has no area effect
     */
    public int areaEffectRadius() {
        for (String tag : special) {
            if (tag.startsWith("AE")) {
                try {
                    return Integer.parseInt(tag.substring(2).replace("=", "").trim());
                } catch (NumberFormatException ignored) {
                    return 0;
                }
            }
        }
        return -1;
    }

    /**
     * @return true for weapons that shoot at range. Hand grenades, which are thrown, and melee weapons need the
     *       physical attack rules, which come in a later stage.
     */
    public boolean isRanged() {
        return (baseRange > 0) && (category != Category.MELEE) && !hasSpecial("THROWN") && !hasSpecial("MELEE")
              && !hasSpecial("PHYSICAL_ONLY");
    }

    /** @return the accumulated damage penalty, 0 or negative */
    public int damagePenalty() {
        return damagePenalty;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    /** @return true when the weapon cannot fire: destroyed, or damaged to -5 or worse */
    public boolean isOutOfAction() {
        return destroyed || (damagePenalty <= OUT_OF_ACTION_PENALTY);
    }

    /**
     * Adds a damage penalty. At -6 or worse the weapon is destroyed.
     *
     * @param penalty a negative number, such as -1
     */
    public void addDamagePenalty(int penalty) {
        damagePenalty += penalty;
        if (damagePenalty <= OUT_OF_ACTION_PENALTY - 1) {
            destroyed = true;
        }
    }

    /** Destroys the weapon outright. */
    public void destroy() {
        destroyed = true;
    }

    /**
     * @param rofUsed the Rate of Fire used; 0 is a single shot
     *
     * @return the rounds (or missiles) the attack uses
     */
    public int ammoNeeded(int rofUsed) {
        if (rofUsed <= 0) {
            return 1;
        } else if (category == Category.ROCKET) {
            return 1 << rofUsed;
        } else {
            return 10 * rofUsed;
        }
    }

    /** @return true when there is enough ammunition for an attack using this much Rate of Fire */
    public boolean hasAmmoFor(int rofUsed) {
        return (ammo == null) || (ammo >= ammoNeeded(rofUsed));
    }

    /** @return the most Rate of Fire the remaining ammunition allows, or -1 when the weapon is empty */
    public int maxUsableRateOfFire() {
        for (int rof = rateOfFire; rof >= 0; rof--) {
            if (hasAmmoFor(rof)) {
                return rof;
            }
        }
        return -1;
    }

    /** Takes the ammunition for one attack out of the magazine. */
    public void useAmmo(int rofUsed) {
        if (ammo != null) {
            ammo = Math.max(0, ammo - ammoNeeded(rofUsed));
        }
    }

    /** @return a short status, such as "LAC (ammo 40, -1)" */
    public String status() {
        StringBuilder result = new StringBuilder(code);
        List<String> notes = new ArrayList<>();
        if (ammo != null) {
            notes.add("ammo " + ammo);
        }
        if (destroyed) {
            notes.add("destroyed");
        } else if (damagePenalty != 0) {
            notes.add(damagePenalty + (isOutOfAction() ? ", out of action" : ""));
        }
        if (!notes.isEmpty()) {
            result.append(" (").append(String.join(", ", notes)).append(")");
        }
        return result.toString();
    }

    @Override
    public String toString() {
        return name + " (" + code + ")";
    }
}
