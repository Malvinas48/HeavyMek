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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import jakarta.annotation.Nullable;
import megamek.client.HGClient;
import megamek.client.event.BoardViewEvent;
import megamek.client.event.BoardViewListenerAdapter;
import megamek.client.ui.clientGUI.AbstractClientGUI;
import megamek.client.ui.clientGUI.MegaMekGUI;
import megamek.client.ui.clientGUI.boardview.BoardView;
import megamek.client.ui.util.MegaMekController;
import megamek.common.board.Coords;
import megamek.common.enums.GamePhase;
import megamek.common.event.GameEvent;
import megamek.common.event.GameListenerAdapter;
import megamek.common.event.GamePhaseChangeEvent;
import megamek.common.event.GameReportEvent;
import megamek.common.event.GameTurnChangeEvent;
import megamek.common.event.board.GameBoardNewEvent;
import megamek.common.event.entity.GameEntityChangeEvent;
import megamek.common.event.player.GamePlayerChangeEvent;
import megamek.common.game.Game;
import megamek.common.heavyGear.ActivationState;
import megamek.common.heavyGear.HGGame;
import megamek.common.heavyGear.HGStep;
import megamek.common.heavyGear.HGUnit;
import megamek.logging.MMLogger;

/**
 * The Heavy Gear game window: the map in the middle, the {@link HGControlPanel} on the right and the battle log at
 * the bottom.
 *
 * <p>MegaMek's map drawing ({@link BoardView}) is reused as it is. It was written for BattleTech and wants a
 * BattleTech game object, so it gets an empty one that only holds the map; the Heavy Gear units are drawn on top
 * as sprites by {@link HGBoardSprites}.</p>
 *
 * <p>Messages from the server arrive on the network thread. Swing may only be touched from its own thread, so
 * every reaction is passed over with {@link SwingUtilities#invokeLater}.</p>
 */
public class HGClientGUI extends AbstractClientGUI {

    private static final MMLogger LOGGER = MMLogger.create(HGClientGUI.class);

    private final HGClient client;
    private final MegaMekController controller;
    private final HGPlanner planner;
    private final Game boardGame = new Game();
    private @Nullable BoardView boardView;
    private HGBoardSprites sprites;
    private HGControlPanel controlPanel;
    private final JTextArea logArea = new JTextArea();
    private @Nullable HGUnit inspectedUnit;
    private boolean victoryShown;
    private final AtomicBoolean refreshQueued = new AtomicBoolean();

    private final GameListenerAdapter gameListener = new GameListenerAdapter() {
        @Override
        public void gamePhaseChange(GamePhaseChangeEvent e) {
            SwingUtilities.invokeLater(() -> phaseChanged(e.getNewPhase()));
        }

        @Override
        public void gameTurnChange(GameTurnChangeEvent e) {
            SwingUtilities.invokeLater(HGClientGUI.this::turnChanged);
        }

        @Override
        public void gameEntityChange(GameEntityChangeEvent e) {
            refresh();
        }

        @Override
        public void gameUnitChange(GameEvent e) {
            refresh();
        }

        @Override
        public void gamePlayerChange(GamePlayerChangeEvent e) {
            refresh();
        }

        @Override
        public void gameReport(GameReportEvent e) {
            SwingUtilities.invokeLater(() -> reportReceived(e.getReport()));
        }

        @Override
        public void gameBoardNew(GameBoardNewEvent e) {
            SwingUtilities.invokeLater(() -> boardGame.setBoard(e.getNewBoard()));
        }
    };

    public HGClientGUI(HGClient client, MegaMekController controller) {
        super(client);
        this.client = client;
        this.controller = controller;
        planner = new HGPlanner(client);
        frame.setTitle(client.getName() + " - HeavyMek");
    }

