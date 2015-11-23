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

import wowlogparserbase.events.BasicEvent;
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
import wowlogparserbase.Constants;
import wowlogparserbase.info.EmptyInfo;
import wowlogparserbase.info.HealingInfo;
import wowlogparserbase.info.HealingInfoAll;
import wowlogparserbase.info.HealingInfoBase;
import wowlogparserbase.info.HealingInfoSpell;
import wowlogparserbase.info.InfoBase;
import wowlogparserbase.info.StringInfo;

/**
 *
 * @author racy
 */
public class ParticipantHealingTableModel implements TableModel, RowInfoInterface {

    static final int TYPE = 0;
    static final int RAWHEALING = 1;
    static final int HEALING = 2;
    static final int HPS = 3;
    static final int ACTIVEHPS = 4;
    static final int AVERAGE = 5;
    static final int MAX = 6;
    static final int PERCENT = 7;
    static final int SCHOOL = 8;
    static final int ID = 9;
    static final int HITS = 10;
    static final int CRITS = 11;
    static final int OVERHEALING = 12;

    public String[] columnNames;
    public int numColumns = 0;
    List<InfoBase> rows = new ArrayList<InfoBase>();
    public List<SpellInfo> healingSpellIDsAll;
    public List<SpellInfo> directHealingSpellIDsAll;
    public List<SpellInfo> periodicHealingSpellIDsAll;
    public List<SpellInfo> healingSpellIDsPet;
    public List<SpellInfo> directHealingSpellIDsPet;
    public List<SpellInfo> periodicHealingSpellIDsPet;
    public List<SpellInfo> healingSpellIDsPlayer;
    public List<SpellInfo> directHealingSpellIDsPlayer;
    public List<SpellInfo> periodicHealingSpellIDsPlayer;

    List<TableModelListener> listeners = new ArrayList<TableModelListener>();
    EventCollection fight;
    FightParticipant participant;
    long totalHealing;

