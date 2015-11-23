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

import wowlogparserbase.events.BasicEvent;
import wowlogparserbase.events.UnknownEvent;

/**
 *
 * @author racy
 */
public class PetParserFightParticipant extends FightParticipant {

    private boolean petAdded = false;
    private BasicEvent startEvent = null;

    public PetParserFightParticipant(FightParticipant p) {
        super(p);
    }

    public PetParserFightParticipant(int numEvents, int numReceivedEvents) {
        super(numEvents, numReceivedEvents);
    }

    public PetParserFightParticipant() {
    }

    public boolean isPetAdded() {
        return petAdded;
    }

    public void setPetAdded(boolean petAdded) {
        this.petAdded = petAdded;
    }

    @Override
    public BasicEvent getStartEvent() {
        if (startEvent == null) {
            return super.getStartEvent();
        }
        return startEvent;
    }

    public void setStartEvent(BasicEvent startEvent) {
        this.startEvent = startEvent;
    }
    
    public static PetParserFightParticipant createEmptyshellWithStartEvent(FightParticipant inP) {
        BasicEvent ev = inP.getStartEvent();
        PetParserFightParticipant p = new PetParserFightParticipant(inP.createEmptyShell());
        p.setStartEvent(ev);
        return p;
    }
}
