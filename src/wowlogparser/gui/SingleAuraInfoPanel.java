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

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import wowlogparser.gui.components.SingleAuraDisplay;
import wowlogparser.gui.components.SingleAuraDisplayRuler;
import wowlogparserbase.*;
import wowlogparserbase.eventfilter.FilterAuraType;
import wowlogparserbase.eventfilter.FilterDestinationIsPlayer;
import wowlogparserbase.eventfilter.FilterOr;
import wowlogparserbase.eventfilter.FilterSourceGuid;
import wowlogparserbase.events.BasicEvent;
import wowlogparserbase.Constants;
import wowlogparserbase.helpers.VerticalLayout;
import wowlogparserbase.helpers.Worker;
import wowlogparserbase.helpers.WorkerDialog;

/**
 *
 * @author racy
 */
public class SingleAuraInfoPanel extends javax.swing.JPanel {

    private double startTime = -1;
    private double endTime = -1;
    private EventCollection events = new EventCollectionSimple();
    private Map<String, String> players;
    private JPanel mainPanel;
    private JPanel topPanel;
    private int auraBarHeight;
    List<SpellInfo> spellInfos = new ArrayList<SpellInfo>();
    Set<String> guids = new HashSet<String>();

    EventCollection filteredEvents = new EventCollectionSimple();
    private SpellInfo spellId = new SpellInfo(0, "", Constants.SCHOOL_NONE);
    Map<String, String> sources = new HashMap<String, String>();
    private String selectedSourceGuid = "";

