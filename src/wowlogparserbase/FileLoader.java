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

import wowlogparserbase.events.healing.*;
import wowlogparserbase.events.damage.*;
import wowlogparserbase.events.aura.*;
import wowlogparserbase.events.*;
import wowlogparserbase.EventCollection;
import wowlogparserbase.FightCollection;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import wowlogparserbase.helpers.FileHelper;

/**
 * A class for loading a combat log file
 * @author racy
 */
public class FileLoader {    
    File file;
    int numErrors = 0;
    public EventCollectionSimple eventCollection = new EventCollectionSimple();
    public FightCollection fightCollection = new FightCollection();
    PetParser petParser = null;
    private long fileUnixTime = 0;
    
    private HashMap<String, Class> classHashMap;
    
    /**
     * Constructor
     * @param f The file to load
     * @param onlyMeAndRaid A boolean describing which events to parse. If true only events from my raid/group/me.
     */
    public FileLoader(File f) {
        file = f;
        createClassHashMap();
    }

    public File getFile() {
        return file;
    }

    public long getFileUnixTime() {
        return fileUnixTime;
    }

    public int getYear() {
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(fileUnixTime);
        return cal.get(Calendar.YEAR);
    }

    /**
     * Create fights from the parsed events.
     */
    public void createFights() {
        int k;
        fightCollection = new FightCollection();
        fightCollection.createFights(eventCollection);
        petParser = new PetParser(this, getFightCollection().getFights());
        if (SettingsSingleton.getInstance().getAutoPlayerPets()) {
            PetParser.insertPetsOnPlayers(petParser.getParsedPlayers(), fightCollection.getFights());
        }
    }
    
    /**
     * Create one total fight from the parsed events.
     */
    public void createTotalFight() {
        int k;
        fightCollection = new FightCollection();
        fightCollection.createTotalFight(eventCollection);
        petParser = new PetParser(this, getFightCollection().getFights());
        if (SettingsSingleton.getInstance().getAutoPlayerPets()) {
            PetParser.insertPetsOnPlayers(petParser.getParsedPlayers(), fightCollection.getFights());
        }
    }

