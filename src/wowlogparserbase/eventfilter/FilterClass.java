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
 * Find events of a specific class (objects that can be casted to the class type)
 * @author racy
 */
public class FilterClass extends Filter {
    Class cl;

    /**
     * Constructor
     * @param cl The event class that we want. 
     *           Events of this class as well as any that extends from this class will get through the filter.
     */
    public FilterClass(Class cl) {
        this.cl = cl;
    }

    @Override
    public boolean filter(BasicEvent e) {
        return cl.isAssignableFrom(e.getClass());
    }
        
}