    @Override
    public void initialize() {
        super.initialize();
        client.getGame().addGameListener(gameListener);
        boardGame.setBoard(client.getGame().board());
        try {
            MegaMekController keys = (controller != null) ? controller : MegaMekGUI.getKeyDispatcher();
            boardView = new BoardView(boardGame, keys, null, 0);
        } catch (IOException e) {
            LOGGER.fatal(e, "The map view could not be created");
            die();
            return;
        }
        boardView.setTooltipProvider((point, movementTarget) -> tooltip(boardView.getCoordsAt(point)));
        boardView.addBoardViewListener(new BoardViewListenerAdapter() {
            @Override
            public void hexMoused(BoardViewEvent event) {
                if ((event.getType() == BoardViewEvent.BOARD_HEX_CLICKED)
                      && (event.getButton() == MouseEvent.BUTTON1)) {
                    hexClicked(event.getCoords());
                }
            }
        });
        boardViews.put(0, boardView);
        boardViewsContainer.setName("BoardView");
        boardViewsContainer.updateMapTabs();

        sprites = new HGBoardSprites(this, planner);
        spriteHandlers.add(sprites);
        sprites.initialize();

        controlPanel = new HGControlPanel(this, planner);
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("Battle log"));
        JScrollPane panelScroll = new JScrollPane(controlPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
              ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        panelScroll.setPreferredSize(new Dimension(430, 600));
        panelScroll.getVerticalScrollBar().setUnitIncrement(16);
        logScroll.setPreferredSize(new Dimension(600, 190));

        JSplitPane mapAndLog = new JSplitPane(JSplitPane.VERTICAL_SPLIT, boardViewsContainer.getPanel(), logScroll);
        mapAndLog.setResizeWeight(1.0);
        JPanel content = new JPanel(new BorderLayout());
        content.add(mapAndLog, BorderLayout.CENTER);
        content.add(panelScroll, BorderLayout.EAST);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(content, BorderLayout.CENTER);
        makeRoomForTheMap();
        frame.setVisible(true);
        refresh();
    }

