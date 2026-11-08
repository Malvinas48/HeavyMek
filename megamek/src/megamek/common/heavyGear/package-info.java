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

/**
 * The Heavy Gear rules and the pieces they work on: the "cartridge" that plugs into MegaMek's console.
 *
 * <p>Nothing in this package draws on the screen or talks to the network. It is plain game logic, so the server,
 * the client and the unit tests can all use the same code. A quick map:</p>
 * <ul>
 *     <li><b>Dice and skills:</b> {@link megamek.common.heavyGear.SkillRoll} reads a roll the Heavy Gear way
 *     (highest die, +1 per extra six, all ones is a fumble). {@link megamek.common.heavyGear.CrewLevel} holds the
 *     crew table.</li>
 *     <li><b>Tables:</b> {@link megamek.common.heavyGear.RangeBand}, {@link megamek.common.heavyGear.MovementBand},
 *     {@link megamek.common.heavyGear.CombatTables}, {@link megamek.common.heavyGear.DamageResult}.</li>
 *     <li><b>Hex geometry:</b> {@link megamek.common.heavyGear.HexGeometry}, used by
 *     {@link megamek.common.heavyGear.FiringArc} and {@link megamek.common.heavyGear.DefenseArc}.</li>
 *     <li><b>Units:</b> {@link megamek.common.heavyGear.HGUnit} is one Gear on the board, built from a unit file by
 *     {@link megamek.common.heavyGear.HGUnitLoader}; {@link megamek.common.heavyGear.HGWeapon} is one of its
 *     weapons.</li>
 *     <li><b>A unit's turn:</b> {@link megamek.common.heavyGear.HGActivation} is the plan a player sends (moves,
 *     turns and one shot); {@link megamek.common.heavyGear.ActivationState} checks it one step at a time;
 *     {@link megamek.common.heavyGear.AttackRules} works out the attack and defense modifiers and
 *     {@link megamek.common.heavyGear.AttackResolver} rolls the dice and applies the damage through
 *     {@link megamek.common.heavyGear.SystemsDamage}.</li>
 *     <li><b>The game itself:</b> {@link megamek.common.heavyGear.HGGame}.</li>
 * </ul>
 *
 * <p>Page references in the comments point at the rules notes in {@code docs/rules}, which cite the books.</p>
 */
package megamek.common.heavyGear;
