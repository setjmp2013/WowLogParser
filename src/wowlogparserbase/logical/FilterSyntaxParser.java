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
package wowlogparserbase.logical;

import java.util.HashMap;
import java.util.Map;
import wowlogparserbase.eventfilter.Filter;
import wowlogparserbase.eventfilter.FilterAmountEq;
import wowlogparserbase.eventfilter.FilterAmountGt;
import wowlogparserbase.eventfilter.FilterAmountLt;
import wowlogparserbase.eventfilter.FilterAnd;
import wowlogparserbase.eventfilter.FilterDestinationName;
import wowlogparserbase.eventfilter.FilterLogType;
import wowlogparserbase.eventfilter.FilterOr;
import wowlogparserbase.eventfilter.FilterSkill;
import wowlogparserbase.eventfilter.FilterSkillExtra;
import wowlogparserbase.eventfilter.FilterSourceName;

/**
 *
 * @author racy
 */
public class FilterSyntaxParser {
    private static final int UNKNOWN = 0;
    private static final int SNAME = 1;
    private static final int DNAME = 2;
    private static final int SKILL = 3;
    private static final int SKILLID = 4;
    private static final int EXTRASKILL = 5;
    private static final int EXTRASKILLID = 6;
    private static final int LOGTYPE = 7;
    private static final int AMOUNT = 8;
    private Map<String, Integer> typeMap;

    private String syntax;

    public FilterSyntaxParser(String syntax) {
        this.syntax = syntax;
        typeMap = new HashMap<String, Integer>();
        typeMap.put("sname", SNAME);
        typeMap.put("sourcename", SNAME);
        typeMap.put("dname", DNAME);
        typeMap.put("destname", DNAME);
        typeMap.put("destinationname", DNAME);
        typeMap.put("skill", SKILL);
        typeMap.put("skillid", SKILLID);
        typeMap.put("extraskill", EXTRASKILL);
        typeMap.put("extraskillid", EXTRASKILLID);
        typeMap.put("logtype", LOGTYPE);
        typeMap.put("amount", AMOUNT);
    }

    public Filter getFilter() throws SyntaxException {
        try {
            String s = syntax.trim();
            if (s.startsWith("(")) {
                int endPar = findClosingParenthesis(s, 0);
                if (endPar == -1) {
                    throw new SyntaxException("Closing parenthesis not found: " + s);
                }
                FilterSyntaxParser p1 = new FilterSyntaxParser(s.substring(1, endPar));
                Filter f1 = p1.getFilter();

                s = s.substring(endPar + 1).trim();
                Filter totFilter = makeCompoundFilter(f1, s);
                return totFilter;
            } else {
                MakeFilterResponse mfr = makeFilter(s);
                Filter f1 = mfr.getFilter();
                s = mfr.getRemains().trim();
                Filter f2 = makeCompoundFilter(f1, s);
                return f2;
            }

        } catch (IndexOutOfBoundsException ex) {
            throw new SyntaxException("Syntax error!");
        }
    }

    private int findClosingParenthesis(String s, int startParenthesisPos) {
        int pos = startParenthesisPos + 1;
        int endPos = -1;
        int count = 1;
        while(pos < s.length()) {
            if (s.charAt(pos) == '(') {
                count++;
            }
            if(s.charAt(pos) == ')') {
                count--;
            }
            if (count == 0) {
                endPos = pos;
                break;
            }
            pos++;
        }
        return endPos;
    }

    /**
     * Make a compund filter from a filter and a string.
     * The string should either start with & or |
     * The string can also be empty and then the supplied filter will be returned unchanged.
     * @param f1 The first filter
     * @param s The remains string
     * @return A filter
     * @throws wowlogparserbase.logical.SyntaxException
     */
    private Filter makeCompoundFilter(Filter f1, String s) throws SyntaxException {
        if (s.startsWith("&")) {
            s = s.substring(1).trim();
            FilterSyntaxParser p2 = new FilterSyntaxParser(s);
            Filter f2 = p2.getFilter();
            Filter totFilter = new FilterAnd(f1, f2);
            return totFilter;
        } else if (s.startsWith("|")) {
            s = s.substring(1).trim();
            FilterSyntaxParser p2 = new FilterSyntaxParser(s);
            Filter f2 = p2.getFilter();
            Filter totFilter = new FilterOr(f1, f2);
            return totFilter;
        } else {
            //No extra stuff
            if (s.length() > 0) {
                throw new SyntaxException("Compound filter error, does not start with & or | remaining string is: " + s);
            }
            return f1;
        }

    }

