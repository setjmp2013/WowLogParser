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

import wowlogparserbase.events.damage.DamageEvent;
import wowlogparserbase.events.SpellSummonEvent;
import wowlogparserbase.events.LogEvent;
import wowlogparserbase.events.BasicEvent;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * A Collection of fights. Is responsible for the initial parsing of fights.
 * @author racy
 */
public class FightCollection {

    private List<Fight> fights = new ArrayList<Fight>();
    private LinkedList<Fight> activeFights = new LinkedList<Fight>();
    private List<FightParticipant> playerInfo = new ArrayList<FightParticipant>();
    private List<FightParticipant> objectInfo = new ArrayList<FightParticipant>();
            
    /**
     * Constructor that specifies the maximum inactive time before a fight is closed.
     */
    public FightCollection() {
    }
    
    /**
     * Specify that no more events will be added. All created fights will be closed.
     */
    protected void noMoreEvents() {
        for (Fight f : fights) {
            if (!f.isReallyFinished()) {
                f.closeFight();
            }
        }
    }
    
    /**
     * Add an event to the fight collection. New fights will be created when neccessary.
     * A new fight can only be created when a DamageEvent occurs, otherwise events are only added to existing fights.
     * @param e The event.
     */
    protected void addEvent(LogEvent e) {
        int k;
        if (e instanceof BasicEvent) {
            BasicEvent be = (BasicEvent)e;
            boolean fightFound = false;
            ListIterator<Fight> fIt = activeFights.listIterator(activeFights.size());
            while(fIt.hasPrevious()){
                Fight f = fIt.previous();
                //If we have one of the victims own events
                fightFound = fightFound | f.addEvent(be);
                if (f.isReallyFinished()) {
                    fIt.remove();
                }
            }

            startFightIfNPCSummonsPet(be);            
                
            //If we already found a fight for the event we do not need to process it further.
            if (fightFound) {
                return;
            }

            //Not added yet, check if a new fight is started
            if (checkIfStartFightEventFriendlyVsEnemy(be)) {
                //New fight.
                Fight f = newFight(be.getDestinationGUID(), be.getDestinationName(), be.getDestinationFlags(), be);
            }

            if (checkIfStartFightEventEnemyVsFriendly(be)) {
                //New fight.
                Fight f = newFight(be.getSourceGUID(), be.getSourceName(), be.getSourceFlags(), be);
            }
        }

    }
    
    /**
     * Create a new fight.
     * @param GUID The fight guid.
     * @param Name The fight name.
     * @param The fight flags.
     * @param e The initial event.
     * @return The new fight.
     */
    protected Fight newFight(String GUID, String Name, long flags, BasicEvent e) {
        Fight f = new Fight();
        f.setGUID(GUID);
        f.setName(Name);
        f.setFlags(flags);
        f.addEvent(e);
        f.parseBossStatus();
        fights.add(f);
        activeFights.add(f);
        return f;
    }
    
    /**
     * Start a fight if an NPC summons a pet. This is done so that pets are correctly assigned to owners.
     * A new fight is only started on a SpellSummonEvent.
     * @param e The event to check.
     */
    void startFightIfNPCSummonsPet(BasicEvent e) {
        //Not added yet, check if a new fight is started
        if (e instanceof SpellSummonEvent) {
            SpellSummonEvent se = (SpellSummonEvent) e;
            //Only summons done by NPCs
            long checkFlags = (Constants.FLAGS_OBJECT_CONTROL_NPC | Constants.FLAGS_OBJECT_TYPE_NPC );
            if ((se.getSourceFlags() & checkFlags) != 0) {
                //Cannot be a friendly NPC
                if ((se.getSourceFlags() & Constants.FLAGS_OBJECT_REACTION_FRIENDLY) == 0) {
                    if (!SettingsSingleton.getInstance().getPetsAsOwner()) {
                        boolean fightFound = false;
                        //Check if the mob that summons has a fight
                        for (int k = fights.size() - 1; k >= 0; k--) {
                            Fight f = fights.get(k);
                            if (f.checkGUID(se.getSourceGUID())) {
                                fightFound = true;
                                break;
                            }
                        }
                        //New fight for the mob that summons the pets if not found
                        if (!fightFound) {
                        Fight f = newFight(se.getSourceGUID(), se.getSourceName(), se.getSourceFlags(), se);
                        }

                        fightFound = false;
                        //Check if the summoned mob has a fight
                        for (int k = fights.size() - 1; k >= 0; k--) {
                            Fight f = fights.get(k);
                            if (f.checkGUID(se.getDestinationGUID())) {
                                fightFound = true;
                                break;
                            }
                        }
                        //Start a new fight with the summoned object
                        if (!fightFound) {
                            //New fight.
                            Fight f = newFight(se.getDestinationGUID(), se.getDestinationName(), se.getDestinationFlags(), se);
                        }
                    } else {
                        //petsAsOwner==true and we get here.
                        boolean fightFound = false;
                        //Check if the mob that summons has a fight
                        for (int k = fights.size() - 1; k >= 0; k--) {
                            Fight f = fights.get(k);
                            if (f.checkGUID(se.getSourceGUID())) {
                                fightFound = true;
                                break;
                            }
                        }
                        //New fight for the mob that summons the pets
                        if (!fightFound) {
                            Fight f = newFight(se.getSourceGUID(), se.getSourceName(), se.getSourceFlags(), se);
                        }                        
                    }
                }
            }
        }        
    }
    
    /**
     * Get all fights
     * @return The fights
     */
    public List<Fight> getFights() {
        return fights;
    }

