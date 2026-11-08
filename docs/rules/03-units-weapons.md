# HeavyMek rules notes 03: units, weapons, perks and flaws, Threat Value

Scope: what a Heavy Gear vehicle record holds and how to store it as a plain-text unit file; every vehicle weapon and weapon tag; every perk and flaw with a tactical effect; the Hunter and Jäger for the first duel; an index of the other vehicles; Threat Value (TV); infantry squad records; one-line summaries of the optional rules; and how the 1997 hex rules map to the 2005 miniatures scale. Movement, line of sight, attacks and damage are in notes 01 and 02; they appear here only where a unit or weapon field needs them.

---

## 0. Conventions and sources

- **A** = *Heavy Gear Tactical Miniatures Rules, 2nd Edition* (Dream Pod 9, 2005, DP9-919). Cited by printed page: (A p.29). PDF page = printed page + 1. The text layer is clean; the weapon tables were read from it and spot-checked against the page images (FGC, DPG, LAC, LRP/24, MRP/18, APGL, LGL, SLC, VB, HG rows; dueling table; minefield, wire and point-total tables).
- **B** = *Heavy Gear Rulebook, 2nd Edition* (Dream Pod 9, 1997), image-only scan. Always cited as (B p.N / PDF M) because the offset changes between chapters:
  - Chapter 7, Tactical Rules: PDF = book page + 154 (p.125 = PDF 279, p.163 = PDF 317).
  - Chapters 8-9, Campaigning and Field Guide: PDF = book page + 230 (p.169 = PDF 399, p.188 = PDF 418, p.229 = PDF 459).
- Markers, as in notes 01 and 02: **DIFF:** = the books disagree (or one contradicts itself). **UNCLEAR:** = not stated, ambiguous or illegible. **Implementation note:** = advice for the HeavyMek data format (my suggestion, not a rule from either book).
- Which book wins is set in [README.md](README.md): B wins where the books differ, and A fills B's gaps. The unit files in [megamek/data/heavygear/units/](../../megamek/data/heavygear/units/) use B's value and keep A's differing value as a `DIFF:` comment. (These notes were drafted with A as the base text, so read every DIFF with that policy in mind.)
- Scale: 1 hex = 1 inch in A = 50 m. 1 MP = one Clear hex, about 6 kph. Sensor and comm ranges are in km; hexes = km x 20 (A p.6, p.8; B p.212 / PDF 442 prints "40 hexes/2 km" and "200 hexes/10 km").

### 0.1 What is where (for later stages)

| Material | Where |
|---|---|
| A 1.6 vehicle datacard (field explanations, Grizzly sample) | A p.4 |
| A 1.14 infantry, 1.15 perks, 1.16 flaws | A p.16-17 |
| A chapter 2 optional rules | A p.18-25 |
| A chapter 3 contents: quick-play tables (p.26-27, including a one-page perk/flaw summary on p.26), weapons tables (p.28-29), TV calculation table (p.30), 3.1 TV of custom Gears (p.30), 3.2 scenarios and scenario generator (p.31), 3.3 army list rules (p.32), 3.4-3.12 combat-group army lists for Earth, North and South (p.33-40), 36 tactical datacards, nine per page, two of them infantry squads (p.42-45) | A p.26-45 |
| A 5.2 dueling perks, 5.3 dueling weapons, 5.4 dueling weapon tags | A p.58-59 |
| B 7.2 vehicle record sheet, crew skills, standard crew table | B p.125-127 / PDF 279-281 |
| B 7.4 number of actions (crew-to-actions table) | B p.130 / PDF 284 |
| B 7.7 rate of fire and ammunition use | B p.143-144 / PDF 297-298 |
| B 7.9 infantry, 7.10 Threat Values, 7.11 optional rules, 7.12 miniatures | B p.148-163 / PDF 302-317 |
| B 8.2.1 suggested point totals | B p.169 / PDF 399 |
| B 9.6 record sheet layout | B p.188 / PDF 418 |
| B 9.7 perks and flaws | B p.189-196 / PDF 419-426 |
| B vehicle weapon list | B p.197 / PDF 427 |
| B 9.8 weapon terms and special characteristics | B p.198-199 / PDF 428-429 |
| B weapon descriptions (stat boxes with usual ammo loads) | B p.200-209 / PDF 430-439 |
| B vehicle descriptions and filled record sheets | B p.210-229 / PDF 440-459 (index in section 5) |
| B blank infantry and vehicle record sheets | PDF 465, PDF 466 |
| B tactical quick-reference tables (fire arcs, damage tables) | PDF 461 (no printed page number) |

What B's PDF 318-335 hold: pages inserted from an air-combat supplement, with their own page numbers (about 6 to 25): Air War Tactical Rules (PDF 318-324), Dogfighting Rules (PDF 325-329), and air-oriented Optional Rules (weather and wind, airdrops of troops and cargo, airlifting) (PDF 330-335). No counters, no maps, no ground-unit tables. Not needed for a ground duel; the airdrop pages are the only link to this file (Airdroppable perk). PDF 462-463 are air-war reference tables (including an aircraft weapon list), PDF 464 is a character sheet.

---

## 1. The vehicle record

Notes 01 section 2.2 already explains the A datacard fields, decodes A's Grizzly sample card, and gives the full Size-to-mass chart (01 section 2.4); 01 section 1.6 has the crew-level table and 01 section 4 the crew-to-actions table. This section adds B's extra fields, the abbreviations used on A's cards, and a field list for the unit file.

### 1.1 The two formats

- **A datacard** (A p.4; samples A p.42-45): name banner, faction banner (NORTHERN GUARD, SOUTHERN MILICIA, CEF), armor in a shield icon followed by a row of armor-tally boxes (open boxes = armor points, the rest shaded), one or two speed arrows, then two text lines: `TV: 380 Crew: 1 FireCon: 0 Sens:0/2` and `Size: 6 Actions: 1 Man: 0 Com:0/10`, then a weapon list (Name, Arc, Acc, BR, Dam, ROF, Ammo) and a Perks/Flaws list. A blank box next to the crew is for the crew's skill level. Weapon special tags are not printed on the card; they come from the weapon tables (A p.28-29).
- **B record sheet** (B p.188 / PDF 418; filled sheets B p.211-229; blank PDF 466): a full page. A separate description page per vehicle gives a "Game Statistics" line (TV and its three part-scores, Lemon Dice) and a specifications box. The sheet holds:
  - General Specifications: Threat Value, Size, Cost.
  - Crew Information: pilot name, rank, squadron, affiliation; boxes for Pilot, Gunnery and Electronic Warfare as level/attribute.
  - Crew Data: Crew, Bonus Actions (with a matching Crew Damage box).
  - Systems Data: Primary Combat and Top speed, Secondary Combat and Top speed (each with a type letter, W or G), Maneuver, Deployment Range; Sensors (rating, range), Communication (rating, range), Fire Control; Armor as Light Damage / Heavy Damage / Overkill thresholds.
  - Systems Damage column: blank boxes to write reduced values (speeds, maneuver, fuel spent, electronics, thresholds) and to tick destroyed weapons 01-10.
  - Armor Damage column: boxes in groups of ten to cross off lost armor points. Light damage costs one box, Heavy two (B p.188 / PDF 418).
  - Weapons table: Name (a manufacturer model name), Code (the generic weapon type), Fire Arc, S/M/L/EX range bands, Acc, Dam, Qty, ROF (printed with a sign, e.g. +2), Special; plus an Ammo column with FULL and LEFT boxes.
  - Perks table: Name, Rating, Game Effect, AUX tick. Flaws table: Name, Rating, Game Effect. Defects table: same columns, for unintended faults of a particular model.

### 1.2 Field list for the unit file

| Field | A label | B label | Meaning and use | Notes |
|---|---|---|---|---|
| name | card title | sheet title / Code Name | display name | B adds a variant name, e.g. "Hunter (Mark II)", "Jäger (Alpha)" (B p.212, p.220 / PDF 442, 450) |
| productionCode | - | title suffix, "Production Code" | e.g. HACS-01MG-MP | background only |
| faction | banner | manufacturer, text | army list use | A uses NORTHERN GUARD / SOUTHERN MILICIA / CEF |
| type | - | Use (e.g. "trooper Gear") | gear, strider, tank, APC, infantry... | needed for army lists and rules hooks (walker, ground, hover) |
| threatValue | TV | Threat Value | points cost with a Qualified crew (section 6) | identical in A and B for the Hunter and Jäger |
| tvParts | - | Offensive / Defensive / Miscellaneous scores (description page) | TV = average of the three, rounded (380 = (450 + 298 + 392) / 3) | B p.153 / PDF 307; A p.30 uses the same Defensive and Misc numbers (section 6.4) |
| size | Size | Size | mass class; ramming, kicks (DM = Size), stacking, weapon Min Size, arm lifting, bridges, falling, towing | chart: notes 01 section 2.4 |
| crew | Crew | Crew | crew count; drives actions; crew hits | robots count as crew (A p.4; B p.126) |
| actions | Actions | Bonus Actions (extra actions beyond the automatic one) | actions per round without penalty | A's "Actions 1" = B's "Bonus Actions 0". Crew-to-actions table: B p.130 / PDF 284 (1/2/4/8/16... crew give 1/2/3/4/5... actions) |
| armor | shield number | Light Damage threshold | base armor; thresholds 1x/2x/3x | drops by 1 per Light and 2 per Heavy result (notes 02 section 11.5) |
| maneuver | Man | Maneuver | added to every Piloting roll (defense, physical attacks, turning tests) | |
| fireControl | FireCon | Fire Control | added to every attack roll (not to target designator attacks) | |
| sensors | Sens r/R | Sensors (rating, range) | rating for detection and active sensors; range in km | Hunter 0 / 2 km = 40 hexes |
| communications | Com r/R | Communication (rating, range) | rating for comm rolls against ECM; range in km | Hunter 0 / 10 km = 200 hexes |
| movement | speed arrows, "Combat/Top" | Primary / Secondary, type letter W or G | MP per round per movement system | use the printed pairs: Top is roughly, not exactly, 2x Combat (Hunter Walker 4/7, Ground 6/12) |
| deploymentRangeKm | - | Deployment Range | operational range; no tactical effect | B only |
| perks / flaws | PERKS/FLAWS list | Perks, Flaws, Defects tables | section 3 | store name, count, rating, option, AUX flag |
| weapons | weapon lines | weapon table | section 2 | see 1.3 |
| cost | - | Cost (marks or dinars) | money; roleplaying only | B only |
| lemonDice | - | Lemon Dice (description page) | a roleplaying feature, not used by the tactical rules read for these notes | B only; UNCLEAR what it does |

### 1.3 Weapon line fields

| Field | A | B | Notes |
|---|---|---|---|
| name / model | generic name in the weapon table | model name on the sheet (e.g. "M222 Autocannon" for a LAC) | keep both: the code drives the rules, the model is flavour |
| code | Name column (LAC, LRP/24...) | CODE | key into the weapon table (section 2.4) |
| arc | F, FF, Rt, L, Rr, T | Forward, Fixed F., ... | F, Rt, L, Rr = 180 degrees; FF = 120 degrees ("one hex ahead" in B); T = 360 (A p.9; B PDF 461). A treats FF as plain F for speed (A p.9); see notes 01 section 6.9. One A card prints "FR" (Black Mamba, A p.43): UNCLEAR, probably a fixed rear mount |
| quantity | repeated lines or "2xHMG" | Qty | B's Hunter sheet prints "-" for the hand grenades and its Jäger sheet "1" (B p.213, p.221 / PDF 443, 451) |
| accuracy | Acc | Acc | added to the attack roll |
| damage | Dam (x8) | Dam | Damage Multiplier; damage = MoS x DM |
| baseRange / rangeBands | BR only | S, M, L, EX all printed | bands are normally BR x1/x2/x4/x8 (A p.4; B p.127). **Implementation note:** store all four bands, because a few weapons break the doubling: Medium Flamer 0/0/0/1, Heavy Flamer 0/0/1/2, Morningstar 0/0/0/1, Net Gun 1/2/3/4 (A p.29, p.59) |
| rateOfFire | ROF | ROF (+2) | ROF 0 = single shots. Use of ROF and ammo: notes 02 section 10 |
| ammo | "60/" (the slash leaves room to tally) | FULL / LEFT | A's core rules only track weapons with 5 or fewer shots (A p.12); B tracks every round (B p.127). Melee weapons: none ("-") |
| special | not on card (from table) | Special column | tag list, section 2.3 |

### 1.4 Abbreviations on A's datacards (A p.42-45)

