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
import java.awt.Color;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import wowlogparserbase.tablerendering.StringColor;
import wowlogparserbase.AmountAndCount;
import wowlogparserbase.EventCollection;
import wowlogparserbase.Fight;
import wowlogparserbase.FightParticipant;
import wowlogparserbase.eventfilter.FilterGuidOr;
import wowlogparserbase.events.BasicEvent;
import wowlogparserbase.Constants;


/**
 *
 * @author racy
 */
public class WhoHealsWhomTableModel extends AbstractTableModel {
    private Object[][] data;
    private int numColumns;
    private int numRows;

    private List<FightParticipant> healerParticipants;
    private List<FightParticipant> targetParticipants;
    
    Fight fight;
    public WhoHealsWhomTableModel(Fight fight) {
        this.fight = fight;
        initData();
    }

    @Override
    public int getRowCount() {
        return numRows;
    }

    @Override
    public int getColumnCount() {
        return numColumns;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return data[rowIndex][columnIndex];
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0) {
            return "";
        }
        return healerParticipants.get(column-1).getName();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return data[0][columnIndex].getClass();
    }

    private Object getHealPercent(FightParticipant healer, FightParticipant target) {
        AmountAndCount totHealing = healer.totalHealing(Constants.SCHOOL_ALL, EventCollection.TYPE_ALL_HEALING, EventCollection.UNIT_PLAYER);
        EventCollection filteredEvents = healer.filter(new FilterGuidOr(null, target.getSourceGUID()));
        AmountAndCount targetHealing = filteredEvents.totalHealing(Constants.SCHOOL_ALL, EventCollection.TYPE_ALL_HEALING, EventCollection.UNIT_PLAYER);
        double percent = MathHelper.makePercentSafe((double)targetHealing.amount, (double)totHealing.amount) * 100.0;
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setGroupingUsed(false);
        nf.setMaximumFractionDigits(0);
        if (percent >= 0.5) {
            double red = 1.0 - ((percent - 25.0)/25.0);
            red = red < 0 ? 0 : red;
            red = red > 1 ? 1 : red;
            double green = 1.0 - (-(percent - 25.0)/25.0);
            green = green < 0 ? 0 : green;
            green = green > 1 ? 1 : green;
            StringColor s = new StringColor(null, new Color((int)(red*255), (int)(green*255), 0), nf.format(percent) + "%");
            return s;            
        } else {
            StringColor s = new StringColor(null, null, "");
            return s;
        }
    }

    private void initData() {
        initHealerAndTargetParticipants();
        numRows = targetParticipants.size();
        numColumns = healerParticipants.size() + 1;        
        data = new Object[numRows][numColumns];
        for (int r=0; r<numRows; r++) {
            data[r][0] = new String(targetParticipants.get(r).getName());
        }
        for (int r=0; r<numRows; r++) {
            for (int c=1; c<numColumns; c++) {
                data[r][c] = getHealPercent(healerParticipants.get(c-1), targetParticipants.get(r));
            }
        }
    }

    private void initHealerAndTargetParticipants() {
        healerParticipants = new ArrayList<FightParticipant>();
        targetParticipants = new ArrayList<FightParticipant>();
        for (int k=0; k<fight.getParticipants().size(); k++) {
            FightParticipant part = fight.getParticipants().get(k);
            if (!part.isPlayer() && !part.isPet()) {
                continue;
            }
            AmountAndCount totHealing = part.totalHealing(Constants.SCHOOL_ALL, EventCollection.TYPE_ALL_HEALING, EventCollection.UNIT_PLAYER);
            EventCollection filteredEvents = part.filter(new FilterGuidOr(null, part.getSourceGUID()));
            AmountAndCount selfHealing = filteredEvents.totalHealing(Constants.SCHOOL_ALL, EventCollection.TYPE_ALL_HEALING, EventCollection.UNIT_PLAYER);
            //Its a healer if self healing is less than 90% of the total healing.
            if ((double)selfHealing.amount < (double)totHealing.amount*0.9) {
                healerParticipants.add(part);
            }
            targetParticipants.add(part);
        }
    }

}


