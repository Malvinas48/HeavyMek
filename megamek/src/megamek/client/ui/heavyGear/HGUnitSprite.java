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

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;

import megamek.client.ui.clientGUI.boardview.BoardView;
import megamek.client.ui.clientGUI.boardview.sprite.Sprite;
import megamek.client.ui.util.StringDrawer;
import megamek.client.ui.util.UIUtil;
import megamek.common.Player;
import megamek.common.board.Coords;
import megamek.common.heavyGear.HGUnit;
import megamek.common.heavyGear.MovementSystem;

/**
 * Draws one Heavy Gear unit on the map as a counter, like a cardboard token (DESIGN.md, "drawn tokens"):
 * <ul>
 *     <li>the owner's colour, with the callsign in big letters and the unit type below it;</li>
 *     <li>a pointer on the side the unit faces;</li>
 *     <li>the movement system (W walker, G ground...) bottom left and the speed (C Combat, T Top) bottom right;</li>
 *     <li>an armor bar along the bottom, green when whole, turning red as armor is lost;</li>
 *     <li>a faded counter once the unit has activated this round, and a grey one with a red cross when it is out of
 *     the fight.</li>
 * </ul>
 * Up to four units sharing a hex are drawn at half size in the hex's corners.
 *
 * <p>The drawing is made at the board's normal zoom on an 84 x 72 pixel hex and scaled by the board view.</p>
 */
public class HGUnitSprite extends Sprite {

    /** How a token is highlighted. */
    public enum Highlight {
        NONE, SELECTED, TARGET
    }

    private static final int HEX_W = 84;
    private static final int HEX_H = 72;

    private final HGUnit unit;
    private final Coords position;
    private final int facing;
    private final Player owner;
    private final int indexInHex;
    private final int countInHex;
    private final boolean ghost;
    private Highlight highlight = Highlight.NONE;
    private MovementSystem shownSystem;
    private boolean shownTopSpeed;

    /**
     * @param boardView  the board view drawing it
     * @param unit       the unit
     * @param owner      the unit's owner, for the colour
     * @param position   where to draw it (a planned position for a ghost)
     * @param facing     the facing to draw
     * @param indexInHex its place among the tokens in the same hex
     * @param countInHex how many tokens share the hex
     * @param ghost      true for the see-through preview of a planned move
     */
    public HGUnitSprite(BoardView boardView, HGUnit unit, Player owner, Coords position, int facing, int indexInHex,
          int countInHex, boolean ghost) {
        super(boardView);
        this.unit = unit;
        this.owner = owner;
        this.position = position;
        this.facing = facing;
        this.indexInHex = Math.min(indexInHex, 3);
        this.countInHex = countInHex;
        this.ghost = ghost;
        shownSystem = unit.movementSystem();
        shownTopSpeed = unit.speedState().isTop();
        getBounds();
    }

    /** Shows a planned movement system and speed instead of the unit's current ones (for set-up previews). */
    public HGUnitSprite showing(MovementSystem system, boolean topSpeed) {
        shownSystem = system;
        shownTopSpeed = topSpeed;
        image = null;
        return this;
    }

    public HGUnit unit() {
        return unit;
    }

    public Coords position() {
        return position;
    }

    public boolean isGhost() {
        return ghost;
    }

    public void setHighlight(Highlight highlight) {
        if (this.highlight != highlight) {
            this.highlight = highlight;
            image = null;
        }
    }

    @Override
    public Rectangle getBounds() {
        Point location = bv.getHexLocation(position);
        bounds = new Rectangle(location.x, location.y, bv.getHexSize().width, bv.getHexSize().height);
        return bounds;
    }

    @Override
    protected int getSpritePriority() {
        return ghost ? 20 : 10;
    }

    @Override
    public void prepare() {
        getBounds();
        GraphicsConfiguration config = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
              .getDefaultConfiguration();
        image = config.createCompatibleImage(bounds.width, bounds.height, Transparency.TRANSLUCENT);
        Graphics2D graph = (Graphics2D) image.getGraphics();
        UIUtil.setHighQualityRendering(graph);
        graph.scale(bv.getScale(), bv.getScale());
        if (countInHex > 1) {
            graph.translate((indexInHex > 1) ? HEX_W / 2.0 : 0, (indexInHex % 2 == 1) ? HEX_H / 2.0 : 0);
            graph.scale(0.5, 0.5);
        }
        if (ghost) {
            graph.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
        } else if (unit.hasActivated() && !unit.isOutOfFight()) {
            graph.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
        }
        drawToken(graph);
        graph.dispose();
    }

