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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import megamek.common.Player;
import jakarta.annotation.Nullable;
import megamek.common.board.Coords;
import megamek.common.game.InGameObject;

/**
 * One Heavy Gear unit on the map: its record sheet ({@link HGDatacard}), its crew, where it stands and everything
 * that has happened to it in the battle.
 *
 * <p>MegaMek keeps every playing piece as an {@link InGameObject}, the "console" side of the plug. Implementing that
 * interface is what lets MegaMek's server send Gears to the players and keep them in the game's unit list.</p>
 *
 * <p>The class is {@link Serializable}: Java can turn it into bytes to send it over the network to each player's
 * computer, where it is turned back into an object.</p>
 */
public class HGUnit implements InGameObject, Serializable {

    /** A unit that is out of the fight still stands on the map as a wreck; this says why it is out. */
    public enum Fate {
        FIGHTING, DESTROYED, CREW_LOST
    }

    private final HGDatacard datacard;
    private final ArrayList<HGWeapon> weapons = new ArrayList<>();
    private final ArrayList<HGMovementMode> movementModes = new ArrayList<>();

    private int id = -1;
    private int ownerId = Player.PLAYER_NONE;
    private String callsign = "";
    private CrewLevel crewLevel = CrewLevel.QUALIFIED;
    private int crew;

    private @Nullable Coords position;
    private int facing;
    private MovementSystem movementSystem;
    private SpeedState speedState = SpeedState.COMBAT;

    private boolean activated;
    private int hexesMovedThisRound;
    private int lastHexesMoved;
    private int stunnedActions;

    private int armor;
    private int maneuverPenalty;
    private boolean fireControlDestroyed;
    private int sensorsPenalty;
    private int communicationsPenalty;
    private boolean powerTransferFailure;
    private boolean ammoStoreRuptured;
    private Fate fate = Fate.FIGHTING;
    private String fateReason = "";

    /**
     * Makes a fresh, undamaged unit from a datacard.
     */
    public HGUnit(HGDatacard datacard) {
        this.datacard = datacard;
        datacard.weapons().forEach(weapon -> weapons.add(weapon.copy()));
        datacard.movement().forEach(mode -> movementModes.add(mode.copy()));
        crew = datacard.crew();
        armor = datacard.armor();
        movementSystem = datacard.primarySystem();
    }

    // region Identity

    public HGDatacard datacard() {
        return datacard;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int newId) {
        id = newId;
    }

    @Override
    public int getOwnerId() {
        return ownerId;
    }

    @Override
    public void setOwnerId(int newOwnerId) {
        ownerId = newOwnerId;
    }

    /** @return the name this unit goes by in the battle, set by the scenario; may be empty */
    public String callsign() {
        return callsign;
    }

    public void setCallsign(String callsign) {
        this.callsign = (callsign == null) ? "" : callsign.trim();
    }

    /** @return the callsign and the unit type, such as "Bear (Hunter)", or just the type without a callsign */
    public String displayName() {
        return callsign.isBlank() ? datacard.name() : callsign + " (" + datacard.name() + ")";
    }

    /** @return the shortest useful name: the callsign, or the unit type without one */
    public String shortName() {
        return callsign.isBlank() ? datacard.name() : callsign;
    }

    @Override
    public String generalName() {
        return datacard.name();
    }

    @Override
    public String specificName() {
        return callsign;
    }

    @Override
    public int getStrength() {
        return (int) Math.round(datacard.threatValue() * crewLevel.threatValueMultiplier());
    }

    // endregion

    // region Crew and actions

    public CrewLevel crewLevel() {
        return crewLevel;
    }

    public void setCrewLevel(CrewLevel crewLevel) {
        this.crewLevel = crewLevel;
    }

    /** @return the crew still aboard */
    public int crew() {
        return crew;
    }

    /**
     * @return the free actions per round: the datacard's value, lowered when casualties take the crew below the
     *       number that value needs (notes 01 section 4)
     */
    public int freeActions() {
        if (crew <= 0) {
            return 0;
        }
        int lost = CombatTables.freeActionsForCrew(datacard.crew()) - CombatTables.freeActionsForCrew(crew);
        return Math.max(0, datacard.actions() - lost);
    }

