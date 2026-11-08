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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.Test;

/**
 * Reads the real unit files from the data folder and checks them against the record sheets (notes 03 section 4).
 */
class HGUnitLoaderTest {

    @Test
    void hunterMatchesItsRecordSheet() throws IOException {
        HGDatacard hunter = HGUnitLoader.load("hunter", null);
        assertEquals("Hunter", hunter.name());
        assertEquals(6, hunter.size());
        assertEquals(1, hunter.crew());
        assertEquals(1, hunter.actions());
        assertEquals(15, hunter.armor());
        assertEquals("15/30/45", hunter.armorThresholds());
        assertEquals(0, hunter.maneuver());
        assertEquals(0, hunter.fireControl());
        assertEquals(380, hunter.threatValue());
        assertEquals(MovementSystem.WALKER, hunter.primarySystem());
        assertEquals(4, hunter.movementMode(MovementSystem.WALKER).combat());
        assertEquals(7, hunter.movementMode(MovementSystem.WALKER).top());
        assertEquals(6, hunter.movementMode(MovementSystem.GROUND).combat());
        assertEquals(12, hunter.movementMode(MovementSystem.GROUND).top());
        assertEquals(List.of("LAC", "LRP/24", "APGL", "HG", "VB"),
              hunter.weapons().stream().map(HGWeapon::code).toList());
    }

    @Test
    void hunterWeapons() throws IOException {
        List<HGWeapon> weapons = HGUnitLoader.load("hunter.yaml", null).weapons();
        HGWeapon autocannon = weapons.get(0);
        assertEquals(FiringArc.F, autocannon.arc());
        assertEquals(0, autocannon.accuracy());
        assertEquals(8, autocannon.damageMultiplier());
        assertArrayEquals(new int[] { 2, 4, 8, 16 }, autocannon.rangeBands());
        assertEquals(2, autocannon.rateOfFire());
        assertEquals(60, autocannon.ammo());
        assertEquals(20, autocannon.ammoNeeded(2));
        assertTrue(autocannon.isRanged());

        HGWeapon rockets = weapons.get(1);
        assertEquals(HGWeapon.Category.ROCKET, rockets.category());
        assertEquals(8, rockets.ammoNeeded(3));
        assertTrue(rockets.hasSpecial("IF"));

        HGWeapon grenadeLauncher = weapons.get(2);
        assertEquals(FiringArc.FF, grenadeLauncher.arc());
        assertEquals(0, grenadeLauncher.areaEffectRadius());
        assertEquals(-1, autocannon.areaEffectRadius());

        assertFalse(weapons.get(3).isRanged(), "hand grenades are thrown");
        assertFalse(weapons.get(4).isRanged(), "the vibroblade is a melee weapon");
        assertNull(weapons.get(4).ammo());
    }

    @Test
    void jagerIsTheHuntersMirror() throws IOException {
        HGDatacard jager = HGUnitLoader.load("jager", null);
        HGDatacard hunter = HGUnitLoader.load("hunter", null);
        assertEquals(hunter.armor(), jager.armor());
        assertEquals(hunter.threatValue(), jager.threatValue());
        assertEquals(hunter.weapons().size(), jager.weapons().size());
    }

    @Test
    void missingValuesGiveAClearMessage() throws IOException {
        ObjectMapper yaml = new ObjectMapper(new YAMLFactory());
        var node = yaml.readTree("name: Broken\nsize: 6\nmovement:\n  walker: { combat: 4, top: 7 }\n");
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
              () -> HGUnitLoader.parse(node, "broken"));
        assertTrue(error.getMessage().contains("armor"), error.getMessage());
    }

    @Test
    void unknownUnitFileIsReported() {
        assertThrows(IOException.class, () -> HGUnitLoader.load("no-such-gear", null));
    }

    @Test
    void unitStartsUndamagedAndCopiesItsWeapons() throws IOException {
        HGDatacard card = HGUnitLoader.load("hunter", null);
        HGUnit first = new HGUnit(card);
        HGUnit second = new HGUnit(card);
        first.weapons().get(0).useAmmo(2);
        assertEquals(40, first.weapons().get(0).ammo());
        assertEquals(60, second.weapons().get(0).ammo(), "each unit has its own magazine");
        assertEquals(1, first.freeActions());
        assertEquals(380, first.getStrength());
    }
}
