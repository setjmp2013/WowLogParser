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
import java.util.ArrayList;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import wowlogparserbase.Fight;
import wowlogparserbase.events.BasicEvent;
import wowlogparserbase.events.LogEvent;
import wowlogparserbase.helpers.XmlHelper;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author racy
 */
public class XMLFightGroup {

    Document doc;
    List<Fight> fightGroup;
    String dir;

    String name;
    List<XMLFightReference> fightReferences = new ArrayList<XMLFightReference>();
    int index = 0;

    public static final String nameTag="name";
    public static final String guidTag="guid";

    public XMLFightGroup(Document doc, List<Fight> fightGroup, String name, String dir) {
        this.doc = doc;
        this.fightGroup = fightGroup;
        this.name = name;
        this.dir = dir;
    }

    public void setFileNumberIndex(int index) {
        this.index = index;
    }
    
    public int getFileNumberIndex() {
        return index;
    }
            
    public XMLFightGroup() {
    }
    
    void addOwner(Node root) {
        //Loop through events until a flag 0x511 is found which means its the person who made the log.
        String name = null;
        String guid = null;
        outerloop:
        for (Fight f : fightGroup) {
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
        
        if (name != null && guid != null) {
            Element ownerNode = doc.createElement("Owner");
            ownerNode.setAttribute(nameTag, name);
            ownerNode.setAttribute(guidTag, guid);
            root.appendChild(ownerNode);
        }
    }
    
    /**
     * Make one XML file for each fight. Also make a new Node with pointers ot those files.
     * @return A node with references to the created XML files.
     */
    public Node makeFightFiles() {
        Element fightGroupNode = doc.createElement("FightGroup");
        fightGroupNode.setAttribute(nameTag, name);        
        addOwner(fightGroupNode);

        ArrayList<String> fileNames = new ArrayList<String>();
        for (Fight fight : fightGroup) {
            Fight fight2 = new Fight(fight);
            fight2.removeAssignedPets();
            XMLFight f = new XMLFight(fight2);
            String filename = "" + index + "_" + (int)f.getFight().getStartTime() + "_" + (int)f.getFight().getDuration() + ".xml";
            index++;
            fileNames.add(filename);
            File file = new File(dir, filename);
            Document fightDoc =  f.makeDocument();
            writeFight(fightDoc, file);
            
            XMLFightReference fightRef = new XMLFightReference(doc, f.getFight(), filename);
            fightGroupNode.appendChild(fightRef.makeNode());
        }
        return fightGroupNode;
    }
    
    /**
     * Make one XML file for each fight. Also make a new Node with pointers ot those files.
     * @return A node with references to the created XML files.
     */
    public Node makeFightFilesZip(ZipOutputStream zOut) throws IOException {
        Element fightGroupNode = doc.createElement("FightGroup");
        fightGroupNode.setAttribute("name", name);        
        addOwner(fightGroupNode);

        ArrayList<String> fileNames = new ArrayList<String>();
        for (Fight fight : fightGroup) {
            Fight fight2 = new Fight(fight);
            fight2.removeAssignedPets();
            XMLFight f = new XMLFight(fight2);
            String filename = "" + index + "_" + (int)f.getFight().getStartTime() + "_" + (int)f.getFight().getDuration() + ".xml";
            index++;
            fileNames.add(filename);
            Document fightDoc =  f.makeDocument();
            writeFightZip(fightDoc, filename, zOut);
            
            XMLFightReference fightRef = new XMLFightReference(doc, f.getFight(), filename);
            fightGroupNode.appendChild(fightRef.makeNode());
        }
        return fightGroupNode;
    }

    public void parseXML(Node rootNode) {
        if (rootNode != null) {
            if (rootNode.getNodeName().equalsIgnoreCase("FightGroup")) {
                NamedNodeMap attrs = rootNode.getAttributes();
                if (attrs != null) {
                    Node nameNode = attrs.getNamedItem("name");
                    if (nameNode != null) {
                        name = nameNode.getTextContent().trim();
                    }
                }
                List<Node> fightRefNodes = XmlHelper.getChildNodes(rootNode, "FightReference");
                fightReferences = new ArrayList<XMLFightReference>();
                for (Node n : fightRefNodes) {
                    if (n != null) {
                        XMLFightReference ref = new XMLFightReference();
                        ref.parseXML(n);
                        fightReferences.add(ref);
                    }
                }
            }
        }
    }

    public List<XMLFightReference> getFightReferences() {
        return fightReferences;
    }

    public String getName() {
        return name;
    }

    public void writeFight(Document doc, File file) {
        ProcessingInstruction pi = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"wlpfight.xsl\"");
        doc.insertBefore(pi, doc.getDocumentElement());
        XmlHelper.writeXmlFile(doc, file);
    }
    
    public void writeFightZip(Document doc, String filename, ZipOutputStream zOut) throws IOException {
        ProcessingInstruction pi = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"wlpfight.xsl\"");
        doc.insertBefore(pi, doc.getDocumentElement());
        ZipEntry zipEntry = new ZipEntry(filename);
        zOut.putNextEntry(zipEntry);
        XmlHelper.writeXmlFile(doc, zOut);
        zOut.closeEntry();
    }
}
