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

import wowlogparserbase.tablerendering.StringColor;
import wowlogparserbase.tablerendering.DefaultTableCellRendererExtra;
import wowlogparserbase.helpers.TableHelper;
import wowlogparserbase.events.SkillInterface;
import wowlogparserbase.events.LogEvent;
import wowlogparserbase.events.BasicEvent;
import wowlogparserbase.events.SkillInterfaceExtra;
import wowlogparserbase.TimePeriod;
import wowlogparserbase.EventCollection;
import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import wowlogparser.*;
import wowlogparserbase.EventCollectionSimple;
import wowlogparserbase.eventfilter.Filter;
import wowlogparserbase.events.damage.DamageMissedEvent;
import wowlogparserbase.helpers.FileHelper;
import wowlogparserbase.logical.FilterSyntaxParser;
import wowlogparserbase.logical.SyntaxException;

/**
 *
 * @author  racy
 */
public class ShowEventsPanel extends javax.swing.JPanel {
    ShowEventsCallback callback;
    List<BasicEvent> allEvents = new ArrayList<BasicEvent>();
    List<BasicEvent> filterEvents = new ArrayList<BasicEvent>();

    String sourceFilter = "";
    String destinationFilter = "";
    String allFilter = "";
    
    File inFile = null;
    Preferences basePrefs;
    
    /** Creates new form ShowEventsPanel */
    public ShowEventsPanel(List<TimePeriod> tps, EventCollection ec, File inFile, Preferences basePrefs) {
        this(tps, ec, new ShowEventsCallback() {
            public void showEventsSaveLogLines(int[] rows) {                
            }
        }, basePrefs);
        
        this.inFile = inFile;
    }
    
