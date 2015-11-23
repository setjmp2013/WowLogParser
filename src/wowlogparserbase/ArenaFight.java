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
import java.util.List;
import wowlogparserbase.eventfilter.FilterClass;
import wowlogparserbase.eventfilter.FilterFlagsOr;
import wowlogparserbase.eventfilter.FilterGuidOr;
import wowlogparserbase.events.damage.DamageEvent;

/**
 *
 * @author racy
 */
public class ArenaFight extends EventCollectionSimple {

    double startTime;
    double endTime;
    List<FightParticipant> participants = new ArrayList<FightParticipant>();

    public ArenaFight(double startTime, double endTime, EventCollection ec) {
        this.startTime = startTime;
        this.endTime = endTime;
        setEvents(ec.filterTime(startTime, endTime));
        createActiveDuration(15, 1.5);
    }

    public void makeParticipants(EventCollection ec, List<FightParticipant> players) {
        participants = new ArrayList<FightParticipant>();
        for (FightParticipant p : players) {
            FightParticipant newPart = p.createEmptyShell();
            EventCollection partEvents = filter(new FilterGuidOr(p.getSourceGUID(), p.getSourceGUID()));
            if (partEvents.size() > 0) {
                EventCollection dmgEvents = partEvents.filter(new FilterClass(DamageEvent.class));
                //Only add a participant if he does damage or receives damage.
                if (dmgEvents.size()>0) {
                    newPart.addEvents(partEvents);
                    participants.add(newPart);
                }
            }
        }
    }

    public List<FightParticipant> getParticipants() {
        return participants;
    }
        
    public List<FightParticipant> getFriendlyParticipants() {
        List<FightParticipant> players = new ArrayList<FightParticipant>();
        for (FightParticipant p : participants) {
            if (p.isFriendly()) {
                players.add(p);
            }
        }
        return players;
    }
    
    public List<FightParticipant> getEnemyParticipants() {
        List<FightParticipant> players = new ArrayList<FightParticipant>();
        for (FightParticipant p : participants) {
            if (p.isHostile()) {
                players.add(p);
            }
        }
        return players;
    }
    
}
