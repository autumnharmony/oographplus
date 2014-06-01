package ru.ssau.graphplus.gui.sidebar;

import java.util.Vector;

import com.sun.star.accessibility.XAccessible;
import com.sun.star.awt.*;
import com.sun.star.lang.DisposedException;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XEventListener;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.ui.LayoutSize;
import com.sun.star.ui.XSidebarPanel;
import com.sun.star.ui.XToolPanel;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

public abstract class PanelBase
	implements XToolPanel, XWindowListener, XSidebarPanel, XComponent, XDialogEventHandler, XContainerWindowEventHandler
{
	/** This is the one method that a derived class has to implement:
	 *  how to react to size changes of the content window.
	 */
	abstract protected void Layout (final Size aWindowSize);
		
	
	protected PanelBase ()
	{
		maDisposeListeners = new Vector<XEventListener>();
	}
	
	
	
	
	protected void Initialize (
			final XWindow xParentWindow,
			final XComponentContext xContext)
	{

		
		// Create the content window of the panel.
        try
        {
	        XWindowPeer xParentPeer = (XWindowPeer) UnoRuntime.queryInterface(XWindowPeer.class, xParentWindow);
	        if (xParentPeer != null)
	        {
	        	xParentWindow.addWindowListener(this);
	
	        	if (xContext == null)
	        		throw new RuntimeException("got null XContext");
	        	XMultiComponentFactory xFactory = xContext.getServiceManager();
	        	if (xFactory == null)
	        		throw new RuntimeException("can not acquire factor from XContext");

	        	XToolkit xToolkit = (XToolkit)UnoRuntime.queryInterface(
	        			XToolkit.class,
	        			xFactory.createInstanceWithContext("com.sun.star.awt.Toolkit", xContext));
	        	WindowDescriptor aWindowDescriptor = new WindowDescriptor(
	        			WindowClass.CONTAINER,
	        			"",
	        			xParentPeer,
	        			(short)-1, // parent index not available
	        			new Rectangle(0,0,10,10),
	        			WindowAttribute.SIZEABLE | WindowAttribute.MOVEABLE | WindowAttribute.NODECORATION);
	        	mxWindow = (XWindow)UnoRuntime.queryInterface(
	        			XWindow.class,
	        			xToolkit.createWindow(aWindowDescriptor));
	        	if (mxWindow == null)
	        		throw new RuntimeException("can not create XWindow for parent "+xParentPeer);

	        	// Make the background transparent.  The slide show paints its own background.
	        	final XWindowPeer xPeer = (XWindowPeer)UnoRuntime.queryInterface(
	        			XWindowPeer.class,
	        			mxWindow);
	        	if (xPeer != null)
	        	{
	        		// Make the window background transparent to avoid some flickering,
	        		// when first the window background and then the canvas content is painted.
	        		xPeer.setBackground(0xffffff);
	        	}
	        			
	        	mxWindow.setVisible(true);
	        }
        }
        catch (Exception aException)
        {
        	throw new RuntimeException("can not create window for Panel: "+aException.getMessage());
        }
	}

	
	
	
	//----- Implementation of UNO interface XWindowListener -----
	
	@Override
	public void windowHidden (final EventObject aEvent)
	{
		CallLayout(0,0);
	}

	
	
	
	@Override
	public void windowMoved (final WindowEvent aEvent)
	{
		CallLayout(aEvent.Width, aEvent.Height);
	}

	
	
	
	@Override
	public void windowResized (final WindowEvent aEvent)
	{
		CallLayout(aEvent.Width, aEvent.Height);
	}

	
	
	
	@Override
	public void windowShown (final EventObject aEvent)
	{
		CallLayout(mxWindow.getPosSize().Width, mxWindow.getPosSize().Height);
	}


	
	
	@Override
	public void disposing (final EventObject aEvent)
	{
		mxWindow = null;
	}


	
	
	//----- Implementation of UNO interface XToolPanel -----
	
	@Override
	public XAccessible createAccessible(XAccessible arg0)
	{
		return (XAccessible)UnoRuntime.queryInterface(XAccessible.class, getWindow());
	}

	
	
	
	@Override
	public XWindow getWindow()
	{
        if (mxWindow == null)
            throw new DisposedException("Panel is already disposed", this);

        return mxWindow;
	}

	
	
	//----- Implementation of UNO interface XSidebarPanel -----
	
	@Override
	public LayoutSize getHeightForWidth (final int nWidth)
	{
		return new LayoutSize(0,0,0);
	}

	
	
	
	//----- Implementation of UNO interface XComponent -----
	
	@Override
	public void dispose ()
	{
		EventObject aEvent = new EventObject(this);
		for (final XEventListener xListener : maDisposeListeners)
			xListener.disposing(aEvent);
	}

	
	
	
	@Override
	public void addEventListener (final XEventListener xListener)
	{
		maDisposeListeners.add(xListener);
	}




	@Override
	public void removeEventListener (final XEventListener xListener)
	{
		maDisposeListeners.remove(xListener);
	}

	
	

	//----- private methods -----
	
	private void CallLayout (final int nWidth, final int nHeight)
	{
		try
		{
			Layout(new Size(nWidth, nHeight));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	

	
	
	protected XWindow mxWindow;
	private final Vector<XEventListener> maDisposeListeners;
}
