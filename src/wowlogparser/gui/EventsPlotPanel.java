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

import java.awt.event.ActionEvent;
import wowlogparserbase.helpers.ChartHelper;
import wowlogparserbase.helpers.VerticalLayout;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.ItemSelectable;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.action.ActionButton;
import wowlogparserbase.EventCollection;
import wowlogparserbase.EventCollectionSimple;
import wowlogparserbase.eventfilter.Filter;
import wowlogparserbase.eventfilter.FilterLogType;
import wowlogparserbase.eventfilter.FilterNamesContain;
import wowlogparserbase.eventfilter.FilterPassThrough;
import wowlogparserbase.events.BasicEvent;
import wowlogparserbase.events.PowerEvent;
import wowlogparserbase.events.SpellInfoEvent;
import wowlogparserbase.events.aura.SpellAuraBaseEvent;
import wowlogparserbase.events.damage.DamageEvent;
import wowlogparserbase.events.damage.DamageMissedEvent;
import wowlogparserbase.events.healing.HealingEvent;
import wowlogparserbase.logical.FilterSyntaxParser;
import wowlogparserbase.logical.SyntaxException;

/**
 *
 * @author racy
 */
public class EventsPlotPanel extends JPanel implements ItemListener {

    EventCollection timeReferenceCollection = null;
    EventCollectionSimple allEvents;
    List<String> logTypes;
    HashMap<String, Integer> datasetIndices;
    HashMap<String, JCheckBox> logBoxes;
    HashMap<JCheckBox, String> logBoxesInverse;
    JTextField textFieldSource = new JTextField();
    JTextField textFieldDestination = new JTextField();
    JButton buttonSelectAll;
    JButton buttonSelectNone;
    JTextField textFieldLogical = new JTextField();
    Filter logicalExpressionFilter = new FilterPassThrough();

    EventXYDataset dataset;
    JFreeChart chart;
    ChartPanel chartPanel;

    JScrollPane p;
    JPanel left = new JPanel();
    JPanel center = new JPanel(new BorderLayout());
    boolean useCheckBoxes = true;
    
    Preferences prefsRoot;

    public EventsPlotPanel() {
        prefsRoot = Preferences.userRoot().node("EventsPlotPanel");
        allEvents = new EventCollectionSimple();
        logTypes = new ArrayList<String>(allEvents.getLogTypes());
        Collections.sort(logTypes);

        setLayout(new BorderLayout());
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setContinuousLayout(false);
        add(splitPane, BorderLayout.CENTER);
        p = new JScrollPane(left);
        p.setBorder(new TitledBorder("Event selection"));
        splitPane.setLeftComponent(p);
        splitPane.setRightComponent(center);
        splitPane.setDividerLocation(270);
//        add(p, BorderLayout.WEST);
//        add(center, BorderLayout.CENTER);
        initCheckboxComponents();
        initPlotComponents();
    }
    
    public EventsPlotPanel(EventCollectionSimple events, Preferences basePrefs) {
        prefsRoot = basePrefs.node("EventsPlotPanel");
        this.allEvents = events;
        logTypes = new ArrayList<String>(events.getLogTypes());
        
        setLayout(new BorderLayout());
        p = new JScrollPane(left);
        p.setBorder(new TitledBorder("Event selection"));
        add(p, BorderLayout.WEST);
        add(center, BorderLayout.CENTER);
        
        initCheckboxComponents();
        initPlotComponents();
    }

    public void removeCheckBoxes() {
        useCheckBoxes = false;
        remove(p);
        validate();
    }

    public void activateCheckBoxes() {
        useCheckBoxes = true;
        add(p, BorderLayout.WEST);
        validate();
    }

    public EventCollection getEvents() {
        return allEvents;
    }

    public void setEvents(EventCollectionSimple events) {
        saveCheckStates();
        this.allEvents = events;
        logTypes = new ArrayList<String>(events.getLogTypes());
        Collections.sort(logTypes);
        initCheckboxComponents();
        initPlotComponents();
        updateUI();
    }

