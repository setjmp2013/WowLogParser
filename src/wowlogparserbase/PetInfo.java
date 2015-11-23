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
import wowlogparserbase.eventfilter.Filter;

/**
 * An extension to FightParticipant used for pets.
 * @author racy
 */
public class PetInfo extends FightParticipant {
    
    protected boolean dead = false;
    
    public PetInfo() {
        super();
    }
    
    /**
     * Copy constructor
     * @param participant
     */
    public PetInfo(PetInfo participant) {
        super(participant);
        this.dead = participant.dead;
    }
    
    /**
     * Make a PetInfo from a FightParticipant
     * @param participant
     */
    public PetInfo(FightParticipant participant) {
        super(participant);
    }

    /**
     * Flag for dead status if the pet info is used for an NPC.
     * @return
     */
    public boolean isDead() {
        return dead;
    }

    /**
     * Flag for dead status if the pet info is used for an NPC.
     * @param dead
     */
    public void setDead(boolean dead) {
        this.dead = dead;
    }

    /**
     * Create a copy of this object with only the info needed to identify it left.
     * @return The copy
     */
    @Override
    public PetInfo createEmptyShell() {
        FightParticipant part = super.createEmptyShell();
        PetInfo p = new PetInfo(part);
        p.setDead(dead);
        return p;
    }
    
    /**
     * Create a copy of this object with only the info needed to identify it left.
     * @return The copy
     */
    @Override
    public PetInfo createSuperEmptyShell() {
        PetInfo p = new PetInfo();
        p.setIsPetObject(isPetObject());
        return p;
    }

    /**
     * Make a received participant from this participant, with fight f.
     * @param f The fight the events are from.
     * @return
     */
    @Override
    public PetInfo makeReceivedParticipant(Fight f) {
        FightParticipant recPart = super.makeReceivedParticipant(f);
        PetInfo p = new PetInfo(recPart);
        return p;
    }

    /**
     * Merge pets that are equal in the list
     * @param list A list of pets
     * @return A new list where equal pets have been merged
     */
    public static List<PetInfo> mergeEqual(List<PetInfo> list) {
        int k,l;
        if(list.size() == 0) {
            return new ArrayList<PetInfo>();
        }
        
        ArrayList<PetInfo> destList = new ArrayList<PetInfo>();
        //Add first pet to initialize the array.
        destList.add(new PetInfo(list.get(0)));
        for (k=1; k<list.size(); k++) {
            PetInfo sourceP = list.get(k);
            boolean found = false;
            for(l=0; l<destList.size(); l++) {
                PetInfo destP = destList.get(l);
                //Merge and replace if equal
                if (sourceP.compareTo(destP) == 0) {
                    ArrayList<PetInfo> mergeList = new ArrayList<PetInfo>();
                    mergeList.add(sourceP);
                    mergeList.add(destP);
                    PetInfo merged = new PetInfo(merge(mergeList));
                    boolean deadStatus = sourceP.isDead() || destP.isDead();
                    merged.setDead(deadStatus);
                    destList.set(l, merged);
                    found = true;
                    break;
                }
            }
            if (!found) {
                destList.add(new PetInfo(sourceP));
            }
        }
        
        return destList;
    }

    private PetInfo lastPet = null;

    @Override
    public void getEventsContinueFromLastReset() {
        super.getEventsContinueFromLastReset();
    }

    @Override
    public PetInfo getEventsContinueFromLast(double startTime, double endTime) {
        PetInfo out = new PetInfo(super.getEventsContinueFromLast(startTime, endTime));
        out.setDead(dead);
        return out;
    }

    @Override
    public PetInfo filter(Filter f) {
        PetInfo out = new PetInfo(super.filter(f));
        return out;
    }

    @Override
    public PetInfo filterAnd(List<Filter> fs) {
        PetInfo out = new PetInfo(super.filterAnd(fs));
        return out;
    }

    @Override
    public PetInfo filterOr(List<Filter> fs) {
        PetInfo out = new PetInfo(super.filterOr(fs));
        return out;
    }

    @Override
    public PetInfo filterTime(double startTime, double endTime) {
        PetInfo out = new PetInfo(super.filterTime(startTime, endTime));
        return out;
    }

}
