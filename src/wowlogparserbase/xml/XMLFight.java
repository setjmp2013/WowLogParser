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

import wowlogparserbase.Fight;
import wowlogparserbase.FightParticipant;
import wowlogparserbase.AmountAndCount;
import java.util.ArrayList;
import org.w3c.dom.*;
import wowlogparserbase.helpers.XmlHelper;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import wowlogparserbase.DispellEvents;
import wowlogparserbase.InterruptEvents;
import wowlogparserbase.WLPNumberFormat;
import wowlogparserbase.Constants;
import wowlogparserbase.events.SpellInterruptEvent;
import wowlogparserbase.events.aura.SpellAuraDispelledEvent;

/**
 *
 * @author racy
 */
public class XMLFight {

    Document doc;
    Fight fight;
    
    String name = "";
    String guid = "";
    boolean merged = false;
    double duration = 0;
    double activeDuration = 0;
    int numMobs = 0;
    long totalDamage = 0;
    XMLFightParticipant xmlVictim = new XMLFightParticipant();
    List<XMLFightParticipant> fightParticipants = new ArrayList<XMLFightParticipant>();

    
    public static final String fightTag = "Fight";
    public static final String mergedTag = "Merged";
    public static final String victimTag = "Victim";
    public static final String durationTag = "Duration";
    public static final String activeDurationTag = "ActiveDuration";
    public static final String numMobsTag = "NumMobs";
    public static final String totalDamageTag="TotalDamage";
    public static final String totalHealingTag="TotalHealing";
    public static final String dispellInfoTag="DispelInfo";
    public static final String interruptInfoTag="InterruptInfo";
    public static final String auraDispelledTag="AuraDispelled";
    public static final String spellInterruptedTag="SpellInterrupted";
    public static final String intEventTag="InterruptEvent";
    public static final String dispEventTag="DispelEvent";
    public static final String nameTag="name";
    public static final String idTag="id";
    public static final String schoolTag="school";
    public static final String guidTag="guid";
    public static final String sourceGuidTag="sourceguid";
    public static final String destinationGuidTag="destguid";
    public static final String sourceTag="source";
    public static final String skillTag="skill";
    public static final String targetTag="target";
    public static final String timeTag="time";
    public static final String npcsTag="Npcs";
    public static final String npcTag="Npc";
    public static final String monthTag="month";
    public static final String dayTag="day";
    public static final String hourTag="hour";
    public static final String minuteTag="minute";
    public static final String secondTag="second";
    
    
    public XMLFight(Fight fight) {
        doc = XmlHelper.createDocument(null);
        this.fight = fight;
    }

    public XMLFight() {
    }

    public Document makeDocument() {
        Element fightNode = doc.createElement(fightTag);
        fightNode.setAttribute(nameTag, fight.getName());
        fightNode.setAttribute(guidTag, fight.getGuid());
        addFightInfo(fightNode);
        
        Element victimNode = doc.createElement(victimTag);
        FightParticipant victim = fight.getVictim();
        XMLFightParticipant victimXP = new XMLFightParticipant(doc, victim, fight);
        Node victimParticipantNode = victimXP.makeNode();        
        fightNode.appendChild(victimNode);
        victimNode.appendChild(victimParticipantNode);

        List<FightParticipant> parts = fight.getParticipants();
        for (FightParticipant p : parts) {
            XMLFightParticipant xp = new XMLFightParticipant(doc, p, fight);
            Node n = xp.makeNode();
            fightNode.appendChild(n);
        }
        doc.appendChild(fightNode);
        return doc;
    }
    
