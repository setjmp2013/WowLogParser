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
import wowlogparserbase.events.healing.HealingEvent;
import wowlogparserbase.events.damage.DamageEvent;
import wowlogparserbase.events.SpellSummonEvent;
import wowlogparserbase.events.StatusEvent;
import wowlogparserbase.events.SpellLeechEvent;
import wowlogparserbase.events.UnknownEvent;
import wowlogparserbase.events.SpellEnergizeEvent;
import wowlogparserbase.events.UnitDiedEvent;
import wowlogparserbase.events.BasicEvent;
import wowlogparserbase.events.SpellDrainEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import wowlogparserbase.events.PartyKillEvent;
import wowlogparserbase.events.UnitDestroyedEvent;
import wowlogparserbase.events.damage.EnvironmentalDamageEvent;

/**
 * This class contains information about a fight. 
 * The EventCollection it extends from will contain all events that are present during the fight.
 * @author racy
 */
public class Fight extends AbstractEventCollection implements Comparable<Fight> {

    protected long fileUnixTime = 0;
    protected String name = null;
    protected String guid = null;
    protected HashSet<String> guids = new HashSet<String>();
    protected long flags = 0;
    protected boolean finished = false;
    protected boolean reallyFinished = false;
    protected boolean isDead = false;
    protected double finishedTime = 0;
    protected List<FightParticipant> participants = new ArrayList<FightParticipant>();
    protected FightParticipant victim = new FightParticipant();
    protected List<PetInfo> victimPets = new ArrayList<PetInfo>();
    protected HashMap<String, PetInfo> victimPetsGuidHash = new HashMap<String, PetInfo>();
    protected double lastDamageTime = 0;
    
    protected boolean merged = false;
    protected double mergedDuration = 0;
    protected List<Fight> mergedSourceFights = new ArrayList<Fight>();
    
    protected boolean isBoss = false;
    protected boolean bossFight = false;
    
    private List<FightParticipant> playerInfoStuff = new ArrayList<FightParticipant>();
    private List<FightParticipant> objectInfoStuff = new ArrayList<FightParticipant>();

    private EventCollectionSimple ec = new EventCollectionSimple();
    
    
    /**
     * Empty constructor
     */
    public Fight() {
        
    }

    /**
     * Set all events at construction time
     */
    public Fight(List<BasicEvent> evs) {
        ec = new EventCollectionSimple(evs);
    }

    /**
     * Constructor, makes a semi deep copy of the fight.
     * @param f The source fight to "copy"
     */
    public Fight(Fight f) {
        super(f);
        ec = new EventCollectionSimple(f.ec);
        name = f.name;
        guid = f.guid;
        guids = new HashSet<String>(f.guids);
        flags = f.flags;
        finished = f.finished;
        reallyFinished = f.reallyFinished;
        isDead = f.isDead;
        finishedTime = f.finishedTime;
        participants = new ArrayList<FightParticipant>(f.participants);
        victim = new FightParticipant(f.victim);
        victimPets = new ArrayList<PetInfo>();
        victimPetsGuidHash = new HashMap<String, PetInfo>();
        addVictimPets(f.getVictimPets());
        lastDamageTime = f.lastDamageTime;
        merged = f.merged;
        mergedDuration = f.mergedDuration;
        mergedSourceFights = new ArrayList<Fight>(f.mergedSourceFights);
        isBoss = f.isBoss;
        playerInfoStuff = f.playerInfoStuff;
        objectInfoStuff = f.objectInfoStuff;
        bossFight = f.bossFight;
    }

    /**
     * Create an empty Fight shell from this Fight. Only the first event is saved for time information issues.
     * @return The new emty shell Fight.
     */
    public Fight createEmptyShell() {
        Fight f = new Fight();
        f.name = name;
        f.guid = guid;
        f.guids = new HashSet<String>(guids);
        f.flags = flags;
        f.finished = finished;
        f.reallyFinished = reallyFinished;
        f.isDead = isDead;
        f.finishedTime = finishedTime;
        f.lastDamageTime = lastDamageTime;
        f.merged = merged;
        f.mergedDuration = mergedDuration;
        f.setActiveDuration(getActiveDuration());
        f.isBoss = isBoss;
        f.addEventAlways(getStartEvent());
        f.bossFight = bossFight;
        return f;
    }

    @Override
    public void createActiveDuration(double maxBreakTime, double globalCooldown) {
        ec.createActiveDuration(maxBreakTime, globalCooldown);
        victim.createActiveDuration(maxBreakTime, globalCooldown);
        setActiveDuration(ec.getActiveDuration());
    }

    /**
     * Find a certain participant in the fight. The compareTo method is used to find it.
     * @param p Information about the participant
     * @return A FightParticipant from this fight that matches the input information or null if not found.
     */
    public FightParticipant findParticipant(FightParticipant p) {
        for (FightParticipant fightP : participants) {
            if (fightP.compareTo(p) == 0) {
                return fightP;
            }
        }
        return null;
    }

