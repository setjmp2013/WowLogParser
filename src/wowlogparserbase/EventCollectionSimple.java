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

import wowlogparserbase.helpers.ListFunctions;
import wowlogparserbase.events.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import wowlogparserbase.eventfilter.Filter;

/**
 * A class for holding and manipulating combat log events
 * @author racy
 */
public class EventCollectionSimple extends AbstractEventCollection implements EventCollectionOptimizedAccess {
    private List<BasicEvent> events = new ArrayList<BasicEvent>();

    protected BasicEvent lastAdded = DefaultObjects.unknownEvent;
    
    public EventCollectionSimple() {
        
    }

    /**
     * Constructor that pre-allocates enough memory for a certain number of events.
     * @param numEvents The number of events.
     */
    public EventCollectionSimple(int numEvents) {
        events = new ArrayList<BasicEvent>(numEvents);
    }
    
    /**
     * Constructor that uses the supplied events for the collection.
     * @param evs The events.
     */
    public EventCollectionSimple(List<BasicEvent> evs) {
        setEvents(new ArrayList<BasicEvent>(evs));
    }

    /**
     * Constructor to make a semi deep copy of another EventCollection
     * @param ec The source collection
     */
    public EventCollectionSimple(EventCollection ec) {
        super(ec);
        events = new ArrayList<BasicEvent>(ec.getEvents());
        lastAdded = ec.getLastAdded();
        setActiveDuration(ec.getActiveDuration());
    }
    
    /**
     * Constructor to make a merged variant of ec1 and ec2
     * @param ec1 A source collection
     * @param ec2 A source collection
     */
    public EventCollectionSimple(EventCollection ec1, EventCollection ec2) {
        HashSet<BasicEvent> tempSet = new HashSet<BasicEvent>();
        tempSet.addAll(ec1.getEvents());
        tempSet.addAll(ec2.getEvents());
        events = new ArrayList<BasicEvent>(tempSet);
        sort();
    }

    /**
     * Get the number of events in this collection.
     * @return
     */
    public final int size() {
        return events.size();
    }
    
    /**
     * Sort the events for this event collection
     */
    public void sort() {
        Collections.sort(events);
    }

    /**
     * Remove duplicate events from this event collection
     */
    public void removeEqualElements() {
        sort();
        LinkedList<BasicEvent> linkedEvents = new LinkedList<BasicEvent>(getEvents());
        ListFunctions.removeEqualLinked(linkedEvents);
        setEvents(new ArrayList<BasicEvent>(linkedEvents));
    }

    /**
     * Get all events from the EventCollection.
     * This method cannot be overriden so only the basic EventCollection events are returned.
     * @return A list with the events.
     */
    public final List<BasicEvent> getEvents() {
        return new ArrayList<BasicEvent>(events);
    }
        
    /**
     * Run the collection through a filter and return a new filtered collection.
     * @param f The filter
     * @return
     */
    public EventCollectionSimple filter(Filter f) {
        List<BasicEvent> outEvs = new ArrayList<BasicEvent>();
        for (BasicEvent e : events) {
            if (f.filter(e)) {
                outEvs.add(e);
            }
        }
        return new EventCollectionSimple(outEvs);
    }
    
    /**
     * Run the collection through a set of filters with logical OR in between and return a new filtered collection.
     * @param fs The filters
     * @return
     */
    public EventCollectionSimple filterOr(List<Filter> fs) {
        ArrayList<BasicEvent> outEvs = new ArrayList<BasicEvent>();
        for (BasicEvent e : events) {
            boolean match = false;
            for (Filter f : fs) {
                if (f.filter(e)) {
                    match = true;
                    break;
                }
            }
            if (match) {
                outEvs.add(e);                
            }
        }
        return new EventCollectionSimple(outEvs);
    }

    /**
     * Run the collection through a set of filters with logical AND in between and return a new filtered collection.
     * @param fs The filters
     * @return
     */
    public EventCollectionSimple filterAnd(List<Filter> fs) {
        ArrayList<BasicEvent> outEvs = new ArrayList<BasicEvent>();
        for (BasicEvent e : events) {
            boolean match = true;
            for (Filter f : fs) {
                if (!f.filter(e)) {
                    match = false;
                    break;
                }
            }
            if (match) {
                outEvs.add(e);                
            }
        }
        return new EventCollectionSimple(outEvs);
    }

    private IntegerPair lastStartStopIndices = new IntegerPair(0, 0);
    private EventCollection lastColl = null;
    public void getEventsContinueFromLastReset() {
        lastStartStopIndices = new IntegerPair(0, 0);
        lastColl = null;
    }
    public EventCollection getEventsContinueFromLast(double startTime, double endTime) {
        int index = lastStartStopIndices.getI2()-2;
        if (index < 0) {
            index = 0;
        }
        IntegerPair startStopIndices = timeFindIndices(index, startTime, endTime);
        if (lastColl == null) {
            lastColl = new EventCollectionSimple();
        }
        lastColl.getEvents().clear();
        for(int k=startStopIndices.getI1(); k<startStopIndices.getI2(); k++) {
            lastColl.addEventAlways(events.get(k));
        }
        lastStartStopIndices = startStopIndices;
        return lastColl;
    }

