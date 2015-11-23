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

import wowlogparserbase.Fight;
import wowlogparserbase.FightParticipant;
import wowlogparserbase.AmountAndCount;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import org.jfree.chart.*;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;
import wowlogparser.*;
import wowlogparserbase.SettingsSingleton;
import wowlogparserbase.WlpPlotFactory;
import wowlogparserbase.events.BasicEvent;

/**
 *
 * @author  racy
 */
public class MultiplePlotsPanel extends javax.swing.JPanel {

    Fight fight;
    Frame owner;
    JDialog diaOwner;
    SelectParticipantsDialog dmgDialog;
    SelectParticipantsDialog healDialog;
    
    /** Creates new form MultiplePlotsPanel */
    public MultiplePlotsPanel(Fight f, Frame owner, JDialog diaOwner) {
        initComponents();
        jPanel1.setLayout(new BorderLayout());
        jPanel2.setLayout(new BorderLayout());
        this.owner = owner;
        this.diaOwner = diaOwner;
        fight = f;
        fight.removeAssignedPets();
        initDialogs();
        initPlots();        
    }

    public void initDialogs() {
        dmgDialog = new SelectParticipantsDialog(owner, "Damage participants", true, fight);
        healDialog = new SelectParticipantsDialog(owner, "Healing participants", true, fight);
        dmgDialog.setLocationRelativeTo(this);
        healDialog.setLocationRelativeTo(this);
    }
    
    public void initPlots() {
        List<Boolean> includedParticipantsDamage = new ArrayList<Boolean>();
        List<Boolean> includedParticipantsHealing = new ArrayList<Boolean>();
        for (int k=0; k<fight.getParticipants().size(); k++) {
            includedParticipantsDamage.add(dmgDialog.getSelected(k));
        }
        for (int k=0; k<fight.getParticipants().size(); k++) {
            includedParticipantsHealing.add(healDialog.getSelected(k));
        }

        long minDamage = 0;
        try {
            minDamage = Long.parseLong(jTextField1.getText());
        } catch (NumberFormatException ex) {                    
        }
        
        long minHealing = 0;
        try {
            minHealing = Long.parseLong(jTextField2.getText());
        } catch (NumberFormatException ex) {            
        }
        
        double interval = jSlider1.getValue();

        DefaultXYDataset damageDataset = WlpPlotFactory.createMultipleParticipantsDamageLinePlotDatset(fight, fight.getParticipants(), includedParticipantsDamage, interval, minDamage);
        DefaultXYDataset healingDataset = WlpPlotFactory.createMultipleParticipantsHealingLinePlotDatset(fight, fight.getParticipants(), includedParticipantsHealing, interval, minHealing);
        JFreeChart damageChart = WlpPlotFactory.createMultipleParticipantsDamageLinePlot(damageDataset, SettingsSingleton.getInstance().getSplinePlots());
        JFreeChart healingChart = WlpPlotFactory.createMultipleParticipantsHealingLinePlot(healingDataset, SettingsSingleton.getInstance().getSplinePlots());

        ChartPanel damagePanel = new ChartPanel(damageChart);
        jPanel1.removeAll();
        jPanel1.add(damagePanel);
        jPanel1.revalidate();

        ChartPanel healingPanel = new ChartPanel(healingChart);
        jPanel2.removeAll();
        jPanel2.add(healingPanel);
        jPanel2.revalidate();
    
    }
        
    class SelectParticipantsDialog extends JDialog {

        Fight fight;
        ArrayList<JCheckBox> checkBoxes = new ArrayList<JCheckBox>();
        JButton okButton;
        
        public SelectParticipantsDialog(Frame owner, String title, boolean modal, Fight f) {
            super(owner, title, modal);
            fight = f;
            initComponents();
            pack();
        }
        
        public void initComponents() {
            JPanel panel = new JPanel();
            getContentPane().add(panel);
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            for (FightParticipant p : fight.getParticipants()) {
                JCheckBox b = new JCheckBox(p.getName(), true);
                checkBoxes.add(b);
                panel.add(b);
            }
            okButton = new JButton("Ok");
            panel.add(okButton);
            okButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    setVisible(false);
                }
            });
        }
        
        public boolean getSelected(int index) {
            if (index < checkBoxes.size()) {
                return checkBoxes.get(index).isSelected();
            } else {
                return false;
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

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jTextField2 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jSlider1 = new javax.swing.JSlider();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();

        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 1019, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 604, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Damage", jPanel1);

        jPanel2.setName("jPanel2"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 1019, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 604, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Healing", jPanel2);

        jTextField1.setText("-1");
        jTextField1.setName("jTextField1"); // NOI18N

        jButton1.setText("Redo Plots");
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setButton(evt);
            }
        });

        jTextField2.setText("-1");
        jTextField2.setName("jTextField2"); // NOI18N

        jLabel1.setText("Min Damage");
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel2.setText("Min Healing");
        jLabel2.setName("jLabel2"); // NOI18N

        jSlider1.setMajorTickSpacing(5);
        jSlider1.setMaximum(60);
        jSlider1.setMinimum(1);
        jSlider1.setMinorTickSpacing(1);
        jSlider1.setPaintLabels(true);
        jSlider1.setPaintTicks(true);
        jSlider1.setSnapToTicks(true);
        jSlider1.setToolTipText("Averaging interval (s)");
        jSlider1.setValue(10);
        jSlider1.setName("jSlider1"); // NOI18N
        jSlider1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderChange(evt);
            }
        });

        jButton2.setText("Select damage participants");
        jButton2.setName("jButton2"); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dmgParticipantsButton(evt);
            }
        });

        jButton3.setText("Select healing participants");
        jButton3.setName("jButton3"); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                healParticipantsButton(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1024, Short.MAX_VALUE)
            .add(jSlider1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1024, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 80, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(jTextField2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 80, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(jLabel1)
                        .add(41, 41, 41)
                        .add(jLabel2)))
                .add(33, 33, 33)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jButton2)
                    .add(layout.createSequentialGroup()
                        .add(jButton3)
                        .add(29, 29, 29)
                        .add(jButton1)))
                .addContainerGap(534, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel2))
                        .add(6, 6, 6)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jTextField2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jButton2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jButton3)
                            .add(jButton1))))
                .add(20, 20, 20)
                .add(jSlider1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 632, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void setButton(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setButton
    initPlots();
}//GEN-LAST:event_setButton

private void sliderChange(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sliderChange
    if (!jSlider1.getValueIsAdjusting()) {
        initPlots();
    }
}//GEN-LAST:event_sliderChange

private void dmgParticipantsButton(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dmgParticipantsButton
    dmgDialog.setVisible(true);
    initPlots();
}//GEN-LAST:event_dmgParticipantsButton

private void healParticipantsButton(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_healParticipantsButton
    healDialog.setVisible(true);
    initPlots();
}//GEN-LAST:event_healParticipantsButton


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSlider jSlider1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    // End of variables declaration//GEN-END:variables

}
