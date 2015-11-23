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
package wowlogparserbase.info;

import wowlogparserbase.Constants;
import java.util.List;
import wowlogparserbase.*;
import wowlogparserbase.events.*;
import wowlogparserbase.eventfilter.*;

/**
 *
 * @author racy
 */
public class DamageInfoAll extends DamageInfoBase {
    int unit = FightParticipant.UNIT_ALL;
    int wantedSchool = Constants.SCHOOL_ALL;
    int wantedType = FightParticipant.TYPE_ALL_DAMAGE;
    String wantedName = null;

    public DamageInfoAll(EventCollection fight, FightParticipant participant) {
        super(fight.getActiveDuration(), participant);
        parse();
    }

    public DamageInfoAll(EventCollection fight, FightParticipant participant, int unit) {
        super(fight.getActiveDuration(), participant);
        this.unit = unit;
        parse();
    }

    public DamageInfoAll(EventCollection fight, FightParticipant participant, int unit, int wantedSchool, int wantedType) {
        super(fight.getActiveDuration(), participant);
        this.unit = unit;
        this.wantedSchool = wantedSchool;
        this.wantedType = wantedType;
        parse();
    }

    public DamageInfoAll(EventCollection fight, FightParticipant participant, int unit, int wantedSchool, int wantedType, String wantedName) {
        super(fight.getActiveDuration(), participant);
        this.unit = unit;
        this.wantedSchool = wantedSchool;
        this.wantedType = wantedType;
        this.wantedName = wantedName;
        parse();
    }

    private void parse() {
        AmountAndCount a;
        a = getParticipant().totalDamage(wantedSchool, wantedType, unit, wantedName);
        setAmountAndCount(a);
        switch(unit) {
            case FightParticipant.UNIT_PLAYER:
                setSkillName("Total Damage");
                break;
            case FightParticipant.UNIT_PET:
                setSkillName("Pet: " + "Total damage");
                break;
            case FightParticipant.UNIT_ALL:
                setSkillName("Both: " + "Total damage");
                break;
            default:
                setSkillName("Total damage");
                break;
        }
    }

    @Override
    public EventCollectionSimple getEvents() {
        EventCollectionSimple out = new EventCollectionSimple();

        if ((unit & FightParticipant.UNIT_PLAYER) != 0) {
            EventCollection temp = getParticipant().filter(new FilterDamageAnySchool(wantedSchool));
            EventCollection temp2 = temp.filter(new FilterEventType(wantedType));
            EventCollection temp3;
            if (wantedName != null) {
                temp3 = temp2.filter(new FilterNameAnd(wantedName, null));
            } else {
                temp3 = temp2;
            }
            out.addEventsAlways(temp3);
        }

        if ((unit & FightParticipant.UNIT_PET) != 0) {
            List<PetInfo> pets = getParticipant().getPetNames();
            for (PetInfo p : pets) {
                EventCollection temp = p.filter(new FilterDamageAnySchool(wantedSchool));
                EventCollection temp2 = temp.filter(new FilterEventType(wantedType));
                EventCollection temp3;
                if (wantedName != null) {
                    temp3 = temp2.filter(new FilterNameAnd(wantedName, null));
                } else {
                    temp3 = temp2;
                }
                out.addEventsAlways(temp3);
            }
        }
        out.sort();
        out.removeEqualElements();

        return out;
    }

    @Override
    public int getId() {
        return ID_NOT_PRESENT;
    }

    @Override
    public int getSchool() {
        return Constants.SCHOOL_NONE;
    }

}
