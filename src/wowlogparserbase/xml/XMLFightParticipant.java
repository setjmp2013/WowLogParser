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
import wowlogparserbase.SpellInfo;
import java.text.NumberFormat;
import java.util.ArrayList;
import org.w3c.dom.*;
import wowlogparserbase.events.BasicEvent;
import wowlogparserbase.helpers.XmlHelper;
import java.text.ParseException;
import java.util.List;
import wowlogparserbase.PetInfo;
import wowlogparserbase.WLPNumberFormat;
import wowlogparserbase.Constants;

/**
 *
 * @author racy
 */
public class XMLFightParticipant {
    Document doc;
    FightParticipant participant = null;
    Fight fight;

    double activeDamageTime = 0;
    double activeHealingTime = 0;
    double activePowerTime = 0;
    String name = "";
    String guid = "";
    String className = "";
    AmountAndCount totalDamage = new AmountAndCount();
    AmountAndCount totalHealing = new AmountAndCount();
    
    public static final String fightParticipantTag="Participant";
    public static final String activeDamageDurationTag="ActiveDamageDuration";
    public static final String activeHealingDurationTag="ActiveHealingDuration";
    public static final String totalDamageTag="TotalDamage";
    public static final String totalHealingTag="TotalHealing";
    public static final String playerTag="Player";
    public static final String petTag="Pet";
    public static final String damageTag="Damage";
    public static final String swingTag="Swing";
    public static final String singleTag="Single";
    public static final String totalTag="Total";
    public static final String spellInfoTag="SpellInfo";
    public static final String rangedTag="Ranged";
    public static final String spellsTag="Spells";
    public static final String spellsPeriodicTag="SpellsPeriodic";
    public static final String spellsDirectTag="SpellsDirect";
    public static final String physicalSkillsTag="PhysicalSkills";
    public static final String healingTag="Healing";
    public static final String receivedEventsTag="ReceivedEvents";
    public static final String nameTag="name";
    public static final String idTag="id";
    public static final String schoolTag="school";
    public static final String powerTypeTag="powerType";
    public static final String guidTag="guid";
    public static final String classTag="class";
    public static final String petsTag="Pets";
    public static final String petInfoTag="PetInfo";
    public static final String powerTag="Power";
    public static final String totalPowerTag="TotalPower";
    
    //Player
    //  Damage
    AmountAndCount playerDamageTotaldamage = new AmountAndCount();
    //    Swing
    //      Total
    AmountAndCount playerDamageSwingTotal = new AmountAndCount();
    //      Single
    //        SpellInfo
    List<XMLSpellInfo> playerDamageSwingSingleSpellinfo = new ArrayList<XMLSpellInfo>();
    //    Physical Skills
    //      Total
    AmountAndCount playerDamagePhysicalskillsTotal = new AmountAndCount();
    //      Single
    //        SpellInfo
    List<XMLSpellInfo> playerDamagePhysicalskillsSingleSpellinfo = new ArrayList<XMLSpellInfo>();
    //    Ranged
    //      Total
    AmountAndCount playerDamageRangedTotal = new AmountAndCount();
    //      Single
    //        SpellInfo
    List<XMLSpellInfo> playerDamageRangedSingleSpellinfo = new ArrayList<XMLSpellInfo>();
    //    Spells
    //      Total
    AmountAndCount playerDamageSpellsTotal = new AmountAndCount();
    //      Single
    //        SpellInfo
    List<XMLSpellInfo> playerDamageSpellsSingleSpellinfo = new ArrayList<XMLSpellInfo>();
    
    //  Healing
    AmountAndCount playerHealingTotalhealing = new AmountAndCount();
    //    Spells
    //      Total
    AmountAndCount playerHealingSpellsTotal = new AmountAndCount();
    //      Single
    //        SpellInfo
    List<XMLSpellInfo> playerHealingSpellsSingleSpellinfo = new ArrayList<XMLSpellInfo>();

    //  Power
    AmountAndCount playerPowerTotalpower = new AmountAndCount();
    //    Spells
    //      Total
    AmountAndCount playerPowerSpellsTotal = new AmountAndCount();
    //      Single
    //        SpellInfo
    List<XMLSpellInfo> playerPowerSpellsSingleSpellinfo = new ArrayList<XMLSpellInfo>();
    
    //Pet
    //  Damage
    AmountAndCount petDamageTotaldamage = new AmountAndCount();
    //    Swing
    //      Total
    AmountAndCount petDamageSwingTotal = new AmountAndCount();
    //      Single
    //        SpellInfo
    List<XMLSpellInfo> petDamageSwingSingleSpellinfo = new ArrayList<XMLSpellInfo>();
    //    Physical Skills
    //      Total
    AmountAndCount petDamagePhysicalskillsTotal = new AmountAndCount();
    //      Single
    //        SpellInfo
    List<XMLSpellInfo> petDamagePhysicalskillsSingleSpellinfo = new ArrayList<XMLSpellInfo>();
    //    Ranged
    //      Total
    AmountAndCount petDamageRangedTotal = new AmountAndCount();
    //      Single
    //        SpellInfo
    List<XMLSpellInfo> petDamageRangedSingleSpellinfo = new ArrayList<XMLSpellInfo>();
    //    Spells
    //      Total
    AmountAndCount petDamageSpellsTotal = new AmountAndCount();
    //      Single
    //        SpellInfo
    List<XMLSpellInfo> petDamageSpellsSingleSpellinfo = new ArrayList<XMLSpellInfo>();
    
    //  Healing
    AmountAndCount petHealingTotalhealing = new AmountAndCount();
    //    Spells
    //      Total
    AmountAndCount petHealingSpellsTotal = new AmountAndCount();
    //      Single
    //        SpellInfo
    List<XMLSpellInfo> petHealingSpellsSingleSpellinfo = new ArrayList<XMLSpellInfo>();
    
    //  Power
    AmountAndCount petPowerTotalpower = new AmountAndCount();
    //    Spells
    //      Total
    AmountAndCount petPowerSpellsTotal = new AmountAndCount();
    //      Single
    //        SpellInfo
    List<XMLSpellInfo> petPowerSpellsSingleSpellinfo = new ArrayList<XMLSpellInfo>();

    //Received Events
    XMLFightParticipant receivedParticipant = null;
    
    List<PetInfo> pets = new ArrayList<PetInfo>();
    
