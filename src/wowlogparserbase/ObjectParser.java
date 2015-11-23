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

import java.util.ArrayList;
import java.util.List;
import wowlogparserbase.events.BasicEvent;
import wowlogparserbase.events.SpellSummonEvent;

/**
 * Parser class for finding objects like totems
 * @author racy
 */
public class ObjectParser {

    List<FightParticipant> objectInfo = new ArrayList<FightParticipant>();

    public ObjectParser(EventCollection ec) {
        objectParsing(ec.getEvents());
    }
       
    void objectParsing(List<BasicEvent> evs) {
        for (BasicEvent be : evs) {
            // Find objects such as totems
            if (be instanceof SpellSummonEvent) {
                SpellSummonEvent se = (SpellSummonEvent) be;
                //Dont add pets, only non pet summons
                if ((se.getDestinationFlags() & Constants.FLAGS_OBJECT_TYPE_PET) == 0) {
                    boolean fromPlayer = false;
                    if ((se.getSourceFlags() & Constants.FLAGS_OBJECT_TYPE_PLAYER) != 0) {
                        fromPlayer = true;
                    }
                    if (fromPlayer) {
                        boolean objectExists = false;
                        for (FightParticipant checkP : objectInfo) {
                            if (checkP.checkGUID(se.getDestinationGUID())) {
                                objectExists = true;
                                break;
                            }
                        }
                        if (!objectExists) {
                            FightParticipant p = new FightParticipant();
                            p.setGUID(se.getDestinationGUID());
                            p.setFlags(se.getDestinationFlags());
                            p.setName(se.getDestinationName());
                            p.setIsPetObject(true);
                            objectInfo.add(p);
                        }
                    }
                }
            }
        }
    }

    public List<FightParticipant> getObjectInfo() {
        return objectInfo;
    }

}
