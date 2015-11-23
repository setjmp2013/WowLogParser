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
import wowlogparserbase.events.SkillInterface;

/**
 * A class used to find players and their classes.
 * @author racy
 */
public class PlayerClassParser {

    static final int[][] classSkills = new int[Constants.numClasses][0];
    static final String[][] classSkillsStr = new String[Constants.numClasses][0];

    List<FightParticipant> playerInfo = new ArrayList<FightParticipant>();

    /**
     * Constructor
     * @param ec The events to use to extract class information from.
     */
    public PlayerClassParser(EventCollection ec) {        
        readParseInfo();        
        classParsing(ec.getEvents());
    }
    
    private static void readParseInfo() {
        //Set class skills to check for when determining class.

        XmlInfoParser info = XmlInfoParser.getInstance();
        
        String[] druidStr = info.xmlGetClassStrLower("druid");
        int[] druid = info.xmlGetClassID("druid");
        String[] rogueStr = info.xmlGetClassStrLower("rogue");
        int[] rogue = info.xmlGetClassID("rogue");
        String[] warriorStr = info.xmlGetClassStrLower("warrior");
        int[] warrior = info.xmlGetClassID("warrior");
        String[] hunterStr = info.xmlGetClassStrLower("hunter");
        int[] hunter = info.xmlGetClassID("hunter");
        String[] mageStr = info.xmlGetClassStrLower("mage");
        int[] mage = info.xmlGetClassID("mage");
        String[] warlockStr = info.xmlGetClassStrLower("warlock");
        int[] warlock = info.xmlGetClassID("warlock");
        String[] shamanStr = info.xmlGetClassStrLower("shaman");
        int[] shaman = info.xmlGetClassID("shaman");
        String[] priestStr = info.xmlGetClassStrLower("priest");
        int[] priest = info.xmlGetClassID("priest");
        String[] paladinStr = info.xmlGetClassStrLower("paladin");
        int[] paladin = info.xmlGetClassID("paladin");
        String[] deathknightStr = info.xmlGetClassStrLower("deathknight");
        int[] deathknight = info.xmlGetClassID("deathknight");
        
        PlayerClassParser.setClassSkillsStr(Constants.CLASS_DEATHKNIGHT, deathknightStr);
        PlayerClassParser.setClassSkillsStr(Constants.CLASS_DRUID, druidStr);
        PlayerClassParser.setClassSkillsStr(Constants.CLASS_ROGUE, rogueStr);
        PlayerClassParser.setClassSkillsStr(Constants.CLASS_WARRIOR, warriorStr);
        PlayerClassParser.setClassSkillsStr(Constants.CLASS_HUNTER, hunterStr);
        PlayerClassParser.setClassSkillsStr(Constants.CLASS_MAGE, mageStr);
        PlayerClassParser.setClassSkillsStr(Constants.CLASS_WARLOCK, warlockStr);
        PlayerClassParser.setClassSkillsStr(Constants.CLASS_SHAMAN, shamanStr);
        PlayerClassParser.setClassSkillsStr(Constants.CLASS_PRIEST, priestStr);
        PlayerClassParser.setClassSkillsStr(Constants.CLASS_PALADIN, paladinStr);
        PlayerClassParser.setClassSkills(Constants.CLASS_DEATHKNIGHT, deathknight);
        PlayerClassParser.setClassSkills(Constants.CLASS_DRUID, druid);
        PlayerClassParser.setClassSkills(Constants.CLASS_ROGUE, rogue);
        PlayerClassParser.setClassSkills(Constants.CLASS_WARRIOR, warrior);
        PlayerClassParser.setClassSkills(Constants.CLASS_HUNTER, hunter);
        PlayerClassParser.setClassSkills(Constants.CLASS_MAGE, mage);
        PlayerClassParser.setClassSkills(Constants.CLASS_WARLOCK, warlock);
        PlayerClassParser.setClassSkills(Constants.CLASS_SHAMAN, shaman);
        PlayerClassParser.setClassSkills(Constants.CLASS_PRIEST, priest);
        PlayerClassParser.setClassSkills(Constants.CLASS_PALADIN, paladin);
        FightParticipant.setClassName(Constants.CLASS_DEATHKNIGHT, info.xmlGetClassName("deathknight"));
        FightParticipant.setClassName(Constants.CLASS_DRUID, info.xmlGetClassName("druid"));
        FightParticipant.setClassName(Constants.CLASS_ROGUE, info.xmlGetClassName("rogue"));
        FightParticipant.setClassName(Constants.CLASS_WARRIOR, info.xmlGetClassName("warrior"));
        FightParticipant.setClassName(Constants.CLASS_HUNTER, info.xmlGetClassName("hunter"));
        FightParticipant.setClassName(Constants.CLASS_MAGE, info.xmlGetClassName("mage"));
        FightParticipant.setClassName(Constants.CLASS_WARLOCK, info.xmlGetClassName("warlock"));
        FightParticipant.setClassName(Constants.CLASS_SHAMAN, info.xmlGetClassName("shaman"));
        FightParticipant.setClassName(Constants.CLASS_PRIEST, info.xmlGetClassName("priest"));
        FightParticipant.setClassName(Constants.CLASS_PALADIN, info.xmlGetClassName("paladin"));
        FightParticipant.setClassName(Constants.CLASS_UNKNOWN, info.xmlGetClassName("unknown"));
    }
    
