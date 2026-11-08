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

import megamek.common.game.AbstractPlayerTurn;
import megamek.common.game.IGame;
import megamek.common.game.InGameObject;

/**
 * One turn in the turn order: the named player picks one of their units that hasn't acted yet and places it
 * (set-up) or activates it (each round). MegaMek's server keeps a list of these and hands them out one by one.
 */
public class HGPlayerTurn extends AbstractPlayerTurn {

    public HGPlayerTurn(int playerId) {
        super(playerId);
    }

    @Override
    public boolean isValidEntity(InGameObject unit, IGame game) {
        if (!(unit instanceof HGUnit hgUnit) || (unit.getOwnerId() != playerId())) {
            return false;
        } else if (game.getPhase().isDeployment()) {
            return !hgUnit.isDeployed();
        } else if (game.getPhase().isMovement()) {
            return hgUnit.isDeployed() && !hgUnit.hasActivated() && !hgUnit.isOutOfFight();
        } else {
            return false;
        }
    }

    /** @return true when the player has at least one unit that can take this turn */
    public boolean hasEligibleUnit(IGame game) {
        return game.getInGameObjects().stream().anyMatch(unit -> isValidEntity(unit, game));
    }
}