    /**
     * Parse the file
     * @return true if success, false otherwise
     */
    public boolean parse() {
        try {
            //long sT = System.currentTimeMillis();
            fileUnixTime = file.lastModified();
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
            String line = null;
            eventCollection = new EventCollectionSimple();
            LineParser lp = new LineParser();
            int row = -1;
            while ((line = reader.readLine()) != null) {
                if (line.trim().equals("")) {
                    continue;
                }
                row++;
                boolean added = false;
                int type;
                try {
                    lp.parse(line);
                    String timeDate = lp.getTimeDate();
                    String[] values = lp.getValues();                    
                    Class c = getEventClass(values);
                    BasicEvent event = (BasicEvent)c.newInstance();
                    if (event.parse(timeDate, values) >= 0) {
                        eventCollection.addEvent(event, row);
                        added = true;
                    } else {
                        int a=0;
                    }
                } catch (ArrayIndexOutOfBoundsException ex) {
                    int a=0;
                } catch (NumberFormatException ex) {
                    int a=0;
                } catch (InstantiationException ex) {
                    int a=0;
                } catch (IllegalAccessException ex) {
                    int a=0;
                }
                
                //Post processing
                if (added) {
                    BasicEvent be = eventCollection.getLastAdded();
                    be.findOldStrings();
                    if (SettingsSingleton.getInstance().getOnlyMeAndRaid()) {
                        if (!(be instanceof UnitDiedEvent || be instanceof UnitDestroyedEvent)) {
                            long test = (be.getSourceFlags()
                                    & (Constants.FLAGS_OBJECT_AFFILIATION_MINE
                                    + Constants.FLAGS_OBJECT_AFFILIATION_PARTY
                                    + Constants.FLAGS_OBJECT_AFFILIATION_RAID))
                                    + (be.getDestinationFlags()
                                    & (Constants.FLAGS_OBJECT_AFFILIATION_MINE
                                    + Constants.FLAGS_OBJECT_AFFILIATION_PARTY
                                    + Constants.FLAGS_OBJECT_AFFILIATION_RAID));
                            if (test == 0) {
                                eventCollection.removeLastAdded();
                            }
                        }
                    }
                } else {
                    numErrors++;
                }
            }
            reader.close();
            //System.out.println(System.currentTimeMillis() - sT);
            System.gc();
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
        return true;
    }
    
    /**
     * Parse the file
     * @param rows The rows to parse, must be a sorted list with no duplicates.
     * @return true if success, false otherwise
     */
    public boolean parse(List<Integer> rows) {
        if (rows.size() < 1) {
            return false;
        }
        BufferedReader reader = null;
        try {
            //long sT = System.currentTimeMillis();
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
            String line = null;
            eventCollection = new EventCollectionSimple();
            LineParser lp = new LineParser();
            int row = -1;
            Iterator<Integer> rowIt = rows.iterator();
            int nextRow = rowIt.next();
            while ((line = reader.readLine()) != null) {
                row++;
                if (row != nextRow) {
                    if (row > nextRow) {
                        if (!rowIt.hasNext()) {                            
                            break;
                        } else {
                            nextRow = rowIt.next();
                            if (row > nextRow) {
                                return false;
                            }
                        }
                    } else {
                        continue;
                    }
                }
                boolean added = false;
                int type;
                try {
                    lp.parse(line);
                    String timeDate = lp.getTimeDate();
                    String[] values = lp.getValues();                    
                    Class c = getEventClass(values);
                    BasicEvent event = (BasicEvent)c.newInstance();
                    if (event.parse(timeDate, values) >= 0) {
                        eventCollection.addEvent(event, row);
                        added = true;
                    }
                } catch (ArrayIndexOutOfBoundsException ex) {
                    int a=0;
                } catch (NumberFormatException ex) {
                    int a=0;
                } catch (InstantiationException ex) {
                    int a=0;
                } catch (IllegalAccessException ex) {
                    int a=0;
                }
                
                //Post processing
                if (added) {
                    BasicEvent be = eventCollection.getLastAdded();
                    be.findOldStrings();
                    if (SettingsSingleton.getInstance().getOnlyMeAndRaid()) {
                        if (!(be instanceof UnitDiedEvent)) {
                            long test = (be.getSourceFlags()
                                    & (Constants.FLAGS_OBJECT_AFFILIATION_MINE
                                    + Constants.FLAGS_OBJECT_AFFILIATION_PARTY
                                    + Constants.FLAGS_OBJECT_AFFILIATION_RAID))
                                    + (be.getDestinationFlags()
                                    & (Constants.FLAGS_OBJECT_AFFILIATION_MINE
                                    + Constants.FLAGS_OBJECT_AFFILIATION_PARTY
                                    + Constants.FLAGS_OBJECT_AFFILIATION_RAID));
                            if (test == 0) {
                                eventCollection.removeLastAdded();
                            }
                        }
                    }
                } else {
                    numErrors++;
                }
            }
            //System.out.println(System.currentTimeMillis() - sT);
            System.gc();
        } catch (FileNotFoundException e) {
            FileHelper.close(reader);
            return false;
        } catch (IOException e) {
            FileHelper.close(reader);
            return false;
        } finally {
            FileHelper.close(reader);            
        }
        return true;
    }

    /**
     * Get the event collection from this parsed file
     * @return The event collection
     */
    public EventCollectionSimple getEventCollection() {
        return eventCollection;
    }

    /**
     * Get the fight collection from this parsed file
     * @return The fight collection
     */
    public FightCollection getFightCollection() {
        return fightCollection;
    }

    /**
     * Get number of errors after parsing
     * @return Number of errors
     */
    public int getNumErrors() {
        return numErrors;
    }
    
    
    /**
     * Create mapping between log types and the event classes that parse that kind of log entry.
     */
    private void createClassHashMap() {
        classHashMap = new HashMap<String, Class>();
        classHashMap.put("SPELL_DAMAGE", DirectSpellDamageEvent.class);
        classHashMap.put("SPELL_PERIODIC_DAMAGE", PeriodicSpellDamageEvent.class);
        classHashMap.put("SWING_DAMAGE", SwingDamageEvent.class);
        classHashMap.put("SPELL_HEAL", DirectHealingEvent.class);
        classHashMap.put("SPELL_PERIODIC_HEAL", PeriodicHealingEvent.class);
        classHashMap.put("RANGE_DAMAGE", RangedDamageEvent.class);
        classHashMap.put("SPELL_PERIODIC_MISSED", PeriodicSpellMissedEvent.class);
        classHashMap.put("SPELL_MISSED", DirectSpellMissedEvent.class);
        classHashMap.put("SWING_MISSED", SwingMissedEvent.class);
        classHashMap.put("RANGE_MISSED", RangedMissedEvent.class);
        classHashMap.put("DAMAGE_SHIELD", DamageShieldEvent.class);
        classHashMap.put("DAMAGE_SHIELD_MISSED", DamageShieldMissedEvent.class);
        classHashMap.put("UNIT_DIED", UnitDiedEvent.class);
        classHashMap.put("UNIT_DESTROYED", UnitDestroyedEvent.class);
        classHashMap.put("SPELL_DRAIN", DirectSpellDrainEvent.class);
        classHashMap.put("SPELL_PERIODIC_DRAIN", PeriodicSpellDrainEvent.class);
        classHashMap.put("SPELL_LEECH", DirectSpellLeechEvent.class);
        classHashMap.put("SPELL_PERIODIC_LEECH", PeriodicSpellLeechEvent.class);
        classHashMap.put("SPELL_ENERGIZE", DirectSpellEnergizeEvent.class);
        classHashMap.put("SPELL_PERIODIC_ENERGIZE", PeriodicSpellEnergizeEvent.class);
        classHashMap.put("SPELL_DISPELL_FAILED", SpellDispellFailedEvent.class);
        classHashMap.put("SPELL_DISPEL_FAILED", SpellDispellFailedEvent.class);
        classHashMap.put("SPELL_AURA_APPLIED_DOSE", SpellAuraAppliedDoseEvent.class);
        classHashMap.put("SPELL_AURA_REMOVED_DOSE", SpellAuraRemovedDoseEvent.class);
        classHashMap.put("SPELL_AURA_BROKEN_SPELL", SpellAuraBrokenSpellEvent.class);
        classHashMap.put("SPELL_AURA_BROKEN", SpellAuraBrokenEvent.class);
        classHashMap.put("SPELL_AURA_REFRESH", SpellAuraRefreshEvent.class);
        classHashMap.put("SPELL_AURA_DISPELLED", SpellAuraDispelledEvent.class);
        classHashMap.put("SPELL_DISPEL", SpellAuraDispelledEvent.class);
        classHashMap.put("SPELL_AURA_APPLIED", SpellAuraAppliedEvent.class);
        classHashMap.put("SPELL_AURA_REMOVED", SpellAuraRemovedEvent.class);
        classHashMap.put("SPELL_AURA_STOLEN", SpellAuraStolenEvent.class);
        classHashMap.put("SPELL_STOLEN", SpellAuraStolenEvent.class);
        classHashMap.put("SPELL_CAST_START", SpellCastStartEvent.class);
        classHashMap.put("SPELL_CAST_SUCCESS", SpellCastSuccessEvent.class);
        classHashMap.put("SPELL_CAST_FAILED", SpellCastFailedEvent.class);
        classHashMap.put("SPELL_INTERRUPT", SpellInterruptEvent.class);
        classHashMap.put("SPELL_INSTAKILL", SpellInstaKillEvent.class);
        classHashMap.put("PARTY_KILL", PartyKillEvent.class);
        classHashMap.put("SPELL_SUMMON", SpellSummonEvent.class);        
        classHashMap.put("SPELL_EXTRA_ATTACKS", SpellExtraAttacksEvent.class);        
        classHashMap.put("SPELL_CREATE", SpellCreateEvent.class);
        classHashMap.put("ENVIRONMENTAL_DAMAGE", EnvironmentalDamageEvent.class);
        classHashMap.put("DAMAGE_SPLIT", DamageSplitEvent.class);
        classHashMap.put("SPELL_RESURRECT", SpellResurrectEvent.class);
    }
    
    /**
     * Get the event class that is responsible for parsing the supplied log type.
     * @param values An array with split values from a log file line.
     * @return The class.
     */
    private Class getEventClass(String[] values) {
        if (values.length < 1) {
            return UnknownEvent.class;
        }
        Class c = classHashMap.get(values[0].toUpperCase());
        if (c == null) {
            return UnknownEvent.class;
        }
        return c;
    }

    public PetParser getPetParser() {
        return petParser;
    }
    
}

