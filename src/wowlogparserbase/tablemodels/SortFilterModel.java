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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import wowlogparserbase.tablerendering.StringPercentColor;

/**
 *
 * @author racy
 */
public class SortFilterModel extends AbstractTableModel implements TableModelListener {
    private TableModel model;
    private int sortColumn;
    private Row[] rows;
    ArrayList<TableModelListener> listeners = new ArrayList<TableModelListener>();

    public SortFilterModel(TableModel m) {
        model = m;
        rows = new Row[model.getRowCount()];
        for (int i = 0; i < rows.length; i++) {
            rows[i] = new Row();
            rows[i].index = i;
        }
        m.addTableModelListener(this);
    }

    public TableModel getModel() {
        return model;
    }

    public int convertRowIndexToModel(int row) {
        if (row >= 0) {
            return rows[row].index;
        } else {
            return row;
        }
    }
    
    public void fireTableDataChanged() {
        TableModelEvent ev = new TableModelEvent(this);
        for(TableModelListener l : listeners) {
            l.tableChanged(ev);
        }
    }

    public void addTableModelListener(TableModelListener l) {
        listeners.add(l);
    }

    public void removeTableModelListener(TableModelListener l) {
        listeners.remove(l);
    }

    public void tableChanged(TableModelEvent e) {
        rows = new Row[model.getRowCount()];
        for (int i = 0; i < rows.length; i++) {
            rows[i] = new Row();
            rows[i].index = i;
        }
        fireTableDataChanged();
    }

    public void sort(int c) {
        sortColumn = c;
        Arrays.sort(rows);
        fireTableDataChanged();
    }

    public void addMouseListener(final JTable table) {
        table.getTableHeader().addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent event) { // check for double
                // click
//                if (event.getClickCount() < 2) {
//                    return;
//                }

                // find column of click and
                int tableColumn = table.columnAtPoint(event.getPoint());

                // translate to table model index and sort
                int modelColumn = table.convertColumnIndexToModel(tableColumn);
                sort(modelColumn);
            }
            });
    }
    
    /*
     * compute the moved row for the three methods that access model elements
     */
    public Object getValueAt(int r, int c) {
        return model.getValueAt(rows[r].index, c);
    }

    public boolean isCellEditable(int r, int c) {
        return model.isCellEditable(rows[r].index, c);
    }

    public void setValueAt(Object aValue, int r, int c) {
        model.setValueAt(aValue, rows[r].index, c);
    }

    /*
     * delegate all remaining methods to the model
     */
    public int getRowCount() {
        return model.getRowCount();
    }

    public int getColumnCount() {
        return model.getColumnCount();
    }

    public String getColumnName(int c) {
        return model.getColumnName(c);
    }

    public Class getColumnClass(int c) {
        return model.getColumnClass(c);
    }

    /*
     * this inner class holds the index of the model row Rows are compared by
     * looking at the model row entries in the sort column
     */
    private class Row implements Comparable {

        public int index;

        public int compareTo(Object other) {
            Row otherRow = (Row) other;
            Object a = model.getValueAt(index, sortColumn);
            Object b = model.getValueAt(otherRow.index, sortColumn);
            if (a instanceof StringPercentColor) {
                a = ((StringPercentColor)a).getText();
            }
            if (b instanceof StringPercentColor) {
                b = ((StringPercentColor)b).getText();
            }
            
            //If string, try to convert to numbers if possible and sort that way.
            if ((a instanceof String)&&(b instanceof String)) {
                String aStr = (String)a;
                String bStr = (String)b;
                try {
                    aStr = aStr.replace(',', '.');
                    bStr = bStr.replace(',', '.');
                    double aDouble = Double.parseDouble(aStr);
                    double bDouble = Double.parseDouble(bStr);
                    
                    return -(int)(aDouble - bDouble);
                } catch(NumberFormatException ex) {
                    //Try to make number by removing everything after a white space or (
                    try {
                        aStr = aStr.split("\\s++")[0];
                        aStr = aStr.split("\\(")[0];
                        bStr = bStr.split("\\s++")[0];
                        bStr = bStr.split("\\(")[0];
                        double aDouble = Double.parseDouble(aStr);
                        double bDouble = Double.parseDouble(bStr);
                        return -(int)(aDouble - bDouble);
                    } catch (NumberFormatException ex2) {
                        //Maybe we have a % number
                        try {
                            if (aStr.contains("%") || bStr.contains("%")) {
                                aStr = aStr.replace("%", "");
                                bStr = bStr.replace("%", "");
                                double aDouble = Double.parseDouble(aStr);
                                double bDouble = Double.parseDouble(bStr);
                                return -(int) (aDouble - bDouble);
                            }
                        } catch (NumberFormatException ex3) {
                            return aStr.compareTo(bStr);
                        }
                    }
                }
            }
            if (a instanceof Comparable) {
                return ((Comparable) a).compareTo(b);
            } else {
                return (index - otherRow.index);
            }
        }
    }
}


