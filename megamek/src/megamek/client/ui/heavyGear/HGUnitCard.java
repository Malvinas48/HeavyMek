/*
 * Copyright (C) 2026 The HeavyMek contributors.
 *
 * This file is part of HeavyMek, a fork of MegaMek.
 *
 * HeavyMek is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License (GPL), version 3 or (at your option) any later version, as published by the Free Software Foundation.
 *
 * HeavyMek is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * A copy of the GPL should have been included with this project; if not, see <https://www.gnu.org/licenses/>.
 *
 * Heavy Gear is a trademark of Dream Pod 9, Inc. HeavyMek is an unofficial fan project, not endorsed by Dream Pod 9.
 */

package megamek.client.ui.heavyGear;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import megamek.common.Player;
import megamek.common.heavyGear.CombatTables;
import megamek.common.heavyGear.HGGame;
import megamek.common.heavyGear.HGMovementMode;
import megamek.common.heavyGear.HGUnit;
import megamek.common.heavyGear.HGWeapon;

/**
 * Writes a unit's record sheet and current state as a small HTML page, for the side panel and the map tooltips.
 */
final class HGUnitCard {

    private HGUnitCard() {
    }

    /** @return the card for a unit; with {@code full} false, a shorter version for tooltips */
    static String html(HGUnit unit, HGGame game, boolean full) {
        Player owner = game.getPlayer(unit.getOwnerId());
        StringBuilder html = new StringBuilder();
        html.append("<b>").append(escape(unit.shortName())).append("</b>");
        if (!unit.callsign().isBlank()) {
            html.append(" &ndash; ").append(escape(unit.datacard().model().isBlank()
                  ? unit.datacard().name() : unit.datacard().model()));
        }
        if (owner != null) {
            html.append(" (").append(escape(owner.getName())).append(")");
        }
        html.append("<br>");
        if (unit.isOutOfFight()) {
            html.append("<font color='#c03030'><b>Out of the fight:</b> ").append(escape(unit.fateReason()))
                  .append("</font><br>");
        } else if (unit.hasActivated()) {
            html.append("<i>Has activated this round.</i><br>");
        }
        html.append("Armor <b>").append(unit.armorThresholds()).append("</b>");
        if (unit.armor() < unit.datacard().armor()) {
            html.append(" (was ").append(unit.datacard().armorThresholds()).append(")");
        }
        html.append("<br>");
        html.append("Crew: ").append(unit.crewLevel()).append(", Piloting ").append(unit.crewLevel().piloting())
              .append(", Gunnery ").append(unit.crewLevel().gunnery());
        if (unit.crew() < unit.datacard().crew()) {
            html.append(", ").append(unit.crew()).append(" of ").append(unit.datacard().crew()).append(" left");
        }
        html.append("<br>");
        html.append("Maneuver ").append(signed(unit.maneuver())).append(", Fire Control ")
              .append(unit.isFireControlDestroyed() ? "destroyed (-5)" : signed(unit.fireControl())).append("<br>");
        html.append("Speeds: ").append(unit.movementModes().stream().map(HGMovementMode::toString)
              .collect(Collectors.joining(", "))).append("<br>");
        if (unit.isDeployed()) {
            html.append("Now: ").append(unit.movementSystem()).append(" at ").append(unit.speedState())
                  .append(", facing ").append(List.of("N", "NE", "SE", "S", "SW", "NW").get(unit.facing()))
                  .append(", hex ").append(unit.position().getBoardNum()).append("<br>");
            int hexes = unit.hexesForDefense();
            html.append("Last move: ").append(hexes).append((hexes == 1) ? " hex" : " hexes").append(" (defense ")
                  .append(signed(CombatTables.targetSpeedModifier(hexes))).append(")<br>");
        }
        List<String> damage = damageNotes(unit);
        if (!damage.isEmpty()) {
            html.append("<font color='#c03030'>Damage: ").append(String.join(", ", damage)).append("</font><br>");
        }
        if (full) {
            html.append("<table cellspacing='0' cellpadding='1'>");
            html.append("<tr><th align='left'>Weapon</th><th>Arc</th><th>Acc</th><th>DM</th><th>Range</th>"
                  + "<th>ROF</th><th>Ammo</th></tr>");
            for (HGWeapon weapon : unit.weapons()) {
                int[] bands = weapon.rangeBands();
                html.append("<tr><td>").append(escape(weapon.code()));
                if (weapon.isDestroyed()) {
                    html.append(" <font color='#c03030'>destroyed</font>");
                } else if (weapon.damagePenalty() != 0) {
                    html.append(" <font color='#c03030'>").append(weapon.damagePenalty())
                          .append(weapon.isOutOfAction() ? " out" : "").append("</font>");
                }
                html.append("</td><td align='center'>").append(weapon.arc().name())
                      .append("</td><td align='center'>").append(signed(weapon.accuracy()))
                      .append("</td><td align='center'>x").append(weapon.damageMultiplier())
                      .append("</td><td align='center'>")
                      .append(weapon.isRanged() ? bands[0] + "/" + bands[1] + "/" + bands[2] + "/" + bands[3] : "close")
                      .append("</td><td align='center'>").append(weapon.rateOfFire())
                      .append("</td><td align='center'>").append((weapon.ammo() == null) ? "-" : weapon.ammo())
                      .append("</td></tr>");
            }
            html.append("</table>");
            if (!unit.datacard().perks().isEmpty()) {
                html.append("Perks: ").append(escape(String.join("; ", unit.datacard().perks()))).append("<br>");
            }
        }
        return html.toString();
    }

    private static List<String> damageNotes(HGUnit unit) {
        List<String> notes = new ArrayList<>();
        if (unit.hasPowerTransferFailure()) {
            notes.add("power transfer failure");
        }
        if (unit.isAmmoStoreRuptured()) {
            notes.add("ammunition store ruptured");
        }
        if (unit.stunnedActions() > 0) {
            notes.add("crew stunned (" + unit.stunnedActions() + " action" + ((unit.stunnedActions() > 1) ? "s" : "")
                  + " lost)");
        }
        if (unit.sensorsPenalty() != 0) {
            notes.add("sensors " + ((unit.sensorsPenalty() <= -5) ? "destroyed" : unit.sensorsPenalty()));
        }
        if (unit.communicationsPenalty() != 0) {
            notes.add("communications " + ((unit.communicationsPenalty() <= -5) ? "destroyed"
                  : unit.communicationsPenalty()));
        }
        if (unit.isImmobile() && !unit.isOutOfFight()) {
            notes.add("cannot move (defends at 0)");
        }
        return notes;
    }

    static String signed(int value) {
        return ((value >= 0) ? "+" : "") + value;
    }

    static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
