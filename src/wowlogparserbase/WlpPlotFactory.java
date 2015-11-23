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
package wowlogparserbase;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.DefaultXYDataset;
import wowlogparserbase.events.BasicEvent;

/**
 * Factory class for creating plot stuff.
 * @author racy
 */
public class WlpPlotFactory {

    /**
     * Create damage plot data for a participant that is present in for example a fight.
     * Includes all unit types.
     * @param ec All events from a fight.
     * @param p The participant to make the plot for.
     * @param updateInterval The update interval for the plot.
     * @return A chart showing the damage done.
     */
    public static double[][] createParticipantDamageLinePlotData(EventCollection ec, EventCollectionOptimizedAccess p, double updateInterval) {
        return createParticipantDamageLinePlotData(ec, p, updateInterval, FightParticipant.UNIT_ALL);
    }
    
    /**
     * Create damage plot data for a participant that is present in for example a fight.
     * @param ec All events from a fight.
     * @param p The participant to make the plot for.
     * @param updateInterval The update interval for the plot.
     * @param unitType The unit type to include
     * @return A chart showing the damage done.
     */
    public static double[][] createParticipantDamageLinePlotData(EventCollection ec, EventCollectionOptimizedAccess p, double updateInterval, int unitType) {
        int k;
        // f.getEndTime() - f.getStartTime() is different than f.getDuration() because
        // merged fights have a duration that is the sum of the individual fights
        double duration = ec.getEndTime() - ec.getStartTime();
        int numVals = (int)Math.round(duration/updateInterval);
        if (numVals < 2) {
            numVals = 2;
            if (duration == 0) {
                duration = 0.1;
            }
        }
        List<Double> valsyDam = new ArrayList<Double>();
        List<Double> valsx = new ArrayList<Double>();

        //Special case with 2, only show average damage
        if (numVals == 2) {
            double startTime = ec.getStartTime();
            double endTime = startTime + duration;
            //AmountAndCount a = p.totalDamage(startTime, endTime);
            AmountAndCount a = p.totalDamage(Constants.SCHOOL_ALL, FightParticipant.TYPE_ALL_DAMAGE, unitType, null, startTime, endTime);
            for (k = 0; k < numVals; k++) {
                valsyDam.add( (double) a.amount / duration);
                valsx.add( (double)k * duration);
            }            
        } else {
            double zeroPeriod = 0;
            boolean skipping = false;
            double startTime = ec.getStartTime();
            p.getEventsContinueFromLastReset();
            double roughUpdateInterval = updateInterval*3.01;
            double roughEndTime = Double.NEGATIVE_INFINITY;
            EventCollection intervalEvents = new FightParticipant();
            for (k = 0; k < numVals; k++) {
                double endTime = startTime + (k + 1) * updateInterval;
                if (endTime > startTime + duration) {
                    endTime = startTime + duration;
                }
                if (roughEndTime<endTime) {
                    intervalEvents = p.getEventsContinueFromLast(endTime-updateInterval-0.01, endTime-updateInterval+roughUpdateInterval);
                    roughEndTime = endTime-updateInterval+roughUpdateInterval;
                }

                AmountAndCount a;
                AmountAndCount a2;
                AmountAndCount a3;
                // Pre check to see if nothing was done
                if (intervalEvents.getNumEventsTotal() > 0) {
                    // Find out if we have a period with nothing done
                    a = intervalEvents.totalDamage(Constants.SCHOOL_ALL, FightParticipant.TYPE_ALL_DAMAGE, unitType, null, endTime-updateInterval, endTime);
                    a2 = intervalEvents.totalHealing(Constants.SCHOOL_ALL, FightParticipant.TYPE_ALL_HEALING, unitType, null, endTime-updateInterval, endTime);
                    a3 = intervalEvents.totalPower(Constants.SCHOOL_ALL, FightParticipant.TYPE_ALL_POWER, unitType, null, endTime-updateInterval, endTime);
                } else {
                    a = DefaultObjects.emptyAmountAndCount;
                    a2 = DefaultObjects.emptyAmountAndCount;
                    a3 = DefaultObjects.emptyAmountAndCount;
                }
                if (a.amount == 0 && a2.amount == 0 && a3.amount == 0) {
                    zeroPeriod+=updateInterval;
                } else {
                    zeroPeriod = 0;
                }
                if (zeroPeriod < updateInterval*2) {
                    if (skipping) {
                        valsyDam.add(0.0);
                        valsx.add((double) (k-1) * updateInterval + updateInterval / 2.0);
                    }
                    skipping = false;
                    valsyDam.add((double) a.amount / updateInterval);
                    valsx.add((double) k * updateInterval + updateInterval / 2.0);
                } else {
                    skipping = true;
                }
            }
        }

        double[][] damageData = new double[2][valsx.size()];
        for (k=0; k<valsx.size(); k++) {
            damageData[0][k] = valsx.get(k);
            damageData[1][k] = valsyDam.get(k);
        }
        return damageData;
    }

