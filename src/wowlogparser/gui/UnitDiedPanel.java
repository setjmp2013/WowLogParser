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

import java.awt.event.ActionEvent;
import wowlogparserbase.tablerendering.StringColor;
import wowlogparserbase.tablerendering.DefaultTableCellRendererExtra;
import wowlogparserbase.helpers.TableHelper;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import wowlogparserbase.events.LogEvent;
import wowlogparserbase.events.UnitDiedEvent;
import wowlogparserbase.events.BasicEvent;
import wowlogparserbase.FileLoader;
import wowlogparserbase.TimePeriod;
import wowlogparserbase.EventCollection;
import wowlogparserbase.FightParticipant;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.prefs.Preferences;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import wowlogparser.*;
import wowlogparserbase.AbstractEventCollection;
import wowlogparserbase.EventCollectionSimple;
import wowlogparserbase.Fight;
import wowlogparserbase.events.StatusEvent;
import wowlogparserbase.events.UnitDestroyedEvent;

/**
 *
 * @author  racy
 */
public class UnitDiedPanel extends javax.swing.JPanel {
    FileLoader fl;
    Fight refFight;
    List<BasicEvent> events;
    Preferences basePrefs;
    List<DiedInfo> diedInfo = new ArrayList<UnitDiedPanel.DiedInfo>();
    AlmostDefaultTableModel tm1;
    TableModel tm2;
    double timeSpan = 10;
    
