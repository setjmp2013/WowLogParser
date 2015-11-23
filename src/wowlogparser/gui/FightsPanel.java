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

import wowlogparserbase.tablemodels.FightParticipantsTableModelReceivedDamage;
import wowlogparserbase.tablemodels.FightParticipantsTableModelDamage;
import wowlogparserbase.tablemodels.FightParticipantsTableModelReceivedHealing;
import wowlogparserbase.tablemodels.FightParticipantsTableModelHealing;
import wowlogparserbase.tablerendering.StringPercentColor;
import wowlogparserbase.tablerendering.StringPercentColorTableCellRenderer;
import wowlogparserbase.helpers.WorkerProgressDialog;
import wowlogparserbase.helpers.TableHelper;
import wowlogparserbase.events.LogEvent;
import wowlogparserbase.events.BasicEvent;
import wowlogparserbase.Fight;
import wowlogparserbase.TimePeriod;
import wowlogparserbase.FightParticipant;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
//import javax.swing.table.TableRowSorter;
import wowlogparser.*;
import wowlogparserbase.tablemodels.SortFilterModel;
import wowlogparserbase.EventCollection;
import wowlogparserbase.EventCollectionSimple;
import wowlogparserbase.IgniteMunching;
import wowlogparserbase.PetInfo;
import wowlogparserbase.SettingsSingleton;
import wowlogparserbase.WLPNumberFormat;
import wowlogparserbase.eventfilter.Filter;
import wowlogparserbase.eventfilter.FilterClass;
import wowlogparserbase.eventfilter.FilterGuidOr;
import wowlogparserbase.Constants;
import wowlogparserbase.events.aura.SpellAuraAppliedEvent;
import wowlogparserbase.events.aura.SpellAuraRemovedEvent;
import wowlogparserbase.helpers.SwingWorkerDialog;
import wowlogparserbase.tablemodels.AbstractParticipantsTableModel;
import wowlogparserbase.tablemodels.FightParticipantsTableModelDamageEnemy;
import wowlogparserbase.xml.ReportJaxb;


/**
 *
 * @author  racy
 */
public class FightsPanel extends javax.swing.JPanel implements ShowEventsCallback {
    
