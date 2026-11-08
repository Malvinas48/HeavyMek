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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import jakarta.annotation.Nullable;
import megamek.common.Configuration;

/**
 * Reads unit files: the plain text files in {@code data/heavygear/units} that describe each kind of Gear, such as
 * {@code hunter.yaml}. They are written in YAML, the same format as MegaMek's scenario files; lines starting with
 * # are comments for people and are ignored here.
 *
 * <p>Besides the stats the rulebook prints, a weapon may say {@code category: rocket}: rockets and missiles count
 * ammunition per missile in bursts (2, 4, 8 or 16), other weapons per 10 rounds. Weapons with a {@code quantity}
 * above 1 become that many separate weapons.</p>
 */
public final class HGUnitLoader {

    /** The folder of the unit files, inside the data folder. */
    public static final String UNITS_FOLDER = "heavygear/units";

    private static final ObjectMapper YAML = new ObjectMapper(new YAMLFactory());

    private HGUnitLoader() {
        // only static methods
    }

    /** @return the folder holding the unit files */
    public static File unitsDirectory() {
        return new File(Configuration.dataDir(), UNITS_FOLDER);
    }

    /**
     * Finds and reads a unit file by name. "hunter" and "hunter.yaml" both work. The scenario's own folder is
     * searched first, then the shared units folder.
     *
     * @param unitName    the unit file's name
     * @param scenarioDir the scenario's folder, or null
     *
     * @throws IOException when the file can't be found or read, or has an error in it
     */
    public static HGDatacard load(String unitName, @Nullable File scenarioDir) throws IOException {
        String fileName = unitName.trim();
        if (!fileName.toLowerCase(Locale.ROOT).endsWith(".yaml")) {
            fileName += ".yaml";
        }
        List<File> candidates = new ArrayList<>();
        if (scenarioDir != null) {
            candidates.add(new File(scenarioDir, fileName));
        }
        candidates.add(new File(unitsDirectory(), fileName));
        for (File candidate : candidates) {
            if (candidate.isFile()) {
                return load(candidate);
            }
        }
        throw new IOException("Unit file " + fileName + " not found in " + unitsDirectory());
    }

