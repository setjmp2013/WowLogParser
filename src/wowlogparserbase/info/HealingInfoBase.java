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

import java.util.List;
import wowlogparserbase.*;
import wowlogparserbase.events.*;
import wowlogparserbase.events.healing.*;
import wowlogparserbase.eventfilter.*;

/**
 *
 * @author racy
 */
public abstract class HealingInfoBase extends HealingInfo {
    double totalDuration;
    private FightParticipant participant;
    private AmountAndCount a;
    String skillName = "";
    double activeParticipantDuration;

    public HealingInfoBase(EventCollection fight, FightParticipant participant) {
        this.participant = participant;
        this.totalDuration = fight.getActiveDuration();
    }

    protected void setAmountAndCount(AmountAndCount a) {
        this.a = a;
        activeParticipantDuration = participant.getActiveHealingTime(15, 1.5, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
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
    public double getHealingAverage() {
        double average = 0;
        if (a.totHit > 0) {
            average = (double)a.amount / (double)a.totHit;
        }
        return average;
    }

    @Override
    public double getHps() {
        double hps = 0;
        if (totalDuration > 0) {
            hps = (double) (a.amount) / totalDuration;
        }
        return hps;
    }

    @Override
    public double getHpsActive() {
        double hps = 0;
        if (activeParticipantDuration > 0) {
            hps = (double) (a.amount) / activeParticipantDuration;
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
    public int getCollectionType() {
        return HEALING;
    }
}
