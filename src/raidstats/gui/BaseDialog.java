/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package raidstats.gui;

import java.awt.Frame;
import javax.swing.JDialog;

/**
 *
 * @author racy
 */
public class BaseDialog extends JDialog {

    BasePanel panel;
    public BaseDialog(Frame owner) {
        super(owner,"Parse window for raidstats.com", true);
        panel = new BasePanel(this);
        getContentPane().add(panel);
        pack();
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    public static void main(String[] args) {
        BaseDialog dia = new BaseDialog(null);
        dia.setLocationByPlatform(true);
        dia.setVisible(true);
    }
}
