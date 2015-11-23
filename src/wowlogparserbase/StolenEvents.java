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

import java.util.ArrayList;
import java.util.List;
import wowlogparserbase.events.BasicEvent;
import wowlogparserbase.events.aura.SpellAuraStolenEvent;

/**
 * Convenience class to handle dispell events.
 * @author racy
 */
public class StolenEvents {

    List<SpellAuraStolenEvent> events = new ArrayList<SpellAuraStolenEvent>();
    List<String> auraNames = new ArrayList<String>();

    public StolenEvents() {
    }

    /**
     * Create a DispellEvents object from events.
     * @param events The events.
     */
    public StolenEvents(List<BasicEvent> events) {
        for (BasicEvent be : events) {
            if (be instanceof SpellAuraStolenEvent) {
                SpellAuraStolenEvent e = (SpellAuraStolenEvent) be;
                addEvent(e);
            }
        }        
    }    
    
    /**
     * Add an aura dispelled event
     * @param e
     */
    public void addEvent(SpellAuraStolenEvent e) {
        events.add(e);
        if (!nameExists(e.getExtraSpellName())) {
            auraNames.add(e.getExtraSpellName());
        }
    }

    /**
     * Check if an aura name is present
     * @param s The name to check for
     * @return
     */
    public boolean nameExists(String s) {
        for (String existing : auraNames) {
            if (existing.equals(s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the dispell events for when a specific aura was dispelled
     * @param name
     * @return
     */
    public List<SpellAuraStolenEvent> getEvents(String name) {
        ArrayList<SpellAuraStolenEvent> retEvents = new ArrayList<SpellAuraStolenEvent>();
        for (SpellAuraStolenEvent e : events) {
            if (e.getExtraSpellName().equals(name)) {
                retEvents.add(e);
            }
        }
        return retEvents;
    }

    /**
     * Get the number of auras found
     * @return
     */
    public int getNumNames() {
        return auraNames.size();
    }

    /**
     * Get an aura name
     * @param index
     * @return
     */
    public String getName(int index) {
        return auraNames.get(index);
    }
}
