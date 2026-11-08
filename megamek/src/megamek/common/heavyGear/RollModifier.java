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
import java.util.List;

/**
 * One line of an itemised list of roll modifiers, such as "Medium range -1". The attack and defense rolls each keep
 * such a list so the battle log can show exactly where every point came from (notes 02 section 3, implementation
 * note).
 *
 * @param label what the modifier is for, in plain words
 * @param value the amount added to the roll; negative values are penalties
 */
public record RollModifier(String label, int value) implements Serializable {

    /** @return the value with its sign, such as "+2" or "-1" */
    public String signedValue() {
        return ((value >= 0) ? "+" : "") + value;
    }

    /** @return the sum of all the given modifiers */
    public static int sum(List<RollModifier> modifiers) {
        return modifiers.stream().mapToInt(RollModifier::value).sum();
    }

    @Override
    public String toString() {
        return label + " " + signedValue();
    }
}
