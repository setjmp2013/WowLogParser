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
package wowlogparserbase.tablemodels;

import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import wowlogparserbase.AmountAndCount;
import wowlogparserbase.EventCollection;
import wowlogparserbase.FightParticipant;
import wowlogparserbase.SpellInfo;
import wowlogparserbase.WLPNumberFormat;
import wowlogparserbase.events.BasicEvent;
import wowlogparserbase.Constants;
import wowlogparserbase.info.DamageInfoAll;
import wowlogparserbase.info.DamageInfoBase;
import wowlogparserbase.info.DamageInfoSpell;
import wowlogparserbase.info.DamageInfoSwings;
import wowlogparserbase.info.EmptyInfo;
import wowlogparserbase.info.InfoBase;
import wowlogparserbase.info.StringInfo;

/**
 *
 * @author racy
 */
public class ModelFactory {
    private ModelFactory() {

    }
    
    public static String makeHitNr(long nr, long tot) {
        NumberFormat nf = WLPNumberFormat.getInstance();
        nf.setMaximumFractionDigits(1);
        nf.setGroupingUsed(false);
        if (nr == 0) {
            return "";
        }
        long total = tot;
        if (total == 0) {
            total = 1;
        }
        String s = "" + nr + " (" + nf.format((double)nr/(double)total*100.0) + "%)";
        return s;
    }

