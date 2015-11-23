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
import java.util.Collections;
import java.util.List;

/**
 * A class that describes a time period
 * @author racy
 */
public class TimePeriod implements Comparable<TimePeriod> {
    public double startTime;
    public double endTime;

    /**
     * Constructor
     * @param startTime The start time
     * @param endTime The end time
     */
    public TimePeriod(double startTime, double endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * Compare this time period to another. The start times are compared and the signum is returned
     * @param o The other time period
     * @return The signum of the start times
     */
    @Override
    public int compareTo(TimePeriod o) {
        return (int)Math.signum(startTime - o.startTime);
    }

    /**
     * Check if this period contains the time d.
     * @param d The time to check.
     * @return true if the period contains the time, false otherwise.
     */
    public boolean contains(double d) {
        if (d >= startTime && d <= endTime) {
            return true;
        }
        return false;
    }
    
    /**
     * Check if two time periods intersects
     * @param p The other time period
     * @return true if they intersect, false otherwise
     */
    public boolean intersect(TimePeriod p) {
        if (startTime >= p.startTime && startTime <= p.endTime) {
            return true;
        }
        if (endTime >= p.startTime && endTime <= p.endTime) {
            return true;
        }
        if (p.startTime >= startTime && p.startTime <= endTime) {
            return true;
        }
        if (p.endTime >= startTime && p.endTime <= endTime) {
            return true;
        }
        return false;
    }
    
    /**
     * Check if two time periods intersects
     * @param p The other time period
     * @return true if they intersect, false otherwise
     */
    public boolean intersect2(TimePeriod p) {
        if (startTime <= p.endTime && p.startTime <= endTime) {
            return true;
        }
        return false;
    }

    /**
     * Requires the Time periods to intersect, otherwise the minimum time and maximum time will be too far apart.
     * @param p The other TimePeriod
     * @return A merged TimePeriod
     */
    public TimePeriod merge(TimePeriod p) {
        double start = Math.min(p.startTime, startTime);
        double end = Math.max(p.endTime, endTime);
        return new TimePeriod(start, end);
    }

    public double getDuration() {
        return endTime - startTime;
    }

    public static TimePeriod mergeTimePeriodsAlways(List<TimePeriod> tps) {
        TimePeriod out = tps.get(0);
        for (TimePeriod tp : tps) {
            out = out.merge(tp);
        }
        return out;
    }
    
    /**
     * Merge time periods. Process all the source time periods and merge any that intersects.
     * @param periods An array of time periods
     * @return The new array of time periods after merging
     */
    public static List<TimePeriod> mergeTimePeriods(List<TimePeriod> inPeriods) {
        List<TimePeriod> periods = new ArrayList<TimePeriod>(inPeriods);
        List<TimePeriod> destTimes = new ArrayList<TimePeriod>();
        int index = 0;
        while(periods.size()>index) {
            TimePeriod p = periods.get(index);
            periods.remove(index);
            boolean merged = false;
            for (int k=0; k<destTimes.size(); k++) {
                TimePeriod p2 = destTimes.get(k);
                if(p.intersect(p2)) {
                    destTimes.set(k, p.merge(p2));
                    merged = true;
                    break;
                }
            }
            if(merged) {                
            } else {
                destTimes.add(p);
            }
        }
        
        //Clean up
        while(true) {
            boolean merged = false;
            first_for:
            for (int k = 0; k < destTimes.size(); k++) {
                TimePeriod p = destTimes.get(k);
                for (int l = 0; l < destTimes.size(); l++) {
                    if (k == l) {
                        continue;
                    }
                    TimePeriod p2 = destTimes.get(l);
                    if (p.intersect(p2)) {
                        merged = true;
                        TimePeriod mergedTime = p.merge(p2);
                        destTimes.set(k, mergedTime);
                        destTimes.remove(l);
                        break first_for;
                    }
                }
            }
            
            if (!merged) {
                break;
            }
        }
        Collections.sort(destTimes);
        return destTimes;
    }
}