    public XMLFightParticipant(Document doc, FightParticipant participant, Fight fight) {
        this.doc = doc;
        this.participant = participant;
        this.fight = fight;
        guid = participant.getSourceGUID();
        activeDamageTime = participant.getActiveDamageTime(15, 1.5, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        activeHealingTime = participant.getActiveHealingTime(15, 1.5, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        activePowerTime = participant.getActivePowerTime(15, 1.5, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }
    
    public XMLFightParticipant() {
    }

    /**
     * Make a FightParticipant node using the FightParticipant, Document and Fight supplied in the constructor.
     * @return The node that was made.
     */
    public Node makeNode() {
        NumberFormat nf = WLPNumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        AmountAndCount a;
        Element partNode = doc.createElement(fightParticipantTag);
        partNode.setAttribute(nameTag, participant.getName());
        partNode.setAttribute(guidTag, participant.getSourceGUID());
        partNode.setAttribute(classTag, participant.getClassName());
                
        Element activeDamageDurationNode = doc.createElement(activeDamageDurationTag);
        activeDamageDurationNode.setTextContent(nf.format(activeDamageTime));
        partNode.appendChild(activeDamageDurationNode);
        
        Element activeHealingDurationNode = doc.createElement(activeHealingDurationTag);
        activeHealingDurationNode.setTextContent(nf.format(activeHealingTime));
        partNode.appendChild(activeHealingDurationNode);

        Element totalDamageNode = doc.createElement(totalDamageTag);
        a = participant.totalDamage(Constants.SCHOOL_ALL, FightParticipant.TYPE_ALL_DAMAGE);
        addAmountAndCount(a, doc, totalDamageNode);
        partNode.appendChild(totalDamageNode);

        Element totalHealingNode = doc.createElement(totalHealingTag);
        a = participant.totalHealing(Constants.SCHOOL_ALL, FightParticipant.TYPE_ALL_HEALING);
        addAmountAndCountHealing(a, doc, totalHealingNode);
        partNode.appendChild(totalHealingNode);

        Element playerNode = doc.createElement(playerTag);
        partNode.appendChild(playerNode);
        Element playerDamageNode = doc.createElement(damageTag);
        playerNode.appendChild(playerDamageNode);
        Element playerHealingNode = doc.createElement(healingTag);
        playerNode.appendChild(playerHealingNode);
        Element playerPowerNode = doc.createElement(powerTag);
        playerNode.appendChild(playerPowerNode);
        
        Element petNode = doc.createElement(petTag);
        partNode.appendChild(petNode);
        Element petsNode = makePetsNode(petsTag);
        petNode.appendChild(petsNode);
        Element petDamageNode = doc.createElement(damageTag);
        petNode.appendChild(petDamageNode);
        Element petHealingNode = doc.createElement(healingTag);
        petNode.appendChild(petHealingNode);
        Element petPowerNode = doc.createElement(powerTag);
        petNode.appendChild(petPowerNode);
        
        //Damage
        Element playerDamageTotalDamageNode = doc.createElement(totalDamageTag);
        a = participant.totalDamage(Constants.SCHOOL_ALL, FightParticipant.TYPE_ALL_DAMAGE, FightParticipant.UNIT_PLAYER);
        addAmountAndCount(a, doc, playerDamageTotalDamageNode);
        playerDamageNode.appendChild(playerDamageTotalDamageNode);

        Element petDamageTotalDamageNode = doc.createElement(totalDamageTag);
        a = participant.totalDamage(Constants.SCHOOL_ALL, FightParticipant.TYPE_ALL_DAMAGE, FightParticipant.UNIT_PET);
        addAmountAndCount(a, doc, petDamageTotalDamageNode);
        petDamageNode.appendChild(petDamageTotalDamageNode);

        playerDamageNode.appendChild(makeDamageSwingNodes(swingTag, FightParticipant.UNIT_PLAYER));
        petDamageNode.appendChild(makeDamageSwingNodes(swingTag, FightParticipant.UNIT_PET));
        playerDamageNode.appendChild(makeDamagePhysicalSkillInfoNodes(physicalSkillsTag, FightParticipant.UNIT_PLAYER));
        petDamageNode.appendChild(makeDamagePhysicalSkillInfoNodes(physicalSkillsTag, FightParticipant.UNIT_PET));
        playerDamageNode.appendChild(makeDamageRangedInfoNodes(rangedTag, FightParticipant.UNIT_PLAYER));
        petDamageNode.appendChild(makeDamageRangedInfoNodes(rangedTag, FightParticipant.UNIT_PET));
        playerDamageNode.appendChild(makeDamageSpellInfoNodes(spellsTag, FightParticipant.UNIT_PLAYER));
        petDamageNode.appendChild(makeDamageSpellInfoNodes(spellsTag, FightParticipant.UNIT_PET));

        //Healing
        Element playerHealingTotalHealingNode = doc.createElement(totalHealingTag);
        a = participant.totalHealing(Constants.SCHOOL_ALL, FightParticipant.TYPE_ALL_HEALING, FightParticipant.UNIT_PLAYER);
        addAmountAndCountHealing(a, doc, playerHealingTotalHealingNode);
        playerHealingNode.appendChild(playerHealingTotalHealingNode);

        Element petHealingTotalHealingNode = doc.createElement(totalHealingTag);
        a = participant.totalHealing(Constants.SCHOOL_ALL, FightParticipant.TYPE_ALL_HEALING, FightParticipant.UNIT_PET);
        addAmountAndCountHealing(a, doc, petHealingTotalHealingNode);
        petHealingNode.appendChild(petHealingTotalHealingNode);

        playerHealingNode.appendChild(makeHealingSpellInfoNodes(spellsTag, FightParticipant.TYPE_ALL_HEALING, FightParticipant.UNIT_PLAYER));
        petHealingNode.appendChild(makeHealingSpellInfoNodes(spellsTag, FightParticipant.TYPE_ALL_HEALING, FightParticipant.UNIT_PET));
        playerHealingNode.appendChild(makeHealingSpellInfoNodes(spellsPeriodicTag, FightParticipant.TYPE_SPELL_HEALING_PERIODIC, FightParticipant.UNIT_PLAYER));
        petHealingNode.appendChild(makeHealingSpellInfoNodes(spellsPeriodicTag, FightParticipant.TYPE_SPELL_HEALING_PERIODIC, FightParticipant.UNIT_PET));
        playerHealingNode.appendChild(makeHealingSpellInfoNodes(spellsDirectTag, FightParticipant.TYPE_SPELL_HEALING_DIRECT, FightParticipant.UNIT_PLAYER));
        petHealingNode.appendChild(makeHealingSpellInfoNodes(spellsDirectTag, FightParticipant.TYPE_SPELL_HEALING_DIRECT, FightParticipant.UNIT_PET));

        //Power
        Element playerPowerTotalPowerNode = doc.createElement(totalPowerTag);
        a = participant.totalPower(Constants.SCHOOL_ALL, FightParticipant.TYPE_ALL_POWER, FightParticipant.UNIT_PLAYER);
        addAmountAndCountPower(a, doc, playerPowerTotalPowerNode);
        playerPowerNode.appendChild(playerPowerTotalPowerNode);

        Element petPowerTotalPowerNode = doc.createElement(totalPowerTag);
        a = participant.totalPower(Constants.SCHOOL_ALL, FightParticipant.TYPE_ALL_POWER, FightParticipant.UNIT_PET);
        addAmountAndCountPower(a, doc, petPowerTotalPowerNode);
        petPowerNode.appendChild(petPowerTotalPowerNode);

        playerPowerNode.appendChild(makePowerSpellInfoNodes(spellsTag, FightParticipant.TYPE_ALL_POWER, FightParticipant.UNIT_PLAYER));
        petPowerNode.appendChild(makePowerSpellInfoNodes(spellsTag, FightParticipant.TYPE_ALL_POWER, FightParticipant.UNIT_PET));

        //Received participant
        if (fight != null) {
            Element receivedEventsNode = doc.createElement(receivedEventsTag);
            XMLFightParticipant receivedPart = new XMLFightParticipant(doc, participant.makeReceivedParticipant(fight), null);
            Node receivedParticipantNode = receivedPart.makeNode();
            receivedEventsNode.appendChild(receivedParticipantNode);
            partNode.appendChild(receivedEventsNode);
        }
        
        return partNode;
    }

    Element makePetsNode(String nodeName) {
        Element el = doc.createElement(nodeName);
        for (PetInfo p : participant.getPetNames()) {
            Element petInfoNode = doc.createElement(petInfoTag);
            petInfoNode.setAttribute(nameTag, p.getName());
            petInfoNode.setAttribute(guidTag, p.getSourceGUID());
            el.appendChild(petInfoNode);
        }
        return el;
    }
    
    Element makeDamageSwingNodes(String nodeName, int unit) {
        AmountAndCount a;
        SpellInfo si;
        Element el;
        Element rootNode = doc.createElement(nodeName);
        Element totalNode = doc.createElement(totalTag);
        rootNode.appendChild(totalNode);

        a = participant.totalDamage(Constants.SCHOOL_ALL, FightParticipant.TYPE_SWING_DAMAGE, unit);

        addAmountAndCount(a, doc, totalNode);
        return rootNode;
    }

    Element makeDamagePhysicalSkillInfoNodes(String nodeName, int unit) {
        NumberFormat nf = WLPNumberFormat.getInstance();
        AmountAndCount a;
        Element el;
        int type = FightParticipant.TYPE_SPELL_DAMAGE;
        
        Element rootNode = doc.createElement(nodeName);
        Element individualNode = doc.createElement(singleTag);
        rootNode.appendChild(individualNode);
        Element totalNode = doc.createElement(totalTag);
        rootNode.appendChild(totalNode);

        a = participant.totalDamage(Constants.SCHOOL_PHYSICAL, FightParticipant.TYPE_SPELL_DAMAGE, unit);
        addAmountAndCount(a, doc, totalNode);
        
        List<SpellInfo> spellInfoArray = participant.getIDs(type, unit);
        for(SpellInfo si : spellInfoArray) {
            if ((si.school & Constants.SCHOOL_PHYSICAL) == 0) {
                continue;
            }
            Element spellInfoNode = doc.createElement(spellInfoTag);
            spellInfoNode.setAttribute(nameTag, si.name);
            spellInfoNode.setAttribute(idTag, nf.format(si.spellID));
            spellInfoNode.setAttribute(schoolTag, BasicEvent.getSchoolStringFromFlags(si.school));

            a = participant.damage(si, type, unit);
            addAmountAndCount(a, doc, spellInfoNode);
            
            individualNode.appendChild(spellInfoNode);
        }
        return rootNode;
    }
    
    Element makeDamageSpellInfoNodes(String nodeName, int unit) {
        NumberFormat nf = WLPNumberFormat.getInstance();
        AmountAndCount a;
        Element el;
        int type = FightParticipant.TYPE_SPELL_DAMAGE;
        Element rootNode = doc.createElement(nodeName);
        Element individualNode = doc.createElement(singleTag);
        rootNode.appendChild(individualNode);
        Element totalNode = doc.createElement(totalTag);
        rootNode.appendChild(totalNode);

        int spellSchools = 
                Constants.SCHOOL_ARCANE | Constants.SCHOOL_FIRE |
                Constants.SCHOOL_FROST | Constants.SCHOOL_HOLY |
                Constants.SCHOOL_NATURE | Constants.SCHOOL_SHADOW;
        a = participant.totalDamage(spellSchools, FightParticipant.TYPE_SPELL_DAMAGE, unit);
        addAmountAndCount(a, doc, totalNode);

        List<SpellInfo> spellInfoArray = participant.getIDs(type, unit);
        for(SpellInfo si : spellInfoArray) {
            if ((si.school & spellSchools) == 0) {
                continue;
            }
            Element spellInfoNode = doc.createElement(spellInfoTag);
            spellInfoNode.setAttribute(nameTag, si.name);
            spellInfoNode.setAttribute(idTag, nf.format(si.spellID));
            spellInfoNode.setAttribute(schoolTag, BasicEvent.getSchoolStringFromFlags(si.school));

            a = participant.damage(si, type, unit);            
            addAmountAndCount(a, doc, spellInfoNode);

            individualNode.appendChild(spellInfoNode);
        }
        return rootNode;
    }

    Element makeDamageRangedInfoNodes(String nodeName, int unit) {
        NumberFormat nf = WLPNumberFormat.getInstance();
        AmountAndCount a;
        Element el;
        int type = FightParticipant.TYPE_RANGED_DAMAGE;
        Element rootNode = doc.createElement(nodeName);
        Element individualNode = doc.createElement(singleTag);
        rootNode.appendChild(individualNode);
        Element totalNode = doc.createElement(totalTag);
        rootNode.appendChild(totalNode);

        a = participant.totalDamage(Constants.SCHOOL_ALL, FightParticipant.TYPE_RANGED_DAMAGE, unit);
        addAmountAndCount(a, doc, totalNode);

        List<SpellInfo> spellInfoArray = participant.getIDs(type, unit);
        for(SpellInfo si : spellInfoArray) {
            Element spellInfoNode = doc.createElement(spellInfoTag);
            spellInfoNode.setAttribute(nameTag, si.name);
            spellInfoNode.setAttribute(idTag, nf.format(si.spellID));
            spellInfoNode.setAttribute(schoolTag, BasicEvent.getSchoolStringFromFlags(si.school));

            a = participant.damage(si, type, unit);            
            addAmountAndCount(a, doc, spellInfoNode);

            individualNode.appendChild(spellInfoNode);
        }
        return rootNode;
    }

    Element makeHealingSpellInfoNodes(String nodeName, int type, int unit) {
        NumberFormat nf = WLPNumberFormat.getInstance();
        AmountAndCount a;
        Element rootNode = doc.createElement(nodeName);
        Element individualNode = doc.createElement(singleTag);
        rootNode.appendChild(individualNode);
        Element totalNode = doc.createElement(totalTag);
        rootNode.appendChild(totalNode);

        a = participant.totalHealing(Constants.SCHOOL_ALL, type, unit);
        addAmountAndCountHealing(a, doc, totalNode);
        
        List<SpellInfo> spellInfoArray = participant.getIDs(type, unit);
        for(SpellInfo si : spellInfoArray) {
            Element spellInfoNode = doc.createElement(spellInfoTag);
            spellInfoNode.setAttribute(nameTag, si.name);
            spellInfoNode.setAttribute(idTag, nf.format(si.spellID));
            spellInfoNode.setAttribute(schoolTag, BasicEvent.getSchoolStringFromFlags(si.school));

            a = participant.healing(si, type, unit);
            addAmountAndCountHealing(a, doc, spellInfoNode);

            individualNode.appendChild(spellInfoNode);
        }
        return rootNode;
    }
    
    Element makePowerSpellInfoNodes(String nodeName, int type, int unit) {
        NumberFormat nf = WLPNumberFormat.getInstance();
        AmountAndCount a;
        Element rootNode = doc.createElement(nodeName);
        Element individualNode = doc.createElement(singleTag);
        rootNode.appendChild(individualNode);
        Element totalNode = doc.createElement(totalTag);
        rootNode.appendChild(totalNode);

        a = participant.totalPower(Constants.SCHOOL_ALL, type, unit);
        addAmountAndCountPower(a, doc, totalNode);
        
        List<SpellInfo> spellInfoArray = participant.getIDs(type, unit);
        for(SpellInfo si : spellInfoArray) {
            Element spellInfoNode = doc.createElement(spellInfoTag);
            spellInfoNode.setAttribute(nameTag, si.name);
            spellInfoNode.setAttribute(idTag, nf.format(si.spellID));
            spellInfoNode.setAttribute(schoolTag, BasicEvent.getSchoolStringFromFlags(si.school));
            spellInfoNode.setAttribute(powerTypeTag, BasicEvent.getPowerTypeString(si.powerType));

            a = participant.power(si, type, unit);
            addAmountAndCountPower(a, doc, spellInfoNode);

            individualNode.appendChild(spellInfoNode);
        }
        return rootNode;
    }

    public static final String amountAndCountTag = "AmountAndCount";
    public static final String amountTag = "Am";
    public static final String amountOverTag = "AmOv";
    public static final String amountAbsorbedTag = "AmAb";
    public static final String amountResistedTag = "AmRe";
    public static final String amountBlockedTag = "AmBl";
    public static final String dpsTag = "Dps";
    public static final String hpsTag = "Hps";
    public static final String ppsTag = "Pps";
    public static final String averageAmountTag = "AvA";
    public static final String maxAmountTag = "MaA";
    public static final String absorbTag = "Ab";
    public static final String blockTag = "Bl";
    public static final String critTag = "Cri";
    public static final String crushingTag = "Cru";
    public static final String glancingTag = "Gla";
    public static final String dodgeTag = "Do";
    public static final String hitTag = "Hi";
    public static final String missTag = "Mi";
    public static final String parryTag = "Pa";
    public static final String reflectTag = "Ref";
    public static final String resistTag = "Res";
    public static final String totHitTag = "THi";
    public static final String totMissTag = "TMi";
    public void addAmountAndCount(AmountAndCount a, Document doc, Node nodeToAddTo) {
        if (a.isZero()) {
            return;
        }
        
        NumberFormat nf = WLPNumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);

        Element rootNode = doc.createElement(amountAndCountTag);
        nodeToAddTo.appendChild(rootNode);

        Element el;
        el = doc.createElement(amountTag);
        el.setTextContent("" + nf.format(a.amount));
        rootNode.appendChild(el);

        el = doc.createElement(dpsTag);
        if (activeDamageTime > 0) {
            el.setTextContent("" + nf.format((double)a.amount / activeDamageTime));
        } else {
            el.setTextContent(nf.format(0));
        }
        rootNode.appendChild(el);

        el = doc.createElement(averageAmountTag);
        if (a.totHit == 0) {
            el.setTextContent(nf.format(0));
        } else {
            el.setTextContent(nf.format(a.amount / a.totHit));
        }
        rootNode.appendChild(el);

        el = doc.createElement(maxAmountTag);
        el.setTextContent(nf.format(a.maxAmount));
        rootNode.appendChild(el);

        el = doc.createElement(absorbTag);
        el.setTextContent(nf.format(a.absorb));
        rootNode.appendChild(el);

        el = doc.createElement(blockTag);
        el.setTextContent(nf.format(a.block));
        rootNode.appendChild(el);

        el = doc.createElement(critTag);
        el.setTextContent(nf.format(a.crit));
        rootNode.appendChild(el);

        el = doc.createElement(crushingTag);
        el.setTextContent(nf.format(a.crushing));
        rootNode.appendChild(el);

        el = doc.createElement(glancingTag);
        el.setTextContent(nf.format(a.glancing));
        rootNode.appendChild(el);

        el = doc.createElement(dodgeTag);
        el.setTextContent(nf.format(a.dodge));
        rootNode.appendChild(el);

        el = doc.createElement(hitTag);
        el.setTextContent(nf.format(a.hit));
        rootNode.appendChild(el);

        el = doc.createElement(missTag);
        el.setTextContent(nf.format(a.miss));
        rootNode.appendChild(el);

        el = doc.createElement(parryTag);
        el.setTextContent(nf.format(a.parry));
        rootNode.appendChild(el);

        el = doc.createElement(reflectTag);
        el.setTextContent(nf.format(a.reflect));
        rootNode.appendChild(el);

        el = doc.createElement(resistTag);
        el.setTextContent(nf.format(a.resist));
        rootNode.appendChild(el);

        el = doc.createElement(totHitTag);
        el.setTextContent(nf.format(a.totHit));
        rootNode.appendChild(el);

        el = doc.createElement(totMissTag);
        el.setTextContent(nf.format(a.totMiss));
        rootNode.appendChild(el);

        el = doc.createElement(amountAbsorbedTag);
        el.setTextContent(nf.format(a.amountAbsorbed));
        rootNode.appendChild(el);

        el = doc.createElement(amountBlockedTag);
        el.setTextContent(nf.format(a.amountBlocked));
        rootNode.appendChild(el);

        el = doc.createElement(amountResistedTag);
        el.setTextContent(nf.format(a.amountResisted));
        rootNode.appendChild(el);
    }
    
    /**
     * Add amount and count info to a healing node.
     * @param a The amount and count for the healing
     * @param overA The amount and count for the overhealing
     * @param doc The document
     * @param rootNode The node to add the info to
     */
    public void addAmountAndCountHealing(AmountAndCount a, Document doc, Node nodeToAddTo) {
        if (a.isZero()) {
            return;
        }
        
        NumberFormat nf = WLPNumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);

        Element rootNode = doc.createElement(amountAndCountTag);
        nodeToAddTo.appendChild(rootNode);

        Element el;
        el = doc.createElement(amountTag);
        el.setTextContent(nf.format(a.amount));
        rootNode.appendChild(el);

        el = doc.createElement(hpsTag);
        if (activeHealingTime > 0) {
            el.setTextContent(nf.format((double)a.amount / activeHealingTime));
        } else {
            el.setTextContent(nf.format(0));
        }
        rootNode.appendChild(el);

        el = doc.createElement(averageAmountTag);
        if (a.totHit == 0) {
            el.setTextContent(nf.format(0));
        } else {
            el.setTextContent(nf.format(a.amount / a.totHit));
        }
        rootNode.appendChild(el);

        el = doc.createElement(maxAmountTag);
        el.setTextContent(nf.format(a.maxAmount));
        rootNode.appendChild(el);

        el = doc.createElement(critTag);
        el.setTextContent(nf.format(a.crit));
        rootNode.appendChild(el);

        el = doc.createElement(hitTag);
        el.setTextContent(nf.format(a.hit));
        rootNode.appendChild(el);

        el = doc.createElement(totHitTag);
        el.setTextContent(nf.format(a.totHit));
        rootNode.appendChild(el);

        el = doc.createElement(amountOverTag);
        el.setTextContent(nf.format(a.overAmount));
        rootNode.appendChild(el);

        el = doc.createElement(amountAbsorbedTag);
        el.setTextContent(nf.format(a.amountAbsorbed));
        rootNode.appendChild(el);

        el = doc.createElement(amountBlockedTag);
        el.setTextContent(nf.format(a.amountBlocked));
        rootNode.appendChild(el);

        el = doc.createElement(amountResistedTag);
        el.setTextContent(nf.format(a.amountResisted));
        rootNode.appendChild(el);
    }
    
    public void addAmountAndCountPower(AmountAndCount a, Document doc, Node nodeToAddTo) {
        if (a.isZero()) {
            return;
        }
        
        NumberFormat nf = WLPNumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);

        Element rootNode = doc.createElement(amountAndCountTag);
        nodeToAddTo.appendChild(rootNode);

        Element el;
        el = doc.createElement(amountTag);
        el.setTextContent(nf.format(a.amount));
        rootNode.appendChild(el);

        el = doc.createElement(ppsTag);
        if (activePowerTime > 0) {
            el.setTextContent(nf.format((double)a.amount / activePowerTime));
        } else {
            el.setTextContent(nf.format(0));
        }
        rootNode.appendChild(el);

        el = doc.createElement(averageAmountTag);
        if (a.totHit == 0) {
            el.setTextContent(nf.format(0));
        } else {
            el.setTextContent(nf.format(a.amount / a.totHit));
        }
        rootNode.appendChild(el);

        el = doc.createElement(maxAmountTag);
        el.setTextContent(nf.format(a.maxAmount));
        rootNode.appendChild(el);

        el = doc.createElement(critTag);
        el.setTextContent(nf.format(a.crit));
        rootNode.appendChild(el);

        el = doc.createElement(hitTag);
        el.setTextContent(nf.format(a.hit));
        rootNode.appendChild(el);

        el = doc.createElement(totHitTag);
        el.setTextContent(nf.format(a.totHit));
        rootNode.appendChild(el);
    }

