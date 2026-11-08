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

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

import megamek.common.Player;
import megamek.common.board.Board;
import megamek.common.board.Coords;

/**
 * Builds small games for the tests: an open 16 x 17 map, two players on opposing sides (North is player 0, South
 * player 1) and Gears loaded from the real unit files.
 */
final class HGTestGames {

    static final int NORTH = 0;
    static final int SOUTH = 1;

    /**
     * The unit files, found by path. Some of MegaMek's own tests point the data folder elsewhere and don't put it
     * back, so the tests don't rely on it.
     */
    static final File UNITS = new File("data/heavygear/units");

    private HGTestGames() {
    }

    static HGGame twoPlayerGame() {
        HGGame game = new HGGame();
        game.setBoard(0, new Board(16, 17));
        Player north = new Player(NORTH, "North");
        north.setTeam(1);
        Player south = new Player(SOUTH, "South");
        south.setTeam(2);
        game.addPlayer(NORTH, north);
        game.addPlayer(SOUTH, south);
        return game;
    }

    static HGUnit gear(String unitFile) {
        try {
            return new HGUnit(HGUnitLoader.load(unitFile, UNITS));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Loads a Gear, gives it to a player and places it at Combat Speed on its primary (walker) system. */
    static HGUnit place(HGGame game, String unitFile, int owner, int x, int y, int facing) {
        HGUnit unit = gear(unitFile);
        unit.setOwnerId(owner);
        unit.setCallsign(unitFile + owner + game.units().size());
        unit.deploy(new Coords(x, y), facing, unit.datacard().primarySystem(), SpeedState.COMBAT);
        game.addUnit(unit);
        return unit;
    }
}
