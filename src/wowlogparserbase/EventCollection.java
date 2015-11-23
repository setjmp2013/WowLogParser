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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import wowlogparserbase.eventfilter.Filter;
import wowlogparserbase.events.BasicEvent;
import wowlogparserbase.events.damage.SpellDamageEvent;
import wowlogparserbase.events.damage.SwingDamageEvent;

/**
 *
 * @author racy
 */
public interface EventCollection {
    public static final int TYPE_UNKNOWN = 0;
    public static final int TYPE_SPELL_DAMAGE = 0x01;
    public static final int TYPE_SPELL_DAMAGE_DIRECT = 0x02;
    public static final int TYPE_SPELL_DAMAGE_PERIODIC = 0x04;
    public static final int TYPE_RANGED_DAMAGE = 0x08;
    public static final int TYPE_SPELL_HEALING = 0x10;
    public static final int TYPE_SPELL_HEALING_DIRECT = 0x20;
    public static final int TYPE_SPELL_HEALING_PERIODIC = 0x40;
    public static final int TYPE_SWING_DAMAGE = 0x100;
    public static final int TYPE_SPELL_LEECH = 0x1000;
    public static final int TYPE_SPELL_LEECH_DIRECT = 0x2000;
    public static final int TYPE_SPELL_LEECH_PERIODIC = 0x4000;
    public static final int TYPE_SPELL_DRAIN = 0x10000;
    public static final int TYPE_SPELL_DRAIN_DIRECT = 0x20000;
    public static final int TYPE_SPELL_DRAIN_PERIODIC = 0x40000;
    public static final int TYPE_SPELL_ENERGIZE = 0x100000;
    public static final int TYPE_SPELL_ENERGIZE_DIRECT = 0x200000;
    public static final int TYPE_SPELL_ENERGIZE_PERIODIC = 0x400000;
    public static final int TYPE_ENVIRONMENTAL_DAMAGE = 0x800000;

    public static final int TYPE_ALL_DAMAGE = TYPE_SPELL_DAMAGE | TYPE_RANGED_DAMAGE | TYPE_SWING_DAMAGE | TYPE_ENVIRONMENTAL_DAMAGE;
    public static final int TYPE_ALL_HEALING = TYPE_SPELL_HEALING;
    public static final int TYPE_ALL_POWER = TYPE_SPELL_LEECH | TYPE_SPELL_DRAIN | TYPE_SPELL_ENERGIZE;
    public static final int TYPE_ANY_SPELL = 0x1000000;

    //UNIT_PLAYER is the default unit. In the base class EventCollection that means all events.
    public static final int UNIT_PLAYER = 0x01;
    //UNIT_PET is for pets for a certain type of participant only. A pet participant itself is parsed with UNIT_PLAYER
    public static final int UNIT_PET = 0x02;
    public static final int UNIT_ALL = UNIT_PET | UNIT_PLAYER;

    /**
     * Add an event. The lastAdded status is updated.
     * @param e The event
     * @return true if added, false otherwise
     */
    boolean addEvent(BasicEvent e);

    /**
     * Add an event
     * @param e The event
     * @param logFileRow The row in the log file for the event
     */
    void addEvent(BasicEvent e, int logFileRow);

    /**
     * Add an event no matter what.
     * The lastEvent status is not updated.
     * @param e The event
     */
    void addEventAlways(BasicEvent e);

    /**
     * Add all the events in the collection.
     * The addEvent method is used to add every event.
     * @param ec
     */
    void addEvents(EventCollection ec);

    /**
     * Add all events in the list.
     * The addEvent method is used to add every event.
     * @param evs
     */
    void addEvents(Collection<BasicEvent> evs);

    /**
     * Add all events in an array no matter what
     * The lastEvent status is not updated.
     * @param evs The events
     */
    void addEventsAlways(Collection<BasicEvent> evs);

    /**
     * Add all events in an array no matter what
     * The lastEvent status is not updated.
     * @param evs The events
     */
    void addEventsAlways(EventCollection ec);

