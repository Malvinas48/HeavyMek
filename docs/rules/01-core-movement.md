# HeavyMek rules notes 01: core rules, turn structure, movement, terrain, line of sight

Scope: dice and skills, units and datacards, the combat round and command points, number of actions, movement and terrain, line of sight and obscurement. Attacks, damage, infantry, perks/flaws and optional rules belong to other notes; they appear here only as cross-references where these rules feed into them.

---

## 0. Conventions

- **A** = *Heavy Gear Tactical Miniatures Rules, 2nd Edition* (DP9, 2005). Cited by printed page: (A p.7). PDF page = printed page + 1.
- **B** = *Heavy Gear Rulebook, 2nd Edition* (DP9, 1997), the hex-map rules. Cited as (B p.133 / PDF 287). PDF page = book page + 9 in chapter 4 (Silhouette Basics) and book page + 154 in chapter 7 (Tactical Rules).
  - Correction to the brief: book p.124 (start of chapter 7) is PDF 278, not PDF 279. PDF 279 is book p.125.
- Which book wins is set in [README.md](README.md): B wins where the books differ, and A fills B's gaps. (These notes were drafted with A as the base text, so read every DIFF with that policy in mind.)
- Markers: **DIFF:** = A and B disagree, or one book contradicts itself. **UNCLEAR:** = not stated, ambiguous or illegible. **Implementation note:** = how the rule maps onto a hex engine.
- Scale (Tactical): 1 hex = 1 table inch = 50 m. 1 MP buys one Clear hex. One round is about 30 seconds (A p.5; B p.124 / PDF 278, p.128 / PDF 282, p.130 / PDF 284). Wherever A says "inch", these notes say "hex".
- Hex orientation: B draws flat-topped hexes, and a unit faces one hexside (the top one in all diagrams) (B p.131, p.133, p.136). MegaMek uses the same layout (direction 0 = top hexside, numbered clockwise).
- Relative directions used below, with `rel = (direction - facing) mod 6`: rel0 front, rel1 front-right, rel2 rear-right, rel3 rear, rel4 rear-left, rel5 front-left.

---

## 1. Dice, skills, crew levels, margins

### 1.1 Rolling dice (A p.3; B p.44 / PDF 53)

"Nd6" means roll N six-sided dice. Every roll in the game is read this way unless a rule says otherwise:

1. Take the single highest die. Dice are never summed.
2. Add +1 for each 6 beyond the first.
3. If every die shows 1, the roll is a **Fumble**: the total is 0 and no modifier can change it.
4. Otherwise add all modifiers. A total below 0 becomes 0.

| Dice showing | Modifier | Total |
|---|---|---|
| 2, 5 | 0 | 5 |
| 6, 2, 6, 6 | 0 | 8 (6, plus 1 for each of the two extra sixes) |
| 1, 1 | +3 | 0 (Fumble; the modifier is ignored) |
| 1, 4 | +2 | 6 |
| 3, 2 | -5 | 0 (floored) |

(Own examples; A and B give the same kind.)

- Unmodified Nd6 ranges from 1 to N+5; a Fumble is 0. The chance of a Fumble is (1/6)^N.
- In A, modifiers never apply to Fumbles, except in Melee Scale, which HeavyMek doesn't use (A p.3).

Implementation note: the roll result needs a separate Fumble flag as well as the number 0, because several rules react to Fumbles specifically (section 1.2).

### 1.2 Fumble effects in the tactical game (B p.44 / PDF 53; A p.27)

| Roll that fumbles | Effect |
|---|---|
| Combat initiative | That side loses. If both sides fumble, roll again. |
| Attack | Automatic miss. |
| Defense | Automatic hit, unless the attack also fumbled. Damage is worked out as if the defense total were 0. |
| Active sensor sweep | No LOS (also A p.8; B p.135). |
| Top-speed sharp turn | Crash: movement ends; roll 1d6 for Light or Heavy damage (split: see the DIFF below). |
| Set-up initiative | The Fumble counts as a die result of 1 (A p.5; B p.128 / PDF 282). |

- **DIFF (each book contradicts itself):** crash damage is Light on 1-3 and Heavy on 4-6 in the turning rules (A p.7; B p.133 / PDF 287), but Light on 1-4 and Heavy on 5-6 in the fumble tables (A p.27; B p.44 / PDF 53).

### 1.3 Rolls against a fixed Threshold (B p.45 / PDF 54)

- One side rolls skill dice + modifiers against a fixed Threshold number.
- Total > Threshold: success. Margin of Success (MoS) = total - Threshold.
- Total < Threshold: failure. Margin of Failure (MoF) = Threshold - total.
- Total = Threshold: a draw. In tactical play, draws favour the defender (B p.45).
- Specific tactical rules settle draws themselves:
  - Top-speed turn test: a draw counts as success (A p.7; B p.133).
  - Active sensor sweep: a draw counts as failure (A p.8; B p.135).
  - Communication roll under ECM: the roll must be higher than the ECM Threshold (A p.9).

Difficulty guide (B p.45), labels paraphrased:

| Threshold | Difficulty |
|---|---|
| 1 | trivial |
| 2 | routine |
| 3 | easy |
| 4 | moderate |
| 5 | challenging |
| 6 | difficult |
| 7 | very difficult |
| 8 | extremely difficult |
| 10 | near impossible |
| 12+ | hopeless |

### 1.4 Opposed rolls, margins and ties (A p.3; B p.46 / PDF 55)

- Both sides roll their skill + modifiers. The higher total wins.
- MoS (winner) = MoF (loser) = the difference between the totals.
- A tie gives margin 0, and the acting side does not succeed. For attacks a tie is a miss (A p.9; B p.137 / PDF 291). In general, ties go to the resisting side (B p.46).
- With three or more participants, work out a separate margin for each pair (B p.46).
- Own example: an attacker rolls 2d6 (1, 4) for a total of 4; the defender rolls 3d6 (2, 3, 6) for a total of 6. The attacker has MoF 2 and misses.

### 1.5 The four tactical skills (A p.3; B p.126 / PDF 280, p.47 / PDF 56)

| Skill | Used for |
|---|---|
| Piloting (called Driving for ground vehicles and Naval Piloting for naval vessels; same function) | defense rolls; physical attacks (A); difficult maneuvers such as top-speed turns and cliff climbing; combat initiative when only one unit per side is left |
| Gunnery | all ranged attacks |
| Electronic Warfare (EW) | passive Detection value; active sensor sweeps; communication rolls under ECM; ECM and ECCM Thresholds |
| Leadership | the commander's set-up initiative and TCP roll (A); combat initiative each round |

- A skill is written "N/+M": roll N dice and add M to the result (A p.3; B p.127, column "Lvl/Attr.").
- A unit with skill level 0 can't attempt any task that needs that skill in the tactical game (B p.126; B p.47).
- Level names (B p.47): 0 Untrained, 1 Rookie, 2 Qualified, 3 Veteran, 4 Elite, 5+ Legendary.
- **DIFF:** In A, all physical attacks use Piloting + Maneuver (A p.2, p.10). In B, punches and melee weapons use Gunnery + Fire Control, while kicks use Piloting + Maneuver (B p.141 / PDF 295). Details belong in the attack notes.
- **DIFF:** B calls the set-up roll a "Tactics" skill test at the commander's skill level (B p.128). A uses the commander's Leadership (A p.5).

### 1.6 Crew levels (A p.3, repeated p.26; B p.127 / PDF 281)

A's crew table:

| Level | Name | Piloting | Gunnery | EW | Leadership | TV multiplier |
|---|---|---|---|---|---|---|
| 1 | Rookie | 1/0 | 1/0 | 1/0 | 1/0 | x0.5 |
| 2 | Qualified | 2/0 | 2/+1 | 1/0 | 2/0 | x1 |
| 3 | Veteran | 3/+1 | 3/+1 | 2/0 | 3/0 | x3 |
| 4 | Elite | 4/+1 | 4/+1 | 3/0 | 4/0 | x4 |
| 5 | Legendary | 5/+1 | 5/+1 | 4/0 | 5/0 | x5 |

B's crew table:

| Name | Piloting | Gunnery | TV multiplier |
|---|---|---|---|
| Rookie | 1/0 | 1/+1 | x0.25 |
| Qualified | 2/0 | 2/+1 | x1 |
| Veteran | 3/+1 | 3/+1 | x2.25 |
| Elite | 4/+1 | 4/+2 | x4 |

- Default crew is Qualified. Datacard TVs assume Qualified crews (A p.3; B p.127).
- Final TV of the unit = datacard TV x crew multiplier (A p.3; B p.127).
- B has no EW or Leadership columns. In tactical play B sets EW one level below Gunnery, optionally swapping the two (B p.126).
- A advises giving the Commander and Second in Command a Leadership one level above normal (Veteran, 3d6) (A p.5). This is advice, not a rule.
- A's Qualified Gunnery "2/+1" looks like a typo, but B prints the same value, so it is intended.
- **DIFF:**
  - Rookie Gunnery bonus: A +0, B +1.
  - Elite Gunnery bonus: A +1, B +2.
  - TV multipliers: Rookie x0.5 (A) vs x0.25 (B); Veteran x3 (A) vs x2.25 (B).
  - Legendary: only A has it.
  - EW: under B's "Gunnery minus one" rule a Rookie has EW 0 and can't use EW at all; A gives Rookies EW 1.

