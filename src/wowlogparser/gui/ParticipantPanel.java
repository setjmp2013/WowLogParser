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
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import wowlogparserbase.FightParticipant;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.table.TableModel;
import wowlogparser.*;
import wowlogparserbase.EventCollection;
import wowlogparserbase.EventCollectionSimple;
import wowlogparserbase.events.BasicEvent;
import wowlogparserbase.events.SkillInterface;
import wowlogparserbase.info.EventsAccessInterface;
import wowlogparserbase.info.InfoBase;
import wowlogparserbase.tablemodels.*;

/**
 *
 * @author  racy
 */
public class ParticipantPanel extends javax.swing.JPanel {

    EventCollection fight;
    FightParticipant particiant;
    double updateInterval = 10;
    AmountGraph g = null;
    double savedSplitLocationRelative = 0.5;
    double currentSplitLocationRelative = 0.5;
    boolean dialogHasBeenShown = false;
    Preferences basePrefs;
    
    /** Creates new form ParticipantPanel */
    public ParticipantPanel(EventCollection f, FightParticipant p, Preferences basePrefs) {
        initComponents();
        this.basePrefs = basePrefs;
        //jPanel1.add(new ParticipantDamagePlot(f,p), BorderLayout.CENTER);
        //jPanel1.add(new JButton("Hej"));
        g = new AmountGraph();
        jPanelGraph.add(g, BorderLayout.CENTER);
        g.setBackground(Color.WHITE);
        
        updateInterval = jSlider1.getValue();
        g.setParticipant(f, p, updateInterval);
        g.repaint();
        
        this.fight = f;
        particiant = p;
        jTable1.setModel(new wowlogparserbase.tablemodels.ParticipantDamageTableModel(f, p));
        jTable2.setModel(new wowlogparserbase.tablemodels.ParticipantHealingTableModel(f, p));
        jTable3.setModel(new wowlogparserbase.tablemodels.ParticipantPowerTableModel(f, p));
        jTable4.setModel(new wowlogparserbase.tablemodels.ParticipantDamageExtendedTableModel(f, p));
        jTable5.setModel(new wowlogparserbase.tablemodels.ParticipantAbilityTableModel(f, p));
        jTable4.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        TableHelper.setAutomaticColumnWidth(jTable1, 2);
        TableHelper.setAutomaticColumnWidth(jTable2, 2);
        TableHelper.setAutomaticColumnWidth(jTable3, 2);
        TableHelper.setAutomaticColumnWidth(jTable4, 2);
        TableHelper.setAutomaticColumnWidth(jTable5, 2);

        addDoubleClickListener(jTable1);
        addDoubleClickListener(jTable2);
        addDoubleClickListener(jTable3);
        addDoubleClickListener(jTable4);
        addAbilityDoubleClickListener(jTable5);

        addPopupListener(jTable1);
        addPopupListener(jTable2);
        addPopupListener(jTable3);
        addPopupListener(jTable4);

        setCurrentGraph();
    }

    void preparePopupMenu(JTable table, MouseEvent e) {
        int row = table.getSelectedRow();
        if (row >= 0) {
            TableModel tm = table.getModel();
            if (tm instanceof RowInfoInterface) {
                RowInfoInterface rii = (RowInfoInterface) tm;
                InfoBase ib = rii.getRow(row);
                if (ib instanceof EventsAccessInterface) {
                    EventsAccessInterface eva = (EventsAccessInterface) ib;
                    EventCollectionSimple events = eva.getEvents();
                    int graphType = EventsPlotFrame2.TYPE_NONE;
                    switch (eva.getCollectionType()) {
                        case EventsAccessInterface.DAMAGE:
                            graphType = EventsPlotFrame2.TYPE_DAMAGE;
                            break;
                        case EventsAccessInterface.HEALING:
                            graphType = EventsPlotFrame2.TYPE_HEALING;
                            break;
                        case EventsAccessInterface.POWER:
                            graphType = EventsPlotFrame2.TYPE_POWER;
                            break;
                    }
                    doPopupMenu(fight, events, graphType, e);
                }
            }
        }
        
    }