    /** Creates new form ShowEventsPanel */
    public ShowEventsPanel(List<TimePeriod> tps, EventCollection ec, ShowEventsCallback cb, Preferences basePrefs) {
        initComponents();
        this.basePrefs = basePrefs.node("ShowEventsPanel");
        this.callback = cb;
        //allEvents.addAll(fight.getEvents());
        //allEvents.addAll(fight.getNonFightDamageHealingEvents());
        List<TimePeriod> tpsMerged;
        if (tps != null) {
            tpsMerged= TimePeriod.mergeTimePeriods(tps);
        } else {
            tpsMerged = new ArrayList<TimePeriod>();
            tpsMerged.add(new TimePeriod(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
        }
        allEvents.addAll(ec.getEvents(tpsMerged));
        Collections.sort(allEvents);
        filterEvents.addAll(allEvents);
        jTable1.setDefaultRenderer(StringColor.class, new DefaultTableCellRendererExtra());
        jTable1.setModel(new ShowEventsTableModel(filterEvents));
        TableHelper.setAutomaticColumnWidth(jTable1, 5);
        jTable1.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                if (e.isPopupTrigger()) {
                    showPopupMenu(e);
                }
                if (e.getButton() == e.BUTTON1) {
                    if (e.getClickCount() == 2) {
                        int row = jTable1.getSelectedRow();
                        BasicEvent ev = (BasicEvent)filterEvents.get(row);
                        JDialog d = new JDialog((JFrame)null, "Event Info", true);
                        JPanel panel = new JPanel(new BorderLayout());
                        JTextArea textArea = new JTextArea(ev.toString());
                        panel.add(new JScrollPane(textArea));
                        d.getContentPane().add(panel);
                        d.pack();
                        d.setLocationRelativeTo(ShowEventsPanel.this);
                        d.setVisible(true);
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                if (e.isPopupTrigger()) {
                    showPopupMenu(e);
                }
            }
            
        });

//        KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
//        kfm.addKeyEventDispatcher(new KeyEventDispatcher() {
//            public boolean dispatchKeyEvent(KeyEvent e) {
//                if (e.isControlDown()) {
//                    if(e.getKeyCode()==KeyEvent.VK_F) {
//                        showSearchDialog();
//                    }
//                }
//                return false;
//            }
//        });
        
        jTable1.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                int row = jTable1.getSelectedRow();
                currentRow = row;
            }
        });

        jTextFieldSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                super.keyTyped(e);
                char c = e.getKeyChar();
                if (c == KeyEvent.VK_ENTER) {
                    findNext();
                }
            }
        });
        
        //Source filter
        jTextFieldSource.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                sourceFilterAction(jTextFieldSource.getText());
            }

            public void removeUpdate(DocumentEvent e) {
                sourceFilterAction(jTextFieldSource.getText());
            }

            public void changedUpdate(DocumentEvent e) {                
                sourceFilterAction(jTextFieldSource.getText());
            }
        });
            
        //Destination filter
        jTextFieldDestination.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                destinationFilterAction(jTextFieldDestination.getText());
            }

            public void removeUpdate(DocumentEvent e) {
                destinationFilterAction(jTextFieldDestination.getText());
            }

            public void changedUpdate(DocumentEvent e) {
                destinationFilterAction(jTextFieldDestination.getText());
            }
        });

        //All filter
        jTextFieldAll.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                allFilterAction(jTextFieldAll.getText());
            }

            public void removeUpdate(DocumentEvent e) {
                allFilterAction(jTextFieldAll.getText());
            }

            public void changedUpdate(DocumentEvent e) {
                allFilterAction(jTextFieldAll.getText());
            }
        });
    }
    
    int currentRow = -1;
    String currentString = "";
    void findFirst() {
        String text = jTextFieldSearch.getText();
        int rows = jTable1.getRowCount();
        int cols = jTable1.getColumnCount();
        int k,l;
        for (k=0; k<rows; k++) {
            if (k<0) {
                continue;
            }
            for (l=0; l<cols; l++) {
                Object val = jTable1.getValueAt(k, l);
                String valStr = val.toString().toLowerCase();
                if (valStr.contains(text.toLowerCase())) {
                    jTable1.setRowSelectionInterval(k, k);
                    Rectangle aRect = jTable1.getCellRect(k, l, true);
                    jTable1.scrollRectToVisible(aRect);
                    return;
                }
            }
        }
    }
    void findNext() {
        String text = jTextFieldSearch.getText();
        int rows = jTable1.getRowCount();
        int cols = jTable1.getColumnCount();
        int k,l;
        for (k=currentRow+1; k<rows; k++) {
            if (k<0) {
                continue;
            }
            for (l=0; l<cols; l++) {
                Object val = jTable1.getValueAt(k, l);
                String valStr = val.toString().toLowerCase();
                if (valStr.contains(text.toLowerCase())) {
                    jTable1.setRowSelectionInterval(k, k);
                    Rectangle aRect = jTable1.getCellRect(k, l, true);
                    jTable1.scrollRectToVisible(aRect);
                    return;
                }
            }
        }
    }
     
    void saveLogLines(int[] rows) {
        int k;
        if (rows.length == 0) {
            JOptionPane.showMessageDialog(this, "Error, not enough rows chosen");
            return;
        }
        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            JFileChooser fc = new JFileChooser();
            fc.setCurrentDirectory(inFile);
            int res = fc.showSaveDialog(this);
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
            } 
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Error, file not found: "+ e.getMessage());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error, IOException: "+ e.getMessage());
        } finally {
            FileHelper.close(reader);
            FileHelper.close(writer);            
        }
        
    }
    
    public void showPopupMenu(MouseEvent e) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem createFightItem = new JMenuItem("Save lines to a file");
        popup.add(createFightItem);        
        
        createFightItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                int[] rows = jTable1.getSelectedRows();
                int[] logRows = new int[rows.length];
                for (int k=0; k<rows.length; k++) {
                    logRows[k] = ShowEventsPanel.this.filterEvents.get(rows[k]).getLogFileRow();
                }
                if (callback != null) {
                    callback.showEventsSaveLogLines(logRows);
                }
                if (inFile != null) {
                    saveLogLines(logRows);
                }
            }
        });
        
        popup.show(e.getComponent(), e.getX(), e.getY());
        
    }
    
    public void sourceFilterAction(String s) {
        sourceFilter = s;        
    }

    public void destinationFilterAction(String s) {
        destinationFilter = s;
    }

    public void allFilterAction(String s) {
        allFilter = s;
    }
    
    private void saveLogicalHistory(String syntax) {
        StringPrefsDb db = new StringPrefsDb(basePrefs);
        List<String> strings = db.getSavedStrings();
        List<String> strings2 = new ArrayList<String>();
        strings2.add(syntax);
        strings.remove(syntax);
        //Only keep at maximum 20 expressions.
        for (int k = 0; k < 19; k++) {
            if (k >= strings.size()) {
                break;
            } else {
                strings2.add(strings.get(k));
            }
        }
        db.putStrings(strings2);
    }
    
    public void applyFilters() {
        filterEvents.clear();
        EventCollection eventsAfterLogical = new EventCollectionSimple(allEvents);
        String logicalExpression = jTextFieldLogical.getText().trim();
        if (logicalExpression.length() > 0) {
            try {
                FilterSyntaxParser p = new FilterSyntaxParser(logicalExpression);
                Filter f = p.getFilter();
                eventsAfterLogical = eventsAfterLogical.filter(f);
                saveLogicalHistory(logicalExpression);
            } catch (SyntaxException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error in logical expression", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        for (LogEvent e1 : eventsAfterLogical.getEvents()) {
            BasicEvent e = (BasicEvent) e1;
            String sourceName = "";
            String destinationName = "";
            if (e.getSourceName() != null) {
                sourceName = e.getSourceName();
            }
            if (e.getDestinationName() != null) {
                destinationName = e.getDestinationName();
            }
            
            if (!sourceName.toLowerCase().contains(sourceFilter.toLowerCase())) {
                continue;
            }
            
            if (!destinationName.toLowerCase().contains(destinationFilter.toLowerCase())) {
                continue;
            }
            
            ArrayList<String> testStr = new  ArrayList<String>();
            testStr.add(sourceName);
            testStr.add(destinationName);
            testStr.add(e.getLogType());
            testStr.add(Integer.toString(e.logFileRow));
            testStr.add(e.getClass().getSimpleName());
            if (e instanceof SkillInterface) {
                SkillInterface si = (SkillInterface) e;
                testStr.add(si.getSkillName());                        
            }
            if (e instanceof SkillInterfaceExtra) {
                SkillInterfaceExtra si = (SkillInterfaceExtra) e;
                testStr.add(si.getExtraSpellName());
            }

            if (e instanceof DamageMissedEvent) {
                DamageMissedEvent dm = (DamageMissedEvent) e;
                testStr.add(dm.getMissType());
            }

            boolean allFound = false;
            for (String s : testStr) {
                if (s.toLowerCase().contains(allFilter.toLowerCase())) {
                    allFound = true;
                    break;
                }
            }
            
            if (!allFound) {
                continue;
            }
            
            filterEvents.add(e);
        }
        jTable1.setModel(new ShowEventsTableModel(filterEvents));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanelSource = new javax.swing.JPanel();
        jPanelDestination = new javax.swing.JPanel();
        jPanelAll = new javax.swing.JPanel();
        jPanelLogical = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanelTop = new javax.swing.JPanel();
        jPanelSearch = new javax.swing.JPanel();
        jTextFieldSearch = new javax.swing.JTextField();
        jLabelSearch = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jButtonFirst = new javax.swing.JButton();
        jButtonNext = new javax.swing.JButton();
        jPanelFilters = new javax.swing.JPanel();
        jPanelFilterButtons = new javax.swing.JPanel();
        jButtonHelp = new javax.swing.JButton();
        jButtonApply = new javax.swing.JButton();
        jButtonHistory = new javax.swing.JButton();
        jPanelSource1 = new javax.swing.JPanel();
        jLabelSource = new javax.swing.JLabel();
        jTextFieldSource = new javax.swing.JTextField();
        jPanelDestination1 = new javax.swing.JPanel();
        jTextFieldDestination = new javax.swing.JTextField();
        jLabelDestination = new javax.swing.JLabel();
        jPanelAll1 = new javax.swing.JPanel();
        jTextFieldAll = new javax.swing.JTextField();
        jLabelAll = new javax.swing.JLabel();
        jPanelLogical1 = new javax.swing.JPanel();
        jLabelLogicalExpression = new javax.swing.JLabel();
        jTextFieldLogical = new javax.swing.JTextField();

        jPanelSource.setLayout(new java.awt.GridBagLayout());

        jPanelDestination.setLayout(new java.awt.GridBagLayout());

        jPanelAll.setLayout(new java.awt.GridBagLayout());

        jPanelLogical.setLayout(new java.awt.GridBagLayout());

        setLayout(new java.awt.BorderLayout());

        jScrollPane1.setPreferredSize(new java.awt.Dimension(1000, 600));

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

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanelTop.setLayout(new java.awt.GridBagLayout());

        jPanelSearch.setBorder(javax.swing.BorderFactory.createTitledBorder("Searching"));
        jPanelSearch.setLayout(new java.awt.GridBagLayout());

        jTextFieldSearch.setColumns(15);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanelSearch.add(jTextFieldSearch, gridBagConstraints);

        jLabelSearch.setText("Search (Press enter to apply)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelSearch.add(jLabelSearch, gridBagConstraints);

        jPanel4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 5));

        jButtonFirst.setText("First");
        jButtonFirst.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButtonFirst.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                firstPressed(evt);
            }
        });
        jPanel4.add(jButtonFirst);

        jButtonNext.setText("Next");
        jButtonNext.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButtonNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextPressed(evt);
            }
        });
        jPanel4.add(jButtonNext);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        jPanelSearch.add(jPanel4, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.weighty = 1.0;
        jPanelTop.add(jPanelSearch, gridBagConstraints);

        jPanelFilters.setBorder(javax.swing.BorderFactory.createTitledBorder("Filters"));
        jPanelFilters.setLayout(new java.awt.GridBagLayout());

        jPanelFilterButtons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 5));

        jButtonHelp.setText("Help");
        jButtonHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonHelpActionPerformed(evt);
            }
        });
        jPanelFilterButtons.add(jButtonHelp);

        jButtonApply.setText("Apply filters");
        jButtonApply.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyPressed(evt);
            }
        });
        jPanelFilterButtons.add(jButtonApply);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        jPanelFilters.add(jPanelFilterButtons, gridBagConstraints);

        jButtonHistory.setText("History");
        jButtonHistory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonHistoryActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        jPanelFilters.add(jButtonHistory, gridBagConstraints);

        jPanelSource1.setLayout(new java.awt.GridBagLayout());

        jLabelSource.setText("Source filter");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        jPanelSource1.add(jLabelSource, gridBagConstraints);

        jTextFieldSource.setColumns(15);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        jPanelSource1.add(jTextFieldSource, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.25;
        gridBagConstraints.weighty = 1.0;
        jPanelFilters.add(jPanelSource1, gridBagConstraints);

        jPanelDestination1.setLayout(new java.awt.GridBagLayout());

        jTextFieldDestination.setColumns(15);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        jPanelDestination1.add(jTextFieldDestination, gridBagConstraints);

        jLabelDestination.setText("Destination filter");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        jPanelDestination1.add(jLabelDestination, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.25;
        gridBagConstraints.weighty = 1.0;
        jPanelFilters.add(jPanelDestination1, gridBagConstraints);

        jPanelAll1.setLayout(new java.awt.GridBagLayout());

        jTextFieldAll.setColumns(15);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        jPanelAll1.add(jTextFieldAll, gridBagConstraints);

        jLabelAll.setText("All fields filter");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        jPanelAll1.add(jLabelAll, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.25;
        gridBagConstraints.weighty = 1.0;
        jPanelFilters.add(jPanelAll1, gridBagConstraints);

        jPanelLogical1.setLayout(new java.awt.GridBagLayout());

        jLabelLogicalExpression.setText("Logical expression");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        jPanelLogical1.add(jLabelLogicalExpression, gridBagConstraints);

        jTextFieldLogical.setColumns(15);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        jPanelLogical1.add(jTextFieldLogical, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelFilters.add(jPanelLogical1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelTop.add(jPanelFilters, gridBagConstraints);

        add(jPanelTop, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    private void firstPressed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_firstPressed
        findFirst();
    }//GEN-LAST:event_firstPressed

    private void nextPressed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextPressed
        findNext();
    }//GEN-LAST:event_nextPressed

private void applyPressed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyPressed
    applyFilters();
}//GEN-LAST:event_applyPressed

private void jButtonHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonHelpActionPerformed
    ShowHelpFrame dia = new ShowHelpFrame("wowlogparser/gui/help/filterhelp.txt");
    dia.setLocationRelativeTo(this);
    dia.setVisible(true);
}//GEN-LAST:event_jButtonHelpActionPerformed

private void jButtonHistoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonHistoryActionPerformed
    StringPrefsDb db = new StringPrefsDb(basePrefs);
    List<String> strings = db.getSavedStrings();
    if (strings.size() == 0) {
        JOptionPane.showMessageDialog(ShowEventsPanel.this, "No history exists. History is updated each time apply is pressed.", "Alert", JOptionPane.WARNING_MESSAGE);
    } else {
        StringChooser chooser = new StringChooser(null, true, strings);
        chooser.setLocationRelativeTo(ShowEventsPanel.this);
        chooser.setVisible(true);
        if (chooser.okPressed) {
            String chosenString = chooser.getChosenString();
            jTextFieldLogical.setText(chosenString);
        }
    }
    
}//GEN-LAST:event_jButtonHistoryActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonApply;
    private javax.swing.JButton jButtonFirst;
    private javax.swing.JButton jButtonHelp;
    private javax.swing.JButton jButtonHistory;
    private javax.swing.JButton jButtonNext;
    private javax.swing.JLabel jLabelAll;
    private javax.swing.JLabel jLabelDestination;
    private javax.swing.JLabel jLabelLogicalExpression;
    private javax.swing.JLabel jLabelSearch;
    private javax.swing.JLabel jLabelSource;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanelAll;
    private javax.swing.JPanel jPanelAll1;
    private javax.swing.JPanel jPanelDestination;
    private javax.swing.JPanel jPanelDestination1;
    private javax.swing.JPanel jPanelFilterButtons;
    private javax.swing.JPanel jPanelFilters;
    private javax.swing.JPanel jPanelLogical;
    private javax.swing.JPanel jPanelLogical1;
    private javax.swing.JPanel jPanelSearch;
    private javax.swing.JPanel jPanelSource;
    private javax.swing.JPanel jPanelSource1;
    private javax.swing.JPanel jPanelTop;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextFieldAll;
    private javax.swing.JTextField jTextFieldDestination;
    private javax.swing.JTextField jTextFieldLogical;
    private javax.swing.JTextField jTextFieldSearch;
    private javax.swing.JTextField jTextFieldSource;
    // End of variables declaration//GEN-END:variables
    
}
