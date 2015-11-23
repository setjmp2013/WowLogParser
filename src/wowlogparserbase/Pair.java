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
public class Pair<S, T> {
    private S obj1;
    private T obj2;
    private boolean toStringSecond = false;

    public Pair(S obj1, T obj2) {
        this.obj1 = obj1;
        this.obj2 = obj2;
    }

    public Pair(S obj1, T obj2, boolean toStringSecond) {
        this.obj1 = obj1;
        this.obj2 = obj2;
        this.toStringSecond = toStringSecond;
    }

    public S getObj1() {
        return obj1;
    }

    public void setObj1(S obj1) {
        this.obj1 = obj1;
    }

    public T getObj2() {
        return obj2;
    }

    public void setObj2(T obj2) {
        this.obj2 = obj2;
    }

    @Override
    public String toString() {
        if (toStringSecond) {
            return obj2.toString();
        } else {
            return obj1.toString();
        }
    }

}
