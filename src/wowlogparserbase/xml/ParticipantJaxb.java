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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jfree.data.xy.DefaultXYDataset;
import wowlogparserbase.*;
import wowlogparserbase.events.BasicEvent;
import wowlogparserbase.Constants;
import wowlogparserbase.helpers.MathHelper;
import wowlogparserbase.xmlbindings.fight.AmountAndCountXmlType;
import wowlogparserbase.xmlbindings.fight.DamageInfoXmlType;
import wowlogparserbase.xmlbindings.fight.HealingInfoXmlType;
import wowlogparserbase.xmlbindings.fight.ParticipantBaseXmlType;
import wowlogparserbase.xmlbindings.fight.ParticipantReceivedXmlType;
import wowlogparserbase.xmlbindings.fight.ParticipantXmlType;
import wowlogparserbase.xmlbindings.fight.PetInfoXmlType;
import wowlogparserbase.xmlbindings.fight.PlayerInfoXmlType;
import wowlogparserbase.xmlbindings.fight.PowerInfoXmlType;
import wowlogparserbase.xmlbindings.fight.SingleAndTotalXmlType;
import wowlogparserbase.xmlbindings.fight.SingleXmlType;
import wowlogparserbase.xmlbindings.fight.SpellInformationXmlType;
import wowlogparserbase.xmlbindings.fight.TotalOnlyXmlType;
import wowlogparserbase.xmlbindings.fight.TotalXmlType;

/**
 *
 * @author racy
 */
public class ParticipantJaxb {
    Fight fight;
    FightParticipant participant;
    double activeTime = 0;
    double activeDamageTime = 0;
    double activeHealingTime = 0;
    double activePowerTime = 0;
    Set<SpellInfo> spellInfos;
    Set<SpellInfoExtended> spellInfosExtended;
    Set<Integer> spellIds;
    Map<Integer, FightParticipant> spellEvents;

