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
 * RaidPlotsPanel.java
 *
 * Created on 2008-jul-12, 13:54:02
 */

package wowlogparser.gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Frame;
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
import org.jfree.data.xy.*;
import wowlogparserbase.*;
import wowlogparserbase.events.*;

/**
 *
 * @author gustav
 */
public class RaidPlotsPanel extends javax.swing.JPanel {

    Frame owner;
    List<Fight> fights;
    List<String> damageNamesWithNumber = new ArrayList<String>();
    List<String> healingNamesWithNumber = new ArrayList<String>();
    SelectParticipantsDialog dmgDialog;
    SelectParticipantsDialog healDialog;

    /** Creates new form RaidPlotsPanel */
    public RaidPlotsPanel(List<Fight> fights, Frame owner) {
        initComponents();
        this.fights = new ArrayList<Fight>();
        this.owner = owner;
        for (Fight f : fights) {
            Fight fCopy = new Fight(f);
            fCopy.removeAssignedPets();
            this.fights.add(fCopy);
        }
        jPanelDamage.setLayout(new BorderLayout());
        //jPanelHealing.setLayout(new BorderLayout());
        initNames();
        initDialogs();
        initPlots();
    }

    public void initNames() {
        damageNamesWithNumber = new ArrayList<String>();
        healingNamesWithNumber = new ArrayList<String>();
        int number = 0;
        for (Fight f : fights) {
            String name = f.getName() + "(" + number + ")";
            damageNamesWithNumber.add(name);
            healingNamesWithNumber.add(name);
            number++;
        }
    }

    public void initDialogs() {
        dmgDialog = new SelectParticipantsDialog(owner, "Damage participants", true, damageNamesWithNumber);
        healDialog = new SelectParticipantsDialog(owner, "Healing participants", true, healingNamesWithNumber);
        dmgDialog.setLocationRelativeTo(this);
        healDialog.setLocationRelativeTo(this);
    }

    public void initPlots() {

        double interval = jSlider1.getValue();
        List<Boolean> includedFightsDamage = new ArrayList<Boolean>();
        List<Boolean> includedFightsHealing = new ArrayList<Boolean>();
        for (int k=0; k<fights.size(); k++) {
            includedFightsDamage.add(dmgDialog.getSelected(k));
        }
        for (int k=0; k<fights.size(); k++) {
            includedFightsHealing.add(healDialog.getSelected(k));
        }
        DefaultXYDataset damageDataset = WlpPlotFactory.createRaidDamageLinePlotDataset(fights, includedFightsDamage, damageNamesWithNumber, interval);
        JFreeChart damageChart = WlpPlotFactory.createRaidDamageLinePlot(damageDataset, SettingsSingleton.getInstance().getSplinePlots());
        DefaultXYDataset healingDataset = WlpPlotFactory.createRaidDamageLinePlotDataset(fights, includedFightsHealing, healingNamesWithNumber, interval);
        JFreeChart healingChart = WlpPlotFactory.createRaidHealingLinePlot(healingDataset, SettingsSingleton.getInstance().getSplinePlots());

        ChartPanel damagePanel = new ChartPanel(damageChart);
        jPanelDamage.removeAll();
        jPanelDamage.add(damagePanel);
        jPanelDamage.revalidate();

        ChartPanel healingPanel = new ChartPanel(healingChart);
        //jPanelHealing.removeAll();
        //jPanelHealing.add(healingPanel);
        //jPanelHealing.revalidate();

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
        jPanelDamage = new javax.swing.JPanel();
        jSlider1 = new javax.swing.JSlider();
        jButton1 = new javax.swing.JButton();

        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        jPanelDamage.setName("jPanelDamage"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanelDamageLayout = new org.jdesktop.layout.GroupLayout(jPanelDamage);
        jPanelDamage.setLayout(jPanelDamageLayout);
        jPanelDamageLayout.setHorizontalGroup(
            jPanelDamageLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 795, Short.MAX_VALUE)
        );
        jPanelDamageLayout.setVerticalGroup(
            jPanelDamageLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 455, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Damage", jPanelDamage);

        jSlider1.setMajorTickSpacing(5);
        jSlider1.setMaximum(60);
        jSlider1.setMinimum(1);
        jSlider1.setMinorTickSpacing(1);
        jSlider1.setPaintLabels(true);
        jSlider1.setPaintTicks(true);
        jSlider1.setSnapToTicks(true);
        jSlider1.setValue(10);
        jSlider1.setName("jSlider1"); // NOI18N
        jSlider1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderChange(evt);
            }
        });

        jButton1.setText("Select fights");
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSelect(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(24, 24, 24)
                .add(jButton1)
                .addContainerGap(683, Short.MAX_VALUE))
            .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 800, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jSlider1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(22, 22, 22)
                .add(jButton1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jSlider1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 480, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void sliderChange(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sliderChange
        if (!jSlider1.getValueIsAdjusting()) {
            initPlots();
        }
    }//GEN-LAST:event_sliderChange

    private void buttonSelect(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSelect
        dmgDialog.setVisible(true);
        initPlots();
    }//GEN-LAST:event_buttonSelect


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JPanel jPanelDamage;
    private javax.swing.JSlider jSlider1;
    private javax.swing.JTabbedPane jTabbedPane1;
    // End of variables declaration//GEN-END:variables

}

class SelectParticipantsDialog extends JDialog {

    List<String> names;
    List<JCheckBox> checkBoxes = new ArrayList<JCheckBox>();
    JButton okButton;

    public SelectParticipantsDialog(Frame owner, String title, boolean modal, List<String> names) {
        super(owner, title, modal);
        this.names = names;
        initComponents();
        pack();
    }

    public void initComponents() {
        JPanel panel = new JPanel();
        getContentPane().add(panel);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        for (String name : names) {
            JCheckBox b = new JCheckBox(name, true);
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