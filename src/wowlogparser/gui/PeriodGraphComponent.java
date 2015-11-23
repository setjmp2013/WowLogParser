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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.Transparency;
import java.awt.event.MouseEvent;
import java.awt.font.LineMetrics;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicBorders.ButtonBorder;
import wowlogparserbase.TimePeriod;

/**
 *
 * @author racy
 */
public class PeriodGraphComponent extends JPanel {

    public static final int SHAPE_CIRCLE = 0;
    public static final int SHAPE_SQUARE = 1;
    
    List<PeriodEvent> pEvents = new ArrayList<PeriodEvent>();
    List<TimePeriod> periods = new ArrayList<TimePeriod>();
    
    String name;
    double min, max, scaleTowardsPixels;
    boolean showMinAsZero;
    int penWidth;
    
    int textBorder = 1;
    int textHeight = 0;
    
    int tickBorder = 1;
    int tickHeight = 5;
    int aroundBorder = 2;
    
    int tickSpacingPixels = 50;
    Font font;

    boolean symbolEventAdded = false;
    
    boolean changed = true;
    Dimension previousSize = new Dimension(0, 0);
    Image image;

    public PeriodGraphComponent() {
        this("Default name", 50, 0, false, 1, 100, 2, 20);
    }
    
    public PeriodGraphComponent(String name, int preferredHeight, double min, double max, boolean showMinAsZero, double scaleTowardsPixels, int penWidth) {
        super();
        setDoubleBuffered(false);
        setPreferredSize(new Dimension(100, preferredHeight));
        this.name = name;
        this.min = min;
        this.max = max;
        this.showMinAsZero = showMinAsZero;
        this.scaleTowardsPixels = scaleTowardsPixels;
        this.penWidth = penWidth;
        font = new Font("Serif", Font.PLAIN, 10);
        setBorder(new BevelBorder(BevelBorder.LOWERED));
        initSize();
        changed = true;
    }    
    
    public PeriodGraphComponent(String name, int preferredHeight, double min, boolean showMinAsZero, double max, double scaleTowardsPixels, int penWidth, int tickSpacingPixels) {
        this(name, preferredHeight, min, max, showMinAsZero, scaleTowardsPixels, penWidth);
        this.tickSpacingPixels = tickSpacingPixels;
    }

    @Override
    public void setBorder(Border border) {
        super.setBorder(border);
        initSize();
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
        initSize();
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
        initSize();
    }

    public int getPenWidth() {
        return penWidth;
    }

    public void setPenWidth(int penWidth) {
        this.penWidth = penWidth;
        initSize();
    }

    public boolean isShowMinAsZero() {
        return showMinAsZero;
    }

    public void setShowMinAsZero(boolean showMinAsZero) {
        this.showMinAsZero = showMinAsZero;
        initSize();
    }

    public int getTextBorder() {
        return textBorder;
    }

    public void setTextBorder(int textBorder) {
        this.textBorder = textBorder;
        initSize();
    }

    public int getTickBorder() {
        return tickBorder;
    }

    public void setTickBorder(int tickBorder) {
        this.tickBorder = tickBorder;
        initSize();
    }

    public int getTickSpacingPixels() {
        return tickSpacingPixels;
    }

    public void setTickSpacingPixels(int tickSpacingPixels) {
        this.tickSpacingPixels = tickSpacingPixels;
        initSize();
    }

    public int getAroundBorder() {
        return aroundBorder;
    }

    public void setAroundBorder(int aroundBorder) {
        this.aroundBorder = aroundBorder;
        initSize();
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        Insets insets = getInsets();
        Dimension rawSize = getSize();
        Dimension size = new Dimension(rawSize.width - insets.left - insets.right, rawSize.height - insets.top - insets.bottom);
        int height = (int)size.getHeight();
        int width = (int)size.getWidth();
        int usefulHeight = height - tickHeight - tickBorder - textBorder - textHeight - 2*aroundBorder;
        int x = event.getX() - insets.left;
        int y = event.getY() - insets.top;
        for (PeriodEvent e : pEvents) {
            if (e.getType() != PeriodEvent.TYPE_SHAPE) {
                continue;
            }
            double diameterX = e.getSizeX() * usefulHeight / scaleTowardsPixels;
            double posX = (x - aroundBorder) / scaleTowardsPixels + min;
            double distX = Math.abs(posX - e.getValue());
            if (distX <= diameterX/2) {
                if (e.getText() != null) {
                    return e.getText();
                }
            }
        }
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(1);
        for (TimePeriod tp : periods) {
            double posX = (x - aroundBorder) / scaleTowardsPixels + min;
            if (tp.contains(posX)) {
                String s = "<html><body>" + "<b>" + getToolTipText() + "</b>" + "<table>";
                s += "<tr><td>Start</td>" + "<td>" + nf.format(tp.startTime - min) + "</td></tr>";
                s += "<tr><td>Stop</td>" + "<td>" + nf.format(tp.endTime - min) + "</td></tr>";
                s += "<tr><td>Duration</td>" + "<td>" + nf.format(tp.endTime - tp.startTime) + "</td></tr>";
                s += "</table></body></html>";
                return s;
            }
        }
        return super.getToolTipText(event);
    }
    
