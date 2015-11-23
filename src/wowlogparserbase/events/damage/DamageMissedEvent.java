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

import java.util.HashMap;
import org.w3c.dom.Element;
import wowlogparserbase.StringDatabase;

/**
 * Convenient class to group up Miss Events
 * @author racy
 */
public class DamageMissedEvent extends DamageEvent {
    private String missType = null;
    
    public String toString() {
        String s = super.toString();
        s = s + "Miss Type = " + getMissType() + newLine;
        return s;
    }

    public Element addXmlAttributes(Element e) {
        super.addXmlAttributes(e);
        e.setAttribute(XMLTAG_MISS_TYPE,getMissType());
        return e;
    }

    @Override
    public void findOldStrings() {
        super.findOldStrings();
        StringDatabase db = StringDatabase.getInstance();
        missType = db.processString(missType);

    }

    public String getMissType() {
        return missType;
    }

    public void setMissType(String missType) {
        this.missType = missType;
    }
}