    /**
     * Remove pets from this fight that are already assigned to a player.
     */
    public void removeAssignedPets() {
        for (int k=0; k<participants.size(); k++) {
            FightParticipant p = participants.get(k);
            if (p.isPetAssignedSomewhere(participants)) {
                participants.remove(k);
                k--;
                if (k >= participants.size()) {
                    break;
                }
            }
        }
    }
    
    /**
     * Sort the events in this fight.
     */
    public void sort() {
        ec.sort();
    }
        
    /**
     * Remove duplicate events from this fight and from the participants contained in this fight.
     */
    public void removeEqualElements() {
        ec.removeEqualElements();
        for (FightParticipant p : participants) {
            p.sort();
            p.removeEqualElements();
        }
    }
    
    /**
     * Find out if this is a merged fight
     * @return true if merged, false otherwise
     */
    public boolean isMerged() {
        return merged;
    }
    
    /**
     * Set merged state
     * @param merged The state
     */
    public void setMerged(boolean merged) {
        this.merged = merged;
    }

    /**
     * Find out if this fight includes participants from the same raid that the log creator is in.
     * @return true if found, false otherwise
     */
    public boolean includesRaidPeople() {
        boolean found = false;
        for(BasicEvent be : ec.getEvents()) {
            if (be instanceof DamageEvent) {
                if ( (be.getSourceFlags() & 
                        (Constants.FLAGS_OBJECT_AFFILIATION_MINE +
                        Constants.FLAGS_OBJECT_AFFILIATION_PARTY +
                        Constants.FLAGS_OBJECT_AFFILIATION_RAID)) != 0 ) {
                    found = true;
                }
            }
            if (found) {
                return found;
            }
        }
        return found;
        
    }
        
    /**
     * Merge a list of fights. The events are merged, equal participants are merged as well.
     * @param fights An array of fights
     * @return A new merged fight
     */
    public static Fight merge(List<Fight> fights) {
        return merge(fights, null, null);
    }

    /**
     * Merge a list of fights. The events are merged, equal participants are merged as well.
     * @param fights An array of fights
     * @param cb A callback object where the progress is reported, from 0 to the number of fights
     * @param cbStr A callback where progress is reported. When sorting is started "sort" is sent
     * @return A new merged fight
     */
    public static Fight merge(List<Fight> fights, IntCallback cb, StringCallback cbStr) {
        if (fights.size() == 0) {
            return null;
        }
//        if (fights.size() == 1) {
//            return new Fight(fights.get(0));
//        }
        Fight targetFight = new Fight();
        targetFight.setGUID("");
        targetFight.setName("Merged fights");
        targetFight.setFlags(fights.get(0).flags);
        targetFight.merged = true;
        targetFight.victim.setGUID(fights.get(0).victim.getSourceGUID());
        targetFight.victim.setName(fights.get(0).victim.getName());
        targetFight.victim.setFlags(fights.get(0).victim.getFlags());
        TreeSet<BasicEvent> eventSet = new TreeSet<BasicEvent>();
        LinkedList<FightParticipant> participants = new LinkedList<FightParticipant>();
        ArrayList<FightParticipant> victimList = new ArrayList<FightParticipant>();

        int num = 0;
        //Loop through the source fights
        for(Fight sourceFight : fights) {
            if (sourceFight.isMerged()) {
                targetFight.addGUIDs(sourceFight.getGuids());
                List<Fight> mergedFightInfo = targetFight.getMergedSourceFights();
                mergedFightInfo.addAll(sourceFight.getMergedSourceFights());
                targetFight.setMergedSourceFights(mergedFightInfo);
            } else {
                targetFight.addGUID(sourceFight.getGuid());
                List<Fight> mergedFightInfo = targetFight.getMergedSourceFights();
                mergedFightInfo.add(sourceFight.createEmptyShell());
                targetFight.setMergedSourceFights(mergedFightInfo);
            }
            num++;
            if (cb != null) {
                cb.reportInt(num);
            }
            //targetFight.events.addAll(sourceFight.events);
            eventSet.addAll(sourceFight.getEvents());
            targetFight.mergedDuration += sourceFight.getDuration();
            targetFight.playerInfoStuff = sourceFight.playerInfoStuff;
            targetFight.objectInfoStuff = sourceFight.objectInfoStuff;
            
            //Merge own events
            victimList.add(sourceFight.victim);
            
            //Merge victimPets
            List<PetInfo> petList = new ArrayList<PetInfo>();
            petList.addAll(sourceFight.getVictimPets());
            targetFight.addVictimPets(petList);
            
            //Loop through the source participants
            for(FightParticipant sourceParticipant : sourceFight.participants) {
                participants.add(sourceParticipant);
            }
        }
        //Merge equal pets in the target fight.
        targetFight.setVictimPets( PetInfo.mergeEqual(targetFight.getVictimPets()) );
        
        if (cbStr != null) {
            cbStr.reportString("Merging participants");
        }

        while(!participants.isEmpty()) {
            FightParticipant firstPart = participants.getFirst();
            ArrayList<FightParticipant> partList = new ArrayList<FightParticipant>();
            ListIterator<FightParticipant> it2 = participants.listIterator();
            while (it2.hasNext()) {
                FightParticipant secondPart = it2.next();
                if (firstPart.checkGUID(secondPart.getSourceGUID())) {
                    partList.add(secondPart);
                    it2.remove();
                }
            }
            FightParticipant p = FightParticipant.merge(partList);
            targetFight.participants.add(p);
        }
        
        if (cbStr != null) {
            cbStr.reportString("Merging victim participant");
        }

        FightParticipant victimPart = FightParticipant.merge(victimList);
        targetFight.victim = victimPart;
        targetFight.ec.setEvents(new ArrayList<BasicEvent>(eventSet));
        targetFight.findLastDamageTime();
        targetFight.createActiveDuration(15, 1.5);
        
        //Remove mergedSourceFight info doublets
        List<Fight> mergedFightInfo = targetFight.getMergedSourceFights();
        outer:
        while (true) {
            boolean found = false;
            for (int k = 0; k < mergedFightInfo.size(); k++) {
                for (int l = 0; l < mergedFightInfo.size(); l++) {
                    if (k == l) {
                        continue;
                    }
                    if (mergedFightInfo.get(k).getGuid().equals(mergedFightInfo.get(l).getGuid())) {
                        mergedFightInfo.remove(l);
                        continue outer;
                    }
                }
            }
            if (!found) {
                break;
            }
        }
        targetFight.setMergedSourceFights(mergedFightInfo);
        targetFight.createActiveDuration(15, 1.5);
        for (FightParticipant p : targetFight.getParticipants()) {
            p.createActiveDuration(15, 1.5);
        }
        return targetFight;
    }

