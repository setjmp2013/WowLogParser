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

package wowlogparserbase.xml;

import java.text.NumberFormat;
import org.w3c.dom.*;
import wowlogparserbase.Fight;
import wowlogparserbase.WLPNumberFormat;

/**
 *
 * @author racy
 */
public class XMLFightReference {
    Document doc;
    Fight fight;
    
    String filename;
    String name;
    int day;
    int duration;
    int hour;
    int minute;
    int month;
    double second;
    int numMobs;

    public XMLFightReference(Document doc, Fight fight, String filename) {
        this.doc = doc;
        this.fight = fight;
        this.filename = filename;
    }

    public Node makeNode() {
        NumberFormat nf = WLPNumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        //Reference to the created xml file.
        Element fightInfoNode = doc.createElement("FightReference");
        fightInfoNode.setAttribute("filename", filename);
        fightInfoNode.setAttribute("name", fight.getName());
        fightInfoNode.setAttribute("duration", nf.format((int) fight.getActiveDuration()));
        fightInfoNode.setAttribute("month", nf.format((int) fight.getStartEvent().month));
        fightInfoNode.setAttribute("day", nf.format((int) fight.getStartEvent().day));
        fightInfoNode.setAttribute("hour", nf.format((int) fight.getStartEvent().hour));
        fightInfoNode.setAttribute("minute", nf.format((int) fight.getStartEvent().minute));
        fightInfoNode.setAttribute("second", nf.format(fight.getStartEvent().second));
        if (fight.isMerged()) {
            fightInfoNode.setAttribute("numMobs", nf.format(fight.getMergedSourceFights().size()));
        } else {
            fightInfoNode.setAttribute("numMobs", nf.format(1));
        }
        return fightInfoNode;
    }
    
    public XMLFightReference() {
    }

    public void parseXML(Node rootNode) {
        if (rootNode != null) {
            if (rootNode.getNodeName().equalsIgnoreCase("FightReference")) {
                NamedNodeMap attrs = rootNode.getAttributes();
                if (attrs != null) {
                    Node filenameNode = attrs.getNamedItem("filename");
                    if (filenameNode != null) {
                        filename = filenameNode.getTextContent().trim();
                    }
                    Node nameNode = attrs.getNamedItem("name");
                    if (nameNode != null) {
                        name = nameNode.getTextContent().trim();
                    }
                    Node durationNode = attrs.getNamedItem("duration");
                    if (durationNode != null) {
                        duration = Integer.parseInt(durationNode.getTextContent().trim());
                    }
                    Node monthNode = attrs.getNamedItem("month");
                    if (monthNode != null) {
                        month = Integer.parseInt(monthNode.getTextContent().trim());
                    }
                    Node dayNode = attrs.getNamedItem("day");
                    if (dayNode != null) {
                        day = Integer.parseInt(dayNode.getTextContent().trim());
                    }
                    Node hourNode = attrs.getNamedItem("hour");
                    if (hourNode != null) {
                        hour = Integer.parseInt(hourNode.getTextContent().trim());
                    }
                    Node minuteNode = attrs.getNamedItem("minute");
                    if (minuteNode != null) {
                        minute = Integer.parseInt(minuteNode.getTextContent().trim());
                    }
                    Node secondNode = attrs.getNamedItem("second");
                    if (secondNode != null) {
                        second = Double.parseDouble(secondNode.getTextContent().trim());
                    }
                    Node numMobsNode = attrs.getNamedItem("numMobs");
                    if (numMobsNode != null) {
                        numMobs = Integer.parseInt(numMobsNode.getTextContent().trim());
                    }
                }
            }
        }
    }

    public int getDay() {
        return day;
    }

    public int getDuration() {
        return duration;
    }

    public String getFilename() {
        return filename;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public int getMonth() {
        return month;
    }

    public String getName() {
        return name;
    }

    public int getNumMobs() {
        return numMobs;
    }

    public double getSecond() {
        return second;
    }
    
}
