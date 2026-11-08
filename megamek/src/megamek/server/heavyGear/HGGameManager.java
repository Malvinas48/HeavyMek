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

package megamek.server.heavyGear;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import megamek.common.Player;
import megamek.common.board.Coords;
import megamek.common.enums.GamePhase;
import megamek.common.game.IGame;
import megamek.common.heavyGear.ActivationState;
import megamek.common.heavyGear.AttackResolver;
import megamek.common.heavyGear.Dice;
import megamek.common.heavyGear.HGActivation;
import megamek.common.heavyGear.HGGame;
import megamek.common.heavyGear.HGPlayerTurn;
import megamek.common.heavyGear.HGStep;
import megamek.common.heavyGear.HGUnit;
import megamek.common.heavyGear.HGWeapon;
import megamek.common.heavyGear.MovementBand;
import megamek.common.heavyGear.MovementSystem;
import megamek.common.heavyGear.RollModifier;
import megamek.common.heavyGear.SkillRoll;
import megamek.common.heavyGear.SpeedState;
import megamek.common.heavyGear.SystemsDamage;
import megamek.common.interfaces.ReportEntry;
import megamek.common.net.enums.PacketCommand;
import megamek.common.net.packets.Packet;
import megamek.logging.MMLogger;
import megamek.server.AbstractGameManager;
import megamek.server.Server;
import megamek.server.commands.ServerCommand;

/**
 * The referee of a Heavy Gear game. It runs on the server, owns the real game, rolls every die and tells the
 * players' computers what happened. The players' computers only send requests ("place this Gear here", "here is my
 * activation") and show the results.
 *
 * <p>A game goes through MegaMek's phases like this:</p>
 * <ol>
 *     <li>{@code STARTING_SCENARIO}: waits until a player has joined for every side of the scenario.</li>
 *     <li>{@code DEPLOYMENT}: set-up, only when some units have no position in the scenario file. The sides take
 *     turns placing one unit at a time.</li>
 *     <li>{@code INITIATIVE}: a new round; the initiative roll decides the activation order.</li>
 *     <li>{@code MOVEMENT}: the activations. Heavy Gear has no separate firing phase: each unit moves and fires in
 *     its own activation.</li>
 *     <li>{@code END}: the end of the round, then back to {@code INITIATIVE}.</li>
 *     <li>{@code VICTORY}: as soon as only one side has units left in the fight.</li>
 * </ol>
 */
public class HGGameManager extends AbstractGameManager {

    private static final MMLogger LOGGER = MMLogger.create(HGGameManager.class);

    private HGGame game = new HGGame();
    private Dice dice = Dice.RANDOM;

    // region Plumbing required by MegaMek's server

    @Override
    public HGGame getGame() {
        return game;
    }

    @Override
    public void setGame(IGame newGame) {
        if (newGame instanceof HGGame hgGame) {
            game = hgGame;
        } else {
            LOGGER.error("A Heavy Gear server was given a game of another kind: {}", newGame);
        }
    }

    /** Replaces the dice; the tests use this to make every roll known in advance. */
    public void setDice(Dice dice) {
        this.dice = dice;
    }

    @Override
    public void resetGame() {
    }

    /**
     * A player who leaves during the battle becomes a "ghost": their units stay and the game waits for them. When
     * they connect again with the same name they take over where they left off.
     */
    @Override
    public void disconnect(Player player) {
        if (game.getPhase().isVictory() || player.isObserver()) {
            game.removePlayer(player.getId());
            send(new Packet(PacketCommand.PLAYER_REMOVE, player.getId()));
        } else {
            player.setGhost(true);
            player.setDone(true);
            transmitPlayerUpdate(player);
            sendLog(List.of(player.getName() + " has left the game. The game waits until they connect again with "
                  + "the same name."));
        }
    }

    @Override
    public void removeAllEntitiesOwnedBy(Player player) {
    }

    @Override
    public void handleCfrPacket(Server.ReceivedPacket rp) {
    }

    @Override
    public void requestGameMaster(Player player) {
    }

    @Override
    public List<ServerCommand> getCommandList(Server server) {
        return Collections.emptyList();
    }

    @Override
    public void addReport(ReportEntry report) {
        sendLog(List.of(report.text()));
    }

