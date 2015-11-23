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

package wowlogparserbase.tablemodels;

import wowlogparserbase.helpers.MathHelper;
import wowlogparserbase.tablerendering.StringPercentColor;
import java.awt.Color;
import wowlogparserbase.Fight;
import wowlogparserbase.FightParticipant;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import wowlogparserbase.AmountAndCount;
import wowlogparserbase.WLPNumberFormat;
import wowlogparserbase.Constants;

/**
 *
 * @author racy
 */
public class FightParticipantsTableModelHealing extends AbstractParticipantsTableModel {

    public final String[] columnNames = {"Name", "Healing", "HPS", "Raw Heal", "Overheal%", "Presence", "Class"};
    public int numColumns = columnNames.length;

    List<FightParticipant> participants;
    Object[][] data;
    
    List<TableModelListener> listeners = new ArrayList<TableModelListener>();
    Fight fight = null;

    @Override
    public boolean isEnemy() {
        return false;
    }
    
    @Override
    public List<FightParticipant> getParticipants() {
        return participants;
    }
    
    public void setFight(Fight f) {
        this.fight = f;
        participants = f.getParticipants();
        data = new Object[participants.size()][numColumns];
        int k;
        long[] healingArr = new long[participants.size()];
        long[] overHealingArr = new long[participants.size()];
        long[] rawHealingArr = new long[participants.size()];
        long allHealing = 0;
        long allRawHealing = 0;
        long allOverHealing = 0;
        for (k=0; k<participants.size(); k++) {
            FightParticipant fp = participants.get(k);
            AmountAndCount a = fp.totalHealing(Constants.SCHOOL_ALL, FightParticipant.TYPE_ALL_HEALING);
            long totalHealing = a.amount;
            long totalOverHealing = a.overAmount;
            
            healingArr[k] = totalHealing - totalOverHealing;
            rawHealingArr[k] = totalHealing;
            overHealingArr[k] = totalOverHealing;
            allHealing += totalHealing - totalOverHealing;
            allRawHealing += totalHealing;
            allOverHealing += totalOverHealing;
        }

        float percentScale = 1;
        long[] sortedHeal = Arrays.copyOf(healingArr, healingArr.length);
        Arrays.sort(sortedHeal);
        long maxHeal = sortedHeal[sortedHeal.length-1];
        //double healPercentScale = 1 / ((double)maxHeal / (double)allHealing);
        double healPercentScale = MathHelper.makePercentSafe((double)allHealing, (double)maxHeal);

        float percentScaleRaw = 1;
        long[] sortedHealRaw = Arrays.copyOf(rawHealingArr, rawHealingArr.length);
        Arrays.sort(sortedHealRaw);
        long maxHealRaw = sortedHealRaw[sortedHealRaw.length-1];
        //double healPercentScaleRaw = 1 / ((double)maxHealRaw / (double)allRawHealing);
        double healPercentScaleRaw = MathHelper.makePercentSafe((double)allRawHealing, (double)maxHealRaw);

        for (k=0; k<participants.size(); k++) {
            FightParticipant fp = participants.get(k);
            String name = fp.getName();
            //double duration = fight.getActiveDuration();
            double activeHealingDuration = fp.getActiveHealingTime(15, 1.5, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
            if (activeHealingDuration == 0) {
                activeHealingDuration = 1;
            }
            
            NumberFormat nf = WLPNumberFormat.getInstance();
            nf.setMaximumFractionDigits(1);
            nf.setGroupingUsed(false);
            NumberFormat nf2 = WLPNumberFormat.getInstance();
            nf2.setMaximumFractionDigits(0);
            nf2.setGroupingUsed(false);
            long totalHealing = healingArr[k];
            long totalHealingRaw = rawHealingArr[k];
            long totalHealingOver = overHealingArr[k];
            double healPercent = MathHelper.makePercentSafe((double) totalHealing, (double) allHealing)*100.0;
            double healPercentRaw = MathHelper.makePercentSafe((double) totalHealingRaw, (double) allRawHealing)*100.0;
            double overHealPercent = MathHelper.makePercentSafe((double) totalHealingOver, (double) totalHealingRaw) * 100.0;

            String healing = "" + totalHealing;
            String hps = "" + nf.format((totalHealing/activeHealingDuration));
            String healingRaw = "" + totalHealingRaw;
            data[k][0] = name;
            data[k][1] = new StringPercentColor(healing + " (" + nf.format(healPercent) + "%)", healPercent*healPercentScale, Color.GREEN);
            data[k][2] = hps;
            data[k][3] = new StringPercentColor(healingRaw + " (" + nf.format(healPercentRaw) + "%)", healPercentRaw*healPercentScaleRaw, Color.GREEN);
            data[k][4] = nf.format(overHealPercent) + " %";
            data[k][5] = nf2.format(Math.round(MathHelper.makePercentSafe(fp.getActiveDuration(), f.getActiveDuration())*100)) + " %";
            data[k][6] = fp.getClassName();
        }
        TableModelEvent ev = new TableModelEvent(this);
        fireTableModelEvent(ev);
    }
    
    public void clearFights() {
        fight = null;
        participants = null;
        data= new String[0][numColumns];
        TableModelEvent ev = new TableModelEvent(this);
        fireTableModelEvent(ev);        
    }
    
    void fireTableModelEvent(TableModelEvent e) {
        for(TableModelListener l : listeners) {
            l.tableChanged(e);
        }
    }

    public void addTableModelListener(TableModelListener l) {
        listeners.add(l);
    }

    public Class<?> getColumnClass(int columnIndex) {
        return data[0][columnIndex].getClass();
    }

    public int getColumnCount() {
        return numColumns;
    }

    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }

    public int getRowCount() {
        if(fight != null) {
            return participants.size();
        } else {
            return 0;
        }
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        return data[rowIndex][columnIndex];
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public void removeTableModelListener(TableModelListener l) {
        listeners.remove(l);
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        data[rowIndex][columnIndex] = (String)aValue;
    }

}