    public static double[][] createParticipantHealingLinePlotData(EventCollection ec, EventCollectionOptimizedAccess p, double updateInterval) {
        return createParticipantHealingLinePlotData(ec, p, updateInterval, FightParticipant.UNIT_ALL);
    }

    public static double[][] createParticipantHealingLinePlotData(EventCollection ec, EventCollectionOptimizedAccess p, double updateInterval, int unitType) {
        int k;
        // f.getEndTime() - f.getStartTime() is different than f.getDuration() because
        // merged fights have a duration that is the sum of the individual fights
        double duration = ec.getEndTime() - ec.getStartTime();
        int numVals = (int)Math.round(duration/updateInterval);
        if (numVals < 2) {
            numVals = 2;
            if (duration == 0) {
                duration = 0.1;
            }
        }
        ArrayList<Double> valsyHeal = new ArrayList<Double>();
        ArrayList<Double> valsx = new ArrayList<Double>();

        //Special case with 2, only show average damage
        if (numVals == 2) {
            double startTime = ec.getStartTime();
            double endTime = startTime + duration;
            AmountAndCount a2 = p.totalHealing(Constants.SCHOOL_ALL, FightParticipant.TYPE_ALL_HEALING, unitType, null, startTime, endTime);
            for (k = 0; k < numVals; k++) {
                valsyHeal.add( (double) a2.amount / duration);
                valsx.add( (double)k * duration);
            }
        } else {
            double zeroPeriod = 0;
            boolean skipping = false;
            double startTime = ec.getStartTime();
            p.getEventsContinueFromLastReset();
            double roughUpdateInterval = updateInterval*3.01;
            double roughEndTime = Double.NEGATIVE_INFINITY;
            EventCollection intervalEvents = new EventCollectionSimple();
            for (k = 0; k < numVals; k++) {
                double endTime = startTime + (k + 1) * updateInterval;
                if (endTime > startTime + duration) {
                    endTime = startTime + duration;
                }
                if (roughEndTime<endTime) {
                    intervalEvents = p.getEventsContinueFromLast(endTime-updateInterval-0.01, endTime-updateInterval+roughUpdateInterval);
                    roughEndTime = endTime-updateInterval+roughUpdateInterval;
                }

                AmountAndCount a;
                AmountAndCount a2;
                AmountAndCount a3;
                // Pre check to see if nothing was done
                if (intervalEvents.getNumEventsTotal() > 0) {
                    // Find out if we have a period with nothing done
                    a = intervalEvents.totalDamage(Constants.SCHOOL_ALL, FightParticipant.TYPE_ALL_DAMAGE, unitType, null, endTime-updateInterval, endTime);
                    a2 = intervalEvents.totalHealing(Constants.SCHOOL_ALL, FightParticipant.TYPE_ALL_HEALING, unitType, null, endTime-updateInterval, endTime);
                    a3 = intervalEvents.totalPower(Constants.SCHOOL_ALL, FightParticipant.TYPE_ALL_POWER, unitType, null, endTime-updateInterval, endTime);
                } else {
                    a = DefaultObjects.emptyAmountAndCount;
                    a2 = DefaultObjects.emptyAmountAndCount;
                    a3 = DefaultObjects.emptyAmountAndCount;
                }
                if (a.amount == 0 && a2.amount == 0 && a3.amount == 0) {
                    zeroPeriod+=updateInterval;
                } else {
                    zeroPeriod = 0;
                }
                if (zeroPeriod < updateInterval*2) {
                    if (skipping) {
                        valsyHeal.add(0.0);
                        valsx.add((double) (k-1) * updateInterval + updateInterval / 2.0);
                    }
                    skipping = false;
                    valsyHeal.add((double) a2.amount / updateInterval);
                    valsx.add((double) k * updateInterval + updateInterval / 2.0);
                } else {
                    skipping = true;
                }
            }
        }

        double[][] healingData = new double[2][valsx.size()];
        for (k=0; k<valsx.size(); k++) {
            healingData[0][k] = valsx.get(k);
            healingData[1][k] = valsyHeal.get(k);
        }
        return healingData;
    }

