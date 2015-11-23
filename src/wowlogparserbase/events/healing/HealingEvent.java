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

package wowlogparserbase.events.healing;

import org.w3c.dom.Element;
import wowlogparserbase.events.AmountInterface;
import wowlogparserbase.events.BasicEvent;

/**
 * The base class for all healing events
 * @author racy
 */
public abstract class HealingEvent extends BasicEvent implements AmountInterface {
    private int healing = 0;
    private boolean crit = false;
    private int overhealingCalculated = 0;
    private int overhealing = -1;
    private int healthDeficit = 0;
    private int absorbed = 0;
    
    public HealingEvent() {
        
    }
    
    public int parse(String timeDate, String[] values) {
        return super.parse(timeDate, values);
    }
    
    public String toString() {
        String s = super.toString();
        s = s + "Healing = " + getHealing() + newLine;
        s = s + "Crit = " + isCrit() + newLine;
        s = s + "Over Healing = " + getOverhealingCalculated() + newLine;
        return s;
    }

    public Element addXmlAttributes(Element e) {
        super.addXmlAttributes(e);
        e.setAttribute(XMLTAG_AMOUNT, ""+getHealing());
        e.setAttribute(XMLTAG_AMOUNT_OVERHEAL, ""+getOverhealing());
        e.setAttribute(XMLTAG_HEALING_CRIT, ""+(isCrit()?1:0));
        return e;
    }

    public int getAbsorbed() {
        return absorbed;
    }

    public void setAbsorbed(int absorbed) {
        this.absorbed = absorbed;
    }

    public int getHealing() {
        return healing;
    }

    public void setHealing(int healing) {
        this.healing = healing;
    }

    public boolean isCrit() {
        return crit;
    }

    public void setCrit(boolean crit) {
        this.crit = crit;
    }

    public int getOverhealingCalculated() {
        return overhealingCalculated;
    }

    public void setOverhealingCalculated(int overhealingCalculated) {
        this.overhealingCalculated = overhealingCalculated;
    }

    public int getOverhealing() {
        return overhealing;
    }

    public void setOverhealing(int overhealing) {
        this.overhealing = overhealing;
    }

    public int getHealthDeficit() {
        return healthDeficit;
    }

    public void setHealthDeficit(int healthDeficit) {
        this.healthDeficit = healthDeficit;
    }

    @Override
    public int getAmount() {
        return healing;
    }

}
