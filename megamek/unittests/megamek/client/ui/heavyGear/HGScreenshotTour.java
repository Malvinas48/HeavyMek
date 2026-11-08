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

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import megamek.client.HGClient;
import megamek.client.ui.clientGUI.boardview.BoardView;
import megamek.client.ui.util.MegaMekController;
import megamek.common.Player;
import megamek.common.board.Coords;
import megamek.common.enums.GamePhase;
import megamek.common.heavyGear.ActivationState;
import megamek.common.heavyGear.HGGame;
import megamek.common.heavyGear.HGStep;
import megamek.common.heavyGear.HGUnit;
import megamek.common.heavyGear.MovementSystem;
import megamek.common.scenario.ScenarioLoader;
import megamek.server.Server;
import megamek.server.heavyGear.HGGameManager;

/**
 * Not a unit test: a little program that opens the real game windows for both sides of the First Duel on one
 * screen, plays the first few turns through the same buttons a player would use, and saves a screenshot at each
 * step. Run it inside a virtual display to look at the screens without a monitor (see tools/ui-check).
 *
 * <pre>java -cp (test classpath) megamek.client.ui.heavyGear.HGScreenshotTour /tmp/hgtour 7 4 [mouse]</pre>
 * The arguments are the folder for the screenshots, the dice seed, the number of activations to play and, with
 * "mouse", whether to plan them with real mouse clicks instead of calling the panel's methods.
 */
public final class HGScreenshotTour {

    private static final List<HGClientGUI> ALL_WINDOWS = new java.util.ArrayList<>();

    private HGScreenshotTour() {
    }

