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

package wowlogparser.gui;

import java.util.ArrayList;
import java.util.List;
import org.jfree.data.DomainInfo;
import org.jfree.data.DomainOrder;
import org.jfree.data.Range;
import org.jfree.data.RangeInfo;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.SeriesChangeEvent;
import org.jfree.data.xy.AbstractXYDataset;
import wowlogparserbase.EventCollection;
import wowlogparserbase.EventCollectionSimple;
import wowlogparserbase.events.BasicEvent;
import wowlogparserbase.events.PowerEvent;
import wowlogparserbase.events.SkillInterface;
import wowlogparserbase.events.SkillInterfaceExtra;
import wowlogparserbase.events.SpellEnergizeEvent;
import wowlogparserbase.events.damage.DamageEvent;
import wowlogparserbase.events.healing.HealingEvent;

/**
 *
 * @author racy
 */
public class EventXYDataset extends AbstractXYDataset implements RangeInfo, DomainInfo {
    private List<Comparable> seriesKeys;
    private List<EventCollectionSimple> seriesDataY;
    private List<List<Double>> seriesDataX;
    private List<Boolean> seriesVisible;

    private List<Boolean> useHealthDeficit;
    
    double domainBorder = 0;
    double rangeBorder = 0;
    
    public EventXYDataset() {
        seriesKeys = new ArrayList<Comparable>();
        seriesDataY = new ArrayList<EventCollectionSimple>();
        seriesDataX = new ArrayList<List<Double>>();
        useHealthDeficit = new ArrayList<Boolean>();
        seriesVisible = new ArrayList<Boolean>();
    }

    public EventXYDataset(double rangeBorder, double domainBorder) {
        this();
        this.rangeBorder = rangeBorder;
        this.domainBorder = domainBorder;
    }

    public BasicEvent getEvent(int series, int item) {
        return seriesDataY.get(series).getEvent(item);
    }
    
    public String getEventTooltip(int series, int item) {        
        if (useHealthDeficit.get(series)) {
            String s = "<html><table border=\"0\" rules=\"none\">";
            s += "<tr><td>Health deficit</td>" + "<td><b>" + getYValue(series, item) + "</b></td></tr>";
            s += "</table></html>";
            return s;
        }
        BasicEvent e = getEvent(series, item);
        String s = "<html><table border=\"0\" rules=\"none\">";
        s += "<tr><td>Log type</td>" + "<td><b>" + e.getLogType() +"</b></td></tr>";
        s += "<tr><td>Log row</td>" + "<td><b>" + (e.logFileRow+1) +"</b></td></tr>";
        s += "<tr><td>Source</td>" + "<td><b>" + e.getSourceName() + "</b></td></tr> ";
        s += "<tr><td>Destination</td>" + "<td><b>" + e.getDestinationName() + "</b></td></tr>";
        if (e instanceof DamageEvent || e instanceof HealingEvent || e instanceof PowerEvent) {
            s += "<tr><td>Amount</td>" + "<td><b>" + getYValue(series, item) + "</b></td></tr>";
        }
        if (e instanceof DamageEvent) {
            DamageEvent de = (DamageEvent)e;
            String s2 = "";
            if (de.isCrit()) {
                s2 += "Crit ";
            }
            if (de.isCrushing()) {
                s2 += "Crushing ";
            }
            if (de.isGlancing()) {
                s2 += "Glancing ";
            }
            if (de.isMiss()) {
                s2 += "Miss ";
            }
            if (de.isDodge()) {
                s2 += "Dodge ";
            }
            if (de.isParry()) {
                s2 += "Parry ";
            }
            if (de.isReflect()) {
                s2 += "Reflect ";
            }
            if (de.isResist()) {
                s2 += "Resist ";
            }
            if (s2.compareTo("") != 0) {
                s += "<tr><td>Flags: </td>" + "<td><b>" + s2 + "</b></td></tr>";
            }

            if (de.getAbsorbed() != 0) {
                s += "<tr><td>Absorbed</td><td><b>"+de.getAbsorbed()+"</b></td></tr>";
            }
            if (de.getBlocked() != 0) {
                s += "<tr><td>Blocked</td><td><b>"+de.getBlocked()+"</b></td></tr>";
            }
            if (de.getResisted() != 0) {
                s += "<tr><td>Resisted</td><td><b>"+de.getResisted()+"</b></td></tr>";
            }
        }
        if (e instanceof HealingEvent) {
            HealingEvent he = (HealingEvent) e;
            s += "<tr><td>Overheal</td>" + "<td><b>" + he.getOverhealingCalculated() + "</b></td></tr>";

        }
        if (e instanceof SkillInterface) {
            SkillInterface si = (SkillInterface)e;
            s += "<tr><td>Skill Name</td>" + "<td><b>" + si.getSkillName() + "</b></td></tr>";
        }
        if (e instanceof SkillInterfaceExtra) {
            SkillInterfaceExtra si = (SkillInterfaceExtra)e;
            s += "<tr><td>Aura Name</td>" + "<td><b>" + si.getExtraSpellName() + "</b></td></tr>";
        }
        if (e instanceof PowerEvent) {
            PowerEvent pe = (PowerEvent)e;
            s += "<tr><td>Power Type</td>" + "<td><b>" + BasicEvent.getPowerTypeString(pe.getPowerType()) + "</b></td></tr>";
        }
        s += "</table></html>";
        return s;
    }
    