    @Override
    public void calculatePlayerInitialCounts() {
    }

    @Override
    public void requestTeamChangeForPlayer(int teamID, Player player) {
    }

    // endregion

    // region Packets from the players

    @Override
    public void handlePacket(int connId, Packet packet) {
        super.handlePacket(connId, packet);
        switch (packet.command()) {
            case ENTITY_DEPLOY -> receiveDeployment(connId, packet);
            case ENTITY_MOVE -> receiveActivation(connId, packet);
            default -> {
            }
        }
    }

    /**
     * Sends a player who has just connected (or reconnected) everything needed to show the game as it is now.
     */
    @Override
    public void sendCurrentInfo(int connId) {
        send(connId, packetHelper.createGameSettingsPacket());
        send(connId, packetHelper.createBoardsPacket());
        send(connId, packetHelper.createCurrentRoundNumberPacket());
        send(connId, createUnitsPacket());
        send(connId, new Packet(PacketCommand.SENDING_REPORTS_ALL, new ArrayList<>(game.battleLog())));
        send(connId, packetHelper.createPhaseChangePacket());
        if (game.getPhase().usesTurns() && (game.getTurn() != null)) {
            send(connId, packetHelper.createTurnListPacket());
            send(connId, packetHelper.createTurnIndexPacket(Player.PLAYER_NONE));
        }
    }

    /** A scenario game starts only once a player has joined for every side. */
    @Override
    protected void checkReady() {
        if (game.getPhase().isStartingScenario() && game.getPlayersList().stream().anyMatch(Player::isGhost)) {
            return;
        }
        super.checkReady();
    }

    // endregion

    // region Phases

    @Override
    protected void prepareForCurrentPhase() {
        switch (game.getPhase()) {
            case DEPLOYMENT -> {
                resetPlayersDone();
                List<String> log = new ArrayList<>();
                game.setTurns(new HGInitiative(game, dice, log).setupTurns());
                game.resetTurnIndex();
                sendLog(log);
                sendCurrentTurns();
            }
            case INITIATIVE -> startRound();
            case MOVEMENT -> {
                resetPlayersDone();
                sendCurrentTurns();
            }
            case END -> sendLog(List.of("End of round " + game.getCurrentRound() + "."));
            case VICTORY -> announceResult();
            default -> {
            }
        }
    }

    @Override
    protected void executeCurrentPhase() {
        if (game.getPhase().isDeployment() || game.getPhase().isMovement()) {
            changeToNextTurn(Player.PLAYER_NONE);
        }
    }

    @Override
    protected void endCurrentPhase() {
        switch (game.getPhase()) {
            case LOUNGE, EXCHANGE, STARTING_SCENARIO -> {
                boolean needsSetup = game.units().stream().anyMatch(unit -> !unit.isDeployed());
                changePhase(needsSetup ? GamePhase.DEPLOYMENT : GamePhase.INITIATIVE);
            }
            case DEPLOYMENT -> changePhase(GamePhase.INITIATIVE);
            case INITIATIVE -> changePhase(GamePhase.MOVEMENT);
            case MOVEMENT -> changePhase(GamePhase.END);
            case END -> changePhase(isGameOver() ? GamePhase.VICTORY : GamePhase.INITIATIVE);
            default -> {
            }
        }
    }

    private void startRound() {
        // MegaMek counts rounds from -1 before the game starts; Heavy Gear's first round is round 1
        game.setCurrentRound(Math.max(0, game.getCurrentRound()));
        incrementAndSendGameRound();
        game.units().forEach(HGUnit::startNewRound);
        List<String> log = new ArrayList<>();
        log.add("=== Round " + game.getCurrentRound() + " ===");
        log.add("Initiative:");
        game.setTurns(new HGInitiative(game, dice, log).roundTurns());
        game.resetTurnIndex();
        sendUnits();
        sendLog(log);
    }

    private void announceResult() {
        List<Player> sidesLeft = game.getPlayersList().stream()
              .filter(player -> !game.fightingUnitsOf(player.getId()).isEmpty())
              .toList();
        if (sidesLeft.size() == 1) {
            sendLog(List.of("*** " + sidesLeft.get(0).getName() + " wins the battle! ***"));
        } else {
            sendLog(List.of("*** The battle is over: no side has a unit left in the fight. ***"));
        }
    }

