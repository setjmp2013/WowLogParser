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
package wowlogparserbase.helpers;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import org.jfree.layout.CenterLayout;

/**
 * Swing worker dialog.
 * The swing worker can change the JComponent to show by reporting a PropertyChangeEvent with name "component" and with the new component as the value.
 * @author racy
 */
public class SwingWorkerDialog extends JDialog implements PropertyChangeListener {
    SwingWorker worker;
    JPanel panel;
    
    /**
     * Constructor
     * @param worker The worker thread to execute
     * @param comp A component to show, for example a JLabel
     * @param owner The owner frame
     * @param title A title string
     */
    public SwingWorkerDialog(SwingWorker worker, JComponent comp, Frame owner, String title) {
        super(owner, title, true);
        this.worker = worker;
        worker.addPropertyChangeListener(this);
        panel = new JPanel(new CenterLayout());
        panel.add(comp);
        add(panel);
        pack();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("state".equalsIgnoreCase(evt.getPropertyName()) && evt.getNewValue() == SwingWorker.StateValue.DONE) {
            setVisible(false);
        }
        if ("text".equalsIgnoreCase(evt.getPropertyName())) {
            panel.removeAll();
            panel.add(new JLabel(evt.getNewValue().toString()));
            pack();
        }
        if ("component".equalsIgnoreCase(evt.getPropertyName())) {
            JComponent comp = (JComponent)evt.getNewValue();
            panel.removeAll();
            Dimension d = panel.getSize();
            if (d.getHeight() < comp.getHeight()) {
                d.setSize(d.getWidth(), comp.getHeight());
            }
            if (d.getWidth() < comp.getWidth()) {
                d.setSize(comp.getWidth(), d.getHeight());
            }
            panel.setSize(d);
            panel.add(comp);
            panel.validate();
            pack();
            panel.repaint();
        }
   }

    /**
     * Get the result returned by the worker thread
     * @return The result object
     */
    public Object getResult() {
        Object result;
        if (worker.isCancelled()) {
            return null;
        }
        try {
            result = worker.get();
        } catch (InterruptedException ex) {
            result = null;
        } catch (ExecutionException ex) {
            result = null;
        }
        return result;
    }
    
    @Override
    public void setVisible(boolean b) {
        if (!isVisible()) {
            worker.execute();
            super.setVisible(b);
        } else {
            super.setVisible(b);
        }
    }
        
    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("Unable to load native look and feel");
        }

        SwingWorker worker = new SwingWorker() {

            @Override
            protected Object doInBackground() throws Exception {
                long startTime = System.currentTimeMillis();
                long endTime = startTime + 5000;
                double tot = (endTime - startTime);
                while (true) {
                    long time = System.currentTimeMillis();
                    if (time > endTime) {
                        break;
                    }
                    if (time - startTime > tot/2) {
                        firePropertyChange("component", null, new JLabel("Half way! aghbaeiugbaeigubiugb"));
                    }
                    Thread.sleep(1);
                }
                return 75;
            }

        };
        SwingWorkerDialog d = new SwingWorkerDialog(worker, new JLabel("Testing swing worker dialog"), null, "Worker test");
        d.setLocationByPlatform(true);
        d.setVisible(true);        
        System.out.println(d.getResult());
        System.exit(0);
    }
}
