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

import wowlogparserbase.*;
import wowlogparserbase.events.BasicEvent;

/**
 *
 * @author racy
 */
public abstract class DamageInfoBase extends DamageInfo {
    private AmountAndCount a;
    private FightParticipant participant;
    double totalDuration;
    double activeParticipantDuration;
    String skillName = "";

    public DamageInfoBase(double totalDuration, FightParticipant participant) {
        this.participant = participant;
        this.totalDuration = totalDuration;
    }

    protected void setAmountAndCount(AmountAndCount a) {
        this.a = a;
        activeParticipantDuration = participant.getActiveDamageTime(15, 1.5, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        fixMissesForPurePeriodic(this.a);
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
    public double getDamageAverage() {
        double average = 0;
        if (a.totHit > 0) {
            average = (double)a.amount / (double)a.totHit;
        }
        return average;
    }

    @Override
    public double getDps() {
        double dps = 0;
        if (totalDuration > 0) {
            dps = (double) a.amount / totalDuration;
        }
        return dps;
    }

    @Override
    public double getDpsActive() {
        double dps = 0;
        if (activeParticipantDuration > 0) {
            dps = (double) a.amount / activeParticipantDuration;
        }
        return dps;
    }

    @Override
    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String n) {
        skillName = n;
    }

    @Override
    public double getTotalDuration() {
        return totalDuration;
    }

    @Override
    public int getCollectionType() {
        return DAMAGE;
    }


}