    public void setVisible(int series, boolean state) {
        seriesVisible.set(series, state);
        seriesChanged(new SeriesChangeEvent(this));
    }
    
    public void addSeries(Comparable key, List<Double> xValues, EventCollectionSimple yValues, boolean useHealthDeficit, boolean isVisible) {
        seriesKeys.add(key);
        seriesDataX.add(xValues);
        seriesDataY.add(yValues);
        this.useHealthDeficit.add(useHealthDeficit);
        seriesVisible.add(isVisible);
        seriesChanged(new SeriesChangeEvent(this));
    }
    
    public void addSeries(Comparable key, List<Double> xValues, EventCollectionSimple yValues, boolean useHealthDeficit) {
        seriesKeys.add(key);
        seriesDataX.add(xValues);
        seriesDataY.add(yValues);
        this.useHealthDeficit.add(useHealthDeficit);
        seriesVisible.add(true);
        seriesChanged(new SeriesChangeEvent(this));
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        EventXYDataset o = (EventXYDataset)super.clone();
        o.seriesDataY = new ArrayList<EventCollectionSimple>(seriesDataY);
        o.seriesKeys = new ArrayList<Comparable>(seriesKeys);
        o.seriesDataX = new ArrayList<List<Double>>(seriesDataX);
        o.useHealthDeficit = new ArrayList<Boolean>(useHealthDeficit);
        return o;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        return false;
    }

    @Override
    public DomainOrder getDomainOrder() {
        return DomainOrder.NONE;
    }

    @Override
    public int getItemCount(int series) {
        if (seriesVisible.get(series)) {
            return this.seriesDataY.get(series).size();
        } else {
            return 0;
        }
    }

    public int getItemCountAll(int series) {
       return this.seriesDataY.get(series).size();
    }

    @Override
    public int getSeriesCount() {
        return seriesDataY.size();
    }

    @Override
    public Comparable getSeriesKey(int index) {
        return seriesKeys.get(index);
    }

    @Override
    public Number getX(int series, int item) {
        return getXValue(series, item);
    }

    @Override
    public double getXValue(int series, int item) {
        return seriesDataX.get(series).get(item);
    }

    @Override
    public Number getY(int series, int item) {
        return getYValue(series, item);
    }

