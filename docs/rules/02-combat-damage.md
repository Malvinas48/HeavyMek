# 02 - Attacks and Damage (Heavy Gear 2nd Ed. tactical rules, notes for HeavyMek)

Rules notes for a programmer. Everything is paraphrased from the two books; numbers and tables are
reproduced exactly but re-laid-out. No code design here, only one-line hints. Which book wins where they
differ is set in [README.md](README.md): B wins, A fills gaps.

## Sources, page numbers, conventions

- **A** = *Heavy Gear Tactical Miniatures Rules, 2nd Edition* (DP9, 2005). Cited "(A p.N)" with the
  printed page number (PDF page = N + 1). Core: sections 1.11-1.13 on A pp.9-15. Also used where the attack
  pipeline needs them: A pp.2-7 (dice, crew, turn sequence, actions, speeds), p.16 (attacking infantry),
  p.17 (perks/flaws), pp.18-25 (optional rules), p.26 (off-table support), p.27 (quick-reference tables),
  pp.28-29 (weapon tables and weapon special characteristics).
- **B** = *Heavy Gear Rulebook, 2nd Edition* (DP9, 1997), image scan. Cited "(B p.N / PDF M)". In this part of the
  scan printed page = PDF page - 154 (checked on every page from PDF 281 to 305; the pagemap hint
  "p.124 = PDF 279" is one page off here). Core: 7.7 Attacks and 7.8 Damage = B pp.137-148 / PDF 291-302;
  firing-arc diagrams B p.136 / PDF 290; arc codes, turn sequence and actions B pp.127-130 / PDF 281-284.
- Scale: 1 inch in A = 1 hex in B = 50 m. Read A's "1-inch area" as "hex".
- Markers: **DIFF:** A and B disagree (both recorded). **UNCLEAR:** ambiguous, contradictory or silent.
  **A-internal / B-internal:** a book contradicts itself. **Implementation note:** one-line hint for the hex engine.
- Hex geometry used below: B's diagrams use flat-topped hexes with the unit facing a hexside (same as MegaMek).
  **Bearing** = angle from the centre of the reference unit's hex to the centre of the other hex, clockwise from
  the unit's facing: 0 = dead ahead, 180 = dead astern. The six neighbours are at 0 (front), 60 (front-right),
  120 (rear-right), 180 (rear), 240 (rear-left), 300 (front-left). Hexes two columns over on the same row sit at
  exactly 90 / 270 ("abeam").
- Out of scope, only cross-referenced: computing LOS/Concealment, movement costs, infantry damage track,
  full perk list, turn sequence. Those belong to the other notes files.

---

## 0. Resolution order (summary)

1. Eligibility (section 2): action available, not evasive, LOS (or forward observer), arc, range band, min range,
   weapon working, ROF/ammo.
2. Attack total (section 3) and defense total (section 4), both floored at 0.
3. Hit only if attack total > defense total. MoS = attack - defense (section 1).
4. Damage = MoS x Damage Multiplier, after ROF / weapon / physical-attack adjustments (sections 7, 10, 11).
5. Compare with the target's current Base Armor: nothing / Light / Heavy / Overkill (section 11).
6. Light or Heavy: permanent armor loss, then Systems Damage roll(s) (sections 11-13).
7. Follow-ups: arms (14), optional knockdown (19), ejection (16, B only).

---

## 1. Dice, opposed tests, Margin of Success, fumbles

### 1.1 Reading a Skill roll (A p.3)
- Roll a number of d6 equal to the Skill level. Do not add them: the result is the single highest die, plus 1 for
  every additional 6 beyond the first.
- If every die shows 1 the roll is a **Fumble**: total 0, and no modifier may change it (A p.3).
- Otherwise add the crew attribute bonus (the "+N" in "Skill/Attribute", e.g. Qualified Gunnery 2/+1) and all
  situation modifiers. A total below 0 becomes 0 (A p.3). The defense total is explicitly floored at 0 too
  (A p.10; A p.27).
- Crew table (A p.3), Piloting / Gunnery as dice/bonus: Rookie 1/0, 1/0; Qualified 2/0, 2/+1; Veteran 3/+1, 3/+1;
  Elite 4/+1, 4/+1; Legendary 5/+1, 5/+1. Default crew is Qualified.
  DIFF: B p.127 / PDF 281 lists Rookie Gunnery 1/+1, Elite Gunnery 4/+2, no Legendary, other TV multipliers.
  (Crew stats belong to the unit notes; shown here because the examples use them.)

Dice-reader test vectors (mine):

| Dice shown | Modifiers | Total |
|---|---|---|
| 2, 5 | 0 | 5 |
| 6, 6, 3 | 0 | 7 |
| 6, 6, 6 | 0 | 8 |
| 1, 1 | +5 | 0 (fumble, modifiers ignored) |
| 1 (single die) | +2 | 0 (fumble) |
| 1, 2 | -4 | 0 (floored; not a fumble) |
| 1, 5 | +2 | 7 |

### 1.2 Deciding a hit (A p.9; B p.137 / PDF 291)
- Ranged attack: attacker rolls Gunnery, defender rolls Piloting; each adds its own modifiers (sections 3-4).
  Physical attacks use other skills (section 7). Infantry defend with Infantry Skill (A p.11; A p.16).
- Attack total strictly above defense total = hit. Equal or below = miss (A p.9 table; B p.137 / PDF 291 table).
- MoS = winner's total - loser's total; MoF is the same difference seen from the loser; a tie gives MoS 0
  (A p.3). For attacks a tie is a miss.
- MoF of the attacker is used for indirect-fire scatter (9) and B's throw deviation (7.4); MoF of a defender is
  the damage factor for saturation fire (10.4).

### 1.3 Fumbles in combat (A p.27, quick-reference "Tactical System Fumble Effects")

| Who fumbles | Result |
|---|---|
| Attacker | Automatic miss. |
| Defender | Automatic hit unless the attack also fumbled; for the damage calculation the defense counts as 0. |
| Both | Miss. |

- B has no separate fumble table; it remarks that even a hopeless shot can land if the target fumbles
  (B p.137 / PDF 291), which is the same outcome since a fumble totals 0. B's evasive example: a fumbled defense
  loses the +3 evasive bonus (B p.141 / PDF 295).
- UNCLEAR: defender fumbles against an attack total of 0 (non-fumble, floored): "automatic hit" but MoS 0, so
  0 damage (except area-effect concussion, 10.5). Neither book says whether MoS has a minimum of 1 here.
- A-internal: A's attack example 2 finishes with a total of -1 (A p.9), against A's own zero floor (A p.3). B's
  version of that example uses other numbers (roll 10, final 2 vs 0, hit with MoS 2, B p.137 / PDF 291). Use the floor.

### 1.4 Things that change a roll after it is made
- Command Point: +2 to a single defense roll (A p.5; B p.129 / PDF 283). Also usable as a penalty-free extra
  action (both). A only: a Command Point can reroll any die roll (A p.5).
  DIFF: B has Tactical and Initiative Command Points (initiative winner gets CPs equal to the initiative MoS,
  lost at the end of the round) and no reroll use (B p.129 / PDF 283). A has Tactical CPs only (A p.5).
- Optional Reality Distortion (A p.25): at "Gritty" level extra sixes do not add +1; A recommends capping MoS
  at 6 at any level. Default for miniatures is "Adventurous" (normal dice).

---

## 2. Can this unit make this attack?

1. An action is available. One attack action = fire one weapon once, or one linked set of weapons once; one
   physical attack; one thrown object (A p.6; B p.130 / PDF 284). A weapon with ROF 0 needs a separate action per
   shot (A p.12).
2. The unit did not declare evasive maneuvers this round (A p.11; B p.141 / PDF 295).
3. LOS to the target (A pp.8-9; B p.134 / PDF 288; computed in the LOS notes), except indirect fire through a
   forward observer (section 9).
4. Target inside the weapon's firing arc (2.1).
5. Distance within the weapon's Extreme band (8 x Base Range) and not below its Minimum Range (2.2).
6. Weapon usable: cumulative damage penalty better than -5, not destroyed (A p.14); ROF Attacks / shots / ammo
   available (section 10); the arm carrying it works (A p.15).
7. Crew present (a crewless unit cannot act, A p.6). Sensor Dependent flaw: no attacks once sensors are damaged
   or destroyed (A p.17).

### 2.1 Firing arcs (A p.9; A p.27; B p.128 / PDF 282; diagrams B p.136 / PDF 290)

| Code | A | B | Hex shape from B's diagrams (B p.136 / PDF 290) |
|---|---|---|---|
| F | forward 180 | forward 180 | Front, front-left, front-right neighbours; further out, the half-plane ahead of the unit. |
| Rt | right 180, includes dead ahead and dead astern | same | Front, front-right, rear-right, rear neighbours; bearings 0-180 inclusive. |
| L | left 180, includes dead ahead and dead astern | same | Front, front-left, rear-left, rear neighbours; bearings 180-360 inclusive. |
| Rr | rear 180 | rear 180 | Rear, rear-left, rear-right neighbours; the half-plane behind. |
| FF | 120 cone on the front; now an optional rule and played as F (or Rr / side for odd mounts) to save time. A p.27 lists "F or FF = forward 180". | 120 cone whose arc begins one hex in front of the vehicle (B p.128 / PDF 282) | The hex directly ahead, plus every hex within +/-60 of the facing direction measured from that hex's centre (edge hexes included). The unit's own front-left and front-right neighbours are NOT inside. |
| T | 360 | 360 | Everything. |

- Infantry have no facing and fire all round (A p.9; B p.136 / PDF 290).
- A weapon with no stated arc fires forward (B p.136 / PDF 290). Side or rear fixed arcs exist but are rare (both).
- DIFF: FF. A collapses it into F; B keeps the narrower cone.
- UNCLEAR: for F and Rr, whether hexes exactly abeam (bearing 90 / 270) are inside. Side arcs explicitly include
  their edge (dead ahead / astern), and B's defense-arc diagram counts abeam hexes as Front (4.3). Suggest:
  an edge hex belongs to both arcs that meet there.
