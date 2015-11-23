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
/*
 * FightSplitterDialog.java
 *
 * Created on 2009-okt-31, 17:03:30
 */

package wowlogparser.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JOptionPane;
import wowlogparserbase.EventCollection;
import wowlogparserbase.EventCollectionSimple;
import wowlogparserbase.Fight;
import wowlogparserbase.FightSplitter;
import wowlogparserbase.FileLoader;
import wowlogparserbase.SpellInfo;
import wowlogparserbase.eventfilter.FilterClass;
import wowlogparserbase.eventfilter.FilterNameAnd;
import wowlogparserbase.events.BasicEvent;
import wowlogparserbase.events.aura.SpellAuraAppliedEvent;
import wowlogparserbase.events.aura.SpellAuraRemovedEvent;

/**
 *
 * @author racy
 */
public class FightSplitterDialog extends javax.swing.JDialog {

    List<Fight> fights = new ArrayList<Fight>();
    Fight sourceFight;
    FileLoader fl;

    EventCollection currentEvents = new EventCollectionSimple();
    List<SpellInfo> currentSpellInfo = new ArrayList<SpellInfo>();

    String source = null;
    String dest = null;

    /** Creates new form FightSplitterDialog */
    public FightSplitterDialog(java.awt.Frame parent, Fight sourceFight, FileLoader fl) {
        super(parent, true);
        initComponents();
        this.sourceFight = sourceFight;
        this.fl = fl;
        jComboBoxType.removeAllItems();
        jComboBoxType.addItem("Aura applied/removed");
        jComboBoxType.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                initCurrentEvents();
            }
        });

        jComboBoxSource.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int selected = jComboBoxSource.getSelectedIndex();
                if (selected < 0) {
                    return;
                }
                if (selected == 0) {
                    source = null;
                } else {
                    source = (String)jComboBoxSource.getItemAt(selected);
                }
                initSpellIdList();
            }
        });

        jComboBoxDest.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int selected = jComboBoxDest.getSelectedIndex();
                if (selected < 0) {
                    return;
                }
                if (selected == 0) {
                    dest = null;
                } else {
                    dest = (String)jComboBoxDest.getItemAt(selected);
                }
                initSpellIdList();
            }
        });
        initCurrentEvents();

    }

    private void initNames() {
        Set<String> sourceNames = new HashSet<String>();
        Set<String> destNames = new HashSet<String>();
        for (BasicEvent e : currentEvents.getEvents()) {
            sourceNames.add(e.getSourceName());
            destNames.add(e.getDestinationName());
        }
        List<String> sourceNamesList = new ArrayList<String>(sourceNames);
        List<String> destNamesList = new ArrayList<String>(destNames);
        Collections.sort(sourceNamesList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        Collections.sort(destNamesList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        jComboBoxSource.removeAllItems();
        jComboBoxSource.addItem("Any");
        for (String name : sourceNamesList) {
            jComboBoxSource.addItem(name);
        }
        jComboBoxDest.removeAllItems();
        jComboBoxDest.addItem("Any");
        for (String name : destNamesList) {
            jComboBoxDest.addItem(name);
        }
    }

    private void initCurrentEvents() {
        jComboBoxSource.removeAllItems();
        jComboBoxDest.removeAllItems();
        jComboBoxSpellId.removeAllItems();
        switch(jComboBoxType.getSelectedIndex()) {
            case 0:
                currentEvents = sourceFight.filter(new FilterClass(SpellAuraAppliedEvent.class));
                break;
        }
        initNames();
    }

    private void initSpellIdList() {
        EventCollection filteredEvents = currentEvents.filter(new FilterNameAnd(source, dest));
        List<SpellInfo> sis = filteredEvents.getIDs(EventCollection.TYPE_ANY_SPELL);
        Collections.sort(sis, new Comparator<SpellInfo>() {
            @Override
            public int compare(SpellInfo o1, SpellInfo o2) {
                return o1.name.compareTo(o2.name);
            }
        });
        jComboBoxSpellId.removeAllItems();
        for (SpellInfo si : sis) {
            jComboBoxSpellId.addItem(si);
        }
    }

    private String getSourceName() {
        int index = jComboBoxSource.getSelectedIndex();
        if (index > 1) {
            return (String)jComboBoxSource.getItemAt(index);
        } else {
            return null;
        }
    }

    private String getDestName() {
        int index = jComboBoxDest.getSelectedIndex();
        if (index > 1) {
            return (String)jComboBoxDest.getItemAt(index);
        } else {
            return null;
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
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jComboBoxType = new javax.swing.JComboBox();
        jTextFieldPreTime = new javax.swing.JTextField();
        jTextFieldPostTime = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jButtonDo = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jComboBoxSpellId = new javax.swing.JComboBox();
        jComboBoxSource = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jComboBoxDest = new javax.swing.JComboBox();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Create split fights");
        setModal(true);

        jComboBoxType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBoxType.setName("jComboBoxType"); // NOI18N

        jTextFieldPreTime.setText("0");
        jTextFieldPreTime.setToolTipText("Use events that occur this many seconds before the start event");
        jTextFieldPreTime.setName("jTextFieldPreTime"); // NOI18N

        jTextFieldPostTime.setText("0.2");
        jTextFieldPostTime.setToolTipText("Use events that occur this many seconds after the end event");
        jTextFieldPostTime.setName("jTextFieldPostTime"); // NOI18N

        jLabel1.setText("Pre-time (s)");
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel2.setText("Post-time (s)");
        jLabel2.setName("jLabel2"); // NOI18N

        jLabel3.setText("Split type");
        jLabel3.setName("jLabel3"); // NOI18N

        jButtonDo.setText("Do split");
        jButtonDo.setName("jButtonDo"); // NOI18N
        jButtonDo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDoActionPerformed(evt);
            }
        });

        jButtonCancel.setText("Cancel");
        jButtonCancel.setName("jButtonCancel"); // NOI18N
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });

        jLabel4.setText("Spell id");
        jLabel4.setName("jLabel4"); // NOI18N

        jComboBoxSpellId.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBoxSpellId.setName("jComboBoxSpellId"); // NOI18N

        jComboBoxSource.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBoxSource.setName("jComboBoxSource"); // NOI18N

        jLabel7.setText("Source Name");
        jLabel7.setName("jLabel7"); // NOI18N

        jLabel8.setText("Destination name");
        jLabel8.setName("jLabel8"); // NOI18N

        jComboBoxDest.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBoxDest.setName("jComboBoxDest"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        jTextPane1.setBackground(new java.awt.Color(230, 230, 230));
        jTextPane1.setEditable(false);
        jTextPane1.setText("Selecting an item in a box changes the content in the boxes below them, so start at the top and work your way down.");
        jTextPane1.setName("jTextPane1"); // NOI18N
        jScrollPane2.setViewportView(jTextPane1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(67, 67, 67)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButtonDo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonCancel))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jLabel3)
                                .addComponent(jComboBoxSource, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jComboBoxDest, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jComboBoxSpellId, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jComboBoxType, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel7)
                            .addComponent(jLabel8))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel2)
                            .addComponent(jTextFieldPostTime, javax.swing.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
                            .addComponent(jLabel1)
                            .addComponent(jTextFieldPreTime)))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 368, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(94, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBoxType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldPreTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBoxSource, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldPostTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBoxDest, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBoxSpellId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonDo)
                    .addComponent(jButtonCancel))
                .addContainerGap(52, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonDoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDoActionPerformed
        FightSplitter fs = new FightSplitter(sourceFight, fl);
        String sourceName = getSourceName();
        String destName = getDestName();
        double pre = Double.parseDouble(jTextFieldPreTime.getText());
        double post = Double.parseDouble(jTextFieldPostTime.getText());
        int spellIdIndex = jComboBoxSpellId.getSelectedIndex();
        if (spellIdIndex == -1) {
            JOptionPane.showMessageDialog(this, "Must select a spell id", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        SpellInfo si = (SpellInfo)jComboBoxSpellId.getSelectedItem();
        switch(jComboBoxType.getSelectedIndex()) {
            case 0:
                fights = fs.split(SpellAuraAppliedEvent.class, SpellAuraRemovedEvent.class,
                        si.spellID, sourceName, destName, pre, post);
                break;
        }
        setVisible(false);
    }//GEN-LAST:event_jButtonDoActionPerformed

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        setVisible(false);
    }//GEN-LAST:event_jButtonCancelActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonDo;
    private javax.swing.JComboBox jComboBoxDest;
    private javax.swing.JComboBox jComboBoxSource;
    private javax.swing.JComboBox jComboBoxSpellId;
    private javax.swing.JComboBox jComboBoxType;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField jTextFieldPostTime;
    private javax.swing.JTextField jTextFieldPreTime;
    private javax.swing.JTextPane jTextPane1;
    // End of variables declaration//GEN-END:variables

}
