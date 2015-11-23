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

package wowlogparserarena.gui;

import wowlogparserbase.helpers.WorkerDialog;
import wowlogparserbase.helpers.Worker;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import javax.swing.event.ListSelectionEvent;
import wowlogparserbase.ArenaInfoParser;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.StandardChartTheme;
import wowlogparser.gui.EventsPlotFrame;
import wowlogparser.gui.EventsPlotPanel;
import wowlogparser.gui.HealingParticipantsPlotDialog;
import wowlogparser.gui.ParticipantDialog;
import wowlogparser.gui.ShowEventsDialog;
import wowlogparserbase.*;

/**
 *
 * @author  racy
 */
public class ArenaBaseFrame extends javax.swing.JFrame {

    Preferences prefs = Preferences.userRoot().node("wowlogparserarena");
    
    FileLoader fl = null;
    List<FightParticipant> playerInfo = new ArrayList<FightParticipant>();
    List<Double> startTimes = new ArrayList<Double>();
    
    List<ArenaFight> fights = new ArrayList<ArenaFight>();
    int selectedFight = -1;
    
    public static final int TYPE_FRIENDLY = 0;
    public static final int TYPE_ENEMY = 1;
    
    /** Creates new form ArenaBaseFrame */
    public ArenaBaseFrame() {
        initComponents();
        SettingsSingleton.getInstance().setAutoBossFights(false);
        SettingsSingleton.getInstance().setAllowNeutralNpcs(false);
        SettingsSingleton.getInstance().setAutoPlayerPets(false);
        SettingsSingleton.getInstance().setMaxInactiveTime(120);
        SettingsSingleton.getInstance().setOnlyDeadMobs(false);
        SettingsSingleton.getInstance().setOnlyMeAndRaid(false);
        SettingsSingleton.getInstance().setPetsAsOwner(false);
        ChartFactory.setChartTheme(StandardChartTheme.createLegacyTheme());
        makeFightTable();
        makeFriendlyTable(null);
        makeEnemyTable(null);
        jTableFights.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                int row = jTableFights.getSelectedRow();
                fightSelected(row);
            }
        });
        jTableFights.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.isPopupTrigger()) {
                    fightsPopup(e);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                if (e.isPopupTrigger()) {
                    fightsPopup(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                if (e.isPopupTrigger()) {
                    fightsPopup(e);
                }
            }
        });
                
        jTableFriendly.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.isPopupTrigger()) {
                    participantPopup(e, TYPE_FRIENDLY);
                }
                if (e.getClickCount() >= 2 && e.getButton() == MouseEvent.BUTTON1) {
                    int row = jTableFriendly.getSelectedRow();
                    friendlyDoubleClicked(row);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                if (e.isPopupTrigger()) {
                    participantPopup(e, TYPE_FRIENDLY);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                if (e.isPopupTrigger()) {
                    participantPopup(e, TYPE_FRIENDLY);
                }
            }
            
        });

        jTableEnemy.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.isPopupTrigger()) {
                    participantPopup(e, TYPE_ENEMY);
                }
                if (e.getClickCount() >= 2 && e.getButton() == MouseEvent.BUTTON1) {
                    int row = jTableEnemy.getSelectedRow();
                    enemyDoubleClicked(row);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                if (e.isPopupTrigger()) {
                    participantPopup(e, TYPE_ENEMY);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                if (e.isPopupTrigger()) {
                    participantPopup(e, TYPE_ENEMY);
                }
            }
            
        });
        
    }
    
    void fightsPopup(MouseEvent e) {
        if (selectedFight >= 0) {
            JPopupMenu popupMenu = new JPopupMenu("Choose an action");
            JMenuItem eventsGraphItem = new JMenuItem("Show events in a graph");
            JMenuItem eventsTableItem = new JMenuItem("Show events in a table");
            popupMenu.add(eventsGraphItem);
            popupMenu.add(eventsTableItem);
            
            eventsGraphItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ArenaFight f = fights.get(selectedFight);
                    if (f != null) {
                        EventCollectionSimple allEvents = new EventCollectionSimple();
                        allEvents.addEvents(f);
                        allEvents.sort();
                        JFrame frame = new EventsPlotFrame(f, allEvents, prefs);
                        frame.setTitle("Events related to fight " + selectedFight);
                        frame.setLocationRelativeTo(ArenaBaseFrame.this);
                        frame.setVisible(true);
                    }
                }
            });
            eventsTableItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ArenaFight f = fights.get(selectedFight);
                    if (f != null) {
                        EventCollection allEvents = new EventCollectionSimple();
                        allEvents.addEvents(f);
                        allEvents.sort();
                        File file = null;
                        if (fl != null) {
                            file = fl.getFile();
                        }
                        ShowEventsDialog d = new ShowEventsDialog(ArenaBaseFrame.this, "Events related to fight " + selectedFight, true, null, allEvents, file, prefs);
                        d.pack();
                        d.setLocationRelativeTo(ArenaBaseFrame.this);
                        d.setVisible(true);
                    }
                }
            });
            
            popupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }
    
    void participantPopup(MouseEvent e, final int type) {
        if (selectedFight >= 0) {
            JPopupMenu popupMenu = new JPopupMenu("Choose an action");
            JMenuItem detailsItem = new JMenuItem("Show detailed info");
            JMenuItem eventsGraphItem = new JMenuItem("Show events in a graph");
            JMenuItem eventsTableItem = new JMenuItem("Show events in a table");
            JMenuItem healingParticipantsItem = new JMenuItem("Show received healing plot");
            popupMenu.add(detailsItem);
            popupMenu.add(eventsGraphItem);
            popupMenu.add(eventsTableItem);
            popupMenu.add(healingParticipantsItem);
            
            detailsItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    FightParticipant p = getSelectedParticipant(type);
                    if (p != null) {
                        ArenaFight f = fights.get(selectedFight);
                        ParticipantDialog d = new ParticipantDialog(ArenaBaseFrame.this, true, f, p, prefs);
                        d.setTitle("Info for " + p.getName());
                        d.pack();
                        d.setLocationRelativeTo(ArenaBaseFrame.this);
                        d.setVisible(true);
                    }
                }
            });
            
            eventsGraphItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    FightParticipant p = getSelectedParticipant(type);
                    if (p != null) {
                        ArenaFight f = fights.get(selectedFight);
                        EventCollectionSimple allEvents = new EventCollectionSimple();
                        allEvents.addEvents(p.getEventsTotal());
                        allEvents.sort();
                        allEvents.removeEqualElements();
                        if (p != null) {
                            JFrame frame = new EventsPlotFrame(f, allEvents, prefs);
                            frame.setTitle("Events related to " + p.getName());
                            frame.setLocationRelativeTo(ArenaBaseFrame.this);
                            frame.setVisible(true);
                        }
                    }
                }
            });
            eventsTableItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    FightParticipant p = getSelectedParticipant(type);
                    if (p != null) {
                        EventCollectionSimple allEvents = new EventCollectionSimple();
                        allEvents.addEvents(p.getEventsTotal());
                        allEvents.sort();
                        allEvents.removeEqualElements();
                        if (p != null) {
                            File f = null;
                            if (fl != null) {
                                f = fl.getFile();
                            }
                            ShowEventsDialog d = new ShowEventsDialog(ArenaBaseFrame.this, "Events related to " + p.getName(), true, null, allEvents, f, prefs);
                            d.pack();
                            d.setLocationRelativeTo(ArenaBaseFrame.this);
                            d.setVisible(true);
                        }
                    }
                }
            });
            healingParticipantsItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    FightParticipant p = getSelectedParticipant(type);
                    if (p != null) {
                        EventCollectionSimple allEvents = new EventCollectionSimple();
                        allEvents.addEvents(p.getEventsTotal());
                        allEvents.sort();
                        allEvents.removeEqualElements();
                        if (p != null) {
                            HealingParticipantsPlotDialog d = new HealingParticipantsPlotDialog(ArenaBaseFrame.this, "Healing received plot for " + p.getName(), true, new Fight(allEvents.getEvents()), p, prefs);
                            d.pack();
                            d.setLocationRelativeTo(ArenaBaseFrame.this);
                            d.setVisible(true);
                        }
                    }
                }
            });
            if (getSelectedParticipant(type)!= null) {
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }        
    }

    public void openFile() {
        JFileChooser fc = new JFileChooser();
        String dir = prefs.get("openDir", "");
        fc.setCurrentDirectory(new File(dir));
        fc.setPreferredSize(new Dimension(600, 400));
        int ret = fc.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            if (f.isFile()) {
                dir = f.getParent();
                prefs.put("openDir", dir);
            }
            parse(f);
        }        
    }

    public void parse(File f) {
        FileParseThread fpt = new FileParseThread(f);
        WorkerDialog d = new WorkerDialog(this, "Parsing file", true, fpt, "Please wait, parsing file...");
        d.setLocationRelativeTo(getContentPane());
        d.setVisible(true);
        AbstractEventCollection.makeOverhealing(fl.getEventCollection().getEvents(), 60);

        PlayerClassParser cp = new PlayerClassParser(fl.getEventCollection());
        playerInfo = cp.getPlayerInfo();

        ArenaInfoParser ap = new ArenaInfoParser(fl.getEventCollection(), playerInfo);
        List<FightParticipant> teamPlayers = new ArrayList<FightParticipant>();
        List<FightParticipant> enemyPlayers = new ArrayList<FightParticipant>();
        teamPlayers = ap.getTeamPlayers();
        enemyPlayers = ap.getEnemyPlayers();
        List<FightParticipant> allPlayers = new ArrayList<FightParticipant>();
        allPlayers.addAll(teamPlayers);
        allPlayers.addAll(enemyPlayers);
        startTimes = ap.getStartTimes();
        
        for (int k=0; k<startTimes.size(); k++) {
            //Last start?
            if (k == startTimes.size()-1) {                
                double st = startTimes.get(k);
                double et = fl.getEventCollection().getEvent(fl.getEventCollection().size()-1).time;
                ArenaFight af = new ArenaFight(st, et, fl.getEventCollection());
                af.makeParticipants(fl.getEventCollection(), allPlayers);
                fights.add(af);
            } else {
                double st = startTimes.get(k);
                double et = startTimes.get(k+1) - 0.01;
                ArenaFight af = new ArenaFight(st, et,fl.getEventCollection());
                af.makeParticipants(fl.getEventCollection(), allPlayers);
                fights.add(af);
            }
        }
        
        makeFightTable();
        makeFriendlyTable(null);
        makeEnemyTable(null);
        int a = 0;        
    }
    
    protected void makeFightTable() {
        jTableFights.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        AlmostDefaultTableModel tm = new AlmostDefaultTableModel();
        tm.setColumnIdentifiers(new String[] {"Period"});
        for (int k=0; k<fights.size(); k++) {
            String name = "Fight " + (k);
            tm.addRow(new String[] {name});
        }
        jTableFights.setModel(tm);
    }
    
    protected void makeFriendlyTable(ArenaFight f) {
        jTableFriendly.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        AlmostDefaultTableModel tm = new AlmostDefaultTableModel();
        tm.setColumnIdentifiers(new String[] {"Name", "Class"});
        if (f!=null) {
            for (FightParticipant p : f.getFriendlyParticipants()) {
                String name = p.getName();
                String pClass = p.getClassName();
                tm.addRow(new String[]{name, pClass});
            }
        }
        jTableFriendly.setModel(tm);
    }

    protected void makeEnemyTable(ArenaFight f) {
        jTableEnemy.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        AlmostDefaultTableModel tm = new AlmostDefaultTableModel();
        tm.setColumnIdentifiers(new String[] {"Name", "Class"});        
        if (f != null) {
            for (FightParticipant p : f.getEnemyParticipants()) {
                String name = p.getName();
                String pClass = p.getClassName();
                tm.addRow(new String[]{name, pClass});
            }
        }
        jTableEnemy.setModel(tm);
    }

    public void fightSelected(int row) {
        if (row >= 0) {
            ArenaFight f = fights.get(row);
            makeFriendlyTable(f);
            makeEnemyTable(f);
            selectedFight = row;
        }
    }
        
    public FightParticipant getSelectedParticipant(int type) {
        switch (type) {
            case ArenaBaseFrame.TYPE_FRIENDLY:
                if (selectedFight >= 0) {
                    int row = jTableFriendly.getSelectedRow();
                    if (row >= 0) {
                        ArenaFight f = fights.get(selectedFight);
                        FightParticipant p = f.getFriendlyParticipants().get(row);
                        return p;
                    }
                }
                break;
            case TYPE_ENEMY:
                if (selectedFight >= 0) {
                    int row = jTableEnemy.getSelectedRow();
                    if (row >= 0) {
                        ArenaFight f = fights.get(selectedFight);
                        FightParticipant p = f.getEnemyParticipants().get(row);
                        return p;
                    }
                }
                break;
        }
        return null;
    }

    public void friendlyDoubleClicked(int row) {
        if (row >= 0) {
            ArenaFight f = fights.get(selectedFight);
            FightParticipant p = f.getFriendlyParticipants().get(row);
            ParticipantDialog d = new ParticipantDialog(this, true, f, p, prefs);
            d.setTitle("Info for " + p.getName());
            d.pack();
            d.setLocationRelativeTo(this);
            d.setVisible(true);
        }
    }
    
    public void enemyDoubleClicked(int row) {
        if (row >= 0) {
            ArenaFight f = fights.get(selectedFight);
            FightParticipant p = f.getEnemyParticipants().get(row);
            ParticipantDialog d = new ParticipantDialog(this, true, f, p, prefs);
            d.setTitle("Info for " + p.getName());
            d.pack();
            d.setLocationRelativeTo(this);
            d.setVisible(true);
        }
    }

    public class FileParseThread extends Worker {

        File file;
        
        public FileParseThread(File f) {
            file = f;
        }

        @Override
        public void run() {
            Cursor c = getCursor();
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            if (file != null) {
                fl = new FileLoader(file);
                boolean ret = fl.parse();
                if (ret) {
                    
                } else {
                    JOptionPane.showMessageDialog(ArenaBaseFrame.this, "Error when parsing, File not found?", "Error", JOptionPane.ERROR_MESSAGE);
                }
                
            }
            setCursor(c);
            hideDialog();
        }
        
    }
    
    public class AlmostDefaultTableModel extends DefaultTableModel {

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

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableFights = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTableFriendly = new javax.swing.JTable();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTableEnemy = new javax.swing.JTable();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItemOpen = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("When opening a file with arena matches there are sections that change each time an arena preparation buff is encountered.");
        jLabel1.setName("jLabel1"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jTableFights.setModel(new javax.swing.table.DefaultTableModel(
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
        jTableFights.setName("jTableFights"); // NOI18N
        jScrollPane1.setViewportView(jTableFights);

        jScrollPane2.setBorder(javax.swing.BorderFactory.createTitledBorder("Friendly players"));
        jScrollPane2.setName("jScrollPane2"); // NOI18N

        jTableFriendly.setModel(new javax.swing.table.DefaultTableModel(
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
        jTableFriendly.setName("jTableFriendly"); // NOI18N
        jScrollPane2.setViewportView(jTableFriendly);

        jScrollPane3.setBorder(javax.swing.BorderFactory.createTitledBorder("Enemy players"));
        jScrollPane3.setName("jScrollPane3"); // NOI18N

        jTableEnemy.setModel(new javax.swing.table.DefaultTableModel(
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
        jTableEnemy.setName("jTableEnemy"); // NOI18N
        jScrollPane3.setViewportView(jTableEnemy);

        jMenuBar1.setName("jMenuBar1"); // NOI18N

        jMenu1.setText("File");
        jMenu1.setName("jMenu1"); // NOI18N

        jMenuItemOpen.setText("Open");
        jMenuItemOpen.setName("jMenuItemOpen"); // NOI18N
        jMenuItemOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemOpenActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItemOpen);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 674, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 153, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(jScrollPane2, 0, 0, Short.MAX_VALUE)
                            .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .add(56, 56, 56)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 410, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 189, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(jScrollPane3, 0, 0, Short.MAX_VALUE)))
                .addContainerGap(91, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void jMenuItemOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemOpenActionPerformed
    openFile();
}//GEN-LAST:event_jMenuItemOpenActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ArenaBaseFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItemOpen;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable jTableEnemy;
    private javax.swing.JTable jTableFights;
    private javax.swing.JTable jTableFriendly;
    // End of variables declaration//GEN-END:variables

}
