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
import java.util.List;

import jakarta.annotation.Nullable;
import megamek.common.board.Coords;

/**
 * Follows one unit through its activation step by step and knows which steps are allowed: the movement rules of
 * notes 01 sections 5.1-5.7 (B pp.130-133). The player's screen uses it to build a plan and the server uses it to
 * check and carry out that plan, so both always agree on the rules.
 *
 * <p>The rules it applies:</p>
 * <ul>
 *     <li><b>MP:</b> at Combat Speed the unit has its Combat MP, at Top Speed its Top MP. Entering a hex costs
 *     1 MP (the board is open ground until terrain arrives in Stage 3). A hex costing more MP than are left can't be
 *     entered (rules README).</li>
 *     <li><b>Turning:</b> all the turning a unit does in one spot counts as one turn, priced by how far it ends up
 *     from its old facing: one hexside is free, two or three cost 1 MP. After turning the unit must enter a new hex
 *     before it can turn again, so turning, firing and turning back in the same spot isn't allowed.</li>
 *     <li><b>Reversing:</b> backing up one hex at a time, keeping the facing, is only allowed if the unit spends no
 *     more than half its Combat MP, and never at Top Speed.</li>
 *     <li><b>Top Speed:</b> the unit must spend more than its Combat MP. A turn of three hexsides at Top Speed
 *     needs a Piloting test (the server rolls it; see {@link #sharpTurnPending()}).</li>
 *     <li><b>Changing movement system:</b> once per round, not at Top Speed. The MP left afterwards are the smaller
 *     of the old pool and the new system's Combat MP, minus what is already spent.</li>
 *     <li><b>Stacking:</b> a hex holds at most 30 Size points of units; friend and foe may share a hex.</li>
 *     <li><b>Firing:</b> one shot per action; a Gear has one action.</li>
 * </ul>
 */
public final class ActivationState {

    private final HGGame game;
    private final HGUnit unit;
    private final boolean atTopSpeed;
    private final int startCombatMP;
    private final int actions;
    private final List<HGStep> steps = new ArrayList<>();

    private Coords position;
    private int facing;
    private MovementSystem system;
    private int mpPool;
    private int mpSpent;
    private int hexesMoved;
    private int turnStartFacing;
    private int turnCost;
    private boolean turningLocked;
    private boolean sharpTurnResolved;
    private boolean reversed;
    private boolean systemChanged;
    private boolean movementEnded;
    private int actionsUsed;

    /**
     * Starts an activation from the unit's current state.
     *
     * @param game    the game, for the map and the other units
     * @param unit    the unit activating
     * @param actions the actions the unit may use this activation
     */
    public ActivationState(HGGame game, HGUnit unit, int actions) {
        this.game = game;
        this.unit = unit;
        this.actions = actions;
        atTopSpeed = unit.speedState().isTop();
        HGMovementMode mode = unit.currentMovementMode();
        startCombatMP = mode.combat();
        mpPool = unit.canMove() ? (atTopSpeed ? mode.top() : mode.combat()) : 0;
        position = unit.position();
        facing = unit.facing();
        system = unit.movementSystem();
        turnStartFacing = facing;
    }

    /** Starts an activation with the actions the unit has available right now. */
    public ActivationState(HGGame game, HGUnit unit) {
        this(game, unit, unit.actionsAvailable());
    }

    // region Checking and applying steps

    /**
     * Checks a step without changing anything.
     *
     * @return why the step isn't allowed, or null when it is
     */
    public @Nullable String check(HGStep step) {
        return switch (step.type()) {
            case FORWARD, BACKWARD -> checkMove(step.type() == HGStep.Type.BACKWARD);
            case TURN_LEFT, TURN_RIGHT -> checkTurn(step.type() == HGStep.Type.TURN_RIGHT);
            case SWITCH_SYSTEM -> checkSwitch(step.system());
            case FIRE -> checkFire(step);
        };
    }

    /**
     * Applies a step after checking it.
     *
     * @return why the step isn't allowed (and nothing changed), or null when it was applied
     */
    public @Nullable String tryApply(HGStep step) {
        String problem = check(step);
        if (problem == null) {
            apply(step);
        }
        return problem;
    }

