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

/**
 *
 * @author racy
 */
public interface Constants {
    public static final long FLAGS_ALL = -1;
    public static final long FLAGS_OBJECT_AFFILIATION_MASK = 15;
    public static final long FLAGS_OBJECT_AFFILIATION_MINE = 1;
    public static final long FLAGS_OBJECT_AFFILIATION_OUTSIDER = 8;
    public static final long FLAGS_OBJECT_AFFILIATION_PARTY = 2;
    public static final long FLAGS_OBJECT_AFFILIATION_RAID = 4;
    public static final long FLAGS_OBJECT_CONTROL_MASK = 768;
    public static final long FLAGS_OBJECT_CONTROL_NPC = 512;
    public static final long FLAGS_OBJECT_CONTROL_PLAYER = 256;
    public static final long FLAGS_OBJECT_FOCUS = 131072;
    public static final long FLAGS_OBJECT_MAINASSIST = 524288;
    public static final long FLAGS_OBJECT_MAINTANK = 262144;
    public static final long FLAGS_OBJECT_NONE = -2147483648;
    public static final long FLAGS_OBJECT_RAIDTARGET1 = 1048576;
    public static final long FLAGS_OBJECT_RAIDTARGET2 = 2097152;
    public static final long FLAGS_OBJECT_RAIDTARGET3 = 4194304;
    public static final long FLAGS_OBJECT_RAIDTARGET4 = 8388608;
    public static final long FLAGS_OBJECT_RAIDTARGET5 = 16777216;
    public static final long FLAGS_OBJECT_RAIDTARGET6 = 33554432;
    public static final long FLAGS_OBJECT_RAIDTARGET7 = 67108864;
    public static final long FLAGS_OBJECT_RAIDTARGET8 = 134217728;
    public static final long FLAGS_OBJECT_REACTION_FRIENDLY = 16;
    public static final long FLAGS_OBJECT_REACTION_HOSTILE = 64;
    public static final long FLAGS_OBJECT_REACTION_MASK = 240;
    public static final long FLAGS_OBJECT_REACTION_NEUTRAL = 32;
    public static final long FLAGS_OBJECT_SPECIAL_MASK = -65536;
    public static final long FLAGS_OBJECT_TARGET = 65536;
    public static final long FLAGS_OBJECT_TYPE_GUARDIAN = 8192;
    public static final long FLAGS_OBJECT_TYPE_MASK = 64512;
    public static final long FLAGS_OBJECT_TYPE_NPC = 2048;
    public static final long FLAGS_OBJECT_TYPE_OBJECT = 16384;
    public static final long FLAGS_OBJECT_TYPE_PET = 4096;
    public static final long FLAGS_OBJECT_TYPE_PLAYER = 1024;
    
    public static final int LOG_TYPE_BC = 0;
    public static final int LOG_TYPE_WOTLK = 1;
    public static final int LOG_TYPE_WOTLK3_2 = 2;
    public static final int LOG_TYPE_CATA4 = 3;

    public static final int POWER_TYPE_ENERGY = 3;
    public static final int POWER_TYPE_FOCUS = 2;
    public static final int POWER_TYPE_HEALTH = -2;
    public static final int POWER_TYPE_MANA = 0;
    public static final int POWER_TYPE_PETHAPPINESS = 4;
    public static final int POWER_TYPE_RAGE = 1;
    public static final int POWER_TYPE_RUNES = 5;
    public static final int POWER_TYPE_RUNIC_POWER = 6;
    public static final int POWER_TYPE_UNKNOWN = -10;
    
    public static final int SCHOOL_ALL = 255;
    public static final int SCHOOL_ARCANE = 64;
    public static final int SCHOOL_FIRE = 4;
    public static final int SCHOOL_FROST = 16;
    public static final int SCHOOL_HOLY = 2;
    public static final int SCHOOL_NATURE = 8;
    public static final int SCHOOL_NONE = 128;
    public static final int SCHOOL_PHYSICAL = 1;
    public static final int SCHOOL_SHADOW = 32;

    public static final int[] schools = {
        SCHOOL_PHYSICAL, SCHOOL_HOLY,  SCHOOL_FIRE,
        SCHOOL_NATURE,   SCHOOL_FROST, SCHOOL_SHADOW,
        SCHOOL_ARCANE, SCHOOL_NONE};
    public static final String[] schoolStrings = {
        "Physical", "Holy", "Fire", "Nature", "Frost", "Shadow", "Arcane", ""};

    public static final int CLASS_UNKNOWN = 0;
    public static final int CLASS_DRUID = 1;
    public static final int CLASS_MAGE = 2;
    public static final int CLASS_WARRIOR = 3;
    public static final int CLASS_WARLOCK = 4;
    public static final int CLASS_SHAMAN = 5;
    public static final int CLASS_PRIEST = 6;
    public static final int CLASS_ROGUE = 7;
    public static final int CLASS_PALADIN = 8;
    public static final int CLASS_HUNTER = 9;
    public static final int CLASS_DEATHKNIGHT = 10;
    public static final int numClasses = 11;

}
