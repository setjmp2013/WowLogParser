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
package wowlogparser.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 *
 * @author racy
 */
public class StringPrefsDb {
    private String BASE_STRING = "StringDb";

    private Preferences prefs;

    public StringPrefsDb(Preferences prefs) {
        this.prefs = prefs.node("StringDb");
    }
    
    public List<String> getSavedStrings() {
        int numSavedStrings = prefs.getInt(BASE_STRING + "numSavedStrings", 0);
        List<String> ret = new ArrayList<String>();
        for (int k=0; k<numSavedStrings; k++) {
            String key = BASE_STRING + "Item" + k;
            String value = prefs.get(key, "");
            ret.add(value);
        }
        return ret;
    }

    public void putStrings(List<String> data) {
        prefs.putInt(BASE_STRING + "numSavedStrings", data.size());
        for (int k=0; k<data.size(); k++) {
            String key = BASE_STRING + "Item" + k;
            prefs.put(key, data.get(k));
        }
    }
}
