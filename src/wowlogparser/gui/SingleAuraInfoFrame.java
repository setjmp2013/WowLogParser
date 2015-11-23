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
/*
 * SingleAuraInfoFrame.java
 *
 * Created on 2010-jun-20, 15:04:25
 */
package wowlogparser.gui;

import javax.swing.JScrollPane;
import wowlogparserbase.EventCollection;

/**
 *
 * @author racy
 */
public class SingleAuraInfoFrame extends javax.swing.JFrame {

    /** Creates new form SingleAuraInfoFrame */
    public SingleAuraInfoFrame(String title, EventCollection ec, double startTime, double endTime) {
        super(title);
        initComponents();
        SingleAuraInfoPanel panel = new SingleAuraInfoPanel();
        getContentPane().add(panel);
        setSize(1024, 768);
        
        panel.setEvents(ec, startTime, endTime);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