    /**
     * Shows one window on top of the others. Without a window manager (as in a virtual display) raising a window
     * isn't reliable, so the other windows are moved off the screen instead.
     */
    private static void showOnTop(HGClientGUI window) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            for (HGClientGUI other : ALL_WINDOWS) {
                other.getFrame().setLocation((other == window) ? 0 : 5000, 0);
            }
            window.getFrame().toFront();
        });
        Thread.sleep(800);
    }

    public static void main(String[] args) throws Exception {
        File outDir = new File((args.length > 0) ? args[0] : "/tmp/hgtour");
        outDir.mkdirs();
        long seed = (args.length > 1) ? Long.parseLong(args[1]) : 7;
        int activations = (args.length > 2) ? Integer.parseInt(args[2]) : 4;

        HGGame game = (HGGame) new ScenarioLoader(new File("data/scenarios/heavygear/first-duel.mms")).load()
              .createGame();
        HGGameManager manager = new HGGameManager();
        Random random = new Random(seed);
        manager.setDice(() -> random.nextInt(6) + 1);
        Server server = new Server(null, 0, manager);
        server.setGame(game);

        MegaMekController controller = new MegaMekController();
        Map<String, HGClientGUI> windows = new LinkedHashMap<>();
        for (String name : List.of("North", "South")) {
            HGClient client = new HGClient(name, "localhost", server.getPort());
            HGClientGUI[] window = new HGClientGUI[1];
            SwingUtilities.invokeAndWait(() -> {
                window[0] = new HGClientGUI(client, controller);
                window[0].initialize();
                window[0].getFrame().setBounds(0, 0, 1600, 1000);
            });
            if (!client.connect()) {
                throw new IllegalStateException(name + " could not connect");
            }
            windows.put(name, window[0]);
            ALL_WINDOWS.add(window[0]);
        }
        Robot robot = new Robot();
        int shot = 0;

        for (int i = 0; i < 2; i++) {
            HGClientGUI window = waitForTurn(windows, GamePhase.DEPLOYMENT);
            if (window == null) {
                break;
            }
            Coords hex = deployHex(window);
            SwingUtilities.invokeAndWait(() -> window.hexClicked(hex));
            capture(robot, window, new File(outDir, String.format("%02d-setup-%s.png", shot++, name(window))));
            SwingUtilities.invokeAndWait(window::placeUnit);
        }

        boolean useMouse = (args.length > 3) && args[3].equals("mouse");
        for (int i = 0; i < activations; i++) {
            HGClientGUI window = waitForTurn(windows, GamePhase.MOVEMENT);
            if (window == null) {
                break;
            }
            if (useMouse) {
                planWithTheMouse(robot, window);
            } else {
                SwingUtilities.invokeAndWait(() -> plan(window));
            }
            capture(robot, window, new File(outDir, String.format("%02d-plan-%s.png", shot++, name(window))));
            if (useMouse) {
                showOnTop(window);
                click(robot, window, button(window, "End activation"));
            } else {
                SwingUtilities.invokeAndWait(window::endActivation);
            }
        }
        Thread.sleep(1500);
        for (HGClientGUI window : windows.values()) {
            capture(robot, window, new File(outDir, String.format("%02d-after-%s.png", shot++, name(window))));
        }
        System.out.println("Screenshots in " + outDir);
        System.exit(0);
    }

    /** Plans an activation with the panel's own methods: change to ground, close in, face the enemy, shoot. */
    private static void plan(HGClientGUI window) {
        HGPlanner planner = window.planner();
        HGUnit unit = planner.unit();
        HGGame game = planner.game();
        HGUnit enemy = game.units().stream()
              .filter(other -> (unit != null) && game.areEnemies(unit, other) && !other.isOutOfFight())
              .findFirst().orElse(null);
        if ((unit == null) || (enemy == null)) {
            return;
        }
        if (unit.movementSystem() == MovementSystem.WALKER) {
            window.addStep(HGStep.switchTo(MovementSystem.GROUND));
        }
        face(window, enemy);
        for (int i = 0; i < 4; i++) {
            ActivationState state = planner.state();
            if ((state == null) || (state.position().distance(enemy.position()) <= 3)) {
                break;
            }
            window.addStep(HGStep.forward());
        }
        face(window, enemy);
        window.hexClicked(enemy.position());
        for (int[] choice : new int[][] { { 1, 1 }, { 0, 1 }, { 0, 0 } }) {
            planner.setWeaponIndex(choice[0]);
            planner.setRateOfFire(choice[1]);
            if (planner.checkShot() == null) {
                break;
            }
        }
        if (planner.checkShot() == null) {
            window.addStep(planner.shot());
        } else {
            planner.setTarget(null);
        }
        window.refresh();
    }


    /**
     * Plans an activation the way a player would, with real mouse clicks on the panel's buttons and on the map:
     * switch to ground, three hexes forward, click the enemy, fire.
     */
    private static void planWithTheMouse(Robot robot, HGClientGUI window) throws Exception {
        showOnTop(window);
        JButton switchButton = button(window, "Switch to Ground");
        if ((switchButton != null) && switchButton.isEnabled()) {
            click(robot, window, switchButton);
        }
        for (int i = 0; i < 3; i++) {
            click(robot, window, button(window, "\u2191 Forward"));
        }
        HGGame game = window.getClient().getGame();
        HGUnit unit = window.planner().unit();
        HGUnit enemy = game.units().stream().filter(other -> (unit != null) && game.areEnemies(unit, other))
              .findFirst().orElse(null);
        if (enemy != null) {
            clickHex(robot, window, enemy.position());
        }
        JButton fire = button(window, "Fire from here");
        if ((fire != null) && fire.isEnabled()) {
            click(robot, window, fire);
        }
        System.out.println(name(window) + " planned with the mouse: " + window.planner().steps());
    }

    private static void click(Robot robot, HGClientGUI window, JButton button) throws Exception {
        if (button == null) {
            throw new IllegalStateException("Button not found in " + name(window));
        }
        Point[] centre = new Point[1];
        SwingUtilities.invokeAndWait(() -> {
            Point corner = button.getLocationOnScreen();
            centre[0] = new Point(corner.x + button.getWidth() / 2, corner.y + button.getHeight() / 2);
        });
        robot.mouseMove(centre[0].x, centre[0].y);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        Thread.sleep(500);
    }

    private static void clickHex(Robot robot, HGClientGUI window, Coords coords) throws Exception {
        Point[] centre = new Point[1];
        SwingUtilities.invokeAndWait(() -> {
            BoardView boardView = window.boardView();
            boardView.centerOnHex(coords);
        });
        Thread.sleep(1500);
        SwingUtilities.invokeAndWait(() -> {
            BoardView boardView = window.boardView();
            JScrollPane scroll = (JScrollPane) boardView.getComponent();
            Point view = scroll.getViewport().getView().getLocationOnScreen();
            Point hex = boardView.getHexLocation(coords);
            // the board view draws the map with a margin of one hex width and height (see getCoordsAt)
            centre[0] = new Point(view.x + 84 + hex.x + boardView.getHexSize().width / 2,
                  view.y + 72 + hex.y + boardView.getHexSize().height / 2);
        });
        robot.mouseMove(centre[0].x, centre[0].y);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        Thread.sleep(700);
    }

    /** Finds a button in the window by the start of its text. */
    private static JButton button(HGClientGUI window, String text) {
        List<Component> todo = new java.util.ArrayList<>(List.of(window.getFrame().getContentPane()));
        while (!todo.isEmpty()) {
            Component component = todo.remove(0);
            if ((component instanceof JButton button) && (button.getText() != null) && button.isShowing()
                  && button.getText().startsWith(text)) {
                return button;
            }
            if (component instanceof Container container) {
                todo.addAll(List.of(container.getComponents()));
            }
        }
        return null;
    }

    private static void face(HGClientGUI window, HGUnit enemy) {
        for (int i = 0; i < 3; i++) {
            ActivationState state = window.planner().state();
            if ((state == null) || state.position().equals(enemy.position())) {
                return;
            }
            int wanted = state.position().direction(enemy.position());
            if (state.facing() == wanted) {
                return;
            }
            int clockwise = Math.floorMod(wanted - state.facing(), 6);
            window.addStep((clockwise <= 3) ? HGStep.turnRight() : HGStep.turnLeft());
        }
    }

    private static Coords deployHex(HGClientGUI window) {
        HGGame game = window.getClient().getGame();
        Player me = game.getPlayer(window.getClient().getLocalPlayerNumber());
        int middle = game.board().getWidth() / 2;
        for (int y = 0; y < game.board().getHeight(); y++) {
            Coords coords = new Coords(middle - 1, (me.getName().equals("North")) ? y : game.board().getHeight() - 1 - y);
            if (game.board().isLegalDeployment(coords, me)) {
                return (me.getName().equals("North")) ? coords.translated(3) : coords.translated(0);
            }
        }
        return new Coords(middle, 0);
    }

    private static HGClientGUI waitForTurn(Map<String, HGClientGUI> windows, GamePhase phase) throws Exception {
        for (int i = 0; i < 300; i++) {
            for (HGClientGUI window : windows.values()) {
                HGClient client = window.getClient();
                if (client.getGame().getPhase() == GamePhase.VICTORY) {
                    return null;
                }
                if (client.isMyTurn() && (client.getGame().getPhase() == phase)
                      && (window.planner().unit() != null)) {
                    Thread.sleep(500);
                    return window;
                }
            }
            Thread.sleep(100);
        }
        throw new IllegalStateException("Timed out waiting for a turn in " + phase);
    }

    private static void capture(Robot robot, HGClientGUI window, File file) throws Exception {
        showOnTop(window);
        SwingUtilities.invokeAndWait(() -> window.getFrame().repaint());
        Thread.sleep(600);
        Rectangle bounds = window.getFrame().getBounds();
        BufferedImage image = robot.createScreenCapture(bounds);
        ImageIO.write(image, "png", file);
        System.out.println("Saved " + file);
    }

    private static String name(HGClientGUI window) {
        return window.getClient().getName();
    }
}
