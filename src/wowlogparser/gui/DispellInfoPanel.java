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

import wowlogparserbase.events.aura.*;
import wowlogparserbase.events.*;
import wowlogparserbase.eventfilter.*;
import wowlogparserbase.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import wowlogparser.*;
import wowlogparserbase.DispellEvents;
import wowlogparserbase.InterruptEvents;
import wowlogparserbase.eventfilter.FilterClass;

/**
 *
 * @author  racy
 */
public class DispellInfoPanel extends javax.swing.JPanel {
    
    Fight fight;
    StolenEvents stolenEvents = new StolenEvents();
    DispellEvents dispellEvents = new DispellEvents();
    InterruptEvents interruptEvents = new InterruptEvents();
    BrokenSpellEvents brokenEvents = new BrokenSpellEvents();
    DefaultTableModel tm1, tm2;
    
    /** Creates new form DispellInfoPanel */
    public DispellInfoPanel(Fight f) {
        initComponents();        
        fight = f;
        jTable1.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                int row = jTable1.getSelectedRow();
                if (row >= 0) {
                    rowSelected(row);
                }
            }
        });
        ButtonPressedCallback cb = new ButtonPressedCallback();
        jRadioButtonDispell.addActionListener(cb);
        jRadioButtonSteals.addActionListener(cb);
        jRadioButtonInterrupt.addActionListener(cb);
        jRadioButtonBroken.addActionListener(cb);
        
        jRadioButtonDispell.setSelected(true);
        initTables(f);
    }
    
    public class ButtonPressedCallback implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            initTables(fight);
        }        
    }
    
    public void initTables(Fight f) {
        if (jRadioButtonDispell.isSelected()) {
            initTablesDispell(f);
        }
        if (jRadioButtonSteals.isSelected()) {
            initTablesStolen(f);
        }
        if (jRadioButtonInterrupt.isSelected()) {
            initTablesInterrupt(f);
        }
        if (jRadioButtonBroken.isSelected()) {
            initTablesBroken(f);
        }
    }
    
    public void rowSelected(int row) {
        if (jRadioButtonDispell.isSelected()) {
            rowSelectedDispell(row);
        }
        if (jRadioButtonSteals.isSelected()) {
            rowSelectedStolen(row);
        }
        if (jRadioButtonInterrupt.isSelected()) {
            rowSelectedInterrupt(row);
        }        
        if (jRadioButtonBroken.isSelected()) {
            rowSelectedBroken(row);
        }        
    }

    public void initTablesStolen(Fight f) {
        stolenEvents = new StolenEvents(f.getEvents());
        
        String[] columnNames1 = {"Aura Name"};
        tm1 = new AlmostDefaultTableModel(columnNames1, 0);
        for (int k=0; k<stolenEvents.getNumNames(); k++) {
            Vector<String> sVec = new Vector<String>();
            sVec.add(stolenEvents.getName(k));
            tm1.addRow(sVec);
        }
        
        String[] columnNames2 = {"Time", "Stealer", "Skill used", "Target"};
        tm2 = new AlmostDefaultTableModel(columnNames2, 0);

        jTable1.setModel(tm1);
        jTable2.setModel(tm2);
        
    }
    
    public void initTablesDispell(Fight f) {
        dispellEvents = new DispellEvents(f.getEvents());
        
        String[] columnNames1 = {"Aura Name"};
        tm1 = new AlmostDefaultTableModel(columnNames1, 0);
        for (int k=0; k<dispellEvents.getNumNames(); k++) {
            Vector<String> sVec = new Vector<String>();
            sVec.add(dispellEvents.getName(k));
            tm1.addRow(sVec);
        }
        
        String[] columnNames2 = {"Time", "Dispeller", "Skill used", "Target"};
        tm2 = new AlmostDefaultTableModel(columnNames2, 0);

        jTable1.setModel(tm1);
        jTable2.setModel(tm2);
        
    }

    public void initTablesInterrupt(Fight f) {
        interruptEvents = new InterruptEvents(f.getEvents());
        
        String[] columnNames1 = {"Aura Name"};
        tm1 = new AlmostDefaultTableModel(columnNames1, 0);
        for (int k=0; k<interruptEvents.getNumNames(); k++) {
            Vector<String> sVec = new Vector<String>();
            sVec.add(interruptEvents.getName(k));
            tm1.addRow(sVec);
        }
        
        String[] columnNames2 = {"Time", "Interrupter", "Skill used", "Target"};
        tm2 = new AlmostDefaultTableModel(columnNames2, 0);

        jTable1.setModel(tm1);
        jTable2.setModel(tm2);
        
    }
    
    public void initTablesBroken(Fight f) {
        brokenEvents = new BrokenSpellEvents(f.getEvents());
        
        String[] columnNames1 = {"Aura Name"};
        tm1 = new AlmostDefaultTableModel(columnNames1, 0);
        for (int k=0; k<brokenEvents.getNumNames(); k++) {
            Vector<String> sVec = new Vector<String>();
            sVec.add(brokenEvents.getName(k));
            tm1.addRow(sVec);
        }
        
        String[] columnNames2 = {"Time", "Breaker", "Skill used", "Target"};
        tm2 = new AlmostDefaultTableModel(columnNames2, 0);

        jTable1.setModel(tm1);
        jTable2.setModel(tm2);
    }

    public void rowSelectedDispell(int row) {
        String name = (String)jTable1.getValueAt(row, 0);

        String[] columnNames2 = {"Time", "Dispeller", "Skill used", "Target"};
        tm2 = new AlmostDefaultTableModel(columnNames2, 0);
        List<SpellAuraDispelledEvent> events = dispellEvents.getEvents(name);
        
        for (SpellAuraDispelledEvent e : events) {
            Vector<String> sVec = new Vector<String>();
            sVec.add(e.getTimeString());
            sVec.add(e.getSourceName());
            sVec.add(e.getSkillName());
            sVec.add(e.getDestinationName());
            tm2.addRow(sVec);            
        }

        jTable2.setModel(tm2);        
    }
    
    public void rowSelectedStolen(int row) {
        String name = (String)jTable1.getValueAt(row, 0);

        String[] columnNames2 = {"Time", "Stealer", "Skill used", "Target"};
        tm2 = new AlmostDefaultTableModel(columnNames2, 0);
        List<SpellAuraStolenEvent> events = stolenEvents.getEvents(name);
        
        for (SpellAuraStolenEvent e : events) {
            Vector<String> sVec = new Vector<String>();
            sVec.add(e.getTimeString());
            sVec.add(e.getSourceName());
            sVec.add(e.getSkillName());
            sVec.add(e.getDestinationName());
            tm2.addRow(sVec);            
        }

        jTable2.setModel(tm2);        
    }
    
    public void rowSelectedInterrupt(int row) {
        String name = (String)jTable1.getValueAt(row, 0);

        String[] columnNames2 = {"Time", "Interrupter", "Skill used", "Target"};
        tm2 = new AlmostDefaultTableModel(columnNames2, 0);
        List<SpellInterruptEvent> events = interruptEvents.getEvents(name);
        
        for (SpellInterruptEvent e : events) {
            Vector<String> sVec = new Vector<String>();
            sVec.add(e.getTimeString());
            sVec.add(e.getSourceName());
            sVec.add(e.getSkillName());
            sVec.add(e.getDestinationName());
            tm2.addRow(sVec);            
        }

        jTable2.setModel(tm2);        
    }

    public void rowSelectedBroken(int row) {
        String name = (String)jTable1.getValueAt(row, 0);

        String[] columnNames2 = {"Time", "Breaker", "Skill used", "Target"};
        tm2 = new AlmostDefaultTableModel(columnNames2, 0);
        List<SpellAuraBrokenBaseEvent> events = brokenEvents.getEvents(name);
        
        for (SpellAuraBrokenBaseEvent e : events) {
            Vector<String> sVec = new Vector<String>();
            sVec.add(e.getTimeString());
            sVec.add(e.getSourceName());
            sVec.add(e.getBreakingSkill());
            sVec.add(e.getDestinationName());
            tm2.addRow(sVec);            
        }

        jTable2.setModel(tm2);        
    }

    public class AlmostDefaultTableModel extends DefaultTableModel {

        public AlmostDefaultTableModel(Object[] columnNames, int rowCount) {
            super(columnNames, rowCount);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
        
    }
    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jRadioButtonDispell = new javax.swing.JRadioButton();
        jRadioButtonInterrupt = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        jRadioButtonBroken = new javax.swing.JRadioButton();
        jRadioButtonSteals = new javax.swing.JRadioButton();

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(jTable2);

        buttonGroup1.add(jRadioButtonDispell);
        jRadioButtonDispell.setText("Dispells");

        buttonGroup1.add(jRadioButtonInterrupt);
        jRadioButtonInterrupt.setText("Interrupts");

        jLabel1.setText("Event type");

        buttonGroup1.add(jRadioButtonBroken);
        jRadioButtonBroken.setText("Broken auras");

        buttonGroup1.add(jRadioButtonSteals);
        jRadioButtonSteals.setSelected(true);
        jRadioButtonSteals.setText("Spell steals");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 251, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 653, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(jRadioButtonDispell)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jRadioButtonSteals)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jRadioButtonInterrupt)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jRadioButtonBroken)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(jRadioButtonDispell)
                    .add(jRadioButtonSteals)
                    .add(jRadioButtonInterrupt)
                    .add(jRadioButtonBroken))
                .add(32, 32, 32)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 481, Short.MAX_VALUE)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 481, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JRadioButton jRadioButtonBroken;
    private javax.swing.JRadioButton jRadioButtonDispell;
    private javax.swing.JRadioButton jRadioButtonInterrupt;
    private javax.swing.JRadioButton jRadioButtonSteals;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    // End of variables declaration//GEN-END:variables
    
}
