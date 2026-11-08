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

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.Nullable;
import megamek.client.HGClient;
import megamek.common.Player;
import megamek.common.board.Coords;
import megamek.common.heavyGear.ActivationState;
import megamek.common.heavyGear.AttackRules;
import megamek.common.heavyGear.HGActivation;
import megamek.common.heavyGear.HGGame;
import megamek.common.heavyGear.HGStep;
import megamek.common.heavyGear.HGUnit;
import megamek.common.heavyGear.HGWeapon;
import megamek.common.heavyGear.MovementSystem;
import megamek.common.heavyGear.RollModifier;
import megamek.common.heavyGear.SpeedState;

/**
 * The player's plan for the current turn, kept on their own computer until they send it: which unit, the steps of
 * its activation, the chosen target and weapon, or where to place a unit at set-up. It holds no Swing code, so the
 * screen (and the tests) only ask it questions and tell it about clicks.
 *
 * <p>Every change goes through {@link ActivationState}, the same rules code the server uses, so a plan the screen
 * accepts is a plan the server accepts.</p>
 */
public class HGPlanner {

    private final HGClient client;

    private int unitId = -1;
    private final List<HGStep> steps = new ArrayList<>();
    private boolean changeSpeed;
    private int targetId = -1;
    private int weaponIndex = 0;
    private int rateOfFire = 0;

    private @Nullable Coords deployCoords;
    private int deployFacing;
    private MovementSystem deploySystem = MovementSystem.WALKER;
    private SpeedState deploySpeed = SpeedState.COMBAT;

    public HGPlanner(HGClient client) {
        this.client = client;
    }

    public HGGame game() {
        return client.getGame();
    }

    // region The unit

    /** @return the unit being placed or activated, or null */
    public @Nullable HGUnit unit() {
        return game().unit(unitId);
    }

    /**
     * Picks a unit and starts a fresh plan for it.
     */
    public void selectUnit(@Nullable HGUnit unit) {
        unitId = (unit == null) ? -1 : unit.getId();
        steps.clear();
        changeSpeed = false;
        targetId = -1;
        weaponIndex = firstUsableWeapon(unit);
        rateOfFire = 0;
        deployCoords = null;
        if (unit != null) {
            deploySystem = unit.movementSystem();
            deployFacing = defaultDeployFacing();
        }
    }

    /** Forgets everything; used when a turn ends. */
    public void reset() {
        selectUnit(null);
    }

    /** @return true when the unit is the local player's and may take the current turn */
    public boolean isEligible(@Nullable HGUnit unit) {
        return (unit != null) && client.isMyTurn() && (game().getTurn() != null)
              && game().getTurn().isValidEntity(unit, game());
    }

    /** @return the local player's units that may take the current turn */
    public List<HGUnit> eligibleUnits() {
        return game().units().stream().filter(this::isEligible).toList();
    }

    // endregion

    // region Activation

    /** @return the plan replayed from the start; null when no unit is selected */
    public @Nullable ActivationState state() {
        HGUnit unit = unit();
        if ((unit == null) || !unit.isDeployed()) {
            return null;
        }
        ActivationState state = new ActivationState(game(), unit);
        steps.forEach(state::tryApply);
        return state;
    }

    public List<HGStep> steps() {
        return List.copyOf(steps);
    }

    /**
     * Adds a step to the plan if the rules allow it.
     *
     * @return why the step isn't allowed, or null when it was added
     */
    public @Nullable String addStep(HGStep step) {
        ActivationState state = state();
        if (state == null) {
            return "Pick a unit first.";
        }
        String problem = state.tryApply(step);
        if (problem == null) {
            steps.add(step);
            if (changeSpeed && !state.canChangeSpeed()) {
                changeSpeed = false;
            }
        }
        return problem;
    }

    /** Takes back the last step. */
    public void undo() {
        if (!steps.isEmpty()) {
            steps.remove(steps.size() - 1);
        }
        ActivationState state = state();
        if (changeSpeed && ((state == null) || !state.canChangeSpeed())) {
            changeSpeed = false;
        }
    }

    /** Takes back every step. */
    public void clearSteps() {
        steps.clear();
        changeSpeed = false;
    }

    /** @return true when the plan includes a shot */
    public boolean hasFired() {
        return steps.stream().anyMatch(step -> step.type() == HGStep.Type.FIRE);
    }

    public boolean isChangeSpeed() {
        return changeSpeed;
    }

    public void setChangeSpeed(boolean changeSpeed) {
        ActivationState state = state();
        this.changeSpeed = changeSpeed && (state != null) && state.canChangeSpeed();
    }

    /** @return the finished plan, ready to send */
    public HGActivation activation() {
        return new HGActivation(unitId, steps, changeSpeed);
    }

    // endregion

    // region Shooting

    public @Nullable HGUnit target() {
        return game().unit(targetId);
    }

    /** Chooses the unit to shoot at; only enemies still in the fight count. */
    public void setTarget(@Nullable HGUnit target) {
        HGUnit unit = unit();
        targetId = ((target == null) || (unit == null) || !game().areEnemies(unit, target) || target.isOutOfFight())
              ? -1 : target.getId();
    }

    public int weaponIndex() {
        return weaponIndex;
    }