    /** Creates new form SingleAuraInfoPanel */
    public SingleAuraInfoPanel() {
        initComponents();
        auraBarHeight = 15;
        mainPanel = new JPanel();
        mainPanel.setLayout(new VerticalLayout(2, VerticalLayout.LEFT));
        mainPanel.setBorder(null);
        JScrollPane sp = jScrollPane1;
        //sp.setBorder(null);
        sp.getHorizontalScrollBar().setUnitIncrement(16);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        
        jContentPanel.add(mainPanel);

        jComboBoxSource.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    selectedSourceGuid = ((Pair<String,String>)e.getItem()).getObj1();
                    if (selectedSourceGuid.isEmpty()) {
                        spellInfos = filteredEvents.getIDs(EventCollection.TYPE_ANY_SPELL);
                    } else {
                        spellInfos = filteredEvents.filter(new FilterSourceGuid(selectedSourceGuid)).getIDs(EventCollection.TYPE_ANY_SPELL);
                    }
                    Collections.sort(spellInfos, new Comparator<SpellInfo>() {

                        @Override
                        public int compare(SpellInfo o1, SpellInfo o2) {
                            return o1.name.compareTo(o2.name);
                        }
                    });
                    initComboBoxSpells();
                    updateDisplay();
                }
            }
        });

        jComboBoxSpells.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    spellId = (SpellInfo)e.getItem();
                    updateDisplay();
                }
            }
        });
    }

    public void setAuraBarHeight(int height) {
        this.auraBarHeight = height;
    }

    public void setEvents(EventCollection events, double startTime, double endTime) {
        this.events = events.filter(new FilterOr(new FilterAuraType(FilterAuraType.TYPE_BUFF), new FilterAuraType(FilterAuraType.TYPE_DEBUFF)));
        this.startTime = startTime;
        this.endTime = endTime;
        players = new HashMap<String, String>();
        Map<String, String> guidNames = events.getAllGuidsAndNames();
        guids = guidNames.keySet();
        sources = events.getSourceGuidsAndNames();
        for (String guid : guids) {
            if (BasicEvent.isPlayerGuid(guid)) {
                players.put(guid, guidNames.get(guid));
            }
        }
        filteredEvents = events.filter(new FilterDestinationIsPlayer());
        spellInfos = new ArrayList<SpellInfo>();
        initComboBoxSource();
        initComboBoxSpells();

    }

    public void updateDisplay() {
        mainPanel.removeAll();
        Set<String> playerGuids = players.keySet();
        int index = 0;
        int maxWidth = 0;

        for (String guid : playerGuids) {
            JPanel p = new JPanel();
            p.setLayout(null);
            JLabel l = new JLabel(players.get(guid));
            l.setSize(new Dimension(100, auraBarHeight));
            l.setLocation(0, 0);
            p.add(l);
            SingleAuraDisplay auraComp = new SingleAuraDisplay();
            auraComp.setAuraBarHeight(auraBarHeight);
            EventCollection evs;
            if (selectedSourceGuid.isEmpty()) {
                evs = events;
            } else {
                evs = events.filter(new FilterSourceGuid(selectedSourceGuid));
            }
            auraComp.setEvents(players.get(guid), guid, spellId.spellID, evs, startTime, endTime);
            auraComp.setLocation(100, 0);
            p.add(auraComp);
            mainPanel.add(p);
            Dimension d = new Dimension(100 + auraComp.getPreferredSize().width, auraBarHeight);
            p.setSize(d);
            p.setPreferredSize(d);
            maxWidth = Math.max(maxWidth, auraComp.getPreferredSize().width);
            index++;
        }

        //Add ticks
        JPanel p = new JPanel();
        p.setLayout(null);
        JLabel l = new JLabel("");
        l.setSize(new Dimension(100, auraBarHeight));
        l.setLocation(0, 0);
        p.add(l);
        SingleAuraDisplayRuler r = new SingleAuraDisplayRuler();
        r.setLocation(100, 0);
        r.setExtents(startTime - startTime, endTime - startTime, 0, 10);
        p.add(r);
        mainPanel.add(p);
        Dimension d = new Dimension(100 + r.getPreferredSize().width, r.getPreferredSize().height);
        p.setSize(d);
        p.setPreferredSize(d);
        maxWidth = Math.max(maxWidth, p.getPreferredSize().width);

        mainPanel.setPreferredSize(new Dimension(maxWidth + 10, index * auraBarHeight + p.getPreferredSize().height));
        mainPanel.setSize(new Dimension(maxWidth + 10, index * auraBarHeight + p.getPreferredSize().height));
        validate();
        repaint();
    }

    public static void main(String[] args) {
        Loader loader = new Loader(new File("D:\\Spel\\WowLogs\\WoWCombatLog_100620_lichy_dead.txt"));
        loader.parse();
        Fight fight = loader.bossFights.get(5);
        SingleAuraInfoFrame f = new SingleAuraInfoFrame("Single aura info", fight, fight.getStartTime(), fight.getEndTime());
        //f.setSize(800, 400);
        f.setVisible(true);
    }

    private void initComboBoxSource() {
        this.selectedSourceGuid = "";
        jComboBoxSource.removeAllItems();
        Set<String> sourceKeys = sources.keySet();
        List<Pair<String, String>> comboList = new ArrayList<Pair<String, String>>();
        for (String sourceGuid : sourceKeys) {
            comboList.add(new Pair<String, String>(sourceGuid, sources.get(sourceGuid), true));
        }
        Collections.sort(comboList, new Comparator<Pair<String, String>>() {

            @Override
            public int compare(Pair<String, String> o1, Pair<String, String> o2) {
                if (BasicEvent.isPlayerGuid(o1.getObj1()) && !BasicEvent.isPlayerGuid(o2.getObj1())) {
                    return -1;
                } else if (!BasicEvent.isPlayerGuid(o1.getObj1()) && BasicEvent.isPlayerGuid(o2.getObj1())) {
                    return 1;
                }
                return o1.getObj2().compareTo(o2.getObj2());
            }
        });
        jComboBoxSource.addItem(new Pair<String, String>("", "Any unit", true));
        if (comboList.size() > 0) {
            for (Pair<String, String> source : comboList) {
                jComboBoxSource.addItem(source);
            }
            jComboBoxSource.setSelectedIndex(0);
        }
        this.selectedSourceGuid = ((Pair<String, String>)jComboBoxSource.getItemAt(0)).getObj1();
    }

    private void initComboBoxSpells() {
        this.spellId = new SpellInfo(0, "", Constants.SCHOOL_NONE);
        jComboBoxSpells.removeAllItems();

        if (spellInfos.size() > 0) {
            for (SpellInfo si : spellInfos) {
                jComboBoxSpells.addItem(si);
            }
            jComboBoxSpells.setSelectedIndex(0);
            this.spellId = spellInfos.get(0);
        }
    }

    static class Loader {

        public File file;
        public FileLoader fl;
        List<Fight> bossFights;

        public Loader(File file) {
            this.file = file;
        }

        void parse() {

            FileParseThread p = new FileParseThread();
            WorkerDialog d = new WorkerDialog((JFrame) null, "Parsing file", true, p, "Please wait, parsing file...");
            d.setVisible(true);
            if (fl == null) {
                return;
            }
            if (fl.getNumErrors() > 0) {
                System.out.println("Error");
            }
            d.dispose();
            List<Fight> fights = fl.getFightCollection().getFights();
            BossParsing bp = new BossParsing((JFrame) null, fl.getFightCollection().getFights(), fl);
            bp.doAutomaticBossParsing();
            bossFights = bp.getOutFights();
            System.gc();
        }

        class FileParseThread extends Worker {

            public FileParseThread() {
            }

            @Override
            public void run() {
                if (file != null) {
                    fl = new FileLoader(file);
                    boolean ret = fl.parse();
                    if (ret) {
                        fl.createFights();
                        EventCollection ec = fl.getEventCollection();
                        AbstractEventCollection.makeOverhealing(ec.getEvents(), 60);
                    } else {
                        System.out.println("Error when parsing");
                    }
                }
                hideDialog();
            }
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

        jTopPanel = new javax.swing.JPanel();
        jComboBoxSpells = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jComboBoxSource = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jContentPanel = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        jTopPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel1.setText("Tip: Hover your mouse pointer over a buff/debuff period to see a tooltip.");

        jLabel2.setText("Tip: Green is a buff, red is a debuff");

        jLabel3.setText("Source unit");

        jLabel4.setText("Spell");

        org.jdesktop.layout.GroupLayout jTopPanelLayout = new org.jdesktop.layout.GroupLayout(jTopPanel);
        jTopPanel.setLayout(jTopPanelLayout);
        jTopPanelLayout.setHorizontalGroup(
            jTopPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTopPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jTopPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 361, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 413, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jTopPanelLayout.createSequentialGroup()
                        .add(jTopPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jComboBoxSource, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 272, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel3))
                        .add(18, 18, 18)
                        .add(jTopPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel4)
                            .add(jComboBoxSpells, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 357, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(96, Short.MAX_VALUE))
        );
        jTopPanelLayout.setVerticalGroup(
            jTopPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTopPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jTopPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(jLabel4))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jTopPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jComboBoxSource, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jComboBoxSpells, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel2)
                .addContainerGap(13, Short.MAX_VALUE))
        );

        add(jTopPanel, java.awt.BorderLayout.NORTH);

        jContentPanel.setLayout(new java.awt.BorderLayout());
        jScrollPane1.setViewportView(jContentPanel);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jComboBoxSource;
    private javax.swing.JComboBox jComboBoxSpells;
    private javax.swing.JPanel jContentPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel jTopPanel;
    // End of variables declaration//GEN-END:variables
}
