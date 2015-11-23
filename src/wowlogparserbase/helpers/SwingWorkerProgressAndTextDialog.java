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
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.ExecutionException;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import org.jfree.layout.CenterLayout;

/**
 * Swing worker dialog.
 * The swing worker thread should report its progress as a PropertyChangeEvent with name "progress". The value should be a number between 0 and 100.
 * The swing worker can change the JComponent to show by reporting a PropertyChangeEvent with name "component" and with the new component as the value.
 * @author racy
 */
public class SwingWorkerProgressAndTextDialog extends JDialog implements PropertyChangeListener {
    SwingWorker worker;
    JPanel panel;
    JPanel compPanel;
    JTextArea textArea;
    JProgressBar pbar;
    JButton cancelButton;

    /**
     * Constructor
     * @param worker The worker thread to execute
     * @param owner The owner frame
     * @param title A title string
     */
    public SwingWorkerProgressAndTextDialog(SwingWorker worker, Frame owner, String title) {
        super(owner, title, true);
        this.worker = worker;
        worker.addPropertyChangeListener(this);
        panel = new JPanel(new BorderLayout());
        pbar = new JProgressBar(0, 100);
        pbar.setValue(0);
        panel.add(pbar, BorderLayout.SOUTH);
        compPanel = new JPanel(new BorderLayout());
        textArea = new JTextArea();
        textArea.setColumns(60);
        textArea.setRows(10);
        textArea.setEditable(false);
        compPanel.add(new JScrollPane(textArea), BorderLayout.CENTER);
        cancelButton = new JButton("Cancel");
        compPanel.add(cancelButton, BorderLayout.SOUTH);
        panel.add(compPanel, BorderLayout.CENTER);
        add(panel);

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingWorkerProgressAndTextDialog.this.worker.cancel(true);
            }
        });
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                SwingWorkerProgressAndTextDialog.this.worker.cancel(true);
            }
        });
        pack();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress".equalsIgnoreCase(evt.getPropertyName())) {
            Number n = (Number)evt.getNewValue();
            int progress = n.intValue();
            pbar.setValue(progress);
        }
        if ("state".equalsIgnoreCase(evt.getPropertyName()) && evt.getNewValue() == SwingWorker.StateValue.DONE) {
            pbar.setValue(100);
            setVisible(false);
        }
        if ("text".equalsIgnoreCase(evt.getPropertyName())) {
            textArea.append(evt.getNewValue().toString());
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
                double tot = (endTime-startTime);
                while (true) {
                    long time = System.currentTimeMillis();
                    setProgress((int)((time - startTime)/tot*100.0));
                    if (time > endTime) {
                        break;
                    }
                    if ((time - startTime) > tot/2) {
                        firePropertyChange("text", null, "Over half way " + time + "\n");
                    }
                    Thread.sleep(1);
                }
                return 53;
            }

        };
        SwingWorkerProgressAndTextDialog d = new SwingWorkerProgressAndTextDialog(worker, null, "Worker test");
        d.setLocationByPlatform(true);
        d.setVisible(true);
        System.out.println(d.getResult());
        System.exit(0);
    }
}