    public static Fight createNewFightDuringFightTimePeriods(List<Fight> fights, FileLoader fl) {
        return createNewFightDuringFightTimePeriods(fights, fl, null, null);
    }

    /**
     * Merge a list of fights. The events are merged, equal participants are merged as well.
     * @param fights An array of fights
     * @param cb A callback object where the progress is reported, from 0 to the number of fights
     * @param cbStr A callback where progress is reported. When sorting is started "sort" is sent
     * @return A new merged fight
     */
    public static Fight createNewFightDuringFightTimePeriods(List<Fight> fights, FileLoader fl, IntCallback cb, StringCallback cbStr) {
        if (fights.size() == 0) {
            return null;
        }
        Fight targetFight = new Fight();
        targetFight.setGUID("");
        targetFight.setName("Merged fights");
        targetFight.setFlags(fights.get(0).flags);
        targetFight.setMerged(true);
        targetFight.victim.setGUID(fights.get(0).victim.getSourceGUID());
        targetFight.victim.setName(fights.get(0).victim.getName());
        targetFight.victim.setFlags(fights.get(0).victim.getFlags());

        int num = 0;
        List<TimePeriod> timePeriods = new ArrayList<TimePeriod>();
        //Loop through the source fights
        for(Fight sourceFight : fights) {
            if (sourceFight.isMerged()) {
                targetFight.addGUIDs(sourceFight.getGuids());
                List<Fight> mergedFightInfo = targetFight.getMergedSourceFights();
                mergedFightInfo.addAll(sourceFight.getMergedSourceFights());
                targetFight.setMergedSourceFights(mergedFightInfo);
            } else {
                targetFight.addGUID(sourceFight.getGuid());
                List<Fight> mergedFightInfo = targetFight.getMergedSourceFights();
                mergedFightInfo.add(sourceFight);
                targetFight.setMergedSourceFights(mergedFightInfo);
            }
            TimePeriod tp = sourceFight.getTimePeriod();
            timePeriods.add(tp);
            num++;
            if (cb != null) {
                cb.reportInt(num);
            }
            targetFight.playerInfoStuff = sourceFight.playerInfoStuff;
            targetFight.objectInfoStuff = sourceFight.objectInfoStuff;
        }

        //Remove mergedSourceFight info doublets
        List<Fight> mergedFightInfo = targetFight.getMergedSourceFights();
        outer:
        while (true) {
            boolean found = false;
            for (int k = 0; k < mergedFightInfo.size(); k++) {
                for (int l = 0; l < mergedFightInfo.size(); l++) {
                    if (k == l) {
                        continue;
                    }
                    if (mergedFightInfo.get(k).getGuid().equals(mergedFightInfo.get(l).getGuid())) {
                        mergedFightInfo.remove(l);
                        continue outer;
                    }
                }
            }
            if (!found) {
                break;
            }
        }
        targetFight.setMergedSourceFights(mergedFightInfo);

        timePeriods = TimePeriod.mergeTimePeriods(timePeriods);
        List<BasicEvent> newFightEvents = fl.getEventCollection().getEvents(timePeriods);
        for (BasicEvent e : newFightEvents) {
            targetFight.addEvent(e);
        }

        targetFight.mergedDuration = 0;
        for (TimePeriod tp : timePeriods) {
            targetFight.mergedDuration += tp.getDuration();
        }

        targetFight.makeParticipants(fl.getFightCollection().getPlayerInfo(), fl.getFightCollection().getObjectInfo());
        if (SettingsSingleton.getInstance().getAutoPlayerPets()) {
            List<Fight> tempList = new ArrayList<Fight>();
            tempList.add(targetFight);
            PetParser.insertPetsOnPlayers(fl.getPetParser().getParsedPlayers(), tempList);
        }
        targetFight.removeAssignedPets();

        targetFight.createActiveDuration(15, 1.5);
        for (FightParticipant p : targetFight.getParticipants()) {
            p.createActiveDuration(15, 1.5);
        }
        return targetFight;
    }

