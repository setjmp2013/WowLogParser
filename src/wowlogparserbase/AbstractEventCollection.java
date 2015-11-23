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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import wowlogparserbase.events.*;
import wowlogparserbase.events.damage.*;
import wowlogparserbase.events.healing.*;

/**
 *
 * @author racy
 */
public abstract class AbstractEventCollection implements EventCollection, Serializable {
    static final String[] classNames = {"Unknown", "Druid", "Mage", "Warrior", "Warlock", "Shaman", "Priest",
    "Rogue", "Paladin", "Hunter", "Death knight"};

    private double activeDuration = 0;

    public AbstractEventCollection() {

    }

    public AbstractEventCollection(EventCollection in) {
        setActiveDuration(in.getActiveDuration());
    }

    /**
     * Static method. Used to set the name that should be shown for a class.
     * @param pClass The player class
     * @param skills The String that should be shown for this class
     */
    public static void setClassName(int pClass, String name) {
        classNames[pClass] = name;
    }

    /**
     * A Comparator class that compares LogEvent:s with regards to its time.
     */
    class TimeComparator implements Comparator<LogEvent> {

        @Override
        public int compare(LogEvent o1, LogEvent o2) {
            return (int)Math.signum(o1.time - o2.time);
        }

    }

    /**
     * Get the events between two times.
     * This method cannot be overriden so only the basic EventCollection events are returned.
     * @param startTime
     * @param endTime
     * @return
     */
    protected abstract List<BasicEvent> getEvents(double startTime, double endTime);

    /**
     * Add an event
     * @param e The event
     * @param logFileRow The row in the log file for the event
     */
    @Override
    public void addEvent(BasicEvent e, int logFileRow) {
        e.setLogFileRow(logFileRow);
        addEvent(e);
    }

    /**
     * Add all the events in the collection.
     * The addEvent method is used to add every event.
     * @param ec
     */
    @Override
    public void addEvents(EventCollection ec) {
        for (BasicEvent e : ec.getEvents()) {
            addEvent(e);
        }
    }

    /**
     * Add all events in the list.
     * The addEvent method is used to add every event.
     * @param evs
     */
    @Override
    public void addEvents(Collection<BasicEvent> evs) {
        for (BasicEvent e : evs) {
            addEvent(e);
        }
    }
    
    /**
     * Add all events in an array no matter what
     * The lastEvent status is not updated.
     * @param evs The events
     */
    @Override
    public void addEventsAlways(Collection<BasicEvent> evs) {
        for (BasicEvent e : evs) {
            addEventAlways(e);
        }
    }

    /**
     * Add all events in an array no matter what
     * The lastEvent status is not updated.
     * @param evs The events
     */
    @Override
    public void addEventsAlways(EventCollection ec) {
        List<BasicEvent> evs = ec.getEvents();
        addEventsAlways(evs);
    }
    
    /**
     * Get a list of the raw log types (the types in the log file) that are present in this collection.
     * @return A set with the log types
     */
    @Override
    public Set<String> getLogTypes() {
        List<BasicEvent> events = getEvents();
        Set<String> types = new HashSet<String>();
        for (BasicEvent e : events) {
            types.add(e.getLogType());
        }
        return types;
    }

    /**
     * Get all guids and the names connected to them.
     * @return A map with guid as key and name as value.
     */
    @Override
    public Map<String, String> getAllGuidsAndNames() {
        List<BasicEvent> events = getEvents();
        Map<String, String> out = new HashMap<String, String>();
        for (BasicEvent e : events) {
            if (!e.getSourceGUID().equalsIgnoreCase("0x0000000000000000")) {
                out.put(e.getSourceGUID(), e.getSourceName());
            }
            if (!e.getDestinationGUID().equalsIgnoreCase("0x0000000000000000")) {
                out.put(e.getDestinationGUID(), e.getDestinationName());
            }
        }
        return out;
    }

    /**
     * Get all source guids and the names connected to them.
     * @return A map with guid as key and name as value.
     */
    @Override
    public Map<String, String> getSourceGuidsAndNames() {
        List<BasicEvent> events = getEvents();
        Map<String, String> out = new HashMap<String, String>();
        for (BasicEvent e : events) {
            if (!e.getSourceGUID().equalsIgnoreCase("0x0000000000000000")) {
                out.put(e.getSourceGUID(), e.getSourceName());
            }
        }
        return out;
    }

    /**
     * Get all destination guids and the names connected to them.
     * @return A map with guid as key and name as value.
     */
    @Override
    public Map<String, String> getDestinationGuidsAndNames() {
        List<BasicEvent> events = getEvents();
        Map<String, String> out = new HashMap<String, String>();
        for (BasicEvent e : events) {
            if (!e.getDestinationGUID().equalsIgnoreCase("0x0000000000000000")) {
                out.put(e.getDestinationGUID(), e.getDestinationName());
            }
        }
        return out;
    }

    /**
     * Get the starting time from the events in this collection
     * @return The starting time
     */
    @Override
    public double getStartTime() {
        List<BasicEvent> evs = getEvents();
        double time = Double.MAX_VALUE;
        for(BasicEvent be : evs) {
            if (be.time < time) {
                time = be.time;
            }
        }
        return time;
    }

    /**
     * Find the first event in this fight
     * @return The first event
     */
    @Override
    public BasicEvent getStartEvent() {
        List<BasicEvent> events = getEvents();
        double time = Double.MAX_VALUE;
        BasicEvent e = new UnknownEvent();
        for(BasicEvent be : events) {
            if (be.time<time) {
                time = be.time;
                e = be;
            }
        }
        return e;
    }

    /**
     * Find the last time in this fight.
     * @return The last time
     */
    @Override
    public double getEndTime() {
        List<BasicEvent> events = getEvents();
        double time = Double.MIN_VALUE;
        for(BasicEvent be : events) {
            if (be.time>time) {
                time = be.time;
            }
        }
        return time;
    }

    /**
     * Get the active duration of the fight.
     * @return The duration
     */
    @Override
    public double getActiveDuration() {
        return activeDuration;
    }

    @Override
    public final void setActiveDuration(double activeDuration) {
        this.activeDuration = activeDuration;
    }

    /**
     * Create the active duration of this EventCollection. If a pause larger than maxBreakTime is present in the
     * fight then that time is not included in the total time.
     * @param maxBreakTime The maximum break time
     */
    @Override
    public void createActiveDuration(double maxBreakTime, double globalCooldown) {
        List<BasicEvent> events = getEvents();
        Collections.sort(events);
        double totalTime = 0;
        double periodStart = 0;
        double periodEnd = 0;
        boolean started = false;
        for (BasicEvent e : events) {
            if (!started) {
                periodStart = e.time;
                periodEnd = e.time;
                started = true;
            }
            if (e.time - periodEnd > maxBreakTime) {
                totalTime += periodEnd - periodStart + 2*globalCooldown;
                periodStart = e.time;
                periodEnd = e.time;
            } else {
                periodEnd = e.time;
            }
        }
        if (periodEnd - periodStart > 0) {
            totalTime += periodEnd - periodStart + 2*globalCooldown;
        }
        activeDuration = totalTime;
    }

    /**
     * Get the total melee damage done from all events
     * @return The damage done
     */
    @Override
    public long getTotalMeleeDamage() {
        int k;
        long damage = 0;
        List<SwingDamageEvent> mde = getMeleeDamageEvents();
        for (k=0; k<mde.size(); k++) {
            damage = damage + mde.get(k).getDamage();
        }
        return damage;
    }

    /**
     * Get the total spell damage done from all events
     * @return The damage done
     */
    @Override
    public long getTotalSpellDamage() {
        int k;
        long damage = 0;
        List<SpellDamageEvent> sde = getSpellDamageEvents();
        for (k=0; k<sde.size(); k++) {
            damage = damage + sde.get(k).getDamage();
        }
        return damage;
    }

