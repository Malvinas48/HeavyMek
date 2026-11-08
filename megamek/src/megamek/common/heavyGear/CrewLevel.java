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

import java.util.Locale;

/**
 * How good a crew is. The values are the 1997 rulebook's crew table (notes 01 section 1.6, B p.127).
 *
 * <p>The rulebook's table has no Leadership column. Leadership is needed for the initiative roll when several
 * units per side are left, so it falls back on the miniatures book, which gives each level its own number of dice
 * and no bonus (A p.3). That is the only value here taken from the miniatures book.</p>
 */
public enum CrewLevel {
    ROOKIE("Rookie", new Skill(1, 0), new Skill(1, 1), new Skill(1, 0), 0.25),
    QUALIFIED("Qualified", new Skill(2, 0), new Skill(2, 1), new Skill(2, 0), 1),
    VETERAN("Veteran", new Skill(3, 1), new Skill(3, 1), new Skill(3, 0), 2.25),
    ELITE("Elite", new Skill(4, 1), new Skill(4, 2), new Skill(4, 0), 4);

    private final String displayName;
    private final Skill piloting;
    private final Skill gunnery;
    private final Skill leadership;
    private final double threatValueMultiplier;

    CrewLevel(String displayName, Skill piloting, Skill gunnery, Skill leadership, double threatValueMultiplier) {
        this.displayName = displayName;
        this.piloting = piloting;
        this.gunnery = gunnery;
        this.leadership = leadership;
        this.threatValueMultiplier = threatValueMultiplier;
    }

    /** @return the skill used for defense rolls and difficult maneuvers */
    public Skill piloting() {
        return piloting;
    }

    /** @return the skill used for ranged attacks */
    public Skill gunnery() {
        return gunnery;
    }

    /** @return the skill used for initiative rolls (see the class comment) */
    public Skill leadership() {
        return leadership;
    }

    /** @return the multiplier for the unit's Threat Value; the printed value assumes a Qualified crew */
    public double threatValueMultiplier() {
        return threatValueMultiplier;
    }

    /** @return the level's name, such as "Veteran" */
    public String displayName() {
        return displayName;
    }

    /**
     * Reads a crew level written in a scenario or unit file. Upper or lower case both work.
     *
     * @throws IllegalArgumentException for an unknown level
     */
    public static CrewLevel parse(String text) {
        String wanted = text.trim().toUpperCase(Locale.ROOT);
        for (CrewLevel level : values()) {
            if (level.name().equals(wanted)) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown crew level \"" + text + "\"; use Rookie, Qualified, Veteran or Elite");
    }

    @Override
    public String toString() {
        return displayName;
    }
}
