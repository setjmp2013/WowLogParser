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

package wowlogparserbase.eventfilter;

import wowlogparserbase.events.BasicEvent;
import wowlogparserbase.events.SkillInterface;

/**
 * Filter events based on Skill(Spell, abilities, etc).
 * Only abilities that implement SkillInterface are eligible.
 * @author racy
 */
public class FilterSkill extends Filter {
    public static final int ANY_ID = -1;
    public static final int ANY_SCHOOL = -1;
    public static final int ANY_POWER_TYPE = -11;
    
    int id;
    String name;
    int school;
    int powerType;

    /**
     * Constructor, selects which skill to match on. Only abilities that implement SkillInterface are eligible.
     * @param id The skill ID to match on, or FilterSkill.ANY_ID to match all ids.
     * @param name The name to match on, or null to match all names.
     * @param school The school to match on(from Constants), or FilterSkill.ANY_SCHOOL to match all schools. If two or more schools are chosen then all those schools must match.
     * @param powerType The power type to match on(from Constants), or FilterSkill.ANY_POWER_TYPE to match all power types.
     */
    public FilterSkill(int id, String name, int school, int powerType) {
        this.id = id;
        this.name = name;
        this.school = school;
        this.powerType = powerType;
    }

    @Override
    public boolean filter(BasicEvent e) {
        if (e instanceof SkillInterface) {
            SkillInterface si = (SkillInterface) e;
            boolean match = true;
            if ( id != ANY_ID ) {
                if ( id != si.getSkillID() ) {
                    match = false;
                }
            }
            if (name != null) {
                if ( !name.equalsIgnoreCase(si.getSkillName()) ) {
                    match = false;
                }
            }
            if ( school != ANY_SCHOOL ) {
                if ( (school & si.getSchool()) != school ) {
                    match = false;
                }
            }
            if (powerType != ANY_POWER_TYPE) {
                if ( powerType != si.getPowerType() ) {
                    match = false;
                }
            }
            return match;
        }
        return false;
    }
    
}
