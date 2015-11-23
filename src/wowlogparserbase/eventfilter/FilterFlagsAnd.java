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

/**
 * Filter used to filter on flags (in Constants), both the flags must match (AND)
 * @author racy
 */
public class FilterFlagsAnd extends Filter {
    long sFlags, dFlags;

    /**
     * The flags to match on. A flag of 0 will make it always match.
     * All flags specified must be present in the event or it will not match.
     * Use BasicEvent.FLAGS_ALL to make a flag never match.
     * @param sFlags
     * @param dFlags
     */
    public FilterFlagsAnd(long sFlags, long dFlags) {
        this.sFlags = sFlags;
        this.dFlags = dFlags;
    }

    @Override
    public boolean filter(BasicEvent e) {
        boolean match = true;
        if (sFlags != 0) {
            if ( (sFlags & e.getSourceFlags()) != sFlags) {
                match = false;
            }
        }
        if (dFlags != 0) {
            if ( (dFlags & e.getDestinationFlags()) != dFlags) {
                match = false;
            }            
        }
        return match;
    }
    
}
