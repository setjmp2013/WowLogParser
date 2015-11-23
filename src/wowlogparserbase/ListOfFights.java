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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class containing a list of Fights.
 * It can be used to find time intersecting fights etc.
 * @author gustav
 */
public class ListOfFights {
    List<Fight> fights = new ArrayList<Fight>();

    /**
     * Add a fight to the list
     * @param f
     */
    public void addFight(Fight f) {
        if (!fights.contains(f)) {
            fights.add(f);
        }
    }

    /**
     * Add several fights to the list
     * @param fs
     */
    public void addFights(Collection<Fight> fs) {
        for (Fight f : fs) {
            addFight(f);
        }
    }

    /**
     * Add several fights from another ListOfFights
     * @param fs
     */
    public void addFights(ListOfFights fs) {
        for (Fight f : fs.fights) {
            addFight(f);
        }
    }

    /**
     * Get the time period that the fights span.
     * @return A TimePeriod object with min/max time
     */
    public TimePeriod getTimePeriod() {
        List<TimePeriod> tps = new ArrayList<TimePeriod>();
        for (Fight f : fights) {
            tps.add(new TimePeriod(f.getStartTimeFast(), f.getEndTimeFast()));
        }

        TimePeriod mergedTP = tps.get(0);
        for (TimePeriod tp : tps) {
            mergedTP = mergedTP.merge(tp);
        }
        return mergedTP;
    }

    /**
     * Create a merged fight from all the fights in this list
     * @return The merged fight
     */
    public Fight getMergedFight() {
        return Fight.merge(fights);
    }

    /**
     * Create a merged fight from all the fights in this list
     * @return The merged fight
     */
    public Fight getMergedFightFromTimePeriods(FileLoader fl) {
        return Fight.createNewFightDuringFightTimePeriods(fights, fl);
    }

    /**
     * Return a List with the fights in this ListOfFights
     * @return
     */
    public List<Fight> getFights() {
        return fights;
    }

    /**
     * Static method that merges a List<ListOfFights> into a single ListOfFights
     * @param in A List with ListOfFights objects.
     * @return A new merged ListOfFight
     */
    static ListOfFights getMergedListOfFights(List<ListOfFights> in) {
        ListOfFights out = new ListOfFights();
        for (ListOfFights lof : in) {
            out.addFights(lof.getFights());
        }
        return out;
    }

    /**
     * Get a representative name. It will be the name of the first fight.
     * @return A name
     */
    public String getName() {
        return fights.get(0).getName();
    }

    /**
     * Get a representative guid, it will be the guid from the first fight.
     * @return A guid
     */
    public String getGuid() {
        return fights.get(0).getGuid();
    }

    /**
     * Get the number of fights in this ListOfFights.
     * @return
     */
    public int size() {
        return fights.size();
    }
}
