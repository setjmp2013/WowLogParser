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

package wowlogparser.gui;

import wowlogparserbase.helpers.WorkerProgressDialog;
import wowlogparserbase.helpers.Worker;
import wowlogparserbase.Fight;
import wowlogparserbase.IntCallback;
import wowlogparserbase.FightParticipant;
import wowlogparserbase.StringCallback;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.table.TableModel;
import javax.swing.JOptionPane;
import wowlogparser.*;
import wowlogparserbase.Version;
import wowlogparserbase.helpers.FileHelper;
import wowlogparserbase.tablemodels.*;

public class MakeHtmlReport {

    public static final String styleFileName = "tablestyle.css";
    public static final String styleResourceName = "html/tablestyle.css";
    boolean merge;
    protected List<Fight> list;
    Preferences basePrefs;
    JFrame owner;
    // Callithrix - They modification - Begin
    String headNote;
    boolean mergeAndIndividual;
    // Callithrix - They modification - End

    public MakeHtmlReport(JFrame owner, List<Fight> list, Preferences basePrefs, boolean merge) {
        this.merge = merge;
        this.list = list;
        this.basePrefs = basePrefs;
        this.owner = owner;
        // Callithrix - They modification - Begin
        this.mergeAndIndividual = false;
        // Callithrix - They modification - End
    }
    
    // Callithrix - They modification - Begin
    public MakeHtmlReport(JFrame owner, List<Fight> list, Preferences basePrefs, boolean merge, boolean mergeAndIndividual) {
        this.merge = merge;
        this.list = list;
        this.basePrefs = basePrefs;
        this.owner = owner;
        this.mergeAndIndividual = mergeAndIndividual;
    }

    // Callithrix - They modification - End

    public List<Fight> getList() {
        return list;
    }

    public void fixList(IntCallback cb, StringCallback cbStr) {
        if (merge) {
            ArrayList<Fight> mergedFight = new ArrayList<Fight>();
            mergedFight.add(Fight.merge(list, cb, cbStr));
            list = mergedFight;
        }
        for (Fight f : list) {
            f.removeAssignedPets();
        }
    }

    private void writeHeader(Writer w) throws IOException {
        w.write("<H1>" + "<a href=\"http://www.gurre.eu/wowlogparser/forum\">Wow Log Parser</a> report (v" + Version.getVersion() + ")" + "</H1>");
    }

    public class MakeHtmlThread extends Worker implements IntCallback, StringCallback {
        File dir;
        public MakeHtmlThread(File dir) {
            this.dir = dir;
        }

        @Override
        public void run() {
            if (mergeAndIndividual) {
                dialog.setWorkerText("Making individual fights...");
                merge = false;
                fixList(MakeHtmlThread.this, MakeHtmlThread.this);
                makeHtml(dir, list, MakeHtmlThread.this, MakeHtmlThread.this);

                dialog.setWorkerText("Merging fights...");
                merge = true;
                fixList(MakeHtmlThread.this, MakeHtmlThread.this);
                dialog.setWorkerText("Making merged fights...");
                makeHtml(dir, list, MakeHtmlThread.this, MakeHtmlThread.this);

                makeFrontHtml(dir);
            } else {
                if (merge) {
                    dialog.setWorkerText("Merging fights...");                    
                    fixList(MakeHtmlThread.this, MakeHtmlThread.this);
                } else {
                    dialog.setWorkerText("Making individual fights...");
                    fixList(MakeHtmlThread.this, MakeHtmlThread.this);
                }
                makeHtml(dir, list, MakeHtmlThread.this, MakeHtmlThread.this);
            }
            hideDialog();
        }

        public void reportInt(int theInt) {
            int val = (theInt*100)/getList().size();
            dialog.setProgress(val);
        }

        public void reportString(String str) {
            if (str.equalsIgnoreCase("sort")) {
                if (merge) {
                    dialog.setWorkerText("Sorting merged fights...");
                }
            } else if (str.equalsIgnoreCase("participantshtml")) {
                if (merge) {
                    dialog.setWorkerText("Making participants for merged fights...");
                } else {
                    dialog.setWorkerText("Making participants for individual fights...");
                }
            } else {
                dialog.setWorkerText(str);
            }
        }
        
    }
    