    public Preferences basePrefs = Preferences.userRoot().node("wowlogparser");
    Fight currentFight = null;
    FightsTableModel model;
    FightParticipantsTableModelDamage modelDamage;
    FightParticipantsTableModelHealing modelHealing;
    FightParticipantsTableModelReceivedDamage modelReceivedDamage;
    FightParticipantsTableModelReceivedHealing modelReceivedHealing;
    FightParticipantsTableModelDamageEnemy modelDamageEnemy;
    SortFilterModel sortModel;
    SortFilterModel sortModelDamage;
    SortFilterModel sortModelHealing;
    SortFilterModel sortModelReceivedDamage;
    SortFilterModel sortModelReceivedHealing;
    SortFilterModel sortModelDamageEnemy;
    List<Fight> fights = new ArrayList<Fight>();
    BaseFrame owner;
            
    
    /** Creates new form FightsPanel */
    public FightsPanel(BaseFrame own) {
        initComponents();
        owner = own;        
        
        model = new FightsTableModel();
        sortModel = new SortFilterModel(model);
        sortModel.addMouseListener(jTable1);
        jTable1.setModel(sortModel);

        modelDamage = new FightParticipantsTableModelDamage();
        sortModelDamage = new SortFilterModel(modelDamage);
        sortModelDamage.addMouseListener(jTableDamage);
        jTableDamage.setModel(sortModelDamage);
        jTableDamage.setDefaultRenderer(StringPercentColor.class, new StringPercentColorTableCellRenderer());
        
        modelHealing = new FightParticipantsTableModelHealing();
        sortModelHealing = new SortFilterModel(modelHealing);
        sortModelHealing.addMouseListener(jTableHealing);
        jTableHealing.setModel(sortModelHealing);
        jTableHealing.setDefaultRenderer(StringPercentColor.class, new StringPercentColorTableCellRenderer());
        
        modelReceivedDamage = new FightParticipantsTableModelReceivedDamage();
        sortModelReceivedDamage = new SortFilterModel(modelReceivedDamage);
        sortModelReceivedDamage.addMouseListener(jTableReceivedDamage);
        jTableReceivedDamage.setModel(sortModelReceivedDamage);
        jTableReceivedDamage.setDefaultRenderer(StringPercentColor.class, new StringPercentColorTableCellRenderer());

        modelReceivedHealing = new FightParticipantsTableModelReceivedHealing();
        sortModelReceivedHealing = new SortFilterModel(modelReceivedHealing);
        sortModelReceivedHealing.addMouseListener(jTableReceivedHealing);
        jTableReceivedHealing.setModel(sortModelReceivedHealing);
        jTableReceivedHealing.setDefaultRenderer(StringPercentColor.class, new StringPercentColorTableCellRenderer());
        
        modelDamageEnemy = new FightParticipantsTableModelDamageEnemy();
        sortModelDamageEnemy = new SortFilterModel(modelDamageEnemy);
        sortModelDamageEnemy.addMouseListener(jTableDamageNpcs);
        jTableDamageNpcs.setModel(sortModelDamageEnemy);
        jTableDamageNpcs.setDefaultRenderer(StringPercentColor.class, new StringPercentColorTableCellRenderer());

        jTable1.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int row = jTable1.getSelectedRow();
                    row = sortModel.convertRowIndexToModel(row);
                    //row = jTable1.convertRowIndexToModel(row);
                    if (row >= 0 && row < fights.size()) {
                        Fight f = fights.get(row);
                        fightRowClicked(f);
                    }
                }
            }
        });
             
        jTable1.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                Component c = e.getComponent();
                if (e.isPopupTrigger()) {
                    doFightsPopupMenu(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                Component c = e.getComponent();
                if (e.isPopupTrigger()) {
                    doFightsPopupMenu(e);
                }
            }
        });

        jTableDamage.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getButton() == e.BUTTON1) {
                    if (e.getClickCount() == 2) {
                        showParticipantDetails(jTableDamage);
                    }
                }
            }
            
        });

        jTableDamage.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                Component c = e.getComponent();
                if (e.isPopupTrigger()) {
                    doParticipantsPopupMenu(e, jTableDamage);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                Component c = e.getComponent();
                if (e.isPopupTrigger()) {
                    doParticipantsPopupMenu(e, jTableDamage);
                }
            }
        });
        
        jTableDamageNpcs.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                Component c = e.getComponent();
                if (e.isPopupTrigger()) {
                    doParticipantsNpcPopupMenu(e, jTableDamageNpcs);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                Component c = e.getComponent();
                if (e.isPopupTrigger()) {
                    doParticipantsNpcPopupMenu(e, jTableDamageNpcs);
                }
            }
        });

        jTableHealing.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getButton() == e.BUTTON1) {
                    if (e.getClickCount() == 2) {
                        showParticipantDetails(jTableHealing);
                    }
                }
            }
            
        });

        jTableHealing.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                Component c = e.getComponent();
                if (e.isPopupTrigger()) {
                    doParticipantsPopupMenu(e, jTableHealing);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                Component c = e.getComponent();
                if (e.isPopupTrigger()) {
                    doParticipantsPopupMenu(e, jTableHealing);
                }
            }
        });

        jTableReceivedDamage.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (e.getClickCount() == 2) {
                        showParticipantDetailsReceived(jTableReceivedDamage);
                    }
                }
            }
            
        });

        jTableReceivedDamage.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                if (e.isPopupTrigger()) {
                    doParticipantsPopupMenu(e, jTableReceivedDamage);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                if (e.isPopupTrigger()) {
                    doParticipantsPopupMenu(e, jTableReceivedDamage);
                }
            }
        });

        jTableReceivedHealing.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getButton() == e.BUTTON1) {
                    if (e.getClickCount() == 2) {
                        showParticipantDetails(jTableReceivedHealing);
                    }
                }
            }
            
        });

        jTableReceivedHealing.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                Component c = e.getComponent();
                if (e.isPopupTrigger()) {
                    doParticipantsPopupMenu(e, jTableReceivedHealing);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                Component c = e.getComponent();
                if (e.isPopupTrigger()) {
                    doParticipantsPopupMenu(e, jTableReceivedHealing);
                }
            }
        });

        jTableDamageNpcs.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (e.getClickCount() == 2) {
                        showParticipantDetailsEnemy(jTableDamageNpcs);
                    }
                }
            }

        });

        TableColumn tc = jTable1.getColumnModel().getColumn(0);
        tc.setPreferredWidth(200);
        tc = jTable1.getColumnModel().getColumn(1);
        tc.setPreferredWidth(100);
                
    }

    public void showParticipantDetails(JTable t) {
        if (currentFight != null) {
            List<FightParticipant> parts = getParticipantList(t);
            if (parts.size() > 0) {
                FightParticipant part = parts.get(0);
                //ParticipantDialog pd = new ParticipantDialog(owner, true, currentFight, part);
                ParticipantFrame pd = new ParticipantFrame(owner, currentFight, part, basePrefs);
                LogEvent ev = currentFight.getStartEvent();
                String time = ev.hour + ":" + ev.minute + ":" + (int) ev.second;
                pd.setTitle(part.getName() + " vs " + currentFight.getName() + " " + time);
                pd.pack();
                pd.setLocationByPlatform(true);
                pd.setVisible(true);
            }
        }
    }
    
    public void showParticipantDetailsReceived(JTable t) {
        if (currentFight != null) {
            List<FightParticipant> parts = getParticipantList(t);
            if (parts.size() > 0) {
                FightParticipant part = parts.get(0);
                
                FightParticipant receivedPart = part.makeReceivedParticipant(currentFight);
                Fight receivedFight = new Fight(currentFight);
                receivedFight.setName(part.getName());

                //ParticipantDialog pd = new ParticipantDialog(owner, true, receivedFight, receivedPart);
                ParticipantFrame pd = new ParticipantFrame(owner, receivedFight, receivedPart, basePrefs);
                LogEvent ev = receivedFight.getStartEvent();
                String time = ev.hour + ":" + ev.minute + ":" + (int) ev.second;
                pd.setTitle(receivedPart.getName() + " vs " + receivedFight.getName() + " " + time);
                pd.pack();
                pd.setLocationByPlatform(true);
                pd.setVisible(true);
            }
        }
    }

    public void showParticipantDetailsEnemy(JTable t) {
        if (currentFight != null) {
            List<FightParticipant> parts = getParticipantList(t);
            if (parts.size() > 0) {
                FightParticipant part = parts.get(0);
                ParticipantFrame pd = new ParticipantFrame(owner, currentFight, part, basePrefs);
                LogEvent ev = currentFight.getStartEvent();
                String time = ev.hour + ":" + ev.minute + ":" + (int) ev.second;
                pd.setTitle(part.getName() + " vs " + "Various sources" + " " + time);
                pd.pack();
                pd.setLocationByPlatform(true);
                pd.setVisible(true);
            }
        }
    }

    public List<Fight> getFightList() {
        ArrayList<Fight> list = new ArrayList<Fight>();
        int[] rows = jTable1.getSelectedRows();
        for (int k = 0; k < rows.length; k++) {
            //int row = jTable1.convertRowIndexToModel(rows[k]);
            int row = sortModel.convertRowIndexToModel(rows[k]);
            Fight f = new Fight(fights.get(row));
            list.add(f);
        }
        return list;
    }

    public Fight getFightListMerged() {
        Cursor c = getCursor();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ArrayList<Fight> list = new ArrayList<Fight>();
        int[] rows = jTable1.getSelectedRows();
        if (rows.length == 1) {
            setCursor(c);
            return fights.get(sortModel.convertRowIndexToModel(rows[0]));
        } else {
            for (int k = 0; k < rows.length; k++) {
                //int row = jTable1.convertRowIndexToModel(rows[k]);
                int row = sortModel.convertRowIndexToModel(rows[k]);
                list.add(fights.get(row));
            }
            FightMergeThread mt = new FightMergeThread(list);
            WorkerProgressDialog wd = new WorkerProgressDialog(owner, "Merging fights", true, mt, "Please wait, merging fights...");
            wd.setLocationRelativeTo(FightsPanel.this);
            wd.setVisible(true);
            Fight mergedFight = mt.getMergedFight();
            setCursor(c);
            return mergedFight;
        }
    }

    public void doFightsPopupMenu(MouseEvent e) {
        JPopupMenu fightsPopupMenu;
        JMenuItem mergeFightsItem;
        JMenuItem fightsDamageChartMenuItem;
        JMenuItem fightsHealingChartMenuItem;
        JMenuItem fightsOpenDialogMenuItem;
        JMenuItem fightsOwnDamageChartMenuItem;
        JMenuItem fightsOwnHealingChartMenuItem;
        JMenuItem fightsShowEventsMenuItem;
        JMenuItem fightsShowDispellInfoMenuItem;
        JMenuItem fightsShowSingleAuraMenuItem;
        JMenuItem fightsShowDiedInfoMenuItem;
        JMenuItem fightsMakeHtml;
        JMenuItem fightsMakeHtmlMerged;
        // Callithrix - They modification - Begin
        JMenuItem fightsMakeHtmlBoth;
        // Callithrix - They modification - End
        JMenuItem fightsMakeBoss;
        JMenuItem fightsShowVictimSpells;
        JMenuItem fightsShowDpsPlots;
        JMenuItem fightsMakeXml;
        JMenuItem fightsMakeXmlMerged;
        JMenuItem fightsShowRaidDps;
        JMenuItem fightsShowEventsGraph;
        JMenuItem fightsWhoHealsWhom;
        JMenuItem fightsSplit;
        
        if (currentFight != null) {
            
            fightsPopupMenu = new JPopupMenu();
            mergeFightsItem = new JMenuItem("Show Fights (Merge)");
            fightsDamageChartMenuItem = new JMenuItem("Participants Damage chart (Merge)");
            fightsHealingChartMenuItem = new JMenuItem("Participants Healing chart (Merge)");
            fightsOpenDialogMenuItem = new JMenuItem("Details for the victim (Merge)");
            fightsOwnDamageChartMenuItem = new JMenuItem("Damage chart for the victim (Merge)");
            fightsOwnHealingChartMenuItem = new JMenuItem("Healing chart for the victim (Merge)");
            fightsShowEventsMenuItem = new JMenuItem("Show all events in a list (Merge)");
            fightsShowDispellInfoMenuItem = new JMenuItem("Show dispell and interrupt info (Merge)");
            fightsShowSingleAuraMenuItem = new JMenuItem("Show buff/debuff graphs for a single spell(Merge)");
            fightsShowDiedInfoMenuItem = new JMenuItem("Show events when someone died (Merge)");
            fightsMakeHtml = new JMenuItem("Make Html report for selected (Individual)");
            fightsMakeHtmlMerged = new JMenuItem("Make Html report for selected (Merge)");
            // Callithrix - They modification - Begin
            fightsMakeHtmlBoth = new JMenuItem("Make Html report for selected (Individual + Merged)");
            // Callithrix - They modification - End
            fightsMakeBoss = new JMenuItem("Make boss fights (Only include selected fights)");
            fightsShowVictimSpells = new JMenuItem("Show spell info for the victim (Merge)");
            fightsShowDpsPlots = new JMenuItem("Timeline plot of all participants dps and hps (Merge)");
            fightsMakeXml = new JMenuItem("Make XML for selected (Individual)");
            fightsMakeXmlMerged = new JMenuItem("Make XML for selected (Merge)");
            fightsShowRaidDps =  new JMenuItem("Show Raid DPS timeline");
            fightsShowEventsGraph = new JMenuItem("Show events in a graph");
            fightsWhoHealsWhom = new JMenuItem("Who heals whom table");
            fightsSplit = new JMenuItem("Split fight with condition");
            

            fightsPopupMenu.add(mergeFightsItem);
            fightsPopupMenu.addSeparator();
            fightsPopupMenu.add(fightsDamageChartMenuItem);
            fightsPopupMenu.add(fightsHealingChartMenuItem);
            fightsPopupMenu.add(fightsShowDpsPlots);
            fightsPopupMenu.add(fightsShowRaidDps);
            fightsPopupMenu.addSeparator();
            fightsPopupMenu.add(fightsOpenDialogMenuItem);
            fightsPopupMenu.add(fightsOwnDamageChartMenuItem);
            fightsPopupMenu.add(fightsOwnHealingChartMenuItem);
            fightsPopupMenu.addSeparator();
            fightsPopupMenu.add(fightsShowEventsMenuItem);
            fightsPopupMenu.add(fightsShowDispellInfoMenuItem);
            fightsPopupMenu.add(fightsShowSingleAuraMenuItem);
            fightsPopupMenu.add(fightsShowDiedInfoMenuItem);
            fightsPopupMenu.add(fightsShowVictimSpells);
            fightsPopupMenu.add(fightsShowEventsGraph);
            fightsPopupMenu.add(fightsWhoHealsWhom);
            fightsPopupMenu.addSeparator();
            fightsPopupMenu.add(fightsMakeHtml);
            fightsPopupMenu.add(fightsMakeHtmlMerged);
            // Callithrix - They modification - Begin
            fightsPopupMenu.add(fightsMakeHtmlBoth);
            // Callithrix - They modification - End
            fightsPopupMenu.addSeparator();
            fightsPopupMenu.add(fightsMakeXml);
            fightsPopupMenu.add(fightsMakeXmlMerged);            
            fightsPopupMenu.addSeparator();
            fightsPopupMenu.add(fightsMakeBoss);
            fightsPopupMenu.add(fightsSplit);

            fightsShowRaidDps.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    List<Fight> list = getFightList();

                    RaidPlotsDialog d =  new RaidPlotsDialog(owner, "Dps vs selected mobs", true, list);

                    d.pack();
                    d.setLocationByPlatform(true);
                    d.setVisible(true);
                }
            });

            fightsShowDpsPlots.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    Fight mergedFight = getFightListMerged();
                                        
                    MultiplePlotsDialog d =  new MultiplePlotsDialog(owner, "Dps and Hps info for all participants", true, mergedFight);
                    
                    d.pack();
                    d.setLocationByPlatform(true);
                    d.setVisible(true);
                }
            });

            fightsShowVictimSpells.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    Fight mergedFight = getFightListMerged();
                                        
                    MobSpellsDialog d =  new MobSpellsDialog(owner, false, mergedFight);
                    
                    d.pack();
                    d.setLocationByPlatform(true);
                    d.setVisible(true);
                }
            });
            
            fightsMakeBoss.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    BossDialog bd = new BossDialog(owner, "Make boss fights", true, getFightList(), owner.fl);
                    bd.setLocationByPlatform(true);
                    bd.setVisible(true);
                    List<Fight> outFights = bd.getOutFights();
                    if (outFights != null) {
                        for (Fight f : outFights) {
                            f.removeAssignedPets();
                        }
                        owner.newFightsPanel("Boss Fights", outFights);
                        //setFights(outFights);
                    }
                    bd.dispose();
                }
            });

    
            fightsMakeHtml.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    List<Fight> list = getFightList();
                    new MakeHtmlReportTabs(owner, list, basePrefs, false).showFileChooser();
                }
            });

            fightsMakeHtmlMerged.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    List<Fight> list = getFightList();
                    new MakeHtmlReportTabs(owner, list, basePrefs, true).showFileChooser();
                }
            });
            
            fightsMakeHtmlBoth.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    List<Fight> list = getFightList();
                    new MakeHtmlReportTabs(owner, list, basePrefs, false, true).showFileChooser();
                }
            });
            
            mergeFightsItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Fight mergedFight = getFightListMerged();
                    fightRowClicked(mergedFight);
                }
            });
            
            fightsDamageChartMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Fight mergedFight = getFightListMerged();
                    
                    JDialog d = new FightDamageChartDialog(owner, false, mergedFight);
                    d.setLocationByPlatform(true);
                    d.setVisible(true);
                }
            });
            
            fightsHealingChartMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Fight mergedFight = getFightListMerged();
                    
                    JDialog d = new FightHealingChartDialog(owner, false, mergedFight);
                    d.setLocationByPlatform(true);
                    d.setVisible(true);
                }
            });

            fightsOpenDialogMenuItem.addActionListener(new ActionListener() {                

                @Override
                public void actionPerformed(ActionEvent e) {                    
                    Fight mergedFight = getFightListMerged();

                    //ParticipantDialog pd = new ParticipantDialog(owner, true, mergedFight, mergedFight.getVictim());
                    ParticipantFrame pd = new ParticipantFrame(owner, mergedFight, mergedFight.getVictim(), basePrefs);
                    LogEvent ev = mergedFight.getStartEvent();
                    String time = ev.hour + ":" + ev.minute + ":" + (int) ev.second;
                    pd.setTitle(mergedFight.getVictim().getName() + " vs " + "Others" + " " + time);
                    pd.pack();
                    pd.setLocationByPlatform(true);
                    pd.setVisible(true);
                }
            });

            fightsOwnDamageChartMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Fight mergedFight = getFightListMerged();
                    
                    JDialog d = new ParticipantDamageChartDialog(owner, false, mergedFight.getVictim());
                    d.setLocationByPlatform(true);
                    d.setVisible(true);
                }
            });

            fightsOwnHealingChartMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Fight mergedFight = getFightListMerged();
                    
                    JDialog d = new ParticipantHealingChartDialog(owner, false, mergedFight.getVictim());
                    d.setLocationByPlatform(true);
                    d.setVisible(true);
                }
            });
            
            fightsShowEventsMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ArrayList<TimePeriod> tps = new ArrayList<TimePeriod>();
                    int[] rows = jTable1.getSelectedRows();
                    for (int k = 0; k < rows.length; k++) {
                        //int row = jTable1.convertRowIndexToModel(rows[k]);
                        int row = sortModel.convertRowIndexToModel(rows[k]);
                        tps.add(new TimePeriod(fights.get(row).getStartTime(), fights.get(row).getEndTime()));
                    }
                                        
                    ShowEventsFrame f =  new ShowEventsFrame(tps, owner.fl.getEventCollection(), new File(owner.file.getAbsolutePath()), basePrefs);
                    
                    f.setLocationByPlatform(true);
                    f.setVisible(true);
                }
            });
            
            fightsShowDispellInfoMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Fight mergedFight = getFightListMerged();
                                        
                    DispellInfoDialog d =  new DispellInfoDialog(owner, true, mergedFight);
                    
                    d.pack();
                    d.setLocationByPlatform(true);
                    d.setVisible(true);
                }
            });

            fightsShowSingleAuraMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Fight mergedFight = getFightListMerged();

                    SingleAuraInfoFrame f = new SingleAuraInfoFrame("Single aura uptime info during the fight vs "+mergedFight.getName(), mergedFight, mergedFight.getStartTime(), mergedFight.getEndTime());

                    f.setLocationByPlatform(true);
                    f.setVisible(true);
                }
            });

            fightsShowDiedInfoMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Fight mergedFight = getFightListMerged();

                    double start = mergedFight.getStartTime();                            
                    double last = mergedFight.getEndTime();
                    List<TimePeriod> tps = new ArrayList<TimePeriod>();
                    tps.add(new TimePeriod(start, last));
                    List<BasicEvent> evs = owner.fl.getEventCollection().getEvents(tps);
                    
                    //ArrayList<BasicEvent> evs = new ArrayList<BasicEvent>();
                    //evs.addAll(mergedFight.events);
                    //evs.addAll(mergedFight.nonFightDamageHealingEvents);
                    
                    UnitDiedDialog d = new UnitDiedDialog(owner, true, owner.fl, evs, basePrefs);
                    
                    d.pack();
                    d.setLocationByPlatform(true);
                    d.setVisible(true);
                }
            });

            fightsMakeXml.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    final List<Fight> list = getFightList();
                    JFileChooser fc = new JFileChooser();
                    String dir = basePrefs.get("XmlDir", "");
                    fc.setCurrentDirectory(new File(dir));
                    int ret = fc.showSaveDialog(owner);
                    if (ret == JFileChooser.APPROVE_OPTION) {
                        final File file = fc.getSelectedFile();
                        File xmlDir = file;
                        if (!xmlDir.isDirectory()) {
                            xmlDir = xmlDir.getParentFile();
                        }
                        basePrefs.put("XmlDir", xmlDir.getAbsolutePath());
                        final File xmlDirFinal = xmlDir;
                        SwingWorker worker = new SwingWorker() {
                            @Override
                            protected Object doInBackground() throws Exception {
                                ReportJaxb report = new ReportJaxb(xmlDirFinal);
                                try {
                                    report.makeFights(owner.fl, list, "Selected Fights", file.getName());
                                } catch (Exception ex) {
                                    return ex.getMessage();
                                }
                                return "";
                            }
                        };

                        SwingWorkerDialog dia = new SwingWorkerDialog(worker, new JLabel("Please wait, Making XML files..."), owner, "Making XML files");
                        dia.setLocationRelativeTo(FightsPanel.this);
                        dia.setVisible(true);
                        String threadRet = (String)dia.getResult();
                        if (!"".equals(threadRet)) {
                            JOptionPane.showMessageDialog(owner, "Error writing to file: "+threadRet, "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            });

            fightsMakeXmlMerged.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Fight mergedFight = getFightListMerged();
                    final ArrayList<Fight> mergedFightArray = new ArrayList<Fight>();
                    mergedFightArray.add(mergedFight);
                    
                    JFileChooser fc = new JFileChooser();
                    String dir = basePrefs.get("XmlDir", "");
                    fc.setCurrentDirectory(new File(dir));
                    int ret = fc.showSaveDialog(owner);
                    if (ret == JFileChooser.APPROVE_OPTION) {
                        final File file = fc.getSelectedFile();
                        File xmlDir = file;
                        if (!xmlDir.isDirectory()) {
                            xmlDir = xmlDir.getParentFile();
                        }
                        basePrefs.put("XmlDir", xmlDir.getAbsolutePath());
                        final File xmlDirFinal = xmlDir;
                        SwingWorker worker = new SwingWorker() {

                            @Override
                            protected Object doInBackground() throws Exception {
                                ReportJaxb report = new ReportJaxb(xmlDirFinal);
                                try {
                                    report.makeFights(owner.fl, mergedFightArray, "Merged Selected Fights", file.getName());
                                } catch (IOException ex) {
                                    return ex.getMessage();
                                }
                                return "";
                            }
                        };
                        SwingWorkerDialog dia = new SwingWorkerDialog(worker, new JLabel("Please wait, Making XML files..."), owner, "Making XML files");
                        dia.setLocationRelativeTo(FightsPanel.this);
                        dia.setVisible(true);
                        String threadRet = (String)dia.getResult();
                        if (!"".equals(threadRet)) {
                            JOptionPane.showMessageDialog(owner, "Error writing to file: "+threadRet, "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            });

            fightsShowEventsGraph.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    Fight mergedFight = getFightListMerged();
                    EventCollectionSimple allEvents = new EventCollectionSimple();
                    allEvents.addEvents(mergedFight);
                    allEvents.sort();
                    allEvents.removeEqualElements();
                    JFrame frame = new EventsPlotFrame(mergedFight, allEvents, basePrefs);
                    frame.setTitle("Events for fight "+mergedFight.getName());
                    frame.setLocationRelativeTo(FightsPanel.this);
                    frame.setVisible(true);

                }
            });
            
            fightsWhoHealsWhom.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    Fight mergedFight = getFightListMerged();
                    JDialog d = new WhoHealsWhomDialog(mergedFight, owner, true);
                    d.setLocationRelativeTo(FightsPanel.this);
                    d.setVisible(true);
                }
            });

            fightsSplit.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    Fight mergedFight = getFightListMerged();
                    FightSplitterDialog d = new FightSplitterDialog(owner, mergedFight, owner.fl);
                    d.setLocationRelativeTo(FightsPanel.this);
                    d.setVisible(true);
                    List<Fight> splitFights = d.getFights();
                    if (splitFights.size() > 0) {
                        owner.newFightsPanel("Split fights", splitFights);
                    } 
                }
            });

            fightsPopupMenu.show(e.getComponent(), e.getX(), e.getY());
        }        
    }

    @Override
    public void showEventsSaveLogLines(int[] rows) {
        int k;
        if (rows.length == 0) {
            JOptionPane.showMessageDialog(owner, "Error, not enough rows chosen");
            return;
        }
        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            File inFile = this.owner.file;
            JFileChooser fc = new JFileChooser();
            fc.setCurrentDirectory(inFile);
            int res = fc.showSaveDialog(owner);
            if (res == JFileChooser.APPROVE_OPTION) {
                File outFile = fc.getSelectedFile();
                Arrays.sort(rows);
                int minRow = rows[0];
                int maxRow = rows[rows.length - 1];
                reader = new BufferedReader(new FileReader(inFile));
                writer = new BufferedWriter(new FileWriter(outFile));
                for (k = 0; k < minRow; k++) {
                    reader.readLine();
                }   
                int currentRowIndex = 0;
                for (k = minRow; k <= maxRow; k++) {                    
                    if (currentRowIndex >= rows.length) {
                        break;
                    }
                    String line = reader.readLine();
                    if (k == rows[currentRowIndex]) {
                        writer.write(line);
                        writer.newLine();
                        currentRowIndex++;
                    }
                }
                writer.close();
                reader.close();
            } 
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(owner, "Error, file not found: "+ e.getMessage());
            return;                
        } catch (IOException e) {
            JOptionPane.showMessageDialog(owner, "Error, IOException: "+ e.getMessage());
        }
    }

    /**
     * Get participant list
     * @param t A table with a SortFilterModel table model
     * @return the list
     */
    public List<FightParticipant> getParticipantList(JTable t) {
        ArrayList<FightParticipant> list = new ArrayList<FightParticipant>();
        int[] rows = t.getSelectedRows();
        SortFilterModel m = (SortFilterModel)t.getModel();
        if (m.getModel() instanceof AbstractParticipantsTableModel) {
            AbstractParticipantsTableModel atm = (AbstractParticipantsTableModel) m.getModel();
            for (int k = 0; k < rows.length; k++) {
                int row = m.convertRowIndexToModel(rows[k]);
                list.add(atm.getParticipants().get(row));
            }
        }
        return list;
    }
    
    /**
     * Get merged participants
     * @param t A table with a SortFilterModel table model
     * @return The participant
     */
    public FightParticipant getParticipantListMerged(JTable t) {
        ArrayList<FightParticipant> list = new ArrayList<FightParticipant>();
        SortFilterModel m = (SortFilterModel)t.getModel();
        int[] rows = t.getSelectedRows();
        if (m.getModel() instanceof AbstractParticipantsTableModel) {
            AbstractParticipantsTableModel atm = (AbstractParticipantsTableModel) m.getModel();
            for (int k = 0; k < rows.length; k++) {
                //int row = jTable2.convertRowIndexToModel(rows[k]);
                int row = m.convertRowIndexToModel(rows[k]);
                list.add(atm.getParticipants().get(row));
            }
        }
        ParticipantMergeThread mt = new ParticipantMergeThread(list);
        WorkerProgressDialog wd = new WorkerProgressDialog(owner, "Merging participants", true, mt, "Merging participants...");
        wd.setLocationRelativeTo(FightsPanel.this);
        wd.setVisible(true);
        FightParticipant part = mt.getMergedParticipant();
        return part;
    }

    public void doParticipantsPopupMenu(MouseEvent e, final JTable t) {
        JPopupMenu participantsPopupMenu;
        JMenuItem openDialogItem;
        JMenuItem receivedDialogItem;
        JMenuItem participantsDamageChartMenuItem;
        JMenuItem participantsHealingChartMenuItem;
        JMenuItem receivedHealingPlotItem;
        JMenuItem auraInfoItem;        
        JMenuItem eventsGraphItem;
        JMenuItem eventsListItem;
        JMenuItem rotationsItem;
        JMenuItem detailsFromFilterItem;
        JMenuItem detailsReceivedFromFilterItem;
        JMenuItem igniteMunchingItem;
        
        if (currentFight != null) {
            participantsPopupMenu = new JPopupMenu();
            openDialogItem = new JMenuItem("Show details (Merge)");
            detailsFromFilterItem = new JMenuItem("Show filtered details (Merge)");
            receivedDialogItem = new JMenuItem("Show details on received events (Merge)");
            detailsReceivedFromFilterItem = new JMenuItem("Show filtered details on received events (Merge)");
            participantsDamageChartMenuItem = new JMenuItem("Damage chart (Merge)");
            participantsHealingChartMenuItem = new JMenuItem("Healing chart (Merge)");
            receivedHealingPlotItem = new JMenuItem("Received healing plot (Merge)");
            auraInfoItem = new JMenuItem("Aura information with graph (Single)");
            eventsGraphItem = new JMenuItem("Show events in a graph (Merge)");
            eventsListItem = new JMenuItem("Show events in a list (Merge)");
            rotationsItem = new JMenuItem("Show rotations");
            igniteMunchingItem = new JMenuItem("Calc ignite munching");

            participantsPopupMenu.add(openDialogItem);
            participantsPopupMenu.add(detailsFromFilterItem);
            participantsPopupMenu.add(receivedDialogItem);
            participantsPopupMenu.add(detailsReceivedFromFilterItem);
            participantsPopupMenu.add(participantsDamageChartMenuItem);
            participantsPopupMenu.add(participantsHealingChartMenuItem);
            participantsPopupMenu.add(receivedHealingPlotItem);
            participantsPopupMenu.add(auraInfoItem);
            participantsPopupMenu.add(eventsListItem);
            participantsPopupMenu.add(eventsGraphItem);
            participantsPopupMenu.add(rotationsItem);
            participantsPopupMenu.add(igniteMunchingItem);

            igniteMunchingItem.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    FightParticipant part = getParticipantListMerged(t);
                    IgniteMunching im = new IgniteMunching();
                    double critDmg = im.calcSpellCritDamage(part.getEventsPerformed());
                    double igniteDmg = im.calcIgniteDamage(part.getEventsPerformed());
                    double igniteMunching = im.calcIngiteMunching(part.getEventsPerformed());

                    double munchingPercent = igniteMunching / (igniteDmg + igniteMunching)*100;
                    NumberFormat nf = NumberFormat.getNumberInstance();
                    nf.setGroupingUsed(false);
                    nf.setMaximumFractionDigits(1);

                    String s = "";
                    s += "Crit Damage = " + nf.format(critDmg) + "\n";
                    s += "Ignite Damage = " + nf.format(igniteDmg) + "\n";
                    s += "Ignite munching = " + nf.format(igniteMunching) + " (" + nf.format(munchingPercent) + ")\n";
                    JOptionPane.showMessageDialog(owner, s, "Ignite muching info", JOptionPane.INFORMATION_MESSAGE);
                }
            });

            receivedHealingPlotItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    LogEvent ev = currentFight.getStartEvent();
                    String time = ev.hour + ":" + ev.minute + ":" + (int) ev.second;
                    FightParticipant part = getParticipantListMerged(t);
                    HealingParticipantsPlotDialog dia = new HealingParticipantsPlotDialog(owner, "Healing received plot vs " + part.getName() + " in fight " + currentFight.getName() + " " + time, false, currentFight, part, basePrefs);
                    dia.pack();
                    dia.setLocationByPlatform(true);
                    dia.setVisible(true);
                }
            });
            openDialogItem.addActionListener(new ActionListener() {                
                public void actionPerformed(ActionEvent e) {
                    FightParticipant part = getParticipantListMerged(t);

                    //ParticipantDialog pd = new ParticipantDialog(owner, true, currentFight, part);
                    ParticipantFrame pd = new ParticipantFrame(owner, currentFight, part, basePrefs);
                    LogEvent ev = currentFight.getStartEvent();
                    String time = ev.hour + ":" + ev.minute + ":" + (int) ev.second;
                    pd.setTitle(part.getName() + " vs " + currentFight.getName() + " " + time);
                    pd.pack();
                    pd.setLocationByPlatform(true);
                    pd.setVisible(true);
                }
            });
            
            detailsFromFilterItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    FightParticipant part = getParticipantListMerged(t);
                    LogicalExpressionInputDialog exprDialog = new LogicalExpressionInputDialog(owner, basePrefs);
                    exprDialog.setLocationRelativeTo(FightsPanel.this);
                    exprDialog.setVisible(true);
                    if (exprDialog.okPressed()) {
                        Filter filter = exprDialog.getFilter();
                        FightParticipant filteredPart = part.filter(filter);
                        ParticipantFrame pd = new ParticipantFrame(owner, currentFight, filteredPart, basePrefs);
                        LogEvent ev = currentFight.getStartEvent();
                        String time = ev.hour + ":" + ev.minute + ":" + (int) ev.second;
                        pd.setTitle(filteredPart.getName() + " vs " + currentFight.getName() + " " + time + " Filtered with: " + exprDialog.getFilterString());
                        pd.pack();
                        pd.setLocationByPlatform(true);
                        pd.setVisible(true);
                    }
                }
            });

            receivedDialogItem.addActionListener(new ActionListener() {                
                public void actionPerformed(ActionEvent e) {
                    FightParticipant part = getParticipantListMerged(t);

                    FightParticipant receivedPart = part.makeReceivedParticipant(currentFight);
                    Fight receivedFight = new Fight(currentFight);
                    receivedFight.setName(part.getName());

                    //ParticipantDialog pd = new ParticipantDialog(owner, true, receivedFight, receivedPart);
                    ParticipantFrame pd = new ParticipantFrame(owner, receivedFight, receivedPart, basePrefs);
                    LogEvent ev = receivedFight.getStartEvent();
                    String time = ev.hour + ":" + ev.minute + ":" + (int) ev.second;
                    pd.setTitle(receivedPart.getName() + " vs " + receivedFight.getName() + " " + time);
                    pd.pack();
                    pd.setLocationByPlatform(true);
                    pd.setVisible(true);
                }
            });

            detailsReceivedFromFilterItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    FightParticipant part = getParticipantListMerged(t);

                    FightParticipant receivedPart = part.makeReceivedParticipant(currentFight);
                    Fight receivedFight = new Fight(currentFight);
                    receivedFight.setName(part.getName());
                    
                    LogicalExpressionInputDialog exprDialog = new LogicalExpressionInputDialog(owner, basePrefs);
                    exprDialog.setLocationRelativeTo(FightsPanel.this);
                    exprDialog.setVisible(true);
                    if (exprDialog.okPressed()) {
                        Filter filter = exprDialog.getFilter();
                        FightParticipant filteredPart = receivedPart.filter(filter);
                        ParticipantFrame pd = new ParticipantFrame(owner, receivedFight, filteredPart, basePrefs);
                        LogEvent ev = receivedFight.getStartEvent();
                        String time = ev.hour + ":" + ev.minute + ":" + (int) ev.second;
                        pd.setTitle(receivedPart.getName() + " vs " + receivedFight.getName() + " " + time + " Filtered with: " + exprDialog.getFilterString());
                        pd.pack();
                        pd.setLocationByPlatform(true);
                        pd.setVisible(true);
                    }
                }
            });

            participantsDamageChartMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    FightParticipant part = getParticipantListMerged(t);
                    
                    JDialog d = new ParticipantDamageChartDialog(owner, false, part);
                    d.setLocationByPlatform(true);
                    d.setVisible(true);
                }
            });

            participantsHealingChartMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    FightParticipant part = getParticipantListMerged(t);
                    
                    JDialog d = new ParticipantHealingChartDialog(owner, false, part);
                    d.setLocationByPlatform(true);
                    d.setVisible(true);
                }
            });

            eventsGraphItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    FightParticipant part = getParticipantListMerged(t);                    
                    EventCollectionSimple allEvents = new EventCollectionSimple();
                    allEvents.addEvents(part.getEventsTotal());
                    allEvents.sort();
                    allEvents.removeEqualElements();
                    JFrame frame = new EventsPlotFrame(currentFight, allEvents, basePrefs);
                    frame.setTitle("Events for "+part.getName()+" vs "+currentFight.getName());
                    frame.setLocationRelativeTo(FightsPanel.this);
                    frame.setVisible(true);
                }
            });

            eventsListItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    FightParticipant part = getParticipantListMerged(t);
                    EventCollectionSimple allEvents = new EventCollectionSimple();
                    allEvents.addEvents(part.getEventsTotal());
                    allEvents.sort();
                    allEvents.removeEqualElements();
                    ShowEventsFrame frame = new ShowEventsFrame("Events for "+part.getName()+" vs "+currentFight.getName(), null, allEvents, new File(owner.file.getAbsolutePath()), basePrefs);
                    frame.setLocationRelativeTo(FightsPanel.this);
                    frame.setVisible(true);
                }
            });

            auraInfoItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Fight f = currentFight;
                    List<FightParticipant> parts = getParticipantList(t);
                    if (parts.size() != 1) {
                        JOptionPane.showMessageDialog(owner, "Can only select one participant", "Alert", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    FightParticipant part = parts.get(0);
                    
                    double min = f.getStartTimeFast();
                    double max = f.getEndTimeFast();
                    TimePeriod tp = new TimePeriod(min, max);
                    List<TimePeriod> tps = new ArrayList<TimePeriod>();
                    tps.add(tp);
                    EventCollection timeSlice = new EventCollectionSimple(owner.fl.getEventCollection().getEvents(tps));
                    ArrayList<Filter> filters = new ArrayList<Filter>();                    
                    filters.add( new FilterGuidOr("apa", part.getSourceGUID()) );
                    //filters.add( new FilterFlagsOr(BasicEvent.FLAGS_ALL, BasicEvent.FLAGS_OBJECT_REACTION_HOSTILE) );
                    EventCollection evs = timeSlice.filterOr(filters);                    
                    ArrayList<Filter> filters2 = new ArrayList<Filter>();
                    filters2.add(new FilterClass(SpellAuraAppliedEvent.class));
                    filters2.add(new FilterClass(SpellAuraRemovedEvent.class));
                    evs = evs.filterOr(filters2);
                    
                    if (evs.size() == 0) {
                        JOptionPane.showMessageDialog(owner, "No Aura applied or removed events to show", "Alert", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    
                    //JDialog d = new AuraInfoDialog(evs.getEvents(), min, max, owner, "Aura information for " + part.getName(), true);
                    JFrame d = new AuraInfoFrame(evs.getEvents(), min, max, owner, "Aura information for " + part.getName(), true);
                    d.setLocationByPlatform(true);
                    d.setVisible(true);
                }
            });
            
            rotationsItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Fight f = currentFight;
                    List<FightParticipant> parts = getParticipantList(t);
                    if (parts.size() != 1) {
                        JOptionPane.showMessageDialog(owner, "Can only select one participant", "Alert", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    FightParticipant part = parts.get(0);
                    RotationDialog d = new RotationDialog(part, owner, true);
                    d.setLocationRelativeTo(owner);
                    d.setVisible(true);
                }
            });
            
            participantsPopupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }
        
    public void doParticipantsNpcPopupMenu(MouseEvent e, final JTable t) {
        JPopupMenu participantsPopupMenu;
        JMenuItem openDialogItem;
        JMenuItem receivedDialogItem;
        JMenuItem participantsDamageChartMenuItem;
        JMenuItem participantsHealingChartMenuItem;
        JMenuItem auraInfoItem;
        JMenuItem eventsGraphItem;
        JMenuItem detailsFromFilterItem;
        JMenuItem detailsReceivedFromFilterItem;

        if (currentFight != null) {
            participantsPopupMenu = new JPopupMenu();
            openDialogItem = new JMenuItem("Show details (Merge)");
            detailsFromFilterItem = new JMenuItem("Show filtered details (Merge)");
            receivedDialogItem = new JMenuItem("Show details on received events (Merge)");
            detailsReceivedFromFilterItem = new JMenuItem("Show filtered details on received events (Merge)");
            participantsDamageChartMenuItem = new JMenuItem("Damage chart (Merge)");
            participantsHealingChartMenuItem = new JMenuItem("Healing chart (Merge)");
            auraInfoItem = new JMenuItem("Aura information with graph (Single)");
            eventsGraphItem = new JMenuItem("Show events in a graph (Merge)");
            participantsPopupMenu.add(openDialogItem);
            participantsPopupMenu.add(detailsFromFilterItem);
            participantsPopupMenu.add(receivedDialogItem);
            participantsPopupMenu.add(detailsReceivedFromFilterItem);
            participantsPopupMenu.add(participantsDamageChartMenuItem);
            participantsPopupMenu.add(participantsHealingChartMenuItem);
            participantsPopupMenu.add(auraInfoItem);
            participantsPopupMenu.add(eventsGraphItem);

            openDialogItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    FightParticipant part = getParticipantListMerged(t);

                    //ParticipantDialog pd = new ParticipantDialog(owner, true, currentFight, part);
                    ParticipantFrame pd = new ParticipantFrame(owner, currentFight, part, basePrefs);
                    LogEvent ev = currentFight.getStartEvent();
                    String time = ev.hour + ":" + ev.minute + ":" + (int) ev.second;
                    pd.setTitle(part.getName() + " vs " + "Various sources" + " " + time);
                    pd.pack();
                    pd.setLocationByPlatform(true);
                    pd.setVisible(true);
                }
            });

            detailsFromFilterItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    FightParticipant part = getParticipantListMerged(t);
                    LogicalExpressionInputDialog exprDialog = new LogicalExpressionInputDialog(owner, basePrefs);
                    exprDialog.setLocationRelativeTo(FightsPanel.this);
                    exprDialog.setVisible(true);
                    if (exprDialog.okPressed()) {
                        Filter filter = exprDialog.getFilter();
                        FightParticipant filteredPart = part.filter(filter);
                        ParticipantFrame pd = new ParticipantFrame(owner, currentFight, filteredPart, basePrefs);
                        LogEvent ev = currentFight.getStartEvent();
                        String time = ev.hour + ":" + ev.minute + ":" + (int) ev.second;
                        pd.setTitle(part.getName() + " vs " + "Various sources" + " " + time + " Filtered with: " + exprDialog.getFilterString());
                        pd.pack();
                        pd.setLocationByPlatform(true);
                        pd.setVisible(true);
                    }
                }
            });

            receivedDialogItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    FightParticipant part = getParticipantListMerged(t);

                    FightParticipant receivedPart = part.makeReceivedParticipant(currentFight);
                    Fight receivedFight = new Fight(currentFight);
                    receivedFight.setName(part.getName());

                    //ParticipantDialog pd = new ParticipantDialog(owner, true, receivedFight, receivedPart);
                    ParticipantFrame pd = new ParticipantFrame(owner, receivedFight, receivedPart, basePrefs);
                    LogEvent ev = receivedFight.getStartEvent();
                    String time = ev.hour + ":" + ev.minute + ":" + (int) ev.second;
                    pd.setTitle(receivedPart.getName() + " vs " + receivedFight.getName() + " " + time);
                    pd.pack();
                    pd.setLocationByPlatform(true);
                    pd.setVisible(true);
                }
            });

            detailsReceivedFromFilterItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    FightParticipant part = getParticipantListMerged(t);

                    FightParticipant receivedPart = part.makeReceivedParticipant(currentFight);
                    Fight receivedFight = new Fight(currentFight);
                    receivedFight.setName(part.getName());

                    LogicalExpressionInputDialog exprDialog = new LogicalExpressionInputDialog(owner, basePrefs);
                    exprDialog.setLocationRelativeTo(FightsPanel.this);
                    exprDialog.setVisible(true);
                    if (exprDialog.okPressed()) {
                        Filter filter = exprDialog.getFilter();
                        FightParticipant filteredPart = receivedPart.filter(filter);
                        ParticipantFrame pd = new ParticipantFrame(owner, receivedFight, filteredPart, basePrefs);
                        LogEvent ev = receivedFight.getStartEvent();
                        String time = ev.hour + ":" + ev.minute + ":" + (int) ev.second;
                        pd.setTitle(receivedPart.getName() + " vs " + receivedFight.getName() + " " + time + " Filtered with: " + exprDialog.getFilterString());
                        pd.pack();
                        pd.setLocationByPlatform(true);
                        pd.setVisible(true);
                    }
                }
            });
            
            participantsDamageChartMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    FightParticipant part = getParticipantListMerged(t);

                    JDialog d = new ParticipantDamageChartDialog(owner, false, part);
                    d.setLocationByPlatform(true);
                    d.setVisible(true);
                }
            });

            participantsHealingChartMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    FightParticipant part = getParticipantListMerged(t);

                    JDialog d = new ParticipantHealingChartDialog(owner, false, part);
                    d.setLocationByPlatform(true);
                    d.setVisible(true);
                }
            });

            eventsGraphItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    FightParticipant part = getParticipantListMerged(t);
                    EventCollectionSimple allEvents = new EventCollectionSimple();
                    allEvents.addEvents(part.getEventsTotal());
                    allEvents.sort();
                    allEvents.removeEqualElements();
                    JFrame frame = new EventsPlotFrame(currentFight, allEvents, basePrefs);
                    frame.setTitle("Events for "+part.getName()+" vs " + "Various sources");
                    frame.setLocationRelativeTo(FightsPanel.this);
                    frame.setVisible(true);
                }
            });

            auraInfoItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Fight f = currentFight;
                    List<FightParticipant> parts = getParticipantList(t);
                    if (parts.size() != 1) {
                        JOptionPane.showMessageDialog(owner, "Can only select one participant", "Alert", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    FightParticipant part = parts.get(0);

                    double min = f.getStartTimeFast();
                    double max = f.getEndTimeFast();
                    TimePeriod tp = new TimePeriod(min, max);
                    List<TimePeriod> tps = new ArrayList<TimePeriod>();
                    tps.add(tp);
                    EventCollection timeSlice = new EventCollectionSimple(owner.fl.getEventCollection().getEvents(tps));
                    ArrayList<Filter> filters = new ArrayList<Filter>();
                    filters.add( new FilterGuidOr("apa", part.getSourceGUID()) );
                    //filters.add( new FilterFlagsOr(BasicEvent.FLAGS_ALL, BasicEvent.FLAGS_OBJECT_REACTION_HOSTILE) );
                    EventCollection evs = timeSlice.filterOr(filters);
                    ArrayList<Filter> filters2 = new ArrayList<Filter>();
                    filters2.add(new FilterClass(SpellAuraAppliedEvent.class));
                    filters2.add(new FilterClass(SpellAuraRemovedEvent.class));
                    evs = evs.filterOr(filters2);

                    if (evs.size() == 0) {
                        JOptionPane.showMessageDialog(owner, "No Aura applied or removed events to show", "Alert", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }

                    //JDialog d = new AuraInfoDialog(evs.getEvents(), min, max, owner, "Aura information for " + part.getName(), true);
                    JFrame d = new AuraInfoFrame(evs.getEvents(), min, max, owner, "Aura information for " + part.getName(), true);
                    d.setLocationByPlatform(true);
                    d.setVisible(true);
                }
            });

            participantsPopupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    public void fightRowClicked(Fight fi) {
//        System.out.println("Total memory = " + Runtime.getRuntime().totalMemory());
//        System.out.println("Free memory = " + Runtime.getRuntime().freeMemory());
//        System.out.println("Used memory = " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
        Fight f = new Fight(fi);
        f.removeAssignedPets();
        NumberFormat nf = WLPNumberFormat.getInstance();
        nf.setMaximumFractionDigits(1);
        nf.setGroupingUsed(false);
        double duration = f.getEndTime() - f.getStartTime();
        long totalDamage = 0;
        for (FightParticipant fp : f.getParticipants()) {
            //totalDamage += fp.totalDamage(BasicEvent.SCHOOL_ALL).amount;
            totalDamage += fp.totalDamage(Constants.SCHOOL_ALL, FightParticipant.TYPE_ALL_DAMAGE).amount;
        }
        long totalSpellDamage = 0;
        for (FightParticipant fp : f.getParticipants()) {
            //totalSpellDamage += fp.totalSpellDamage(BasicEvent.SCHOOL_ALL).amount;
            totalSpellDamage += fp.totalDamage(Constants.SCHOOL_ALL, FightParticipant.TYPE_SPELL_DAMAGE).amount;
        }
        long totalMeleeDamage = 0;
        for (FightParticipant fp : f.getParticipants()) {
            //totalMeleeDamage += fp.totalMeleeDamage(BasicEvent.SCHOOL_ALL).amount;
            totalMeleeDamage += fp.totalDamage(Constants.SCHOOL_ALL, FightParticipant.TYPE_SWING_DAMAGE).amount;
        }
        long totalRangedDamage = 0;
        for (FightParticipant fp : f.getParticipants()) {
            //totalRangedDamage += fp.totalRangedDamage(BasicEvent.SCHOOL_ALL).amount;
            totalRangedDamage += fp.totalDamage(Constants.SCHOOL_ALL, FightParticipant.TYPE_RANGED_DAMAGE).amount;
        }
        long totalHealing = 0;
        for (FightParticipant fp : f.getParticipants()) {
            totalHealing += fp.totalHealing(Constants.SCHOOL_ALL, FightParticipant.TYPE_ALL_HEALING).amount;
        }
        String info = "";
        info += f.getName() + "\n";
        info += "GUID = " + f.getGuid() + "\n";
        info += "Mob ID = " + f.getMobID() + "\n\n";

        if (f.isMerged()) {
            if (f.isDead()) {
                info += "Killed\n";
            } else {
                info += "Not killed\n";
            }
            info += "Merged fight duration = " + nf.format(duration) + "\n";
        } else {
            if (f.isDead()) {
                info += "Killed\n";
            } else {
                info += "Not killed\n";
            }
            info += "Fight duration = " + nf.format(duration) + "\n";
        }
        info += "Total Damage = " + totalDamage + "\n";
        info += "Total DPS = " + nf.format((double) totalDamage / duration) + "\n";
        info += "Total Spell Damage = " + totalSpellDamage + "\n";
        info += "Total Spell DPS = " + nf.format((double) totalSpellDamage / duration) + "\n";
        info += "Total Melee Damage = " + totalMeleeDamage + "\n";
        info += "Total Melee DPS = " + nf.format((double) totalMeleeDamage / duration) + "\n";
        info += "Total Ranged Damage = " + totalRangedDamage + "\n";
        info += "Total Ranged DPS = " + nf.format((double) totalRangedDamage / duration) + "\n";
        info += "Total Healing = " + totalHealing + "\n";
        info += "Total HPS = " + nf.format((double) totalHealing / duration) + "\n";
        info += "\n";
        if (f.isMerged()) {
            info += "Fights merged from the following mobs:\n";
            List<Fight> mergedSourceFights = f.getMergedSourceFights();
            for (Fight mf : mergedSourceFights) {
                info += mf.getName() + " , GUID: " + mf.getGuid() + "\n";
            }
        }
        if (f.getVictimPets().size() > 0 && SettingsSingleton.getInstance().getPetsAsOwner()) {
            info += "Pets merged into the fight:\n";
            List<PetInfo> petInfo = f.getVictimPets();
            for (PetInfo pi : petInfo) {
                info += pi.getName() + " , GUID: " + pi.getSourceGUID() + " Dead: " + pi.isDead() + "\n";
            }
        }
        jTextArea1.setText(info);

        jTextArea1.setCaretPosition(0);
        modelDamage.setFight(f);
        sortModelDamage.sort(1);
        modelHealing.setFight(f);
        sortModelHealing.sort(1);
        modelReceivedDamage.setFight(f);
        sortModelReceivedDamage.sort(1);
        modelReceivedHealing.setFight(f);
        sortModelReceivedHealing.sort(1);
        modelDamageEnemy.setFight(f);
        sortModelDamageEnemy.sort(1);
        TableHelper.setAutomaticColumnWidth(jTableDamage, 5);
        TableHelper.setAutomaticColumnWidth(jTableHealing, 5);
        TableHelper.setAutomaticColumnWidth(jTableReceivedDamage, 5);
        TableHelper.setAutomaticColumnWidth(jTableReceivedHealing, 5);
        TableHelper.setAutomaticColumnWidth(jTableDamageNpcs, 5);
        setCurrentFight(f);
    }

    public void setCurrentFight(Fight f) {
        currentFight = f;
        owner.setFightText(currentFight.getName());
    }
    
    public void clearFights() {
        model.clearFights();
        modelDamage.clearFights();
        modelHealing.clearFights();
        modelReceivedDamage.clearFights();
        modelReceivedHealing.clearFights();
        modelDamageEnemy.clearFights();

        fights = new ArrayList<Fight>();
    }
    
    public void addFight(Fight f) {
        fights.add(f);
        model.addFight(f.getName(), 
                ""+f.getEvents().get(0).getTimeString(), 
                Integer.toString((int)(f.getEndTime() - f.getStartTime())) + " s");
    }

    public void setFights(List<Fight> fs) {
        clearFights();
        for (Fight f : fs) {
            addFight(f);
        }
    }
    
    public List<Fight> getFights() {
        return fights;
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
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jSplitPane2 = new javax.swing.JSplitPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTableDamage = new javax.swing.JTable();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTableHealing = new javax.swing.JTable();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTableReceivedDamage = new javax.swing.JTable();
        jScrollPane6 = new javax.swing.JScrollPane();
        jTableReceivedHealing = new javax.swing.JTable();
        jScrollPane7 = new javax.swing.JScrollPane();
        jTableDamageNpcs = new javax.swing.JTable();

        setLayout(new java.awt.BorderLayout());

        jSplitPane1.setDividerLocation(250);

        jPanel1.setLayout(new java.awt.BorderLayout());

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
        jTable1.setMinimumSize(new java.awt.Dimension(200, 64));
        jScrollPane1.setViewportView(jTable1);

        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jSplitPane1.setLeftComponent(jPanel1);
        jPanel1.getAccessibleContext().setAccessibleParent(jSplitPane1);

        jPanel2.setLayout(new java.awt.BorderLayout());

        jSplitPane2.setDividerLocation(150);
        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane3.setViewportView(jTextArea1);

        jSplitPane2.setLeftComponent(jScrollPane3);

        jTableDamage.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane2.setViewportView(jTableDamage);

        jTabbedPane1.addTab("Damage", jScrollPane2);

        jTableHealing.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane4.setViewportView(jTableHealing);

        jTabbedPane1.addTab("Healing", jScrollPane4);

        jTableReceivedDamage.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane5.setViewportView(jTableReceivedDamage);

        jTabbedPane1.addTab("Received Damage", jScrollPane5);

        jTableReceivedHealing.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane6.setViewportView(jTableReceivedHealing);

        jTabbedPane1.addTab("Received Healing", jScrollPane6);

        jTableDamageNpcs.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane7.setViewportView(jTableDamageNpcs);

        jTabbedPane1.addTab("Enemy Npcs", jScrollPane7);

        jSplitPane2.setRightComponent(jTabbedPane1);

        jPanel2.add(jSplitPane2, java.awt.BorderLayout.CENTER);

        jSplitPane1.setRightComponent(jPanel2);
        jPanel2.getAccessibleContext().setAccessibleParent(jSplitPane1);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTableDamage;
    private javax.swing.JTable jTableDamageNpcs;
    private javax.swing.JTable jTableHealing;
    private javax.swing.JTable jTableReceivedDamage;
    private javax.swing.JTable jTableReceivedHealing;
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration//GEN-END:variables
    
}