    /**
     * Parse xml node structure and fill in the class variables.
     * @param rootNode The root node
     */
    public void parseXML(Node rootNode) {
        if (rootNode == null) {
            return;
        }
        try {
        NumberFormat nf = WLPNumberFormat.getInstance();
        if (rootNode.getNodeName().equalsIgnoreCase(fightParticipantTag)) {
            NamedNodeMap attrs = rootNode.getAttributes();
            Node nameNode = attrs.getNamedItem(nameTag);
            if (nameNode != null) {
                name = nameNode.getTextContent().trim();
            }
            Node guidNode = attrs.getNamedItem(guidTag);
            if (guidNode != null) {
                guid  = guidNode.getTextContent().trim();
            }
            Node classNode = attrs.getNamedItem(classTag);
            if (classNode != null) {
                className = classNode.getTextContent().trim();
            }
            
            Node activeDamageDurationNode = XmlHelper.getChildNode(rootNode, activeDamageDurationTag);
            if (activeDamageDurationNode != null) {
                activeDamageTime = nf.parse(activeDamageDurationNode.getTextContent().trim()).doubleValue();
            }
            Node activeHealingDurationNode = XmlHelper.getChildNode(rootNode, activeHealingDurationTag);
            if (activeHealingDurationNode != null) {
                activeHealingTime = nf.parse(activeHealingDurationNode.getTextContent().trim()).doubleValue();
            }            
            Node totalDamageNode = XmlHelper.getChildNode(rootNode, totalDamageTag);
            if (totalDamageNode != null) {
                parseAmountAndCount(totalDamageNode, totalDamage);
            }
            Node totalHealingNode = XmlHelper.getChildNode(rootNode, totalHealingTag);
            if (totalHealingNode != null) {
                parseAmountAndCount(totalHealingNode, totalHealing);
            }
            
            Node playerNode = XmlHelper.getChildNode(rootNode, playerTag);
            if (playerNode!=null) {
                Node playerDamageNode = XmlHelper.getChildNode(playerNode, damageTag);
                if (playerDamageNode != null) {
                    Node playerDamageTotalNode = XmlHelper.getChildNode(playerDamageNode, totalDamageTag);
                    if (playerDamageTotalNode != null) {
                        playerDamageTotaldamage = new AmountAndCount();
                        parseAmountAndCount(playerDamageTotalNode, playerDamageTotaldamage);
                    }
                    Node playerDamageSwingNode = XmlHelper.getChildNode(playerDamageNode, swingTag);
                    if (playerDamageSwingNode != null) {
                        playerDamageSwingTotal = new AmountAndCount();
                        parseSkillSpellNode(playerDamageSwingNode, playerDamageSwingTotal, null);
                    }
                    Node playerDamagePhysicalskillsNode = XmlHelper.getChildNode(playerDamageNode, physicalSkillsTag);
                    if (playerDamagePhysicalskillsNode != null) {
                        playerDamagePhysicalskillsTotal = new AmountAndCount();
                        playerDamagePhysicalskillsSingleSpellinfo = new ArrayList<XMLSpellInfo>();
                        parseSkillSpellNode(playerDamagePhysicalskillsNode, playerDamagePhysicalskillsTotal, playerDamagePhysicalskillsSingleSpellinfo);
                    }
                    Node playerDamageRangedNode = XmlHelper.getChildNode(playerDamageNode, rangedTag);
                    if (playerDamageRangedNode != null) {
                        playerDamageRangedTotal = new AmountAndCount();
                        playerDamageRangedSingleSpellinfo = new ArrayList<XMLSpellInfo>();
                        parseSkillSpellNode(playerDamageRangedNode, playerDamageRangedTotal, playerDamageRangedSingleSpellinfo);
                    }
                    Node playerDamageSpellsNode = XmlHelper.getChildNode(playerDamageNode, spellsTag);
                    if (playerDamageSpellsNode != null) {
                        playerDamageSpellsTotal = new AmountAndCount();
                        playerDamageSpellsSingleSpellinfo = new ArrayList<XMLSpellInfo>();
                        parseSkillSpellNode(playerDamageSpellsNode, playerDamageSpellsTotal, playerDamageSpellsSingleSpellinfo);
                    }
                }
                Node playerHealingNode = XmlHelper.getChildNode(playerNode, healingTag);
                if (playerHealingNode != null) {
                    Node playerHealingTotalNode = XmlHelper.getChildNode(playerHealingNode, totalHealingTag);
                    if (playerHealingTotalNode != null) {
                        playerHealingTotalhealing = new AmountAndCount();
                        parseAmountAndCount(playerHealingTotalNode, playerHealingTotalhealing);
                    }                    
                    Node playerHealingSpellsNode = XmlHelper.getChildNode(playerHealingNode, spellsTag);
                    if (playerHealingSpellsNode != null) {
                        playerHealingSpellsTotal = new AmountAndCount();
                        playerHealingSpellsSingleSpellinfo = new ArrayList<XMLSpellInfo>();
                        parseSkillSpellNode(playerHealingSpellsNode, playerHealingSpellsTotal, playerHealingSpellsSingleSpellinfo);
                    }
                }
                Node playerPowerNode = XmlHelper.getChildNode(playerNode, powerTag);
                if (playerPowerNode != null) {
                    Node playerPowerTotalNode = XmlHelper.getChildNode(playerPowerNode, totalPowerTag);
                    if (playerPowerTotalNode != null) {
                        playerPowerTotalpower = new AmountAndCount();
                        parseAmountAndCount(playerPowerTotalNode, playerPowerTotalpower);
                    }                    
                    Node playerPowerSpellsNode = XmlHelper.getChildNode(playerPowerNode, spellsTag);
                    if (playerPowerSpellsNode != null) {
                        playerPowerSpellsTotal = new AmountAndCount();
                        playerPowerSpellsSingleSpellinfo = new ArrayList<XMLSpellInfo>();
                        parseSkillSpellNode(playerPowerSpellsNode, playerPowerSpellsTotal, playerPowerSpellsSingleSpellinfo);
                    }
                }
            }
            Node petNode = XmlHelper.getChildNode(rootNode, petTag);
            if (petNode!=null) {
                Node petsNode = XmlHelper.getChildNode(petNode, petsTag);
                if (petsNode != null) {
                    pets = new ArrayList<PetInfo>();
                    List<Node> petInfoNodes = XmlHelper.getChildNodes(petsNode, petInfoTag);
                    for (Node n : petInfoNodes) {
                        NamedNodeMap petAttrs = n.getAttributes();
                        if (petAttrs != null) {
                            Node petNameNode = petAttrs.getNamedItem(nameTag);
                            Node petGuidNode = petAttrs.getNamedItem(guidTag);
                            if (petNameNode != null && petGuidNode != null) {
                                PetInfo pi = new PetInfo();
                                pi.setName(petNameNode.getTextContent().trim());
                                pi.setSourceGUID(petGuidNode.getTextContent().trim());
                                pets.add(pi);
                            }
                        }
                    }
                }
                Node petDamageNode = XmlHelper.getChildNode(petNode, damageTag);
                if (petDamageNode != null) {
                    Node petDamageTotalNode = XmlHelper.getChildNode(petDamageNode, totalDamageTag);
                    if (petDamageTotalNode != null) {
                        petDamageTotaldamage = new AmountAndCount();
                        parseAmountAndCount(petDamageTotalNode, petDamageTotaldamage);
                    }
                    Node petDamageSwingNode = XmlHelper.getChildNode(petDamageNode, swingTag);
                    if (petDamageSwingNode != null) {
                        petDamageSwingTotal = new AmountAndCount();
                        parseSkillSpellNode(petDamageSwingNode, petDamageSwingTotal, null);
                    }
                    Node petDamagePhysicalskillsNode = XmlHelper.getChildNode(petDamageNode, physicalSkillsTag);
                    if (petDamagePhysicalskillsNode != null) {
                        petDamagePhysicalskillsTotal = new AmountAndCount();
                        petDamagePhysicalskillsSingleSpellinfo = new ArrayList<XMLSpellInfo>();
                        parseSkillSpellNode(petDamagePhysicalskillsNode, petDamagePhysicalskillsTotal, petDamagePhysicalskillsSingleSpellinfo);
                    }
                    Node petDamageRangedNode = XmlHelper.getChildNode(petDamageNode, rangedTag);
                    if (petDamageRangedNode != null) {
                        petDamageRangedTotal = new AmountAndCount();
                        petDamageRangedSingleSpellinfo = new ArrayList<XMLSpellInfo>();
                        parseSkillSpellNode(petDamageRangedNode, petDamageRangedTotal, petDamageRangedSingleSpellinfo);
                    }
                    Node petDamageSpellsNode = XmlHelper.getChildNode(petDamageNode, spellsTag);
                    if (petDamageSpellsNode != null) {
                        petDamageSpellsTotal = new AmountAndCount();
                        petDamageSpellsSingleSpellinfo = new ArrayList<XMLSpellInfo>();
                        parseSkillSpellNode(petDamageSpellsNode, petDamageSpellsTotal, petDamageSpellsSingleSpellinfo);
                    }
                }
                Node petHealingNode = XmlHelper.getChildNode(petNode, healingTag);
                if (petHealingNode != null) {
                    Node petHealingTotalNode = XmlHelper.getChildNode(petHealingNode, totalHealingTag);
                    if (petHealingTotalNode != null) {
                        petHealingTotalhealing = new AmountAndCount();
                        parseAmountAndCount(petHealingTotalNode, petHealingTotalhealing);
                    }                    
                    Node petHealingSpellsNode = XmlHelper.getChildNode(petHealingNode, spellsTag);
                    if (petHealingSpellsNode != null) {
                        petHealingSpellsTotal = new AmountAndCount();
                        petHealingSpellsSingleSpellinfo = new ArrayList<XMLSpellInfo>();
                        parseSkillSpellNode(petHealingSpellsNode, petHealingSpellsTotal, petHealingSpellsSingleSpellinfo);
                    }
                }
                Node petPowerNode = XmlHelper.getChildNode(petNode, powerTag);
                if (petPowerNode != null) {
                    Node petPowerTotalNode = XmlHelper.getChildNode(petPowerNode, totalPowerTag);
                    if (petPowerTotalNode != null) {
                        petPowerTotalpower = new AmountAndCount();
                        parseAmountAndCount(petPowerTotalNode, petPowerTotalpower);
                    }                    
                    Node petPowerSpellsNode = XmlHelper.getChildNode(petPowerNode, spellsTag);
                    if (petPowerSpellsNode != null) {
                        petPowerSpellsTotal = new AmountAndCount();
                        petPowerSpellsSingleSpellinfo = new ArrayList<XMLSpellInfo>();
                        parseSkillSpellNode(petPowerSpellsNode, petPowerSpellsTotal, petPowerSpellsSingleSpellinfo);
                    }
                }
            }
        }
        } catch(ParseException ex) {
            throw new NumberFormatException();
        }
    }

