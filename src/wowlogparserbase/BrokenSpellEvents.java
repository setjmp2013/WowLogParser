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
import wowlogparserbase.events.aura.SpellAuraBrokenBaseEvent;
import wowlogparserbase.events.aura.SpellAuraBrokenSpellEvent;

/**
 * Convenience class to handle broken events
 * @author racy
 */
public class BrokenSpellEvents {

    List<SpellAuraBrokenBaseEvent> events = new ArrayList<SpellAuraBrokenBaseEvent>();
    List<String> auraNames = new ArrayList<String>();

    public BrokenSpellEvents() {
    }

    /**
     * Create an BrokenSpellEvents object from events.
     * @param events The events.
     */
    public BrokenSpellEvents(List<BasicEvent> events) {
        for (BasicEvent be : events) {
            if (be instanceof SpellAuraBrokenBaseEvent) {
                SpellAuraBrokenBaseEvent e = (SpellAuraBrokenBaseEvent) be;
                addEvent(e);
            }
        }
    }

    /**
     * Add a spell broken event
     * @param e
     */
    public void addEvent(SpellAuraBrokenBaseEvent e) {
        events.add(e);
        if (!nameExists(e.getSkillName())) {
            auraNames.add(e.getSkillName());
        }
    }

    /**
     * Check if a broken spell name is present
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
     * Get the events for when a specific spell was broken
     * @param name The spell name
     * @return
     */
    public List<SpellAuraBrokenBaseEvent> getEvents(String name) {
        ArrayList<SpellAuraBrokenBaseEvent> retEvents = new ArrayList<SpellAuraBrokenBaseEvent>();
        for (SpellAuraBrokenBaseEvent e : events) {
            if (e.getSkillName().equals(name)) {
                retEvents.add(e);
            }
        }
        return retEvents;
    }

    /**
     * Get the number of spell names that were broken
     * @return
     */
    public int getNumNames() {
        return auraNames.size();
    }

    /**
     * Get a specific spell name that was broken
     * @param index The index
     * @return
     */
    public String getName(int index) {
        return auraNames.get(index);
    }
}
