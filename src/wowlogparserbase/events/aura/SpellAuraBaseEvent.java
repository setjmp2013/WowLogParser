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

import org.w3c.dom.Element;
import wowlogparserbase.StringDatabase;
import wowlogparserbase.events.SkillInterface;
import wowlogparserbase.events.BasicEvent;
import wowlogparserbase.Constants;
import wowlogparserbase.xmlbindings.fight.SpellAuraBaseEventXmlType;

/**
 *
 * @author racy
 */
public class SpellAuraBaseEvent extends BasicEvent implements SkillInterface {
    private int spellID = -1;
    private String spellName = null;
    private int school = 0;

    public SpellAuraBaseEvent() {
    }
    
    /**
     * Parse a log line
     * @param timeDate
     * @param values
     * @return
     */
    @Override
    public int parse(String timeDate, String[] values) {
        int index = super.parse(timeDate, values);
        if (index < 0) {
            return index;
        }
        if (values.length < index + 3) {
            return -1;
        }
        
        setSkillID(intParse(values[index + 0]));
        setskillName(values[index + 1]);
        setSchool(intParse(values[index + 2]));

        return index + 3;
    }

    public int getSkillID() {
        return spellID;
    }

    public String getSkillName() {
        return spellName;
    }

    public int getPowerType() {
        return Constants.POWER_TYPE_UNKNOWN;
    }

    public int getSchool() {
        return school;
    }

    public void setSchool(int school) {
        this.school = school;
    }

    public void setSkillID(int spellID) {
        this.spellID = spellID;
    }

    public void setskillName(String spellName) {
        this.spellName = removeFnutt(spellName);
    }
    
    public String toString() {
        String s = super.toString();
        s = s + "SpellID = " + spellID + newLine;
        s = s + "Spell Name = " + spellName + newLine;
        s = s + "School = " + getSchoolStringFromFlags(school) + newLine;
        return s;
    }

    public Element addXmlAttributes(Element e) {
        super.addXmlAttributes(e);
        e.setAttribute(XMLTAG_SPELL_ID, ""+spellID);
        e.setAttribute(XMLTAG_SPELL_NAME, spellName);
        e.setAttribute(XMLTAG_SPELL_SCHOOL, getSchoolStringFromFlags(school));
        return e;
    }

    public void addXmlAttributesBean(Object o) {
        super.addXmlAttributesJaxb(o);
        if (o instanceof SpellAuraBaseEventXmlType) {
            SpellAuraBaseEventXmlType o2 = (SpellAuraBaseEventXmlType) o;
            o2.setSpid(getSkillID());
            o2.setSpna(getSkillName());
            o2.setSpsc(getSchoolStringFromFlags(getSchool()));
        }
    }
    
    @Override
    public void findOldStrings() {
        super.findOldStrings();
        StringDatabase db = StringDatabase.getInstance();
        spellName = db.processString(spellName);

    }
}
