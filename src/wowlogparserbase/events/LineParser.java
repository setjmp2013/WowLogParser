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

import java.util.ArrayList;

/**
 * A parser class that parses log file lines.
 */
public class LineParser {

    String timeDate = null;
    ArrayList<String> values = null;
    String[] strArr = new String[0];

    public void parse(String line) {
        char[] chars = line.toCharArray();

        int index = 0;
        //Find first non whitespace char
        while (index < chars.length) {
            if (chars[index] == ' ') {
                index++;
                continue;
            } else {
                break;
            }
        }

        //Find two white space streaks
        int startIndex = index;
        int endIndex = index;
        int numStreaks = 0;
        while (index < chars.length) {
            if (chars[index] == ' ') {
                numStreaks++;
                if (numStreaks >= 2) {
                    endIndex = index;
                    break;
                }
                index++;
                while (chars[index] == ' ') {
                    index++;
                }
                continue;
            } else {
                index++;
            }
        }
        timeDate = new String(line.substring(startIndex, endIndex));

        //Find the values
        values = new ArrayList<String>(15);
        boolean added = false;
        startIndex = index;
        outer:
        while (index < chars.length) {
            added = false;
            if (chars[index] == '"') {
                index++;
                startIndex = index;
                while (index < chars.length) {
                    if (chars[index] == '"') {
                        endIndex = index;
                        values.add(new String(line.substring(startIndex, endIndex).trim()));
                        added = true;
                        index++;
                        while (index < chars.length) {
                            if (chars[index] == ',') {
                                index++;
                                startIndex = index;
                                continue outer;
                            } else {
                                index++;
                            }
                        }
                        continue outer;
                    } else {
                        index++;
                    }
                }
            }
            if (chars[index] == ',') {
                endIndex = index;
                values.add(new String(line.substring(startIndex, endIndex).trim()));
                added = true;
                index++;
                startIndex = index;
                continue outer;
            }
            index++;
        }
        if (!added) {
            endIndex = index;
            values.add(new String(line.substring(startIndex, endIndex).trim()));
        }
    }

    public void parseOnlyTimeDate(String line) {
        char[] chars = line.toCharArray();

        int index = 0;
        //Find first non whitespace char
        while (index < chars.length) {
            if (chars[index] == ' ') {
                index++;
                continue;
            } else {
                break;
            }
        }

        //Find two white space streaks
        int startIndex = index;
        int endIndex = index;
        int numStreaks = 0;
        while (index < chars.length) {
            if (chars[index] == ' ') {
                numStreaks++;
                if (numStreaks >= 2) {
                    endIndex = index;
                    break;
                }
                index++;
                while (chars[index] == ' ') {
                    index++;
                }
                continue;
            } else {
                index++;
            }
        }
        timeDate = new String(line.substring(startIndex, endIndex));
    }

    public String getTimeDate() {
        return timeDate;
    }

    public String[] getValues() {
        return values.toArray(strArr);
    }
}
