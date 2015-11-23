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
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author racy
 */
public class BossParserSourceSimple implements BossParserSource {

    List<BossInfo> bossInfos;

    public BossParserSourceSimple() {
        bossInfos = new ArrayList<BossInfo>();
    }

    public void add(BossInfo bi) {
        bossInfos.add(bi);
    }

    @Override
    public List<BossInfo> getBossInfo() {
        return Collections.unmodifiableList(bossInfos);
    }

    @Override
    public List<Integer> getMobIDs() {
        List<BossInfo> bossInfoList = getBossInfo();
        ArrayList<Integer> bossIDs = new ArrayList<Integer>();
        for(BossInfo bossInfo : bossInfoList) {
            List<Integer> mobList = new ArrayList<Integer>();
            for (NpcInfo mob : bossInfo.getMobs()) {
                mobList.add(mob.getId());
            }
            bossIDs.addAll(mobList);
        }
        return bossIDs;
    }

    @Override
    public List<String> getMobNames() {
        List<BossInfo> bossInfoList = getBossInfo();
        ArrayList<String> bossStrings = new ArrayList<String>();
        for(BossInfo bossInfo : bossInfoList) {
            List<String> mobList = new ArrayList<String>();
            for (NpcInfo mob : bossInfo.getMobs()) {
                mobList.add(mob.getName());
            }
            bossStrings.addAll(mobList);
        }
        return bossStrings;
    }

}