- Implementation note: every arc test is a bearing test from the shooter's hex centre (for B's FF, from the
  centre of the hex in front of the shooter).

### 2.2 Range bands (A p.10; A p.4; B p.138 / PDF 292; B p.127 / PDF 281)
Range is counted centre to centre in hexes (B p.141 / PDF 295 says both units count as being in the middle of
their hexes). Base Range (BR) is the top of Short. Datacards print the four band limits as S/M/L/X, e.g. LAC
2/4/8/16 (A p.28).

| Band | A extent | B extent | Attack modifier |
|---|---|---|---|
| Point Blank | bases/minis touching, or within 1 inch; also units sharing a 1-inch area (A p.8 stacking) | same hex | +1 |
| Short | after PB, out to BR | 1 hex out to BR | 0 |
| Medium | up to 2 x BR | same | -1 |
| Long | up to 4 x BR | same | -2 |
| Extreme | up to 8 x BR | same | -3 |
| beyond 8 x BR | no attack | no attack | - |

- DIFF / UNCLEAR: PB in hex terms. B: same hex only. A: "within 1 inch" can be read as same hex or adjacent hex.
  The same choice decides A's PB defense penalty (4.3) and therefore every physical attack in A.
  Implementation note: make PB distance (0, or 0-1) a rules option; B's value is 0.
- Minimum Range: a weapon tagged "Min Range N" cannot attack a target at a range below N (A p.29), e.g. Light
  Field Mortar 4, Light Artillery Gun 10 (A pp.28-29). The infantry 62mm light mortar cannot fire at range 2 or
  less (A p.16). Neither book sets a general minimum range for indirect fire.
- Tag "-N Dam. per R.B.": the DM drops by N for each band beyond Short (A p.29).
- Optional heat haze: count range as double the real distance (A p.25).

---

## 3. Attack roll

Attack total = Gunnery roll (dice + attribute) + every applicable modifier below. Rows 1-5 are the core list in
A p.9 and B p.137 / PDF 291 (A p.9 calls row 1 "Targeting System Rating"; the datacard calls it Fire Control).

| # | Modifier | Value | Source | Notes |
|---|---|---|---|---|
| 1 | Fire Control rating | datacard value | A p.4, p.9; B p.137 / PDF 291 | Applies to all attack rolls. Not to target-designator "attacks" (A p.17). |
| 2 | Weapon Accuracy | weapon Acc + its cumulative damage penalties + penalties of the arm holding it | A p.4, p.14-15; B p.127 / PDF 281 | Subtable A results land here (13.1). |
| 3 | Range band | PB +1, S 0, M -1, L -2, X -3 | A p.10; B p.138 / PDF 292 | See 2.2. |
| 4 | Attacker movement | Stationary +2; half Combat Speed or less +1; Combat Speed 0; Top Speed -3 | A p.10, pp.6-7; B p.138 / PDF 292 | Band is the one announced for the activation (section 5). Reverse movement is only allowed at half Combat or less, so it earns +1 (A p.7). |
| 5 | Obscurement | minus the target's Concealment value | A p.8, p.10; B p.134 / PDF 288, p.138 / PDF 292 | Concealment = obscurement of hexes between the two units plus the defender's own hex (B p.134; A p.8 example), only whole inches count (A p.8). Terrain: Swamp 1, Woodland 1, Jungle 2, Water 2 (Deep Water 4, B p.134) only in the defender's hex and only if the defender is not Hover, Naval or Amphibious (A p.10; B p.138). Add Stealth where it applies (A p.9), Hull Down +1 (A p.6), smoke cloud 2 (A p.17). Indirect fire uses the forward observer's value (section 9). |
| 6 | Called shot | -1 | A p.12; B p.142 / PDF 296 | Section 8. |
| 7 | Extra actions this round | -1 per action beyond the free allowance, on every action roll that round | A p.6; B p.130 / PDF 284 | Declared in Step One (section 5). |
| 8 | Target is infantry | -2, unless the weapon is Anti-Infantry or the attacker is infantry | A p.16; B p.151 / PDF 305 | Infantry details are in the infantry notes. |
| 9 | Guided weapon vs a target painted this round by an allied target designator | +2 | A p.29 | Section 9. |
| 10 | Saturation fire | + half the weapon's ROF, rounded down | A p.13; B p.144 / PDF 298 | 10.4. |
| 11 | Fire Control system destroyed | -5 | A p.14-15; B p.146-147 / PDF 300-301 | UNCLEAR: whether the destroyed system's own rating still counts; suggest -5 replaces it. |
| 12 | Sensors destroyed | -5 on actions that need sensors | A p.14; B p.146 / PDF 300 | UNCLEAR whether ordinary direct fire "needs sensors" (fire control covers targeting); probably only sensor tasks. |
| 13 | Sniper Systems perk | +1 at Long and Extreme | A p.17 | |
| 14 | Pintle mount weapon | -1 if moving up to Combat Speed, -2 at Top Speed, on top of the rest | A p.17 | |
| 15 | Defective Fire Control flaw | roll 1d6 before the test; if <= rating, apply -rating | A p.17 | |
| 16 | Optional rules | night without sensors -1 (A p.22); thunderstorm indirect fire -2 (A p.24); sandstorm forbids indirect fire (A p.25); heat haze -1 to artillery (A p.25); remote-controlled drone -1, via satellite uplink -2 (A p.20); extreme cold without protection -1 to all action tests (A p.24) | | |

Physical attacks start from a different skill (section 7).

Implementation note: keep two itemised modifier lists (attack side and defense side); the opposed roll needs both
shown and both are affected by different events.

---

## 4. Defense roll

Defense total = Piloting roll (dice + attribute; Infantry Skill for infantry) + modifiers below; floor 0 (A p.10).

| # | Modifier | Value | Source | Notes |
|---|---|---|---|---|
| 1 | Maneuver rating | datacard value, after damage (-1 / -2 results) | A p.4, p.10; B p.139 / PDF 293 | Negative for big slow vehicles, positive for Gears etc. |
| 2 | Target speed | table 4.2 | A p.10, p.27; B p.139 / PDF 293 | Infantry: always 0 (A p.16; B p.151 / PDF 305). |
| 3 | Defense arc | A: Front 0, Rear or PB -1. B: Front 0, Rear Flank -1, Rear -2 | A p.10; B p.139 / PDF 293 | 4.3. |
| 4 | Multiple Attacker Penalty | 0 / -1 / -2 / -3 | A p.10, p.27 | A only (4.4). |
| 5 | Evasive maneuvers | +3 to every defense roll this round | A p.11; B p.141 / PDF 295 | Section 6. |
| 6 | Command Point | +2 to one roll | A p.5; B p.129 / PDF 283 | |
| 7 | All movement systems knocked out | defense total is 0 from then on | A p.14; B p.146 / PDF 300 | 13.4. |
| 8 | Perks | Chaff/Flare: +rating vs Guided weapons. Acrobatic Handling: +rating to Maneuver for close combat and vs PB attacks. Improved Rear Defense: no rear penalty. Autopilot: dodges as a Skill 1 pilot. Shield: one action buys a second try at a defense roll that failed (11.3). | A p.17 | 18 lists all hooks. |
| 9 | Flaws | Unstable: -1 Maneuver at Top Speed. Decreased Maneuver: -rating in the listed movement mode. | A p.17 | |
| 10 | Optional rules | climbing walkers/infantry -2 (A p.22); ECM "spoof" second defense roll vs missiles/guided (A p.25); buildings defend at 0 vs rams (A p.21) | | 19. |

### 4.2 Target speed modifiers (A p.10; A p.27; B p.139 / PDF 293)

| Hexes moved | Defense modifier |
|---|---|
| 0 | -3 |
| 1-2 | -2 |
| 3-4 | -1 |
| 5-6 | 0 |
| 7-9 | +1 |
| 10-19 | +2 |
| 20-99 | +3 |
| 100-999 | +4 |

- DIFF (minor): the 100-999 row is in B p.139 and A's quick-reference p.27 but missing from A p.10.
- Which number to look up:
  - Target already moved this round: hexes moved this round.
  - Target has not moved yet this round: its last recorded move (A p.10; B p.139).
  - First round and not yet moved: the most hexes it could move at its current speed in its current terrain
    (A p.5, p.10; B p.128 / PDF 282, p.139).
  - Snap fire against a unit that is moving (section 5): its whole MP allowance for the round, even though it
    has only covered part of its path (A p.6; B p.129 / PDF 283).
  - A unit that chose to stay Stationary has moved 0, so -3 (A p.6).
- Implementation note: store per unit "hexes entered this round" (not MP spent, turns in place do not count) and
  the value from its previous move.

### 4.3 Defense arc (where the attack comes from)

**A** (A p.10; A p.2): Front 0; Rear -1; Point Blank -1 (bases touching or within 1 inch, whatever the
direction). A's diagram splits front and rear with a line through the unit at right angles to its facing.
A p.2 notes that A deliberately removed B's rear-flank zone.

**B** (B p.139 / PDF 293), read from the three hex diagrams:

| B arc | Bearing from defender to attacker | Adjacent hexes inside | Modifier |
|---|---|---|---|
| Front | 270 through 0 to 90, edges included (abeam hexes are Front) | front, front-left, front-right | 0 |
| Rear | strictly between 150 and 210 (a 60-degree wedge straight back; hexes on the 150/210 lines are NOT Rear) | rear only | -2 |
| Rear Flank | everything else: over 90 up to 150, and 210 up to under 270 | rear-left, rear-right | -1 |

