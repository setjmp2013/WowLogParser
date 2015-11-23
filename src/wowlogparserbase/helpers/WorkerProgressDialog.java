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

import java.awt.Dialog;
import java.awt.Frame;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

/**
 *
 * @author racy
 */
public class WorkerProgressDialog extends WorkerDialog {
    JProgressBar bar;
    int progress;
    public WorkerProgressDialog(Frame owner, String title, boolean modal, Worker p, String message) {
        super(owner, title, modal, p, message);
        initProgressDialog();
    }

    public WorkerProgressDialog(Dialog owner, String title, boolean modal, Worker p, String message) {
        super(owner, title, modal, p, message);
        initProgressDialog();
    }

    protected void initProgressDialog() {
        getContentPane().removeAll();
        panel = new JPanel();
        label = new JLabel(message);
        bar = new JProgressBar(0, 100);
        
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        getContentPane().add(panel);
        panel.add(label);
        panel.add(bar);
        pack();        
    }
    
    @Override
    public synchronized void setProgress(int progress) {
        this.progress = progress;
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                bar.setValue(WorkerProgressDialog.this.progress);
            }
        });
    }

}
