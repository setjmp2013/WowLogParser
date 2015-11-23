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

import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import wowlogparserbase.Fight;
import wowlogparserbase.FightParticipant;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.event.ComponentAdapter;
import java.awt.event.WindowAdapter;
import java.util.prefs.Preferences;
import javax.swing.JDialog;
import wowlogparser.*;
import wowlogparserbase.EventCollection;

/**
 *
 * @author racy
 */
public class ParticipantDialog extends JDialog {

    public ParticipantPanel panel = null;
    
    /**
     * 
     * @param owner
     * @param modal
     * @param ec All the events for the fight to show, for time calculation purposes
     * @param p
     */
    public ParticipantDialog(Frame owner, boolean modal, EventCollection ec, FightParticipant p, Preferences basePrefs) {
        super(owner, modal);
        setModalityType(ModalityType.DOCUMENT_MODAL);
        initComponents(ec, p, basePrefs);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                panel.dispose();
                panel = null;
                super.windowClosing(e);
            }
        });
        
        this.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentShown(ComponentEvent e) {
                super.componentShown(e);
                panel.dialogShown();
            }
        });
    }

    public void initComponents(EventCollection ec, FightParticipant p, Preferences basePrefs) {
        panel =  new ParticipantPanel(ec, p, basePrefs);
        getContentPane().add(panel);
    }
}
