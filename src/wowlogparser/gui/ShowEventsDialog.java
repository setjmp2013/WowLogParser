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

import wowlogparserbase.TimePeriod;
import wowlogparserbase.EventCollection;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.prefs.Preferences;
import javax.swing.JDialog;
import wowlogparser.*;

/**
 *
 * @author racy
 */
public class ShowEventsDialog extends JDialog {
    
    ShowEventsPanel panel;
    
   public ShowEventsDialog(Frame owner, String title, boolean modal, ArrayList<TimePeriod> tps, EventCollection ec, File f, Preferences basePrefs) {
        super(owner, title, modal);
        panel = new ShowEventsPanel(tps, ec, f, basePrefs);
        getContentPane().add(panel);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addListeners();
    }

   public ShowEventsDialog(Frame owner, boolean modal, ArrayList<TimePeriod> tps, EventCollection ec, File f, Preferences basePrefs) {
        super(owner, "All events (even vs other mobs) during the duration of the selected fights.", modal);
        panel = new ShowEventsPanel(tps, ec, f, basePrefs);
        getContentPane().add(panel);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addListeners();
    }

    public ShowEventsDialog(Frame owner, boolean modal, ArrayList<TimePeriod> tps, EventCollection ec, ShowEventsCallback cb, Preferences basePrefs) {
        super(owner, "All events (even vs other mobs) during the duration of the selected fights.", modal);
        panel = new ShowEventsPanel(tps, ec, cb, basePrefs);
        getContentPane().add(panel);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addListeners();
    }
    
    public ShowEventsDialog(Frame owner, String title, boolean modal, ArrayList<TimePeriod> tps, EventCollection ec, ShowEventsCallback cb, Preferences basePrefs) {
        super(owner, title, modal);
        panel = new ShowEventsPanel(tps, ec, cb, basePrefs);
        getContentPane().add(panel);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addListeners();
    }

    private void addListeners() {
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                panel = null;
                super.windowClosing(e);
            }
        });
    }
}