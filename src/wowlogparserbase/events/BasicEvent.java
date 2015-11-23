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

package wowlogparserbase.events;

import java.lang.reflect.Method;
import org.w3c.dom.Element;
import wowlogparserbase.Constants;
import wowlogparserbase.SettingsSingleton;
import wowlogparserbase.StringDatabase;
import wowlogparserbase.xmlbindings.fight.BasicEventXmlType;

/**
 * The basic event that all Wow combat log events inherits from. It inherits from LogEvent,
 * an even more basic class that only saves the time and date.
 * @author racy
 */
public abstract class BasicEvent extends LogEvent {

    private String sourceGUID = null;
    private String sourceName = null;
    private long sourceFlags = 0;
    private long sourceFlags2 = 0;
    private String destinationGUID = null;
    private String destinationName = null;
    private long destinationFlags = 0;
    private long destinationFlags2 = 0;
    private String logType = null;

    /**
     * Default constructor
     */
    public BasicEvent() {
        
    }
        
    /**
     * Parse a log line
     * @param timeDate
     * @param values
     * @return
     */
    public int parse(String timeDate, String[] values) {
        int index = super.parse(timeDate, values);
        if (index < 0) {
            return index;
        }
                
        int version = SettingsSingleton.getInstance().getWowVersion();
        if (version < 420) {
            if (values.length < index + 7) {
                return -1;
            }
            setLogType(values[index + 0]);
            setSourceGUID(values[index + 1]);
            setSourceName(values[index + 2]);
            setSourceFlags(longParse(values[index + 3]));
            setDestinationGUID(values[index + 4]);
            setDestinationName(values[index + 5]);
            setDestinationFlags(longParse(values[index + 6]));
            return index + 7;
        } else {
            if (values.length < index + 9) {
                return -1;
            }
            setLogType(values[index + 0]);
            setSourceGUID(values[index + 1]);
            setSourceName(values[index + 2]);
            setSourceFlags(longParse(values[index + 3]));
            setSourceFlags2(longParse(values[index + 4]));
            setDestinationGUID(values[index + 5]);
            setDestinationName(values[index + 6]);
            setDestinationFlags(longParse(values[index + 7]));
            setDestinationFlags2(longParse(values[index + 8]));
            return index + 9;
        }                
    }
    
    @Override
    public void findOldStrings() {
        super.findOldStrings();
        StringDatabase db = StringDatabase.getInstance();
        setSourceGUID(db.processString(getSourceGUID()));
        setDestinationGUID(db.processString(getDestinationGUID()));
        setSourceName(db.processString(getSourceName()));
        setDestinationName(db.processString(getDestinationName()));
        setLogType(db.processString(getLogType()));
    }
    
    /**
     * A static method for getting spell school indices from spell school flags
     * @param schoolFlags
     * @return An array of indices
     */
    private static int[] getSchoolIndices(int schoolFlags) {
        int k;
        int[] index = new int[Constants.schools.length];
        int nrIndex = 0;
        for (k=0; k<Constants.schools.length; k++) {
            if ((schoolFlags & Constants.schools[k]) > 0) {
                index[nrIndex] = k;
                nrIndex++;
            }
        }
        int[] outIndex = new int[nrIndex];
        for (k=0; k<nrIndex; k++) {
            outIndex[k] = index[k];
        }
        return outIndex;
    }
    
    /**
     * Get the description for a certain spell school
     * @param schoolFlags The school flags
     * @return A string with the description
     */
    public static String getSchoolStringFromFlags(int schoolFlags) {
        //Special check for frostfire
        if ( ((schoolFlags&Constants.SCHOOL_FIRE)!=0) && ((schoolFlags&Constants.SCHOOL_FROST)!=0) ) {
            return "Frostfire";
        }
        else if (((schoolFlags&Constants.SCHOOL_FROST)!=0) && ((schoolFlags&Constants.SCHOOL_NATURE)!=0)) {
            return "Froststorm";
        }
        else if (((schoolFlags&Constants.SCHOOL_SHADOW)!=0) && ((schoolFlags&Constants.SCHOOL_NATURE)!=0)) {
            return "Shadowstorm";
        }
        else {
            int[] inds = getSchoolIndices(schoolFlags);
            if(inds.length > 0) {
                return Constants.schoolStrings[inds[0]];
            } else {
                return "Unknown";
            }
        }
    }
     
