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
import wowlogparserbase.events.SkillInterfaceExtra;
import wowlogparserbase.events.aura.SpellAuraBaseEvent;

/**
 *
 * @author racy
 */
public class SpellDispellFailedEvent extends SpellAuraBaseEvent implements SkillInterfaceExtra {
    private int extraSpellID = -1;
    private String extraSpellName = null;
    private int extraSpellSchool = 0;

    public SpellDispellFailedEvent() {
    }


    @Override
    public int parse(String timeDate, String[] values) {
        int index = super.parse(timeDate, values);
        if (index < 0) {
            return index;
        }
        if (values.length < index + 3) {
            return -1;
        }

        //"SPELL_DISPEL_FAILED"
        //spellId, spellName, spellSchool, extraSpellId, extraSpellName, extraSpellSchool
        setExtraSpellID(intParse(values[index + 0]));
        setExtraSpellName(values[index + 1]);
        setExtraSpellSchool(intParse(values[index + 2]));

        return index + 3;

    }

    public int getExtraSchool() {
        return extraSpellSchool;
    }

    public int getExtraSpellID() {
        return extraSpellID;
    }

    public String getExtraSpellName() {
        return extraSpellName;
    }

    public void setExtraSpellID(int extraSpellID) {
        this.extraSpellID = extraSpellID;
    }

    public void setExtraSpellName(String extraSpellName) {
        this.extraSpellName = removeFnutt(extraSpellName);
    }

    public void setExtraSpellSchool(int extraSpellSchool) {
        this.extraSpellSchool = extraSpellSchool;
    }

    public String toString() {
        String s = super.toString();
        s = s + "Extra SpellID = " + extraSpellID + newLine;
        s = s + "Extra Spell Name = " + extraSpellName + newLine;
        s = s + "Extra School = " + getSchoolStringFromFlags(extraSpellSchool) + newLine;
        return s;
    }

    public Element addXmlAttributes(Element e) {
        super.addXmlAttributes(e);
        e.setAttribute(XMLTAG_SPELL_EXTRA_ID, ""+extraSpellID);
        e.setAttribute(XMLTAG_SPELL_EXTRA_NAME, extraSpellName);
        e.setAttribute(XMLTAG_SPELL_EXTRA_SCHOOL, getSchoolStringFromFlags(extraSpellSchool));
        return e;
    }

    @Override
    public void findOldStrings() {
        super.findOldStrings();
        StringDatabase db = StringDatabase.getInstance();
        extraSpellName = db.processString(extraSpellName);
    }
}
