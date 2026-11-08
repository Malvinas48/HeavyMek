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
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.Scrollable;

import jakarta.annotation.Nullable;
import megamek.common.Player;
import megamek.common.enums.GamePhase;
import megamek.common.heavyGear.ActivationState;
import megamek.common.heavyGear.AttackRules;
import megamek.common.heavyGear.HGGame;
import megamek.common.heavyGear.HGMovementMode;
import megamek.common.heavyGear.HGStep;
import megamek.common.heavyGear.HGUnit;
import megamek.common.heavyGear.HGWeapon;
import megamek.common.heavyGear.MovementSystem;
import megamek.common.heavyGear.RangeBand;
import megamek.common.heavyGear.RollModifier;
import megamek.common.heavyGear.SpeedState;

/**
 * The panel beside the map: what is happening, the selected unit's record sheet, and the controls for the current
 * turn (placing a unit at set-up, or planning an activation).
 *
 * <p>The panel never changes the game itself. Buttons tell the {@link HGPlanner} or the {@link HGClientGUI}, which
 * then asks the panel to {@link #refresh()} from the new state.</p>
 */
class HGControlPanel extends JPanel implements Scrollable {

    private static final String WAITING = "waiting";
    private static final String DEPLOY = "deploy";
    private static final String ACTIVATE = "activate";
    private static final List<String> FACINGS = List.of("N", "NE", "SE", "S", "SW", "NW");

    private final HGClientGUI gui;
    private final HGPlanner planner;
    private boolean updating;

    private final JLabel phaseLabel = new JLabel(" ");
    private final JLabel turnLabel = new JLabel(" ");
    private final JEditorPane messageLabel = htmlPane();
    private final JEditorPane unitCard = htmlPane();

    private final CardLayout cards = new CardLayout();
    private final JPanel cardPanel = new JPanel(cards);
    private final JEditorPane waitingLabel = htmlPane();

    private final JComboBox<HGUnit> deployUnitBox = unitBox();
    private final JLabel deployFacingLabel = new JLabel();
    private final JComboBox<MovementSystem> deploySystemBox = new JComboBox<>();
    private final JComboBox<SpeedState> deploySpeedBox = new JComboBox<>(SpeedState.values());
    private final JButton placeButton = new JButton("Place unit");

    private final JComboBox<HGUnit> activateUnitBox = unitBox();
    private final JButton turnLeftButton = new JButton("↶ Left");
    private final JButton forwardButton = new JButton("↑ Forward");
    private final JButton turnRightButton = new JButton("Right ↷");
    private final JButton backwardButton = new JButton("↓ Reverse");
    private final JButton switchButton = new JButton("Switch system");
    private final JButton undoButton = new JButton("Undo");
    private final JButton clearButton = new JButton("Start over");
    private final JEditorPane moveLabel = htmlPane();
    private final JCheckBox speedBox = new JCheckBox();
    private final JEditorPane targetLabel = htmlPane();
    private final JComboBox<String> weaponBox = new JComboBox<>();
    private final JComboBox<String> rofBox = new JComboBox<>();
    private final JEditorPane shotPreview = htmlPane();
    private final JButton fireButton = new JButton("Fire from here");
    private final JButton doneButton = new JButton("End activation");

    HGControlPanel(HGClientGUI gui, HGPlanner planner) {
        this.gui = gui;
        this.planner = planner;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        phaseLabel.setFont(phaseLabel.getFont().deriveFont(Font.BOLD, 16f));
        turnLabel.setFont(turnLabel.getFont().deriveFont(Font.BOLD, 13f));
        header.add(phaseLabel);
        header.add(turnLabel);
        header.add(messageLabel);
        header.add(Box.createVerticalStrut(4));
        add(header, BorderLayout.NORTH);

        cardPanel.add(waitingPanel(), WAITING);
        cardPanel.add(deployPanel(), DEPLOY);
        cardPanel.add(activatePanel(), ACTIVATE);
        add(cardPanel, BorderLayout.CENTER);

        unitCard.setBorder(BorderFactory.createTitledBorder("Unit"));
        add(unitCard, BorderLayout.SOUTH);
    }

    // region Building the panels

