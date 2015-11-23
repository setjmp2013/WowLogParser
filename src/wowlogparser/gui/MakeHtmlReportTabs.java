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
import wowlogparserbase.StringCallback;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import wowlogparser.*;
import wowlogparserbase.Version;
import wowlogparserbase.html.HtmlMaker;

public class MakeHtmlReportTabs {


    // List of fights with assigned pets still there.
    protected List<Fight> list;
    Preferences basePrefs;
    JFrame owner;

    boolean mergeAndIndividual;
    boolean merge;

    private HtmlMaker htmlMaker;

    /**
     * Constructor
     * @param owner
     * @param list List of fights with assigned pets still there.
     * @param basePrefs
     * @param merge
     */
    public MakeHtmlReportTabs(JFrame owner, List<Fight> list, Preferences basePrefs, boolean merge) {
        this.merge = merge;
        this.list = list;
        this.basePrefs = basePrefs;
        this.owner = owner;
        this.mergeAndIndividual = false;
        htmlMaker = new HtmlMaker(owner, Version.getVersion());
    }
    
    /**
     * Constructor
     * @param owner
     * @param list List of fights with assigned pets still there.
     * @param basePrefs
     * @param merge
     * @param mergeAndIndividual
     */
    public MakeHtmlReportTabs(JFrame owner, List<Fight> list, Preferences basePrefs, boolean merge, boolean mergeAndIndividual) {
        this.merge = merge;
        this.list = list;
        this.basePrefs = basePrefs;
        this.owner = owner;
        this.mergeAndIndividual = mergeAndIndividual;
        htmlMaker = new HtmlMaker(owner, Version.getVersion());
    }

    List<Fight> getList() {
        return list;
    }

    public class MakeHtmlThread extends Worker implements IntCallback, StringCallback {
        File dir;
        public MakeHtmlThread(File dir) {
            this.dir = dir;
        }

        @Override
        public void run() {
            try {
                if (mergeAndIndividual) {
                    htmlMaker.setMergeAndIndividual(true);
                    dialog.setWorkerText("Making individual fights...");
                    htmlMaker.makeIndividual(list, dir, MakeHtmlThread.this, MakeHtmlThread.this);

                    dialog.setWorkerText("Making merged fights...");
                    htmlMaker.makeMerge(list, dir, MakeHtmlThread.this, MakeHtmlThread.this);
                    htmlMaker.makeFrontHtml(dir);
                } else {
                    htmlMaker.setMergeAndIndividual(false);
                    if (merge) {
                        dialog.setWorkerText("Making merged fights...");
                        htmlMaker.makeMerge(list, dir, MakeHtmlThread.this, MakeHtmlThread.this);
                    } else {
                        dialog.setWorkerText("Making individual fights...");
                        htmlMaker.makeIndividual(list, dir, MakeHtmlThread.this, MakeHtmlThread.this);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(owner, "Something went wrong when writing the html files: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            hideDialog();
        }

        public void reportInt(int theInt) {
            int val = theInt;
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
        MakeHtmlTabsSettingsDialog d = new MakeHtmlTabsSettingsDialog(owner, true, basePrefs);
        d.setLocationRelativeTo(owner);
        d.setVisible(true);
        if (d.isSuccessful()) {
            File dir = new File(d.getOutputDirectory());
            htmlMaker.setHeadNote(d.getNote());
            htmlMaker.setMakePlayerGraph(d.isPlayerGraphs());
            htmlMaker.setMakeRaidGraph(d.isRaidGraphs());
            htmlMaker.setMakeRaidGraphMulti(d.isRaidGraphsMulti());
            htmlMaker.setChartHeight(d.getChartHeight());
            htmlMaker.setChartWidth(d.getChartWidth());
            htmlMaker.setCustomCss(d.isCustomCss());
            htmlMaker.setCustomCssFilename(d.getCustomCssFilename());
            MakeHtmlThread ht = new MakeHtmlThread(dir);
            WorkerProgressDialog dia = new WorkerProgressDialog(owner, "Making HTML", true, ht, "Making HTML files, please wait...");
            dia.setLocationRelativeTo(owner.getContentPane());
            dia.setVisible(true);
        }
    }
    

}
