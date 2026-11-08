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

package megamek.client;

import java.util.ArrayList;
import java.util.List;

import megamek.common.board.Coords;
import megamek.common.event.GameReportEvent;
import megamek.common.game.InGameObject;
import megamek.common.heavyGear.HGActivation;
import megamek.common.heavyGear.HGGame;
import megamek.common.heavyGear.HGPlayerTurn;
import megamek.common.heavyGear.MovementSystem;
import megamek.common.heavyGear.SpeedState;
import megamek.common.net.enums.PacketCommand;
import megamek.common.net.packets.Packet;
import megamek.logging.MMLogger;

/**
 * A player's connection to a Heavy Gear game. It keeps a copy of the game up to date from the server's packets
 * and sends the player's requests (placing a unit, activating a unit) to the server.
 *
 * <p>MegaMek's {@link AbstractClient} does the general work: connecting, players, chat, phase changes. This class
 * only handles the packets that are special to Heavy Gear.</p>
 */
public class HGClient extends AbstractClient {

    private static final MMLogger LOGGER = MMLogger.create(HGClient.class);

    private final HGGame game = new HGGame();

    public HGClient(String name, String host, int port) {
        super(name, host, port);
    }

    @Override
    public HGGame getGame() {
        return game;
    }

    @Override
    public boolean isMyTurn() {
        HGPlayerTurn turn = game.getTurn();
        return (turn != null) && (turn.playerId() == localPlayerNumber);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected boolean handleGameSpecificPacket(Packet packet) throws Exception {
        switch (packet.command()) {
            case SENDING_ENTITIES -> game.setUnitList((List<InGameObject>) packet.getObject(0));
            case ENTITY_UPDATE -> game.receiveUnit((InGameObject) packet.getObject(0));
            case SENDING_TURNS -> game.setTurns((List<HGPlayerTurn>) packet.getObject(0));
            case TURN -> game.setTurnIndex(packet.getIntValue(0), packet.getIntValue(1));
            case SENDING_REPORTS -> {
                List<String> lines = (List<String>) packet.getObject(0);
                game.addToBattleLog(lines);
                possiblyWriteToLog(String.join("\n", lines) + "\n");
                game.fireGameEvent(new GameReportEvent(this, String.join("\n", lines)));
            }
            case SENDING_REPORTS_ALL -> {
                List<String> lines = (List<String>) packet.getObject(0);
                game.replaceBattleLog(lines);
                game.fireGameEvent(new GameReportEvent(this, ""));
            }
            case SENDING_GAME_SETTINGS, SENDING_PLANETARY_CONDITIONS, SENDING_MINEFIELDS -> {
                // Heavy Gear has no game options, weather or minefields yet
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    /**
     * Asks the server to place a unit at set-up.
     */
    public void deploy(int unitId, Coords coords, int facing, MovementSystem system, SpeedState speed) {
        send(new Packet(PacketCommand.ENTITY_DEPLOY, unitId, coords, facing, system, speed));
    }

    /**
     * Sends a unit's whole activation plan to the server.
     */
    public void activate(HGActivation activation) {
        LOGGER.info("Sending activation {}", activation);
        send(new Packet(PacketCommand.ENTITY_MOVE, new HGActivation(activation.unitId(),
              new ArrayList<>(activation.steps()), activation.changeSpeed())));
    }
}
