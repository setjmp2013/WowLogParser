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
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Stroke;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jfree.chart.*;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.DefaultXYDataset;
import wowlogparserbase.*;
import wowlogparserbase.eventfilter.Filter;
import wowlogparserbase.eventfilter.FilterClass;
import wowlogparserbase.eventfilter.FilterGuidOr;
import wowlogparserbase.events.BasicEvent;
import wowlogparserbase.Constants;
import wowlogparserbase.events.damage.DamageEvent;
import wowlogparserbase.events.healing.HealingEvent;

/**
 *
 * @author  gustav
 */
public class HealingParticipantsPlotPanel extends javax.swing.JPanel {

    FightParticipant participant;
    FightParticipant receivedParticipant;
    Fight fight;
    Preferences prefs;
    
    boolean preferredSet = false;
    Dimension preferred;
    
    List<PersonInfo> healers;
    JFreeChart chart;
    ChartPanel chartPanel;

    public HealingParticipantsPlotPanel() {
        initComponents();
    }
    
    /** Creates new form HealingParticipantsPlotPanel */
    public HealingParticipantsPlotPanel(Fight fight, FightParticipant participant, Preferences basePrefs) {
        initComponents();
        init(fight, participant, basePrefs);
    }

    private void init(Fight fight, FightParticipant participant, Preferences basePrefs) {
        this.participant = participant;
        this.fight = fight;
        this.receivedParticipant = participant.makeReceivedParticipant(fight);
        this.prefs = basePrefs.node("HealingParticipantsPlotPanel");
        jCheckBoxDots.setSelected(prefs.getBoolean("dots", true));
        jCheckBoxHealth.setSelected(prefs.getBoolean("health", true));
        preferred = jPanelPlot.getPreferredSize();
        jPanelPlot.setLayout(new BorderLayout());
        initPlot(jSlider1.getValue());
        jSlider1.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (!jSlider1.getValueIsAdjusting()) {
                    initPlot(jSlider1.getValue());
                }
            }
        });

        jCheckBoxDots.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (jCheckBoxDots.isSelected()) {
                    jSlider1.setEnabled(false);
                } else {
                    jSlider1.setEnabled(true);
                }
                initPlot(jSlider1.getValue());
                prefs.putBoolean("dots", jCheckBoxDots.isSelected());
            }
        });

        jCheckBoxHealth.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                initPlot(jSlider1.getValue());
                prefs.putBoolean("health", jCheckBoxHealth.isSelected());
            }
        });
    }

    public void initPlot(double interval) {
        if (jCheckBoxDots.isSelected()) {
            initPlotDots();
        } else {
            initPlotLines(interval);
        }
        if (!preferredSet) {
            chartPanel.setPreferredSize(preferred);
            preferredSet = true;
        }
    }

    public void makeHealthDeficitArray(List<BasicEvent> events, double fightStartTime, List<Double> x, EventCollection y) {
        for (BasicEvent e : events) {
            if (e instanceof DamageEvent) {
                DamageEvent de = (DamageEvent) e;
                if (de.getDamage() > 0) {
                    x.add(de.time - fightStartTime);
                    y.addEvent(de);
                }
            }
            if (e instanceof HealingEvent) {
                HealingEvent he = (HealingEvent) e;
                if (he.getHealing() > 0) {
                    x.add(he.time - fightStartTime);
                    y.addEvent(he);
                }
            }
        }
    }
    
    public void makeHealthDeficitArray(List<BasicEvent> events, double fightStartTime, List<Double> x, List<Double> y) {
        for (BasicEvent e : events) {
            if (e instanceof DamageEvent) {
                DamageEvent de = (DamageEvent) e;
                if (de.getDamage() > 0) {
                    x.add(de.time - fightStartTime);
                    y.add((double)de.getHealthDeficit());
                }
            }
            if (e instanceof HealingEvent) {
                HealingEvent he = (HealingEvent) e;
                if (he.getHealing() > 0) {
                    x.add(he.time - fightStartTime);
                    y.add((double)he.getHealthDeficit());
                }
            }
        }
    }

    public void initPlotDots() {
        double fightStartTime = fight.getStartTime();

        EventCollectionSimple ec = new EventCollectionSimple(receivedParticipant.getEvents());
        healers = getHealers(ec.getEvents());
       
        //Make damage array
        EventCollectionSimple damage = ec.filter(new FilterClass(DamageEvent.class));
        List<Double> damageArrayX = new ArrayList<Double>();
        for (BasicEvent e : damage.getEvents()) {
            damageArrayX.add(e.time - fightStartTime);
        }        

        //Make health array
        ArrayList<Double> healthDeficitArrayX = new ArrayList<Double>();
        EventCollectionSimple healthDeficit = new EventCollectionSimple();
        makeHealthDeficitArray(ec.getEvents(), fightStartTime, healthDeficitArrayX, healthDeficit);

        // Make healing arrays.
        for (PersonInfo pi : healers) {
            FilterClass fc = new FilterClass(HealingEvent.class);
            FilterGuidOr fg = new FilterGuidOr(pi.getGuid(), "apa");
            List<Filter> filters = new ArrayList<Filter>();
            filters.add(fc);
            filters.add(fg);
            EventCollectionSimple personEvents = ec.filterAnd(filters);
            
            ArrayList<Double> personHealingX = new ArrayList<Double>();
            for (BasicEvent e : personEvents.getEvents()) {
                personHealingX.add(e.time - fightStartTime);
            }
            pi.setEvents(personEvents);
            pi.setXVals(personHealingX);
        }
        
        //Make plot
        boolean chartMade = false;
        //DefaultXYDataset dataset = new DefaultXYDataset();
        EventXYDataset eDataset = new EventXYDataset();

        //Damage series
        eDataset.addSeries("Damage(dmg)", damageArrayX, damage, false);
        
        //Health deficit series
        if (jCheckBoxHealth.isSelected()) {
            eDataset.addSeries("Health Deficit(-health)", healthDeficitArrayX, healthDeficit, true);
        }

        //Healing seriees
        for (PersonInfo pi : healers) {
            eDataset.addSeries(pi.getName(), pi.getXVals(), pi.getEvents(), false);
        }
        chart = ChartFactory.createScatterPlot("Healing for all healers, and received damage", "Time (s)", "Healing/Damage", eDataset, PlotOrientation.VERTICAL, true, true, false);
        chart.getXYPlot().getRenderer().setBaseToolTipGenerator(new EventXYTooltipGenerator());
        chartPanel = new ChartPanel(chart, true);
        chartPanel.setReshowDelay(100);
        chartPanel.setInitialDelay(10);
        ChartHelper.addScrollWheelZoom(chartPanel);       
        setBrushes(chart.getXYPlot());
        jPanelPlot.removeAll();
        jPanelPlot.add(chartPanel);
        jPanelPlot.revalidate();        
    }

    public void initPlotLines(double interval) {
        double fightStartTime = fight.getStartTime();
        double fightEndTime = fight.getEndTime();
        double totalTime = fightEndTime - fightStartTime;
        int numVals = (int)Math.round(totalTime / interval);
        double[] xVal = new double[numVals];

        List<BasicEvent> events = receivedParticipant.getEvents();
        healers = getHealers(events);
        
        //Make damage array
        List<Double> damageArray = new ArrayList<Double>();
        for (int k = 0; k < numVals; k++) {
            double startTime = fightStartTime + (double) k * interval;
            double endTime = fightStartTime + (double) (k + 1) * interval;
            xVal[k] = (endTime + startTime) / 2.0 - fightStartTime;
            AmountAndCount a = receivedParticipant.totalDamage(Constants.SCHOOL_ALL, FightParticipant.TYPE_ALL_DAMAGE, FightParticipant.UNIT_ALL, null, startTime, endTime);
            damageArray.add((double)a.amount);
        }
        
        //Make health array
        ArrayList<Double> healthDeficitArrayY = new ArrayList<Double>();
        ArrayList<Double> healthDeficitArrayX = new ArrayList<Double>();
        makeHealthDeficitArray(events, fightStartTime, healthDeficitArrayX, healthDeficitArrayY);

        // Make healing arrays.
        for (PersonInfo pi : healers) {
            ArrayList<BasicEvent> personEvents = new ArrayList<BasicEvent>();
            for (BasicEvent e : events) {
                if (e.getSourceGUID().equals(pi.getGuid()) && (e instanceof HealingEvent)) {
                    personEvents.add(e);
                }
            }
            
            ArrayList<Double> personHealing = new ArrayList<Double>();
            for (int k=0; k<numVals; k++) {
                double startTime = fightStartTime + (double)k * interval;
                double endTime = fightStartTime + (double)(k+1) * interval;
                xVal[k] = (endTime + startTime) / 2.0 - fightStartTime;
                FightParticipant tempP = new FightParticipant();
                tempP.setEvents(personEvents);
                AmountAndCount a = tempP.totalHealing(Constants.SCHOOL_ALL, FightParticipant.TYPE_ALL_HEALING, FightParticipant.UNIT_ALL, null, startTime, endTime);
                personHealing.add((double)a.amount);
            }
            pi.setYVals(personHealing);
        }
        
        //Make plot
        boolean chartMade = false;
        DefaultXYDataset dataset = new DefaultXYDataset();

        //Damage series
        double[][] damageData = new double[2][numVals];
        for (int k = 0; k < numVals; k++) {
            damageData[0][k] = xVal[k];
            damageData[1][k] = damageArray.get(k) / interval;
        }
        dataset.addSeries("Damage(dps)", damageData);
        
        //Health deficit series
        if (jCheckBoxHealth.isSelected()) {
            double[][] healthDeficitData = new double[2][healthDeficitArrayX.size()];
            for (int k = 0; k < healthDeficitArrayX.size(); k++) {
                healthDeficitData[0][k] = healthDeficitArrayX.get(k);
                healthDeficitData[1][k] = healthDeficitArrayY.get(k);
            }
            dataset.addSeries("Health Deficit(-health)", healthDeficitData);
        }

        //Healing seriees
        for (PersonInfo pi : healers) {
            double[][] data = new double[2][numVals];
            for (int k=0; k<numVals; k++) {
                data[0][k] = xVal[k];
                data[1][k] = pi.getYVals().get(k) / interval;
            }
            dataset.addSeries(pi.getName(), data);
        }
        chart = ChartFactory.createXYLineChart("HPS for all healers", "Time (s)", "HPS", dataset, PlotOrientation.VERTICAL, true, true, false);
        chartPanel = new ChartPanel(chart, true);
        ChartHelper.addScrollWheelZoom(chartPanel);
        setBrushes(chart.getXYPlot());
        jPanelPlot.removeAll();
        jPanelPlot.add(chartPanel);
        jPanelPlot.revalidate();        
    }
    
    /**
     * Set brushes in the plot.
     * Assume that index 0 is damage.
     * @param plot
     */
    void setBrushes(XYPlot plot) {
        if (jCheckBoxDots.isSelected()) {
            plot.getRenderer().setSeriesStroke(0, null);
        } else {
            for (int k = 0; k < plot.getSeriesCount(); k++) {
                float width = 2;
                if (k == 0) {
                    width = 3;
                }
                Stroke s = plot.getRenderer().getSeriesStroke(k);
                if (s instanceof BasicStroke) {
                    BasicStroke bs = (BasicStroke) s;
                    BasicStroke newStroke = new BasicStroke(width, bs.getEndCap(), bs.getLineJoin(), bs.getMiterLimit(), bs.getDashArray(), bs.getDashPhase());
                    plot.getRenderer().setSeriesStroke(k, newStroke);
                }
                if (s == null) {
                    BasicStroke newStroke = new BasicStroke(width);
                    plot.getRenderer().setSeriesStroke(k, newStroke);
                }
            }
        }
    }
    
    List<PersonInfo> getHealers(List<BasicEvent> events) {
        HashSet<PersonInfo> healerGuids = new HashSet<PersonInfo>();
        for (BasicEvent e : events) {
            if (e instanceof HealingEvent) {
                HealingEvent he = (HealingEvent) e;
                healerGuids.add(new PersonInfo(he.getSourceName(), he.getSourceGUID()));
            }
        }
        return new ArrayList<PersonInfo>(healerGuids);
    }
    
    class PersonInfo implements Comparable {
        String name;
        String guid;
        EventCollectionSimple events;
        ArrayList<Double> xVals;
        ArrayList<Double> yVals;

        public PersonInfo(String name, String guid) {
            this.name = name;
            this.guid = guid;
        }

        public ArrayList<Double> getXVals() {
            return xVals;
        }

        public void setXVals(ArrayList<Double> xVals) {
            this.xVals = xVals;
        }
        
        public EventCollectionSimple getEvents() {
            return events;
        }

        public void setEvents(EventCollectionSimple events) {
            this.events = events;
        }

        public ArrayList<Double> getYVals() {
            return yVals;
        }

        public void setYVals(ArrayList<Double> yVals) {
            this.yVals = yVals;
        }
 
        public String getGuid() {
            return guid;
        }

        public String getName() {
            return name;
        }
        
        public int compareTo(Object o) {
            if (o instanceof PersonInfo) {
                PersonInfo in = (PersonInfo) o;
                return guid.compareTo(in.guid);
            } else {
                return -1;
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final PersonInfo other = (PersonInfo) obj;
            if (this.guid != other.guid && (this.guid == null || !this.guid.equals(other.guid))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 73 * hash + (this.guid != null ? this.guid.hashCode() : 0);
            return hash;
        }
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanelPlot = new javax.swing.JPanel();
        jPanelTools = new javax.swing.JPanel();
        jSlider1 = new javax.swing.JSlider();
        jCheckBoxDots = new javax.swing.JCheckBox();
        jCheckBoxHealth = new javax.swing.JCheckBox();

        jPanelPlot.setName("jPanelPlot"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanelPlotLayout = new org.jdesktop.layout.GroupLayout(jPanelPlot);
        jPanelPlot.setLayout(jPanelPlotLayout);
        jPanelPlotLayout.setHorizontalGroup(
            jPanelPlotLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 1000, Short.MAX_VALUE)
        );
        jPanelPlotLayout.setVerticalGroup(
            jPanelPlotLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 677, Short.MAX_VALUE)
        );

        jPanelTools.setName("jPanelTools"); // NOI18N

        jSlider1.setMajorTickSpacing(10);
        jSlider1.setMaximum(60);
        jSlider1.setMinimum(1);
        jSlider1.setMinorTickSpacing(1);
        jSlider1.setPaintLabels(true);
        jSlider1.setPaintTicks(true);
        jSlider1.setSnapToTicks(true);
        jSlider1.setValue(3);
        jSlider1.setName("jSlider1"); // NOI18N

        jCheckBoxDots.setText("Show dots everytime a heal or damage event is done");
        jCheckBoxDots.setName("jCheckBoxDots"); // NOI18N

        jCheckBoxHealth.setText("Show health deficit");
        jCheckBoxHealth.setName("jCheckBoxHealth"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanelToolsLayout = new org.jdesktop.layout.GroupLayout(jPanelTools);
        jPanelTools.setLayout(jPanelToolsLayout);
        jPanelToolsLayout.setHorizontalGroup(
            jPanelToolsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jSlider1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1000, Short.MAX_VALUE)
            .add(jPanelToolsLayout.createSequentialGroup()
                .add(145, 145, 145)
                .add(jPanelToolsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jCheckBoxHealth)
                    .add(jCheckBoxDots))
                .addContainerGap(574, Short.MAX_VALUE))
        );
        jPanelToolsLayout.setVerticalGroup(
            jPanelToolsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanelToolsLayout.createSequentialGroup()
                .addContainerGap()
                .add(jCheckBoxDots)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jCheckBoxHealth)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 18, Short.MAX_VALUE)
                .add(jSlider1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanelTools, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(jPanelPlot, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jPanelTools, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanelPlot, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBoxDots;
    private javax.swing.JCheckBox jCheckBoxHealth;
    private javax.swing.JPanel jPanelPlot;
    private javax.swing.JPanel jPanelTools;
    private javax.swing.JSlider jSlider1;
    // End of variables declaration//GEN-END:variables

}