    /**
     * Get all swing damage events
     * @return The swing damage events
     */
    @Override
    public List<SwingDamageEvent> getMeleeDamageEvents() {
        List<BasicEvent> events = getEvents();
        int k;
        List<SwingDamageEvent> outEvents = new ArrayList<SwingDamageEvent>();
        for (k=0; k<events.size(); k++) {
            if(events.get(k) instanceof SwingDamageEvent) {
                outEvents.add((SwingDamageEvent)events.get(k));
            }
        }
        return outEvents;
    }

    /**
     * Get all spell damage events
     * @return The spell damage events
     */
    @Override
    public List<SpellDamageEvent> getSpellDamageEvents() {
        List<BasicEvent> events = getEvents();
        int k;
        List<SpellDamageEvent> outEvents = new ArrayList<SpellDamageEvent>();
        for (k=0; k<events.size(); k++) {
            if(events.get(k) instanceof SpellDamageEvent) {
                outEvents.add((SpellDamageEvent)events.get(k));
            }
        }
        return outEvents;
    }

    /**
     * Set the events in the list as the events this collection should contain.
     * All current events are lost.
     * @param events
     */
    @Override
    public void setEvents(EventCollection events) {
        setEvents(events.getEvents());
    }

    /**
     * Get all log events that are inside any of the time periods that are supplied
     * This method cannot be overriden so only the basic EventCollection events are returned.
     * @param tps The time periods
     * @return An array of the events that were found
     */
    @Override
    public final <T extends BasicEvent> List<BasicEvent> getEvents(List<TimePeriod> tps) {
        return getEvents(getEvents(), tps);
    }

    /**
     * Get all log events that are inside any of the time periods that are supplied
     * @param inEvs An array of source events to search in
     * @param tps The time periods
     * @return An array of the events that were found
     */
    public static <T extends BasicEvent> List<BasicEvent> getEvents(List<T> inEvs, List<TimePeriod> tps) {
        List<BasicEvent> evs = new ArrayList<BasicEvent>();
        for (TimePeriod tp : tps) {
            for (BasicEvent e : inEvs) {
                if (e.time >= tp.startTime && e.time <= tp.endTime) {
                    evs.add(e);
                }
            }
        }
        return evs;
    }

    public static int getType(BasicEvent be) {
        if (checkIfSwingTypesMatch(be, TYPE_SWING_DAMAGE)) {
            return TYPE_SWING_DAMAGE;
        }
        if (checkIfDamageTypesMatch(be, TYPE_SPELL_DAMAGE_DIRECT)) {
            return TYPE_SPELL_DAMAGE_DIRECT;
        }
        if (checkIfDamageTypesMatch(be, TYPE_SPELL_DAMAGE_PERIODIC)) {
            return TYPE_SPELL_DAMAGE_PERIODIC;
        }
        if (checkIfDamageTypesMatch(be, TYPE_RANGED_DAMAGE)) {
            return TYPE_RANGED_DAMAGE;
        }
        if (checkIfPowerTypesMatch(be, TYPE_SPELL_DRAIN_DIRECT)) {
            return TYPE_SPELL_DRAIN_DIRECT;
        }
        if (checkIfPowerTypesMatch(be, TYPE_SPELL_DRAIN_PERIODIC)) {
            return TYPE_SPELL_DRAIN_PERIODIC;
        }
        if (checkIfPowerTypesMatch(be, TYPE_SPELL_ENERGIZE_DIRECT)) {
            return TYPE_SPELL_ENERGIZE_DIRECT;
        }
        if (checkIfPowerTypesMatch(be, TYPE_SPELL_ENERGIZE_PERIODIC)) {
            return TYPE_SPELL_ENERGIZE_PERIODIC;
        }
        if (checkIfHealingTypesMatch(be, TYPE_SPELL_HEALING_DIRECT)) {
            return TYPE_SPELL_HEALING_DIRECT;
        }
        if (checkIfHealingTypesMatch(be, TYPE_SPELL_HEALING_PERIODIC)) {
            return TYPE_SPELL_HEALING_PERIODIC;
        }
        if (checkIfPowerTypesMatch(be, TYPE_SPELL_LEECH_DIRECT)) {
            return TYPE_SPELL_LEECH_DIRECT;
        }
        if (checkIfPowerTypesMatch(be, TYPE_SPELL_LEECH_PERIODIC)) {
            return TYPE_SPELL_LEECH_PERIODIC;
        }
        if (checkIfEnvironmentalTypesMatch(be, TYPE_ENVIRONMENTAL_DAMAGE)) {
            return TYPE_ENVIRONMENTAL_DAMAGE;
        }
        return TYPE_UNKNOWN;
    }

    /**
     * Check if any of the types matches.
     * @param be The event
     * @param type The type to check for
     * @return true if match, false otherwise
     */
    public static boolean checkIfAnyTypeMatch(BasicEvent be, int type) {
        boolean found = false;
        found = found | checkIfDamageTypesMatch(be, type);
        found = found | checkIfDamageMissTypesMatch(be, type);
        found = found | checkIfSwingTypesMatch(be, type);
        found = found | checkIfSwingMissTypesMatch(be, type);
        found = found | checkIfEnvironmentalTypesMatch(be, type);
        found = found | checkIfHealingTypesMatch(be, type);
        found = found | checkIfPowerTypesMatch(be, type);
        return found;
    }

    /**
     * Check if an event matches any type of spell event (SkillInterface)
     * @param be The event
     * @param type The type to check for
     * @return true if match, false otherwise
     */
    public static boolean checkIfAnySpellMatch(BasicEvent be, int type) {
        boolean found = false;
        if ((type & TYPE_ANY_SPELL) != 0) {
            if (be instanceof SkillInterface) {
                found = true;
            }
        }
        return found;
    }

    /**
     * Check if an event matches a certain type of damage miss type
     * @param be The event
     * @param type The damage type to check for
     * @return true if match, false otherwise
     */
    public static boolean checkIfDamageMissTypesMatch(BasicEvent be, int type) {
        boolean foundMiss = false;
        if ((type & TYPE_RANGED_DAMAGE) != 0) {
            if (be instanceof RangedMissedEvent) {
                foundMiss = true;
            }
        }
        if ((type & TYPE_SPELL_DAMAGE) != 0) {
            if (be instanceof SpellMissedEvent) {
                foundMiss = true;
            }
        }
        if ((type & TYPE_SPELL_DAMAGE_DIRECT) != 0) {
            if (be instanceof SpellMissedEvent) {
                foundMiss = true;
            }
        }
        if ((type & TYPE_SPELL_DAMAGE_PERIODIC) != 0) {
            if (be instanceof SpellMissedEvent) {
                foundMiss = true;
            }
        }
        return foundMiss;
    }

    /**
     * Check if an event matches a certain type of swing damage miss type
     * @param be The event
     * @param type The damage type to check for
     * @return true if match, false otherwise
     */
    public static boolean checkIfSwingMissTypesMatch(BasicEvent be, int type) {
        boolean foundMiss = false;
        if ((type & TYPE_SWING_DAMAGE) != 0) {
            if (be instanceof SwingMissedEvent) {
                foundMiss = true;
            }
        }
        return foundMiss;
    }

    /**
     * Check if an event matches a certain type of damage type
     * @param be The event
     * @param type The damage type to check for
     * @return true if match, false otherwise
     */
    public static boolean checkIfDamageTypesMatch(BasicEvent be, int type) {
        boolean found = false;
        if ((type & TYPE_RANGED_DAMAGE) != 0) {
            if (be instanceof RangedDamageEvent) {
                found = true;
            }
        }
        if ((type & TYPE_SPELL_DAMAGE) != 0) {
            if (be instanceof SpellDamageEvent) {
                found = true;
            }
        }
        if ((type & TYPE_SPELL_DAMAGE_DIRECT) != 0) {
            if (be instanceof DirectSpellDamageEvent) {
                found = true;
            }
        }
        if ((type & TYPE_SPELL_DAMAGE_PERIODIC) != 0) {
            if (be instanceof PeriodicSpellDamageEvent) {
                found = true;
            }
        }
        return found;
    }

