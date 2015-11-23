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

import java.util.Arrays;
import java.util.List;
import javax.swing.table.TableModel;
import org.jfree.data.xy.DefaultXYDataset;
import wowlogparserbase.AmountAndCount;
import wowlogparserbase.DispellEvents;
import wowlogparserbase.Fight;
import wowlogparserbase.FightParticipant;
import wowlogparserbase.InterruptEvents;
import wowlogparserbase.WlpPlotFactory;
import wowlogparserbase.Constants;
import wowlogparserbase.events.SpellInterruptEvent;
import wowlogparserbase.events.aura.SpellAuraDispelledEvent;
import wowlogparserbase.helpers.MathHelper;
import wowlogparserbase.tablemodels.WhoHealsWhomTableModel;
import wowlogparserbase.xmlbindings.fight.*;

/**
 *
 * @author racy
 */
public class FightJaxb {
    Fight fight;

    public FightJaxb(Fight fight) {
        this.fight = fight;
    }

    public static LineGraphXmlType makeLineGraph(DefaultXYDataset dataset, double xStepSize) {
        LineGraphXmlType out = new LineGraphXmlType();
        int numValuesDamage = dataset.getItemCount(0);
        List<Double> xVals = out.getXValues();
        List<Double> yVals = out.getYValues();
        for (int k=0; k<numValuesDamage; k++) {
            double x = dataset.getXValue(0, k);
            double y = dataset.getYValue(0, k);
            if (yVals.size() > 0) {
                if (yVals.get(yVals.size()-1) == 0 && y == 0) {
                }
                else {
                    xVals.add(x);
                    yVals.add(y);
                }
            } else {
                xVals.add(x);
                yVals.add(y);
            }
        }
        out.setNumPoints(out.getXValues().size());
        out.setXStep(xStepSize);
        return out;
    }

    public wowlogparserbase.xmlbindings.fight.Fight makeFightObject() {
        wowlogparserbase.xmlbindings.fight.Fight fightXml = new wowlogparserbase.xmlbindings.fight.Fight();
        fightXml.setActiveDuration(MathHelper.round2Decimals(fight.getActiveDuration()));
        fightXml.setDuration(MathHelper.round2Decimals(fight.getEndTime() - fight.getStartTime()));
        fightXml.setGuid(fight.getGuid());
        fightXml.setMerged(fight.isMerged());
        fightXml.setName(fight.getName());

        DefaultXYDataset raidDamageDataset = WlpPlotFactory.createRaidDamageLinePlotDataset(Arrays.asList(fight), Arrays.asList(true), Arrays.asList("fight"), 5);
        fightXml.setDamageGraph(makeLineGraph(raidDamageDataset, 5));

        DefaultXYDataset raidHealingDataset = WlpPlotFactory.createRaidHealingLinePlotDataset(Arrays.asList(fight), Arrays.asList(true), Arrays.asList("fight"), 5);
        fightXml.setHealingGraph(makeLineGraph(raidHealingDataset, 5));

        long damage = 0;
        long healing = 0;
        for (FightParticipant p : fight.getParticipants()) {
            AmountAndCount a = p.totalDamage(Constants.SCHOOL_ALL, FightParticipant.TYPE_ALL_DAMAGE);
            damage += a.amount;
            a = p.totalHealing(Constants.SCHOOL_ALL, FightParticipant.TYPE_ALL_HEALING);
            healing += a.amount;
        }
        fightXml.setTotalDamage(damage);
        fightXml.setTotalHealing(healing);
        NpcsXmlType npcs = new NpcsXmlType();
        if (fight.isMerged()) {
            fightXml.setNumMobs(fight.getMergedSourceFights().size());
            List<Fight> sourceFights = fight.getMergedSourceFights();
            for (Fight f : sourceFights) {
                NpcXmlType npc = createNpc(f);
                npcs.getNpc().add(npc);
            }
        } else {
            fightXml.setNumMobs(1);
            NpcXmlType npc = createNpc(fight);
            npcs.getNpc().add(npc);
        }
        fightXml.setNpcs(npcs);

        fightXml.setInterruptInfo(createInterruptInfo());
        fightXml.setDispelInfo(createDispelInfo());
        fightXml.setVictim(createVictim());
        for (FightParticipant p : fight.getParticipants()) {
            fightXml.getParticipant().add(createParticipant(p));
        }
        WhoHealsWhomTableModel whwTable = new WhoHealsWhomTableModel(fight);
        fightXml.setWhwTable(makeTable(whwTable));
        return fightXml;
    }

