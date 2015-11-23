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
import java.util.List;
import wowlogparserbase.eventfilter.FilterAuraType;
import wowlogparserbase.eventfilter.FilterClass;
import wowlogparserbase.eventfilter.FilterEventType;
import wowlogparserbase.eventfilter.FilterGuidOr;
import wowlogparserbase.eventfilter.FilterNameAnd;
import wowlogparserbase.eventfilter.FilterNameOr;
import wowlogparserbase.eventfilter.FilterSkill;
import wowlogparserbase.events.aura.*;
import wowlogparserbase.events.*;

/**
 *
 * @author racy
 */
public class FightSplitter {
    Fight sourceFight;
    FileLoader fl;

    /**
     *
     * @param sourceFight The fight to search for start/stop events from.
     * @param fl A file loader with events to create new fights from.
     */
    public FightSplitter(Fight sourceFight, FileLoader fl ) {
        this.sourceFight = sourceFight;
        this.fl = fl;
    }

    /**
     *
     * @param startClass The event class that signifies a start event
     * @param stopClass The event class that signifies a stop event
     * @param spellId The spell ID to split on.
     * @param sourceName The source name, null = don't care
     * @param targetName The target name, null = don't care
     * @param preBufferTime Extra time to include in the beginning of a period.
     * @param postBufferTime Extra time to include at the end of a period.
     * @return
     */
    public List<Fight> split(Class startClass, Class stopClass, int spellId, String sourceName, String targetName, double preBufferTime, double postBufferTime) {
        List<Fight> out = new ArrayList<Fight>();
        EventCollection appliedEvents = new EventCollectionSimple(
                sourceFight.filter(
                new FilterClass(startClass)
                ).filter(new FilterNameAnd(sourceName, targetName)
                ).filter(new FilterSkill(spellId, null, FilterSkill.ANY_SCHOOL, FilterSkill.ANY_POWER_TYPE)));
        EventCollection removedEvents = new EventCollectionSimple(
                sourceFight.filter(
                new FilterClass(stopClass)
                ).filter(new FilterNameAnd(sourceName, targetName)
                ).filter(new FilterSkill(spellId, null, FilterSkill.ANY_SCHOOL, FilterSkill.ANY_POWER_TYPE)));
        List<BasicEvent> events = new ArrayList<BasicEvent>();
        events.addAll(appliedEvents.getEvents());
        events.addAll(removedEvents.getEvents());
        Collections.sort(events);

        List<TimePeriod> timePeriods = compilePeriods(events, sourceFight.getStartTime(), sourceFight.getEndTime(), preBufferTime, postBufferTime);
        out = makeFightsFromPeriods(timePeriods);
        
        return out;
    }

    public List<TimePeriod> compilePeriods(List<BasicEvent> events, double min, double max, double preBufferTime, double postBufferTime) {
        List<TimePeriod> tempPeriods = new ArrayList<TimePeriod>();
        int firstIndex = 0;
        while(true) {
            boolean foundStart = false;
            boolean foundStop = false;
            int startI = 0;
            int stopI = 0;
            for (int k=firstIndex; k<events.size(); k++) {
                //If the first event is a stop event, then assume a period from the start to this stop event.
                if (k == 0 && !foundStart && !foundStop) {
                    if (events.get(k) instanceof SpellAuraRemovedEvent) {
                        TimePeriod p = new TimePeriod(min, events.get(k).time);
                        tempPeriods.add(p);
                        break;
                    }
                }
                if (!foundStart) {
                    if (events.get(k) instanceof SpellAuraAppliedEvent) {
                        foundStart = true;
                        startI = k;
                    }
                } else {
                    if (!foundStop) {
                        if (events.get(k) instanceof SpellAuraRemovedEvent) {
                            foundStop = true;
                            stopI = k;
                        }
                    }
                }

                if (foundStart && foundStop) {
                    break;
                }
            }

            if (foundStart && foundStop) {
                TimePeriod p = new TimePeriod(events.get(startI).time - preBufferTime, events.get(stopI).time + postBufferTime);
                tempPeriods.add(p);
            } else if (foundStart) {
                TimePeriod p = new TimePeriod(events.get(startI).time - preBufferTime, max);
                tempPeriods.add(p);
            }

            firstIndex++;
            if (firstIndex >= events.size()) {
                break;
            }
        }

        List<TimePeriod> periods = TimePeriod.mergeTimePeriods(tempPeriods);
        return periods;
    }

    private List<Fight> makeFightsFromPeriods(List<TimePeriod> timePeriods) {
        List<Fight> out = new ArrayList<Fight>();
        for (int k=0; k<timePeriods.size(); k++) {
            TimePeriod tp = timePeriods.get(k);
            Fight f = new Fight(fl.getEventCollection().getEvents(tp.startTime, tp.endTime));
            f.setGUID(sourceFight.getGuid());
            f.setName("Part of:" + sourceFight.getName());
            f.makeParticipants(fl.getFightCollection().getPlayerInfo(), fl.getFightCollection().getObjectInfo());
            if (SettingsSingleton.getInstance().getAutoPlayerPets()) {
                List<Fight> tempList = new ArrayList<Fight>();
                tempList.add(f);
                PetParser.insertPetsOnPlayers(fl.getPetParser().getParsedPlayers(), tempList);
            }
            f.removeAssignedPets();
            out.add(f);
        }
        return out;
    }
}
