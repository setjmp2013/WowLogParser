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

import java.io.Serializable;

/**
 *
 * @author racy
 */
public class IntegerPair implements Serializable {
    int i1, i2;

    public IntegerPair(int i1, int i2) {
        this.i1 = i1;
        this.i2 = i2;
    }

    public int getI1() {
        return i1;
    }

    public void setI1(int i1) {
        this.i1 = i1;
    }

    public int getI2() {
        return i2;
    }

    public void setI2(int i2) {
        this.i2 = i2;
    }

}
