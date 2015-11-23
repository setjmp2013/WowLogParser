
package wowlogparserbase.events;

import org.w3c.dom.Element;
import wowlogparserbase.Constants;
import wowlogparserbase.StringDatabase;

/**
 *
 * @author racy
 */
public class PowerEvent extends BasicEvent implements SkillInterface, AmountInterface {

    private int spellID = 0;
    private String spellName = null;
    private int school = 0;
    private int amount = 0;
    private int powerType = Constants.POWER_TYPE_UNKNOWN;
    
    public PowerEvent() {
        
    }

    @Override
    public int parse(String timeDate, String[] values) {
        int index = super.parse(timeDate, values);
        if(index < 0) {
            return index;
        }
        //spellId, spellName, spellSchool, amount, powerType, extraAmount        
        setSkillID(Integer.parseInt(values[index + 0]));
        setSkillName(values[index + 1]);
        setSchool(intParse(values[index + 2]));
        setAmount(Integer.parseInt(values[index + 3]));
        setPowerType(Integer.parseInt(values[index + 4]));

        return index + 5;
    }

    public int getSkillID() {
        return spellID;
    }

    public String getSkillName() {
        return spellName;
    }

    public int getSchool() {
        return school;
    }

    public int getPowerType() {
        return powerType;
    }

    public int getAmount() {
        return amount;
    }
    
    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setPowerType(int powerType) {
        this.powerType = powerType;
    }

    public void setSchool(int school) {
        this.school = school;
    }

    public void setSkillID(int spellID) {
        this.spellID = spellID;
    }

    public void setSkillName(String spellName) {
        this.spellName = removeFnutt(spellName);
    }

    public String toString() {
        String s = super.toString();
        s = s + "SpellID = " + spellID + newLine;
        s = s + "Spell Name = " + spellName + newLine;
        s = s + "School = " + getSchoolStringFromFlags(school) + newLine;
        s = s + "Amount = " + amount + newLine;
        s = s + "Power Type = " + getPowerTypeString(powerType) + newLine;
        return s;
    }
    
    public Element addXmlAttributes(Element e) {
        super.addXmlAttributes(e);
        e.setAttribute(XMLTAG_SPELL_ID, ""+spellID);
        e.setAttribute(XMLTAG_SPELL_NAME, spellName);
        e.setAttribute(XMLTAG_SPELL_SCHOOL, getSchoolStringFromFlags(school));
        e.setAttribute(XMLTAG_AMOUNT, ""+amount);
        e.setAttribute(XMLTAG_POWER_TYPE, getPowerTypeString(powerType));
        return e;
    }

    @Override
    public void findOldStrings() {
        super.findOldStrings();
        StringDatabase db = StringDatabase.getInstance();
        spellName = db.processString(spellName);

    }
}