| Card text | Meaning |
|---|---|
| `2 x MAN.ARM 6` | two Manipulator Arms, rating 6 |
| `2 X BATTLE ARM 9`, `3 X TOOL ARM 1` | Battle Arms rating 9; Tool Arms rating 1 |
| `RE.ARMOR 2(F)`, `REINF. ARMOR 4(F)` | Reinforced Armor rating 2 (or 4) on the front |
| `L.SENS.PROFIL 1`, `L.SENSOR PROFILE 1` | Large Sensor Profile flaw, rating 1 |
| `IMP.REAR DEF.`, `IMPR. REAR DEF.` | Improved Rear Defense |
| `IMPR. OFF-ROAD` | Improved Off-Road Ability |
| `HIGH TOWING CAP. (DOUBLE)` | High Towing Capacity, x2 |
| `HEAT ARMOR 5` | HEAT-Resistant Armor rating 5 |
| `TARG.DESIGN. 2`, `TARGET DESIGN. 1` | Target Designator rating 2 (rating = its Base Range) |
| `ECM 3`, `ECCM 2` | ECM / ECCM ratings |
| `AMMO/FUEL CONT.` | Ammo/Fuel Containment |
| `BACKUP SENS.`, `BACKUP COMM.` | Backup Sensors / Backup Communications |
| `REINF. CREW` | Reinforced Crew Compartment |
| `CHASSIS REINF.` | Chassis Reinforcement |
| `STAB. MOUNT x2 FOR LFG` | Stabilizer Mount for the two Light Field Guns |
| `WEAK FACE (R)` | Weak Facing flaw, rear |
| `EXPOSED AUX SYS` | Exposed Auxiliary Systems flaw |
| `VULNERABLE TO HAYWIRE EFECTS` | Vulnerable to Haywire Effects flaw |
| `SEARCHLIGHT 2` | Searchlight; UNCLEAR what the 2 is (probably its range) |
| `2xHMG ... 400ea/` | two HMGs, 400 rounds each |

### 1.5 Suggested unit-file conventions (Implementation note)

- One file per variant. Keys as in `megamek/data/heavygear/units/hunter.yaml`: `name, model, productionCode, faction, type, size, crew, actions, armor, maneuver, fireControl, sensors{rating,rangeKm}, communications{rating,rangeKm}, movement{primary, walker{combat,top}, ground{combat,top}}, threatValue, tvParts{offensive,defensive,miscellaneous}, perks[], flaws[], weapons[]`.
- Perk and flaw entries as objects: `{name, count, rating, option, aux, facing, note}` (for example `option: Desert` for Hostile Environment Protection, `facing: F` for Reinforced Armor, `count: 2` for paired arms).
- Weapon entries: `{name, model, code, arc, quantity, accuracy, damage, baseRange, rangeBands[4], rateOfFire, ammo, special[]}`. Keep `special` as tags from section 2.3, and keep the code so the loader can fill missing values from a shared weapon table.
- Derived values (armor thresholds, range in hexes, crew-adjusted TV) are better computed by the loader than stored.
- Where A and B differ, keep A's value and put B's in a comment marked `DIFF:`, as in the two YAML files.

---

## 2. Weapons

### 2.1 Weapon terms (A p.4, p.28-29; B p.127 / PDF 281, p.198 / PDF 428)

| Term | Meaning |
|---|---|
| Code | short generic name (LAC, LRP/24). For rocket packs the number after the slash is the number of rockets carried (LRP/24 = 24 rockets) |
| Accuracy (Acc) | added to the attack roll; 0 is average, plus is better. Damage can lower it |
| Damage Multiplier (DM, Dam) | damage = Margin of Success x DM. Effectiveness grows much faster than the number: doubling the DM makes a weapon about four times as effective (B compares x2 with x1, A x10 with x5) (B p.198; A p.4) |
| Range / Base Range (BR) | BR is the top of the Short band; Medium, Long and Extreme end at 2x, 4x and 8x BR, in hexes. Range 0 = melee only, used at Point Blank (B p.198). Range modifiers: notes 02 section 2.2 |
| Rate of Fire (ROF) | 0 = one shot per attack. +1 or more allows burst, walking and saturation fire (notes 02 section 10). B's 9.8.1 says ROF 0 weapons have no ROF entry on the record sheet (B p.198), yet the Hunter and Jäger sheets print 0 |
| Minimum Size (Min. Size) | smallest vehicle Size that can mount the weapon (B p.198; A p.30 says to check it when customizing) |
| Ammo | rounds carried. A's tables instead give "Ammo (ea.)", the TV cost of each round (A p.30, worked example) |
| TV Rating (A only) | TV of the weapon itself; used to price custom loadouts (section 6.4) |
| Special | tags, section 2.3 |

ROF and ammunition in one paragraph (details in notes 02 section 10): in A's core rules each burst, walking or saturation attack uses up one point of the weapon's current ROF; a gun regains full ROF with a reload action, while missiles, mortars and the DPG cannot be reloaded in battle; ammo is only tracked for weapons with 5 or fewer rounds (A p.12). In B (and A's optional "old way", A p.18) every round is counted: burst fire uses 10 rounds per ROF point used, rockets and missiles double per ROF point (2, 4, 8, 16), saturation uses 30 rounds or 8 rockets per ROF point, and at least 10 rounds or 4 rockets are needed to saturate a hex (B p.143-144 / PDF 297-298; A p.18).

### 2.2 Range bands in practice

| Band | Distance (hexes) | Example: LAC, BR 2 |
|---|---|---|
| Point Blank | same hex / bases touching | - |
| Short | up to BR | 1-2 |
| Medium | up to 2 x BR | 3-4 |
| Long | up to 4 x BR | 5-8 |
| Extreme | up to 8 x BR | 9-16 |

(Band edges as in A p.10 and B p.127; the modifiers per band are in notes 02 section 2.2.)

### 2.3 Special characteristics (weapon tags)

The "Tag" column is a suggested token for the unit files (Implementation note); the "Book text" column shows how the books abbreviate it.

| Tag | Book text | What it does in play | Source |
|---|---|---|---|
| `AI` | Anti-Infantry, Anti-Inf., Anti-I, AI | No -2 penalty when attacking infantry. If the weapon is also Area Effect, every trooper in the area takes the full damage instead of it working down the squad's track. An infantry squad attacked by an AI weapon must take a morale test (optional morale rules) | A p.16, p.19, p.29; B p.151-152 / PDF 305-306, p.199 / PDF 429 |
| `AE0`, `AE1` | Area Effect, AE=0, AE0 | One attack roll; everyone in the area, friend or foe, defends separately. AE0 = the target hex only (A: a 1-inch circle); AE1 = radius 1 hex (A: a 3-inch circle). In A even a unit that defends successfully takes damage equal to half the DM. For A's area sizes vs hexes see section 9 | A p.13, p.29; B p.199 / PDF 429, p.162 / PDF 316 |
| `G` | Guided | Has every benefit of indirect fire, plus +2 to hit a target painted this round by an allied target designator. B: the designator must be within communication range, and a guided indirect shot at a painted target needs no separate forward observer | A p.29; B p.199 / PDF 429 |
| `IF` | Indirect Fire, Ind. Fire, IF | May fire over obstacles at a target seen by a forward observer (who spends an action and may need a comm roll against ECM). The attack uses the observer's obscurement; on a hit only the target hex's obscurement reduces the MoS; a miss scatters by the MoF. Procedure: notes 02 section 9 | A p.12, p.29; B p.199 / PDF 429 |
| `MR<n>` | Min. Range n, Min Rg n | Cannot attack targets closer than n hexes | A p.29; B p.199 / PDF 429 |
| `DPRB<n>` | -n Dam. per R.B. | Lasers and particle beams: DM drops by n for each range band beyond Short (B: the listed DM holds at Point Blank and Short) | A p.29; B p.199 / PDF 429 |
| `HW` | Haywire | When it causes Light or Heavy damage, roll twice on the Systems Damage Table. Interacts with the Haywire Resistant perk and the Vulnerable to Haywire flaw (section 3) | A p.29; B p.199 / PDF 429 |
| `INC`, `SB` | Incendiary; Slow Burn, Slow Burn Inc. | B: no normal damage; the hit makes a fire whose Intensity = DM + MoS, and vehicles take damage equal to that Intensity (not multiplied). Quick-burn weapons hurt only on the round of the hit; slow-burn weapons repeat the same damage for a number of rounds equal to the MoS (only while the target stays in the hex). Against infantry: (Intensity) x MoS. Fire Resistant perk halves Intensity; Highly Flammable flaw doubles it. A's own definition says only that these weapons are meant to set their targets on fire; A uses Intensity in its firestarting rule, where slow-burn ("persistent") weapons count double | B p.199 / PDF 429, p.157-158 / PDF 311-312; A p.20, p.29 |
| `FRAG` | Frag Ammo, (With Frag. Ammo included) | B: firing fragmentation rounds gives +1 to the attack, halves the DM and adds +2 ROF; it still cannot walk or saturate unless its normal ROF is at least +1. Only the Frag Cannon uses it. **DIFF:** A's table prints the Frag Cannon with frag ammo already applied (x7, Acc +1) but keeps ROF 0 and adds AE=0 | B p.199 / PDF 429; A p.28 |
| `AP` | Armor Piercing | A only. Halve the target's armor (round up) when comparing damage. A thrown vibroblade loses it | A p.29, p.59 |
| `AC` | Armor Crushing | A only. A successful hit strips extra armor: 2 points on Light and 4 on Heavy instead of 1 and 2 | A p.29, p.59 |
| `ENT` | Entangle | A only (defined in the dueling chapter). On a hit the attacker chooses damage or ensnaring; an ensnared vehicle cannot move or fight until it escapes with a Piloting roll against the attack's MoS, modified by (weapon DM - trapped vehicle's Size); arms add half the largest arm's rating, rounded up. Ranged entanglers (Net Gun) are on a cable: MoS 3+ attaches the cable; the winch drags weight up to the DM taken as a Size; the cable has damage points equal to its rating and is hit at -3 by ranged fire. Wire obstacles also use the tag | A p.23, p.59 |
| `CLUMSY` | Clumsy | A only. -1 Maneuver while the weapon is carried | A p.59 |
| `DISP` | Disposable | One-use pack weapons, dropped when empty; the DPG cannot be reloaded (A p.12); B describes the pack gun and anti-personnel mortar as discarded once empty (B p.202 / PDF 432). Panzerfausts (A only) carry the same tag | A p.12, p.28; B p.197 / PDF 427 |
| `MELEE` | Physical Attack (Only) | Used only as a physical attack (Piloting roll + Maneuver, at Point Blank). Chassis Reinforcement adds +1 to the DM of punches and kicks instead of having its own DM | B p.197 / PDF 427, p.206 / PDF 436; A p.11, p.29 |
| `THROWN` | footnotes *, **, *** | Vibroblade, vibroax and grenades can be thrown 1 hex with Piloting; to recover a thrown blade or axe the unit must enter that hex and spend an action next turn | A p.29, p.59 |
| `HEAT` | (class, not printed on weapons) | Weapons that count as HEAT for HEAT-Resistant Armor: bazookas, mortars, rockets and grenades, missiles, lasers, particle beams. Not HEAT: cannons, railguns, infantry rifles, punches and kicks, ramming, melee weapons. Also used as a minefield tag in A. **DIFF:** A's list (A p.17) names bazookas, mortars, grenades, missiles, lasers and particle beams but not rockets | B p.191 / PDF 421; A p.17, p.23 |

Notes on the tags:
- A's wording for Area Effect gives circle diameters (1 inch, 3 inches); B gives hex radii. Both lead to "target hex only" for AE0 and "target hex plus the six around it" for AE1 on a hex map.
- Haywire in roleplaying terms also deals an electrical attack (B p.199); not needed for tactical play.
- "Physical Attack Only", "Disposable", "Slow Burn" and "Entangle" are listed as undefined in notes 02 (UNCLEAR item 26); the rows above give the definitions found in B and in A's dueling chapter.

### 2.4 Complete vehicle weapons table

All vehicle weapons in A's tables (A p.28-29, 87 entries) plus the ten extra weapons in A's dueling list (A p.59), compared with B's list (B p.197 / PDF 427, 44 entries, all of which also appear in A). Columns:
- "Range S/M/L/Ex" in hexes; "BR" = Short band.
- "TV (A)" and "Ammo TV ea. (A)" price the weapon and each round of its ammunition (section 6.4).
- "In B list?": "yes, same" means B prints the identical range, DM, Acc, ROF, Min Size and tags; otherwise the differing B values are shown and marked **DIFF:**.
- "B usual load": the "Usual Ammo Load" (or "Usual Energy Charge" for energy weapons) from B's weapon descriptions (B p.200-209 / PDF 430-439). It is a typical load, not a rule; the actual ammo is on each vehicle's card.
- Names are A's. B's list uses the same names with small variations (e.g. "AP Grenade Launcher", "Fragmentation Cannon", "Heavy Particle Accelerator"); B's description page calls the DPG a "Disposable Pack Gun" while its table says "Deployable Pack Gun".

#### Cannons, guns and recoilless weapons (A p.28 / PDF 29)

