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

import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author racy
 */
public class TimePeriodTest {

    public TimePeriodTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of compareTo method, of class TimePeriod.
     */
    @Test
    public void testCompareTo() {
        System.out.println("compareTo");
        TimePeriod p1, p2;
        int result;
        int expectedResult;
        boolean accumulatedResult = true;

        p1 = new TimePeriod(0, 1);
        p2 = new TimePeriod(-1, 1);
        result = p1.compareTo(p2);
        expectedResult = 1;
        accumulatedResult = accumulatedResult & (result == expectedResult);

        p1 = new TimePeriod(0, 1);
        p2 = new TimePeriod(1, 2);
        result = p1.compareTo(p2);
        expectedResult = -1;
        accumulatedResult = accumulatedResult & (result == expectedResult);

        p1 = new TimePeriod(0, 1);
        p2 = new TimePeriod(0, 2);
        result = p1.compareTo(p2);
        expectedResult = 0;
        accumulatedResult = accumulatedResult & (result == expectedResult);
    }

    /**
     * Test of contains method, of class TimePeriod.
     */
    @Test
    public void testContains() {
        System.out.println("contains");
        double d;
        TimePeriod p;
        boolean result;
        boolean expectedResult;
        boolean accumulatedResult = true;
        p = new TimePeriod(0, 1);

        d = 0;
        result = p.contains(d);
        expectedResult = true;
        accumulatedResult = accumulatedResult & (result == expectedResult);

        d = 1;
        result = p.contains(d);
        expectedResult = true;
        accumulatedResult = accumulatedResult & (result == expectedResult);

        d = 0.5;
        result = p.contains(d);
        expectedResult = true;
        accumulatedResult = accumulatedResult & (result == expectedResult);

        d = -1;
        result = p.contains(d);
        expectedResult = false;
        accumulatedResult = accumulatedResult & (result == expectedResult);

        d = 2;
        result = p.contains(d);
        expectedResult = false;
        accumulatedResult = accumulatedResult & (result == expectedResult);
    }

    /**
     * Test of intersect method, of class TimePeriod.
     */
    @Test
    public void testIntersect() {
        System.out.println("intersect");
        TimePeriod p1, p2;
        boolean result;
        boolean expectedResult;
        boolean accumulatedResult = true;

        p1 = new TimePeriod(0, 1);
        p2 = new TimePeriod(1, 2);
        result = p1.intersect(p2);
        expectedResult = true;
        accumulatedResult = accumulatedResult && (result == expectedResult);

        p1 = new TimePeriod(0, 1);
        p2 = new TimePeriod(-1, 0);
        result = p1.intersect(p2);
        expectedResult = true;
        accumulatedResult = accumulatedResult && (result == expectedResult);

        p1 = new TimePeriod(0, 1);
        p2 = new TimePeriod(1.1, 2);
        result = p1.intersect(p2);
        expectedResult = false;
        accumulatedResult = accumulatedResult && (result == expectedResult);

        p1 = new TimePeriod(0, 1);
        p2 = new TimePeriod(-1, -0.1);
        result = p1.intersect(p2);
        expectedResult = false;
        accumulatedResult = accumulatedResult && (result == expectedResult);

        p1 = new TimePeriod(0, 1);
        p2 = new TimePeriod(0.5, 0.6);
        result = p1.intersect(p2);
        expectedResult = true;
        accumulatedResult = accumulatedResult && (result == expectedResult);

        p1 = new TimePeriod(0, 1);
        p2 = new TimePeriod(-1, 2);
        result = p1.intersect(p2);
        expectedResult = true;
        accumulatedResult = accumulatedResult && (result == expectedResult);

        p1 = new TimePeriod(0, 1);
        p2 = new TimePeriod(-1, 0.5);
        result = p1.intersect(p2);
        expectedResult = true;
        accumulatedResult = accumulatedResult && (result == expectedResult);
        
        p1 = new TimePeriod(0, 1);
        p2 = new TimePeriod(0.5, 2);
        result = p1.intersect(p2);
        expectedResult = true;
        accumulatedResult = accumulatedResult && (result == expectedResult);

        assertTrue(accumulatedResult);
    }

    /**
     * Test of intersect2 method, of class TimePeriod.
     */
    @Test
    public void testIntersect2() {
        System.out.println("intersect2");
        TimePeriod p1, p2;
        boolean result;
        boolean expectedResult;
        boolean accumulatedResult = true;

        p1 = new TimePeriod(0, 1);
        p2 = new TimePeriod(1, 2);
        result = p1.intersect2(p2);
        expectedResult = true;
        accumulatedResult = accumulatedResult && (result == expectedResult);

        p1 = new TimePeriod(0, 1);
        p2 = new TimePeriod(-1, 0);
        result = p1.intersect2(p2);
        expectedResult = true;
        accumulatedResult = accumulatedResult && (result == expectedResult);

        p1 = new TimePeriod(0, 1);
        p2 = new TimePeriod(1.1, 2);
        result = p1.intersect2(p2);
        expectedResult = false;
        accumulatedResult = accumulatedResult && (result == expectedResult);

        p1 = new TimePeriod(0, 1);
        p2 = new TimePeriod(-1, -0.1);
        result = p1.intersect2(p2);
        expectedResult = false;
        accumulatedResult = accumulatedResult && (result == expectedResult);

        p1 = new TimePeriod(0, 1);
        p2 = new TimePeriod(0.5, 0.6);
        result = p1.intersect2(p2);
        expectedResult = true;
        accumulatedResult = accumulatedResult && (result == expectedResult);

        p1 = new TimePeriod(0, 1);
        p2 = new TimePeriod(-1, 2);
        result = p1.intersect2(p2);
        expectedResult = true;
        accumulatedResult = accumulatedResult && (result == expectedResult);

        p1 = new TimePeriod(0, 1);
        p2 = new TimePeriod(-1, 0.5);
        result = p1.intersect2(p2);
        expectedResult = true;
        accumulatedResult = accumulatedResult && (result == expectedResult);

        p1 = new TimePeriod(0, 1);
        p2 = new TimePeriod(0.5, 2);
        result = p1.intersect2(p2);
        expectedResult = true;
        accumulatedResult = accumulatedResult && (result == expectedResult);

        assertTrue(accumulatedResult);
    }

}