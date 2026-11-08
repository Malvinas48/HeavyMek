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

import java.io.File;
import java.util.Vector;

import megamek.common.options.AbstractOptionsInfo;
import megamek.common.options.BasicGameOptions;
import megamek.common.options.IOption;

/**
 * The game options for a Heavy Gear game. There are no Heavy Gear options yet, only MegaMek's basic ones (autosave
 * and the like). Optional rules such as Hull Down will be added here later.
 */
public class HGRuleOptions extends BasicGameOptions {

    @Override
    public synchronized void initialize() {
        super.initialize();
    }

    @Override
    public Vector<IOption> loadOptions() {
        return null;
    }

    @Override
    public Vector<IOption> loadOptions(File file, boolean print) {
        return null;
    }

    @Override
    protected AbstractOptionsInfo getOptionsInfoImp() {
        return HGRuleOptionsInfo.INSTANCE;
    }

    private static class HGRuleOptionsInfo extends AbstractOptionsInfo {
        private static final AbstractOptionsInfo INSTANCE = new HGRuleOptionsInfo();

        HGRuleOptionsInfo() {
            super("HGRuleOptionsInfo");
        }
    }
}
