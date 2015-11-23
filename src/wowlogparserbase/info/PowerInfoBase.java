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
import wowlogparserbase.events.healing.*;
import wowlogparserbase.eventfilter.*;

/**
 *
 * @author racy
 */
public abstract class PowerInfoBase extends PowerInfo {
    double totalDuration;
    private FightParticipant participant;
    private AmountAndCount a;
    String skillName = "";
    double activeParticipantDuration;
    int powerKind = 0;
    int powerType = Constants.POWER_TYPE_UNKNOWN;

    public PowerInfoBase(EventCollection fight, FightParticipant participant) {
        this.participant = participant;
        this.totalDuration = fight.getActiveDuration();
    }

    protected void setAmountAndCount(AmountAndCount a) {
        this.a = a;
        activeParticipantDuration = participant.getActivePowerTime(15, 1.5, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }

    public void setNoCounts() {
        setNoNums(a);
    }

    public FightParticipant getParticipant() {
        return participant;
    }

    @Override
    public AmountAndCount getAmountAndCount() {
        return a;
    }

    @Override
    public double getPowerAverage() {
        double average = 0;
        if (a.totHit > 0) {
            average = (double)a.amount / (double)a.totHit;
        }
        return average;
    }

    @Override
    public double getPps() {
        double hps = 0;
        if (totalDuration > 0) {
            hps = (double) a.amount / totalDuration;
        }
        return hps;
    }

    @Override
    public double getPpsActive() {
        double hps = 0;
        if (activeParticipantDuration > 0) {
            hps = (double) a.amount / activeParticipantDuration;
        }
        return hps;
    }

    @Override
    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String n) {
        skillName = n;
    }

    @Override
    public int getPowerKind() {
        return powerKind;
    }

    public void setPowerKind(int powerKind) {
        this.powerKind = powerKind;
    }

    void parsePowerKind(int type) {
        int parsedKind = 0;
        if ((type & FightParticipant.TYPE_SPELL_DRAIN) != 0) {
            parsedKind |= POWER_KIND_DRAIN;
        }
        if ((type & FightParticipant.TYPE_SPELL_DRAIN_DIRECT) != 0) {
            parsedKind |= POWER_KIND_DRAIN;
        }
        if ((type & FightParticipant.TYPE_SPELL_DRAIN_PERIODIC) != 0) {
            parsedKind |= POWER_KIND_DRAIN;
        }

        if ((type & FightParticipant.TYPE_SPELL_ENERGIZE) != 0) {
            parsedKind |= POWER_KIND_ENERGIZE;
        }
        if ((type & FightParticipant.TYPE_SPELL_ENERGIZE_DIRECT) != 0) {
            parsedKind |= POWER_KIND_ENERGIZE;
        }
        if ((type & FightParticipant.TYPE_SPELL_ENERGIZE_PERIODIC) != 0) {
            parsedKind |= POWER_KIND_ENERGIZE;
        }

        if ((type & FightParticipant.TYPE_SPELL_LEECH) != 0) {
            parsedKind |= POWER_KIND_LEECH;
        }
        if ((type & FightParticipant.TYPE_SPELL_LEECH_DIRECT) != 0) {
            parsedKind |= POWER_KIND_LEECH;
        }
        if ((type & FightParticipant.TYPE_SPELL_LEECH_PERIODIC) != 0) {
            parsedKind |= POWER_KIND_LEECH;
        }
        setPowerKind(parsedKind);
    }

    @Override
    public int getPowerType() {
        return powerType;
    }

    public void setPowerType(int powerType) {
        this.powerType = powerType;
    }

    @Override
    public int getCollectionType() {
        return POWER;
    }
}