    /**
     * Create the active duration of this fight. If a pause larger than maxBreakTime is present in the
     * fight then that time is not included in the total time.
     * @param maxBreakTime The maximum break time
     */
    void createActiveDuration(double maxBreakTime, double globalCooldown);

    /**
     * Calculate damage for a certain spell
     * @param si The spell info
     * @return The result
     */
    AmountAndCount damage(SpellInfo si);

    /**
     * Calculate damage for a certain spell
     * @param si The spell info
     * @param type The type of damage: TYPE_RANGED_DAMAGE, TYPE_SPELL_DAMAGE
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL, only UNIT_PLAYER and UNIT_ALL works in EventCollection
     * @param sGUID null for no source GUID check, otherwise only the specified source GUID is used
     * @param dGUID null for no destination GUID check, otherwise only the specified destination GUID is used
     * @return The result
     */
    AmountAndCount damage(SpellInfo si, int type, int unit);

    /**
     * Run the collection through a filter and return a new filtered collection.
     * @param f The filter
     * @return
     */
    EventCollection filter(Filter f);

    /**
     * Run the collection through a set of filters with logical AND in between and return a new filtered collection.
     * @param fs The filters
     * @return
     */
    EventCollection filterAnd(List<Filter> fs);

    /**
     * Run the collection through a set of filters with logical OR in between and return a new filtered collection.
     * @param fs The filters
     * @return
     */
    EventCollection filterOr(List<Filter> fs);

    /**
     * Special method to filter on time on sorted collections, for speed purposes.
     * @param startTime The start time
     * @param endTime Then end time
     * @return
     */
    EventCollection filterTime(double startTime, double endTime);

    /**
     * Get the active damage time for this collection. Requires the collection events to be sorted.
     * @param maxBreakTime The maximum time with no damage
     * @param globalCooldown 2*globalCooldown is added to each coherent period of damage time because spells and abilities takes time to use.
     * @param startTime The start time to consider
     * @param endTime The end time to consider
     * @return The accumulated time that has activity
     */
    double getActiveDamageTime(double maxBreakTime, double globalCooldown, double startTime, double endTime);

    /**
     * Get the active duration of the fight.
     * @return The duration
     */
    double getActiveDuration();

    /**
     * Get the active healing time for this collection. Requires the collection events to be sorted.
     * @param maxBreakTime The maximum time with no healing
     * @param globalCooldown 2*globalCooldown is added to each coherent period of healing time because spells and abilities takes time to use.
     * @return The accumulated time that has activity
     */
    double getActiveHealingTime(double maxBreakTime, double globalCooldown, double startTime, double endTime);

    /**
     * Get the active power event time for this collection. Requires the collection events to be sorted.
     * @param maxBreakTime The maximum time with no power events
     * @param globalCooldown 2*globalCooldown is added to each coherent period of power time because spells and abilities takes time to use.
     * @return The accumulated time that has activity
     */
    double getActivePowerTime(double maxBreakTime, double globalCooldown, double startTime, double endTime);

    /**
     * Get the active damage+healing+power time for this collection. Requires the collection events to be sorted.
     * @param maxBreakTime The maximum time with no damage
     * @param globalCooldown 2*globalCooldown is added to each coherent period of time because spells and abilities takes time to use.
     * @param startTime The start time to consider
     * @param endTime The end time to consider
     * @return The accumulated time that has activity
     */
    double getActiveTime(double maxBreakTime, double globalCooldown, double startTime, double endTime);

    /**
     * Get all guids and the names connected to them.
     * @return A map with guid as key and name as value.
     */
    Map<String, String> getAllGuidsAndNames();

    /**
     * Get all guids and the names connected to them.
     * @return A map with guid as key and name as value.
     */
    Map<String, String> getSourceGuidsAndNames();

    /**
     * Get all guids and the names connected to them.
     * @return A map with guid as key and name as value.
     */
    Map<String, String> getDestinationGuidsAndNames();

    /**
     * Get the damage school flags used by this participant
     * @return The damage school flags
     */
    int getDamageSchoolFlags();

    /**
     * Get the damage school flags used by this participant
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL, only UNIT_PLAYER and UNIT_ALL works in EventCollection
     * @return The damage school flags
     */
    int getDamageSchoolFlags(int unit);

