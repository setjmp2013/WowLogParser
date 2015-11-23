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

package wowlogparserbase.events;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.regex.Pattern;
import wowlogparserbase.WLPNumberFormat;

/**
 *
 * Taken and sumarised from the new combatlog (its now LoD and can be found in "\World of Warcraft\WoWTest\AddOns\Blizzard_CombatLog\")
 * The 1st 8 arguments are always
 * Code:
 * 
 * timestamp, event, sourceGUID, sourceName, sourceFlags, destGUID, destName, destFlags
 * 
 * and every argument after that is as follows:
 * 
 * Code:

 * "SWING_DAMAGE"
 * amount, school, resisted, blocked, absorbed, critical, glancing, crushing
 * 
 * "SWING_MISSED"
 * missType

 * "RANGE_DAMAGE"
 * spellId, spellName, spellSchool, amount, school, resisted, blocked, absorbed, critical, glancing, crushing
 * 
 * "RANGE_MISSED"
 * spellId, spellName, spellSchool, missType
 * 
 * "SPELL_DAMAGE"
 * spellId, spellName, spellSchool, amount, school, resisted, blocked, absorbed, critical, glancing, crushing
 * 
 * "SPELL_MISSED"
 * spellId, spellName, spellSchool, missType
 * 
 * "SPELL_HEAL"
 * spellId, spellName, spellSchool, amount, critical
 * 
 * "SPELL_ENERGIZE"
 * spellId, spellName, spellSchool, amount, powerType
 * 
 * "SPELL_PERIODIC_MISSED"
 * spellId, spellName, spellSchool, missType
 * 
 * "SPELL_PERIODIC_DAMAGE"
 * spellId, spellName, spellSchool, amount, school, resisted, blocked, absorbed, critical, glancing, crushing
 * 
 * "SPELL_PERIODIC_HEAL"
 * spellId, spellName, spellSchool, amount, critical
 * 
 * "SPELL_PERIODIC_DRAIN"
 * spellId, spellName, spellSchool, amount, powerType, extraAmount
 * 
 * "SPELL_PERIODIC_LEECH"
 * spellId, spellName, spellSchool, amount, powerType, extraAmount
 * 
 * "SPELL_PERIODIC_ENERGIZE"
 * spellId, spellName, spellSchool, amount, powerType
 * 
 * "SPELL_DRAIN"
 * spellId, spellName, spellSchool, amount, powerType, extraAmount
 * 
 * "SPELL_LEECH"
 * spellId, spellName, spellSchool, amount, powerType, extraAmount
 * 
 * "SPELL_INTERRUPT"
 * spellId, spellName, spellSchool, extraSpellId, extraSpellName, extraSpellSchool
 * 
 * "SPELL_EXTRA_ATTACKS"
 * spellId, spellName, spellSchool, amount
 * 
 * "SPELL_INSTAKILL"
 * spellId, spellName, spellSchool
 * 
 * "SPELL_DURABILITY_DAMAGE"
 * spellId, spellName, spellSchool
 * 
 * "SPELL_DURABILITY_DAMAGE_ALL"
 * spellId, spellName, spellSchool
 * 
 * "SPELL_DISPEL_FAILED"
 * spellId, spellName, spellSchool, extraSpellId, extraSpellName, extraSpellSchool
 * 
 * "SPELL_AURA_DISPELLED"
 * spellId, spellName, spellSchool, extraSpellId, extraSpellName, extraSpellSchool, auraType
 * 
 * "SPELL_AURA_STOLEN"
 * spellId, spellName, spellSchool, extraSpellId, extraSpellName, extraSpellSchool, auraType
 * 
 * "SPELL_AURA_APPLIED"
 * spellId, spellName, spellSchool, auraType
 * (source arguments are nil)
 * 
 * "SPELL_AURA_REMOVED"
 * spellId, spellName, spellSchool, auraType
 * (source arguments are nil)
 * 
 * "SPELL_AURA_APPLIED_DOSE"
 * spellId, spellName, spellSchool, auraType, amount
 * (source arguments are nil)
 * 
 * "SPELL_AURA_REMOVED_DOSE"
 * spellId, spellName, spellSchool, auraType, amount
 * (source arguments are nil)
 * 
 * "SPELL_CAST_START"
 * spellId, spellName, spellSchool
 * 
 * "SPELL_CAST_SUCCESS"
 * spellId, spellName, spellSchool
 * 
 * "SPELL_CAST_FAILED"
 * spellId, spellName, spellSchool, missType
 * 
 * @author racy
 */
