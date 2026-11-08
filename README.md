# HeavyMek

HeavyMek turns [MegaMek](https://github.com/MegaMek/megamek), the open-source computer version of the BattleTech board game, into a computer version of **Heavy Gear's classic tactical game** (Heavy Gear 2nd Edition, Dream Pod 9, 1997). You play on hex maps, using the map editor, lobby and network play that MegaMek already has.

> **Status: Stage 0 done.** HeavyMek builds, starts under its own name with placeholder art, and carries only the data it needs. It doesn't play anything yet: the BattleTech units are gone and the Heavy Gear rules arrive in Stage 2. [DESIGN.md](DESIGN.md) has the plan, stage by stage.

## The idea in one picture

Think of MegaMek as a game console with BattleTech as the cartridge. The console already has a slot for a second cartridge, which its team used for a different BattleTech variant. HeavyMek builds a Heavy Gear cartridge for that slot and then removes the BattleTech cartridge for good.

## What you'll need

- **Java 21.** On Arch or CachyOS, install it with `sudo pacman -S jdk21-openjdk`. Then make it the default with `sudo archlinux-java set java-21-openjdk`, because HeavyMek won't start on Java 17.
- **The Heavy Gear 2nd Edition rulebook**, if you want to know the rules. HeavyMek doesn't include Dream Pod 9's books; the PDFs are sold on DriveThruRPG.

## Building and running

From the repository folder:

```sh
./gradlew run          # start HeavyMek straight from the source code
./gradlew installDist  # build a playable copy in megamek/build/install/HeavyMek
./gradlew test         # run the automated tests
```

To play the built copy, run `megamek/build/install/HeavyMek/HeavyMek.sh`. The first build downloads Gradle and HeavyMek's libraries, so it takes a few minutes.

## Credits and licensing

- Almost all of the code is MegaMek's, written by The MegaMek Team and its contributors ([CONTRIBUTORS.md](CONTRIBUTORS.md)). MegaMek's original README is kept at [docs/upstream/MegaMek-README.md](docs/upstream/MegaMek-README.md).
- Code: GNU GPL v3 ([LICENSE](LICENSE)). Data and assets from MegaMek: CC BY-NC-SA 4.0 ([LICENSE.assets](LICENSE.assets)).
- Heavy Gear and Silhouette are trademarks of Dream Pod 9, Inc. HeavyMek is an unofficial, non-commercial fan project. It isn't affiliated with or endorsed by Dream Pod 9 or The MegaMek Team.

Details are in [LICENSING.md](LICENSING.md).