    /**
     * Find the last time in this fight.
     * @return The last time
     */
    double getEndTime();

    /**
     * Last damage event vs the victim
     * @return The time
     */
    double getEndTimeFast();

    /**
     * Get an event at a certain index
     * @param i The index for the event
     * @return
     */
    BasicEvent getEvent(int i);

    /**
     * Get all events from the EventCollection.
     * This method cannot be overriden so only the basic EventCollection events are returned.
     * @return A list with the events.
     */
    List<BasicEvent> getEvents();

    /**
     * Get all log events that are inside any of the time periods that are supplied
     * This method cannot be overriden so only the basic EventCollection events are returned.
     * @param tps The time periods
     * @return An array of the events that were found
     */
    <T extends BasicEvent> List<BasicEvent> getEvents(List<TimePeriod> tps);

    /**
     * Get events of a specific event class
     * This method cannot be overriden so only the basic EventCollection events are returned.
     * @param <T> Will be the class type that the object o has.
     * @param o A dummy object that selects the correct class to use.
     * @return An array of all event objects that has the class o has, or any sub class.
     */
    <T> List<T> getEvents(T o);

    /**
     * Get the all events, can be different from getEvents() if this is a complex type.
     * @return
     */
    List<BasicEvent> getEventsTotal();

    /**
     * Get IDs for a certain type of skills/spells
     * @param type The type of skill/spell, for example TYPE_SPELL_DAMAGE_DIRECT
     * @return
     */
    List<SpellInfo> getIDs(int type);

    /**
     * Get IDs for a certain type of skills/spells
     * @param type The type of skill/spell, for example TYPE_SPELL_DAMAGE_DIRECT
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL, only UNIT_PLAYER and UNIT_ALL works in EventCollection
     * @return
     */
    List<SpellInfo> getIDs(int type, int unit);

    /**
     * Get IDs for a certain type of skills/spells
     * @param type The type of skill/spell, for example TYPE_SPELL_DAMAGE_DIRECT
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL, only UNIT_PLAYER and UNIT_ALL works in EventCollection
     * @param schools The wanted schools, if a spell matches any of the schools specified it will be used, set to -1 for any school.
     * @return
     */
    List<SpellInfo> getIDs(int type, int unit, int schools);

    /**
     * Get IDs for a certain type of skills/spells
     * @param originalUnit The unit type to save in SpellInfoExtended
     * @param type The type of skill/spell, for example TYPE_SPELL_DAMAGE_DIRECT
     * @return
     */
    List<SpellInfoExtended> getIDsExtended(int originalUnit, int type);

    /**
     * Get IDs for a certain type of skills/spells
     * @param originalUnit The unit type to save in SpellInfoExtended
     * @param type The type of skill/spell, for example TYPE_SPELL_DAMAGE_DIRECT
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL, only UNIT_PLAYER and UNIT_ALL works in EventCollection
     * @return
     */
    List<SpellInfoExtended> getIDsExtended(int originalUnit, int type, int unit);

    /**
     * Get IDs for a certain type of skills/spells
     * @param originalUnit The unit type to save in SpellInfoExtended
     * @param type The type of skill/spell, for example TYPE_SPELL_DAMAGE_DIRECT
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL, only UNIT_PLAYER and UNIT_ALL works in EventCollection
     * @param schools The wanted schools, if a spell matches any of the schools specified it will be used, set to -1 for any school.
     * @return
     */
    List<SpellInfoExtended> getIDsExtended(int originalUnit, int type, int unit, int schools);

    /**
     * Get the last added event
     * @return The last event
     */
    BasicEvent getLastAdded();

    /**
     * Get a list of the raw log types (the types in the log file) that are present in this collection.
     * @return A set with the log types
     */
    Set<String> getLogTypes();

    /**
     * Get all swing damage events
     * @return The swing damage events
     */
    List<SwingDamageEvent> getMeleeDamageEvents();

    /**
     * Get the number of events total, can be different from size() if this is a complex type.
     * @return The number of events total
     */
    int getNumEventsTotal();