    public static double[][] createParticipantPowerLinePlotData(EventCollection ec, EventCollection p, double updateInterval) {
        return createParticipantPowerLinePlotData(ec, p, updateInterval, FightParticipant.UNIT_ALL);
    }

    public static double[][] createParticipantPowerLinePlotData(EventCollection ec, EventCollection p, double updateInterval, int unitType) {
        int k;
        // f.getEndTime() - f.getStartTime() is different than f.getDuration() because
        // merged fights have a duration that is the sum of the individual fights
        double duration = ec.getEndTime() - ec.getStartTime();
        int numVals = (int)Math.round(duration/updateInterval);
        if (numVals < 2) {
            numVals = 2;
            if (duration == 0) {
                duration = 0.1;
            }
        }
        ArrayList<Double> valsyPower = new ArrayList<Double>();
        ArrayList<Double> valsx = new ArrayList<Double>();

        //Special case with 2, only show average damage
        if (numVals == 2) {
            double startTime = ec.getStartTime();
            double endTime = startTime + duration;
            AmountAndCount a3 = p.totalPower(Constants.SCHOOL_ALL, FightParticipant.TYPE_ALL_POWER, unitType, null, startTime, endTime);
            for (k = 0; k < numVals; k++) {
                valsyPower.add((double) a3.amount / duration);
                valsx.add( (double)k * duration);
            }
        } else {
            double zeroPeriod = 0;
            boolean skipping = false;
            double startTime = ec.getStartTime();
            for (k = 0; k < numVals; k++) {
                double endTime = startTime + (k + 1) * updateInterval;
                if (endTime > startTime + duration) {
                    endTime = startTime + duration;
                }
                // Find out if we have a period with nothing done
                AmountAndCount a = p.totalDamage(Constants.SCHOOL_ALL, FightParticipant.TYPE_ALL_DAMAGE, unitType, null, endTime-updateInterval, endTime);
                AmountAndCount a2 = p.totalHealing(Constants.SCHOOL_ALL, FightParticipant.TYPE_ALL_HEALING, unitType, null, endTime-updateInterval, endTime);
                AmountAndCount a3 = p.totalPower(Constants.SCHOOL_ALL, FightParticipant.TYPE_ALL_POWER, unitType, null, endTime-updateInterval, endTime);
                if (a.amount == 0 && a2.amount == 0 && a3.amount == 0) {
                    zeroPeriod+=updateInterval;
                } else {
                    zeroPeriod = 0;
                }
                if (zeroPeriod < updateInterval*2) {
                    if (skipping) {
                    valsyPower.add(0.0);
                    valsx.add((double) (k-1) * updateInterval + updateInterval / 2.0);
                    }
                    skipping = false;
                    valsyPower.add((double) a3.amount / updateInterval);
                    valsx.add((double) k * updateInterval + updateInterval / 2.0);
                } else {
                    skipping = true;
                }
            }
        }

        double[][] powerData = new double[2][valsx.size()];
        for (k=0; k<valsx.size(); k++) {
            powerData[0][k] = valsx.get(k);
            powerData[1][k] = valsyPower.get(k);
        }
        return powerData;
    }

