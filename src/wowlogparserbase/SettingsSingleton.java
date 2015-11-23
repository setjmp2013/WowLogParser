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
public class SettingsSingleton {
    private static SettingsSingleton theInstance = null;

    private boolean autoPlayerPets = true;
    private boolean autoBossFights = false;
    private boolean onlyDeadMobs = false;
    private boolean onlyMeAndRaid = false;
    private boolean petsAsOwner = false;
    private boolean allowNeutralNpcs = false;
    private double maxInactiveTime = 120;
    private boolean splinePlots = false;
    private int wowVersion = 420;

    private SettingsSingleton() {
    }

    public boolean getSplinePlots() {
        return splinePlots;
    }

    public void setSplinePlots(boolean splinePlots) {
        this.splinePlots = splinePlots;
    }

    public boolean getAutoBossFights() {
        return autoBossFights;
    }

    public void setAutoBossFights(boolean autoBossFights) {
        this.autoBossFights = autoBossFights;
    }

    public boolean getAutoPlayerPets() {
        return autoPlayerPets;
    }

    public void setAutoPlayerPets(boolean autoPlayerPets) {
        this.autoPlayerPets = autoPlayerPets;
    }

    public boolean getAllowNeutralNpcs() {
        return allowNeutralNpcs;
    }

    public void setAllowNeutralNpcs(boolean allowNeutralNpcs) {
        this.allowNeutralNpcs = allowNeutralNpcs;
    }

    public boolean getOnlyDeadMobs() {
        return onlyDeadMobs;
    }

    public void setOnlyDeadMobs(boolean onlyDeadMobs) {
        this.onlyDeadMobs = onlyDeadMobs;
    }

    public boolean getOnlyMeAndRaid() {
        return onlyMeAndRaid;
    }

    public void setOnlyMeAndRaid(boolean onlyMeAndRaid) {
        this.onlyMeAndRaid = onlyMeAndRaid;
    }

    public boolean getPetsAsOwner() {
        return petsAsOwner;
    }

    public void setPetsAsOwner(boolean petsAsOwner) {
        this.petsAsOwner = petsAsOwner;
    }

    public double getMaxInactiveTime() {
        return maxInactiveTime;
    }

    public void setMaxInactiveTime(double maxInactiveTime) {
        this.maxInactiveTime = maxInactiveTime;
    }

    /**
     * Wow version. Example: 3.2.2 is 322 3.3 = 330
     * @param wowVersion
     */
    public int getWowVersion() {
        return wowVersion;
    }

    /**
     * Wow version. Example: 3.2.2 is 322 3.3 = 330
     * @param wowVersion
     */
    public void setWowVersion(int wowVersion) {
        this.wowVersion = wowVersion;
    }

    public static SettingsSingleton getInstance() {
        if (SettingsSingleton.theInstance == null) {
            theInstance = new SettingsSingleton();
        }
        return theInstance;
    }

}