    public void setScaleTowardsPixels(double scale) {
        scaleTowardsPixels = scale;
        initSize();
        changed = true;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D)g;
        Rectangle vrect = getVisibleRect();
        Insets insets = getInsets();
        Dimension rawSize = getSize();
        Dimension size = new Dimension(rawSize.width - insets.left - insets.right, rawSize.height - insets.top - insets.bottom);
        int height = (int)size.getHeight();
        int width = (int)size.getWidth();
        g2d.setPaintMode();
        
        g.setFont(font);
        if (textHeight == 0) {            
            textHeight = (int)g2d.getFontMetrics().getHeight();
        }

        int usefulHeight = height - tickHeight - tickBorder - textBorder - textHeight - 2*aroundBorder;
        
        if ( changed || (!previousSize.equals(size)) ) {
            changed = false;
            previousSize = new Dimension(size);
            image = this.createImage(width, height);
            Graphics2D img2d = (Graphics2D)image.getGraphics();
            img2d.setFont(font);
            img2d.setPaintMode();
            
            //Paint ticks every 10 units.
            img2d.setStroke(new BasicStroke(1));
            img2d.setColor(Color.BLACK);
            double tickPos = aroundBorder;
            double tickVal = min;
            int tickSpacing = (int) (tickSpacingPixels / scaleTowardsPixels);
            tickSpacing = tickSpacing < 1 ? 1 : tickSpacing;
            while (tickPos < width - aroundBorder && tickVal <= max) {
                img2d.drawLine((int) tickPos, aroundBorder + usefulHeight + tickBorder, (int) tickPos, aroundBorder + usefulHeight + tickBorder + tickHeight);

                //Draw text
                String s;
                if (showMinAsZero) {
                    s = Integer.toString((int) (tickVal - min));
                } else {
                    s = Integer.toString((int) tickVal);
                }
                img2d.getFontMetrics().stringWidth(s);
                LineMetrics lm = font.getLineMetrics(s, img2d.getFontRenderContext());
                img2d.drawString(s, (int) tickPos - img2d.getFontMetrics().stringWidth(s) / 2, height - aroundBorder);

                tickPos += tickSpacing * scaleTowardsPixels;
                tickVal += tickSpacing;
            }

            //Paint the content
            img2d.setStroke(new BasicStroke(1));
            img2d.setColor(Color.YELLOW);
            for (int k = 0; k < periods.size(); k++) {
                TimePeriod p = periods.get(k);
                double start = (p.startTime - min) * scaleTowardsPixels + aroundBorder;
                double stop = (p.endTime - min) * scaleTowardsPixels + aroundBorder;
                stop = stop >= width - aroundBorder ? width - aroundBorder - 1 : stop;
                stop = stop < aroundBorder ? aroundBorder : stop;
                img2d.fillRect((int) start, aroundBorder, (int) Math.round(stop - start), usefulHeight);
            }
            img2d.setStroke(new BasicStroke(penWidth));
            for (int k = 0; k < pEvents.size(); k++) {
                PeriodEvent p = pEvents.get(k);
                double pos = (p.getValue() - min) * scaleTowardsPixels + aroundBorder;
                switch (p.getType()) {
                    case PeriodEvent.TYPE_START:
                    case PeriodEvent.TYPE_STOP:
                        img2d.setColor(p.getColor());
                        img2d.drawLine((int) Math.round(pos), aroundBorder, (int) Math.round(pos), aroundBorder + usefulHeight);
                        break;
                    case PeriodEvent.TYPE_SHAPE:
                        img2d.setColor(p.getColor());
                        switch (p.getShape()) {
                            case SHAPE_CIRCLE:
                                int diameterX = (int) (usefulHeight * p.getSizeX());
                                diameterX = diameterX / 2 * 2;
                                int diameterY = (int) (usefulHeight * p.getSizeY());
                                diameterY = diameterY / 2 * 2;
                                img2d.fillOval((int) pos - diameterX / 2, aroundBorder + usefulHeight / 2 - diameterY / 2, diameterX, diameterY);
                                break;
                            case SHAPE_SQUARE:
                                int baseX = (int) (usefulHeight * p.getSizeX());
                                baseX = baseX / 2 * 2;
                                int baseY = (int) (usefulHeight * p.getSizeY());
                                baseY = baseY / 2 * 2;
                                img2d.fillRect((int) pos - baseX / 2, aroundBorder + usefulHeight / 2 - baseY / 2, baseX, baseY);
                                break;
                        }
                        break;
                }
            }
        }
        
