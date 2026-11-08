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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import jakarta.annotation.Nullable;
import megamek.common.Player;
import megamek.common.Team;
import megamek.common.board.Board;
import megamek.common.board.Coords;
import megamek.common.enums.GamePhase;
import megamek.common.event.entity.GameEntityChangeEvent;
import megamek.common.event.GamePhaseChangeEvent;
import megamek.common.event.GameTurnChangeEvent;
import megamek.common.event.UnitChangedGameEvent;
import megamek.common.game.AbstractGame;
import megamek.common.game.InGameObject;
import megamek.common.interfaces.ReportEntry;
import megamek.common.rolls.Roll;

/**
 * A Heavy Gear game: the players, the map, the units, the turn order and the battle log.
 *
 * <p>The server holds the real game; each player's computer holds a copy that the server keeps up to date by
 * sending packets. MegaMek's {@link AbstractGame} already looks after players, boards and the unit list; this class
 * adds what Heavy Gear needs on top.</p>
 */
public final class HGGame extends AbstractGame {

    /** The most Size points of units one hex can hold (notes 01 section 5.10). */
    public static final int STACKING_LIMIT = 30;

    private final HGRuleOptions options = new HGRuleOptions();
    private GamePhase phase = GamePhase.UNKNOWN;
    private GamePhase lastPhase = GamePhase.UNKNOWN;
    private final List<HGPlayerTurn> turnList = new ArrayList<>();
    private final List<InGameObject> graveyard = new ArrayList<>();
    private final List<String> battleLog = new ArrayList<>();

    public HGGame() {
        setBoard(0, new Board());
    }

    // region Phase

    @Override
    public HGRuleOptions getOptions() {
        return options;
    }

    @Override
    public GamePhase getPhase() {
        return phase;
    }

    @Override
    public void setPhase(GamePhase phase) {
        this.phase = phase;
    }

    @Override
    public void setLastPhase(GamePhase lastPhase) {
        this.lastPhase = lastPhase;
    }

    public GamePhase getLastPhase() {
        return lastPhase;
    }

    @Override
    public void receivePhase(GamePhase phase) {
        GamePhase oldPhase = this.phase;
        setPhase(phase);
        fireGameEvent(new GamePhaseChangeEvent(this, oldPhase, phase));
    }

    /**
     * The set-up and activation phases are played as long as someone has a turn; the initiative and end-of-round
     * phases are worked out by the server alone and skipped straight away.
     */
    @Override
    public boolean isCurrentPhasePlayable() {
        return switch (phase) {
            case DEPLOYMENT, MOVEMENT -> hasMoreTurns();
            case INITIATIVE, END -> false;
            default -> true;
        };
    }

    @Override
    public boolean isForceVictory() {
        return false;
    }

    // endregion

    // region Players and teams

    @Override
    public void addPlayer(int id, Player player) {
        super.addPlayer(id, player);
        player.setGame(this);
        setupTeams();
    }

    @Override
    public void setPlayer(int id, Player player) {
        players.put(id, player);
        player.setGame(this);
        setupTeams();
    }

    @Override
    public void removePlayer(int id) {
        players.remove(id);
        setupTeams();
    }

    /** Puts the players into teams by their team number. Players on the same team are on the same side. */
    @Override
    public void setupTeams() {
        List<Team> newTeams = new ArrayList<>();
        for (Player player : getPlayersList()) {
            if (player.getTeam() == Player.TEAM_UNASSIGNED) {
                continue;
            }
            Team team = newTeams.stream().filter(t -> t.getId() == player.getTeam()).findFirst().orElse(null);
            if ((team == null) || (player.getTeam() == Player.TEAM_NONE)) {
                team = new Team(player.getTeam());
                newTeams.add(team);
            }
            team.addPlayer(player);
        }
        teams.clear();
        teams.addAll(newTeams);
    }

    /** @return true when the two units belong to players on opposing sides */
    public boolean areEnemies(HGUnit unit1, HGUnit unit2) {
        Player owner1 = getPlayer(unit1.getOwnerId());
        Player owner2 = getPlayer(unit2.getOwnerId());
        return (owner1 != null) && (owner2 != null) && owner1.isEnemyOf(owner2);
    }

    // endregion

    // region Units

    @Override
    public void replaceUnits(List<InGameObject> units) {
        setUnitList(units);
    }

    /** Adds a unit on the server, giving it a new id if its id is taken. */
    public void addUnit(HGUnit unit) {
        int id = unit.getId();
        if ((id < 0) || inGameObjects.containsKey(id)) {
            id = getNextEntityId();
            unit.setId(id);
        }
        inGameObjects.put(id, unit);
    }