    /** @return true when at most one side has units left in the fight */
    boolean isGameOver() {
        long sidesLeft = game.getPlayersList().stream()
              .filter(player -> !game.fightingUnitsOf(player.getId()).isEmpty())
              .count();
        return sidesLeft <= 1;
    }

    private void changeToNextTurn(int previousPlayerId) {
        while (true) {
            if (!game.hasMoreTurns()) {
                endCurrentPhase();
                return;
            }
            HGPlayerTurn nextTurn = game.changeToNextTurn();
            if ((nextTurn != null) && nextTurn.hasEligibleUnit(game)) {
                break;
            }
        }
        send(packetHelper.createTurnIndexPacket(previousPlayerId));
        Player player = game.getPlayer(game.getTurn().playerId());
        if ((player != null) && player.isGhost()) {
            sendGhostSkipMessage(player);
        }
    }

    private void endCurrentTurn(int playerId) {
        if (isGameOver()) {
            changePhase(GamePhase.VICTORY);
        } else {
            changeToNextTurn(playerId);
        }
    }

    private void resetPlayersDone() {
        game.getPlayersList().forEach(player -> player.setDone(false));
        transmitAllPlayerDones();
    }

    // endregion

    // region Set-up

    private void receiveDeployment(int connId, Packet packet) {
        HGUnit unit = (packet.getObject(0) instanceof Integer id) ? game.unit(id) : null;
        Coords coords = (packet.getObject(1) instanceof Coords c) ? c : null;
        int facing = (packet.getObject(2) instanceof Integer f) ? f : 0;
        MovementSystem system = (packet.getObject(3) instanceof MovementSystem s) ? s : null;
        SpeedState speed = (packet.getObject(4) instanceof SpeedState s) ? s : SpeedState.COMBAT;
        HGPlayerTurn turn = game.getTurn();
        Player player = game.getPlayer(connId);
        String problem = null;
        if (!game.getPhase().isDeployment() || (turn == null) || (unit == null) || (player == null)
              || !turn.isValid(connId, unit, game)) {
            problem = "It isn't your turn to place that unit.";
        } else if ((coords == null) || !game.board().contains(coords)) {
            problem = "That hex isn't on the map.";
        } else if (!game.board().isLegalDeployment(coords, player)) {
            problem = "That hex is outside your deployment zone.";
        } else if (game.sizeInHex(coords, unit.getId()) + unit.datacard().size() > HGGame.STACKING_LIMIT) {
            problem = "That hex is full.";
        } else if ((system == null) || (unit.movementMode(system) == null)) {
            problem = "That unit has no such movement system.";
        }
        if (problem != null) {
            refuse(connId, problem);
            return;
        }
        unit.deploy(coords, facing, system, speed);
        sendUnits();
        sendLog(List.of(player.getName() + " places " + unit.displayName() + " in hex " + coords.getBoardNum()
              + ", facing " + facingName(facing) + ", on its " + system + " system at " + speed + "."));
        endCurrentTurn(connId);
    }

    // endregion

    // region Activations

    private void receiveActivation(int connId, Packet packet) {
        if (!(packet.getObject(0) instanceof HGActivation activation)) {
            LOGGER.error("Activation packet without an activation: {}", packet);
            return;
        }
        HGUnit unit = game.unit(activation.unitId());
        HGPlayerTurn turn = game.getTurn();
        if (!game.getPhase().isMovement() || (turn == null) || (unit == null) || !turn.isValid(connId, unit, game)) {
            refuse(connId, "It isn't your turn to activate that unit.");
            return;
        }
        String problem = checkPlan(unit, activation);
        if (problem != null) {
            refuse(connId, "Your plan for " + unit.shortName() + " doesn't follow the rules: " + problem);
            return;
        }
        List<String> log = new ArrayList<>();
        carryOut(unit, activation, log);
        sendUnits();
        sendLog(log);
        endCurrentTurn(connId);
    }