    private void apply(HGStep step) {
        switch (step.type()) {
            case FORWARD, BACKWARD -> {
                boolean backward = (step.type() == HGStep.Type.BACKWARD);
                position = destination(backward);
                mpSpent += moveCost();
                hexesMoved++;
                reversed |= backward;
                closeTurnPoint();
            }
            case TURN_LEFT, TURN_RIGHT -> {
                facing = Math.floorMod(facing + ((step.type() == HGStep.Type.TURN_RIGHT) ? 1 : -1), 6);
                int newCost = turnCostFor(facing);
                mpSpent += newCost - turnCost;
                turnCost = newCost;
            }
            case SWITCH_SYSTEM -> {
                HGMovementMode newMode = unit.movementMode(step.system());
                system = step.system();
                systemChanged = true;
                if (newMode != null) {
                    mpPool = Math.min(mpPool, newMode.combat());
                }
            }
            case FIRE -> {
                actionsUsed++;
                turningLocked = true;
            }
        }
        steps.add(step);
    }

    private @Nullable String checkMove(boolean backward) {
        String problem = checkCanMove();
        if (problem != null) {
            return problem;
        }
        if (backward && atTopSpeed) {
            return "No reversing at Top Speed.";
        }
        Coords destination = destination(backward);
        if (!game.board().contains(destination)) {
            return "That's the edge of the map.";
        }
        if (game.sizeInHex(destination, unit.getId()) + unit.datacard().size() > HGGame.STACKING_LIMIT) {
            return "That hex is full: at most " + HGGame.STACKING_LIMIT + " Size points of units fit in one hex.";
        }
        int newSpent = mpSpent + moveCost();
        if (newSpent > mpPool) {
            return "Not enough MP left.";
        }
        if ((backward || reversed) && !MovementBand.isHalfCombat(newSpent, startCombatMP)) {
            return "Reversing is only allowed at Half Combat Speed: at most " + (startCombatMP / 2) + " MP.";
        }
        return null;
    }

    private @Nullable String checkTurn(boolean right) {
        String problem = checkCanMove();
        if (problem != null) {
            return problem;
        }
        if (turningLocked) {
            return "After firing, the unit has to move a hex before it can turn again.";
        }
        int newFacing = Math.floorMod(facing + (right ? 1 : -1), 6);
        int newSpent = mpSpent - turnCost + turnCostFor(newFacing);
        if (newSpent > mpPool) {
            return "Not enough MP left to turn that far.";
        }
        if (reversed && !MovementBand.isHalfCombat(newSpent, startCombatMP)) {
            return "Reversing is only allowed at Half Combat Speed: at most " + (startCombatMP / 2) + " MP.";
        }
        return null;
    }

    private @Nullable String checkCanMove() {
        if (movementEnded) {
            return unit.shortName() + "'s movement has ended.";
        } else if (!unit.canMove()) {
            return unit.shortName() + " cannot move.";
        } else if (mpPool <= 0) {
            return "No MP this round.";
        }
        return null;
    }

    private @Nullable String checkSwitch(@Nullable MovementSystem newSystem) {
        HGMovementMode newMode = (newSystem == null) ? null : unit.movementMode(newSystem);
        if (newMode == null) {
            return unit.shortName() + " has no such movement system.";
        } else if (newSystem == system) {
            return "That system is already in use.";
        } else if (systemChanged) {
            return "A unit may change movement system only once per round.";
        } else if (atTopSpeed) {
            return "No changing movement system at Top Speed.";
        } else if (newMode.isKnockedOut()) {
            return "The " + newSystem + " system is knocked out.";
        } else if (!unit.canMove() || movementEnded) {
            return unit.shortName() + " cannot move.";
        }
        return null;
    }

    private @Nullable String checkFire(HGStep step) {
        if (actionsUsed >= actions) {
            return (actions == 0) ? unit.shortName() + " has no action this round." : "No actions left this round.";
        }
        HGWeapon weapon = unit.weapon(step.weaponIndex());
        return AttackRules.checkAttack(game, unit, position, facing, weapon, step.rateOfFire(),
              game.unit(step.targetId()));
    }

    // endregion

    // region What the server needs for random events

    /**
     * @return true when the turn being made in the current spot is a sharp turn at Top Speed (three hexsides) whose
     *       Piloting test hasn't been rolled yet (notes 01 section 5.7; B p.133)
     */
    public boolean sharpTurnPending() {
        return atTopSpeed && !sharpTurnResolved
              && (HexGeometry.hexsideDifference(turnStartFacing, facing) >= 3);
    }

    /** @return the Threshold of the sharp-turn test: 3 plus the MP cost of the hex the unit turns in */
    public int sharpTurnThreshold() {
        return 3 + moveCost();
    }