    private JComponent waitingPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(waitingLabel, BorderLayout.NORTH);
        return panel;
    }

    private JComponent deployPanel() {
        JPanel panel = column();
        panel.setBorder(BorderFactory.createTitledBorder("Set-up: place a unit"));
        panel.add(row(new JLabel("Unit:"), deployUnitBox));
        panel.add(note("Click a green hex on the map to put the unit there."));
        JButton left = new JButton("↶");
        JButton right = new JButton("↷");
        left.addActionListener(e -> changeDeployFacing(-1));
        right.addActionListener(e -> changeDeployFacing(1));
        panel.add(row(new JLabel("Facing:"), left, deployFacingLabel, right));
        panel.add(row(new JLabel("Movement system:"), deploySystemBox));
        panel.add(row(new JLabel("Speed:"), deploySpeedBox));
        panel.add(note("<i>A unit attacked in the first round before it moves defends as if it had moved as far as "
              + "this speed allows.</i>"));
        panel.add(row(placeButton));

        deployUnitBox.addActionListener(e -> {
            if (!updating && (deployUnitBox.getSelectedItem() instanceof HGUnit unit)) {
                planner.selectUnit(unit);
                gui.refresh();
            }
        });
        deploySystemBox.addActionListener(e -> {
            if (!updating && (deploySystemBox.getSelectedItem() instanceof MovementSystem system)) {
                planner.setDeploySystem(system);
                gui.refresh();
            }
        });
        deploySpeedBox.addActionListener(e -> {
            if (!updating && (deploySpeedBox.getSelectedItem() instanceof SpeedState speed)) {
                planner.setDeploySpeed(speed);
                gui.refresh();
            }
        });
        placeButton.addActionListener(e -> gui.placeUnit());
        return wrap(panel);
    }

    private JComponent activatePanel() {
        JPanel panel = column();
        JPanel movePanel = column();
        movePanel.setBorder(BorderFactory.createTitledBorder("Move"));
        movePanel.add(row(new JLabel("Unit:"), activateUnitBox));
        JPanel arrows = new JPanel(new GridLayout(2, 3, 3, 3));
        arrows.add(turnLeftButton);
        arrows.add(forwardButton);
        arrows.add(turnRightButton);
        arrows.add(undoButton);
        arrows.add(backwardButton);
        arrows.add(clearButton);
        movePanel.add(row(arrows));
        movePanel.add(row(switchButton));
        movePanel.add(moveLabel);
        movePanel.add(row(speedBox));
        panel.add(movePanel);

        JPanel firePanel = column();
        firePanel.setBorder(BorderFactory.createTitledBorder("Fire (one shot per action)"));
        firePanel.add(targetLabel);
        firePanel.add(row(new JLabel("Weapon:"), weaponBox));
        firePanel.add(row(new JLabel("Rate of Fire:"), rofBox));
        firePanel.add(shotPreview);
        firePanel.add(row(fireButton));
        panel.add(firePanel);
        panel.add(row(doneButton));
        panel.add(note("<i>Plan the whole activation, then press End activation: the server carries it out and "
              + "rolls the dice. Yellow hexes show the path, the faded token where it ends. You can fire at any point "
              + "along the path.</i>"));

        activateUnitBox.addActionListener(e -> {
            if (!updating && (activateUnitBox.getSelectedItem() instanceof HGUnit unit)) {
                planner.selectUnit(unit);
                gui.refresh();
            }
        });
        turnLeftButton.addActionListener(e -> gui.addStep(HGStep.turnLeft()));
        forwardButton.addActionListener(e -> gui.addStep(HGStep.forward()));
        turnRightButton.addActionListener(e -> gui.addStep(HGStep.turnRight()));
        backwardButton.addActionListener(e -> gui.addStep(HGStep.backward()));
        switchButton.addActionListener(e -> {
            MovementSystem other = otherSystem();
            if (other != null) {
                gui.addStep(HGStep.switchTo(other));
            }
        });
        undoButton.addActionListener(e -> {
            planner.undo();
            gui.clearMessage();
            gui.refresh();
        });
        clearButton.addActionListener(e -> {
            planner.clearSteps();
            gui.clearMessage();
            gui.refresh();
        });
        speedBox.addActionListener(e -> {
            if (!updating) {
                planner.setChangeSpeed(speedBox.isSelected());
                gui.refresh();
            }
        });
        weaponBox.addActionListener(e -> {
            if (!updating && (weaponBox.getSelectedIndex() >= 0)) {
                planner.setWeaponIndex(weaponBox.getSelectedIndex());
                gui.refresh();
            }
        });
        rofBox.addActionListener(e -> {
            if (!updating && (rofBox.getSelectedIndex() >= 0)) {
                planner.setRateOfFire(rofBox.getSelectedIndex());
                gui.refresh();
            }
        });
        fireButton.addActionListener(e -> gui.addStep(planner.shot()));
        doneButton.addActionListener(e -> gui.endActivation());
        doneButton.setFont(doneButton.getFont().deriveFont(Font.BOLD));
        return wrap(panel);
    }

    // endregion

    // region Refreshing

    /** Shows a refusal or a hint in red under the turn line. */
    void showMessage(@Nullable String message) {
        messageLabel.setText(((message == null) || message.isBlank()) ? ""
              : "<font color='#e05050'><b>" + message + "</b></font>");
    }

    /** Updates everything from the game and the plan. Call on the Swing thread. */
    void refresh() {
        updating = true;
        try {
            HGGame game = planner.game();
            boolean myTurn = gui.getClient().isMyTurn();
            phaseLabel.setText(phaseText(game));
            turnLabel.setText(turnText(game, myTurn));
            HGUnit shown = (planner.unit() != null) ? planner.unit() : gui.inspectedUnit();
            unitCard.setText((shown == null)
                  ? "<i>Point at a unit on the map to see its details, or click it to keep them here.</i>"
                  : HGUnitCard.html(shown, game, true));
            if (myTurn && game.getPhase().isDeployment()) {
                cards.show(cardPanel, DEPLOY);
                refreshDeploy();
            } else if (myTurn && game.getPhase().isMovement()) {
                cards.show(cardPanel, ACTIVATE);
                refreshActivate();
            } else {
                cards.show(cardPanel, WAITING);
                waitingLabel.setText(waitingText(game));
            }
        } finally {
            updating = false;
        }
    }

    private void refreshDeploy() {
        setItems(deployUnitBox, planner.eligibleUnits(), planner.unit());
        HGUnit unit = planner.unit();
        deployFacingLabel.setText(" " + FACINGS.get(planner.deployFacing()) + " ");
        deploySystemBox.removeAllItems();
        if (unit != null) {
            unit.movementModes().stream().map(HGMovementMode::system).forEach(deploySystemBox::addItem);
            deploySystemBox.setSelectedItem(planner.deploySystem());
        }
        deploySpeedBox.setSelectedItem(planner.deploySpeed());
        placeButton.setEnabled((unit != null) && (planner.deployCoords() != null));
        placeButton.setText((planner.deployCoords() == null)
              ? "Place unit (click a hex first)"
              : "Place in hex " + planner.deployCoords().getBoardNum());
    }

    private void refreshActivate() {
        setItems(activateUnitBox, planner.eligibleUnits(), planner.unit());
        activateUnitBox.setEnabled(planner.steps().isEmpty());
        ActivationState state = planner.state();
        HGUnit unit = planner.unit();
        boolean ready = (state != null) && (unit != null);
        enableStep(turnLeftButton, state, HGStep.turnLeft());
        enableStep(forwardButton, state, HGStep.forward());
        enableStep(turnRightButton, state, HGStep.turnRight());
        enableStep(backwardButton, state, HGStep.backward());
        MovementSystem other = otherSystem();
        switchButton.setVisible(other != null);
        if (other != null) {
            switchButton.setText("Switch to " + other);
            enableStep(switchButton, state, HGStep.switchTo(other));
        }
        undoButton.setEnabled(!planner.steps().isEmpty());
        clearButton.setEnabled(!planner.steps().isEmpty());
        doneButton.setEnabled(ready);

        if (!ready) {
            moveLabel.setText("Click one of your units on the map to activate it.");
            speedBox.setVisible(false);
            refreshFire(null, null);
            return;
        }
        StringBuilder move = new StringBuilder();
        move.append("MP spent <b>").append(state.mpSpent()).append("</b> of ").append(state.mpPool())
              .append(", ").append(state.hexesMoved()).append((state.hexesMoved() == 1) ? " hex" : " hexes")
              .append(" moved<br>");
        move.append("Speed: <b>").append(state.band()).append("</b> (")
              .append(HGUnitCard.signed(state.band().attackModifier())).append(" to its attacks)<br>");
        move.append("Facing ").append(FACINGS.get(state.facing())).append(" on ").append(state.system());
        if (state.actions() == 0) {
            move.append("<br><font color='#c03030'>The crew is stunned: no action this round.</font>");
        }
        String warning = state.topSpeedWarning();
        if (warning != null) {
            move.append("<br><font color='#c03030'>").append(warning).append("</font>");
        }
        if (state.sharpTurnPending()) {
            move.append("<br><font color='#c06000'>A three-hexside turn at Top Speed: the server rolls a Piloting "
                  + "test (needs ").append(state.sharpTurnThreshold()).append(").</font>");
        }
        moveLabel.setText(move.toString());

        speedBox.setVisible(unit.canMove());
        if (state.isAtTopSpeed()) {
            speedBox.setText("Slow down to Combat Speed next round");
        } else {
            speedBox.setText("<html>Shift up to Top Speed next round<br><small>(spend exactly all "
                  + state.startCombatMP() + " Combat MP)</small></html>");
        }
        speedBox.setEnabled(state.canChangeSpeed());
        speedBox.setSelected(planner.isChangeSpeed());
        refreshFire(state, unit);
    }

    private void refreshFire(@Nullable ActivationState state, @Nullable HGUnit unit) {
        HGUnit target = planner.target();
        weaponBox.removeAllItems();
        rofBox.removeAllItems();
        if ((state == null) || (unit == null)) {
            targetLabel.setText("");
            shotPreview.setText("");
            fireButton.setEnabled(false);
            return;
        }
        for (HGWeapon weapon : unit.weapons()) {
            weaponBox.addItem(weaponLabel(weapon));
        }
        weaponBox.setSelectedIndex(Math.min(planner.weaponIndex(), unit.weapons().size() - 1));
        HGWeapon weapon = unit.weapon(planner.weaponIndex());
        int maxRof = (weapon == null) ? 0 : Math.max(0, weapon.maxUsableRateOfFire());
        for (int rof = 0; rof <= maxRof; rof++) {
            rofBox.addItem((rof == 0) ? "Single shot" : "Burst, ROF " + rof + " (x"
                  + AttackRules.damageMultiplier(weapon, rof) + ", " + weapon.ammoNeeded(rof)
                  + ((weapon.category() == HGWeapon.Category.ROCKET) ? " rockets)" : " rounds)"));
        }
        rofBox.setSelectedIndex(Math.min(planner.rateOfFire(), maxRof));

        if (target == null) {
            targetLabel.setText("Target: <i>click an enemy unit on the map</i>");
        } else {
            int distance = state.position().distance(target.position());
            targetLabel.setText("Target: <b>" + HGUnitCard.escape(target.displayName()) + "</b>, " + distance
                  + ((distance == 1) ? " hex" : " hexes"));
        }
        String problem = planner.checkShot();
        if (planner.hasFired()) {
            shotPreview.setText("<i>The shot is in the plan. Undo takes it back.</i>");
            fireButton.setEnabled(false);
        } else if (problem != null) {
            shotPreview.setText(HGUnitCard.escape(problem));
            fireButton.setEnabled(false);
        } else {
            shotPreview.setText(shotText(state, unit, weapon, target));
            fireButton.setEnabled(true);
        }
    }

    private String shotText(ActivationState state, HGUnit unit, HGWeapon weapon, HGUnit target) {
        RangeBand band = AttackRules.rangeBand(weapon, state.position(), target);
        List<RollModifier> attack = planner.attackModifiers();
        List<RollModifier> defense = planner.defenseModifiers();
        int attackBonus = unit.crewLevel().gunnery().bonus() + RollModifier.sum(attack);
        int defenseBonus = target.isImmobile() ? 0 : target.crewLevel().piloting().bonus() + RollModifier.sum(defense);
        int multiplier = AttackRules.damageMultiplier(weapon, planner.rateOfFire());
        StringBuilder text = new StringBuilder();
        text.append("<b>").append(band).append(" range.</b><br>");
        text.append("Attack: Gunnery ").append(unit.crewLevel().gunnery().dice()).append("d6 ")
              .append(HGUnitCard.signed(attackBonus)).append(" (skill ")
              .append(HGUnitCard.signed(unit.crewLevel().gunnery().bonus())).append(", ")
              .append(attack.stream().map(RollModifier::toString).collect(Collectors.joining(", ")))
              .append(")<br>");
        if (target.isImmobile()) {
            text.append("Defense: 0, the target cannot move.<br>");
        } else {
            text.append("Defense: Piloting ").append(target.crewLevel().piloting().dice()).append("d6 ")
                  .append(HGUnitCard.signed(defenseBonus)).append(" (skill ")
                  .append(HGUnitCard.signed(target.crewLevel().piloting().bonus())).append(", ")
                  .append(defense.stream().map(RollModifier::toString).collect(Collectors.joining(", ")))
                  .append(")<br>");
        }
        text.append("A hit deals Margin of Success x ").append(multiplier).append(" against armor ")
              .append(target.armorThresholds()).append(" (Light, Heavy, destroyed).");
        if (weapon.areaEffectRadius() >= 0) {
            text.append("<br>Area effect: everyone in the blast defends separately.");
        }
        text.append("<br><i>The speed bonus is for the move so far; moving on after firing can change it.</i>");
        return text.toString();
    }

    private static String weaponLabel(HGWeapon weapon) {
        StringBuilder label = new StringBuilder(weapon.code());
        label.append(" x").append(weapon.damageMultiplier());
        if (weapon.ammo() != null) {
            label.append(", ammo ").append(weapon.ammo());
        }
        if (!weapon.isRanged()) {
            label.append(" (close combat, later)");
        } else if (weapon.isOutOfAction()) {
            label.append(" (out of action)");
        }
        return label.toString();
    }

    private void enableStep(JButton button, @Nullable ActivationState state, HGStep step) {
        String problem = (state == null) ? "Pick a unit first." : state.check(step);
        button.setEnabled(problem == null);
        button.setToolTipText(problem);
    }

    private @Nullable MovementSystem otherSystem() {
        ActivationState state = planner.state();
        HGUnit unit = planner.unit();
        if ((state == null) || (unit == null)) {
            return null;
        }
        return unit.movementModes().stream().map(HGMovementMode::system)
              .filter(system -> system != state.system())
              .findFirst().orElse(null);
    }

    private String phaseText(HGGame game) {
        String round = (game.getCurrentRound() > 0) ? "Round " + game.getCurrentRound() + " · " : "";
        return switch (game.getPhase()) {
            case STARTING_SCENARIO, EXCHANGE, LOUNGE -> "Getting ready";
            case DEPLOYMENT -> "Set-up";
            case MOVEMENT -> round + "Activations";
            case VICTORY -> "The battle is over";
            default -> round + game.getPhase();
        };
    }

    private String turnText(HGGame game, boolean myTurn) {
        if (game.getPhase() == GamePhase.VICTORY) {
            return " ";
        } else if (myTurn) {
            return game.getPhase().isDeployment() ? "Your turn: place a unit." : "Your turn: activate a unit.";
        }
        return game.currentPlayer().map(player -> "Waiting for " + player.getName() + ".").orElse(" ");
    }

    private String waitingText(HGGame game) {
        StringBuilder text = new StringBuilder();
        if (game.getPhase().isStartingScenario()) {
            text.append("Waiting for every side to have a player.<br><br>The other players start HeavyMek, choose "
                  + "<b>Connect to a Game</b> and enter the name of their side:<ul>");
            for (Player player : game.getPlayersList()) {
                text.append("<li>").append(HGUnitCard.escape(player.getName()))
                      .append(player.isGhost() ? " (not here yet)" : " (here)").append("</li>");
            }
            text.append("</ul>");
        } else if (game.getPhase().isVictory()) {
            String result = game.battleLog().stream().filter(line -> line.startsWith("***")).reduce((a, b) -> b)
                  .orElse("The battle is over.");
            text.append("<b>").append(HGUnitCard.escape(result.replace("*", "").trim())).append("</b><br><br>")
                  .append("Close this window to return to the main menu.");
        } else {
            text.append("The other side is playing. Their moves and every die roll appear in the battle log below.");
        }
        return text.toString();
    }

    private void changeDeployFacing(int hexsides) {
        planner.turnDeployFacing(hexsides);
        gui.refresh();
    }

    // endregion

    // region Small helpers

    /** @return a block of text that wraps at the panel's width */
    private static JEditorPane note(String html) {
        JEditorPane pane = htmlPane();
        pane.setText(html);
        return pane;
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 16;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return visibleRect.height - 32;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    private static <T> void setItems(JComboBox<T> box, List<T> items, @Nullable T selected) {
        box.removeAllItems();
        items.forEach(box::addItem);
        if ((selected != null) && items.contains(selected)) {
            box.setSelectedItem(selected);
        }
    }

    private static JComboBox<HGUnit> unitBox() {
        JComboBox<HGUnit> box = new JComboBox<>();
        box.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                  boolean isSelected, boolean cellHasFocus) {
                Object shown = (value instanceof HGUnit unit) ? unit.displayName() : value;
                return super.getListCellRendererComponent(list, shown, index, isSelected, cellHasFocus);
            }
        });
        return box;
    }

    private static JEditorPane htmlPane() {
        JEditorPane pane = new JEditorPane("text/html", "");
        pane.setEditable(false);
        pane.setOpaque(false);
        pane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        pane.setAlignmentX(Component.LEFT_ALIGNMENT);
        return pane;
    }

    private static JPanel column() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }

    private static JPanel row(JComponent... components) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        for (JComponent component : components) {
            row.add(component);
        }
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        return row;
    }

    private static JComponent wrap(JPanel panel) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(panel, BorderLayout.NORTH);
        return wrapper;
    }

    // endregion
}