    /**
     * Parse for a spell/skill node
     * @param node The node
     * @param totalAmountAndCount Destination AmountAndCount for the total field.
     * @param spellInfoArray Destination SpellInfo array for the Single field
     */
    public void parseSkillSpellNode(Node node, AmountAndCount totalAmountAndCount, List<XMLSpellInfo> spellInfoArray) {
        Node totalNode = XmlHelper.getChildNode(node, totalTag);
        if (totalNode != null && totalAmountAndCount != null) {
            parseAmountAndCount(totalNode, totalAmountAndCount);
        }
        Node singleNode = XmlHelper.getChildNode(node, singleTag);
        if (singleNode != null && spellInfoArray != null) {
            List<Node> spellInfoNodes = XmlHelper.getChildNodes(singleNode, spellInfoTag);
            for (Node listNode : spellInfoNodes) {
                XMLSpellInfo xmlSi = parseSpellInfoNode(listNode);
                spellInfoArray.add(xmlSi);
            }
        }
        
    }
    
    public XMLSpellInfo parseSpellInfoNode(Node listNode) {
        XMLSpellInfo xmlSi = new XMLSpellInfo();
        NamedNodeMap attrs = listNode.getAttributes();
        if (attrs != null) {
            Node idNode = attrs.getNamedItem(idTag);
            if (idNode != null) {
                xmlSi.setId(idNode.getTextContent().trim());
            }
            Node nameNode = attrs.getNamedItem(nameTag);
            if (nameNode != null) {
                xmlSi.setName(nameNode.getTextContent().trim());
            }
            Node schoolNode = attrs.getNamedItem(schoolTag);
            if (schoolNode != null) {
                xmlSi.setSchool(schoolNode.getTextContent().trim());
            }
            Node powerTypeNode = attrs.getNamedItem(powerTypeTag);
            if (powerTypeNode != null) {
                xmlSi.setPowerType(powerTypeNode.getTextContent().trim());
            }
        }
        AmountAndCount a = new AmountAndCount();
        parseAmountAndCount(listNode, a);
        xmlSi.setA(a);
        return xmlSi;
    }
    
