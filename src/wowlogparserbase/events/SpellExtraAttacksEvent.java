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

package wowlogparserbase.events;

import org.w3c.dom.Element;
import wowlogparserbase.events.SpellInfoEvent;

/**
 *
 * @author racy
 */
public class SpellExtraAttacksEvent extends SpellInfoEvent {

    private int extraAttacks = 0;
    
    public SpellExtraAttacksEvent() {
    }

    public int parse(String timeDate, String[] values) {
        int index = super.parse(timeDate, values);
        if (index < 0) {
            return index;
        }
        if (values.length < index + 4) {
            return -1;
        }

        //"SPELL_EXTRA_ATTACKS"
        //spellId, spellName, spellSchool, extraAttacks
        setSkillID(Integer.parseInt(values[index + 0]));
        setSkillName(values[index + 1]);
        setSchool(intParse(values[index + 2]));
        setExtraAttacks(intParse(values[index + 3]));
        
        return index + 4;
    }

    public int getExtraAttacks() {
        return extraAttacks;
    }

    public void setExtraAttacks(int extraAttacks) {
        this.extraAttacks = extraAttacks;
    }

    public String toString() {
        String s = super.toString();
        s = s + "Extra Attacks = " + extraAttacks + newLine;
        return s;
    }

    public Element addXmlAttributes(Element e) {
        super.addXmlAttributes(e);
        e.setAttribute(XMLTAG_EXTRA_ATTACKS, ""+extraAttacks);
        return e;
    }
}
