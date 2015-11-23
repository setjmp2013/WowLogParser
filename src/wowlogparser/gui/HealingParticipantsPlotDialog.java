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

import java.awt.Frame;
import java.util.prefs.Preferences;
import javax.swing.JDialog;
import wowlogparserbase.*;

/**
 *
 * @author racy
 */
public class HealingParticipantsPlotDialog extends JDialog {
    FightParticipant participant;
    Fight fight;
    Preferences basePrefs;
    
    HealingParticipantsPlotPanel panel;

    public HealingParticipantsPlotDialog(Frame owner, String title, boolean modal, Fight fight, FightParticipant participant, Preferences basePrefs) {
        super(owner, title, modal);
        this.participant = participant;
        this.fight = fight;
        this.basePrefs = basePrefs;
        initComponents();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    public void initComponents() {
        panel = new HealingParticipantsPlotPanel(fight, participant, basePrefs);
        getContentPane().add(panel);
    }
}
