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

package wowlogparserbase.events.aura;

import java.util.HashMap;
import org.w3c.dom.Element;
import wowlogparserbase.StringDatabase;
import wowlogparserbase.events.AuraTypeInterface;

/**
 *
 * @author racy
 */
public class SpellAuraBrokenEvent extends SpellAuraBrokenBaseEvent implements AuraTypeInterface {
    private String auraType = null;

    public SpellAuraBrokenEvent() {
    }


    @Override
    public int parse(String timeDate, String[] values) {
        int index = super.parse(timeDate, values);
        if (index < 0) {
            return index;
        }
        if (values.length < index + 1) {
            return -1;
        }

        //"SPELL_AURA_BROKEN"
        //spellId, spellName, spellSchool, auraType
        //(source arguments are nil)        
        setAuraType(values[index + 0]);
        return index + 1;

    }

    public void setAuraType(String auraType) {
        this.auraType = auraType;
    }
    
    public boolean isDebuff() {
        return isDebuffString(auraType);
    }

    public boolean isBuff() {
        return isBuffString(auraType);
    }

    public String toString() {
        String s = super.toString();
        s = s + "Aura Type = " + auraType + newLine;
        return s;
    }

    public Element addXmlAttributes(Element e) {
        super.addXmlAttributes(e);
        e.setAttribute(XMLTAG_AURA_TYPE, ""+auraType);
        return e;
    }

    @Override
    public void findOldStrings() {
        super.findOldStrings();
        StringDatabase db = StringDatabase.getInstance();
        auraType = db.processString(auraType);

    }
    public String getBreakingSkill() {
        return "Unknown";
    }
}