    /**
     * Reads one unit file.
     *
     * @throws IOException when the file can't be read or has an error in it
     */
    public static HGDatacard load(File file) throws IOException {
        JsonNode node = YAML.readTree(file);
        String name = file.getName().replaceFirst("(?i)\\.yaml$", "");
        try {
            return parse(node, name);
        } catch (IllegalArgumentException e) {
            throw new IOException(file.getName() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Turns the contents of a unit file into a datacard.
     *
     * @param node     the file's contents
     * @param fileName the file name without ".yaml"
     *
     * @throws IllegalArgumentException when a required value is missing or a value can't be read
     */
    public static HGDatacard parse(JsonNode node, String fileName) {
        if ((node == null) || !node.isObject()) {
            throw new IllegalArgumentException("the file is empty or not a list of values");
        }
        List<HGMovementMode> movement = new ArrayList<>();
        MovementSystem primary = readMovement(node.get("movement"), movement);
        return new HGDatacard(fileName,
              requiredText(node, "name"),
              text(node, "model", ""),
              text(node, "productionCode", ""),
              text(node, "faction", ""),
              text(node, "type", "gear"),
              requiredInt(node, "size"),
              intValue(node, "crew", 1),
              intValue(node, "actions", 1),
              requiredInt(node, "armor"),
              intValue(node, "maneuver", 0),
              intValue(node, "fireControl", 0),
              rating(node.get("sensors")),
              rating(node.get("communications")),
              primary,
              movement,
              intValue(node, "threatValue", 0),
              readTraits(node.get("perks")),
              readTraits(node.get("flaws")),
              readWeapons(node.get("weapons")),
              node.hasNonNull("image") ? node.get("image").asText() : null);
    }

    private static MovementSystem readMovement(@Nullable JsonNode node, List<HGMovementMode> movement) {
        if ((node == null) || !node.isObject()) {
            throw new IllegalArgumentException("the movement section is missing");
        }
        for (Map.Entry<String, JsonNode> field : node.properties()) {
            if (!field.getKey().equals("primary")) {
                MovementSystem system = MovementSystem.parse(field.getKey());
                movement.add(new HGMovementMode(system, requiredInt(field.getValue(), "combat"),
                      requiredInt(field.getValue(), "top")));
            }
        }
        if (movement.isEmpty()) {
            throw new IllegalArgumentException("the movement section has no movement system");
        }
        if (node.hasNonNull("primary")) {
            MovementSystem primary = MovementSystem.parse(node.get("primary").asText());
            if (movement.stream().noneMatch(mode -> mode.system() == primary)) {
                throw new IllegalArgumentException("the primary movement system " + primary + " has no speeds");
            }
            return primary;
        }
        return movement.get(0).system();
    }

    private static List<HGWeapon> readWeapons(@Nullable JsonNode node) {
        List<HGWeapon> weapons = new ArrayList<>();
        if (node == null || node.isNull()) {
            return weapons;
        }
        for (JsonNode weaponNode : node) {
            String code = text(weaponNode, "code", "");
            String name = text(weaponNode, "name", code);
            if (name.isBlank()) {
                throw new IllegalArgumentException("a weapon has neither a name nor a code");
            }
            int baseRange = intValue(weaponNode, "baseRange", 0);
            int[] bands = null;
            if (weaponNode.hasNonNull("rangeBands")) {
                JsonNode bandsNode = weaponNode.get("rangeBands");
                if (!bandsNode.isArray() || (bandsNode.size() != 4)) {
                    throw new IllegalArgumentException(name + ": rangeBands needs four numbers, such as [2, 4, 8, 16]");
                }
                bands = new int[4];
                for (int i = 0; i < 4; i++) {
                    bands[i] = bandsNode.get(i).asInt();
                }
            }
            List<String> special = new ArrayList<>();
            if (weaponNode.hasNonNull("special")) {
                weaponNode.get("special").forEach(tag -> special.add(tag.asText()));
            }
            Integer ammo = weaponNode.hasNonNull("ammo") ? weaponNode.get("ammo").asInt() : null;
            HGWeapon weapon = new HGWeapon(name,
                  code.isBlank() ? name : code,
                  text(weaponNode, "model", ""),
                  FiringArc.parse(text(weaponNode, "arc", "F")),
                  intValue(weaponNode, "accuracy", 0),
                  requiredInt(weaponNode, "damage"),
                  baseRange,
                  bands,
                  intValue(weaponNode, "rateOfFire", 0),
                  ammo,
                  HGWeapon.Category.parse(text(weaponNode, "category", "gun")),
                  special);
            int quantity = Math.max(1, intValue(weaponNode, "quantity", 1));
            for (int i = 0; i < quantity; i++) {
                weapons.add(weapon.copy());
            }
        }
        return weapons;
    }

    /** Perks and flaws are kept as readable text for now; they get rules in later stages. */
    private static List<String> readTraits(@Nullable JsonNode node) {
        List<String> traits = new ArrayList<>();
        if (node == null || node.isNull()) {
            return traits;
        }
        for (JsonNode trait : node) {
            if (trait.isObject()) {
                StringBuilder text = new StringBuilder(text(trait, "name", "?"));
                if (trait.hasNonNull("option")) {
                    text.append(" (").append(trait.get("option").asText()).append(")");
                }
                if (trait.hasNonNull("count") && (trait.get("count").asInt() > 1)) {
                    text.append(" x").append(trait.get("count").asInt());
                }
                if (trait.hasNonNull("rating")) {
                    text.append(", rating ").append(trait.get("rating").asInt());
                }
                traits.add(text.toString());
            } else {
                traits.add(trait.asText());
            }
        }
        return traits;
    }

    private static int rating(@Nullable JsonNode node) {
        if (node == null || node.isNull()) {
            return 0;
        }
        return node.isObject() ? intValue(node, "rating", 0) : node.asInt();
    }

    private static String requiredText(JsonNode node, String field) {
        if (!node.hasNonNull(field) || node.get(field).asText().isBlank()) {
            throw new IllegalArgumentException("the value '" + field + "' is missing");
        }
        return node.get(field).asText().trim();
    }

    private static String text(JsonNode node, String field, String defaultValue) {
        return node.hasNonNull(field) ? node.get(field).asText().trim() : defaultValue;
    }

    private static int requiredInt(JsonNode node, String field) {
        if (!node.hasNonNull(field)) {
            throw new IllegalArgumentException("the value '" + field + "' is missing");
        }
        return intValue(node, field, 0);
    }

    private static int intValue(JsonNode node, String field, int defaultValue) {
        if (!node.hasNonNull(field)) {
            return defaultValue;
        }
        JsonNode value = node.get(field);
        if (!value.canConvertToInt() && !value.isTextual()) {
            throw new IllegalArgumentException("'" + field + "' should be a whole number");
        }
        try {
            return value.isTextual() ? Integer.parseInt(value.asText().trim().replace("+", "")) : value.asInt();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'" + field + "' should be a whole number, not \"" + value.asText()
                  + "\"");
        }
    }
}