    public void setEvents(EventCollection timeReferenceCollection, EventCollectionSimple events) {
        saveCheckStates();
        this.timeReferenceCollection = timeReferenceCollection;
        this.allEvents = events;
        logTypes = new ArrayList<String>(events.getLogTypes());
        Collections.sort(logTypes);
        initCheckboxComponents();
        initPlotComponents();
        updateUI();
    }

    public Preferences getPrefsRoot() {
        return prefsRoot;
    }

    public void setPrefsRoot(Preferences prefs) {
        this.prefsRoot = prefs.node("EventsPlotPanel");
    }

    public void saveCheckStates() {
        for (int k=0; k<logTypes.size(); k++) {
            String logType = logTypes.get(k);
            JCheckBox box = logBoxes.get(logType);
            prefsRoot.putBoolean(logType, box.isSelected());
        }
    }
    
    public void itemStateChanged(ItemEvent e) {        
        if (logBoxes.containsValue(e.getItem())) {
            String logType = logBoxesInverse.get(e.getItem());
            JCheckBox box = logBoxes.get(logType);
            Integer ind = datasetIndices.get(logType);
            if (ind != null) {
                dataset.setVisible(ind, box.isSelected());
            }
        }
    }

    private void saveLogicalHistory(String syntax) {
        StringPrefsDb db = new StringPrefsDb(prefsRoot);
        List<String> strings = db.getSavedStrings();
        List<String> strings2 = new ArrayList<String>();
        strings2.add(syntax);
        strings.remove(syntax);
        //Only keep at maximum 20 expressions.
        for (int k = 0; k < 19; k++) {
            if (k >= strings.size()) {
                break;
            } else {
                strings2.add(strings.get(k));
            }
        }
        db.putStrings(strings2);
    }

