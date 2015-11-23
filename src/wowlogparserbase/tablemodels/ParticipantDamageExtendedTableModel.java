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
import wowlogparserbase.info.InfoBase;
import wowlogparserbase.info.StringInfo;

/**
 *
 * @author racy
 */
public class ParticipantDamageExtendedTableModel implements TableModel, RowInfoInterface {

    public enum ColEnum {
        TYPE("Type"),
        DAMAGE("Dmg"),
        DAMAGE_HIT("DmgHit"),
        DAMAGE_CRIT("DmgCrit"),
        DAMAGE_GLANCING("DmgGlanc"),
        DAMAGE_CRUSHING("DmgCrush"),
        DAMAGE_OVER("DmgOver"),
        DAMAGE_ABSORBED("DmgAbs"),
        DAMAGE_RESISTED("DmgRes"),
        DAMAGE_BLOCKED("DmgBlo"),
        DPS("Dps"),
        ACTIVEDPS("AcDps"),
        AVERAGE("AvDmg"),
        MAX("MaxDmg"),
        HIT_AVERAGE("AvHit"),
        CRIT_AVERAGE("AvCrit"),
        PERCENT("%tot"),
        SCHOOL("Sch"),
        ID("ID"),
        HITS("Hits"),
        GLANCING("Glanc"),
        CRITS("Crits"),
        DODGE("Dodge"),
        PARRY("Parry"),
        BLOCK("Block"),
        RESIST("Resist"),
        REFLECT("Reflect"),
        MISS("Miss"),
        TOTMISS("TotMiss"),
        CRUSH("Crush");

        private String name;
        ColEnum(String name) {
            this.name = name;
        }
        
        public String columnString() {
            return this.name;
        }
    }
    public int numColumns;
    public String[] columnNames;
    public List<SpellInfo> damageSpellIDsAll;
    public List<SpellInfo> rangedSpellIDsAll;
    public List<SpellInfo> damageSpellIDsPlayer;
    public List<SpellInfo> rangedSpellIDsPlayer;
    public List<SpellInfo> damageSpellIDsPet;
    public List<SpellInfo> rangedSpellIDsPet;
    List<InfoBase> rows = new ArrayList<InfoBase>();
    List<TableModelListener> listeners = new ArrayList<TableModelListener>();
    EventCollection fight;
    FightParticipant participant;
    long totalDamage;

    public ParticipantDamageExtendedTableModel(EventCollection f, FightParticipant p) {
        fight = f;
        participant = p;
        createTable();
    }

    public void makeColumns() {
        numColumns = ColEnum.values().length;
        columnNames = new String[numColumns];
        for (ColEnum colEn : ColEnum.values()) {
            columnNames[colEn.ordinal()] = colEn.columnString();
        }
    }

    public InfoBase getRow(int row) {
        return rows.get(row);
    }

    public Object getColumnObject(int row, int col) {
        ColEnum colEnum = ColEnum.values()[col];
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
        switch(colEnum) {
            case TYPE:
                ret = "" + di.getSkillName();
                break;
            case DAMAGE:
                ret = "" + a.amount;
                break;
            case DAMAGE_CRIT:
                ret = "" + a.amountCrit;
                break;
            case DAMAGE_HIT:
                ret = "" + a.amountHit;
                break;
            case DAMAGE_CRUSHING:
                ret = "" + a.amountCrushing;
                break;
            case DAMAGE_GLANCING:
                ret = "" + a.amountGlancing;
                break;
            case DAMAGE_OVER:
                ret = "" + a.overAmount;
                break;
            case DAMAGE_ABSORBED:
                ret = "" + a.amountAbsorbed;
                break;
            case DAMAGE_RESISTED:
                ret = "" + a.amountResisted;
                break;
            case DAMAGE_BLOCKED:
                ret = "" + a.amountBlocked;
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
            case HIT_AVERAGE:
                if (a.hit == 0) {
                    ret = "";
                } else {
                    ret = nf.format(Math.round((double)a.amountHit / (double)a.hit));
                }
                break;
            case CRIT_AVERAGE:
                if (a.crit == 0) {
                    ret = "";
                } else {
                    ret = nf.format(Math.round((double)a.amountCrit / (double)a.crit));
                }
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
            case BLOCK:
                ret = ModelFactory.makeHitNr(a.block, a.totHit + a.totMiss);
                break;
            case RESIST:
                ret = ModelFactory.makeHitNr(a.resist, a.totHit + a.totMiss);
                break;
            case REFLECT:
                ret = ModelFactory.makeHitNr(a.reflect, a.totHit + a.totMiss);
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
