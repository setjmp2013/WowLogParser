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
package wowlogparser.gui.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import wowlogparserbase.EventCollection;
import wowlogparserbase.EventCollectionSimple;
import wowlogparserbase.TimePeriodExtra;
import wowlogparserbase.eventfilter.FilterDestinationGuid;
import wowlogparserbase.eventfilter.FilterSkill;
import wowlogparserbase.events.BasicEvent;
import wowlogparserbase.events.aura.SpellAuraAppliedEvent;
import wowlogparserbase.events.aura.SpellAuraRefreshEvent;
import wowlogparserbase.events.aura.SpellAuraRemovedEvent;

/**
 *
 * @author racy
 */
public class SingleAuraDisplay extends JPanel {

    private String guid = "";
    private int spellId = -1;
    private EventCollection events = new EventCollectionSimple();
    private double globalStartTime = -1;
    private double globalEndTime = -1;
    private double pixelsPerSecond = 5;
    private List<TimePeriodExtra> timePeriods = new ArrayList<TimePeriodExtra>();
    private int auraBarHeight;
    private String playerName = "";

    public SingleAuraDisplay() {
        setLayout(null);
        this.auraBarHeight = 15;
        setPreferredSize(new Dimension(10, auraBarHeight));
        setSize(new Dimension(10, auraBarHeight));
        setBorder(null);
        setBackground(Color.black);
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setEvents(String playerName, String guid, int spellId, EventCollection events, double startTime, double endTime) {
        this.playerName = playerName;
        setToolTipText(playerName);
        this.guid = guid;
        this.spellId = spellId;
        this.events = events.filter(new FilterDestinationGuid(guid)).filter(new FilterSkill(spellId, null, FilterSkill.ANY_SCHOOL, FilterSkill.ANY_POWER_TYPE));
        this.events.sort();
        this.globalStartTime = startTime;
        this.globalEndTime = endTime;
        timePeriods = new ArrayList<TimePeriodExtra>();
        boolean auraInProgress = false;
        double auraStartTime = -1;
        double lastTime = -1;
        Color color = Color.GREEN;
        for (int i = 0; i < this.events.size(); i++) {
            BasicEvent e = this.events.getEvent(i);
            lastTime = e.time;
            if (e instanceof SpellAuraRemovedEvent) {
                if (auraInProgress) {
                    auraInProgress = false;
                    timePeriods.add(new TimePeriodExtra(color, auraStartTime, e.time));
                }
            }
            if (e instanceof SpellAuraAppliedEvent) {
                if (!auraInProgress) {
                    SpellAuraAppliedEvent sa = (SpellAuraAppliedEvent)e;
                    if (sa.isBuff()) {
                        color = Color.GREEN;
                    } else {
                        color = Color.RED;
                    }
                    auraInProgress = true;
                    auraStartTime = e.time;
                }
            }
            if (e instanceof SpellAuraRefreshEvent) {
                if (!auraInProgress) {
                    SpellAuraRefreshEvent sa = (SpellAuraRefreshEvent)e;
                    if (sa.isBuff()) {
                        color = Color.GREEN;
                    } else {
                        color = Color.RED;
                    }
                    auraInProgress = true;
                    auraStartTime = e.time;
                }
            }
        }
        if (auraInProgress) {
            timePeriods.add(new TimePeriodExtra(color, auraStartTime, lastTime));
        }


        initComps();
    }

    private void initComps() {
        removeAll();
        setPreferredSize(new Dimension((int) Math.ceil((globalEndTime - globalStartTime) * pixelsPerSecond) + 5, auraBarHeight));
        setSize(new Dimension((int) Math.ceil((globalEndTime - globalStartTime) * pixelsPerSecond) + 5, auraBarHeight));
        NumberFormat nf = NumberFormat.getInstance();
        nf.setGroupingUsed(false);
        nf.setMaximumFractionDigits(1);
        for (TimePeriodExtra tp : timePeriods) {
            JLabel l = new JLabel();
            l.setOpaque(true);
            l.setBackground((Color)tp.getObj());
            int w = (int) Math.round(tp.getDuration() * pixelsPerSecond);
            int x = (int) Math.round((tp.startTime - globalStartTime) * pixelsPerSecond);
            l.setPreferredSize(new Dimension(w, auraBarHeight));
            l.setSize(new Dimension(w, auraBarHeight));
            l.setLocation(x, 0);
            l.setToolTipText(playerName + " < " + nf.format(tp.getDuration()) + " s >");
            add(l);
        }
        repaint();
    }

    public void setStartTime(double startTime) {
        this.globalStartTime = startTime;
    }

    public int getAuraBarHeight() {
        return auraBarHeight;
    }

    public void setAuraBarHeight(int auraBarHeight) {
        this.auraBarHeight = auraBarHeight;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (TimePeriodExtra tp : timePeriods) {
            int x1 = (int) Math.round(tp.startTime * pixelsPerSecond);
            int x2 = (int) Math.round(tp.endTime * pixelsPerSecond);
        }
    }
}