    public void addFightInfo(Element e) {        
        NumberFormat nf = WLPNumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        Element mergedElement = doc.createElement(mergedTag);
        mergedElement.setTextContent("" + fight.isMerged());
        e.appendChild(mergedElement);
        
        Element durationElement = doc.createElement(durationTag);
        durationElement.setTextContent(nf.format(fight.getEndTime() - fight.getStartTime()));
        e.appendChild(durationElement);
        
        Element activeDurationElement = doc.createElement(activeDurationTag);
        activeDurationElement.setTextContent(nf.format(fight.getActiveDuration()));
        e.appendChild(activeDurationElement);
        
        Element numMobsElement = doc.createElement(numMobsTag);
        if (fight.isMerged()) {
            numMobsElement.setTextContent(nf.format(fight.getMergedSourceFights().size()));
            Element npcsElement = doc.createElement(npcsTag);
            e.appendChild(npcsElement);
            List<Fight> sourceFights = fight.getMergedSourceFights();
            for (Fight f : sourceFights) {
                Element npcElement = createNpcElement(f, nf);
                npcsElement.appendChild(npcElement);
            }
        } else {
            numMobsElement.setTextContent(nf.format(1));
            Element npcsElement = doc.createElement(npcsTag);
            e.appendChild(npcsElement);
            Element npcElement = createNpcElement(fight, nf);
            npcsElement.appendChild(npcElement);
        }
        e.appendChild(numMobsElement);
        
        

        Element totalDamageElement = doc.createElement(totalDamageTag);
        Element totalHealingElement = doc.createElement(totalHealingTag);
        long damage = 0;
        long healing = 0;
        for (FightParticipant p : fight.getParticipants()) {
            AmountAndCount a = p.totalDamage(Constants.SCHOOL_ALL, FightParticipant.TYPE_ALL_DAMAGE);
            damage += a.amount;
            a = p.totalHealing(Constants.SCHOOL_ALL, FightParticipant.TYPE_ALL_HEALING);
            healing += a.amount;
        }
        totalDamageElement.setTextContent(nf.format(damage));
        e.appendChild(totalDamageElement);
        totalHealingElement.setTextContent(nf.format(healing));
        e.appendChild(totalHealingElement);
        
        Element dispellInfoElement = doc.createElement(dispellInfoTag);
        DispellEvents de = new DispellEvents(fight.getEvents());
        int numNames = de.getNumNames();
        for (int k=0; k<numNames; k++) {
            String auraName = de.getName(k);
            Element auraElement = doc.createElement(auraDispelledTag);
            auraElement.setAttribute(nameTag, auraName);
            List<SpellAuraDispelledEvent> events = de.getEvents(auraName);
            for (SpellAuraDispelledEvent ev : events) {
                Element eventElement = doc.createElement(dispEventTag);
                ev.addXmlAttributes(eventElement);
//                eventElement.setAttribute(sourceTag, ev.sourceName);
//                eventElement.setAttribute(sourceGuidTag, ev.sourceGUID);
//                eventElement.setAttribute(skillTag, ev.getName());
//                eventElement.setAttribute(targetTag, ev.destinationName);
//                eventElement.setAttribute(destinationGuidTag, ev.destinationGUID);
//                eventElement.setAttribute(timeTag, ev.getTimeStringExact());
                auraElement.appendChild(eventElement);
            }
            dispellInfoElement.appendChild(auraElement);
        }
        e.appendChild(dispellInfoElement);

        Element interruptInfoElement = doc.createElement(interruptInfoTag);
        InterruptEvents ie = new InterruptEvents(fight.getEvents());
        numNames = ie.getNumNames();
        for (int k=0; k<numNames; k++) {
            String spellName = ie.getName(k);
            Element spellElement = doc.createElement(spellInterruptedTag);
            spellElement.setAttribute(nameTag, spellName);
            List<SpellInterruptEvent> events = ie.getEvents(spellName);
            for (SpellInterruptEvent ev : events) {
                Element eventElement = doc.createElement(intEventTag);
                ev.addXmlAttributes(eventElement);
//                eventElement.setAttribute(sourceTag, ev.sourceName);
//                eventElement.setAttribute(skillTag, ev.getName());
//                eventElement.setAttribute(targetTag, ev.destinationName);
//                eventElement.setAttribute(timeTag, ev.getTimeStringExact());
                spellElement.appendChild(eventElement);
            }
            interruptInfoElement.appendChild(spellElement);
        }
        e.appendChild(interruptInfoElement);
    }

