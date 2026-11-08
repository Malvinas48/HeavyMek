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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import megamek.client.HGClient;
import megamek.common.Configuration;
import megamek.common.Player;
import megamek.common.board.Coords;
import megamek.common.enums.GamePhase;
import megamek.common.event.GameListenerAdapter;
import megamek.common.event.GamePhaseChangeEvent;
import megamek.common.event.GameTurnChangeEvent;
import megamek.common.game.GameType;
import megamek.common.heavyGear.ActivationState;
import megamek.common.heavyGear.HGActivation;
import megamek.common.heavyGear.HGGame;
import megamek.common.heavyGear.HGPlayerTurn;
import megamek.common.heavyGear.HGStep;
import megamek.common.heavyGear.HGUnit;
import megamek.common.heavyGear.MovementSystem;
import megamek.common.heavyGear.SpeedState;
import megamek.common.scenario.Scenario;
import megamek.common.scenario.ScenarioLoader;
import megamek.server.Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Plays the First Duel scenario from start to finish over a real network connection: a server on this computer
 * and two players, North and South, each played by a small script. The script places its Gear, then every round
 * turns towards the enemy, closes in and fires.
 *
 * <p>This checks that every piece fits together: the scenario file, the unit files, the packets in both
 * directions, set-up, initiative, activations, damage and the end of the game. The dice are seeded, so each
 * battle is the same every time; several seeds give several different battles.</p>
 */
class HGDuelTest {

    private Server server;
    private final List<HGClient> clients = new ArrayList<>();
    private final List<ScriptedPlayer> players = new ArrayList<>();
    private File previousDataDir;

    @BeforeEach
    void setUp() {
        // some of MegaMek's own tests point the data folder elsewhere and don't put it back
        previousDataDir = Configuration.dataDir();
        Configuration.setDataDir(new File("data"));
    }

    @AfterEach
    void tearDown() {
        Configuration.setDataDir(previousDataDir);
        players.forEach(ScriptedPlayer::stop);
        clients.forEach(HGClient::die);
        if (server != null) {
            server.die();
        }
    }

    @ParameterizedTest(name = "dice seed {0}")
    @ValueSource(longs = { 20261108, 1, 2, 3, 4, 5 })
    void scriptedPlayersFightTheFirstDuelToTheEnd(long seed) throws Exception {
        Scenario scenario = new ScenarioLoader(new File("data/scenarios/heavygear/first-duel.mms")).load();
        assertEquals(GameType.HG, scenario.getGameType());
        HGGame game = (HGGame) scenario.createGame();
        assertEquals(2, game.units().size());
        assertTrue(game.units().stream().noneMatch(HGUnit::isDeployed), "both Gears are placed at set-up");

        HGGameManager manager = new HGGameManager();
        Random random = new Random(seed);
        manager.setDice(() -> random.nextInt(6) + 1);
        server = new Server(null, 0, manager);
        server.setGame(game);

        CountDownLatch battleOver = new CountDownLatch(2);
        for (String name : List.of("North", "South")) {
            HGClient client = new HGClient(name, "localhost", server.getPort());
            ScriptedPlayer player = new ScriptedPlayer(client, battleOver);
            client.getGame().addGameListener(player);
            clients.add(client);
            players.add(player);
            assertTrue(client.connect(), name + " should connect");
        }

        boolean ended = battleOver.await(180, TimeUnit.SECONDS);
        String log = String.join("\n", manager.getGame().battleLog());
        assertTrue(ended, "the duel should end; the battle log so far:\n" + log);
        assertTrue(log.contains("wins the battle"), log);
        assertTrue(log.contains("places Bear (Hunter)") && log.contains("places Wolf (Jager)"), log);
        assertTrue(log.contains("Attack: Gunnery"), log);
        assertTrue(manager.getGame().getCurrentRound() >= 1);
        assertTrue(log.contains("=== Round 1 ==="), "rounds are counted from 1");
        long sidesLeft = manager.getGame().getPlayersList().stream()
              .filter(player -> !manager.getGame().fightingUnitsOf(player.getId()).isEmpty())
              .count();
        assertTrue(sidesLeft <= 1);
        for (HGClient client : clients) {
            assertEquals(GamePhase.VICTORY, client.getGame().getPhase());
            assertTrue(client.getGame().battleLog().stream().noneMatch(line -> line.startsWith("Refused")),
                  "the server should accept every plan the rules code allowed");
        }
    }