    public void parseAmountAndCount(Node nodeThatContainsAmountAndCount, AmountAndCount a) {

        Node rootNode = XmlHelper.getChildNode(nodeThatContainsAmountAndCount, amountAndCountTag);

        Node n;
        n = XmlHelper.getChildNode(rootNode, amountTag);
        if (n != null) {
            a.amount = Long.parseLong(n.getTextContent().trim());
        }
        
        n = XmlHelper.getChildNode(rootNode, maxAmountTag);
        if (n != null) {
            a.maxAmount = Long.parseLong(n.getTextContent().trim());
        }

        n = XmlHelper.getChildNode(rootNode, hitTag);
        if (n != null) {
            a.hit = Integer.parseInt(n.getTextContent().trim());
        }

        n = XmlHelper.getChildNode(rootNode, critTag);
        if (n != null) {
            a.crit = Integer.parseInt(n.getTextContent().trim());
        }

        n = XmlHelper.getChildNode(rootNode, totHitTag);
        if (n != null) {
            a.totHit = Integer.parseInt(n.getTextContent().trim());
        }

        n = XmlHelper.getChildNode(rootNode, totMissTag);
        if (n != null) {
            a.totMiss = Integer.parseInt(n.getTextContent().trim());
        }

        n = XmlHelper.getChildNode(rootNode, missTag);
        if (n != null) {
            a.miss = Integer.parseInt(n.getTextContent().trim());
        }

        n = XmlHelper.getChildNode(rootNode, dodgeTag);
        if (n != null) {
            a.dodge = Integer.parseInt(n.getTextContent().trim());
        }

        n = XmlHelper.getChildNode(rootNode, parryTag);
        if (n != null) {
            a.parry = Integer.parseInt(n.getTextContent().trim());
        }
        
        n = XmlHelper.getChildNode(rootNode, absorbTag);
        if (n != null) {
            a.absorb = Integer.parseInt(n.getTextContent().trim());
        }
        
        n = XmlHelper.getChildNode(rootNode, blockTag);
        if (n != null) {
            a.block = Integer.parseInt(n.getTextContent().trim());
        }

        n = XmlHelper.getChildNode(rootNode, resistTag);
        if (n != null) {
            a.resist = Integer.parseInt(n.getTextContent().trim());
        }
        
        n = XmlHelper.getChildNode(rootNode, reflectTag);
        if (n != null) {
            a.reflect = Integer.parseInt(n.getTextContent().trim());
        }
        
        n = XmlHelper.getChildNode(rootNode, crushingTag);
        if (n != null) {
            a.crushing = Integer.parseInt(n.getTextContent().trim());
        }

        n = XmlHelper.getChildNode(rootNode, glancingTag);
        if (n != null) {
            a.glancing = Integer.parseInt(n.getTextContent().trim());
        }

        n = XmlHelper.getChildNode(rootNode, amountOverTag);
        if (n != null) {
            a.overAmount = Integer.parseInt(n.getTextContent().trim());
        }

        n = XmlHelper.getChildNode(rootNode, amountAbsorbedTag);
        if (n != null) {
            a.amountAbsorbed = Integer.parseInt(n.getTextContent().trim());
        }

        n = XmlHelper.getChildNode(rootNode, amountBlockedTag);
        if (n != null) {
            a.amountBlocked = Integer.parseInt(n.getTextContent().trim());
        }

        n = XmlHelper.getChildNode(rootNode, amountResistedTag);
        if (n != null) {
            a.amountResisted = Integer.parseInt(n.getTextContent().trim());
        }
    }

