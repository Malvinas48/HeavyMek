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

package megamek.client.ui.heavyGear;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import megamek.client.ui.clientGUI.boardview.BoardView;
import megamek.client.ui.clientGUI.boardview.spriteHandler.BoardViewSpriteHandler;
import megamek.common.Player;
import megamek.common.board.Coords;
import megamek.common.heavyGear.ActivationState;
import megamek.common.heavyGear.HGGame;
import megamek.common.heavyGear.HGStep;
import megamek.common.heavyGear.HGUnit;

/**
 * Keeps the pictures on the map up to date: a token for every unit, the deployment zone during set-up, and the
 * planned move (shaded hexes numbered with the MP spent, plus a see-through token where the unit will end up).
 * The whole set is simply rebuilt whenever something changes; a Heavy Gear battle has few enough pieces for that.
 */
class HGBoardSprites extends BoardViewSpriteHandler {

    private static final Color ZONE = new Color(80, 200, 80, 70);
    private static final Color PATH = new Color(255, 220, 60, 110);

    private final HGClientGUI gui;
    private final HGPlanner planner;

    HGBoardSprites(HGClientGUI gui, HGPlanner planner) {
        super(gui);
        this.gui = gui;
        this.planner = planner;
    }

    @Override
    public void initialize() {
    }

    @Override
    public void dispose() {
        clear();
    }

    /** Rebuilds every sprite. Call on the Swing thread. */
    void update() {
        clear();
        BoardView boardView = gui.boardView();
        if (boardView == null) {
            return;
        }
        HGGame game = planner.game();
        HGUnit selected = planner.unit();
        boolean myTurn = planner.isEligible(selected);

        if (game.getPhase().isDeployment() && myTurn && (selected != null) && !selected.isDeployed()) {
            addDeploymentZone(boardView, game);
        }
        if (game.getPhase().isMovement() && myTurn && (selected != null)) {
            addPlannedPath(boardView, selected);
        }
        addTokens(boardView, game, selected);
        boardView.addSprites(currentSprites);
    }

    private void addDeploymentZone(BoardView boardView, HGGame game) {
        for (int x = 0; x < game.board().getWidth(); x++) {
            for (int y = 0; y < game.board().getHeight(); y++) {
                Coords coords = new Coords(x, y);
                if (planner.checkDeployHex(coords) == null) {
                    currentSprites.add(new HGHexShadeSprite(boardView, coords, ZONE, null));
                }
            }
        }
    }

    private void addPlannedPath(BoardView boardView, HGUnit unit) {
        ActivationState replay = new ActivationState(planner.game(), unit);
        for (HGStep step : planner.steps()) {
            replay.tryApply(step);
            if (step.isMove()) {
                currentSprites.add(new HGHexShadeSprite(boardView, replay.position(), PATH,
                      replay.mpSpent() + " MP"));
            }
        }
    }

    private void addTokens(BoardView boardView, HGGame game, HGUnit selected) {
        Map<Coords, List<HGUnit>> byHex = new LinkedHashMap<>();
        for (HGUnit unit : game.units()) {
            if (unit.isDeployed()) {
                byHex.computeIfAbsent(unit.position(), coords -> new ArrayList<>()).add(unit);
            }
        }
        HGUnit target = planner.target();
        for (Map.Entry<Coords, List<HGUnit>> hex : byHex.entrySet()) {
            List<HGUnit> units = hex.getValue();
            for (int i = 0; i < units.size(); i++) {
                HGUnit unit = units.get(i);
                Player owner = owner(game, unit);
                HGUnitSprite sprite = new HGUnitSprite(boardView, unit, owner, hex.getKey(), unit.facing(), i,
                      units.size(), false);
                if ((selected != null) && (unit.getId() == selected.getId())) {
                    sprite.setHighlight(HGUnitSprite.Highlight.SELECTED);
                } else if ((target != null) && (unit.getId() == target.getId())) {
                    sprite.setHighlight(HGUnitSprite.Highlight.TARGET);
                }
                currentSprites.add(sprite);
            }
        }
        addGhost(boardView, game, selected);
    }

    /** The see-through token where the planned move ends, or where a unit is about to be placed. */
    private void addGhost(BoardView boardView, HGGame game, HGUnit selected) {
        if ((selected == null) || !planner.isEligible(selected)) {
            return;
        }
        if (game.getPhase().isDeployment() && (planner.deployCoords() != null)) {
            // show the unit as it will be placed, with the system and speed chosen for it
            currentSprites.add(new HGUnitSprite(boardView, selected, owner(game, selected), planner.deployCoords(),
                  planner.deployFacing(), 0, 1, true)
                  .showing(planner.deploySystem(), planner.deploySpeed().isTop()));
        } else if (game.getPhase().isMovement()) {
            ActivationState state = planner.state();
            if ((state != null) && (!state.position().equals(selected.position())
                  || (state.facing() != selected.facing()))) {
                boolean topNextRound = planner.isChangeSpeed() != state.isAtTopSpeed();
                currentSprites.add(new HGUnitSprite(boardView, selected, owner(game, selected), state.position(),
                      state.facing(), 0, 1, true).showing(state.system(), topNextRound));
            }
        }
    }

    private static Player owner(HGGame game, HGUnit unit) {
        Player owner = game.getPlayer(unit.getOwnerId());
        return (owner != null) ? owner : new Player(unit.getOwnerId(), "?");
    }
}
