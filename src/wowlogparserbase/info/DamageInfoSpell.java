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
import wowlogparserbase.events.*;
import wowlogparserbase.events.damage.*;
import wowlogparserbase.eventfilter.*;

/**
 *
 * @author racy
 */
public class DamageInfoSpell extends DamageInfoBase {
    SpellInfo si;
    int unit;
    int wantedType = FightParticipant.TYPE_SPELL_DAMAGE;

    public DamageInfoSpell(EventCollection fight, FightParticipant participant, SpellInfo si) {
        super(fight.getActiveDuration(), participant);
        this.si = si;
        this.unit = FightParticipant.UNIT_ALL;
        parse();
    }

    public DamageInfoSpell(EventCollection fight, FightParticipant participant, SpellInfo si, int unit) {
        super(fight.getActiveDuration(), participant);
        this.si = si;
        this.unit = unit;
        parse();
    }

    public DamageInfoSpell(EventCollection fight, FightParticipant participant, SpellInfo si, int unit, int wantedType) {
        super(fight.getActiveDuration(), participant);
        this.si = si;
        this.unit = unit;
        this.wantedType = wantedType;
        parse();
    }

    private void parse() {
        setSkillName(si.name);
        AmountAndCount a;
        a = getParticipant().damage(si, wantedType, unit);
        setAmountAndCount(a);
        switch(unit) {
            case FightParticipant.UNIT_PLAYER:
                setSkillName(si.name);
                break;
            case FightParticipant.UNIT_PET:
                setSkillName("Pet: " + si.name);
                break;
            case FightParticipant.UNIT_ALL:
                setSkillName(si.name);
                break;
            default:
                setSkillName(si.name);
                break;
        }
    }

    @Override
    public EventCollectionSimple getEvents() {
        EventCollectionSimple out = new EventCollectionSimple();

        if ((unit & FightParticipant.UNIT_PLAYER) != 0) {
            EventCollection temp = getParticipant().filter(new FilterClass(DamageEvent.class)).filter(new FilterSkill(si.spellID, null, FilterSkill.ANY_SCHOOL, FilterSkill.ANY_POWER_TYPE)).filter(new FilterEventType(wantedType));
            out.addEventsAlways(temp);
        }

        if ((unit & FightParticipant.UNIT_PET) != 0) {
            List<PetInfo> pets = getParticipant().getPetNames();
            for (PetInfo p : pets) {
                EventCollection tempPet = p.filter(new FilterClass(DamageEvent.class)).filter(new FilterSkill(si.spellID, null, FilterSkill.ANY_SCHOOL, FilterSkill.ANY_POWER_TYPE)).filter(new FilterEventType(wantedType));
                out.addEventsAlways(tempPet);
            }
        }
        out.sort();
        out.removeEqualElements();
        
        return out;
    }

    @Override
    public int getId() {
        return si.spellID;
    }

    @Override
    public int getSchool() {
        return si.school;
    }

}