    /**
     *
     * @param rows A list where this method will add the rows.
     * @param fight
     * @param participant
     * @return The total damage for the participant.
     */
    public static long makeDamageRows(List<InfoBase> rows, EventCollection fight, FightParticipant participant) {
        List<SpellInfo> damageSpellIDsAll;
        List<SpellInfo> rangedSpellIDsAll;
        List<SpellInfo> damageSpellIDsPlayer;
        List<SpellInfo> rangedSpellIDsPlayer;
        List<SpellInfo> damageSpellIDsPet;
        List<SpellInfo> rangedSpellIDsPet;
        damageSpellIDsAll = participant.getIDs(FightParticipant.TYPE_SPELL_DAMAGE);
        rangedSpellIDsAll = participant.getIDs(FightParticipant.TYPE_RANGED_DAMAGE);
        damageSpellIDsPet = participant.getIDs(FightParticipant.TYPE_SPELL_DAMAGE, FightParticipant.UNIT_PET);
        rangedSpellIDsPet = participant.getIDs(FightParticipant.TYPE_RANGED_DAMAGE, FightParticipant.UNIT_PET);
        damageSpellIDsPlayer = participant.getIDs(FightParticipant.TYPE_SPELL_DAMAGE, FightParticipant.UNIT_PLAYER);
        rangedSpellIDsPlayer = participant.getIDs(FightParticipant.TYPE_RANGED_DAMAGE, FightParticipant.UNIT_PLAYER);

        AmountAndCount a, a2;

        NumberFormat nf = WLPNumberFormat.getInstance();
        nf.setMaximumFractionDigits(1);
        nf.setGroupingUsed(false);
        DamageInfoBase di;

        // All Damage
        di = new DamageInfoAll(fight, participant);
        di.setSkillName("All");
        long totalDamage = di.getAmountAndCount().amount;
        rows.add(di);
        rows.add(new EmptyInfo());

        // Swing damage
        if (participant.hasPets()) {
            di = new DamageInfoSwings(fight, participant, FightParticipant.UNIT_PLAYER);
            di.setSkillName("Melee swings");
            rows.add(di);
            di = new DamageInfoSwings(fight, participant, FightParticipant.UNIT_PET);
            di.setSkillName("Pet: Melee swings");
            rows.add(di);
        } else {
            di = new DamageInfoSwings(fight, participant, FightParticipant.UNIT_PLAYER);
            di.setSkillName("Melee swings");
            rows.add(di);
        }
        rows.add(new EmptyInfo());

        // Physical schools
        int physicalSchools = Constants.SCHOOL_PHYSICAL;
        di = new DamageInfoAll(fight, participant, FightParticipant.UNIT_ALL, physicalSchools, FightParticipant.TYPE_SPELL_DAMAGE);
        di.setSkillName("Physical Skills");
        rows.add(di);
        if (participant.hasPets()) {
            for (SpellInfo s : damageSpellIDsPlayer) {
                if ((s.school & physicalSchools) == 0) {
                    continue;
                }
                di = new DamageInfoSpell(fight, participant, s, FightParticipant.UNIT_PLAYER);
                rows.add(di);
            }
            for (SpellInfo s : damageSpellIDsPet) {
                if ((s.school & physicalSchools) == 0) {
                    continue;
                }
                di = new DamageInfoSpell(fight, participant, s, FightParticipant.UNIT_PET);
                di.setSkillName("Pet: " + s.name);
                rows.add(di);
            }
        } else {
            for (SpellInfo s : damageSpellIDsAll) {
                if ((s.school & physicalSchools) == 0) {
                    continue;
                }
                di = new DamageInfoSpell(fight, participant, s, FightParticipant.UNIT_ALL);
                rows.add(di);
            }
        }
        rows.add(new EmptyInfo());

        // Spell schools
        int spellSchools =
                Constants.SCHOOL_ARCANE | Constants.SCHOOL_FIRE |
                Constants.SCHOOL_FROST | Constants.SCHOOL_HOLY |
                Constants.SCHOOL_NATURE | Constants.SCHOOL_SHADOW;
        di = new DamageInfoAll(fight, participant, FightParticipant.UNIT_ALL, spellSchools, FightParticipant.TYPE_SPELL_DAMAGE);
        di.setSkillName("Spells(All)");
        rows.add(di);

        if (participant.hasPets()) {
            for (SpellInfo s : damageSpellIDsPlayer) {
                if ((s.school & spellSchools) == 0) {
                    continue;
                }
                di = new DamageInfoSpell(fight, participant, s, FightParticipant.UNIT_PLAYER);
                rows.add(di);
            }
            for (SpellInfo s : damageSpellIDsPet) {
                if ((s.school & spellSchools) == 0) {
                    continue;
                }
                di = new DamageInfoSpell(fight, participant, s, FightParticipant.UNIT_PET);
                di.setSkillName("Pet: "+ s.name);
                rows.add(di);
            }
        } else {
            for (SpellInfo s : damageSpellIDsAll) {
                if ((s.school & spellSchools) == 0) {
                    continue;
                }
                di = new DamageInfoSpell(fight, participant, s, FightParticipant.UNIT_ALL);
                rows.add(di);
            }
        }
        rows.add(new EmptyInfo());

        // Ranged schools
        di = new DamageInfoAll(fight, participant, FightParticipant.UNIT_ALL, Constants.SCHOOL_ALL, FightParticipant.TYPE_RANGED_DAMAGE);
        di.setSkillName("Ranged(All)");
        rows.add(di);
        if (participant.hasPets()) {
            for (SpellInfo s : rangedSpellIDsPlayer) {
                di = new DamageInfoSpell(fight, participant, s, FightParticipant.UNIT_PLAYER, FightParticipant.TYPE_RANGED_DAMAGE);
                rows.add(di);
            }
            for (SpellInfo s : rangedSpellIDsPet) {
                di = new DamageInfoSpell(fight, participant, s, FightParticipant.UNIT_PET, FightParticipant.TYPE_RANGED_DAMAGE);
                di.setSkillName("Pet: " + s.name);
                rows.add(di);
            }
        } else {
            for (SpellInfo s : rangedSpellIDsAll) {
                di = new DamageInfoSpell(fight, participant, s, FightParticipant.UNIT_ALL, FightParticipant.TYPE_RANGED_DAMAGE);
                rows.add(di);
            }
        }

        rows.add(new EmptyInfo());

        di = new DamageInfoAll(fight, participant, FightParticipant.UNIT_ALL, Constants.SCHOOL_ALL, FightParticipant.TYPE_ENVIRONMENTAL_DAMAGE);
        di.setSkillName("Environmental");
        rows.add(di);
        return totalDamage;
    }

