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

import wowlogparserbase.helpers.WorkerProgressDialog;
import wowlogparserbase.Fight;
import wowlogparserbase.TimePeriod;
import java.awt.Dialog;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.table.DefaultTableModel;
import wowlogparser.*;
import wowlogparserbase.BossInfo;
import wowlogparserbase.BossParserSource;
import wowlogparserbase.BossParserSourceSimple;
import wowlogparserbase.BossParsing;
import wowlogparserbase.FileLoader;
import wowlogparserbase.NpcInfo;

/**
 * A panel that allows doing boss parsing.
 * 
 * @author  racy
 */
public class BossPanel extends javax.swing.JPanel {
    
    List<Fight> fights;
    AlmostDefaultTableModel tm1;
    AlmostDefaultTableModel tm2;
    
    List<Integer> table1Indices = new ArrayList<Integer>();
    List<Integer> table2Indices = new ArrayList<Integer>();
    
    List<Fight> outFights = null;
    FileLoader fl;
    
    Dialog owner = null;
    Frame ownerFrame = null;
    
    /** Creates new form BossPanel */
    public BossPanel(Dialog owner, List<Fight> fights, FileLoader fl) {
        this.fights = fights;
        this.owner = owner;
        this.fl = fl;
        initComponents();
        initTable1();
        initTable2();
    }

    /** Creates new form BossPanel */
    public BossPanel(Frame owner, List<Fight> fights, FileLoader fl) {
        this.fights = fights;
        this.ownerFrame = owner;
        this.fl = fl;
        initComponents();
        initTable1();
        initTable2();
    }

    /**
     * Get generated boss fights
     * @return A list of boss fights
     */
    public List<Fight> getOutFights() {
        return outFights;
    }
    
    public void initTable1() {
        String[] tm1Columns = {"Fight name", "Duration"};
        tm1 = new AlmostDefaultTableModel(tm1Columns, 0);
        jTable1.getColumnModel().getColumn(0).setPreferredWidth(200);
        jTable1.getColumnModel().getColumn(1).setPreferredWidth(100);
        table1Indices = new ArrayList<Integer>();
        outer:
        for (int k=0; k<fights.size(); k++) {
            for(int l=0; l<table2Indices.size(); l++) {
                if (table2Indices.get(l) == k) {
                    continue outer;
                }
            }
            Fight f = fights.get(k);
            String name = f.getName();
            String duration = "" + (int) f.getDuration();
            Vector<String> v = new Vector<String>();
            v.add(name);
            v.add(duration);
            tm1.addRow(v);
            table1Indices.add(k);
        }
        jTable1.setModel(tm1);
    }
    
    public void initTable2() {
        String[] tm2Columns = {"Fight name", "Duration"};
        tm2 = new AlmostDefaultTableModel(tm2Columns, 0);
        jTable2.getColumnModel().getColumn(0).setPreferredWidth(200);
        jTable2.getColumnModel().getColumn(1).setPreferredWidth(100);
        for (int k=0; k<table2Indices.size(); k++) {
            int index = table2Indices.get(k);
            Fight f = fights.get(index);
            String name = f.getName();
            String duration = "" + (int) f.getDuration();
            Vector<String> v = new Vector<String>();
            v.add(name);
            v.add(duration);
            tm2.addRow(v);
        }
        jTable2.setModel(tm2);        
    }

    /**
     * A table model class
     */
    public class AlmostDefaultTableModel extends DefaultTableModel {

        public AlmostDefaultTableModel(Object[] columnNames, int rowCount) {
            super(columnNames, rowCount);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }

    private void addRows(int[] rows) {
        Arrays.sort(rows);
        if (rows.length > 0) {
            for (int k = 0; k < rows.length; k++) {
                int row = rows[k];
                int index = table1Indices.get(row);
                table2Indices.add(index);
            }
            for (int k = rows.length - 1; k >= 0; k--) {
                int row = rows[k];
                table1Indices.remove(row);
            }
            Collections.sort(table1Indices);
            Collections.sort(table2Indices);
            initTable1();
            initTable2();
        }        
    }
    
