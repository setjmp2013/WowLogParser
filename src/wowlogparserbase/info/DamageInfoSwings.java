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

import java.util.ArrayList;
import java.util.List;
import wowlogparserbase.*;
import wowlogparserbase.events.damage.*;
import wowlogparserbase.eventfilter.*;
import wowlogparserbase.Constants;

/**
 *
 * @author racy
 */
public class DamageInfoSwings extends DamageInfoBase {

    private static final int school = Constants.SCHOOL_PHYSICAL;
    int unit;

    public DamageInfoSwings(EventCollection fight, FightParticipant participant) {
        super(fight.getActiveDuration(), participant);
        unit = FightParticipant.UNIT_ALL;
        parse();
    }

    public DamageInfoSwings(EventCollection fight, FightParticipant participant, int unit) {
        super(fight.getActiveDuration(), participant);
        this.unit = unit;
        parse();
    }

    public DamageInfoSwings(EventCollection fight, FightParticipant participant, int unit, String skillName) {
        super(fight.getActiveDuration(), participant);
        this.unit = unit;
        setSkillName(skillName);
        parse();
    }

    private void parse() {
        AmountAndCount a;
        a = getParticipant().totalDamage(Constants.SCHOOL_ALL, FightParticipant.TYPE_SWING_DAMAGE, unit);
        setAmountAndCount(a);
        switch(unit) {
            case FightParticipant.UNIT_PLAYER:
                setSkillName("Melee Swings");
                break;
            case FightParticipant.UNIT_PET:
                setSkillName("Pet: Melee Swings");
                break;
            case FightParticipant.UNIT_ALL:
                setSkillName("Melee Swings");
                break;
            default:
                setSkillName("Melee Swings");
                break;
        }
    }

    @Override
    public EventCollectionSimple getEvents() {
        EventCollectionSimple out = new EventCollectionSimple();
        List<Filter> filters = new ArrayList<Filter>();
        filters.add(new FilterClass(SwingDamageEvent.class));
        filters.add(new FilterClass(SwingMissedEvent.class));

        if ((unit & FightParticipant.UNIT_PLAYER) != 0) {
            EventCollection temp = getParticipant().filterOr(filters);
            out.addEventsAlways(temp);
        }

        if ((unit & FightParticipant.UNIT_PET) != 0) {
            List<PetInfo> pets = getParticipant().getPetNames();
            for (PetInfo p : pets) {
                EventCollection tempPet = p.filterOr(filters);
                out.addEventsAlways(tempPet);
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
        return school;
    }

}
