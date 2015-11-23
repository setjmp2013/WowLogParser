/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wowlogparserbase.tablerendering;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.plaf.basic.BasicLabelUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author racy
 */
public class StringPercentColorTableCellRenderer extends DefaultTableCellRenderer implements TableCellRenderer {

    private Color color;
    private double percent;
    JTable table;
    int row;
    int column;
    Object value;
    boolean isSelected;
    boolean hasFocus;
    
    public StringPercentColorTableCellRenderer() {
    }
    
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        StringPercentColor s = (StringPercentColor) value;
        color = s.getColor();
        percent = s.getPercent();
        this.table = table;
        this.value = value;
        this.isSelected = isSelected;
        this.hasFocus = hasFocus;
        this.row = row;
        this.column = column;
        setText(s.getText());
        return this;
    }

    @Override
    protected void paintComponent(Graphics g) {
        setOpaque(true);
        super.paintComponent(g);
        setOpaque(false);
        g.setPaintMode();
        g.setColor(getBackground());
        int left = getInsets().left;
        int top = getInsets().top;
        int w = getSize().width - getInsets().left - getInsets().right;
        int h = getSize().height - getInsets().top - getInsets().bottom;
        g.fillRect(left, top, w, h);
        
        g.setColor(color);
        double percentWidth = percent/100.0 * (float)w;
        g.fillRect(left, top, (int)percentWidth, h);
        super.paintComponent(g);
    }

}
