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

import jakarta.annotation.Nullable;

/**
 * One step of a unit's activation: move a hex, turn a hexside, change movement system or fire a weapon.
 *
 * @param type        what the step does
 * @param weaponIndex for {@link Type#FIRE}: which weapon, by its position in the unit's weapon list
 * @param targetId    for {@link Type#FIRE}: the id of the unit shot at
 * @param rateOfFire  for {@link Type#FIRE}: how much Rate of Fire to use; 0 is a single shot, more is a burst
 * @param system      for {@link Type#SWITCH_SYSTEM}: the movement system to change to
 */
public record HGStep(Type type, int weaponIndex, int targetId, int rateOfFire, @Nullable MovementSystem system)
      implements Serializable {

    /** The kinds of step. */
    public enum Type {
        /** Move one hex straight ahead. */
        FORWARD,
        /** Move one hex straight back, keeping the facing (only at Half Combat Speed or slower). */
        BACKWARD,
        /** Turn one hexside to the left (counter-clockwise). */
        TURN_LEFT,
        /** Turn one hexside to the right (clockwise). */
        TURN_RIGHT,
        /** Change to the other movement system. */
        SWITCH_SYSTEM,
        /** Fire one weapon once. */
        FIRE
    }

    public static HGStep forward() {
        return new HGStep(Type.FORWARD, -1, -1, 0, null);
    }

    public static HGStep backward() {
        return new HGStep(Type.BACKWARD, -1, -1, 0, null);
    }

    public static HGStep turnLeft() {
        return new HGStep(Type.TURN_LEFT, -1, -1, 0, null);
    }

    public static HGStep turnRight() {
        return new HGStep(Type.TURN_RIGHT, -1, -1, 0, null);
    }

    public static HGStep switchTo(MovementSystem system) {
        return new HGStep(Type.SWITCH_SYSTEM, -1, -1, 0, system);
    }

    public static HGStep fire(int weaponIndex, int targetId, int rateOfFire) {
        return new HGStep(Type.FIRE, weaponIndex, targetId, rateOfFire, null);
    }

    /** @return true for steps that enter a new hex */
    public boolean isMove() {
        return (type == Type.FORWARD) || (type == Type.BACKWARD);
    }

    /** @return true for turning steps */
    public boolean isTurn() {
        return (type == Type.TURN_LEFT) || (type == Type.TURN_RIGHT);
    }
}