    /**
     * Merge all fights that have the same name.
     * @param fights The fights
     * @param prefix A prefix to put in front of the fight name after merging.
     * @return A list with the merged fights.
     */
    public static List<Fight> mergeEqualFights(List<Fight> fights, String prefix) {
        List<List<Fight>> fightGrouping = new ArrayList<List<Fight>>();        
        List<Fight> fightsCopy = new ArrayList<Fight>(fights);
        
        //Find mobs with equal names and group them up.
        while (true) {
            outerFor:
            for (int k = 0; k < fightsCopy.size(); k++) {
                Fight f = fightsCopy.get(k);
                boolean found = false;
                for (List<Fight> fightGroupingSingle : fightGrouping) {
                    if (fightGroupingSingle.size() == 0) {
                        continue;
                    }
                    Fight testFight = fightGroupingSingle.get(0);
                    if (f.getName().equalsIgnoreCase(testFight.getName())) {
                        fightGroupingSingle.add(f);
                        fightsCopy.remove(k);
                        break outerFor;
                    }
                }
                if (!found) {
                    List<Fight> newGrouping = new ArrayList<Fight>();
                    newGrouping.add(f);
                    fightGrouping.add(newGrouping);
                    fightsCopy.remove(k);
                    break outerFor;
                }
            }
            if (fightsCopy.size() == 0) {
                break;
            }
        }
        
        //Merge the groupings
        List<Fight> mergedFights = new ArrayList<Fight>();
        for (List<Fight> fightGroupingSingle : fightGrouping) {
            Fight mergedFight = Fight.merge(fightGroupingSingle);
            mergedFight.setName(prefix + fightGroupingSingle.get(0).getName());
            mergedFights.add(mergedFight);
        }
        
        return mergedFights;
    }
    
    /**
     * Find the last time where a damage event was performed and save it in an internal variable. 
     * The fight has to be sorted.
     */
    public void findLastDamageTime() {
        int k;
        for(k=size()-1; k>=0; k--) {
            BasicEvent be = ec.getEvent(k);
            if (be instanceof DamageEvent) {
                DamageEvent e = (DamageEvent) be;
                lastDamageTime = e.time;
                break;
            }
        }
    }
    
    /**
     * Close the fight so that no more events will be added to it from the addEvent method.
     */
    public void closeFight() {
        int k, l;
        finished = true;
        reallyFinished = true;
        LinkedList<BasicEvent> tempList = new LinkedList<BasicEvent>(getEvents());
        ListIterator<BasicEvent> fIt = tempList.listIterator(tempList.size());
        while(fIt.hasPrevious()) {
            BasicEvent e = fIt.previous();
            if (e.time > lastDamageTime) {
                fIt.remove();
            }
        }
        ec.setEvents(new ArrayList<BasicEvent>(tempList));
        createActiveDuration(15, 1.5);
    }
    
    /**
     * Get the duration of the fight. If it is a merged fight the sum of all source fights will be returned.
     * @return The duration
     */
    public double getDuration() {
        if (merged) {
            return mergedDuration;
        } else {
            if (size() > 0) {
                return getEndTime() - getStartTime();
            } else {
                return 0;
            }
        }
    }
    
    /**
     * Return the last damage time.
     * @return the last damage time
     */
    double getLastDamageTime() {
        return lastDamageTime;
    }
    
    public void addGUIDs(Collection<String> newGuids) {
        guids.addAll(newGuids);
    }

    public void addGUID(String guid) {
        guids.add(guid);
    }

    public Set<String> getGuids() {
        return new HashSet<String>(this.guids);
    }

    /**
     * Set the guid of the victim
     * @param guid The guid
     */
    public void setGUID(String guid) {
        this.guid = guid;
        victim.setGUID(guid);
    }
    
    /**
     * Set the name of the victim
     * @param name The name
     */
    public void setName(String name) {
        this.name = name;
        victim.setName(name);
    }
    
    /**
     * Set the flags for the victim
     * @param flags The flags
     */
    public void setFlags(long flags) {
        this.flags = flags;
        victim.setFlags(flags);
    }

