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

package wowlogparserbase.helpers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author racy
 */
public class XmlHelper {
    public static Document openXml(InputStream xmlStream) {
        //BufferedReader xmlReader = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(xmlName)));
        try {
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = db.parse(xmlStream);
            return doc;
        } catch (IOException ex) {
            return null;
        } catch (ParserConfigurationException ex) {
            return null;
        } catch (SAXException ex) {
            return null;
        }
    }

    public static List<Node> getChildNodes(Node baseNode, String name) {
        ArrayList<Node> retNodes = new ArrayList<Node>();
        if (baseNode.hasChildNodes()) {
            NodeList nList = baseNode.getChildNodes();
            for (int k = 0; k < nList.getLength(); k++) {
                Node n = nList.item(k);
                if (n.getNodeName().equalsIgnoreCase(name)) {
                    retNodes.add(n);
                }
            }
        }
        return retNodes;
    }

    public static Node getChildNodeWithAttribute(Node baseNode, String name, String attributeName, String attributeContent) {
        if (baseNode.hasChildNodes()) {
            NodeList nList = baseNode.getChildNodes();
            for (int k = 0; k < nList.getLength(); k++) {
                Node n = nList.item(k);
                if (n instanceof Element) {
                    Element el = (Element) n;
                    if (el.getAttribute(attributeName).equals(attributeContent)) {
                        return n;
                    }
                }
            }
        }
        return null;
    }

    public static Node getChildNode(Node baseNode, String name) {
        List<Node> nodes = getChildNodes(baseNode, name);
        if (nodes.size() > 0) {
            return nodes.get(0);
        } else {
            return null;
        }
    }

    public static List<Integer> splitCommaInt(String inS) {
        String[] split = inS.split(",");
        List<Integer> outInts = new ArrayList<Integer>();
        for (String s : split) {
            try {
                Integer i = Integer.parseInt(s.trim());
                outInts.add(i);
            } catch (NumberFormatException ex) {
                continue;
            }
        }
        return outInts;
    }

    public static List<String> splitCommaFnuttStr(String inS) {
        inS = inS.trim();
        if (!inS.contains("\"")) {
            return new ArrayList<String>();
        }
        boolean fnuttStarted = false;
        ArrayList<Integer> breakPos = new ArrayList<Integer>();
        ArrayList<String> outStrings = new ArrayList<String>();
        for (int k = 0; k < inS.length(); k++) {
            if (fnuttStarted) {
                if (inS.charAt(k) == '\"') {
                    fnuttStarted = false;
                    continue;
                }
            } else {
                if (inS.charAt(k) == '\"') {
                    fnuttStarted = true;
                    continue;
                }
                if (inS.charAt(k) == ',') {
                    breakPos.add(k);
                    continue;
                }
            }
        }
        int startPos = 0;
        int endPos = 0;
        for (int k = 0; k < breakPos.size(); k++) {
            endPos = breakPos.get(k);
            String s = inS.substring(startPos, endPos);
            s = s.trim();
            s = s.replace("\"", "");
            outStrings.add(s);
            startPos = endPos + 1;
        }
        endPos = inS.length();
        String s = inS.substring(startPos, endPos);
        s = s.trim();
        s = s.replace("\"", "");
        outStrings.add(s);
        startPos = endPos + 1;

        return outStrings;
    }

    public static void writeXmlFile(Node doc, File file) {
        try {
            // Prepare the DOM document for writing
            Source source = new DOMSource(doc);

            // Prepare the output file
            Result result = new StreamResult(file);

            // Write the DOM document to the file
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.transform(source, result);
        } catch (TransformerConfigurationException e) {
        } catch (TransformerException e) {
        }
    }
    
    public static void writeXmlFile(Node doc, OutputStream stream) {
        try {
            // Prepare the DOM document for writing
            Source source = new DOMSource(doc);

            // Prepare the output file
            Result result = new StreamResult(stream);

            // Write the DOM document to the file
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.transform(source, result);
        } catch (TransformerConfigurationException e) {
        } catch (TransformerException e) {
        }
    }

    public static Document createDocument(String rootName) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            DOMImplementation impl = builder.getDOMImplementation();
            Document doc = impl.createDocument(null, rootName, null);
            return doc;
        } catch(ParserConfigurationException ex) {
            
        }
        return null;            
    }
}
