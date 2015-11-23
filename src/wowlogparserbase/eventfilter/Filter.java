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

import java.io.Serializable;
import wowlogparserbase.events.BasicEvent;

/**
 * Base class for event filters
 * @author racy
 */
public abstract class Filter implements Serializable {
    public abstract boolean filter(BasicEvent e);

    /**
     * Convenience function for subclasses
     * Check with AND if (t1 == s1) && (t2 == s2)
     * A string of null means disregard the string, so it is not used in the comparison at all.
     * If any of the strings are null then only the other string is compared so it is no longer an And operation.
     */
    static boolean checkStringsAnd(String t1, String t2 , String s1, String s2) {
        boolean match1 = true;
        boolean match2 = true;
        if (s1 != null && t1 != null) {
            if (!s1.equalsIgnoreCase(t1)) {
                match1 = false;
            }
        }
        if (s2 != null && t2 != null) {
            if (!s2.equalsIgnoreCase(t2)) {
                match2 = false;
            }
        }
        return match1 && match2;
    }
    
    /**
     * Convenience function for subclasses
     * Check with OR if (t1 == s1) || (t2 == s2)
     * A string of null means disregard the string, so it is not used in the comparison at all.
     * If any of the strings are null then only the other string is compared so it is no longer an Or operation.
     */
    static boolean checkStringsOr(String t1, String t2, String s1, String s2) {
        boolean match1 = false;
        boolean match2 = false;
        if (s1 != null && t1 != null) {
            if (s1.equalsIgnoreCase(t1)) {
                match1 = true;
            }
        }
        
        if (s2 != null && t2 != null) {
            if (s2.equalsIgnoreCase(t2)) {
                match2 = true;
            }
        }
        return match1 || match2;
    }
}
