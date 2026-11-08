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

import jakarta.annotation.Nullable;

/**
 * A unit's printed record sheet: the numbers from its unit file, which never change during a battle. The unit on
 * the map ({@link HGUnit}) keeps a reference to its datacard and tracks damage separately, the way a player keeps a
 * clean record sheet and puts damage markers on the miniature.
 *
 * @param fileName         the unit file this came from, without ".yaml", such as "hunter"
 * @param name             the unit's name, such as "Hunter"
 * @param model            the model name, such as "Hunter (Mark II)"
 * @param productionCode   the production code
 * @param faction          the faction that builds it
 * @param type             the kind of unit, such as "gear"
 * @param size             the Size
 * @param crew             the crew carried
 * @param actions          the actions per round when undamaged
 * @param armor            the Base Armor
 * @param maneuver         the Maneuver modifier, added to defense rolls
 * @param fireControl      the Fire Control modifier, added to attack rolls
 * @param sensors          the Sensors rating
 * @param communications   the Communications rating
 * @param primarySystem    the primary movement system
 * @param movement         every movement system with its speeds
 * @param threatValue      the Threat Value for a Qualified crew
 * @param perks            the perks, as written in the file
 * @param flaws            the flaws, as written in the file
 * @param weapons          the weapons, undamaged and fully loaded
 * @param image            an optional picture to use instead of the drawn token (not used yet)
 */
public record HGDatacard(String fileName, String name, String model, String productionCode, String faction,
      String type, int size, int crew, int actions, int armor, int maneuver, int fireControl, int sensors,
      int communications, MovementSystem primarySystem, List<HGMovementMode> movement, int threatValue,
      List<String> perks, List<String> flaws, List<HGWeapon> weapons, @Nullable String image)
      implements Serializable {

    /**
     * Copies the lists, so changing a list afterwards doesn't change the datacard. The copies are ArrayLists
     * because those can travel over the network.
     */
    public HGDatacard {
        movement = new ArrayList<>(movement);
        perks = new ArrayList<>(perks);
        flaws = new ArrayList<>(flaws);
        weapons = new ArrayList<>(weapons);
    }

    /** @return the speeds of one movement system, or null if the unit doesn't have it */
    public @Nullable HGMovementMode movementMode(MovementSystem system) {
        return movement.stream().filter(mode -> mode.system() == system).findFirst().orElse(null);
    }

    /** @return the armor thresholds as printed on the record sheet, such as "15/30/45" */
    public String armorThresholds() {
        return armor + "/" + (2 * armor) + "/" + (3 * armor);
    }
}
