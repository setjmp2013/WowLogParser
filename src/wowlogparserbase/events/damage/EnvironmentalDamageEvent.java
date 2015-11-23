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

import org.w3c.dom.Element;
import wowlogparserbase.StringDatabase;
import wowlogparserbase.Constants;
import wowlogparserbase.SettingsSingleton;


/**
 *
 * @author racy
 */
public class EnvironmentalDamageEvent extends DamageEvent {
    private String environmentalType;
    
    public EnvironmentalDamageEvent() {
        
    }
    
    /**
     * Parse the following event.
     * event, sourceGUID, sourceName, sourceFlags, destGUID, destName, destFlags, environmentalType, amount, school, resisted, blocked, absorbed, critical, glancing, crushing
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
            if (values.length < index + 9) {
                return -1;
            }
            setEnvironmentalType(values[index + 0]);
            setDamage(Integer.parseInt(values[index + 1]));
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
        } else {
            //WOTLK +
            if (values.length < index + 10) {
                return -1;
            }
            setEnvironmentalType(values[index + 0]);
            setDamage(Integer.parseInt(values[index + 1]));
            setOverkill(Integer.parseInt(values[index + 2]));
            setSchool(intParse(values[index + 3]));

            //Resisted
            if (!values[index + 4].contains("nil")) {
                setResisted(intParse(values[index + 4]));
            }

            //Blocked
            if (!values[index + 5].contains("nil")) {
                setBlocked(intParse(values[index + 5]));
            }

            //absorbed
            if (!values[index + 6].contains("nil")) {
                setAbsorbed(intParse(values[index + 6]));
            }

            //critical
            if (!values[index + 7].contains("nil")) {
                setCrit(true);
            }

            //glancing
            if (!values[index + 8].contains("nil")) {
                setGlancing(true);
            }

            //crushing
            if (!values[index + 9].contains("nil")) {
                setCrushing(true);
            }
            return index + 10;
        }
        
    }

    @Override
    public String toString() {
        String s = super.toString();
        s = s + "Environmental type = " + getEnvironmentalType() + newLine;
        return s;
    }

    public Element addXmlAttributes(Element e) {
        super.addXmlAttributes(e);
        e.setAttribute(XMLTAG_ENVIRONMENTAL_TYPE,getEnvironmentalType());
        return e;
    }

    @Override
    public void findOldStrings() {
        super.findOldStrings();
        StringDatabase db = StringDatabase.getInstance();
        environmentalType = db.processString(environmentalType);

    }

    public String getEnvironmentalType() {
        return environmentalType;
    }

    public void setEnvironmentalType(String environmentalType) {
        this.environmentalType = environmentalType;
    }
}
