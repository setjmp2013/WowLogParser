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

package wowlogparserbase.events.damage;

import org.w3c.dom.Element;
import wowlogparserbase.StringDatabase;
import wowlogparserbase.Constants;
import wowlogparserbase.SettingsSingleton;
import wowlogparserbase.events.SkillInterface;

/**
 *
 * @author racy
 */
public class RangedDamageEvent extends DamageEvent implements SkillInterface {
    private int spellID = -1;
    private String spellName = null;

    public RangedDamageEvent() {
        
    }
    
    public int parse(String timeDate, String[] values) {
        int index = super.parse(timeDate, values);
        if (index < 0) {
            return index;
        }
        int version = SettingsSingleton.getInstance().getWowVersion();
        
        if (version >= 200 && version < 300) {
            //BC
            
            if (values.length < index + 11) {
                return -1;
            }
            setSkillID(Integer.parseInt(values[index + 0]));
            setSkillName(values[index + 1]);
            setSchool(intParse(values[index + 2]));
            setDamage(Integer.parseInt(values[index + 3]));

            //school, resisted, blocked, absorbed, critical, glancing, crushing

            //resisted
            if (!values[index + 5].contains("nil")) {
                setResisted(intParse(values[index + 5]));
            }

            //blocked
            if (!values[index + 6].contains("nil")) {
                setBlocked(intParse(values[index + 6]));
            }

            //absorbed
            if (!values[index + 7].contains("nil")) {
                setAbsorbed(intParse(values[index + 7]));
            }

            //critical
            if (!values[index + 8].contains("nil")) {
                setCrit(true);
            }

            //glancing
            if (!values[index + 9].contains("nil")) {
                setGlancing(true);
            }

            //crushing
            if (!values[index + 10].contains("nil")) {
                setCrushing(true);
            }
            return index + 11;
        } else {
            //WOTLK
            if (values.length < index + 12) {
                return -1;
            }
            setSkillID(Integer.parseInt(values[index + 0]));
            setSkillName(values[index + 1]);
            setSchool(intParse(values[index + 2]));
            setDamage(Integer.parseInt(values[index + 3]));
            setOverkill(intParse(values[index + 4]));

            //school, resisted, blocked, absorbed, critical, glancing, crushing

            //resisted
            if (!values[index + 6].contains("nil")) {
                setResisted(intParse(values[index + 6]));
            }

            //blocked
            if (!values[index + 7].contains("nil")) {
                setBlocked(intParse(values[index + 7]));
            }

            //absorbed
            if (!values[index + 8].contains("nil")) {
                setAbsorbed(intParse(values[index + 8]));
            }

            //critical
            if (!values[index + 9].contains("nil")) {
                setCrit(true);
            }

            //glancing
            if (!values[index + 10].contains("nil")) {
                setGlancing(true);
            }

            //crushing
            if (!values[index + 11].contains("nil")) {
                setCrushing(true);
            }
            return index + 12;
        }
    }

    public void setSkillID(int spellID) {
        this.spellID = spellID;
    }

    public void setSkillName(String spellName) {
        this.spellName = removeFnutt(spellName);
    }
    
    public int getSkillID() {
        return spellID;
    }

    public String getSkillName() {
        return spellName;
    }

    public int getSchool() {
        return super.getSchool();
    }

    public int getPowerType() {
        return Constants.POWER_TYPE_UNKNOWN;
    }
    
    public String toString() {
        String s = super.toString();
        s = s + "SpellID = " + spellID + newLine;
        s = s + "Spell Name = " + spellName + newLine;
        return s;
    }

    public Element addXmlAttributes(Element e) {
        super.addXmlAttributes(e);
        e.setAttribute(XMLTAG_SPELL_ID, ""+spellID);
        e.setAttribute(XMLTAG_SPELL_NAME, ""+spellName);
        return e;
    }

    @Override
    public void findOldStrings() {
        super.findOldStrings();
        StringDatabase db = StringDatabase.getInstance();
        spellName = db.processString(spellName);

    }
}
