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

package wowlogparserbase.events.damage;

import wowlogparserbase.Constants;
import wowlogparserbase.SettingsSingleton;


/**
 *
 * @author racy
 */
public class SwingDamageEvent extends DamageEvent {
    
    public SwingDamageEvent() {
        
    }
    
    /**
     * Parse the following event.
     * event, sourceGUID, sourceName, sourceFlags, destGUID, destName, destFlags, amount, school, resisted, blocked, absorbed, critical, glancing, crushing
     * @param line
     * @return
     */
    public int parse(String timeDate, String[] values) {        
        int index = super.parse(timeDate, values);
        if (index < 0) {
            return index;
        }
        int version = SettingsSingleton.getInstance().getWowVersion();

        if (version >= 200 && version < 300) {
            //BC
            if (values.length < index + 8) {
                return -1;
            }
            setDamage(Integer.parseInt(values[index + 0]));
            setSchool(intParse(values[index + 1]));

            //Resisted
            if (!values[index + 2].contains("nil")) {
                setResisted(intParse(values[index + 2]));
            }

            //Blocked
            if (!values[index + 3].contains("nil")) {
                setBlocked(intParse(values[index + 3]));
            }

            //absorbed
            if (!values[index + 4].contains("nil")) {
                setAbsorbed(intParse(values[index + 4]));
            }

            //critical
            if (!values[index + 5].contains("nil")) {
                setCrit(true);
            }

            //glancing
            if (!values[index + 6].contains("nil")) {
                setGlancing(true);
            }

            //crushing
            if (!values[index + 7].contains("nil")) {
                setCrushing(true);
            }
            return index + 8;
        } else {
            //WOTLK+
            if (values.length < index + 9) {
                return -1;
            }
            setDamage(Integer.parseInt(values[index + 0]));
            setOverkill(Integer.parseInt(values[index + 1]));
            setSchool(intParse(values[index + 2]));

            //Resisted
            if (!values[index + 3].contains("nil")) {
                setResisted(intParse(values[index + 3]));
            }

            //Blocked
            if (!values[index + 4].contains("nil")) {
                setBlocked(intParse(values[index + 4]));
            }

            //absorbed
            if (!values[index + 5].contains("nil")) {
                setAbsorbed(intParse(values[index + 5]));
            }

            //critical
            if (!values[index + 6].contains("nil")) {
                setCrit(true);
            }

            //glancing
            if (!values[index + 7].contains("nil")) {
                setGlancing(true);
            }

            //crushing
            if (!values[index + 8].contains("nil")) {
                setCrushing(true);
            }
            return index + 9;
        }
    }
}
