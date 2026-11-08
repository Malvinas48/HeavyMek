# HeavyMek design doc

This is a living document. It records the decisions made so far, how the pieces fit together, and the plan in stages. Update it whenever a decision changes or a stage is done.

**Status (8 Nov 2026):** MegaMek 0.51.01.5 has been imported. Stage 0 needs the build download sites allowed in claude.ai settings. Stage 1 needs the Heavy Gear 2nd Edition rulebook PDF.

## Decisions

| Date | Decision | Why |
|---|---|---|
| 2026-11-08 | Play the **Heavy Gear 2nd Edition (1997) tactical rules** | They were written for hex maps, and MegaMek is built around hex maps. |
| 2026-11-08 | **Remove BattleTech entirely**, in stages (see below) | HeavyMek is a Heavy Gear game, not a BattleTech one. |
| 2026-11-08 | **Two-player duel first**; computer opponent later | MegaMek's bot only understands BattleTech, so a Heavy Gear bot is a project of its own. |
| 2026-11-08 | Claude **builds and tests in the cloud** once Maven Central and the Gradle sites are allowed | Writing Java against about 950,000 lines of code without compiling would be guesswork. |
| 2026-11-08 | Gears, weapons and scenarios are **plain text files** | They're easy to read, tweak and add to by hand. |
| 2026-11-08 | The repository stays **private** | Heavy Gear's unit stats are Dream Pod 9's material. |

## Sources

- *Heavy Gear Rulebook, 2nd Edition* (DP9-101, 1997) has the tactical rules. It's sold as a PDF on DriveThruRPG.
- *Technical Manual, 2nd Edition* (DP9-104, a "Heavy Gear Revitalized" PDF on DriveThruRPG) covers vehicle construction and technology, for later stages.
- More Gear stats will come from the 2nd Edition vehicle books in a later stage.
- Rule notes in this repository paraphrase the books and cite page numbers. They never copy them.

## How it fits together

### The console and the cartridge

MegaMek is like a game console with BattleTech as the cartridge. The console part doesn't care much which game is plugged in: hex maps, the map editor, the lobby, network play, chat, the board display, dice and reports. The cartridge part is all BattleTech: the units, the rules referee, the record sheets and the bot.

MegaMek already has a second cartridge slot. Its team used it for **Strategic BattleForce (SBF)**, a different BattleTech ruleset, and SBF is the pattern HeavyMek copies:

| Piece | What it does | MegaMek's contract | SBF's version | Heavy Gear version (planned) |
|---|---|---|---|---|
| Game type | Says which cartridge is plugged in | `common/game/GameType.java` | `SBF` | `HG` |
| Game state | Everything on the table: units, map, turn | `IGame`, `AbstractGame` | `SBFGame` | `HGGame` |
| Referee | Server side: checks moves, rolls dice, applies damage | `IGameManager`, `AbstractGameManager` | `SBFGameManager` | `HGGameManager` |
| Player's connection | Client side: talks to the referee | `IClient`, `AbstractClient` | `SBFClient` | `HGClient` |
| Screen | The board, unit panels and buttons | `IClientGUI`, `AbstractClientGUI` | `SBFClientGUI` | `HGClientGUI` |

All paths are under `megamek/src/megamek/`. `MegaMekGUI` picks the cartridge (in `getGameManager`, `getClient` and `getClientGUI`) from the `gametype:` line of a scenario file, which `ScenarioV2` reads. A Heavy Gear battle would start from a scenario containing `gametype: HG`.

For scale: SBF is about 14,000 lines in 89 files (counting everything named SBF). The BattleTech referee alone (`TWGameManager.java`) is 33,000 lines, and all of MegaMek is about 950,000 lines in 3,800 Java files.

### The core dice mechanic (to confirm against the rulebook)

Heavy Gear uses Dream Pod 9's Silhouette dice. This is what we expect before reading the 2nd Edition book:

- Roll as many six-sided dice as the skill level and **keep the highest die**. Each extra 6 adds +1, and all 1s is a fumble.
- Attack and defense are **opposed rolls**, like an arm-wrestle: both sides roll, and the gap between the two results (the margin of success) decides how hard the hit lands. That margin multiplies the weapon's damage.
- The damage is compared with the target's armor. Below the armor it does nothing. At or above the armor it does light damage, at twice the armor heavy damage, and at three times the armor it destroys the target.

