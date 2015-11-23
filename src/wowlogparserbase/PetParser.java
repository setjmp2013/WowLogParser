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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import wowlogparserbase.events.*;
import wowlogparserbase.events.healing.*;
import wowlogparserbase.events.aura.*;

/**
 * Class used to find which players own which pets.
 * @author racy
 */
public class PetParser {
    
    FileLoader fileLoader;
    List<Fight> fights;

    List<PetParserFightParticipant> pets = new ArrayList<PetParserFightParticipant>();
    List<FightParticipant> players = new ArrayList<FightParticipant>();
    List<FightParticipant> parsedPlayers = new ArrayList<FightParticipant>();
    List<Integer> armyOfTheDeadPetIds;
    List<Integer> armyOfTheDeadSkillIds;
    double maxArmyOfTheDeadDelay = 8;

    /**
     * Constructor
     * @param fileLoader A FileLoader that is parsed.
     * @param fights Fights where players are extracted from.
     */
    public PetParser(FileLoader fileLoader, List<Fight> fights) {
        this.fileLoader = fileLoader;
        this.fights = fights;
        initArmyOfTheDeadStuff();
        findPetsAndPlayers();
        doParsing();
    }

    private void initArmyOfTheDeadStuff() {
        armyOfTheDeadPetIds = new ArrayList<Integer>();
        armyOfTheDeadSkillIds = new ArrayList<Integer>();
        armyOfTheDeadPetIds.add(24207);
        armyOfTheDeadSkillIds.add(42650);

    }

