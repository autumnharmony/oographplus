package ru.ssau.graphplus.gui.sidebar;

import com.sun.star.frame.XFrame;
import com.sun.star.ui.UIElementType;
import com.sun.star.ui.XUIElement;

/** A simple implementation of the XUIElement interface.
 */
public class UIElement
	implements XUIElement
{
	public UIElement (
			final String sResourceURL,
			final PanelBase aPanel)
	{
		mxFrame = null;
		msResourceURL = sResourceURL;
		maPanel = aPanel;
	}

    public UIElement(XFrame mxFrame, String msResourceURL, PanelBase maPanel) {
        this.mxFrame = mxFrame;
        this.msResourceURL = msResourceURL;
        this.maPanel = maPanel;
    }

    @Override
	public XFrame getFrame ()
	{
		return mxFrame;
	}
	
	
	

	@Override
	public Object getRealInterface ()
	{
		return maPanel;
	}

	
	
	
	@Override
	public String getResourceURL ()
	{
		return msResourceURL;
	}
	
	
	

	@Override
	public short getType()
	{
		return UIElementType.TOOLPANEL;
	}

	
	
	
	private final XFrame mxFrame;
	private final String msResourceURL;
	private final PanelBase maPanel;
}