    /**
     * Get all spell damage events
     * @return The spell damage events
     */
    List<SpellDamageEvent> getSpellDamageEvents();

    /**
     * Find the first event in this fight
     * @return The first event
     */
    BasicEvent getStartEvent();

    /**
     * Get the starting time from the events in this collection
     * @return The starting time
     */
    double getStartTime();

    /**
     * First damage event vs the victim
     * @return The time
     */
    double getStartTimeFast();

    TimePeriod getTimePeriod();

    /**
     * Get the total melee damage done from all events
     * @return The damage done
     */
    long getTotalMeleeDamage();

    /**
     * Get the total spell damage done from all events
     * @return The damage done
     */
    long getTotalSpellDamage();

    boolean hasDoublets();

    /**
     * Calculate healing for a certain spell
     * @param si The spell info
     * @return The result
     */
    AmountAndCount healing(SpellInfo si);

    /**
     * Calculate healing for a certain spell
     * @param si The spell info
     * @param type The types can be TYPE_SPELL_HEALING, TYPE_SPELL_HEALING_DIRECT, TYPE_SPELL_HEALING_PERIODIC or a bit combination of those
     * The type TYPE_SPELL_HEALING includes DIRECT and PERIODIC.
     * @return The result
     */
    AmountAndCount healing(SpellInfo si, int type, int unit);

    /**
     * Calculate overhealing for a certain spell
     * @param si The spell info
     * @return The result
     */
    AmountAndCount overHealing(SpellInfo si);

    /**
     * Calculate overhealing for a certain spell
     * @param si The spell info
     * @param type The types can be TYPE_SPELL_HEALING, TYPE_SPELL_HEALING_DIRECT, TYPE_SPELL_HEALING_PERIODIC or a bit combination of those
     * The type TYPE_SPELL_HEALING includes DIRECT and PERIODIC.
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL, only UNIT_PLAYER and UNIT_ALL works in EventCollection
     * @return The result
     */
    AmountAndCount overHealing(SpellInfo si, int type, int unit);

    /**
     * Calculate amount for a certain power spell, using all type
     * @param si The spell info
     * @return The result
     */
    AmountAndCount power(SpellInfo si);

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
    AmountAndCount power(SpellInfo si, int type, int unit);

    /**
     * Remove the last added event
     */
    void removeLastAdded();

    void setActiveDuration(double activeDuration);

    /**
     * Set the events in the list as the events this collection should contain.
     * All current events are lost.
     * @param events
     */
    void setEvents(Collection<BasicEvent> events);

    /**
     * Set the events in the list as the events this collection should contain.
     * All current events are lost.
     * @param events
     */
    void setEvents(EventCollection events);

    /**
     * Get the number of events in this collection.
     * @return
     */
    int size();

    /**
     * Sort the events for this event collection
     */
    void sort();

    /**
     * Get total damage.
     * The school flags can be found in BasicEvent
     * @param damageSchoolFlags The damage schools to process
     * @param type The types can be TYPE_RANGED_DAMAGE, TYPE_SPELL_DAMAGE, TYPE_SWING_DAMAGE or a bit combination of those
     * @return The results
     */
    AmountAndCount totalDamage(int damageSchoolFlags, int type);

    /**
     * Get total damage.
     * The school flags can be found in Constants
     * @param damageSchoolFlags The damage schools to process
     * @param type The types can be TYPE_RANGED_DAMAGE, TYPE_SPELL_DAMAGE, TYPE_SWING_DAMAGE or a bit combination of those
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL, only UNIT_PLAYER and UNIT_ALL works in EventCollection
     * @return The results
     */
    AmountAndCount totalDamage(int damageSchoolFlags, int type, int unit);

    /**
     * Get total damage.
     * The school flags can be found in Constants
     * @param damageSchoolFlags The damage schools to process
     * @param type The types can be TYPE_RANGED_DAMAGE, TYPE_SPELL_DAMAGE, TYPE_SWING_DAMAGE or a bit combination of those
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL, only UNIT_PLAYER and UNIT_ALL works in EventCollection
     * @param name The wanted name for the unit, or null for any
     * @return The results
     */
    AmountAndCount totalDamage(int damageSchoolFlags, int type, int unit, String name);