    private void removeRows(int[] rows) {
        if (rows.length > 0) {
            for (int k = 0; k < rows.length; k++) {
                int row = rows[k];
                int index = table2Indices.get(row);
                table1Indices.add(index);
            }
            for (int k = rows.length - 1; k >= 0; k--) {
                int row = rows[k];
                table2Indices.remove(row);
            }
            Collections.sort(table1Indices);
            Collections.sort(table2Indices);
            initTable1();
            initTable2();
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
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jButton4 = new javax.swing.JButton();

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

        jButton1.setText("Add");
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButton(evt);
            }
        });

        jButton2.setText("Remove");
        jButton2.setName("jButton2"); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButton(evt);
            }
        });

        jButton3.setText("Make Boss Fights");
        jButton3.setName("jButton3"); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                makeButton(evt);
            }
        });

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        jTextArea1.setColumns(20);
        jTextArea1.setEditable(false);
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(3);
        jTextArea1.setText("Manually: Add the bosses to the list and press Make boss fights. All other mobs that was fought at the same time as the boss are added to the resulting fight.\nAutomatically: Boss fights are created from information in an XML file.");
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setName("jTextArea1"); // NOI18N
        jScrollPane3.setViewportView(jTextArea1);

        jButton4.setText("Try automatic finding of bosses");
        jButton4.setName("jButton4"); // NOI18N
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tryAutomatic(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 723, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(layout.createSequentialGroup()
                                .add(jButton4)
                                .add(74, 74, 74)
                                .add(jButton1))
                            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 316, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(jButton2)
                                .add(36, 36, 36)
                                .add(jButton3)
                                .add(179, 179, 179))
                            .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 401, Short.MAX_VALUE)))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jButton1)
                    .add(jButton2)
                    .add(jButton3)
                    .add(jButton4))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(jScrollPane2)
                    .add(jScrollPane1)))
        );
    }// </editor-fold>//GEN-END:initComponents

private void addButton(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButton
    int[] rows = jTable1.getSelectedRows();//GEN-LAST:event_addButton
    addRows(rows);
}                          

private void removeButton(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButton
    int[] rows = jTable2.getSelectedRows();
    removeRows(rows);    
}//GEN-LAST:event_removeButton

private void makeButton(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_makeButton
    outFights = new ArrayList<Fight>();
    BossParserSourceSimple bossSource = new BossParserSourceSimple();
    for (int k=0; k<table2Indices.size(); k++) {
        int index = table2Indices.get(k);
        Fight f = fights.get(index);
        int mobId = f.getMobID();
        if (mobId == -1) {
            List<Integer> mobIds = f.getMobIDs();
            BossInfo bi = new BossInfo(f.getName());
            for(int mi : mobIds) {
                bi.addMob(new NpcInfo("", mi, true));
            }
            bossSource.add(bi);
        } else {
            BossInfo bi = new BossInfo(f.getName());
            bi.addMob(new NpcInfo(f.getName(), mobId, true));
            bossSource.add(bi);
        }
    }

    BossParsing bp;
    if (owner != null) {
        bp = new BossParsing(owner, fights, fl, bossSource);
    } else if (ownerFrame != null) {
        bp = new BossParsing(ownerFrame, fights, fl, bossSource);
    } else {
        bp = new BossParsing((JFrame)null, fights, fl, bossSource);
    }
    bp.doAutomaticBossParsing();
    outFights = bp.getOutFights();

    if (owner != null) {
        owner.setVisible(false);
    } else if (ownerFrame != null) {
        ownerFrame.setVisible(false);
    }
}//GEN-LAST:event_makeButton

private void tryAutomatic(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tryAutomatic
    BossParsing bp;
    if (owner != null) {
        bp = new BossParsing(owner, fights, fl);
    } else if (ownerFrame != null) {
        bp = new BossParsing(ownerFrame, fights, fl);
    } else {
        bp = new BossParsing(fights, fl);
    }
    bp.doAutomaticBossParsing();
    outFights = bp.getOutFights();
    if (owner != null) {
        owner.setVisible(false);
    } else if (ownerFrame != null) {
        ownerFrame.setVisible(false);
    }
}//GEN-LAST:event_tryAutomatic

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration//GEN-END:variables
}