Implementation note: store each crew as (dice, bonus) for each of the four skills plus a TV multiplier, with presets per level.

---

## 2. Combat units, combat groups and the datacard

### 2.1 Combat units and combat groups (A p.3; B p.124 / PDF 278)

- A combat unit is one vehicle or one 10-trooper infantry squad, shown by one model or counter.
- A combat group is two or more units that activate together.
- Gears usually come in squadrons of 5.
- B adds: infantry squads form platoons of 4 squads, and a battle typically has 1 to 3 combat groups per side.
- Force building (Core, Elite, Specialist and Support groups; A p.2 and chapter 3) is covered in other notes.

Implementation note: activation alternates by combat group (section 3.6), but each unit in the active group still moves and acts one at a time.

### 2.2 Datacard fields (A p.4; B p.125-127 / PDF 279-281)

| Field (card label) | Meaning | How the game uses it | Source |
|---|---|---|---|
| Threat Value (TV) | the unit's points value | force balancing; multiplied by the crew multiplier | A p.4; B p.125 |
| Armor (number in the shield) | base armor | Damage thresholds: Light = 1x armor, Heavy = 2x, Overkill = 3x. Damage equal to or above a threshold gives that result, and only the worst result applies. Light removes 1 point of armor, Heavy removes 2, Overkill destroys the unit. A unit at 0 armor is destroyed. The row of boxes on the card tallies the armor. | A p.4, p.14; B p.126, p.145 / PDF 299 |
| Fire Control (FireCon) | targeting systems | added to every attack roll | A p.4; B p.126 |
| Sensors (Sens r/R) | rating r / range R in km | r is added to the passive Detection value and to active sweeps; R is the maximum LOS and detection range | A p.4, p.8; B p.126, p.135 |
| Communications (Com r/R) | rating / range in km | r is added to communication rolls against ECM; R is the comm range (and A's ECCM area) | A p.4, p.9 |
| Maneuver (Man) | agility, zero-average | added to every Piloting roll: defense, physical attacks in A, turning tests | A p.4; B p.126 |
| Speed (arrow boxes "C/T") | Combat/Top MP for each movement system. The first (single) arrow is the primary system (Walker for Gears); the second (double) arrow is the secondary system (Ground for Gears). | MP per round (section 5) | A p.4; B p.126 |
| Crew | number of crew, plus a blank box for the skill level | actions; casualties | A p.4 |
| Actions | free actions per round | section 4 | A p.4, p.6 |
| Size | mass class (section 2.4) | ramming and physical attacks, stacking, falling damage | A p.4; B p.125 |
| Perks/Flaws | special rules | A p.17 | A p.4 |
| Weapons | Name, Arc, Acc, BR, Dam, ROF, Ammo | section 2.5 | A p.4; B p.127 |

- Top Speed is nominally twice Combat Speed (A p.4; B p.126), but printed values are rounded from kph. B converts 70 kph to 12 MP and 140 kph to 23 MP, so MP = kph / 6 rounded to the nearest whole number (B p.130). Always use the printed pair; A's sample card shows Ground 6/11.
- Sensor and comm ranges are in km. Hexes = km x 20 (A p.8: 1 km = 20 inches).

### 2.3 Sample card decoded (A p.4, the Grizzly)

Useful as a test fixture:

| Item | Card value | Derived |
|---|---|---|
| Armor | 18 | Light at 18+ damage, Heavy at 36+, Overkill at 54+ |
| Walker speed | 3/6 | |
| Ground speed | 6/11 | |
| TV | 888 | |
| Crew / Actions | 1 / 1 | |
| FireCon | 0 | |
| Sens | 0 / 2 km | 40 hexes |
| Com | 0 / 10 km | 200 hexes |
| Size | 7 | 7.4-10 t |
| Man | -1 | |
| Armor boxes | 18 open, 12 shaded | |
| Perks/Flaws | 2 Manipulator Arms (rating 7); Reinforced Armor 2 (front); Large Sensor Profile 1 | |

Weapons (Arc / Acc / BR / Dam / ROF / Ammo):

| Weapon | Arc | Acc | BR | Dam | ROF | Ammo |
|---|---|---|---|---|---|---|
| HAC | F | 0 | 3 | x12 | 1 | 30 |
| MRP/18 (two of them) | F | -1 | 2 | x18 | 3 | 18 |
| HMG | FF | 0 | 1 | x4 | 3 | 300 |
| DPG | F | -1 | 2 | x8 | 2 | 30 |
| HGM | F | 0 | 5 | x20 | 0 | 12 |
| VB | F | 0 | 0 | x8 | 0 | - |

Range bands for the HAC (BR 3): Short 1-3, Medium 4-6, Long 7-12, Extreme 13-24 hexes.

### 2.4 Size-to-mass chart (A p.4 for Sizes 1-10; B p.125 / PDF 279 for Sizes 1-100)

- B's formula: the largest mass of a Size, in kg, is about ((Size + 0.5) x 3)^3. The printed table rounds these values.
- Inverse: Size = the smallest S for which ((S + 0.5) x 3)^3 >= mass in kg. Use the table where it applies.
- A's values for Sizes 1-10 match B's exactly.

Mass in metric tons:

| Size | Mass | Size | Mass | Size | Mass | Size | Mass |
|---|---|---|---|---|---|---|---|
| 1 | 0-0.08 | 26 | 451-500 | 51 | 3501-3700 | 76 | 11601-12100 |
| 2 | 0.09-0.3 | 27 | 501-560 | 52 | 3701-3900 | 77 | 12101-12600 |
| 3 | 0.4-1.1 | 28 | 561-630 | 53 | 3901-4100 | 78 | 12601-13100 |
| 4 | 1.2-2.4 | 29 | 631-690 | 54 | 4101-4400 | 79 | 13101-13600 |
| 5 | 2.5-4.4 | 30 | 691-770 | 55 | 4401-4600 | 80 | 13601-14100 |
| 6 | 4.5-7.3 | 31 | 771-840 | 56 | 4601-4900 | 81 | 14101-14600 |
| 7 | 7.4-10 | 32 | 841-930 | 57 | 4901-5100 | 82 | 14601-15200 |
| 8 | 11-16 | 33 | 931-1000 | 58 | 5101-5400 | 83 | 15201-15700 |
| 9 | 17-22 | 34 | 1001-1100 | 59 | 5401-5700 | 84 | 15701-16300 |
| 10 | 23-30 | 35 | 1101-1210 | 60 | 5701-6000 | 85 | 16301-16900 |
| 11 | 31-40 | 36 | 1201-1300 | 61 | 6001-6300 | 86 | 16901-17500 |
| 12 | 41-52 | 37 | 1301-1400 | 62 | 6301-6600 | 87 | 17501-18100 |
| 13 | 53-65 | 38 | 1401-1500 | 63 | 6601-6900 | 88 | 18101-18700 |
| 14 | 66-81 | 39 | 1501-1700 | 64 | 6901-7200 | 89 | 18701-19400 |
| 15 | 82-100 | 40 | 1701-1800 | 65 | 7201-7600 | 90 | 19401-20000 |
| 16 | 101-120 | 41 | 1801-1900 | 66 | 7601-7900 | 91 | 20001-20700 |
| 17 | 121-145 | 42 | 1901-2100 | 67 | 7901-8300 | 92 | 20701-21400 |
| 18 | 146-170 | 43 | 2101-2200 | 68 | 8301-8700 | 93 | 21401-22100 |
| 19 | 171-200 | 44 | 2201-2400 | 69 | 8701-9100 | 94 | 22101-22800 |
| 20 | 201-230 | 45 | 2401-2500 | 70 | 9101-9500 | 95 | 22801-23500 |
| 21 | 231-270 | 46 | 2501-2700 | 71 | 9501-9900 | 96 | 23501-24100 |
| 22 | 271-310 | 47 | 2701-2900 | 72 | 9901-10300 | 97 | 24101-25000 |
| 23 | 311-350 | 48 | 2901-3100 | 73 | 10301-10700 | 98 | 25001-25800 |
| 24 | 351-400 | 49 | 3101-3300 | 74 | 10701-11200 | 99 | 25801-26600 |
| 25 | 401-450 | 50 | 3301-3500 | 75 | 11201-11600 | 100 | 26601-27400 |

Implementation note: Size 30 is also the per-hex stacking limit. Any unit above Size 30 needs a multi-hex footprint (section 5.10).

### 2.5 Weapon columns (A p.4; B p.127 / PDF 281)

- **Arc:** one of F, Rt, L, Rr, FF, T (section 6.9). A weapon with no arc listed uses F (B p.136).
- **Acc (Accuracy):** added to attack rolls with that weapon. Damage can lower it.
- **BR (Base Range):** in hexes; it is the top of the Short band (section 2.6).
- **Dam (Damage Multiplier):** damage = MoS x Dam (A p.14; B p.145). Effectiveness grows faster than the number: x10 is about four times as effective as x5 (A p.4; B p.127).
- **ROF (Rate of Fire):**
  - A (p.4): ROF 0 fires one shot per action; more shots need more actions. A weapon with ROF 1 or more can make rapid-fire attacks. Each rapid-fire use lowers its current ROF by 1; at 0 it can still fire single shots. Guns get their full ROF back by spending an action to reload. Missiles and mortars can't be reloaded during a battle. Ammo isn't tracked unless the optional ammo rules are used (A p.2, p.4).
  - B (p.127): ROF 0 fires one round per attack. ROF 1 or more allows burst and multi-target fire (B p.143). Ammo is tracked round by round, and a weapon with 0 ammo can't fire. Physical attacks use no ammo.
  - **DIFF:** A replaces B's ammo counting with ROF depletion. This change is intentional (A p.2).
- **Ammo:** rounds carried. Optional in A, required in B.
- **Linked weapons** (Weapon Link perk): one action fires the whole link, using the worst Acc and BR in it (A p.17). Covered in the attack notes.

### 2.6 Range bands (A p.4, p.10; B p.127, p.138 / PDF 292)

| Band | Distance in hexes (B) | A's wording | Attack modifier |
|---|---|---|---|
| Point Blank | 0 (same hex) | models or bases touching, or within 1 inch | +1 |
| Short | 1 to BR | up to BR inches | 0 |
| Medium | BR+1 to 2xBR | | -1 |
| Long | 2xBR+1 to 4xBR | | -2 |
| Extreme | 4xBR+1 to 8xBR | | -3 |
| (out of range) | more than 8xBR | | no attack |

- Range is counted between hex centers (B p.141 / PDF 295, stated for thrown objects).
- **DIFF:** Point Blank is the same hex in B, but "touching or within 1 inch" in A. A literal translation of A to hexes would make adjacent hexes Point Blank too, which would also trigger A's -1 defense penalty for Point Blank attacks (A p.10). Pick one.
- **UNCLEAR:** weapons with BR 0 (for example the VB on the sample card) presumably reach Point Blank only.
- **UNCLEAR:** for targets that occupy several hexes, B measures to the target's nearest hex. B's sentence is garbled (B p.133).

---

## 3. The combat round

### 3.1 Round structure (A p.5-6, p.27; B p.128-129 / PDF 282-283)

- Step 0 Set-up happens once, before the first round.
- Each round then runs: Step 1 Declaration, Step 2 Initiative, Step 3 Activation, Step 4 Miscellaneous Events.
- Rounds repeat until the battle is decided or the scenario objectives are met.
- Each unit gets one action per round by default; more are possible at a penalty (section 4).
- A combat group activates only once per round (A p.6; B p.129).

### 3.2 Step Zero: set-up (A p.5; B p.128 / PDF 282)

1. Agree the map and terrain. Default deployment zones are the first 3 inches (A) or first 3 hex rows (B) along opposite map edges. Scenarios may set other zones.
2. Each side names a Commander unit and a Second-in-Command unit. They may be kept secret but must be written down.
3. Set-up initiative: each side rolls its Commander's Leadership (A), or a Tactics test at the commander's level (B). A Fumble counts as 1, and ties are re-rolled. The winner chooses which side deploys first.
4. The sides take turns placing one whole combat group at a time.
5. As each unit is placed, record its movement system and its speed state:
   - Movement system: primary or secondary. Most non-Gear vehicles have only one.
   - Speed state: Stationary, Hull Down, Combat or Top (A). B records only the speed: Stationary, Combat or Top.
   - A suggests a marker die next to the unit: 1 Stationary, 2 Hull Down, 3 primary-system Combat, 4 primary-system Top, 5 secondary-system Combat, 6 secondary-system Top (A p.5, p.7).
6. Tactical Command Points (section 3.3):
   - A: once every unit is placed, each side rolls its Commander's Leadership once. The result is its TCP for the whole battle.
   - B: the step 3 set-up roll itself gives the TCP.
   - **DIFF:** two rolls in A, one roll in B.

Other set-up rules:

- Airdroppable perk: the unit may be placed anywhere on the map at the start, as long as no enemy unit has LOS to it (A p.2, p.17).
- First-round defense: a unit attacked in round 1 before it has moved defends as if it had moved the maximum number of hexes its declared speed allows in its current terrain (A p.5, p.10; B p.128).
  - **UNCLEAR:** what "maximum" means for units declared Stationary or Hull Down at set-up. Read literally it is 0 hexes, which gives a -3 defense modifier.
- Command succession: if the Commander is lost, the Second in Command takes over. If both are lost:
  - A: future Leadership rolls use the best Leadership among the side's surviving pilots (A p.5).
  - B: they use the next-highest-ranking soldier, at level 1 by default (B p.128).
  - **DIFF.**

Implementation note: deployment alternates per combat group, with movement system and speed chosen per unit as it is placed. MegaMek's per-unit deployment needs a group-level turn plus per-unit mode and speed prompts.

### 3.3 Command Points (A p.5, p.26; B p.129 / PDF 283)

TCP roll (A p.5):

| Commander's Leadership | Dice | TCP range |
|---|---|---|
| Rookie | 1d6 | 1-6 |
| Qualified | 2d6 | 1-7 |
| Veteran | 3d6 | 1-8 |
| Elite | 4d6 | 1-9 |
| Legendary | 5d6 | 1-10 |

- TCP = the roll's result (highest die plus extra sixes). The table's minimum is 1, so a Fumble apparently counts as 1, which is what B states for its combined set-up/TCP roll.
  - **UNCLEAR:** A never says so directly; this is inferred from the table.
- TCPs last the whole battle and are never regained once spent (A p.5; B p.129).
- **Initiative Command Points (B only):** each round the initiative winner gets ICPs equal to its initiative MoS, and the loser gets none. Unspent ICPs are lost in Step 4. Otherwise ICPs work like TCPs (B p.129).
  - **DIFF:** A dropped per-round command points on purpose (A p.2).
- Who may use them: any unit with a working Communication system (A p.5; B p.129).
- Under enemy ECM, passing a point to a unit needs a successful Communication roll by the commander's unit (A p.9; B p.136).
- Points may be played at any time during the game (A p.5).

What a point buys (1 point each):

| Use | Effect | A | B |
|---|---|---|---|
| Extra action | one more action this round, without the multiple-action penalty | p.5 | p.129 |
| Defensive maneuvering | +2 to a single defense roll | p.5 | p.129 |
| Out-of-sequence activation | activate a unit that hasn't activated yet this round (for example, to escape). When its group activates later, it can't move again, but it can still use any actions it has left. | p.5 | p.129 |
| About-face | turn a unit by up to 180 degrees (up to 3 hexsides), even if it already activated and has no MP left | p.5 | p.129 |
| Reroll | reroll any die roll | p.5 | (not in B) |

- A unit that has to turn to snap fire pays one point for the about-face (A p.6).
- **UNCLEAR:** reroll scope: own rolls only, or the opponent's too? Which result stands (presumably the new one)? Can the same roll be rerolled twice?
- **UNCLEAR:** whether a failed transfer under ECM loses the point.
- **UNCLEAR:** when an out-of-sequence activation may happen: only between activations, or also as an interrupt during an enemy's move.

Implementation note: command points need interrupt prompts: before and after rolls, and during enemy movement. A needs one counter per side; B needs two (TCP and ICP).

### 3.4 Step One: declaration (A p.5, p.11; B p.128, p.130, p.141)

Both sides declare for the coming round:

- **Extra actions** beyond each unit's free actions (section 4). B requires them to be declared here (B p.130).
- **Evasive maneuvers** (A p.11; B p.141):
  - Uses the unit's action and must be declared now; a unit can't switch to evasion later in the round.
  - Gives +3 to all of the unit's defense rolls this round.
  - The unit can do nothing else that round: no attacks, no other actions.
  - Declaring it more than once gives no extra bonus.
- **UNCLEAR:** the order of declarations and whether they are secret. A simultaneous hidden choice is the natural reading.

Implementation note: this is a per-round declaration phase before initiative. MegaMek's normal phase list has nothing like it.

### 3.5 Step Two: initiative (A p.6; B p.129 / PDF 283)

1. Each side rolls its Commander's Leadership (or the successor's, section 3.2). If exactly one unit per side remains, both sides roll Piloting instead.
2. The highest total wins. Ties are re-rolled. A Fumble loses, and if both sides fumble they roll again (A p.27; B p.44).
3. Who activates first:
   - A: the winner chooses.
   - B: the side with fewer combat groups chooses; if both have the same number, the winner chooses.
   - **DIFF.**