    /** Creates new form UnitDiedPanel */
    public UnitDiedPanel(FileLoader fl, List<BasicEvent> evs, Preferences basePrefs) {
        initComponents();
        this.refFight = new Fight(evs);
        this.fl = fl;
        this.events = evs;
        this.basePrefs = basePrefs;
        loadVariables();
        init();
        initDiedTable();
        initEventsTable(-1);
        jTable1.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                initEventsTable(jTable1.getSelectedRow());
            }
        });
        
        jTextField1.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                durationTextChanged();
            }

            public void removeUpdate(DocumentEvent e) {
                durationTextChanged();
            }

            public void changedUpdate(DocumentEvent e) {
                durationTextChanged();
            }
        });

        jTextField1.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                initEventsTable(jTable1.getSelectedRow());
            }
        });

        jCheckBoxVictimEvents.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                initEventsTable(jTable1.getSelectedRow());
            }
        });
        
        jCheckBoxVsVictim.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                initEventsTable(jTable1.getSelectedRow());
            }
        });

        durationTextChanged();                    
    }

    public void loadVariables() {
        String diedTime = basePrefs.get("UnitDiedTime", "10");
        boolean victimEvents = basePrefs.getBoolean("UnitDiedVictimOnly", true) ;
        boolean vsVictimEvents = basePrefs.getBoolean("UnitDiedVsVictimOnly", true) ;
        jCheckBoxVictimEvents.setSelected(victimEvents);
        jCheckBoxVsVictim.setSelected(vsVictimEvents);
        jTextField1.setText(diedTime);
    }
    
    public void saveVariables() {
        basePrefs.put("UnitDiedTime", jTextField1.getText());
        basePrefs.putBoolean("UnitDiedVictimOnly", jCheckBoxVictimEvents.isSelected());
        basePrefs.putBoolean("UnitDiedVsVictimOnly", jCheckBoxVsVictim.isSelected());
    }
    
    public void durationTextChanged() {
        String s = jTextField1.getText();
        double d = 10;
        try {
            d = Double.parseDouble(s);
        } catch (NumberFormatException ex) {
        }
        timeSpan = d;
    }
    
    public void initEventsTable(int index) {
        if (index < 0) {
            String[] columnNames1 = {""};
            tm2 = new AlmostDefaultTableModel(columnNames1, 0);
            jTable2.setModel(tm2);
            return;
        }
        
        DiedInfo di = diedInfo.get(index);
        List<TimePeriod> tps = new ArrayList<TimePeriod>();
        tps.add(new TimePeriod(di.event.time - timeSpan, di.event.time));
        List<BasicEvent> evs = AbstractEventCollection.getEvents(events, tps);
        List<BasicEvent> evsOut = new ArrayList<BasicEvent>();
        for(LogEvent ev : evs) {
            if (ev instanceof BasicEvent) {
                BasicEvent e = (BasicEvent) ev;
                if (jCheckBoxVsVictim.isSelected()) {
                    if (e.getDestinationGUID().equals(di.participant.getSourceGUID())) {
                        evsOut.add(e);
                    }
                } else if (jCheckBoxVictimEvents.isSelected()) {
                    if (e.getSourceGUID().equals(di.participant.getSourceGUID()) || e.getDestinationGUID().equals(di.participant.getSourceGUID())) {
                        evsOut.add(e);
                    }
                } else {
                    evsOut.add(e);
                }
            }
        }
        tm2 = new ShowEventsTableModel(evsOut);
        jTable2.setModel(tm2);
        jTable2.setDefaultRenderer(StringColor.class, new DefaultTableCellRendererExtra());
        TableHelper.setAutomaticColumnWidth(jTable2, 5);
        FightParticipant part = new FightParticipant();
        part.setSourceGUID(di.participant.getSourceGUID());
        part.addEvents(evsOut);
        healingParticipantsPlotPanel1 = new HealingParticipantsPlotPanel(refFight, part, basePrefs);
        jTabbedPane1.setComponentAt(1, healingParticipantsPlotPanel1);
    }
    
    public void initDiedTable() {
        String[] columnNames1 = {"Player", "Time"};
        tm1 = new AlmostDefaultTableModel(columnNames1, 0);
        for (int k=0; k<diedInfo.size(); k++) {
            Vector<String> sVec = new Vector<String>();
            sVec.add(diedInfo.get(k).participant.getName());
            sVec.add(diedInfo.get(k).event.getTimeString());
            tm1.addRow(sVec);
        }
        
        jTable1.setModel(tm1);
        TableHelper.setAutomaticColumnWidth(jTable1, 5);
    }
    
    public void init() {
        diedInfo = new ArrayList<DiedInfo>();
        List<FightParticipant> playerInfo = fl.getFightCollection().getPlayerInfo();
        for (LogEvent ev : events) {
            if (ev instanceof UnitDiedEvent || ev instanceof UnitDestroyedEvent) {
                StatusEvent e = (StatusEvent) ev;
                boolean found = false;
                FightParticipant foundP = null;
                for (FightParticipant p : playerInfo) {
                    if(p.getSourceGUID().equals(e.getDestinationGUID())) {
                        found = true;
                        foundP = p;
                        break;
                    }
                }
                if(found) {
                    diedInfo.add(new DiedInfo(e, foundP));
                }
            }
        }
    }
    
    public class DiedInfo {
        public StatusEvent event;
        public FightParticipant participant;

        public DiedInfo(StatusEvent dieDestroyEvent, FightParticipant participant) {
            this.event = dieDestroyEvent;
            this.participant = participant;
        }        
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

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jCheckBoxVictimEvents = new javax.swing.JCheckBox();
        jCheckBoxVsVictim = new javax.swing.JCheckBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        healingParticipantsPlotPanel1 = new wowlogparser.gui.HealingParticipantsPlotPanel();

        jLabel1.setText("Seconds to show");

        jTextField1.setText("10");

        jCheckBoxVictimEvents.setSelected(true);
        jCheckBoxVictimEvents.setText("Only show events that include the victim");
        jCheckBoxVictimEvents.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showEventsBoxChanged(evt);
            }
        });

        jCheckBoxVsVictim.setText("Only show events against the victim");

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 69, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jCheckBoxVictimEvents)
                    .add(jCheckBoxVsVictim))
                .addContainerGap(43, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jCheckBoxVictimEvents)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jCheckBoxVsVictim)))
                .addContainerGap(21, Short.MAX_VALUE))
        );

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

        jTabbedPane1.addTab("Event list", jScrollPane2);

        org.jdesktop.layout.GroupLayout healingParticipantsPlotPanel1Layout = new org.jdesktop.layout.GroupLayout(healingParticipantsPlotPanel1);
        healingParticipantsPlotPanel1.setLayout(healingParticipantsPlotPanel1Layout);
        healingParticipantsPlotPanel1Layout.setHorizontalGroup(
            healingParticipantsPlotPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 915, Short.MAX_VALUE)
        );
        healingParticipantsPlotPanel1Layout.setVerticalGroup(
            healingParticipantsPlotPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 589, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Health deficit graph", healingParticipantsPlotPanel1);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 180, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 920, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 617, Short.MAX_VALUE)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 617, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void showEventsBoxChanged(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showEventsBoxChanged
        initEventsTable(jTable1.getSelectedRow());
    }//GEN-LAST:event_showEventsBoxChanged
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private wowlogparser.gui.HealingParticipantsPlotPanel healingParticipantsPlotPanel1;
    private javax.swing.JCheckBox jCheckBoxVictimEvents;
    private javax.swing.JCheckBox jCheckBoxVsVictim;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
    
}