    /**
     * Get a description of a power type.
     * @param powerType The power type
     * @return A string with the description
     */
    public static String getPowerTypeString(int powerType) {
        //powerType
        //0 = mana
        //1 = rage
        //2 = focus
        //3 = energy
        //4 = pethappiness
        //5 = runes
        //-2 = health
        String s = "Unknown";
        switch (powerType) {
            case Constants.POWER_TYPE_MANA:
                s = "Mana";
                break;
            case Constants.POWER_TYPE_RAGE:
                s = "Rage";
                break;
            case Constants.POWER_TYPE_FOCUS:
                s = "Focus";
                break;
            case Constants.POWER_TYPE_ENERGY:
                s = "Energy";
                break;
            case Constants.POWER_TYPE_PETHAPPINESS:
                s = "Pet Happiness";
                break;
            case Constants.POWER_TYPE_RUNES:
                s = "Runes";
                break;
            case Constants.POWER_TYPE_RUNIC_POWER:
                s = "Runic Power";
                break;
            case Constants.POWER_TYPE_HEALTH:
                s = "Health";
                break;
            case Constants.POWER_TYPE_UNKNOWN:
                s = "Unknown";
                break;

        }
        return s;
    }

    public static boolean isBuffString(String s) {
        if (s == null) {
            return false;
        }
        if (s.toLowerCase().equals("buff")) {
            return true;
        } else {
            return false;
        }        
    }
    