4. B only: the winner receives ICPs equal to its MoS.

ECM and ECCM status is checked during this phase (section 6.6).

### 3.6 Step Three: activation (A p.6; B p.129 / PDF 283)

1. The side whose turn it is picks one of its combat groups that hasn't activated this round.
2. That group's units go one at a time. Each unit finishes all its movement and actions before the next one starts. A unit may move, act, do both, or do neither.
3. Before moving, the unit announces its speed. This caps its MP and sets the movement modifier for its own attacks for the whole activation.
   - A's example: Combat Speed announced, so at most Combat MP.
   - B's example: Half Combat announced, so at most half the Combat MP (A p.6; B p.129).
4. Actions (firing, activating systems and so on) may come before, during or after the unit's movement.
5. Right after it finishes moving, the unit declares any speed change (Combat to Top, or Top to Combat). The change applies from next round (A p.6; B p.129, p.131). Section 5.5 covers the immediate effect of going to Top.
6. A unit that neither moves nor acts during its group's activation loses both for the round (A p.6; B p.129). Exceptions: Standby (section 3.8, A only), snap fire (section 3.7) and command points.
7. When the whole group is done, the other side activates one of its groups. The sides alternate until every group has activated.
8. If one side has no groups left to activate, the other side activates its remaining groups one after another (A p.6; B p.129). A p.27 says the same thing as "the side with nothing left skips its turns".

