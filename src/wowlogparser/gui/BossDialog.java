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

import java.awt.BorderLayout;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JDialog;
import wowlogparserbase.Fight;
import wowlogparserbase.FileLoader;

/**
 *
 * @author racy
 */
public class BossDialog extends JDialog {
    List<Fight> fights;
    BossPanel bossPanel;
    FileLoader fl;
    
    public BossDialog(Frame owner, String title, boolean modal, List<Fight> fights, FileLoader fl) {
        super(owner, title, modal);
        this.fights = fights;
        this.fl = fl;
        initComponents();
    }

    public void initComponents() {
        bossPanel = new BossPanel(this, fights, fl);
        getContentPane().add(bossPanel);
        pack();
    }
    
    public List<Fight> getOutFights() {
        return bossPanel.getOutFights();
    }
}
