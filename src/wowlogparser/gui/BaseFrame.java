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

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.ScrollPane;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.StandardChartTheme;
import wowlogparserarena.gui.ArenaBaseFrame;
import wowlogparserbase.*;
import wowlogparserbase.helpers.*;
import wowlogparserbase.events.*;
import wowlogparserbase.xml.ReportJaxb;

/**
 * The main frame window for the application
 * @author  racy
 */
public class BaseFrame extends javax.swing.JFrame {

    static BaseFrame frame;
    FightsPanel fightsPanel = new FightsPanel(this);
    List<FightsPanel> fightPanelsExtra = new ArrayList<FightsPanel>();
    StatusBar statusBar = new StatusBar();
    public FileLoader fl = null;
    public File file = null;

    public Preferences basePrefs = Preferences.userRoot().node("wowlogparser");
    public boolean apa;
            
    SettingsDialog settingsDialog;
    
    /** Creates new form BaseFrame */
    public BaseFrame() {
        ToolTipManager.sharedInstance().setInitialDelay(500);
        initComponents();
        ScrollPane sp = new ScrollPane();
        jTabbedPane1.addTab("Fights", fightsPanel);
        settingsDialog = new SettingsDialog(this, "Settings", true, basePrefs);
        ChartFactory.setChartTheme(StandardChartTheme.createLegacyTheme());
        try {
            InputStream iconStream = getClass().getClassLoader().getResourceAsStream("wowlogparserbase/extrafiles/icons/logicon.png");
            setIconImage(ImageIO.read(iconStream));
        } catch(IOException ex) {            
        }
        getContentPane().add(statusBar, BorderLayout.SOUTH);
        statusBar.setMessage("Ready");
        jTabbedPane1.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("tabclosed".equalsIgnoreCase(evt.getPropertyName())) {
                    fightPanelsExtra.remove((FightsPanel)evt.getNewValue());
                }
            }
        });
    }

    public void newFightsPanel(String name, List<Fight> fights) {
        FightsPanel p = new FightsPanel(this);
        p.setFights(fights);
        jTabbedPane1.addTab(name, p);
        jTabbedPane1.setSelectedComponent(p);
    }
    
    public void setFightText(String text) {
        jTextFieldCurrentFight.setText(text);
    }

    public void setStatusText() {
        if ((fl != null) && (file != null)) {
            EventCollectionSimple ec = fl.getEventCollection();
            if (ec.size() > 0) {
                BasicEvent startEv = ec.getEvent(0);
                BasicEvent endEv = ec.getEvent(ec.size()-1);
                int startMonth = startEv.month;
                int startDay = startEv.day;
                int endMonth = endEv.month;
                int endDay = endEv.day;

                //GregorianCalendar now = new GregorianCalendar();
                //int year = now.get(GregorianCalendar.YEAR);
                GregorianCalendar fileDate = new GregorianCalendar();
                fileDate.setTimeInMillis(file.lastModified());
                int year = fileDate.get(GregorianCalendar.YEAR);

                GregorianCalendar calStart = new GregorianCalendar(year, startMonth-1, startDay);
                GregorianCalendar calEnd = new GregorianCalendar(year, endMonth-1, endDay);
                if ((startDay == endDay) && (startMonth == endMonth)) {
                    String message = "Combat log for date: " +
                            calStart.getDisplayName(GregorianCalendar.MONTH, GregorianCalendar.LONG, Locale.ENGLISH) + " " +
                            calStart.get(GregorianCalendar.DAY_OF_MONTH);
                    message = message + ". File last modified: " + DateFormat.getDateInstance(DateFormat.SHORT).format(fileDate.getTime());
                    statusBar.setMessage(message);
                } else {
                    String message = "Combat log for dates: " +
                            calStart.getDisplayName(GregorianCalendar.MONTH, GregorianCalendar.LONG, Locale.ENGLISH) + " " +
                            calStart.get(GregorianCalendar.DAY_OF_MONTH) + " - " +
                            calEnd.getDisplayName(GregorianCalendar.MONTH, GregorianCalendar.LONG, Locale.ENGLISH) + " " +
                            calEnd.get(GregorianCalendar.DAY_OF_MONTH);
                    message = message + ". File last modified: " + DateFormat.getDateInstance(DateFormat.SHORT).format(fileDate.getTime());
                    statusBar.setMessage(message);
                }
            }
        }
        else {
            statusBar.setMessage("Ready");
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jTextFieldCurrentFight = new javax.swing.JTextField();
        jLabelCurrentFight = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabelHint1 = new javax.swing.JLabel();
        jLabelHint2 = new javax.swing.JLabel();
        jLabelHint3 = new javax.swing.JLabel();
        jTabbedPane1 = new wowlogparser.gui.JTabbedPaneWithCloseIcons();
        jMenuBar2 = new javax.swing.JMenuBar();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItemOpenDates = new javax.swing.JMenuItem();
        jMenuItem10 = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JSeparator();
        jMenuItem9 = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenuItem6 = new javax.swing.JMenuItem();
        jMenuItem7 = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        jMenuItem8 = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        jMenuItemArena = new javax.swing.JMenuItem();
        jMenu5 = new javax.swing.JMenu();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItemMemoryInfo = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                BaseFrame.this.windowClosing(evt);
            }
        });

        jPanel2.setLayout(new java.awt.BorderLayout());

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel1.setLayout(new java.awt.GridBagLayout());

        jButton1.setText("Reparse");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanel1.add(jButton1, gridBagConstraints);

        jTextFieldCurrentFight.setColumns(30);
        jTextFieldCurrentFight.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        jPanel1.add(jTextFieldCurrentFight, gridBagConstraints);

        jLabelCurrentFight.setLabelFor(jTextFieldCurrentFight);
        jLabelCurrentFight.setText("Current fight");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        jPanel1.add(jLabelCurrentFight, gridBagConstraints);

        jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.Y_AXIS));

        jLabelHint1.setText("Hint: Select participants or fights and right click to get a popup menu");
        jPanel3.add(jLabelHint1);

        jLabelHint2.setText("Hint: Double click a fight participant to see details");
        jPanel3.add(jLabelHint2);

        jLabelHint3.setText("Hint: Go to Tools->Settings to set WoW version etc.");
        jPanel3.add(jLabelHint3);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 20, 0, 20);
        jPanel1.add(jPanel3, gridBagConstraints);

        jPanel2.add(jPanel1, java.awt.BorderLayout.NORTH);

        jTabbedPane1.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        jTabbedPane1.setPreferredSize(new java.awt.Dimension(1024, 800));
        jPanel2.add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);

        jMenu3.setText("File");

        jMenuItem1.setLabel("Open");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileOpen(evt);
            }
        });
        jMenu3.add(jMenuItem1);

        jMenuItemOpenDates.setText("Open and select dates");
        jMenuItemOpenDates.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileOpenDates(evt);
            }
        });
        jMenu3.add(jMenuItemOpenDates);

        jMenuItem10.setText("Open and make a total fight only");
        jMenuItem10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileOpenTotal(evt);
            }
        });
        jMenu3.add(jMenuItem10);
        jMenu3.add(jSeparator4);

        jMenuItem9.setText("Make package for web application");
        jMenuItem9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                makeWebPackage(evt);
            }
        });
        jMenu3.add(jMenuItem9);
        jMenu3.add(jSeparator3);

        jMenuItem2.setText("Exit");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileExit(evt);
            }
        });
        jMenu3.add(jMenuItem2);

        jMenuBar2.add(jMenu3);

        jMenu4.setText("Tools");

        jMenuItem5.setText("Assign pets(requires loaded file)");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                assignPets(evt);
            }
        });
        jMenu4.add(jMenuItem5);

        jMenuItem6.setText("Make boss fights(requires loaded file)");
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                makeBossFights(evt);
            }
        });
        jMenu4.add(jMenuItem6);

        jMenuItem7.setText("Make XML, boss fights + merged individual");
        jMenuItem7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                makeXML(evt);
            }
        });
        jMenu4.add(jMenuItem7);
        jMenu4.add(jSeparator1);

        jMenuItem8.setText("Settings");
        jMenuItem8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toolsSettings(evt);
            }
        });
        jMenu4.add(jMenuItem8);
        jMenu4.add(jSeparator2);

        jMenuItemArena.setText("Arena Mode");
        jMenuItemArena.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemArenaActionPerformed(evt);
            }
        });
        jMenu4.add(jMenuItemArena);

        jMenuBar2.add(jMenu4);

        jMenu5.setText("Help");

        jMenuItem4.setText("Info about the parsing");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpInfo(evt);
            }
        });
        jMenu5.add(jMenuItem4);

        jMenuItemMemoryInfo.setText("Memory Info");
        jMenuItemMemoryInfo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemMemoryInfoActionPerformed(evt);
            }
        });
        jMenu5.add(jMenuItemMemoryInfo);

        jMenuItem3.setText("About");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpAbout(evt);
            }
        });
        jMenu5.add(jMenuItem3);

        jMenuBar2.add(jMenu5);

        setJMenuBar(jMenuBar2);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void fileOpen(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileOpen
        String dirString = basePrefs.get("fileDir", "");
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File(dirString));
        fc.setPreferredSize(new Dimension(600, 400));

        int retVal = fc.showOpenDialog(this);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            file = fc.getSelectedFile();
            basePrefs.put("fileDir", file.getAbsoluteFile().getParent());
            title(file.getName());
            parse();
        }
    }//GEN-LAST:event_fileOpen

    public void title(String text) {
        String s = "WowLogParser " + Version.getVersion() + " by Dracy on Dragonblight(EU) - " + text;
        setTitle(s);
    }
    
    private void fileExit(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileExit
        System.exit(0);
    }//GEN-LAST:event_fileExit

    private void helpAbout(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpAbout
        JDialog about = new JDialog(this, "About");
        about.setModal(true);
        about.getContentPane().add(new AboutPanel());
        about.pack();
        about.setLocationRelativeTo(this);
        about.setVisible(true);
    }//GEN-LAST:event_helpAbout

    private void helpInfo(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpInfo
        InfoDialog d = new InfoDialog(this, "Info about the parser", true);
        d.pack();
        d.setLocationByPlatform(true);
        d.setVisible(true);
    }//GEN-LAST:event_helpInfo

    private void windowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_windowClosing
    }//GEN-LAST:event_windowClosing

    private void assignPets(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_assignPets
        if (fl!=null) {
            PetDialog pd = new PetDialog(this, "Assign pets", true, fightsPanel.getFights(), fl);
            pd.pack();
            pd.setLocationByPlatform(true);
            pd.setVisible(true);
        }
    }//GEN-LAST:event_assignPets