    /**
     * Check if the victims guid matches the supplied one
     * @param guid The guid to check
     * @return true if they match, false otherwise
     */
    boolean checkGUID(String guid) {
        if (guid.equals(this.guid)) {
            return true;
        } else {
            if (this.guids.contains(guid)) {
                return true;
            } else {
                return false;
            }
        }
    }
    
    /**
     * Add a pet to the victim.
     * @param p The pet.
     */
    protected void addVictimPet(PetInfo p) {
        victimPets.add(p);
        victimPetsGuidHash.put(p.getSourceGUID(), p);
    }
    
    /**
     * Add pets to the victim.
     * @param pets The pets.
     */
    protected void addVictimPets(List<PetInfo> pets) {
        for (PetInfo p : pets) {
            addVictimPet(p);
        }
    }
    
    protected void clearVictimPets() {
        victimPets = new ArrayList<PetInfo>();
        victimPetsGuidHash = new HashMap<String, PetInfo>();
    }
    
    /**
     * Set victim pets.
     * @param pets The pets.
     */
    protected void setVictimPets(List<PetInfo> pets) {
        clearVictimPets();
        for (PetInfo p : pets) {
            addVictimPet(p);
        }
    }

    /**
     * Get a mapping from pet guids to PetInfo objects.
     * @return A map with the mapping.
     */
    protected Map<String, PetInfo> getVictimPetsHash() {
        return victimPetsGuidHash;
    }
    
    /**
     * Add events to the fight. Status events that does not belong to the fight 
     * but is within the same time period is also added.
     * 
     * @param e The event to add
     * @return true if an event that belongs to the fight is added, false if it does not belong to the fight(can still be added though)
     */
    public boolean addEvent(BasicEvent e) {
        if (isReallyFinished()) {
            return false;
        }
        
        if (isFinished()) {
            //If more events happens at the same time(or close to the same time) as the unit_died event happened(dots etc), we dont want a new fight to start. 
            //Give it a 15 second leeway to finish and only add events that have the fights GUID in the event.
            if (isDead()) {
                if (Math.abs(e.time - finishedTime) > 15) {
                    reallyFinished = true;
                    return false;
                }
                if (checkGUID(e.getSourceGUID()) || checkGUID(e.getDestinationGUID())) {
                    //Let it continue if the victim guid is in the event.
                } else {
                    return false;
                }
            }
        }
        
        //Initialize lastDamageTime so that the fight is not closed instantly
        if (size() == 0) {
            lastDamageTime = e.time;
        }
        
        //Close fight if it has been open without damage for a long time?
        if (size() > 0) {
            double currentTime = e.time;
            if (currentTime - getLastDamageTime() > SettingsSingleton.getInstance().getMaxInactiveTime()) {
                closeFight();
                return false;
            }
        }
        
        if (e instanceof SpellSummonEvent) {
            SpellSummonEvent se = (SpellSummonEvent) e;
            if (checkGUID(se.getSourceGUID())) {
                PetInfo p = new PetInfo();
                p.setSourceGUID(se.getDestinationGUID());
                p.setFlags(se.getDestinationFlags());
                p.setName(se.getDestinationName());
                p.setIsPetObject(true);
                addVictimPet(p);
                ec.addEvent(e);
                return true;
            }
        }

        if (e instanceof HealingEvent) {
            ec.addEvent(e);
            return false;
        }
        
        if (e instanceof SpellEnergizeEvent) {
            ec.addEvent(e);
            return false;
        }

        // Check if a pet dies and flag it as dead if so. Set as finished if the main mob is dead as well as all pets.
        if (e instanceof UnitDiedEvent || e instanceof PartyKillEvent || e instanceof UnitDestroyedEvent) {
            PetInfo petI = victimPetsGuidHash.get(e.getDestinationGUID());
            if (petI != null) {
                petI.setDead(true);
            }
            boolean allDead = isDead();
            for (PetInfo pi : getVictimPets()) {
                if (!pi.isDead()) {
                    allDead = false;
                    break;
                }
            }
            if (allDead) {
                finished = true;
                finishedTime = e.time;
            }
        }
        
        boolean added = false;
        //If the event is done to the victim or the victim does anything to someone else
        //That is the victims GUID is present in the event.
        if (checkGUID(e.getDestinationGUID()) || checkGUID(e.getSourceGUID())) {
            if (e instanceof StatusEvent) {
                if ((e instanceof UnitDiedEvent || e instanceof UnitDestroyedEvent) && (!isMerged())) {
                    if (checkGUID(e.getDestinationGUID())) {
                        if (SettingsSingleton.getInstance().getPetsAsOwner()) {
                            if (getVictimPets().size() == 0) {
                                finished = true;
                                finishedTime = e.time;
                            } else {
                                // TODO check if all pets are dead.
                                boolean allDead = true;
                                for (PetInfo pi : getVictimPets()) {
                                    if (!pi.isDead()) {
                                        allDead = false;
                                        break;
                                    }
                                }
                                if (allDead) {
                                    finished = true;
                                    finishedTime = e.time;
                                } else {
                                    finished = false;
                                }
                            }
                        } else {
                            finished = true;
                            finishedTime = e.time;
                        }
                        isDead = true;
                        ec.addEvent(e);
                        added = true;
                    }
                } else {
                    ec.addEvent(e);
                    added = true;
                }
                return added;
            }
            if (e instanceof DamageEvent) {
                lastDamageTime = e.time;
            }
            ec.addEvent(e);
            added = true;            
        } else {
            //We come here if the event does not have the victims GUID

            //If petsAsOwner is set, then add pet events to the fight itself
            if (SettingsSingleton.getInstance().getPetsAsOwner()) {
                if (getVictimPetsHash().get(e.getSourceGUID()) != null) {
                    //e.sourceGUID = guid;
                    ec.addEvent(e);
                    return true;
                }
                if (getVictimPetsHash().get(e.getDestinationGUID()) != null) {
                    //e.destinationGUID = guid;
                    ec.addEvent(e);
                    return true;
                }
            }
            //Add environmental damage
            if (e instanceof EnvironmentalDamageEvent) {
                ec.addEvent(e);
                return false;
            }
            //Damage without source gets added, can be some kind of fight damage.
            if ( (e instanceof DamageEvent) && (e.getSourceGUID().equalsIgnoreCase("0x0000000000000000")) ) {
                ec.addEvent(e);
                return false;
            }
            if ( !(e instanceof DamageEvent) && !(e instanceof HealingEvent) && !(e instanceof SpellLeechEvent) && !(e instanceof SpellDrainEvent)) {
                // Add events not related to the victim to the fight
                // only if they are not Damage, healing or power events.
                // Also let the event continue to other fights by returning false.
                ec.addEvent(e);
                added = false;
            } else {
                added = false;
            }
        }
        return added;
    }
    
