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

import wowlogparserbase.helpers.Worker;
import wowlogparserbase.helpers.WorkerProgressDialog;
import java.awt.Dialog;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import wowlogparserbase.events.BasicEvent;

/**
 *
 * @author gustav
 */
public class BossParsing {

    private double minBossDuration = 0.5;

    List<ListOfFights> tempFights;
    List<BossInfo> bossInfoList;
    Set<String> mobSet;
    Set<Integer> mobIdSet;
    List<Fight> outFights = null;
    List<Fight> fights;
    FileLoader fl;

    Dialog owner = null;
    Frame ownerFrame = null;

    BossParserSource bossParser;

    public BossParsing(List<Fight> fights, FileLoader fl) {
        this.fights = fights;
        this.fl = fl;
        bossParser = XmlInfoParser.getInstance();
    }

    public BossParsing(Dialog owner, List<Fight> fights, FileLoader fl) {
        this(fights, fl);
        this.owner = owner;
    }

    public BossParsing(Frame ownerFrame, List<Fight> fights, FileLoader fl) {
        this(fights, fl);
        this.ownerFrame = ownerFrame;
    }

    public BossParsing(Dialog owner, List<Fight> fights, FileLoader fl, BossParserSource bossSource) {
        this(owner, fights, fl);
        this.bossParser = bossSource;
    }

    public BossParsing(Frame ownerFrame, List<Fight> fights, FileLoader fl, BossParserSource bossSource) {
        this(ownerFrame, fights, fl);
        this.bossParser = bossSource;
    }

    public double getMinBossDuration() {
        return minBossDuration;
    }

    public void setMinBossDuration(double minBossDuration) {
        this.minBossDuration = minBossDuration;
    }

    /**
     * Get generated boss fights
     * @return A list of boss fights
     */
    public List<Fight> getOutFights() {
        return outFights;
    }