    /** @return actions lost to "crew stunned" results that have not been paid off yet */
    public int stunnedActions() {
        return stunnedActions;
    }

    // endregion

    // region Position and speed

    /** @return the hex this unit stands in, or null before it is deployed */
    public @Nullable Coords position() {
        return position;
    }

    public boolean isDeployed() {
        return position != null;
    }

    /** @return the facing, 0 (north) to 5, clockwise */
    public int facing() {
        return facing;
    }

    public void setPosition(Coords position, int facing) {
        this.position = position;
        this.facing = Math.floorMod(facing, 6);
    }

    /** @return the movement system in use */
    public MovementSystem movementSystem() {
        return movementSystem;
    }

    public void setMovementSystem(MovementSystem system) {
        if (movementMode(system) != null) {
            movementSystem = system;
        }
    }

    public SpeedState speedState() {
        return speedState;
    }

    public void setSpeedState(SpeedState speedState) {
        this.speedState = speedState;
    }

    /** @return every movement system with its current, possibly damaged, speeds */
    public List<HGMovementMode> movementModes() {
        return List.copyOf(movementModes);
    }

    /** @return the current speeds of a movement system, or null if the unit doesn't have it */
    public @Nullable HGMovementMode movementMode(MovementSystem system) {
        return movementModes.stream().filter(mode -> mode.system() == system).findFirst().orElse(null);
    }

    /** @return the speeds of the movement system in use */
    public HGMovementMode currentMovementMode() {
        HGMovementMode mode = movementMode(movementSystem);
        return (mode != null) ? mode : movementModes.get(0);
    }

    /**
     * Places the unit on the map at set-up.
     *
     * @param coords the hex
     * @param facing the facing, 0 to 5
     * @param system the movement system it starts with
     * @param speed  its starting speed; a unit attacked in the first round before it moves defends as if it had
     *               moved as far as that speed allows (notes 01 section 3.2)
     */
    public void deploy(Coords coords, int facing, MovementSystem system, SpeedState speed) {
        setPosition(coords, facing);
        setMovementSystem(system);
        HGMovementMode mode = currentMovementMode();
        switch (speed) {
            case STATIONARY -> {
                speedState = SpeedState.COMBAT;
                lastHexesMoved = 0;
            }
            case TOP -> {
                speedState = SpeedState.TOP;
                lastHexesMoved = mode.top();
            }
            default -> {
                speedState = SpeedState.COMBAT;
                lastHexesMoved = mode.combat();
            }
        }
    }

    // endregion

    // region The round

    /** @return true once the unit has had its activation this round */
    public boolean hasActivated() {
        return activated;
    }

    /** Clears last round's activation at the start of a new round. */
    public void startNewRound() {
        activated = false;
        hexesMovedThisRound = 0;
    }

    /**
     * Records a finished activation.
     *
     * @param hexesMoved the hexes entered during it
     */
    public void finishActivation(int hexesMoved) {
        activated = true;
        hexesMovedThisRound = hexesMoved;
        lastHexesMoved = hexesMoved;
    }

    /**
     * Works out the actions this activation and pays off any stunned-crew debt (notes 02 section 13.3).
     *
     * @return the actions the unit can use in the activation that is starting
     */
    public int beginActivationActions() {
        int free = freeActions();
        int lost = Math.min(free, stunnedActions);
        stunnedActions -= lost;
        return free - lost;
    }

    /** @return the actions the unit would have if it activated now, without changing anything */
    public int actionsAvailable() {
        return Math.max(0, freeActions() - stunnedActions);
    }

    /**
     * @return the hexes to use on the target speed table when this unit is attacked: this round's move once it has
     *       moved, otherwise its last recorded move (notes 02 section 4.2)
     */
    public int hexesForDefense() {
        return activated ? hexesMovedThisRound : lastHexesMoved;
    }