    private void drawToken(Graphics2D graph) {
        boolean out = unit.isOutOfFight();
        Color fill = out ? new Color(110, 110, 110) : owner.getColour().getColour();
        Color text = isDark(fill) ? Color.WHITE : Color.BLACK;

        drawFacingPointer(graph, out ? Color.DARK_GRAY : fill.darker());

        graph.setColor(fill);
        graph.fillRoundRect(14, 13, 56, 46, 10, 10);
        if (unit.hasActivated() && !out && !ghost) {
            graph.setColor(new Color(0, 0, 0, 70));
            graph.fillRoundRect(14, 13, 56, 46, 10, 10);
        }
        switch (highlight) {
            case SELECTED -> {
                graph.setColor(Color.WHITE);
                graph.setStroke(new BasicStroke(4));
            }
            case TARGET -> {
                graph.setColor(new Color(230, 30, 30));
                graph.setStroke(new BasicStroke(4));
            }
            default -> {
                graph.setColor(fill.darker().darker());
                graph.setStroke(ghost
                      ? new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[] { 4f, 3f }, 0f)
                      : new BasicStroke(2));
            }
        }
        graph.drawRoundRect(14, 13, 56, 46, 10, 10);

        new StringDrawer(trim(unit.shortName(), 9)).at(42, 25).absoluteCenter().color(text)
              .font(new Font(Font.SANS_SERIF, Font.BOLD, 12)).draw(graph);
        new StringDrawer(trim(unit.datacard().name(), 12)).at(42, 37).absoluteCenter().color(text)
              .font(new Font(Font.SANS_SERIF, Font.PLAIN, 9)).draw(graph);
        new StringDrawer(systemLetter(shownSystem)).at(23, 46).absoluteCenter().color(text)
              .font(new Font(Font.SANS_SERIF, Font.BOLD, 9)).draw(graph);
        new StringDrawer(shownTopSpeed ? "T" : "C").at(61, 46).absoluteCenter().color(text)
              .font(new Font(Font.SANS_SERIF, Font.BOLD, 9)).draw(graph);

        drawArmorBar(graph);

        if (out) {
            graph.setColor(new Color(220, 20, 20));
            graph.setStroke(new BasicStroke(4));
            graph.drawLine(18, 17, 66, 55);
            graph.drawLine(66, 17, 18, 55);
        }
    }

    private void drawFacingPointer(Graphics2D graph, Color color) {
        AffineTransform saved = graph.getTransform();
        graph.rotate(Math.toRadians(60 * facing), HEX_W / 2.0, HEX_H / 2.0);
        Polygon pointer = new Polygon(new int[] { 42 - 9, 42 + 9, 42 }, new int[] { 13, 13, 2 }, 3);
        graph.setColor(color);
        graph.fillPolygon(pointer);
        graph.setColor(Color.BLACK);
        graph.setStroke(new BasicStroke(1));
        graph.drawPolygon(pointer);
        graph.setTransform(saved);
    }

    private void drawArmorBar(Graphics2D graph) {
        int full = Math.max(1, unit.datacard().armor());
        double share = Math.max(0, Math.min(1, unit.armor() / (double) full));
        int width = (int) Math.round(40 * share);
        graph.setColor(new Color(30, 30, 30));
        graph.fillRect(22, 52, 40, 4);
        graph.setColor((share > 0.8) ? new Color(60, 200, 60) : ((share > 0.5) ? new Color(230, 200, 40)
              : new Color(230, 60, 40)));
        graph.fillRect(22, 52, width, 4);
    }

    private static String systemLetter(MovementSystem system) {
        return system.toString().substring(0, 1);
    }

    private static String trim(String text, int maxLength) {
        return (text.length() <= maxLength) ? text : text.substring(0, maxLength - 1) + ".";
    }

    private static boolean isDark(Color color) {
        return (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) < 140;
    }

    @Override
    public boolean isInside(Point point) {
        return getBounds().contains(point);
    }
}
