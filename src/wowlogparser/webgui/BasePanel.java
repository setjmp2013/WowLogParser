/*
 * BasePanel.java
 *
 * Created on den 23 maj 2008, 20:09
 */

package wowlogparser.webgui;

import wowlogparserbase.helpers.WorkerDialog;
import wowlogparserbase.helpers.WorkerProgressDialog;
import wowlogparserbase.helpers.Worker;
import java.awt.Cursor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.zip.ZipOutputStream;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import org.jfree.ui.FilesystemFilter;
import wowlogparser.gui.*;
import wowlogparserbase.*;
import wowlogparserbase.helpers.SwingWorkerProgressAndTextDialog;
import wowlogparserbase.helpers.SwingWorkerProgressDialog;
import wowlogparserbase.xml.ReportJaxb;
import wowlogparserbase.xml.XMLReport;

/**
 *
 * @author  racy
 */
public class BasePanel extends javax.swing.JPanel {

    Preferences basePrefs = Preferences.userRoot().node("wowlogparser").node("webgui");
    
    File logFile = null;
    BaseFrame owner = null;
    FileLoader fl = null;
    List<Fight> fights = null;
    List<Fight> bossFights = null;
    
    static final String englishString = "English";
    static final String germanString = "German";
    static final String frenchString = "French";
    static final String spanishString = "Spanish";
    
    /** Creates new form BasePanel */
    public BasePanel() {
        SettingsSingleton s = SettingsSingleton.getInstance();
        s.setAutoPlayerPets(true);
        s.setOnlyMeAndRaid(false);
        s.setPetsAsOwner(false);
        s.setMaxInactiveTime(120);
        initComponents();
        initLanguageBox();
        loadSettings();
        clear();
    }

    public void setOwner(wowlogparser.webgui.BaseFrame owner) {
        this.owner = owner;
    }    

    final void initLanguageBox() {
        ComboBoxModel model = new DefaultComboBoxModel(new String[] {englishString, germanString, frenchString});
        jLanguageBox.setModel(model);
        jLanguageBox.setEditable(false);
        jLanguageBox.setSelectedItem(englishString);        
    }

    public String getLanguage() {
        return (String)jLanguageBox.getSelectedItem();
    }

    public void applyLanguage() {
        String lanuage = getLanguage();
        if (lanuage.equalsIgnoreCase(englishString)) {
            XmlInfoParser.setLanguageEN();
        }
        if (lanuage.equalsIgnoreCase(germanString)) {
            XmlInfoParser.setLanguageDE();
        }
        if (lanuage.equalsIgnoreCase(frenchString)) {
            XmlInfoParser.setLanguageFR();
        }
        if (lanuage.equalsIgnoreCase(spanishString)) {
            XmlInfoParser.setLanguageES();
        }
    }

    public final void loadSettings() {
        String languageString = basePrefs.get("language", englishString);
        jLanguageBox.setSelectedItem(languageString);
    }
    
    public void saveSettings() {
        String languageString = (String)jLanguageBox.getSelectedItem();
        basePrefs.put("language", languageString);
    }
    
    final void clear() {
        fl = null;
        fights = null;
        bossFights = null;
        jFightTable.setModel(new AlmostDefaultTableModel());
    }
    
