# HeavyMek design doc

This is a living document. It records the decisions made so far, how the pieces fit together, and the plan in stages. Update it whenever a decision changes or a stage is done.

**Status (8 Nov 2026):** Stages 0 and 1 are done, and Stage 2 is built and waiting for your play-test. HeavyMek plays its first Heavy Gear battle: a Hunter against a Jäger, two players, from set-up to the last Gear standing. The rules notes are in [docs/rules](docs/rules/README.md). Next is Stage 3, the battlefield.

## Decisions

| Date | Decision | Why |
|---|---|---|
| 2026-11-08 | Play the **Heavy Gear 2nd Edition (1997) tactical rules** | They were written for hex maps, and MegaMek is built around hex maps. |
| 2026-11-08 | **Remove BattleTech entirely**, in stages (see below) | HeavyMek is a Heavy Gear game, not a BattleTech one. |
| 2026-11-08 | **Two-player duel first**; computer opponent later | MegaMek's bot only understands BattleTech, so a Heavy Gear bot is a project of its own. |
| 2026-11-08 | Claude **builds and tests in the cloud** once Maven Central and the Gradle sites are allowed | Writing Java against about 950,000 lines of code without compiling would be guesswork. |
| 2026-11-08 | Gears, weapons and scenarios are **plain text files** | They're easy to read, tweak and add to by hand. |
| 2026-11-08 | The repository stays **private** | Heavy Gear's unit stats are Dream Pod 9's material. |
| 2026-11-08 | Where the books differ, the **1997 rulebook** wins | The 1997 rules are the hex-map game you chose. The 2005 Tactical Miniatures Rules are for playing with miniatures on a tabletop, not on a hex grid, so they're only consulted where the rulebook says nothing. (Revised the same day: the 2005 book was briefly the main text; you confirmed the rulebook wins.) |
| 2026-11-08 | Keep a trimmed copy of MegaMek's data in `megamek/data` (about 150 MB) | One clone is enough to build and play; no separate data repository. |
| 2026-11-08 | Units appear on the board as **drawn tokens** (player colour, name, facing tip, movement mode, damage boxes) | No artwork needed to start. Each unit file can later name its own picture (an optional `image:` line) once there's art of our own. |
| 2026-11-08 | Default terrain tileset: MegaMek's **saxarba** | The best-looking tileset. The lighter ones (27 MB) can replace it if the repository gets too big. |
| 2026-11-08 | A unit's activation is **planned on the player's screen and sent in one go**; the server replays the plan with the same rules code and rolls the dice | Moving and firing happen in one activation in Heavy Gear, so the plan travels as a whole. Sharing one copy of the rules means the screen's preview and the server never disagree. |
| 2026-11-08 | Until the game can ask questions in the middle of a turn, **choices are made for the players**: a side that may choose who goes first lets the other side go first, and damage that the rules let the attacker or defender place (odd or even die) goes where it hurts most or least | Moving second is usually the better choice. Proper prompts come with command points in Stage 4. |
| 2026-11-08 | Until Stage 4, **each player is one side and each unit is its own combat group** | The duel needs nothing more; combat groups arrive with squads and command. |
| 2026-11-08 | The main menu's **Connect** joins a Heavy Gear game; the button that joined SBF games now says "Connect (BattleTech)" and joins BattleTech games | A second player has to be able to join the duel. BattleTech stays reachable until it's removed. |
| 2026-11-08 | Heavy Gear data lives in `megamek/data/heavygear/units` (unit files) and `megamek/data/scenarios/heavygear` (scenarios and their maps) | MegaMek's scenario chooser already looks in `data/scenarios`, and shows each folder there as a tab. |

## Sources

- *Heavy Gear Rulebook, 2nd Edition* (DP9-101, 1997): the hex-map tactical rules (chapter 7) and the Field Guide with Gear and weapon stats. It wins where the books differ. It's sold as a PDF on DriveThruRPG.
- *Heavy Gear Tactical Miniatures Rules, 2nd Edition* (Dream Pod 9, 2005): rules for tabletop miniatures, not hex maps. Only consulted where the rulebook says nothing.
- *Terra Nova Companion*: setting background (cities and regions), for scenario flavour later.
- *Technical Manual, 2nd Edition* (DP9-104, a "Heavy Gear Revitalized" PDF on DriveThruRPG) covers vehicle construction and technology, for later stages.
- More Gear stats will come from the 2nd Edition vehicle books in a later stage.
- Rule notes in this repository paraphrase the books and cite page numbers. They never copy them.

## How it fits together

### The console and the cartridge

MegaMek is like a game console with BattleTech as the cartridge. The console part doesn't care much which game is plugged in: hex maps, the map editor, the lobby, network play, chat, the board display, dice and reports. The cartridge part is all BattleTech: the units, the rules referee, the record sheets and the bot.