    @Test
    void aPlayerWhoLeavesCanComeBackAndFinishTheBattle() throws Exception {
        HGGame game = (HGGame) new ScenarioLoader(new File("data/scenarios/heavygear/first-duel.mms")).load()
              .createGame();
        HGGameManager manager = new HGGameManager();
        Random random = new Random(42);
        manager.setDice(() -> random.nextInt(6) + 1);
        server = new Server(null, 0, manager);
        server.setGame(game);

        CountDownLatch battleOver = new CountDownLatch(2);
        HGClient north = new HGClient("North", "localhost", server.getPort());
        ScriptedPlayer northPlayer = new ScriptedPlayer(north, battleOver);
        north.getGame().addGameListener(northPlayer);
        clients.add(north);
        players.add(northPlayer);
        CountDownLatch neverUsed = new CountDownLatch(1);
        HGClient firstSouth = new HGClient("South", "localhost", server.getPort());
        ScriptedPlayer firstSouthPlayer = new ScriptedPlayer(firstSouth, neverUsed);
        firstSouth.getGame().addGameListener(firstSouthPlayer);
        players.add(firstSouthPlayer);
        assertTrue(north.connect());
        assertTrue(firstSouth.connect());

        // South places its Gear, then its computer "crashes"
        assertTrue(firstSouthPlayer.placed.await(30, TimeUnit.SECONDS), "South should place its Gear");
        firstSouthPlayer.stop();
        firstSouth.die();
        Player south = manager.getGame().getPlayersList().stream()
              .filter(player -> player.getName().equals("South")).findFirst().orElseThrow();
        for (int i = 0; (i < 100) && !south.isGhost(); i++) {
            Thread.sleep(100);
        }
        assertTrue(south.isGhost(), "a player who leaves becomes a ghost");

        // South connects again under the same name and takes over its Gear
        HGClient secondSouth = new HGClient("South", "localhost", server.getPort());
        ScriptedPlayer secondSouthPlayer = new ScriptedPlayer(secondSouth, battleOver);
        secondSouth.getGame().addGameListener(secondSouthPlayer);
        clients.add(secondSouth);
        players.add(secondSouthPlayer);
        assertTrue(secondSouth.connect());

        boolean ended = battleOver.await(180, TimeUnit.SECONDS);
        String log = String.join("\n", manager.getGame().battleLog());
        assertTrue(ended, "the duel should end after South comes back; the log:\n" + log);
        assertTrue(log.contains("South has left the game"), log);
        assertTrue(log.contains("wins the battle"), log);
        assertEquals(south.getId(), secondSouth.getLocalPlayerNumber(), "South gets its old seat back");
    }

    /**
     * A very simple player: places its Gear in the middle of its zone, then every round switches to its faster
     * ground system, turns towards the nearest enemy, closes to two hexes and takes the best shot it can.
     */
    private static class ScriptedPlayer extends GameListenerAdapter {

        private final HGClient client;
        private final CountDownLatch battleOver;
        private final CountDownLatch placed = new CountDownLatch(1);
        private final ExecutorService worker = Executors.newSingleThreadExecutor();

        ScriptedPlayer(HGClient client, CountDownLatch battleOver) {
            this.client = client;
            this.battleOver = battleOver;
        }

        void stop() {
            worker.shutdownNow();
        }

        @Override
        public void gameTurnChange(GameTurnChangeEvent e) {
            if (client.isMyTurn()) {
                worker.submit(this::play);
            }
        }

        @Override
        public void gamePhaseChange(GamePhaseChangeEvent e) {
            if (e.getNewPhase() == GamePhase.VICTORY) {
                battleOver.countDown();
            }
        }

        private void play() {
            HGGame game = client.getGame();
            HGPlayerTurn turn = game.getTurn();
            if (!client.isMyTurn() || (turn == null)) {
                return;
            }
            HGUnit unit = game.units().stream().filter(u -> turn.isValidEntity(u, game)).findFirst().orElse(null);
            if (unit == null) {
                return;
            }
            if (game.getPhase().isDeployment()) {
                deploy(game, unit);
            } else if (game.getPhase().isMovement()) {
                activate(game, unit);
            }
        }

        private void deploy(HGGame game, HGUnit unit) {
            Player me = game.getPlayer(client.getLocalPlayerNumber());
            int middle = game.board().getWidth() / 2;
            for (int y = 0; y < game.board().getHeight(); y++) {
                Coords coords = new Coords(middle, y);
                if (game.board().isLegalDeployment(coords, me) && game.unitsAt(coords).isEmpty()) {
                    int facing = (y < game.board().getHeight() / 2) ? 3 : 0;
                    client.deploy(unit.getId(), coords, facing, MovementSystem.GROUND, SpeedState.COMBAT);
                    placed.countDown();
                    return;
                }
            }
        }

        private void activate(HGGame game, HGUnit unit) {
            HGUnit enemy = game.units().stream()
                  .filter(other -> game.areEnemies(unit, other) && other.isDeployed() && !other.isOutOfFight())
                  .min(Comparator.comparingInt(other -> other.position().distance(unit.position())))
                  .orElse(null);
            ActivationState state = new ActivationState(game, unit);
            List<HGStep> steps = new ArrayList<>();
            if (enemy != null) {
                if (unit.movementSystem() != MovementSystem.GROUND) {
                    tryStep(state, steps, HGStep.switchTo(MovementSystem.GROUND));
                }
                faceEnemy(state, steps, enemy);
                while ((state.position().distance(enemy.position()) > 2) && tryStep(state, steps, HGStep.forward())) {
                    // keep closing in
                }
                faceEnemy(state, steps, enemy);
                int[][] shots = { { 1, 1 }, { 0, 1 }, { 0, 0 }, { 1, 0 }, { 2, 0 } };
                for (int[] shot : shots) {
                    if (tryStep(state, steps, HGStep.fire(shot[0], enemy.getId(), shot[1]))) {
                        break;
                    }
                }
            }
            client.activate(new HGActivation(unit.getId(), steps, false));
        }

        private static void faceEnemy(ActivationState state, List<HGStep> steps, HGUnit enemy) {
            if (state.position().equals(enemy.position())) {
                return;
            }
            int wanted = state.position().direction(enemy.position());
            for (int i = 0; (i < 3) && (state.facing() != wanted); i++) {
                int clockwise = Math.floorMod(wanted - state.facing(), 6);
                HGStep step = (clockwise <= 3) ? HGStep.turnRight() : HGStep.turnLeft();
                if (!tryStep(state, steps, step)) {
                    return;
                }
            }
        }

        private static boolean tryStep(ActivationState state, List<HGStep> steps, HGStep step) {
            if (state.tryApply(step) == null) {
                steps.add(step);
                return true;
            }
            return false;
        }
    }
}
