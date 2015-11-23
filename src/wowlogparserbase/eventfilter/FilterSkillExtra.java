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
import wowlogparserbase.events.SkillInterfaceExtra;

/**
 * Filter events based on Extra Skills(Spells, abilities, etc that are dispelled, interrupted etc).
 * Only abilities that implement SkillInterfaceExtra are eligible.
 * @author racy
 */
public class FilterSkillExtra extends Filter {
    public static final int ANY_ID = -1;
    public static final int ANY_SCHOOL = -1;
    public static final int ANY_POWER_TYPE = -11;
    
    int extraSpellId;
    String extraSpellName;
    int extraSpellSchool;

    /**
     * Constructor, selects which skill to match on. Only abilities that implement SkillInterfaceExtra are eligible.
     * @param extraSpellId The skill ID to match on, or FilterSkill.ANY_ID to match all ids.
     * @param extraSpellName The name to match on, or null to match all names.
     * @param extraSpellSchool The school to match on(from Constants), or FilterSkill.ANY_SCHOOL to match all schools. If two or more schools are chosen then all those schools must match.
     */
    public FilterSkillExtra(int extraSpellId, String extraSpellName, int extraSpellSchool) {
        this.extraSpellId = extraSpellId;
        this.extraSpellName = extraSpellName;
        this.extraSpellSchool = extraSpellSchool;
    }

    @Override
    public boolean filter(BasicEvent e) {
        if (e instanceof SkillInterfaceExtra) {
            SkillInterfaceExtra si = (SkillInterfaceExtra) e;
            boolean match = true;
            if ( extraSpellId != ANY_ID ) {
                if ( extraSpellId != si.getExtraSpellID() ) {
                    match = false;
                }
            }
            if (extraSpellName != null) {
                if ( !extraSpellName.equalsIgnoreCase(si.getExtraSpellName()) ) {
                    match = false;
                }
            }
            if ( extraSpellSchool != ANY_SCHOOL ) {
                if ( (extraSpellSchool & si.getExtraSchool()) != extraSpellSchool ) {
                    match = false;
                }
            }
            return match;
        }
        return false;
    }
    
}