    void doPopupMenu(final EventCollection fight, final EventCollectionSimple events, final int graphType, MouseEvent e) {
        JPopupMenu pop = new JPopupMenu("Show info for selected row");
        JMenuItem showGraphsItem = new JMenuItem("Show graphs");
        JMenuItem showEventsTableItem = new JMenuItem("Show events in a list");
        pop.add(showGraphsItem);
        pop.add(showEventsTableItem);

        showGraphsItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EventsPlotFrame2 frame = new EventsPlotFrame2(fight, events, basePrefs, graphType);
                frame.setLocationRelativeTo(ParticipantPanel.this);
                frame.setVisible(true);
                frame.toFront();
            }
        });

        showEventsTableItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame evFrame = new JFrame("Events");
                evFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                evFrame.setSize(1024, 768);
                evFrame.setLocationRelativeTo(ParticipantPanel.this);
                JPanel p = new JPanel(new BorderLayout());
                evFrame.getContentPane().add(p);
                p.add(new ShowEventsPanel(null, events, (ShowEventsCallback)null, basePrefs));
                evFrame.setVisible(true);
                evFrame.toFront();
            }
        });
        pop.show(e.getComponent(), e.getX(), e.getY());
    }

    void addPopupListener(final JTable table) {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                if (e.isPopupTrigger()) {
                    preparePopupMenu(table, e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                if (e.isPopupTrigger()) {
                    preparePopupMenu(table, e);
                }
            }
        });
    }

    void addDoubleClickListener(final JTable table) {
        table.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (e.getClickCount() >= 2) {
                        int row = table.getSelectedRow();
                        if (row >=0 ) {
                            TableModel tm = table.getModel();
                            if (tm instanceof RowInfoInterface) {
                                RowInfoInterface rii = (RowInfoInterface) tm;
                                InfoBase ib = rii.getRow(row);
                                if (ib instanceof EventsAccessInterface) {
                                    EventsAccessInterface eva = (EventsAccessInterface)ib;
                                    EventCollectionSimple events = eva.getEvents();
                                    int graphType = EventsPlotFrame2.TYPE_NONE;
                                    switch(eva.getCollectionType()) {
                                        case EventsAccessInterface.DAMAGE:
                                            graphType = EventsPlotFrame2.TYPE_DAMAGE;
                                            break;
                                        case EventsAccessInterface.HEALING:
                                            graphType = EventsPlotFrame2.TYPE_HEALING;
                                            break;
                                        case EventsAccessInterface.POWER:
                                            graphType = EventsPlotFrame2.TYPE_POWER;
                                            break;
                                    }
                                    EventsPlotFrame2 frame = new EventsPlotFrame2(fight, events, basePrefs, graphType);
                                    frame.setLocationRelativeTo(ParticipantPanel.this);
                                    frame.setVisible(true);
                                    frame.toFront();
                                }
                            }
                        }
                    }
                }
            }
        });
        
    }

    void addAbilityDoubleClickListener(final JTable table) {
        table.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (e.getClickCount() >= 2) {
                        int row = table.getSelectedRow();
                        if (row >=0 ) {
                            TableModel tm = table.getModel();
                            if (tm instanceof ParticipantAbilityTableModel) {
                                ParticipantAbilityTableModel atm = (ParticipantAbilityTableModel) tm;
                                AbilityRow ar = atm.getRow(row);
                                if ( ar.getEvents().size() > 0 ) {
                                    List<BasicEvent> siList = ar.getEvents();
                                    ShowEventsPanel evPanel = new ShowEventsPanel(null, new EventCollectionSimple(siList), (ShowEventsCallback)null, basePrefs);
                                    JFrame frame = new JFrame("Events");
                                    frame.getContentPane().add(evPanel);
                                    frame.pack();
                                    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                                    frame.setLocationRelativeTo(ParticipantPanel.this);
                                    frame.setVisible(true);
                                    frame.toFront();
                                }
                            }
                        }
                    }
                }
            }
        });

    }

    public void setCurrentGraph() {
        if (g != null) {
            int selectedTab = jTabbedPane1.getSelectedIndex();
            switch (selectedTab) {
                case 0:
                    resumeSavedSplitLocation();
                    g.setDamageGraph();
                    break;
                case 1:
                    resumeSavedSplitLocation();
                    g.setHealingGraph();
                    break;
                case 2:
                    resumeSavedSplitLocation();
                    g.setPowerGraph();
                    break;
                case 3:
                    setSavedSplitLocation();
                    jSplitPane1.setDividerLocation(0.1);
                    setCurrentSplitLocation();
                    g.setDamageAndHealingGraph();
                    break;
                case 4:
                    resumeSavedSplitLocation();
                    g.setDamageGraph();
                    break;
                case 5:
                    setSavedSplitLocation();
                    jSplitPane1.setDividerLocation(1.0);
                    setCurrentSplitLocation();
                    g.setNoGraph();
                    g.repaint();
                    break;
                default:
                    g.setNoGraph();
                    break;
            }
        }
    }

    boolean saved = false;
    public void setSavedSplitLocation() {
        if (!saved) {
            savedSplitLocationRelative = (double)jSplitPane1.getDividerLocation() / (double)jSplitPane1.getHeight();
            saved = true;
        }
    }
    
    public void setCurrentSplitLocation() {
        currentSplitLocationRelative = (double)jSplitPane1.getDividerLocation() / (double)jSplitPane1.getHeight();        
    }

    public void resumeSavedSplitLocation() {
        if (isVisible() && dialogHasBeenShown) {
            jSplitPane1.setDividerLocation(savedSplitLocationRelative);
            setCurrentSplitLocation();
            saved = false;
        }
    }

    public void resumeCurrentSplitLocation() {
        if (isVisible() && dialogHasBeenShown) {
            jSplitPane1.setDividerLocation(currentSplitLocationRelative);
        }
    }

    public void dialogShown() {
        setSavedSplitLocation();
        setCurrentSplitLocation();
        dialogHasBeenShown = true;
    }
    
    public void dispose() {
        if (g != null) {
            g.dispose();
        }
        particiant = null;
        fight = null;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jSlider1 = new javax.swing.JSlider();
        jPanelGraph = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable3 = new javax.swing.JTable();
        jPanelDamageAndHealing = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTable4 = new javax.swing.JTable();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTable5 = new javax.swing.JTable();

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                resized(evt);
            }
        });

        jSplitPane1.setDividerLocation(400);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jSlider1.setMajorTickSpacing(10);
        jSlider1.setMaximum(60);
        jSlider1.setMinimum(1);
        jSlider1.setMinorTickSpacing(1);
        jSlider1.setPaintLabels(true);
        jSlider1.setPaintTicks(true);
        jSlider1.setSnapToTicks(true);
        jSlider1.setToolTipText("Averaging interval (s)");
        jSlider1.setValue(10);
        jSlider1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                averageIntervalSliderChange(evt);
            }
        });

        jPanelGraph.setLayout(new java.awt.BorderLayout());

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jSlider1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1098, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanelGraph, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1098, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jSlider1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelGraph, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 309, Short.MAX_VALUE))
        );

        jPanelGraph.getAccessibleContext().setAccessibleName("");

        jSplitPane1.setRightComponent(jPanel1);

        jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabChanged(evt);
            }
        });

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

        jTabbedPane1.addTab("Damage", jScrollPane1);

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

        jTabbedPane1.addTab("Healing", jScrollPane2);

        jTable3.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane3.setViewportView(jTable3);

        jTabbedPane1.addTab("Power", jScrollPane3);

        org.jdesktop.layout.GroupLayout jPanelDamageAndHealingLayout = new org.jdesktop.layout.GroupLayout(jPanelDamageAndHealing);
        jPanelDamageAndHealing.setLayout(jPanelDamageAndHealingLayout);
        jPanelDamageAndHealingLayout.setHorizontalGroup(
            jPanelDamageAndHealingLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 1093, Short.MAX_VALUE)
        );
        jPanelDamageAndHealingLayout.setVerticalGroup(
            jPanelDamageAndHealingLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 371, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Damage&Healing graph", jPanelDamageAndHealing);

        jTable4.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane4.setViewportView(jTable4);

        jTabbedPane1.addTab("Extended Damage Info", jScrollPane4);

        jTable5.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane5.setViewportView(jTable5);

        jTabbedPane1.addTab("Ability info", jScrollPane5);

        jSplitPane1.setLeftComponent(jTabbedPane1);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1100, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jSplitPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 768, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void averageIntervalSliderChange(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_averageIntervalSliderChange
        if (!jSlider1.getValueIsAdjusting()) {
            updateInterval = jSlider1.getValue();
            g.setParticipant(fight, particiant, updateInterval);
            setCurrentGraph();
        }
    }//GEN-LAST:event_averageIntervalSliderChange

    private void tabChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabChanged
        setCurrentGraph();
    }//GEN-LAST:event_tabChanged

private void resized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_resized
    if (isVisible() && dialogHasBeenShown) {
        resumeCurrentSplitLocation();
    }
}//GEN-LAST:event_resized
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanelDamageAndHealing;
    private javax.swing.JPanel jPanelGraph;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JSlider jSlider1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTable jTable3;
    private javax.swing.JTable jTable4;
    private javax.swing.JTable jTable5;
    // End of variables declaration//GEN-END:variables
    
}