Implementation note: MegaMek's alternating unit turns become alternating combat-group turns. A "current group" holds the turn until all its units are done.

### 3.7 Snap fire (A p.6; B p.129)

Snap fire is reaction fire at the unit that is currently moving.

| Aspect | A | B |
|---|---|---|
| Who may snap fire | enemy units that haven't activated yet this round | any enemy unit (no restriction written) |
| Cost | one or more of the shooter's actions; each one lowers its remaining attacks for the round by 1. No command point is needed unless the shooter must turn to bring a weapon to bear (then a CP about-face). | one or more of the shooter's actions |
| Allowed target | only the unit that is moving | same |
| When | at any point along the mover's path | same |
| Mover's defense speed | based on the mover's whole MP allowance for this activation, not the distance covered so far | same |
| Several snap actions by one shooter | the mover must spend at least 1 MP (or finish moving) between them | same |
| Forward observers | must act before the units they spot for | same |
| Name | "snap fire" | unnamed |

- **UNCLEAR:** "MP allowance" probably means the cap of the announced speed (for example Combat MP), converted to hexes moved on the target-speed table.
- **UNCLEAR (B):** whether an already-activated unit with actions to spare may snap fire. B's activation text also says a unit can't act after its own activation, which conflicts.
- **UNCLEAR:** whether snap actions can be non-attack tasks. The text allows "a task" aimed at the mover, for example an active sensor sweep.

Implementation note: the opponent must see the mover's path one step at a time, with an interrupt after every MP spent.

### 3.8 Standby (A p.6, p.2; A only)

- During its activation, a unit (typically fire support with guided or long-range weapons) spends an action to go on Standby.
- Later in the round, if a target designator paints a target or a forward observer passes on a target, the Standby unit fires one weapon once at it.
- If no call comes, the action is wasted.
- **DIFF:** not in B.
- **UNCLEAR:** whether the Standby shot happens at the moment of the call, inside another unit's activation. Presumably it does.

### 3.9 Step Four: miscellaneous events (A p.6; B p.129)

- Resolve artillery, air strikes and (A, Tactical scale) orbital strikes. Rules are in A's off-table support section (A p.26) and other notes.
- B: ICPs reset to 0, and any unused actions are lost.
- The next round then starts at Step One.

---

## 4. Number of actions (A p.6; B p.130 / PDF 284)

Every vehicle gets 1 free action, and more crew gives more free actions. The crew needed doubles for each extra action:

| Crew at least | Free actions |
|---|---|
| 1 | 1 |
| 2 | 2 |
| 4 | 3 |
| 8 | 4 |
| 16 | 5 |
| 32 | 6 |
| 64 | 7 |
| 128 | 8 |
| 256 | 9 |
| 512 | 10 |

- Formula: free actions = 1 + floor(log2(crew)) for crew of 1 or more. A unit with no crew can take no actions at all.
- The card's "Actions" field holds the undamaged value (A p.4, p.6).
- **DIFF (A contradicts itself):** A's prose says 4 crew give 4 actions, but A's own tank example (4 crew, 3 actions) and B's table both give 3. Follow the table.
- **Crew casualties:** when the crew drops below the minimum for its current tier, the unit loses that free action. A 4-crew tank reduced to 3 crew keeps 2 free actions (A p.6; B p.130).
- **Extra actions:**
  - A unit may take more actions than its free number. Each extra action gives -1 to all of that unit's actions this round. Example: 1 free action plus 2 extra gives -2 on all three.
  - Extra actions are declared in Step One (B p.130).
  - Taking fewer actions than allowed gives no bonus (A p.6; B p.130).
  - An extra action bought with a command point carries no penalty (section 3.3).
- **Other effects on actions** (cross-references):
  - Advanced Controls perk: +1 action whatever the crew size.
  - Automation perk: its rating counts as extra crew.
  - Passengers never add actions (A p.17).
  - "Crew stunned" light damage: -1 action for 1 round (A p.27).

Typical actions (A p.6; B p.130):

| Action | Notes |
|---|---|
| Fire one weapon (or one linked set) once | |
| Pick up a gun, or reload to restore ROF | A |
| Use a target designator | A: success gives a free communication action |
| Make one physical attack or throw one object | |
| Activate ECM, or activate ECCM | section 6.6 |
| Active sensor sweep | A: success gives a free communication action |
| Communicate (for example, send coordinates) | |
| Act as forward observer | |
| Go on Standby | A |
| Embark or disembark crew or passengers | B: one crewman, or as many passengers as the vehicle's Size, per action |
| Evasive maneuvers | uses the action (section 3.4) |

- **UNCLEAR:** whether the multiple-action penalty also applies to defense rolls. A says it applies to "all actions"; B says "all action test rolls". A defense roll is not normally an action.

---

## 5. Movement and terrain

### 5.1 Movement points and scale (A p.6; B p.130 / PDF 284)

- 1 MP moves a unit into one Clear hex (50 m). 1 MP of speed is about 6 kph. Other terrain costs more (section 5.8).
- Each round a unit gets MP equal to its current speed (Combat or Top) for its current movement system.
- MP are spent on entering hexes (terrain cost plus any elevation change, sections 5.8-5.9) and on turning (section 5.7).
- MP not spent by the end of the activation are lost (A p.7; B p.133).
- **UNCLEAR:** whether a unit can enter a hex that costs more than its remaining MP. Neither book says; the examples simply throw away leftover MP. Infantry are an exception: a hex never costs them more than 2 MP, so they can always move at least one hex (A p.16; B p.150).

### 5.2 Speed states at a glance

| State | MP received | MP that may be spent | Own attack modifier | Notes | Source |
|---|---|---|---|---|---|
| Stationary | Combat MP | 0 | +2 | defends as 0 hexes moved (-3) | A p.6-7, p.10; B p.131, p.138 |
| Hull Down (A only) | Combat MP | 1 MP at the end of the move to go down (after any movement) | by MP spent | attacks against it take +1 obscurement | A p.6-7 |
| Half Combat | Combat MP | 1 to half the Combat MP | +1 | reverse movement allowed | A p.7; B p.131 |
| Combat | Combat MP | up to Combat MP | 0 | spending all of it allows a shift to Top | A p.7; B p.131 |
| Top | Top MP | from Combat MP + 1 up to Top MP | -3 | no change of movement system; sharp-turn test | A p.7; B p.131 |

- **Defense side** (owned by the attack notes; listed because movement has to be recorded for it): target speed modifier by hexes moved (A p.10, p.27):

| Hexes moved | Defense modifier |
|---|---|
| 0 | -3 |
| 1-2 | -2 |
| 3-4 | -1 |
| 5-6 | 0 |
| 7-9 | +1 |
| 10-19 | +2 |
| 20-99 | +3 |
| 100-999 | +4 (A p.27 only) |

- A unit that hasn't moved yet this round uses its last recorded movement (A p.10).
- "Half Combat Speed or less" means MP spent x 2 <= Combat MP. B's example counts 3 of 6 MP as half (B p.131).
  - **UNCLEAR:** rounding for odd Combat MP. Comparing exactly as above avoids the question.

Implementation note: per unit, store the current movement system, speed state, MP spent this activation, and hexes moved in its last activation. The last one persists across rounds.

### 5.3 Stationary and Hull Down (A p.6-7; B p.131 / PDF 285)

**Stationary:**

- Only a unit at Combat Speed (not Top) can be Stationary, by spending 0 MP. It gets +2 to its attacks and -3 to its defense rolls (A p.6).
- B: a stationary unit may still turn by one hexside and still counts as stationary. It can also still dodge (B p.131). A doesn't mention this.

**Hull Down** (A core rule; not in B):

- At the end of its move, a unit spends 1 MP to go hull down. Attacks against it then take +1 obscurement (A p.6). A p.27 lists hull down alongside terrain obscurement in the attack modifiers.
- If its total spend, including that 1 MP, is no more than half its Combat MP, it keeps the +1 slow-speed attack bonus (A p.7).
- If it stays hull down next round without moving, it counts as Stationary (+2 to attacks) and keeps the hull-down benefit (A p.7).
- Getting up costs 1 MP when it next moves, in any later round. In that round it can't shift to Top Speed; in the round after, spending its full Combat MP allows the shift (A p.7).
- Marker die: 2.
- **UNCLEAR:** whether a unit at Top Speed can go hull down. Stationary is Combat-only, so presumably hull down is too.
- **UNCLEAR:** whether hull down's +1 also counts toward Concealment for the LOS/detection check, or only as an attack penalty.
- **DIFF (within A):** A's optional Hull-Down Positions rule (A p.20) replaces the flat 1 MP with terrain-based costs (section 5.11).

### 5.4 Combat speed and reverse (A p.7; B p.131 / PDF 285)

- At Combat Speed a unit gets its Combat MP and may spend from 0 up to all of it. Spending 0 makes it Stationary.
- Attacks have no movement penalty at Combat Speed. Spending half or less gives +1.
- **Reverse:** a unit spending no more than half its Combat MP may move backward instead of forward: out through its rear hexside, keeping its facing. It still gets the +1. Reverse is not possible at higher speeds.
- **UNCLEAR:**
  - whether forward and backward hexes can be mixed in one move;
  - whether backward hexes count as the "one hex forward" required between turns (section 5.7);
  - backward hexes are assumed to cost the same as forward ones.