    private void initCheckboxComponents() {
        left.removeAll();
        //left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setLayout(new VerticalLayout(3, VerticalLayout.LEFT));
        
        JPanel p = new JPanel();
        p.setBorder(new TitledBorder("Source Filter"));
        p.setMinimumSize(new Dimension(0,0));
        textFieldSource = new JTextField(10);
        p.add(textFieldSource);
        left.add(p);
        p.setSize(textFieldSource.getPreferredSize());

        JPanel p2 = new JPanel();
        p2.setBorder(new TitledBorder("Destination Filter"));
        p2.setMinimumSize(new Dimension(0,0));
        textFieldDestination = new JTextField(10);
        p2.add(textFieldDestination);
        left.add(p2);
        p2.setSize(textFieldDestination.getPreferredSize());
        
        JPanel p4 = new JPanel();
        p4.setLayout(new GridBagLayout());
        GridBagConstraints c2 = new GridBagConstraints();
        p4.setBorder(new TitledBorder("Logical Expression"));
        p4.setMinimumSize(new Dimension(0,0));
        textFieldLogical = new JTextField(11);
        c2.anchor = GridBagConstraints.WEST;
        c2.gridx = 0;
        c2.gridy = 0;
        c2.gridwidth = 3;
        p4.add(textFieldLogical, c2);
        c2.gridwidth = 1;
        
        JButton helpButton = new JButton("Help");
        c2.gridx = 0;
        c2.gridy = 1;
        p4.add(helpButton, c2);
        helpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JDialog helpDia = new ShowHelpDialog(null, "wowlogparser/gui/help/filterhelp.txt", false);
                helpDia.setLocationRelativeTo(EventsPlotPanel.this);
                helpDia.setVisible(true);
            }
        });

        JButton historyButton = new JButton("History");
        c2.gridx = 1;
        c2.gridy = 1;
        p4.add(historyButton, c2);
        historyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                StringPrefsDb db = new StringPrefsDb(prefsRoot);
                List<String> strings = db.getSavedStrings();
                if (strings.size() == 0) {
                    JOptionPane.showMessageDialog(EventsPlotPanel.this, "No history exists. History is updated each time apply is pressed.", "Alert", JOptionPane.WARNING_MESSAGE);
                } else {
                    StringChooser chooser = new StringChooser(null, true, strings);
                    chooser.setLocationRelativeTo(EventsPlotPanel.this);
                    chooser.setVisible(true);
                    if (chooser.okPressed) {
                        String chosenString = chooser.getChosenString();
                        textFieldLogical.setText(chosenString);
                    }
                }
            }
        });

        JButton applyButton = new JButton("Apply");
        c2.gridx = 2;
        c2.gridy = 1;
        p4.add(applyButton, c2);
        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String syntax = textFieldLogical.getText().trim();
                    if (syntax.isEmpty()) {
                        logicalExpressionFilter = new FilterPassThrough();
                    } else {
                        FilterSyntaxParser parser = new FilterSyntaxParser(syntax);
                        logicalExpressionFilter = parser.getFilter();
                        saveLogicalHistory(syntax);
                    }
                } catch(SyntaxException ex) {
                    JOptionPane.showMessageDialog(EventsPlotPanel.this, ex.getMessage(), "Error in logical expression", JOptionPane.ERROR_MESSAGE);
                    logicalExpressionFilter = new FilterPassThrough();
                }
                initPlotComponentsLater();
            }
        });
        left.add(p4);

        JPanel p3 = new JPanel();
        p3.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        p3.setBorder(new TitledBorder("Select types"));
        p3.setMinimumSize(new Dimension(0,0));

        buttonSelectAll = new JButton("All");
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 0;
        p3.add(buttonSelectAll, c);
        buttonSelectAll.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                for (Object key : logBoxes.keySet()) {
                    JCheckBox box = logBoxes.get(key);
                    ItemListener[] temp = box.getItemListeners();
                    for (ItemListener l : temp) {
                        box.removeItemListener(l);
                    }
                    box.setSelected(true);
                    for (ItemListener l : temp) {
                        box.addItemListener(l);
                    }
                }
                initPlotComponentsLater();
            }
        });

        buttonSelectNone = new JButton("None");
        c.gridx = 1;
        c.gridy = 0;
        p3.add(buttonSelectNone, c);
        buttonSelectNone.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                for (Object key : logBoxes.keySet()) {
                    JCheckBox box = logBoxes.get(key);
                    ItemListener[] temp = box.getItemListeners();
                    for (ItemListener l : temp) {
                        box.removeItemListener(l);
                    }
                    box.setSelected(false);
                    for (ItemListener l : temp) {
                        box.addItemListener(l);
                    }
                }
                initPlotComponentsLater();
            }
        });
        left.add(p3);

        textFieldSource.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {                
                initPlotComponentsLater();
            }

            public void removeUpdate(DocumentEvent e) {
                initPlotComponentsLater();
            }

            public void changedUpdate(DocumentEvent e) {
                initPlotComponentsLater();
            }
        });
        textFieldDestination.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {                
                initPlotComponentsLater();
            }

            public void removeUpdate(DocumentEvent e) {
                initPlotComponentsLater();
            }

            public void changedUpdate(DocumentEvent e) {
                initPlotComponentsLater();
            }
        });

        logBoxes = new HashMap<String, JCheckBox>();
        logBoxesInverse = new HashMap<JCheckBox, String>();
        for (int k=0; k<logTypes.size(); k++) {
            String logType = logTypes.get(k);
            boolean state = prefsRoot.getBoolean(logType, true);
            JCheckBox box = new JCheckBox(logType, state);
            box.addItemListener(this);
            left.add(box);
            logBoxes.put(logType, box);
            logBoxesInverse.put(box, logType);
        }        
    }
    
    protected void initPlotComponentsLater() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                initPlotComponents();
            }
        });
    }
    
    private void initPlotComponents() {
        center.removeAll();
        EventCollectionSimple events = this.allEvents.filter(logicalExpressionFilter);
        dataset = new EventXYDataset(10,10);
        List<Shape> seriesShapes = new ArrayList<Shape>();
        List<Color> seriesColor = new ArrayList<Color>();
        double minTime;
        if (timeReferenceCollection != null) {
            minTime = timeReferenceCollection.getStartTime();

        } else {
            minTime = events.getStartTime();
        }
        Color lightBlueColor = new Color(0x33, 0x99, 0xFF);
        String sourceFilter = textFieldSource.getText();
        String destFilter = textFieldDestination.getText();
        datasetIndices = new HashMap<String, Integer>();
        for (int k=0; k<logTypes.size(); k++) {
            String logType = logTypes.get(k);
            boolean boxSelected = true;
            if (useCheckBoxes) {
                JCheckBox box = logBoxes.get(logType);
                boxSelected = box.isSelected();
            }
            List<Filter> filters = new ArrayList<Filter>();
            filters.add(new FilterLogType(logType));
            filters.add(new FilterNamesContain(sourceFilter, destFilter));
            EventCollectionSimple thisTypeCollection = events.filterAnd(filters);
            List<Double> xValues = new ArrayList<Double>();
            for (BasicEvent e : thisTypeCollection.getEvents()) {
                xValues.add(e.time - minTime);
            }
            if (thisTypeCollection.size() > 0) {
                dataset.addSeries(logType, xValues, thisTypeCollection, false, boxSelected);
                datasetIndices.put(logType, dataset.getSeriesCount()-1);

                double shapeSize = 6;
                double shapeHalfSize = shapeSize / 2;
                Shape s = new Ellipse2D.Double(-shapeHalfSize, -shapeHalfSize, shapeSize, shapeSize);
                Color c = Color.GRAY;
                BasicEvent testEvent = thisTypeCollection.getEvent(0);
                if (testEvent instanceof SpellInfoEvent) {
                    c = Color.BLUE;
                }
                if (testEvent instanceof DamageEvent) {
                    s = new Rectangle2D.Double(-shapeHalfSize, -shapeHalfSize, shapeSize, shapeSize);
                    c = Color.RED;
                    if (testEvent instanceof DamageMissedEvent) {
                        c = new Color(150, 0, 0);
                    }
                }
                if (testEvent instanceof HealingEvent) {
                    int[] pointsX = new int[]{0, (int) shapeHalfSize, 0, (int) -shapeHalfSize};
                    int[] pointsY = new int[]{(int) -shapeHalfSize, 0, (int) shapeHalfSize, 0};

                    s = new Polygon(pointsX, pointsY, 4);
                    c = Color.GREEN;
                }
                if (testEvent instanceof SpellAuraBaseEvent) {
                    int[] pointsX = new int[]{(int) -shapeHalfSize, (int) shapeHalfSize, 0};
                    int[] pointsY = new int[]{(int) -shapeHalfSize, (int) -shapeHalfSize, (int) shapeHalfSize};
                    s = new Polygon(pointsX, pointsY, 3);
                    c = lightBlueColor;
                }
                if (testEvent instanceof PowerEvent) {
                    c = Color.YELLOW;
                }
                seriesShapes.add(s);
                seriesColor.add(c);
            }
        }
        if (dataset.getSeriesCount() > 0) {
            chart = ChartFactory.createScatterPlot("Events", "Time", "Amount", dataset, PlotOrientation.VERTICAL, true, true, false);
            chart.getXYPlot().getRenderer().setBaseToolTipGenerator(new EventXYTooltipGenerator());
            for (int k = 0; k < seriesShapes.size(); k++) {
                chart.getXYPlot().getRenderer().setSeriesShape(k, seriesShapes.get(k));
                chart.getXYPlot().getRenderer().setSeriesPaint(k, seriesColor.get(k));
            }
            chartPanel = new ChartPanel(chart, true);
            chartPanel.setReshowDelay(100);
            chartPanel.setInitialDelay(10);
            chartPanel.setZoomAroundAnchor(true);
            ChartHelper.addScrollWheelZoom(chartPanel);
            chartPanel.setMaximumDrawHeight(1200);
            chartPanel.setMaximumDrawWidth(1900);
            center.add(chartPanel);
            validate();
        } else {
            chart = ChartFactory.createScatterPlot("Events", "Time", "Amount", new DefaultXYDataset(), PlotOrientation.VERTICAL, true, true, false);
            chartPanel = new ChartPanel(chart, true);
            center.add(chartPanel);
            validate();            
        }
    }

    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.getContentPane().add(new EventsPlotPanel());
        f.pack();
        f.setVisible(true);
    }
}