    /**
     * Create a Damage line plot with red lines.
     * @param dataset The damage data.
     * @return A chart
     */
    public static JFreeChart createDamageLinePlot(double[][] damageData, boolean spline) {
        DefaultXYDataset dataset = new DefaultXYDataset();
        dataset.addSeries("Damage", damageData);
        return WlpPlotFactory.createDamageLinePlot(dataset, spline);
    }

    /**
     * Create a Damage line plot with red lines.
     * @param dataset A dataset with only one series.
     * @return A chart.
     */
    public static JFreeChart createDamageLinePlot(DefaultXYDataset dataset, boolean spline) {
        JFreeChart chart = ChartFactory.createXYLineChart("Damage per second", "Time (s)", "DPS", dataset, PlotOrientation.VERTICAL, false, true, false);
        Plot plot = chart.getPlot();
        if (plot instanceof XYPlot) {
            XYPlot xy = (XYPlot) plot;
            if (spline) {
                xy.setRenderer(new XYSplineRenderer());
            }
            XYItemRenderer renderer = xy.getRenderer();
            renderer.setSeriesPaint(0, Color.RED);
        }
        return chart;
    }

    /**
     * Create a Healing line plot with green lines.
     * @param healingData The healing data.
     * @return A chart.
     */
    public static JFreeChart createHealingLinePlot(double[][] healingData, boolean spline) {
        DefaultXYDataset dataset = new DefaultXYDataset();
        dataset.addSeries("Healing", healingData);
        return WlpPlotFactory.createHealingLinePlot(dataset, spline);
    }

    /**
     * Create a Healing line plot with green lines.
     * @param dataset A dataset with only one series.
     * @return A chart.
     */
    public static JFreeChart createHealingLinePlot(DefaultXYDataset dataset, boolean spline) {
        JFreeChart chart = ChartFactory.createXYLineChart("Healing per second", "Time (s)", "HPS", dataset, PlotOrientation.VERTICAL, false, true, false);
        Plot plot = chart.getPlot();
        if (plot instanceof XYPlot) {
            XYPlot xy = (XYPlot) plot;
            if (spline) {
                xy.setRenderer(new XYSplineRenderer());
            }
            XYItemRenderer renderer = xy.getRenderer();
            renderer.setSeriesPaint(0, Color.GREEN);
        }
        return chart;
    }

    /**
     * Create a Power line plot with yellow lines.
     * @param powerData The power data.
     * @return A chart.
     */
    public static JFreeChart createPowerLinePlot(double[][] powerData, boolean spline) {
        DefaultXYDataset dataset = new DefaultXYDataset();
        dataset.addSeries("Power", powerData);
        return WlpPlotFactory.createPowerLinePlot(dataset, spline);
    }

    /**
     * Create a Power line plot with yellow lines.
     * @param dataset A dataset with only one series.
     * @return A chart.
     */
    public static JFreeChart createPowerLinePlot(DefaultXYDataset dataset, boolean spline) {
        JFreeChart chart = ChartFactory.createXYLineChart("Power per second", "Time (s)", "PPS", dataset, PlotOrientation.VERTICAL, false, true, false);
        Plot plot = chart.getPlot();
        if (plot instanceof XYPlot) {
            XYPlot xy = (XYPlot) plot;
            if (spline) {
                xy.setRenderer(new XYSplineRenderer());
            }
            XYItemRenderer renderer = xy.getRenderer();
            renderer.setSeriesPaint(0, new Color(0.7f, 0.7f, 0f));
        }
        return chart;
    }

    /**
     * Create a Damage and Healing line plot with red and green lines.
     * @param damageData The damage data.
     * @param healingData The healing data.
     * @return A chart.
     */
    public static JFreeChart createDamageAndHealingLinePlot(double[][] damageData, double[][] healingData, boolean spline) {
        DefaultXYDataset dataset = new DefaultXYDataset();
        dataset.addSeries("Damage", damageData);
        dataset.addSeries("Healing", healingData);
        return WlpPlotFactory.createDamageAndHealingLinePlot(dataset, spline);
    }