Implementation note: MegaMek's backward move step fits directly. Cap the total at half the Combat MP whenever a backward step is used.

### 5.5 Top speed (A p.7; B p.131 / PDF 285)

1. **Shifting up:** a unit at Combat Speed that spends exactly all its Combat MP may declare a shift to Top right after moving. For the rest of this round it counts as at Top Speed for attack and defense purposes, so any attack it makes later this round takes -3.
2. **Following rounds:** it receives its Top MP and must spend more than its Combat MP (at least Combat + 1), up to its Top MP.
3. **Shifting down:** after any Top-speed move, the player may declare a return to Combat Speed. Combat MP apply from next round.
4. Marker die: 4 (primary system) or 6 (secondary system).

- Example (B p.131): a Hunter on its Ground system (Combat 6, Top 12) must spend 7 to 12 MP each round at Top.
- Other limits at Top Speed: no change of movement system (section 5.6); sharp-turn test (section 5.7); amphibious units can't enter or leave water (section 5.8); attacks take -3.
- **UNCLEAR:** what happens if terrain or blocking units make the minimum Top-speed spend impossible.
- **UNCLEAR:** "all its Combat MP" when the last reachable hex costs more than the MP left. Read strictly, the unit can't shift.

### 5.6 Multiple movement systems (A p.7; B p.132 / PDF 286)

- A unit with two systems (Gears: Walker primary, Ground "SMS" secondary) may switch systems during its move:
  - only while Stationary or at Combat Speed, never at Top (A). B says Combat Speed only; a Stationary unit is a Combat-speed unit that spent 0 MP, so the two agree in practice.
  - at most once per round, declared during the unit's movement.
- In the switching round, the MP pool comes from the system the unit started with. Hexes entered before the switch cost per the old system's terrain column; hexes after it cost per the new system's column.
- If the MP already spent exceed the new system's allowance, the unit stops at the switch.

Examples:

| Source | Start | MP spent before switch | New system | Result given |
|---|---|---|---|---|
| A p.7 | Ground, Combat 6 | 4 | Walker, Combat 4 | stops (limit reached); had it spent 2, it would have 2 MP left |
| B p.132 | Walker, Combat 7 | 4 | SMS (wheels) | 3 MP left |
| B p.132 | Ground, Combat 7 | 6 | Walker, Combat 5 | 0 MP left; stops |
| B p.132 | Ground 8 (amphibious jeep) | 4 | Naval 6 | 4 MP left (8 - 4) |

- **DIFF (remaining MP):** A's example gives new allowance - MP spent (4 - 2 = 2). B's jeep gives old pool - MP spent (8 - 4 = 4), even though Naval 6 - 4 would give 2. The rule "remaining = min(old pool, new allowance) - MP spent" fits A and B's first two examples, but not the jeep.
- **DIFF (stop condition):** B stops the unit when MP spent are *more than* the new allowance; A's example stops when they are *equal*.
- **UNCLEAR:** B's jeep "crosses half" a 2-hex river with 4 Naval MP. That doesn't fit Naval paying 1 MP per water hex (B p.154); the example is probably wrong.
- **UNCLEAR:** which system and speed apply next round. Presumably the new system at Combat Speed.
- The datacard's first arrow gives the primary system's speeds and the second arrow the secondary's (A p.4). Marker dice 3/4 are primary, 5/6 secondary (A p.5).

Implementation note: track a "switched this round" flag and the starting system's MP pool.

### 5.7 Turning and facing (B p.133 / PDF 287 primary; A p.7)

- A vehicle faces one hexside and moves forward through it, or backward through the opposite hexside when reversing (section 5.4).
- Infantry have no facing (A p.9; B p.136).

Cost of a turn (a change of facing at one point of the path):

| Hexsides turned (shortest way) | Degrees | MP |
|---|---|---|
| 1 | 60 | 0 |
| 2 | 120 | 1 |
| 3 | 180 | 1 |

Any turn up to a full spin costs 1 MP.

- After a turn, the unit must move at least one hex forward before turning again (A p.7; B p.133). So two free 60-degree turns can't be chained in one hex; a 120-degree change in one hex is a single 1 MP turn.
- Exceptions:
  - A stationary unit may adjust its facing by one hexside (B p.131).
  - A command point buys an about-face of up to 3 hexsides at any time (A p.5; B p.129).
- Worked path (A p.7; B p.133): an APC with 10 MP moves 2 Clear hexes (2 MP), turns 1 hexside right (0), moves 3 Rough hexes at 2 MP each (6), turns 2 hexsides left (1), and stops. The last MP is wasted.

**Top-speed sharp turns:**

- **DIFF (trigger):**
  - B: a turn of 3 or more hexsides (180 degrees or more) at Top Speed.
  - A's text: any turn of more than 60 degrees (2 or more hexsides) at Top Speed.
  - Both books' fumble tables call it a "high-speed 180 degree turn".
- The test: Piloting (+ Maneuver) against Threshold = 3 + the MP cost of the terrain. It doesn't count as an action (A p.7; B p.133).
- Success or draw: the turn happens normally.
- Failure: the unit first slides one hex straight ahead in its old facing, then completes the turn. If something occupies that hex, it is a ramming attempt (B p.133; A p.7).
- Fumble: the unit crashes. Its movement ends even if it has MP left. Roll 1d6 for Light or Heavy damage (see the DIFF on the split in section 1.2).
- Own example: a walker with Piloting 2d6 and Maneuver -1 turns 3 hexsides at Top Speed in a Woodland hex (walker cost 1). Threshold = 4. The roll is 3, 5; 5 - 1 = 4, a draw, so the turn succeeds.
- **UNCLEAR:** which terrain counts. Assumed: the hex where the turn is made, using the unit's current movement column. Whether elevation costs are included is not said.
- **UNCLEAR:** whether the 1-hex skid costs MP; what happens if the skid hex is impassable (water, cliff, map edge); whether the turn's 1 MP is still paid when the test fails.

Implementation note: price a turn by the shortest hexside difference accumulated since the last forward step: 0 or 1 is free, 2 or 3 costs 1 MP. Trigger the Top-speed test at 3 hexsides (B) or 2 (A), whichever rule is chosen.

### 5.8 Terrain types and costs (A p.7, p.21; B p.124, p.132-134, p.154-156)

- A hex has exactly one terrain type: the terrain under its center dot (B p.124, p.133).
- A (miniatures) instead uses the terrain under more than half of the model's base, with disputes settled by a die: 1-3 the owner decides, 4-6 the opponent (A p.7). Not needed on hexes.
- MP cost is paid per hex entered, by movement system:

