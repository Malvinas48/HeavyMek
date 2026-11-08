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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;

import jakarta.annotation.Nullable;
import megamek.client.ui.clientGUI.boardview.BoardView;
import megamek.client.ui.clientGUI.boardview.sprite.HexSprite;
import megamek.client.ui.util.StringDrawer;
import megamek.client.ui.util.UIUtil;
import megamek.common.board.Coords;

/**
 * Tints one hex with a see-through colour and an optional short label. Used to show the deployment zone, the hexes
 * of a planned move (labelled with the MP spent so far) and the hexes a shot can reach.
 */
public class HGHexShadeSprite extends HexSprite {

    private final Color color;
    private final @Nullable String label;

    public HGHexShadeSprite(BoardView boardView, Coords coords, Color color, @Nullable String label) {
        super(boardView, coords);
        this.color = color;
        this.label = label;
    }

    @Override
    public void prepare() {
        updateBounds();
        image = createNewHexImage();
        Graphics2D graph = (Graphics2D) image.getGraphics();
        UIUtil.setHighQualityRendering(graph);
        graph.scale(bv.getScale(), bv.getScale());
        Polygon hex = BoardView.getHexPoly();
        graph.setColor(color);
        graph.fillPolygon(hex);
        graph.setColor(color.darker());
        graph.setStroke(new BasicStroke(1.5f));
        graph.drawPolygon(hex);
        if (label != null) {
            new StringDrawer(label).at(42, 64).absoluteCenter().color(Color.BLACK)
                  .outline(Color.WHITE, 2f).font(new Font(Font.SANS_SERIF, Font.BOLD, 11)).draw(graph);
        }
        graph.dispose();
    }

    @Override
    public boolean isBehindTerrain() {
        return false;
    }

    @Override
    protected int getSpritePriority() {
        return 1;
    }
}