public class LogEvent implements Comparable<LogEvent>, Serializable {
    private static final int[] monthDays = {31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    public byte month;
    public byte day;
    public byte hour;
    public byte minute;
    public float second;
    
    public double time;    
    public int logFileRow = -1;
    //public static String newLine = System.getProperty("line.sparator");
    public static final String newLine = "\n";
    static Pattern whiteSpacePattern = Pattern.compile("\\s++");
    static Pattern slashPattern = Pattern.compile("/");
    static Pattern colonPattern = Pattern.compile(":");
    static Pattern hexPattern = Pattern.compile("0x");
    
    /**
     * Default constructor
     */
    public LogEvent() {
    }

    /**
     * Set the log file row that this event came from
     * @param row The row
     */
    public void setLogFileRow(int row) {
        this.logFileRow = row;
    }

    /**
     * Get the log file row that this event came from
     * @return
     */
    public int getLogFileRow() {
        return logFileRow;
    }
    
    /**
     * Compare thie event to another. The comparison is done on the row numbers.
     * @param o The other object
     * @return Negative if this row is less than the other, 0 if equal and positive if this row is greater than the other.
     */
    @Override
    public int compareTo(LogEvent o) {
        return (int)Math.signum(logFileRow - o.logFileRow);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LogEvent) {
            LogEvent e = (LogEvent) obj;
            if (logFileRow == e.logFileRow) {
                return true;
            }
        } else {
            return false;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + this.logFileRow;
        return hash;
    }
    
    /**
     * Parse a log line
     * @param timeDate The time/date string
     * @param values An array made from the comma separated list in the log file
     * @return next argument index or -1 if error
     */
    public int parse(String timeDate, String[] values) {
        //String[] splitLine = timeDate.split("\\s++");
        String[] splitLine = whiteSpacePattern.split(timeDate);
        if (splitLine.length < 2) {
            return -1;
        }
        String dateStr = splitLine[0];
        String timeStr = splitLine[1];
        try {
            setDate(dateStr);
            setTime(timeStr);
            calcTime();
        } catch (NumberFormatException ex) {
            return -1;
        }
        return 0;
    }    
    
    /**
     * Make a time string where the seconds are integers
     * @return The time string
     */
    public String getTimeString() {
        NumberFormat nf = WLPNumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumIntegerDigits(2);
        nf.setGroupingUsed(false);
        String s = "" + nf.format(hour)+":"+nf.format(minute)+":";
        s = s + nf.format((int)second);
        return s;
    }
    
    /**
     * Make a time string where the seconds have 2 decimals
     * @return The time string
     */
    public String getTimeStringExact() {
        NumberFormat nf = WLPNumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        nf.setMinimumIntegerDigits(2);
        nf.setGroupingUsed(false);
        NumberFormat nf2 = WLPNumberFormat.getInstance();
        nf2.setMaximumFractionDigits(2);
        nf2.setMinimumFractionDigits(2);
        nf2.setMinimumIntegerDigits(2);
        nf2.setGroupingUsed(false);
        String s = "" + nf.format(hour)+":"+nf.format(minute)+":";
        s = s + nf2.format(second);
        return s;
    }

    /**
     * Calculate the time in seconds from the start of the year
     */
    protected void calcTime() {
        int k;
        time = 0;
        for(k=1; k<month; k++) {
            time += monthDays[k-1]*24*60*60;
        }
        time += (day-1)*24*60*60;
        time += (hour)*60*60;
        time += (minute)*60;
        time += second;
    }
        
    /**
     * Set the date
     * @param dateStr The date string
     */
    public void setDate(String dateStr) {
        //String[] dates = dateStr.split("/");
        String[] dates = slashPattern.split(dateStr);
        month = (byte)Integer.parseInt(dates[0]);
        day = (byte)Integer.parseInt(dates[1]);
    }
    
    /**
     * Set the time
     * @param timeStr The time string
     */
    public void setTime(String timeStr) {
        //String[] times = timeStr.split(":");
        String[] times = colonPattern.split(timeStr);
        hour = (byte)Integer.parseInt(times[0]);
        minute = (byte)Integer.parseInt(times[1]);
        second = Float.parseFloat(times[2]);
    }
    
    /**
     * Parse an integer
     * @param s The string to parse
     * @return An integer parsed from the string
     */
    public static int intParse(String s) {
        if(s.contains("0x")) {
            //return Integer.parseInt(s.split("0x")[1], 16);
            return Integer.parseInt(hexPattern.split(s)[1], 16);
        } else {
            return Integer.parseInt(s);
        }
    }
    
    /**
     * Parse a long
     * @param s The string to parse
     * @return A long parsed from the string
     */
    public static long longParse(String s) {
        if(s.contains("0x")) {
            //return Long.parseLong(s.split("0x")[1], 16);
            return Long.parseLong(hexPattern.split(s)[1], 16);
        } else {
            return Long.parseLong(s);
        }
    }
    
    public String toString() {
        String s = "";
        s = s + "Parser Class = " + getClass().getSimpleName() + newLine;
        s = s + "Time = " + getTimeStringExact() + newLine;
        s = s + "TimeSeconds = " + (int)time + newLine;
        s = s + "Log file row = " + logFileRow + newLine;
        return s;
    }

    public void findOldStrings() {
        
    }
}
