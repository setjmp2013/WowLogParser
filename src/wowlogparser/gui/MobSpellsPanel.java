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

import wowlogparserbase.helpers.TableHelper;
import wowlogparserbase.events.SkillInterface;
import wowlogparserbase.events.BasicEvent;
import wowlogparserbase.Fight;
import wowlogparserbase.FightParticipant;
import wowlogparserbase.SpellInfo;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import wowlogparser.*;
import wowlogparserbase.WLPNumberFormat;

/**
 *
 * @author  racy
 */
public class MobSpellsPanel extends javax.swing.JPanel {

    Fight fight;
    List<SpellInfo> sis;
    List<List<SkillInterface>> spellEvents;
    
    AlmostDefaultTableModel tm1, tm2;
    
    public MobSpellsPanel() {
        this(new Fight());
    }
    
    /** Creates new form MobSpellsPanel */
    public MobSpellsPanel(Fight f) {
        fight = f;
        initComponents();
        makeSpellInfo();
        jTable1.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                int row = jTable1.getSelectedRow();
                if (row >= 0) {
                    rowSelected(row);
                }
            }
        });
    }

    public void rowSelected(int row) {
        if(row > -1) {
            initEventsTable(row);
        }
    }
    
    public void makeSpellInfo() {
        FightParticipant p = fight.getVictim();
        sis = p.getIDs(FightParticipant.TYPE_ANY_SPELL);
        spellEvents = new ArrayList<List<SkillInterface>>();
        for (int k=0; k<sis.size(); k++) {
            SpellInfo si = sis.get(k);
            ArrayList<SkillInterface> singleSpellEvents = new ArrayList<SkillInterface>();
            for (BasicEvent e : p.getEvents()) {
                if (e instanceof wowlogparserbase.events.SkillInterface){                    
                    SkillInterface skill = (SkillInterface) e;
                    if (si.spellID == skill.getSkillID()) {
                        singleSpellEvents.add(skill);
                    }
                }                        
            }
            spellEvents.add(singleSpellEvents);
        }
        initSpellNameTable();
        initEventsTable(-1);
    }
    
    public void initSpellNameTable() {
        String[] columnNames1 = {"Skill Name", "Skill ID"};
        tm1 = new AlmostDefaultTableModel(columnNames1, 0);
        for (int k=0; k<spellEvents.size(); k++) {
            List<SkillInterface> events = spellEvents.get(k);

            //Only add data if there are events, otherwise remove them.
            if (events.size() > 0) {
                Vector<String> sVec = new Vector<String>();
                sVec.add(events.get(0).getSkillName());
                sVec.add("" + events.get(0).getSkillID());
                tm1.addRow(sVec);
            } else {
                removeRow(k);
                k--;
            }
        }
        
        jTable1.setModel(tm1);
        jTable1.getColumnModel().getColumn(0).setPreferredWidth(100);
        jTable1.getColumnModel().getColumn(1).setPreferredWidth(60);        
    }

    private void removeRow(int row) {
        sis.remove(row);
        spellEvents.remove(row);
    }

    public void initEventsTable(int row) {
        double startTime = fight.getVictim().getStartTime();
        NumberFormat nf = WLPNumberFormat.getInstance();
        nf.setMaximumFractionDigits(1);
        nf.setGroupingUsed(false);
        
        String[] columnNames2 = {"Time", "Time from start", "Time since last", "Skill name", "Target", "Event type", "Skill ID"};
        if (row == -1) {
            tm2 = new AlmostDefaultTableModel(columnNames2, 0);
            jTable2.setModel(tm2);
            return;
        }
        List<SkillInterface> skillEvents = spellEvents.get(row);
        tm2 = new AlmostDefaultTableModel(columnNames2, 0);
        List<BasicEvent> processedEvents = new ArrayList<BasicEvent>();
        for(SkillInterface e : skillEvents) {
            BasicEvent be = null;
            if (e instanceof BasicEvent) {
                be = (BasicEvent) e;
            } else {
                continue;
            }
            double deltaTime = be.time - startTime;
            double sinceLastTime = 0;
            for (int k=processedEvents.size()-1; k>=0; k--) {
                BasicEvent testEvent = processedEvents.get(k);
                if(testEvent.getClass().equals(be.getClass())) {
                    sinceLastTime = be.time - testEvent.time;
                    break;
                }
            }
            Vector<String> sVec = new Vector<String>();
            sVec.add(be.getTimeStringExact());
            sVec.add(nf.format(deltaTime));
            sVec.add(nf.format(sinceLastTime));
            sVec.add(e.getSkillName());
            sVec.add(be.getDestinationName());
            sVec.add(e.getClass().getSimpleName());
            sVec.add("" + e.getSkillID());
            tm2.addRow(sVec);
            processedEvents.add(be);
        }
        jTable2.setModel(tm2);
        TableHelper.setAutomaticColumnWidth(jTable2, 5);
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
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();

        jScrollPane1.setName("jScrollPane1"); // NOI18N

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
        jTable1.setName("jTable1"); // NOI18N
        jScrollPane1.setViewportView(jTable1);

        jScrollPane2.setName("jScrollPane2"); // NOI18N

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
        jTable2.setName("jTable2"); // NOI18N
        jScrollPane2.setViewportView(jTable2);

        jLabel1.setText("Fight Info");
        jLabel1.setName("jLabel1"); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 239, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 555, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .add(14, 14, 14)
                .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 246, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(540, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(19, 19, 19)
                .add(jLabel1)
                .add(29, 29, 29)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(jScrollPane2)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 538, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    // End of variables declaration//GEN-END:variables

}
