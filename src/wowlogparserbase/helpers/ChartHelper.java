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

package wowlogparserbase.helpers;

import java.awt.Point;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.ui.RectangleEdge;

/**
 *
 * @author racy
 */
public class ChartHelper {
    public static void addScrollWheelZoom(final ChartPanel chartPanel) {
        chartPanel.addMouseWheelListener(new MouseWheelListener() {

            public void mouseWheelMoved(MouseWheelEvent e) {
                double unitsToScroll = e.getUnitsToScroll();
                Point p = new Point(e.getX(), e.getY());
                XYPlot plot = chartPanel.getChart().getXYPlot();
                if (unitsToScroll < 0) {
                    //Zoom in
                    Rectangle2D dataArea = chartPanel.getChartRenderingInfo().getPlotInfo().getDataArea();
                    RectangleEdge domainAxisEdge = plot.getDomainAxisEdge();
                    double clickValue = plot.getDomainAxis().java2DToValue(chartPanel.translateScreenToJava2D(p).getX(), dataArea, domainAxisEdge);
                    chartPanel.zoomInDomain(e.getX(), e.getY());
                    //chartPanel.repaint();
                    Rectangle2D dataAreaAfter = chartPanel.getChartRenderingInfo().getPlotInfo().getDataArea();
                    RectangleEdge domainAxisEdgeAfter = plot.getDomainAxisEdge();
                    double clickValueAfter = plot.getDomainAxis().java2DToValue(chartPanel.translateScreenToJava2D(p).getX(), dataAreaAfter, domainAxisEdgeAfter);
                    double delta = clickValueAfter - clickValue;
                    Range range = plot.getDomainAxis().getRange();
                    Range shiftedRange = Range.shift(range, -delta, true);
                    plot.getDomainAxis().setRange(shiftedRange);
                } else {
                    //Zoom out
                    Rectangle2D dataArea = chartPanel.getChartRenderingInfo().getPlotInfo().getDataArea();
                    RectangleEdge domainAxisEdge = plot.getDomainAxisEdge();
                    double clickValue = plot.getDomainAxis().java2DToValue(chartPanel.translateScreenToJava2D(p).getX(), dataArea, domainAxisEdge);
                    chartPanel.zoomOutDomain(e.getX(), e.getY());
                    //chartPanel.repaint();
                    Rectangle2D dataAreaAfter = chartPanel.getChartRenderingInfo().getPlotInfo().getDataArea();
                    RectangleEdge domainAxisEdgeAfter = plot.getDomainAxisEdge();
                    double clickValueAfter = plot.getDomainAxis().java2DToValue(chartPanel.translateScreenToJava2D(p).getX(), dataAreaAfter, domainAxisEdgeAfter);
                    double delta = clickValueAfter - clickValue;
                    Range range = plot.getDomainAxis().getRange();
                    Range shiftedRange = Range.shift(range, -delta, true);
                    plot.getDomainAxis().setRange(shiftedRange);
                }
            }
        });
        
    }
}