Rear wedge check: range 1 = only the rear neighbour; range 2 = only the hex two straight back (the rear
neighbour's own rear-left/rear-right neighbours lie on the 150/210 lines and are Rear Flank); range 3 = the hex
three straight back plus the two hexes beside it.

- DIFF: A has two zones plus a PB penalty; B has three zones and no PB penalty.
- UNCLEAR: B gives no arc for an attack from inside the same hex (A: PB, -1).
- UNCLEAR: which position sets the arc for indirect fire (firer or forward observer) and for blast victims who
  were not the aimed target. Presumably the firer's position.
- Improved Rear Defense perk removes the rear penalty (A p.17).
- Implementation note: arc = bearing from the defender's hex centre to the attacker's hex centre relative to
  the defender's facing; the attacker's own facing is irrelevant.

### 4.4 Multiple Attacker Penalty, MAP (A only: A p.10; A p.27)
- Counted per defender and per attacking combat group: the first unit of a group to attack this defender gives 0,
  the second different unit of that group -1, the third -2, the fourth and later -3 (cap).
- A unit attacking the same defender again does not raise the count.
- A unit from another combat group starts that group's own count at 0.
- UNCLEAR: the counter presumably resets each round (A says "in a short time frame").
- UNCLEAR: when a unit attacks the same defender twice, whether its second attack uses the group's current
  count or its own original position.
- DIFF: B has no MAP (B p.137 / PDF 291 lists only Maneuver, defender movement and arc).
- Test vector (mine): in one round a defender is attacked by X1, X2, Y1, X3, X4, X5 (X and Y are combat groups)
  -> penalties 0, -1, 0, -2, -3, -3.

---

## 5. Timing: attacks during movement, snap fire, standby, extra actions
(Turn sequence itself belongs to the turn-sequence notes; only the attack-relevant parts are here.)

- A unit's actions may be taken before, during or after its own move (A p.6; B p.129 / PDF 283).
- The attacker movement modifier follows the speed announced for the activation, and the unit may not then
  spend more MP than that band allows: Stationary (0 MP), half Combat or less, Combat, Top (A p.6; B p.129 gives
  the half-Combat example). Stationary is only possible if the unit began its move at Combat Speed, not Top
  (A p.6).
- Shift to Top Speed: declared right after the move; from then on, for the rest of the round, the unit counts as
  moving at Top Speed for attack and defense (A p.7). Implementation note: an attack made after that declaration
  takes -3.
- Hull Down (A pp.6-7): spend 1 MP at the end of the move; the unit then has +1 obscurement against attacks; it
  pays 1 MP to get up later (and cannot shift to Top Speed that turn). If it used no more than half Combat Speed
  it still gets +1 on its own attacks; staying Hull Down the next turn counts as Stationary (+2). B 7.7 has no
  hull-down rule (rest of B not searched). See also optional Hull-Down Positions (19).
- **Snap fire** (A p.6; B p.129 / PDF 283): while an enemy unit is moving, a unit may spend one or more of its
  actions to shoot at (or act against) that moving unit only. The shot may be taken at any point of the mover's
  path; the mover's defense speed modifier uses its full MP allowance for the round. If the shooter uses several
  actions, the mover must spend at least 1 MP (or finish moving) between them.
  - DIFF: A allows snap fire only to units not yet activated this round, it costs one of the shooter's attacks
    for the round, and it costs no Command Point unless the shooter must turn to bring the weapon to bear.
    B lets any enemy unit do it and states no further limits.
  - UNCLEAR: arc, range and LOS presumably measured at the chosen point of the path; and which attacker movement
    band a not-yet-activated snap shooter uses (suggest: it commits to its band for the round when it fires).
  - Implementation note: offer snap fire as an interrupt at each hex step of the moving unit.
- Forward observers must act before the units they direct (A p.6; B p.129 / PDF 283).
- **Standby** (A p.6; DIFF: A only): a fire-support unit may spend an action during its own activation to wait for a
  call. If later that round a friendly target designator paints a target or a forward observer gives it one, the
  standby unit fires one weapon, once. If no call comes the action is wasted.
  UNCLEAR: whether that shot needs another action (suggest no; the standby action pays for it).
- Extra actions (A pp.5-6; B pp.128-130 / PDF 282-284): declared in Step One (Declaration) together with
  evasive maneuvers. Each action beyond the crew's free allowance gives -1 to every action roll that round
  (e.g. 1-crew Gear taking 3 actions: -2 on all three). A Command Point buys one extra action with no penalty.
  Crew-to-actions (B p.130 / PDF 284): crew 1 -> 1 action, 2 -> 2, 4 -> 3, 8 -> 4, 16 -> 5, then doubling
  (32 -> 6 ... 512 -> 10). A-internal: A p.6 text says four crew give four actions, but its own example (and B)
  give three.
- Linked weapons (Weapon Link perk, A p.17): one action fires the whole link using the worst Accuracy and the
  worst Base Range among them; each weapon rolls separately in turn, and once one misses all the remaining ones
  miss automatically (they still use their shots).

---

## 6. Evasive maneuvers (A p.11; B p.141 / PDF 295)
- Must be declared at the start of the round (Step One); cannot be switched to later in the round.
- Counts as an action. Gives +3 to every defense roll the unit makes this round.
- While evading the unit may not attack or take any other action that round (B's example: it cannot even send
  a report).
- Declaring it more than once gives no extra bonus.
- Movement is not an action, so the unit still moves (inference).
- A fumbled defense still totals 0 (B p.141).
- Implementation note: a per-round flag set at declaration that blocks all of the unit's actions.

---

## 7. Physical attacks (A pp.10-11; B pp.140-141 / PDF 294-295)

### 7.0 Common rules
- Each physical attack or throw is one action (A p.6; B p.130). They use no ammunition (B p.127 / PDF 281).
- Defender rolls Piloting (Infantry Skill if infantry) with the normal defense modifiers.
- In A every physical attack (ram, punch, kick, stomp, melee weapon, throw) rolls **Piloting + Maneuver** instead
  of Gunnery (A p.10; A p.2; A p.4 says Maneuver modifies all Piloting rolls including PB attacks).
- UNCLEAR: whether the attacker-movement modifier, PB +1, Fire Control and obscurement also apply to physical
  attacks (the extra-action penalty surely does). A names only skill + Maneuver, and says thrown items need no
  range modifier (A p.11); B applies range and movement modifiers to throws (B p.141). Close-combat weapons have
  their own Accuracy in A's table (A p.29), which suggests Accuracy applies to melee weapons.
- In A, the defender also gets the PB arc penalty (-1) whenever the reading of PB (2.2) covers the contact
  distance.

| Attack | A roll | B roll | Damage Multiplier |
|---|---|---|---|
| Ram | Piloting + Maneuver | Piloting (Maneuver not mentioned) | own Size + impact modifier; each side hurts the other (7.1) |
| Punch | Piloting + Maneuver | Gunnery + Fire Control | arm rating; attacker may choose a lower DM ("pulled" punch) |
| Kick / stomp | Piloting + Maneuver | Piloting + Maneuver | vehicle Size; damage doubled if it can put its full weight on the target |
| Melee weapon | Piloting + Maneuver | Gunnery + Fire Control | weapon's DM |
| Throw | Piloting + Maneuver, 1 hex max, no range modifiers | Gunnery with range and movement modifiers | item's DM |

- DIFF: skill per attack type (table above). B-internal: B's introduction to 7.7.3 says all physical attacks use
  Piloting except melee weapons, but B's punch paragraph uses Gunnery + Fire Control (B pp.140-141).

### 7.1 Ramming (A p.11; B p.140 / PDF 294; A p.27)
- Opposed roll as normal. Unlike every other attack, both vehicles take damage.
- **Impact speed** from the direction of the ram:

| Direction | A p.11 and B p.140 | A p.27 quick-reference |
|---|---|---|
| Head-on | attacker speed + defender speed | same |
| Side | attacker speed | (attacker speed + defender speed) / 2, rounded up |
| Rear | attacker speed - defender speed | same |

  DIFF: side impact (A-internal as well).
- "Speed" = hexes moved: the rammer's move this round, the defender's most recent move (both books' example).
- Direction in hexes (B p.140 / PDF 294 diagram), taken from the defender's facing: rammer in the 60-degree cone
  straight ahead = head-on; in the 60-degree cone straight behind = rear; anywhere else = side. For an adjacent
  rammer: front neighbour = head-on, rear neighbour = rear, the four diagonal neighbours = side. (These cones
  include the hexes on their 30-degree edges, unlike B's Rear defense arc; it only matters beyond range 1.)
- **Impact speed modifier** (A p.11; B p.140 / PDF 294; A p.27):

| Impact speed | Damage modifier |
|---|---|
| 1-2 | -2 |
| 3-4 | -1 |
| 5-6 | 0 |
| 7-9 | +1 |
| 10-19 | +2 |
| 20-99 | +3 |
| 100-999 | +4 |

  DIFF (minor): A p.11 stops at 20-99; B p.140 and A p.27 have the 100-999 row.
- Each vehicle's impact DM = its own Size + the modifier. Damage taken by each vehicle = attack MoS x the OTHER
  vehicle's impact DM; each then checks its own armor normally (A p.11; B p.140).
- Book example (both): Size 6 Gear side-rams a Size 12 tank; the Gear moved 8 -> +1; DMs 7 and 13; MoS 2 ->
  Gear takes 26, tank takes 14 (A p.11; B p.140). The A p.27 side formula, (8 + 7) / 2 = 7.5 -> 8, gives the
  same +1 here.
- After a miss (A p.11; DIFF: A only): if the rammer still has MP it moves one hex onward and stops there; otherwise it
  stops in the collision hex. UNCLEAR: "one hex onward" in hex terms (presumably one hex past the collision point
  along its path, stacking permitting).
- UNCLEAR: rear ram when the rammer is not faster (impact speed 0 or less; the table starts at 1).
- UNCLEAR: a small vehicle's impact DM can drop to 0 or below (Size 1-2 at low speed); no floor given.
- Hooks: Ram Plate perk halves impact damage taken in the plate's arc; Autopilot can be set to ram large targets
  without risking the crew (A p.17). Optional: buildings are rammed as immobile Size 6 (Urban) or Size 12 (Dense
  Urban) with defense 0 (A p.21). (Walls as Size 4 / 10 objects exist only at Duelist scale; at tactical scale
  they are folded into Urban terrain, A p.21.)
- Implementation note: resolve the ram when the rammer tries to enter the target's hex; the neighbour it comes
  from fixes the direction.

### 7.2 Punching (A p.11; B p.141 / PDF 295)
- Needs an arm or similar limb; construction-type arms only if their perk says they are strong enough.
- DM = the punching arm's rating; the attacker may deliberately use a lower DM.
- Arm damage penalties apply to punching (A p.15). Chassis Reinforcement adds +1 DM to punches and kicks
  (A p.17; A p.29). Manipulator, Tool and Battle arms punch at their rating (A p.17).
- DIFF: skill (7.0 table).

### 7.3 Kicking and stomping (A p.11; B p.141 / PDF 295)
- Walker vehicles only: kick vehicles, stomp infantry squads.
- Piloting + Maneuver against the target's Piloting (Infantry Skill against infantry).
- DM = the kicker's Size; double the damage if it can bring its full weight down on the target.
- UNCLEAR: when "full weight" applies (stomping infantry? much smaller targets?). Neither book's example doubles:
  Size 6 Gear kicks a light tank, attack 6 vs defense 2, MoS 4 -> 4 x 6 = 24 (A p.11; B p.140 / PDF 294).

### 7.4 Throwing
**A** (A p.11; A p.29):
- At least one arm. Battle and Tool arms cannot throw unless designed to; a Battle Arm cannot pick the item up by
  itself.
- Only vibroblades, vibroaxes and grenades, and only 1 inch (1 hex); no range modifiers; Piloting + Maneuver.
- A thrown vibroblade loses its Armor Piercing. To get a thrown vibroblade or vibroax back the unit must move into
  the area where it landed and spend an action there on the next turn (A p.29 footnotes).
- Grenade stats (A p.29): Hand Grenade x15, Acc -1, Anti-Infantry; Haywire Grenade x10, Acc -1, Haywire; Heavy
  Grenade x25, Acc -1; Self-Destruct Grenade x30, Acc -1, AE 0.
- UNCLEAR: whether the item's Accuracy is added; what happens to a missed throw (A has no deviation rule).

**B** (B p.141 / PDF 295):
- Items such as rocks or grenades; arm restrictions as in A.
- Base range in metres = 2 x (arm rating - item Size). If the item's Size is more than half the thrower's Size,
  half of another arm's rating may be added to the arm rating first. A weapon's Size = its Minimum Size - 1.
  Further bands double as usual.
- Gunnery roll with normal range and movement modifiers. The item lands on the target if the attack total is
  equal to or higher than the defense (a tie lands: DIFF from the normal rule).
- Miss: the item deviates 2 x MoF metres in a direction rolled on one die; on hexes, put it in the nearest hex,
  aiming point = centre of the target hex.
- Fumble: deviates as for a miss but toward the thrower; if it ends on another unit, that unit defends and is hit
  if its defense fails.
- UNCLEAR: B gives throw ranges and deviation in metres while a tactical hex is 50 m, so most throws stay in or next
  to the thrower's hex; conversion and rounding are not given.

### 7.5 Melee weapons (A p.11; B p.141 / PDF 295; stats A p.29)
- Damage = MoS x the weapon's DM from the record sheet, plus its special rules.
- A p.29 close-combat stats (DM, Acc, special): Vibroblade x8, 0, Armor Piercing; Vibrorapier x6, +1, Armor
  Piercing; Vibroax x10, -1, Armor Crushing; Chain Sword x9, 0; Mauler Fist x9, +1, Armor Crushing; Spike Gun x12,
  -1; Heavy Spike Gun x14, -1, Armor Piercing; Haywire Whip x7, +1, Entangle and Haywire; Chassis Reinforcement +1
  DM, physical attacks only.
- DIFF: skill (7.0 table).

---

## 8. Called shots (A p.12; B p.142 / PDF 296)
- Pick a location before rolling: 1 Fire Control, 2 Structure, 3 Crew compartment, 4 Movement systems,
  5 Auxiliary systems (the first five lines of the Systems Damage Table).
- -1 to the attack roll.
- If the hit causes Light or Heavy damage, a 6 on the Systems Damage location roll also hits the chosen location
  instead of "roll twice"; 1-5 apply normally (A p.12; table footnotes A p.15, B p.146 / PDF 300).
- If damage is below the armor value nothing happens, so the called location is irrelevant (follows from 11.2).
- DIFF: B only, tiny targets (headlights and the like): the shot must be aimed and needs MoS 3 or more to hit the tiny
  target; MoS 1-2 hits the chosen location but not the tiny target (B p.142).
- Weak Point flaw: when a called shot successfully hits that point, the vehicle's Base Armor is reduced by the
  flaw's rating (A p.17). UNCLEAR: for that hit only or permanently.
- Optional: at night a searchlight can be shot out with an aimed shot; success destroys it (A p.22).
- Implementation note: keep the called location on the attack record so the systems roll can use it.

---

## 9. Indirect fire (A p.12; B p.142 / PDF 296)
- Only weapons tagged Indirect Fire, plus Guided weapons, which get every indirect-fire benefit (A p.29).
  Indirect-fire weapons can also be fired directly at targets in LOS (the books' burst examples fire a rocket pack
  directly, A p.12).
- An allied unit with valid LOS to the target must act as forward observer (FO). Being FO costs the FO one
  action, plus a communication roll if enemy ECM is active (comms rules: A p.9). One FO can feed several firers.
  The FO acts before the firers (A p.6; B p.129 / PDF 283).
- The round arcs over obstacles, including intervening elevation, so the firer needs no LOS of its own.
- Attack roll: the firer's own Gunnery, Fire Control, Accuracy, range (from the firer) and movement modifiers,
  but the obscurement penalty is the FO's Concealment value toward the target instead of the firer's
  (A p.12 and B p.142, example 2).
- Hit test uses that penalty. If it hits, recompute with only the target hex's own obscurement; the MoS rises by
  the difference.
- Miss: the round lands MoF hexes away from the target hex, direction random.
- UNCLEAR: what a scattered round does where it lands (attack anything there? AE blast there?). A's thunderstorm
  text implies misses can come down on the FO (A p.24).
- UNCLEAR: both books' example 2 says the FO-to-target obscurement is 0 yet speaks of the MoS rising when
  obscurement is removed; with a 0 penalty there is nothing to remove. Follow the rule text.
- UNCLEAR: whether the free communication action earned by a successful active-sensor lock (A p.8) or target
  designation (A p.6) can stand in for the FO action.
- UNCLEAR: whether an IF weapon firing at a target its own crew can see may still use the "target hex only" MoS
  boost.
- Minimum ranges come only from weapon tags (2.2).
- Target designator (A p.17; A p.6): a designator "attack" has Base Range = its rating, Accuracy +0, no Fire
  Control, no damage; on a success the target stays marked for friendly Guided weapons for the rest of the round
  (ECM cannot undo it), and the designating unit gets a free communication action. Guided weapons get +2 against a
  painted target (A p.29). Standby units fire on the paint or the FO's call (section 5).
- Counter Battery Sensors perk: a unit within half its Sensor range of an indirect-firing weapon may try to locate
  it with an Electronic Warfare test against threshold 1 (base range = perk rating x Sensor range); on success it
  can pass the coordinates on like an FO (A p.17).
- Optional: thunderstorm -2 to indirect fire (A p.24); no indirect fire in a sandstorm (A p.25); heat haze -1 to
  artillery (A p.25); a unit in Full Hull-Down may only use indirect weapons (A p.20).
- Implementation note: roll the scatter direction on 1d6 mapped to the six hex directions (my suggestion; B uses
  one die for throw deviation direction).

---

## 10. Rate of Fire attacks (A pp.12-13 core; A p.18 optional ammunition rules; B pp.143-144 / PDF 297-298)

A's core rules drop ammunition counting; A's optional ammunition rules (A p.18) are the same as B's original.

### 10.1 ROF bookkeeping, A core (A p.12; A p.4)
- A weapon's ROF = the number of special "ROF Attacks" (burst, walking, saturation) it can make before reloading.
  ROF 0: no ROF Attacks; one shot per action.
- Each ROF Attack used lowers the count by 1. At 0 the weapon can still fire single shots.
- Reloading a gun costs one action and restores all ROF Attacks (or the shots of a ROF 0 gun); all Gears are
  assumed to carry spare clips. Exceptions: the Deployable Pack Gun cannot be reloaded; missiles and mortars cannot
  be reloaded in battle (they keep firing single shots).
- Only weapons holding 5 or fewer shots (grenades, single-use weapons) track individual shots.
- UNCLEAR: the weapon tag "Disposable" (A p.28) is not defined; presumably single-use, tracked under the 5-shot rule.
- B / A optional: track every round of ammunition; out of ammo = cannot fire; guns reload a clip with an action,
  missiles and mortars are gone once spent (A p.18; B p.127 / PDF 281).
- Implementation note: per weapon keep "ROF Attacks left" and, if 5 or fewer, "shots left".

### 10.2 Burst fire
- Any weapon with ROF 1+.
- **A core** (A p.12): add the full ROF to the DM; one ROF Attack used. A examples: Light Autocannon x8, ROF 2
  -> x10; LRP/24 x12, ROF 3 -> x15; MoS 2 gives 20 and 30 damage.
- **B and A optional** (B p.143 / PDF 297; A p.18): against vehicles and hard targets (buildings, structures)
  add the ROF to the DM; against infantry and other soft targets add the ROF to the MoS instead. 10 rounds of
  ammunition per ROF point used; the shooter may use only part of the ROF; ROF 0 uses 1 round. Book example:
  x8 gun, ROF 2, MoS 2: full ROF 20 damage (20 rounds), ROF 1 18 damage (10 rounds); against infantry MoS becomes
  4 -> 32.
- Missiles and rockets (B p.143; A p.18): instead of 10 rounds per point they use 2^ROF missiles (ROF 1 -> 2,
  2 -> 4, 3 -> 8, 4 -> 16).
- DIFF: A core adds ROF to DM whatever the target. B also confirms the soft-target rule in its infantry section
  (B p.151 / PDF 305).
- UNCLEAR: A's optional text says the soft-target bonus goes on the "attack roll", but its example adds it to the
  MoS; B says MoS. Use MoS (bonus applies only after a hit).
- DIFF: against man-made structures A's optional structure rules add ROF to the MoS (A p.21); B adds it to the DM
  for buildings (B p.143).

### 10.3 Walking fire (A p.13; B p.143 / PDF 297; A p.18)
- Declare before any roll. Up to ROF + 1 targets, all inside the weapon's arc; one separate attack roll per
  target; no target may be attacked twice in the round by the same weapon.
- **A core**: each attack uses the weapon's normal DM (A's example: three targets, each at x12); one ROF Attack.
- **B / A optional**: each extra target lowers the ROF by 1 for damage purposes, the remainder being the burst
  bonus on every attack; ammunition is charged for the full ROF (example: ROF 4 rocket pod, 3 targets -> +2 burst
  on each attack, 16 rockets used).
- DIFF: damage bonus and cost as above.

### 10.4 Saturation fire (A p.13; B p.144 / PDF 298; A p.18)
- Weapon with ROF 1+. Choose one hex (A: a 1-inch area). Roll one attack, adding half the ROF rounded down; the
  ROF does not raise DM or MoS. Write the total down.
- Every unit in that hex now, or entering it later this round, is attacked automatically, friend or foe, and makes a
  normal defense roll against that number. It must beat it; damage = the defender's MoF x the weapon's DM
  (a tie = MoF 0 = no damage).
- Range limit: A core no farther than 2 x BR; B / A optional no farther than the weapon's Medium range (the same
  2 x BR).
- Cost: A core one ROF Attack. B / A optional 30 rounds (or 8 rockets) per ROF point used; if the weapon has
  less, the attack still stands but the magazine is emptied; at least 10 rounds (or 4 rockets) are needed.
- UNCLEAR: "attacked regardless of speed, maneuver or allegiance" - read as "nobody can avoid being attacked";
  both books still call for a normal defensive roll, so defenders keep their normal modifiers.
- UNCLEAR: which attack modifiers apply when the target is a hex (presumably range, movement, Fire Control,
  Accuracy; obscurement of the hex not stated).
- Implementation note: a zone effect on one hex lasting until the end of the round, triggered on entry (an
  interrupt during movement) and once immediately for units already there.

### 10.5 Area-effect weapons (A p.13; B p.144 / PDF 298; A p.29)
- Rated "AE N" = blast radius N hexes (A: inches). AE 0 = target hex only; AE 1 = target hex plus its six
  neighbours (A p.29: AE 1 = 3-inch diameter; B p.144 diagram).
- One attack roll. Every vehicle and squad in the blast, allies included, makes its own defense roll against it.
- Defender beaten: damage = its MoS x DM. Defender not beaten (tie or better): it still takes concussion damage
  equal to half the DM, as flat damage.
- B example (B p.144): AE 1, x30, aimed at a Gear with 15/30/45; attack 3; the Gear defends 6, a strider next to it
  3; both take 15; the Gear suffers Light damage, the strider shrugs it off.
- A's example (A p.13) is anti-infantry AE against squads (damage per trooper); see the infantry notes (A p.16).
- UNCLEAR: rounding of half DM for odd DMs (e.g. x3, x15); whether a hex (not a unit) can be the aim point
  (A's example places a template on an area, which suggests yes); per-defender obscurement; what a direct-fire AE
  miss does (no scatter rule outside indirect fire).

---

## 11. Damage and armor (A p.14-15; B p.145, p.147 / PDF 299, 301; A p.27)

### 11.1 Total damage
- Total damage = MoS x Damage Multiplier (A p.14; B p.145). DMs scale roughly exponentially in effect (x10 is about
  four times x5, A p.4; B p.127).
- DM adjustments covered elsewhere: burst (+ROF, 10.2), walking fire (10.3), AE concussion (half DM flat, 10.5),
  ram (Size + impact modifier, 7.1), kick (Size, doubled with full weight, 7.3), punch (arm rating, 7.2),
  "-N per range band" tag (2.2), Chassis Reinforcement (+1 to punch and kick), optional simplified damage (19).

### 11.2 Damage versus armor (A p.14; B p.145 / PDF 299; A p.27)
Compare with the target's current Base Armor (call it Ar):

| Damage | Result | Effect |
|---|---|---|
| below Ar | No effect | bounces off |
| Ar or more, below 2 x Ar | Light Damage | Base Armor -1 permanently, roll on the Light Systems Damage Table |
| 2 x Ar or more, below 3 x Ar | Heavy Damage | Base Armor -2 permanently, roll on the Heavy Systems Damage Table |
| 3 x Ar or more | Overkill | vehicle destroyed, remove it |

- Only the worst result applies (no Light on top of Heavy).
- A-internal: the Light row of A p.14's table prints the armor change without its minus sign and with a stray
  dash in front of "roll"; A p.15 text, A p.27 and B all give -1.
- Test vectors (mine), Base Armor 15 (15/30/45): 14 -> none; 15 -> Light; 29 -> Light; 30 -> Heavy;
  44 -> Heavy; 45 -> Overkill.

### 11.3 The armor value used for the comparison (hooks)
- Armor Piercing weapon: halve the target's armor, round up (A p.29).
- Reinforced Armor perk: + its rating when hit on the reinforced facing (A p.17).
- Weak Facing flaw: halve Base Armor, rounded up, when attacked on that side (A p.17).
- Heat Resistant Armor perk: + its rating against heat weapons (bazookas, mortars, grenades, missiles, lasers,
  particle beams) (A p.17).
- Shield perk: + half its rating in the front or side arc (owner chooses). After a failed defense the owner can pay
  one action for a second defense roll; if that one succeeds the hit still lands as first rolled, but the full
  rating is added instead.
  After Light damage the shield rating is halved; Heavy damage after the shield was used destroys it (A p.17).
- Weak Point flaw: see 8.
- UNCLEAR: order of operations when several apply (e.g. AP on a reinforced facing).

### 11.4 When a vehicle is destroyed (A p.14; B p.145 / PDF 299)
- Base Armor reaches 0; or Overkill; or a Systems Damage result says it is destroyed.
- See 13.3 for a vehicle whose whole crew is lost.

### 11.5 Armor loss (A p.15; B p.147 / PDF 301)
- Light: -1 Base Armor, Heavy: -2 Base Armor, permanently, besides the systems effect. Thresholds are recomputed
  from the new value (each point lost lowers the Heavy threshold by 2 and the Overkill threshold by 3).
- Losses always come off Base Armor even if the vehicle has armor perks (A p.15). DIFF: B lets players take the
  points off the perk instead if they prefer (B p.147).
- Armor Crushing weapons: 1 extra point on a Light result, 2 extra on Heavy (A p.29).
- Both books' example: 15/30/45 takes 24 -> Light -> 14/28/42; then takes 28 -> now Heavy -> 12/24/36; 36 or more
  would now kill it.
- Optional Reality Distortion armor loss per damaging hit (A p.25): Gritty 0 / -1, Adventurous -1 / -2 (the
  standard rule), Cinematic -2 / -4 (Light / Heavy).
- Haywire weapons: a Light or Heavy result from them is rolled on the Systems Damage Table twice (A p.29);
  Vulnerable to Haywire flaw: one more roll (A p.17).

---

## 12. Systems Damage tables

### 12.1 Procedure (A p.14; B p.145 / PDF 299)
- After Light or Heavy damage the attacker rolls 1d6 on the matching table (plain d6, not a Skill roll).
- When the result could hit one of several components (which weapon, which arm, which movement type...), roll
  1d6: odd, the defender picks; even, the attacker picks.
- A result naming a system the vehicle does not have, or has already lost, does nothing beyond the armor loss.

### 12.2 Main table (A p.15; B p.146 / PDF 300; A p.27)

| d6 | System | Light Damage | Heavy Damage |
|---|---|---|---|
| 1 | Fire Control | roll on Subtable A | roll on Subtable A, +1 |
| 2 | Structure | roll on Subtable B | roll on Subtable B, +1 |
| 3 | Crew | crew stunned: lose 1 action for 1 round | 10% casualties, at least 1 |
| 4 | Movement | -1 MP | half the remaining MP (round down) and -2 Maneuver |
| 5 | Auxiliary systems | 1d6 auxiliary systems each take -1 (A: pick each on Subtable C) | 1d6 auxiliary systems destroyed (A: pick each on Subtable C) |
| 6 | Roll twice on this table | roll twice more on the Light table | roll twice more on the Heavy table |

- Called shot: a 6 is applied as the chosen location (1-5) instead of "roll twice" (footnote to both tables).
- DIFF: B's auxiliary row has no Subtable C (B p.146).
- UNCLEAR: whether a 6 rolled during a "roll twice" spawns two more rolls (suggest yes, keep chaining).

### 12.3 Subtable A: Fire Control (A p.15; B p.147 / PDF 301; A p.27)

| Roll | Effect |
|---|---|
| 1 | -1 to one weapon |
| 2 | -2 to one weapon |
| 3 | -1 to every weapon |
| 4 | one weapon destroyed |
| 5 | Fire Control system destroyed: -5 to attacks |
| 6 | roll twice on Subtable A |
| 7 | ammunition / fuel hit, roll 1d6: 1-3 ammo store and fuel tank ruptured, the vehicle can no longer move or fire; 4-6 chain reaction, the vehicle explodes and the crew is killed |

- 7 is only reachable with the Heavy +1 (or a flaw modifier).
- UNCLEAR: whether the Heavy +1 also applies to the two rolls caused by a 6.

### 12.4 Subtable B: Structural (three versions)

| Roll | A text (A p.15) | A quick-reference (A p.27) | B (B p.147 / PDF 301) |
|---|---|---|---|
| 1 | -1 MP | same | same |
| 2 | half the remaining MP (round down) | same | same |
| 3 | -1 Maneuver | same | same |
| 4 | -2 Maneuver | same | same |
| 5 | Power Transfer Failure: no movement | same | same |
| 6 | roll twice on Subtable B | roll twice on Subtable B | catastrophic crew compartment failure: 75% casualties, at least 1 |
| 7 | catastrophic structural damage, roll 1d6: 1-3 vehicle destroyed, crew survives; 4-6 crew compartment failure, 75% casualties, at least 1 | roll 1d6: 1-3 crew compartment failure, 75% casualties, at least 1; 4-6 complete structural failure, vehicle destroyed, crew survives | complete structural failure: vehicle destroyed, crew survives |

- DIFF: in B a Light hit (roll 6) can wreck the crew compartment; A moved it to 7 (Heavy only), as A p.2 says
  light damage can no longer kill a crew.
- A-internal: the 1-3 / 4-6 sub-results of 7 are swapped between A p.15 and A p.27.

### 12.5 Subtable C: Auxiliary systems (A only: A p.15; A p.27)
Roll once per auxiliary system hit:

| Roll | System hit | If that perk is absent or already destroyed |
|---|---|---|
| 1 | Target Designator | Sensors |
| 2 | Autopilot | Sensors |
| 3 | Smoke Launchers | Sensors |
| 4 | ECM | Communications |
| 5 | ECCM | Communications |
| 6 | Satellite Uplink | Communications |

If the fallback is gone as well, nothing more happens (12.1).

---

## 13. What each kind of system damage does

### 13.1 Fire Control and weapons (A p.14; B p.145-147 / PDF 299-301)
- Weapon damage results are Accuracy penalties on the chosen weapon; they add up. At a total of -5 the weapon is
  out of action; at -6 or worse it is destroyed and blown off (A p.14). "-1 to every weapon" adds -1 to each.
- With several weapons, use the odd/even pick (12.1). Arms count as weapons (section 14).
- Fire Control destroyed: -5 to attacks (A p.15). A vehicle whose sensors or fire control are completely
  destroyed can still attempt actions needing them, at -5 (A p.14; B p.146).
- DIFF (minor): the -5 out / -6 destroyed rule for weapons is stated in A p.14; B states it only for arms (B p.147).
- Hooks (A p.17): Shielded Weapons perk ignores one weapon result from Subtable A, then is lost; Exposed Fire
  Control flaw +1 on Subtable A rolls; Hazardous Ammo/Fuel flaw +2 on the ammo/fuel sub-roll; Ammo/Fuel
  Containment perk ignores the first ammo/fuel hit (armor still lost).

### 13.2 Structure (A p.14; B p.145 / PDF 299)
- MP losses apply to one movement type; Maneuver losses apply to the whole vehicle.
- Power Transfer Failure: every movement system is out; weapons still fire.
- Catastrophic crew compartment failure: the structure caves in on the crew. The Reinforced Crew Compartment perk
  does not help; the Reinforced Structure perk absorbs it like any other structure hit.
- UNCLEAR: A's perk list (A p.17) has "Reinforced Chassis" (ignore the first Structure hit, armor still lost), not
  "Reinforced Structure"; assume they are the same perk.
- UNCLEAR: Power Transfer Failure puts all movement out of service, and totalled movement makes defense 0 (13.4);
  neither book links the two explicitly (suggest: defense 0).
- Fragile Chassis flaw: +1 on Subtable B rolls (A p.17).

### 13.3 Crew (A p.14; B p.146 / PDF 300)
- Crew stunned: one action lost. If the unit already used its actions this round, the loss carries into the next
  round(s) as a debt; Command Points may pay it (both). B: the debt is paid off by normal or extra declared actions.
- Casualties lower the crew count and so the number of free actions (5).
- Passengers aboard: one die decides whether crew or passengers take the hit (A p.14; B p.146). UNCLEAR: which die
  results mean which. A's Passenger Seating perk says to randomise among crew and passengers (A p.17).
  Passengers normally cannot replace crew.
- UNCLEAR: rounding of 10% and 75% casualties (only the minimum of 1 is given).
- A crewless unit takes no actions (A p.6). UNCLEAR: whether it otherwise counts as destroyed or merely disabled
  (suggest: out of play, wreck remains).
- DIFF: B only: after the battle roll 1d6 per casualty: 1-3 dead, 4-6 wounded; for big crews assume half and half
  (B p.146). Campaign only.
- Hooks (A p.17): Reinforced Crew Compartment ignores the first Crew hit, then is lost; Emergency Medical ignores
  the first Crew Stunned (armor still lost); Exposed (crew compartment) flaw makes the hit one step worse
  (Light becomes Heavy; Heavy destroys the vehicle).

### 13.4 Movement (A p.14; B p.146 / PDF 300)
- A speed loss is taken from Top Speed (A: from one of the vehicle's Top Speeds, i.e. one movement type; B: from
  Top Speed first) and Combat Speed is then recalculated. DIFF (wording).
- UNCLEAR: recalculation rounding. A's sample datacard (A p.4, speeds 3/6 and 6/11) fits Combat = Top / 2 rounded
  up; check against the movement notes.
- UNCLEAR: "half the remaining MP" - most likely half the current Top Speed rating, not the MP left this turn.
- If all movement systems are knocked out, by accumulated losses or by heavy damage, the vehicle's defense total is
  0 from then on (A p.14; B p.146). UNCLEAR: A adds "max. -5" to the accumulated losses without saying what it
  means for MP (five hits? speed reaching 0?).
- Hooks (A p.17): Rugged Movement Systems ignores the first movement hit; Exposed (movement) flaw makes it one step
  worse.

### 13.5 Auxiliary systems (A p.14; B p.146 / PDF 300)
- Sensors, Communications, and every perk marked AUX (A lists Autopilot, ECM, ECCM, Smoke Launchers, Satellite
  Uplink, Target Designator; B's examples add life support and ejection seats).
- A damaged aux system takes -1 per hit on any roll using it (or loses 20% efficiency, e.g. mining gear); at -5 it
  is destroyed (both).
- Communications destroyed: cannot act as forward observer (both); A: cannot receive Command Points; B: cannot talk
  to other units. DIFF.
- DIFF: B only: a turret counts as an auxiliary system and can be disabled (the "-1" results do not affect it); a disabled
  turret is frozen and its weapons become fixed in the arc where they last fired (B p.146). UNCLEAR: which arc
  code that becomes.
- Hooks (A p.17): Backup Sensors ignores the sensor effect of the first aux hit; Backup Communications ignores the
  first aux hit on comms; Sensor Dependent flaw: no attacks or movement once sensors are damaged or destroyed.

---

## 14. Damage to arms (A p.15; B p.147 / PDF 301)
- Battle, Tool and Manipulator arms are part of the chassis but count as weapons for damage (A: "are"; B: "may
  be" - minor DIFF).
- If the machine carries no other weapon, "weapon" results go to the arms automatically; otherwise use the usual
  odd/even pick.
- Damage to an arm also applies to the weapon it is holding. Handheld weapons are normally listed in the design;
  if not, they are unaffected.
- Arm penalties apply equally to everything the arm does: firing the held weapon, punching, manipulating. At -5
  total the arm is out of action; at -6 or worse it is destroyed and blown off.
- UNCLEAR: whether a held weapon then suffers the penalty twice (once as "arm penalty to weapon fire" and once as
  "damage passed to the weapon"). Suggest once.

## 15. Picking up guns (A p.15, A only)
- A unit can take the handheld weapons and grenades (not missile packs or other mounted weapons) of an enemy Gear
  it has destroyed. One action. The weapon comes in its state just before the wreck died (damage, remaining
  ROF Attacks or shots). Reloading it is a second action. No TV cost.
- UNCLEAR: where the unit must be (presumably in the wreck's hex, by analogy with retrieving a thrown blade,
  A p.29); whether it must be the unit that destroyed it.
- DIFF: B has no rule for picking up weapons.

## 16. Ejection and evacuation (B p.148 / PDF 302, B only)
- Overkill without an ejection system: the crewman escapes unharmed only on a 6 on one die.
- With an ejection system: one die; 1-2 the system fails and he dies; 3+ he ejects and lands in the same hex.
- A crewman can also be ejected voluntarily from a vehicle with an ejection system: one action.
- (Roleplaying option: the GM may let player characters always eject.)
- DIFF: A has no ejection rules (A's only related action is embarking/disembarking crew or passengers, A p.6).

---

## 17. Weapon special characteristics that hook into attacks (A p.29)

| Tag | Effect |
|---|---|
| Anti-Infantry (AI) | No -2 when attacking infantry. |
| Area Effect (AE N) | Blast radius N (10.5); AE 0 = 1-inch area, AE 1 = 3-inch diameter. |
| Guided | All indirect-fire benefits; +2 against targets painted by an allied target designator. |
| Haywire | Two Systems Damage rolls when it causes Light or Heavy damage. |
| Indirect Fire | May fire indirectly (section 9). |
| Minimum Range N | Cannot attack targets nearer than N. |
| -N Dam. per R.B. | DM drops by N per band beyond Short. |
| Armor Crushing | 1 extra armor point lost on Light, 2 extra on Heavy. |
| Armor Piercing | Target armor halved (round up). |
| Incendiary | Meant to set targets alight (optional fire rules, 19). |

UNCLEAR: tags used in the tables but not defined in A: Disposable, Slow Burn (Inc.), Entangle, Physical Attack Only.

## 18. Perk and flaw hooks (A p.17; full perk rules belong to the unit notes)

| Perk / flaw | Hook | Effect |
|---|---|---|
| Acrobatic Handling | defense / close combat | +rating to Maneuver in close combat and vs PB attacks |
| Advanced Neural Net | defense | spend an action: +1 Maneuver to end of turn, once per turn |
| Ammo/Fuel Containment | systems | ignore first ammo/fuel hit, armor still lost |
| Anti-Personnel Charges | attack | fire at PB against attackers, no action; 2 dice + rating; DM x3; counts as AI; uses 1d6 charges |
| Autopilot (AUX) | defense / ram | dodges as a Skill 1 pilot; can be set to ram big targets with no risk to the crew |
| Backup Communications / Backup Sensors | systems | ignore first aux hit on that system |
| Chaff/Flare | defense | +rating on defense rolls against Guided weapons |
| Chassis Reinforcement | physical | +1 DM to punches and kicks |
| Counter Battery Sensors | indirect fire | locate indirect firers (section 9) |
| Emergency Medical | systems | ignore first Crew Stunned, armor still lost |
| Heat Resistant Armor | armor | +rating vs heat weapons |
| Improved Rear Defense | defense | no rear-arc penalty |
| Low Profile | obscurement | +1 obscurement when behind 2+ obscurement and when Hull Down |
| Camo Netting | obscurement | +1 concealment in Woodland or Jungle while stationary |
| Pintle Mount | attack | -1 when moving up to Combat, -2 at Top |
| Ram Plate | ram | half damage from impacts in the plate's arc |
| Reinforced Armor | armor | +rating on the reinforced facing |
| Reinforced Chassis | systems | ignore first Structure hit, armor still lost |
| Reinforced Crew Compartment | systems | ignore first Crew hit (not vs crew compartment failure) |
| Rugged Movement Systems | systems | ignore first movement hit |
| Shield | armor / defense | 11.3 |
| Shielded Weapons | systems | ignore one weapon result on Subtable A, then lost |
| Smoke Launchers (AUX) | obscurement | range 1, no action, during movement; smoke radius 1, obscurement 2 to end of turn |
| Sniper Systems | attack | +1 at Long and Extreme |
| Stabilizer Mount | attack | one action while stationary to deploy before firing oversized weapons; moving cancels it |
| Target Designator (AUX) | attack | section 9 |
| Weapon Link | attack | section 5 |
| Defective (Fire Control / Active Sensors) | attack | 1d6 before the test; if <= rating, -rating |
| Exposed (AUX / Crew compartment / Movement) | systems | that hit one step worse; Heavy destroys the vehicle |
| Exposed Fire Control Systems | systems | +1 on Subtable A |
| Fragile Chassis | systems | +1 on Subtable B |
| Hazardous Ammo/Fuel Storage | systems | +2 on the ammo/fuel sub-roll |
| Sensor Dependent | attack | no attacks or movement once sensors are damaged |
| Unstable / Decreased Maneuver | defense | -1 Maneuver at Top Speed / -rating in the listed mode |
| Vulnerable to Haywire | systems | one more roll from Haywire weapons |
| Weak Facing | armor | Base Armor halved (round up) on that side |
| Weak Point | armor | called shot on it lowers Base Armor by the rating |

---

## 19. Optional rules in A that touch attacks or damage (A ch.2)

- **Simplified damage** (A p.20; meant for very large games): same thresholds. Light = shaken, no lasting effect;
  a second Light is replaced by one Heavy. Heavy = all MP allowances and weapon damage halved (round up); a second
  Heavy = Overkill. Overkill = destroyed. UNCLEAR: whether armor still degrades (presumably not).
- **Walker knockdown** (A p.22): if one attack does damage of at least twice the walker's Size, the pilot makes a
  Piloting test against threshold MoS + 1; failure = falls down; fumble = falls hard and takes Light damage.
  Standing up costs 1 MP before any other movement. UNCLEAR: whether a tie passes; what being down does to
  defense or attacks.
- **Climbing and falling** (A p.22): falling damage at tactical scale = two dice x vehicle Size x levels fallen
  (Duelist scale halves it). UNCLEAR: whether "two dice" is read Silhouette-style (highest) or summed. Climbing
  walkers and infantry: -2 on all defense rolls.
- **Hull-Down Positions** (A p.20): pay extra MP by terrain (Clear not possible; Rough, Woodland +2; Sand, Moon
  Dust +3; Jungle +1; Swamp +1; Water, Liquid Gases +3 (only if fully immersed); Snow, Frozen Gases +2; Ice +3;
  Urban +2; Dense Urban +1; elevation edge +1). Hull-Down: +1 obscurement, direct fire allowed. Full Hull-Down: +2
  obscurement, only indirect weapons and systems. Walkers may go prone for 1 MP less (1 MP to get up); infantry
  count as prone walkers. Aircraft cannot (VTOLs with stall speed 0 and Nap-of-Earth flyers excepted).
- **Man-made structures** (A p.21): structures take damage points against a Damage Point Capacity instead of
  Light/Heavy (Urban 80, Dense Urban 100 per hex; roads 100); ROF adds to MoS against them. Damaging a building
  also attacks units inside at half DM and MoS - 1; an attack aimed at a unit inside that misses hits the building
  with full DM x MoF. Rams: buildings are immobile Size 6 / 12 with defense 0. Walls are a Duelist-scale feature
  only; at tactical scale they are ignored as part of Urban terrain.
- **Firestarting** (A p.20): 100 points of incendiary Intensity into one hex ignites it (no attack roll; persistent
  incendiaries count double); fires grow from 10 to 20 then spread downwind; a vehicle crossing a fire tests
  Piloting against half the Intensity or takes an incendiary attack with MoS = its MoF (fumble: MoS = the
  threshold).
- **Night** (A p.22): Concealment doubled for visual detection; units without sensors move at half speed and take
  -1 to weapon fire; searchlights light up targets and can be shot out (8).
- **Optional ammunition rules** (A p.18): as B, see section 10.
- **Electronic warfare** (A p.25): ECM "spoof" - against missile or Guided attacks the defender also rolls
  Electronic Warfare + ECM rating as a second defense; if either defense succeeds the attack fails.
- **Reality Distortion** (A p.25): see 1.4 and 11.5.
- **Weather** (A pp.24-25): mostly concealment changes (LOS notes); direct attack effects: thunderstorm -2 to
  indirect fire, sandstorm no indirect fire, heat haze doubles range and -1 to artillery, extreme cold -1 to all
  action tests for unprotected units, mud lowers infantry ROF by 1.
- **Drones** (A p.20): remote control -1 to the drone's actions, via satellite uplink -2; any damage effect that
  would hurt the crew cuts the drone off.

## 20. Off-table support (A p.26, A only; B defers to its Chapter 8 and sourcebooks, B p.129 / PDF 283)
Common: bought before the game; resolved only during Step Four (Miscellaneous Events); needs a living side
commander or second-in-command; the caller needs LOS to the target, working communications, a saved action, and must beat any
ECM with the call.

| Strike | Cost | Attack roll | Result |
|---|---|---|---|
| Artillery | 100 TV | 1d6 + 1, no other modifiers; a 1 is a fumble and fails | defenders roll normally; on MoS 1+ the 1-inch target area is saturated: x12 damage to units in it; the area stays saturated until the next Miscellaneous Events phase, anyone staying in or entering defends against the recorded number |
| Air | 300 TV | 2d6 + 2 (read Silhouette-style), no other modifiers; all ones = fumble and fails | defender rolls normally; on MoS 1+ the targeted unit takes damage at DM x15 |
| Orbital | 1000 TV | 3d6, no modifiers; all ones = fumble and fails; needs a living unit with Satellite Uplink | target unit destroyed outright, no defense roll; only that one unit |

- UNCLEAR: artillery "x12 damage" read as MoS x 12 per defender, like saturation fire; orbital strike "success"
  with no defense roll presumably means any non-fumble.

---

## 21. Worked examples (my own numbers; for unit tests)

### Example 1 - direct fire with burst, Light damage (A rules, then the B result)
Setup:
- Gear A, Qualified (Gunnery 2 dice, +1). Fire Control 0. Light Autocannon: 2/4/8/16 (BR 2), Acc 0, x8, ROF 2,
  two ROF Attacks left. Combat Speed 6; announced the half-Combat band (so at most 3 MP this activation) and
  fires after spending 3 MP.
- Gear B, Qualified (Piloting 2 dice, +0). Maneuver 0. Base Armor 16 (16/32/48). Moved 4 hexes earlier this round.
- A is 3 hexes straight behind B (bearing 180 from B). One of the two hexes between them is Woodland, the other
  Clear; B stands in Clear. Concealment 1, so LOS exists (Detection 4).
- No other unit of A's combat group has attacked B this round. A elects burst fire.

| Step | Value |
|---|---|
| Gunnery dice 4, 6 | 6 |
| Attribute | +1 |
| Fire Control / Accuracy | 0 / 0 |
| Range 3 vs BR 2 = Medium | -1 |
| Movement 3 of 6 MP = half Combat or less | +1 |
| Obscurement | -1 |
| **Attack total** | **6** |
| Piloting dice 5, 3 | 5 |
| Maneuver | 0 |
| Target speed 4 hexes | -1 |
| Arc (A: rear) | -1 |
| MAP (first attacker) | 0 |
| **Defense total** | **3** |

- 6 > 3: hit, MoS 3. Burst: DM 8 + 2 = 10; LAC ROF Attacks 2 -> 1.
- Damage 3 x 10 = 30. 30 >= 16 and < 32: Light. Base Armor 16 -> 15 (15/30/45).
- Light Systems Damage roll 4 = Movement: -1 MP to one of B's Top Speeds; Combat Speed recalculated.
- Same shot under B: bearing 180 is in B's Rear wedge, -2, so defense 2 and MoS 4; burst against a vehicle still
  adds ROF to DM -> 4 x 10 = 40 -> Heavy (>= 32, < 48) -> Base Armor 14, Heavy table; 20 rounds of ammunition used
  instead of an ROF Attack.

### Example 2 - indirect fire through a forward observer, Heavy damage (A rules; B identical here)
Setup:
- Vehicle C, Qualified (Gunnery 2 dice, +1). Fire Control 0. Light Field Mortar: 4/8/16/32 (BR 4), Acc -1, x15,
  ROF 0, Indirect Fire, AE 0, Min Range 4. Began the round at Combat Speed and spends 0 MP (Stationary).
- Tank T, Qualified (Piloting 2 dice, +0). Maneuver -2. Base Armor 22 (22/44/66). Moved 6 hexes this round.
  C lies in T's front arc. T is 10 hexes from C (Long, 9-16; above Min Range 4). A ridge blocks C's LOS.
- Gear D (C's ally) has LOS to T with one Woodland hex between them, and T stands in Jungle: D's Concealment
  value toward T = 1 + 2 = 3. D spends one action as FO, before C fires. No ECM.

| Step | Value |
|---|---|
| Gunnery dice 6, 6 | 7 (6, +1 for the extra six) |
| Attribute | +1 |
| Fire Control / Accuracy | 0 / -1 |
| Range Long | -2 |
| Stationary | +2 |
| Obscurement (FO's value) | -3 |
| **Attack total** | **4** |
| Piloting dice 2, 3 | 3 |
| Maneuver | -2 |
| Target speed 6 hexes | 0 |
| Arc front | 0 |
| **Defense total** | **1** |

- 4 > 1: hit (MoS 3 so far). Hit confirmed, so recount obscurement as the target hex only (Jungle 2): attack total
  4 + 3 - 2 = 5, MoS 4.
- Damage 4 x 15 = 60. 60 >= 44 and < 66: Heavy. Base Armor 22 -> 20 (20/40/60).
- Heavy Systems Damage roll 2 = Structure: Subtable B roll 4 + 1 = 5 = Power Transfer Failure: T cannot move but
  can still fire (and, by the reading in 13.2, defends at 0 from now on).
- AE 0: any other unit in T's hex defends separately against the same attack; if it is not beaten it still takes
  half of x15 (UNCLEAR rounding).
- Test vectors: if T's defense total had been 4, the attack misses (tie) even though the target-hex-only total
  would have been 5, because the recount happens only after a hit. If T's total had been 6, MoF 2: the round
  scatters 2 hexes in a random direction.

### Example 3 - head-on ram, both vehicles damaged (A rules, then B)
Setup:
- Gear R, Size 6, Qualified (Piloting 2 dice, +0), Maneuver +1, Base Armor 15 (15/30/45). Moved 6 hexes this round
  and rams tank K from the hex directly in front of K (head-on).
- Tank K, Size 10, Qualified (Piloting 2 dice, +0), Maneuver -2, last move 5 hexes, Base Armor 22 (22/44/66).
- Impact speed 6 + 5 = 11 -> +2. Impact DM: R = 6 + 2 = 8; K = 10 + 2 = 12.
- Assumptions (see 7.0 UNCLEAR): attack = Piloting + Maneuver only; A's PB defense penalty applies because the
  vehicles are in contact.

| Step | Value |
|---|---|
| Piloting dice 5, 2 | 5 |
| Maneuver | +1 |
| **Attack total** | **6** |
| K Piloting dice 4, 1 | 4 |
| Maneuver | -2 |
| Target speed 5 hexes | 0 |
| Arc (A: PB) | -1 |
| **Defense total** | **1** |

- 6 > 1: hit, MoS 5.
- R takes 5 x 12 = 60 -> >= 45: Overkill, R is destroyed.
- K takes 5 x 8 = 40 -> >= 22 and < 44: Light. K Base Armor 22 -> 21, roll on the Light table.
- Under B: head-on is Front (0) and there is no PB penalty, so K defends 2; B does not mention Maneuver for rams, so
  R's total is 5 (6 if Maneuver is added). MoS 3 (or 4): R takes 36 (Heavy) or 48 (Overkill); K takes 24 or 32
  (Light either way).

### Small vectors
- Area effect: AE 1, x20, aimed at Gear P (Base Armor 12); attack total 4. P defends 5: not beaten, concussion
  10 -> below 12, no effect. Gear Q in the next hex (Base Armor 14) defends 2: MoS 2 -> 40 -> Heavy (>= 28, < 42).
- Saturation: LAC (ROF 2) saturates a hex, attack roll 5 + 1 = 6 recorded. A unit entering later defends 4: MoF 2
  -> 2 x 8 = 16 damage. A unit defending 6: tie, no damage.

---

## 22. Open questions (all UNCLEAR items)
1. Defender fumbles against an attack total of 0: hit with MoS 0 means no damage? (1.3)
2. Point Blank distance in hexes for A: same hex only, or adjacent too? Drives the A PB defense penalty. (2.2, 4.3)
3. Whether abeam hexes (bearing 90/270) are inside F and Rr weapon arcs. (2.1)
4. Fire Control destroyed: does -5 replace the rating or add to it? Do ordinary attacks "need sensors"? (3)
5. B: arc for attacks from the same hex; arc for indirect fire and for blast victims. (4.3)
6. MAP: per round reset; repeat attacker uses which count. (4.4)
7. Snap fire: where arc/range/LOS are measured; which movement band a not-yet-activated shooter uses. (5)
8. Standby shot: free, or does it need another action? (5)
9. Physical attacks: which ranged-attack modifiers also apply (movement, PB +1, Fire Control, Accuracy). (7.0)
10. Ram: "move one hex onward" after a miss; rear rams with impact speed 0 or less; impact DM 0 or less. (7.1)
11. Kick: when "full weight" doubling applies. (7.3)
12. A throws: Accuracy and miss behaviour; B throws: metres-to-hex conversion. (7.4)
13. Weak Point: permanent or per hit. (8)
14. Indirect fire: what a scattered round does; whether sensor-lock or designator comms can replace the FO action;
    IF benefit when the firer has LOS; the books' example 2 is self-contradictory. (9)
15. Burst vs soft targets in A optional: attack roll or MoS. (10.2)
16. Saturation: defender modifiers; which attack modifiers apply to a hex target. (10.4)
17. AE: rounding of half DM; aiming at a hex; per-defender obscurement; direct-fire AE misses. (10.5)
18. Armor modifiers: order of operations. (11.3)
19. Systems damage: chaining of "roll twice"; does the Heavy +1 carry into those re-rolls. (12)
20. Reinforced Structure vs Reinforced Chassis naming; Power Transfer Failure -> defense 0? (13.2)
21. Crew/passenger die split; rounding of 10% / 75% casualties; status of a crewless vehicle. (13.3)
22. Combat Speed recalculation rounding; meaning of "half the remaining MP"; A's "max -5" for movement. (13.4)
23. Turret frozen "in the arc last fired". (13.5)
24. Arm damage double counting on held weapons. (14)
25. Picking up guns: proximity requirement. (15)
26. Undefined weapon tags: Disposable, Slow Burn, Entangle, Physical Attack Only. (10.1, 17)
27. Optional rules: simplified damage armor loss; knockdown tie and prone effects; falling-damage dice reading;
    artillery damage reading; orbital strike success test. (19, 20)

## 23. Where A and B differ (all DIFF items)
1. Point Blank: A touching / within 1 inch; B same hex. (2.2)
2. Defense arcs: A Front 0, Rear or PB -1; B Front 0, Rear Flank -1, Rear -2. (4.3)
3. Multiple Attacker Penalty: A only. (4.4)
4. Target speed and impact speed tables: 100-999 = +4 rows in B and A's quick-reference, missing from A pp.10-11.
5. Side ram impact speed: attacker speed (A p.11, B) vs (attacker + defender) / 2 rounded up (A p.27). (7.1)
6. Ram miss movement: A only. (7.1)
7. Physical attack skills: A all Piloting + Maneuver; B punch and melee weapons Gunnery + Fire Control, kick
   Piloting + Maneuver, throw Gunnery with range/movement modifiers, ram Piloting. (7.0)
8. Throwing: A grenades/blades/axes only, 1 hex, no range modifiers, no miss rule; B range from arm rating and
   item Size in metres, deviation on a miss, toward the thrower on a fumble, ties land. (7.4)
9. Called shots: B adds tiny-target precision shots (MoS 3+). (8)
10. ROF: A core uses an ROF-Attack counter (no ammo); B (and A's optional rules) count rounds and missiles. Burst:
    A core always adds ROF to DM, B adds to MoS against soft targets. Walking fire: A core normal DM, B burst bonus
    reduced by 1 per extra target. Saturation: same range in practice; A core costs one ROF Attack, B 30 rounds /
    8 rockets per ROF point (minimum 10 / 4). (10)
11. ROF against buildings: B adds to DM; A optional structures adds to MoS. (10.2)
12. FF arc: A plays it as F; B uses a 120-degree cone starting one hex ahead. (2.1)
13. Subtable B: B 6 = crew compartment failure (reachable on Light), 7 = structural failure; A 6 = roll twice,
    7 = sub-roll (and A's own two pages swap the sub-results). (12.4)
14. Auxiliary damage: A uses Subtable C; B has none, but adds turrets (frozen when disabled), life support and
    ejection seats. (12.2, 13.5)
15. Communications destroyed: A no FO and no Command Points; B no FO and no talking to other units. (13.5)
16. Movement damage wording: A one of the Top Speeds (with "max. -5"); B Top Speed first. (13.4)
17. Armor perks: B lets players take armor loss from the perk instead; A never. (11.5)
18. Weapon -5 / -6 rule: stated for weapons in A, only for arms in B. (13.1)
19. Arms are weapons for damage: A "are", B "may be". (14)
20. Crew casualty aftermath (dead / wounded roll): B only. (13.3)
21. Ejection and evacuation: B only. Picking up guns: A only. (15, 16)
22. Snap fire: A only for units not yet activated, costs one of their attacks; B any enemy unit. (5)
23. Command Points: A Tactical only, can reroll any die; B Tactical + Initiative, no reroll. (1.4)
24. Hull Down in the core rules: A only (B 7.7 has none). (5)
25. Crew skill table values (B p.127 vs A p.3). (1.1)
26. Off-table artillery / air / orbital strikes: A p.26; B defers to its Chapter 8. (20)
27. Attack example 2 numbers differ between the books (A: miss at -1; B: hit with MoS 2 using rear -2). (1.3)
28. (Movement scope, noted because it is crash damage) high-speed turn crash: A p.7 Light on 1-3 / Heavy on 4-6;
    A p.27 fumble table Light on 1-4 / Heavy on 5-6. A-internal.
