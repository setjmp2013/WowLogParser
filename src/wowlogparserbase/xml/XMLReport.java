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
import java.io.File;
import java.util.ArrayList;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import wowlogparserbase.helpers.XmlHelper;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.w3c.dom.ProcessingInstruction;

/**
 *
 * @author racy
 */
public class XMLReport {
    File dir;
    boolean zip = false;
    ZipOutputStream zOut;
    int index = 0;
    public XMLReport(File dir) {
        this.dir = dir;
    }

    public XMLReport(ZipOutputStream zOut) {
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
    public void makeFights(List<Fight> fights, String name, String filename) throws FileNotFoundException, IOException {
        Document doc = XmlHelper.createDocument(null);
        Node fightGroupNode;
        if (zip) {
            XMLFightGroup xmlFightGroup = new XMLFightGroup(doc, fights, name, "");
            xmlFightGroup.setFileNumberIndex(index);
            fightGroupNode = xmlFightGroup.makeFightFilesZip(zOut);
            setFileNumberIndex(xmlFightGroup.getFileNumberIndex());
            doc.appendChild(fightGroupNode);
            writeZip(doc, zOut, filename);
        } else {
            XMLFightGroup xmlFightGroup = new XMLFightGroup(doc, fights, name, dir.getAbsolutePath());
            xmlFightGroup.setFileNumberIndex(index);
            fightGroupNode = xmlFightGroup.makeFightFiles();
            setFileNumberIndex(xmlFightGroup.getFileNumberIndex());
            doc.appendChild(fightGroupNode);
            write(doc, filename);                
        }
    }
    
    /**
     * Make a boss fights XML group. 
     * Already made boss fights must be passed into this method because of dependency issues.
     * @param bossFights Already made boss fights.
     */
    public void makeBossFights(List<Fight> bossFights) throws FileNotFoundException, IOException {
        makeFights(bossFights, "Boss Fights", "BossFightsIndex.xml");
    }

    /**
     * Make individually merged fights XML group.
     * @param fights The already merged fights.
     */
    public void makeIndividuallyMergedFights(List<Fight> fights) throws FileNotFoundException, IOException {        
        makeFights(fights, "Individual mob types", "IndividuallyMergedFightsIndex.xml");
    }

    public void makeIndividualFights(List<Fight> fights) throws FileNotFoundException, IOException {
        makeFights(fights, "Individual mobs", "IndividualFightsIndex.xml");
    }
    
    /**
     * Make a single fight XML group
     * @param fight The fight
     */
    public void makeFight(Fight fight) throws FileNotFoundException, IOException {        
        ArrayList<Fight> fights = new ArrayList<Fight>();
        fights.add(fight);
        if (fight.isMerged()) {
            makeFights(fights, "Single fight (merged)", "SingleFightIndex.xml");
        } else {
            makeFights(fights, "Single fight", "SingleFightIndex.xml");
        }
    }
    
    public void write(Document doc, String filename) {
        ProcessingInstruction pi = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"wlpreport.xsl\"");
        doc.insertBefore(pi, doc.getDocumentElement());
        File file = new File(dir, filename);
        XmlHelper.writeXmlFile(doc, file);
    }
    
    public void writeZip(Document doc, ZipOutputStream zOut, String filename) throws IOException {
        ProcessingInstruction pi = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"wlpreport.xsl\"");
        doc.insertBefore(pi, doc.getDocumentElement());
        ZipEntry ze = new ZipEntry(filename);
        zOut.putNextEntry(ze);
        XmlHelper.writeXmlFile(doc, zOut);
        zOut.closeEntry();
    }

    public static ZipOutputStream makeZipOutputStream(File file) throws FileNotFoundException {
        OutputStream out = new FileOutputStream(file);
        ZipOutputStream zOut = new ZipOutputStream(out);
        return zOut;
    }
}
