# Heavy Gear rules notes

These notes are the rulebook as a programmer needs it: every rule, number and table HeavyMek implements, with the page it comes from. They're paraphrased from Dream Pod 9's books for this private project. They don't replace the books and must not be published.

| File | What it covers |
|---|---|
| [01-core-movement.md](01-core-movement.md) | Dice, skills, crews, the datacard, the combat round, command points, actions, movement, terrain, elevation, line of sight, sensors, firing arcs |
| [02-combat-damage.md](02-combat-damage.md) | Attack and defense rolls, every modifier, physical attacks, indirect fire, rate of fire, damage, the systems damage tables, worked examples |
| [03-units-weapons.md](03-units-weapons.md) | The vehicle stat block, weapons, perks and flaws, threat values, infantry in brief |
| [../../megamek/data/heavygear/units/](../../megamek/data/heavygear/units/) | The unit files for the first duel (Hunter and Jäger), now game data |

## How to read them

- **A** is the *Heavy Gear Tactical Miniatures Rules, 2nd Edition* (Dream Pod 9, 2005). **B** is the *Heavy Gear Rulebook, 2nd Edition* (1997), chapter 7 for the tactical rules. "(B p.139 / PDF 293)" means printed page 139, which is page 293 of the scanned PDF.
- **DIFF:** marks a place where A and B disagree. **UNCLEAR:** marks something neither book settles.
- **Implementation note:** marks a hint about how the rule fits a hex engine.

## Which book wins

**B, the 1997 rulebook.** It's the hex-map game you chose. A is written for miniatures on a tabletop, not for hex grids, so it's only consulted where B says nothing. Rules that only A has (Hull Down, Standby, the Multiple Attacker Penalty, picking up guns, the auxiliary-systems damage table) are left out at first and can come back later as game options.

## How Heavy Gear maps onto MegaMek

| Heavy Gear | In HeavyMek |
|---|---|
| One hex is 50 m; 1 MP enters one Clear hex | MegaMek's hex boards as they are. A 16x17 board is 800 by 850 m. |
| Units face a hexside and turn by hexsides | MegaMek's six facings. A one-hexside turn is free; two or three hexsides cost 1 MP. |
| Terrain costs MP per movement system (walker, ground, hover) | MegaMek's terrain types, priced per movement system from the notes' terrain table. |
| Elevation | MegaMek's height levels. Stage 2 settles the details from notes 01 section 5.9. |
| Set-up: sides take turns placing a combat group, and each unit picks its movement system and speed | MegaMek's deployment phase, taking turns by combat group. |
| Each round: Declaration, Initiative, Activation, Miscellaneous events | An HG round in the new game type: a declaration step, an initiative roll, alternating group activations, then end-of-round events. |
| Activation: each unit of the group moves and acts before the next unit goes | One turn per unit inside the group's activation: move, then fire, in one go. |
| Snap fire: reacting to an enemy that is moving | Later stage (Stage 3); the first duel has no reactions. |
| Datacard stats, weapons, perks and flaws | A new Heavy Gear unit class loaded from plain text files. MegaMek's BattleTech unit classes aren't used. |

## Choices where the books differ

This is what HeavyMek will do (B's rule), and what A does instead. Ask for any of them to be switched.

| Topic | HeavyMek (B) | A instead |
|---|---|---|
| Who acts first each round | The side with fewer combat groups chooses; the initiative winner chooses on a tie | The initiative winner chooses |
| Command points | Battle-long points from set-up, plus per-round points equal to the initiative margin | Battle-long points only, but a point can buy a re-roll |
| Snap fire | Any enemy unit may react | Only units that haven't activated yet, and it uses up one of their attacks |
| Point Blank range | Same hex | Touching, or within one hex |
| Being shot from behind | Rear flank -1, rear -2 to defense | Rear -1; plus a penalty when several units attack the same target |
| Ammunition | Rounds are counted | Rate of fire wears down per use; ammo counting is optional |
| Punches and melee weapons | Rolled with Gunnery + Fire Control | Everything physical uses Piloting + Maneuver |
| Fixed-forward weapons | A narrow 120° cone starting one hex ahead | Same as an ordinary forward arc |
| Crew quality | B's skill table and Threat Value multipliers | A's table, which adds Legendary pilots and EW/Leadership columns |
| Sharp turns at Top Speed | Piloting test for turns of 180° or more | Turns over 60° |
| Jamming (ECM) | Blocks communications and sensors | Blocks communications only, and weakens each round |

## Questions neither book settles

Each topic file ends with its full list. These matter first, for the duel, so here's what HeavyMek will do until you say otherwise:

- **Entering a hex that costs more MP than you have left:** not allowed. Leftover MP are lost, as in the books' examples.
- **Terrain for the sharp-turn test at Top Speed:** the hex where the turn happens, using the unit's current movement system.
- **Line of sight running exactly along a hexside:** the defender picks the hex that's better for them, as MegaMek does for BattleTech.
- **Hexes on the boundary between the front and rear arcs:** they count as front.
- **Odd Combat Speed and "half speed":** compare MP spent x 2 with Combat MP, so no rounding is needed.
