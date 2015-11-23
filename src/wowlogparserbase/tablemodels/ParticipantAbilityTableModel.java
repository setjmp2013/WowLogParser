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
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import wowlogparserbase.EventCollection;
import wowlogparserbase.WLPNumberFormat;
import wowlogparserbase.eventfilter.FilterAnd;
import wowlogparserbase.eventfilter.FilterClass;
import wowlogparserbase.eventfilter.FilterDestinationGuid;
import wowlogparserbase.eventfilter.FilterSourceGuid;
import wowlogparserbase.events.BasicEvent;
import wowlogparserbase.events.SkillInterface;
import wowlogparserbase.events.SpellResurrectEvent;
import wowlogparserbase.events.UnitDiedEvent;
import wowlogparserbase.events.aura.SpellAuraAppliedEvent;
import wowlogparserbase.events.aura.SpellAuraDispelledEvent;

/**
 *
 * @author racy
 */
public class ParticipantAbilityTableModel implements TableModel {

//    public static final int NAME = 0;
//    public static final int ID = 1;
//    public static final int SCHOOL = 2;
    enum Col {NAME ("Name"), ID ("Skill ID"), SCHOOL ("School"), COUNT ("Count");
        String desc;
        Col(String desc) {
            this.desc = desc;
        }

        public String getDescription() {
            return desc;
        }

    }


    public String[] columnNames;
    public int numColumns = 0;
    List<AbilityRow> rows = new ArrayList<AbilityRow>();

    List<TableModelListener> listeners = new ArrayList<TableModelListener>();
    FightParticipant participant;

    public ParticipantAbilityTableModel(EventCollection f, FightParticipant p) {
        participant = p;
        createTable();
    }

    private void makeColumns() {
        //If NPC then add a crushing field
        numColumns = Col.values().length;
        columnNames = new String[numColumns];
        for (Col c : Col.values()) {
            columnNames[c.ordinal()] = c.getDescription();
        }
    }

    public AbilityRow getRow(int row) {
        return rows.get(row);
    }

    public Object getColumnObject(int row, int col) {
        AbilityRow ar = rows.get(row);
        if (!ar.hasSpellInfo()) {
            String ret = "";
            Col eCol = Col.values()[col];
            switch (eCol) {
                case NAME:
                    ret = ar.getReplacementString();
                    break;
                case COUNT:
                    if (ar.getNum() > 0)
                    ret = "" + ar.getNum();
                    break;
            }
            return ret;
        }

        SpellInfo si = ar.getSpellInfo();
        NumberFormat nf = WLPNumberFormat.getInstance();
        nf.setMaximumFractionDigits(1);
        nf.setGroupingUsed(false);

        String ret = "";
        Col eCol = Col.values()[col];
        switch(eCol) {
            case NAME:
                ret = "" + si.name;
                break;
            case ID:
                ret = "" + si.spellID;
                break;
            case SCHOOL:
                ret = BasicEvent.getSchoolStringFromFlags(si.school);
                break;
            case COUNT:
                ret = "" + ar.getNum();
                break;
        }
        return ret;
    }

    private void createTable() {
        makeColumns();
        makeRows();
    }

