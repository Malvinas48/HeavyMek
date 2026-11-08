# HeavyMek scenario: the first duel.
#
# A plain text file in YAML. Lines starting with # are comments. To make your own scenario, copy this file,
# change the names and units, and save it in this folder with a new name ending in .mms; it then shows up in
# "Start a Scenario" under the heavygear tab.
#
# How to play it on one computer: start HeavyMek, choose Start a Scenario, pick First Duel, set North to Me and
# South to Other Human. Then start HeavyMek a second time, choose Connect to a Game, type the name South and press
# Okay.

MMSVersion: 2
gametype: HG
singleplayer: false          # two people play; the host picks a side and the network port
name: First Duel
planet: Terra Nova
description: >
  A Northern Hunter and a Southern Jager meet on open ground. Each side places its Gear along its own map edge,
  then the two fight until one of them is out of the battle.

map: open-ground-16x17.board

factions:
  - name: North
    deploy: N                # North places its units in the three hex rows along the top edge
    units:
      - unit: hunter         # the unit file data/heavygear/units/hunter.yaml
        name: Bear           # the callsign shown on the map
        crew: Qualified      # Rookie, Qualified, Veteran or Elite

  - name: South
    deploy: S                # South places its units in the three hex rows along the bottom edge
    units:
      - unit: jager
        name: Wolf
        crew: Qualified

# A unit can also start already placed, skipping set-up, by giving its hex and facing:
#      - unit: hunter
#        at: [8, 2]          # column 8, row 2, as numbered on the map
#        facing: S           # N, NE, SE, S, SW or NW
#        system: ground      # walker or ground
#        speed: combat       # stationary, combat or top
