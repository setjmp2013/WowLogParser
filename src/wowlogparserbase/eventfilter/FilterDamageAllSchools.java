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
import wowlogparserbase.events.damage.DamageEvent;

/**
 * Filter events based on Damage school.
 * Only abilities that inherit from DamageEvent are eligible.
 * @author racy
 */
public class FilterDamageAllSchools extends Filter {
    public static final int ANY_SCHOOL = -1;

    int school;

    /**
     * Constructor, selects which damage school to match on. Only events that inherit from DamageEvent are eligible.
     * @param school The school to match on(from Constants), or FilterDamageSchool.ANY_SCHOOL to match all schools. If two or more schools are chosen then all those schools must match.
     */
    public FilterDamageAllSchools(int school) {
        this.school = school;
    }

    @Override
    public boolean filter(BasicEvent e) {
        if (e instanceof DamageEvent) {
            DamageEvent de = (DamageEvent) e;
            boolean match = true;
            if ( school != ANY_SCHOOL ) {
                if ( (school & de.getSchool()) != school ) {
                    match = false;
                }
            }
            return match;
        }
        return false;
    }

}
