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
 * The speed a unit announces for its activation, and the modifier it gives to the unit's own attacks
 * (notes 01 section 5.2; notes 02 section 3, row 4; B p.138).
 *
 * <p>Announcing a speed caps the MP the unit may spend this activation. HeavyMek works the announcement out from
 * the MP actually spent, picking the slowest speed that covers them, because announcing a faster speed than needed
 * would only cost attack bonus.</p>
 */
public enum MovementBand {
    /** Combat Speed with 0 MP spent. */
    STATIONARY("Stationary", 2),
    /** Combat Speed spending no more than half the Combat MP. Reversing is only allowed in this band. */
    HALF_COMBAT("Half Combat Speed", 1),
    /** Combat Speed, up to all the Combat MP. */
    COMBAT("Combat Speed", 0),
    /** Top Speed: more than the Combat MP, up to the Top MP. */
    TOP("Top Speed", -3);

    private final String displayName;
    private final int attackModifier;

    MovementBand(String displayName, int attackModifier) {
        this.displayName = displayName;
        this.attackModifier = attackModifier;
    }

    /**
     * Works out the band from the MP spent.
     *
     * @param atTopSpeed true when the unit is at Top Speed this round
     * @param mpSpent    the MP spent this activation
     * @param combatMP   the unit's Combat MP when it started the activation
     */
    public static MovementBand of(boolean atTopSpeed, int mpSpent, int combatMP) {
        if (atTopSpeed) {
            return TOP;
        } else if (mpSpent <= 0) {
            return STATIONARY;
        } else if (isHalfCombat(mpSpent, combatMP)) {
            return HALF_COMBAT;
        } else {
            return COMBAT;
        }
    }

    /**
     * "Half Combat Speed or less" compares MP spent x 2 with the Combat MP, so odd Combat MP need no rounding
     * (rules README, "Odd Combat Speed and half speed").
     */
    public static boolean isHalfCombat(int mpSpent, int combatMP) {
        return mpSpent * 2 <= combatMP;
    }

    /** @return the modifier for the unit's own attacks */
    public int attackModifier() {
        return attackModifier;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