    public Element createNpcElement(Fight f, NumberFormat nf) {
        Element npcElement = doc.createElement(npcTag);
        npcElement.setAttribute(idTag, nf.format(f.getMobID()));
        npcElement.setAttribute(nameTag, f.getName());
        npcElement.setAttribute(guidTag, f.getGuid());
        npcElement.setAttribute(monthTag, nf.format((int) f.getStartEvent().month));
        npcElement.setAttribute(dayTag, nf.format((int) f.getStartEvent().day));
        npcElement.setAttribute(hourTag, nf.format((int) f.getStartEvent().hour));
        npcElement.setAttribute(minuteTag, nf.format((int) f.getStartEvent().minute));
        npcElement.setAttribute(secondTag, nf.format(f.getStartEvent().second));
        return npcElement;
    }
    
    public void parseXML(Node rootNode) {
        try {
        NumberFormat nf = WLPNumberFormat.getInstance();
        NamedNodeMap attrs = rootNode.getAttributes();
        if (attrs!= null) {
            Node nameNode = attrs.getNamedItem(nameTag);
            if (nameNode != null) {
                name = nameNode.getTextContent().trim();
            }
            Node guidNode = attrs.getNamedItem(guidTag);
            if (guidNode != null) {
                guid = guidNode.getTextContent().trim();
            }
        }

        Node mergedNode = XmlHelper.getChildNode(rootNode, mergedTag);
        if (mergedNode != null) {
            merged = Boolean.parseBoolean(mergedNode.getTextContent().trim());
        }
        
        Node durationNode = XmlHelper.getChildNode(rootNode, durationTag);
        if (durationNode != null) {
            duration = nf.parse(durationNode.getTextContent().trim()).doubleValue();
        }

        Node activeDurationNode = XmlHelper.getChildNode(rootNode, activeDurationTag);
        if (activeDurationNode != null) {
            activeDuration = nf.parse(activeDurationNode.getTextContent().trim()).doubleValue();
        }

        Node numMobsNode = XmlHelper.getChildNode(rootNode, numMobsTag);
        if (numMobsNode != null) {
            numMobs = Integer.parseInt(numMobsNode.getTextContent().trim());
        }

        Node totalDamageNode = XmlHelper.getChildNode(rootNode, totalDamageTag);
        if (totalDamageNode != null) {
            totalDamage = Long.parseLong(totalDamageNode.getTextContent().trim());
        }

        Node victimNode = XmlHelper.getChildNode(rootNode, victimTag);
        if (victimNode != null) {
            Node victimFightparticipantNode = XmlHelper.getChildNode(victimNode, XMLFightParticipant.fightParticipantTag);
            if (victimFightparticipantNode != null) {
                xmlVictim = new XMLFightParticipant();
                xmlVictim.parseXML(victimFightparticipantNode);
            }
        }
        List<Node> fightParticipantNodes = XmlHelper.getChildNodes(rootNode, XMLFightParticipant.fightParticipantTag);
        fightParticipants = new ArrayList<XMLFightParticipant>();
        for (Node partNode : fightParticipantNodes) {
            if (partNode != null) {
                XMLFightParticipant xmlPart = new XMLFightParticipant();
                xmlPart.parseXML(partNode);
                fightParticipants.add(xmlPart);
            }
        }
        } catch (ParseException ex) {
            throw new NumberFormatException();
        }
    }
    
    public Fight getFight() {
        return fight;
    }

    public double getActiveDuration() {
        return activeDuration;
    }

    public double getDuration() {
        return duration;
    }

    public List<XMLFightParticipant> getFightParticipants() {
        return fightParticipants;
    }

    public String getGuid() {
        return guid;
    }

    public boolean isMerged() {
        return merged;
    }

    public String getName() {
        return name;
    }

    public int getNumMobs() {
        return numMobs;
    }

    public long getTotalDamage() {
        return totalDamage;
    }

    public XMLFightParticipant getXmlVictim() {
        return xmlVictim;
    }
    
    
}
