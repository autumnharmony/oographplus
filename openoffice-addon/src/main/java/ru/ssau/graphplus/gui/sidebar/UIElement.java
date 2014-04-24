package ru.ssau.graphplus.gui.sidebar;

import com.sun.star.awt.XWindow;
import com.sun.star.frame.XFrame;
import com.sun.star.ui.UIElementType;
import com.sun.star.ui.XUIElement;

/**
 * A simple implementation of the XUIElement interface.
 */
public class UIElement
        implements XUIElement {

    private final XWindow window;
    private final XFrame mxFrame;
    private final String msResourceURL;
    private final PanelBase maPanel;

    public UIElement(
            final String sResourceURL,
            final PanelBase aPanel) {
        mxFrame = null;
        msResourceURL = sResourceURL;
        maPanel = aPanel;
        window = maPanel.getWindow();
    }

    @Override
    public XFrame getFrame() {
        return mxFrame;
    }

    @Override
    public Object getRealInterface() {
        return window;
    }

    @Override
    public String getResourceURL() {
        return msResourceURL;
    }

    @Override
    public short getType() {
        return UIElementType.TOOLPANEL;
    }
}
