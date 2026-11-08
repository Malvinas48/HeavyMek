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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.Nullable;
import megamek.common.board.Coords;
import megamek.common.jacksonAdapters.CoordsDeserializer;

/**
 * Reads one entry of a side's unit list in a Heavy Gear scenario file, for example:
 * <pre>
 * units:
 *   - unit: hunter          # the unit file, data/heavygear/units/hunter.yaml
 *     name: Bear            # the callsign shown on the map (optional)
 *     crew: Veteran         # Rookie, Qualified (the default), Veteran or Elite
 *     at: [8, 15]           # the hex, as numbered on the map (optional: without it the player places it)
 *     facing: N             # N, NE, SE, S, SW or NW, or 0 to 5 (optional, default N)
 *     system: ground        # the movement system it starts on (optional, default its primary)
 *     speed: combat         # stationary, combat or top (optional, default combat)
 * </pre>
 */
public final class HGScenarioUnitReader {

    private static final List<String> FACINGS = List.of("N", "NE", "SE", "S", "SW", "NW");

    private HGScenarioUnitReader() {
        // only static methods
    }

    /**
     * Makes a unit from its scenario entry. A unit with an {@code at:} position starts on the map; one without waits
     * to be placed at set-up.
     *
     * @param node        the entry
     * @param scenarioDir the scenario's folder, searched first for the unit file
     *
     * @throws IOException when the unit file can't be found or read, or the entry has an error
     */
    public static HGUnit read(JsonNode node, @Nullable File scenarioDir) throws IOException {
        if (!node.hasNonNull("unit")) {
            throw new IOException("A unit in the scenario has no 'unit:' line naming its unit file");
        }
        HGUnit unit = new HGUnit(HGUnitLoader.load(node.get("unit").asText(), scenarioDir));
        try {
            if (node.hasNonNull("name")) {
                unit.setCallsign(node.get("name").asText());
            }
            if (node.hasNonNull("crew")) {
                unit.setCrewLevel(CrewLevel.parse(node.get("crew").asText()));
            }
            MovementSystem system = node.hasNonNull("system")
                  ? MovementSystem.parse(node.get("system").asText())
                  : unit.datacard().primarySystem();
            if (unit.movementMode(system) == null) {
                throw new IllegalArgumentException(unit.datacard().name() + " has no " + system + " system");
            }
            unit.setMovementSystem(system);
            if (node.has("at")) {
                Coords position = CoordsDeserializer.parseNode(node.get("at"));
                SpeedState speed = node.hasNonNull("speed")
                      ? SpeedState.parse(node.get("speed").asText())
                      : SpeedState.COMBAT;
                unit.deploy(position, parseFacing(node.get("facing")), system, speed);
            }
        } catch (IllegalArgumentException e) {
            throw new IOException("Unit " + node.get("unit").asText() + ": " + e.getMessage(), e);
        }
        return unit;
    }

    /** Reads a facing given as a compass direction (N, NE, SE, S, SW, NW) or a number from 0 to 5. */
    static int parseFacing(@Nullable JsonNode node) {
        if ((node == null) || node.isNull()) {
            return 0;
        } else if (node.canConvertToInt()) {
            return Math.floorMod(node.asInt(), 6);
        }
        int index = FACINGS.indexOf(node.asText().trim().toUpperCase(Locale.ROOT));
        if (index < 0) {
            throw new IllegalArgumentException("Unknown facing \"" + node.asText() + "\"; use N, NE, SE, S, SW or NW");
        }
        return index;
    }
}