| Code | Name (A) | Acc | DM | Range S/M/L/Ex | BR | ROF | Min Size | Special (A) | TV (A) | Ammo TV ea. (A) | In B list (p.197)? | B usual load (B p.200-209) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| VLMG | Very Light Machinegun | 0 | x2 | 1/2/4/8 | 1 | +3 | 2 | Anti-Inf. | 25 | 0.02 | no (A only) | - |
| LMG | Light Machine Gun | 0 | x3 | 1/2/4/8 | 1 | +4 | 3 | Anti-Inf. | 52 | 0.05 | yes, same | 800-5000 shells (p.200) |
| HMG | Heavy Machine Gun | 0 | x4 | 1/2/4/8 | 1 | +3 | 3 | Anti-Inf. | 59 | 0.09 | yes, same | 400-3000 shells (p.200) |
| FGC | Frag. Cannon | +1 | x7 | 1/2/4/8 | 1 | 0 | 5 | (With Frag. Ammo included) Anti-Inf., AE=0 | 195 | 0.75 | **DIFF:** B DM x14, Acc 0, Special 'Anti-Inf., Frag Ammo' | 16 shells (p.202) |
| VLRF | Very Light Rifle | 0 | x6 | 2/4/8/16 | 2 | 0 | 3 | - | 44 | 0.18 | no (A only) | - |
| LRF | Light Rifle | 0 | x8 | 3/6/12/24 | 3 | 0 | 4 | - | 92 | 0.36 | no (A only) | - |
| MRF | Medium Rifle | 0 | x10 | 4/8/16/32 | 4 | 0 | 4 | - | 165 | 0.66 | no (A only) | - |
| HRF | Heavy Rifle | 0 | x12 | 4/8/16/32 | 4 | 0 | 5 | - | 209 | 0.85 | no (A only) | - |
| DPG | Deployable Pack Gun | -1 | x8 | 2/4/8/16 | 2 | +2 | 3 | Disposable | 58 | 0.15 | yes, same | 30 shells (p.202) |
| LAAC | Light Anti-Air. Cannon | 0 | x8 | 2/4/8/16 | 2 | +6 | 4 | - | 121 | 0.28 | no (A only) | - |
| MAAC | Medium Anti-Air. Cannon | 0 | x10 | 3/6/12/24 | 3 | +4 | 4 | - | 176 | 0.51 | no (A only) | - |
| HAAC | Heavy Anti-Air. Cannon | 0 | x12 | 3/6/12/24 | 3 | +3 | 5 | - | 230 | 0.68 | no (A only) | - |
| VLAC | Very Light Autocannon | 0 | x6 | 2/4/8/16 | 2 | +2 | 3 | - | 69 | 0.18 | no (A only) | - |
| LAC | Light Autocannon | 0 | x8 | 2/4/8/16 | 2 | +2 | 4 | - | 108 | 0.28 | yes, same | 60 shells (p.200) |
| MAC | Medium Autocannon | 0 | x10 | 3/6/12/24 | 3 | +1 | 4 | - | 163 | 0.51 | yes, same | 50 shells (p.201) |
| HAC | Heavy Autocannon | 0 | x12 | 3/6/12/24 | 3 | +1 | 5 | - | 220 | 0.68 | yes, same | 30 shells (p.201) |
| VHAC | Very Hvy Autocannon | 0 | x15 | 3/6/12/24 | 3 | +1 | 5 | - | 324 | 1.01 | no (A only) | - |
| LAG | Light Artillery Gun | -2 | x12 | 25/50/100/200 | 25 | +1 | 8 | Ind. Fire, AE=0, Min. Range 10 | 1107 | 4.4 | no (A only) | - |
| VLFG | Very Lt Field Gun | -1 | x20 | 5/10/20/40 | 5 | 0 | 6 | Indirect Fire | 525 | 2.10 | no (A only) | - |
| LFG | Light Field Gun | 0 | x22 | 5/10/20/40 | 5 | 0 | 8 | Indirect Fire | 915 | 3.65 | yes, same | 50 shells (p.201) |
| HFG | Heavy Field Gun | 0 | x28 | 8/16/32/64 | 8 | 0 | 10 | Indirect Fire | 1945 | 7.78 | yes, same | 40 shells (p.201) |
| VHFG | Very Hvy Field Gun | 0 | x33 | 10/20/40/80 | 10 | 0 | 12 | Indirect Fire | 3134 | 12.54 | no (A only) | - |
| SC | Snub Cannon | -1 | x28 | 1/2/4/8 | 1 | 0 | 6 | - | 524 | 2.10 | yes, same | 3 shells (p.202) |
| LPZ | Light Panzerfaust | -1 | x10 | 1/2/4/8 | 1 | 0 | 2 | Disposable | 14 | - | no (A only) | - |
| MPZ | Medium Panzerfaust | -1 | x15 | 1/2/4/8 | 1 | 0 | 2 | Disposable | 30 | - | no (A only) | - |
| HPZ | Heavy Panzerfaust | -1 | x20 | 2/4/8/16 | 2 | 0 | 3 | Disposable | 55 | - | no (A only) | - |
| RFB | Rapid-Fire Bazooka | 0 | x14 | 1/2/4/8 | 1 | +2 | 4 | - | 278 | 0.79 | yes, same | 30 rockets (p.207) |
| LBZK | Light Bazooka | 0 | x15 | 2/4/8/16 | 2 | 0 | 4 | - | 234 | 0.93 | yes, same | 5 rockets (p.206) |
| MBZK | Medium Bazooka | 0 | x20 | 2/4/8/16 | 2 | 0 | 4 | - | 409 | 1.63 | yes, same | 3 rockets (p.207) |
| HBZK | Heavy Bazooka | 0 | x25 | 2/4/8/16 | 2 | 0 | 5 | - | 634 | 2.53 | yes, same | 3 rockets (p.207) |

#### Rockets and missiles (A p.28 / PDF 29)

| Code | Name (A) | Acc | DM | Range S/M/L/Ex | BR | ROF | Min Size | Special (A) | TV (A) | Ammo TV ea. (A) | In B list (p.197)? | B usual load (B p.200-209) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| VLRP/8 | Very Lt. Rocket Pack/8 | -1 | x8 | 1/2/4/8 | 1 | +3 | 3 | Indirect Fire | 114 | 0.26 | no (A only) | - |
| VLRP/32 | Very Lt. Rocket Pack/32 | -1 | x8 | 1/2/4/8 | 1 | +4 | 3 | Indirect Fire | 129 | 0.26 | no (A only) | - |
| VLRP/128 | Very Lt. Rocket Pack/128 | -1 | x8 | 1/2/4/8 | 1 | +6 | 3 | Indirect Fire | 165 | 0.26 | no (A only) | - |
| LRP/8 | Light Rocket Pack/8 | -1 | x12 | 1/2/4/8 | 1 | +1 | 3 | Indirect Fire | 194 | 0.58 | yes, same | 8 rockets (pack sizes listed: 8, 16, 24 or 32) (p.204) |
| LRP/16 | Light Rocket Pack/16 | -1 | x12 | 1/2/4/8 | 1 | +2 | 3 | Indirect Fire | 209 | 0.58 | yes, same | 16 rockets (pack sizes listed: 8, 16, 24 or 32) (p.204) |
| LRP/24 | Light Rocket Pack/24 | -1 | x12 | 1/2/4/8 | 1 | +3 | 3 | Indirect Fire | 226 | 0.58 | yes, same | 24 rockets (pack sizes listed: 8, 16, 24 or 32) (p.204) |
| LRP/32 | Light Rocket Pack/32 | -1 | x12 | 1/2/4/8 | 1 | +4 | 3 | Indirect Fire | 245 | 0.58 | yes, same | 32 rockets (pack sizes listed: 8, 16, 24 or 32) (p.204) |
| MRP/9 | Med. Rocket Pack/9 | -1 | x18 | 2/4/8/16 | 2 | +1 | 4 | Indirect Fire | 425 | 1.33 | yes, same | 9 rockets (pack sizes listed: 9, 18 or 36) (p.204) |
| MRP/18 | Med. Rocket Pack/18 | -1 | x18 | 2/4/8/16 | 2 | +3 | 4 | Indirect Fire | 469 | 1.33 | yes, same | 18 rockets (pack sizes listed: 9, 18 or 36) (p.204) |
| MRP/36 | Med. Rocket Pack/36 | -1 | x18 | 2/4/8/16 | 2 | +4 | 4 | Indirect Fire | 494 | 1.33 | yes, same | 36 rockets (pack sizes listed: 9, 18 or 36) (p.204) |
| IRP/10 | Inc. Rocket Pack/10 | -1 | x13 | 1/2/4/8 | 1 | +1 | 4 | Ind. Fire, Slow Burn Inc. | 339 | 1.02 | yes, same | 10 rockets (pack sizes listed: 10, 20 or 30) (p.205) |
| IRP/20 | Inc. Rocket Pack/20 | -1 | x13 | 1/2/4/8 | 1 | +2 | 4 | Ind. Fire, Slow Burn Inc. | 363 | 1.02 | yes, same | 20 rockets (pack sizes listed: 10, 20 or 30) (p.205) |
| IRP/30 | Inc. Rocket Pack/30 | -1 | x13 | 1/2/4/8 | 1 | +3 | 4 | Ind. Fire, Slow Burn Inc. | 390 | 1.02 | yes, same | 30 rockets (pack sizes listed: 10, 20 or 30) (p.205) |
| HRP/24 | Heavy Rocket Pack/24 | -1 | x20 | 3/6/12/24 | 3 | +3 | 5 | Indirect Fire | 596 | 1.71 | yes, same | 24 rockets (pack sizes listed: 24 or 48) (p.204) |
| HRP/48 | Heavy Rocket Pack/48 | -1 | x20 | 3/6/12/24 | 3 | +4 | 5 | Indirect Fire | 623 | 1.71 | yes, same | 48 rockets (pack sizes listed: 24 or 48) (p.204) |
| HIRP/24 | Hvy Inc. Rocket Pack/24 | -1 | x16 | 3/6/12/24 | 3 | +3 | 5 | Ind. Fire, Slow Burn Inc. | 606 | 1.70 | no (A only) | - |
| HIRP/48 | Hvy Inc. Rocket Pack/48 | -1 | x16 | 3/6/12/24 | 3 | +4 | 5 | Ind. Fire, Slow Burn Inc. | 641 | 1.70 | no (A only) | - |
| AAM | Anti-Aircraft Missile | +1 | x10 | 8/16/32/64 | 8 | 0 | 7 | Guided, Min. Range 10 | 2066 | 8.30 | no (A only) | - |
| ABM | Airburst Missile | 0 | x10 | 3/6/12/24 | 3 | 0 | 6 | AE=1 | 508 | 2.00 | no (A only) | - |
| AGM | Anti-Gear Missile | +1 | x15 | 3/6/12/24 | 3 | 0 | 4 | Guided,Indirect Fire | 756 | 15.12 | no (A only) | - |
| ATM | Anti-Tank Missile | +1 | x25 | 3/6/12/24 | 3 | 0 | 6 | Guided, Indirect Fire | 1956 | 39.12 | yes, same | 1 missile (p.205) |
| HATM | Heavy AT Missile | +1 | x30 | 5/10/20/40 | 5 | 0 | 9 | Guided, Indirect Fire | 3075 | 61.50 | yes, same | none given (no B description box) |

#### Support weapons: flamers, mortars, grenade launchers (A p.29 / PDF 30)

| Code | Name (A) | Acc | DM | Range S/M/L/Ex | BR | ROF | Min Size | Special (A) | TV (A) | Ammo TV ea. (A) | In B list (p.197)? | B usual load (B p.200-209) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| LFL | Light Flamer | +1 | x5 | 0/0/0/0 | 0 | 0 | 2 | Slow Burn | 14 | 0.06 | no (A only) | - |
| MFL | Med Flamer | +1 | x7 | 0/0/0/1 | 0 | +1 | 2 | Slow Burn, Indirect Fire | 68 | 0.17 | no (A only) | - |
| HFL | Heavy Flamer | +1 | x9 | 0/0/1/2 | 0 | +2 | 3 | Slow Burn, Ind. Fire | 134 | 0.27 | no (A only) | - |
| APM | Anti-Personnel Mortar | 0 | x4 | 2/4/8/16 | 2 | 0 | 3 | Anti-Inf., Ind. Fire, AE=0, Min Rg 2 | 95 | 0.36 | yes, same | 9 shells (p.202) |
| LGM | Light Guided Mortar | -1 | x15 | 3/6/12/24 | 3 | 0 | 4 | Guided, Ind. Fire, Min Range 3 | 304 | 6.05 | yes, same | 12 shells (p.203) |
| HGM | Heavy Guided Mortar | -1 | x20 | 5/10/20/40 | 5 | 0 | 5 | Guided, Ind. Fire, Min Range 5 | 632 | 12.60 | yes, same | 6 shells (p.203) |
| LFM | Lt. Field Mortar | -1 | x15 | 4/8/16/32 | 4 | 0 | 5 | Indirect Fire, AE=0, Min Rg 4 | 522 | 2.09 | no (A only) | - |
| MFM | Med. Field Mortar | -1 | x20 | 5/10/20/40 | 5 | 0 | 6 | Indirect Fire, AE=0, Min Rg 5 | 947 | 3.79 | no (A only) | - |
| HFM | Hvy Field Mortar | -1 | x25 | 6/12/24/48 | 6 | 0 | 7 | Indirect Fire, AE=0, Min Rg 6 | 1516 | 6.06 | no (A only) | - |
| APGL | Anti-Personnel G.L. | -1 | x3 | 1/2/4/8 | 1 | 0 | 2 | Anti-Inf., Indirect Fire, AE=0 | 29 | 0.10 | yes, same | 6 grenades (p.203) |
| LGL | Light G.L. | -1 | x15 | 1/2/4/8 | 1 | +2 | 4 | Indirect Fire | 316 | 0.90 | **DIFF:** B Min Size 0 | 20 grenades (p.203) |
| HGL | Heavy G.L. | -1 | x20 | 2/4/8/16 | 2 | +1 | 5 | Indirect Fire | 529 | 1.63 | yes, same | 12 grenades (p.204) |

#### Advanced weapons: particle accelerators, railguns, lasers (A p.29 / PDF 30)

