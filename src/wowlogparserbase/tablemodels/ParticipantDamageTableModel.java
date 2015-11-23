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

import wowlogparserbase.info.EmptyInfo;
import wowlogparserbase.Fight;
import wowlogparserbase.FightParticipant;
import wowlogparserbase.SpellInfo;
import wowlogparserbase.AmountAndCount;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import wowlogparserbase.EventCollection;
import wowlogparserbase.WLPNumberFormat;
import wowlogparserbase.events.BasicEvent;
import wowlogparserbase.info.DamageInfo;
import wowlogparserbase.info.DamageInfoAll;
import wowlogparserbase.info.DamageInfoBase;
import wowlogparserbase.info.DamageInfoSpell;
import wowlogparserbase.info.DamageInfoSwings;
import wowlogparserbase.info.InfoBase;
import wowlogparserbase.info.StringInfo;

/**
 *
 * @author racy
 */
public class ParticipantDamageTableModel implements TableModel, RowInfoInterface {

    static final int TYPE = 0;
    static final int DAMAGE = 1;
    static final int DPS = 2;
    static final int ACTIVEDPS = 3;
    static final int AVERAGE = 4;
    static final int MAX = 5;
    static final int PERCENT = 6;
    static final int SCHOOL = 7;
    static final int ID = 8;
    static final int HITS = 9;
    static final int GLANCING = 10;
    static final int CRITS = 11;
    static final int DODGE = 12;
    static final int PARRY = 13;
    static final int MISS = 14;
    static final int TOTMISS = 15;
    static final int CRUSH = 16;

    public String[] columnNames;
    public int numColumns = 0;
    List<InfoBase> rows = new ArrayList<InfoBase>();
    List<TableModelListener> listeners = new ArrayList<TableModelListener>();
    EventCollection fight;
    FightParticipant participant;
    long totalDamage;
    
    public ParticipantDamageTableModel(EventCollection f, FightParticipant p) {
        fight = f;
        participant = p;
        createTable();
    }

    public void makeColumns() {
        //If NPC then add a crushing field
        if (participant.isNpc()) {
            numColumns = 17;
        } else {
            numColumns = 16;
        }
        columnNames = new String[numColumns];
        columnNames[TYPE] = "Dmg Type";
        columnNames[HITS] = "Hits";
        columnNames[CRITS] = "Crits";
        columnNames[DAMAGE] = "Dmg";
        columnNames[DPS] = "DPS";
        columnNames[ACTIVEDPS] = "Active DPS";
        columnNames[AVERAGE] = "Avg dmg";
        columnNames[MAX] = "Max dmg";
        columnNames[SCHOOL] = "School";
        columnNames[ID] = "ID";
        columnNames[PERCENT] = "% of tot";
        columnNames[TOTMISS] = "Tot missed";
        columnNames[DODGE] = "Dodge";
        columnNames[PARRY] = "Parry";
        columnNames[GLANCING] = "Glancing";
        columnNames[MISS] = "Miss";
        if (participant.isNpc()) {
            columnNames[CRUSH] = "Crushing";
        }
    }

    public InfoBase getRow(int row) {
        return rows.get(row);
    }

    public Object getColumnObject(int row, int col) {
        InfoBase info = rows.get(row);
        if (!info.hasInfo()) {
            return "";
        }

        if (info instanceof StringInfo && col == 0) {
            return info.toString();
        }

        if (!(info instanceof DamageInfo)) {
            return "";
        }

        NumberFormat nf = WLPNumberFormat.getInstance();
        nf.setMaximumFractionDigits(1);
        nf.setGroupingUsed(false);

        DamageInfo di = (DamageInfo)info;
        String ret = "";
        AmountAndCount a = di.getAmountAndCount();
        switch(col) {
            case TYPE:
                ret = "" + di.getSkillName();
                break;
            case DAMAGE:
                ret = "" + a.amount;
                break;
            case DPS:
                ret = nf.format(di.getDps());
                break;
            case ACTIVEDPS:
                ret = nf.format(di.getDpsActive());
                break;
            case AVERAGE:
                ret = nf.format((int)di.getDamageAverage());
                break;
            case MAX:
                ret = "" + a.maxAmount;
                break;
            case PERCENT:
                if (totalDamage > 0) {
                    ret = nf.format((double) di.getAmountAndCount().amount / (double) totalDamage * 100.0);
                } else {
                    ret = "0";
                }
                break;
            case SCHOOL:
                ret = BasicEvent.getSchoolStringFromFlags(di.getSchool());
                break;
            case ID:
                if (di.getId() != DamageInfo.ID_NOT_PRESENT) {
                    ret = "" + di.getId();
                } else {
                    ret = "";
                }
                break;
            case HITS:
                ret = ModelFactory.makeHitNr(a.hit, a.totHit + a.totMiss);
                break;
            case GLANCING:
                ret = ModelFactory.makeHitNr(a.glancing, a.totHit + a.totMiss);
                break;
            case CRITS:
                ret = ModelFactory.makeHitNr(a.crit, a.totHit + a.totMiss);
                break;
            case DODGE:
                ret = ModelFactory.makeHitNr(a.dodge, a.totHit + a.totMiss);
                break;
            case PARRY:
                ret = ModelFactory.makeHitNr(a.parry, a.totHit + a.totMiss);
                break;
            case MISS:
                ret = ModelFactory.makeHitNr(a.miss, a.totHit + a.totMiss);
                break;
            case TOTMISS:
                ret = ModelFactory.makeHitNr(a.totMiss, a.totHit + a.totMiss);
                break;
            case CRUSH:
                ret = ModelFactory.makeHitNr(a.crushing, a.totHit + a.totMiss);
                break;

        }
        return ret;
    }

    public final void createTable() {
        makeColumns();
        totalDamage = ModelFactory.makeDamageRowsSeparateDotAndDd(rows, fight, participant);
    }


    void fireTableModelEvent(TableModelEvent e) {
        for (TableModelListener l : listeners) {
            l.tableChanged(e);
        }
    }

    public void addTableModelListener(TableModelListener l) {
        listeners.add(l);
    }

    public Class<?> getColumnClass(int columnIndex) {
        return getColumnObject(0, columnIndex).getClass();
    }

    public int getColumnCount() {
        return numColumns;
    }

    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }

    public int getRowCount() {
        return rows.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        return getColumnObject(rowIndex, columnIndex);
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public void removeTableModelListener(TableModelListener l) {
        listeners.remove(l);
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

    }
}
