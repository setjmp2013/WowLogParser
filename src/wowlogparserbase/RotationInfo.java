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

import wowlogparserbase.helpers.MathHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import wowlogparserbase.events.BasicEvent;

/**
 *
 * @author racy
 */
public class RotationInfo {
    double strideMean;
    double strideStd;
    double numberOfCasts;
    double strideMax;
    double strideMin;
    List<BasicEvent> events;
    List<Double> strides;
    
    public <T extends BasicEvent> RotationInfo(List<T> events) {
        this.events = new ArrayList<BasicEvent>(events);
        makeStatistics(events);
    }

    private <T extends BasicEvent> void makeStatistics(List<T> events) {
        strides = makeStrides(events);
        if (strides == null) {
            int a=0;
        }
        strideMean = MathHelper.mean(strides);
        strideStd = MathHelper.stdDev(strides);
        numberOfCasts = events.size();
        try {
            strideMax = Collections.max(strides);
            strideMin = Collections.min(strides);
        } catch(Exception ex) {
            int a=0;
        }
    }

    private <T extends BasicEvent> List<Double> makeStrides(List<T> events) {
        List<Double> strides = new ArrayList<Double>();
        for (int k=1; k< events.size(); k++) {
            strides.add(events.get(k).time - events.get(k-1).time);
        }
        return strides;
    }
    
    public double getNumberOfCasts() {
        return numberOfCasts;
    }

    public double getStrideMax() {
        return strideMax;
    }

    public double getStrideMean() {
        return strideMean;
    }

    public double getStrideMin() {
        return strideMin;
    }

    public double getStrideStd() {
        return strideStd;
    }

    public List<BasicEvent> getEvents() {
        return events;
    }
    
    public List<Double> getStrides() {
        return strides;
    }
    
}
