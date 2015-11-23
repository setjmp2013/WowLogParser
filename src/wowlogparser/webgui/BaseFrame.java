/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wowlogparser.webgui;

import java.awt.Frame;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;

/**
 *
 * @author racy
 */
public class BaseFrame extends JFrame {

    BasePanel panel;
    public BaseFrame() {
        super("Package creator for Wlp Web application");
        panel = new BasePanel();
        panel.setOwner(this);
        getContentPane().add(panel);
        pack();
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    public static void main(String[] args) {
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
        BaseFrame dia = new BaseFrame();
        dia.setLocationByPlatform(true);
        dia.setVisible(true);
    }
}