    /**
     * Do automatic boss encounter parsing
     * Fights are found by matching mobs to data in the boss XML file.
     * The mobs are primarilly matched against mob ID, but if mob ID is not
     * present then mob name is used.
     */
    public void doAutomaticBossParsing() {
        outFights = new ArrayList<Fight>();
        bossInfoList = bossParser.getBossInfo();
        tempFights = new ArrayList<ListOfFights>();

        mobIdSet = new HashSet<Integer>();
        mobSet = new HashSet<String>();
        for (BossInfo bi : bossInfoList) {
            for (NpcInfo ni : bi.getMobs()) {
                if (ni.getId() == NpcInfo.UNKNOWN_ID) {
                    mobSet.add(ni.getName().toLowerCase());
                } else {
                    mobIdSet.add(ni.getId());
                }
            }
        }

        //Phase 1. Merge all fights intersecting all boss mobs.
        Worker worker = new Worker() {

            public void run() {
                for (int k = 0; k < fights.size(); k++) {
                    Fight f = fights.get(k);
                    if (dialog != null) {
                        dialog.setProgress((k * 100) / fights.size());
                    }

                    //Make sure the first fight is a boss mob
                    boolean match = false;
                    if (mobIdSet.contains(BasicEvent.getNpcID(f.getGuid()))) {
                        match = true;
                    }
                    if (mobSet.contains(f.getName().toLowerCase())) {
                        match = true;
                    }
                    if (!match) {
                        continue;
                    }

                    ListOfFights lof = new ListOfFights();
                    lof.addFight(f);
                    for (Fight testFight : fights) {
                        TimePeriod totalTP = lof.getTimePeriod();
                        TimePeriod testFightTP = new TimePeriod(testFight.getStartTimeFast(), testFight.getEndTimeFast());
                        if (totalTP.intersect(testFightTP)) {
                            lof.addFight(testFight);
                        }
                    }
                    tempFights.add(lof);
                }
                hideDialog();
            }
        };
        if (owner != null) {
            WorkerProgressDialog wd = new WorkerProgressDialog(owner, "Merging...", true, worker, "Merging phase 1...");
            wd.setLocationRelativeTo(owner);
            wd.setVisible(true);
        } else if (ownerFrame != null) {
            WorkerProgressDialog wd = new WorkerProgressDialog(ownerFrame, "Merging...", true, worker, "Merging phase 1...");
            wd.setLocationRelativeTo(ownerFrame);
            wd.setVisible(true);
        } else {
            worker.run();
        }

        //Phase 2. Merge fights from the same encounter.
        Worker worker2 = new Worker() {

            public void run() {
                int listSize = bossInfoList.size();
                int progress = 0;
                for (BossInfo bossInfo : bossInfoList) {
                    double maxTimeBetweenMobs = bossInfo.getMaxIdleTime();
                    progress++;
                    if (dialog != null) {
                        dialog.setProgress((progress * 100) / listSize);
                    }
                    while (true) {
                        double endTime = 0;
                        boolean found = false;
                        ListOfFights currentBossFight = new ListOfFights();
                        tempFightsLoop:
                        for (int k = 0; k < tempFights.size(); k++) {
                            ListOfFights lof = tempFights.get(k);
                            if (!found) {
                                //Make sure that we use a fight if none has been added
                                endTime = lof.getTimePeriod().startTime;
                            }
                            for (NpcInfo mob : bossInfo.getMobs()) {
                                boolean match = false;
                                if (mob.getId() == NpcInfo.UNKNOWN_ID) {
                                    if (lof.getName().equalsIgnoreCase(mob.getName())) {
                                        match = true;
                                    }
                                } else {
                                    if (BasicEvent.getNpcID(lof.getGuid()) == mob.getId()) {
                                        match = true;
                                    }
                                }
                                if (match) {
                                    if ((Math.abs(lof.getTimePeriod().startTime - endTime) < maxTimeBetweenMobs) ||
                                            (Math.abs(lof.getTimePeriod().endTime - endTime) < maxTimeBetweenMobs)) {
                                        found = true;
                                        currentBossFight.addFights(lof);
                                        endTime = lof.getTimePeriod().endTime;
                                        tempFights.remove(k);
                                        k--;
                                        continue tempFightsLoop;
                                    }
                                }
                            }
                        }
                        if (currentBossFight.size() > 0) {
                            //Fight mergedFight = currentBossFight.getMergedFight();
                            Fight mergedFight = currentBossFight.getMergedFightFromTimePeriods(fl);
                            if (!mergedFight.isMerged()) {
                                int a = 0;
                            }
                            mergedFight.setName("Boss Fight:" + bossInfo.getEncounterName());
                            fixIsDead(mergedFight, bossInfo);
                            outFights.add(mergedFight);
                        }
                        if (!found) {
                            break;
                        }
                    }
                }

                //Remove fights with duration 0.
                ListIterator<Fight> it = outFights.listIterator();
                while(it.hasNext()) {
                    Fight f = it.next();
                    if (f.getDuration() < minBossDuration) {
                        it.remove();
                    }
                }
                hideDialog();
            }
        };
        if (owner != null) {
            WorkerProgressDialog wd2 = new WorkerProgressDialog(owner, "Merging...", true, worker2, "Merging phase 2...");
            wd2.setLocationRelativeTo(owner);
            wd2.setVisible(true);
        } else if (ownerFrame != null) {
            WorkerProgressDialog wd2 = new WorkerProgressDialog(ownerFrame, "Merging...", true, worker2, "Merging phase 2...");
            wd2.setLocationRelativeTo(ownerFrame);
            wd2.setVisible(true);
        } else {
            worker2.run();
        }
        Collections.sort(outFights);
        for(Fight f : outFights) {
            f.setBossFight(true);
        }
    }

    private void fixIsDead(Fight mergedFight, BossInfo bossInfo) {
        if (mergedFight.isMerged()) {
            boolean allFound = true;
            boolean allDead = true;
            int bossMobs = 0;
            for (NpcInfo npc : bossInfo.getMobs()) {
                if (npc.isBossMob()) {
                    bossMobs++;
                    boolean found = false;
                    for (Fight f : mergedFight.getMergedSourceFights()) {
                        if (f.getMobID() == npc.getId()) {
                            found = true;
                            if (!f.isDead()) {
                                allDead = false;
                            }
                        }
                    }
                    if (!found) {
                        allFound = false;
                    }
                }
            }
            if (bossMobs > 0) {
                if (allFound && allDead) {
                    mergedFight.setDead(true);
                } else {
                    mergedFight.setDead(false);
                }
            }
        }
    }
}
