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
package wowlogparserbase.info;

import wowlogparserbase.*;

/**
 *
 * @author racy
 */
public abstract class PowerInfo extends InfoBase implements EventsAccessInterface {
    public static final int ID_NOT_PRESENT = -1;
    public static final int POWER_KIND_UNKNOWN = 0;
    public static final int POWER_KIND_LEECH = 1;
    public static final int POWER_KIND_DRAIN = 2;
    public static final int POWER_KIND_ENERGIZE = 4;

    public abstract EventCollectionSimple getEvents();
    public abstract String getSkillName();
    public abstract int getId();
    public abstract AmountAndCount getAmountAndCount();
    public abstract double getPowerAverage();
    public abstract double getPps();
    public abstract double getPpsActive();
    public abstract int getSchool();
    public abstract int getPowerKind();
    public abstract int getPowerType();
    public abstract int getCollectionType();
}
