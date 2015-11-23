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

import wowlogparserbase.FightParticipant;
import wowlogparserbase.SpellInfo;
import wowlogparserbase.AmountAndCount;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import wowlogparserbase.EventCollection;
import wowlogparserbase.WLPNumberFormat;
import wowlogparserbase.events.BasicEvent;
import wowlogparserbase.Constants;
import wowlogparserbase.info.EmptyInfo;
import wowlogparserbase.info.InfoBase;
import wowlogparserbase.info.PowerInfo;
import wowlogparserbase.info.PowerInfoAll;
import wowlogparserbase.info.PowerInfoBase;
import wowlogparserbase.info.PowerInfoSpell;
import wowlogparserbase.info.StringInfo;

/**
 *
 * @author racy
 */
public class ParticipantPowerTableModel implements TableModel, RowInfoInterface {

    static final int TYPE = 0;
    static final int AMOUNT = 1;
    static final int PPS = 2;
    static final int ACTIVEPPS = 3;
    static final int AVERAGE = 4;
    static final int MAX = 5;
    static final int PERCENT = 6;
    static final int POWERTYPE = 7;
    static final int SCHOOL = 8;
    static final int ID = 9;
    static final int HITS = 10;
    static final int CRITS = 11;
    static final int MISS = 12;
    static final int TOTMISS = 13;

    public String[] columnNames;
    public int numColumns = 0;
    List<InfoBase> rows = new ArrayList<InfoBase>();
    public List<SpellInfo> drainSpellIDsAll;
    public List<SpellInfo> leechSpellIDsAll;
    public List<SpellInfo> energizeSpellIDsAll;
    public List<SpellInfo> drainSpellIDsPet;
    public List<SpellInfo> leechSpellIDsPet;
    public List<SpellInfo> energizeSpellIDsPet;
    public List<SpellInfo> drainSpellIDsPlayer;
    public List<SpellInfo> leechSpellIDsPlayer;
    public List<SpellInfo> energizeSpellIDsPlayer;
    List<TableModelListener> listeners = new ArrayList<TableModelListener>();
    EventCollection fight;
    FightParticipant participant;
    long totalAmountDrain;
    long totalAmountLeech;
    long totalAmountEnergize;

    public ParticipantPowerTableModel(EventCollection f, FightParticipant p) {
        fight = f;
        participant = p;
        createTable();
    }

    public void makeColumns() {
        //If NPC then add a crushing field
        numColumns  = 14;
        columnNames = new String[numColumns];
        columnNames[TYPE] = "Power Type";
        columnNames[HITS] = "Hits";
        columnNames[CRITS] = "Crits";
        columnNames[AMOUNT] = "Amount";
        columnNames[PPS] = "PPS";
        columnNames[ACTIVEPPS] = "Active PPS";
        columnNames[AVERAGE] = "Avg amount";
        columnNames[MAX] = "Max amount";
        columnNames[POWERTYPE] = "Power type";
        columnNames[SCHOOL] = "School";
        columnNames[ID] = "ID";
        columnNames[PERCENT] = "% of tot";
        columnNames[TOTMISS] = "Tot missed";
        columnNames[MISS] = "Miss";
}

    @Override
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
        if (!(info instanceof PowerInfo)) {
            return "";
        }

        NumberFormat nf = WLPNumberFormat.getInstance();
        nf.setMaximumFractionDigits(1);
        nf.setGroupingUsed(false);

