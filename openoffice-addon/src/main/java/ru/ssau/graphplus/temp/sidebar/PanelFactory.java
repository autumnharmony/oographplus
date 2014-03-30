package ru.ssau.graphplus.temp.sidebar;

import com.sun.star.awt.XWindow;
import com.sun.star.beans.PropertyValue;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.frame.XFrame;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.XInitialization;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.ui.XUIElement;
import com.sun.star.ui.XUIElementFactory;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.Exception;
import com.sun.star.uno.RuntimeException;
import com.sun.star.uno.XComponentContext;

public class PanelFactory
	implements XUIElementFactory, XServiceInfo, XInitialization
{
    // This service name (but not this variable) is used in Factories.xcu. 
    // If you change one then change the other as well.
    // The variable is used in the Component class to register this factory as service so that it can be used by the sidebar.
	public final static String __serviceName = "org.apache.openoffice.sidebar.SidebarDemoPanelFactory";
	
	// This URL prefix (but not this variable) is used in Sidebar.xcu by ImplementationURL values.
	// If you change one then change the other as well.
	private final static String msURLPrefix = "private:resource/toolpanel/SidebarDemoPanelFactory/";

   
    
    
    public PanelFactory (final XComponentContext xContext)
    {
    	mxContext = xContext;
    }
    

    

	@Override
	public XUIElement createUIElement (
			final String sResourceURL,
			final PropertyValue[] aArgumentList)
			throws NoSuchElementException, IllegalArgumentException 
	{
    	Log.Instance().println("createUIElement "+sResourceURL);
        if ( ! sResourceURL.startsWith(msURLPrefix))
        {
            throw new NoSuchElementException(sResourceURL, this);
        }
        
        // Retrieve values from the given arguments.
        XWindow xParentWindow = null;
        XFrame xFrame = null;
        for (final PropertyValue aValue : aArgumentList)
        {
            switch (aValue.Name)
            {
                case "ParentWindow":
                {
                    try
                    {
                        xParentWindow = (XWindow)AnyConverter.toObject(XWindow.class, aValue.Value);
                    }
                    catch (IllegalArgumentException aException)
                    {
                        Log.Instance().PrintStackTrace(aException);
                    }
                }
                break;
                
                case "Frame":
                {
                    try
                    {
                        xFrame = (XFrame)AnyConverter.toObject(XFrame.class, aValue.Value);
                    }
                    catch (IllegalArgumentException aException)
                    {
                        Log.Instance().PrintStackTrace(aException);
                    }
                }
                break;
                
                // Other values that are available but not used here are:
                case "SfxBindings":     // This is used as a hack and works only when not crossing compartment boundaries (ie only in local C++ extensions).
                case "Theme":           // An XPropertySet with all the sidebar theme values in it.
                case "Sidebar":         // A com.sun.star.ui.XSidebar object that can be used to trigger layouts of the sidebar.
                                        // Use this when the height of your panel changes.
                case "Canvas":          // A XCanvas object.  This is only provided when the 'WantsCanvas' flag in Sidebar.xcu has been set for the panel.
                case "ApplicationName": // The application part of the current sidebar context.
                case "ContextName":     // The context part of the current sidebar context.
            }
        }
        
        // Make sure that parent window and frame where provided in the arguments.
        if (xParentWindow == null)
        {
            final String sMessage = "No parent given provided to the UIElement factory. Cannot create tool panel.";
            Log.Instance().println(sMessage);
            throw new IllegalArgumentException(sMessage, this, (short) 2);
        }
        if (xFrame == null)
        {
            final String sMessage = "No frame given to the UIElement factory. Cannot create tool panel.";
            Log.Instance().println(sMessage);
            throw new IllegalArgumentException(sMessage, this, (short) 2);
        }

        // The panel name is the part of the URL that follows the prefix.
        final String sPanelName = sResourceURL.substring(msURLPrefix.length());

        final Object aPanel;
        switch (sPanelName)
        {
            case "SearchAndReplacePanel":
                // Create the panel and the XUIElement wrapper.
                aPanel = new SidebarSearchAndReplacePanel(mxContext, xParentWindow, xFrame);
                break;
                
            // Add more case statements for more panel types.

            default:
                throw new RuntimeException("factory does not support panel '"+sPanelName+"'");
        }
         
        // Finally create the XUIElement wrapper and return the wrapped panel.
        return new PanelUIElement(xParentWindow, sResourceURL, aPanel);
	}

	
	
	
	@Override
	public String getImplementationName ()
	{
        return msImplementationName;
	}

	
	
	
	@Override
	public String[] getSupportedServiceNames ()
	{
	    return maServiceNames;
	}
	
	
	

	@Override
	public boolean supportsService (final String sServiceName)
	{
		for (final String sSupportedServiceName : maServiceNames)
			if (sSupportedServiceName.equals(sServiceName))
                return true;
		return false;
	}
	
	
	

	@Override
	public void initialize (final Object[] aArgumentList)
			throws Exception
	{
	}


	

    private static final String msImplementationName = PanelFactory.class.getName();
    private static final String[] maServiceNames = { __serviceName };
    private final XComponentContext mxContext;
}
