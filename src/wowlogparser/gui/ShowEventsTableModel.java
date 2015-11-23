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

import wowlogparserbase.tablerendering.StringColor;
import wowlogparserbase.helpers.ListFunctions;
import java.awt.Color;
import wowlogparserbase.events.healing.HealingEvent;
import wowlogparserbase.events.damage.DamageEvent;
import wowlogparserbase.events.damage.DamageMissedEvent;
import wowlogparserbase.events.SkillInterface;
import wowlogparserbase.events.BasicEvent;
import wowlogparserbase.events.SkillInterfaceExtra;
import wowlogparserbase.events.PowerEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import wowlogparserbase.events.DamageSplitEvent;
import wowlogparserbase.events.aura.SpellAuraBaseEvent;

/**
 *
 * @author racy
 */
public class ShowEventsTableModel implements TableModel {

    private static final int TIME = 0;
    private static final int TYPE = 1;
    private static final int CLASS = 2;
    private static final int SOURCE = 3;
    private static final int DESTINATION = 4;
    private static final int SKILL = 5;
    private static final int EXTRASKILL = 6;
    private static final int AMOUNT = 7;
    private static final int ROW = 8;

    public String[] columnNames;
    public Object data[][] = new Object[0][0];
    public int numColumns = 0;
    public int numRows = 0;    
    List<TableModelListener> listeners = new ArrayList<TableModelListener>();
        
    public ShowEventsTableModel(List<BasicEvent> allEvents) {
        createTable(allEvents);
    }

    private void createTable(List<BasicEvent> allEvents) {

        numColumns = 9;
        columnNames = new String[numColumns];
        columnNames[TIME] = "Time";
        columnNames[TYPE] = "Log type";
        columnNames[CLASS] = "Parser class";
        columnNames[SOURCE] = "Source";
        columnNames[DESTINATION] = "Destination";
        columnNames[SKILL] = "Skill/Spell";
        columnNames[EXTRASKILL] = "Extra Skill/Spell";
        columnNames[AMOUNT] = "Amount";
        columnNames[ROW] = "Row in Logfile";
        
        ListFunctions.removeEqual(allEvents);
        
        data = new Object[allEvents.size()][numColumns];
        
        int k;
        Color lightBlueColor = new Color(0x33, 0x99, 0xFF);
        for (k=0; k<allEvents.size(); k++) {
            BasicEvent e = (BasicEvent)allEvents.get(k);
            Color forground = null;
            Color background = null;
            if (e instanceof DamageEvent) {
                background = Color.RED;
            }
            if (e instanceof HealingEvent) {
                background = Color.GREEN;
            }
            if (e instanceof PowerEvent) {
                background = Color.YELLOW;
            }
            if (e instanceof SpellAuraBaseEvent) {
                background = lightBlueColor;
            }
            data[k][TIME] = new StringColor(forground, background, e.getTimeStringExact());
            data[k][TYPE] = new StringColor(forground, background,e.getLogType());
            data[k][SOURCE] = new StringColor(forground, background,e.getSourceName());
            data[k][DESTINATION] = new StringColor(forground, background,e.getDestinationName());
            data[k][SKILL] = new StringColor(forground, background, "");
            data[k][EXTRASKILL] = new StringColor(forground, background, "");
            data[k][AMOUNT] = new StringColor(forground, background, "");
            data[k][ROW] = new StringColor(forground, background, ""+(e.getLogFileRow()+1));
            data[k][CLASS] = new StringColor(forground, background, e.getClass().getSimpleName());
            if (e instanceof SkillInterface) {
                SkillInterface e2 = (SkillInterface) e;
                data[k][SKILL] = new StringColor(forground, background, e2.getSkillName());
            }
            if (e instanceof SkillInterfaceExtra) {
                SkillInterfaceExtra e2 = (SkillInterfaceExtra) e;
                data[k][EXTRASKILL] = new StringColor(forground, background, e2.getExtraSpellName());
            }
            if (e instanceof DamageEvent) {
                if (e instanceof DamageMissedEvent) {
                    DamageMissedEvent e2 = (DamageMissedEvent) e;
                    data[k][AMOUNT] = new StringColor(forground, background, "" + e2.getMissType());
                } else {
                    DamageEvent e2 = (DamageEvent) e;
                    data[k][AMOUNT] = new StringColor(forground, background, "" + e2.getDamage());
                }
            }
            if (e instanceof HealingEvent) {
                HealingEvent e2 = (HealingEvent) e;
                data[k][AMOUNT] = new StringColor(forground, background, "" + e2.getHealing());
            }
            if (e instanceof PowerEvent) {
                PowerEvent e2 = (PowerEvent) e;
                data[k][AMOUNT] = new StringColor(forground, background, "" + e2.getAmount());
            }
            if (e instanceof DamageSplitEvent) {
                DamageSplitEvent e2 = (DamageSplitEvent) e;
                data[k][AMOUNT] = new StringColor(forground, background, "" + e2.getAmount());
            }
        }
    }

    void fireTableModelEvent(TableModelEvent e) {
        for(TableModelListener l : listeners) {
            l.tableChanged(e);
        }
    }
    
    @Override
    public void addTableModelListener(TableModelListener l) {
        listeners.add(l);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (data.length > 0) {
            return data[0][columnIndex].getClass();
        }
        return String.class;
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
        return data.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return data[rowIndex][columnIndex];
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

