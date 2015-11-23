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
import wowlogparserbase.WLPNumberFormat;
import wowlogparserbase.Constants;

/**
 *
 * @author racy
 */
public class FightParticipantsTableModel extends AbstractParticipantsTableModel {

    public final String[] columnNames = {"Name", "Damage", "DPS", "Healing", "HPS", "Class"};
    public int numColumns = columnNames.length;

    List<FightParticipant> participants;
    
    Object[][] data;
    
    ArrayList<TableModelListener> listeners = new ArrayList<TableModelListener>();
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
        participants = fight.getParticipants();
        data = new Object[participants.size()][numColumns];
        if (participants.size() == 0) {
            return;
        }
        int k;
        long[] damageArr = new long[participants.size()];
        long[] healingArr = new long[participants.size()];
        long allDamage = 0;
        long allHealing = 0;
        for (k=0; k<participants.size(); k++) {
            FightParticipant fp = participants.get(k);
            long totalDamage = fp.totalDamage(Constants.SCHOOL_ALL, FightParticipant.TYPE_ALL_DAMAGE).amount;
            long totalHealing = fp.totalHealing(Constants.SCHOOL_ALL, FightParticipant.TYPE_ALL_HEALING).amount;
            damageArr[k] = totalDamage;
            healingArr[k] = totalHealing;
            allDamage += totalDamage;
            allHealing += totalHealing;
        }

        float percentScale = 1;
        long[] sortedDam = Arrays.copyOf(damageArr, damageArr.length);
        long[] sortedHeal = Arrays.copyOf(healingArr, healingArr.length);
        Arrays.sort(sortedDam);
        Arrays.sort(sortedHeal);
        long maxDam = sortedDam[sortedDam.length-1];
        long maxHeal = sortedHeal[sortedHeal.length-1];
        //double damPercentScale = 1 / ((double)maxDam / (double)allDamage);
        //double healPercentScale = 1 / ((double)maxHeal / (double)allHealing);
        double damPercentScale = MathHelper.makePercentSafe((double)allDamage, (double)maxDam);
        double healPercentScale = MathHelper.makePercentSafe((double)allHealing, (double)maxHeal);
        
        Color lightRed = new Color(255, 100, 100);
        for (k=0; k<participants.size(); k++) {
            FightParticipant fp = participants.get(k);
            String name = fp.getName();
            long totalDamage = damageArr[k];
            String damage = "" + totalDamage;
            //double duration = fight.getActiveDuration();
            double activeDamageDuration = fp.getActiveDamageTime(15, 1.5, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
            double activeHealingDuration = fp.getActiveHealingTime(15, 1.5, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
            if (activeDamageDuration == 0) {
                activeDamageDuration = 1;
            }
            if (activeHealingDuration == 0) {
                activeHealingDuration = 1;
            }
            
            NumberFormat nf = WLPNumberFormat.getInstance();
            nf.setMaximumFractionDigits(1);
            nf.setGroupingUsed(false);
            String dps = "" + nf.format((totalDamage/activeDamageDuration));
            long totalHealing = healingArr[k];
            String healing = "" + totalHealing;
            String hps = "" + nf.format((totalHealing/activeHealingDuration));
            double damPercent = MathHelper.makePercentSafe((double) totalDamage, (double) allDamage)*100.0;
            double healPercent = MathHelper.makePercentSafe((double) totalHealing, (double) allHealing)*100.0;
            data[k][0] = name;
            data[k][1] = new StringPercentColor(damage + " (" + nf.format(damPercent) + "%)", damPercent*damPercentScale, lightRed);
            data[k][2] = dps;
            data[k][3] = new StringPercentColor(healing + " (" + nf.format(healPercent) + "%)", healPercent*healPercentScale, Color.GREEN);
            data[k][4] = hps;
            data[k][5] = fp.getClassName();
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