MegaMek already has a second cartridge slot. Its team used it for **Strategic BattleForce (SBF)**, a different BattleTech ruleset, and SBF is the pattern HeavyMek copies:

| Piece | What it does | MegaMek's contract | SBF's version | Heavy Gear version |
|---|---|---|---|---|
| Game type | Says which cartridge is plugged in | `common/game/GameType.java` | `SBF` | `HG` |
| Game state | Everything on the table: units, map, turn | `IGame`, `AbstractGame` | `SBFGame` | `HGGame` |
| Referee | Server side: checks moves, rolls dice, applies damage | `IGameManager`, `AbstractGameManager` | `SBFGameManager` | `HGGameManager` |
| Player's connection | Client side: talks to the referee | `IClient`, `AbstractClient` | `SBFClient` | `HGClient` |
| Screen | The board, unit panels and buttons | `IClientGUI`, `AbstractClientGUI` | `SBFClientGUI` | `HGClientGUI` |

All paths are under `megamek/src/megamek/`. `MegaMekGUI` picks the cartridge (in `getGameManager`, `getClient` and `getClientGUI`) from the `gametype:` line of a scenario file, which `ScenarioV2` reads. A Heavy Gear battle starts from a scenario containing `gametype: HG`.

Where the Heavy Gear pieces are:

| Package | What's in it |
|---|---|
| `common/heavyGear` | The rules and the playing pieces, with no screen or network code: dice, tables, arcs, units and their files, one activation step by step, attacks and damage. `package-info.java` is a map of the package. |
| `server/heavyGear` | `HGGameManager`, the referee, and `HGInitiative` (set-up and round initiative). |
| `client/HGClient.java` | The player's connection. |
| `client/ui/heavyGear` | The game window: `HGClientGUI`, the side panel, the drawn tokens and the player's plan (`HGPlanner`). |

How a turn travels: the player builds a plan in `HGPlanner`, which checks every step with `ActivationState`. The finished plan (`HGActivation`) goes to the server as one packet. `HGGameManager` checks it again with `ActivationState`, carries it out, rolls the dice through `AttackResolver` and `SystemsDamage`, and sends every player the updated units and the new battle log lines.

For scale: SBF is about 14,000 lines in 89 files (counting everything named SBF). The BattleTech referee alone (`TWGameManager.java`) is 33,000 lines, and all of MegaMek is about 950,000 lines in 3,800 Java files.

### The core dice mechanic

Heavy Gear uses Dream Pod 9's Silhouette dice, confirmed against the rulebook in Stage 1 and built in `SkillRoll`:

- Roll as many six-sided dice as the skill level and **keep the highest die**. Each extra 6 adds +1, and all 1s is a fumble.
- Attack and defense are **opposed rolls**, like an arm-wrestle: both sides roll, and the gap between the two results (the margin of success) decides how hard the hit lands. That margin multiplies the weapon's damage.
- The damage is compared with the target's armor. Below the armor it does nothing. At or above the armor it does light damage, at twice the armor heavy damage, and at three times the armor it destroys the target.
- A tie misses: the attack has to beat the defense.

## Removing BattleTech

It's like renovating a house while living in it. You build the new rooms first, move in, and then knock down the old rooms one at a time, so you're never left without a working kitchen.

1. **Side by side.** Heavy Gear works next to BattleTech (Stage 2).
2. **Move in.** The menus and defaults switch to Heavy Gear, and BattleTech can no longer be reached from the menus.
3. **Demolish in chunks.** Delete BattleTech piece by piece, building and testing after each chunk: the rules referee, the units and unit files, record sheets, the bot, BattleTech-only dialogs, and the other BattleTech game modes (Alpha Strike, BattleForce and SBF).
4. **Keep the shared pieces.** Boards, hexes and terrain, players and teams, chat, the lobby and networking stay. They get trimmed where BattleTech left its fingerprints.

The trade-off we accepted: once BattleTech is gone, pulling future MegaMek fixes into HeavyMek stops being practical. HeavyMek becomes its own project.

## Stages

Each stage ends with something you can try.

### Stage 0: Foundation (done, 8 Nov 2026)

- The build works with Java 21 and Gradle, in the cloud and on your PC: `./gradlew run`, `./gradlew installDist`, `./gradlew test`. MegaMek's suite-release tooling, Windows .exe wrapper and style checks are gone.
- `megamek/data` holds a trimmed copy of MegaMek's data repository (`mm-data` commit 5b184977, CC BY-NC-SA 4.0): fonts, the saxarba terrain tiles, the interface skins' images and what the board display needs. BattleTech units, unit art, portraits, camouflage, record sheets, universe data and map packs are left out. See `megamek/data/README.md`.
- Sentry crash reporting is removed, and registering games on MegaMek's server browser is hidden.
- The window, main menu and welcome dialog say HeavyMek. The splash screen, logo and icons are placeholders drawn by `tools/make_placeholder_art.py`.
- Checked by launching the installed game on a virtual display and opening the Map Editor (`tools/ui-check`).

