/*
This file is part of Wow Log Parser, a program to parse World of Warcraft combat log files.
Copyright (C) Gustav Haapalahti

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package wowlogparserbase.info;

import wowlogparserbase.AmountAndCount;

/**
 *
 * @author racy
 */
public abstract class InfoBase {
    private boolean hasInfo = true;
    public void setHasInfo(boolean b) {
        hasInfo = b;
    }
    public boolean hasInfo() {
        return hasInfo;
    }
    protected static void fixMissesForPurePeriodic(AmountAndCount a) {
        if (a.numDirectHits == 0 && a.numPeriodicHits > 0) {
            setPeriodicSpecial(a);
        }
    }

    protected static void setPeriodicSpecial(AmountAndCount a) {
        a.miss = 0;
        a.dodge = 0;
        a.parry = 0;
        a.block = 0;
        a.totMiss = a.miss + a.dodge + a.parry + a.absorb + a.block + a.resist + a.reflect;
    }

    protected static void setNoNums(AmountAndCount a) {
        a.miss = 0;
        a.dodge = 0;
        a.parry = 0;
        a.block = 0;
        a.absorb = 0;
        a.crit = 0;
        a.crushing = 0;
        a.glancing = 0;
        a.totMiss = 0;
        a.hit = 0;
    }
}