        PowerInfo pi = (PowerInfo)info;
        String ret = "";
        AmountAndCount a = pi.getAmountAndCount();
        switch(col) {
            case TYPE:
                ret = "" + pi.getSkillName();
                break;
            case AMOUNT:
                ret = "" + a.amount;
                break;
            case PPS:
                ret = nf.format(pi.getPps());
                break;
            case ACTIVEPPS:
                ret = nf.format(pi.getPpsActive());
                break;
            case AVERAGE:
                ret = nf.format((int)pi.getPowerAverage());
                break;
            case MAX:
                ret = "" + a.maxAmount;
                break;
            case PERCENT:
                switch(pi.getPowerKind()) {
                    case PowerInfo.POWER_KIND_DRAIN:
                        ret = "0";
                        if (totalAmountDrain > 0) {
                            ret = nf.format((double) pi.getAmountAndCount().amount / (double) totalAmountDrain * 100.0);
                        }
                        break;
                    case PowerInfo.POWER_KIND_ENERGIZE:
                        ret = "0";
                        if (totalAmountEnergize > 0) {
                            ret = nf.format((double) pi.getAmountAndCount().amount / (double) totalAmountEnergize * 100.0);
                        }
                        break;
                    case PowerInfo.POWER_KIND_LEECH:
                        ret = "0";
                        if (totalAmountLeech > 0) {
                            ret = nf.format((double) pi.getAmountAndCount().amount / (double) totalAmountLeech * 100.0);
                        }
                        break;
                    default:
                        ret = "";
                        break;
                }
                break;
            case POWERTYPE:
                ret = BasicEvent.getPowerTypeString(pi.getPowerType());
                break;
            case SCHOOL:
                ret = BasicEvent.getSchoolStringFromFlags(pi.getSchool());
                break;
            case ID:
                if (pi.getId() != PowerInfo.ID_NOT_PRESENT) {
                    ret = "" + pi.getId();
                } else {
                    ret = "";
                }
                break;
            case HITS:
                ret = ModelFactory.makeHitNr(a.hit, a.totHit + a.totMiss);
                break;
            case CRITS:
                ret = ModelFactory.makeHitNr(a.crit, a.totHit + a.totMiss);
                break;
            case MISS:
                ret = ModelFactory.makeHitNr(a.miss, a.totHit + a.totMiss);
                break;
            case TOTMISS:
                ret = ModelFactory.makeHitNr(a.totMiss, a.totHit + a.totMiss);
                break;

        }
        return ret;
    }

    public final void createTable() {
        makeColumns();

        drainSpellIDsAll = participant.getIDs(FightParticipant.TYPE_SPELL_DRAIN, FightParticipant.UNIT_ALL);
        leechSpellIDsAll = participant.getIDs(FightParticipant.TYPE_SPELL_LEECH, FightParticipant.UNIT_ALL);
        energizeSpellIDsAll = participant.getIDs(FightParticipant.TYPE_SPELL_ENERGIZE, FightParticipant.UNIT_ALL);
        drainSpellIDsPet = participant.getIDs(FightParticipant.TYPE_SPELL_DRAIN, FightParticipant.UNIT_PET);
        leechSpellIDsPet = participant.getIDs(FightParticipant.TYPE_SPELL_LEECH, FightParticipant.UNIT_PET);
        energizeSpellIDsPet = participant.getIDs(FightParticipant.TYPE_SPELL_ENERGIZE, FightParticipant.UNIT_PET);
        drainSpellIDsPlayer = participant.getIDs(FightParticipant.TYPE_SPELL_DRAIN, FightParticipant.UNIT_PLAYER);
        leechSpellIDsPlayer = participant.getIDs(FightParticipant.TYPE_SPELL_LEECH, FightParticipant.UNIT_PLAYER);
        energizeSpellIDsPlayer = participant.getIDs(FightParticipant.TYPE_SPELL_ENERGIZE, FightParticipant.UNIT_PLAYER);

        PowerInfoBase pi;

        // All drain
        pi = new PowerInfoAll(fight, participant, FightParticipant.UNIT_ALL, Constants.SCHOOL_ALL, FightParticipant.TYPE_SPELL_DRAIN);
        totalAmountDrain = pi.getAmountAndCount().amount;
        pi.setSkillName("Drain(All)");
        pi.setNoCounts();
        rows.add(pi);
        if (participant.hasPets()) {
            for (SpellInfo s : drainSpellIDsPlayer) {
                pi = new PowerInfoSpell(fight, participant, s, FightParticipant.UNIT_PLAYER, FightParticipant.TYPE_SPELL_DRAIN);
                rows.add(pi);
            }
            for (SpellInfo s : drainSpellIDsPet) {
                pi = new PowerInfoSpell(fight, participant, s, FightParticipant.UNIT_PET, FightParticipant.TYPE_SPELL_DRAIN);
                pi.setSkillName("Pet: " + s.name);
                rows.add(pi);
            }
        } else {
            for (SpellInfo s : drainSpellIDsAll) {
                pi = new PowerInfoSpell(fight, participant, s, FightParticipant.UNIT_ALL, FightParticipant.TYPE_SPELL_DRAIN);
                rows.add(pi);
            }
        }
        rows.add(new EmptyInfo());

        // All leech
        pi  = new PowerInfoAll(fight, participant, FightParticipant.UNIT_ALL, Constants.SCHOOL_ALL, FightParticipant.TYPE_SPELL_LEECH);
        totalAmountLeech = pi.getAmountAndCount().amount;
        pi.setSkillName("Leech(All)");
        pi.setNoCounts();
        rows.add(pi);
        if (participant.hasPets()) {
            for (SpellInfo s : leechSpellIDsPlayer) {
                pi = new PowerInfoSpell(fight, participant, s, FightParticipant.UNIT_PLAYER, FightParticipant.TYPE_SPELL_LEECH);
                rows.add(pi);
            }
            for (SpellInfo s : leechSpellIDsPet) {
                pi = new PowerInfoSpell(fight, participant, s, FightParticipant.UNIT_PET, FightParticipant.TYPE_SPELL_LEECH);
                pi.setSkillName("Pet: " + s.name);
                rows.add(pi);
            }
        } else {
            for (SpellInfo s : leechSpellIDsAll) {
                pi = new PowerInfoSpell(fight, participant, s, FightParticipant.UNIT_ALL, FightParticipant.TYPE_SPELL_LEECH);
                rows.add(pi);
            }
        }
        rows.add(new EmptyInfo());

        // All energize
        pi  = new PowerInfoAll(fight, participant, FightParticipant.UNIT_ALL, Constants.SCHOOL_ALL, FightParticipant.TYPE_SPELL_ENERGIZE);
        totalAmountEnergize = pi.getAmountAndCount().amount;
        pi.setSkillName("Energize(All)");
        pi.setNoCounts();
        rows.add(pi);
        if (participant.hasPets()) {
            for (SpellInfo s : energizeSpellIDsPlayer) {
                pi = new PowerInfoSpell(fight, participant, s, FightParticipant.UNIT_PLAYER, FightParticipant.TYPE_SPELL_ENERGIZE);
                rows.add(pi);
            }
            for (SpellInfo s : energizeSpellIDsPet) {
                pi = new PowerInfoSpell(fight, participant, s, FightParticipant.UNIT_PET, FightParticipant.TYPE_SPELL_ENERGIZE);
                pi.setSkillName("Pet: " + s.name);
                rows.add(pi);
            }
        } else {
            for (SpellInfo s : energizeSpellIDsAll) {
                pi = new PowerInfoSpell(fight, participant, s, FightParticipant.UNIT_ALL, FightParticipant.TYPE_SPELL_ENERGIZE);
                rows.add(pi);
            }
        }
        if (participant.hasPets()) {
            rows.add(new EmptyInfo());

            // Pet name breakdown
            rows.add(new StringInfo("Pet Name Breakdown"));
            Set<String> petNames = new HashSet<String>();
            for (FightParticipant p : participant.getPetNames()) {
                petNames.add(p.getName());
            }
            Iterator<String> it = petNames.iterator();
            while (it.hasNext()) {
                String name = it.next();
                pi = new PowerInfoAll(fight, participant, FightParticipant.UNIT_PET, Constants.SCHOOL_ALL, FightParticipant.TYPE_ALL_POWER, name);
                pi.setSkillName(name);
                rows.add(pi);
            }
        }
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