    /**
     * Create a Damage and Healing line plot with red and green lines.
     * @param dataset A dataset with TWO series. Damage as the first and healing as the second.
     * @return A chart.
     */
    public static JFreeChart createDamageAndHealingLinePlot(DefaultXYDataset dataset, boolean spline) {
        JFreeChart chart = ChartFactory.createXYLineChart("Damage and Healing per second", "Time (s)", "DPS/HPS", dataset, PlotOrientation.VERTICAL, false, true, false);
        Plot plot = chart.getPlot();
        if (plot instanceof XYPlot) {
            XYPlot xy = (XYPlot) plot;
            if (spline) {
                xy.setRenderer(new XYSplineRenderer());
            }
            XYItemRenderer renderer = xy.getRenderer();
            renderer.setSeriesPaint(0, Color.RED);
            renderer.setSeriesPaint(1, Color.GREEN);
        }
        return chart;
    }

    /**
     * Create raid dps plot data. The lists fights, includedFights and names have to have the same number of elements and
     * each index has to correspond to the same fight.
     * @param fights The fights to plot for.
     * @param includeFights A boolean for each fight that tells wether to include it in the plot or not.
     * @param names The name that each fight gets in the plot.
     * @param interval The averaging interval.
     * @return Array with plot data.
     */
    public static DefaultXYDataset createRaidDamageLinePlotDataset(List<Fight> fights, List<Boolean> includedFights, List<String> names, double interval) {
        double fightsStartTime = Double.POSITIVE_INFINITY;
        double fightsEndTime = Double.NEGATIVE_INFINITY;
        for (int k=0; k<fights.size(); k++) {
            if (includedFights.get(k)) {
                Fight f = fights.get(k);
                double startT = f.getStartTime();
                double endT = f.getEndTime();
                fightsStartTime = Math.min(startT, fightsStartTime);
                fightsEndTime = Math.max(endT, fightsEndTime);
            }
        }

        double totalTime = fightsEndTime - fightsStartTime;
        int numVals = (int)Math.round(totalTime / interval);
        ArrayList<ArrayList<Double>> damageValues = new ArrayList<ArrayList<Double>>();
        double[] xVal = new double[numVals];

        for (Fight f : fights) {
            ArrayList<Double> damVal = new ArrayList<Double>();
            for (int k=0; k<numVals; k++) {
                double startTime = fightsStartTime + (double)k * interval;
                double endTime = fightsStartTime + (double)(k+1) * interval;
                xVal[k] = (endTime + startTime) / 2.0 - fightsStartTime;
                AmountAndCount damage = new AmountAndCount();
                for (FightParticipant p : f.getParticipants()) {
                    AmountAndCount a;
                    a = p.totalDamage(Constants.SCHOOL_ALL,
                            FightParticipant.TYPE_ALL_DAMAGE,
                            FightParticipant.UNIT_ALL, null,
                            startTime, endTime);
                    damage.add(a);

                }
                damVal.add((double)damage.amount / (endTime - startTime));
            }

            damageValues.add(damVal);
        }

        DefaultXYDataset damageDataset = new DefaultXYDataset();
        for (int k=0; k<damageValues.size(); k++) {
            ArrayList<Double> yVals = damageValues.get(k);
            double[][] xy = new double[2][0];
            xy[0] = xVal;
            xy[1] = new double[yVals.size()];
            for (int l=0; l<yVals.size(); l++) {
                xy[1][l] = yVals.get(l);
            }
            if (includedFights.get(k)) {
                damageDataset.addSeries(names.get(k), xy);
            }
        }
        return damageDataset;
    }

    /**
     * Create a raid damage chart
     * @param dataset The raid damage dataset.
     * @return
     */
    public static JFreeChart createRaidDamageLinePlot(DefaultXYDataset dataset, boolean spline) {
        JFreeChart damageChart = ChartFactory.createXYLineChart("Damage", "Time", "Dps", dataset, PlotOrientation.VERTICAL, true, true, false);
        if (spline) {
            damageChart.getXYPlot().setRenderer(new XYSplineRenderer());
        }
        for (int k=0; k<damageChart.getXYPlot().getSeriesCount(); k++) {
            Stroke s = damageChart.getXYPlot().getRenderer().getSeriesStroke(k);
            if (s instanceof BasicStroke) {
                BasicStroke bs = (BasicStroke) s;
                BasicStroke newStroke = new BasicStroke(3f, bs.getEndCap(), bs.getLineJoin(), bs.getMiterLimit(), bs.getDashArray(), bs.getDashPhase());
                damageChart.getXYPlot().getRenderer().setSeriesStroke(k, newStroke);
            }
            if (s == null) {
                BasicStroke newStroke = new BasicStroke(3f);
                damageChart.getXYPlot().getRenderer().setSeriesStroke(k, newStroke);
            }
        }
        return damageChart;
    }

