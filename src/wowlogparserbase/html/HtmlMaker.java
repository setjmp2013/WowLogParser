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

package wowlogparserbase.html;

import wowlogparserbase.helpers.FileHelper;
import wowlogparserbase.Fight;
import wowlogparserbase.IntCallback;
import wowlogparserbase.FightParticipant;
import wowlogparserbase.StringCallback;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.table.TableModel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.DefaultXYDataset;
import wowlogparserbase.Constants;
import wowlogparserbase.SettingsSingleton;
import wowlogparserbase.WlpPlotFactory;
import wowlogparserbase.tablemodels.*;
import wowlogparserbase.tablerendering.StringColor;

public class HtmlMaker {

    private static final String defaultMainCssFilename = "wlp_global.css";
    private static final String[] cssFilenames = {
        "css/" + defaultMainCssFilename};
    private static final String[] jsFilenames = {
        "js/dhtmlSuite-common.js-uncompressed.js",
        "js/dhtmlSuite-dynamicContent.js-uncompressed.js",
        "js/dhtmlSuite-tabView.js-uncompressed.js"};

    private static final String[] cssResourceFiles = {
        "wowlogparserbase/extrafiles/html/tab-view.css",
        "wowlogparserbase/extrafiles/html/" + defaultMainCssFilename};
    private static final String[] jsResourceFiles = {
        "wowlogparserbase/extrafiles/html/dhtmlSuite-common.js-uncompressed.js",
        "wowlogparserbase/extrafiles/html/dhtmlSuite-tabView.js-uncompressed.js",
        "wowlogparserbase/extrafiles/html/dhtmlSuite-dynamicContent.js-uncompressed.js"};
    private static final String[] imageResourceFiles = {};
    private static final String[] imageTabViewResourceFiles = {
        "wowlogparserbase/extrafiles/html/images/tab-view-close.png",
        "wowlogparserbase/extrafiles/html/images/tab-view-close-over.png",
        "wowlogparserbase/extrafiles/html/images/tab_left_active.png",
        "wowlogparserbase/extrafiles/html/images/tab_left_inactive.png",
        "wowlogparserbase/extrafiles/html/images/tab_left_over.png",
        "wowlogparserbase/extrafiles/html/images/tab_right_active.png",
        "wowlogparserbase/extrafiles/html/images/tab_right_inactive.png",
        "wowlogparserbase/extrafiles/html/images/tab_right_over.png"};
    private static final String tableClassDamage = "MYTABLE";
    private static final String tableClassHealing = "MYTABLEH";
    private static final String tableClassPower = "MYTABLEP";
    private static final String tableClassAbility = "MYTABLEA";
    private static final String tableClassWhoHealsWhom = "MYTABLEWHW";
    private Map<Integer, String> tableClassForClasses;
    private Map<Integer, String> linkClassForClasses;

    // List of fights with assigned pets still there.
    private List<Fight> list;
    private String version;
    private boolean mergeAndIndividual = false;

    private String headNote;
    private boolean makeRaidGraph = true;
    private boolean makeRaidGraphMulti = true;
    private boolean makePlayerGraph = true;
    private int chartHeight = 400;
    private int chartWidth = 600;
    //private String cssNameClass = "name";
    private String cssTitleClass = "title";
    private boolean customCss = false;
    private String customCssFilename = "";

    /**
     * Constructor
     * @param owner
     * @param list List of fights with assigned pets still there.
     * @param basePrefs
     * @param merge
     */
    public HtmlMaker(JFrame owner, String version) {
        this.version = version;
        init();
    }

    public void makeIndividual(List<Fight> fights, File dir, IntCallback cb, StringCallback cbStr) throws IOException {
        fixList(fights, false, cb, cbStr);
        makeHtml(dir, list, false, cb, cbStr);        
    }

    public void makeMerge(List<Fight> fights, File dir, IntCallback cb, StringCallback cbStr) throws IOException {
        fixList(fights, true, cb, cbStr);
        makeHtml(dir, list, true, cb, cbStr);
    }

    private void init() {
        tableClassForClasses = new HashMap<Integer, String>();
        tableClassForClasses.put(Constants.CLASS_PRIEST, "MYTABLEPRI");
        tableClassForClasses.put(Constants.CLASS_DEATHKNIGHT, "MYTABLEDKN");
        tableClassForClasses.put(Constants.CLASS_DRUID, "MYTABLEDRD");
        tableClassForClasses.put(Constants.CLASS_HUNTER, "MYTABLEHUN");
        tableClassForClasses.put(Constants.CLASS_MAGE, "MYTABLEMAG");
        tableClassForClasses.put(Constants.CLASS_PALADIN, "MYTABLEPAL");
        tableClassForClasses.put(Constants.CLASS_ROGUE, "MYTABLEROG");
        tableClassForClasses.put(Constants.CLASS_SHAMAN, "MYTABLESMN");
        tableClassForClasses.put(Constants.CLASS_WARLOCK, "MYTABLEWRL");
        tableClassForClasses.put(Constants.CLASS_WARRIOR, "MYTABLEWAR");
        tableClassForClasses.put(Constants.CLASS_UNKNOWN, "MYTABLEUNK");

        linkClassForClasses = new HashMap<Integer, String>();
        linkClassForClasses.put(Constants.CLASS_PRIEST, "pri");
        linkClassForClasses.put(Constants.CLASS_DEATHKNIGHT, "dkn");
        linkClassForClasses.put(Constants.CLASS_DRUID, "drd");
        linkClassForClasses.put(Constants.CLASS_HUNTER, "hun");
        linkClassForClasses.put(Constants.CLASS_MAGE, "mag");
        linkClassForClasses.put(Constants.CLASS_PALADIN, "pal");
        linkClassForClasses.put(Constants.CLASS_ROGUE, "rog");
        linkClassForClasses.put(Constants.CLASS_SHAMAN, "smn");
        linkClassForClasses.put(Constants.CLASS_WARLOCK, "wrl");
        linkClassForClasses.put(Constants.CLASS_WARRIOR, "war");
        linkClassForClasses.put(Constants.CLASS_UNKNOWN, "unk");
    }