    public double getActiveDamageTime() {
        return activeDamageTime;
    }

    public double getActiveHealingTime() {
        return activeHealingTime;
    }

    public double getActivePowerTime() {
        return activePowerTime;
    }

    public String getClassName() {
        return className;
    }

    public String getName() {
        return name;
    }

    public List<XMLSpellInfo> getPetDamagePhysicalskillsSingleSpellinfo() {
        return petDamagePhysicalskillsSingleSpellinfo;
    }

    public AmountAndCount getPetDamagePhysicalskillsTotal() {
        return petDamagePhysicalskillsTotal;
    }

    public List<XMLSpellInfo> getPetDamageRangedSingleSpellinfo() {
        return petDamageRangedSingleSpellinfo;
    }

    public AmountAndCount getPetDamageRangedTotal() {
        return petDamageRangedTotal;
    }

    public List<XMLSpellInfo> getPetDamageSpellsSingleSpellinfo() {
        return petDamageSpellsSingleSpellinfo;
    }

    public AmountAndCount getPetDamageSpellsTotal() {
        return petDamageSpellsTotal;
    }

    public List<XMLSpellInfo> getPetDamageSwingSingleSpellinfo() {
        return petDamageSwingSingleSpellinfo;
    }

    public AmountAndCount getPetDamageSwingTotal() {
        return petDamageSwingTotal;
    }

