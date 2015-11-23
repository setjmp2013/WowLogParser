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

package wowlogparserbase.helpers;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 *
 * @author racy
 */
public class TableHelper {

    protected TableHelper() {
    }
    
    public static void setAutomaticColumnWidth(JTable t, int margin) {
        int numColumns = t.getColumnCount();
        int numRows = t.getRowCount();
        for (int k=0; k<numColumns; k++) {
            double width = 0;
            TableColumn tc = t.getColumnModel().getColumn(k);
            TableCellRenderer headerRenderer = tc.getHeaderRenderer();
            if (headerRenderer == null) {
                headerRenderer = t.getTableHeader().getDefaultRenderer();
            }
            if (headerRenderer != null) {
                Component c = headerRenderer.getTableCellRendererComponent(t, tc.getHeaderValue(), false, false, -1, k);
                width = Math.max(c.getPreferredSize().getWidth(), width);
            }
            for (int r = 0; r<numRows; r++) {
                TableCellRenderer renderer = t.getCellRenderer(r, k);
                Component c = renderer.getTableCellRendererComponent(t, t.getValueAt(r, k), false, false, r, k);
                width = Math.max(c.getPreferredSize().getWidth(), width);
            }
            if (width > 0) {
                tc.setPreferredWidth((int)(width + margin));
            }
        }
    }    
}
