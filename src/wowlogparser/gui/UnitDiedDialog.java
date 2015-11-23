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

import wowlogparserbase.events.LogEvent;
import wowlogparserbase.FileLoader;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JDialog;
import wowlogparser.*;
import wowlogparserbase.events.BasicEvent;

/**
 *
 * @author racy
 */
public class UnitDiedDialog extends JDialog {
    
    UnitDiedPanel p;
            
    public UnitDiedDialog(Frame owner, boolean modal, FileLoader fl, List<BasicEvent> evs, Preferences basePrefs) {
        super(owner, "Display the events that was done to someone just before he/she died.", modal);
        initComponents(fl, evs, basePrefs);
    }    
    
    public void initComponents(FileLoader fl, List<BasicEvent> evs, Preferences basePrefs) {
        p = new UnitDiedPanel(fl, evs, basePrefs);
        getContentPane().add(p);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                p.saveVariables();
                super.windowClosing(e);
                dispose();
            }
            
        });
    }

}