    public AmountAndCount getPetDamageTotaldamage() {
        return petDamageTotaldamage;
    }

    public List<XMLSpellInfo> getPetHealingSpellsSingleSpellinfo() {
        return petHealingSpellsSingleSpellinfo;
    }

    public AmountAndCount getPetHealingSpellsTotal() {
        return petHealingSpellsTotal;
    }

    public AmountAndCount getPetHealingTotalhealing() {
        return petHealingTotalhealing;
    }

    public List<XMLSpellInfo> getPetPowerSpellsSingleSpellinfo() {
        return petPowerSpellsSingleSpellinfo;
    }

    public AmountAndCount getPetPowerSpellsTotal() {
        return petPowerSpellsTotal;
    }

    public AmountAndCount getPetPowerTotalpower() {
        return petPowerTotalpower;
    }

    public List<XMLSpellInfo> getPlayerDamagePhysicalskillsSingleSpellinfo() {
        return playerDamagePhysicalskillsSingleSpellinfo;
    }

    public AmountAndCount getPlayerDamagePhysicalskillsTotal() {
        return playerDamagePhysicalskillsTotal;
    }

    public List<XMLSpellInfo> getPlayerDamageRangedSingleSpellinfo() {
        return playerDamageRangedSingleSpellinfo;
    }