    // endregion

    // region Weapons

    /** @return the weapons, in unit file order */
    public List<HGWeapon> weapons() {
        return List.copyOf(weapons);
    }

    /** @return the weapon at a position in the list, or null for a bad index */
    public @Nullable HGWeapon weapon(int index) {
        return ((index >= 0) && (index < weapons.size())) ? weapons.get(index) : null;
    }

    // endregion

    // region Damage

    /** @return the current Base Armor */
    public int armor() {
        return armor;
    }

    /** @return the current armor thresholds, such as "14/28/42" */
    public String armorThresholds() {
        return armor + "/" + (2 * armor) + "/" + (3 * armor);
    }

    /** @return the Maneuver modifier after damage */
    public int maneuver() {
        return datacard.maneuver() + maneuverPenalty;
    }

    /** @return the Fire Control modifier after damage: -5 once the system is destroyed */
    public int fireControl() {
        return fireControlDestroyed ? -5 : datacard.fireControl();
    }

    public boolean isFireControlDestroyed() {
        return fireControlDestroyed;
    }

    public int sensorsPenalty() {
        return sensorsPenalty;
    }

    public int communicationsPenalty() {
        return communicationsPenalty;
    }

    public boolean hasPowerTransferFailure() {
        return powerTransferFailure;
    }

    public boolean isAmmoStoreRuptured() {
        return ammoStoreRuptured;
    }

    /**
     * @return true when every movement system is out: by a power failure, a ruptured fuel tank or speed losses.
     *       Such a unit defends with a total of 0 (notes 02 section 13.4).
     */
    public boolean isImmobile() {
        return powerTransferFailure || ammoStoreRuptured || movementModes.stream().allMatch(HGMovementMode::isKnockedOut);
    }

    /** @return true when the unit can still move this round */
    public boolean canMove() {
        return !isOutOfFight() && !isImmobile() && !currentMovementMode().isKnockedOut();
    }

    /** @return true when the unit can still fire */
    public boolean canFire() {
        return !isOutOfFight() && !ammoStoreRuptured;
    }

    /** @return why the unit is out of the fight, or {@link Fate#FIGHTING} */
    public Fate fate() {
        return fate;
    }

    public String fateReason() {
        return fateReason;
    }

    /** @return true for a destroyed unit and for one whose crew is gone; the wreck stays on the map */
    public boolean isOutOfFight() {
        return fate != Fate.FIGHTING;
    }

    /** Lowers the Base Armor; at 0 the unit is destroyed (notes 02 section 11.4). */
    public void loseArmor(int points) {
        armor = Math.max(0, armor - points);
        if (armor == 0) {
            destroy("its armor is gone");
        }
    }

    public void addManeuverPenalty(int penalty) {
        maneuverPenalty += penalty;
    }

    public void destroyFireControl() {
        fireControlDestroyed = true;
    }

    public void addSensorsPenalty(int penalty) {
        sensorsPenalty += penalty;
    }

    public void addCommunicationsPenalty(int penalty) {
        communicationsPenalty += penalty;
    }

    public void failPowerTransfer() {
        powerTransferFailure = true;
    }

    public void ruptureAmmoStore() {
        ammoStoreRuptured = true;
    }

    /** Adds a lost action from a "crew stunned" result. */
    public void stunCrew() {
        stunnedActions++;
    }

    /**
     * Removes crew. A unit with no crew left takes no actions and is out of the fight.
     *
     * @param casualties crew members lost
     */
    public void loseCrew(int casualties) {
        crew = Math.max(0, crew - casualties);
        if ((crew == 0) && (fate == Fate.FIGHTING)) {
            fate = Fate.CREW_LOST;
            fateReason = "its crew is gone";
        }
    }

    /** Destroys the unit. */
    public void destroy(String reason) {
        if (fate != Fate.DESTROYED) {
            fate = Fate.DESTROYED;
            fateReason = reason;
        }
    }

    // endregion

    @Override
    public String toString() {
        return "HGUnit #" + id + " " + displayName();
    }
}