    private void makeRows() {
        if (participant.getSourceGUID() == null) {
            return;
        }
        EventCollection skillsEcDest = participant.getEventsReceived().filter(new FilterAnd(new FilterClass(SkillInterface.class), new FilterDestinationGuid(participant.getSourceGUID())));
        EventCollection skillsEcSource = participant.getEventsPerformed().filter(new FilterAnd(new FilterClass(SkillInterface.class), new FilterSourceGuid(participant.getSourceGUID())));
        EventCollection unitDiedEvents = participant.getEventsReceived().filter(new FilterAnd(new FilterClass(UnitDiedEvent.class), new FilterDestinationGuid(participant.getSourceGUID())));

        List<SkillInterface> spellListBuffsGiven = getBuffs(skillsEcSource);
        List<SkillInterface> spellListBuffsGained = getBuffs(skillsEcDest);
        List<SkillInterface> spellListDebuffsGained = getDebuffs(skillsEcDest);
        List<SkillInterface> spellListDebuffsGiven = getDebuffs(skillsEcSource);
        List<SkillInterface> spellListResurrectsGiven = getResurrects(skillsEcSource);
        List<SkillInterface> spellListResurrectsGained = getResurrects(skillsEcDest);
        List<SkillInterface> friendlyDispellsMade = getFriendlyDispells(skillsEcSource);
        List<SkillInterface> hostileDispellsMade = getHostileDispells(skillsEcSource);

        Map<Integer, AbilityRow> buffsGivenMap = new HashMap<Integer, AbilityRow>();
        Map<Integer, AbilityRow> buffsGainedMap = new HashMap<Integer, AbilityRow>();
        Map<Integer, AbilityRow> debuffsGivenMap = new HashMap<Integer, AbilityRow>();
        Map<Integer, AbilityRow> debuffsGainedMap = new HashMap<Integer, AbilityRow>();
        Map<Integer, AbilityRow> resurrectsGivenMap = new HashMap<Integer, AbilityRow>();
        Map<Integer, AbilityRow> resurrectsGainedMap = new HashMap<Integer, AbilityRow>();
        Map<Integer, AbilityRow> friendlyDispellsMap = new HashMap<Integer, AbilityRow>();
        Map<Integer, AbilityRow> hostileDispellsMap = new HashMap<Integer, AbilityRow>();

        sortName(spellListBuffsGained);
        sortName(spellListBuffsGiven);
        sortName(spellListDebuffsGained);
        sortName(spellListDebuffsGiven);
        sortName(spellListResurrectsGained);
        sortName(spellListResurrectsGiven);
        sortName(friendlyDispellsMade);
        sortName(hostileDispellsMade);

        if (spellListBuffsGained.size() > 0) {
            addRow(new AbilityRow(""));
            addRow(new AbilityRow("Buffs Gained"));
            for (SkillInterface skI : spellListBuffsGained) {
                addRow(buffsGainedMap, skI);
            }
        }

        if (spellListBuffsGiven.size() > 0) {
            addRow(new AbilityRow(""));
            addRow(new AbilityRow("Buffs Given"));
            for (SkillInterface skI : spellListBuffsGiven) {
                addRow(buffsGivenMap, skI);
            }
        }

        if (spellListDebuffsGained.size() > 0) {
            addRow(new AbilityRow(""));
            addRow(new AbilityRow("Debuffs Gained"));
            for (SkillInterface skI : spellListDebuffsGained) {
                addRow(debuffsGainedMap, skI);
            }
        }

        if (spellListDebuffsGiven.size() > 0) {
            addRow(new AbilityRow(""));
            addRow(new AbilityRow("Debuffs Given"));
            for (SkillInterface skI : spellListDebuffsGiven) {
                addRow(debuffsGivenMap, skI);
            }
        }

        if (spellListResurrectsGained.size() > 0) {
            addRow(new AbilityRow(""));
            addRow(new AbilityRow("Resurrects Gained"));
            for (SkillInterface skI : spellListResurrectsGained) {
                addRow(resurrectsGainedMap, skI);
            }
        }

        if (spellListResurrectsGiven.size() > 0) {
            addRow(new AbilityRow(""));
            addRow(new AbilityRow("Resurrects Given"));
            for (SkillInterface skI : spellListResurrectsGiven) {
                addRow(resurrectsGivenMap, skI);
            }
        }

        if (friendlyDispellsMade.size() > 0) {
            addRow(new AbilityRow(""));
            addRow(new AbilityRow("Friendly Dispels"));
            for (SkillInterface skI : friendlyDispellsMade) {
                addRow(friendlyDispellsMap, skI);
            }
        }

        if (hostileDispellsMade.size() > 0) {
            addRow(new AbilityRow(""));
            addRow(new AbilityRow("Hostile Dispels"));
            for (SkillInterface skI : hostileDispellsMade) {
                addRow(hostileDispellsMap, skI);
            }
        }

        if (unitDiedEvents.size() > 0) {
            addRow(new AbilityRow(""));
            AbilityRow deathsRow = new AbilityRow("Deaths", unitDiedEvents.size());
            deathsRow.addEvents(unitDiedEvents.getEvents());
            addRow(deathsRow);
        }
    }