    public static boolean isDebuffString(String s) {
        if (s == null) {
            return false;
        }
        if (s.toLowerCase().equals("debuff")) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Convenience function to parse flags
     * @param flags The flags to check
     * @return true or false
     */
    public static boolean isReactionFriendly(long flags) {
        if ((flags&Constants.FLAGS_OBJECT_REACTION_FRIENDLY) != 0) {
            return true;
        }
        return false;
    }
    
    /**
     * Convenience function to parse flags
     * @param flags The flags to check
     * @return true or false
     */
    public static boolean isReactionHostile(long flags) {
        if ((flags&Constants.FLAGS_OBJECT_REACTION_HOSTILE) != 0) {
            return true;
        }
        return false;
    }

    /**
     * Convenience function to parse flags
     * @param flags The flags to check
     * @return true or false
     */
    public static boolean isReactionNeutral(long flags) {
        if ((flags&Constants.FLAGS_OBJECT_REACTION_NEUTRAL) != 0) {
            return true;
        }
        return false;
    }

    /**
     * Convenience function to parse flags
     * @param flags The flags to check
     * @return true or false
     */
    public static boolean isAffiliationMine(long flags) {
        if ((flags&Constants.FLAGS_OBJECT_AFFILIATION_MINE) != 0) {
            return true;
        }
        return false;
    }

    /**
     * Convenience function to parse flags
     * @param flags The flags to check
     * @return true or false
     */
    public static boolean isAffiliationParty(long flags) {
        if ((flags&Constants.FLAGS_OBJECT_AFFILIATION_PARTY) != 0) {
            return true;
        }
        return false;
    }

    /**
     * Convenience function to parse flags
     * @param flags The flags to check
     * @return true or false
     */
    public static boolean isAffiliationRaid(long flags) {
        if ((flags&Constants.FLAGS_OBJECT_AFFILIATION_RAID) != 0) {
            return true;
        }
        return false;
    }

    /**
     * Convenience function to parse flags
     * @param flags The flags to check
     * @return true or false
     */
    public static boolean isAffiliationOutsider(long flags) {
        if ((flags&Constants.FLAGS_OBJECT_AFFILIATION_OUTSIDER) != 0) {
            return true;
        }
        return false;
    }

    /**
     * Convenience function to parse flags
     * @param flags The flags to check
     * @return true or false
     */
    public static boolean isControlPlayer(long flags) {
        if ((flags&Constants.FLAGS_OBJECT_CONTROL_PLAYER) != 0) {
            return true;
        }
        return false;
    }

    /**
     * Convenience function to parse flags
     * @param flags The flags to check
     * @return true or false
     */
    public static boolean isControlNpc(long flags) {
        if ((flags&Constants.FLAGS_OBJECT_CONTROL_NPC) != 0) {
            return true;
        }
        return false;
    }

    /**
     * Convenience function to parse flags
     * @param flags The flags to check
     * @return true or false
     */
    public static boolean isTypePlayer(long flags) {
        if ((flags&Constants.FLAGS_OBJECT_TYPE_PLAYER) != 0) {
            return true;
        }
        return false;
    }

    /**
     * Convenience function to parse flags
     * @param flags The flags to check
     * @return true or false
     */
    public static boolean isTypePet(long flags) {
        if ((flags&Constants.FLAGS_OBJECT_TYPE_PET) != 0) {
            return true;
        }
        return false;
    }

    /**
     * Convenience function to parse flags
     * @param flags The flags to check
     * @return true or false
     */
    public static boolean isTypeNpc(long flags) {
        if ((flags&Constants.FLAGS_OBJECT_TYPE_NPC) != 0) {
            return true;
        }
        return false;
    }

    /**
     * Convenience function to parse flags
     * @param flags The flags to check
     * @return true or false
     */
    public static boolean isTypeObject(long flags) {
        if ((flags&Constants.FLAGS_OBJECT_TYPE_OBJECT) != 0) {
            return true;
        }
        return false;
    }

    /**
     * Convenience function to parse flags
     * @param flags The flags to check
     * @return true or false
     */
    public static boolean isTypeGuardian(long flags) {
        if ((flags&Constants.FLAGS_OBJECT_TYPE_GUARDIAN) != 0) {
            return true;
        }
        return false;
    }

    /**
     * Convenience function to parse flags
     * @param flags The flags to check
     * @return true or false
     */
    public static boolean isFocus(long flags) {
        if ((flags&Constants.FLAGS_OBJECT_FOCUS) != 0) {
            return true;
        }
        return false;
    }

    /**
     * Convenience function to parse flags
     * @param flags The flags to check
     * @return true or false
     */
    public static boolean isMainAssist(long flags) {
        if ((flags&Constants.FLAGS_OBJECT_MAINASSIST) != 0) {
            return true;
        }
        return false;
    }
    
    /**
     * Check if the GUId says that it is a player character
     * @param guid The guid
     * @return true or false
     */
    public static boolean isPlayerGuid(String guid) {
        if (getUnitTypeNumber(guid) == 0) {
            return true;
        }
        return false;
    }

    /**
     * Check if the GUId says that it is a world object
     * @param guid The guid
     * @return true or false
     */
    public static boolean isWorldObjectGuid(String guid) {
        if (getUnitTypeNumber(guid) == 1) {
            return true;
        }
        return false;
    }

    /**
     * Check if the GUId says that it is a pet from a player (not totems)
     * @param guid The guid
     * @return true or false
     */
    public static boolean isPetGuid(String guid) {
        if (getUnitTypeNumber(guid) == 4) {
            return true;
        }
        return false;
    }

    /**
     * Check if the GUId says that it is an NPC (totems from players are also NPC:s)
     * @param guid The guid
     * @return true or false
     */
    public static boolean isNpcGuid(String guid) {
        if (getUnitTypeNumber(guid) == 3) {
            return true;
        }
        return false;
    }

    /**
     * Check if the GUId says that it is an NPC (totems from players are also NPC:s)
     * @param guid The guid
     * @return true or false
     */
    public static boolean isPvpVehicleGuid(String guid) {
        if (getUnitTypeNumber(guid) == 5) {
            return true;
        }
        return false;
    }

    public static boolean isFriendlyPetNpc(String guid, long flags) {
        if (isNpcGuid(guid) && isControlPlayer(flags) && isReactionFriendly(flags) && isTypePet(flags)) {
            return true;
        } else {
            return false;
        }
    }

    public static int getUnitTypeNumber(String guid) {
        String B = guid.substring(4, 5);
        int val = Integer.parseInt(B, 16);
        val = val & 0x07;
        return val;
    }

    /**
     * Get the NPC id if the GUID represents an npc
     * @param guid The guid
     * @return The NPC id or -1 if its not an NPC
     */
    public static int getNpcID(String guid) {
        if (guid.length() > 16) {
            String idString = "";
            int version = SettingsSingleton.getInstance().getWowVersion();
            if (version == 330) {
                idString = guid.substring(5, 9);
            } else if(version >= 400) {
                idString = guid.substring(6, 10);
            } else {
                idString = guid.substring(8, 12);
            }
            try {
                int id = Integer.parseInt(idString, 16);
                return id;
            } catch (NumberFormatException ex) {
                return -1;
            }
        } else {
            return -1;
        }        
    }
    
    /**
     * Set or clear a bit in an integer.
     * @param val The source value to manipulate.
     * @param mask The mask that singles out the interesting bit, for example 0x0002 for the second bit, 0x0004 for the 3rd bit.
     * @param state The new state of the bit.
     * @return The manipulated integer.
     */
    public static int setBooleanBit(int val, int mask, boolean state) {
        if (state) {
            val = val | mask;
        } else {
            val = val & (~mask);        
        }
        return val;
    }
            
    /**
     * Get a bit in an integer.
     * @param val The source value.
     * @param mask The mask that singles out the interesting bit, for example 0x0002 for the second bit, 0x0004 for the 3rd bit.
     * @return The The masked out boolean value.
     */
    public static boolean getBooleanBit(int val, int mask) {
        if ((val&mask) != 0) {
            return true;
        }
        return false;
    }

    public static String removeFnutt(String s) {
        return s.replace("\"", "");
    }
    
    public String toString() {
        String s = super.toString();
        s = s + "SourceGUID = " + getSourceGUID() + newLine;
        s = s + "SourceName = " + getSourceName() + newLine;
        s = s + "sourceFlags = " + getSourceFlags() + newLine;
        s = s + "DestinationGUID = " + getDestinationGUID() + newLine;
        s = s + "DestinationName = " + getDestinationName() + newLine;
        s = s + "DestinationFlags = " + getDestinationFlags() + newLine;
        s = s + "LogType = " + getLogType() + newLine;
        return s;
    }
    
    public Element addXmlAttributes(Element e) {
        e.setAttribute(XMLTAG_SOURCE_NAME,getSourceName());
        e.setAttribute(XMLTAG_SOURCE_GUID,getSourceGUID());
        e.setAttribute(XMLTAG_SOURCE_FLAGS, ""+getSourceFlags());
        e.setAttribute(XMLTAG_DESTINATION_NAME,getDestinationName());
        e.setAttribute(XMLTAG_DESTINATION_GUID,getDestinationGUID());
        e.setAttribute(XMLTAG_DESTINATION_FLAGS, ""+getDestinationFlags());
        e.setAttribute(XMLTAG_LOGTYPE,getLogType());
        return e;
    }
    
    public void addXmlAttributesJaxb(Object o) {
        if (o instanceof BasicEventXmlType) {
            BasicEventXmlType o2 = (BasicEventXmlType) o;

            o2.setSona(getSourceName());
            o2.setSogu(getSourceGUID());
            o2.setSofl(getSourceFlags());
            o2.setDena(getDestinationName());
            o2.setDegu(getDestinationGUID());
            o2.setDefl(getDestinationFlags());
            o2.setTy(getLogType());
        }
    }

    public static Method getSetMethod(Method[] methods, String propertyName) {
        for (Method m : methods) {
            if (m.getName().startsWith("set")) {
                if (m.getName().substring(3).equalsIgnoreCase(propertyName)) {
                    return m;
                }
            }
        }
        return null;
    }
    public static Method getSetMethod(Object o, Class valueType, String propertyName) throws NoSuchMethodException {
        String methodName = "set" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
        return o.getClass().getMethod(methodName, valueType);
    }
                
    public static final String XMLTAG_SOURCE_GUID = "sogu";
    public static final String XMLTAG_DESTINATION_GUID = "degu";
    public static final String XMLTAG_SOURCE_NAME = "sona";
    public static final String XMLTAG_DESTINATION_NAME = "dena";
    public static final String XMLTAG_SOURCE_FLAGS = "sofl";
    public static final String XMLTAG_DESTINATION_FLAGS = "defl";
    public static final String XMLTAG_TIME = "ti";
    public static final String XMLTAG_LOGTYPE = "ty";
    public static final String XMLTAG_SPELL_ID = "spid";
    public static final String XMLTAG_SPELL_NAME = "spna";
    public static final String XMLTAG_SPELL_SCHOOL = "spsc";
    public static final String XMLTAG_SPELL_EXTRA_ID = "spxid";
    public static final String XMLTAG_SPELL_EXTRA_NAME = "spxna";
    public static final String XMLTAG_SPELL_EXTRA_SCHOOL = "spxsc";
    public static final String XMLTAG_AMOUNT = "am";
    public static final String XMLTAG_AMOUNT_EXTRA = "amx";
    public static final String XMLTAG_AMOUNT_RESISTED = "amre";
    public static final String XMLTAG_AMOUNT_BLOCKED = "ambl";
    public static final String XMLTAG_AMOUNT_ABSORBED = "amab";
    public static final String XMLTAG_AMOUNT_OVERKILL = "amok";
    public static final String XMLTAG_AMOUNT_OVERHEAL = "amoh";
    public static final String XMLTAG_AURA_TYPE = "auty";
    public static final String XMLTAG_DAMAGE_FLAGS = "dafl";
    public static final String XMLTAG_MISS_TYPE = "mity";
    public static final String XMLTAG_ENVIRONMENTAL_TYPE = "enty";
    public static final String XMLTAG_HEALING_CRIT = "hecr";
    public static final String XMLTAG_POWER_TYPE = "poty";
    public static final String XMLTAG_EXTRA_ATTACKS = "xat";

    public String getSourceGUID() {
        return sourceGUID;
    }

    public void setSourceGUID(String sourceGUID) {
        this.sourceGUID = sourceGUID;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = removeFnutt(sourceName);
    }

    public long getSourceFlags() {
        return sourceFlags;
    }

    public void setSourceFlags(long sourceFlags) {
        this.sourceFlags = sourceFlags;
    }

    public long getSourceFlags2() {
        return sourceFlags2;
    }

    public void setSourceFlags2(long sourceFlags2) {
        this.sourceFlags2 = sourceFlags2;
    }

    public String getDestinationGUID() {
        return destinationGUID;
    }

    public void setDestinationGUID(String destinationGUID) {
        this.destinationGUID = destinationGUID;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = removeFnutt(destinationName);
    }

    public long getDestinationFlags() {
        return destinationFlags;
    }

    public void setDestinationFlags(long destinationFlags) {
        this.destinationFlags = destinationFlags;
    }

    public long getDestinationFlags2() {
        return destinationFlags2;
    }

    public void setDestinationFlags2(long destinationFlags2) {
        this.destinationFlags2 = destinationFlags2;
    }

    public String getLogType() {
        return logType;
    }

    public void setLogType(String logType) {
        this.logType = logType;
    }
    
}
