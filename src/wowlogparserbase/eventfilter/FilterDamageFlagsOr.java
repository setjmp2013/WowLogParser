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
 * Filter on damage flags.
 * The flags are defined in DamageEvent as CRIT, MISS, RESIST, etc.
 * @author racy
 */
public class FilterDamageFlagsOr extends Filter {

    private int flags;

    /**
     * Filter on damage flags.
     * @param flags The flags to let through. The flags are defined in DamageEvent as CRIT, MISS, RESIST, etc.
     */
    public FilterDamageFlagsOr(int flags) {
        this.flags = flags;
    }

    @Override
    public boolean filter(BasicEvent e) {
        if (e instanceof DamageEvent) {
            DamageEvent de = (DamageEvent)e;
            int damageFlags = de.getDamageFlags();
            if ((damageFlags & flags) != 0) {
                return true;
            }
        }
        return false;
    }
}
