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

import wowlogparserbase.FightParticipant;
import wowlogparserbase.SpellInfo;
import wowlogparserbase.AmountAndCount;
import java.awt.Frame;
import java.util.List;
import javax.swing.JDialog;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.general.DefaultPieDataset;
import wowlogparser.*;
import wowlogparserbase.Constants;

/**
 *
 * @author racy
 */
public class ParticipantDamageChartDialog extends JDialog {
    FightParticipant participant;
    public ParticipantDamageChartDialog(Frame owner, boolean modal, FightParticipant p) {
        super(owner, "Damage distribution - " + p.getName(), modal);
        participant = p;
        DefaultPieDataset dataset = new DefaultPieDataset();
        
        List<SpellInfo> damageSpellIDs;
        List<SpellInfo> rangedSpellIDs;
        //damageSpellIDs = participant.getDamageSpellIDs();
        //rangedSpellIDs = participant.getRangedSpellIDs();
        damageSpellIDs = participant.getIDs(FightParticipant.TYPE_SPELL_DAMAGE);
        rangedSpellIDs = participant.getIDs(FightParticipant.TYPE_RANGED_DAMAGE);
        AmountAndCount a;
        //a = participant.totalMeleeDamage(BasicEvent.SCHOOL_ALL);
        a = participant.totalDamage(Constants.SCHOOL_ALL, FightParticipant.TYPE_SWING_DAMAGE);
        dataset.setValue("Melee", a.amount);
        for (SpellInfo si : damageSpellIDs) {
            a = participant.damage(si);
            dataset.setValue(si.name, a.amount);
        }
        for (SpellInfo si : rangedSpellIDs) {
            a = participant.damage(si);
            dataset.setValue(si.name, a.amount);
        }        
        
        JFreeChart chart;
        chart = ChartFactory.createPieChart("Damage distribution", dataset, true, true, false);        
        ChartPanel chartP = new ChartPanel(chart);
        PiePlot pie = (PiePlot)chart.getPlot();
        pie.setSimpleLabels(false);
        
        getContentPane().add(chartP);        
        pack();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
}