private void makeBossFights(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_makeBossFights
    if (fl == null) {
        return;
    }
    BossDialog bd = new BossDialog(this, "Make boss fights", true, fl.getFightCollection().getFights(), fl);
    bd.setLocationByPlatform(true);
    bd.setVisible(true);
    List<Fight> outFights = bd.getOutFights();
    if (outFights != null) {
        newFightsPanel("Boss fights", outFights);
    }
    bd.dispose();
}//GEN-LAST:event_makeBossFights

private void makeXML(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_makeXML

    String dirString = basePrefs.get("xmlDir", "");
    JFileChooser fc = new JFileChooser(new File(dirString));
    fc.setDialogTitle("Select directory (Files in the directory may be overwritten)");
    fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    int retVal = fc.showDialog(this, "Choose");

    int index = 0;
    if (retVal == JFileChooser.APPROVE_OPTION) {
        File dir = fc.getSelectedFile();
        basePrefs.put("xmlDir", dir.getAbsolutePath());

        ReportJaxb report = new ReportJaxb(dir);
        report.setFileNumberIndex(index);

        BossParsing bp = new BossParsing(this, fl.getFightCollection().getFights(), fl);
        bp.doAutomaticBossParsing();
        List<Fight> bossFights = bp.getOutFights();
        try {
            report.makeBossFights(fl, bossFights);
            bossFights = null;
        } catch(IOException ex) {
            JOptionPane.showMessageDialog(this, "Error writing to file", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        List<Fight> indMergedFights = Fight.mergeEqualFights(fl.getFightCollection().getFights(), "");
        try {
            report.makeIndividuallyMergedFights(fl, indMergedFights);
            indMergedFights = null;
        } catch(IOException ex) {
            JOptionPane.showMessageDialog(this, "Error writing to file", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Fight allMergedFights = Fight.merge(fl.getFightCollection().getFights());
        try {
            report.makeFight(fl, allMergedFights);
            allMergedFights = null;
        } catch(IOException ex) {
            JOptionPane.showMessageDialog(this, "Error writing to file", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    }
    
}//GEN-LAST:event_makeXML

private void toolsSettings(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toolsSettings
    settingsDialog.setLocationRelativeTo(this);
    settingsDialog.setVisible(true);
}//GEN-LAST:event_toolsSettings

private void fileOpenTotal(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileOpenTotal
        String dirString = basePrefs.get("fileDir", "");
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File(dirString));

        int retVal = fc.showOpenDialog(this);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            file = fc.getSelectedFile();
            basePrefs.put("fileDir", file.getAbsolutePath());
            title(file.getName());
            parseTotal();
        }
}//GEN-LAST:event_fileOpenTotal

private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
parse();
}//GEN-LAST:event_jButton1ActionPerformed

private void jMenuItemArenaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemArenaActionPerformed
    JFrame f = new ArenaBaseFrame();
    f.setLocationByPlatform(true);
    f.setVisible(true);
    dispose();
}//GEN-LAST:event_jMenuItemArenaActionPerformed

private void fileOpenDates(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileOpenDates
    String dirString = basePrefs.get("fileDir", "");
    JFileChooser fc = new JFileChooser();
    fc.setCurrentDirectory(new File(dirString));
    fc.setPreferredSize(new Dimension(600, 400));

    int retVal = fc.showOpenDialog(this);
    if (retVal == JFileChooser.APPROVE_OPTION) {
        file = fc.getSelectedFile();
        basePrefs.put("fileDir", file.getAbsoluteFile().getParent());
        title(file.getName());
        parseDates();
    }
}//GEN-LAST:event_fileOpenDates

private void jMenuItemMemoryInfoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemMemoryInfoActionPerformed
    String s = "Current JVM size = " + Math.round(Runtime.getRuntime().totalMemory() / (1024.0*1024.0)) + " MB";
    s += "\n";
    s += "Max JVM size = " + Math.round(Runtime.getRuntime().maxMemory()/(1024.0*1024.0)) + " MB";
    s += "\n";
    s += "Memory Used = " + Math.round((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) /(1024.0*1024.0)) + " MB";
    JOptionPane.showMessageDialog(this, s, "Memory info", JOptionPane.INFORMATION_MESSAGE);
}//GEN-LAST:event_jMenuItemMemoryInfoActionPerformed

private void makeWebPackage(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_makeWebPackage
    wowlogparser.webgui.BaseFrame fr = new wowlogparser.webgui.BaseFrame();
    fr.setLocationRelativeTo(this);
    fr.setVisible(true);
}//GEN-LAST:event_makeWebPackage
//  Arena mode
//    wowlogparserarena.gui.ArenaBaseFrame frame = new wowlogparserarena.gui.ArenaBaseFrame();
//    frame.setLocationByPlatform(true);
//    frame.setVisible(true);
//    dispose();
    

    void clear() {
        fightsPanel.clearFights();
        fightsPanel = new FightsPanel(this);
        fightPanelsExtra.clear();
        jTabbedPane1.removeAll();
        jTabbedPane1.add("Fights", fightsPanel);
        fl = null;
        System.gc();
    }

    void parse() {
        clear();

        FileParseThread p = new FileParseThread();
        WorkerDialog d = new WorkerDialog(this, "Parsing file", true, p, "Please wait, parsing file...");
        d.setLocationRelativeTo(getContentPane());
        d.setVisible(true);
        if (fl == null) {
            return;
        }
        if (fl.getNumErrors() > 0) {
            JOptionPane.showMessageDialog(this, "Error parsing " + fl.getNumErrors() + " lines, consider changing WoW version in Tools->Settings menu.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        d.dispose();
        List<Fight> fights = fl.getFightCollection().getFights();
        updateTables(fights);
        if (SettingsSingleton.getInstance().getAutoBossFights()) {
            BossParsing bp = new BossParsing(this, fl.getFightCollection().getFights(), fl);
            bp.doAutomaticBossParsing();
            List<Fight> bossFights = bp.getOutFights();
            newFightsPanel("Boss Fights", bossFights);
        }
        setStatusText();
        System.gc();
    }
        
    void parseDates() {
        clear();

        FilePeekThread peekThread = new FilePeekThread();
        WorkerDialog peekD = new WorkerDialog(this, "Scanning file for dates", true, peekThread, "Please wait, scanning file for dates...");
        peekD.setLocationRelativeTo(getContentPane());
        peekD.setVisible(true);
        FilePeeker peeker = peekThread.getFilePeeker();
        if (peeker == null) {
            JOptionPane.showMessageDialog(this, "Error when scanning the file", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Set<WlpDate> dates = peeker.getDates();
        SelectDatesDialog dia = new SelectDatesDialog(dates, this, "Select the dates to use", true);
        dia.setLocationRelativeTo(this);
        dia.setVisible(true);
        dates = dia.getDates();
        List<Integer> rows = peeker.getRows(dates);
        
        FileParseThreadRows p = new FileParseThreadRows(rows);
        WorkerDialog d = new WorkerDialog(this, "Parsing file", true, p, "Please wait, parsing file...");
        d.setLocationRelativeTo(getContentPane());
        d.setVisible(true);
        if (fl == null) {
            return;
        }
        if (fl.getNumErrors() > 0) {
            JOptionPane.showMessageDialog(this, "Error parsing " + fl.getNumErrors() + " lines, consider changing WoW version in Tools->Settings menu.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        d.dispose();
        List<Fight> fights = fl.getFightCollection().getFights();
        updateTables(fights);
        if (SettingsSingleton.getInstance().getAutoBossFights()) {
            BossParsing bp = new BossParsing(this, fl.getFightCollection().getFights(), fl);
            bp.doAutomaticBossParsing();
            List<Fight> bossFights = bp.getOutFights();
            newFightsPanel("Boss Fights", bossFights);
        }
        setStatusText();
        System.gc();
    }

    void parseTotal() {
        clear();
        long t = System.currentTimeMillis();
        FileParseThreadTotal p = new FileParseThreadTotal();
        WorkerDialog d = new WorkerDialog(this, "Parsing file", true, p, "Please wait, parsing file...");
        d.setLocationRelativeTo(getContentPane());
        d.setVisible(true);
        if (fl.getNumErrors() > 0) {
            JOptionPane.showMessageDialog(this, "Error parsing " + fl.getNumErrors() + " lines, consider changing WoW version in Tools->Settings menu.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        d.dispose();
        List<Fight> fights = fl.getFightCollection().getFights();
        System.out.println(System.currentTimeMillis() - t);
        updateTables(fights);
        setStatusText();
    }

    public class FileParseThread extends Worker {

        public FileParseThread() {
        }

        @Override
        public void run() {
            Cursor c = getCursor();
            try {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                if (file != null) {
                    fl = new FileLoader(file);
                    boolean ret = fl.parse();
                    if (ret) {
                        fl.createFights();
                        EventCollection ec = fl.getEventCollection();
                        AbstractEventCollection.makeOverhealing(ec.getEvents(), 60);
                    } else {
                        JOptionPane.showMessageDialog(BaseFrame.this, "Error when parsing, File not found?", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } finally {
                setCursor(c);
                hideDialog();
            }
        }
        
    }
    
    public class FilePeekThread extends Worker {

        FilePeeker fp = null;
        
        public FilePeekThread() {
        }

        @Override
        public void run() {
            Cursor c = getCursor();            
            try {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                if (file != null) {
                    fp = new FilePeeker(file);
                    boolean ret = fp.parse();
                    if (ret) {
                    } else {
                        fp = null;
                        JOptionPane.showMessageDialog(BaseFrame.this, "Error when parsing, File not found?", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } finally {
                setCursor(c);
                hideDialog();
            }
        }

        public FilePeeker getFilePeeker() {
            return fp;
        }
        
    }

    public class FileParseThreadRows extends Worker {

        List<Integer> rows;
        public FileParseThreadRows(List<Integer> rows) {
            this.rows = rows;
        }

        @Override
        public void run() {
            Cursor c = getCursor();            
            try {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                if (file != null) {
                    fl = new FileLoader(file);
                    boolean ret = fl.parse(rows);
                    if (ret) {
                        fl.createFights();
                        EventCollection ec = fl.getEventCollection();
                        AbstractEventCollection.makeOverhealing(ec.getEvents(), 60);
                    } else {
                        JOptionPane.showMessageDialog(BaseFrame.this, "Error when parsing, File not found?", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } finally {
                setCursor(c);
                hideDialog();
            }
        }
        
    }

    public class FileParseThreadTotal extends Worker {

        public FileParseThreadTotal() {
        }

        @Override
        public void run() {
            Cursor c = getCursor();
            try {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                if (file != null) {
                    fl = new FileLoader(file);
                    boolean ret = fl.parse();
                    if (ret) {
                        fl.createTotalFight();
                    } else {
                        JOptionPane.showMessageDialog(BaseFrame.this, "Error when parsing, File not found?", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
                EventCollection ec = fl.getEventCollection();
                AbstractEventCollection.makeOverhealing(ec.getEvents(), 60);
            } finally {
                setCursor(c);
                hideDialog();
            }
        }
        
    }

    public void updateTables(List<Fight> fights) {
        fightsPanel.clearFights();
        //Update fights
        for (Fight f : fights) {            
            if (SettingsSingleton.getInstance().getOnlyDeadMobs()) {
                if (f.isDead()) {
                    fightsPanel.addFight(f);
                }
            } else {
                fightsPanel.addFight(f);
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        final String[] args2 = args;
        Runnable mainThread = new Runnable() {

            public void run() {
                int guiType = 0;
                for (String s : args2) {
                    if (s.trim().equalsIgnoreCase("--syslf")) {
                        guiType = 1;
                    }
                    if (s.trim().equalsIgnoreCase("--nimbuslf")) {
                        guiType = 2;
                    }
                    if (s.trim().equalsIgnoreCase("--defaultlf")) {
                        guiType = 3;
                    }
                }
                switch(guiType) {
                    case 0:
                        try {
                            UIManager.setLookAndFeel(
                                    UIManager.getSystemLookAndFeelClassName());
                        } catch (Exception e) {
                            System.out.println("Unable to load native look and feel");
                        }
                        try {
                            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
                        } catch (Exception e) {
                            System.out.println("Unable to load nimbus look and feel");
                        }
                        break;
                    case 1:
                        try {
                            UIManager.setLookAndFeel(
                                    UIManager.getSystemLookAndFeelClassName());
                        } catch (Exception e) {
                            System.out.println("Unable to load native look and feel");
                        }
                        break;
                    case 2:
                        try {
                            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
                        } catch (Exception e) {
                            System.out.println("Unable to load nimbus look and feel");
                        }
                        break;
                    case 3:
                        break;
                }
                frame = new BaseFrame();
                frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
                frame.setLocationByPlatform(true);
                frame.title("");
                frame.setVisible(true);
            }
        };
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread t, Throwable e) {
                String stackTrace = "";
                for (StackTraceElement s : e.getStackTrace()) {
                    stackTrace += s.toString() + "\n";
                }
                JOptionPane.showMessageDialog(null, "Unknown error. \n" + e.getMessage() + "\n" + stackTrace, "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        java.awt.EventQueue.invokeLater(mainThread);
    }    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabelCurrentFight;
    private javax.swing.JLabel jLabelHint1;
    private javax.swing.JLabel jLabelHint2;
    private javax.swing.JLabel jLabelHint3;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenuBar jMenuBar2;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem10;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JMenuItem jMenuItem9;
    private javax.swing.JMenuItem jMenuItemArena;
    private javax.swing.JMenuItem jMenuItemMemoryInfo;
    private javax.swing.JMenuItem jMenuItemOpenDates;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private wowlogparser.gui.JTabbedPaneWithCloseIcons jTabbedPane1;
    private javax.swing.JTextField jTextFieldCurrentFight;
    // End of variables declaration//GEN-END:variables
}

