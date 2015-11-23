/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wowlogparser.gui;

import wowlogparser.*;
import java.awt.Frame;
import java.util.prefs.Preferences;
import javax.swing.JDialog;

/**
 *
 * @author racy
 */
public class SettingsDialog extends JDialog {

    Preferences prefs;
    SettingsPanel panel;
    public SettingsDialog(Frame owner, String title, boolean modal, Preferences prefs) {
        super(owner, title, modal);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.prefs = prefs;
        
        initComponents();
    }

    public void initComponents() {
        panel = new SettingsPanel(this, prefs);
        getContentPane().add(panel);
        pack();        
    }
    
}
