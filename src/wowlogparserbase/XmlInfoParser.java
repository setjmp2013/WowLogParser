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

package wowlogparserbase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import wowlogparserbase.helpers.XmlHelper;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Singleton class used to parse the configuration XML file.
 * @author racy
 */
public class XmlInfoParser implements BossParserSource {

    private static final String xmlFileEN = "bossinfo/ClassParsing.xml";
    private static final String xmlFileDE = "bossinfo/ClassParsingDE.xml";
    private static final String xmlFileFR = "bossinfo/ClassParsingFR.xml";
    private static final String xmlFileES = "bossinfo/ClassParsingES.xml";
    private static String xmlFile = xmlFileEN;
    private static XmlInfoParser theInstance = null;

    protected Document doc = null;
    protected Node rootNode = null;
    
    protected XmlInfoParser() {
        try {
            doc = XmlHelper.openXml(new FileInputStream(new File(xmlFile)));
            rootNode = doc.getDocumentElement();
        } catch (FileNotFoundException fileNotFoundException) {
            rootNode = null;
            doc = null;
            throw new RuntimeException("Could not open the Class Parsing/Boss List xml file.");
        }
        //rootNode = XmlHelper.openXml(getClass().getClassLoader().getResourceAsStream(xmlFile));
    }
    
    /**
     * Set english language
     */
    public static void setLanguageEN() {
        theInstance = null;
        xmlFile = xmlFileEN;
    }
    
    /**
     * Set german language
     */
    public static void setLanguageDE() {
        theInstance = null;
        xmlFile = xmlFileDE;
    }

    /**
     * Set french language.
     */
    public static void setLanguageFR() {
        theInstance = null;
        xmlFile = xmlFileFR;
    }

    /**
     * Set french language.
     */
    public static void setLanguageES() {
        theInstance = null;
        xmlFile = xmlFileES;
    }

    /**
     * Get the instance.
     * @return An XmlInfoParser instance.
     */
    public static XmlInfoParser getInstance() {
        if (theInstance == null) {
            theInstance = new XmlInfoParser();
        }
        return theInstance;
    }
        
    /**
     * Get localized spell name from the XML file
     * @param nameEn The english spell name
     * @return Empty string if not found, otherwise the localized spell name.
     */
    String getSpellName(String nameEn) {
        List<Node> spellNodes = XmlHelper.getChildNodes(rootNode, "spell");
        for (Node n : spellNodes) {
            if (n!=null) {
                NamedNodeMap attrs = n.getAttributes();
                if (attrs != null) {
                    Node nameEnNode = attrs.getNamedItem("nameEn");
                    Node nameNode = attrs.getNamedItem("name");
                    if (nameEnNode != null && nameNode != null) {
                        String s = nameEnNode.getTextContent();
                        if (s.equalsIgnoreCase(nameEn)) {
                            return nameNode.getTextContent();
                        }
                    }
                }
            }
        }
        return "";
    }
    
    /**
     * Get information about all boss encounters.
     * @return A list with one BossInfo object for each encounter
     */
    @Override
    public List<BossInfo> getBossInfo() {
        List<BossInfo> bossInfoList = new ArrayList<BossInfo>();
        List<Node> bossNodes = XmlHelper.getChildNodes(rootNode, "boss");
        for (Node bossNode : bossNodes) {
            if (bossNode != null) {
                NamedNodeMap attrs = bossNode.getAttributes();
                Node encounterNode = attrs.getNamedItem("encounter");
                Node instanceNode = attrs.getNamedItem("instance");
                Node maxIdleTimeNode = attrs.getNamedItem("maxidletime");
                BossInfo bossInfo = new BossInfo(encounterNode.getTextContent());
                if (maxIdleTimeNode != null) {
                    try {
                        bossInfo.setMaxIdleTime(Double.parseDouble(maxIdleTimeNode.getTextContent()));
                    } catch(NumberFormatException ex) {
                    }
                }
                List<Node> mobNodes = XmlHelper.getChildNodes(bossNode, "mob");
                for (Node mobNode : mobNodes) {
                    attrs = mobNode.getAttributes();
                    Node nameNode = attrs.getNamedItem("name");
                    Node idNode = attrs.getNamedItem("id");
                    Node isBossNode = attrs.getNamedItem("boss");
                    String name = "Unknown57523";
                    int id = NpcInfo.UNKNOWN_ID;
                    boolean isBoss = false;
                    if (nameNode != null) {
                        name = nameNode.getTextContent();
                    }
                    if (idNode != null) {
                        try {
                            id = Integer.parseInt(idNode.getTextContent());
                        } catch(NumberFormatException ex) {                            
                        }
                    }
                    if (isBossNode != null) {
                        String val = isBossNode.getTextContent();
                        if ("true".equalsIgnoreCase(val)) {
                            isBoss = true;
                        }
                    }
                    bossInfo.addMob(new NpcInfo(name, id, isBoss));
                }
                bossInfoList.add(bossInfo);
            }
        }
        return bossInfoList;
    }
    
