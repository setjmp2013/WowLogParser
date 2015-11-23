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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.prefs.Preferences;
import javax.swing.JFrame;
import wowlogparserbase.EventCollection;
import wowlogparserbase.TimePeriod;

/**
 *
 * @author racy
 */
public class ShowEventsFrame extends JFrame {
    ShowEventsPanel panel;

   public ShowEventsFrame(String title, ArrayList<TimePeriod> tps, EventCollection ec, File f, Preferences basePrefs) {
        super(title);
        panel = new ShowEventsPanel(tps, ec, f, basePrefs);
        getContentPane().add(panel);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        addListeners();
    }

   public ShowEventsFrame(ArrayList<TimePeriod> tps, EventCollection ec, File f, Preferences basePrefs) {
        super("All events (even vs other mobs) during the duration of the selected fights.");
        panel = new ShowEventsPanel(tps, ec, f, basePrefs);
        getContentPane().add(panel);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        addListeners();
    }

    public ShowEventsFrame(ArrayList<TimePeriod> tps, EventCollection ec, ShowEventsCallback cb, Preferences basePrefs) {
        super("All events (even vs other mobs) during the duration of the selected fights.");
        panel = new ShowEventsPanel(tps, ec, cb, basePrefs);
        getContentPane().add(panel);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        addListeners();
    }

    public ShowEventsFrame(String title, ArrayList<TimePeriod> tps, EventCollection ec, ShowEventsCallback cb, Preferences basePrefs) {
        super(title);
        panel = new ShowEventsPanel(tps, ec, cb, basePrefs);
        getContentPane().add(panel);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
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
