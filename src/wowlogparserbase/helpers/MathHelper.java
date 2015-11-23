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

import java.util.List;

/**
 *
 * @author racy
 */
public class MathHelper {
    public static double makePercentSafe(double upper, double lower) {
        if (lower == 0) {
            return 0;
        }
        return upper/lower;
    }
    
    public static <T extends Number> double mean(List<T> numbers) {
        if (numbers.size() == 0) {
            return 0;
        }
        double mean = 0;
        for (Number n : numbers) {
            mean += n.doubleValue();
        }
        mean /= (double)numbers.size();
        return mean;
    }

    public static <T extends Number> double sqMean(List<T> numbers) {
        if (numbers.size() == 0) {
            return 0;
        }
        double mean = 0;
        for (Number n : numbers) {
            mean += (n.doubleValue()*n.doubleValue());
        }
        mean /= (double)numbers.size();
        return mean;
    }

    public static <T extends Number> double variance(List<T> numbers) {
        double mean = mean(numbers);
        double sqMean = sqMean(numbers);
        return sqMean - mean*mean;
    }
    
    public static <T extends Number> double stdDev(List<T> numbers) {
        return Math.sqrt(variance(numbers));
    }

    public static double round1Decimals(double val) {
        return Math.round(val*10) / 10.0;
    }

    public static double round2Decimals(double val) {
        return Math.round(val*100) / 100.0;
    }

    public static double round3Decimals(double val) {
        return Math.round(val*1000) / 1000.0;
    }
}