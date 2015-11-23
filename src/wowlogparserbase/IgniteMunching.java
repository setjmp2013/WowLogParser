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

import wowlogparserbase.eventfilter.FilterDamageFlagsOr;
import wowlogparserbase.eventfilter.FilterEventType;
import wowlogparserbase.eventfilter.FilterSkill;
import wowlogparserbase.events.damage.DamageEvent;

/**
 *
 * @author racy
 */
public class IgniteMunching {

    public IgniteMunching() {
    }

    public double calcSpellCritDamage(EventCollection ec) {
        EventCollection ec2 = ec.filter(new FilterDamageFlagsOr(DamageEvent.CRIT));
        return ec2.totalDamage(Constants.SCHOOL_FIRE, EventCollection.TYPE_SPELL_DAMAGE).amount;
    }
    
    public double calcIgniteDamage(EventCollection ec) {
        EventCollection ec2 = ec.filter(new FilterEventType(EventCollection.TYPE_SPELL_DAMAGE_PERIODIC))
                .filter(new FilterSkill(FilterSkill.ANY_ID, "Ignite", FilterSkill.ANY_SCHOOL, FilterSkill.ANY_POWER_TYPE));
        return ec2.totalDamage(Constants.SCHOOL_FIRE, EventCollection.TYPE_SPELL_DAMAGE_PERIODIC).amount;
    }

    public double calcIngiteMunching(EventCollection ec) {
        double critDmg = calcSpellCritDamage(ec);
        double igniteDmg = calcIgniteDamage(ec);
        double munching = critDmg*0.4 - igniteDmg;
        return munching;
    }
}
