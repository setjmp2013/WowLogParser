/*
 * SettingsDialog.java
 *
 * Created on den 14 maj 2008, 19:26
 */

package wowlogparser.gui;

import java.text.NumberFormat;
import java.util.prefs.Preferences;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.event.ListDataListener;
import wowlogparserbase.SettingsSingleton;
import wowlogparserbase.XmlInfoParser;

/**
 *
 * @author  racy
 */
public class SettingsPanel extends javax.swing.JPanel {

    final WowVersion version200 = new WowVersion("2.0.x - 2.x.x (BC)", 200);
    final WowVersion version300 = new WowVersion("3.0.x - 3.1.x (WOTLK)", 300);
    final WowVersion version320 = new WowVersion("3.2.x (WOTLK)", 320);
    final WowVersion version330 = new WowVersion("3.3 PTR (WOTLK)", 330);
    final WowVersion version332 = new WowVersion("3.3.1 and 3.3.2 (WOTLK)", 332);
    final WowVersion version401 = new WowVersion("4.0.1 (Cataclysm)", 401);
    final WowVersion version420 = new WowVersion("4.2.0 (Cataclysm)", 420);

    JDialog owner;
    Preferences prefs;
    
    public static final String englishString = "English";
    public static final String germanString = "German";
    public static final String frenchString = "French";
    public static final String spanishString = "Spanish";
    
    /** Creates new form SettingsDialog */
    public SettingsPanel(JDialog owner, Preferences prefs) {
        this.owner = owner;
        this.prefs = prefs;
        initComponents();

        initLanguageComboBox();
        initWowVersionComboBox();
        
        loadSettings();
        applyLanguage();
    }

    private void applySettings() {
        SettingsSingleton s = SettingsSingleton.getInstance();
        s.setAutoBossFights(getAutoBossFights());
        s.setAutoPlayerPets(getAutoPlayerPets());
        s.setAllowNeutralNpcs(getAllowNeutralNpcs());
        s.setOnlyDeadMobs(getOnlyDeadMobs());
        s.setOnlyMeAndRaid(getOnlyMeAndMyRaid());
        s.setPetsAsOwner(getPetsAsOwner());
        s.setMaxInactiveTime(getMaxInactiveTime());
        s.setSplinePlots(getSplinePlots());
        WowVersion version = (WowVersion)jComboBoxWowVersion.getSelectedItem();
        s.setWowVersion(version.getVersion());
    }

    private void initWowVersionComboBox() {
        jComboBoxWowVersion.removeAllItems();
        jComboBoxWowVersion.addItem(version200);
        jComboBoxWowVersion.addItem(version300);
        jComboBoxWowVersion.addItem(version320);
        jComboBoxWowVersion.addItem(version330);
        jComboBoxWowVersion.addItem(version332);
        jComboBoxWowVersion.addItem(version401);
        jComboBoxWowVersion.addItem(version420);
    }

    private void initLanguageComboBox() {
        ComboBoxModel model = new DefaultComboBoxModel(new String[] {englishString, germanString, frenchString, spanishString});
        jLanguageComboBox.setModel(model);
        jLanguageComboBox.setEditable(false);
        jLanguageComboBox.setSelectedItem(englishString);
    }
    
    public void loadSettings() {
        setSplinePlots(prefs.getBoolean("splinePlots", false));
        setOnlyDeadMobs(prefs.getBoolean("onlyDeadMobs", false));
        setOnlyMeAndMyRaid(prefs.getBoolean("onlyMeAndRaid", false));
        setPetsAsOwner(prefs.getBoolean("petsAsOwner", false));
        setAutoPlayerPets(prefs.getBoolean("autoPlayerPets", true));
        setAutoBossFights(prefs.getBoolean("autoBossFights", false));
        setAllowNeutralNpcs(prefs.getBoolean("neutralNpcs", false));
        setMaxInactiveTime(prefs.getDouble("maxInactiveTime", 120));
        String languageString = prefs.get("language", englishString);
        jLanguageComboBox.setSelectedItem(languageString);
        jComboBoxWowVersion.setSelectedItem(new WowVersion("", prefs.getInt("wowVersion", 420)));
        applySettings();
    }
    
