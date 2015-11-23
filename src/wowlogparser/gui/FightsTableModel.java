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
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 *
 * @author racy
 */
public class FightsTableModel implements TableModel {

    public final String[] columnNames = {"Name", "Time", "Duration"};
    public int numColumns = columnNames.length;
    public int numRows = 0;    
    List<FightsRow> rows = new ArrayList<FightsRow>();    
    List<TableModelListener> listeners = new ArrayList<TableModelListener>();
    
    public void clearFights() {
        rows = new ArrayList<FightsRow>();
        TableModelEvent ev = new TableModelEvent(this);
        fireTableModelEvent(ev);
    }
    
    public void addFight(String name, String time, String duration) {
        FightsRow fr = new FightsRow(name, time, duration);
        rows.add(fr);
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
        return String.class;
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
        FightsRow fr = rows.get(rowIndex);
        switch(columnIndex) {
            case 0:
                return fr.name;
            case 1:
                return fr.time;
            case 2:
                return fr.duration;
        }
        return "";
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public void removeTableModelListener(TableModelListener l) {
        listeners.remove(l);
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    }

    public class FightsRow {
        public String name;
        public String time;
        public String duration;
        
        public FightsRow(String name, String time, String duration) {
            this.name = name;
            this.time = time;
            this.duration = duration;
        }
    }

}

        