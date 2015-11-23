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
import wowlogparserbase.EventCollection;
import wowlogparserbase.eventfilter.FilterSkill;
import wowlogparserbase.events.BasicEvent;
import wowlogparserbase.events.aura.SpellAuraRemovedEvent;

/**
 * A class for parsing arena information
 * @author racy
 */
public class ArenaInfoParser {
    EventCollection events;
    List<FightParticipant> playerInfo;

    List<Double> startTimes = new ArrayList<Double>();
    List<FightParticipant> teamPlayers = new ArrayList<FightParticipant>();
    List<FightParticipant> enemyPlayers = new ArrayList<FightParticipant>();
    

    /**
     * Construct an ArenaInfoParser with the supplied events and player info.
     * @param events The events.
     * @param playerInfo A list with player info.
     */
    public ArenaInfoParser(EventCollection events, List<FightParticipant> playerInfo) {
        this.events = events;
        this.playerInfo = playerInfo;
        parseEvents();
        findTeamPlayers();
        findEnemyPlayers();
    }
    
    protected void findTeamPlayers() {
        for (FightParticipant p : playerInfo) {
            if (p.isPlayer() && p.isRaidPerson()) {
                teamPlayers.add(p);
            }
        }
    }
    
    protected void findEnemyPlayers() {
        for (FightParticipant p : playerInfo) {
            if (p.isPlayer()) {
                if ( p.isHostile() ) {
                    enemyPlayers.add(p);
                }
            }
        }
    }

    protected void parseEvents() {
        EventCollection ec = events.filter(new FilterSkill(FilterSkill.ANY_ID, "Arena Preparation", FilterSkill.ANY_SCHOOL, FilterSkill.ANY_POWER_TYPE));
        List<SpellAuraRemovedEvent> removedEvents = ec.getEvents(new SpellAuraRemovedEvent());
        
        if (removedEvents.size() == 0) {
            return;
        }
        double delta = 5;
        double time = removedEvents.get(0).time;
        startTimes.add(time);
        
        for (SpellAuraRemovedEvent e : removedEvents) {
            if (Math.abs(e.time - time) > delta) {
                //New preparation
                time = e.time;
                startTimes.add(time);
            }
        }
    }
    
    /**
     * Get a list with times where an arena is entered.
     * @return A list with the times.
     */
    public List<Double> getStartTimes() {
        return startTimes;
    }

    /**
     * Get a list of team players.
     * @return A list of team players.
     */
    public List<FightParticipant> getTeamPlayers() {
        return new ArrayList<FightParticipant>(teamPlayers);
    }

    /**
     * Get a list of enemy players.
     * @return A list of enemy players.
     */
    public List<FightParticipant> getEnemyPlayers() {
        return new ArrayList<FightParticipant>(enemyPlayers);
    }
    
}
