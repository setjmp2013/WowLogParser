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

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author racy
 */
public class StringDatabase {
    private static final Object lock = new Object();
    private static StringDatabase theInstance = null;
    private static StringDatabase backupInstance = null;

    private Map<String, String> stringMap;

    private StringDatabase() {
        stringMap = new HashMap<String, String>();
    }

    public static StringDatabase getInstance() {
        if (theInstance == null) {
            StringDatabase db = new StringDatabase();
            backupInstance = db;
            theInstance = db;
        }
        return theInstance;
    }

    public static void makeTemporaryInstance() {
        theInstance = new StringDatabase();
    }

    public static void removeTemporaryInstance() {
        theInstance = backupInstance;
    }

    public String processString(String s) {
        String mapString = stringMap.get(s);
        if (mapString == null) {
            stringMap.put(s, s);
            return s;
        } else {
            return mapString;
        }
    }
}
