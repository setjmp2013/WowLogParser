/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wowlogparserbase.events;

import org.w3c.dom.Element;
import wowlogparserbase.Constants;
import wowlogparserbase.StringDatabase;
import wowlogparserbase.xmlbindings.fight.SpellInfoEventXmlType;

/**
 *
 * @author racy
 */
public class SpellInfoEvent extends BasicEvent implements SkillInterface {
    private int spellID = 0;
    private String spellName = null;
    private int school = 0;

    public String toString() {
        String s = super.toString();
        s = s + "SpellID = " + spellID + newLine;
        s = s + "Spell Name = " + spellName + newLine;
        s = s + "School = " + getSchoolStringFromFlags(school) + newLine;
        return s;
    }
    
    public Element addXmlAttributes(Element e) {
        super.addXmlAttributes(e);
        e.setAttribute(XMLTAG_SPELL_ID, ""+getSkillID());
        e.setAttribute(XMLTAG_SPELL_NAME, getSkillName());
        e.setAttribute(XMLTAG_SPELL_SCHOOL, getSchoolStringFromFlags(getSchool()));
        return e;
    }

    public void addXmlAttributesJaxb(Object o) {
        super.addXmlAttributesJaxb(o);
        if (o instanceof SpellInfoEventXmlType) {
            SpellInfoEventXmlType o2 = (SpellInfoEventXmlType) o;
            o2.setSpid(getSkillID());
            o2.setSpna(getSkillName());
            o2.setSpsc(getSchoolStringFromFlags(getSchool()));
        }
    }

    @Override
    public void findOldStrings() {
        super.findOldStrings();
        StringDatabase db = StringDatabase.getInstance();
        spellName = db.processString(spellName);
    }

    public int getSchool() {
        return school;
    }

    public void setSchool(int school) {
        this.school = school;
    }

    public int getSkillID() {
        return spellID;
    }

    public void setSkillID(int spellID) {
        this.spellID = spellID;
    }

    public String getSkillName() {
        return spellName;
    }

    public void setSkillName(String spellName) {
        this.spellName = removeFnutt(spellName);
    }

    @Override
    public int getPowerType() {
        return Constants.POWER_TYPE_UNKNOWN;
    }
    
    
}