    /**
     * Get the names of all mobs in all encounters.
     * @return A list with all mob names.
     */
    @Override
    public List<String> getMobNames() {
        List<BossInfo> bossInfoList = getBossInfo();
        ArrayList<String> bossStrings = new ArrayList<String>();
        for(BossInfo bossInfo : bossInfoList) {
            List<String> mobList = new ArrayList<String>();
            for (NpcInfo mob : bossInfo.getMobs()) {
                mobList.add(mob.getName());
            }
            bossStrings.addAll(mobList);
        }
        return bossStrings;
    }
    
    /**
     * Get mob IDs for all mobs in all encounters.
     * If the IDs aren't set in the XML file then they will have the value NpcInfo.UNKNOWN_ID
     * @return
     */
    @Override
    public List<Integer> getMobIDs() {
        List<BossInfo> bossInfoList = getBossInfo();
        ArrayList<Integer> bossIDs = new ArrayList<Integer>();
        for(BossInfo bossInfo : bossInfoList) {
            List<Integer> mobList = new ArrayList<Integer>();
            for (NpcInfo mob : bossInfo.getMobs()) {
                mobList.add(mob.getId());
            }
            bossIDs.addAll(mobList);
        }
        return bossIDs;
    }

    /**
     * Get the XML file name.
     * @return The filename.
     */
    public String getXmlFile() {
        return xmlFile;
    }

    /**
     * Get a player class name from the english version.
     * @param classNameEn The english class name.
     * @return The localized class name.
     */
    public String xmlGetClassName(String classNameEn) {
        Node baseNode = rootNode;
        List<Node> nodes = XmlHelper.getChildNodes(baseNode, "class");
        for(Node n : nodes) {
            NamedNodeMap attrs = n.getAttributes();
            Node attrNode = attrs.getNamedItem("name");
            Node attrNode2 = attrs.getNamedItem("nameEn");
            if (attrNode != null && attrNode2 != null) {
                String name = attrNode.getTextContent();
                String nameEn = attrNode2.getTextContent();
                if (nameEn.equalsIgnoreCase(classNameEn)) {
                    return name;
                }
            }
        }
        return "Unknown";
    }

    /**
     * Get spell name strings that identify a certain player class.
     * @param classNameEn The english player class name.
     * @return An array with spell names.
     */
    public String[] xmlGetClassStr(String classNameEn) {
        Node baseNode = rootNode;
        List<Node> nodes = XmlHelper.getChildNodes(baseNode, "class");
        for(Node n : nodes) {
            NamedNodeMap attrs = n.getAttributes();
            Node attrNode = attrs.getNamedItem("name");
            Node attrNode2 = attrs.getNamedItem("nameEn");
            if (attrNode != null && attrNode2 != null) {
                String name = attrNode.getTextContent();
                String nameEn = attrNode2.getTextContent();
                if (nameEn.equalsIgnoreCase(classNameEn)) {
                    Node strNode = XmlHelper.getChildNode(n, "spellstrings");
                    String content = strNode.getTextContent();
                    List<String> splitStr = XmlHelper.splitCommaFnuttStr(content);                    
                    return splitStr.toArray(new String[0]);
                }
            }
        }
        return new String[0];
    }
    
    /**
     * Get spell name strings that identify a certain player class, in lower case.
     * @param classNameEn The english player class name.
     * @return An array with spell names in lower case.
     */
    public String[] xmlGetClassStrLower(String classNameEn) {
        Node baseNode = rootNode;
        List<Node> nodes = XmlHelper.getChildNodes(baseNode, "class");
        for(Node n : nodes) {
            NamedNodeMap attrs = n.getAttributes();
            Node attrNode = attrs.getNamedItem("name");
            Node attrNode2 = attrs.getNamedItem("nameEn");
            if (attrNode != null && attrNode2 != null) {
                String name = attrNode.getTextContent();
                String nameEn = attrNode2.getTextContent();
                if (nameEn.equalsIgnoreCase(classNameEn)) {
                    Node strNode = XmlHelper.getChildNode(n, "spellstrings");
                    String content = strNode.getTextContent();
                    List<String> splitStr = XmlHelper.splitCommaFnuttStr(content);
                    for (int k=0; k<splitStr.size(); k++) {
                        String s = splitStr.get(k);
                        splitStr.set(k, s.toLowerCase());
                    }
                    return splitStr.toArray(new String[0]);
                }
            }
        }
        return new String[0];
    }

