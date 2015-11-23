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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.xml.bind.JAXBElement;
import wowlogparserbase.Fight;
import wowlogparserbase.FileLoader;
import wowlogparserbase.events.BasicEvent;
import wowlogparserbase.helpers.MathHelper;
import wowlogparserbase.xmlbindings.report.*;
import wowlogparserbase.xmlbindings.fight.*;

/**
 *
 * @author racy
 */
public class ReportJaxb {
    File dir;
    boolean zip = false;
    ZipOutputStream zOut;
    int index = 0;
    public ReportJaxb(File dir) {
        this.dir = dir;
    }

    public ReportJaxb(ZipOutputStream zOut) {
        this.zip = true;
        this.zOut = zOut;
    }

    public void setFileNumberIndex(int index) {
        this.index = index;
    }

    public int getFileNumberIndex() {
        return index;
    }

    /**
     * Make a boss fights XML group.
     * Already made boss fights must be passed into this method because of dependency issues.
     * @param fights The fights to write
     * @param name The name to be set in the index file
     * @param filename The filename to use for the index file
     */
    public void makeFights(FileLoader fl, List<Fight> fights, String name, String filename) throws FileNotFoundException, IOException {
        FightGroup fg = new FightGroup();
        fg.setName(name);
        OwnerXmlType owner = getOwner(fights);
        fg.setOwner(owner);
        for (Fight f : fights) {
            Fight fight = new Fight(f);
            fight.removeAssignedPets();
            String fightFilename = "" + index + "_" + (int) fight.getStartTime() + "_" + (int) fight.getDuration() + ".xml";
            FightReferenceXmlType fightRef = new FightReferenceXmlType();
            fightRef.setFilename(fightFilename);
            fightRef.setName(fight.getName());
            fightRef.setDuration(MathHelper.round2Decimals(fight.getActiveDuration()));
            fightRef.setYear(fl.getYear());
            fightRef.setMonth(fight.getStartEvent().month);
            fightRef.setDay(fight.getStartEvent().day);
            fightRef.setHour(fight.getStartEvent().hour);
            fightRef.setMinute(fight.getStartEvent().minute);
            fightRef.setSecond(fight.getStartEvent().second);
            if (fight.isMerged()) {
                fightRef.setNumMobs(fight.getMergedSourceFights().size());
            } else {
                fightRef.setNumMobs(1);
            }
            fightRef.setBossFight(fight.isBossFight());
            fg.getFightReference().add(fightRef);

            FightJaxb fightJaxb = new FightJaxb(fight);
            wowlogparserbase.xmlbindings.fight.Fight fightXml = fightJaxb.makeFightObject();
            if (zip) {
                writeZip(fightXml, zOut, fightFilename);
            } else {
                write(fightXml, fightFilename);
            }

            index++;
        }
        if (zip) {
            writeZip(fg, zOut, filename);
        } else {
            write(fg, filename);
        }
    }


    private OwnerXmlType getOwner(List<Fight> fights) {
        //Loop through events until a flag 0x511 is found which means its the person who made the log.
        String name = "";
        String guid = "";
        outerloop:
        for (Fight f : fights) {
            for (BasicEvent e : f.getEvents()) {
                if (e.getSourceFlags() == 0x511) {
                    name = e.getSourceName();
                    guid = e.getSourceGUID();
                    break outerloop;
                }
                if (e.getDestinationFlags() == 0x511) {
                    name = e.getDestinationName();
                    guid = e.getDestinationGUID();
                    break outerloop;
                }
            }
        }

        OwnerXmlType owner = new OwnerXmlType();
        owner.setGuid(guid);
        owner.setName(name);
        return owner;
    }

    /**
     * Make a boss fights XML group.
     * Already made boss fights must be passed into this method because of dependency issues.
     * @param bossFights Already made boss fights.
     */
    public void makeBossFights(FileLoader fl, List<Fight> bossFights) throws FileNotFoundException, IOException {
        makeFights(fl, bossFights, "Boss Fights", "BossFightsIndex.xml");
    }

    /**
     * Make individually merged fights XML group.
     * @param fights The already merged fights.
     */
    public void makeIndividuallyMergedFights(FileLoader fl, List<Fight> fights) throws FileNotFoundException, IOException {
        makeFights(fl, fights, "Individual mob types", "IndividuallyMergedFightsIndex.xml");
    }

    public void makeIndividualFights(FileLoader fl, List<Fight> fights) throws FileNotFoundException, IOException {
        makeFights(fl, fights, "Individual mobs", "IndividualFightsIndex.xml");
    }

    /**
     * Make a single fight XML group
     * @param fight The fight
     */
    public void makeFight(FileLoader fl, Fight fight) throws FileNotFoundException, IOException {
        List<Fight> fights = new ArrayList<Fight>();
        fights.add(fight);
        if (fight.isMerged()) {
            makeFights(fl, fights, "Single fight (merged)", "SingleFightIndex.xml");
        } else {
            makeFights(fl, fights, "Single fight", "SingleFightIndex.xml");
        }
    }

    public void write(Object doc, String filename) throws IOException {
        File file = new File(dir, filename);
        FileOutputStream fOut = new FileOutputStream(file);
        try {
            javax.xml.bind.JAXBContext jaxbCtx = javax.xml.bind.JAXBContext.newInstance(doc.getClass().getPackage().getName());
            javax.xml.bind.Marshaller marshaller = jaxbCtx.createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_ENCODING, "UTF-8"); //NOI18N
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
            marshaller.marshal(doc, fOut);
        } catch (javax.xml.bind.JAXBException ex) {
            // XXXTODO Handle exception
            java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE, null, ex); //NOI18N
        }
        fOut.close();
    }

    public void writeZip(Object doc, ZipOutputStream zOut, String filename) throws IOException {
        ZipEntry ze = new ZipEntry(filename);
        zOut.putNextEntry(ze);
        try {
            javax.xml.bind.JAXBContext jaxbCtx = javax.xml.bind.JAXBContext.newInstance(doc.getClass().getPackage().getName());
            javax.xml.bind.Marshaller marshaller = jaxbCtx.createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_ENCODING, "UTF-8"); //NOI18N
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
            marshaller.marshal(doc, zOut);
        } catch (javax.xml.bind.JAXBException ex) {
            // XXXTODO Handle exception
            java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE, null, ex); //NOI18N
        }
        zOut.closeEntry();
    }

    public static ZipOutputStream makeZipOutputStream(File file) throws FileNotFoundException {
        OutputStream out = new FileOutputStream(file);
        ZipOutputStream zOut = new ZipOutputStream(out);
        return zOut;
    }

}
