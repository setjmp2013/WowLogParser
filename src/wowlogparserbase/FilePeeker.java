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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import wowlogparserbase.events.LineParser;
import wowlogparserbase.events.LogEvent;

/**
 * Parse a log file, but only the time/date part so splitting can be made.
 * @author racy
 */
public class FilePeeker {
    List<LogEvent> events;
    int numErrors = 0;
    File file;

    public FilePeeker(File file) {
        this.file = file;
    }

    public List<LogEvent> getEvents() {
        return events;
    }

    public File getFile() {
        return file;
    }

    public int getNumErrors() {
        return numErrors;
    }
    
    /**
     * Get a set of uniue dates contained in the log file.
     * @return A set of dates.
     */
    public Set<WlpDate> getDates() {
        Set<WlpDate> dates = new TreeSet<WlpDate>();
        WlpDate previousDate = new WlpDate(-1,1,1);
        for (LogEvent e : events) {
            WlpDate d = new WlpDate(0, e.month, e.day);
            if (previousDate.compareTo(d) != 0) {
                dates.add(d);
                previousDate = d;
            }
        }
        return dates;
    }
    
    /**
     * Get the rows that has the supplied date.
     * @param d The date
     * @return The row indices.
     */
    public List<Integer> getRows(WlpDate d) {
        List<Integer> rows = new ArrayList<Integer>();
        for (LogEvent e : events) {
            if(e.month == d.getMonth() && e.day == d.getDay()) {
                rows.add(e.getLogFileRow());
            }
        }
        return rows;
    }
            
    /**
     * Get the rows that has the supplied date.
     * @param d The date
     * @return The row indices.
     */
    public List<Integer> getRows(Set<WlpDate> dates) {
        List<Integer> rows = new ArrayList<Integer>();
        for (WlpDate d : dates) {
            rows.addAll(getRows(d));
        }
        TreeSet<Integer> rowSet = new TreeSet<Integer>(rows);
        rows = new ArrayList<Integer>(rowSet);
        return rows;
    }

    public boolean parse() {
        try {
            //long sT = System.currentTimeMillis();
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
            String line = null;
            LineParser lp = new LineParser();
            int row = -1;
            String[] values = new String[0];
            events = new ArrayList<LogEvent>();
            while ((line = reader.readLine()) != null) {
                row++;
                boolean added = false;
                int type;
                LogEvent event = new LogEvent();
                try {
                    lp.parseOnlyTimeDate(line);
                    String timeDate = lp.getTimeDate();
                    if (event.parse(timeDate, values) >= 0) {
                        events.add(event);
                        event.setLogFileRow(row);
                        added = true;
                    }
                } catch (ArrayIndexOutOfBoundsException ex) {
                    int a=0;
                } catch (NumberFormatException ex) {
                    int a=0;
                }                
                //Post processing
                if (added) {
                    event.findOldStrings();
                } else {
                    numErrors++;
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
        return true;
    }
    
}