    /**
     * Check if an event matches a certain type of swing damage type
     * @param be The event
     * @param type The damage type to check for
     * @return true if match, false otherwise
     */
    public static boolean checkIfSwingTypesMatch(BasicEvent be, int type) {
        boolean found = false;
        if ((type & TYPE_SWING_DAMAGE) != 0) {
            if (be instanceof SwingDamageEvent) {
                found = true;
            }
        }
        return found;
    }

    /**
     * Check if an event matches a certain type of swing damage type
     * @param be The event
     * @param type The damage type to check for
     * @return true if match, false otherwise
     */
    public static boolean checkIfEnvironmentalTypesMatch(BasicEvent be, int type) {
        boolean found = false;
        if ((type & TYPE_ENVIRONMENTAL_DAMAGE) != 0) {
            if (be instanceof EnvironmentalDamageEvent) {
                found = true;
            }
        }
        return found;
    }

    /**
     * Check if an event matches a certain type of healing type
     * @param be The event
     * @param type The healing type to check for
     * @return true if match, false otherwise
     */
    public static boolean checkIfHealingTypesMatch(BasicEvent be, int type) {
        boolean found = false;
        if ((type & TYPE_SPELL_HEALING) != 0) {
            if (be instanceof SpellHealingEvent) {
                found = true;
            }
        }
        if ((type & TYPE_SPELL_HEALING_DIRECT) != 0) {
            if (be instanceof DirectHealingEvent) {
                found = true;
            }
        }
        if ((type & TYPE_SPELL_HEALING_PERIODIC) != 0) {
            if (be instanceof PeriodicHealingEvent) {
                found = true;
            }
        }
        return found;
    }

    /**
     * Check if an event matches a certain type of power type
     * @param be The event
     * @param type The power type to check for
     * @return true if match, false otherwise
     */
    public static boolean checkIfPowerTypesMatch(BasicEvent be, int type) {
        boolean found = false;
        if ((type & TYPE_SPELL_LEECH) != 0) {
            if (be instanceof SpellLeechEvent) {
                found = true;
            }
        }
        if ((type & TYPE_SPELL_LEECH_DIRECT) != 0) {
            if (be instanceof DirectSpellLeechEvent) {
                found = true;
            }
        }
        if ((type & TYPE_SPELL_LEECH_PERIODIC) != 0) {
            if (be instanceof PeriodicSpellLeechEvent) {
                found = true;
            }
        }
        if ((type & TYPE_SPELL_DRAIN) != 0) {
            if (be instanceof SpellDrainEvent) {
                found = true;
            }
        }
        if ((type & TYPE_SPELL_DRAIN_DIRECT) != 0) {
            if (be instanceof DirectSpellDrainEvent) {
                found = true;
            }
        }
        if ((type & TYPE_SPELL_DRAIN_PERIODIC) != 0) {
            if (be instanceof PeriodicSpellDrainEvent) {
                found = true;
            }
        }
        if ((type & TYPE_SPELL_ENERGIZE) != 0) {
            if (be instanceof SpellEnergizeEvent) {
                found = true;
            }
        }
        if ((type & TYPE_SPELL_ENERGIZE_DIRECT) != 0) {
            if (be instanceof DirectSpellEnergizeEvent) {
                found = true;
            }
        }
        if ((type & TYPE_SPELL_ENERGIZE_PERIODIC) != 0) {
            if (be instanceof PeriodicSpellEnergizeEvent) {
                found = true;
            }
        }
        return found;
    }

