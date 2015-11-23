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
import javax.swing.JDialog;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.general.DefaultPieDataset;
import wowlogparserbase.Fight;
import wowlogparserbase.FightParticipant;
import wowlogparserbase.Constants;

/**
 *
 * @author racy
 */
public class FightHealingChartDialog extends JDialog {
    Fight fight;    
    public FightHealingChartDialog(Frame owner, boolean modal, Fight f) {
        super(owner, "Healing distribution - " + f.getName(), modal);
        fight = f;
        JFreeChart chart;
        DefaultPieDataset dataset = new DefaultPieDataset();
        //Set values
        for (FightParticipant p : fight.getParticipants()) {
            if (p.isPetAssignedSomewhere(fight.getParticipants())) {
                continue;
            }
            dataset.setValue(p.getName(),p.totalHealing(Constants.SCHOOL_ALL, FightParticipant.TYPE_ALL_HEALING).amount);
        }

        chart = ChartFactory.createPieChart("Healing distribution", dataset, true, true, false);        
        ChartPanel chartP = new ChartPanel(chart);
        PiePlot pie = (PiePlot)chart.getPlot();
        pie.setSimpleLabels(false);
        
        getContentPane().add(chartP);        
        pack();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
}