    /**
     * Get total damage during a certain time frame.
     * The school flags can be found in Constants
     * @param damageSchoolFlags The damage schools to process
     * @param type The types can be TYPE_RANGED_DAMAGE, TYPE_SPELL_DAMAGE, TYPE_SWING_DAMAGE or a bit combination of those
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL, only UNIT_PLAYER and UNIT_ALL works in EventCollection
     * @param name The wanted name for the unit, or null for any
     * @param startTime The start time
     * @param endTime The end time
     * @return The results
     */
    AmountAndCount totalDamage(int damageSchoolFlags, int type, int unit, String name, double startTime, double endTime);

    /**
     * Get total healing.
     * The school flags can be found in Constants
     * @param damageSchoolFlags The damage schools to process
     * @param type The types can be TYPE_SPELL_HEALING, TYPE_SPELL_HEALING_DIRECT, TYPE_SPELL_HEALING_PERIODIC or a bit combination of those
     * The type TYPE_SPELL_HEALING includes DIRECT and PERIODIC.
     * @return
     */
    AmountAndCount totalHealing(int healingSchoolFlags, int type);

    /**
     * Get total healing.
     * The school flags can be found in Constants
     * @param damageSchoolFlags The damage schools to process
     * @param type The types can be TYPE_SPELL_HEALING, TYPE_SPELL_HEALING_DIRECT, TYPE_SPELL_HEALING_PERIODIC or a bit combination of those
     * The type TYPE_SPELL_HEALING includes DIRECT and PERIODIC.
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL, only UNIT_PLAYER and UNIT_ALL works in EventCollection
     * @return
     */
    AmountAndCount totalHealing(int healingSchoolFlags, int type, int unit);

    /**
     * Get total healing.
     * The school flags can be found in Constants
     * @param damageSchoolFlags The damage schools to process
     * @param type The types can be TYPE_SPELL_HEALING, TYPE_SPELL_HEALING_DIRECT, TYPE_SPELL_HEALING_PERIODIC or a bit combination of those
     * The type TYPE_SPELL_HEALING includes DIRECT and PERIODIC.
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL, only UNIT_PLAYER and UNIT_ALL works in EventCollection
     * @param name The wanted name for the unit, or null for any
     * @return
     */
    AmountAndCount totalHealing(int healingSchoolFlags, int type, int unit, String name);

    /**
     * Get total healing for a certain time frame.
     * The school flags can be found in Constants
     * @param damageSchoolFlags The damage schools to process
     * @param type The types can be TYPE_SPELL_HEALING, TYPE_SPELL_HEALING_DIRECT, TYPE_SPELL_HEALING_PERIODIC or a bit combination of those
     * The type TYPE_SPELL_HEALING includes DIRECT and PERIODIC.
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL, only UNIT_PLAYER and UNIT_ALL works in EventCollection
     * @param name The wanted name for the unit, or null for any
     * @param startTime The start time
     * @param endTime The end time
     * @return
     */
    AmountAndCount totalHealing(int healingSchoolFlags, int type, int unit, String name, double startTime, double endTime);

    /**
     * Get total overhealing.
     * The school flags can be found in Constants
     * @param damageSchoolFlags The damage schools to process
     * @param type The types can be TYPE_SPELL_HEALING, TYPE_SPELL_HEALING_DIRECT, TYPE_SPELL_HEALING_PERIODIC or a bit combination of those
     * The type TYPE_SPELL_HEALING includes DIRECT and PERIODIC.
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL, only UNIT_PLAYER and UNIT_ALL works in EventCollection
     * @return
     */
    AmountAndCount totalOverHealing(int healingSchoolFlags, int type);

    /**
     * Get total overhealing.
     * The school flags can be found in Constants
     * @param damageSchoolFlags The damage schools to process
     * @param type The types can be TYPE_SPELL_HEALING, TYPE_SPELL_HEALING_DIRECT, TYPE_SPELL_HEALING_PERIODIC or a bit combination of those
     * The type TYPE_SPELL_HEALING includes DIRECT and PERIODIC.
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL
     * @return
     */
    AmountAndCount totalOverHealing(int healingSchoolFlags, int type, int unit);