    /**
     * Make overhealing numbers for all Healing events in the list.
     * @param evs The list of events.
     */
    public static void makeOverhealing(List<BasicEvent> evs, double timeWithoutDamageEventsBeforeDeficitReset) {
        Map<String, FightParticipant> healingTargetsMap = new HashMap<String, FightParticipant>();
        for (BasicEvent le : evs) {
            //Energize event. Handle life taps for health deficit. Since the damage part is not shown
            //it is assumed that the health removed is the same as the mana gained.
            //Damage event, then change healthDeficit
            energizeIf:
            if (le instanceof SpellEnergizeEvent) {
                SpellEnergizeEvent e = (SpellEnergizeEvent) le;
                XmlInfoParser xp = XmlInfoParser.getInstance();
                String lifeTapName = xp.getSpellName("Life Tap");
                if (!e.getSkillName().equalsIgnoreCase(lifeTapName)) {
                    break energizeIf;
                }

                boolean found = false;
                FightParticipant foundTarget = healingTargetsMap.get(e.getDestinationGUID());
                if (foundTarget != null) {
                    found = true;
                }
                if (found) {
                    foundTarget.setHealthDeficit(foundTarget.getHealthDeficit() - e.getAmount());
                } else {
                    //Only add new target if its a pet or player
                    if ((e.getDestinationFlags() & (Constants.FLAGS_OBJECT_TYPE_PET + Constants.FLAGS_OBJECT_TYPE_PLAYER)) != 0) {
                        foundTarget = new FightParticipant();
                        foundTarget.setGUID(e.getDestinationGUID());
                        foundTarget.setName(e.getDestinationName());
                        foundTarget.setHealthDeficit(foundTarget.getHealthDeficit() - e.getAmount());
                        healingTargetsMap.put(foundTarget.getSourceGUID(), foundTarget);
                    }
                }
            }

            //Damage event, then change healthDeficit
            if (le instanceof DamageEvent) {
                DamageEvent e = (DamageEvent) le;

                boolean found = false;
                FightParticipant foundTarget = healingTargetsMap.get(e.getDestinationGUID());
                if (foundTarget != null) {
                    found = true;
                    foundTarget.setLastActionDoneForOverhealingCalcs(e.time);
                }
                FightParticipant actionTarget = healingTargetsMap.get(e.getSourceGUID());
                if (actionTarget != null) {
                    actionTarget.setLastActionDoneForOverhealingCalcs(e.time);
                }
                if (found) {
                    if (Math.abs(e.time - foundTarget.getLastActionDoneForOverhealingCalcs()) > timeWithoutDamageEventsBeforeDeficitReset) {
                        foundTarget.setHealthDeficit(0);
                    }
                    foundTarget.setHealthDeficit(foundTarget.getHealthDeficit() - e.getDamage());
                } else {
                    //Only add new target if its a pet or player
                    if ((e.getDestinationFlags() & (Constants.FLAGS_OBJECT_TYPE_PET + Constants.FLAGS_OBJECT_TYPE_PLAYER)) != 0) {
                        foundTarget = new FightParticipant();
                        foundTarget.setGUID(e.getDestinationGUID());
                        foundTarget.setName(e.getDestinationName());
                        foundTarget.setHealthDeficit(foundTarget.getHealthDeficit() - e.getDamage());
                        healingTargetsMap.put(foundTarget.getSourceGUID(), foundTarget);
                    }
                }
                if (foundTarget != null) {
                    e.setHealthDeficit(foundTarget.getHealthDeficit());
                }
            }

            //Healing event, then change healthDeficit and see if overhealing was performed
            if (le instanceof HealingEvent) {
                HealingEvent e = (HealingEvent) le;
                e.setOverhealingCalculated(0);
                boolean found = false;
                FightParticipant foundTarget = healingTargetsMap.get(e.getDestinationGUID());
                if (foundTarget != null) {
                    found = true;
                    foundTarget.setLastActionDoneForOverhealingCalcs(e.time);
                }
                FightParticipant actionTarget = healingTargetsMap.get(e.getSourceGUID());
                if (actionTarget != null) {
                    actionTarget.setLastActionDoneForOverhealingCalcs(e.time);
                }

                if (found) {
                    if (Math.abs(e.time - foundTarget.getLastActionDoneForOverhealingCalcs()) > timeWithoutDamageEventsBeforeDeficitReset) {
                        foundTarget.setHealthDeficit(0);
                    }
                } else {
                    //Only add new target if its a pet or player
                    if ((e.getDestinationFlags() & (Constants.FLAGS_OBJECT_TYPE_PET + Constants.FLAGS_OBJECT_TYPE_PLAYER)) != 0) {
                        foundTarget = new FightParticipant();
                        foundTarget.setGUID(e.getDestinationGUID());
                        foundTarget.setName(e.getDestinationName());
                        healingTargetsMap.put(foundTarget.getSourceGUID(), foundTarget);
                    }
                }

                if (foundTarget != null) {
                    if (e.getOverhealing() < 0) {
                        //Old style log without overhealing included.
                        foundTarget.setHealthDeficit(foundTarget.getHealthDeficit() + e.getHealing());
                        if (foundTarget.getHealthDeficit() > 0) {
                            e.setOverhealingCalculated(foundTarget.getHealthDeficit());
                            foundTarget.setHealthDeficit(0);
                        }
                    } else {
                        //New style log with overhealing included.
                        foundTarget.setHealthDeficit(foundTarget.getHealthDeficit() + e.getHealing());
                        e.setOverhealingCalculated(e.getOverhealing());
                        if (foundTarget.getHealthDeficit() > 0 || e.getOverhealing() > 0) {
                            foundTarget.setHealthDeficit(0);
                        }
                    }
                    e.setHealthDeficit(foundTarget.getHealthDeficit());
                }
            }

            spellCastSuccessIf:
            if (le instanceof SpellCastSuccessEvent) {
                final int bloodrageDeficit = 720;
                SpellCastSuccessEvent e = (SpellCastSuccessEvent) le;
                XmlInfoParser xp = XmlInfoParser.getInstance();
                String bloodrageName = xp.getSpellName("Bloodrage");
                if (!e.getSkillName().equalsIgnoreCase(bloodrageName)) {
                    break spellCastSuccessIf;
                }
                boolean found = false;
                FightParticipant foundTarget = healingTargetsMap.get(e.getSourceGUID());
                if (foundTarget != null) {
                    found = true;
                }
                if (found) {
                    foundTarget.setHealthDeficit(foundTarget.getHealthDeficit() - bloodrageDeficit);
                } else {
                    //Only add new target if its a pet or player
                    if ((e.getDestinationFlags() & (Constants.FLAGS_OBJECT_TYPE_PET + Constants.FLAGS_OBJECT_TYPE_PLAYER)) != 0) {
                        foundTarget = new FightParticipant();
                        foundTarget.setGUID(e.getSourceGUID());
                        foundTarget.setName(e.getSourceName());
                        foundTarget.setHealthDeficit(foundTarget.getHealthDeficit() - bloodrageDeficit);
                        healingTargetsMap.put(foundTarget.getSourceGUID(), foundTarget);
                    }
                }
            }

            if (le instanceof UnitDiedEvent || le instanceof UnitDestroyedEvent) {
                StatusEvent e = (StatusEvent) le;

                boolean found = false;
                FightParticipant foundTarget = healingTargetsMap.get(e.getDestinationGUID());
                if (foundTarget != null) {
                    found = true;
                }
                if (found) {
                    foundTarget.setHealthDeficit(0);
                } else {
                    //Only add new target if its a pet or player
                    if ((e.getDestinationFlags() & (Constants.FLAGS_OBJECT_TYPE_PET + Constants.FLAGS_OBJECT_TYPE_PLAYER)) != 0) {
                        foundTarget = new FightParticipant();
                        foundTarget.setGUID(e.getDestinationGUID());
                        foundTarget.setName(e.getDestinationName());
                        foundTarget.setHealthDeficit(0);
                        healingTargetsMap.put(foundTarget.getSourceGUID(), foundTarget);
                    }
                }
            }

        }

    }

    /**
     * Calculate damage for a certain spell
     * @param si The spell info
     * @return The result
     */
    @Override
    public AmountAndCount damage(SpellInfo si) {
        return damage(si, TYPE_ALL_DAMAGE, UNIT_ALL);
    }

    /**
     * Calculate damage for a certain spell
     * @param si The spell info
     * @param type The type of damage: TYPE_RANGED_DAMAGE, TYPE_SPELL_DAMAGE
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL, only UNIT_PLAYER and UNIT_ALL works in EventCollection
     * @param sGUID null for no source GUID check, otherwise only the specified source GUID is used
     * @param dGUID null for no destination GUID check, otherwise only the specified destination GUID is used
     * @return The result
     */
    @Override
    public AmountAndCount damage(SpellInfo si, int type, int unit) {
        List<BasicEvent> events = getEvents();
        AmountAndCount a = new AmountAndCount();
        if ((unit & UNIT_PLAYER) != 0) {
            for (BasicEvent be : events) {
                boolean found = checkIfDamageTypesMatch(be, type);
                boolean foundMiss = checkIfDamageMissTypesMatch(be, type);

                if (found) {
                    DamageEvent e = (DamageEvent) be;
                    SkillInterface i = (SkillInterface) be;
                    if (i.getSkillID() == si.spellID) {
                        //a.addHitType(e.getDamage(), e.isCrit(),e.isCrushing());
                        a.addDamageHitType(e);
                    }
                }
                if (foundMiss) {
                    DamageEvent e = (DamageEvent) be;
                    SkillInterface i = (SkillInterface) be;
                    if (i.getSkillID() == si.spellID) {
                        a.addMissType(e);
                    }
                }
            }
        }

        return a;
    }

    /**
     * Get total damage.
     * The school flags can be found in BasicEvent
     * @param damageSchoolFlags The damage schools to process
     * @param type The types can be TYPE_RANGED_DAMAGE, TYPE_SPELL_DAMAGE, TYPE_SWING_DAMAGE or a bit combination of those
     * @return The results
     */
    @Override
    public AmountAndCount totalDamage(int damageSchoolFlags, int type) {
        return totalDamage(damageSchoolFlags, type, UNIT_ALL);
    }

    /**
     * Get total damage.
     * The school flags can be found in BasicEvent
     * @param damageSchoolFlags The damage schools to process
     * @param type The types can be TYPE_RANGED_DAMAGE, TYPE_SPELL_DAMAGE, TYPE_SWING_DAMAGE or a bit combination of those
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL, only UNIT_PLAYER and UNIT_ALL works in EventCollection
     * @return The results
     */
    @Override
    public AmountAndCount totalDamage(int damageSchoolFlags, int type, int unit) {
        return totalDamage(damageSchoolFlags, type, unit, null);
    }

