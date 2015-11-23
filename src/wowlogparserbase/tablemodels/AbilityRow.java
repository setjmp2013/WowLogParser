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
package wowlogparserbase.tablemodels;

import java.util.ArrayList;
import java.util.List;
import wowlogparserbase.EventCollection;
import wowlogparserbase.SpellInfo;
import wowlogparserbase.events.BasicEvent;
import wowlogparserbase.events.SkillInterface;

/**
 *
 * @author racy
 */
public class AbilityRow implements Comparable<AbilityRow> {
    private SpellInfo si = new SpellInfo(0, "", 0);
    private int num = 0;
    private String replacementString = "";
    private boolean hasSpellInfo = false;
    private List<BasicEvent> events = new ArrayList<BasicEvent>();

    public AbilityRow(SpellInfo si) {
        this.si = si;
        hasSpellInfo = true;
    }

    public AbilityRow(String replacementString) {
        this.replacementString = replacementString;
        hasSpellInfo = false;
    }

    public AbilityRow(String replacementString, int num) {
        this.replacementString = replacementString;
        this.num = num;
        hasSpellInfo = false;
    }

    public boolean hasSpellInfo() {
        return hasSpellInfo;
    }

    public SpellInfo getSpellInfo() {
        return si;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getReplacementString() {
        return replacementString;
    }

    public void addEvent(BasicEvent skI) {
        events.add(skI);
    }

    public void addEvents(List<BasicEvent> evs) {
        events.addAll(evs);
    }

    public List<BasicEvent> getEvents() {
        return events;
    }
    
    @Override
    public int compareTo(AbilityRow o) {
        return si.compareTo(o.si);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AbilityRow other = (AbilityRow) obj;
        if (si.equals(other.si)) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return si.hashCode();
    }


}
