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

package wowlogparserbase.helpers;

import java.util.*;

/**
 *
 * @author racy
 */
public class ListFunctions {
    
    /**
     * Remove equal elements from a list so that only one of each remains. 
     * The list must be sorted before calling this function.
     * @param c The list
     */
    public static <T extends Comparable> void removeEqual(List<T> c) {
        int k = 0;
        while (true) {
            if (k + 1 >= c.size()) {
                return;
            }
            Comparable e = c.get(k);
            Comparable e2 = c.get(k+1);
            if (e.compareTo(e2) == 0) {
                c.remove(k+1);
                continue;
            }
            k++;
        }        
    }

    /**
     * Remove equal elements from a list so that only one of each remains. 
     * The list must be sorted before calling this function.
     * @param c The list
     */
    public static <T extends Comparable> void removeEqualLinked(LinkedList<T> c) {
        ListIterator<T> it = c.listIterator(0);
        try {
            while (true) {
                Comparable e = it.next();
                Comparable e2 = it.next();
                if (e.compareTo(e2) == 0) {
                    it.remove();
                    it.previous();
                    continue;
                } else {
                    it.previous();
                }
            }
        } catch (NoSuchElementException ex) {
        }
    }

    /**
     * Remove "removeItems" from "list"
     * @param list The source list
     * @param removeItems A list with items to remove
     */
    public static <T extends Comparable> void remove(List<T> list, List<T> removeItems) {
        int k;
        for (k=0; k<removeItems.size(); k++) {
            Comparable rem = removeItems.get(k);
            for (int l = 0; l<list.size(); l++) {
                if (rem.compareTo(list.get(l)) == 0) {
                    list.remove(l);
                    if (l >= list.size()) {
                        break;
                    }
                }
            }
        }
    }

    /**
     * Check if "list" contains "item" using the Comparable interface
     * @param list The source list
     * @param item The item to check for
     * @return true if the item exists, false otherwise
     */
    public static <T extends Comparable> boolean contains(List<T> list, T item) {
        int k;
        boolean exists = false;
        for (T sourceItem : list) {
            if (sourceItem.compareTo(item) == 0) {
                exists = true;
                break;
            }
        }
        return exists;
    }
}