    /**
     * Get total damage.
     * The school flags can be found in BasicEvent
     * @param damageSchoolFlags The damage schools to process
     * @param type The types can be TYPE_RANGED_DAMAGE, TYPE_SPELL_DAMAGE, TYPE_SWING_DAMAGE or a bit combination of those
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL, only UNIT_PLAYER and UNIT_ALL works in EventCollection
     * @param name The wanted name for the unit, or null for any
     * @return The results
     */
    @Override
    public AmountAndCount totalDamage(int damageSchoolFlags, int type, int unit, String name) {
        return totalDamage(damageSchoolFlags, type, unit, name, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    /**
     * Get total damage during a certain time frame.
     * The school flags can be found in BasicEvent
     * @param damageSchoolFlags The damage schools to process
     * @param type The types can be TYPE_RANGED_DAMAGE, TYPE_SPELL_DAMAGE, TYPE_SWING_DAMAGE or a bit combination of those
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL, only UNIT_PLAYER and UNIT_ALL works in EventCollection
     * @param name The wanted name for the unit, or null for any
     * @param startTime The start time
     * @param endTime The end time
     * @return The results
     */
    @Override
    public AmountAndCount totalDamage(int damageSchoolFlags, int type, int unit, String name, double startTime, double endTime) {
        List<BasicEvent> events = getEvents(startTime, endTime);
        AmountAndCount a = new AmountAndCount();
        if ((unit & UNIT_PLAYER) != 0) {
            for (BasicEvent be : events) {
                if (be.time >= startTime && be.time <= endTime) {
                    boolean found = false;
                    boolean foundMiss = false;
                    found = found | checkIfDamageTypesMatch(be, type);
                    if (!found) {
                        found = found | checkIfSwingTypesMatch(be, type);
                    }
                    if (!found) {
                        found = found | checkIfEnvironmentalTypesMatch(be, type);
                    }
                    foundMiss = foundMiss | checkIfDamageMissTypesMatch(be, type);
                    if (!foundMiss) {
                        foundMiss = foundMiss | checkIfSwingMissTypesMatch(be, type);
                    }

                    if (found) {
                        if (name != null) {
                            if (!be.getSourceName().equalsIgnoreCase(name)) {
                                continue;
                            }
                        }
                        DamageEvent e = (DamageEvent) be;
                        if ((e.getSchool() & damageSchoolFlags) > 0) {
                            //a.addHitType(e.getDamage(), e.isCrit(),e.isCrushing());
                            a.addDamageHitType(e);
                        }
                    }
                    if (foundMiss) {
                        if (name != null) {
                            if (!be.getSourceName().equalsIgnoreCase(name)) {
                                continue;
                            }
                        }
                        DamageEvent e = (DamageEvent) be;
                        if ((e.getSchool() & damageSchoolFlags) > 0) {
                            a.addMissType(e);
                        }
                    }
                }
            }
        }
        return a;
    }

    /**
     * Calculate healing for a certain spell
     * @param si The spell info
     * @return The result
     */
    @Override
    public AmountAndCount healing(SpellInfo si) {
        return healing(si, TYPE_ALL_HEALING, UNIT_ALL);
    }

    /**
     * Calculate healing for a certain spell
     * @param si The spell info
     * @param type The types can be TYPE_SPELL_HEALING, TYPE_SPELL_HEALING_DIRECT, TYPE_SPELL_HEALING_PERIODIC or a bit combination of those
     *          The type TYPE_SPELL_HEALING includes DIRECT and PERIODIC.
     * @return The result
     */
    @Override
    public AmountAndCount healing(SpellInfo si, int type, int unit) {
        List<BasicEvent> events = getEvents();
        AmountAndCount a = new AmountAndCount();
        if ((unit & UNIT_PLAYER) != 0) {
            for (BasicEvent be : events) {
                boolean found = checkIfHealingTypesMatch(be, type);

                if (found) {
                    SpellHealingEvent e = (SpellHealingEvent) be;
                    SkillInterface i = (SkillInterface) be;
                    if (i.getSkillID() == si.spellID) {
                        //a.addHitType(e.getHealing(), e.isCrit(), false);
                        //a.addOverAmountType(e.getOverhealingCalculated());
                        a.addHealingHitType(e);
                    }
                }
            }
        }

        return a;
    }

    /**
     * Get total healing.
     * The school flags can be found in BasicEvent
     * @param damageSchoolFlags The damage schools to process
     * @param type The types can be TYPE_SPELL_HEALING, TYPE_SPELL_HEALING_DIRECT, TYPE_SPELL_HEALING_PERIODIC or a bit combination of those
     *          The type TYPE_SPELL_HEALING includes DIRECT and PERIODIC.
     * @return
     */
    @Override
    public AmountAndCount totalHealing(int healingSchoolFlags, int type) {
        return totalHealing(healingSchoolFlags, type, UNIT_ALL, null, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    /**
     * Get total healing.
     * The school flags can be found in BasicEvent
     * @param damageSchoolFlags The damage schools to process
     * @param type The types can be TYPE_SPELL_HEALING, TYPE_SPELL_HEALING_DIRECT, TYPE_SPELL_HEALING_PERIODIC or a bit combination of those
     *          The type TYPE_SPELL_HEALING includes DIRECT and PERIODIC.
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL, only UNIT_PLAYER and UNIT_ALL works in EventCollection
     * @return
     */
    @Override
    public AmountAndCount totalHealing(int healingSchoolFlags, int type, int unit) {
        return totalHealing(healingSchoolFlags, type, unit, null, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    /**
     * Get total healing.
     * The school flags can be found in BasicEvent
     * @param damageSchoolFlags The damage schools to process
     * @param type The types can be TYPE_SPELL_HEALING, TYPE_SPELL_HEALING_DIRECT, TYPE_SPELL_HEALING_PERIODIC or a bit combination of those
     *          The type TYPE_SPELL_HEALING includes DIRECT and PERIODIC.
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL, only UNIT_PLAYER and UNIT_ALL works in EventCollection
     * @param name The wanted name for the unit, or null for any
     * @return
     */
    @Override
    public AmountAndCount totalHealing(int healingSchoolFlags, int type, int unit, String name) {
        return totalHealing(healingSchoolFlags, type, unit, name, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    /**
     * Get total healing for a certain time frame.
     * The school flags can be found in BasicEvent
     * @param damageSchoolFlags The damage schools to process
     * @param type The types can be TYPE_SPELL_HEALING, TYPE_SPELL_HEALING_DIRECT, TYPE_SPELL_HEALING_PERIODIC or a bit combination of those
     *          The type TYPE_SPELL_HEALING includes DIRECT and PERIODIC.
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL, only UNIT_PLAYER and UNIT_ALL works in EventCollection
     * @param name The wanted name for the unit, or null for any
     * @param startTime The start time
     * @param endTime The end time
     * @return
     */
    @Override
    public AmountAndCount totalHealing(int healingSchoolFlags, int type, int unit, String name, double startTime, double endTime) {
        List<BasicEvent> events = getEvents(startTime, endTime);
        AmountAndCount a = new AmountAndCount();
        if ((unit & UNIT_PLAYER) != 0) {
            for (BasicEvent be : events) {
                if (be.time >= startTime && be.time <= endTime) {
                    boolean found = checkIfHealingTypesMatch(be, type);
                    if (found) {
                        if (name != null) {
                            if (!be.getSourceName().equalsIgnoreCase(name)) {
                                continue;
                            }
                        }
                        SpellHealingEvent e = (SpellHealingEvent) be;
                        if ((e.getSchool() & healingSchoolFlags) > 0) {
                            //a.addHitType(e.getHealing(), e.isCrit(), false);
                            //a.addOverAmountType(e.getOverhealingCalculated());
                            a.addHealingHitType(e);
                        }
                    }
                }
            }
        }

        return a;
    }

    /**
     * Calculate overhealing for a certain spell
     * @param si The spell info
     * @return The result
     */
    @Override
    public AmountAndCount overHealing(SpellInfo si) {
        return overHealing(si, TYPE_ALL_HEALING, UNIT_ALL);
    }

    /**
     * Calculate overhealing for a certain spell
     * @param si The spell info
     * @param type The types can be TYPE_SPELL_HEALING, TYPE_SPELL_HEALING_DIRECT, TYPE_SPELL_HEALING_PERIODIC or a bit combination of those
     *          The type TYPE_SPELL_HEALING includes DIRECT and PERIODIC.
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL, only UNIT_PLAYER and UNIT_ALL works in EventCollection
     * @return The result
     */
    @Override
    public AmountAndCount overHealing(SpellInfo si, int type, int unit) {
        List<BasicEvent> events = getEvents();
        AmountAndCount a = new AmountAndCount();
        if ((unit & UNIT_PLAYER) != 0) {
            for (BasicEvent be : events) {
                boolean found = checkIfHealingTypesMatch(be, type);

                if (found) {
                    SpellHealingEvent e = (SpellHealingEvent) be;
                    SkillInterface i = (SkillInterface) be;
                    if (i.getSkillID() == si.spellID) {
                        a.addHitType(e.getOverhealingCalculated(), e.isCrit());
                    }
                }
            }
        }

        return a;
    }

    /**
     * Get total overhealing.
     * The school flags can be found in BasicEvent
     * @param damageSchoolFlags The damage schools to process
     * @param type The types can be TYPE_SPELL_HEALING, TYPE_SPELL_HEALING_DIRECT, TYPE_SPELL_HEALING_PERIODIC or a bit combination of those
     *          The type TYPE_SPELL_HEALING includes DIRECT and PERIODIC.
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL, only UNIT_PLAYER and UNIT_ALL works in EventCollection
     * @return
     */
    @Override
    public AmountAndCount totalOverHealing(int healingSchoolFlags, int type) {
        return totalOverHealing(healingSchoolFlags, type, UNIT_ALL);
    }

    /**
     * Get total overhealing.
     * The school flags can be found in BasicEvent
     * @param damageSchoolFlags The damage schools to process
     * @param type The types can be TYPE_SPELL_HEALING, TYPE_SPELL_HEALING_DIRECT, TYPE_SPELL_HEALING_PERIODIC or a bit combination of those
     *          The type TYPE_SPELL_HEALING includes DIRECT and PERIODIC.
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL
     * @return
     */
    @Override
    public AmountAndCount totalOverHealing(int healingSchoolFlags, int type, int unit) {
        return totalOverHealing(healingSchoolFlags, type, unit, null);
    }

    /**
     * Get total overhealing.
     * The school flags can be found in BasicEvent
     * @param damageSchoolFlags The damage schools to process
     * @param type The types can be TYPE_SPELL_HEALING, TYPE_SPELL_HEALING_DIRECT, TYPE_SPELL_HEALING_PERIODIC or a bit combination of those
     *          The type TYPE_SPELL_HEALING includes DIRECT and PERIODIC.
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL
     * @param name The wanted name for the unit, or null for any
     * @return
     */
    @Override
    public AmountAndCount totalOverHealing(int healingSchoolFlags, int type, int unit, String name) {
        return totalOverHealing(healingSchoolFlags, type, unit, name, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    /**
     * Get total overhealing for a certain time frame.
     * The school flags can be found in BasicEvent
     * @param damageSchoolFlags The damage schools to process
     * @param type The types can be TYPE_SPELL_HEALING, TYPE_SPELL_HEALING_DIRECT, TYPE_SPELL_HEALING_PERIODIC or a bit combination of those
     *          The type TYPE_SPELL_HEALING includes DIRECT and PERIODIC.
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL, only UNIT_PLAYER and UNIT_ALL works in EventCollection
     * @param name The wanted name for the unit, or null for any
     * @param startTime The start time
     * @param endTime The end time
     * @return
     */
    @Override
    public AmountAndCount totalOverHealing(int healingSchoolFlags, int type, int unit, String name, double startTime, double endTime) {
        List<BasicEvent> events = getEvents(startTime, endTime);
        AmountAndCount a = new AmountAndCount();
        if ((unit & UNIT_PLAYER) != 0) {
            //for (BasicEvent be : getEvents()) {
            for (BasicEvent be : events) {
                if (be.time >= startTime && be.time <= endTime) {
                    boolean found = checkIfHealingTypesMatch(be, type);
                    if (found) {
                        if (name != null) {
                            if (!be.getSourceName().equalsIgnoreCase(name)) {
                                continue;
                            }
                        }
                        SpellHealingEvent e = (SpellHealingEvent) be;
                        if ((e.getSchool() & healingSchoolFlags) > 0) {
                            a.addHitType(e.getOverhealingCalculated(), e.isCrit());
                        }
                    }
                }
            }
        }

        return a;
    }

    /**
     * Calculate amount for a certain power spell, using all type
     * @param si The spell info
     * @return The result
     */
    @Override
    public AmountAndCount power(SpellInfo si) {
        return power(si, TYPE_ALL_POWER, UNIT_ALL);
    }

        /**
     * Calculate amount for a certain power spell
     * @param si The spell info
     * @param type The types can be TYPE_SPELL_LEECH, TYPE_SPELL_LEECH_DIRECT, TYPE_SPELL_LEECH_PERIODIC,
     * TYPE_SPELL_DRAIN, TYPE_SPELL_DRAIN_DIRECT, TYPE_SPELL_DRAIN_PERIODIC,
     * TYPE_SPELL_ENERGIZE, TYPE_SPELL_ENERGIZE_DIRECT, TYPE_SPELL_ENERGIZE_PERIODIC
     * or a bit combination of those
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL, only UNIT_PLAYER and UNIT_ALL works in EventCollection
     * @return The result
     */
    @Override
    public AmountAndCount power(SpellInfo si, int type, int unit) {
        List<BasicEvent> events = getEvents();
        AmountAndCount a = new AmountAndCount();
        if ((unit & UNIT_PLAYER) != 0) {
            for (BasicEvent be : events) {
                boolean found = checkIfPowerTypesMatch(be, type);
                if (found) {
                    PowerEvent e = (PowerEvent) be;
                    SkillInterface i = (SkillInterface) be;
                    if (i.getSkillID() == si.spellID) {
                        //a.addHitType(e.getAmount(), false, false);
                        a.addPowerHitType(e);
                    }
                }
            }
        }

        return a;
    }

    /**
     * Get total power.
     * The school flags can be found in BasicEvent
     * @param damageSchoolFlags The damage schools to process
     * @param type The types can be TYPE_SPELL_LEECH, TYPE_SPELL_LEECH_DIRECT, TYPE_SPELL_LEECH_PERIODIC,
     * TYPE_SPELL_DRAIN, TYPE_SPELL_DRAIN_DIRECT, TYPE_SPELL_DRAIN_PERIODIC,
     * TYPE_SPELL_ENERGIZE, TYPE_SPELL_ENERGIZE_DIRECT, TYPE_SPELL_ENERGIZE_PERIODIC
     * or a bit combination of those
     * @return The results
     */
    @Override
    public AmountAndCount totalPower(int schoolFlags, int type) {
        return totalPower(schoolFlags, type, UNIT_ALL);
    }

    /**
     * Get total power.
     * The school flags can be found in BasicEvent
     * @param damageSchoolFlags The damage schools to process
     * @param type The types can be TYPE_SPELL_LEECH, TYPE_SPELL_LEECH_DIRECT, TYPE_SPELL_LEECH_PERIODIC,
     * TYPE_SPELL_DRAIN, TYPE_SPELL_DRAIN_DIRECT, TYPE_SPELL_DRAIN_PERIODIC,
     * TYPE_SPELL_ENERGIZE, TYPE_SPELL_ENERGIZE_DIRECT, TYPE_SPELL_ENERGIZE_PERIODIC
     * or a bit combination of those
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL, only UNIT_PLAYER and UNIT_ALL works in EventCollection
     * @return The results
     */
    @Override
    public AmountAndCount totalPower(int schoolFlags, int type, int unit) {
        return totalPower(schoolFlags, type, unit, null);
    }

    /**
     * Get total power.
     * The school flags can be found in BasicEvent
     * @param damageSchoolFlags The damage schools to process
     * @param type The types can be TYPE_SPELL_LEECH, TYPE_SPELL_LEECH_DIRECT, TYPE_SPELL_LEECH_PERIODIC,
     * TYPE_SPELL_DRAIN, TYPE_SPELL_DRAIN_DIRECT, TYPE_SPELL_DRAIN_PERIODIC,
     * TYPE_SPELL_ENERGIZE, TYPE_SPELL_ENERGIZE_DIRECT, TYPE_SPELL_ENERGIZE_PERIODIC
     * or a bit combination of those
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL, only UNIT_PLAYER and UNIT_ALL works in EventCollection
     * @param name The wanted name for the unit, or null for any
     * @return The results
     */
    @Override
    public AmountAndCount totalPower(int schoolFlags, int type, int unit, String name) {
        return totalPower(schoolFlags, type, unit, name, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    /**
     * Get total power for a certain time frame.
     * The school flags can be found in BasicEvent
     * @param damageSchoolFlags The damage schools to process
     * @param type The types can be TYPE_SPELL_LEECH, TYPE_SPELL_LEECH_DIRECT, TYPE_SPELL_LEECH_PERIODIC,
     * TYPE_SPELL_DRAIN, TYPE_SPELL_DRAIN_DIRECT, TYPE_SPELL_DRAIN_PERIODIC,
     * TYPE_SPELL_ENERGIZE, TYPE_SPELL_ENERGIZE_DIRECT, TYPE_SPELL_ENERGIZE_PERIODIC
     * or a bit combination of those
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL, only UNIT_PLAYER and UNIT_ALL works in EventCollection
     * @param name The wanted name for the unit, or null for any
     * @param startTime The start time
     * @param endTime The end time
     * @return The results
     */
    @Override
    public AmountAndCount totalPower(int schoolFlags, int type, int unit, String name, double startTime, double endTime) {
        List<BasicEvent> events = getEvents(startTime, endTime);
        AmountAndCount a = new AmountAndCount();
        if ((unit & UNIT_PLAYER) != 0) {
            //for (BasicEvent be : getEvents()) {
            for (BasicEvent be : events) {
                if (be.time >= startTime && be.time <= endTime) {
                    boolean found = checkIfPowerTypesMatch(be, type);
                    if (found) {
                        if (name != null) {
                            if (!be.getSourceName().equalsIgnoreCase(name)) {
                                continue;
                            }
                        }
                        PowerEvent e = (PowerEvent) be;
                        if ((e.getSchool() & schoolFlags) > 0) {
                            //a.addHitType(e.getAmount(), false, false);
                            a.addPowerHitType(e);
                        }
                    }
                }
            }
        }

        return a;
    }

    /**
     * Get the damage school flags used by this participant
     * @return The damage school flags
     */
    @Override
    public int getDamageSchoolFlags() {
        return getDamageSchoolFlags(UNIT_ALL);
    }

    /**
     * Get the damage school flags used by this participant
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL, only UNIT_PLAYER and UNIT_ALL works in EventCollection
     * @return The damage school flags
     */
    @Override
    public int getDamageSchoolFlags(int unit) {
        List<BasicEvent> events = getEvents();
        int schoolFlags = 0;
        if ((unit & UNIT_PLAYER) != 0) {
            for (BasicEvent be : events) {
                if (be instanceof DamageEvent) {
                    DamageEvent e = (DamageEvent) be;
                    schoolFlags = schoolFlags | e.getSchool();
                }
            }
        }

        return schoolFlags;
    }

    /**
     * Get IDs for a certain type of skills/spells
     * @param type The type of skill/spell, for example TYPE_SPELL_DAMAGE_DIRECT
     * @return
     */
    @Override
    public List<SpellInfo> getIDs(int type) {
        return getIDs(type, UNIT_ALL);
    }

    /**
     * Get IDs for a certain type of skills/spells
     * @param type The type of skill/spell, for example TYPE_SPELL_DAMAGE_DIRECT
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL, only UNIT_PLAYER and UNIT_ALL works in EventCollection
     * @return
     */
    @Override
    public List<SpellInfo> getIDs(int type, int unit) {
        return getIDs(type, unit, -1);
    }

    /**
     * Get IDs for a certain type of skills/spells
     * @param type The type of skill/spell, for example TYPE_SPELL_DAMAGE_DIRECT
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL, only UNIT_PLAYER and UNIT_ALL works in EventCollection
     * @param schools The wanted schools, if a spell matches any of the schools specified it will be used, set to -1 for any school.
     * @return
     */
    @Override
    public List<SpellInfo> getIDs(int type, int unit, int schools) {
        List<BasicEvent> events = getEvents();
        Set<SpellInfo> ids = new HashSet<SpellInfo>();
        if ((unit & UNIT_PLAYER) != 0) {
            for (BasicEvent be : events) {
                boolean found = false;
                found = found | checkIfAnySpellMatch(be, type);
                if(!found) {
                    found = found | checkIfDamageTypesMatch(be, type);
                }
                if(!found) {
                    found = found | checkIfHealingTypesMatch(be, type);
                }
                if(!found) {
                    found = found | checkIfPowerTypesMatch(be, type);
                }

                if (found) {
                    SkillInterface e = (SkillInterface) be;
                    SpellInfo beSi = new SpellInfo(e.getSkillID(), e.getSkillName(), e.getSchool());
                    beSi.powerType = e.getPowerType();
                    if (!ids.contains(beSi)) {
                        if (schools == -1) {
                            ids.add(beSi);
                        } else {
                            int schoolCheck = schools & beSi.school;
                            if (schoolCheck != 0) {
                                ids.add(beSi);
                            }
                        }
                    }
                }
            }
        }

        return new ArrayList<SpellInfo>(ids);
    }

    /**
     * Get IDs for a certain type of skills/spells
     * @param originalUnit The unit type to save in SpellInfoExtended
     * @param type The type of skill/spell, for example TYPE_SPELL_DAMAGE_DIRECT
     * @return
     */
    @Override
    public List<SpellInfoExtended> getIDsExtended(int originalUnit, int type) {
        return getIDsExtended(originalUnit, type, UNIT_ALL);
    }

    /**
     * Get IDs for a certain type of skills/spells
     * @param originalUnit The unit type to save in SpellInfoExtended
     * @param type The type of skill/spell, for example TYPE_SPELL_DAMAGE_DIRECT
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL, only UNIT_PLAYER and UNIT_ALL works in EventCollection
     * @return
     */
    @Override
    public List<SpellInfoExtended> getIDsExtended(int originalUnit, int type, int unit) {
        return getIDsExtended(originalUnit, type, unit, -1);
    }

    /**
     * Get IDs for a certain type of skills/spells
     * @param originalUnit The unit type to save in SpellInfoExtended
     * @param type The type of skill/spell, for example TYPE_SPELL_DAMAGE_DIRECT
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL, only UNIT_PLAYER and UNIT_ALL works in EventCollection
     * @param schools The wanted schools, if a spell matches any of the schools specified it will be used, set to -1 for any school.
     * @return
     */
    @Override
    public List<SpellInfoExtended> getIDsExtended(int originalUnit, int type, int unit, int schools) {
        List<BasicEvent> events = getEvents();
        Set<SpellInfoExtended> ids = new HashSet<SpellInfoExtended>();
        if ((unit & UNIT_PLAYER) != 0) {
            for (BasicEvent be : events) {
                boolean found = false;
                found = found | checkIfAnySpellMatch(be, type);
                if(!found) {
                    found = found | checkIfDamageTypesMatch(be, type);
                }
                if(!found) {
                    found = found | checkIfHealingTypesMatch(be, type);
                }
                if(!found) {
                    found = found | checkIfPowerTypesMatch(be, type);
                }

                if (found) {
                    SkillInterface e = (SkillInterface) be;
                    SpellInfoExtended beSi = new SpellInfoExtended(e, be, originalUnit);
                    if (!ids.contains(beSi)) {
                        if (schools == -1) {
                            ids.add(beSi);
                        } else {
                            int schoolCheck = schools & beSi.school;
                            if (schoolCheck != 0) {
                                ids.add(beSi);
                            }
                        }
                    }
                }
            }
        }

        return new ArrayList<SpellInfoExtended>(ids);
    }

    /**
     * Get spell ids of a certain type and unit from an existing list.
     * @param ids The list
     * @param type The wanted type
     * @param unit The wanted unit
     * @return A new list
     */
    public static List<SpellInfoExtended> getIDsExtendedFromList(Collection<SpellInfoExtended> ids, int type, int unit) {
        List<SpellInfoExtended> out = new ArrayList<SpellInfoExtended>();
        for (SpellInfoExtended si : ids) {
            if ((si.unit & unit) != 0) {
                boolean found = false;
                found = found | checkIfAnySpellMatch(si.ev, type);
                if(!found) {
                    found = found | checkIfDamageTypesMatch(si.ev, type);
                }
                if(!found) {
                    found = found | checkIfHealingTypesMatch(si.ev, type);
                }
                if(!found) {
                    found = found | checkIfPowerTypesMatch(si.ev, type);
                }
                if (found) {
                    out.add(si);
                }
            }
        }
        return out;
    }

    /**
     * Get the active damage+healing+power time for this collection. Requires the collection events to be sorted.
     * @param maxBreakTime The maximum time with no damage
     * @param globalCooldown 2*globalCooldown is added to each coherent period of time because spells and abilities takes time to use.
     * @param startTime The start time to consider
     * @param endTime The end time to consider
     * @return The accumulated time that has activity
     */
    @Override
    public double getActiveTime(double maxBreakTime, double globalCooldown, double startTime, double endTime) {
        List<BasicEvent> events = getEvents(startTime, endTime);
        Collections.sort(events);
        double totalTime = 0;
        double periodStart = 0;
        double periodEnd = 0;
        boolean started = false;
        //for (BasicEvent be : getEvents()) {
        for (BasicEvent e : events) {
            if (e instanceof DamageEvent || e instanceof HealingEvent || e instanceof PowerEvent) {
                if (!started) {
                    periodStart = e.time;
                    periodEnd = e.time;
                    started = true;
                }
                if (e.time >= startTime && e.time <= endTime) {
                    if (e.time - periodEnd > maxBreakTime) {
                        totalTime += periodEnd - periodStart + 2*globalCooldown;
                        periodStart = e.time;
                        periodEnd = e.time;
                    } else {
                        periodEnd = e.time;
                    }
                }
            }
        }
        if (periodEnd - periodStart > 0) {
            totalTime += periodEnd - periodStart + 2*globalCooldown;
        }
        return totalTime;
    }

    /**
     * Get the active damage time for this collection. Requires the collection events to be sorted.
     * @param maxBreakTime The maximum time with no damage
     * @param globalCooldown 2*globalCooldown is added to each coherent period of damage time because spells and abilities takes time to use.
     * @param startTime The start time to consider
     * @param endTime The end time to consider
     * @return The accumulated time that has activity
     */
    @Override
    public double getActiveDamageTime(double maxBreakTime, double globalCooldown, double startTime, double endTime) {
        List<BasicEvent> events = getEvents(startTime, endTime);
        double totalTime = 0;
        double periodStart = 0;
        double periodEnd = 0;
        boolean started = false;
        //for (BasicEvent be : getEvents()) {
        for (BasicEvent be : events) {
            if (be instanceof DamageEvent) {
                DamageEvent e = (DamageEvent) be;
                if (!started) {
                    periodStart = e.time;
                    periodEnd = e.time;
                    started = true;
                }
                if (be.time >= startTime && be.time <= endTime) {
                    if (e.time - periodEnd > maxBreakTime) {
                        totalTime += periodEnd - periodStart + 2*globalCooldown;
                        periodStart = e.time;
                        periodEnd = e.time;
                    } else {
                        periodEnd = e.time;
                    }
                }
            }
        }
        if (periodEnd - periodStart > 0) {
            totalTime += periodEnd - periodStart + 2*globalCooldown;
        }
        return totalTime;
    }

    /**
     * Get the active healing time for this collection. Requires the collection events to be sorted.
     * @param maxBreakTime The maximum time with no healing
     * @param globalCooldown 2*globalCooldown is added to each coherent period of healing time because spells and abilities takes time to use.
     * @return The accumulated time that has activity
     */
    @Override
    public double getActiveHealingTime(double maxBreakTime, double globalCooldown, double startTime, double endTime) {
        List<BasicEvent> events = getEvents(startTime, endTime);
        double totalTime = 0;
        double periodStart = 0;
        double periodEnd = 0;
        boolean started = false;
        //for (BasicEvent be : getEvents()) {
        for (BasicEvent be : events) {
            if (be instanceof HealingEvent) {
                HealingEvent e = (HealingEvent) be;
                if (!started) {
                    periodStart = e.time;
                    periodEnd = e.time;
                    started = true;
                }
                if (be.time >= startTime && be.time <= endTime) {
                    if (e.time - periodEnd > maxBreakTime) {
                        totalTime += periodEnd - periodStart + 2*globalCooldown;
                        periodStart = e.time;
                        periodEnd = e.time;
                    } else {
                        periodEnd = e.time;
                    }
                }
            }
        }
        if (periodEnd - periodStart > 0) {
            totalTime += periodEnd - periodStart + 2*globalCooldown;
        }
        return totalTime;
    }

    /**
     * Get the active power event time for this collection. Requires the collection events to be sorted.
     * @param maxBreakTime The maximum time with no power events
     * @param globalCooldown 2*globalCooldown is added to each coherent period of power time because spells and abilities takes time to use.
     * @return The accumulated time that has activity
     */
    @Override
    public double getActivePowerTime(double maxBreakTime, double globalCooldown, double startTime, double endTime) {
        List<BasicEvent> events = getEvents(startTime, endTime);
        Collections.sort(events);
        double totalTime = 0;
        double periodStart = 0;
        double periodEnd = 0;
        boolean started = false;
        //for (BasicEvent be : getEvents()) {
        for (BasicEvent be : events) {
            if (be instanceof PowerEvent) {
                PowerEvent e = (PowerEvent) be;
                if (!started) {
                    periodStart = e.time;
                    periodEnd = e.time;
                    started = true;
                }
                if (be.time >= startTime && be.time <= endTime) {
                    if (e.time - periodEnd > maxBreakTime) {
                        totalTime += periodEnd - periodStart + 2*globalCooldown;
                        periodStart = e.time;
                        periodEnd = e.time;
                    } else {
                        periodEnd = e.time;
                    }
                }
            }
        }
        if (periodEnd - periodStart > 0) {
            totalTime += periodEnd - periodStart + 2*globalCooldown;
        }
        return totalTime;
    }

    /**
     * Get events of a specific event class
     * This method cannot be overriden so only the basic EventCollection events are returned.
     * @param <T> Will be the class type that the object o has.
     * @param o A dummy object that selects the correct class to use.
     * @return An array of all event objects that has the class o has, or any sub class.
     */
    @Override
    public final <T> List<T> getEvents(T o) {
        List<BasicEvent> events = getEvents();
        Object testObj = (Object)o;
        List<T> out = (ArrayList<T>)new ArrayList();
        for (BasicEvent e : events) {
            if (testObj.getClass().isInstance(e)) {
                out.add((T)e);
            }
        }
        return out;

    }

    @Override
    public TimePeriod getTimePeriod() {
        double startTime = getStartTime();
        double endTime = getEndTime();
        TimePeriod tp = new TimePeriod(startTime, endTime);
        return tp;
    }

    @Override
    public boolean hasDoublets() {
        List<BasicEvent> events = getEvents();
        Collections.sort(events);
        for (int k=1; k<events.size(); k++) {
            if (events.get(k).equals(events.get(k-1))) {
                return true;
            }
        }
        return false;
    }

}