    /**
     * Replays the plan without rolling any dice to make sure every step is allowed.
     *
     * @return the first problem found, or null when the plan is legal
     */
    private String checkPlan(HGUnit unit, HGActivation activation) {
        ActivationState dryRun = new ActivationState(game, unit);
        for (HGStep step : activation.steps()) {
            String problem = dryRun.tryApply(step);
            if (problem != null) {
                return problem;
            }
        }
        if (activation.changeSpeed() && !dryRun.canChangeSpeed()) {
            return "it can only shift up to Top Speed after spending exactly all " + dryRun.startCombatMP()
                  + " Combat MP.";
        }
        return null;
    }

    /**
     * Carries out a checked plan, rolling the dice for sharp turns and attacks.
     */
    void carryOut(HGUnit unit, HGActivation activation, List<String> log) {
        log.add(unit.displayName() + " activates.");
        // the announced speed covers the whole activation, so it comes from the whole plan
        ActivationState plan = new ActivationState(game, unit);
        activation.steps().forEach(plan::tryApply);
        MovementBand band = plan.band();
        boolean shiftUp = activation.changeSpeed() && !plan.isAtTopSpeed();
        int lastMovementStep = -1;
        for (int i = 0; i < activation.steps().size(); i++) {
            if (activation.steps().get(i).type() != HGStep.Type.FIRE) {
                lastMovementStep = i;
            }
        }

        MoveLogger moves = new MoveLogger(unit, log);
        int actions = unit.beginActivationActions();
        if (actions < unit.freeActions()) {
            log.add("  Its crew is still stunned: " + (actions == 0 ? "no action" : "one action fewer")
                  + " this round.");
        }
        ActivationState state = new ActivationState(game, unit, actions);
        for (int i = 0; i < activation.steps().size(); i++) {
            HGStep step = activation.steps().get(i);
            if ((step.isMove() || (step.type() == HGStep.Type.FIRE)) && state.sharpTurnPending()) {
                sharpTurnTest(unit, state, log);
            }
            if (unit.isOutOfFight()) {
                break;
            }
            String problem = state.check(step);
            if (problem != null) {
                if (step.type() == HGStep.Type.FIRE) {
                    log.add("  The shot can't be taken: " + problem);
                } else {
                    log.add("  " + unit.shortName() + " stops moving: " + problem);
                    state.endMovement();
                }
                continue;
            }
            state.tryApply(step);
            unit.setPosition(state.position(), state.facing());
            if (step.type() == HGStep.Type.FIRE) {
                moves.logMovementSoFar(state);
                HGWeapon weapon = unit.weapon(step.weaponIndex());
                HGUnit target = game.unit(step.targetId());
                MovementBand attackBand = (shiftUp && (i > lastMovementStep)) ? MovementBand.TOP : band;
                new AttackResolver(game, dice, log).resolve(unit, state.position(), weapon, step.rateOfFire(),
                      target, attackBand);
            }
        }
        if (state.sharpTurnPending() && !unit.isOutOfFight()) {
            sharpTurnTest(unit, state, log);
        }

        unit.setPosition(state.position(), state.facing());
        unit.setMovementSystem(state.system());
        unit.finishActivation(state.hexesMoved());
        moves.logMovementSoFar(state);
        log.add("  " + unit.shortName() + " spent " + state.mpSpent() + " of " + state.mpPool() + " MP on "
              + state.system() + ": " + band + " (" + new RollModifier("", band.attackModifier()).signedValue()
              + " to its attacks), " + state.hexesMoved() + ((state.hexesMoved() == 1) ? " hex" : " hexes")
              + " moved.");
        if (activation.changeSpeed() && !unit.isOutOfFight()) {
            if (state.isAtTopSpeed()) {
                unit.setSpeedState(SpeedState.COMBAT);
                log.add("  " + unit.shortName() + " slows down to Combat Speed from next round.");
            } else if (state.canChangeSpeed()) {
                unit.setSpeedState(SpeedState.TOP);
                log.add("  " + unit.shortName() + " shifts up to Top Speed from next round.");
            }
        }
    }