| Terrain | Walker | Ground | Hover | Obscurement | Source / notes |
|---|---|---|---|---|---|
| Clear | 1 | 1 | 1 | 0 | A p.7; B p.132 |
| Rough | 1 | 2 | 1 | 0 | |
| Sand (B: "Sand, Dust") | 2 | 2 | 1 | 0 | |
| Woodland | 1 | 2 | 2 | 1 | |
| Jungle | 2 | 3 | A: 3; B: n/a (can't enter) | 2 | DIFF |
| Swamp | 3 | 4 | 1 | 1 | |
| Water | 2* | 3* | 1 | 2** | |
| Deep Water | 2* | 3* | 1 | 4** | |
| Snow | 2 | 2 | 1 | 0 | B only |
| Deep Snow | 3 | 4 | 1 | 1 | B only |
| Ice | 2 | 3 | 1 | 0 | B only |
| Urban (B: 1-6 buildings in the hex) | 2 | 1 | 2 | 1 | A core p.7; B optional p.156 |
| Dense Urban (B: 7+ buildings) | 3 | 2 | 3 | 2 | same |
| Entering a hex 1 level higher | +2 | +2 | +4 | - | A "up 30 degree slope"; B "+1 elevation" |
| Entering a hex 1 level lower | +1 | +0 | +0 | - | A "down 30 degree slope"; B "-1 elevation" |

**\* Water access:**

- Walkers and ground vehicles may enter water only if they are Amphibious. Any other walker or ground vehicle that enters is flooded and put out of action.
- Amphibious units can't enter or leave water at Top Speed (A p.7; B p.132).
- The Amphibious perk crosses water at its movement type's cost (A p.17).
- B optional rules (B p.154):
  - Naval vessels and submarines pay 1 MP per water hex.
  - Submarines change depth in Deep Water at 2 MP per level.
  - Sealed walkers and ground vehicles that can't float ("bottom crawlers") pay the normal water cost, plus elevation changes in Deep Water.

**\*\* Water obscurement:**

- Applies only to a defender standing in that water hex, and only if the defender isn't a hover (A p.7; B p.132) or isn't a hover, naval or amphibious unit (A p.10; B p.134, p.138).
- Water hexes between the two units add nothing.
- The Aquatic Sensors perk ignores water obscurement (A p.7; B p.154).

**Urban terrain:**

- Damage Point Capacity is 80 for Urban and 100 for Dense Urban: per square inch in A, per building in B (A p.7, p.21; B p.156).
- B: each intervening Urban hex gives -1 to attacks (Dense Urban -2), i.e. its normal obscurement (B p.156).
- A: infantry may enter a building for 1 MP. Vehicles may not enter buildings unless the building has lost at least half its Damage Point Capacity, in which case it costs the same as Rough (A p.21).
- A destroyed structure becomes Rough (B p.154).

**Perks and flaws:**

- Improved Off-Road Ability: -1 MP on any terrain costing more than 1, to a minimum of 1 (A p.17).
- Poor Off-Road Ability: +1 MP when entering terrain that costs 2 or more (A p.17).

**Infantry on foot:** 2 MP, walker column, never more than 2 MP for one hex, no Top Speed (A p.16; B p.150). B also gives jetpack squads 6 MP at hover costs and ATV squads 8 MP at ground costs (B p.150).

- **DIFF:** A's core table includes Urban and Dense Urban but has no Snow, Deep Snow or Ice. B's core table has Snow, Deep Snow and Ice and moves Urban to optional rules. A's weather rules (A p.23-24) treat snow, ice and mud as conditions instead; for example, Mud adds +1 MP to all terrain.
- **DIFF:** Jungle for hovers costs 3 MP in A but is impassable in B.
- **UNCLEAR:** whether elevation surcharges apply per level on multi-level steps (section 5.9).

Implementation note: each HeavyMek terrain type needs a cost lookup per movement system. MegaMek terrain types and levels (for example light and heavy woods) won't map one-to-one.

### 5.9 Elevation, slopes and cliffs (B p.124, p.132-134, p.150, p.156-157; A p.7-8, p.16, p.22)

- Elevation comes in levels of 50 m. Each hex has one level: the level at its center dot (B p.124, p.133).
- Units have no height of their own, and terrain features (woods, buildings) have no height for LOS; only hex levels block LOS (section 6.2). This is inferred: B treats height differences under 50 m as insignificant (B p.124), and neither book gives unit or feature heights.
- Moving up one level costs +2 MP for walkers, +2 for ground and +4 for hover. Moving down one level costs +1 for walkers and nothing for ground or hover (B p.132; A p.7).
- **UNCLEAR:** the core rules don't say what a step of 2 or more levels between neighbouring hexes costs, or whether it is allowed. The obvious reading is a surcharge per level. The optional cliff rule settles it:
- **Cliffs** (optional; B p.157 / PDF 311; A p.22):
  - A change of 2 or more levels between two hexes is a cliff. Vehicles can't climb or descend cliffs.
  - Exception: walkers with an arm strong enough to lift a vehicle of their own Size. They climb or descend 1 level per round, with a Piloting test for each level: Threshold 5 in B, 4 in A (**DIFF**). On a failure or Fumble they fall the remaining levels.
  - Climbing uses the unit's whole MP allowance and counts as spending its full Combat MP.
  - Climbing walkers and infantry take -2 on all defense rolls.
- **Infantry:** may go down cliffs but never up (A p.16; B p.150; B p.157). They can cross at most two elevation changes per hex (A p.16; B p.150).
  - **DIFF (within A):** A's optional cliff text says infantry may climb or descend (A p.22).
- **Falling** (optional): damage = a standard 2d6 roll x Size x levels fallen. A Fumble on that roll means the unit is destroyed (B p.157). A gives the same formula at Tactical scale (A p.22).
- **Elevation as range** (optional, B p.156 / PDF 310):
  - If the level difference is greater than the horizontal distance in hexes, use the level difference as the range.
  - More realistic option: range = sqrt(hexes^2 + levels^2), rounded to the nearest whole number.
- Knocked-down walkers spend 1 MP to stand before they can move (B p.159; A p.22, 2.2.13). Cross-reference.
- Bridges cost the same as Clear. Each bridge hex has its own level, Size capacity and Damage Point Capacity (B p.155; A p.21).

### 5.10 Stacking (B p.133 / PDF 287; A p.8)

- A hex can hold at most 30 Size points of units in total.
- Infantry: each group of up to 5 troopers counts as Size 3, so a full 10-trooper squad counts as 6.
- Units in the same hex fight at Point Blank (range 0) (B p.133). A represents stacking as models whose bases touch (A p.8).
- Friend and foe can therefore share a hex; the rules describe combat inside one.
- A unit above Size 30 occupies several hexes:
  - at least Size / 30, rounded up (Size 61 needs 3 hexes);
  - the actual number of hexes, and the Size points per hex, are fixed when the unit is designed. B's example is a Size 150 carrier taking 7 hexes, about 22 Size points each (B p.133).
- **UNCLEAR:** passing through a hex that is already at the cap; entering an enemy-occupied hex other than by ramming; being forced into an overstacked hex (for example by a skid).

Implementation note: MegaMek's stacking check must become "total Size per hex <= 30", counting 3 points per 5 infantry troopers. Large units need multi-hex footprints.

### 5.11 Optional movement rules (summary only)

These are not core rules. They are listed so the engine design doesn't rule them out.

- **Roads** (A p.21; B p.155):
  - Any unit following a road pays the Clear cost, whatever the terrain underneath.
  - Ground vehicles also get bonus MP equal to half their current speed, rounded down. A requires the whole move to be on road; B lets the bonus MP be spent only along the road. **DIFF.**
  - The small streets inside Urban hexes don't count as roads.
- **Railroads** (A p.21): like roads, but only vehicles with Railroad movement get the bonus, and they must follow the rails.
- **Hull-Down Positions** (A p.20), replacing section 5.3's flat 1 MP:

| Terrain | Extra MP to go hull down |
|---|---|
| Clear | not possible |
| Rough, Woodland | +2 |
| Sand (or moon dust) | +3 |
| Jungle | +1 |
| Swamp | +1 |
| Water (or liquid gases) | +3, only for units that can fully submerge |
| Snow (or frozen gases) | +2 |
| Ice | +3 |
| Urban | +2 |
| Dense Urban | +1 |
| At the edge of an elevation change | +1 |

  - Hull Down gives +1 obscurement, and the unit may still use direct-fire weapons. Full Hull Down gives +2 obscurement, but only indirect-fire weapons and systems may be used.
  - Walkers may go prone: this lowers the hull-down cost by 1, and standing up costs 1 MP. A prone walker can stay hull down while moving by paying the hull-down cost again each time. Infantry count as prone walkers.
  - Aircraft can't go hull down, except VTOLs with stall speed 0 and units with the Nap-of-Earth Flight perk (A p.20).
- **Weather:**
  - B (p.159): light rain gives obscurement 1 beyond 20 hexes; heavy rain gives 2; active scanning reduces either by 1. Sandstorms and blizzards add +1 obscurement per hex, and blizzards also cost +1 MP per hex for units without HEP: Extreme Cold. Tempests add +2 per hex.
  - A (p.23-25) has a fuller and different set: for example sandstorm +2 concealment per inch, light rain +1 per two inches, mud +1 MP everywhere.
  - **DIFF.** Not detailed here.
- **Smoke launchers** (A p.17): fired during movement at no action cost, range 1 inch. They make a cloud of 1-inch radius with obscurement 2 until the end of the turn.
  - **UNCLEAR:** the hex footprint of a 1-inch-radius cloud (one hex, or a hex plus its ring).
- **Camo Netting** (A p.17): +1 Concealment when stationary in Woodland or Jungle.
- **Jump Jets** (A p.17): the unit may jump forward up to the perk's rating in MP, and upward up to half the rating, once per move. Cross-reference to the perk notes.
  - **UNCLEAR:** how "upward MP" converts to elevation levels.

---

## 6. Line of sight and obscurement

### 6.1 When a unit has LOS (A p.8; B p.134 / PDF 288)

LOS means the observer's sensors and fire control can lock on, not literal eyesight. A unit has LOS to a target unless at least one of these holds:

1. The target is beyond the observer's sensor range. A unit without sensors uses unaided vision: 1 km, i.e. 20 hexes (A p.8; B p.135).
2. An intervening hex is at least one level higher than both units.
3. Either unit is in the dead zone of an intervening elevation, i.e. it is next to a rise that lies between the two units.
4. The Concealment between them is greater than the observer's Detection rating. Equal is fine.

- LOS is needed to attack (A p.9; B p.137), to act as forward observer, to call strikes, and so on.
- An active sensor sweep can override condition 4, but not conditions 2 or 3 (A p.8).

### 6.2 Elevation blocking and dead zones on a hex map (A p.8; B p.134)

- **Intervening hexes:** the hexes the straight line between the two hex centers passes through, not counting either unit's own hex. Neither book defines how to trace the line; this is the usual hex convention.
- **Blocking (condition 2):** some intervening hex has a level higher than both the observer's and the target's.
- **Dead zone (condition 3):** a unit is in a dead zone when the intervening hex next to it on the line is higher than its own hex. LOS is then blocked even if the other unit is above that rise.
- Example (A p.8; B p.134): an APC and a Gear are 6 hexes apart, each behind its own ridge. If the Gear climbs its own ridge it still can't see the APC, which sits in the dead zone at the foot of the far ridge. From the top of the far ridge, it can.
- Sensors can't see through elevation, only through obscurement (A p.8).
- **UNCLEAR:** lines that run exactly along a hexside, between two hexes. Neither book covers it. Pick a convention; BattleTech, for example, lets the defender choose the better hex.
- **UNCLEAR:** dead zones against an observer far above the rise. The literal rule still blocks.
- **UNCLEAR:** reading "next to a rise between the units" as "the intervening hex adjacent to the unit on the line" is an interpretation. The alternative, "any higher hex adjacent to the unit", isn't supported by the examples.

Implementation note: MegaMek's LOS code (with heights for woods and buildings) can't be reused as is. HeavyMek blocks LOS only by hex elevation and dead zones, and sums obscurement along the line.

### 6.3 Concealment (A p.8; B p.134 / PDF 288)

- **Base Concealment** = the obscurement of every intervening hex + the obscurement of the defender's own hex (B). The attacker's own hex never counts.
  - A's text mentions only terrain between the units, but A's worked example also adds the defender's Swamp hex, so in practice A agrees with B.
- **Units at different elevations:**
  - B: count only the intervening hexes at the higher of the two units' levels, plus the defender's hex.
  - A: count only the terrain actually on the line of sight, which is vaguer. Use B.
- A counts obscurement per whole inch (A p.8), i.e. per hex here.

Modifiers:

| Item | Change to Concealment | Source |
|---|---|---|
| Stealth perk (defender) | + its rating. By day only if the line already has some obscurement; at night always. | A p.9; B p.136 |
| Hull Down (defender) | +1 against attacks (whether it counts for LOS is UNCLEAR) | A p.6, p.27 |
| Camo Netting | +1 when stationary in Woodland or Jungle | A p.17 |
| Low Profile perk | +1 when behind 2 or more points of obscurement, and when hull down | A p.17 |
| Large Sensor Profile flaw | minus its rating when the defender is searched by enemy active sensors | A p.17 |
| Smoke | cloud obscurement 2 | A p.17 |
| Night (optional, A) | doubled for visual detection | A p.22 |

- Concealment is also the obscurement penalty subtracted from attack rolls (A p.10; B p.138). That belongs in the attack notes.
- Own example: two Woodland hexes lie between the units and the defender stands in Jungle, so Concealment = 1 + 1 + 2 = 4. An observer with Detection 4 still has LOS (4 is not greater than 4).
- Book examples:
  - A p.8: 3 inches of Jungle give 6, which is more than Detection 4, so the defender stays hidden.
  - B p.134: 2 Jungle hexes plus the defender's own Jungle hex give 6, more than 4: hidden.

### 6.4 Detection rating (A p.8; B p.135 / PDF 289)

- **Base Detection** (unaided senses), for every unit including infantry: 4 by day, 2 at night.
- **Passive Sensor value** = the vehicle's Sensor rating + the crew's EW level (its number of dice).
- **Detection rating** = the higher of the two.
- **Range:** unaided vision reaches 1 km (20 hexes). A unit with sensors uses its sensor range instead.
- Own example: Sensors +1 with an Elite crew (EW 3 in A's table) gives a passive value of 4, so Detection is 4 by day and 4 at night.
- Infantry: Detection 4 by day and 2 at night, range 1 km. B: night-vision gear gives 4 at night (A p.16; B p.150).
- **UNCLEAR:** a sensor range shorter than 1 km. By the wording it replaces the 1 km vision range anyway.

### 6.5 Active sensor LOS (A p.8; B p.135 / PDF 289)

- An active sweep takes a full action, and the unit must have sensors. It works through concealment, not through elevation.
- Roll EW skill + Sensor rating against:
  **Threshold = Concealment + the defender's Stealth - 1 per hex the defender moved - 1 per weapon the defender fired this round.**
  A writes it as "Threshold minus Sensor rating", which is equivalent.
- A: the Threshold can't go below 0 after all modifiers (A p.8).
- Success (strictly higher than the Threshold): the unit has LOS to that defender. A draw, failure or Fumble gives no LOS.
- A only: a successful lock grants a free communication action, for example to relay coordinates (A p.2, p.6, p.8).

B adds these modifiers (B p.135):

| Applies to | Modifier | Value |
|---|---|---|
| Observer's roll | Sensor bonus | the Sensor rating (default 0) |
| Observer's roll | Range | -1 per extra range band (see below) |
| Observer's roll | Target sensor profile | per perk or flaw (default 0) |
| Observer's roll | Target sensor signature | +2 per extra range band the target itself used for active sensors this round |
| Threshold | Stealth | + its rating |
| Threshold | Ground movement | -1 per hex the target moved |
| Threshold | Air movement | -1 per 5 Air War hexes the target moved |
| Threshold | Combat | -1 per weapon the target fired |

- **B range boosting** (B p.135):
  - The listed sensor range is the passive range; active sweeps within it take no penalty.
  - Each doubling of the range costs an Active Sensor action and -1 on the roll, and makes the unit easier for enemies to detect (+2 per doubling).
  - Without relays or spotters, the horizon caps the range. High ground extends it; low ground shortens it.
- B: active sensor rolls are also subject to enemy ECM (B p.136). Not in A.
- Own example: two Woodland hexes lie between the units and the defender is in Swamp, so Concealment = 3. The defender moved 4 hexes (-4) and has Stealth 2 (+2), so the Threshold is 3 + 2 - 4 = 1. The observer rolls EW 2d6 (1, 3) for 3, plus Sensors 0 = 3, which beats 1: lock.
- Book examples:
  - A p.8: Threshold 5 - 2 + 2 = 5; the roll is 5 + 1 = 6, so the lock succeeds.
  - B p.135: Threshold 7 - 2 + 3 = 8; the roll is 6 + 1 = 7, so it fails.
- **UNCLEAR:** how long a lock lasts (this round only? until the target moves?), and whether other friendly units may use it for direct fire. A's free communication action suggests passing it on, for example for indirect fire.
- **UNCLEAR:** whether "hexes moved" means the target's movement so far this round or its last recorded move.
- **UNCLEAR:** the order of A's floor at 0 when both Large Sensor Profile and Stealth apply. Assume it is applied after all modifiers.

### 6.6 Communications, ECM and ECCM (A p.9, p.17; B p.136 / PDF 290)

- By default all units communicate freely (comm ranges are 10 km or more) unless enemy ECM is active (A p.9).
- **Communication test** (only when ECM applies): the sender rolls EW + Comm rating and must beat the ECM Threshold. The sender rolls, not the receiver (A p.9; B p.136).
- **Activating ECM or ECCM:** costs an action. Roll EW + the system's rating at once; the total is that system's Threshold. It takes effect from the start of the next round (A p.9; B p.136).
- **Decay:** in A the Threshold drops by 1 each round after that (A p.9). B has no decay. **DIFF.**
- **ECM effect:** if ECM is active and working during the Initiative phase, every enemy unit within the ECM unit's Sensor range (A p.9; B p.136) must beat its Threshold for the whole round:
  - on Communication rolls (A);
  - on Communication and Sensor rolls (B).
  - **DIFF.**
  - **DIFF (within A):** A's ECM perk text gives the range as the vehicle's base communication range instead (A p.17).
- **ECCM:** if active during the Initiative phase, each enemy ECM within the ECCM unit's range compares Thresholds. If the ECCM Threshold is equal or higher, that ECM does nothing this round (A p.9; B p.136).
  - Range: Communication range in A, Sensor range in B. **DIFF.**
  - B: with several ECCM units active, the ECM compares against each of them.
- **Re-rolling a Threshold:**
  - A: a unit may spend as many actions as it likes; the best result is kept for next round.
  - B: each re-roll costs an action, and the newest result stands even if it is lower.
  - **DIFF.**
- **Own side:** in A, a side's ECM doesn't affect its own units (A p.9). In B, a side's ECCM doesn't affect its own ECM (B p.136).
- **Command point transfers** under ECM need a communication test by the commander's unit. Infantry test with their Infantry skill and Comm 0 (B p.136). Infantry have Comm rating -2 and 10 km range (A p.16; B p.150).
- Target designator paints are not affected by ECM (A p.17).
- A has an advanced optional EW section (A p.25, 2.2.33), covered in other notes.
- **UNCLEAR:** in A, when the decay starts (one round after the system takes effect?) and whether it has a minimum.
- **UNCLEAR:** which other A actions need a communication roll beyond CP transfers (forward observer relays, the free communication actions, artillery calls); these are defined in other sections.

### 6.7 Stealth (A p.9; B p.136 / PDF 290)

- The Stealth perk's rating is added to the defender's Concealment.
- By day, only if the line already has some obscurement: stealth doesn't make a unit invisible.
- At night, always.
- B: flying stealth units take -1 per 5 Air War hexes instead of the normal movement modifier. Aircraft are out of scope.
- B optional night rules: stealth divides the distance at which a unit can be heard by its rating (B p.159).
- **UNCLEAR:** whether the defender's own hex counts as "obscurement on the line" for the daytime condition. B counts that hex in Concealment, so presumably yes.

### 6.8 Night (core and optional)

- **Core:** base Detection is 2 at night (A p.8; B p.135), and Stealth always applies at night (A p.9; B p.136).
- **Optional A** (p.22): Concealment is doubled for visual detection. Vehicles without sensors (not infantry) move at half speed and take -1 on weapon fire. Searchlights are allowed.
- **Optional B** (p.159):
  - Use the night Detection value.
  - Infantry without night vision, and vehicles without working sensors, move at half speed and take -1 on all attacks.
  - Units can be heard at a distance: vehicles 2 km (40 hexes), infantry 300 m (6 hexes), naval units 1 km unless moving slower than 2 MP. Stealth divides these distances by its rating (rating 4 means 500 m for a vehicle).
- **Searchlights:**
  - A perk (p.17): at night, the arc the searchlight covers is treated as daylight, out to the searchlight's range.
  - Optional rules (A p.22; B p.159): searchlights are switched on or off at the start of each round. While one is on, any sensor check or attack against that unit counts as daylight, from any direction. Enemies may shoot the light out with an aimed (called) shot.
- **DIFF:** A's optional doubling of Concealment at night has no counterpart in B.
  - **UNCLEAR:** combined with the core night Detection of 2, it may count the darkness twice.

### 6.9 Firing arcs on a hex map (A p.4, p.9, p.27; B p.128, p.136 / PDF 282, 290)

- The arcs: F 180 degrees, Rt 180, L 180, Rr 180, FF 120, T 360.
- The side arcs (Rt, L) include the lines straight ahead and straight behind.
- A weapon with no arc listed uses F (B p.136).
- Infantry have no facing and can fire all round (A p.9; B p.136).

Adjacent hexes in each arc, from B's diagrams (Y = in the arc):

| Arc | rel0 front | rel1 front-right | rel2 rear-right | rel3 rear | rel4 rear-left | rel5 front-left |
|---|---|---|---|---|---|---|
| F | Y | Y | - | - | - | Y |
| Rt | Y | Y | Y | Y | - | - |
| L | Y | - | - | Y | Y | Y |
| Rr | - | - | Y | Y | Y | - |
| T | Y | Y | Y | Y | Y | Y |
| FF | Y | - | - | - | - | - |

FF also covers hexes further out; see below.

**Beyond the adjacent ring.** B only draws adjacent hexes, so this is the geometric reading of the stated angles. Take the bearing from the firer's hex center to the target's hex center, measured from the facing (0 degrees = straight ahead, clockwise):

- F: -90 to +90 degrees. Rr: +90 to +270. Rt: 0 to 180, inclusive. L: 180 to 360, inclusive.
- **UNCLEAR:** hexes exactly at +/-90 degrees. They occur at even distances, for example one step rel1 then one step rel2. Are they in F, in Rr, or in both? Since the side arcs include their center line, "both" fits best.
- **FF:** the arc starts one hex ahead of the unit (B p.128). It contains the rel0 neighbour plus every hex inside the 120-degree wedge that opens from that hex, bounded by the straight hex rows running from it in directions rel5 and rel1 (the rows themselves included). The firer's own rel1 and rel5 neighbours are therefore outside FF.
  - B's diagram fits this reading, but at its one-ring scale it would also fit a 180-degree arc from the hex ahead. The stated 120 degrees decides it.

- **DIFF:** A treats FF as an ordinary F arc (180 degrees) to speed up play. The true 120-degree fixed arc, and fixed side or rear arcs, are optional in A (A p.2, p.9, p.27). B uses the true FF.
- **UNCLEAR:** arcs for targets in the same hex (Point Blank). Neither book says; treating them as inside every arc is a common hex convention.
- Snap fire at a unit outside all the shooter's arcs needs a command point about-face first (A p.6).
- Defense arcs (Front and Rear, used for defense modifiers and armor facing) are in the attack notes (A p.10).

Implementation note: check that MegaMek's BattleTech-oriented arc helpers aren't reused unchanged. HeavyMek needs F and Rr as half-planes, Rt and L as half-planes that include the center line, and FF with its apex moved one hex ahead.

---

## 7. Open questions (UNCLEAR index)

1. Crash damage split, 1-3/4-6 or 1-4/5-6. Each book contradicts itself (sections 1.2, 5.7).
2. Whether a Fumble on A's TCP roll counts as 1 (section 3.3).
3. Command points: reroll scope; whether a failed transfer under ECM loses the point; when an out-of-sequence activation may happen (section 3.3).
4. First-round defense for units declared Stationary or Hull Down at set-up (section 3.2).
5. Order and secrecy of Step One declarations (section 3.4).
6. Snap fire: what "MP allowance" means; whether B lets already-activated units snap fire; non-attack snap actions (section 3.7).
7. When a Standby shot resolves (section 3.8).
8. Whether the multiple-action penalty applies to defense rolls (section 4).
9. Entering a hex with too few MP left (section 5.1).
10. Rounding of "half Combat Speed" (section 5.2).
11. Hull Down at Top Speed; whether Hull Down's +1 counts for LOS/detection (section 5.3).
12. Reverse movement mixed with forward movement and turns (section 5.4).
13. Top Speed: minimum spend blocked; "spend all Combat MP" when the last hex costs too much (section 5.5).
14. MP left after switching movement system; B's jeep example (section 5.6).
15. Turn test: which terrain counts; skid MP and obstacles; whether the turn MP is paid on failure (section 5.7).
16. Steps of 2+ levels without the optional cliff rule (section 5.9).
17. Stacking: passing through full hexes, entering enemy hexes, forced overstacking (section 5.10).
18. LOS along a hexside; dead zones against a high observer; reading of "adjacent" (section 6.2).
19. Sensor lock duration and sharing; timing of "hexes moved"; order of A's floor at 0 (section 6.5).
20. When ECM decay starts in A, and its minimum (section 6.6).
21. Stealth's daytime condition and the defender's own hex (section 6.7).
22. Night concealment counted twice (section 6.8).
23. F/Rr boundary hexes at +/-90 degrees; arcs for same-hex targets (section 6.9).
24. Hex footprint of a smoke cloud; jump jets' upward MP (section 5.11).
25. BR 0 weapons; range to multi-hex targets (section 2.6).
26. Sensor ranges below 1 km (section 6.4).

---

## 8. A vs B differences (DIFF index)

"A vs A" means A contradicts itself; "B vs B" likewise.

| # | Topic | A | B | Section |
|---|---|---|---|---|
| 1 | Crew table | Rookie Gunnery 1/0, Elite Gunnery 4/+1; TV x0.5 / x1 / x3 / x4 / x5; has Legendary, EW and Leadership columns | Rookie Gunnery 1/+1, Elite Gunnery 4/+2; TV x0.25 / x1 / x2.25 / x4; EW = Gunnery - 1 | 1.6 |
| 2 | Skill for physical attacks | Piloting for all | Gunnery for punches and melee weapons; Piloting for kicks | 1.5 |
| 3 | Set-up roll and TCP | Leadership; set-up and TCP are separate rolls | "Tactics" test; one roll gives both | 3.2 |
| 4 | Both commanders lost | best surviving Leadership | next-ranking soldier, level 1 by default | 3.2 |
| 5 | Per-round command points | none (removed on purpose) | ICP = initiative MoS, lost at end of round | 3.3 |
| 6 | CP reroll | yes | no | 3.3 |
| 7 | Who activates first | initiative winner chooses | side with fewer groups chooses; winner if tied | 3.5 |
| 8 | Snap fire | only units not yet activated; costs an attack; CP if it must turn | any enemy unit | 3.7 |
| 9 | Standby | yes | no | 3.8 |
| 10 | Point Blank | touching or within 1 inch | same hex | 2.6 |
| 11 | Ammunition | ROF depletion, ammo optional | ammo tracked | 2.5 |
| 12 | Actions for 4 crew | text says 4; example says 3 (A vs A) | table says 3 | 4 |
| 13 | Hull Down | core: flat 1 MP; optional: terrain table (A vs A) | not present | 5.3, 5.11 |
| 14 | Stationary facing change | not mentioned | 1 hexside allowed | 5.3 |
| 15 | MP after switching movement system | new allowance - MP spent; stops at equal | old pool - MP spent (jeep); stops when greater | 5.6 |
| 16 | Top-speed turn test trigger | more than 60 degrees (text); 180 degrees (fumble table) | 180 degrees or more | 5.7 |
| 17 | Crash damage split | 1-3/4-6 (turning text) vs 1-4/5-6 (table) (A vs A) | same contradiction (B vs B) | 1.2, 5.7 |
| 18 | Terrain table | Jungle hover 3; Urban in the core table; no snow or ice; "30 degree slope" | Jungle hover not allowed; Urban optional; Snow, Deep Snow, Ice; "+/-1 elevation" | 5.8 |
| 19 | Water obscurement exemptions | hover (terrain table) vs hover, naval, amphibious (p.10) (A vs A) | same split, p.132 vs p.134/138 (B vs B) | 5.8 |
| 20 | Cliffs | climb Threshold 4; infantry may climb (optional) vs only descend (infantry rules) (A vs A) | Threshold 5; infantry descend only | 5.9 |
| 21 | Concealment at different elevations | "terrain on the line of sight"; defender's hex only in the example | higher-level hexes plus the defender's hex, explicitly | 6.3 |
| 22 | Active sensors | Threshold floored at 0; free communication action on success | range bands, sensor signature, air movement modifier; subject to ECM | 6.5 |
| 23 | ECM and ECCM | ECM blocks communication only; Threshold decays 1 per round; re-roll keeps the best; ECCM covers its comm range; ECM range: Sensor (p.9) vs comm (p.17) (A vs A) | ECM blocks communication and sensors; no decay; newest re-roll stands; ECCM covers its Sensor range | 6.6 |
| 24 | FF arc | treated as F (180 degrees); true FF optional | 120 degrees, starting one hex ahead | 6.9 |
| 25 | Road bonus MP | whole move must be on road | bonus MP spent only along the road | 5.11 |
| 26 | Night (optional) | Concealment doubled for visual detection | night Detection value plus hearing ranges | 6.8 |
| 27 | Weather (optional) | e.g. sandstorm +2 per inch | e.g. sandstorm +1 per hex | 5.11 |
| 28 | Urban Damage Point Capacity unit | per square inch | per building | 5.8 |
