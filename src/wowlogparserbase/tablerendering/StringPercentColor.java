/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wowlogparserbase.tablerendering;

import java.awt.Color;

/**
 *
 * @author racy
 */
public class StringPercentColor {
    String text;
    double percent;
    Color color;

    public StringPercentColor(String text, double percent, Color color) {
        this.text = text;
        this.percent = percent;
        this.color = color;
    }

    public double getPercent() {
        return percent;
    }

    public void setPercent(float percent) {
        this.percent = percent;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return getText();
    }
    
}