    /**
     * Create raid hps plot data. The lists fights, includedFights and names have to have the same number of elements and
     * each index has to correspond to the same fight.
     * @param fights The fights to plot for.
     * @param includeFights A boolean for each fight that tells wether to include it in the plot or not.
     * @param names The name that each fight gets in the plot.
     * @param interval The averaging interval.
     * @return Array with plot data.
     */
    public static DefaultXYDataset createRaidHealingLinePlotDataset(List<Fight> fights, List<Boolean> includedFights, List<String> names, double interval) {
        double fightsStartTime = Double.POSITIVE_INFINITY;
        double fightsEndTime = Double.NEGATIVE_INFINITY;
        for (int k=0; k<fights.size(); k++) {
            if (includedFights.get(k)) {
                Fight f = fights.get(k);
                double startT = f.getStartTime();
                double endT = f.getEndTime();
                fightsStartTime = Math.min(startT, fightsStartTime);
                fightsEndTime = Math.max(endT, fightsEndTime);
            }
        }

        double totalTime = fightsEndTime - fightsStartTime;
        int numVals = (int)Math.round(totalTime / interval);
        ArrayList<ArrayList<Double>> healingValues = new ArrayList<ArrayList<Double>>();
        double[] xVal = new double[numVals];

        for (Fight f : fights) {
            ArrayList<Double> healVal = new ArrayList<Double>();
            for (int k=0; k<numVals; k++) {
                double startTime = fightsStartTime + (double)k * interval;
                double endTime = fightsStartTime + (double)(k+1) * interval;
                xVal[k] = (endTime + startTime) / 2.0 - fightsStartTime;
                AmountAndCount healing = new AmountAndCount();
                for (FightParticipant p : f.getParticipants()) {
                    AmountAndCount a;
                    a = p.totalHealing(Constants.SCHOOL_ALL,
                        FightParticipant.TYPE_ALL_HEALING,
                        FightParticipant.UNIT_ALL, null,
                        startTime, endTime);
                    healing.add(a);

                }
                healVal.add((double)healing.amount / (endTime - startTime));
            }

            healingValues.add(healVal);
        }

        DefaultXYDataset healingDataset = new DefaultXYDataset();
        for (int k=0; k<healingValues.size(); k++) {
            ArrayList<Double> yVals = healingValues.get(k);
            double[][] xy = new double[2][0];
            xy[0] = xVal;
            xy[1] = new double[yVals.size()];
            for (int l=0; l<yVals.size(); l++) {
                xy[1][l] = yVals.get(l);
            }
            if (includedFights.get(k)) {
                healingDataset.addSeries(names.get(k), xy);
            }
        }
        return healingDataset;
    }

    /**
     * Create a raid healing chart
     * @param dataset The raid healing dataset.
     * @return
     */
    public static JFreeChart createRaidHealingLinePlot(DefaultXYDataset dataset, boolean spline) {
        JFreeChart healingChart = ChartFactory.createXYLineChart("Healing", "Time", "Hps", dataset, PlotOrientation.VERTICAL, true, true, false);
        if (spline) {
            healingChart.getXYPlot().setRenderer(new XYSplineRenderer());
        }
        for (int k=0; k<healingChart.getXYPlot().getSeriesCount(); k++) {
            Stroke s = healingChart.getXYPlot().getRenderer().getSeriesStroke(k);
            if (s instanceof BasicStroke) {
                BasicStroke bs = (BasicStroke) s;
                BasicStroke newStroke = new BasicStroke(3f, bs.getEndCap(), bs.getLineJoin(), bs.getMiterLimit(), bs.getDashArray(), bs.getDashPhase());
                healingChart.getXYPlot().getRenderer().setSeriesStroke(k, newStroke);
            }
            if (s == null) {
                BasicStroke newStroke = new BasicStroke(3f);
                healingChart.getXYPlot().getRenderer().setSeriesStroke(k, newStroke);
            }
        }
        return healingChart;
    }