| Code | Name (A) | Acc | DM | Range S/M/L/Ex | BR | ROF | Min Size | Special (A) | TV (A) | Ammo TV ea. (A) | In B list (p.197)? | B usual load (B p.200-209) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| LPA | Light Particle Acc. | +1 | x10 | 2/4/8/16 | 2 | 0 | 6 | -1 Dam. per R.B., Haywire | 270 | 1.07 | yes, same | energy: 20 shots (p.209) |
| HPA | Heavy Particle Acc. | +1 | x15 | 3/6/12/24 | 3 | 0 | 8 | -1 Dam. per R.B., Haywire | 672 | 2.68 | yes, same | energy: 15 shots (p.209) |
| LRG | Light Railgun | 0 | x14 | 5/10/20/40 | 5 | +2 | 7 | - | 603 | 1.93 | yes, same | energy: 60 shots (p.208) |
| HRG | Heavy Railgun | 0 | x35 | 10/20/40/80 | 10 | 0 | 12 | - | 3339 | 13.35 | yes, same | energy: 24 shots (p.209) |
| SLC | Sniper Laser Cannon | +1 | x12 | 5/10/20/40 | 5 | 0 | 4 | -1 Dam. per R.B. | 371 | 1.48 | **DIFF:** B Min Size 14 | energy: 20 shots (p.208) |
| HGLC | Gatling Laser | +1 | x16 | 2/4/8/16 | 2 | +1 | 4 | -3 Dam. per R.B. | 350 | 1.06 | no (A only) | - |
| LLC | Light Laser Cannon | +1 | x16 | 5/10/20/40 | 5 | 0 | 5 | -2 Dam. per R.B. | 483 | 1.93 | yes, same | energy: 15 shots (p.208) |
| HLC | Heavy Laser Cannon | +1 | x20 | 5/10/20/40 | 5 | 0 | 5 | -3 Dam. per R.B. | 623 | 2.48 | yes, same | energy: 10 shots (p.208) |
| LPLC | Lt Pulse Laser Cannon | +1 | x20 | 3/6/12/24 | 3 | 0 | 5 | -3 Dam. per R.B. | 474 | 1.90 | no (A only) | - |
| HPLC | Hvy Pulse Laser Cannon | +1 | x24 | 3/6/12/24 | 3 | 0 | 5 | -4 Dam. per R.B. | 642 | 2.57 | no (A only) | - |

#### Close combat weapons (A p.29 / PDF 30)

| Code | Name (A) | Acc | DM | Range S/M/L/Ex | BR | ROF | Min Size | Special (A) | TV (A) | Ammo TV ea. (A) | In B list (p.197)? | B usual load (B p.200-209) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| CR | Chassis Reinfor. | 0 | +1 Dam. | 0/0/0/0 | 0 | 0 | n/a | Physical Attack Only | Veh. Size | n/a | yes, same | n/a (p.206) |
| MF | Mauler Fist | +1 | x9 | 0/0/0/0 | 0 | 0 | 3 | Armor Crushing | 61 | n/a | no (A only) | - |
| HWP | Haywire Whip | +1 | x7 | 0/0/0/0 | 0 | 0 | 4 | Entangle, Haywire | 110 | 0.44 | no (A only) | - |
| CS | Chain Sword | 0 | x9 | 0/0/0/0 | 0 | 0 | 3 | - | 20 | n/a | no (A only) | - |
| VB | Vibroblade* | 0 | x8 | 0/0/0/0 | 0 | 0 | 3 | Armor Piercing | 16 | n/a | **DIFF:** B Special 'Physical Attack Only' | N/A (p.205) |
| VR | Vibrorapier | +1 | x6 | 0/0/0/0 | 0 | 0 | 3 | Armor Piercing | 27 | n/a | no (A only) | - |
| VA | Vibroax** | -1 | x10 | 0/0/0/0 | 0 | 0 | 3 | Armor Crushing | 34 | n/a | no (A only) | - |
| SKG | Spike Gun | -1 | x12 | 0/0/0/0 | 0 | 0 | 3 | - | 38 | 0.15 | no (A only) | - |
| HSKG | Heavy Spike Gun | -1 | x14 | 0/0/0/0 | 0 | 0 | 4 | Armor Piercing | 66 | 0.27 | no (A only) | - |
| HWG | Haywire Grenade*** | -1 | x10 | 0/0/0/0 | 0 | 0 | 2 | Haywire | 10 each | n/a | no (A only) | - |
| HG | Hand Grenade*** | -1 | x15 | 0/0/0/0 | 0 | 0 | 2 | Anti-Infantry | 11 each | n/a | yes, same | 1 grenade (often carried in 3- or 6-packs) (p.206) |
| HHG | Heavy Grenade*** | -1 | x25 | 0/0/0/0 | 0 | 0 | 2 | - | 21 each | n/a | no (A only) | - |
| SDG | Self-Destruct Grenade*** | -1 | x30 | 0/0/0/0 | 0 | 0 | 2 | AE=0 | 60 each | n/a | no (A only) | - |

#### Dueling weapons list (A p.59 / PDF 60) - only the entries not already above

| Code | Name (A) | Acc | DM | Range S/M/L/Ex | BR | ROF | Min Size | Special (A) | TV (A) | Ammo TV ea. (A) | In B list (p.197)? | B usual load (B p.200-209) |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| VC | Vibroclaws | +1 | x8 | 0/0/0/0 | 0 | 0 | 2 | - | 24 | N/A | no (A only) | - |
| LN | Lance | -1 | x13 | 0/0/0/0 | 0 | 0 | 3 | - | 28 | N/A | no (A only) | - |
| HLN | Heavy Lance | -2 | x15 | 0/0/0/0 | 0 | 0 | 3 | Armor Crushing, Clumsy | 28 | N/A | no (A only) | - |
| ELN | Explosive Lance | -1 | x14 | 0/0/0/0 | 0 | 0 | 3 | - | 33 | 0.14 | no (A only) | - |
| HWLN | Haywire Lance | +1 | x9 | 0/0/0/0 | 0 | 0 | 3 | Haywire | 61 | 0.25 | no (A only) | - |
| WP | Whip | +1 | x7 | 0/0/0/0 | 0 | 0 | 3 | Entangle | 55 | N/A | no (A only) | - |
| NG | Net Gun | 0 | x6 | 1/2/3/4 | 1 | 0 | 5 | Entangle | 222 | 0.89 | no (A only) | - |
| MC | Mace | -1 | x8 | 0/0/0/0 | 0 | 0 | 2 | - | 11 | N/A | no (A only) | - |
| MS | Morningstar | -2 | x10 | 0/0/0/1 | 0 | 0 | 3 | Entangle | 38 | N/A | no (A only) | - |
| PP | Piston Punch | 0 | x10 | 0/0/0/0 | 0 | 0 | 2 | Clumsy | 13 | N/A | no (A only) | - |

Notes on the table:
- **DIFF:** Frag Cannon: B x14, Acc 0, ROF 0, tags Anti-Inf. and Frag Ammo; A x7, Acc +1, ROF 0, tags Anti-Inf. and AE=0 "with frag ammo included". B's frag rule (section 2.3) applied to B's base line gives x7, +1 and ROF +2, so A applied two of the three frag effects, dropped the ROF bonus and added AE=0.
- **DIFF:** Light Grenade Launcher Min Size: B 0, A 4. B's 0 is almost certainly a misprint (every other launcher is 2 or more).
- **DIFF:** Sniper Laser Cannon Min Size: B 14, A 4. B's 14 is probably a misprint for 4 (the Light Laser Cannon is Min Size 5).
- **DIFF:** Vibroblade: B tags it Physical Attack Only (and the Hunter sheet says "Physical Attack"); A tags it Armor Piercing (lost if thrown) and lets it be thrown 1 hex.
- B has one description box per rocket-pack family, listing all pack sizes (Light Rocket Packs: 8, 16, 24 or 32 rockets); the load of a given code is its suffix.
- Hand grenades are priced per grenade in A (11 TV each) and carried in small numbers (Hunter and Jäger: 3).
- "TV (A)" for Chassis Reinforcement is "Veh. Size": its TV equals the vehicle's Size.
- No fleet-scale (landship) weapons are included: A lists them only on its fleet datacards (A chapter 6, p.76 onward). Aircraft weapons (B PDF 462-463) and personal-scale weapons (A chapter 4) are also left out.

### 2.5 Infantry weapons (A p.16; B p.149 / PDF 303)

Both books give the same numbers. Infantry do not track ammunition (B p.149).

| Weapon | Type | Acc | DM | Range S/M/L/Ex | ROF | Tags |
|---|---|---|---|---|---|---|
| 7mm Rifle | standard | 0 | x2 | 1/2/4/8 | 0 | |
| 7mm Assault Rifle | standard | 0 | x2 | 1/2/4/8 | 1 | |
| 9mm Heavy Rifle | standard | 0 | x3 | 1/2/4/8 | 0 | |
| 15mm Sniper Rifle | standard | +1 | x4 | 2/4/8/16 | 0 | |
| 9mm Light Machine Gun | heavy | 0 | x3 | 2/4/8/16 | 2 | |
| 9mm Chaingun | heavy | 0 | x3 | 1/2/4/8 | 4 | |
| 24mm Anti-HG Rifle | heavy | +1 | x7 | 3/6/12/24 | 0 | |
| 37mm Grenade Rifle | heavy | 0 | x8 | 1/2/4/8 | 0 | IF (A only) |
| 50mm Rocket Launcher | heavy | 0 | x14 | 1/2/4/8 | 0 | |
| 62mm Light Mortar | heavy | -1 | x12 | 3/6/12/24 | 0 | IF (A only); cannot fire at range 2 or less |
| Sniper Laser Rifle | heavy | +1 | x4 | 4/8/16/32 | 0 | |

- **DIFF:** A marks the 37mm Grenade Rifle and 62mm Light Mortar as indirect-fire weapons (A p.16); B's table has no such mark (B p.149).
- Massed-fire ROF bonus and how squads attack: section 7.

---

## 3. Perks and flaws

### 3.1 Conventions

- **AUX**: the perk is an Auxiliary System, so Auxiliary System hits on the Systems Damage Table can knock it out (A p.17; B p.189 / PDF 419). A's auxiliary-systems subtable picks what is hit: 1 Target Designator, 2 Autopilot, 3 Smoke Launchers (each hits Sensors instead if the vehicle lacks it or it is already gone), 4 ECM, 5 ECCM, 6 Satellite Uplink (each hits Communications instead) (A p.27; full table in notes 02 section 12.5).
- **R**: the perk has a rating, printed next to its name (B p.189 / PDF 419). A shows the rating after the name (`ECM 3`).
- "Ignore the first X hit" perks are used up when they work; B lets a technician restore them between battles.
- Sources: A p.17 (repeated in short form on A p.26); A p.58 for dueling perks; B p.189-196 / PDF 419-426.
- Column "In": A, B or both. Rows where the effects differ are marked **DIFF:**.

### 3.2 Perks with a tactical effect