    public ParticipantHealingTableModel(EventCollection f, FightParticipant p) {
        fight = f;
        participant = p;
        createTable();
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
        if (!(info instanceof HealingInfo)) {
            return "";
        }

        NumberFormat nf = WLPNumberFormat.getInstance();
        nf.setMaximumFractionDigits(1);
        nf.setGroupingUsed(false);

        HealingInfo hi = (HealingInfo)info;
        String ret = "";
        AmountAndCount a = hi.getAmountAndCount();
        switch(col) {
            case TYPE:
                ret = "" + hi.getSkillName();
                break;
            case RAWHEALING:
                ret = "" + a.amount;
                break;
            case HEALING:
                ret = "" + (a.amount - a.overAmount);
                break;
            case HPS:
                ret = nf.format(hi.getHps());
                break;
            case ACTIVEHPS:
                ret = nf.format(hi.getHpsActive());
                break;
            case AVERAGE:
                ret = nf.format((int)hi.getHealingAverage());
                break;
            case MAX:
                ret = "" + a.maxAmount;
                break;
            case PERCENT:
                if (totalHealing > 0) {
                    ret = nf.format((double) hi.getAmountAndCount().amount / (double) totalHealing * 100.0);
                } else {
                    ret = "0";
                }
                break;
            case SCHOOL:
                ret = BasicEvent.getSchoolStringFromFlags(hi.getSchool());
                break;
            case ID:
                if (hi.getId() != HealingInfo.ID_NOT_PRESENT) {
                    ret = "" + hi.getId();
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
            case OVERHEALING:
                ret = ModelFactory.makeHitNr(a.overAmount, a.amount);
                break;

        }
        return ret;
    }

    public void makeColumns() {
        numColumns = 13;
        columnNames =  new String[numColumns];
        columnNames[TYPE] = "Healing Type";
        columnNames[RAWHEALING] = "Raw Heal";
        columnNames[HEALING] = "Healing";
        columnNames[HPS] = "HPS";
        columnNames[ACTIVEHPS] = "Active HPS";
        columnNames[SCHOOL] = "School";
        columnNames[ID] = "ID";
        columnNames[PERCENT] = "% of tot";
        columnNames[AVERAGE] = "Avg heal";
        columnNames[MAX] = "Max heal";
        columnNames[HITS] = "Hits";
        columnNames[CRITS] = "Crits";
        columnNames[OVERHEALING] = "Overheal";
    }

    public final void createTable() {
        makeColumns();

        healingSpellIDsAll = participant.getIDs(FightParticipant.TYPE_SPELL_HEALING);
        directHealingSpellIDsAll = participant.getIDs(FightParticipant.TYPE_SPELL_HEALING_DIRECT);
        periodicHealingSpellIDsAll = participant.getIDs(FightParticipant.TYPE_SPELL_HEALING_PERIODIC);
        healingSpellIDsPet = participant.getIDs(FightParticipant.TYPE_SPELL_HEALING, FightParticipant.UNIT_PET);
        directHealingSpellIDsPet = participant.getIDs(FightParticipant.TYPE_SPELL_HEALING_DIRECT, FightParticipant.UNIT_PET);
        periodicHealingSpellIDsPet = participant.getIDs(FightParticipant.TYPE_SPELL_HEALING_PERIODIC, FightParticipant.UNIT_PET);
        healingSpellIDsPlayer = participant.getIDs(FightParticipant.TYPE_SPELL_HEALING, FightParticipant.UNIT_PLAYER);
        directHealingSpellIDsPlayer = participant.getIDs(FightParticipant.TYPE_SPELL_HEALING_DIRECT, FightParticipant.UNIT_PLAYER);
        periodicHealingSpellIDsPlayer = participant.getIDs(FightParticipant.TYPE_SPELL_HEALING_PERIODIC, FightParticipant.UNIT_PLAYER);

        NumberFormat nf = WLPNumberFormat.getInstance();
        nf.setMaximumFractionDigits(1);
        nf.setGroupingUsed(false);

        HealingInfoBase hi;

        // All healing
        hi = new HealingInfoAll(fight, participant);
        totalHealing = hi.getAmountAndCount().amount;
        hi.setSkillName("All Healing");
        rows.add(hi);

        rows.add(new EmptyInfo());

        //Healing spells
        hi = new HealingInfoAll(fight, participant, FightParticipant.UNIT_ALL, Constants.SCHOOL_ALL, FightParticipant.TYPE_SPELL_HEALING);
        hi.setSkillName("Healing Spells(All)");
        hi.setNoCounts();
        rows.add(hi);
        if (participant.hasPets()) {
            for (SpellInfo s : healingSpellIDsPlayer) {
                hi = new HealingInfoSpell(fight, participant, s, FightParticipant.UNIT_PLAYER, FightParticipant.TYPE_SPELL_HEALING);
                rows.add(hi);
            }
            for (SpellInfo s : healingSpellIDsPet) {
                hi = new HealingInfoSpell(fight, participant, s, FightParticipant.UNIT_PET, FightParticipant.TYPE_SPELL_HEALING);
                hi.setSkillName("Pet: " + s.name);
                rows.add(hi);
            }
        } else {
            for (SpellInfo s : healingSpellIDsAll) {
                hi = new HealingInfoSpell(fight, participant, s, FightParticipant.UNIT_ALL, FightParticipant.TYPE_SPELL_HEALING);
                rows.add(hi);
            }
        }
        rows.add(new EmptyInfo());
        
        // Direct healing spells
        hi = new HealingInfoAll(fight, participant, FightParticipant.UNIT_ALL, Constants.SCHOOL_ALL, FightParticipant.TYPE_SPELL_HEALING_DIRECT);
        hi.setSkillName("Direct Healing Spells(All)");
        hi.setNoCounts();
        rows.add(hi);
        if (participant.hasPets()) {
            for (SpellInfo s : directHealingSpellIDsPlayer) {
                hi = new HealingInfoSpell(fight, participant, s, FightParticipant.UNIT_PLAYER, FightParticipant.TYPE_SPELL_HEALING_DIRECT);
                rows.add(hi);
            }
            for (SpellInfo s : directHealingSpellIDsPet) {
                hi = new HealingInfoSpell(fight, participant, s, FightParticipant.UNIT_PET, FightParticipant.TYPE_SPELL_HEALING_DIRECT);
                hi.setSkillName("Pet: " + s.name);
                rows.add(hi);
            }
        } else {
            for (SpellInfo s : directHealingSpellIDsAll) {
                hi = new HealingInfoSpell(fight, participant, s, FightParticipant.UNIT_ALL, FightParticipant.TYPE_SPELL_HEALING_DIRECT);
                rows.add(hi);
            }
        }
        rows.add(new EmptyInfo());

        // Periodic healing spells
        hi = new HealingInfoAll(fight, participant, FightParticipant.UNIT_ALL, Constants.SCHOOL_ALL, FightParticipant.TYPE_SPELL_HEALING_PERIODIC);
        hi.setSkillName("HOT Healing Spells(All)");
        hi.setNoCounts();
        rows.add(hi);
        if (participant.hasPets()) {
            for (SpellInfo s : periodicHealingSpellIDsPlayer) {
                hi = new HealingInfoSpell(fight, participant, s, FightParticipant.UNIT_PLAYER, FightParticipant.TYPE_SPELL_HEALING_PERIODIC);
                rows.add(hi);
            }
            for (SpellInfo s : periodicHealingSpellIDsPet) {
                hi = new HealingInfoSpell(fight, participant, s, FightParticipant.UNIT_PET, FightParticipant.TYPE_SPELL_HEALING_PERIODIC);
                hi.setSkillName("Pet: " + s.name);
                rows.add(hi);
            }
        } else {
            for (SpellInfo s : periodicHealingSpellIDsAll) {
                hi = new HealingInfoSpell(fight, participant, s, FightParticipant.UNIT_ALL, FightParticipant.TYPE_SPELL_HEALING_PERIODIC);
                rows.add(hi);
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
                hi = new HealingInfoAll(fight, participant, FightParticipant.UNIT_PET, Constants.SCHOOL_ALL, FightParticipant.TYPE_ALL_HEALING, name);
                hi.setSkillName(name);
                rows.add(hi);
            }
        }
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
