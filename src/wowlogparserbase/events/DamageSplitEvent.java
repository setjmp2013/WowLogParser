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

import wowlogparserbase.Constants;
import java.util.HashMap;
import org.w3c.dom.Element;
import wowlogparserbase.SettingsSingleton;
import wowlogparserbase.StringDatabase;
import wowlogparserbase.events.damage.DamageEvent;

/**
 *
 * @author racy
 */
public class DamageSplitEvent extends BasicEvent implements SkillInterface, AmountInterface {
    private int spellID = -1;
    private String spellName = null;
    private int school;
    private int amount = 0;
    private int resisted = 0;
    private int blocked = 0;
    private int absorbed = 0;
    private boolean crit;
    private boolean glancing;
    private boolean crushing;

    public DamageSplitEvent() {
        
    }

    public int parse(String timeDate, String[] values) {
        int index = super.parse(timeDate, values);

        if (index < 0) {
            return index;
        }
        if (values.length < index + 10) {
            return -1;
        }
        setID(Integer.parseInt(values[index + 0]));
        setName(values[index + 1]);
        setSchool(intParse(values[index + 2]));
        setAmount(Integer.parseInt(values[index + 3]));

        //school, resisted, blocked, absorbed, critical, glancing, crushing

        //resisted
        if (!values[index + 4].contains("nil")) {
            setResisted(intParse(values[index + 4]));
        }

        //blocked
        if (!values[index + 5].contains("nil")) {
            setBlocked(intParse(values[index + 5]));
        }

        //absorbed
        if (!values[index + 6].contains("nil")) {
            setAbsorbed(intParse(values[index + 6]));
        }

        //critical
        if (!values[index + 7].contains("nil")) {
            setCrit(true);
        }

        //glancing
        if (!values[index + 8].contains("nil")) {
            setGlancing(true);
        }

        //crushing
        if (!values[index + 9].contains("nil")) {
            setCrushing(true);
        }
        return index + 10;
    }

    public boolean isCrit() {
        return crit;
    }

    public void setCrit(boolean crit) {
        this.crit = crit;
    }

    public boolean isCrushing() {
        return crushing;
    }

    public void setCrushing(boolean crushing) {
        this.crushing = crushing;
    }

    public boolean isGlancing() {
        return glancing;
    }

    public void setGlancing(boolean glancing) {
        this.glancing = glancing;
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
    
    public String toString() {
        String s = super.toString();
        s = s + "Amount = " + getAmount() + newLine;
        s = s + "Crit = " + isCrit() + newLine;
        s = s + "Glancing = " + isGlancing() + newLine;
        s = s + "Crushing = " + isCrushing() + newLine;
        s = s + "Resisted = " + getResisted() + newLine;
        s = s + "Blocked = " + getBlocked() + newLine;
        s = s + "Absorbed = " + getAbsorbed() + newLine;
        s = s + "SpellID = " + getSkillID() + newLine;
        s = s + "Spell Name = " + getSkillName() + newLine;
        s = s + "School = " + getSchoolStringFromFlags(getSchool()) + newLine;
        return s;
    }

    public Element addXmlAttributes(Element e) {
        super.addXmlAttributes(e);
        int damFlags = makeDamageFlags();
        e.setAttribute(XMLTAG_AMOUNT, ""+getAmount());
        e.setAttribute(XMLTAG_DAMAGE_FLAGS, ""+damFlags);
        e.setAttribute(XMLTAG_SPELL_ID, ""+getSkillID());
        e.setAttribute(XMLTAG_SPELL_NAME,getSkillName());
        e.setAttribute(XMLTAG_SPELL_SCHOOL, getSchoolStringFromFlags(getSchool()));
        return e;
    }

    private int makeDamageFlags() {
        int damFlags = 0;
        damFlags = setBooleanBit(damFlags, CRIT, crit);
        damFlags = setBooleanBit(damFlags, CRUSHING, crushing);
        damFlags = setBooleanBit(damFlags, GLANCING, glancing);
        return damFlags;
    }
    
    @Override
    public void findOldStrings() {
        super.findOldStrings();
        StringDatabase db = StringDatabase.getInstance();
        spellName = db.processString(spellName);

    }

    private static final short CRIT = 1;
    private static final short MISS = 1<<1;
    private static final short RESIST = 1<<2;
    private static final short DODGE = 1<<3;
    private static final short PARRY = 1<<4;
    private static final short BLOCK = 1<<5;
    private static final short ABSORB = 1<<6;
    private static final short REFLECT = 1<<7;
    private static final short GLANCING = 1<<8;
    private static final short CRUSHING = 1<<9;

    public void setID(int spellID) {
        this.spellID = spellID;
    }

    public void setName(String spellName) {
        this.spellName = removeFnutt(spellName);
    }

    public void setSchool(int school) {
        this.school = school;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getResisted() {
        return resisted;
    }

    public void setResisted(int resisted) {
        this.resisted = resisted;
    }

    public int getBlocked() {
        return blocked;
    }

    public void setBlocked(int blocked) {
        this.blocked = blocked;
    }

    public int getAbsorbed() {
        return absorbed;
    }

    public void setAbsorbed(int absorbed) {
        this.absorbed = absorbed;
    }
}
