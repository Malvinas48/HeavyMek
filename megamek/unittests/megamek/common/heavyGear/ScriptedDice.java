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

package megamek.common.heavyGear;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * A dice cup for tests: it hands out the numbers it was given, in order, so every roll in a test is known in
 * advance. Running out of numbers fails the test, which catches rules code that rolls more dice than expected.
 */
public class ScriptedDice implements Dice {

    private final Deque<Integer> faces = new ArrayDeque<>();

    public ScriptedDice(int... faces) {
        for (int face : faces) {
            this.faces.add(face);
        }
    }

    /** Adds more numbers at the end. */
    public ScriptedDice then(int... moreFaces) {
        for (int face : moreFaces) {
            faces.add(face);
        }
        return this;
    }

    @Override
    public int d6() {
        if (faces.isEmpty()) {
            throw new IllegalStateException("The test ran out of scripted dice");
        }
        return faces.removeFirst();
    }

    /** @return how many scripted numbers were not used */
    public int unused() {
        return faces.size();
    }
}
