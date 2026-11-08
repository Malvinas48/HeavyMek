# HeavyMek game data

This folder holds the art and data files the game loads at runtime. Most of it is copied from MegaMek's data repository, [MegaMek/mm-data](https://github.com/MegaMek/mm-data), at commit `5b184977` (5 Nov 2026). It's licensed under CC BY-NC-SA 4.0 ([LICENSE.assets](../../LICENSE.assets)) and credit goes to The MegaMek Team and its artists.

HeavyMek only copies what it uses:

| Folder | What it is |
|---|---|
| `fonts/` | Interface fonts. Each font folder has its own license file. |
| `images/hexes/` | Terrain tiles for the default "saxarba" tileset, plus the files it includes and the board markers (artillery, fog and so on). |
| `images/widgets/` | Images used by the interface skins that HeavyMek keeps (`megamek/mmconf/skins`). |
| `images/misc/` | HeavyMek's placeholder splash screen, logo and window icons (see below). |
| `css/`, `mapgen/`, `mapsetup/`, `sounds/` | Small interface, map-generator and sound files. |

Left out on purpose: BattleTech units and unit art, portraits and camouflage, record sheets, random assignment tables, the BattleTech universe data, MegaMek's map packs, and the other tilesets.

## Placeholder art

The splash screen, logo, medal and icons in `images/misc/` aren't MegaMek's. `tools/make_placeholder_art.py` draws them, keeping MegaMek's file names so that no code has to change. To use real art instead, save images with the same names over them.