    /**
     * Create a dataset for multiple participants damage plot. The Lists participants and includedParticipants must be the same size.
     * @param ec Events from the fight where the participants are, must contain events that are equal/before and equal/after
     * the first and last event that all the participants have since it decides the start and end of the plot.
     * @param participants The participants to plot.
     * @param includedParticipants List that tells wether a participant should be plotted or not.
     * @param interval The averaging interval.
     * @param minDamage The minimum damage allowed. If below this the participant is not plotted.
     * @return A dataset.
     */
    public static DefaultXYDataset createMultipleParticipantsDamageLinePlotDatset(EventCollection ec, List<FightParticipant> participants, List<Boolean> includedParticipants, double interval, long minDamage) {
        double fightStartTime = ec.getStartTime();
        double fightEndTime = ec.getEndTime();
        double totalTime = fightEndTime - fightStartTime;
        int numVals = (int)Math.round(totalTime / interval);
        List<List<Double>> damageValues = new ArrayList<List<Double>>();
        List<Double> totalDamageValues = new ArrayList<Double>();
        List<String> damageNames = new ArrayList<String>();
        double[] xVal = new double[numVals];

        for (FightParticipant p : participants) {
            List<Double> damVal = new ArrayList<Double>();
            long totalDamage = 0;
            for (int k=0; k<numVals; k++) {
                double startTime = fightStartTime + (double)k * interval;
                double endTime = fightStartTime + (double)(k+1) * interval;
                xVal[k] = (endTime + startTime) / 2.0 - fightStartTime;
                AmountAndCount damage = p.totalDamage(Constants.SCHOOL_ALL,
                        FightParticipant.TYPE_ALL_DAMAGE,
                        FightParticipant.UNIT_ALL, null,
                        startTime, endTime);
                damVal.add((double)damage.amount / (endTime - startTime));
                totalDamage += damage.amount;
            }
            damageValues.add(damVal);
            damageNames.add(p.getName());
            totalDamageValues.add((double)totalDamage);
        }

        //Add damage chart
        DefaultXYDataset damageDataset = new DefaultXYDataset();
        for (int k=0; k<damageValues.size(); k++) {
            List<Double> yVals = damageValues.get(k);
            double[][] xy = new double[2][0];
            xy[0] = xVal;
            xy[1] = new double[yVals.size()];
            for (int l=0; l<yVals.size(); l++) {
                xy[1][l] = yVals.get(l);
            }
            if ((totalDamageValues.get(k) > minDamage) && includedParticipants.get(k)) {
                damageDataset.addSeries(damageNames.get(k), xy);
            }
        }
        return damageDataset;
    }

    /**
     * Create a multiple participants damage plot.
     * @param dataset The dataset to use.
     * @return A chart.
     */
    public static JFreeChart createMultipleParticipantsDamageLinePlot(DefaultXYDataset dataset, boolean spline) {
        JFreeChart damageChart = ChartFactory.createXYLineChart("Damage", "Time", "Dps", dataset, PlotOrientation.VERTICAL, true, true, false);
        if (spline) {
            damageChart.getXYPlot().setRenderer(new XYSplineRenderer());
        }
        for (int k=0; k<damageChart.getXYPlot().getSeriesCount(); k++) {
            Stroke s = damageChart.getXYPlot().getRenderer().getSeriesStroke(k);
            if (s instanceof BasicStroke) {
                BasicStroke bs = (BasicStroke) s;
                BasicStroke newStroke = new BasicStroke(3f, bs.getEndCap(), bs.getLineJoin(), bs.getMiterLimit(), bs.getDashArray(), bs.getDashPhase());
                damageChart.getXYPlot().getRenderer().setSeriesStroke(k, newStroke);
            }
            if (s == null) {
                BasicStroke newStroke = new BasicStroke(3f);
                damageChart.getXYPlot().getRenderer().setSeriesStroke(k, newStroke);
            }
        }
        return damageChart;
    }

