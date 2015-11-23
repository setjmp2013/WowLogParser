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

import java.util.HashMap;
import org.w3c.dom.Element;
import wowlogparserbase.StringDatabase;

/**
 *
 * @author racy
 */
public class SpellDrainEvent extends PowerEvent {
    private String extraAmount = null;
    
    public SpellDrainEvent() {
        
    }

    @Override
    public int parse(String timeDate, String[] values) {
        int index = super.parse(timeDate, values);
        if (index < 0) {
            return index;
        }
        if (values.length < index + 1) {
            return -1;
        }
        
        setExtraAmount(values[index + 0]);
        
        return index + 1;
    }

    public String getExtraAmount() {
        return extraAmount;
    }

    public void setExtraAmount(String extraAmount) {
        this.extraAmount = extraAmount;
    }
    
    public String toString() {
        String s = super.toString();
        s = s + "Extra Amount = " + extraAmount + newLine;
        return s;
    }

    public Element addXmlAttributes(Element e) {
        super.addXmlAttributes(e);
        e.setAttribute(XMLTAG_AMOUNT_EXTRA, ""+extraAmount);
        return e;
    }

    @Override
    public void findOldStrings() {
        super.findOldStrings();
        StringDatabase db = StringDatabase.getInstance();
        extraAmount = db.processString(extraAmount);

    }
}