| Perk | Tags | In | Effect | Source |
|---|---|---|---|---|
| Acrobatic Handling | AUX, R | A | Add the rating to Maneuver for close-combat attacks and defenses and when defending against Point Blank attacks | A p.17, p.58 |
| Advanced Controls | | A, B | One extra action per round (B: as long as at least one crewman is aboard) | A p.17; B p.189 |
| Advanced Neural Net | | A | Once per turn, spend an action to get +1 Maneuver until the end of the turn | A p.17 |
| Airdroppable | | A, B | **DIFF:** A: may be deployed anywhere on the table at the start of the game, out of enemy LOS. B: background only; refers to the air supplement for paradrop rules | A p.17; B p.189 |
| Ammo/Fuel Containment (System) | | A, B | Cancels the first "Ammo/Fuel Hit" on the Fire Control table; the armor loss still applies (A) | A p.17; B p.189 |
| Ammo Storage | | B | Armored storage for spare clips; without a manipulator arm or ammo tool arm the crew reloads by hand at one shot per action; stored clips are not AUX | B p.189 |
| Amphibious | | A, B | May cross Water (and Deep Water, B) at its own movement type's cost; not a submarine. Walkers and ground vehicles only | A p.17; B p.189 |
| Anti-Personnel Charges | R | A | Fired at Point Blank against an attacker without spending an action: 2 dice + rating as the attack, DM x3, counts as Anti-Infantry; each use spends 1d6 charges | A p.17 |
| Aquatic Sensors | AUX | B | No obscurement from Water or Deep Water while in water (some aircraft versions within a set distance) | B p.189 |
| Artificial Intelligence | R | B | Counts as one crewman with skill = rating; no live crew needed; immune to Crew hits | B p.189 |
| Automation | R | A, B | Adds the rating to the crew count (more actions). A: with no live crew left the vehicle needs remote control; B: needs at least one live crewman or the AI perk | A p.17; B p.190 |
| Autopilot | AUX | A, B | Keeps the vehicle moving straight or turning one hexside; can ram large targets as a Skill 1 pilot with the crew bailed out. **DIFF:** A says it dodges as a Skill 1 pilot; B says it cannot dodge or fire | A p.17; B p.190 |
| Backup Communications | | A, B | Ignore the communications effect of the first Auxiliary System hit | A p.17; B p.190 |
| Backup Fire Control | | B | Ignore the first "Fire Control Destroyed" result | B p.190 |
| Backup Life Support | | B | Life support survives the loss of all auxiliary systems (needs Life Support) | B p.190 |
| Backup Sensors | | A, B | Ignore the sensor effect of the first Auxiliary System hit | A p.17; B p.190 |
| Battle Arm | R | A, B | A crude arm. A: like a manipulator arm but cannot pick up and use weapons. B: lifts attached loads of Size up to the rating; punching versions hit at DM = rating | A p.17; B p.190 |
| Camo Netting | | A | +1 Concealment while stationary in Woodland or Jungle | A p.17 |
| Chaff/Flare | R | A | Add the rating to defense rolls against Guided weapons | A p.17 |
| Chassis Reinforcement | | A, B | +1 to the DM of punches and kicks (B: of physical attacks). Also listed as a weapon (CR, section 2.4) | A p.17, p.29; B p.197, p.206 |
| Counter Battery Sensors | R | A | When an indirect-fire weapon fires within half this unit's sensor range, the operator may try to locate it: Electronic Warfare roll, Threshold 1, base range = rating x sensor range; on a success it can pass the coordinates on like a forward observer | A p.17 |
| Easy to Modify | | A, B | +2 to technical rolls to modify or repair (campaign use) | A p.17; B p.191 |
| ECM | AUX, R | A, B | Jamming: one action per roll, Electronic Warfare test (B: + rating); the result is the Threshold that enemy communication (B: and sensor) tests in range must beat each round. **DIFF:** range = the vehicle's communication range (A) or sensor range (B). A's optional EW rules add Degrade Sensors and Spoof Attack modes (section 8.1). Procedure: notes 01 section 6.6 | A p.17, p.25; B p.191 |
| ECCM | AUX, R | A, B | Counters jamming; works like ECM (B: its result is the Threshold enemy ECM tests in range must beat; range = sensor range). A's optional rules add Counteract ECM and Degrade ECM Range modes | A p.17, p.25; B p.191 |
| Ejection System | AUX | B | On an Overkill the crewman rolls one die: 1-2 dies, 3+ ejects (without it, only a 6 saves him). Voluntary ejection costs an action | B p.148 / PDF 302, p.191 |
| Emergency Medical | | A, B | Ignore the first "Crew Stunned" result (A: armor loss still applies) | A p.17; B p.191 |
| Fire Resistant | | B | Halve the Intensity of flame attacks against the vehicle; with Hostile Environment Protection: Extreme Heat it is immune to incendiaries | B p.191-192 |
| Haywire Resistant | | B | Against haywire hits: on Light damage ignore the second damage roll; on Heavy damage treat the second roll as Light | B p.191 |
| HEAT-Resistant Armor | R | A, B | Add the rating to Base Armor against HEAT weapons (section 2.3). A card text: `HEAT ARMOR 5` | A p.17; B p.191 |
| High Towing Capacity | | A, B | Towing capacity doubled (B: doubled or tripled, per design) | A p.17; B p.192 |
| Hostile Environment Protection | option | A, B | Immunity to one environment. Options: Desert, Extreme Heat, Extreme Cold, High Pressure (and Extreme Pressure), Underwater (B); A lists Desert and Extreme Cold. Matters in the weather rules: without the matching protection, vehicles in extreme heat or cold take Light damage when they go to Top Speed and may break down; sandstorms can shut down vehicles without the Desert option; blizzards cost +1 MP per hex (B) or -1 to all tests (A) without Extreme Cold | A p.17, p.24-25; B p.159, p.192 |
| Improved Off-Road Ability | | A, B | -1 MP for any terrain that costs more than 1 MP (minimum 1); A also gives +1 to cross ditches | A p.17, p.23; B p.192 |
| Improved Rear Defense | | A, B | A: no penalty when attacked from the rear. B: rear-flank and rear penalties each one smaller (0 and -1). Same idea scaled to each book's arcs (notes 02 section 4.3) | A p.17; B p.192 |
| Jump Jets | AUX, R | A, B | Jump forward up to the rating in MP and up to half the rating upward, once per move (A). **DIFF:** B makes a jump cost an action, replacing normal movement, and caps the obstacle height at half the jump distance | A p.17; B p.192 |
| Large Doors | | A | Up to 2 x Size passengers can get in or out at no cost; 2 crewmen can get in or out for one action | A p.17 |
| Life Support / Limited Life Support | AUX | A, B | Sealed crew environment (limited: about a week). B: if destroyed in a hostile environment the crew are casualties | A p.17; B p.192 |
| Low Profile | | A | +1 obscurement when behind 2+ points of obscurement and when hull down; under Size 15 it cannot fire over walls | A p.17, p.21 |
| Manipulator Arm | R | A, B | Lifts objects of Size up to the rating (never above 2x own Size; several arms: strongest + half the others); can use hand-held weapons; can punch at DM = rating | A p.17; B p.193 |
| Mining Equipment | AUX | B | Can be used as a melee weapon: light duty x3 at Acc -1, heavy duty x10 at Acc -2. A uses mining gear for terrain clearing and digging (section 8.1) | B p.193; A p.23 |
| Passenger Seating | | A, B | Passengers add no actions and cannot drive. A: passengers share Crew hits with the crew; B: they cannot use the crew's ejection system | A p.17; B p.193 |
| Pintle Mount | | A | Carries one infantry weapon in a 180-degree arc chosen before the game; -1 to attacks when moving up to Combat Speed, -2 at Top Speed | A p.17 |
| Ram Plate | | A | Takes half damage from impacts (rams, crashes) on the plate's arc | A p.17 |
| Reinforced Armor | R, facing | A, B | Add the rating to Base Armor when hit on that facing (B facings: front, rear, left rear flank, right rear flank). Card text `RE.ARMOR 2(F)` | A p.17; B p.193 |
| Reinforced Chassis | | A, B | Ignore the first Structure hit (A: armor loss still applies) | A p.17; B p.193 |
| Reinforced Crew Compartment | | A, B | Ignore the first Crew hit | A p.17; B p.193 |
| Reinforced Location Armor | R | B | Add the rating to Base Armor when the hit lands in one named damage-table location | B p.193 |
| Rugged Movement Systems | | A, B | Ignore the first Movement hit | A p.17; B p.193 |
| Satellite Uplink | AUX | A, B | Long-range communication by satellite; in A it is required to call an Orbital Strike (section 6.6) | A p.17, p.26; B p.194 |
| Searchlight | AUX (B) | A, B | At night the arc it covers counts as daylight out to its range (A: F or whatever arc it is mounted on; B: FF). Firing at a vehicle with a lit searchlight is also treated as daylight; it can be shot out with a called shot | A p.17, p.22; B p.159, p.193 |
| Shield | R | A | Adds half its rating to Base Armor on the front or one side (player's choice). Once, after a failed defense roll, spend an action to roll again; if the reroll succeeds the hit still lands as rolled first, but with the full rating added. Light damage halves the rating; Heavy damage after use destroys the shield | A p.17 |
| Shielded Weapons | | A, B | Ignore the first weapon-damage result on the Fire Control table (-1 or -2 to one weapon, -1 to all weapons in B, or one weapon destroyed); then lost | A p.17; B p.194 |
| Smoke Launchers | AUX | A | Fire at any time during movement, no action: a smoke cloud of 1-hex radius within 1 hex, Obscurement 2, until the end of the turn | A p.17 |
| Sniper Systems | | A, B | +1 to attacks at Long and Extreme range | A p.17; B p.194 |
| Stabilizer Mount | | A | Spend an action while stationary to brace; then oversized weapons on the mount may fire; moving cancels it | A p.17 |
| Stealth | AUX, R | A, B | Adds the rating to Concealment. **DIFF:** A: in daylight only when there is obscurement between the units, always at night. B: only against sensors, never against visual detection. See notes 01 section 6.7 | A p.9, p.17; B p.194 |
| Target Designator | AUX (its rating is its range) | A, B | An attack with Base Range = rating and Acc +0, not modified by Fire Control, doing no damage; a hit paints the target for friendly Guided weapons until the end of the round; ECM cannot block it. A: a successful designation also gives a free communication action | A p.6, p.17; B p.194 |
| Tool Arm | R | A, B | Specialised arm (scoop, claw); lifts like a manipulator arm; some can punch at DM = rating | A p.17; B p.194 |
| Vehicle Bay | | B | Carries smaller vehicles (rated by type and tonnage); a carried vehicle needs a full round to get out, during which it may fire but not move | B p.194 |
| Weapon Link | | A, B | One action fires every weapon in the link, using the worst Acc and Base Range among them; each rolls separately, and after the first miss the rest automatically miss (still using ammo). B: linked weapons may also fire alone, and a weapon may belong to several links | A p.17; B p.194 |
| Wide Angle Searchlight | AUX | B | As Searchlight, but lighting the F, Rt, L or Rr arc | B p.194 |

### 3.3 Flaws

| Flaw | Tags | In | Effect | Source |
|---|---|---|---|---|
| Annoyance | | A, B | No tactical effect | A p.17; B p.195 |
| Decreased Maneuver | R | A | -rating to Maneuver in the listed movement mode | A p.17 |
| Defective Active Sensors | R (1-5) | A, B | Before an active sensor attempt roll 1 die; if it is at or under the rating, the rating is a penalty on that test | A p.17; B p.195 |
| Defective Fire Control | R (1-5) | A, B | Before firing roll 1 die; at or under the rating, the rating is a penalty on the attack | A p.17; B p.195 |
| Exposed Auxiliary Systems / Exposed Crew Compartment / Exposed Movement System | | A, B | Results on that system are one step worse (none to Light, Light to Heavy, Heavy to the worst). **DIFF:** A says the worst step destroys the vehicle; B limits it to that system (all auxiliary systems lost, all crew killed, or all movement lost) | A p.17; B p.195 |
| Exposed Fire Control Systems | | A, B | +1 on Fire Control table rolls | A p.17; B p.195 |
| Extreme Overheating | | B | Two rounds running of moving and firing, firing 3+ weapons, or jumping: automatic Light damage; three rounds running: Heavy damage | B p.195 |
| Fragile Chassis | | A, B | +1 on Structural table rolls | A p.17; B p.195 |
| Fuel Inefficient | | A | More fuel use; no tactical effect | A p.17 |
| Hazardous Ammo/Fuel Storage | | A, B | +2 on the follow-up roll after an "Ammo/Fuel Hit" | A p.17; B p.195 |
| Highly Flammable | | B | Incendiary attacks count double Intensity | B p.195 |
| Inefficient Controls | | B | One action fewer per round (not when the crew is down to one) | B p.196 |
| Large Sensor Profile | R | A, B | Subtract the rating from the vehicle's Concealment against sensors | A p.17; B p.196 |
| No Communication | | B | No radio: cannot act as forward observer or use Command Points | B p.196 |
| No Sensors | | B | Visual detection only; needs a human crew | B p.196 |
| Overheating | | B | Three rounds running of moving and firing, firing 3+ weapons, or jumping: automatic Light damage | B p.196 |
| Poor Off-Road Ability | | A, B | +1 MP to enter terrain costing 2 MP or more (B: elevation changes excepted). **DIFF:** B also allows paying the normal cost with a Piloting roll per hex against 4 + the hex's MP cost (fail: Light movement damage or stuck 1d6 rounds; fumble: Heavy movement damage or stuck for good) | A p.17; B p.196 |
| Poor Towing Capacity | | A, B | Towing capacity halved | A p.17; B p.196 |
| Random Shutdown | R | B | When strained (each round it moved, or every 15 minutes at Top Speed) roll 2 dice against 1 + rating; on a failure it shuts down for 1d6 rounds | B p.196 |
| Sensor Dependent | | A, B | **DIFF:** A: with sensors damaged or destroyed the vehicle can neither attack nor move. B: it detects only through its sensors and is blind once they are destroyed | A p.17; B p.196 |
| Unstable | | A, B | **DIFF:** A: -1 Maneuver at Top Speed. B: -1 to every Piloting roll at Top Speed or in terrain costing more than 1 MP | A p.17; B p.196 |
| Vulnerable to Haywire Effects | | A, B | Haywire weapons get one extra Systems Damage roll (three instead of two) | A p.17; B p.196 |
| Weak Facing | facing | A, B | Base Armor halved on that facing (A: round up) | A p.17; B p.196 |
| Weak Point | R | A, B | A called shot that hits that system lowers Base Armor by the rating for that hit | A p.17; B p.196 |

### 3.4 Perks and flaws with no tactical effect

Background or roleplaying only (B says so, or the effect is outside a battle): Audio System, Cargo Bay, Crew Accommodations, Geological Sensor, Laboratories (rating helps the lab's skill tests), Loudspeakers, Microlabs, Passenger Accommodations, Sick Bay (B p.189-194); Hostile Environment Protection outside the weather rules; A's dueling perks Scoring Sensors, Sporting Weapons and Trideo Link-up (sport duels with harmless weapons; A p.58). Fleet-scale perks (A section 6.2.10) belong to the landship rules and are not covered.

---

## 4. Gears for the first duel: Hunter and Jäger

The Hunter (Northern) and the Jäger (Southern) are the two basic trooper Gears. B's Jäger text describes the Jäger as a copy of the Hunter that the two sides keep matching change for change, and the numbers bear it out: same TV, armor, speeds, electronics and weapon types. That makes them a fair mirror match for a first duel. Unit files: `megamek/data/heavygear/units/hunter.yaml` and `jager.yaml`.

Sources: A datacards (Hunter A p.42 / PDF 43; Jäger A p.43 / PDF 44); B Field Guide description pages (Hunter B p.212 / PDF 442; Jäger B p.220 / PDF 450) and record sheets (Hunter B p.213 / PDF 443; Jäger B p.221 / PDF 451). Every value below was read from the page images.

### 4.1 Chassis

| Stat | Hunter (A) | Hunter (B) | Jäger (A) | Jäger (B) |
|---|---|---|---|---|
| Full name / code | Hunter | Hunter (Mark II), HACS-01MG-MP | Jager | Jäger (Alpha), OACS-01M/SU |
| Faction | Northern Guard | Northco, trooper Gear | Southern MILICIA | Territorial Arms, general purpose Gear |
| TV | 380 | 380 (Offensive 450, Defensive 298, Misc. 392, Lemon Dice 3) | 380 | 380 (450 / 298 / 392, Lemon Dice 3) |
| Size | 6 | 6 | 6 | 6 |
| Crew | 1 | 1 | 1 | 1 |
| Actions | 1 | Bonus Actions 0 | 1 | Bonus Actions 0 |
| Armor | 15 | 15 / 30 / 45 (Light / Heavy / Overkill) | 15 | 15 / 30 / 45 |
| Maneuver | 0 | 0 | 0 | 0 |
| Fire Control | 0 | 0 | 0 | 0 |
| Sensors (rating / km) | 0 / 2 | 0 / 2 (40 hexes) | 0 / 2 | 0 / 2 (40 hexes) |
| Communications (rating / km) | 0 / 10 | 0 / 10 (200 hexes) | 0 / 10 | 0 / 10 (200 hexes) |
| Walker MP (Combat / Top) | 4 / 7 | W 4 / 7 (primary; Walk 42 kph) | 4 / 7 | W 4 / 7 (primary; Walk 41 kph) |
| Ground MP (Combat / Top) | 6 / 12 | G 6 / 12 (secondary; Ground 72 kph) | 6 / 12 | G 6 / 12 (secondary; Ground 74 kph) |
| Deployment range | - | 500 km | - | 500 km |
| Perks | 2 x Manipulator Arm 6; Easy to Modify | Easy to Modify; Hostile Environment Protection (Desert); Manipulator Arm x2, rating 6, can punch | 2 x Manipulator Arm 6; Easy to Modify | same as Hunter (B) |
| Flaws / Defects | none | none / none | none | none / none |
| Other (B specs) | | 4.3 m tall, 3.0 m wide, 6627 kg, 45 mm armor, 450 hp V-engine, 221,590 marks | | 4.3 m, 3.0 m, 6637 kg, 45 mm, 450 hp, 221,667 dinars |

- **DIFF:** Hostile Environment Protection (Desert) is on both B sheets but on neither A card. It has no effect unless a desert weather rule is in play (section 3.2).
- A's two speed arrows are Walker (first) and Ground (second); B labels them primary W and secondary G.
- The kph figures match MP x 6 (7 x 6 = 42, 12 x 6 = 72); the Jäger's 41 and 74 kph are background and do not change its MP.

### 4.2 Weapons (identical arcs, numbers and loads unless noted)

| Code | Hunter model (B) | Jäger model (B) | Arc | Acc | DM | Range S/M/L/Ex | ROF | Ammo | Tags |
|---|---|---|---|---|---|---|---|---|---|
| LAC | M222 Autocannon (25 mm Riley) | PR-25 Autocannon | F | 0 | x8 | 2/4/8/16 | 2 | 60 | - |
| LRP/24 | RP-109 Pepperbox Rocket Pod | Vogel-6 Rocket Pod | F | -1 | x12 | 1/2/4/8 | 3 | 24 | IF |
| APGL | MK IV Grenade Launcher | HLB-16 AP G(renade) Launcher | Hunter FF; Jäger: see DIFF | -1 | x3 | 1/2/4/8 | 0 | 6 | AI, IF, AE0 |
| HG | M-2A Hand Grenade | HG-2 Hand Grenade | F | -1 | x15 | 0 (thrown 1 hex, A) | 0 | 3 | AI |
| VB | HW-VB1 Vibroknife | HHVB-3 Vibroblade | F | 0 | x8 | 0 (Point Blank only) | 0 | - | see DIFF |

- **DIFF:** Jäger APGL arc: A prints F (A p.43), B prints "Fixed F." (B p.221). The Hunter's APGL is FF in both. Because A treats FF as F (A p.9), the difference only matters if HeavyMek implements the narrower 120-degree FF arc.
- **DIFF:** Vibroblade tags: A's weapon table gives Armor Piercing, lost when thrown (A p.29); B's list gives Physical Attack Only (B p.197), the Hunter sheet "Physical Attack", the Jäger sheet "-".
- Hand grenade quantity: B's Hunter sheet prints Qty "-", the Jäger sheet "1"; both carry 3 grenades. Treat as one launcher-less weapon with 3 uses.
- ROF: B prints +2 and +3; A prints 2 and 3. Same values.
- Tags come from the weapon tables (A p.28-29; B p.197); A's cards do not print them. B's sheets print LAC "-", LRP/24 "Indirect Fire", APGL "Anti-I, IF, AE=0" (Jäger "AI, IF, AE0"), HG "Anti-Infantry".

### 4.3 Derived values for testing

| Item | Value |
|---|---|
| Armor thresholds (fresh) | Light 15+, Heavy 30+, Overkill 45+ damage; Light costs 1 armor, Heavy 2 |
| LAC bands (BR 2) | Short 1-2, Medium 3-4, Long 5-8, Extreme 9-16 hexes |
| LRP/24 and APGL bands (BR 1) | Short 1, Medium 2, Long 3-4, Extreme 5-8 hexes |
| LAC burst | DM x8 + ROF 2 = x10 (A p.12 example: MoS 2 gives 20 damage) |
| LRP/24 burst | x12 + 3 = x15 (A p.12 example: MoS 2 gives 30) |
| Kick / punch | kick DM = Size = x6; punch DM = arm rating = x6 (A p.11, p.17) |
| TV with other crews | A multipliers: Rookie 190, Qualified 380, Veteran 1140, Elite 1520, Legendary 1900. B multipliers: Rookie 95, Qualified 380, Veteran 855, Elite 1520 (section 6.2) |
| TV build-up (A p.30) | chassis 699 (Defensive 298 + Misc. 392 + punch 9) + weapons 443.32 = 1142.32; / 3 = 380.77, round down to 380 |
| A's variant example (A p.30) | swap the LAC (60 rounds) for a MAC (40 rounds) and carry 6 grenades: weapons 534.92, TV 411 |

---

## 5. Other vehicles: where to find them

### 5.1 B Field Guide record sheets (B p.210-229 / PDF 440-459)

Each vehicle has a description page (background, specifications box, weapon payload, Game Statistics line with TV, its three part-scores and Lemon Dice) followed by a filled record sheet. Title bars were read from the images.

| Vehicle | Code on sheet | Kind | Description page | Record sheet |
|---|---|---|---|---|
| Cheetah | HACS-01LG-SCT | Northern scout Gear | B p.210 / PDF 440 | B p.211 / PDF 441 |
| Hunter | HACS-01MG-MP | Northern trooper Gear | B p.212 / PDF 442 | B p.213 / PDF 443 |
| Jaguar | HACS-02MG-MPS | Northern commando / general purpose Gear | B p.214 / PDF 444 | B p.215 / PDF 445 |
| Grizzly | HACS-02HG-MPS | Northern heavy fire-support Gear | B p.216 / PDF 446 | B p.217 / PDF 447 |
| Iguana | OACS-01L/SC | Southern scout Gear | B p.218 / PDF 448 | B p.219 / PDF 449 |
| Jäger | OACS-01M/SU | Southern trooper Gear | B p.220 / PDF 450 | B p.221 / PDF 451 |
| Black Mamba | OACS-05M/SU (UNCLEAR: the digit is partly cut off in the scan) | Southern general-purpose / paratroop Gear | B p.222 / PDF 452 | B p.223 / PDF 453 |
| Spitting Cobra | OACS-01H/SU | Southern heavy fire-support Gear | B p.224 / PDF 454 | B p.225 / PDF 455 |
| Mammoth | WACS-01FS-AST (UNCLEAR: partly cut off) | Northern strider, crew 2 | B p.226 / PDF 456 | B p.227 / PDF 457 |
| Naga | OWCS-01AS | Southern strider, crew 2 | B p.228 / PDF 458 | B p.229 / PDF 459 |

Other Field Guide pages are background without record sheets: 9.1 technology (B p.179 / PDF 409); 9.2 Heavy Gears overview (B p.180-181 / PDF 410-411); Hunter and Tiger cutaway art (PDF 412, 413); 9.3 conventional vehicles including hovertanks (PDF 414-415); striders (PDF 416); landships (PDF 417).

### 5.2 A datacards

Tactical datacards, nine per page (A p.42-45 / PDF 43-46):

| Page | Faction | Units |
|---|---|---|
| A p.42 / PDF 43 | Northern Guard | Hunter (TV 380), Jaguar (628), Cheetah (625), Grizzly (888), Kodiak (1072), White Cat (710), Mammoth (strider, 1500), Assault Mammoth (991), standard infantry squad (74) |
| A p.43 / PDF 44 | Southern MILICIA | Jager (380), Black Mamba (671), Iguana (584), Spitting Cobra (818), King Cobra (1100), Chatterbox (621), Naga (strider, 1645), Long Fang Naga (651), standard infantry squad (74) |
| A p.44 / PDF 45 | CEF (Earth), plus one Northern card | Battleframe, HT-68 hovertank, HT-72 hovertank, Battleframe 2-07, MOAB, GRELs (infantry), Type 55 Battle Frame, HPC-64 Hover APC; Ferret (Northern Guard) |
| A p.45 / PDF 46 | North and South vehicles | Badger APC, Klemm light tank, Aller tank, Black Cat (Northern Guard); Sidewinder, Snakeye Black Mamba, Caiman, Hun light tank, Visigoth (Southern MILICIA) |

- TVs checked on the page images by card position (left to right, top to bottom). The infantry squad's 74 matches the infantry TV formula (section 7.6). Other card stats were not transcribed here.
- Duelist-scale datacards (named pilots and tournament teams) are on A p.62-65 / PDF 63-66: PA-09 Gladiator, Rapier Iguana, Nemesis Jaguar, Cheetah Fast Cat II, Black Mamba, Black Adder, Dartjäger, Strike Cheetah, Gladiator, Jaguar, Bruiser, Mayhem, Hero Jager, Armored Jager, Desert Jager, Blitz Jager, Rattlesnake, Warhawk, Grizzly, Mad Dog, Cheetah and others (names from the text layer).
- Fleet-scale landship datacards start at A p.76 / PDF 77 (different scale, not covered).
- A's army lists (A p.33-40) name many more variants (Hunter Paratrooper, Assault Hunter, Hunter Commando, Headhunter, Jager Paratrooper, Blitz Jager, Command Jager, Desert Viper, Pit Viper, Flamm Jager and others); most have no datacard in A.

### 5.3 A's chassis values for custom Gears (A p.30)

TV of a custom loadout = (chassis subtotal + total weapon TV) / 3, rounded down (section 6.4). Chassis subtotal = Defensive TV (DTV) + Miscellaneous TV (MTV) + the cost of punching.

| Vehicle | Size | DTV | MTV | Punch | Chassis subtotal |
|---|---|---|---|---|---|
| Hunter | 6 | 298 | 392 | 9 | 699 |
| Headhunter | 6 | 298 | 429 | 9 | 736 |
| Cheetah | 6 | 800 | 696 | 9 | 1505 |
| White Cat | 6 | 800 | 933 | 9 | 1742 |
| Jaguar | 6 | 552 | 268 | 9 | 829 |
| Grizzly | 7 | 257 | 236 | 12.25 | 505.25 |
| Kodiak | 7 | 304 | 745 | 12.25 | 1061.25 |
| Mammoth | 9 | 313 | 794 | 20.25 | 1127.25 |
| Jager | 6 | 298 | 392 | 9 | 699 |
| Jager Command | 6 | 298 | 429 | 9 | 736 |
| Iguana | 6 | 464 | 950 | 9 | 1423 |
| Chatterbox | 6 | 470 | 1268 | 9 | 1747 |
| Black Mamba | 6 | 618 | 295 | 9 | 922 |
| Spitting Cobra | 7 | 327 | 167 | 12.25 | 506.25 |
| King Cobra | 7 | 331 | 913 | 12.25 | 1256.25 |
| Naga | 8 | 301 | 144 | 0 | 445 |
| Warrior | 6 | 301 | 488 | 9 | 798 |
| Chieftain | 6 | 301 | 526 | 9 | 836 |
| Warrior IV | 6 | 401 | 488 | 9 | 898 |
| Chieftain IV | 6 | 401 | 778 | 9 | 1188 |
| Crusader IV | 7 | 267 | 148 | 12.25 | 427.25 |
| Ferret | 5 | 319 | 302 | 6.25 | 627.25 |
| Wild Ferret | 5 | 319 | 260 | 6.25 | 585.25 |
| Type 6-16 | 6 | 350 | 526 | 9 | 885 |
| Type 2-07 | 6 | 350 | 515 | 9 | 874 |
| MOAB | 9 | 204 | 287 | 0 | 491 |

The punch column equals (Size / 2) squared (Size 5: 6.25, 6: 9, 7: 12.25, 9: 20.25), and 0 for machines without punching arms (Naga, MOAB). This pattern is my observation; A does not state a formula.

---

## 6. Threat Value

### 6.1 What it is (A p.4; B p.125 / PDF 279, p.153 / PDF 307)

- Every combat unit, infantry included, has a Threat Value: a single points figure for its offensive, defensive and miscellaneous abilities. Higher is stronger. Printed TVs assume a Qualified crew.
- B splits a vehicle's TV into three part-scores (B p.153): Offensive (weapons and ability to deal damage), Defensive (speed, maneuverability, armor), Miscellaneous (ECM, designators, transport capacity and other special abilities). The printed TV is their average (Hunter: (450 + 298 + 392) / 3 = 380). Infantry TVs are not split.
- Games are balanced by giving each side the same TV budget and buying units at their TV (A p.4; B p.125, p.153).

### 6.2 Crew level multipliers

Final unit TV = printed TV x the crew multiplier (A p.3, p.26; B p.127 / PDF 281). Full crew-skill table and the skill DIFFs: notes 01 section 1.6.

| Crew level | A vehicles (A p.3) | B vehicles (B p.127) | A infantry (A p.16) | B infantry (B p.152 / PDF 306) |
|---|---|---|---|---|
| Rookie (1) | x0.5 | x0.25 | x0.5 | x0.25 |
| Qualified (2) | x1 | x1 | x1 | x1 |
| Veteran (3) | x3 | x2.25 | x3 | x2.25 |
| Elite (4) | x4 | x4 | x4 | x4 |
| Legendary (5) | x5 | - | x5 | x6.25 |
| Legendary (6) / (7) / (8) | - | - | - | x9 / x12.25 / x16 |

- **DIFF:** B's multipliers are (skill / 2) squared; A's Rookie, Veteran and Legendary values are not. B has no Legendary vehicle crew; B's infantry table goes up to skill 8 (all labelled Legendary).
- A's optional morale rules shift the multiplier (A p.19): a unit whose Morale Threshold is made 1 worse subtracts 1 from its multiplier (never below x0.25), 1 better adds 1, and Fanatical (never tests morale) adds 4. Base Morale Thresholds: Rookie 5, Regular 4, Veteran 3, Elite 2, Legendary 1.

### 6.3 Using TV to set up a battle

| A (A p.32) TV per side | A battle | A units | A time | B (B p.169 / PDF 399) TV per side | B battle | B time |
|---|---|---|---|---|---|---|
| 2500 | Patrol | 5-6 | 1 h | 2000 | Patrol | 1 h |
| 5000 | Skirmish | 7-15 | 2-3 h | 4000 | Skirmish | 2-3 h |
| 10000 | Raid | 16-20 | 3-4 h | 8000 | Raid | 3-4 h |
| 15000 | Minor battle | 21-30 | 5-6 h | 12000 | Minor battle | 5-6 h |
| 30000 | Major battle | 31+ | 8 h+ | 20000 | Major battle | 8 h+ |

- **DIFF:** the suggested totals differ (table above). Both are for Qualified crews.
- Duels (B p.153): keep the two units' TVs within about 10% of each other for an open-ground fight with no objectives. The Miscellaneous score matters little in a one-on-one fight; terrain and objectives make it matter more. TV is most accurate in large, complex battles.
- B's set-up options (B p.169): players may bid down the TV they need (steps of 100); a player who takes a smaller force earns victory points multiplied by the inverse of the fraction ("Going for Glory"); a side may give up 10% of its TV for each extra complication (subplot) imposed on the enemy.
- A's army-list rules (A p.32): a Battle Force is made of combat groups of 1-6 units (Core, Elite, Specialist, Support; Earth groups separate). The first group may be any type, the second must be Core, then alternate (any, Core, any, Core...), so Core groups make up a third to a half of the force. Unit roles: Trooper, Heavy Trooper, Command and Specialist Gears, Armour (striders, tanks, APCs, light vehicles), Infantry. Each list gives an average TV per group; more than twice that average is legal but frowned upon. A commander and a second in command are required; the commander is a Veteran (unless the force is a single group) and cannot be infantry. Special scenarios may ignore these rules.

### 6.4 Computing TV

- A's method for custom loadouts (A p.30): weapon load = sum over weapons of (weapon TV + ammo TV each x rounds carried); grenades are priced each. TV = (chassis subtotal from section 5.3 + weapon load) / 3, rounded down. Remember each weapon's Min Size.
  - Hunter: LAC 108 + 0.28 x 60 = 124.80; LRP/24 226 + 0.58 x 24 = 239.92; APGL 29 + 0.10 x 6 = 29.6; HG 11 x 3 = 33; VB 16. Total 443.32. (699 + 443.32) / 3 = 380.77, TV 380.
  - Variant: MAC with 40 rounds (163 + 0.51 x 40 = 183.40) instead of the LAC, plus 3 more grenades (33): total 534.92, TV (699 + 534.92) / 3 = 411.
- B gives the three part-scores directly. For the Hunter and Jäger, A's DTV and MTV equal B's Defensive and Miscellaneous scores, and A's weapon load plus the punch cost (443.32 + 9 = 452.32) is close to B's Offensive score of 450. The full design-and-cost system that produces these numbers is in B's vehicle design chapter ("Engineering Notebook", ending at PDF 277) and is not covered here.
- Crew multipliers apply after this (6.2).

### 6.5 Other things bought with TV (A only unless noted)

| Item | Cost | Source |
|---|---|---|
| Artillery strike (off-table) | 100 TV each | A p.26 |
| Air strike | 300 TV each | A p.26 |
| Orbital strike (needs a Satellite Uplink unit alive) | 1000 TV each | A p.26 |
| Wire obstacle / dense wire | 4 / 16 TV per inch | A p.23 |
| Minefields (A) | AI 9, Improved AI 18, Anti-Armor 24, Heavy Anti-Armor 54, Haywire 40 TV per inch | A p.23 |
| Minefields (B) | Anti-Personnel 2, Improved AP 3, General Purpose 5, Heavy GP 7, Anti-Armor 8, Heavy Anti-Armor 10 TV per hex | B p.158 / PDF 312 |
| Revetment / foxhole / trench | 100 per position / 25 per squad / 50 per inch | A p.23 |
| Fascine | 5 each | A p.23 |
| Jetpack or ATV infantry | double the squad's TV | B p.152 |

- **DIFF:** the two books' minefields are different systems with very different costs (A: an attack with DM, ROF and 50 "ammo"; B: a hex with a DM and separate Thresholds against vehicles and infantry).
- Off-table strikes in A (A p.26): used in the Miscellaneous Events phase, need the commander or second in command alive, a spotter with LOS, a working radio, a saved action and a comm roll past any ECM. Artillery: 1 die + 1 attack, saturates a 1-hex area at x12 until the next Miscellaneous phase. Air strike: 2 dice + 2 attack, x15 against one unit. Orbital strike: 3 dice, destroys one unit with no defense roll.

---

## 7. Infantry in brief (A p.16; B p.148-152 / PDF 302-306)

### 7.1 What a squad record holds

- A's infantry card (A p.16, samples A p.42-43): Name, Quality (skill level), Armor, Encumbrance, Stamina, TV; weapon lines (Name/Type, Acc, Range S/M/L/Ex, Dam, ROF); a damage track numbered 1-10 with each trooper marked LDR (leader), STD (standard weapon) or HVY (heavy weapon).
- B's blank infantry sheet (PDF 465, three squads per page): Unit Name, Quality, Armor, Encumbrance, Stamina, Armor Point(s), Damage Point(s); one Standard Weapon line and one Heavy Weapon line (Acc, Dmg, S/M/L/EX, ROF); damage track rows 1-10, each with a STD/HVY choice and 11 damage boxes. There is no TV box.
- A's sample squads (both sides, A p.42-43): Quality 2, Armor 2, Encumbrance 0, Stamina 3, TV 74; weapons 9mm LMG (heavy) and 7mm Assault Rifle; trooper 1 is the leader, troopers 5 and 10 carry heavy weapons.

Implementation note: a squad file needs `quality, stamina, armor{type, value}, encumbrance, standardWeapon, heavyWeapon, troopers[10]{role: leader|std|hvy}, movement{type: foot|jetpack|atv}`; damage points per trooper = Stamina + Armor.

### 7.2 Quality, armor, encumbrance

| Quality | Stamina | Skill | Morale Threshold (A optional) |
|---|---|---|---|
| Rookie | 3 | 1 | 5 |
| Qualified | 3 | 2 | 4 |
| Veteran | 4 | 3 | 3 |
| Elite | 4 | 4 | 2 |
| Legendary | 5 | 5 | 1 |

(Stamina and Skill are the same in A p.16 and B p.148; one generic Infantry Skill covers attack, defense and other tests. Morale Thresholds are from A's optional morale rules, A p.19, which call level 2 "Regular".)

| Armor | Points | Encumbrance (A) | Encumbrance (B) |
|---|---|---|---|
| Light flak | 2 | 0 | 0 |
| Heavy flak | 4 | 1 (none for Veteran and better) | 1 for Rookie and Qualified, none for Veteran and better |
| Turtleshell | 6 | 2 (none for Veteran and better) | 2 for Rookie and Qualified, 1 for Veteran, none for Elite and Legendary |

- Each point of Encumbrance is -1 on all the squad's rolls (B p.148).
- **DIFF:** Turtleshell on Veterans: no encumbrance in A, 1 point in B.

### 7.3 Movement, sensors, actions

- 2 MP per round on foot, always at Combat Speed; Walker terrain costs, but never more than 2 MP for one hex; they can go down cliffs but not up; at most two elevation changes per hex (A p.16; B p.150).
- B options: jetpack squads have 6 MP and pay Hover costs; light ATV squads have 8 MP and pay Ground costs; either doubles the squad's TV (B p.150, p.152). A mentions both but gives no numbers.
- Detection 4 by day, 2 at night (4 with night-vision gear, B), range 1 km = 20 hexes; communications range 10 km at rating -2 (A p.16; B p.150).
- One action per squad, normally an attack. One attack per weapon type for that action, all with the Infantry Skill; troopers with the same weapon fire at the same target; at most two different targets per squad (B p.150).
- Massed-fire ROF bonus, added to the weapon's own ROF: 1 trooper with that weapon +0, 2 troopers +1, 4 troopers +2, 8 troopers +3 (A p.16; B p.150). The bonus drops as casualties take the count below 2, 4 or 8.

### 7.4 Being attacked

- Infantry never get a defense modifier from their own slow movement (it is always 0), and non-infantry attackers take -2 unless the weapon is Anti-Infantry (A p.16; B p.151).
- Burst fire against infantry adds the ROF to the Margin of Success instead of the DM (B p.151; A's optional ammo rule p.18).
- Damage comes off one point at a time from one trooper's track; when a trooper's boxes are gone he is a casualty and the rest goes to the next trooper up or down the list, wrapping around between 1 and 10. Starting point (roll 1 die):

| Die | A start / direction (A p.16) | B start / direction (B p.151) |
|---|---|---|
| 1 | trooper 1, down | trooper 1, down |
| 2 | trooper 2, down | trooper 3, down |
| 3 | trooper 2, down | trooper 5, down |
| 4 | trooper 9, up | trooper 6, up |
| 5 | trooper 9, up | trooper 8, up |
| 6 | trooper 10, up | trooper 10, up |

- **DIFF:** the hit-location tables differ (above).
- A weapon that is both Anti-Infantry and Area Effect deals its full damage to every trooper in the area (A p.16; B p.152).
- If the leader dies, the squad's Leadership drops to 1 (A p.16; B p.149).
- **DIFF:** the damage track has ten boxes per trooper in A's text (A p.16) and eleven on B's sheet (B p.149, PDF 465). Eleven fits the largest possible total (Stamina 5 + Armor 6).
- Morale for squads (pinning, rallying): A's optional morale rules, section 8.1.

### 7.5 Infantry weapons

Section 2.5.

### 7.6 Infantry TV (A p.16; B p.152)

- Each trooper: square root of (damage points squared x his weapon's DM). Add the ten troopers, then apply the crew multiplier from section 6.2 (B; A's sample cards are Qualified, x1).
- Check with A's standard squad: damage points 3 + 2 = 5. Eight rifles (x2): 8 x sqrt(25 x 2) = 56.57. Two LMGs (x3): 2 x sqrt(25 x 3) = 17.32. Total 73.9, printed as 74 (A p.42-43).
- Jetpacks or ATVs double the final TV (B p.152). B does not charge for ordinary kit (goggles, radios, night vision).

---

## 8. Optional rules, one line each

Full procedures for the combat-related ones are in notes 02 (section 19) and the movement-related ones in notes 01 (section 5.11); this is only a checklist.

### 8.1 A chapter 2 (A p.18-25)

| Rule | One-line summary |
|---|---|
| 2.0 Optional ammunition | Count every round: bursts use 10 rounds per ROF point (rockets double per point), saturation 30 rounds or 8 rockets per point; replaces the ROF-depletion bookkeeping (A p.18). |
| 2.1 Morale (checks, conditions, rallying, pinned infantry) | Units test Leadership against a Threshold set by skill (Rookie 5 ... Legendary 1) when shot at, damaged or seeing friends die; each failure is a cumulative -1 to action tests (not defense); a Rally action or a leader's test removes penalties; squads that fail are pinned; morale also shifts the TV multiplier (A p.19-20). |
| 2.2.1 Simplified damage | For big games: two Light results become a Heavy; a Heavy halves MP and weapon damage; a second Heavy or any Overkill removes the unit (A p.20). |
| 2.2.2 Drones | Drones act on their own skills or are run by a friendly unit that hands over its actions (remote control with an EW roll and -1, satellite -2; wire control with no roll but a snag risk) (A p.20). |
| 2.2.3 Firestarting | 100 points of incendiary Intensity (persistent weapons count double) ignite an area; fires grow and spread downwind; crossing a fire needs a Piloting test (A p.20). |
| 2.2.4 Hull-down positions | Pay extra MP by terrain to get +1 obscurement (or +2 "full hull-down", indirect fire only); walkers can go prone for 1 MP less (A p.20-21). |
| 2.2.5-2.2.9 Structures, bridges, roads, railroads, urban terrain, walls | Buildings and bridges have damage-point capacities; bridges have Size limits; roads give ground vehicles bonus MP and Clear cost; railroad movement follows rails; urban and dense urban terrain have their own costs and obscurement; walls block movement (A p.21). |
| 2.2.10 Climbing and falling | Only walkers with strong enough arms climb cliffs (Piloting 4 per level, one level per round, -2 defense while climbing); falling damage = 2 dice x Size x levels (halved in Duelist scale) (A p.22). |
| 2.2.11 Night operations | Concealment doubled for visual detection; vehicles without sensors move at half speed and shoot at -1; searchlights light themselves up as targets (A p.22). |
| 2.2.12 Repairs | Technicians get Labor Points (skill x 5 per day) and roll against repair Thresholds per damage type (A p.22). |
| 2.2.13 Walker knockdown | A walker taking damage of at least twice its Size in one attack tests Piloting against MoS + 1 or falls; a fumble also gives Light damage; standing up costs 1 MP (A p.22). |
| 2.2.14-2.2.20 Combat engineering, wire obstacles, fascines, minefields, prepared positions, anti-vehicle obstacles, mining equipment and tool arms | Field works: wire that entangles, fascines to bridge ditches, minefields as standing attacks, revetments, foxholes and trenches (automatic hull-down; revetments soak 10 damage), dragon's teeth, hedgehogs and ditches, and digging or clearing terrain with mining gear (A p.22-23). |
| 2.2.21-2.2.32 Weather (rain, monsoon, thunderstorm, snow, blizzard, dangerous-terrain modifiers, extreme temperatures, ice, mud, sandstorms, heat haze, dust clouds, clouds and fog) | Extra concealment per inch, extra MP costs, penalties or bans on indirect fire and air support, breakdown risks without the right Hostile Environment Protection, shorter command distance (A p.23-25). |
| 2.2.33 Electronic warfare | ECM modes (jam communications, degrade sensors, spoof missile and guided attacks with a second defense roll) and ECCM modes (counteract ECM, shrink its range), one effect per system per round, each activation costs an action (A p.25). |
| 2.2.34-2.2.36 Reality Distortion Factor | Choose Gritty (extra sixes don't add), Adventurous (default) or Cinematic play; optional MoS cap of 6; armor lost per Light/Heavy hit: Gritty 0/1, Adventurous 1/2, Cinematic 2/4 (A p.25). |

### 8.2 B 7.11 optional tactical rules (B p.154-159 / PDF 308-313)

| Rule | One-line summary |
|---|---|
| 7.11.1 Aquatic movement | Naval and submarine units pay 1 MP per water hex, amphibious units their own water cost; submarines change depth for 2 MP per level; Deep Water has Obscurement 4 (sensors); a Heavy hit may sink a boat; non-submarines fight at -2 Maneuver under water, where only torpedoes and depth charges work. |
| 7.11.2 Bridges, roads and urban terrain | Structures have damage-point capacities and take damage like infantry; bridges collapse under vehicles above their Size capacity; roads add half the current speed in MP for ground vehicles; Urban (1-6 buildings) and Dense Urban (7+) hexes have their own costs and obscurement. |
| 7.11.3 Elevation as range | If the height difference is larger than the horizontal range, use the height difference as the range (or compute it exactly). |
| 7.11.4 Cliffs | Two or more levels = a cliff; only walkers with arms able to lift their own Size climb (Piloting 5 per level); falling damage = 2 dice x Size x levels, a fumble kills. |
| 7.11.5 Incendiary effects | Incendiary hits deal Intensity + MoS (slow-burn repeats for MoS rounds), against infantry multiplied by the MoS; 100 Intensity ignites a hex; fires grow, spread and smoke. |
| 7.11.6 Minefields | A minefield hex is a permanent saturation attack with a DM and separate Thresholds against vehicles and infantry (six sample types with TV per hex). |
| 7.11.7 Multiple attacker penalty | Optional in B (core in A, where only attackers from the same combat group count): -1 to defense against the second attacker, -2 the third, -3 after. |
| 7.11.8 Towing | Up to half the vehicle's own mass with no penalty, up to three quarters at Combat Speed only, up to full mass at half Combat Speed. |
| 7.11.9 Walkers falling | Same trigger as A's knockdown; **DIFF:** in B a failed test means falling with Light damage and a fumble means Heavy damage (A: failure = fall only, fumble = Light). |
| 7.11.10 Weather | Extreme temperatures, night (Night Detection score, noise detection ranges), rain, sandstorms, blizzards and tempests as extra obscurement per hex. |

B 7.8.4 Ejection (B p.148 / PDF 302) is a core rule, not optional: see the Ejection System perk (section 3.2).

---

## 9. Scale: B's hex map, B's miniatures rules and A

### 9.1 B's own conversion from hexes to miniatures (7.12, B p.160-163 / PDF 314-317)

- B plays miniatures over an imaginary hex grid: tape measures replace hex counting, and two templates (attack arcs and defense arcs, a "phantom hex" carried with the model) replace hexsides for arcs, facing and turn costs. All other rules are unchanged.
- Tactical scale: models are 1/87 (HO), but the battlefield is drawn at 1/1000, so one 50 m hex = **5 cm** of table. Skirmish scale: 10 m hexes and 6-second rounds at 1/100, so one hex = **10 cm**.
- Movement: one MP buys one hex-equivalent (5 cm or 10 cm) of Clear ground; other terrain multiplies the distance by its MP cost; a model is in the terrain its base mostly covers.
- Ranges: hexes x hex-equivalent, measured base centre to base centre. Example: Base Range 2 = bands of 10/20/40/80 cm at tactical scale. In skirmish play the group either keeps the 50 m range bands (so 1/2/4/8 becomes 50/100/200/400 cm, and units get at most one extra action) or uses 10 m bands (10/20/40/80 cm) with normal actions.
- Line of sight by string from the sensor point; obscurement counts per full hex-equivalent of cover crossed; a model more than half hidden is in cover and a hit on it strikes the cover on 1-3 on a die.
- Area effect: AE1 = radius of one hex-equivalent (5 cm); AE0 = half a hex-equivalent (2.5 cm).
- Physical attacks: models within half a hex-equivalent (2.5 cm tactical, 5 cm skirmish).
- Defense template values: Front 0, rear flanks -1, rear -2 (B p.161; same as B's hex rules).

### 9.2 How A lines up with B

| Item | B hex rules | B 7.12 miniatures | A miniatures (2005) |
|---|---|---|---|
| Tactical ground unit | 1 hex = 50 m | 5 cm = 50 m (1/1000) | 1 inch (2.54 cm) = 50 m (A p.5, p.8) |
| Tactical round | about 30 s | same | about 30 s (A p.5) |
| Skirmish / Duelist | - | 10 cm = 10 m, 6 s rounds | Duelist: 1 inch = 10 m, 6 s rounds (A p.5) |
| Other A scales | - | - | Melee 1 inch = 2 m (6 s); Fleet 1 inch = 500 m (5 min) |
| 1 MP | 1 Clear hex (about 6 kph) | 1 hex-equivalent | 1 inch of Clear (A still calls it "one clear mapboard hex", about 6 kph, A p.6) |
| Point Blank | same hex | within 2.5 cm (physical attacks) | bases touching or within 1 inch (A p.10) |
| AE0 / AE1 | target hex / radius 1 hex | 2.5 cm / 5 cm radius | 1-inch / 3-inch diameter circle (A p.13, p.29) |
| Stacking | per hex (notes 01 section 5.10) | - | 30 Size points per 50 m area; 5 infantrymen = Size 3 (A p.8) |

- Conclusion: A's inch is B's hex. Every A distance (MP, Base Range, sensor range in inches, AE sizes, "1 inch" rules) converts 1:1 to hexes, so A's datacards and weapon tables can be used on a hex map with no conversion. A's table is just drawn at roughly half the size of B's own miniatures conversion (2.54 cm instead of 5 cm per hex).
- A's 3-inch AE1 circle and B's radius-1 burst both cover the target hex plus its six neighbours on a hex grid.

---

## 10. Open questions (UNCLEAR index)

1. Lemon Dice on B's description pages: a roleplaying feature; not explained in the pages read (section 1.2).
2. A's datacard arc "FR" on the Black Mamba (A p.43): probably a fixed rear mount; A defines no such arc (section 1.3).
3. `SEARCHLIGHT 2` and similar unexplained numbers on A's cards: the rating's meaning (range?) is not given (section 1.4).
4. Black Mamba and Mammoth production codes are partly cut off in the B scan (section 5.1).
5. Area Effect in A: a unit that defends successfully still takes "half the DM" as concussion damage; rounding not stated, and whether this applies to every AE weapon or only blasts is not said (section 2.3).
6. Incendiary damage in A: the tag is defined only as setting targets alight; use B's Intensity rules (section 2.3) unless notes 02 decide otherwise.
7. Disposable: neither book says whether a disposable weapon with ROF (the DPG, ROF +2) loses its ROF uses permanently, beyond A's "cannot be reloaded" (A p.12).
8. Which minefield system to use (A's per-inch attacks or B's per-hex Thresholds) (section 6.5).
9. B's Light Grenade Launcher Min Size 0 and Sniper Laser Cannon Min Size 14 look like misprints (section 2.4).
10. HATM has no B description box, so no B "usual load" (section 2.4).

## 11. A vs B differences (DIFF index)

| Topic | A | B | Section |
|---|---|---|---|
| Crew TV multipliers | Rookie 0.5, Veteran 3, Legendary 5 | Rookie 0.25, Veteran 2.25, no Legendary vehicle crews | 6.2 |
| Infantry TV multipliers | 0.5 / 1 / 3 / 4 / 5 | 0.25 / 1 / 2.25 / 4 / 6.25 (up to 16 at skill 8) | 6.2 |
| Suggested battle sizes | 2500 / 5000 / 10000 / 15000 / 30000 | 2000 / 4000 / 8000 / 12000 / 20000 | 6.3 |
| Minefields | per-inch attacks with TV 9-54 | per-hex Thresholds with TV 2-10 | 6.5 |
| Frag Cannon | x7, Acc +1, ROF 0, AE=0 | x14, Acc 0, ROF 0, Frag Ammo rule | 2.4 |
| LGL Min Size | 4 | 0 (misprint?) | 2.4 |
| SLC Min Size | 4 | 14 (misprint?) | 2.4 |
| Vibroblade | Armor Piercing, can be thrown | Physical Attack Only | 2.4, 4.2 |
| Hunter / Jäger perks | no Hostile Environment Protection | HEP (Desert) | 4.1 |
| Jäger APGL arc | F | Fixed Forward | 4.2 |
| Infantry indirect-fire weapons | 37mm Grenade Rifle and 62mm mortar marked IF | not marked | 2.5 |
| Infantry hit location | 1 / 2-3 / 4-5 / 6 start at 1 / 2 / 9 / 10 | 1-6 start at 1 / 3 / 5 / 6 / 8 / 10 | 7.4 |
| Infantry damage boxes | 10 | 11 | 7.4 |
| Turtleshell on Veterans | no encumbrance | 1 point | 7.2 |
| ECM / ECCM range | communication range | sensor range | 3.2 |
| Autopilot | dodges as Skill 1 | cannot dodge | 3.2 |
| Airdroppable | deploy anywhere out of enemy LOS | background only | 3.2 |
| Jump Jets | free with movement | costs an action, replaces movement | 3.2 |
| Stealth | daytime only with obscurement between; always at night | sensors only, never visual | 3.2 |
| Exposed (aux, crew, movement) | worst step destroys the vehicle | worst step wipes out that system only | 3.3 |
| Poor Off-Road Ability | +1 MP only | +1 MP or a Piloting roll per hex | 3.3 |
| Sensor Dependent | cannot move or attack once sensors are damaged | blind once sensors are destroyed | 3.3 |
| Unstable | -1 Maneuver at Top Speed | -1 Piloting at Top Speed or in costly terrain | 3.3 |
| HEAT weapon list | omits rockets | includes rockets | 2.3 |
| Walkers falling | failure: fall; fumble: Light damage | failure: Light damage; fumble: Heavy damage | 8.2 |
| Cliff climbing Threshold | 4 per level | 5 per level | 8.1, 8.2 |
| Multiple attacker penalty | core rule, same combat group only | optional, any attackers | 8.2 |
| Ammo tracking | ROF depletion; ammo only for 5 shots or fewer (counting is optional) | every round counted | 2.1 |
| Scale | 1 inch = 50 m | hex = 50 m; minis at 5 cm per hex | 9 |