    /**
     * The Piloting test for a turn of three hexsides at Top Speed (notes 01 section 5.7; B p.133): Piloting plus
     * Maneuver against 3 + the MP cost of the hex. A draw passes. On a failure the unit skids one hex straight
     * ahead first; on a fumble it crashes, stops and takes Light damage (1-3 on a die) or Heavy damage (4-6).
     */
    private void sharpTurnTest(HGUnit unit, ActivationState state, List<String> log) {
        int threshold = state.sharpTurnThreshold();
        List<RollModifier> modifiers = (unit.maneuver() != 0)
              ? List.of(new RollModifier("Maneuver", unit.maneuver()))
              : List.of();
        SkillRoll roll = SkillRoll.roll("Piloting", unit.crewLevel().piloting(), modifiers, dice);
        log.add("  Sharp turn at Top Speed, needs " + threshold + ": " + roll.describe());
        if (roll.fumble()) {
            state.resolveSharpTurn(null);
            state.endMovement();
            int die = dice.d6();
            boolean heavy = die >= 4;
            log.add("  " + unit.shortName() + " crashes (" + die + "): its movement ends and it takes "
                  + (heavy ? "Heavy" : "Light") + " damage.");
            new SystemsDamage(dice, log).applyCrash(unit, heavy);
        } else if (roll.total() >= threshold) {
            state.resolveSharpTurn(null);
            log.add("  The turn succeeds.");
        } else {
            Coords skid = state.skidDestination();
            state.resolveSharpTurn(skid);
            log.add("  The turn fails: " + ((skid != null)
                  ? unit.shortName() + " skids into hex " + skid.getBoardNum() + " before completing it."
                  : "there's no room to skid, so " + unit.shortName() + " completes it in place."));
        }
        unit.setPosition(state.position(), state.facing());
    }

    /**
     * Writes the movement into the battle log in pieces, so a shot fired halfway along the path appears between the
     * part of the move before it and the part after it.
     */
    private static final class MoveLogger {
        private final HGUnit unit;
        private final List<String> log;
        private Coords from;
        private int facing;
        private int hexesLogged;

        MoveLogger(HGUnit unit, List<String> log) {
            this.unit = unit;
            this.log = log;
            from = unit.position();
            facing = unit.facing();
        }

        void logMovementSoFar(ActivationState state) {
            int hexes = state.hexesMoved() - hexesLogged;
            if (hexes > 0) {
                log.add("  " + unit.shortName() + " moves " + hexes + ((hexes == 1) ? " hex" : " hexes") + " from "
                      + from.getBoardNum() + " to " + state.position().getBoardNum() + ", facing "
                      + facingName(state.facing()) + ".");
            } else if (state.facing() != facing) {
                log.add("  " + unit.shortName() + " turns to face " + facingName(state.facing()) + ".");
            }
            from = state.position();
            facing = state.facing();
            hexesLogged = state.hexesMoved();
        }
    }

    // endregion

    // region Sending

    private Packet createUnitsPacket() {
        return new Packet(PacketCommand.SENDING_ENTITIES, new ArrayList<>(game.getInGameObjects()));
    }

    private void sendUnits() {
        send(createUnitsPacket());
    }

    /** Adds lines to the battle log and sends them to every player. */
    void sendLog(List<String> lines) {
        if (!lines.isEmpty()) {
            game.addToBattleLog(lines);
            send(new Packet(PacketCommand.SENDING_REPORTS, new ArrayList<>(lines)));
            LOGGER.info("Battle log:\n{}", String.join("\n", lines));
        }
    }

    /** Tells one player why their request was refused and gives them the turn again. */
    private void refuse(int connId, String reason) {
        LOGGER.warn("Refused a request from player {}: {}", connId, reason);
        send(connId, new Packet(PacketCommand.SENDING_REPORTS, new ArrayList<>(List.of("Refused: " + reason))));
        if (game.getPhase().usesTurns()) {
            send(connId, packetHelper.createTurnListPacket());
            send(connId, packetHelper.createTurnIndexPacket(Player.PLAYER_NONE));
        }
    }

    // endregion

    /** @return a facing as a compass direction, such as "NE" */
    static String facingName(int facing) {
        return List.of("N", "NE", "SE", "S", "SW", "NW").get(Math.floorMod(facing, 6));
    }

    @Override
    public String toString() {
        return game.units().stream().map(HGUnit::toString).collect(Collectors.joining(", ", "HGGameManager[", "]"));
    }
}