    private InterruptInfoXmlType createInterruptInfo() {
        InterruptInfoXmlType intInfo = new InterruptInfoXmlType();
        InterruptEvents ie = new InterruptEvents(fight.getEvents());
        int numNames = ie.getNumNames();
        for (int k = 0; k < numNames; k++) {
            InterruptInfoXmlType.SpellInterrupted spellInterrupted = new InterruptInfoXmlType.SpellInterrupted();
            spellInterrupted.setName(ie.getName(k));
            List<SpellInterruptEvent> events = ie.getEvents(ie.getName(k));
            for (SpellInterruptEvent ev : events) {
                SpellInterruptEventXmlType intEvXml = new SpellInterruptEventXmlType();
                ev.addXmlAttributesJaxb(intEvXml);
                spellInterrupted.getInterruptEvent().add(intEvXml);
            }
            intInfo.getSpellInterrupted().add(spellInterrupted);
        }
         return intInfo;
    }

    private DispelInfoXmlType createDispelInfo() {
        DispelInfoXmlType disInfo = new DispelInfoXmlType();
        DispellEvents de = new DispellEvents(fight.getEvents());
        int numNames = de.getNumNames();
        for (int k = 0; k < numNames; k++) {
            DispelInfoXmlType.AuraDispelled auraDispelled = new DispelInfoXmlType.AuraDispelled();
            auraDispelled.setName(de.getName(k));
            List<SpellAuraDispelledEvent> events = de.getEvents(de.getName(k));
            for (SpellAuraDispelledEvent ev : events) {
                SpellAuraDispelledEventXmlType disEvXml = new SpellAuraDispelledEventXmlType();
                ev.addXmlAttributesBean(disEvXml);
                auraDispelled.getDispelEvent().add(disEvXml);
            }
            disInfo.getAuraDispelled().add(auraDispelled);
        }
         return disInfo;
    }

    private NpcXmlType createNpc(Fight f) {
        NpcXmlType npc = new NpcXmlType();
        npc.setId(f.getMobID());
        npc.setGuid(f.getGuid());
        npc.setName(f.getName());
        npc.setMonth(f.getStartEvent().month);
        npc.setDay(f.getStartEvent().day);
        npc.setHour(f.getStartEvent().hour);
        npc.setMinute(f.getStartEvent().minute);
        npc.setSecond(f.getStartEvent().second);
        return npc;
    }

    private VictimXmlType createVictim() {
        VictimXmlType victm = new VictimXmlType();
        ParticipantXmlType pXml = createParticipant(fight.getVictim());
        victm.setParticipant(pXml);
        return victm;
    }

    private ParticipantXmlType createParticipant(FightParticipant p) {
        ParticipantJaxb pJaxb = new ParticipantJaxb(fight, p);
        return (ParticipantXmlType)pJaxb.makeParticipantObject(false);
    }

    private TableXmlType makeTable(TableModel tm) {
        int r, c;
        int numRows = tm.getRowCount();
        int numCols = tm.getColumnCount();
        TableXmlType xmlTable = new TableXmlType();
        TableRowXmlType header = new TableRowXmlType();
        xmlTable.setNumCols(numCols);
        xmlTable.setNumRows(numRows);
        for (c=0; c<numCols; c++) {
            header.getCol().add(tm.getColumnName(c));
        }
        xmlTable.setHeaderRow(header);
        for (r=0; r<numRows; r++) {
            TableRowXmlType tr = new TableRowXmlType();
            for (c=0; c<numCols; c++) {
                tr.getCol().add(tm.getValueAt(r, c).toString());
            }
            xmlTable.getBodyRow().add(tr);
        }
        return xmlTable;
    }
}
