# HeavyMek

HeavyMek turns [MegaMek](https://github.com/MegaMek/megamek), the open-source computer version of the BattleTech board game, into a computer version of **Heavy Gear's classic tactical game** (Heavy Gear 2nd Edition, Dream Pod 9, 1997). You play on hex maps, using the map editor, lobby and network play that MegaMek already has.

> **Status: Stage 2 built.** HeavyMek plays its first Heavy Gear battle: two players, a Hunter against a Jäger on open ground, from set-up to the last Gear standing. Terrain and line of sight come in Stage 3. [DESIGN.md](DESIGN.md) has the plan, stage by stage.

## The idea in one picture

Think of MegaMek as a game console with BattleTech as the cartridge. The console already has a slot for a second cartridge, which its team used for a different BattleTech variant. HeavyMek builds a Heavy Gear cartridge for that slot and then removes the BattleTech cartridge for good.

## What you'll need

- **Java 21.** On Arch or CachyOS, install it with `sudo pacman -S jdk21-openjdk`. It installs next to any Java you already have, so Java 17 stays. You don't need to change your default Java: the build and `HeavyMek.sh` find Java 21 by themselves.
- **The Heavy Gear 2nd Edition rulebook**, if you want to know the rules. HeavyMek doesn't include Dream Pod 9's books; the PDFs are sold on DriveThruRPG.

## Building and running

From the repository folder:

```sh
./gradlew run          # start HeavyMek straight from the source code
./gradlew installDist  # build a playable copy in megamek/build/install/HeavyMek
./gradlew test         # run the automated tests
```

To play the built copy, run `megamek/build/install/HeavyMek/HeavyMek.sh`. The first build downloads Gradle and HeavyMek's libraries, so it takes a few minutes.

## Playing the first duel

Two players, each in their own HeavyMek window. On one computer:

1. Build the game once with `./gradlew installDist`, then start it with `megamek/build/install/HeavyMek/HeavyMek.sh`.
2. Choose **Start a Scenario**, open the **heavygear** tab and pick **First Duel**.
3. In "Set Scenario Players", set North to **Me** and South to **Other Human**. Keep the port in the next dialog (2346) and press OK. Your window opens and waits for South.
4. Start `HeavyMek.sh` a second time. Choose **Connect to a Game**, type **South** as your name, leave the server address as `localhost` and press OK.

Over a network, the second player types the first computer's address instead of `localhost` (port 2346 has to be open).

How a game goes:

- **Set-up.** The sides take turns placing their Gear. Click a green hex in your zone, set the facing, movement system and speed in the panel on the right, then press **Place**.
- **Each round** starts with an initiative roll; then the sides take turns activating one Gear each.
- **Your activation.** Plan it with the panel's buttons: turn, move forward, reverse, switch between walker and ground. Yellow hexes show your path with the MP spent, and a faded token shows where you'll end up. Click an enemy to target it, pick a weapon and a burst size, and press **Fire from here**: the panel shows every modifier of the shot first. You can fire before, during or after moving. **Undo** and **Start over** take steps back. **End activation** sends the plan; the server carries it out and rolls the dice.
- **The battle log** at the bottom shows every die roll and every modifier.
- The game ends when one side has no Gear left in the fight. Close the window to return to the main menu.

Every Gear and scenario is a plain text file you can open and edit: the Gears are in `megamek/data/heavygear/units`, the scenario and its map in `megamek/data/scenarios/heavygear`. The scenario file explains how to make your own.

## Credits and licensing

- Almost all of the code is MegaMek's, written by The MegaMek Team and its contributors ([CONTRIBUTORS.md](CONTRIBUTORS.md)). MegaMek's original README is kept at [docs/upstream/MegaMek-README.md](docs/upstream/MegaMek-README.md).
- Code: GNU GPL v3 ([LICENSE](LICENSE)). Data and assets from MegaMek: CC BY-NC-SA 4.0 ([LICENSE.assets](LICENSE.assets)).
- Heavy Gear and Silhouette are trademarks of Dream Pod 9, Inc. HeavyMek is an unofficial, non-commercial fan project. It isn't affiliated with or endorsed by Dream Pod 9 or The MegaMek Team.

Details are in [LICENSING.md](LICENSING.md).
