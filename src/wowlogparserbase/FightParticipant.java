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

import wowlogparserbase.eventfilter.Filter;
import wowlogparserbase.events.BasicEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import wowlogparserbase.helpers.ListFunctions;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import wowlogparserbase.events.SkillInterface;

/**
 * A class containing information about a fight participant.
 * @author racy
 */
public class FightParticipant extends AbstractEventCollection implements Comparable, EventCollectionOptimizedAccess {

    protected Set<String> guids = new HashSet<String>();
    protected String sourceGUID = null;
    protected String name = "";
    protected long flags = 0;
    protected int playerClass = Constants.CLASS_UNKNOWN;

    protected EventCollectionSimple eventsMade = new EventCollectionSimple();
    protected EventCollectionSimple eventsReceived = new EventCollectionSimple();
    protected List<PetInfo> petNames = new ArrayList<PetInfo>();

    protected int healthDeficit = 0;
    
    protected boolean isPetObject = false;
    protected double lastActionDoneForOverhealingCalcs = 0;
    
    /**
     * Empty constructor
     */
    public FightParticipant() {
    }

    /**
     * Ensure capacity constructor
     */
    public FightParticipant(int numEvents, int numReceivedEvents) {
        eventsMade = new EventCollectionSimple(numReceivedEvents);
        eventsReceived = new EventCollectionSimple(numReceivedEvents);
    }

    /**
     * Constructor to make a semi deep copy of another participant
     * @param p The source participant
     */
    public FightParticipant(FightParticipant p) {
        super(p);
        sourceGUID = p.sourceGUID;
        name = p.name;
        flags = p.flags;
        guids = new HashSet<String>(p.guids);
        isPetObject = p.isPetObject;
        eventsMade = new EventCollectionSimple(p.eventsMade);
        eventsReceived = new EventCollectionSimple(p.eventsReceived);
        petNames = new ArrayList<PetInfo>(p.petNames);
        playerClass = p.playerClass;
    }

    @Override
    public void createActiveDuration(double maxBreakTime, double globalCooldown) {
        eventsMade.createActiveDuration(maxBreakTime, globalCooldown);
        eventsReceived.createActiveDuration(maxBreakTime, globalCooldown);
        for (PetInfo pi : petNames) {
            pi.createActiveDuration(maxBreakTime, globalCooldown);
        }
        setActiveDuration(eventsMade.getActiveDuration());
    }


    /**
     * Get the time where the last relevant event was made for overhealing calculations.
     * @return The time.
     */
    public double getLastActionDoneForOverhealingCalcs() {
        return lastActionDoneForOverhealingCalcs;
    }

    /**
     * Set the last time where a relevant event was made for overhealing calculations.
     * @param lastDamageReceivedOrDoneForOverhealingCalcs The time.
     */
    public void setLastActionDoneForOverhealingCalcs(double lastDamageReceivedOrDoneForOverhealingCalcs) {
        this.lastActionDoneForOverhealingCalcs = lastDamageReceivedOrDoneForOverhealingCalcs;
    }
    
    /**
     * Get the player class name of this participant
     * @return The class name
     */
    public String getClassName() {
        return classNames[playerClass];
    }
    
    /**
     * Create and empty shell copy of this participant, without events etc.
     * @return A new stripped participant
     */
    public FightParticipant createEmptyShell() {
        FightParticipant p = new FightParticipant();
        p.setSourceGUID(getSourceGUID());
        p.setName(getName());
        p.setFlags(getFlags());
        p.playerClass = playerClass;
        p.setIsPetObject(isPetObject);
        for(PetInfo pi : getPetNames()) {
            p.getPetNames().add(pi.createEmptyShell());
        }
        return p;
    }
    
    /**
     * Create and empty shell copy of this participant, without events etc.
     * @return A new stripped participant
     */
    public FightParticipant createSuperEmptyShell() {
        FightParticipant p = new FightParticipant();
        for(PetInfo pi : getPetNames()) {
            p.getPetNames().add(pi.createSuperEmptyShell());
        }
        return p;
    }

    /**
     * Make a received participant from this participant, with fight f.
     * @param f The fight the events are from.
     * @return
     */
    public FightParticipant makeReceivedParticipant(Fight f) {
        FightParticipant p = new FightParticipant();
        p.setName("Various sources");
        p.eventsMade = new EventCollectionSimple(eventsReceived);
        p.setFlags(f.flags);
        p.setActiveDuration(eventsReceived.getActiveDuration());
        for (PetInfo pi : petNames) {
            PetInfo newPi = pi.makeReceivedParticipant(f);
            p.petNames.add(newPi);
        }
        return p;
    }
    