    void parse() {
        jMakeButton.setEnabled(false);
        clear();
        
        applyLanguage();

        //Parse file and make fights
        FileParseThread p = new FileParseThread();
        SwingWorkerProgressAndTextDialog d = new SwingWorkerProgressAndTextDialog(p, owner, "Parsing log file");
        d.setLocationRelativeTo(this);
        d.setVisible(true);
        Object result = d.getResult();
        if (result != null) {
            fl = (FileLoader) result;
        } else {
            JOptionPane.showMessageDialog(this, "Error when parsing", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (fl.getNumErrors() > 0) {
            JOptionPane.showMessageDialog(this, "Error parsing " + fl.getNumErrors() + " lines", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        d.dispose();        
        fights = fl.getFightCollection().getFights();
        
        //Boss fights
        BossParsing bp = new BossParsing(owner, fights, fl);
        bp.doAutomaticBossParsing();
        bossFights = bp.getOutFights();
        Fight merged = Fight.merge(bossFights);
        bossFights.add(merged);

        updateTables();
    }

    File workerFile;
    
    void makePackage() {
        String dirS = basePrefs.get("packagePath", "");
        File dir = new File(dirS);
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(dir);
        fc.setFileFilter(new ZipFileFilter());
        int ret = fc.showSaveDialog(this);
        
        if (ret == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            dir = f.getParentFile();
            FileFilter ff = fc.getFileFilter();
            if (ff instanceof ZipFileFilter) {
                ZipFileFilter zff = (ZipFileFilter) ff;
                f = zff.makeAutomaticExtension(f);
            }
            basePrefs.put("packagePath", dir.getAbsolutePath());

            workerFile = f;
            Worker worker = new Worker() {
                @Override
                public void run() {
                    super.run();
                    try {
                        ZipOutputStream zOut = ReportJaxb.makeZipOutputStream(workerFile);
                        ReportJaxb report = new ReportJaxb(zOut);
                        report.makeFights(fl, bossFights, "Fights", "report.xml");
                        zOut.close();
                        hideDialog();
                    } catch (FileNotFoundException ex) {
                        JOptionPane.showMessageDialog(owner, "Error creating file", "Error", JOptionPane.ERROR_MESSAGE);
                        hideDialog();
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(owner, "Error writing to file", "Error", JOptionPane.ERROR_MESSAGE);
                        hideDialog();
                    }
                }
            };
            WorkerDialog d = new WorkerDialog(owner, "Making package", true, worker, "Making package, please wait...");
            d.setLocationRelativeTo(this);
            d.setVisible(true);
            JOptionPane.showMessageDialog(this, "Writing package completed", "Done", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    void updateTables() {
        NumberFormat nf = WLPNumberFormat.getInstance();
        nf.setMaximumFractionDigits(0);
        nf.setGroupingUsed(false);
        String[] columns = {"Name", "Time", "Active Duration"};
        
        AlmostDefaultTableModel tm = new AlmostDefaultTableModel();
        tm.setColumnIdentifiers(columns);
        for (Fight f : bossFights) {
            String[] row = {f.getName(), f.getStartEvent().getTimeString(), nf.format(f.getActiveDuration())};
            tm.addRow(row);
        }
        jFightTable.setModel(tm);

    }
    
    Fight mergeFights(List<Fight> fs) {
        Cursor c = getCursor();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        FightMergeThread mt = new FightMergeThread(fs);
        WorkerProgressDialog wd = new WorkerProgressDialog(owner, "Merging fights", true, mt, "Please wait, merging fights...");
        wd.setLocationRelativeTo(this);
        wd.setVisible(true);
        Fight mergedFight = mt.getMergedFight();
        setCursor(c);
        return mergedFight;
    }
    
    class AlmostDefaultTableModel extends DefaultTableModel {

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
        
    }

    class FileParseThread extends SwingWorker {
        public FileParseThread() {
        }

        @Override
        protected Object doInBackground() throws Exception {
            if (logFile != null) {
                firePropertyChange("text", null, "Parsing logfile...\n");
                FileLoader flTemp = new FileLoader(logFile);
                boolean ret = flTemp.parse();
                if (ret) {
                    setProgress(50);
                    firePropertyChange("text", null, "Creating fights...\n");
                    flTemp.createFights();
                    EventCollection ec = flTemp.getEventCollection();
                    AbstractEventCollection.makeOverhealing(ec.getEvents(), 60);
                    setProgress(100);
                    return flTemp;
                } else {
                    return null;
                }
            }
            return null;
        }

        @Override
        protected void done() {
            super.done();
        }
        
    }
    
    class ZipFileFilter extends FileFilter {

        public File makeAutomaticExtension(File f) {
            File dir = f.getParentFile();
            String fName = f.getName();
            String fNameLower = fName.toLowerCase();
            if (!fNameLower.endsWith(".zip")) {
                int dotIndex = fNameLower.lastIndexOf('.');
                if (dotIndex == -1) {
                    fName = fName + ".zip";
                }
            }
            f = new File(dir, fName);
            return f;
        }

        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            if (f.getName().endsWith(".zip")) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String getDescription() {
            return "zip files (*.zip)";
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jFightTable = new javax.swing.JTable();
        jLoadButton = new javax.swing.JButton();
        jMakeButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        jLanguageBox = new javax.swing.JComboBox();

        jTabbedPane1.setName("jTabbedPane1"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        jFightTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jFightTable.setName("jFightTable"); // NOI18N
        jScrollPane1.setViewportView(jFightTable);

        jTabbedPane1.addTab("Boss fights", jScrollPane1);

        jLoadButton.setText("Load log file");
        jLoadButton.setName("jLoadButton"); // NOI18N
        jLoadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadButton(evt);
            }
        });

        jMakeButton.setText("Make package");
        jMakeButton.setEnabled(false);
        jMakeButton.setName("jMakeButton"); // NOI18N
        jMakeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                makePackageButton(evt);
            }
        });

        jSeparator1.setName("jSeparator1"); // NOI18N

        jLanguageBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jLanguageBox.setName("jLanguageBox"); // NOI18N
        jLanguageBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                languageChanged(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1034, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .add(23, 23, 23)
                .add(jLoadButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 130, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(71, 71, 71)
                .add(jLanguageBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 101, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(709, 709, 709))
            .add(jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1034, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .add(24, 24, 24)
                .add(jMakeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 130, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(880, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(22, 22, 22)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLoadButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 70, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLanguageBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jMakeButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 70, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 146, Short.MAX_VALUE)
                .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 426, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void loadButton(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadButton
        String dirString = basePrefs.get("fileDir", "");
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File(dirString));

        int retVal = fc.showOpenDialog(this);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            logFile = fc.getSelectedFile();
            File dir = new File("");
            if (logFile.isFile()) {
                if (logFile.getParentFile().isDirectory()) {
                    dir = logFile.getParentFile();
                }
            }
            basePrefs.put("fileDir", dir.getAbsolutePath());
            parse();
            if (bossFights != null && fights != null) {
                jMakeButton.setEnabled(true);
                this.owner.setTitle("Log file: " + logFile.getName());
            } else {
                jMakeButton.setEnabled(false);
            }
        }
}//GEN-LAST:event_loadButton

private void languageChanged(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_languageChanged
    saveSettings();
}//GEN-LAST:event_languageChanged

private void makePackageButton(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_makePackageButton
    makePackage();
}//GEN-LAST:event_makePackageButton


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable jFightTable;
    private javax.swing.JComboBox jLanguageBox;
    private javax.swing.JButton jLoadButton;
    private javax.swing.JButton jMakeButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTabbedPane jTabbedPane1;
    // End of variables declaration//GEN-END:variables

}