    public void showFileChooser() {
        // Callithrix - They modification - Begin
        headNote = JOptionPane.showInputDialog(this.owner, "Enter guild name or raid note", "Custom headline", JOptionPane.QUESTION_MESSAGE);
        // Callithrix - They modification - End
        Preferences prefs = basePrefs.node("html");
        String dirString = prefs.get("outputPath", "");
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File(dirString));
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setDialogTitle("Choose a directory where the HTML files should be saved.");
        int res = fc.showDialog(owner, "Ok");
        if (res == JFileChooser.APPROVE_OPTION) {
            File dir = fc.getSelectedFile();
            prefs.put("outputPath", dir.getAbsolutePath());
            MakeHtmlThread ht = new MakeHtmlThread(dir);
            WorkerProgressDialog dia = new WorkerProgressDialog(owner, "Making HTML", true, ht, "Making HTML files, please wait...");
            dia.setLocationRelativeTo(owner.getContentPane());
            dia.setVisible(true);
        }
    }
    
    // Callithrix - They modification - Begin
    public void makeFrontHtml(File dir) {
        BufferedWriter w = null;
        try {
            makeStyle(dir);
            File file = new File(dir.getAbsolutePath(), "index.html");
            w = new BufferedWriter(new FileWriter(file));
            w.write("<HTML>");
            w.newLine();
            w.write("<HEAD>");
            w.newLine();

            if (this.headNote != null && this.headNote.length() != 0) {
                w.write("<TITLE>" + headNote + "</TITLE>");
                w.newLine();
            }

            w.write("<link rel=\"STYLESHEET\" type=\"text/css\" href=\"" + styleFileName + "\">");
            w.newLine();
            w.write("</HEAD>");
            w.newLine();
            w.write("<BODY BGCOLOR=#000000 TEXT=WHITE LINK=WHITE>");
            w.newLine();
            writeHeader(w);
            w.newLine();

            if (this.headNote != null && this.headNote.length() != 0) {
                w.write("<H2>" + this.headNote + "</H2>");
                w.newLine();
            }

            w.write("<H2>Table of Contents</H2><BR><BR>");
            w.write("<DIV ALIGN=\"center\">");
            w.newLine();
            w.write("<A HREF=\"merged/index.html\">Whole raid merged</A><BR>");
            w.newLine();
            w.write("<A HREF=\"individual/index.html\">All fights individually</A><BR>");
            w.newLine();
            w.write("</DIV>");
            w.write("</BODY>");
            w.newLine();
            w.write("</HTML>");
            w.newLine();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(owner, "Something went wrong when writing the html files.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch(NullPointerException ex) {
            JOptionPane.showMessageDialog(owner, "Something went wrong when writing the html files.", "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            FileHelper.close(w);
        }
    }
    // Callithrix - They modification - End
    
    public void makeHtml(File dir, List<Fight> list, IntCallback cb, StringCallback cbStr) {
        BufferedWriter w = null;
        try {
            // Callithrix - They modification - Begin
            if (this.mergeAndIndividual) {
                if (this.merge) {
                    dir = new File(dir.getAbsolutePath() + "/merged");
                } else {
                    dir = new File(dir.getAbsolutePath() + "/individual");
                }

                if (!dir.exists()) {
                    if (!dir.mkdirs()) {
                        JOptionPane.showMessageDialog(owner, "Could not create the directory.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }
            // Callithrix - They modification - End

            makeStyle(dir);
            File file = new File(dir.getAbsolutePath(), "index.html");               
            w = new BufferedWriter(new FileWriter(file));
            w.write("<HTML>");
            w.newLine();
            w.write("<HEAD>");
            w.newLine();
            // Callithrix - They modification - Begin
            if (this.headNote != null && this.headNote.length() != 0) {
                w.write("<TITLE>" + headNote + "</TITLE>");
                w.newLine();
            }
            // Callithrix - They modification - End
            w.write("<link rel=\"STYLESHEET\" type=\"text/css\" href=\"" + styleFileName + "\">");
            w.newLine();
            w.write("</HEAD>");
            w.newLine();
            w.write("<BODY BGCOLOR=#000000 TEXT=WHITE LINK=WHITE>");
            w.newLine();
            writeHeader(w);
            w.newLine();
            // Callithrix - They modification - Begin
            if (this.headNote != null && this.headNote.length() != 0) {
                w.write("<H2>" + this.headNote + "</H2>");
                w.newLine();
            }
            // Callithrix - They modification - End
            for (int k = 0; k < list.size(); k++) {
                if (cb != null) {
                    cb.reportInt(k);
                }
                Fight f = list.get(k);
                w.write("<H2><A HREF=\"" + "participant_" + k + ".html\">" + f.getName() + " " + f.getEvents().get(0).getTimeString() + "</A></H2>");
                w.newLine();
                w.write("<DIV ALIGN=\"CENTER\">");
                w.newLine();
                writeFightTable(w, f, k);
                w.newLine();
                w.write("</DIV>");
                w.newLine();
                w.write("<br><br>");

                //Write victim participant info
                File victimFile = new File(dir.getAbsolutePath(), "participant_" + k + ".html");
                BufferedWriter victimW = new BufferedWriter(new FileWriter(victimFile));
                writeParticipant(victimW, f, f.getVictim(), "Others");
                victimW.close();
            }
            w.write("</BODY>");
            w.newLine();
            w.write("</HTML>");
            w.newLine();

            cbStr.reportString("participantshtml");
            makeParticipantsHtml(dir, list, cb);

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(owner, "Something went wrong when writing the html files.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch(NullPointerException ex) {
            JOptionPane.showMessageDialog(owner, "Something went wrong when writing the html files.", "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            FileHelper.close(w);
        }
    }

    public void makeParticipantsHtml(File dir, List<Fight> list, IntCallback cb) throws IOException {
        int k, l;
        for (k = 0; k < list.size(); k++) {
            if (cb != null) {
                cb.reportInt(k);
            }
            Fight f = list.get(k);
            for (l = 0; l < f.getParticipants().size(); l++) {
                BufferedWriter w = null;
                try {
                    FightParticipant p = f.getParticipants().get(l);
                    File file = new File(dir.getAbsolutePath(), "participant_" + k + "_" + l + ".html");
                    w = new BufferedWriter(new FileWriter(file));
                    writeParticipant(w, f, p, f.getName());
                    w.newLine();
                    w.write("<br> <br>");
                    w.newLine();
                    FightParticipant pr = p.makeReceivedParticipant(f);
                    writeParticipant(w, f, pr, p.getName());
                } finally {
                    FileHelper.close(w);
                }
            }
        }
    }

    public void writeParticipant(BufferedWriter w, Fight f, FightParticipant p, String fightName) throws IOException {
        w.write("<HTML>");
        w.newLine();
        w.write("<HEAD>");
        w.newLine();
        // Callithrix - They modification - Begin
        if (this.headNote != null && this.headNote.length() != 0) {
            w.write("<TITLE>" + headNote + "</TITLE>");
            w.newLine();
        }
        // Callithrix - They modification - End
        w.write("<link rel=\"STYLESHEET\" type=\"text/css\" href=\"" + styleFileName + "\">");
        w.newLine();
        w.write("</HEAD>");
        w.newLine();
        w.write("<BODY BGCOLOR=#000000 TEXT=WHITE LINK=WHITE>");
        w.newLine();
        writeHeader(w);
        w.newLine();
        // Callithrix - They modification - Begin
        if (this.headNote != null && this.headNote.length() != 0) {
            w.write("<H2>" + this.headNote + "</H2>");
            w.newLine();
        }
        // Callithrix - They modification - End            
        w.write("<H2>" + p.getName() + " vs " + fightName + "</H1>");
        w.newLine();

        w.write("<DIV ALIGN=\"CENTER\">");
        w.newLine();
        writeParticipantDamageTable(w, f, p);
        w.newLine();
        w.write("</DIV>");
        w.newLine();
        w.write("<BR>");
        w.newLine();
        w.write("<DIV ALIGN=\"CENTER\">");
        w.newLine();
        writeParticipantHealingTable(w, f, p);
        w.newLine();
        w.write("</DIV>");
        w.newLine();
        w.write("<BR>");
        w.newLine();
        w.write("<DIV ALIGN=\"CENTER\">");
        w.newLine();
        writeParticipantPowerTable(w, f, p);
        w.newLine();
        w.write("</DIV>");
        w.newLine();

        w.write("</BODY>");
        w.newLine();
        w.write("</HTML>");
        w.newLine();
    }

    public void writeFightTable(BufferedWriter w, Fight f, int fightIndex) throws IOException {
        int k;
        FightParticipantsTableModelDamage ftmDamage = new FightParticipantsTableModelDamage();
        ftmDamage.setFight(f);
        FightParticipantsTableModelHealing ftmHealing = new FightParticipantsTableModelHealing();
        ftmHealing.setFight(f);
        
        //Fix links
        for (k=0; k<ftmDamage.getRowCount(); k++) {
            String name = ftmDamage.getValueAt(k, 0).toString();
            String pFile = "participant_" + fightIndex + "_" + k + ".html";
            String newName = "<A HREF=\"" + pFile + "\">" + name + "</A>";
            ftmDamage.setValueAt(newName, k, 0);
        }
        //Fix links
        for (k=0; k<ftmHealing.getRowCount(); k++) {
            String name = ftmHealing.getValueAt(k, 0).toString();
            String pFile = "participant_" + fightIndex + "_" + k + ".html";
            String newName = "<A HREF=\"" + pFile + "\">" + name + "</A>";
            ftmHealing.setValueAt(newName, k, 0);
        }
        
        SortFilterModel sorter = new SortFilterModel(ftmDamage);
        sorter.sort(1);        
        SortFilterModel sorter2 = new SortFilterModel(ftmHealing);
        sorter2.sort(1);
        w.write("<TABLE>");
        w.write("<TD>");
        writeTable(w, sorter, "MYTABLE");
        w.write("</TD>");
        w.newLine();
        w.write("<TD>");
        writeTable(w, sorter2, "MYTABLEH");
        w.write("</TD>");
        w.write("</TABLE>");
    }

    public void writeParticipantDamageTable(BufferedWriter w, Fight f, FightParticipant p) throws IOException {
            TableModel tm = new ParticipantDamageTableModel(f, p);
            writeTable(w, tm, "MYTABLE");
    }

    public void writeParticipantHealingTable(BufferedWriter w, Fight f, FightParticipant p) throws IOException {
            TableModel tm = new ParticipantHealingTableModel(f, p);
            writeTable(w, tm, "MYTABLEH");
    }

    public void writeParticipantPowerTable(BufferedWriter w, Fight f, FightParticipant p) throws IOException {
            TableModel tm = new ParticipantPowerTableModel(f, p);
            writeTable(w, tm, "MYTABLEP");
    }

    public void writeTable(BufferedWriter w, TableModel tm, String tableClass) throws IOException {
        int k, l;
        int numCols = tm.getColumnCount();
        int numRows = tm.getRowCount();

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
                w.write("<TD CLASS=\"" + tableClass + "\">");
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
    
    public void makeStyle(File dir) throws IOException, NullPointerException {
        BufferedReader styleReader = null;
        BufferedWriter styleWriter = null;
        try {
            styleReader = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(styleResourceName)));
            File styleFile = new File(dir.getAbsolutePath(), styleFileName);
            styleWriter = new BufferedWriter(new FileWriter(styleFile));
            String line;
            while ((line = styleReader.readLine()) != null) {
                styleWriter.write(line);
                styleWriter.newLine();
            }
        } finally {
            FileHelper.close(styleReader);
            FileHelper.close(styleWriter);
        }
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