Stage 1 checks all of this against the book.

## Removing BattleTech

It's like renovating a house while living in it. You build the new rooms first, move in, and then knock down the old rooms one at a time, so you're never left without a working kitchen.

1. **Side by side.** Heavy Gear works next to BattleTech (Stage 2).
2. **Move in.** The menus and defaults switch to Heavy Gear, and BattleTech can no longer be reached from the menus.
3. **Demolish in chunks.** Delete BattleTech piece by piece, building and testing after each chunk: the rules referee, the units and unit files, record sheets, the bot, BattleTech-only dialogs, and the other BattleTech game modes (Alpha Strike, BattleForce and SBF).
4. **Keep the shared pieces.** Boards, hexes and terrain, players and teams, chat, the lobby and networking stay. They get trimmed where BattleTech left its fingerprints.

The trade-off we accepted: once BattleTech is gone, pulling future MegaMek fixes into HeavyMek stops being practical. HeavyMek becomes its own project.

## Stages

Each stage ends with something you can try.

### Stage 0: Foundation

- The build works with Java 21 and Gradle, both in the cloud and on your PC.
- Replace the dependency on MegaMek's separate data repository (`mm-data`, expected at `../mm-data`) with a copy of only what HeavyMek needs, kept in this repository: hex terrain tiles, boards, fonts and interface images (credited, CC BY-NC-SA 4.0). Leave out BattleTech units, unit art and random assignment tables. Right now `/megamek/data` is git-ignored because MegaMek fills it from `mm-data` at build time.
- Switch off everything that talks to MegaMek's servers: Sentry crash reporting and server-browser (metaserver) registration.
- Show "HeavyMek" in the window title and on the main menu.
- **Done when:** HeavyMek builds and opens its main menu on your PC.

### Stage 1: Read the rules

- Go through the 2nd Edition tactical rules and write `docs/rules-notes.md`: our own summary of each rule, with page numbers.
- Decide how the rules map onto MegaMek: the turn sequence and phases, the hex scale and height levels, and what a unit record holds.
- Pick the Gears for the first duel and write their text files.
- **Done when:** you've read the notes and agree with the mapping.

### Stage 2: First duel

- Add the `HG` game type, started from a scenario file.
- Gears load from text files. Add deployment, movement with movement points and speeds, firing with Silhouette dice, light damage, heavy damage and destruction. The game ends when one side has no Gears left.
- Two players: two windows on one PC, or over a LAN.
- Unit tests for the dice and damage rules.
- **Done when:** you can play a duel from the first move to the last Gear standing.

### Stage 3: The battlefield

Terrain movement costs, cover and line of sight, elevation, fire arcs, detection and sensors, the system damage tables, physical attacks and ramming.

### Stage 4: Squads and command

Initiative and activation of combat groups, leadership and command, indirect fire, electronic warfare, other vehicles and infantry.

### Stage 5: BattleTech demolition

Steps 2 to 4 of "Removing BattleTech". This can start once Stage 2 works and run alongside the later stages.

### Stage 6: Forces

A unit editor or force builder, plus more Gears from the 2nd Edition vehicle books.

### Stage 7: Computer opponent

A Heavy Gear bot.

## Unit files (sketch)

There will be one text file per Gear, written in YAML like MegaMek's scenario files. The exact fields will follow the rulebook's stat blocks and get settled in Stage 1. The numbers below are invented:

```yaml
# Example only: invented values, not a real Gear.
name: Example Gear
type: gear
size: 6
crew: 1
armor: 14
maneuver: -1
sensors: -1
fireControl: 0
movement:
  walker: { combat: 4, top: 7 }
  ground: { combat: 6, top: 11 }
weapons:
  - name: Light Autocannon
    arc: front
    accuracy: 0
    damage: 8          # damage multiplier
    baseRange: 3       # hexes
    rateOfFire: 2
    ammo: 60
```

## Open questions

- The hex scale and height levels (Stage 1, from the rulebook).
- How the rulebook's turn sequence maps onto MegaMek's phases (Stage 1).
- Which Gears fight the first duel (Stage 1). The classic Hunter vs. Jäger matchup is the obvious pick.
- Where Heavy Gear data files live once `mm-data` is gone (Stage 0).
- Java package names. New code goes in `megamek.*` packages next to SBF for now. Renaming everything to `heavymek.*` is cosmetic and can wait.
