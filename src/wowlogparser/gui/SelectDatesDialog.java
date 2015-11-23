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

import wowlogparserbase.helpers.VerticalLayout;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import wowlogparserbase.WlpDate;

/**
 *
 * @author racy
 */
public class SelectDatesDialog extends JDialog {
    List<WlpDate> dates;
    List<Boolean> checkStates;
    List<JCheckBox> boxes;
    public SelectDatesDialog(Set<WlpDate> dates, Frame owner, String title, boolean modal) {
        super(owner, title, modal);
        this.dates = new ArrayList<WlpDate>(dates);
        initComponents();
        pack();
    }

    private void initComponents() {
        JPanel p = new JPanel(new BorderLayout());
        add(p);
        JPanel pCenter = new JPanel(new VerticalLayout());
        JPanel pBottom = new JPanel(new FlowLayout());
        p.add(pCenter, BorderLayout.CENTER);
        p.add(pBottom, BorderLayout.SOUTH);
        boxes = new ArrayList<JCheckBox>();
        checkStates = new ArrayList<Boolean>();
        for (WlpDate d : dates) {
            JCheckBox b = new JCheckBox("" + d.getDay() + " / " + d.getMonth());
            b.setSelected(true);
            pCenter.add(b);
            boxes.add(b);
            checkStates.add(true);
        }
        JButton okButton = new JButton("Ok");
        pBottom.add(okButton);
        okButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                for (int k=0; k<boxes.size(); k++) {
                    checkStates.set(k, boxes.get(k).isSelected());
                }
                SelectDatesDialog.this.dispose();
            }
        });
    }
    
    public Set<WlpDate> getDates() {
        Set<WlpDate> outDates = new TreeSet<WlpDate>();
        for (int k=0; k<checkStates.size(); k++) {
            boolean selected = checkStates.get(k);
            if (selected) {
                outDates.add(dates.get(k));
            }
        }
        return outDates;
    }
}
