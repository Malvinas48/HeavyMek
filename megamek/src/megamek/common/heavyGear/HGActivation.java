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

/**
 * A player's whole plan for one unit's activation, sent to the server in one go: the moves and turns in order,
 * with at most one shot somewhere along the way, and whether to change speed afterwards (notes 01 section 3.6).
 *
 * <p>The server replays the plan step by step and rolls the dice; the player's screen only shows a preview.</p>
 *
 * @param unitId      the unit activating
 * @param steps       the steps in order
 * @param changeSpeed true to change speed after moving: a unit at Combat Speed that spent exactly its Combat MP
 *                    shifts up to Top Speed, a unit at Top Speed shifts back down to Combat Speed. Either change
 *                    applies from the next round.
 */
public record HGActivation(int unitId, List<HGStep> steps, boolean changeSpeed) implements Serializable {

    /** Copies the steps into an ArrayList, which can travel over the network. */
    public HGActivation {
        steps = new ArrayList<>(steps);
    }
}