    private void classParsing(List<BasicEvent> evs) {
        for (BasicEvent be : evs) {
            //Automatic class parsing based on skills used. A playerInfo array is created with the info.
            if ((be.getSourceFlags() & Constants.FLAGS_OBJECT_TYPE_PLAYER) != 0) {
                boolean playerFound = false;
                for (int k = 0; k < playerInfo.size(); k++) {
                    FightParticipant p = playerInfo.get(k);
                    if (p.getSourceGUID().equals(be.getSourceGUID())) {
                        tryToFindClassStr(be, p);
                        tryToFindClass(be, p);
                        playerFound = true;
                        break;
                    }
                }
                if (!playerFound) {
                    FightParticipant p = new FightParticipant();
                    p.setGUID(be.getSourceGUID());
                    p.setFlags(be.getSourceFlags());
                    p.setName(be.getSourceName());
                    playerInfo.add(p);
                }
            }
        }
    }

    /**
     * Try to find the class of this participant from an event. Only do it if the class is still CLASS_UNKNOWN
     * @param be The event
     */
    private void tryToFindClass(BasicEvent be, FightParticipant p) {
        int k,l;
        if (p.getPlayerClass() == Constants.CLASS_UNKNOWN) {
            if (be instanceof SkillInterface) {
                SkillInterface si = (SkillInterface) be;
                int skillID = si.getSkillID();
                for (k=0; k<classSkills.length; k++) {   
                    int[] skills = classSkills[k];
                    for (l = 0; l < skills.length; l++) {
                        if (skillID == skills[l]) {
                            p.setPlayerClass(k);
                            return;
                        }
                    }
                }
            }
        }
    }

    /**
     * Try to find the class of this participant from an event. Only do it if the class is still CLASS_UNKNOWN
     * @param be The event
     */
    private void tryToFindClassStr(BasicEvent be, FightParticipant p) {
        int k,l;
        if (p.getPlayerClass() == Constants.CLASS_UNKNOWN) {
            if (be instanceof SkillInterface) {
                SkillInterface si = (SkillInterface) be;
                String skillName = si.getSkillName();
                for (k=0; k<classSkillsStr.length; k++) {   
                    String[] skills = classSkillsStr[k];
                    for (l = 0; l < skills.length; l++) {
                        if (skillName.trim().equalsIgnoreCase(skills[l])) {
                            p.setPlayerClass(k);
                            return;
                        }
                    }
                }
            }
        }
    }

    /**
     * Static method for all participants. Used to set the skills that identify different classes.
     * @param pClass The player class
     * @param skills The skill IDs that should be used to identify this class
     */
    private static void setClassSkills(int pClass, int[] skills) {
        classSkills[pClass] = skills;
    }
    
    /**
     * Static method for all participants. Used to set the skills that identify different classes.
     * @param pClass The player class
     * @param skills The skill IDs that should be used to identify this class
     */
    private static void setClassSkillsStr(int pClass, String[] skills) {
        classSkillsStr[pClass] = skills;
    }

    /**
     * Returns the parsed player info in a FightParticipant object. 
     * There are no events in the returned FightParticipant objects.
     * @return The info
     */
    public List<FightParticipant> getPlayerInfo() {
        return playerInfo;
    }
}
