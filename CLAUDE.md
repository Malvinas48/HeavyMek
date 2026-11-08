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
- MegaMek's crash reporting (Sentry) and server-browser (metaserver) registration point at MegaMek's servers. They must be off in HeavyMek; Stage 0 removes them.

## Building

- Java 21 toolchain and the Gradle wrapper (`./gradlew`). The build downloads libraries from Maven Central and the Gradle sites.
- Until Stage 0 is done, the build still expects MegaMek's data repository at `../mm-data`.