    /**
     * Get spell IDs that identify a player class.
     * @param classNameEn The english player class.
     * @return An array with spell ids.
     */
    public int[] xmlGetClassID(String classNameEn) {
        Node baseNode = rootNode;
        List<Node> nodes = XmlHelper.getChildNodes(baseNode, "class");
        for(Node n : nodes) {
            NamedNodeMap attrs = n.getAttributes();
            Node attrNode = attrs.getNamedItem("name");
            Node attrNode2 = attrs.getNamedItem("nameEn");
            if (attrNode != null && attrNode2 != null) {
                String name = attrNode.getTextContent();
                String nameEn = attrNode2.getTextContent();
                if (nameEn.equalsIgnoreCase(classNameEn)) {
                    Node strNode = XmlHelper.getChildNode(n, "spellids");
                    String content = strNode.getTextContent();
                    List<Integer> splitInt = XmlHelper.splitCommaInt(content);                    
                    int[] out = new int[splitInt.size()];
                    for (int k=0; k<splitInt.size(); k++) {
                        out[k] = splitInt.get(k);
                    }
                    return out;
                }
            }
        }
        return new int[0];
    }

    public void addEncounter(String name) {
        List<Node> bossNodes = XmlHelper.getChildNodes(rootNode, "boss");
        boolean exists = false;
        for (Node bossNode : bossNodes) {
            if (bossNode != null) {
                NamedNodeMap attrs = bossNode.getAttributes();
                Node encounterNode = attrs.getNamedItem("encounter");
                if (encounterNode != null) {
                    if (encounterNode.getTextContent().trim().toLowerCase().equals(name.trim().toLowerCase())) {
                        exists = true;
                        break;
                    }
                }
            }
        }
        if (!exists) {
            Element newBoss = doc.createElement("boss");
            newBoss.setAttribute("encounter", name);
            newBoss.setAttribute("instance", "");
            rootNode.appendChild(newBoss);
        }
    }

    public void addModifyNpc(String encounterName, NpcInfo npcInfo) {
        Node bossNode = XmlHelper.getChildNodeWithAttribute(rootNode, "boss", "encounter", encounterName);
        if (bossNode == null) {
            addEncounter(encounterName);
            bossNode = XmlHelper.getChildNodeWithAttribute(rootNode, "boss", "encounter", encounterName);
        }
        if (bossNode != null) {
            Element mobEl = doc.createElement("mob");
            mobEl.setAttribute("name", npcInfo.getName());
            mobEl.setAttribute("id", Integer.toString(npcInfo.getId()));
            mobEl.setAttribute("boss", Boolean.toString(npcInfo.isBossMob()));
            bossNode.appendChild(mobEl);
        }
    }

    public NpcInfo getNpcInfo(String encounterName, int id) {
        NpcInfo npcInfo = null;
        Node bossNode = XmlHelper.getChildNodeWithAttribute(rootNode, "boss", "encounter", encounterName);
        if (bossNode != null) {
            Node mobNode = XmlHelper.getChildNodeWithAttribute(bossNode, "mob", "id", Integer.toString(id));
            if (mobNode != null) {
                if (mobNode instanceof Element) {
                    Element mobEl = (Element) mobNode;
                    String mobName = mobEl.getAttribute("name");
                    Integer mobId = null;
                    Boolean mobIsBoss = false;
                    try {
                        mobId = Integer.parseInt(mobEl.getAttribute("id"));
                    } catch(NumberFormatException ex) {                        
                    }
                    try {
                        mobIsBoss = Boolean.parseBoolean(mobEl.getAttribute("boss"));
                    } catch(NumberFormatException ex) {
                    }
                    if (mobId != null) {
                        if (mobId == id) {
                            npcInfo = new NpcInfo(mobName, mobId, mobIsBoss);
                        }
                    }
                }
            }
        }
        return npcInfo;
    }

    public void saveFile() {
        XmlHelper.writeXmlFile(rootNode, new File(xmlFile));
    }
}


