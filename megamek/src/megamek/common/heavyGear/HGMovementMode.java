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
 * One movement system of one unit with its current speeds, for example a Hunter's Ground system at Combat 6 and
 * Top 12. Damage lowers the speeds here.
 *
 * <p>Speed loss comes off Top Speed first and Combat Speed is then worked out again as half the Top Speed,
 * rounded up (notes 02 section 13.4). That rule fits every printed Gear: 7 gives 4 and 12 gives 6.</p>
 */
public class HGMovementMode implements Serializable {

    private final MovementSystem system;
    private int combat;
    private int top;

    /**
     * @param system the movement system
     * @param combat its Combat MP
     * @param top    its Top MP
     */
    public HGMovementMode(MovementSystem system, int combat, int top) {
        this.system = system;
        this.combat = combat;
        this.top = top;
    }

    /** @return a separate copy, so one unit file can be placed on the map several times */
    public HGMovementMode copy() {
        return new HGMovementMode(system, combat, top);
    }

    public MovementSystem system() {
        return system;
    }

    public int combat() {
        return combat;
    }

    public int top() {
        return top;
    }

    /** @return true once damage has brought this system's Top Speed to 0 */
    public boolean isKnockedOut() {
        return top <= 0;
    }

    /** Takes MP off Top Speed and recalculates Combat Speed. */
    public void loseMP(int amount) {
        setTop(top - amount);
    }

    /** Halves Top Speed, rounding down, and recalculates Combat Speed (a Heavy movement hit). */
    public void halveMP() {
        setTop(top / 2);
    }

    private void setTop(int newTop) {
        top = Math.max(0, newTop);
        combat = (top + 1) / 2;
    }

    @Override
    public String toString() {
        return system + " " + combat + "/" + top;
    }
}