    /**
     * Set a fights array
     * @param fights The fights
     */
    public void setFights(List<Fight> fights) {
        this.fights = fights;
    }

    /**
     * Get the player info array containing empty FightParticipant:s with class info
     * @return An array with information about classes.
     */
    public List<FightParticipant> getPlayerInfo() {
        return playerInfo;
    }

    /**
     * Get the object info array containing empty FightParticipant:s with objects that are summoned by a player, such as totems.
     * @return An array with information about classes.
     */
    public List<FightParticipant> getObjectInfo() {
        return objectInfo;
    }
    
    /**
     * Perform post processing after all events have been added.
     * Remove fights with no damage events in them.
     * Create active duration for fights.
     */
    public void postProcessing() {
        for (Fight f : fights) {
            f.createActiveDuration(15, 1.5);
        }
        ListIterator<Fight> it = fights.listIterator();
        while (it.hasNext()) {
            Fight f = it.next();
            if (f.getNumberOfDamageEvents() == 0) {
                it.remove();
            }
        }
    }
    
    /**
     * Get the active time periods from all fights. If fights overlap then the time periods are merged.
     * @return The created time periods
     */
    public List<TimePeriod> getAllFightPeriods() {
        return getFightPeriods(fights);
    }
    
    /**
     * Get the active time periods from the fights in fs. If fights overlap then the time periods are merged.
     * @param fs The fights
     * @return The created time periods
     */
    public List<TimePeriod> getFightPeriods(List<Fight> fs)  {
        List<TimePeriod> periods = new ArrayList<TimePeriod>();
        for (Fight f : fs) {
            TimePeriod tp = new TimePeriod(f.getStartTime(), f.getEndTime());
            periods.add(tp);
        }
        return TimePeriod.mergeTimePeriods(periods);
    }    
    
    /**
     * Create fights with the events from the supplied collection.
     * @param ec The EventCollection to use.
     */
    public void createFights(EventCollection ec) {
        List<BasicEvent> events = ec.getEvents();
        for(BasicEvent e : events) {
            addEvent(e);
        }
        noMoreEvents();
        postProcessing();

        PlayerClassParser pcp = new PlayerClassParser(ec);
        playerInfo = pcp.getPlayerInfo();
        ObjectParser op = new ObjectParser(ec);
        objectInfo = op.getObjectInfo();
        for (Fight fi : getFights()) {
            fi.makeParticipants(getPlayerInfo(), getObjectInfo());
        }
    }
    
    /**
     * Create only one total fight from the supplied events.
     * @param ec The EventCollection to use.
     */
    public void createTotalFight(EventCollection ec) {
        Fight f = new Fight(ec.getEvents());
        f.setGUID("");
        f.setName("Total");
        f.setFlags(0);
        f.setMerged(false);
        f.setLastDamageTime(f.getEndTimeFast());
        fights.add(f);

        noMoreEvents();
        postProcessing();

        PlayerClassParser pcp = new PlayerClassParser(ec);
        playerInfo = pcp.getPlayerInfo();
        ObjectParser op = new ObjectParser(ec);
        objectInfo = op.getObjectInfo();
        for (Fight fi : getFights()) {
            fi.makeParticipants(getPlayerInfo(), getObjectInfo());
        }
    }

    private boolean checkIfStartFightEventEnemyVsFriendly(BasicEvent e) {
        if (e instanceof DamageEvent) {
            DamageEvent de = (DamageEvent) e;
            //Start new fight with a damage event if the source is a hostile NPC and the destination is a friendly player or pet
            if (BasicEvent.isPlayerGuid(de.getDestinationGUID()) || BasicEvent.isPetGuid(de.getDestinationGUID()) || BasicEvent.isFriendlyPetNpc(de.getDestinationGUID(), de.getDestinationFlags())) {
                if (BasicEvent.isReactionFriendly(de.getDestinationFlags())) {
                    if (BasicEvent.isNpcGuid(de.getSourceGUID()) || BasicEvent.isPvpVehicleGuid(de.getSourceGUID())) {
                        if (BasicEvent.isReactionHostile(de.getSourceFlags())) {
                            return true;
                        } else if (SettingsSingleton.getInstance().getAllowNeutralNpcs()) {
                            if (BasicEvent.isReactionNeutral(de.getSourceFlags())) {
                                //If allowing neutral fights
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean checkIfStartFightEventFriendlyVsEnemy(BasicEvent e) {
        if (e instanceof DamageEvent) {
            DamageEvent de = (DamageEvent) e;

            //Start new fight with a damage event if the destination is an NPC and hostile and source is a friendly player or pet
            if (BasicEvent.isPlayerGuid(de.getSourceGUID()) || BasicEvent.isPetGuid(de.getSourceGUID()) || BasicEvent.isFriendlyPetNpc(de.getSourceGUID(), de.getSourceFlags())) {
                if (BasicEvent.isReactionFriendly(de.getSourceFlags())) {
                    if (BasicEvent.isNpcGuid(de.getDestinationGUID()) || BasicEvent.isPvpVehicleGuid(de.getDestinationGUID())) {
                        if (BasicEvent.isReactionHostile(de.getDestinationFlags())) {
                            //New fight.
                            return true;
                        } else if (SettingsSingleton.getInstance().getAllowNeutralNpcs()) {
                            if (BasicEvent.isReactionNeutral(de.getDestinationFlags())) {
                                //If allowing neutral fights
                                return true;
                            }
                        } else if (de.getDestinationName().toLowerCase().contains("training dummy")) {
                            //New fight if it is a training dummy
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    
}


        