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
package wowlogparser.gui.components;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author racy
 */
public class SingleAuraDisplayRuler extends JPanel {

    private double globalStartTime = -1;
    private double globalEndTime = -1;
    private double tickStart = -1;
    private double tickSpacing = -1;
    private double pixelsPerSecond = 5;
    private int tickHeight = 5;
    private int heightSpacing = 2;

    private Font font;
    private int fontHeight;

    private boolean initialized = false;

    public SingleAuraDisplayRuler() {
        setLayout(null);
        setBorder(null);
        //setBackground(Color.black);

        BufferedImage im = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = im.createGraphics();
        font = new Font("Dialog", Font.PLAIN, 12);
        TextLayout layout = new TextLayout("10", font, g2d.getFontRenderContext());
        Rectangle2D bounds = layout.getBounds();
        this.fontHeight = (int)Math.round(bounds.getHeight());
    }

    /**
     * Call after the component is in a window.
     * @param startTime
     * @param endTime
     * @param tickStart
     * @param tickSpacing
     */
    public void setExtents(double startTime, double endTime, double tickStart, double tickSpacing) {
        this.globalStartTime = startTime;
        this.globalEndTime = endTime;
        this.tickStart = tickStart;
        this.tickSpacing = tickSpacing;
        setPreferredSize(new Dimension((int) Math.ceil((globalEndTime - globalStartTime) * pixelsPerSecond) + 5, tickHeight + heightSpacing + fontHeight + heightSpacing));
        setSize(new Dimension((int) Math.ceil((globalEndTime - globalStartTime) * pixelsPerSecond) + 5, tickHeight + heightSpacing + fontHeight + heightSpacing));
    }

    public void setStartTime(double startTime) {
        this.globalStartTime = startTime;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D)g;

        NumberFormat nf = NumberFormat.getInstance();
        nf.setGroupingUsed(false);
        nf.setMaximumFractionDigits(0);
        //g2d.setColor(Color.black);
        g2d.setFont(font);
        double tickTime = tickStart;
        while(tickTime < globalEndTime) {
            int xPos = getXPos(tickTime);
            g2d.drawLine(xPos, 0, xPos, tickHeight);
            String s = nf.format(tickTime);
            TextLayout layout = new TextLayout(s, font, g2d.getFontRenderContext());
            Rectangle2D bounds = layout.getBounds();
            double fontX = xPos + bounds.getX() - bounds.getWidth()/2;
            double fontY = tickHeight + heightSpacing - bounds.getY();
            Rectangle rect = new Rectangle((int)(fontX + bounds.getX()-1), (int)(fontY + bounds.getY()-1), (int)bounds.getWidth()+2, (int)bounds.getHeight()+2);
            if(g2d.hitClip(rect.x, rect.y, rect.width, rect.height)) {
                layout.draw(g2d, (float)fontX, (float)fontY);
            } else {
            }
            tickTime += tickSpacing;
        }
    }

    private int getXPos(double time) {
        return (int) Math.round((time - globalStartTime) * pixelsPerSecond);
    }

    public static void main(String[] args) {
        SingleAuraDisplayRuler r = new SingleAuraDisplayRuler();
        r.setExtents(0, 100, 10, 10);
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(400, 400);
        f.getContentPane().add(r);
        f.setVisible(true);
        int a = 0;
    }
}
