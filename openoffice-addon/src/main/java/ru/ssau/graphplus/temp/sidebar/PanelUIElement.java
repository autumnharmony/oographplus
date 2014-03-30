package ru.ssau.graphplus.temp.sidebar;

import com.sun.star.awt.XWindow;
import com.sun.star.frame.XFrame;
import com.sun.star.ui.UIElementType;
import com.sun.star.ui.XUIElement;

/** Generic XUIElement implementation for sidebar panels.
 * 
 *  Can be used for any panel implementation without changes.
 */
public class PanelUIElement
	implements XUIElement
{
	public PanelUIElement (
			final XWindow xParentWindow,
			final String sResourceURL,
			final Object aPanel)
	{
		mxFrame = null;
		msResourceURL = sResourceURL;
		
		maPanel = aPanel;
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
	private final Object maPanel;
}