    /**
     * Make filter from a string. The string must have an expression as the first occurence.
     * It can be a compound expression with & and | after the first expression.
     * @param s The expression.
     * @return A response containing the first Filter and whatever remains if it is a compound expression.
     * @throws wowlogparserbase.logical.SyntaxException
     */
    private MakeFilterResponse makeFilter(String s) throws SyntaxException {

        GetKeyResponse res = getKey(s);
        String s2 = res.getRemains();
        char opChar = res.getOp();
        int type = res.getType();
        if (type == UNKNOWN) {
            throw new SyntaxException("Unknown key: " + res.getKeyName());
        }
        if (s2.startsWith("\"")) {
            int endFnuttPos = s2.indexOf("\"", 1);
            if (endFnuttPos == -1) {
                throw new SyntaxException("Cannot find a matching end \" in the content string: " + s);
            }
            String s3 = s2.substring(1, endFnuttPos);
            Filter f = makeFilter(type, opChar, s3, res.getKeyName());
            if(endFnuttPos + 1 >= s2.length()) {
                MakeFilterResponse mfr = new MakeFilterResponse("", f);
                return mfr;
            } else {
                String remains = s2.substring(endFnuttPos+1);
                MakeFilterResponse mfr = new MakeFilterResponse(remains, f);
                return mfr;
            }
        } else {
            throw new SyntaxException("The content string does not begin with a \" which it must do: " + s);
        }
    }

    private GetKeyResponse getKey(String s) {
        int splitPos = s.length();
        int i;
        i = s.indexOf('=');
        splitPos = Math.min(splitPos, i == -1 ? splitPos:i);
        i = s.indexOf('<');
        splitPos = Math.min(splitPos, i == -1 ? splitPos:i);
        i = s.indexOf('>');
        splitPos = Math.min(splitPos, i == -1 ? splitPos:i);
        String s1 = s.substring(0, splitPos).trim();
        String s2 = s.substring(splitPos).trim();
        char opChar = s2.charAt(0);
        s2 = s2.substring(1).trim();
        Integer type = typeMap.get(s1.toLowerCase());
        if (type == null) {
            GetKeyResponse r = new GetKeyResponse(UNKNOWN, opChar, s1, s);
            return r;
        } else {
            GetKeyResponse r = new GetKeyResponse(type, opChar, s1, s2);
            return r;
        }
    }

    private Filter makeFilter(int type, char opChar, String content, String keyName) throws SyntaxException {
        try {
            switch(type) {
                case AMOUNT:
                    switch(opChar) {
                        case '=':
                            return new FilterAmountEq(Integer.parseInt(content));
                        case '<':
                            return new FilterAmountLt(Integer.parseInt(content));
                        case '>':
                            return new FilterAmountGt(Integer.parseInt(content));
                        default:
                            throw new SyntaxException("Unknown operator " + opChar + " for amount.");
                    }
                case DNAME:
                    if (opChar != '=') {
                        throw new SyntaxException("Destination name must have = operator.");
                    }
                    return new FilterDestinationName(content);
                case SNAME:
                    if (opChar != '=') {
                        throw new SyntaxException("Source name must have = operator.");
                    }
                    return new FilterSourceName(content);
                case SKILL:
                    if (opChar != '=') {
                        throw new SyntaxException("Skill must have = operator.");
                    }
                    return new FilterSkill(FilterSkill.ANY_ID, content, FilterSkill.ANY_SCHOOL, FilterSkill.ANY_POWER_TYPE);
                case SKILLID:
                    if (opChar != '=') {
                        throw new SyntaxException("Skill id must have = operator.");
                    }
                    return new FilterSkill(Integer.parseInt(content), null, FilterSkill.ANY_SCHOOL, FilterSkill.ANY_POWER_TYPE);
                case EXTRASKILL:
                    if (opChar != '=') {
                        throw new SyntaxException("Extra skill must have = operator.");
                    }
                    return new FilterSkillExtra(FilterSkillExtra.ANY_ID, content, FilterSkillExtra.ANY_SCHOOL);
                case EXTRASKILLID:
                    if (opChar != '=') {
                        throw new SyntaxException("Extra skill id must have = operator.");
                    }
                    return new FilterSkillExtra(Integer.parseInt(content), null, FilterSkill.ANY_SCHOOL);
                case LOGTYPE:
                    if (opChar != '=') {
                        throw new SyntaxException("Log type must have = operator.");
                    }
                    return new FilterLogType(content);
                default:
                    throw new SyntaxException("Unknown filter type: " + keyName);

            }
        } catch (NumberFormatException ex) {
            throw new SyntaxException("A number was malformed in the expression");
        }
    }

    public static void main(String[] args) {
        FilterSyntaxParser p = new FilterSyntaxParser("logtype=\"SPELL_DAMAGE\" & amount<\"3\" &(skillid=\"12\" &(sname = \"Dracy\" & dname=\"Alithiel\")) ");
        //FilterSyntaxParser p = new FilterSyntaxParser("logtype=\"SPELL_DAMAGE\" & skillid=\"12\" & sname = \"Dracy\" & dname=\"Alithiel\" ");
        try {
            Filter f = p.getFilter();
            int a=0;
        } catch(SyntaxException ex) {
            System.out.println(ex.getMessage());
        }
    }
}

class GetKeyResponse {
    int type;
    char op;
    String keyName;
    String remains;

    public GetKeyResponse(int type, char op, String keyName, String remains) {
        this.type = type;
        this.op = op;
        this.keyName = keyName;
        this.remains = remains;
    }

    public String getRemains() {
        return remains;
    }

    public int getType() {
        return type;
    }

    public char getOp() {
        return op;
    }

    public String getKeyName() {
        return keyName;
    }
    
}