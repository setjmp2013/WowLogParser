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
 * Match vs a time period.
 * @author racy
 */
public class FilterTime extends Filter {
    double startTime, endTime;

    /**
     * Constructor
     * @param startTime The start time
     * @param endTime The end time
     */
    public FilterTime(double startTime, double endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    @Override
    public boolean filter(BasicEvent e) {
        if (e.time <= endTime && e.time >= startTime) {
            return true;
        } else {
            return false;
        }
    }

}
