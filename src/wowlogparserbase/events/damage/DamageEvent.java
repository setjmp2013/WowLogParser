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
import wowlogparserbase.Constants;
import wowlogparserbase.events.AmountInterface;
import wowlogparserbase.events.BasicEvent;

/**
 * Base class for all damage events, even missed damage events
 * @author racy
 */
public class DamageEvent extends BasicEvent implements AmountInterface {
    private int damage = 0;
    private int overkill = 0;
    private short damageFlags = 0; //Crit crush, miss etc.
    
    private int resisted = 0;
    private int blocked = 0;
    private int absorbed = 0;
    private int school = 0;
    private int healthDeficit = 0;
    
    public static final short CRIT = 1;
    public static final short MISS = 1<<1;
    public static final short RESIST = 1<<2;
    public static final short DODGE = 1<<3;
    public static final short PARRY = 1<<4;
    public static final short BLOCK = 1<<5;
    public static final short ABSORB = 1<<6;
    public static final short REFLECT = 1<<7;
    public static final short GLANCING = 1<<8;
    public static final short CRUSHING = 1<<9;
    
    public DamageEvent() {        
    }
    
    public int parse(String timeDate, String[] values) {
        return super.parse(timeDate, values);
    }
    
    /**
     * Parse a miss type string and save the results.
     * @param missType The miss type string
     */
    public void parseMissType(String missType) {
        if (missType.toLowerCase().equals("miss")) {
            setMiss(true);
            return;
        }
        if (missType.toLowerCase().equals("resist")) {
            setResist(true);
            return;
        }
        if (missType.toLowerCase().equals("dodge")) {
            setDodge(true);
            return;
        }
        if (missType.toLowerCase().equals("parry")) {
            setParry(true);
            return;
        }
        if (missType.toLowerCase().equals("block")) {
            setBlock(true);
            return;
        }
        if (missType.toLowerCase().equals("absorb")) {
            setAbsorb(true);
            return;
        }
        if (missType.toLowerCase().equals("reflect")) {
            setReflect(true);
            return;
        }
    }
    
    public String toString() {
        String s = super.toString();
        s = s + "Damage = " + getDamage() + newLine;
        s = s + "Overkill = " + getOverkill() + newLine;
        s = s + "Crit = " + isCrit() + newLine;
        s = s + "Miss = " + isMiss() + newLine;
        s = s + "Resist = " + isResist() + newLine;
        s = s + "Dodge = " + isDodge() + newLine;
        s = s + "Parry = " + isParry() + newLine;
        s = s + "Block = " + isBlock() + newLine;
        s = s + "Absorb = " + isAbsorb() + newLine;
        s = s + "Reflect = " + isReflect() + newLine;
        s = s + "Glancing = " + isGlancing() + newLine;
        s = s + "Crushing = " + isCrushing() + newLine;
        s = s + "Resisted = " + getResisted() + newLine;
        s = s + "Blocked = " + getBlocked() + newLine;
        s = s + "Absorbed = " + getAbsorbed() + newLine;
        s = s + "School = " + getSchoolStringFromFlags(getSchool()) + newLine;
        return s;
        
    }

    public Element addXmlAttributes(Element e) {
        super.addXmlAttributes(e);
        e.setAttribute(XMLTAG_AMOUNT, ""+getDamage());
        e.setAttribute(XMLTAG_AMOUNT_ABSORBED, ""+getAbsorbed());
        e.setAttribute(XMLTAG_AMOUNT_BLOCKED, ""+getBlocked());
        e.setAttribute(XMLTAG_AMOUNT_RESISTED, ""+getResisted());
        e.setAttribute(XMLTAG_AMOUNT_OVERKILL, ""+getOverkill());
        e.setAttribute(XMLTAG_SPELL_SCHOOL, ""+getSchoolStringFromFlags(getSchool()));
        e.setAttribute(XMLTAG_DAMAGE_FLAGS, ""+damageFlags);
        return e;
    }

    public boolean isCrit() {
        return getBooleanBit(damageFlags, CRIT);
    }

    public void setCrit(boolean val) {
        damageFlags = (short)setBooleanBit(damageFlags, CRIT, val);
    }

    public boolean isMiss() {
        return getBooleanBit(damageFlags, MISS);
    }

    public void setMiss(boolean val) {
        damageFlags = (short)setBooleanBit(damageFlags, MISS, val);
    }

    public boolean isResist() {
        return getBooleanBit(damageFlags, RESIST);
    }

    public void setResist(boolean val) {
        damageFlags = (short)setBooleanBit(damageFlags, RESIST, val);
    }

    public boolean isDodge() {
        return getBooleanBit(damageFlags, DODGE);
    }

    public void setDodge(boolean val) {
        damageFlags = (short)setBooleanBit(damageFlags, DODGE, val);
    }

    public boolean isParry() {
        return getBooleanBit(damageFlags, PARRY);
    }

    public void setParry(boolean val) {
        damageFlags = (short)setBooleanBit(damageFlags, PARRY, val);
    }

    public boolean isBlock() {
        return getBooleanBit(damageFlags, BLOCK);
    }

    public void setBlock(boolean val) {
        damageFlags = (short)setBooleanBit(damageFlags, BLOCK, val);
    }

    public boolean isAbsorb() {
        return getBooleanBit(damageFlags, ABSORB);
    }

    public void setAbsorb(boolean val) {
        damageFlags = (short)setBooleanBit(damageFlags, ABSORB, val);
    }

    public boolean isReflect() {
        return getBooleanBit(damageFlags, REFLECT);
    }

    public void setReflect(boolean val) {
        damageFlags = (short)setBooleanBit(damageFlags, REFLECT, val);
    }

    public boolean isGlancing() {
        return getBooleanBit(damageFlags, GLANCING);
    }

    public void setGlancing(boolean val) {
        damageFlags = (short)setBooleanBit(damageFlags, GLANCING, val);
    }

    public boolean isCrushing() {
        return getBooleanBit(damageFlags, CRUSHING);
    }

    public void setCrushing(boolean val) {
        damageFlags = (short)setBooleanBit(damageFlags, CRUSHING, val);
    }
    
    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public int getOverkill() {
        return overkill;
    }

    public void setOverkill(int overkill) {
        this.overkill = overkill;
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

    public int getSchool() {
        return school;
    }

    public void setSchool(int school) {
        this.school = school;
    }

    public int getHealthDeficit() {
        return healthDeficit;
    }

    public void setHealthDeficit(int healthDeficit) {
        this.healthDeficit = healthDeficit;
    }

    @Override
    public int getAmount() {
        return damage;
    }

    public short getDamageFlags() {
        return damageFlags;
    }

    public void setDamageFlags(short damageFlags) {
        this.damageFlags = damageFlags;
    }

}
