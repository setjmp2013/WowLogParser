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

package wowlogparserbase.eventfilter;

import wowlogparserbase.events.AuraTypeInterface;
import wowlogparserbase.events.BasicEvent;

/**
 * Filters events based on aura type. The classes that can possibly get through this filter are those that
 * implement the interface AuraTypeInterface
 * @author racy
 */
public class FilterAuraType extends Filter {
    public static final int TYPE_BUFF = 0;
    public static final int TYPE_DEBUFF = 1;
    
    int type;
    
    /**
     * The type, can be TYPE_BUFF or TYPE_DEBUFF
     * @param type
     */
    public FilterAuraType(int type) {
        this.type = type;
    }

    @Override
    public boolean filter(BasicEvent e) {
        if (e instanceof AuraTypeInterface) {
            AuraTypeInterface at = (AuraTypeInterface) e;
            switch(type) {
                case TYPE_BUFF:
                    if (at.isBuff()) {
                        return true;
                    }
                    break;
                case TYPE_DEBUFF:
                    if (at.isDebuff()) {
                        return true;
                    }
                    break;
            }
        }
        return false;
    }

}
