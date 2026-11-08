# HeavyMek

HeavyMek turns [MegaMek](https://github.com/MegaMek/megamek), the open-source computer version of the BattleTech board game, into a computer version of **Heavy Gear's classic tactical game** (Heavy Gear 2nd Edition, Dream Pod 9, 1997). You play on hex maps, using the map editor, lobby and network play that MegaMek already has.

> **Status: just getting started.** This repository holds an unmodified copy of MegaMek 0.51.01.5 plus HeavyMek's plan. It doesn't build yet and doesn't play Heavy Gear yet. [DESIGN.md](DESIGN.md) has the plan, stage by stage, and where things stand.

## The idea in one picture

Think of MegaMek as a game console with BattleTech as the cartridge. The console already has a slot for a second cartridge, which its team used for a different BattleTech variant. HeavyMek builds a Heavy Gear cartridge for that slot and then removes the BattleTech cartridge for good.

## What you'll need

- **Java 21.** On Arch or CachyOS: `sudo pacman -S jdk21-openjdk`.
- **The Heavy Gear 2nd Edition rulebook**, if you want to know the rules. HeavyMek doesn't include Dream Pod 9's books; the PDFs are sold on DriveThruRPG.

## Building

You can't build it yet. MegaMek's build expects its separate data repository (`mm-data`) to sit next to it. Stage 0 in [DESIGN.md](DESIGN.md) replaces that with the few files HeavyMek needs.

## Credits and licensing

- Almost all of the code is MegaMek's, written by The MegaMek Team and its contributors ([CONTRIBUTORS.md](CONTRIBUTORS.md)). MegaMek's original README is kept at [docs/upstream/MegaMek-README.md](docs/upstream/MegaMek-README.md).
- Code: GNU GPL v3 ([LICENSE](LICENSE)). Data and assets from MegaMek: CC BY-NC-SA 4.0 ([LICENSE.assets](LICENSE.assets)).
- Heavy Gear and Silhouette are trademarks of Dream Pod 9, Inc. HeavyMek is an unofficial, non-commercial fan project. It isn't affiliated with or endorsed by Dream Pod 9 or The MegaMek Team.

Details are in [LICENSING.md](LICENSING.md).