    public ParticipantJaxb(Fight fight, FightParticipant participant) {
        this.fight = fight;
        this.participant = participant;
        activeTime = participant.getActiveDuration();
        activeDamageTime = participant.getActiveDamageTime(15, 1.5, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        activeHealingTime = participant.getActiveHealingTime(15, 1.5, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        activePowerTime = participant.getActivePowerTime(15, 1.5, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        spellInfos = new HashSet<SpellInfo>(participant.getIDs(FightParticipant.TYPE_ANY_SPELL));
        spellInfosExtended = new HashSet<SpellInfoExtended>();
        spellInfosExtended.addAll(participant.getIDsExtended(FightParticipant.UNIT_PLAYER, FightParticipant.TYPE_ANY_SPELL, FightParticipant.UNIT_PLAYER));
        spellInfosExtended.addAll(participant.getIDsExtended(FightParticipant.UNIT_PET, FightParticipant.TYPE_ANY_SPELL, FightParticipant.UNIT_PET));
        spellIds = new HashSet<Integer>();
        for (SpellInfo si : spellInfos) {
            spellIds.add(si.spellID);
        }
        spellEvents = participant.splitSpellEvents(spellIds);
    }

    public ParticipantBaseXmlType makeParticipantObject(boolean received) {
        AmountAndCount a;
        AmountAndCountXmlType aXml;
        ParticipantBaseXmlType pXml;
        if (received) {
            pXml = new ParticipantReceivedXmlType();
        } else {
            pXml = new ParticipantXmlType();
        }
        pXml.setActiveDuration(activeTime);
        pXml.setActiveDamageDuration(activeDamageTime);
        pXml.setActiveHealingDuration(activeHealingTime);
        pXml.setClazz(participant.getClassName());
        pXml.setGuid(participant.getSourceGUID());
        pXml.setName(participant.getName());
        
        DefaultXYDataset ds;

        double[][] damagePlotData = WlpPlotFactory.createParticipantDamageLinePlotData(fight, participant, 5);
        ds = new DefaultXYDataset();
        ds.addSeries("0", damagePlotData);
        pXml.setDamageGraph(FightJaxb.makeLineGraph(ds, 5));

        double[][] healingPlotData = WlpPlotFactory.createParticipantHealingLinePlotData(fight, participant, 5);
        ds = new DefaultXYDataset();
        ds.addSeries("0", healingPlotData);
        pXml.setHealingGraph(FightJaxb.makeLineGraph(ds, 5));

        TotalXmlType totDamage = new TotalXmlType();
        a = participant.totalDamage(Constants.SCHOOL_ALL, FightParticipant.TYPE_ALL_DAMAGE);
        aXml = new AmountAndCountXmlType();
        setAmountAndCountDamage(aXml, a);
        totDamage.setAmountAndCount(aXml);
        pXml.setTotalDamage(totDamage);

        TotalXmlType totHealing = new TotalXmlType();
        a = participant.totalHealing(Constants.SCHOOL_ALL, FightParticipant.TYPE_ALL_HEALING);
        aXml = new AmountAndCountXmlType();
        setAmountAndCountHealing(aXml, a);
        totHealing.setAmountAndCount(aXml);
        pXml.setTotalHealing(totHealing);

        PlayerInfoXmlType playerXml = makePlayerNode();
        pXml.setPlayer(playerXml);

        PetInfoXmlType petXml = makePetNode();
        pXml.setPet(petXml);

        if (!received) {
            ParticipantXmlType pXml2 = (ParticipantXmlType) pXml;
            ParticipantXmlType.ReceivedEvents receivedEvents = new ParticipantXmlType.ReceivedEvents();
            ParticipantJaxb partRecJaxb = new ParticipantJaxb(fight, participant.makeReceivedParticipant(fight));
            receivedEvents.setParticipant((ParticipantReceivedXmlType)partRecJaxb.makeParticipantObject(true));
            pXml2.setReceivedEvents(receivedEvents);
        }
        return pXml;
    }

    private PlayerInfoXmlType makePlayerNode() {

        PlayerInfoXmlType player = new PlayerInfoXmlType();

        DefaultXYDataset ds;

        double[][] damagePlotData = WlpPlotFactory.createParticipantDamageLinePlotData(fight, participant, 5, FightParticipant.UNIT_PLAYER);
        ds = new DefaultXYDataset();
        ds.addSeries("0", damagePlotData);
        player.setDamageGraph(FightJaxb.makeLineGraph(ds, 5));
        
        double[][] healingPlotData = WlpPlotFactory.createParticipantHealingLinePlotData(fight, participant, 5, FightParticipant.UNIT_PLAYER);
        ds = new DefaultXYDataset();
        ds.addSeries("0", healingPlotData);
        player.setHealingGraph(FightJaxb.makeLineGraph(ds, 5));

        DamageInfoXmlType damage = new DamageInfoXmlType();
        damage.setSpells(makeDamageSpells(EventCollection.UNIT_PLAYER));
        damage.setRanged(makeDamageRanged(EventCollection.UNIT_PLAYER));
        damage.setPhysicalSkills(makeDamagePhysicalSkills(EventCollection.UNIT_PLAYER));
        damage.setSwing(makeDamageSwing(EventCollection.UNIT_PLAYER));
        damage.setTotalDamage(makeDamageTotal(EventCollection.UNIT_PLAYER));
        player.setDamage(damage);

        HealingInfoXmlType healing = new HealingInfoXmlType();
        healing.setSpells(makeHealingSpells(EventCollection.TYPE_SPELL_HEALING, EventCollection.UNIT_PLAYER));
        healing.setSpellsDirect(makeHealingSpells(EventCollection.TYPE_SPELL_HEALING_DIRECT, EventCollection.UNIT_PLAYER));
        healing.setSpellsPeriodic(makeHealingSpells(EventCollection.TYPE_SPELL_HEALING_PERIODIC, EventCollection.UNIT_PLAYER));
        healing.setTotalHealing(makeHealingTotal(EventCollection.UNIT_PLAYER));
        player.setHealing(healing);

        PowerInfoXmlType power = new PowerInfoXmlType();
        power.setSpells(makePowerSpells(EventCollection.TYPE_ALL_POWER, EventCollection.UNIT_PLAYER));
        power.setTotalPower(makePowerTotal(EventCollection.UNIT_PLAYER));
        player.setPower(power);

        return player;
    }

    private PetInfoXmlType makePetNode() {

        PetInfoXmlType pet = new PetInfoXmlType();

        DefaultXYDataset ds;

        double[][] damagePlotData = WlpPlotFactory.createParticipantDamageLinePlotData(fight, participant, 5, FightParticipant.UNIT_PET);
        ds = new DefaultXYDataset();
        ds.addSeries("0", damagePlotData);
        pet.setDamageGraph(FightJaxb.makeLineGraph(ds, 5));
        
        double[][] healingPlotData = WlpPlotFactory.createParticipantHealingLinePlotData(fight, participant, 5, FightParticipant.UNIT_PET);
        ds = new DefaultXYDataset();
        ds.addSeries("0", healingPlotData);
        pet.setHealingGraph(FightJaxb.makeLineGraph(ds, 5));

        DamageInfoXmlType damage = new DamageInfoXmlType();
        damage.setSpells(makeDamageSpells(EventCollection.UNIT_PET));
        damage.setRanged(makeDamageRanged(EventCollection.UNIT_PET));
        damage.setPhysicalSkills(makeDamagePhysicalSkills(EventCollection.UNIT_PET));
        damage.setSwing(makeDamageSwing(EventCollection.UNIT_PET));
        damage.setTotalDamage(makeDamageTotal(EventCollection.UNIT_PET));
        pet.setDamage(damage);

        HealingInfoXmlType healing = new HealingInfoXmlType();
        healing.setSpells(makeHealingSpells(EventCollection.TYPE_SPELL_HEALING, EventCollection.UNIT_PET));
        healing.setSpellsDirect(makeHealingSpells(EventCollection.TYPE_SPELL_HEALING_DIRECT, EventCollection.UNIT_PET));
        healing.setSpellsPeriodic(makeHealingSpells(EventCollection.TYPE_SPELL_HEALING_PERIODIC, EventCollection.UNIT_PET));
        healing.setTotalHealing(makeHealingTotal(EventCollection.UNIT_PET));
        pet.setHealing(healing);

        PowerInfoXmlType power = new PowerInfoXmlType();
        power.setSpells(makePowerSpells(EventCollection.TYPE_ALL_POWER, EventCollection.UNIT_PET));
        power.setTotalPower(makePowerTotal(EventCollection.UNIT_PET));
        pet.setPower(power);

        return pet;
    }

    private TotalXmlType makePowerTotal(int unit) {
        AmountAndCount a;
        AmountAndCountXmlType aXml;

        TotalXmlType total = new TotalXmlType();
        a = participant.totalPower(Constants.SCHOOL_ALL, FightParticipant.TYPE_ALL_POWER, unit);
        aXml = new AmountAndCountXmlType();
        setAmountAndCountPower(aXml, a);
        total.setAmountAndCount(aXml);

        return total;
    }

    private SingleAndTotalXmlType makePowerSpells(int type, int unit) {
        AmountAndCount a;
        AmountAndCountXmlType aXml;

        SingleAndTotalXmlType spells = new SingleAndTotalXmlType();

        TotalXmlType total = new TotalXmlType();
        a = participant.totalPower(Constants.SCHOOL_ALL, type, unit);
        aXml = new AmountAndCountXmlType();
        setAmountAndCountPower(aXml, a);
        total.setAmountAndCount(aXml);
        spells.setTotal(total);

        SingleXmlType single = new SingleXmlType();
        //List<SpellInfo> spellInfoArray = participant.getIDs(type, unit);
        List<SpellInfoExtended> spellInfoArrayTemp = AbstractEventCollection.getIDsExtendedFromList(spellInfosExtended, type, unit);
        //Fix to remove difference between periodic and non periodic spells
        Set<SpellInfo> spellInfoArray = new HashSet<SpellInfo>();
        for (SpellInfoExtended sie : spellInfoArrayTemp) {
            spellInfoArray.add(new SpellInfo(sie));
        }
        for(SpellInfo si : spellInfoArray) {
            SpellInformationXmlType siXml = new SpellInformationXmlType();
            siXml.setId(si.spellID);
            siXml.setName(si.name);
            siXml.setSchool(BasicEvent.getSchoolStringFromFlags(si.school));

            //a = participant.power(si, type, unit);
            a = spellEvents.get(si.spellID).power(si, type, unit);
            aXml = new AmountAndCountXmlType();
            setAmountAndCountPower(aXml, a);
            siXml.setAmountAndCount(aXml);
            single.getSpellInfo().add(siXml);
        }
        spells.setSingle(single);
        return spells;
    }

    private TotalXmlType makeHealingTotal(int unit) {
        AmountAndCount a;
        AmountAndCountXmlType aXml;

        TotalXmlType total = new TotalXmlType();
        a = participant.totalHealing(Constants.SCHOOL_ALL, FightParticipant.TYPE_ALL_HEALING, unit);
        aXml = new AmountAndCountXmlType();
        setAmountAndCountHealing(aXml, a);
        total.setAmountAndCount(aXml);

        return total;
    }

    private SingleAndTotalXmlType makeHealingSpells(int type, int unit) {
        AmountAndCount a;
        AmountAndCountXmlType aXml;

        SingleAndTotalXmlType spells = new SingleAndTotalXmlType();

        TotalXmlType total = new TotalXmlType();
        a = participant.totalHealing(Constants.SCHOOL_ALL, type, unit);
        aXml = new AmountAndCountXmlType();
        setAmountAndCountHealing(aXml, a);
        total.setAmountAndCount(aXml);
        spells.setTotal(total);

        SingleXmlType single = new SingleXmlType();
        //List<SpellInfo> spellInfoArray = participant.getIDs(type, unit);
        List<SpellInfoExtended> spellInfoArrayTemp = AbstractEventCollection.getIDsExtendedFromList(spellInfosExtended, type, unit);
        //Fix to remove difference between periodic and non periodic spells
        Set<SpellInfo> spellInfoArray = new HashSet<SpellInfo>();
        for (SpellInfoExtended sie : spellInfoArrayTemp) {
            spellInfoArray.add(new SpellInfo(sie));
        }
        for(SpellInfo si : spellInfoArray) {
            SpellInformationXmlType siXml = new SpellInformationXmlType();
            siXml.setId(si.spellID);
            siXml.setName(si.name);
            siXml.setSchool(BasicEvent.getSchoolStringFromFlags(si.school));

            //a = participant.healing(si, type, unit);
            a = spellEvents.get(si.spellID).healing(si, type, unit);
            aXml = new AmountAndCountXmlType();
            setAmountAndCountHealing(aXml, a);
            siXml.setAmountAndCount(aXml);
            single.getSpellInfo().add(siXml);
        }
        spells.setSingle(single);
        return spells;
    }

    private TotalXmlType makeDamageTotal(int unit) {
        AmountAndCount a;
        AmountAndCountXmlType aXml;

        TotalXmlType total = new TotalXmlType();
        a = participant.totalDamage(Constants.SCHOOL_ALL, FightParticipant.TYPE_ALL_DAMAGE, unit);
        aXml = new AmountAndCountXmlType();
        setAmountAndCountDamage(aXml, a);
        total.setAmountAndCount(aXml);

        return total;
    }

    private TotalOnlyXmlType makeDamageSwing(int unit) {
        AmountAndCount a;
        AmountAndCountXmlType aXml;

        TotalOnlyXmlType swing = new TotalOnlyXmlType();
        TotalXmlType swingTotal = new TotalXmlType();
        a = participant.totalDamage(Constants.SCHOOL_ALL, FightParticipant.TYPE_SWING_DAMAGE, unit);
        aXml = new AmountAndCountXmlType();
        setAmountAndCountDamage(aXml, a);
        swingTotal.setAmountAndCount(aXml);
        swing.setTotal(swingTotal);
        
        return swing;
    }

    private SingleAndTotalXmlType makeDamageSpells(int unit) {
        AmountAndCount a;
        AmountAndCountXmlType aXml;

        int type = FightParticipant.TYPE_SPELL_DAMAGE;
        int spellSchools =
                Constants.SCHOOL_ARCANE | Constants.SCHOOL_FIRE |
                Constants.SCHOOL_FROST | Constants.SCHOOL_HOLY |
                Constants.SCHOOL_NATURE | Constants.SCHOOL_SHADOW;

        SingleAndTotalXmlType spells = new SingleAndTotalXmlType();
        TotalXmlType total = new TotalXmlType();
        a = participant.totalDamage(spellSchools, type, unit);
        aXml = new AmountAndCountXmlType();
        setAmountAndCountDamage(aXml, a);
        total.setAmountAndCount(aXml);
        spells.setTotal(total);

        SingleXmlType single = new SingleXmlType();
        //List<SpellInfo> spellInfoArray = participant.getIDs(type, unit);
        List<SpellInfoExtended> spellInfoArrayTemp = AbstractEventCollection.getIDsExtendedFromList(spellInfosExtended, type, unit);
        //Fix to remove difference between periodic and non periodic spells
        Set<SpellInfo> spellInfoArray = new HashSet<SpellInfo>();
        for (SpellInfoExtended sie : spellInfoArrayTemp) {
            spellInfoArray.add(new SpellInfo(sie));
        }
        for(SpellInfo si : spellInfoArray) {
            if ((si.school & spellSchools) == 0) {
                continue;
            }
            SpellInformationXmlType siXml = new SpellInformationXmlType();
            siXml.setId(si.spellID);
            siXml.setName(si.name);
            siXml.setSchool(BasicEvent.getSchoolStringFromFlags(si.school));

            //a = participant.damage(si, type, unit);
            a = spellEvents.get(si.spellID).damage(si, type, unit);
            aXml = new AmountAndCountXmlType();
            setAmountAndCountDamage(aXml, a);
            siXml.setAmountAndCount(aXml);
            single.getSpellInfo().add(siXml);
        }
        spells.setSingle(single);
        return spells;
    }

    private SingleAndTotalXmlType makeDamageRanged(int unit) {
        AmountAndCount a;
        AmountAndCountXmlType aXml;

        int type = FightParticipant.TYPE_RANGED_DAMAGE;
        int spellSchools = Constants.SCHOOL_ALL;

        SingleAndTotalXmlType ranged = new SingleAndTotalXmlType();
        TotalXmlType total = new TotalXmlType();
        a = participant.totalDamage(spellSchools, type, unit);
        aXml = new AmountAndCountXmlType();
        setAmountAndCountDamage(aXml, a);
        total.setAmountAndCount(aXml);
        ranged.setTotal(total);

        SingleXmlType single = new SingleXmlType();
        //List<SpellInfo> spellInfoArray = participant.getIDs(type, unit);
        List<SpellInfoExtended> spellInfoArrayTemp = AbstractEventCollection.getIDsExtendedFromList(spellInfosExtended, type, unit);
        //Fix to remove difference between periodic and non periodic spells
        Set<SpellInfo> spellInfoArray = new HashSet<SpellInfo>();
        for (SpellInfoExtended sie : spellInfoArrayTemp) {
            spellInfoArray.add(new SpellInfo(sie));
        }
        for(SpellInfo si : spellInfoArray) {
            if ((si.school & spellSchools) == 0) {
                continue;
            }
            SpellInformationXmlType siXml = new SpellInformationXmlType();
            siXml.setId(si.spellID);
            siXml.setName(si.name);
            siXml.setSchool(BasicEvent.getSchoolStringFromFlags(si.school));

            //a = participant.damage(si, type, unit);
            a = spellEvents.get(si.spellID).damage(si, type, unit);
            aXml = new AmountAndCountXmlType();
            setAmountAndCountDamage(aXml, a);
            siXml.setAmountAndCount(aXml);
            single.getSpellInfo().add(siXml);
        }
        ranged.setSingle(single);
        return ranged;
    }

    private SingleAndTotalXmlType makeDamagePhysicalSkills(int unit) {
        AmountAndCount a;
        AmountAndCountXmlType aXml;

        int type = FightParticipant.TYPE_SPELL_DAMAGE;
        int spellSchools = Constants.SCHOOL_PHYSICAL;

        SingleAndTotalXmlType physical = new SingleAndTotalXmlType();
        TotalXmlType total = new TotalXmlType();
        a = participant.totalDamage(spellSchools, type, unit);
        aXml = new AmountAndCountXmlType();
        setAmountAndCountDamage(aXml, a);
        total.setAmountAndCount(aXml);
        physical.setTotal(total);

        SingleXmlType single = new SingleXmlType();
        //List<SpellInfo> spellInfoArray = participant.getIDs(type, unit);
        List<SpellInfoExtended> spellInfoArrayTemp = AbstractEventCollection.getIDsExtendedFromList(spellInfosExtended, type, unit);
        //Fix to remove difference between periodic and non periodic spells
        Set<SpellInfo> spellInfoArray = new HashSet<SpellInfo>();
        for (SpellInfoExtended sie : spellInfoArrayTemp) {
            spellInfoArray.add(new SpellInfo(sie));
        }
        for(SpellInfo si : spellInfoArray) {
            if ((si.school & spellSchools) == 0) {
                continue;
            }
            SpellInformationXmlType siXml = new SpellInformationXmlType();
            siXml.setId(si.spellID);
            siXml.setName(si.name);
            siXml.setSchool(BasicEvent.getSchoolStringFromFlags(si.school));

            //a = participant.damage(si, type, unit);
            a = spellEvents.get(si.spellID).damage(si, type, unit);
            aXml = new AmountAndCountXmlType();
            setAmountAndCountDamage(aXml, a);
            siXml.setAmountAndCount(aXml);
            single.getSpellInfo().add(siXml);
        }
        physical.setSingle(single);
        return physical;
    }

    private void setAmountAndCountDamage(AmountAndCountXmlType aXml, AmountAndCount a) {
        if (a.absorb != 0) {
            aXml.setAb(a.absorb);
        }
        aXml.setAm(a.amount);
        if (a.amountAbsorbed != 0) {
            aXml.setAmAb(a.amountAbsorbed);
        }
        if (a.amountBlocked != 0) {
            aXml.setAmBl(a.amountBlocked);
        }
        if (a.overAmount != 0) {
            aXml.setAmOv(a.overAmount);
        }
        if (a.amountResisted != 0) {
            aXml.setAmRe(a.amountResisted);
        }
        if (a.totHit == 0) {
            aXml.setAvA(0);
        } else {
            aXml.setAvA(MathHelper.round2Decimals((double)a.amount / (double)a.totHit));
        }
        if (a.block != 0) {
            aXml.setBl(a.block);
        }
        if (a.crit != 0) {
            aXml.setCri(a.crit);
        }
        if (a.crushing != 0) {
            aXml.setCru(a.crushing);
        }
        if (a.dodge != 0) {
            aXml.setDo(a.dodge);
        }
        if (activeDamageTime > 0) {
            aXml.setDps(MathHelper.round2Decimals((double)a.amount / activeDamageTime));
        }
        if (a.glancing != 0) {
            aXml.setGla(a.glancing);
        }
        aXml.setHi(a.hit);
        aXml.setMaA(a.maxAmount);
        if (a.miss != 0) {
            aXml.setMi(a.miss);
        }
        if (a.parry != 0) {
            aXml.setPa(a.parry);
        }
        if (a.reflect != 0) {
            aXml.setRef(a.reflect);
        }
        if (a.resist != 0) {
            aXml.setRes(a.resist);
        }
        aXml.setTHi(a.totHit);
        if (a.totMiss != 0) {
            aXml.setTMi(a.totMiss);
        }
    }

    private void setAmountAndCountHealing(AmountAndCountXmlType aXml, AmountAndCount a) {
        aXml.setAm(a.amount);
        if (a.amountAbsorbed != 0) {
            aXml.setAmAb(a.amountAbsorbed);
        }
        if (a.amountBlocked != 0) {
            aXml.setAmBl(a.amountBlocked);
        }
        if (a.overAmount != 0) {
            aXml.setAmOv(a.overAmount);
        }
        if (a.amountResisted != 0) {
            aXml.setAmRe(a.amountResisted);
        }
        if (a.totHit == 0) {
            aXml.setAvA(0);
        } else {
            aXml.setAvA(MathHelper.round2Decimals((double)a.amount / (double)a.totHit));
        }
        if (a.crit != 0) {
            aXml.setCri(a.crit);
        }
        if (activeHealingTime > 0) {
            aXml.setHps(MathHelper.round2Decimals((double)a.amount / activeHealingTime));
        }
        if (a.glancing != 0) {
            aXml.setGla(a.glancing);
        }
        aXml.setHi(a.hit);
        aXml.setMaA(a.maxAmount);
        aXml.setTHi(a.totHit);
        if (a.totMiss != 0) {
            aXml.setTMi(a.totMiss);
        }
    }

    private void setAmountAndCountPower(AmountAndCountXmlType aXml, AmountAndCount a) {
        aXml.setAm(a.amount);
        if (a.totHit == 0) {
            aXml.setAvA(0);
        } else {
            aXml.setAvA(MathHelper.round2Decimals((double)a.amount / (double)a.totHit));
        }
        if (a.crit != 0) {
            aXml.setCri(a.crit);
        }
        if (activePowerTime > 0) {
            aXml.setPps(MathHelper.round2Decimals((double)a.amount / activePowerTime));
        }
        if (a.glancing != 0) {
            aXml.setGla(a.glancing);
        }
        aXml.setHi(a.hit);
        aXml.setMaA(a.maxAmount);
        aXml.setTHi(a.totHit);
        if (a.totMiss != 0) {
            aXml.setTMi(a.totMiss);
        }
    }
}