    private void sortName(List<SkillInterface> list) {
        Collections.sort(list, new Comparator<SkillInterface>() {
            @Override
            public int compare(SkillInterface o1, SkillInterface o2) {
                return o1.getSkillName().compareTo(o2.getSkillName());
            }
        });

    }

    private List<SkillInterface> getBuffs(EventCollection ec) {
        List<SkillInterface> list = new ArrayList<SkillInterface>();
        for (BasicEvent e : ec.getEvents()) {
            if (e instanceof SpellAuraAppliedEvent) {
                SpellAuraAppliedEvent saa = (SpellAuraAppliedEvent) e;
                if (saa.isBuff()) {
                    list.add(saa);
                }
            }
        }
        return list;
    }

    private List<SkillInterface> getDebuffs(EventCollection ec) {
        List<SkillInterface> list = new ArrayList<SkillInterface>();
        for (BasicEvent e : ec.getEvents()) {
            if (e instanceof SpellAuraAppliedEvent) {
                SpellAuraAppliedEvent saa = (SpellAuraAppliedEvent) e;
                if (saa.isDebuff()) {
                    list.add(saa);
                }
            }
        }
        return list;
    }

    private List<SkillInterface> getFriendlyDispells(EventCollection ec) {
        List<SkillInterface> list = new ArrayList<SkillInterface>();
        for (BasicEvent e : ec.getEvents()) {
            if (e instanceof SpellAuraDispelledEvent) {
                SpellAuraDispelledEvent sad = (SpellAuraDispelledEvent) e;
                if (BasicEvent.isReactionFriendly(sad.getDestinationFlags())) {
                    list.add(sad);
                }
            }
        }
        return list;
    }

    private List<SkillInterface> getHostileDispells(EventCollection ec) {
        List<SkillInterface> list = new ArrayList<SkillInterface>();
        for (BasicEvent e : ec.getEvents()) {
            if (e instanceof SpellAuraDispelledEvent) {
                SpellAuraDispelledEvent sad = (SpellAuraDispelledEvent) e;
                if (BasicEvent.isReactionHostile(sad.getDestinationFlags())) {
                    list.add(sad);
                }
            }
        }
        return list;
    }

    private List<SkillInterface> getResurrects(EventCollection ec) {
        List<SkillInterface> list = new ArrayList<SkillInterface>();
        for (BasicEvent e : ec.getEvents()) {
            if (e instanceof SpellResurrectEvent) {
                SpellResurrectEvent sr = (SpellResurrectEvent) e;
                list.add(sr);
            }
        }
        return list;
    }

    private void addRow(AbilityRow ar) {
        rows.add(ar);
    }

    private void addRow(Map<Integer, AbilityRow> abilityMap, SkillInterface skI) {
        AbilityRow ar = new AbilityRow(new SpellInfo(skI.getSkillID(), skI.getSkillName(), skI.getSchool()));
        int key = ar.getSpellInfo().spellID;
        AbilityRow mapAr = abilityMap.get(key);
        if (mapAr == null) {
            ar.setNum(ar.getNum() + 1);
            abilityMap.put(key, ar);
            rows.add(ar);
            ar.addEvent((BasicEvent)skI);
        } else {
            mapAr.setNum(mapAr.getNum() + 1);
            mapAr.addEvent((BasicEvent)skI);
        }
    }


    void fireTableModelEvent(TableModelEvent e) {
        for (TableModelListener l : listeners) {
            l.tableChanged(e);
        }
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
        listeners.add(l);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return getColumnObject(0, columnIndex).getClass();
    }

    @Override
    public int getColumnCount() {
        return numColumns;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return getColumnObject(rowIndex, columnIndex);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
        listeners.remove(l);
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

    }

}