        g2d.drawImage(image, insets.left, insets.left, null);
        
        g2d.setColor(Color.BLACK);
        int pos = 0;
        int nameWidth = g2d.getFontMetrics().stringWidth(name);
        int center = (vrect.x + vrect.x + vrect.width)/2;
        g2d.drawString(name, insets.left + center - nameWidth/2, insets.top + aroundBorder + usefulHeight/2 + textHeight/2);
    }
    
    public void addStart(Color c, double pos) {
        if (pos >= min && pos <= max) {
            pEvents.add(new PeriodEvent(c, PeriodEvent.TYPE_START, pos));
        }
        changed = true;
    }

    public void addStop(Color c, double pos) {
        if (pos >= min && pos <= max) {
            pEvents.add(new PeriodEvent(c, PeriodEvent.TYPE_STOP, pos));
        }
        changed = true;
    }

    public void addStart(double pos) {
        addStart(Color.GREEN, pos);
        changed = true;
    }

    public void addStop(double pos) {
        addStop(Color.RED, pos);
        changed = true;
    }
    
    public void addEvent(Color c, int shape, double pos, double sizeX, double sizeY, String text) {
        if (pos >= min && pos <= max) {
            pEvents.add(new PeriodEvent(c, PeriodEvent.TYPE_SHAPE, pos, shape, sizeX, sizeY, text));
            if (!symbolEventAdded) {
                symbolEventAdded = true;
                setToolTipText("Hover over a symbol to get information");
            }
        }        
        changed = true;
    }
    
    public void compilePeriods() {
        List<TimePeriod> tempPeriods = new ArrayList<TimePeriod>();
        int firstIndex = 0;
        while(true) {
            boolean foundStart = false;
            boolean foundStop = false;
            int startI = 0;
            int stopI = 0;
            for (int k=firstIndex; k<pEvents.size(); k++) {
                //If the first event is a stop event, then assume a period from the start to this stop event.
                if (k == 0 && !foundStart && !foundStop) {
                    if (pEvents.get(k).getType() == PeriodEvent.TYPE_STOP) {
                        TimePeriod p = new TimePeriod(min, pEvents.get(k).getValue());
                        tempPeriods.add(p);
                        break;
                    }
                }
                if (!foundStart) {
                    if (pEvents.get(k).getType() == PeriodEvent.TYPE_START) {
                        foundStart = true;
                        startI = k;
                    }
                } else {
                    if (!foundStop) {
                        if (pEvents.get(k).getType() == PeriodEvent.TYPE_STOP) {
                            foundStop = true;
                            stopI = k;
                        }
                    }
                }
                
                if (foundStart && foundStop) {
                    break;
                }
            }
            
            if (foundStart && foundStop) {
                TimePeriod p = new TimePeriod(pEvents.get(startI).getValue(), pEvents.get(stopI).getValue());
                tempPeriods.add(p);
            } else if (foundStart) {
                TimePeriod p = new TimePeriod(pEvents.get(startI).getValue(), max);
                tempPeriods.add(p);                
            }
            
            firstIndex++;
            if (firstIndex >= pEvents.size()) {
                break;
            }
        }
        
        periods = TimePeriod.mergeTimePeriods(tempPeriods);
        changed = true;
    }

    public void initSize() {
        Dimension d = getPreferredSize();
        Insets insets = getInsets();
        d.width = (int)( 2*aroundBorder + (max-min)*scaleTowardsPixels + insets.left + insets.right);
        setPreferredSize(d);
        setSize(d);
        changed = true;
    }
    
    class PeriodEvent {
        public static final int TYPE_NONE = 0;
        public static final int TYPE_START = 1;
        public static final int TYPE_STOP = 2;
        public static final int TYPE_SHAPE = 3;
        protected int type;
        protected double value;
        protected Color color;
        protected int shape;
        protected double sizeX;
        protected double sizeY;
        protected String text;

        public PeriodEvent(Color color, int type, double value, int shape, double sizeX, double sizeY, String text) {
            this.color = color;
            this.type = type;
            this.value = value;
            this.shape = shape;
            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.text = text;
        }

        public PeriodEvent(Color color, int type, double value) {
            this.color = color;
            this.type = type;
            this.value = value;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }

        public Color getColor() {
            return color;
        }

        public void setColor(Color color) {
            this.color = color;
        }

        public int getShape() {
            return shape;
        }

        public void setShape(int shape) {
            this.shape = shape;
        }

        public double getSizeX() {
            return sizeX;
        }

        public void setSizeX(double size) {
            this.sizeX = size;
        }

        public double getSizeY() {
            return sizeY;
        }

        public void setSizeY(double sizeY) {
            this.sizeY = sizeY;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
                
    }    
}
