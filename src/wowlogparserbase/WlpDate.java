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

/**
 * Class describing a date, year, month, day.
 * @author racy
 */
public class WlpDate implements Comparable<WlpDate> {
    //private static final int[] monthDays = {31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    private static final int[] cumulativeMonthDays = {0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335, 366};

    int year;
    byte month;
    byte day;
    
    long value;

    public WlpDate(int year, int month, int day) {
        this.year = year;
        this.month = (byte)month;
        this.day = (byte)day;
        makeValue();
    }

    public byte getDay() {
        return day;
    }

    public void setDay(byte day) {
        this.day = day;
        makeValue();
    }

    public byte getMonth() {
        return month;
    }

    public void setMonth(byte month) {
        this.month = month;
        makeValue();
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
        makeValue();
    }

    public long getValue() {
        return value;
    }

    @Override
    public int compareTo(WlpDate o) {
        return (int)Math.signum((double)value - (double)o.value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WlpDate other = (WlpDate) obj;
        if (this.value != other.value) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + (int) (this.value ^ (this.value >>> 32));
        return hash;
    }

    private void makeValue() {
        value = year*cumulativeMonthDays[12] + month*cumulativeMonthDays[month-1] + day;
    }
    
    
}
