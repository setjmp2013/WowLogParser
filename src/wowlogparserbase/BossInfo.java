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

package wowlogparserbase;

import java.util.ArrayList;
import java.util.List;

/**
 * Class made for holding information about a boss encounter and the mobs that are present in it.
 * @author racy
 */
public class BossInfo {
    double maxIdleTime = 15;
    String encounterName = "";
    List<NpcInfo> mobs = new ArrayList<NpcInfo>();
    
    public BossInfo(String encounterName) {
        this.encounterName = encounterName;
    }

    public void addMob(NpcInfo mob) {
        mobs.add(mob);
    }
    
    public String getEncounterName() {
        return encounterName;
    }

    public void setEncounterName(String encounterName) {
        this.encounterName = encounterName;
    }

    public List<NpcInfo> getMobs() {
        return new ArrayList<NpcInfo>(mobs);
    }

    public void setMobs(List<NpcInfo> mobs) {
        this.mobs = new ArrayList<NpcInfo>(mobs);
    }

    public double getMaxIdleTime() {
        return maxIdleTime;
    }

    public void setMaxIdleTime(double maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
    }    
}