    /**
     * Returns the finished status
     * @return true if the fight is finished, false otherwise
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * Returns the finished status, only returns true if the time has gone past the actual time that the unit_died event was received.
     * @return true if the fight is finished, false otherwise
     */
    public boolean isReallyFinished() {
        return reallyFinished;
    }

    /**
     * Check if the victim is dead
     * @return true if dead, false otherwise
     */
    public boolean isDead() {
        return isDead;
    }

    /**
     * Set the dead status of the fight.
     * @param dead true if dead, false otherwise
     */
    public void setDead(boolean dead) {
        isDead = dead;
    }
    
    /**
     * Add a new player participant to the fight.
     * @param e The initial event.
     * @param name The player name.
     * @param flags The player flags.
     * @param guid The player guid.
     * @return The new participant.
     */
    protected FightParticipant newPlayerParticipant(BasicEvent e, String name, long flags, String guid) {
        FightParticipant fp = new FightParticipant();
        fp.setGUID(guid);
        fp.setName(name);
        fp.setFlags(flags);
        fp.addEvent(e);
        for (FightParticipant pInfo : playerInfoStuff) {
            if (pInfo.getSourceGUID().equals(fp.getSourceGUID())) {
                fp.setPlayerClass(pInfo.getPlayerClass());
            }
        }
        participants.add(fp);
        return fp;
    }
    
    /**
     * Add a new object participant to the fight.
     * @param e The initial event.
     * @param name The object name.
     * @param flags The object flags.
     * @param guid The object guid.
     * @return The new participant.
     */
    protected FightParticipant newObjectParticipant(BasicEvent e, String name, long flags, String guid) {
        FightParticipant fp = new FightParticipant();
        fp.setGUID(guid);
        fp.setName(name);
        fp.setFlags(flags);
        fp.addEvent(e);
        fp.setIsPetObject(true);
        participants.add(fp);
        return fp;
    }
    
    /**
     * Make participants for this fight.
     * @param playerInfoStuff An array of players with class information
     * @param objectInfoStuff An array with objects that can be participants.
     */
    public void makeParticipants(List<FightParticipant> playerInfoStuff, List<FightParticipant> objectInfoStuff) {
        this.playerInfoStuff = playerInfoStuff;
        this.objectInfoStuff = objectInfoStuff;
        makeParticipants();
    }
        