### Stage 1: Read the rules (done, 8 Nov 2026)

- The notes in `docs/rules` summarise every tactical rule with page numbers. Their README maps the rules onto MegaMek, lists where the books disagree and how HeavyMek rules on the open questions.
- The Hunter and the Jäger fight the first duel; their unit files are in `megamek/data/heavygear/units`.

### Stage 2: First duel (built 8 Nov 2026, waiting for your play-test)

- The `HG` game type, started from the scenario `megamek/data/scenarios/heavygear/first-duel.mms` on an open 16 x 17 map.
- Gears load from their unit files, and a scenario names each unit's file, callsign, crew level and, optionally, its starting hex.
- **Set-up:** a set-up roll, then the sides take turns placing a unit in their deployment zone, choosing its facing, movement system and speed.
- **Each round:** an initiative roll (Piloting in a duel, Leadership with more units), then the sides take turns activating one unit at a time.
- **Movement:** MP and the speed bands (Stationary, Half Combat, Combat, Top), turning costs, reversing, shifting up to Top Speed and back, changing between walker and ground, the Piloting test for sharp turns at Top Speed (with skids and crashes), and stacking.
- **Firing:** one shot per action, at any point along the move. Firing arcs including the narrow fixed-forward cone, range bands, every attack and defense modifier that applies on open ground, bursts using Rate of Fire, area effect, fumbles, and ammunition counted round by round.
- **Damage:** armor, Light, Heavy and Overkill, and the full systems damage tables: fire control and weapons, structure, crew, movement and auxiliary systems.
- **The end:** the game ends as soon as one side has no unit left in the fight.
- **The window:** the map with drawn tokens, a side panel to place units and plan activations (the path, where the unit ends up and the modifiers of the shot are shown before you commit), and a battle log that shows every die roll.
- **Tests:** 72 unit tests check the rules and the unit files against the notes. Networked tests play six complete duels between two scripted players, and one more in which South's computer drops out after set-up and South rejoins under the same name. `HGScreenshotTour` (in the test folder) plays the first turns in real windows, with real mouse clicks if asked, and saves screenshots.
- **Leaving and rejoining:** a player who disconnects becomes a "ghost"; the game waits, and they take over again by connecting with the same name.
- **Done when:** you can play a duel from the first move to the last Gear standing.

Left for later stages: terrain, line of sight and elevation (Stage 3); snap fire, thrown grenades, melee weapons and other physical attacks (Stage 3); combat groups, command points, extra actions, evasive maneuvers and indirect fire (Stage 4); saving a game in progress.

### Stage 3: The battlefield

Terrain movement costs, cover and line of sight, elevation, detection and sensors, snap fire, physical attacks (punches, kicks, melee weapons, thrown grenades) and ramming.

### Stage 4: Squads and command

Combat groups and their activation, leadership and command points (with the choices the game currently makes for the players), extra actions and evasive maneuvers, indirect fire, electronic warfare, other vehicles and infantry.

### Stage 5: BattleTech demolition

Steps 2 to 4 of "Removing BattleTech". This can start once Stage 2 works and run alongside the later stages.

Already noticed along the way:
- The main menu's "Quick Tip" lines are BattleTech tips.
- `megamek/docs` is MegaMek's BattleTech documentation.
- The game still scans for BattleTech unit files at startup and finds none.

### Stage 6: Forces

A unit editor or force builder, plus more Gears from the 2nd Edition vehicle books.

### Stage 7: Computer opponent

A Heavy Gear bot.

## Unit files

One YAML file per kind of Gear in `megamek/data/heavygear/units`, such as `hunter.yaml`. The fields follow the rulebook's record sheets; `docs/rules/03-units-weapons.md` section 1 describes them, and the files themselves carry comments with their page references. Weapons can add `category: rocket` (bursts then count rockets), and a unit may later name its own picture with an `image:` line.

A scenario lists each side's units by unit file:

```yaml
factions:
  - name: North
    deploy: N
    units:
      - unit: hunter       # data/heavygear/units/hunter.yaml
        name: Bear         # the callsign on the map
        crew: Veteran      # Rookie, Qualified, Veteran or Elite
        at: [8, 2]         # optional: start placed, skipping set-up
        facing: S
```

## Open questions

- Java package names. New code goes in `megamek.*` packages next to SBF for now. Renaming everything to `heavymek.*` is cosmetic and can wait.
- The map display still has MegaMek's BattleTech-themed border and top bar (from the interface skin). Restyling it belongs with Stage 5.
