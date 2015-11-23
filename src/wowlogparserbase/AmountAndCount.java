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

import wowlogparserbase.events.PeriodicInterface;
import wowlogparserbase.events.PowerEvent;
import wowlogparserbase.events.damage.DamageEvent;
import wowlogparserbase.events.healing.HealingEvent;

/**
 * Container class for the amount of something and how many misses, resists, dodges etc.
 * @author racy
 */
public class AmountAndCount {

    public long amount = 0;
    public long amountHit = 0;
    public long amountCrit = 0;
    public long amountGlancing = 0;
    public long amountCrushing = 0;
    public long overAmount = 0;
    public long maxAmount = 0;
    public long amountResisted = 0;
    public long amountBlocked = 0;
    public long amountAbsorbed = 0;
    public int hit = 0; //All hits
    public int crit = 0; //Crits, part of hits are crits
    public int totHit = 0; //All hits, crits
    public int totMiss = 0;
    public int miss = 0;
    public int dodge = 0;
    public int parry = 0;
    public int absorb = 0;
    public int block = 0;
    public int resist = 0;
    public int reflect = 0;
    public int crushing = 0;
    public int glancing = 0;

    public int school = 0;
    public int numPeriodicHits = 0;
    public int numDirectHits = 0;

    /**
     * Empty constructor
     */
    public AmountAndCount() {
        
    }

    /**
     * Copy constructor.
     * @param a The object to copy.
     */
    public AmountAndCount(AmountAndCount a) {
        this.amount = a.amount;
        this.amountGlancing = a.amountGlancing;
        this.amountCrushing = a.amountCrushing;
        this.amountHit = a.amountHit;
        this.amountCrit = a.amountCrit;
        this.overAmount = a.overAmount;
        this.maxAmount = a.maxAmount;
        this.amountAbsorbed = a.amountAbsorbed;
        this.amountBlocked = a.amountBlocked;
        this.amountResisted = a.amountResisted;
        this.hit = a.hit;
        this.crit = a.crit;
        this.totHit = a.totHit;
        this.totMiss = a.totMiss;
        this.miss = a.miss;
        this.dodge = a.dodge;
        this.parry = a.parry;
        this.absorb = a.absorb;
        this.block = a.block;
        this.resist = a.resist;
        this.reflect = a.reflect;
        this.crushing = a.crushing;
        this.glancing = a.glancing;
        this.school = a.school;
        this.numDirectHits = a.numDirectHits;
        this.numPeriodicHits = a.numPeriodicHits;
    }
    