    public void saveSettings() {
        prefs.putBoolean("splinePlots", getSplinePlots());
        prefs.putBoolean("onlyDeadMobs", getOnlyDeadMobs());
        prefs.putBoolean("onlyMeAndRaid", getOnlyMeAndMyRaid());
        prefs.putBoolean("petsAsOwner", getPetsAsOwner());
        prefs.putBoolean("autoPlayerPets", getAutoPlayerPets());
        prefs.putBoolean("autoBossFights", getAutoBossFights());
        prefs.putBoolean("neutralNpcs", getAllowNeutralNpcs());
        prefs.putDouble("maxInactiveTime", getMaxInactiveTime());
        String languageString = (String)jLanguageComboBox.getSelectedItem();
        prefs.put("language", languageString);
        WowVersion version = (WowVersion)jComboBoxWowVersion.getSelectedItem();
        prefs.putInt("wowVersion", version.getVersion());
        applySettings();
    }
    
    public String getLanguage() {
        return (String)jLanguageComboBox.getSelectedItem();
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
    
    public boolean getOnlyMeAndMyRaid() {
        return jOnlyMeAndRaidCheckBox.isSelected();
    }
    
    public boolean getOnlyDeadMobs() {
        return jOnlyDeadMobsCheckBox.isSelected();
    }
    
    public boolean getPetsAsOwner() {
        return jPetsAsOwnerCheckBox.isSelected();
    }

    public void setOnlyMeAndMyRaid(boolean b) {
        jOnlyMeAndRaidCheckBox.setSelected(b);
    }
    
    public void setOnlyDeadMobs(boolean b) {
        jOnlyDeadMobsCheckBox.setSelected(b);
    }
        
    public void setPetsAsOwner(boolean b) {
        jPetsAsOwnerCheckBox.setSelected(b);
    }

    public boolean getAutoBossFights() {
        return jAutoBossFightsCheckBox.isSelected();
    }

    public void setAutoBossFights(boolean b) {
        this.jAutoBossFightsCheckBox.setSelected(b);
    }

    public boolean getAutoPlayerPets() {
        return jAutoPlayerPetsCheckBox.isSelected();
    }

    public void setAutoPlayerPets(boolean b) {
        this.jAutoPlayerPetsCheckBox.setSelected(b);
    }

    public boolean getAllowNeutralNpcs() {
        return jCheckBoxAllowNeutralNpcs.isSelected();
    }

    public void setAllowNeutralNpcs(boolean b) {
        jCheckBoxAllowNeutralNpcs.setSelected(b);
    }

    public void setMaxInactiveTime(double time) {
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setGroupingUsed(false);
        nf.setMaximumFractionDigits(2);
        jTextFieldMaxInactiveTime.setText(nf.format(time));
    }

    public double getMaxInactiveTime() {
        double num = 120;
        try {
            num = Double.parseDouble(jTextFieldMaxInactiveTime.getText());
        } catch(NumberFormatException ex) {
            setMaxInactiveTime(num);
        }
        return num;
    }

    public boolean getSplinePlots() {
        return jCheckBoxSplinePlots.isSelected();
    }

    public void setSplinePlots(boolean splinePlots) {
        jCheckBoxSplinePlots.setSelected(splinePlots);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jOnlyMeAndRaidCheckBox = new javax.swing.JCheckBox();
        jOnlyDeadMobsCheckBox = new javax.swing.JCheckBox();
        jButton1 = new javax.swing.JButton();
        jLanguageComboBox = new javax.swing.JComboBox();
        jPetsAsOwnerCheckBox = new javax.swing.JCheckBox();
        jAutoPlayerPetsCheckBox = new javax.swing.JCheckBox();
        jAutoBossFightsCheckBox = new javax.swing.JCheckBox();
        jCheckBoxAllowNeutralNpcs = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        jTextFieldMaxInactiveTime = new javax.swing.JTextField();
        jCheckBoxSplinePlots = new javax.swing.JCheckBox();
        jComboBoxWowVersion = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();

        jOnlyMeAndRaidCheckBox.setSelected(true);
        jOnlyMeAndRaidCheckBox.setText("Only me and my raid (may remove some relevant events as well)");
        jOnlyMeAndRaidCheckBox.setName("jOnlyMeAndRaidCheckBox"); // NOI18N

        jOnlyDeadMobsCheckBox.setText("Only dead mobs");
        jOnlyDeadMobsCheckBox.setName("jOnlyDeadMobsCheckBox"); // NOI18N

        jButton1.setText("Ok");
        jButton1.setName("jButton1"); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okPressed(evt);
            }
        });

        jLanguageComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jLanguageComboBox.setName("jLanguageComboBox"); // NOI18N

        jPetsAsOwnerCheckBox.setText("Assign mob pets to the mob itself to prevent \"pet fights spam\"");
        jPetsAsOwnerCheckBox.setName("jPetsAsOwnerCheckBox"); // NOI18N

        jAutoPlayerPetsCheckBox.setText("Automatically assign player pets on load");
        jAutoPlayerPetsCheckBox.setName("jAutoPlayerPetsCheckBox"); // NOI18N

        jAutoBossFightsCheckBox.setText("Automatically parse boss fights on load");
        jAutoBossFightsCheckBox.setName("jAutoBossFightsCheckBox"); // NOI18N

        jCheckBoxAllowNeutralNpcs.setText("Allow fights vs Neutral NPCs (might give strange fights)");
        jCheckBoxAllowNeutralNpcs.setName("jCheckBoxAllowNeutralNpcs"); // NOI18N

        jLabel1.setText("Max inactive time before a fight is closed");
        jLabel1.setName("jLabel1"); // NOI18N

        jTextFieldMaxInactiveTime.setText("120");
        jTextFieldMaxInactiveTime.setName("jTextFieldMaxInactiveTime"); // NOI18N

        jCheckBoxSplinePlots.setText("Spline interpolation on plots");
        jCheckBoxSplinePlots.setName("jCheckBoxSplinePlots"); // NOI18N

        jComboBoxWowVersion.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBoxWowVersion.setName("jComboBoxWowVersion"); // NOI18N

        jLabel2.setText("Wow Version");
        jLabel2.setName("jLabel2"); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jOnlyMeAndRaidCheckBox)
                .addContainerGap(58, Short.MAX_VALUE))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap(310, Short.MAX_VALUE)
                .add(jButton1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 79, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jOnlyDeadMobsCheckBox)
                .addContainerGap(290, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jPetsAsOwnerCheckBox)
                .addContainerGap(70, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jAutoPlayerPetsCheckBox)
                .addContainerGap(176, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jAutoBossFightsCheckBox)
                .addContainerGap(180, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel1)
                .addContainerGap(194, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jCheckBoxAllowNeutralNpcs)
                .addContainerGap(102, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jTextFieldMaxInactiveTime, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 84, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(305, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLanguageComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 132, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(257, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jCheckBoxSplinePlots)
                .addContainerGap(236, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLabel2)
                .addContainerGap(327, Short.MAX_VALUE))
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jComboBoxWowVersion, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 145, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(244, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(25, 25, 25)
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jComboBoxWowVersion, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jCheckBoxSplinePlots)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jOnlyMeAndRaidCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jOnlyDeadMobsCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPetsAsOwnerCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jAutoPlayerPetsCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jAutoBossFightsCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jCheckBoxAllowNeutralNpcs)
                .add(7, 7, 7)
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jTextFieldMaxInactiveTime, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jLanguageComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 31, Short.MAX_VALUE)
                .add(jButton1)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

private void okPressed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okPressed
    saveSettings();
    applyLanguage();
    applySettings();
    owner.setVisible(false);
}//GEN-LAST:event_okPressed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jAutoBossFightsCheckBox;
    private javax.swing.JCheckBox jAutoPlayerPetsCheckBox;
    private javax.swing.JButton jButton1;
    private javax.swing.JCheckBox jCheckBoxAllowNeutralNpcs;
    private javax.swing.JCheckBox jCheckBoxSplinePlots;
    private javax.swing.JComboBox jComboBoxWowVersion;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JComboBox jLanguageComboBox;
    private javax.swing.JCheckBox jOnlyDeadMobsCheckBox;
    private javax.swing.JCheckBox jOnlyMeAndRaidCheckBox;
    private javax.swing.JCheckBox jPetsAsOwnerCheckBox;
    private javax.swing.JTextField jTextFieldMaxInactiveTime;
    // End of variables declaration//GEN-END:variables

}

class WowVersion {

    String versionStr;
    int version;

    public WowVersion(String versionStr, int version) {
        this.versionStr = versionStr;
        this.version = version;
    }

    public int getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return versionStr;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WowVersion other = (WowVersion) obj;
        if (this.version != other.version) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.version;
        return hash;
    }
}