    /**
     * Compare to another participant
     * @param o The participant to compare it to
     * @return 0 if the GUIDs match, the String class compareTo results otherwise
     */
    public int compareTo(Object o) {
        if (o instanceof FightParticipant) {
            FightParticipant p = (FightParticipant) o;
            return getSourceGUID().compareTo(p.getSourceGUID());
        } else {
            return -1;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FightParticipant other = (FightParticipant) obj;
        if (this.sourceGUID != other.sourceGUID && (this.sourceGUID == null || !this.sourceGUID.equals(other.sourceGUID))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + (this.sourceGUID != null ? this.sourceGUID.hashCode() : 0);
        return hash;
    }

    /**
     * Check if the participant is a member of my group or raid.
     * @return true if a member, false otherwise
     */
    public boolean isRaidPerson() {
        if ( (getFlags() &
                (Constants.FLAGS_OBJECT_AFFILIATION_MINE +
                Constants.FLAGS_OBJECT_AFFILIATION_PARTY +
                Constants.FLAGS_OBJECT_AFFILIATION_RAID)) != 0 ) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Sort the events for this participant
     */
    public void sort() {
        eventsMade.sort();
        eventsReceived.sort();
        //eventsAll.sort();
        Collections.sort(getPetNames());
    }
    
    /**
     * Remove duplicate events from this participant
     */
    public void removeEqualElements() {
        eventsMade.removeEqualElements();
        //eventsAll.removeEqualElements();
        eventsReceived.removeEqualElements();
        ListFunctions.removeEqual(getPetNames());
    }

    /**
     * Merge participants. The GUID is set to "" if there are 2 or more to merge. 
     * The name is set to "Merged participants" if 2 or more to merge.
     * The flags are merged at a bit level with & to retain flags that are equal for all participants.
     * @param list The participants to merge
     * @return A new merged participant
     */
    public static <T extends FightParticipant> FightParticipant merge(List<T> list) {
        return merge(list, null, null);
    }
    
    /**
     * Merge participants. The GUID is set to "" if there are 2 or more to merge. 
     * The name is set to "Merged participants" if 2 or more to merge.
     * The flags are merged at a bit level with & to retain flags that are equal for all participants.
     * @param participantList The participants to merge
     * @return A new merged participant
     */
    public static <T extends FightParticipant> FightParticipant merge(List<T> participantList, IntCallback cb, StringCallback cbStr) {
        if(participantList.size() == 0) {
            FightParticipant p = new FightParticipant();
            p.setGUID("");
            p.setName("Merged participants");
            return p;
        }
        if(participantList.size() == 1) {
            return new FightParticipant(participantList.get(0));
        }
        
        boolean allSame = true;
        Set<String> testGuids = participantList.get(0).getSourceGUIDs();
        testGuids.add(participantList.get(0).getSourceGUID());
        String testGuid;
        if (testGuids.size()==1) {
            allSame = true;
            testGuid = testGuids.iterator().next();
        } else {
            allSame = false;
            testGuid = "";
        }
        String name = participantList.get(0).getName();
        long flags = participantList.get(0).getFlags();
        boolean isObject = participantList.get(0).isPetObject();
        int playerClass = participantList.get(0).playerClass;
        FightParticipant p = new FightParticipant();
        int index = 0;
        for (FightParticipant sourceP : participantList) {
            index++;
            if (cb != null) {
                cb.reportInt(index);
            }
            flags = flags & sourceP.getFlags();
            if (!sourceP.checkGUID(testGuid)) {
                allSame = false;
            }
            p.addSourceGUIDs(sourceP.guids);
            p.eventsMade.addEventsAlways(sourceP.eventsMade);
            //p.eventsAll.addEventsAlways(sourceP.eventsAll);
            p.eventsReceived.addEventsAlways(sourceP.eventsReceived);
            p.getPetNames().addAll(sourceP.getPetNames());
        }
        //Merge equal pets
        p.setPetNames(PetInfo.mergeEqual(p.getPetNames()));
        
        //If all merging participants are the same
        if(allSame) {
            p.setGUID(testGuid);
            p.setName(name);
            p.setFlags(flags);
            p.setPlayerClass(playerClass);
            p.setIsPetObject(isObject);
        } else {
            p.setGUID("");
            p.setName("Merged participants");            
            p.setFlags(flags);
        }
        if (cbStr != null) {
            cbStr.reportString("sort");
        }
        p.sort();
        p.removeEqualElements();
        return p;
    }
        
    /**
     * Calculate damage for a certain spell
     * @param si The spell info
     * @param type The type of damage: TYPE_RANGED_DAMAGE, TYPE_SPELL_DAMAGE
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL
     * @return The result
     */
    public AmountAndCount damage(SpellInfo si, int type, int unit) {
        AmountAndCount a = eventsMade.damage(si, type, unit);

        if ((unit & UNIT_PET) != 0) {
            for (PetInfo p : getPetNames()) {
                AmountAndCount petAmount = p.damage(si, type, UNIT_ALL);
                a.add(petAmount);
            }
        }
        return a;
    }

    /**
     * Get total damage during a certain time frame.
     * The school flags can be found in Constants
     * @param damageSchoolFlags The damage schools to process
     * @param type The types can be TYPE_RANGED_DAMAGE, TYPE_SPELL_DAMAGE, TYPE_SWING_DAMAGE or a bit combination of those
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL
     * @param name The name of the unit, or null for any.
     * @param startTime The start time
     * @param endTime The end time
     * @return The results
     */
    @Override
    public AmountAndCount totalDamage(int damageSchoolFlags, int type, int unit, String name, double startTime, double endTime) {
        AmountAndCount a = eventsMade.totalDamage(damageSchoolFlags, type, unit, name, startTime, endTime);

        if ((unit & UNIT_PET) != 0) {
            for (PetInfo p : getPetNames()) {
                AmountAndCount petAmount = p.totalDamage(damageSchoolFlags, type, UNIT_ALL, name, startTime, endTime);
                a.add(petAmount);
            }
        }
        return a;
    }
    
    /**
     * Calculate healing for a certain spell
     * @param si The spell info
     * @param type The types can be TYPE_SPELL_HEALING, TYPE_SPELL_HEALING_DIRECT, TYPE_SPELL_HEALING_PERIODIC or a bit combination of those
     *          The type TYPE_SPELL_HEALING includes DIRECT and PERIODIC.
     * @return The result
     */
    public AmountAndCount healing(SpellInfo si, int type, int unit) {
        AmountAndCount a = eventsMade.healing(si, type, unit);
        
        if ((unit & UNIT_PET) != 0) {
            for (PetInfo p : getPetNames()) {
                AmountAndCount petAmount = p.healing(si, type, UNIT_ALL);
                a.add(petAmount);
            }
        }
        return a;
    }

    /**
     * Get total healing for a certain time frame.
     * The school flags can be found in Constants
     * @param damageSchoolFlags The damage schools to process
     * @param type The types can be TYPE_SPELL_HEALING, TYPE_SPELL_HEALING_DIRECT, TYPE_SPELL_HEALING_PERIODIC or a bit combination of those
     *          The type TYPE_SPELL_HEALING includes DIRECT and PERIODIC.
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL
     * @param name The wanted name for the unit
     * @param startTime The start time
     * @param endTime The end time
     * @return
     */
    @Override
    public AmountAndCount totalHealing(int healingSchoolFlags, int type, int unit, String name, double startTime, double endTime) {
        AmountAndCount a = eventsMade.totalHealing(healingSchoolFlags, type, unit, name, startTime, endTime);

        if ((unit & UNIT_PET) != 0) {
            for (PetInfo p : getPetNames()) {
                AmountAndCount petAmount = p.totalHealing(healingSchoolFlags, type, UNIT_ALL, name, startTime, endTime);
                a.add(petAmount);
            }
        }
        return a;
    }

    /**
     * Calculate overhealing for a certain spell
     * @param si The spell info
     * @param type The types can be TYPE_SPELL_HEALING, TYPE_SPELL_HEALING_DIRECT, TYPE_SPELL_HEALING_PERIODIC or a bit combination of those
     *          The type TYPE_SPELL_HEALING includes DIRECT and PERIODIC.
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL
     * @return The result
     */
    public AmountAndCount overHealing(SpellInfo si, int type, int unit) {
        AmountAndCount a = eventsMade.overHealing(si, type, unit);

        if ((unit & UNIT_PET) != 0) {
            for (PetInfo p : getPetNames()) {
                AmountAndCount petAmount = p.overHealing(si, type, UNIT_ALL);
                a.add(petAmount);
            }
        }
        return a;
    }

    /**
     * Get total overhealing for a certain time frame.
     * The school flags can be found in Constants
     * @param damageSchoolFlags The damage schools to process
     * @param type The types can be TYPE_SPELL_HEALING, TYPE_SPELL_HEALING_DIRECT, TYPE_SPELL_HEALING_PERIODIC or a bit combination of those
     *          The type TYPE_SPELL_HEALING includes DIRECT and PERIODIC.
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL
     * @param name The name of the unit, or null for any.
     * @param startTime The start time
     * @param endTime The end time
     * @return
     */
    @Override
    public AmountAndCount totalOverHealing(int healingSchoolFlags, int type, int unit, String name, double startTime, double endTime) {
        AmountAndCount a = eventsMade.totalOverHealing(healingSchoolFlags, type, unit, name, startTime, endTime);

        if ((unit & UNIT_PET) != 0) {
            for (PetInfo p : getPetNames()) {
                AmountAndCount petAmount = p.totalOverHealing(healingSchoolFlags, type, UNIT_ALL, name, startTime, endTime);
                a.add(petAmount);
            }
        }
        return a;
    }
    
    /**
     * Calculate amount for a certain power spell
     * @param si The spell info
     * @param type The types can be TYPE_SPELL_LEECH, TYPE_SPELL_LEECH_DIRECT, TYPE_SPELL_LEECH_PERIODIC,
     * TYPE_SPELL_DRAIN, TYPE_SPELL_DRAIN_DIRECT, TYPE_SPELL_DRAIN_PERIODIC,
     * TYPE_SPELL_ENERGIZE, TYPE_SPELL_ENERGIZE_DIRECT, TYPE_SPELL_ENERGIZE_PERIODIC 
     * or a bit combination of those
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL
     * @return The result
     */
    public AmountAndCount power(SpellInfo si, int type, int unit) {
        AmountAndCount a = eventsMade.power(si, type, unit);

        if ((unit & UNIT_PET) != 0) {
            for (PetInfo p : getPetNames()) {
                AmountAndCount petAmount = p.power(si, type, UNIT_ALL);
                a.add(petAmount);
            }
        }
        return a;
    }

    /**
     * Get total power for a certain time frame.
     * The school flags can be found in Constants
     * @param damageSchoolFlags The damage schools to process
     * @param type The types can be TYPE_SPELL_LEECH, TYPE_SPELL_LEECH_DIRECT, TYPE_SPELL_LEECH_PERIODIC,
     * TYPE_SPELL_DRAIN, TYPE_SPELL_DRAIN_DIRECT, TYPE_SPELL_DRAIN_PERIODIC,
     * TYPE_SPELL_ENERGIZE, TYPE_SPELL_ENERGIZE_DIRECT, TYPE_SPELL_ENERGIZE_PERIODIC 
     * or a bit combination of those
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL
     * @param name The name of the unit, or null for any.
     * @param startTime The start time
     * @param endTime The end time
     * @return The results
     */
    @Override
    public AmountAndCount totalPower(int schoolFlags, int type, int unit, String name, double startTime, double endTime) {
        AmountAndCount a = eventsMade.totalPower(schoolFlags, type, unit, name, startTime, endTime);

        if ((unit & UNIT_PET) != 0) {
            for (PetInfo p : getPetNames()) {
                AmountAndCount petAmount = p.totalPower(schoolFlags, type, UNIT_ALL, name, startTime, endTime);
                a.add(petAmount);
            }
        }
        return a;
    }

    /**
     * Get the damage school flags used by this participant
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL
     * @return The damage school flags
     */
    @Override
    public int getDamageSchoolFlags(int unit) {
        int schoolFlags = eventsMade.getDamageSchoolFlags(unit);
        
        if ((unit & UNIT_PET) != 0) {
            for (PetInfo p : getPetNames()) {
                int petSchools = p.getDamageSchoolFlags(UNIT_ALL);
                schoolFlags = schoolFlags | petSchools;
            }
        }
        
        return schoolFlags;
    }

    /**
     * Get IDs for a certain type of skills/spells
     * @param type The type of skill/spell, for example TYPE_SPELL_DAMAGE_DIRECT
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL
     * @param schools The wanted schools, if a spell matches any of the schools specified it will be used, set to -1 for any school.
     * @return
     */
    @Override
    public List<SpellInfo> getIDs(int type, int unit, int school) {
        List<SpellInfo> ids = eventsMade.getIDs(type, unit, school);

        if ((unit & UNIT_PET) != 0) {
            for (PetInfo p : getPetNames()) {
                List<SpellInfo> petIds = p.getIDs(type, UNIT_ALL, school);
                ids.addAll(petIds);
                Collections.sort(ids);
                ListFunctions.removeEqual(ids);
            }
        }
        return ids;
    }

    /**
     * Get IDs for a certain type of skills/spells
     * @param originalUnit The unit type to save in SpellInfoExtended
     * @param type The type of skill/spell, for example TYPE_SPELL_DAMAGE_DIRECT
     * @param unit Unit types, for example UNIT_PET, UNIT_PLAYER, UNIT_ALL
     * @param schools The wanted schools, if a spell matches any of the schools specified it will be used, set to -1 for any school.
     * @return
     */
    @Override
    public List<SpellInfoExtended> getIDsExtended(int originalUnit, int type, int unit, int school) {
        List<SpellInfoExtended> ids = eventsMade.getIDsExtended(originalUnit, type, unit, school);

        if ((unit & UNIT_PET) != 0) {
            for (PetInfo p : getPetNames()) {
                List<SpellInfoExtended> petIds = p.getIDsExtended(originalUnit, type, UNIT_ALL, school);
                ids.addAll(petIds);
                Collections.sort(ids);
                ListFunctions.removeEqual(ids);
            }
        }
        return ids;
    }
    
    /**
     * Split events for spells into separate lists
     * @param spellIds Spell ids to split for
     * @return The result
     */
    public Map<Integer, FightParticipant> splitSpellEvents(Set<Integer> spellIds) {
        Map<Integer, FightParticipant> out = new HashMap<Integer, FightParticipant>();
        for (int id : spellIds) {
            FightParticipant p = createEmptyShell();
            out.put(id, p);
        }
        for (BasicEvent be : eventsMade.getEvents()) {
            if (be instanceof SkillInterface) {
                SkillInterface i = (SkillInterface) be;
                if (spellIds.contains(i.getSkillID())) {
                    FightParticipant ec = out.get(i.getSkillID());
                    ec.addEventAlways(be);
                }
            }
        }
        for (BasicEvent be : eventsReceived.getEvents()) {
            if (be instanceof SkillInterface) {
                SkillInterface i = (SkillInterface) be;
                if (spellIds.contains(i.getSkillID())) {
                    FightParticipant ec = out.get(i.getSkillID());
                    ec.eventsReceived.addEventAlways(be);
                }
            }
        }
        for (int k=0; k<petNames.size(); k++) {
            PetInfo sourcePet = petNames.get(k);
            for (BasicEvent be : sourcePet.getEvents()) {
                if (be instanceof SkillInterface) {
                    SkillInterface i = (SkillInterface) be;
                    if (spellIds.contains(i.getSkillID())) {
                        FightParticipant ec = out.get(i.getSkillID());
                        PetInfo destPet = ec.petNames.get(k);
                        destPet.addEventAlways(be);
                    }
                }
            }
            for (BasicEvent be : sourcePet.getEventsReceived().getEvents()) {
                if (be instanceof SkillInterface) {
                    SkillInterface i = (SkillInterface) be;
                    if (spellIds.contains(i.getSkillID())) {
                        FightParticipant ec = out.get(i.getSkillID());
                        PetInfo destPet = ec.petNames.get(k);
                        destPet.addEventReceivedAlways(be);
                    }
                }
            }
        }

        return out;
    }

    private FightParticipant lastPart = null;

    public void getEventsContinueFromLastReset() {
        eventsMade.getEventsContinueFromLastReset();
        eventsReceived.getEventsContinueFromLastReset();
        for (PetInfo pi : getPetNames()) {
            pi.getEventsContinueFromLastReset();
        }
        lastPart = null;
    }

    @Override
    public FightParticipant getEventsContinueFromLast(double startTime, double endTime) {
        if (lastPart == null) {
            lastPart = createSuperEmptyShell();
        }
        lastPart.eventsMade = new EventCollectionSimple();
        lastPart.eventsReceived = new EventCollectionSimple();
        //lastPart.eventsAll = new EventCollectionSimple();

        lastPart.eventsMade.addEventsAlways(eventsMade.getEventsContinueFromLast(startTime, endTime));
        //lastPart.eventsAll.addEventsAlways(eventsAll.getEventsContinueFromLast(startTime, endTime));
        lastPart.eventsReceived.addEventsAlways(eventsReceived.getEventsContinueFromLast(startTime, endTime));
        for(int k=0; k<getPetNames().size(); k++) {
            PetInfo sourcePet = getPetNames().get(k);
            lastPart.getPetNames().set(k, sourcePet.getEventsContinueFromLast(startTime, endTime));
        }

        return lastPart;
    }

    /**
     * Add an event to this participant. Only adds it if the source GUIDs match.
     * @param e The event
     * @return true if added, false otherwise
     */
    @Override
    public boolean addEvent(BasicEvent e) {
        boolean added = false;
        if (checkGUID(e.getSourceGUID())) {
        //if (e.getSourceGUID().equals(getSourceGUID())) {
            //If the event was already added, then skip.
            if (getLastAdded().logFileRow != e.logFileRow) {
                eventsMade.addEvent(e);
                //eventsAll.addEvent(e);
                added = true;
            }
        }
        if (checkGUID(e.getDestinationGUID())) {
        //if (e.getDestinationGUID().equals(getSourceGUID())) {
            //If the event was already added, then skip.
            if (eventsReceived.getLastAdded().logFileRow != e.logFileRow) {
                eventsReceived.addEvent(e);
                //eventsAll.addEvent(e);
                added = true;
            }
        }
        return added;
    }
            
    /**
     * Add all events to "events received" no matter what
     * @param evs The events
     */
    public void addEventsReceivedAlways(List<BasicEvent> evs) {
        eventsReceived.addEventsAlways(evs);
    }

    /**
     * Set the guid for the participant
     * @param guid The guid
     */
    void setGUID(String guid) {
        setSourceGUID(guid);
    }

    /**
     * Set the name for the participant
     * @param name The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Set the flags for this participant
     * @param flags The flags
     */
    public void setFlags(long flags) {
        this.flags = flags;
    }

    /**
     * Set the class for this participant
     * @param playerClass The class number, for example CLASS_DRUID
     */
    void setPlayerClass(int playerClass) {
        this.playerClass = playerClass;                
    }

    public int getPlayerClass() {
        return playerClass;
    }
    
    /**
     * Check if a guid matches this participant:s guid
     * @param guid The guid to check
     * @return true if match, false otherwise
     */
    boolean checkGUID(String guid) {
        if (guid.equals(getSourceGUID())) {
            return true;
        } else {
            if (guids.contains(guid)) {
                return true;
            } else {
                return false;
            }
        }
    }
    
    /**
     * Check if this participant is a pet
     * @return true if it is a pet, false otherwise
     */
    public boolean isPet() {
        return BasicEvent.isTypePet(getFlags());
    }

    /**
     * Check if this participant is a pet
     * @return true if it is a pet, false otherwise
     */
    public boolean isGuardian() {
        return BasicEvent.isTypeGuardian(getFlags());
    }

    /**
     * Check if this participant is a player
     * @return true if it is a player, false otherwise
     */
    public boolean isPlayer() {
        return BasicEvent.isTypePlayer(getFlags());
    }

    /**
     * Check if this participant is friendly
     * @return true if it, false otherwise
     */
    public boolean isFriendly() {
        return BasicEvent.isReactionFriendly(getFlags());
    }

    /**
     * Check if this participant is hostile
     * @return true if it, false otherwise
     */
    public boolean isHostile() {
        return BasicEvent.isReactionHostile(getFlags());
    }

    /**
     * Check if this participant is an NPC
     * @return true if it is an NPC, false otherwise
     */
    public boolean isNpc() {
        return BasicEvent.isTypeNpc(getFlags());
    }

    /**
     * Check if the participant is a pet.
     * @return true if pet, false otherwise.
     */
    public boolean isPetObject() {
        return isPetObject;
    }

    /**
     * Set the pet status for this participant.
     * @param isPetObject true if pet, false otherwise.
     */
    public void setIsPetObject(boolean isPetObject) {
        this.isPetObject = isPetObject;
    }
    
    /**
     * Check if this participant has pets assigned
     * @return true if pets are present, false otherwise
     */
    public boolean hasPets() {
        if (getPetNames().size() > 0) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Returns true if this participant is a pet and if any of the listed participants owns it
     * @param participants The participants to check
     * @return A boolean describing the status
     */
    public boolean isPetAssignedSomewhere(List<FightParticipant> participants) {
        if (isPet() || isPetObject() || isGuardian()) {
            PetInfo thisPet = new PetInfo(this);
            ArrayList<PetInfo> names = new ArrayList<PetInfo>();
            for (FightParticipant p : participants) {
                names.addAll(p.getPetNames());
            }
            boolean exists = ListFunctions.contains(names, thisPet);
            return exists;
        } else {
            return false;
        }
    }

    /**
     * Get the participant name.
     * @return The name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the participant guid.
     * @return The guid.
     */
    public String getSourceGUID() {
        return sourceGUID;
    }

    /**
     * Set the participant guid.
     * @param sourceGUID The guid.
     */
    public void setSourceGUID(String sourceGUID) {
        this.sourceGUID = sourceGUID;
    }

    public void addSourceGUIDs(Collection<String> guids) {
        this.guids.addAll(guids);
    }

    public Set<String> getSourceGUIDs() {
        Set<String> retGuids = new HashSet<String>(this.guids);
        return retGuids;
    }

    public void clearGUIDs() {
        this.guids = new HashSet<String>();
    }

    /**
     * Get the participant flags.
     * @return The flags.
     */
    public long getFlags() {
        return flags;
    }

    /**
     * Get the events that this participant received, that is the participant is present in the destination field.
     * @return A collection with the events.
     */
    public EventCollectionSimple getEventsReceived() {
        return eventsReceived;
    }
    
    /**
     * Get the events that this participant performed, that is the participant is present in the source field.
     * @return A collection with the events.
     */
    public EventCollectionSimple getEventsPerformed() {
        return eventsMade;
    }

    /**
     * Get the pets that are associanted with this participant.
     * @return  A list with the pets.
     */
    public List<PetInfo> getPetNames() {
        return petNames;
    }

    /**
     * Set the pets that are associated with this participant.
     * @param petNames A list with the pets.
     */
    public void setPetNames(List<PetInfo> petNames) {
        this.petNames = petNames;
    }

    /**
     * Get the current health deficit for this participant.
     * @return The health deficit.
     */
    public int getHealthDeficit() {
        return healthDeficit;
    }

    /**
     * Set the current health deficit for this participant.
     * @param healthDeficit The health deficit.
     */
    public void setHealthDeficit(int healthDeficit) {
        this.healthDeficit = healthDeficit;
    }

    /**
     * Get the active power time for this collection. Requires the collection events to be sorted.
     * Overloaded method that takes pet power into account.
     * @param maxBreakTime The maximum time with no power
     * @param globalCooldown 2*globalCooldown is added to each coherent period of damage time because spells and abilities takes time to use.
     * @param startTime The start time to consider
     * @param endTime The end time to consider
     * @return The accumulated time that has activity
     */
    @Override
    public double getActivePowerTime(double maxBreakTime, double globalCooldown, double startTime, double endTime) {
        EventCollectionSimple tempCollection = new EventCollectionSimple(eventsMade.getEvents());
        for (PetInfo pi : getPetNames()) {
            tempCollection.addEventsAlways(pi);
        }
        tempCollection.sort();
        tempCollection.removeEqualElements();
        return tempCollection.getActivePowerTime(maxBreakTime, globalCooldown, startTime, endTime);
    }

    /**
     * Get the active healing time for this collection. Requires the collection events to be sorted.
     * Overloaded method that takes pet healing into account.
     * @param maxBreakTime The maximum time with no healing
     * @param globalCooldown 2*globalCooldown is added to each coherent period of damage time because spells and abilities takes time to use.
     * @param startTime The start time to consider
     * @param endTime The end time to consider
     * @return The accumulated time that has activity
     */
    public double getActiveHealingTime(double maxBreakTime, double globalCooldown, double startTime, double endTime) {
        EventCollectionSimple tempCollection = new EventCollectionSimple(eventsMade.getEvents());
        for (PetInfo pi : getPetNames()) {
            tempCollection.addEventsAlways(pi);
        }
        tempCollection.sort();
        tempCollection.removeEqualElements();
        return tempCollection.getActiveHealingTime(maxBreakTime, globalCooldown, startTime, endTime);
    }

    /**
     * Get the active damage time for this collection. Requires the collection events to be sorted.
     * Overloaded method that takes pet damage into account.
     * @param maxBreakTime The maximum time with no damage
     * @param globalCooldown 2*globalCooldown is added to each coherent period of damage time because spells and abilities takes time to use.
     * @param startTime The start time to consider
     * @param endTime The end time to consider
     * @return The accumulated time that has activity
     */
    public double getActiveDamageTime(double maxBreakTime, double globalCooldown, double startTime, double endTime) {
        EventCollectionSimple tempCollection = new EventCollectionSimple(eventsMade.getEvents());
        for (PetInfo pi : getPetNames()) {
            tempCollection.addEventsAlways(pi);
        }
        tempCollection.sort();
        tempCollection.removeEqualElements();
        return tempCollection.getActiveDamageTime(maxBreakTime, globalCooldown, startTime, endTime);
    }

    /**
     * Get the active damage+healing+power time for this collection. Requires the collection events to be sorted.
     * Overloaded method takes pets into account.
     * @param maxBreakTime The maximum time with no damage
     * @param globalCooldown 2*globalCooldown is added to each coherent period of time because spells and abilities takes time to use.
     * @param startTime The start time to consider
     * @param endTime The end time to consider
     * @return The accumulated time that has activity
     */
    public double getActiveTime(double maxBreakTime, double globalCooldown, double startTime, double endTime) {
        EventCollectionSimple tempCollection = new EventCollectionSimple(eventsMade.getEvents());
        for (PetInfo pi : getPetNames()) {
            tempCollection.addEventsAlways(pi);
        }
        tempCollection.sort();
        tempCollection.removeEqualElements();
        return tempCollection.getActiveTime(maxBreakTime, globalCooldown, startTime, endTime);
    }

    @Override
    public int getNumEventsTotal() {
        int numEvents = 0;
        numEvents += eventsMade.getNumEventsTotal();
        numEvents += eventsReceived.getNumEventsTotal();
        for (PetInfo pi : getPetNames()) {
            numEvents += pi.getNumEventsTotal();
        }
        return numEvents;
    }

    /**
     * Get all events from the FightParticipant.
     * This method can be overriden by classes that inherit from FightParticipant.
     * @return A list with the events.
     */
    public List<BasicEvent> getEventsTotal() {
        List<BasicEvent> out = new ArrayList<BasicEvent>(eventsMade.size());
        out.addAll(eventsMade.getEvents());
        out.addAll(eventsReceived.getEvents());
        for (PetInfo pi : petNames) {
            out.addAll(pi.getEventsTotal());
        }
        Collections.sort(out);
        ListFunctions.removeEqual(out);
        return out;
    }

    @Override
    public boolean hasDoublets() {
        boolean ret = eventsMade.hasDoublets();
        ret = ret | eventsReceived.hasDoublets();
        for (PetInfo pi : petNames) {
            ret = ret | pi.hasDoublets();
        }
        return ret;
    }

    @Override
    public void addEventAlways(BasicEvent e) {
        eventsMade.addEventAlways(e);
    }

    public void addEventReceivedAlways(BasicEvent e) {
        eventsReceived.addEventAlways(e);
    }

    @Override
    public FightParticipant filter(Filter f) {
        FightParticipant out = createEmptyShell();
        out.eventsMade.addEventsAlways(eventsMade.filter(f));
        out.eventsReceived.addEventsAlways(eventsReceived.filter(f));
        for (int k=0; k<petNames.size(); k++) {
            out.petNames.set(k, petNames.get(k).filter(f));
        }
        return out;
    }

    @Override
    public FightParticipant filterAnd(List<Filter> fs) {
        FightParticipant out = createEmptyShell();
        out.eventsMade.addEventsAlways(eventsMade.filterAnd(fs));
        out.eventsReceived.addEventsAlways(eventsReceived.filterAnd(fs));
        for (int k=0; k<petNames.size(); k++) {
            out.petNames.set(k, petNames.get(k).filterAnd(fs));
        }
        return out;
    }

    @Override
    public FightParticipant filterOr(List<Filter> fs) {
        FightParticipant out = createEmptyShell();
        out.eventsMade.addEventsAlways(eventsMade.filterOr(fs));
        out.eventsReceived.addEventsAlways(eventsReceived.filterOr(fs));
        for (int k=0; k<petNames.size(); k++) {
            out.petNames.set(k, petNames.get(k).filterOr(fs));
        }
        return out;
    }

    @Override
    public FightParticipant filterTime(double startTime, double endTime) {
        FightParticipant out = createEmptyShell();
        out.eventsMade.addEventsAlways(eventsMade.filterTime(startTime, endTime));
        out.eventsReceived.addEventsAlways(eventsReceived.filterTime(startTime, endTime));
        for (int k=0; k<petNames.size(); k++) {
            out.petNames.set(k, petNames.get(k).filterTime(startTime, endTime));
        }
        return out;
    }

    @Override
    public double getEndTimeFast() {
        return eventsMade.getEndTimeFast();
    }

    @Override
    public BasicEvent getEvent(int i) {
        return eventsMade.getEvent(i);
    }

    @Override
    public List<BasicEvent> getEvents() {
        return eventsMade.getEvents();
    }

    @Override
    protected List<BasicEvent> getEvents(double startTime, double endTime) {
        return eventsMade.getEvents(startTime, endTime);
    }

    @Override
    public BasicEvent getLastAdded() {
        return eventsMade.getLastAdded();
    }

    @Override
    public double getStartTimeFast() {
        return eventsMade.getStartTimeFast();
    }

    @Override
    public void removeLastAdded() {
        eventsMade.removeLastAdded();
    }

    @Override
    public void setEvents(Collection<BasicEvent> events) {
        eventsMade = new EventCollectionSimple();
        eventsMade.setEvents(events);
    }

    @Override
    public int size() {
        return eventsMade.size();
    }


}
