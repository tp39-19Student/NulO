package display;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

public class CustomScrollBarUI extends BasicScrollBarUI {
    Color backgroundColor;
    Color foregroundColor;

    CustomScrollBarUI(Color backgroundColor, Color foregroundColor) {
        super();
        this.backgroundColor = backgroundColor;
        this.foregroundColor = foregroundColor;
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        JButton ret = new JButton();
        ret.setPreferredSize(new Dimension(0, 0));
        return ret;
    }
    @Override
    protected JButton createIncreaseButton(int orientation) {
        JButton ret = new JButton();
        ret.setPreferredSize(new Dimension(0, 0));
        return ret;
    }

    @Override
    protected void configureScrollBarColors() {
        super.configureScrollBarColors();
        trackColor = backgroundColor;
        thumbColor = foregroundColor;
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        if(thumbBounds.isEmpty() || !scrollbar.isEnabled())     {
            return;
        }

        g.setColor(thumbColor);
        g.fillRoundRect(thumbBounds.x, thumbBounds.y + 2, thumbBounds.width - 3, thumbBounds.height - 5, 10, 10);

    }
}