    private void findPetsAndPlayers() {
        int k;
        for(Fight f : fights) {
            for (FightParticipant p : f.getParticipants()) {
                if (p.isPlayer()) {
                    boolean found = false;
                    for (k=0; k<players.size(); k++) {
                        FightParticipant foundParticipant = players.get(k);
                        //Merge participants from different fights so all pets are found
                        if (p.getSourceGUID().equals(foundParticipant.getSourceGUID())) {
                            found = true;
                            List<FightParticipant> list = new ArrayList<FightParticipant>();
                            list.add(foundParticipant);
                            list.add(p.createEmptyShell());
                            players.set(k, FightParticipant.merge(list));
                        }
                    }
                    if (!found) {
                        players.add(p.createEmptyShell());
                    }
                }
                if (p.isPet() || p.isPetObject() || p.isGuardian()) {
                    boolean found = false;
                    for (FightParticipant foundParticipant : pets) {
                        if (p.getSourceGUID().equals(foundParticipant.getSourceGUID())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        pets.add(PetParserFightParticipant.createEmptyshellWithStartEvent(p));
                    }
                }
            }
        }
        parsedPlayers = new ArrayList<FightParticipant>();
        for (FightParticipant p : players) {
            FightParticipant p2 = p.createEmptyShell();
            p2.setPetNames(new ArrayList<PetInfo>());
            parsedPlayers.add(p2);
        }
    }

    /**
     * Do automatic parsing of pets
     */
    private void doParsing() {
        List<BasicEvent> events = fileLoader.getEventCollection().getEvents();
        outer:
        for (int k=0; k<events.size(); k++) {
            LogEvent le = events.get(k);
            if (le instanceof SpellSummonEvent) {
                SpellSummonEvent e = (SpellSummonEvent) le;
                String playerGUID = e.getSourceGUID();
                String petGUID = e.getDestinationGUID();
                if (tryAddPetToPlayer(playerGUID, petGUID)) {
                    continue outer;
                }
            }

            if (le instanceof SpellAuraRemovedEvent) {
                SpellAuraRemovedEvent ae = (SpellAuraRemovedEvent) le;
                if (armyOfTheDeadSkillIds.contains(ae.getSkillID())) {
                    for (FightParticipant pi : pets) {
                        if (armyOfTheDeadPetIds.contains(BasicEvent.getNpcID(pi.getSourceGUID()))) {
                            BasicEvent startEvent = pi.getStartEvent();
                            if (Math.abs(startEvent.time - ae.time) < maxArmyOfTheDeadDelay) {
                                tryAddPetToPlayer(ae.getDestinationGUID(), pi.getSourceGUID());
                            }
                        }
                    }
                }
            }

            if ((le instanceof SpellEnergizeEvent) || (le instanceof PeriodicHealingEvent)) {
                BasicEvent e = (BasicEvent) le;
                SkillInterface si = (SkillInterface) le;
                String name = si.getSkillName().toLowerCase();
                if (name.equals("feed pet effect") || name.equals("go for the throat") || name.equals("mend pet")) {
                    String playerGUID = e.getSourceGUID();
                    String petGUID = e.getDestinationGUID();
                    if (tryAddPetToPlayer(playerGUID, petGUID)) {
                        continue outer;
                    }
                }
            }
            
//Commented away because of possible errors when hunters mend their pets at the same time.
//            if (le instanceof SpellCastSuccessEvent) {
//                SpellCastSuccessEvent e = (SpellCastSuccessEvent) le;
//                if (e.getName().equalsIgnoreCase("mend pet")) {
//                    int l = k+1;
//                    while (l < events.size()) {
//                        LogEvent testEvent = events.get(l);
//                        //Break if no mend pet aura applied is found within 3 seconds.
//                        if (Math.abs(testEvent.time - e.time) > 3) {
//                            break;
//                        }
//                        if (testEvent instanceof SpellAuraAppliedEvent) {
//                            SpellAuraAppliedEvent appliedEvent = (SpellAuraAppliedEvent) testEvent;
//                            if (appliedEvent.getName().equalsIgnoreCase("mend pet")) {
//                                String playerGUID = e.sourceGUID;
//                                String petGUID = appliedEvent.destinationGUID;
//                                if (tryAddPetToPlayer(playerGUID, petGUID)) {
//                                    continue outer;
//                                }
//                            }
//                        }
//                        l++;
//                    }
//                }
//            }
        }
        
        postProcessing();
    }

    /**
     * Check if the pets that has not been added yet has the same "pet ID" in the GUID
     * as another pet already added to a player. In that case add the pet to that player.
     */
    private void postProcessing() {
        outerPet:
        for (FightParticipant pet : pets) {
            boolean found = false;
            outerPlayer:
            for (FightParticipant player : parsedPlayers) {
                for(PetInfo playerPet : player.getPetNames()) {
                    if (playerPet.getSourceGUID().equals(pet.getSourceGUID())) {
                        found = true;
                        break outerPlayer;
                    }
                }
            }
            //Check if we can add a non found pet to some player.
            if (!found) {
                for (FightParticipant player : parsedPlayers) {
                    for (PetInfo playerPet : player.getPetNames()) {
                        if (playerPet.getSourceGUID().length()>12 && pet.getSourceGUID().length() > 12) {
                            String petString = pet.getSourceGUID().substring(0, 12);
                            String playerPetString = playerPet.getSourceGUID().substring(0, 12);
                            if (petString.equals(playerPetString)) {
                                tryAddPetToPlayer(player.getSourceGUID(), pet.getSourceGUID());
                                continue outerPet;
                            }
                        }
                    }
                }                
            }
            
        }
    }
    
    /**
     * Try to add a pet to a player
     * @param ownerGUID
     * @param petGUID
     * @return false if the pet cannot be added, true if it is added or was added already
     */
    private boolean tryAddPetToPlayer(String ownerGUID, String petGUID) {
        //Add to player
        for (FightParticipant p : parsedPlayers) {
            if (p.getSourceGUID().equals(ownerGUID)) {
                //Check if pet already exists on the player
                for (PetInfo pi : p.getPetNames()) {
                    if (pi.getSourceGUID().equals(petGUID)) {
                        return true;
                    }
                }
                //Find out if the pet exists in the list and add it
                for (PetParserFightParticipant pi : pets) {
                    if (!pi.isPetAdded()) {
                        if (pi.getSourceGUID().equals(petGUID)) {
                            p.getPetNames().add(new PetInfo(pi));
                            pi.setPetAdded(true);
                            return true;
                        }
                    }
                }
            }
        }
        //Add to the parent player if it was a pet that summoned another pet.
        for (FightParticipant p : parsedPlayers) {
            for (PetInfo playerPet : p.getPetNames()) {
                if (playerPet.getSourceGUID().equals(ownerGUID)) {
                    //Check if pet already exists on the player
                    for (PetInfo pi : p.getPetNames()) {
                        if (pi.getSourceGUID().equals(petGUID)) {
                            return true;
                        }
                    }
                    //Find out if the pet exists in the list and add it
                    for (PetParserFightParticipant pi : pets) {
                        if (!pi.isPetAdded()) {
                            if (pi.getSourceGUID().equals(petGUID)) {
                                p.getPetNames().add(new PetInfo(pi));
                                pi.setPetAdded(true);
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Get all pets.
     * @return A list with pets.
     */
    public List<PetParserFightParticipant> getPets() {
        return pets;
    }

    /**
     * Get all players.
     * @return A list with players.
     */
    public List<FightParticipant> getPlayers() {
        return players;
    }

    /**
     * Get players with the correct pets applied.
     * @return A list with parsed players.
     */
    public List<FightParticipant> getParsedPlayers() {
        return parsedPlayers;
    }

    /**
     * Insert the correct pets for the players that owns them. This is done on the real fights array.
     * @param players Empty shell with players that describe what pets they own.
     * @param fights The fight array where the real data resides.
     * The players in fights will have the correct pets added to them.
     */
    public static void insertPetsOnPlayers(List<FightParticipant> players, List<Fight> fights) {
        
        //First clear all current pets so that we get a fresh start.
        for (Fight f : fights) {
            for (FightParticipant p : f.getParticipants()) {                
                if (p.isPlayer()) {
                    p.getPetNames().clear();
                }
            }
        }
        
        //Loop through all players and fights to add the pets at the correct places
        for (FightParticipant playerP : players) {
            if (playerP.hasPets()) {
                for (Fight f : fights) {
                    FightParticipant fightPlayer = f.findParticipant(playerP);
                    boolean dummyPlayerCreated = false;
                    if (fightPlayer == null) {
                        fightPlayer = new FightParticipant();
                        fightPlayer.setName(playerP.getName());
                        fightPlayer.setSourceGUID(playerP.getSourceGUID());
                        fightPlayer.setFlags(playerP.getFlags());
                        dummyPlayerCreated = true;
                    }

                    boolean petsAdded = false;
                    for (PetInfo pi : playerP.getPetNames()) {
                        FightParticipant fightPet = f.findParticipant(pi);
                        if (fightPet == null) {
                            continue;
                        }
                        fightPlayer.getPetNames().add(new PetInfo(fightPet));
                        petsAdded = true;
                    }
                    if (petsAdded && dummyPlayerCreated) {
                        f.getParticipants().add(fightPlayer);
                    }
                }
            }
        }

    }
}
