# HeavyMek: notes for Claude

HeavyMek is a private fork of MegaMek (Java 21, Gradle). It's being converted to play the Heavy Gear 2nd Edition (Dream Pod 9, 1997) tactical rules on hex maps. **Read [DESIGN.md](DESIGN.md) first.** It holds the decisions, the staged plan and the current status. Update it whenever a decision changes or a stage finishes.

## Working with the owner

- The owner is new to Java. Explain changes in plain language, using simple analogies where they help.
- Workflow: Claude builds features and pushes them, then the owner pulls and learns by exploring the working code.
- Game data (Gears, weapons, scenarios) lives in plain text files that the owner can edit by hand.

## Ground rules

- The rules come from Dream Pod 9's Heavy Gear 2nd Edition books, which the owner supplies as PDFs. Implement the mechanics in code. Never paste rulebook text or artwork into the repository; notes paraphrase and cite page numbers.
- BattleTech is being removed entirely, in the order DESIGN.md describes. Don't add BattleTech features. Don't delete BattleTech code that Heavy Gear still relies on until its replacement works. The game must build and launch after every change.
- Heavy Gear plugs in as a new game type. MegaMek's Strategic BattleForce (SBF) ruleset is the pattern: `GameType`, `AbstractGame`, `IGameManager`, `AbstractClient` and `IClientGUI`, chosen by the `gametype:` line in a scenario file.
- MegaMek's crash reporting (Sentry) is removed and its server-browser registration is hidden; keep it that way, because they point at MegaMek's servers.

## Building and checking

- Java 21 toolchain and the Gradle wrapper: `./gradlew run`, `./gradlew installDist` (playable copy in `megamek/build/install/HeavyMek`), `./gradlew test`. The build downloads libraries from Maven Central and the Gradle sites.
- Game data lives in `megamek/data` (trimmed from MegaMek's mm-data; see its README). Don't add BattleTech data back.
- In the cloud workspace: set `LC_ALL=C.UTF-8` (some data file names aren't ASCII), and keep Gradle and test memory modest (the machine has about 8 GB), e.g. `-Dorg.gradle.jvmargs=-Xmx1g` and test `maxHeapSize` 2g.
- To look at screens without a monitor, use `tools/ui-check` with `xvfb-run`.