    /**
     * MegaMek opens game windows at 800 x 600 until the player has resized one. That leaves the Heavy Gear map
     * cramped next to its side panel, so a small window grows to fill most of the screen.
     */
    private void makeRoomForTheMap() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        if ((frame.getWidth() < 1100) || (frame.getHeight() < 700)) {
            frame.setSize(Math.max(frame.getWidth(), Math.min(screen.width - 40, 1500)),
                  Math.max(frame.getHeight(), Math.min(screen.height - 80, 950)));
            frame.setLocationRelativeTo(null);
        }
    }

    // region What the panel and the sprites ask for

    @Override
    public HGClient getClient() {
        return client;
    }

    /** @return the player's plan for the current turn */
    HGPlanner planner() {
        return planner;
    }

    /** @return the map view, or null before the window is set up */
    @Nullable BoardView boardView() {
        return boardView;
    }

    /** @return the unit last clicked on the map, for the side panel when no unit is selected */
    @Nullable HGUnit inspectedUnit() {
        return (inspectedUnit == null) ? null : client.getGame().unit(inspectedUnit.getId());
    }

    /**
     * Redraws the panel and the map from the game and the plan. Safe to call from any thread; several calls in a
     * row are combined into one redraw.
     */
    void refresh() {
        if (!refreshQueued.compareAndSet(false, true)) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            refreshQueued.set(false);
            if (controlPanel != null) {
                controlPanel.refresh();
            }
            if (sprites != null) {
                sprites.update();
            }
        });
    }

    void clearMessage() {
        controlPanel.showMessage(null);
    }

    // endregion

    // region What the player does

    /** Adds a step to the activation plan, or says why it isn't allowed. */
    void addStep(HGStep step) {
        controlPanel.showMessage(planner.addStep(step));
        refresh();
    }

    /** Sends the set-up placement to the server. */
    void placeUnit() {
        HGUnit unit = planner.unit();
        Coords coords = planner.deployCoords();
        if ((unit == null) || (coords == null) || !client.isMyTurn()) {
            return;
        }
        client.deploy(unit.getId(), coords, planner.deployFacing(), planner.deploySystem(), planner.deploySpeed());
        planner.reset();
        clearMessage();
        refresh();
    }

    /** Sends the activation plan to the server, after a warning when it breaks the Top Speed minimum. */
    void endActivation() {
        ActivationState state = planner.state();
        if ((state == null) || !client.isMyTurn()) {
            return;
        }
        String warning = state.topSpeedWarning();
        if ((warning != null) && (JOptionPane.showConfirmDialog(frame, warning + "\nEnd the activation anyway?",
              "Top Speed", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)) {
            return;
        }
        if (!planner.hasFired() && (planner.target() != null) && (planner.checkShot() == null)
              && (JOptionPane.showConfirmDialog(frame, "You have a target but no shot in the plan.\n"
              + "End the activation without firing?", "No shot", JOptionPane.YES_NO_OPTION)
              != JOptionPane.YES_OPTION)) {
            return;
        }
        client.activate(planner.activation());
        planner.reset();
        clearMessage();
        refresh();
    }

    /** Reacts to a left click on a hex of the map. */
    void hexClicked(Coords coords) {
        HGGame game = client.getGame();
        List<HGUnit> unitsHere = game.unitsAt(coords);
        HGUnit selected = planner.unit();
        String message = null;
        if (client.isMyTurn() && game.getPhase().isDeployment() && (selected != null)) {
            message = planner.setDeployCoords(coords);
        } else if (client.isMyTurn() && game.getPhase().isMovement()) {
            Optional<HGUnit> enemy = unitsHere.stream()
                  .filter(unit -> (selected != null) && game.areEnemies(selected, unit) && !unit.isOutOfFight())
                  .findFirst();
            Optional<HGUnit> own = unitsHere.stream().filter(planner::isEligible).findFirst();
            if (enemy.isPresent()) {
                planner.setTarget(enemy.get());
            } else if (own.isPresent() && (selected != own.get())) {
                if (planner.steps().isEmpty()) {
                    planner.selectUnit(own.get());
                } else {
                    message = "Finish this unit's plan, or press Start over, before picking another unit.";
                }
            }
        }
        if (!unitsHere.isEmpty()) {
            inspectedUnit = unitsHere.get(0);
        }
        controlPanel.showMessage(message);
        refresh();
    }

    // endregion

    // region What the server tells us

    private void phaseChanged(GamePhase phase) {
        planner.reset();
        clearMessage();
        refresh();
        if ((phase == GamePhase.VICTORY) && !victoryShown) {
            victoryShown = true;
            String result = client.getGame().battleLog().stream().filter(line -> line.startsWith("***"))
                  .reduce((first, second) -> second).orElse("The battle is over.").replace("*", "").trim();
            JOptionPane.showMessageDialog(frame, result, "HeavyMek", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void turnChanged() {
        planner.reset();
        List<HGUnit> eligible = planner.eligibleUnits();
        if (!eligible.isEmpty()) {
            HGUnit unit = eligible.get(0);
            planner.selectUnit(unit);
            centerOn(unit.isDeployed() ? unit.position() : planner.deploymentZoneCentre());
        }
        refresh();
    }

    /** Scrolls the map to a hex, now and once more shortly after, when the map has caught up with the board. */
    private void centerOn(@Nullable Coords coords) {
        if ((boardView != null) && (coords != null)) {
            boardView.centerOnHex(coords);
            Timer again = new Timer(400, e -> boardView.centerOnHex(coords));
            again.setRepeats(false);
            again.start();
        }
    }

    private void reportReceived(String newLines) {
        StringBuilder text = new StringBuilder();
        for (String line : client.getGame().battleLog()) {
            // a blank line before each round and each activation makes the log easier to scan
            if ((text.length() > 0) && (line.startsWith("===") || line.endsWith(" activates."))) {
                text.append('\n');
            }
            text.append(line).append('\n');
        }
        logArea.setText(text.toString());
        logArea.setCaretPosition(logArea.getDocument().getLength());
        for (String line : newLines.split("\n")) {
            if (line.startsWith("Refused:")) {
                controlPanel.showMessage(HGUnitCard.escape(line));
            }
        }
        refresh();
    }

    private @Nullable String tooltip(@Nullable Coords coords) {
        if (coords == null) {
            return null;
        }
        List<HGUnit> units = client.getGame().unitsAt(coords);
        if (units.isEmpty()) {
            return null;
        }
        StringBuilder html = new StringBuilder("<html>");
        for (HGUnit unit : units) {
            html.append(HGUnitCard.html(unit, client.getGame(), false)).append("<br>");
        }
        return html.append("</html>").toString();
    }

    // endregion

    // region Window plumbing

    @Override
    public void handleExit() {
        GUIP.setWindowPosX(frame.getLocation().x);
        GUIP.setWindowPosY(frame.getLocation().y);
        GUIP.setWindowSizeWidth(frame.getSize().width);
        GUIP.setWindowSizeHeight(frame.getSize().height);
        frame.setVisible(false);
        die();
    }

    @Override
    public void die() {
        client.getGame().removeGameListener(gameListener);
        if (boardView != null) {
            boardView.dispose();
        }
        client.die();
        if (controller != null) {
            controller.removeAllActions();
            controller.clientGUI = null;
        }
        super.die();
    }

    @Override
    protected boolean saveGame() {
        LOGGER.info("Saving is not available in Heavy Gear games yet");
        return false;
    }

    @Override
    public JComponent turnTimerComponent() {
        return controlPanel;
    }

    @Override
    public void setChatBoxActive(boolean active) {
    }

    @Override
    public void clearChatBox() {
    }

    // endregion
}
