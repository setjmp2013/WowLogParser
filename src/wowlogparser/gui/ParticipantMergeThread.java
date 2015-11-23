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

package wowlogparser.gui;

import wowlogparserbase.helpers.Worker;
import wowlogparserbase.IntCallback;
import wowlogparserbase.FightParticipant;
import wowlogparserbase.StringCallback;
import java.util.ArrayList;
import java.util.List;
import wowlogparser.*;

/**
 *
 * @author racy
 */
public class ParticipantMergeThread extends Worker implements IntCallback, StringCallback {

    List<FightParticipant> sourceParticipants;
    FightParticipant mergedParticipant = null;

    public ParticipantMergeThread(List<FightParticipant> sourceParticipants) {
        this.sourceParticipants = sourceParticipants;
    }

    public FightParticipant getMergedParticipant() {
        return mergedParticipant;
    }

    @Override
    public void run() {
        mergedParticipant = FightParticipant.merge(sourceParticipants, this, this);
        hideDialog();
    }

    public void reportString(String str) {
        if (str.equalsIgnoreCase("sort")) {
            dialog.setWorkerText("Sorting the merged participants...");
        } else {
            dialog.setWorkerText(str);
        }
    }

    public void reportInt(int theInt) {
        dialog.setProgress((theInt * 100) / sourceParticipants.size());
    }
}