    /**
     * Make participants for this fight
     */
    public void makeParticipants() {
        int k, l;
        HashMap<String, FightParticipant> partHash = new HashMap<String, FightParticipant>();
        HashSet<String> objInfo = new HashSet<String>();
        for (FightParticipant p : objectInfoStuff) {
            objInfo.add(p.getSourceGUID());
        }
        
        victim = new FightParticipant();
        victim.setName(name);
        victim.setGUID(guid);
        victim.addSourceGUIDs(getGuids());
        victim.setFlags(flags);
        List<PetInfo> victimPetsPart = new ArrayList<PetInfo>();
        for (PetInfo pet : getVictimPets()) {
            victimPetsPart.add(pet.createEmptyShell());
        }
        victim.setPetNames(victimPetsPart);

        participants = new ArrayList<FightParticipant>();
        for (k=0; k<ec.size(); k++) {
            BasicEvent be = ec.getEvent(k);
            boolean foundParticipant = false;

            FightParticipant sPart = partHash.get(be.getSourceGUID());
            FightParticipant dPart = partHash.get(be.getDestinationGUID());
            if (sPart!=null) {
                foundParticipant = foundParticipant | sPart.addEvent(be);
            }
            if (dPart!=null) {
                foundParticipant = foundParticipant | dPart.addEvent(be);
            }

             //New participant perhaps
            if (!foundParticipant) {
                boolean sourceExists = false;
                boolean destinationExists = false;
                //If none of
                if (!be.getSourceName().equals("nil")) {
                    sourceExists = true;
                }
                
                if (!be.getDestinationName().equals("nil")) {
                    destinationExists = true;
                }

                boolean made = false;
                if (sourceExists) {
                    String currGuid = be.getSourceGUID();
                    String currName = be.getSourceName();
                    long currFlags = be.getSourceFlags();
                    if (!made) {
                        //Add a new participant if its a player or pet
                        if (BasicEvent.isTypePlayer(currFlags) || BasicEvent.isTypePet(currFlags) || BasicEvent.isTypeGuardian(currFlags)) {
                            if (BasicEvent.isReactionFriendly(currFlags)) {
                                if (!(be instanceof UnknownEvent)) {
                                    FightParticipant fp = newPlayerParticipant(be,currName, currFlags, currGuid);
                                    partHash.put(fp.getSourceGUID(), fp);
                                    made = true;
                                }
                            }
                        }
                    }
                    if (!made) {
                        //Check if it is an object
                        if (objInfo.contains(currGuid)) {
                            FightParticipant fp = newObjectParticipant(be,currName, currFlags, currGuid);
                            partHash.put(fp.getSourceGUID(), fp);
                            made = true;
                        }
                    }
                }
                if (destinationExists) {
                    String currGuid = be.getDestinationGUID();
                    String currName = be.getDestinationName();
                    long currFlags = be.getDestinationFlags();
                    if (!made) {
                        //Add a new participant if its a player or pet
                        if (BasicEvent.isTypePlayer(currFlags) || BasicEvent.isTypePet(currFlags) || BasicEvent.isTypeGuardian(currFlags)) {
                            if (BasicEvent.isReactionFriendly(currFlags)) {
                                if (!(be instanceof UnknownEvent)) {
                                    FightParticipant fp = newPlayerParticipant(be,currName, currFlags, currGuid);
                                    partHash.put(fp.getSourceGUID(), fp);
                                    made = true;
                                }
                            }
                        }
                    }
                    if (!made) {
                        //Check if it is an object
                        if (objInfo.contains(currGuid)) {
                            FightParticipant fp = newObjectParticipant(be,currName, currFlags, currGuid);
                            partHash.put(fp.getSourceGUID(), fp);
                            made = true;
                        }
                    }
                }
            }
            //Add to as the victims own damage or add to its pets?
             victim.addEvent(be);
             for (PetInfo pet : victimPetsPart) {
                 pet.addEvent(be);
             }
        }
        victim.createActiveDuration(15, 1.5);
        for (FightParticipant p : participants) {
            p.createActiveDuration(15, 1.5);
        }
    }

    /**
     * Parse the boss information to find out if this fight is vs an NPC that is present in the boss section of the XML file.
     */
    public void parseBossStatus() {
        XmlInfoParser p = XmlInfoParser.getInstance();
        List<String> bossStrings = p.getMobNames();
        String lowerName = name.toLowerCase();
        for (String s : bossStrings) {
            if (lowerName.equals(s.toLowerCase())) {
                setIsBoss(true);
                return;
            }
        }
        setIsBoss(false);
    }
    
    /**
     * Set the isBoss flag.
     * @param isBoss true or false.
     */
    public void setIsBoss(boolean isBoss) {
        this.isBoss = isBoss;
    }

    /**
     * Get the is boss status.
     * @return true or false.
     */
    public boolean isBoss() {
        return isBoss;
    }

    /**
     * Get the fight flags.
     * @return The flags.
     */
    public long getFlags() {
        return flags;
    }

    /**
     * Get the fight guid.
     * @return The guid.
     */
    public String getGuid() {
        return guid;
    }

    /**
     * Get the fight name.
     * @return The name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the participants in this fight.
     * @return The fight participants.
     */
    public List<FightParticipant> getParticipants() {
        return participants;
    }

    /**
     * Get the enemy participants in this fight.
     * @return The fight participants.
     */
    public List<FightParticipant> getEnemyParticipants() {
        List<FightParticipant> enemyParticipants = new ArrayList<FightParticipant>();
        if (isMerged()) {
            for(Fight f : getMergedSourceFights()) {
                enemyParticipants.add(f.getVictim());
            }
        } else {
            enemyParticipants.add(getVictim());
        }
        return enemyParticipants;
    }