    private void fixList(List<Fight> fights, boolean merge, IntCallback cb, StringCallback cbStr) {
        if (merge) {
            ArrayList<Fight> mergedFight = new ArrayList<Fight>();
            mergedFight.add(Fight.merge(fights, cb, cbStr));
            list = mergedFight;
        } else {
            list = fights;
        }
    }

    private void writeDocType(BufferedWriter w) throws IOException {
        w.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">");
        w.newLine();
    }

    private void writeHeader(BufferedWriter w) throws IOException {
        w.write("<H1>" + makeLinkClosed("http://www.gurre.eu/wowlogparser/forum", "Wow Log Parser report (v" + version + ")", null) + "</H1>");
        //w.write("<H1>" + makeLinkClosed("http://www.gurre.eu/wowlogparser/forum", "Wow Log Parser", null) + " report (v" + BaseFrame.version + ")" + "</H1>");
        w.newLine();
    }

    private void writeCssImports(BufferedWriter w) throws IOException {
        for (String name : cssFilenames) {
            w.write("<link rel=\"STYLESHEET\" type=\"text/css\" href=\"" + name + "\"/>");
            w.newLine();
        }
    }

    private void writeJsImports(BufferedWriter w) throws IOException {
        for (String name : jsFilenames) {
            w.write("<script type=\"text/javascript\" src=\"" + name + "\">");
            w.write("</script>");
            w.newLine();
        }
    }

    private void writeMakeTab(BufferedWriter w, String divName, String[] menuTitles) throws IOException {
        w.write("<script type=\"text/javascript\">");
        w.newLine();
        w.write("DHTMLSuite.createStandardObjects();");
        w.newLine();
        w.write("DHTMLSuite.configObj.setImagePath('images/');");
        w.newLine();
        w.write("DHTMLSuite.configObj.setCssPath('css/');");
        w.newLine();

        String varName = "tabViewObj" + divName;
        w.write("var "); w.write(varName); w.write(" = new DHTMLSuite.tabView();");
        w.write(varName); w.write(".setParentId('");
        w.write(divName); w.write("');");
        w.write(varName); w.write(".setTabTitles(Array(");
        for (int k = 0; k < menuTitles.length; k++) {
            w.write("'");
            w.write(menuTitles[k]);
            w.write("'");
            if (k < menuTitles.length - 1) {
                w.write(",");
            }
        }
        w.write("));");
        //w.write(varName);w.write(".setCloseButtons(Array(false,true,true,true));");
        w.write(varName); w.write(".setIndexActiveTab(0);");
        w.write(varName); w.write(".setWidth('100%');");
        w.write(varName); w.write(".setHeight('100%');");
        w.write(varName); w.write(".init();");
        w.newLine();

        w.write("</script>");
        w.newLine();
    }

    /**
     * Sets wether both merged and individual fights will be made. If this is set to true then
     * any calls to makeIndividual and makeMerged will write into "dir/individual" and "dir/merged"
     * instead of just "dir".
     * @param mergeAndIndividual
     */
    public void setMergeAndIndividual(boolean mergeAndIndividual) {
        this.mergeAndIndividual = mergeAndIndividual;
    }

    public void setChartHeight(int chartHeight) {
        this.chartHeight = chartHeight;
    }

    public void setChartWidth(int chartWidth) {
        this.chartWidth = chartWidth;
    }

    public void setCustomCss(boolean customCss) {
        this.customCss = customCss;
    }

    public void setCustomCssFilename(String customCssFilename) {
        this.customCssFilename = customCssFilename;
    }

    public void setHeadNote(String headNote) {
        this.headNote = headNote;
    }

    public void setMakePlayerGraph(boolean makePlayerGraph) {
        this.makePlayerGraph = makePlayerGraph;
    }

    public void setMakeRaidGraph(boolean makeRaidGraph) {
        this.makeRaidGraph = makeRaidGraph;
    }

    public void setMakeRaidGraphMulti(boolean makeRaidGraphMulti) {
        this.makeRaidGraphMulti = makeRaidGraphMulti;
    }
    
    public void makeFrontHtml(File dir) throws IOException {
        copyFiles(dir);
        File file = new File(dir.getAbsolutePath(), "index.html");
        BufferedWriter w = new BufferedWriter(new FileWriter(file));
        writeDocType(w);
        w.write("<HTML>");
        w.newLine();
        w.write("<HEAD>");
        w.newLine();

        if (this.headNote != null && this.headNote.length() != 0) {
            w.write("<TITLE>" + headNote + "</TITLE>");
            w.newLine();
        }

        writeCssImports(w);
        writeJsImports(w);
        w.write("</HEAD>");
        w.newLine();
        //w.write("<BODY BGCOLOR=" + bgColor + " TEXT=" + textColor + " LINK=" + linkColor + ">");
        w.write("<BODY>");
        w.newLine();
        writeHeader(w);
        w.newLine();

        if (this.headNote != null && this.headNote.length() != 0) {
            w.write("<H2>" + this.headNote + "</H2>");
            w.newLine();
        }

        w.write("<H2>Table of Contents</H2><BR><BR>");
        w.write("<DIV>");
        w.newLine();
        w.write(makeLinkClosed("merged/index.html", "Whole raid merged", cssTitleClass) + "<BR>");
        //w.write("<A HREF=\"merged/index.html\">Whole raid merged</A><BR>");
        w.newLine();
        w.write(makeLinkClosed("individual/index.html", "All fights individually", cssTitleClass) + "<BR>");
        //w.write("<A HREF=\"individual/index.html\">All fights individually</A><BR>");
        w.newLine();
        w.write("</DIV>");
        w.newLine();
        w.write("</BODY>");
        w.newLine();
        w.write("</HTML>");
        w.newLine();
        w.close();
    }

    private void makeHtml(File dir, List<Fight> list, boolean merge, IntCallback cb, StringCallback cbStr) throws IOException {
        if (mergeAndIndividual) {
            if (merge) {
                dir = new File(dir.getAbsolutePath() + "/merged");
            } else {
                dir = new File(dir.getAbsolutePath() + "/individual");
            }

            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    throw new IOException("Could not create directory in makeHtml.");
                }
            }
        }

        copyFiles(dir);
        File file = new File(dir.getAbsolutePath(), "index.html");
        BufferedWriter w = new BufferedWriter(new FileWriter(file));
        writeDocType(w);
        w.write("<HTML>");
        w.newLine();
        w.write("<HEAD>");
        w.newLine();
        if (this.headNote != null && this.headNote.length() != 0) {
            w.write("<TITLE>" + headNote + "</TITLE>");
            w.newLine();
        }
        writeCssImports(w);
        writeJsImports(w);
        //w.write("<style type=\"text/css\"> .dhtmlgoodies_aTab { border: none; } .dhtmlgoodies_tabPane { border: none; } </style>");
        w.write("</HEAD>");
        w.newLine();
        //w.write("<BODY BGCOLOR=" + bgColor + " TEXT=" + textColor + " LINK=" + linkColor + ">");
        w.write("<BODY>");
        w.newLine();
        w.write("<DIV style=\"margin: 0px auto; width: 800px;\">");
        writeHeader(w);
        w.newLine();
        if (this.headNote != null && this.headNote.length() != 0) {
            w.write("<H2>" + this.headNote + "</H2>");
            w.newLine();
        }
        for (int k = 0; k < list.size(); k++) {
            if (cb != null) {
                cb.reportInt((k+1)*100/list.size());
            }
            Fight f = new Fight(list.get(k));
            f.removeAssignedPets();
            String participantFilename = "participant_" + k + ".html";
            w.write("<H2>" + makeLinkClosed(participantFilename, f.getName() + " " + f.getEvents().get(0).getTimeString(), cssTitleClass) + "</H2>");
            //w.write("<H2><A HREF=\"" + participantFilename + "\">" + f.getName() + " " + f.getEvents().get(0).getTimeString() + "</A></H2>");
            w.newLine();
            w.write(makeLinkClosed("whw_" + k + ".html", "Who heals whom table", cssTitleClass));
            //w.write("<A HREF=\"whw_" + k + ".html\">Who heals whom table</A>");
            w.newLine();
            if (makeRaidGraph) {
                w.write("<br>");
                String dpsFilename = "rdg_dps_" + k + ".png";
                String hpsFilename = "rdg_hps_" + k + ".png";
                makeRaidDpsGraph(dir, dpsFilename, f);
                makeRaidHpsGraph(dir, hpsFilename, f);
                w.write("&nbsp;" + makeLinkClosed(dpsFilename, "Raid DPS graph", cssTitleClass));
                //w.write("&nbsp;<A HREF=\"" + dpsFilename + "\">Raid DPS graph</A>");
                w.newLine();
                w.write("&nbsp;" + makeLinkClosed(hpsFilename, "Raid HPS graph", cssTitleClass));
                //w.write("&nbsp;<A HREF=\"" + hpsFilename + "\">Raid HPS graph</A>");
                w.newLine();
            }
            if (makeRaidGraphMulti) {
                w.write("<br>");
                String dpsFilename = "rdg_multi_dps_" + k + ".png";
                String hpsFilename = "rdg_multi_hps_" + k + ".png";
                makeRaidDpsGraphMulti(dir, dpsFilename, f);
                makeRaidHpsGraphMulti(dir, hpsFilename, f);
                w.write("&nbsp;" + makeLinkClosed(dpsFilename, "Multi DPS graph", cssTitleClass));
                //w.write("&nbsp;<A HREF=\"" + dpsFilename + "\">Multi DPS graph</A>");
                w.newLine();
                w.write("&nbsp;" + makeLinkClosed(hpsFilename, "Multi HPS graph", cssTitleClass));
                //w.write("&nbsp;<A HREF=\"" + hpsFilename + "\">Multi HPS graph</A>");
                w.newLine();
            }
            w.write("<DIV ALIGN=\"LEFT\">");
            w.newLine();
            writeFightTableAndParticipants(w, f, k, dir);
            w.newLine();
            w.write("</DIV>");
            w.newLine();
            w.write("<br><br>");

            //Write victim participant info
            File victimFile = new File(dir.getAbsolutePath(), "participant_" + k + ".html");
            BufferedWriter victimW = new BufferedWriter(new FileWriter(victimFile));
            writeParticipantPre(victimW);
            writeParticipant(dir, victimW, f, f.getVictim(), "Others", "victimTabs", "" + k);
            writeParticipantPost(victimW);
            victimW.close();
        }
        w.write("</DIV>");
        w.write("</BODY>");
        w.newLine();
        w.write("</HTML>");
        w.newLine();
        w.close();

        if (cbStr != null) {
            cbStr.reportString("Making Who heals whom files.");
        }
        makeWhoHealsWhomHtml(dir, list, cb);

    }

    private void makeWhoHealsWhomHtml(File dir, List<Fight> list, IntCallback cb) throws IOException {
        int k, l;
        for (k = 0; k < list.size(); k++) {
            if (cb != null) {
                cb.reportInt((k+1)*100/list.size());
            }
            Fight f = list.get(k);
            File file = new File(dir.getAbsolutePath(), "whw_" + k + ".html");
            BufferedWriter w = new BufferedWriter(new FileWriter(file));

            writeWhoHealsWhom(w, f);
            w.newLine();
            w.close();
        }
    }

    private void writeWhoHealsWhom(BufferedWriter w, Fight f) throws IOException {
        w.write("<HTML>");
        w.newLine();
        w.write("<HEAD>");
        w.newLine();
        if (this.headNote != null && this.headNote.length() != 0) {
            w.write("<TITLE>" + headNote + "</TITLE>");
            w.newLine();
        }
        writeCssImports(w);
        writeJsImports(w);
        w.write("</HEAD>");
        w.newLine();
        //w.write("<BODY BGCOLOR=" + bgColor + " TEXT=" + textColor + " LINK=" + linkColor + ">");
        w.write("<BODY>");
        w.newLine();
        w.write("<DIV style=\"margin: 0px auto; width: 1024px;\">");
        w.newLine();

        writeHeader(w);
        w.newLine();
        if (this.headNote != null && this.headNote.length() != 0) {
            w.write("<H2>" + this.headNote + "</H2>");
            w.newLine();
        }
        w.write("<H2>Who heals whom in fight vs " + f.getName() + "</H2>");
        w.newLine();

        w.write("<DIV>");
        w.newLine();
        writeWhoHealsWhomTable(w, f);
        w.write("</DIV>");
        w.newLine();
        w.write("</BODY>");
        w.newLine();
        w.write("</HTML>");
        w.newLine();
    }

    private void makeParticipantHtml(File dir, String filename, Fight f, FightParticipant p, int fightIndex, int partIndex) throws IOException {
        int k, l;
        File file = new File(dir.getAbsolutePath(), filename);
        BufferedWriter w = new BufferedWriter(new FileWriter(file));
        writeParticipantPre(w);

        w.write("<div id=\"");
        w.write("donerectabs");
        w.write("\">");

        w.write("<div class=\"DHTMLSuite_aTab\">");
        w.newLine();
        writeParticipant(dir, w, f, p, f.getName(), "doneTabs", "" + fightIndex + "_" + partIndex);
        w.newLine();
        w.write("</div>");
        w.newLine();

        w.write("<div class=\"DHTMLSuite_aTab\">");
        w.newLine();
        FightParticipant pr = p.makeReceivedParticipant(f);
        writeParticipant(dir, w, f, pr, p.getName(), "recTabs", "" + fightIndex + "_" + partIndex + "_rec");
        w.write("</div>");
        w.newLine();

        w.write("</div>");
        w.newLine();
        writeMakeTab(w, "donerectabs", new String[] {"Events Made", "Events Received"});

        writeParticipantPost(w);
        w.close();
    }

    private void writeParticipantPre(BufferedWriter w) throws IOException {
        w.write("<HTML>");
        w.newLine();
        w.write("<HEAD>");
        w.newLine();
        if (this.headNote != null && this.headNote.length() != 0) {
            w.write("<TITLE>" + headNote + "</TITLE>");
            w.newLine();
        }
        writeCssImports(w);
        writeJsImports(w);
        //w.write("<style type=\"text/css\"> .dhtmlgoodies_aTab { border: none; } .dhtmlgoodies_tabPane { border: none; } </style>");
        w.write("</HEAD>");
        w.newLine();
        //w.write("<BODY BGCOLOR=" + bgColor + " TEXT=" + textColor + " LINK=" + linkColor + ">");
        w.write("<BODY>");
        w.newLine();
    }

    private void writeParticipantPost(BufferedWriter w) throws IOException {
        w.write("</BODY>");
        w.newLine();
        w.write("</HTML>");
        w.newLine();
    }

    private void writeParticipant(File dir, BufferedWriter w, Fight f, FightParticipant p, String fightName, String divName, String graphBaseFilename) throws IOException {
        w.write("<DIV style=\"margin: 0px auto; \">");
        w.newLine();

        writeHeader(w);
        w.newLine();
        if (this.headNote != null && this.headNote.length() != 0) {
            w.write("<H2>" + this.headNote + "</H2>");
            w.newLine();
        }
        w.write("<H2>" + p.getName() + " vs " + fightName + "</H2>");
        w.newLine();

        if (makePlayerGraph) {
            String dpsGraphFilename = graphBaseFilename + "_dps.png";
            String hpsGraphFilename = graphBaseFilename + "_hps.png";
            makePlayerDpsGraph(f, p, dir, dpsGraphFilename);
            makePlayerHpsGraph(f, p, dir, hpsGraphFilename);
            w.write(makeLinkClosed(dpsGraphFilename, "DPS graph", cssTitleClass));
            //w.write("<A HREF=\"" + dpsGraphFilename + "\">DPS graph</A>");
            w.newLine();
            w.write("&nbsp;" + makeLinkClosed(hpsGraphFilename, "HPS graph", cssTitleClass));
            //w.write("&nbsp;<A HREF=\"" + hpsGraphFilename + "\">HPS graph</A>");
            w.newLine();
        }

        w.write("<div id=\"");
        w.write(divName);
        w.write("\">");

        w.write("<div class=\"DHTMLSuite_aTab\">");
        w.newLine();
        w.write("<DIV>");
        w.newLine();
        writeParticipantDamageTable(w, f, p);
        w.newLine();
        w.write("</DIV>");
        w.newLine();
        w.write("</DIV>");
        w.newLine();

        w.write("<div class=\"DHTMLSuite_aTab\">");
        w.newLine();
        w.write("<DIV>");
        w.newLine();
        writeParticipantHealingTable(w, f, p);
        w.newLine();
        w.write("</DIV>");
        w.newLine();
        w.write("</DIV>");
        w.newLine();

        w.write("<div class=\"DHTMLSuite_aTab\">");
        w.newLine();
        w.write("<DIV>");
        w.newLine();
        writeParticipantPowerTable(w, f, p);
        w.newLine();
        w.write("</DIV>");
        w.newLine();
        w.write("</DIV>");
        w.newLine();

        w.write("<div class=\"DHTMLSuite_aTab\">");
        w.newLine();
        w.write("<DIV>");
        w.newLine();
        writeParticipantDamageExtendedTable(w, f, p);
        w.newLine();
        w.write("</DIV>");
        w.newLine();
        w.write("</DIV>");
        w.newLine();

        w.write("<div class=\"DHTMLSuite_aTab\">");
        w.newLine();
        w.write("<DIV>");
        w.newLine();
        writeParticipantAbilityTable(w, f, p);
        w.newLine();
        w.write("</DIV>");
        w.newLine();
        w.write("</DIV>");
        w.newLine();

        w.write("</DIV>");
        w.newLine();
        w.write("</DIV>");
        w.newLine();
        writeMakeTab(w, divName, new String[] {"Damage", "Healing", "Power", "Extended Damage Info", "Ability Info"});
    }

    private void writeFightTableAndParticipants(BufferedWriter w, Fight f, int fightIndex, File dir) throws IOException {
        int k;
        FightParticipantsTableModelDamage ftmDamage = new FightParticipantsTableModelDamage();
        ftmDamage.setFight(f);
        FightParticipantsTableModelHealing ftmHealing = new FightParticipantsTableModelHealing();
        ftmHealing.setFight(f);
        FightParticipantsTableModelReceivedDamage ftmRecDamage = new FightParticipantsTableModelReceivedDamage();
        ftmRecDamage.setFight(f);
        FightParticipantsTableModelReceivedHealing ftmRecHealing = new FightParticipantsTableModelReceivedHealing();
        ftmRecHealing.setFight(f);

        //Fix links
        for (k=0; k<ftmDamage.getRowCount(); k++) {
            String name = ftmDamage.getValueAt(k, 0).toString();
            String pFile = "participant_" + fightIndex + "_" + k + ".html";
            String newName = makeLinkClosed(pFile, name, linkClassForClasses.get(f.getParticipants().get(k).getPlayerClass()));
            //String newName = "<A HREF=\"" + pFile + "\">" + name + "</A>";
            ftmDamage.setValueAt(newName, k, 0);
        }
        //Fix links
        for (k=0; k<ftmRecDamage.getRowCount(); k++) {
            String name = ftmRecDamage.getValueAt(k, 0).toString();
            String pFile = "participant_" + fightIndex + "_" + k + ".html";
            String newName = makeLinkClosed(pFile, name, linkClassForClasses.get(f.getParticipants().get(k).getPlayerClass()));
            //String newName = "<A HREF=\"" + pFile + "\">" + name + "</A>";
            ftmRecDamage.setValueAt(newName, k, 0);
        }
        //Fix links
        for (k=0; k<ftmHealing.getRowCount(); k++) {
            String name = ftmHealing.getValueAt(k, 0).toString();
            String pFile = "participant_" + fightIndex + "_" + k + ".html";
            String newName = makeLinkClosed(pFile, name, linkClassForClasses.get(f.getParticipants().get(k).getPlayerClass()));
            //String newName = "<A HREF=\"" + pFile + "\">" + name + "</A>";
            ftmHealing.setValueAt(newName, k, 0);
        }
        //Fix links
        for (k=0; k<ftmRecHealing.getRowCount(); k++) {
            String name = ftmRecHealing.getValueAt(k, 0).toString();
            String pFile = "participant_" + fightIndex + "_" + k + ".html";
            String newName = makeLinkClosed(pFile, name, linkClassForClasses.get(f.getParticipants().get(k).getPlayerClass()));
            //String newName = "<A HREF=\"" + pFile + "\">" + name + "</A>";
            ftmRecHealing.setValueAt(newName, k, 0);
        }

        //Write participants
        for (k=0; k<f.getParticipants().size(); k++) {
            FightParticipant p = f.getParticipants().get(k);
            String pFile = "participant_" + fightIndex + "_" + k + ".html";
            makeParticipantHtml(dir, pFile, f, p, fightIndex, k);
        }

        SortFilterModel sorter = new SortFilterModel(ftmDamage);
        sorter.sort(1);
        SortFilterModel sorter2 = new SortFilterModel(ftmHealing);
        sorter2.sort(1);
        SortFilterModel sorter3 = new SortFilterModel(ftmRecDamage);
        sorter3.sort(1);
        SortFilterModel sorter4 = new SortFilterModel(ftmRecHealing);
        sorter4.sort(1);

        w.write("<div id=\"");
        w.write("fight" + (fightIndex+1));
        w.write("\">");

        List<Integer> classColumns;
        List<Integer> classes;

        w.write("<div class=\"DHTMLSuite_aTab\">");
        w.newLine();
        classColumns = new ArrayList<Integer>(); classColumns.add(0); classColumns.add(3);
        classes = new ArrayList<Integer>();
        for(k=0; k<f.getParticipants().size(); k++) {
            int unsortedRow = sorter.convertRowIndexToModel(k);
            classes.add(f.getParticipants().get(unsortedRow).getPlayerClass());
        }
        writeTable(w, sorter, tableClassDamage, classColumns, classes);
        w.newLine();
        w.write("</div>");
        w.newLine();

        w.write("<div class=\"DHTMLSuite_aTab\">");
        w.newLine();
        classColumns = new ArrayList<Integer>(); classColumns.add(0); classColumns.add(5);
        classes = new ArrayList<Integer>();
        for(k=0; k<f.getParticipants().size(); k++) {
            int unsortedRow = sorter2.convertRowIndexToModel(k);
            classes.add(f.getParticipants().get(unsortedRow).getPlayerClass());
        }
        writeTable(w, sorter2, tableClassHealing, classColumns, classes);
        w.newLine();
        w.write("</div>");
        w.newLine();

        w.write("<div class=\"DHTMLSuite_aTab\">");
        w.newLine();
        classColumns = new ArrayList<Integer>(); classColumns.add(0); classColumns.add(3);
        classes = new ArrayList<Integer>();
        for(k=0; k<f.getParticipants().size(); k++) {
            int unsortedRow = sorter3.convertRowIndexToModel(k);
            classes.add(f.getParticipants().get(unsortedRow).getPlayerClass());
        }
        writeTable(w, sorter3, tableClassDamage, classColumns, classes);
        w.newLine();
        w.write("</div>");
        w.newLine();

        w.write("<div class=\"DHTMLSuite_aTab\">");
        w.newLine();
        classColumns = new ArrayList<Integer>(); classColumns.add(0); classColumns.add(5);
        classes = new ArrayList<Integer>();
        for(k=0; k<f.getParticipants().size(); k++) {
            int unsortedRow = sorter4.convertRowIndexToModel(k);
            classes.add(f.getParticipants().get(unsortedRow).getPlayerClass());
        }
        writeTable(w, sorter4, tableClassHealing, classColumns, classes);
        w.newLine();
        w.write("</div>");
        w.newLine();

        w.write("</div>");
        w.newLine();
        writeMakeTab(w, "fight" + (fightIndex+1), new String[] {"Damage", "Healing", "Received Damage", "Received Healing"});
        w.newLine();
    }

    private void writeParticipantDamageTable(BufferedWriter w, Fight f, FightParticipant p) throws IOException {
            TableModel tm = new ParticipantDamageTableModel(f, p);
            writeTable(w, tm, tableClassDamage, null, null);
    }

    private void writeParticipantDamageExtendedTable(BufferedWriter w, Fight f, FightParticipant p) throws IOException {
            TableModel tm = new ParticipantDamageExtendedTableModel(f, p);
            writeTable(w, tm, tableClassDamage, null, null);
    }

    private void writeParticipantHealingTable(BufferedWriter w, Fight f, FightParticipant p) throws IOException {
            TableModel tm = new ParticipantHealingTableModel(f, p);
            writeTable(w, tm, tableClassHealing, null, null);
    }

    private void writeParticipantPowerTable(BufferedWriter w, Fight f, FightParticipant p) throws IOException {
            TableModel tm = new ParticipantPowerTableModel(f, p);
            writeTable(w, tm, tableClassPower, null, null);
    }

    private void writeParticipantAbilityTable(BufferedWriter w, Fight f, FightParticipant p) throws IOException {
            TableModel tm = new ParticipantAbilityTableModel(f, p);
            writeTable(w, tm, tableClassAbility, null, null);
    }

    private void writeWhoHealsWhomTable(BufferedWriter w, Fight f) throws IOException {
            TableModel tm = new WhoHealsWhomTableModel(f);
            writeTable(w, tm, tableClassWhoHealsWhom, null, null);
    }

    /**
     * Write a table.
     * @param w
     * @param tm
     * @param tableClass
     * @param classColumns A list of which columns should have class colours, or null for default.
     * @param classes A list of classes for each row in the table, or null for default.
     * @throws java.io.IOException
     */
    private void writeTable(BufferedWriter w, TableModel tm, String tableClass, List<Integer> classColumns, List<Integer> classes) throws IOException {
        int k, l;
        int numCols = tm.getColumnCount();
        int numRows = tm.getRowCount();
        if (classColumns == null) {
            classColumns = new ArrayList<Integer>();
        }

        //w.write("<TABLE CLASS=\"" + tableClass + "\" ALIGN=\"CENTER\">");
        w.write("<TABLE CLASS=\"" + tableClass + "\">");
        w.newLine();
        w.write("<THEAD CLASS=\"" + tableClass + "\">");
        w.newLine();
        w.write("<TR CLASS=\"" + tableClass + "\">");
        w.newLine();
        for (k = 0; k < numCols; k++) {
            w.write("<TH CLASS=\"" + tableClass + "\">" + tm.getColumnName(k) + "</TH>");
            w.newLine();
        }
        w.write("</THEAD>");
        w.newLine();
        w.write("</TR>");
        w.newLine();
        w.write("<TBODY>");
        w.newLine();
        for (k = 0; k < numRows; k++) {
            w.write("<TR CLASS=\"" + tableClass + "\">");
            w.newLine();
            for (l = 0; l < numCols; l++) {
                Object val = tm.getValueAt(k, l);
                if (val instanceof StringColor) {
                    StringColor sc = (StringColor)val;
                    if (sc.getBackground() != null) {
                        String hexString = Integer.toHexString(sc.getBackground().getRGB()&0xFFFFFF);
                        if (hexString.length() < 6) {
                            int numZeros = 6 - hexString.length();
                            for (int v=0; v<numZeros; v++) {
                                hexString = "0" + hexString;
                            }
                        }
                        String cssBgColor = "#" + hexString;
                        if (classColumns.contains(Integer.valueOf(l))) {
                            w.write("<TD CLASS=\"" + tableClassForClasses.get(classes.get(k)) + "\" style=\"background: " + cssBgColor + "\">");
                        } else {
                            w.write("<TD CLASS=\"" + tableClass + "\" style=\"background: " + cssBgColor + "\">");
                        }
                    } else {
                        if (classColumns.contains(Integer.valueOf(l))) {
                            w.write("<TD CLASS=\"" + tableClassForClasses.get(classes.get(k)) + "\">");
                        } else {
                            w.write("<TD CLASS=\"" + tableClass + "\">");
                        }
                    }
                } else {
                    if (classColumns.contains(Integer.valueOf(l))) {
                        w.write("<TD CLASS=\"" + tableClassForClasses.get(classes.get(k)) + "\">");
                    } else {
                        w.write("<TD CLASS=\"" + tableClass + "\">");
                    }
                }
                if (tm.getValueAt(k, 0).equals("")) {
                    w.write("&nbsp");
                } else {
                    w.write(tm.getValueAt(k, l).toString());
                }
                w.write("</TD>");
                w.newLine();
            }
            w.write("</TR>");
            w.newLine();
        }
        w.write("</TBODY>");
        w.newLine();
        w.write("</TABLE>");
    //w.newLine();

    }

    private void copyFiles(File dir) throws IOException, NullPointerException {
        File cssDir = new File(dir.getAbsolutePath(), "css");
        if (!cssDir.exists()) {
            if(!cssDir.mkdir()) {
                throw new IOException("Could not create css dir.");
            }
        }
        for (String resourceName : cssResourceFiles) {
            InputStream inStream = getClass().getClassLoader().getResourceAsStream(resourceName);
            String outName = new File(resourceName).getName();
            File outFile = new File(cssDir, outName);
            FileHelper.copyFile(inStream, outFile);
        }
        File jsDir = new File(dir.getAbsolutePath(), "js");
        if (!jsDir.exists()) {
            if (!jsDir.mkdir()) {
                throw new IOException("Could not create js dir.");
            }
        }
        for (String resourceName : jsResourceFiles) {
            InputStream inStream = getClass().getClassLoader().getResourceAsStream(resourceName);
            String outName = new File(resourceName).getName();
            File outFile = new File(jsDir, outName);
            FileHelper.copyFile(inStream, outFile);
        }
        File imagesDir = new File(dir.getAbsolutePath(), "images");
        if (!imagesDir.exists()) {
            if (!imagesDir.mkdir()) {
                throw new IOException("Could not create images dir.");
            }
        }
        for (String resourceName : imageResourceFiles) {
            InputStream inStream = getClass().getClassLoader().getResourceAsStream(resourceName);
            String outName = new File(resourceName).getName();
            File outFile = new File(imagesDir, outName);
            FileHelper.copyFile(inStream, outFile);
        }
        File imagesTabViewDir = new File(dir.getAbsolutePath(), "images/tab-view");
        if (!imagesTabViewDir.exists()) {
            if (!imagesTabViewDir.mkdir()) {
                throw new IOException("Could not create images/tab-view dir.");
            }
        }
        for (String resourceName : imageTabViewResourceFiles) {
            InputStream inStream = getClass().getClassLoader().getResourceAsStream(resourceName);
            String outName = new File(resourceName).getName();
            File outFile = new File(imagesTabViewDir, outName);
            FileHelper.copyFile(inStream, outFile);
        }

        if (customCss) {
            InputStream inStream = null;
            try {
                inStream = new FileInputStream(new File(customCssFilename));
                File outFile = new File(cssDir, defaultMainCssFilename);
                FileHelper.copyFile(inStream, outFile);
            } finally {
                FileHelper.close(inStream);
            }
        }
    }

    private void makeRaidDpsGraph(File dir, String filename, Fight f) throws IOException {
        List<Fight> fights = new ArrayList<Fight>();
        fights.add(f);
        List<Boolean> includedFights = new ArrayList<Boolean>();
        includedFights.add(true);
        List<String> names = new ArrayList<String>();
        names.add(f.getName());
        DefaultXYDataset dataset = WlpPlotFactory.createRaidDamageLinePlotDataset(fights, includedFights, names, 10);
        JFreeChart chart = WlpPlotFactory.createRaidDamageLinePlot(dataset, SettingsSingleton.getInstance().getSplinePlots());
        ChartUtilities.saveChartAsPNG(new File(dir, filename), chart, chartWidth, chartHeight);
    }

    private void makeRaidDpsGraphMulti(File dir, String filename, Fight f) throws IOException {
        List<FightParticipant> participants = new ArrayList<FightParticipant>();
        participants.addAll(f.getParticipants());
        List<Boolean> includedParticipants = new ArrayList<Boolean>();
        List<String> names = new ArrayList<String>();
        for (FightParticipant p : participants) {
            includedParticipants.add(true);
            names.add(p.getName());
        }

        DefaultXYDataset dataset = WlpPlotFactory.createMultipleParticipantsDamageLinePlotDatset(f, participants, includedParticipants, 10, 0);
        JFreeChart chart = WlpPlotFactory.createMultipleParticipantsDamageLinePlot(dataset, SettingsSingleton.getInstance().getSplinePlots());
        ChartUtilities.saveChartAsPNG(new File(dir, filename), chart, chartWidth, chartHeight);
    }

    private void makeRaidHpsGraph(File dir, String filename, Fight f) throws IOException {
        List<Fight> fights = new ArrayList<Fight>();
        fights.add(f);
        List<Boolean> includedFights = new ArrayList<Boolean>();
        includedFights.add(true);
        List<String> names = new ArrayList<String>();
        names.add(f.getName());
        DefaultXYDataset dataset = WlpPlotFactory.createRaidHealingLinePlotDataset(fights, includedFights, names, 10);
        JFreeChart chart = WlpPlotFactory.createRaidHealingLinePlot(dataset, SettingsSingleton.getInstance().getSplinePlots());
        ChartUtilities.saveChartAsPNG(new File(dir, filename), chart, chartWidth, chartHeight);
    }

    private void makeRaidHpsGraphMulti(File dir, String filename, Fight f) throws IOException {
        List<FightParticipant> participants = new ArrayList<FightParticipant>();
        participants.addAll(f.getParticipants());
        List<Boolean> includedParticipants = new ArrayList<Boolean>();
        List<String> names = new ArrayList<String>();
        for (FightParticipant p : participants) {
            includedParticipants.add(true);
            names.add(p.getName());
        }

        DefaultXYDataset dataset = WlpPlotFactory.createMultipleParticipantsHealingLinePlotDatset(f, participants, includedParticipants, 10, 0);
        JFreeChart chart = WlpPlotFactory.createMultipleParticipantsHealingLinePlot(dataset, SettingsSingleton.getInstance().getSplinePlots());
        ChartUtilities.saveChartAsPNG(new File(dir, filename), chart, chartWidth, chartHeight);
    }

    private void makePlayerDpsGraph(Fight f, FightParticipant p, File dir, String filename) throws IOException {
        double[][] data = WlpPlotFactory.createParticipantDamageLinePlotData(f, p, 10);
        JFreeChart chart = WlpPlotFactory.createDamageLinePlot(data, SettingsSingleton.getInstance().getSplinePlots());
        ChartUtilities.saveChartAsPNG(new File(dir, filename), chart, chartWidth, chartHeight);
    }

    private void makePlayerHpsGraph(Fight f, FightParticipant p, File dir, String filename) throws IOException {
        double[][] data = WlpPlotFactory.createParticipantHealingLinePlotData(f, p, 10);
        JFreeChart chart = WlpPlotFactory.createHealingLinePlot(data, SettingsSingleton.getInstance().getSplinePlots());
        ChartUtilities.saveChartAsPNG(new File(dir, filename), chart, chartWidth, chartHeight);
    }

    private String makeLinkClosed(String hrefLink, String text, String cssClassName) {
        String s = "";
        if (cssClassName == null) {
            s = "<a href=\"" + hrefLink + "\">" + text + "</a>";
        } else {
            s = "<a href=\"" + hrefLink + "\" class=\"" + cssClassName + "\">" + text + "</a>";
        }
        return s;
    }

    public class IndexDamage implements Comparable {
        public int index;
        public long damage;

        public IndexDamage(int index, long damage) {
            this.index = index;
            this.damage = damage;
        }

        public int compareTo(Object o) {
            if (o instanceof IndexDamage) {
                IndexDamage id =  (IndexDamage)o;
                return (int)Math.signum((double)damage - (double)id.damage);
            } else {
                return 0;
            }
        }

    }

}
