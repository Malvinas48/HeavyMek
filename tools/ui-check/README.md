# UI check

Helpers for looking at HeavyMek's screens without a monitor, for example in a cloud workspace. They start the installed game inside a virtual display, click and type where you tell them, and save screenshots.

You need Java 21, `xvfb-run` (the virtual display) and ImageMagick (for `import`).

```sh
./gradlew installDist
xvfb-run -a -s "-screen 0 1600x1000x24" tools/ui-check/ui-session.sh \
    megamek/build/install/HeavyMek 40 /tmp/shot \
    click:804:722 sleep:1500 click:1400:626 sleep:15000
```

That example waits 40 seconds for the game to start and clicks "Acknowledge" on the welcome dialog. It then clicks "Map Editor" and waits 15 seconds. It saves `/tmp/shot_0.png` before the clicks and `/tmp/shot_1.png` after them.

Steps for `RobotClick.java`:

| Step | What it does |
|---|---|
| `click:X:Y` | Left-click at screen position X, Y |
| `dclick:X:Y` | Double-click |
| `sleep:MS` | Wait MS milliseconds |
| `key:NAME` | Press a key: `ENTER`, `ESCAPE`, `TAB`, `SPACE`, and so on |
| `type:TEXT` | Type text |

Screen positions depend on the virtual screen size (1600x1000 above), so take a screenshot first to find them.

## Heavy Gear screens

`HGScreenshotTour`, in the test sources, opens the real game windows for both sides of the First Duel on one screen, plays the first turns and saves a screenshot at each step. It's quicker than clicking through the menus and shows both players' views:

```sh
./gradlew :megamek:compileTestJava
# print the test classpath once (any Gradle task that prints sourceSets.test.runtimeClasspath will do)
cd megamek
xvfb-run -a -s "-screen 0 1600x1000x24" java -cp "$TEST_CLASSPATH" \
    megamek.client.ui.heavyGear.HGScreenshotTour /tmp/hgtour 7 4 mouse
```

The arguments are the folder for the screenshots, the dice seed, how many activations to play and, with `mouse`, whether to plan them with real mouse clicks on the panel and the map (otherwise the tour calls the panel's methods directly).

