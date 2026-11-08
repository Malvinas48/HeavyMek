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

/**
 * What a hit does, found by comparing the damage with the target's current Base Armor (notes 02 section 11.2;
 * B p.145):
 * <ul>
 *     <li>below the armor: no effect, it bounces off;</li>
 *     <li>at least the armor: Light damage (armor -1, roll on the Light systems damage table);</li>
 *     <li>at least twice the armor: Heavy damage (armor -2, roll on the Heavy table);</li>
 *     <li>at least three times the armor: Overkill, the unit is destroyed.</li>
 * </ul>
 * With armor 15 the thresholds are 15 / 30 / 45, as printed on the record sheets.
 */
public enum DamageResult {
    NONE("no effect"),
    LIGHT("Light damage"),
    HEAVY("Heavy damage"),
    OVERKILL("Overkill");

    private final String displayName;

    DamageResult(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @param damage the total damage (Margin of Success x Damage Multiplier)
     * @param armor  the target's current Base Armor
     */
    public static DamageResult of(int damage, int armor) {
        if (armor <= 0) {
            return (damage > 0) ? OVERKILL : NONE;
        } else if (damage >= 3 * armor) {
            return OVERKILL;
        } else if (damage >= 2 * armor) {
            return HEAVY;
        } else if (damage >= armor) {
            return LIGHT;
        } else {
            return NONE;
        }
    }

    @Override
    public String toString() {
        return displayName;
    }
}
