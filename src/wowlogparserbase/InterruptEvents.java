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
import wowlogparserbase.events.SpellInterruptEvent;

/**
 * Convenience class to handle Interrupt events
 * @author racy
 */
public class InterruptEvents {

    List<SpellInterruptEvent> events = new ArrayList<SpellInterruptEvent>();
    List<String> auraNames = new ArrayList<String>();

    public InterruptEvents() {
    }

    /**
     * Create an InterruptEvents object from events.
     * @param events The events.
     */
    public InterruptEvents(List<BasicEvent> events) {
        for (BasicEvent be : events) {
            if (be instanceof SpellInterruptEvent) {
                SpellInterruptEvent e = (SpellInterruptEvent) be;
                addEvent(e);
            }
        }
    }

    /**
     * Add a spell interrupt event
     * @param e
     */
    public void addEvent(SpellInterruptEvent e) {
        events.add(e);
        if (!nameExists(e.getExtraSpellName())) {
            auraNames.add(e.getExtraSpellName());
        }
    }

    /**
     * Check if an interrupted spell name is present
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
     * Get the events for when a specific spell was interrupted
     * @param name The spell name
     * @return
     */
    public List<SpellInterruptEvent> getEvents(String name) {
        ArrayList<SpellInterruptEvent> retEvents = new ArrayList<SpellInterruptEvent>();
        for (SpellInterruptEvent e : events) {
            if (e.getExtraSpellName().equals(name)) {
                retEvents.add(e);
            }
        }
        return retEvents;
    }

    /**
     * Get the number of spell names that were interrupted
     * @return
     */
    public int getNumNames() {
        return auraNames.size();
    }

    /**
     * Get a specific spell name that was interrupted
     * @param index The index
     * @return
     */
    public String getName(int index) {
        return auraNames.get(index);
    }
}