    /**
     *
     * @param rows A list where this method will add the rows.
     * @param fight
     * @param participant
     * @return The total damage for the participant.
     */
    public static long makeDamageRowsSeparateDotAndDd(List<InfoBase> rows, EventCollection fight, FightParticipant participant) {
        List<SpellInfo> damageSpellIDsDdPlayer;
        List<SpellInfo> damageSpellIDsDdPet;
        List<SpellInfo> damageSpellIDsDotPlayer;
        List<SpellInfo> damageSpellIDsDotPet;
        List<SpellInfo> rangedSpellIDsAll;
        List<SpellInfo> rangedSpellIDsPlayer;
        List<SpellInfo> rangedSpellIDsPet;
        damageSpellIDsDdPlayer = participant.getIDs(FightParticipant.TYPE_SPELL_DAMAGE_DIRECT, FightParticipant.UNIT_PLAYER);
        damageSpellIDsDdPet = participant.getIDs(FightParticipant.TYPE_SPELL_DAMAGE_DIRECT, FightParticipant.UNIT_PET);
        damageSpellIDsDotPlayer = participant.getIDs(FightParticipant.TYPE_SPELL_DAMAGE_PERIODIC, FightParticipant.UNIT_PLAYER);
        damageSpellIDsDotPet = participant.getIDs(FightParticipant.TYPE_SPELL_DAMAGE_PERIODIC, FightParticipant.UNIT_PET);
        rangedSpellIDsAll = participant.getIDs(FightParticipant.TYPE_RANGED_DAMAGE);
        rangedSpellIDsPet = participant.getIDs(FightParticipant.TYPE_RANGED_DAMAGE, FightParticipant.UNIT_PET);
        rangedSpellIDsPlayer = participant.getIDs(FightParticipant.TYPE_RANGED_DAMAGE, FightParticipant.UNIT_PLAYER);

        AmountAndCount a, a2;

        NumberFormat nf = WLPNumberFormat.getInstance();
        nf.setMaximumFractionDigits(1);
        nf.setGroupingUsed(false);
        DamageInfoBase di;

        // All Damage
        di = new DamageInfoAll(fight, participant);
        di.setSkillName("All");
        //di.setNoCounts();
        long totalDamage = di.getAmountAndCount().amount;
        rows.add(di);
        rows.add(new EmptyInfo());

        // Swing damage
        if (participant.hasPets()) {
            di = new DamageInfoSwings(fight, participant, FightParticipant.UNIT_PLAYER);
            di.setSkillName("Melee swings");
            rows.add(di);
            di = new DamageInfoSwings(fight, participant, FightParticipant.UNIT_PET);
            di.setSkillName("Pet: Melee swings");
            rows.add(di);
        } else {
            di = new DamageInfoSwings(fight, participant, FightParticipant.UNIT_PLAYER);
            di.setSkillName("Melee swings");
            rows.add(di);
        }
        rows.add(new EmptyInfo());

        // Physical schools DD
        int physicalSchools = Constants.SCHOOL_PHYSICAL;
        di = new DamageInfoAll(fight, participant, FightParticipant.UNIT_ALL, physicalSchools, FightParticipant.TYPE_SPELL_DAMAGE_DIRECT);
        di.setSkillName("Physical Skills(DD)");
        //di.setNoCounts();
        rows.add(di);
        if (participant.hasPets()) {
            for (SpellInfo s : damageSpellIDsDdPlayer) {
                if ((s.school & physicalSchools) == 0) {
                    continue;
                }
                di = new DamageInfoSpell(fight, participant, s, FightParticipant.UNIT_PLAYER, FightParticipant.TYPE_SPELL_DAMAGE_DIRECT);
                rows.add(di);
            }
            for (SpellInfo s : damageSpellIDsDdPet) {
                if ((s.school & physicalSchools) == 0) {
                    continue;
                }
                di = new DamageInfoSpell(fight, participant, s, FightParticipant.UNIT_PET, FightParticipant.TYPE_SPELL_DAMAGE_DIRECT);
                di.setSkillName("Pet: " + s.name);
                rows.add(di);
            }
        } else {
            for (SpellInfo s : damageSpellIDsDdPlayer) {
                if ((s.school & physicalSchools) == 0) {
                    continue;
                }
                di = new DamageInfoSpell(fight, participant, s, FightParticipant.UNIT_ALL, FightParticipant.TYPE_SPELL_DAMAGE_DIRECT);
                rows.add(di);
            }
        }
        rows.add(new EmptyInfo());

        // Physical schools Dot
        di = new DamageInfoAll(fight, participant, FightParticipant.UNIT_ALL, physicalSchools, FightParticipant.TYPE_SPELL_DAMAGE_PERIODIC);
        di.setSkillName("Physical Skills(Dot)");
        //di.setNoCounts();
        rows.add(di);
        if (participant.hasPets()) {
            for (SpellInfo s : damageSpellIDsDotPlayer) {
                if ((s.school & physicalSchools) == 0) {
                    continue;
                }
                di = new DamageInfoSpell(fight, participant, s, FightParticipant.UNIT_PLAYER, FightParticipant.TYPE_SPELL_DAMAGE_PERIODIC);
                rows.add(di);
            }
            for (SpellInfo s : damageSpellIDsDotPet) {
                if ((s.school & physicalSchools) == 0) {
                    continue;
                }
                di = new DamageInfoSpell(fight, participant, s, FightParticipant.UNIT_PET, FightParticipant.TYPE_SPELL_DAMAGE_PERIODIC);
                di.setSkillName("Pet: " + s.name);
                rows.add(di);
            }
        } else {
            for (SpellInfo s : damageSpellIDsDotPlayer) {
                if ((s.school & physicalSchools) == 0) {
                    continue;
                }
                di = new DamageInfoSpell(fight, participant, s, FightParticipant.UNIT_ALL, FightParticipant.TYPE_SPELL_DAMAGE_PERIODIC);
                rows.add(di);
            }
        }
        rows.add(new EmptyInfo());

        // Spell schools DD
        int spellSchools =
                Constants.SCHOOL_ARCANE | Constants.SCHOOL_FIRE |
                Constants.SCHOOL_FROST | Constants.SCHOOL_HOLY |
                Constants.SCHOOL_NATURE | Constants.SCHOOL_SHADOW;
        di = new DamageInfoAll(fight, participant, FightParticipant.UNIT_ALL, spellSchools, FightParticipant.TYPE_SPELL_DAMAGE_DIRECT);
        di.setSkillName("Spells(DD)");
        //di.setNoCounts();
        rows.add(di);

        if (participant.hasPets()) {
            for (SpellInfo s : damageSpellIDsDdPlayer) {
                if ((s.school & spellSchools) == 0) {
                    continue;
                }
                di = new DamageInfoSpell(fight, participant, s, FightParticipant.UNIT_PLAYER, FightParticipant.TYPE_SPELL_DAMAGE_DIRECT);
                rows.add(di);
            }
            for (SpellInfo s : damageSpellIDsDdPet) {
                if ((s.school & spellSchools) == 0) {
                    continue;
                }
                di = new DamageInfoSpell(fight, participant, s, FightParticipant.UNIT_PET, FightParticipant.TYPE_SPELL_DAMAGE_DIRECT);
                di.setSkillName("Pet: "+ s.name);
                rows.add(di);
            }
        } else {
            for (SpellInfo s : damageSpellIDsDdPlayer) {
                if ((s.school & spellSchools) == 0) {
                    continue;
                }
                di = new DamageInfoSpell(fight, participant, s, FightParticipant.UNIT_ALL, FightParticipant.TYPE_SPELL_DAMAGE_DIRECT);
                rows.add(di);
            }
        }
        rows.add(new EmptyInfo());

        // Spell schools Dot
        di = new DamageInfoAll(fight, participant, FightParticipant.UNIT_ALL, spellSchools, FightParticipant.TYPE_SPELL_DAMAGE_PERIODIC);
        di.setSkillName("Spells(Dot)");
        //di.setNoCounts();
        rows.add(di);

        if (participant.hasPets()) {
            for (SpellInfo s : damageSpellIDsDotPlayer) {
                if ((s.school & spellSchools) == 0) {
                    continue;
                }
                di = new DamageInfoSpell(fight, participant, s, FightParticipant.UNIT_PLAYER, FightParticipant.TYPE_SPELL_DAMAGE_PERIODIC);
                rows.add(di);
            }
            for (SpellInfo s : damageSpellIDsDotPet) {
                if ((s.school & spellSchools) == 0) {
                    continue;
                }
                di = new DamageInfoSpell(fight, participant, s, FightParticipant.UNIT_PET, FightParticipant.TYPE_SPELL_DAMAGE_PERIODIC);
                di.setSkillName("Pet: "+ s.name);
                rows.add(di);
            }
        } else {
            for (SpellInfo s : damageSpellIDsDotPlayer) {
                if ((s.school & spellSchools) == 0) {
                    continue;
                }
                di = new DamageInfoSpell(fight, participant, s, FightParticipant.UNIT_ALL, FightParticipant.TYPE_SPELL_DAMAGE_PERIODIC);
                rows.add(di);
            }
        }
        rows.add(new EmptyInfo());

        // Ranged schools
        di = new DamageInfoAll(fight, participant, FightParticipant.UNIT_ALL, Constants.SCHOOL_ALL, FightParticipant.TYPE_RANGED_DAMAGE);
        di.setSkillName("Ranged(All)");
        //di.setNoCounts();
        rows.add(di);
        if (participant.hasPets()) {
            for (SpellInfo s : rangedSpellIDsPlayer) {
                di = new DamageInfoSpell(fight, participant, s, FightParticipant.UNIT_PLAYER, FightParticipant.TYPE_RANGED_DAMAGE);
                rows.add(di);
            }
            for (SpellInfo s : rangedSpellIDsPet) {
                di = new DamageInfoSpell(fight, participant, s, FightParticipant.UNIT_PET, FightParticipant.TYPE_RANGED_DAMAGE);
                di.setSkillName("Pet: " + s.name);
                rows.add(di);
            }
        } else {
            for (SpellInfo s : rangedSpellIDsAll) {
                di = new DamageInfoSpell(fight, participant, s, FightParticipant.UNIT_ALL, FightParticipant.TYPE_RANGED_DAMAGE);
                rows.add(di);
            }
        }

        rows.add(new EmptyInfo());

        di = new DamageInfoAll(fight, participant, FightParticipant.UNIT_ALL, Constants.SCHOOL_ALL, FightParticipant.TYPE_ENVIRONMENTAL_DAMAGE);
        di.setSkillName("Environmental");
        rows.add(di);

        if (participant.hasPets()) {
            rows.add(new EmptyInfo());

            // Pet name breakdown
            rows.add(new StringInfo("Pet Name Breakdown"));
            Set<String> petNames = new HashSet<String>();
            for (FightParticipant p : participant.getPetNames()) {
                petNames.add(p.getName());
            }
            Iterator<String> it = petNames.iterator();
            while (it.hasNext()) {
                String name = it.next();
                di = new DamageInfoAll(fight, participant, FightParticipant.UNIT_PET, Constants.SCHOOL_ALL, FightParticipant.TYPE_ALL_DAMAGE, name);
                di.setSkillName(name);
                rows.add(di);
            }
        }
        return totalDamage;
    }
}
