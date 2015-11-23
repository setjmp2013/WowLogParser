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

/**
 * A class for describing a spell
 * @author racy
 */
public class SpellInfo implements Comparable {
    public String name;
    public int spellID;
    public int school;
    public int powerType = Constants.POWER_TYPE_UNKNOWN;

    /**
     * Constructor
     * @param spellID
     * @param name
     * @param school
     * @param powerType
     */
    public SpellInfo(int spellID, String name, int school, int powerType) {
        this.name = name;
        this.spellID = spellID;
        this.school = school;
        this.powerType = powerType;
    }

    /**
     * Constructor
     * @param spellID
     * @param name
     * @param school
     */
    public SpellInfo(int spellID, String name, int school) {
        this.name = name;
        this.spellID = spellID;
        this.school = school;
    }

    /**
     * Constructor
     * @param s SpellInfo to copy
     */
    public SpellInfo(SpellInfo s) {
        this.name = s.name;
        this.spellID = s.spellID;
        this.school = s.school;
        this.powerType = s.powerType;
    }

    /**
     * Compare this spell info to another by means of spell id
     * @param o The other spell info
     * @return 0 if they match, the signum of the difference otherwise
     */
    public int compareTo(Object o) {
        if (o instanceof SpellInfo) {
            SpellInfo si = (SpellInfo) o;
            return (int)Math.signum(spellID - si.spellID);
        } else {
            return -1;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SpellInfo other = (SpellInfo) obj;
        if (this.spellID != other.spellID) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + this.spellID;
        return hash;
    }

    @Override
    public String toString() {
        return name + " (" + spellID + ")";
    }
    
}
