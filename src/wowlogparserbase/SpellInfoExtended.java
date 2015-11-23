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

import wowlogparserbase.events.BasicEvent;
import wowlogparserbase.events.SkillInterface;

/**
 * A class for describing a spell
 * @author racy
 */
public class SpellInfoExtended extends SpellInfo implements Comparable {
    int unit;
    int type;
    BasicEvent ev;

    /**
     * Constructor
     * @param spellID
     * @param name
     * @param school
     */
    public SpellInfoExtended(SkillInterface si, BasicEvent be, int unit) {
        super(si.getSkillID(), si.getSkillName(), si.getSchool(), si.getPowerType());
        this.unit = unit;
        this.ev = be;
        type = AbstractEventCollection.getType(be);
    }

    /**
     * Constructor
     * @param s SpellInfo to copy
     */
    public SpellInfoExtended(SpellInfoExtended s) {
        super(s);
        this.unit = s.unit;
        this.ev = s.ev;
    }

    /**
     * Compare this spell info to another by means of spell id
     * @param o The other spell info
     * @return 0 if they match, the signum of the difference otherwise
     */
    @Override
    public int compareTo(Object o) {
        if (o instanceof SpellInfoExtended) {
            SpellInfoExtended si = (SpellInfoExtended) o;
            if (spellID != si.spellID) {
                return (int)Math.signum(spellID - si.spellID);
            } else if (unit != si.unit) {
                return (int)Math.signum(unit - si.unit);
            } else if (type != si.type) {
                return (int)Math.signum(type - si.type);
            } else {
                return 0;
            }
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
        final SpellInfoExtended other = (SpellInfoExtended) obj;
        if ((this.spellID != other.spellID) || (this.unit != other.unit) || (this.type != other.type) ) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = this.spellID + this.unit*1000000;
        return hash;
    }

}
