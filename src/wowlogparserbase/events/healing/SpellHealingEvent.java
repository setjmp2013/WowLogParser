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

package wowlogparserbase.events.healing;

import org.w3c.dom.Element;
import wowlogparserbase.StringDatabase;
import wowlogparserbase.Constants;
import wowlogparserbase.SettingsSingleton;
import wowlogparserbase.events.SkillInterface;

/**
 *
 * @author racy
 */
public class SpellHealingEvent extends HealingEvent implements SkillInterface {
    private int spellID = -1;
    private String spellName = null;
    private int school = 0;

    public SpellHealingEvent() {
        
    }
    
    public int parse(String timeDate, String[] values) {
        int index = super.parse(timeDate, values);
        int version = SettingsSingleton.getInstance().getWowVersion();
        
        int logType = Constants.LOG_TYPE_BC;
        if (values.length > 12) {
            logType = Constants.LOG_TYPE_WOTLK;
        }
        if (values.length > 13) {
            logType = Constants.LOG_TYPE_WOTLK3_2;
        }
        if (version >= 200 && version < 300) {
            setSkillID(Integer.parseInt(values[index + 0]));
            setSkillName(values[index + 1]);
            setSchool(intParse(values[index + 2]));
            setHealing(Integer.parseInt(values[index + 3]));

            //school, resisted, blocked, absorbed, critical, glancing, crushing

            //critical
            if (!values[index + 4].contains("nil")) {
                setCrit(true);
            }
            return index + 5;
        } else if (version >= 300 && version < 320) {
            setSkillID(Integer.parseInt(values[index + 0]));
            setSkillName(values[index + 1]);
            setSchool(intParse(values[index + 2]));
            setHealing(Integer.parseInt(values[index + 3]));
            setOverhealing(Integer.parseInt(values[index + 4]));

            //school, resisted, blocked, absorbed, critical, glancing, crushing

            //critical
            if (!values[index + 5].contains("nil")) {
                setCrit(true);
            }
            return index + 6;
        } else {
            setSkillID(Integer.parseInt(values[index + 0]));
            setSkillName(values[index + 1]);
            setSchool(intParse(values[index + 2]));
            setHealing(Integer.parseInt(values[index + 3]));
            setOverhealing(Integer.parseInt(values[index + 4]));
            setAbsorbed(Integer.parseInt(values[index + 5]));

            //school, resisted, blocked, absorbed, critical, glancing, crushing

            //critical
            if (!values[index + 6].contains("nil")) {
                setCrit(true);
            }
            return index + 7;
        }
    }

    public int getSkillID() {
        return spellID;
    }

    public String getSkillName() {
        return spellName;
    }
    
    public int getSchool() {
        return school;
    }

    public int getPowerType() {
        return Constants.POWER_TYPE_UNKNOWN;
    }

    public void setSchool(int school) {
        this.school = school;
    }

    public void setSkillID(int spellID) {
        this.spellID = spellID;
    }

    public void setSkillName(String spellName) {
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

    @Override
    public void findOldStrings() {
        super.findOldStrings();
        StringDatabase db = StringDatabase.getInstance();
        spellName = db.processString(spellName);

    }
}
