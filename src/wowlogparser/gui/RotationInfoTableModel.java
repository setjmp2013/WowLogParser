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

import java.text.NumberFormat;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import wowlogparserbase.RotationInfo;
import wowlogparserbase.SpellInfo;
import wowlogparserbase.events.BasicEvent;

/**
 *
 * @author racy
 */
public class RotationInfoTableModel extends AbstractTableModel {
    List<SpellInfo> spellInfos;
    List<RotationInfo> rotationInfos;
    
    private static final int SPELL_NAME = 0;
    private static final int SPELL_ID = 1;
    private static final int SPELL_SCHOOL = 2;
    private static final int ROTINFO_NUM_CASTS = 3;
    private static final int ROTINFO_STRIDE_AVG = 4;
    private static final int ROTINFO_STRIDE_STD = 5;
    private static final int ROTINFO_STRIDE_MIN = 6;
    private static final int ROTINFO_STRIDE_MAX = 7;
    
    private String[] columnNames = {"Spell Name", "Spell ID", "Spell School", "Num casts", "Stride Mean", "Stride Std", "Stride Min", "Stride Max"};

    public RotationInfoTableModel(List<SpellInfo> spellInfos, List<RotationInfo> rotationInfos) {
        this.spellInfos = spellInfos;
        this.rotationInfos = rotationInfos;        
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }
    
    @Override
    public int getColumnCount() {
        return 8;
    }

    @Override
    public int getRowCount() {
        return spellInfos.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        nf.setGroupingUsed(false);
        switch(columnIndex) {
            case SPELL_NAME:
                return spellInfos.get(rowIndex).name;
            case SPELL_ID:
                return Integer.valueOf(spellInfos.get(rowIndex).spellID);
            case SPELL_SCHOOL:
                return BasicEvent.getSchoolStringFromFlags(spellInfos.get(rowIndex).school);
            case ROTINFO_NUM_CASTS:
                return new Double2Sign(rotationInfos.get(rowIndex).getNumberOfCasts());
            case ROTINFO_STRIDE_AVG:
                return new Double2Sign(rotationInfos.get(rowIndex).getStrideMean());
            case ROTINFO_STRIDE_STD:
                return new Double2Sign(rotationInfos.get(rowIndex).getStrideStd());
            case ROTINFO_STRIDE_MIN:
                return new Double2Sign(rotationInfos.get(rowIndex).getStrideMin());
            case ROTINFO_STRIDE_MAX:
                return new Double2Sign(rotationInfos.get(rowIndex).getStrideMax());                
        }
        return "";
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch(columnIndex) {
            case SPELL_NAME:
                return String.class;
            case SPELL_ID:
                return Integer.class;
            case SPELL_SCHOOL:
                return String.class;
            case ROTINFO_NUM_CASTS:
                return Double2Sign.class;
            case ROTINFO_STRIDE_AVG:
                return Double2Sign.class;
            case ROTINFO_STRIDE_STD:
                return Double2Sign.class;
            case ROTINFO_STRIDE_MIN:
                return Double2Sign.class;
            case ROTINFO_STRIDE_MAX:
                return Double2Sign.class;
        }
        return String.class;
    }

}