    /**
     * Create a dataset for multiple participants healing plot. The Lists participants and includedParticipants must be the same size.
     * @param ec Events from the fight where the participants are, must contain events that are equal/before and equal/after
     * the first and last event that all the participants have since it decides the start and end of the plot.
     * @param participants The participants to plot.
     * @param includedParticipants List that tells wether a participant should be plotted or not.
     * @param interval The averaging interval.
     * @param minDamage The minimum healing allowed. If below this the participant is not plotted.
     * @return A dataset.
     */
    public static DefaultXYDataset createMultipleParticipantsHealingLinePlotDatset(EventCollection ec, List<FightParticipant> participants, List<Boolean> includedParticipants, double interval, long minHealing) {
        double fightStartTime = ec.getStartTime();
        double fightEndTime = ec.getEndTime();
        double totalTime = fightEndTime - fightStartTime;
        int numVals = (int)Math.round(totalTime / interval);
        ArrayList<ArrayList<Double>> healingValues = new ArrayList<ArrayList<Double>>();
        ArrayList<Double> totalHealingValues = new ArrayList<Double>();
        ArrayList<String> healingNames = new ArrayList<String>();
        double[] xVal = new double[numVals];

        for (FightParticipant p : participants) {
            ArrayList<Double> healVal = new ArrayList<Double>();
            long totalHealing = 0;
            for (int k=0; k<numVals; k++) {
                double startTime = fightStartTime + (double)k * interval;
                double endTime = fightStartTime + (double)(k+1) * interval;
                xVal[k] = (endTime + startTime) / 2.0 - fightStartTime;
                AmountAndCount healing = p.totalHealing(Constants.SCHOOL_ALL,
                        FightParticipant.TYPE_ALL_HEALING,
                        FightParticipant.UNIT_ALL, null,
                        startTime, endTime);
                healVal.add((double)healing.amount / (endTime - startTime));
                totalHealing += healing.amount;
            }
            healingValues.add(healVal);
            healingNames.add(p.getName());
            totalHealingValues.add((double)totalHealing);
        }

        DefaultXYDataset healingDataset = new DefaultXYDataset();
        for (int k=0; k<healingValues.size(); k++) {
            ArrayList<Double> yVals = healingValues.get(k);
            double[][] xy = new double[2][0];
            xy[0] = xVal;
            xy[1] = new double[yVals.size()];
            for (int l=0; l<yVals.size(); l++) {
                xy[1][l] = yVals.get(l);
            }
            if ((totalHealingValues.get(k) > minHealing) && includedParticipants.get(k)) {
                healingDataset.addSeries(healingNames.get(k), xy);
            }
        }
        return healingDataset;        
    }

    /**
     * Create a multiple participants healing plot.
     * @param dataset The dataset to use.
     * @return A chart.
     */
    public static JFreeChart createMultipleParticipantsHealingLinePlot(DefaultXYDataset dataset, boolean spline) {
        JFreeChart healingChart = ChartFactory.createXYLineChart("Healing", "Time", "Hps", dataset, PlotOrientation.VERTICAL, true, true, false);
        if (spline) {
            healingChart.getXYPlot().setRenderer(new XYSplineRenderer());
        }
        for (int k=0; k<healingChart.getXYPlot().getSeriesCount(); k++) {
            Stroke s = healingChart.getXYPlot().getRenderer().getSeriesStroke(k);
            if (s instanceof BasicStroke) {
                BasicStroke bs = (BasicStroke) s;
                BasicStroke newStroke = new BasicStroke(3f, bs.getEndCap(), bs.getLineJoin(), bs.getMiterLimit(), bs.getDashArray(), bs.getDashPhase());
                healingChart.getXYPlot().getRenderer().setSeriesStroke(k, newStroke);
            }
            if (s == null) {
                BasicStroke newStroke = new BasicStroke(3f);
                healingChart.getXYPlot().getRenderer().setSeriesStroke(k, newStroke);
            }
        }
        return healingChart;
    }
}