    @Override
    public double getYValue(int series, int item) {
        BasicEvent e = seriesDataY.get(series).getEvent(item);
        if (e instanceof DamageEvent) {
            DamageEvent de = (DamageEvent)e;
            if (useHealthDeficit.get(series)) {
                return de.getHealthDeficit();
            } else {
                return de.getDamage();
            }
        }
        if (e instanceof HealingEvent) {
            HealingEvent he = (HealingEvent)e;
            if (useHealthDeficit.get(series)) {
                return he.getHealthDeficit();
            } else {
                return he.getHealing();
            }
        }
        if (e instanceof PowerEvent) {
            PowerEvent pe = (PowerEvent)e;
            if (useHealthDeficit.get(series)) {
                return 0;
            } else {
                return pe.getAmount();
            }
        }
        return 0;
    }

    @Override
    public int hashCode() {
        int result;
        result = seriesKeys.hashCode();
        result = 150 * result + this.seriesDataY.hashCode();
        return result;
    }

    @Override
    public int indexOf(Comparable key) {
        return seriesKeys.indexOf(key);
    }

    public void removeSeries(Comparable key) {
        int index = indexOf(key);
        if (index >= 0) {
            seriesKeys.remove(index);
            seriesDataY.remove(index);
            seriesDataY.remove(index);
            useHealthDeficit.remove(index);
            seriesVisible.remove(index);
            seriesChanged(new SeriesChangeEvent(this));
        }
    }

    public Range getRangeBounds(boolean includeInterval) {
        Range r = new Range(getRangeLowerBound(includeInterval), getRangeUpperBound(includeInterval));
        return r;
    }

    public double getRangeLowerBound(boolean includeInterval) {
        double val = Double.POSITIVE_INFINITY;
        for (int sIndex = 0; sIndex<getSeriesCount(); sIndex++) {
            for (int iIndex = 0; iIndex<getItemCountAll(sIndex); iIndex++) {
                val = val < getYValue(sIndex, iIndex) ? val : getYValue(sIndex, iIndex);
            }
        }
        if (val == Double.POSITIVE_INFINITY) {
            return 0;
        }
        return val - rangeBorder;
    }

    public double getRangeUpperBound(boolean includeInterval) {
        double val = Double.NEGATIVE_INFINITY;
        for (int sIndex = 0; sIndex<getSeriesCount(); sIndex++) {
            for (int iIndex = 0; iIndex<getItemCountAll(sIndex); iIndex++) {
                val = val > getYValue(sIndex, iIndex) ? val : getYValue(sIndex, iIndex);
            }
        }
        if (val == Double.NEGATIVE_INFINITY) {
            return 0;
        }
        return val + rangeBorder;
    }

    public Range getDomainBounds(boolean includeInterval) {
        Range r = new Range(getDomainLowerBound(includeInterval), getDomainUpperBound(includeInterval));
        return r;
    }

    public double getDomainLowerBound(boolean includeInterval) {
        double val = Double.POSITIVE_INFINITY;
        for (int sIndex = 0; sIndex<getSeriesCount(); sIndex++) {
            for (int iIndex = 0; iIndex<getItemCountAll(sIndex); iIndex++) {
                val = val < getXValue(sIndex, iIndex) ? val : getXValue(sIndex, iIndex);
            }
        }
        if (val == Double.POSITIVE_INFINITY) {
            return 0;
        }
        return val - domainBorder;
    }

    public double getDomainUpperBound(boolean includeInterval) {
        double val = Double.NEGATIVE_INFINITY;
        for (int sIndex = 0; sIndex<getSeriesCount(); sIndex++) {
            for (int iIndex = 0; iIndex<getItemCountAll(sIndex); iIndex++) {
                val = val > getXValue(sIndex, iIndex) ? val : getXValue(sIndex, iIndex);
            }
        }
        if (val == Double.NEGATIVE_INFINITY) {
            return 0;
        }
        return val + domainBorder;
    }    
    
}
