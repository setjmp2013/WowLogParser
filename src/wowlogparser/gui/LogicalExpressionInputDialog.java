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
 * LogicalExpressionInputDialog.java
 *
 * Created on 2010-mar-14, 17:11:26
 */

package wowlogparser.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JOptionPane;
import wowlogparserbase.eventfilter.*;
import wowlogparserbase.logical.FilterSyntaxParser;
import wowlogparserbase.logical.SyntaxException;

/**
 *
 * @author racy
 */
public class LogicalExpressionInputDialog extends javax.swing.JDialog {

    private java.awt.Frame parent;
    private Preferences prefs;
    private int MAX_SAVED_STRINGS = 20;

    private Filter filter = null;
    private String filterString = "";
    private boolean ok = false;

    /** Creates new form LogicalExpressionInputDialog */
    public LogicalExpressionInputDialog(java.awt.Frame parent, Preferences basePrefs) {
        super(parent, true);
        setModalityType(ModalityType.DOCUMENT_MODAL);
        this.parent = parent;
        this.prefs = basePrefs.node("LogicalExpressionInputDialog");
        initComponents();
        loadSettings();
    }

    private void loadSettings() {
        String expStr = prefs.get("expression", "");
        jTextFieldExpression.setText(expStr);
    }

    private void saveSettings() {
        prefs.put("expression", jTextFieldExpression.getText());
    }

    public boolean okPressed() {
        return ok;
    }

    public Filter getFilter() {
        return filter;
    }

    public String getFilterString() {
        return filterString;
    }

    private void saveLogicalHistory(String syntax) {
        StringPrefsDb db = new StringPrefsDb(prefs);
        List<String> strings = db.getSavedStrings();
        List<String> strings2 = new ArrayList<String>();
        strings2.add(syntax);
        strings.remove(syntax);
        //Only keep at maximum MAX_SAVED_STRINGS expressions.
        for (int k = 0; k < MAX_SAVED_STRINGS; k++) {
            if (k >= strings.size()) {
                break;
            } else {
                strings2.add(strings.get(k));
            }
        }
        db.putStrings(strings2);
    }
    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTextFieldExpression = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jButtonOk = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();
        jButtonHelp = new javax.swing.JButton();
        jButtonHistory = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jTextFieldExpression.setName("jTextFieldExpression"); // NOI18N

        jLabel1.setText("Logical expression");
        jLabel1.setName("jLabel1"); // NOI18N

        jButtonOk.setText("Ok");
        jButtonOk.setName("jButtonOk"); // NOI18N
        jButtonOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOkActionPerformed(evt);
            }
        });

        jButtonCancel.setText("Cancel");
        jButtonCancel.setName("jButtonCancel"); // NOI18N
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });

        jButtonHelp.setText("Help");
        jButtonHelp.setName("jButtonHelp"); // NOI18N
        jButtonHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonHelpActionPerformed(evt);
            }
        });

        jButtonHistory.setText("History");
        jButtonHistory.setName("jButtonHistory"); // NOI18N
        jButtonHistory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonHistoryActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jTextFieldExpression, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 471, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jButtonHistory)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 197, Short.MAX_VALUE)
                        .addComponent(jButtonOk, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonCancel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonHelp, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldExpression, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 23, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButtonHelp, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButtonCancel)
                            .addComponent(jButtonOk))
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonHistory)
                        .addContainerGap())))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOkActionPerformed
        FilterSyntaxParser parser = new FilterSyntaxParser(jTextFieldExpression.getText().trim());
        try {
            filter = parser.getFilter();
            ok = true;
            filterString = jTextFieldExpression.getText().trim();
            saveLogicalHistory(filterString);
            saveSettings();
            setVisible(false);
        } catch(SyntaxException ex) {
            ok = false;
            JOptionPane.showMessageDialog(LogicalExpressionInputDialog.this, "Error when parsing expression. Check help for info.", "Syntax error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButtonOkActionPerformed

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        ok = false;
        setVisible(false);
    }//GEN-LAST:event_jButtonCancelActionPerformed

    private void jButtonHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonHelpActionPerformed
        ShowHelpFrame help = new ShowHelpFrame("wowlogparser/gui/help/filterhelp.txt");
        help.setLocationRelativeTo(this);
        help.setVisible(true);
    }//GEN-LAST:event_jButtonHelpActionPerformed

    private void jButtonHistoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonHistoryActionPerformed
    StringPrefsDb db = new StringPrefsDb(prefs);
    List<String> strings = db.getSavedStrings();
    if (strings.size() == 0) {
        JOptionPane.showMessageDialog(LogicalExpressionInputDialog.this, "No history exists. History is updated each time ok is pressed with a valid filter.", "Alert", JOptionPane.WARNING_MESSAGE);
    } else {
        StringChooser chooser = new StringChooser(null, true, strings);
        chooser.setLocationRelativeTo(LogicalExpressionInputDialog.this);
        chooser.setVisible(true);
        if (chooser.okPressed) {
            String chosenString = chooser.getChosenString();
            jTextFieldExpression.setText(chosenString);
        }
    }
    }//GEN-LAST:event_jButtonHistoryActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonHelp;
    private javax.swing.JButton jButtonHistory;
    private javax.swing.JButton jButtonOk;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField jTextFieldExpression;
    // End of variables declaration//GEN-END:variables

}