    public void setWeaponIndex(int index) {
        weaponIndex = index;
        HGUnit unit = unit();
        HGWeapon weapon = (unit == null) ? null : unit.weapon(index);
        rateOfFire = (weapon == null) ? 0 : Math.min(rateOfFire, Math.max(0, weapon.maxUsableRateOfFire()));
    }

    public int rateOfFire() {
        return rateOfFire;
    }

    public void setRateOfFire(int rateOfFire) {
        this.rateOfFire = Math.max(0, rateOfFire);
    }

    /** @return the shot as it would be added now, at the end of the plan so far */
    public HGStep shot() {
        return HGStep.fire(weaponIndex, targetId, rateOfFire);
    }

    /** @return why the shot can't be taken from where the plan stands now, or null when it can */
    public @Nullable String checkShot() {
        ActivationState state = state();
        if (state == null) {
            return "Pick a unit first.";
        } else if (targetId < 0) {
            return "Click an enemy unit on the map to target it.";
        }
        return state.check(shot());
    }

    /** @return the attack roll's modifiers for the shot from where the plan stands now, besides the Gunnery bonus */
    public List<RollModifier> attackModifiers() {
        ActivationState state = state();
        HGUnit unit = unit();
        HGUnit target = target();
        HGWeapon weapon = (unit == null) ? null : unit.weapon(weaponIndex);
        if ((state == null) || (target == null) || (weapon == null)) {
            return List.of();
        }
        return AttackRules.attackModifiers(unit, weapon, state.position(), target, state.band());
    }

    /** @return the target's defense modifiers against a shot from where the plan stands now */
    public List<RollModifier> defenseModifiers() {
        ActivationState state = state();
        HGUnit target = target();
        if ((state == null) || (target == null)) {
            return List.of();
        }
        return AttackRules.defenseModifiers(target, state.position());
    }

    private static int firstUsableWeapon(@Nullable HGUnit unit) {
        if (unit == null) {
            return 0;
        }
        for (int i = 0; i < unit.weapons().size(); i++) {
            HGWeapon weapon = unit.weapons().get(i);
            if (weapon.isRanged() && !weapon.isOutOfAction() && (weapon.maxUsableRateOfFire() >= 0)) {
                return i;
            }
        }
        return 0;
    }

    // endregion

    // region Set-up

    public @Nullable Coords deployCoords() {
        return deployCoords;
    }

    /**
     * @return why the unit can't be placed in this hex, or null when it can
     */
    public @Nullable String checkDeployHex(Coords coords) {
        HGUnit unit = unit();
        Player player = game().getPlayer(client.getLocalPlayerNumber());
        if ((unit == null) || (player == null)) {
            return "Pick a unit first.";
        } else if (!game().board().contains(coords)) {
            return "That hex isn't on the map.";
        } else if (!game().board().isLegalDeployment(coords, player)) {
            return "That hex is outside your deployment zone.";
        } else if (game().sizeInHex(coords, unit.getId()) + unit.datacard().size() > HGGame.STACKING_LIMIT) {
            return "That hex is full.";
        }
        return null;
    }

    /** @return true when the hex is in the local player's deployment zone */
    public boolean isInDeploymentZone(Coords coords) {
        Player player = game().getPlayer(client.getLocalPlayerNumber());
        return (player != null) && game().board().isLegalDeployment(coords, player);
    }

    public @Nullable String setDeployCoords(Coords coords) {
        String problem = checkDeployHex(coords);
        if (problem == null) {
            deployCoords = coords;
        }
        return problem;
    }

    public int deployFacing() {
        return deployFacing;
    }

    public void turnDeployFacing(int hexsides) {
        deployFacing = Math.floorMod(deployFacing + hexsides, 6);
    }

    public MovementSystem deploySystem() {
        return deploySystem;
    }

    public void setDeploySystem(MovementSystem system) {
        deploySystem = system;
    }

    public SpeedState deploySpeed() {
        return deploySpeed;
    }

    public void setDeploySpeed(SpeedState speed) {
        deploySpeed = speed;
    }

    /** @return a hex in the middle of the local player's deployment zone, or null without a zone */
    public @Nullable Coords deploymentZoneCentre() {
        int middle = game().board().getWidth() / 2;
        List<Coords> zone = new ArrayList<>();
        for (int y = 0; y < game().board().getHeight(); y++) {
            Coords coords = new Coords(middle, y);
            if (isInDeploymentZone(coords)) {
                zone.add(coords);
            }
        }
        return zone.isEmpty() ? null : zone.get(zone.size() / 2);
    }

    /** Units placed along the north edge face south and the other way round, so they face the enemy. */
    private int defaultDeployFacing() {
        Player player = game().getPlayer(client.getLocalPlayerNumber());
        if (player == null) {
            return 0;
        }
        int height = game().board().getHeight();
        int middle = game().board().getWidth() / 2;
        for (int y = 0; y < height; y++) {
            if (game().board().isLegalDeployment(new Coords(0, y), player)
                  || game().board().isLegalDeployment(new Coords(middle, y), player)) {
                return (y < height / 2) ? 3 : 0;
            }
        }
        return 0;
    }

    // endregion
}
