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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import wowlogparserbase.eventfilter.Filter;
import wowlogparserbase.eventfilter.FilterClass;
import wowlogparserbase.events.BasicEvent;
import wowlogparserbase.events.SkillInterface;
import wowlogparserbase.events.SpellCastSuccessEvent;
import wowlogparserbase.events.damage.DirectSpellDamageEvent;
import wowlogparserbase.events.healing.DirectHealingEvent;
import wowlogparserbase.events.healing.SpellHealingEvent;

/**
 *
 * @author racy
 */
public class RotationParser {
    EventCollection events;
    HashMap<SpellInfo, List<SpellCastSuccessEvent>> spellCastSuccessMap = new HashMap<SpellInfo, List<SpellCastSuccessEvent>>();
    HashMap<SpellInfo, List<DirectSpellDamageEvent>> spellDamageMap = new HashMap<SpellInfo, List<DirectSpellDamageEvent>>();
    HashMap<SpellInfo, List<DirectHealingEvent>> spellHealingMap = new HashMap<SpellInfo, List<DirectHealingEvent>>();

    public RotationParser(EventCollection events) {
        this.events = events;
        makeSpellSuccessMap();
        makeSpellDamageMap();
        makeSpellHealingMap();
    }

    private void makeSpellSuccessMap() {
        List<SpellCastSuccessEvent> successEvents = events.getEvents(new SpellCastSuccessEvent());
        for (SpellCastSuccessEvent e : successEvents) {
            SpellInfo si = new SpellInfo(e.getSkillID(), e.getSkillName(), e.getSchool());
            List<SpellCastSuccessEvent> storedEvents = spellCastSuccessMap.get(si);
            if (storedEvents == null) {
                storedEvents = new ArrayList<SpellCastSuccessEvent>();
            }
            storedEvents.add(e);
            spellCastSuccessMap.put(si, storedEvents);
        }
        
        List<SpellInfo> sis = new ArrayList<SpellInfo>(spellCastSuccessMap.keySet());
        if (sis != null) {
            for (SpellInfo si : sis) {
                List<SpellCastSuccessEvent> evs = spellCastSuccessMap.get(si);
                if (evs.size() < 2) {
                    spellCastSuccessMap.remove(si);
                }
            }
        }
    }
    
    private void makeSpellDamageMap() {
        FilterClass f1 = new FilterClass(DirectSpellDamageEvent.class);
        FilterClass f2 = new FilterClass(SkillInterface.class);
        List<BasicEvent> damageEvents = events.filter(f1).filter(f2).getEvents();

        for (BasicEvent be : damageEvents) {
            DirectSpellDamageEvent e = (DirectSpellDamageEvent)be;
            SpellInfo si = new SpellInfo(e.getSkillID(), e.getSkillName(), e.getSchool());
            //Do not use events that already have SpellCastSuccess events. Probably instants.
            if(spellCastSuccessMap.containsKey(si)) {
                continue;
            }
            List<DirectSpellDamageEvent> storedEvents = spellDamageMap.get(si);
            if (storedEvents == null) {
                storedEvents = new ArrayList<DirectSpellDamageEvent>();
            }
            storedEvents.add(e);
            spellDamageMap.put(si, storedEvents);
        }

        List<SpellInfo> sis = new ArrayList<SpellInfo>(spellDamageMap.keySet());
        if (sis != null) {
            for (SpellInfo si : sis) {
                List<DirectSpellDamageEvent> evs = spellDamageMap.get(si);
                if (evs.size() < 2) {
                    spellDamageMap.remove(si);
                }
            }
        }
    }

    private void makeSpellHealingMap() {
        FilterClass f1 = new FilterClass(DirectHealingEvent.class);
        FilterClass f2 = new FilterClass(SkillInterface.class);
        List<BasicEvent> healingEvents = events.filter(f1).filter(f2).getEvents();

        for (BasicEvent be : healingEvents) {
            DirectHealingEvent e = (DirectHealingEvent)be;
            SpellInfo si = new SpellInfo(e.getSkillID(), e.getSkillName(), e.getSchool());
            //Do not use events that already have SpellCastSuccess events. Probably instants.
            if(spellCastSuccessMap.containsKey(si)) {
                continue;
            }
            if(spellDamageMap.containsKey(si)) {
                continue;
            }
            List<DirectHealingEvent> storedEvents = spellHealingMap.get(si);
            if (storedEvents == null) {
                storedEvents = new ArrayList<DirectHealingEvent>();
            }
            storedEvents.add(e);
            spellHealingMap.put(si, storedEvents);
        }

        List<SpellInfo> sis = new ArrayList<SpellInfo>(spellHealingMap.keySet());
        if (sis != null) {
            for (SpellInfo si : sis) {
                List<DirectHealingEvent> evs = spellHealingMap.get(si);
                if (evs.size() < 2) {
                    spellHealingMap.remove(si);
                }
            }
        }
    }

    public List<SpellInfo> getSpells() {
        List<SpellInfo> sis = new ArrayList<SpellInfo>();
        sis.addAll(spellCastSuccessMap.keySet());
        sis.addAll(spellDamageMap.keySet());
        sis.addAll(spellHealingMap.keySet());
        return sis;
    }
    
    public RotationInfo getRotationInfo(SpellInfo si) {
        List<SpellCastSuccessEvent> eventsSuccess = spellCastSuccessMap.get(si);
        List<DirectSpellDamageEvent> eventsDamage = spellDamageMap.get(si);
        List<DirectHealingEvent> eventsHealing = spellHealingMap.get(si);
        List<BasicEvent> events = new ArrayList<BasicEvent>();
        if (eventsSuccess != null) {
            events.addAll(eventsSuccess);
        }

        if (eventsDamage != null) {
            events.addAll(eventsDamage);
        }

        if (eventsHealing != null) {
            events.addAll(eventsHealing);
        }

        if (events == null) {
            return null;
        }
        Collections.sort(events);
        RotationInfo inf = new RotationInfo(events);
        return inf;
    }
    
    
}