    /**
     * Find start and end indices in the list of events for a time range.
     * @param startIndex The index to start searching from
     * @param startTime The start time
     * @param endTime The end time
     * @return A pair with the start index and end index, end index is the last element +1.
     */
    public IntegerPair timeFindIndices(int startIndex, double startTime, double endTime) {
        int index = startIndex;
        if (index < 0) {
            index = 0;
        }
        int iStart = events.size();
        int iEnd = events.size();
        //Find start index
        while(index < events.size()) {
            if (events.get(index).time >= startTime) {
                iStart = index;
                break;
            }
            index++;
        }
        //Find end index
        while(index < events.size()) {
            if (events.get(index).time > endTime) {
                iEnd = index;
                break;
            }
            index++;
        }
        return new IntegerPair(iStart, iEnd);
    }

    /**
     * Special method to filter on time on sorted collections, for speed purposes.
     * @param startTime The start time
     * @param endTime Then end time
     * @return
     */
    public EventCollection filterTime(double startTime, double endTime) {
        LogEvent le = new LogEvent();
        
        le.time = startTime;
        int startIndex = Collections.binarySearch(events, le, new TimeComparator());
        if (startIndex < 0) {
            startIndex = -startIndex - 1;
        }
        //Make sure we get the first event if several events have the same time, since binarySearch does not guarantee the first.
        if (startIndex < events.size() && startIndex >= 0) {
            if (events.get(startIndex).time == startTime) {
                while (events.get(startIndex).time == startTime) {
                    startIndex--;
                    if (startIndex < 0) {
                        break;
                    }
                }
                startIndex++;
                if (startIndex < 0) {
                    startIndex = 0;
                }
            }
        }
        
        le.time = endTime;
        int endIndex = Collections.binarySearch(events, le, new TimeComparator());
        if (endIndex < 0) {
            endIndex = -endIndex - 2;
        }
        //Make sure we get the last event if several events have the same time, since binarySearch does not guarantee the last.
        if (endIndex < events.size() && endIndex >= 0) {
            if (events.get(endIndex).time == endTime) {
                while (events.get(endIndex).time == endTime) {
                    endIndex++;
                    if (endIndex >= events.size()) {
                        break;
                    }
                }
                endIndex--;
                if (endIndex > events.size() - 1) {
                    endIndex = events.size() - 1;
                }
            }
        }

        int evSize = getEvents().size();
        ArrayList<BasicEvent> outEvs = new ArrayList<BasicEvent>();
        for (int k=startIndex; k<=endIndex; k++) {
            if (k >= evSize) {
                continue;
            }
            if (k<0) {
                continue;
            }
            outEvs.add(events.get(k));
        }
        return new EventCollectionSimple(outEvs);
    }

    /**
     * Get the events between two times.
     * This method cannot be overriden so only the basic EventCollection events are returned.
     * @param startTime
     * @param endTime
     * @return
     */
    protected final List<BasicEvent> getEvents(double startTime, double endTime) {
        return filterTime(startTime, endTime).getEvents();
    }
    
    /**
     * Add an event. The lastAdded status is updated.
     * @param e The event
     * @return true if added, false otherwise
     */
    public boolean addEvent(BasicEvent e) {
        events.add(e);
        lastAdded = e;
        return true;
    }
    

    /**
     * Add an event no matter what.
     * The lastEvent status is not updated.
     * @param e The event
     */
    public void addEventAlways(BasicEvent e) {
        events.add(e);
        lastAdded = e;
    }

    /**
     * Get the number of events total, can be overridden by classes that inherit from this.
     * @return The number of events total
     */
    public int getNumEventsTotal() {
        return events.size();
    }

    @Override
    public List<BasicEvent> getEventsTotal() {
        return new ArrayList<BasicEvent>(events);
    }
    
    /**
     * Get the last added event
     * @return The last event
     */
    public BasicEvent getLastAdded() {
        return lastAdded;
    }

    /**
     * Remove the last added event
     */
    public void removeLastAdded() {
        events.remove(events.size()-1);
    }

    /**
     * Get an event at a certain index
     * @param i The index for the event
     * @return
     */
    public BasicEvent getEvent(int i) {
        return events.get(i);
    }

    /**
     * First damage event vs the victim
     * @return The time
     */
    public double getStartTimeFast() {
        if (events.size() > 0) {
            return events.get(0).time;
        } else {
            return 0;
        }
    }
    
    /**
     * Last damage event vs the victim
     * @return The time
     */
    public double getEndTimeFast() {
        if (events.size() > 0) {
            return events.get(events.size()-1).time;
        } else {
            return 0;
        }
    }
        
    /**
     * Set the events in the list as the events this collection should contain.
     * All current events are lost.
     * @param events
     */
    public void setEvents(Collection<BasicEvent> events) {
        this.events = new ArrayList<BasicEvent>(events);
    }

}