    /**
     * Get total overhealing.
     * The school flags can be found in Constants
     * @param damageSchoolFlags The damage schools to process
     * @param type The types can be TYPE_SPELL_HEALING, TYPE_SPELL_HEALING_DIRECT, TYPE_SPELL_HEALING_PERIODIC or a bit combination of those
     * The type TYPE_SPELL_HEALING includes DIRECT and PERIODIC.
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL
     * @param name The wanted name for the unit, or null for any
     * @return
     */
    AmountAndCount totalOverHealing(int healingSchoolFlags, int type, int unit, String name);

    /**
     * Get total overhealing for a certain time frame.
     * The school flags can be found in Constants
     * @param damageSchoolFlags The damage schools to process
     * @param type The types can be TYPE_SPELL_HEALING, TYPE_SPELL_HEALING_DIRECT, TYPE_SPELL_HEALING_PERIODIC or a bit combination of those
     * The type TYPE_SPELL_HEALING includes DIRECT and PERIODIC.
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL, only UNIT_PLAYER and UNIT_ALL works in EventCollection
     * @param name The wanted name for the unit, or null for any
     * @param startTime The start time
     * @param endTime The end time
     * @return
     */
    AmountAndCount totalOverHealing(int healingSchoolFlags, int type, int unit, String name, double startTime, double endTime);

    /**
     * Get total power.
     * The school flags can be found in Constants
     * @param damageSchoolFlags The damage schools to process
     * @param type The types can be TYPE_SPELL_LEECH, TYPE_SPELL_LEECH_DIRECT, TYPE_SPELL_LEECH_PERIODIC,
     * TYPE_SPELL_DRAIN, TYPE_SPELL_DRAIN_DIRECT, TYPE_SPELL_DRAIN_PERIODIC,
     * TYPE_SPELL_ENERGIZE, TYPE_SPELL_ENERGIZE_DIRECT, TYPE_SPELL_ENERGIZE_PERIODIC
     * or a bit combination of those
     * @return The results
     */
    AmountAndCount totalPower(int schoolFlags, int type);

    /**
     * Get total power.
     * The school flags can be found in Constants
     * @param damageSchoolFlags The damage schools to process
     * @param type The types can be TYPE_SPELL_LEECH, TYPE_SPELL_LEECH_DIRECT, TYPE_SPELL_LEECH_PERIODIC,
     * TYPE_SPELL_DRAIN, TYPE_SPELL_DRAIN_DIRECT, TYPE_SPELL_DRAIN_PERIODIC,
     * TYPE_SPELL_ENERGIZE, TYPE_SPELL_ENERGIZE_DIRECT, TYPE_SPELL_ENERGIZE_PERIODIC
     * or a bit combination of those
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL, only UNIT_PLAYER and UNIT_ALL works in EventCollection
     * @return The results
     */
    AmountAndCount totalPower(int schoolFlags, int type, int unit);

    /**
     * Get total power.
     * The school flags can be found in Constants
     * @param damageSchoolFlags The damage schools to process
     * @param type The types can be TYPE_SPELL_LEECH, TYPE_SPELL_LEECH_DIRECT, TYPE_SPELL_LEECH_PERIODIC,
     * TYPE_SPELL_DRAIN, TYPE_SPELL_DRAIN_DIRECT, TYPE_SPELL_DRAIN_PERIODIC,
     * TYPE_SPELL_ENERGIZE, TYPE_SPELL_ENERGIZE_DIRECT, TYPE_SPELL_ENERGIZE_PERIODIC
     * or a bit combination of those
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL, only UNIT_PLAYER and UNIT_ALL works in EventCollection
     * @param name The wanted name for the unit, or null for any
     * @return The results
     */
    AmountAndCount totalPower(int schoolFlags, int type, int unit, String name);

    /**
     * Get total power for a certain time frame.
     * The school flags can be found in Constants
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
    AmountAndCount totalPower(int schoolFlags, int type, int unit, String name, double startTime, double endTime);

}