    public AmountAndCount getPlayerDamageRangedTotal() {
        return playerDamageRangedTotal;
    }

    public List<XMLSpellInfo> getPlayerDamageSpellsSingleSpellinfo() {
        return playerDamageSpellsSingleSpellinfo;
    }

    public AmountAndCount getPlayerDamageSpellsTotal() {
        return playerDamageSpellsTotal;
    }

    public List<XMLSpellInfo> getPlayerDamageSwingSingleSpellinfo() {
        return playerDamageSwingSingleSpellinfo;
    }

    public AmountAndCount getPlayerDamageSwingTotal() {
        return playerDamageSwingTotal;
    }

    public AmountAndCount getPlayerDamageTotaldamage() {
        return playerDamageTotaldamage;
    }

    public List<XMLSpellInfo> getPlayerHealingSpellsSingleSpellinfo() {
        return playerHealingSpellsSingleSpellinfo;
    }

    public AmountAndCount getPlayerHealingSpellsTotal() {
        return playerHealingSpellsTotal;
    }

    public AmountAndCount getPlayerHealingTotalhealing() {
        return playerHealingTotalhealing;
    }

    public List<XMLSpellInfo> getPlayerPowerSpellsSingleSpellinfo() {
        return playerPowerSpellsSingleSpellinfo;
    }

    public AmountAndCount getPlayerPowerSpellsTotal() {
        return playerPowerSpellsTotal;
    }

    public AmountAndCount getPlayerPowerTotalpower() {
        return playerPowerTotalpower;
    }

    public XMLFightParticipant getReceivedParticipant() {
        return receivedParticipant;
    }

    public AmountAndCount getTotalDamage() {
        return totalDamage;
    }

    public AmountAndCount getTotalHealing() {
        return totalHealing;
    }
    
    
}


