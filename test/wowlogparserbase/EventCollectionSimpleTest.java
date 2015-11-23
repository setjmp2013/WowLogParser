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
import java.util.Collection;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import wowlogparserbase.eventfilter.Filter;
import wowlogparserbase.eventfilter.FilterPassThrough;
import wowlogparserbase.events.BasicEvent;
import wowlogparserbase.events.damage.DamageEvent;

/**
 *
 * @author racy
 */
public class EventCollectionSimpleTest {

    public EventCollectionSimpleTest() {
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
     * Test of size method, of class EventCollectionSimple.
     */
    @Test
    public void testSize() {
        System.out.println("size");
        EventCollectionSimple instance = new EventCollectionSimple();
        instance.addEventAlways(new DamageEvent());
        instance.addEventAlways(new DamageEvent());
        int expResult = 2;
        int result = instance.size();
        assertEquals(expResult, result);
    }

    /**
     * Test of sort method, of class EventCollectionSimple.
     */
    @Test
    public void testSort() {
        System.out.println("sort");
        EventCollectionSimple instance = new EventCollectionSimple();

        BasicEvent e1 = new DamageEvent();
        e1.setLogFileRow(4);
        instance.addEventAlways(e1);

        BasicEvent e2 = new DamageEvent();
        e2.setLogFileRow(1);
        instance.addEventAlways(e2);

        BasicEvent e3 = new DamageEvent();
        e3.setLogFileRow(2);
        instance.addEventAlways(e3);

        instance.sort();

        boolean correct = true;
        correct = correct & instance.getEvent(0).getLogFileRow() == 1;
        correct = correct & instance.getEvent(1).getLogFileRow() == 2;
        correct = correct & instance.getEvent(2).getLogFileRow() == 4;
        assertTrue(correct);
    }

    /**
     * Test of removeEqualElements method, of class EventCollectionSimple.
     */
    @Test
    public void testRemoveEqualElements() {
        System.out.println("removeEqualElements");
        EventCollectionSimple instance = new EventCollectionSimple();

        BasicEvent e1 = new DamageEvent();
        e1.setLogFileRow(1);
        instance.addEventAlways(e1);

        BasicEvent e2 = new DamageEvent();
        e2.setLogFileRow(2);
        instance.addEventAlways(e2);

        BasicEvent e3 = new DamageEvent();
        e3.setLogFileRow(1);
        instance.addEventAlways(e3);

        BasicEvent e4 = new DamageEvent();
        e4.setLogFileRow(2);
        instance.addEventAlways(e4);

        instance.removeEqualElements();

        boolean equalFound = false;
        for (int k=0; k<instance.size(); k++) {
            for (int l=0; l<instance.size(); l++) {
                if (l==k) {
                    continue;
                }
                if (instance.getEvent(k).equals(instance.getEvent(l))) {
                    equalFound = true;
                }
            }
            
        }
        assertFalse(equalFound);
    }


    /**
     * Test of filter method, of class EventCollectionSimple.
     */
    @Test
    public void testFilter() {
        System.out.println("filter");
        Filter f = new Filter() {
            @Override
            public boolean filter(BasicEvent e) {
                return true;
            }
        };
        EventCollectionSimple instance = new EventCollectionSimple();
        instance.addEventAlways(new DamageEvent());
        instance.addEventAlways(new DamageEvent());
        instance.addEventAlways(new DamageEvent());
        EventCollectionSimple result = instance.filter(f);

        assertEquals(instance.size(), result.size());
    }

    /**
     * Test of filterAnd method, of class EventCollectionSimple.
     */
    @Test
    public void testFilterAndOr() {
        System.out.println("filterAnd and filterOr");
        List<Filter> fs = new ArrayList<Filter>();
        fs.add(new Filter() {
            @Override
            public boolean filter(BasicEvent e) {
                return true;
            }
        });
        fs.add(new Filter() {
            @Override
            public boolean filter(BasicEvent e) {
                return false;
            }
        });
        EventCollectionSimple instance = new EventCollectionSimple();
        instance.addEventAlways(new DamageEvent());
        instance.addEventAlways(new DamageEvent());
        instance.addEventAlways(new DamageEvent());
        EventCollectionSimple resultAnd = instance.filterAnd(fs);
        EventCollectionSimple resultOr = instance.filterOr(fs);
        assertEquals(0, resultAnd.size());
        assertEquals(instance.size(), resultOr.size());

    }

    /**
     * Test of timeFindIndices method, of class EventCollectionSimple.
     */
    @Test
    public void testTimeFindIndices() {
        System.out.println("timeFindIndices");
        EventCollectionSimple instance = new EventCollectionSimple();

        BasicEvent e1 = new DamageEvent();
        e1.setLogFileRow(1);
        e1.time = 0;
        instance.addEventAlways(e1);

        BasicEvent e2 = new DamageEvent();
        e2.setLogFileRow(2);
        e2.time = 1;
        instance.addEventAlways(e2);

        BasicEvent e3 = new DamageEvent();
        e3.setLogFileRow(1);
        e3.time = 2;
        instance.addEventAlways(e3);

        BasicEvent e4 = new DamageEvent();
        e4.setLogFileRow(2);
        e4.time = 3;
        instance.addEventAlways(e4);

        instance.sort();
        
        IntegerPair result = instance.timeFindIndices(0, 1, 2);

        assertEquals(1, result.getI1());
        assertEquals(3, result.getI2());
    }

    /**
     * Test of filterTime method, of class EventCollectionSimple.
     */
    @Test
    public void testFilterTime() {
        System.out.println("filterTime");
        double startTime = 0.0;
        double endTime = 0.0;
        EventCollectionSimple instance = new EventCollectionSimple();

        BasicEvent e1 = new DamageEvent();
        e1.setLogFileRow(1);
        e1.time = 0;
        instance.addEventAlways(e1);

        BasicEvent e2 = new DamageEvent();
        e2.setLogFileRow(2);
        e2.time = 1;
        instance.addEventAlways(e2);

        BasicEvent e3 = new DamageEvent();
        e3.setLogFileRow(3);
        e3.time = 2;
        instance.addEventAlways(e3);

        BasicEvent e4 = new DamageEvent();
        e4.setLogFileRow(4);
        e4.time = 3;
        instance.addEventAlways(e4);

        EventCollection result = instance.filterTime(1, 2);
        assertEquals(2, result.size());
        assertEquals(1, result.getEvent(0).time, 0.00001);
        assertEquals(2, result.getEvent(1).time, 0.00001);
    }

    /**
     * Test of getLastAdded method, of class EventCollectionSimple.
     */
    @Test
    public void testGetLastAdded() {
        System.out.println("getLastAdded");
        EventCollectionSimple instance = new EventCollectionSimple();
        BasicEvent e1 = new DamageEvent();
        e1.setLogFileRow(1);
        instance.addEventAlways(e1);

        BasicEvent e2 = new DamageEvent();
        e2.setLogFileRow(2);
        instance.addEventAlways(e2);

        BasicEvent e3 = new DamageEvent();
        e3.setLogFileRow(3);
        instance.addEventAlways(e3);
        
        BasicEvent result = instance.getLastAdded();

        assertEquals(3, result.getLogFileRow());
    }

    /**
     * Test of removeLastAdded method, of class EventCollectionSimple.
     */
    @Test
    public void testRemoveLastAdded() {
        System.out.println("removeLastAdded");
        EventCollectionSimple instance = new EventCollectionSimple();
        BasicEvent e1 = new DamageEvent();
        e1.setLogFileRow(1);
        instance.addEventAlways(e1);

        BasicEvent e2 = new DamageEvent();
        e2.setLogFileRow(2);
        instance.addEventAlways(e2);

        BasicEvent e3 = new DamageEvent();
        e3.setLogFileRow(3);
        instance.addEventAlways(e3);

        instance.removeLastAdded();

        assertEquals(2, instance.size());
        assertEquals(2, instance.getEvent(instance.size()-1).getLogFileRow());
    }

    /**
     * Test of getEvent method, of class EventCollectionSimple.
     */
    @Test
    public void testGetEvent() {
        System.out.println("getEvent");
        EventCollectionSimple instance = new EventCollectionSimple();
        BasicEvent e1 = new DamageEvent();
        e1.setLogFileRow(1);
        instance.addEventAlways(e1);

        BasicEvent e2 = new DamageEvent();
        e2.setLogFileRow(2);
        instance.addEventAlways(e2);

        BasicEvent e3 = new DamageEvent();
        e3.setLogFileRow(3);
        instance.addEventAlways(e3);

        assertEquals(2, instance.getEvent(1).getLogFileRow());
    }

    /**
     * Test of getStartTimeFast method, of class EventCollectionSimple.
     */
    @Test
    public void test_getStartTimeFast_getEndTimeFast() {
        System.out.println("getStartTimeFast");
        EventCollectionSimple instance = new EventCollectionSimple();
        BasicEvent e1 = new DamageEvent();
        e1.setLogFileRow(1);
        e1.time = 0;
        instance.addEventAlways(e1);

        BasicEvent e2 = new DamageEvent();
        e2.setLogFileRow(2);
        e2.time = 1;
        instance.addEventAlways(e2);

        BasicEvent e3 = new DamageEvent();
        e3.setLogFileRow(3);
        e3.time = 2;
        instance.addEventAlways(e3);

        BasicEvent e4 = new DamageEvent();
        e4.setLogFileRow(4);
        e4.time = 3;
        instance.addEventAlways(e4);

        assertEquals(0, instance.getStartTimeFast(), 0.000001);
        assertEquals(3, instance.getEndTimeFast(), 0.000001);
    }

}