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

import wowlogparserbase.helpers.ChartHelper;
import java.awt.BorderLayout;
import wowlogparserbase.Fight;
import wowlogparserbase.FightParticipant;
import wowlogparserbase.AmountAndCount;
import java.awt.Color;
import java.awt.Graphics;
import java.math.BigDecimal;
import java.util.ArrayList;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.xy.*;
import wowlogparser.*;
import wowlogparserbase.EventCollection;
import wowlogparserbase.EventCollectionOptimizedAccess;
import wowlogparserbase.EventCollectionSimple;
import wowlogparserbase.SettingsSingleton;
import wowlogparserbase.WlpPlotFactory;
import wowlogparserbase.events.BasicEvent;

/**
 *
 * @author racy
 */
public class AmountGraph extends JPanel {

    JFreeChart chart = null;
    ChartPanel chartPanel = null;
    DefaultXYDataset damageDataset = null;
    DefaultXYDataset healingDataset = null;
    DefaultXYDataset powerDataset = null;
    DefaultXYDataset damageHealingDataset = null;

    public AmountGraph() {
        setLayout(new BorderLayout());
    }
    
    public void dispose() {
        removeAll();
        chart = null;
        chartPanel = null;
        damageDataset = null;
        healingDataset = null;
        powerDataset = null;
    }
        
    public void setParticipant(EventCollection ec, EventCollectionOptimizedAccess p, double updateInterval) {

        double[][] damageData = WlpPlotFactory.createParticipantDamageLinePlotData(ec, p, updateInterval);
        double[][] healingData = WlpPlotFactory.createParticipantHealingLinePlotData(ec, p, updateInterval);
        double[][] powerData = WlpPlotFactory.createParticipantPowerLinePlotData(ec, p, updateInterval);
        
        if (damageData[0].length == 2) {
            setToolTipText("Amount of Damage, Healing or Power per second. \n. The fight is too short for the update interval, showing only average DPS,HPS,PPS over the whole fight");
        } else {
            setToolTipText("Amount of Damage, Healing or Power per second. \n. Averaging interval "+updateInterval +" seconds.");
        }
        damageDataset = new DefaultXYDataset();
        healingDataset = new DefaultXYDataset();
        powerDataset = new DefaultXYDataset();
        damageHealingDataset = new DefaultXYDataset();

        damageDataset.addSeries("Damage", damageData);
        healingDataset.addSeries("Healing", healingData);
        powerDataset.addSeries("Power", powerData);
        damageHealingDataset.addSeries("Damage", damageData);
        damageHealingDataset.addSeries("Healing", healingData);
    }

    void setDamageGraph() {
        removeAll();
        chart = WlpPlotFactory.createDamageLinePlot(damageDataset, SettingsSingleton.getInstance().getSplinePlots());
        chartPanel = new ChartPanel(chart);
        add(chartPanel, BorderLayout.CENTER);
        ChartHelper.addScrollWheelZoom(chartPanel);
        revalidate();
    }
    
    void setHealingGraph() {
        removeAll();
        chart = WlpPlotFactory.createHealingLinePlot(healingDataset, SettingsSingleton.getInstance().getSplinePlots());
        chartPanel = new ChartPanel(chart);
        add(chartPanel, BorderLayout.CENTER);
        ChartHelper.addScrollWheelZoom(chartPanel);
        revalidate();
    }

    void setPowerGraph() {
        removeAll();
        chart = WlpPlotFactory.createPowerLinePlot(powerDataset, SettingsSingleton.getInstance().getSplinePlots());
        chartPanel = new ChartPanel(chart);
        add(chartPanel, BorderLayout.CENTER);
        ChartHelper.addScrollWheelZoom(chartPanel);
        revalidate();
    }

    void setDamageAndHealingGraph() {
        removeAll();
        chart = WlpPlotFactory.createDamageAndHealingLinePlot(damageHealingDataset, SettingsSingleton.getInstance().getSplinePlots());
        chartPanel = new ChartPanel(chart);
        add(chartPanel, BorderLayout.CENTER);
        ChartHelper.addScrollWheelZoom(chartPanel);
        revalidate();
    }

    void setNoGraph() {
        removeAll();
    }

}