    /**
     * Get the victim participant.
     * @return The victim participant.
     */
    public FightParticipant getVictim() {
        return victim;
    }

    /**
     * If this fight is merged then get the fights that were used to create it.
     * @return A list of source fights.
     */
    public List<Fight> getMergedSourceFights() {
        return mergedSourceFights;
    }

    /**
     * Set the merged source fights. Any current source fights are lost.
     * @param mergedSourceFights A list of fights.
     */
    public void setMergedSourceFights(List<Fight> mergedSourceFights) {
        this.mergedSourceFights = mergedSourceFights;
    }

    /**
     * Get the pets that the victim has summoned.
     * @return A list of pets.
     */
    public List<PetInfo> getVictimPets() {
        return victimPets;
    }

    /**
     * Get the mob type ID from the GUID. If the GUID is not present -1 is returned.
     * 
     * If the fight is a merged fight, then all source fights are traversed and
     * if all the mob IDs are the same it is returned, otherwise -1 is returned.
     * @return The ID
     */
    public int getMobID() {
        if (isMerged()) {
            int id = -1;
            for (Fight f : mergedSourceFights) {
                int currentID = f.getMobID();
                if (id == -1) {
                    id = currentID;
                } else {
                    if (currentID != id) {
                        id = -1;
                        break;
                    }
                }
            }
            return id;
        } else {
            return BasicEvent.getNpcID(getGuid());
        }
    }
    
    /**
     * Get mob IDs from a merged fight.
     * 
     * if the fight is not merged then an array only containing the ID of 
     * the current fight mob is returned.
     * 
     * Any ID that cannot be decided is set to -1.
     * 
     * @return The IDs
     */
    public List<Integer> getMobIDs() {
        ArrayList<Integer> ids = new ArrayList<Integer>();
        if (isMerged()) {
            for (Fight f : mergedSourceFights) {
                int id = f.getMobID();
                ids.add(id);
            }
        } else {
            ids.add(getMobID());
        }
        return ids;
    }

    /**
     * Compare to another fight
     * @param o The fight to compare it to
     * @return 0 if the fights start at the same time, -1 if this fight starts before f and 1 if f starts before this fight
     */
    public int compareTo(Fight f) {
        return (int)Math.signum(getStartTime()-f.getStartTime());
    }
    
    /**
     * Set the time of the last damage event.
     * @param time The time.
     */
    public void setLastDamageTime(double time) {
        this.lastDamageTime = time;
    }
    
    public int getNumberOfDamageEvents() {
        int num = 0;
        for (BasicEvent e : ec.getEvents()) {
            if (e instanceof DamageEvent) {
                num++;
            }
        }
        return num;
    }

    public long getFileUnixTime() {
        return fileUnixTime;
    }

    public void setFileUnixTime(long fileUnixTime) {
        this.fileUnixTime = fileUnixTime;
    }

    /**
     * Get status if this fight is a boss fight (boss encounter)
     * @return
     */
    public boolean isBossFight() {
        return bossFight;
    }

    /**
     * Set that this fight is a boss fight (boss encounter)
     * @param isBossFight
     */
    public void setBossFight(boolean isBossFight) {
        this.bossFight = isBossFight;
    }

    @Override
    public void addEventAlways(BasicEvent e) {
        ec.addEventAlways(e);
    }

    @Override
    public EventCollection filter(Filter f) {
        EventCollection out = ec.filter(f);
        return out;
    }

    @Override
    public EventCollection filterAnd(List<Filter> fs) {
        EventCollection out = ec.filterAnd(fs);
        return out;
    }

    @Override
    public EventCollection filterOr(List<Filter> fs) {
        EventCollection out = ec.filterOr(fs);
        return out;
    }

    @Override
    public EventCollection filterTime(double startTime, double endTime) {
        EventCollection out = ec.filterTime(startTime, endTime);
        return out;
    }

    @Override
    public double getEndTimeFast() {
        return ec.getEndTimeFast();
    }

    @Override
    public BasicEvent getEvent(int i) {
        return ec.getEvent(i);
    }

    @Override
    public List<BasicEvent> getEvents() {
        return ec.getEvents();
    }

    @Override
    protected List<BasicEvent> getEvents(double startTime, double endTime) {
        return ec.getEvents(startTime, endTime);
    }

    @Override
    public BasicEvent getLastAdded() {
        return ec.getLastAdded();
    }

    @Override
    public int getNumEventsTotal() {
        int numEvents = 0;
        numEvents += ec.getNumEventsTotal();
        return numEvents;
    }

    @Override
    public List<BasicEvent> getEventsTotal() {
        return new ArrayList<BasicEvent>(ec.getEvents());
    }

    @Override
    public double getStartTimeFast() {
        return ec.getStartTimeFast();
    }

    @Override
    public void removeLastAdded() {
        ec.removeLastAdded();
    }

    @Override
    public void setEvents(Collection<BasicEvent> events) {
        ec.setEvents(events);
    }

    @Override
    public int size() {
        return ec.size();
    }

}
