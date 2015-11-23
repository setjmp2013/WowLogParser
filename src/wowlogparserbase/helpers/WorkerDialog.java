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
import java.awt.Dialog;
import java.awt.Frame;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * A dialog class that starts a Worker thread.
 * @author racy
 */
public class WorkerDialog extends JDialog {

    protected Worker p;
    protected String titleStr;
    protected String textStr;
    protected JPanel panel;
    protected JLabel label;
    protected String message;

    public WorkerDialog(Frame owner, String title, boolean modal, Worker p, String message) {
        super(owner, title, modal);
        this.message = message;
        this.p = p;
        init();
    }
    public WorkerDialog(Frame owner, String title, boolean modal, Worker p) {
        super(owner, title, modal);
        this.message = "Working, please wait...";
        this.p = p;
        init();
    }

    public WorkerDialog(Dialog owner, String title, boolean modal, Worker p, String message) {
        super(owner, title, modal);
        this.message = message;
        this.p = p;
        init();
    }
    public WorkerDialog(Dialog owner, String title, boolean modal, Worker p) {
        super(owner, title, modal);
        this.message = "Working, please wait...";
        this.p = p;
        init();
    }

    public void init() {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        panel = new JPanel(new BorderLayout());
        label = new JLabel(message);
        getContentPane().add(panel);
        panel.add(label, BorderLayout.CENTER);
        pack();        
    }
    
    @Override
    public void setVisible(boolean b) {
        if (!isVisible() && b) {
            p.setDialog(this);
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    p.start();
                }
            });
        }
        super.setVisible(b);
    }
    
    /**
     * Number between 0 and 100.
     * @param progress progress between 0 and 100
     */
    public void setProgress(int progress) {
        
    }
    
    public synchronized void setWorkerTitle(String text) {
        titleStr = text;
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                setTitle(titleStr);
            }
        });
    }

    public void setWorkerText(String text) {
        textStr = text;
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                label.setText(textStr);
                pack();
            }
        });
    }
}
