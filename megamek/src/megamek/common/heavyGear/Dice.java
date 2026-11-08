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

import megamek.common.compute.Compute;

/**
 * Where die rolls come from. The real game uses MegaMek's random number generator ({@link #RANDOM}); the unit
 * tests hand in a fixed list of numbers instead, so every roll in a test is known in advance.
 *
 * <p>Think of it as the dice cup: the rules code asks the cup for a die and never cares how the cup works.</p>
 */
@FunctionalInterface
public interface Dice {

    /** @return one six-sided die, 1 to 6 */
    int d6();

    /** The dice cup the game uses: MegaMek's shared random generator. */
    Dice RANDOM = Compute::d6;
}
