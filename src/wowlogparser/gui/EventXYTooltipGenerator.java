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

import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.data.xy.XYDataset;
import wowlogparserbase.events.BasicEvent;
import wowlogparserbase.events.damage.DamageEvent;

/**
 *
 * @author racy
 */
public class EventXYTooltipGenerator implements XYToolTipGenerator {

    StandardXYToolTipGenerator stdGenerator = new StandardXYToolTipGenerator();

    public String generateToolTip(XYDataset dataset, int series, int item) {
        if (!(dataset instanceof EventXYDataset)) {
            return stdGenerator.generateToolTip(dataset, series, item);
        }
        EventXYDataset eDataset = (EventXYDataset) dataset;
        BasicEvent e = eDataset.getEvent(series, item);
        String s = eDataset.getEventTooltip(series, item);
        return s;
    }
    

}