    /** Replaces one unit with a newer copy sent by the server (client side). */
    public void receiveUnit(InGameObject unit) {
        InGameObject oldUnit = inGameObjects.put(unit.getId(), unit);
        fireGameEvent(new UnitChangedGameEvent(this, oldUnit, unit));
    }

    /** Replaces the whole unit list with one sent by the server (client side). */
    public void setUnitList(List<InGameObject> units) {
        inGameObjects.clear();
        units.forEach(unit -> inGameObjects.put(unit.getId(), unit));
        fireGameEvent(new GameEntityChangeEvent(this, null));
    }

    @Override
    public List<InGameObject> getGraveyard() {
        return graveyard;
    }

    /** @return every Heavy Gear unit in the game, wrecks included */
    public List<HGUnit> units() {
        return inGameObjects.values().stream()
              .filter(HGUnit.class::isInstance)
              .map(HGUnit.class::cast)
              .sorted((u1, u2) -> Integer.compare(u1.getId(), u2.getId()))
              .toList();
    }

    /** @return the unit with this id, or null */
    public @Nullable HGUnit unit(int id) {
        return (inGameObjects.get(id) instanceof HGUnit hgUnit) ? hgUnit : null;
    }

    /** @return the units standing in a hex, wrecks included */
    public List<HGUnit> unitsAt(Coords coords) {
        return units().stream().filter(unit -> coords.equals(unit.position())).toList();
    }

    /**
     * @return the Size points of the units in a hex, leaving out one unit (usually the one that wants to enter)
     */
    public int sizeInHex(Coords coords, int excludedUnitId) {
        return unitsAt(coords).stream()
              .filter(unit -> unit.getId() != excludedUnitId)
              .mapToInt(unit -> unit.datacard().size())
              .sum();
    }

    /** @return the units of a player that are still in the fight */
    public List<HGUnit> fightingUnitsOf(int playerId) {
        return units().stream().filter(unit -> unit.getOwnerId() == playerId).filter(unit -> !unit.isOutOfFight())
              .toList();
    }

    /** @return the map; Heavy Gear games use a single board for now */
    public Board board() {
        return getBoard(0);
    }

    // endregion

    // region Turns

    @Override
    public @Nullable HGPlayerTurn getTurn() {
        return ((turnIndex < 0) || (turnIndex >= turnList.size())) ? null : turnList.get(turnIndex);
    }

    @Override
    public List<HGPlayerTurn> getTurnsList() {
        return Collections.unmodifiableList(turnList);
    }

    public void setTurns(List<HGPlayerTurn> newTurns) {
        turnList.clear();
        turnList.addAll(newTurns);
    }

    public boolean hasMoreTurns() {
        return turnList.size() > turnIndex + 1;
    }

    /** Moves on to the next turn in the list and returns it, or null at the end. */
    public @Nullable HGPlayerTurn changeToNextTurn() {
        turnIndex++;
        return getTurn();
    }

    /** Sets the current turn (client side) and tells the screens about it. */
    public void setTurnIndex(int turnIndex, int previousPlayerId) {
        setTurnIndex(turnIndex);
        HGPlayerTurn currentTurn = getTurn();
        if (currentTurn != null) {
            fireGameEvent(new GameTurnChangeEvent(this, getPlayer(currentTurn.playerId()), previousPlayerId));
        }
    }

    /** @return the player whose turn it is, if any */
    public Optional<Player> currentPlayer() {
        HGPlayerTurn turn = getTurn();
        return (turn == null) ? Optional.empty() : Optional.ofNullable(getPlayer(turn.playerId()));
    }

    // endregion

    // region Battle log

    /** @return every line of the battle log so far */
    public List<String> battleLog() {
        return Collections.unmodifiableList(battleLog);
    }

    public void addToBattleLog(List<String> lines) {
        battleLog.addAll(lines);
    }

    public void replaceBattleLog(List<String> lines) {
        battleLog.clear();
        battleLog.addAll(lines);
    }

    @Override
    public ReportEntry getNewReport(int messageId) {
        return new HGReportEntry("");
    }

    /** A report line; MegaMek's game interface asks for one, Heavy Gear's log is plain text lines. */
    public record HGReportEntry(String text) implements ReportEntry {
        @Override
        public ReportEntry addRoll(Roll roll) {
            return this;
        }
    }

    // endregion
}
