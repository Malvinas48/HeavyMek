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

/**
 * One crew skill, written "dice/bonus" on the record sheets: "2/+1" means roll two dice and add 1 to the result
 * (notes 01 section 1.5).
 *
 * <p>A {@code record} is Java's short way to write a small class that only holds values: Java writes the
 * constructor, the getters ({@code dice()}, {@code bonus()}), {@code equals} and {@code hashCode} for us.</p>
 *
 * @param dice  the number of six-sided dice to roll (the skill level)
 * @param bonus the attribute bonus added to the roll
 */
public record Skill(int dice, int bonus) implements Serializable {

    @Override
    public String toString() {
        return dice + "/" + ((bonus > 0) ? "+" : "") + bonus;
    }
}