    /** @return the facing the unit had before the turn being made in the current spot */
    public int facingBeforeTurn() {
        return turnStartFacing;
    }

    /**
     * Records the sharp-turn test as rolled. After a failed test the unit first skids one hex straight ahead in
     * its old facing, then completes the turn; the skid costs no MP but counts as a hex moved.
     *
     * @param skidTo the hex the unit skidded into, or null when it didn't skid
     */
    public void resolveSharpTurn(@Nullable Coords skidTo) {
        sharpTurnResolved = true;
        if (skidTo != null) {
            position = skidTo;
            hexesMoved++;
        }
    }

    /**
     * @return the hex a skid would end in, or null when the unit can't skid there (off the map or a full hex).
     *       Skidding into another unit would be a ram, which comes with the close combat rules; until then the unit
     *       simply doesn't skid.
     */
    public @Nullable Coords skidDestination() {
        Coords skid = position.translated(turnStartFacing);
        boolean fits = game.sizeInHex(skid, unit.getId()) + unit.datacard().size() <= HGGame.STACKING_LIMIT;
        return (game.board().contains(skid) && game.unitsAt(skid).isEmpty() && fits) ? skid : null;
    }

    /** Ends the unit's movement early, after a crash. It may still fire. */
    public void endMovement() {
        movementEnded = true;
    }

    // endregion

    // region The state so far

    public HGUnit unit() {
        return unit;
    }

    public Coords position() {
        return position;
    }

    public int facing() {
        return facing;
    }

    /** @return the movement system in use at this point */
    public MovementSystem system() {
        return system;
    }

    public int mpSpent() {
        return mpSpent;
    }

    /** @return the MP the unit may spend this activation */
    public int mpPool() {
        return mpPool;
    }

    public int mpLeft() {
        return Math.max(0, mpPool - mpSpent);
    }

    public int hexesMoved() {
        return hexesMoved;
    }

    public boolean isAtTopSpeed() {
        return atTopSpeed;
    }

    /** @return the Combat MP the unit started the activation with */
    public int startCombatMP() {
        return startCombatMP;
    }

    /** @return the speed band so far, which sets the unit's own attack modifier */
    public MovementBand band() {
        return MovementBand.of(atTopSpeed, mpSpent, startCombatMP);
    }

    public int actions() {
        return actions;
    }

    public int actionsUsed() {
        return actionsUsed;
    }

    /** @return the steps applied so far */
    public List<HGStep> steps() {
        return List.copyOf(steps);
    }

    /** @return true when the unit has reversed this activation */
    public boolean hasReversed() {
        return reversed;
    }

    /** @return true when the unit changed movement system this activation */
    public boolean hasChangedSystem() {
        return systemChanged;
    }

    /** @return true when turning is blocked until the unit enters a new hex */
    public boolean isTurningLocked() {
        return turningLocked;
    }

    /**
     * @return true when the unit may change speed after this move: shifting up needs exactly all the Combat MP
     *       spent; shifting down is always allowed at Top Speed
     */
    public boolean canChangeSpeed() {
        if (atTopSpeed) {
            return true;
        }
        return unit.canMove() && !movementEnded && !reversed && (mpSpent == startCombatMP) && (startCombatMP > 0);
    }

    /** @return the least MP a Top Speed unit has to spend this activation (more than its Combat MP) */
    public int minimumTopSpeedMP() {
        return startCombatMP + 1;
    }

    /**
     * @return a reminder when the plan breaks the Top Speed minimum, or null. A blocked unit may have no way to
     *       spend that much, so the screen only warns and the server accepts the plan.
     */
    public @Nullable String topSpeedWarning() {
        if (atTopSpeed && unit.canMove() && !movementEnded && (mpSpent < minimumTopSpeedMP())) {
            return "At Top Speed a unit must spend at least " + minimumTopSpeedMP() + " MP (Combat MP + 1).";
        }
        return null;
    }

    // endregion

    private Coords destination(boolean backward) {
        return position.translated(backward ? (facing + 3) % 6 : facing);
    }

    /** The MP to enter a hex. Every hex is open ground for now; terrain costs arrive in Stage 3. */
    private int moveCost() {
        return 1;
    }

    private int turnCostFor(int newFacing) {
        return (HexGeometry.hexsideDifference(turnStartFacing, newFacing) >= 2) ? 1 : 0;
    }

    private void closeTurnPoint() {
        turnStartFacing = facing;
        turnCost = 0;
        turningLocked = false;
        sharpTurnResolved = false;
    }
}