    /**
     * Check if all fields are 0.
     * @return false if all fields are 0, true otherwise.
     */
    public boolean isZero() {
        if (this.absorb != 0) {
            return false;
        }
        if (this.amount != 0) {
            return false;
        }
        if (this.amountGlancing != 0) {
            return false;
        }
        if (this.amountCrushing != 0) {
            return false;
        }
        if (this.amountHit != 0) {
            return false;
        }
        if (this.amountCrit != 0) {
            return false;
        }
        if (this.overAmount != 0) {
            return false;
        }
        if (this.amountAbsorbed != 0) {
            return false;
        }
        if (this.amountBlocked != 0) {
            return false;
        }
        if (this.amountResisted != 0) {
            return false;
        }
        if (this.block != 0) {
            return false;
        }
        if (this.crit != 0) {
            return false;
        }
        if (this.crushing != 0) {
            return false;
        }
        if (this.glancing != 0) {
            return false;
        }
        if (this.dodge != 0) {
            return false;
        }
        if (this.hit != 0) {
            return false;
        }
        if (this.maxAmount != 0) {
            return false;
        }
        if (this.miss != 0) {
            return false;
        }
        if (this.parry != 0) {
            return false;
        }
        if (this.reflect != 0) {
            return false;
        }
        if (this.resist != 0) {
            return false;
        }
        if (this.totHit != 0) {
            return false;
        }
        if (this.totMiss != 0) {
            return false;
        }
        if (this.numDirectHits != 0) {
            return false;
        }
        if (this.numPeriodicHits != 0) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Add one AmountAndCount to this one
     * @param a The other AmountAndCount
     */
    public void add(AmountAndCount a) {
        amount += a.amount;
        amountGlancing += a.amountGlancing;
        amountCrushing += a.amountCrushing;
        amountHit += a.amountHit;
        amountCrit += a.amountCrit;
        overAmount += a.overAmount;
        amountAbsorbed += a.amountAbsorbed;
        amountBlocked += a.amountBlocked;
        amountResisted += a.amountResisted;
        hit += a.hit;
        crit += a.crit;
        totHit += a.totHit;
        totMiss += a.totMiss;
        miss += a.miss;
        dodge += a.dodge;
        parry += a.parry;
        absorb += a.absorb;
        block += a.block;
        resist += a.resist;
        reflect += a.reflect;        
        crushing += a.crushing;
        glancing += a.glancing;
        if (a.maxAmount > maxAmount) {
            maxAmount = a.maxAmount;
        }
        numDirectHits += a.numDirectHits;
        numPeriodicHits += a.numPeriodicHits;
    }
    
    /**
     * Add a hit to this AmountAndCount
     * @param amount The amount
     * @param crit true if a crit
     */
    public void addHitType(int amount, boolean crit) {
        this.amount += amount;
        if (amount > maxAmount) {
            maxAmount = amount;
        }
        if (crit) {
            this.amountCrit += amount;
            this.crit++;
            this.totHit++;
        } else {
            this.amountHit += amount;
            this.hit++;
            this.totHit++;
        }
    }
    
    public void addPowerHitType(PowerEvent e) {
        this.amount += e.getAmount();
        if (e.getAmount() > maxAmount) {
            maxAmount = e.getAmount();
        }
        
        totHit++;
        this.hit++;

        if (e instanceof PeriodicInterface) {
            numPeriodicHits++;
        } else {
            numDirectHits++;
        }
    }

    public void addHealingHitType(HealingEvent e) {
        this.amount += e.getHealing();
        if (e.getHealing() > maxAmount) {
            maxAmount = e.getHealing();
        }
        this.overAmount += e.getOverhealingCalculated();        
        
        totHit++;
        if (e.isCrit()) {
            this.amountCrit += e.getHealing();
            this.crit++;
        } else {
            this.amountHit += e.getHealing();
            this.hit++;
        }

        if (e instanceof PeriodicInterface) {
            numPeriodicHits++;
        } else {
            numDirectHits++;
        }
    }

    public void addDamageHitType(DamageEvent e) {
        this.amount += e.getDamage();
        if (e.getDamage() > maxAmount) {
            maxAmount = e.getDamage();
        }
        this.amountAbsorbed += e.getAbsorbed();
        this.amountBlocked += e.getBlocked();
        this.amountResisted += e.getResisted();
        
        totHit++;
        if (e.isCrit()) {
            this.amountCrit += e.getDamage();
            this.crit++;
        } else if (e.isCrushing()) {
            this.amountCrushing += e.getDamage();
            this.crushing++;
        } else if (e.isGlancing()) {
            this.amountGlancing += e.getDamage();
            this.glancing++;
        } else {
            this.amountHit += e.getDamage();
            this.hit++;
        }
        if (e instanceof PeriodicInterface) {
            numPeriodicHits++;
        } else {
            numDirectHits++;
        }
    }
    
    /**
     * Add a miss to this AmountAndCount
     * @param e The miss event
     */
    public void addMissType(DamageEvent e) {
        if (e.isMiss()) {
            miss++;
            totMiss++;
        }        
        if (e.isDodge()) {
            dodge++;
            totMiss++;
        }        
        if (e.isParry()) {
            parry++;
            totMiss++;
        }        
        if (e.isAbsorb()) {
            absorb++;
            totMiss++;
        }        
        if (e.isBlock()) {
            block++;
            totMiss++;
        }        
        if (e.isResist()) {
            resist++;
            totMiss++;
        }        
        if (e.isReflect()) {
            reflect++;
            totMiss++;
        }        
    }
    
    /**
     * Add an overAmount to this AmountAndCount
     * @param overAmount The overAmount
     */
    public void addOverAmountType(int overAmount) {
        this.overAmount += overAmount;
    }
}
