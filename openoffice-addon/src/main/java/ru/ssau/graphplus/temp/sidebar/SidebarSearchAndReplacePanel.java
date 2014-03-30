package ru.ssau.graphplus.temp.sidebar;

import com.sun.star.accessibility.XAccessible;
import com.sun.star.awt.ActionEvent;
import com.sun.star.awt.ContainerWindowProvider;
import com.sun.star.awt.PosSize;
import com.sun.star.awt.Rectangle;
import com.sun.star.awt.WindowEvent;
import com.sun.star.awt.XActionListener;
import com.sun.star.awt.XButton;
import com.sun.star.awt.XContainerWindowProvider;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XControlContainer;
import com.sun.star.awt.XTextComponent;
import com.sun.star.awt.XWindow;
import com.sun.star.awt.XWindowListener2;
import com.sun.star.awt.XWindowPeer;
import com.sun.star.frame.XController;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XModel;
import com.sun.star.lang.DisposedException;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.text.XTextRange;
import com.sun.star.ui.LayoutSize;
import com.sun.star.ui.XSidebarPanel;
import com.sun.star.ui.XToolPanel;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.XSearchDescriptor;
import com.sun.star.util.XSearchable;
import com.sun.star.view.XSelectionSupplier;

/** This is the actual sidebar panel for the search and replace dialog.
 *  It implements two interfaces, the mandatory XToolPanel and the optional XSidebarPanel.
 *  
 *  In the constructor the panel registers listeners at the two buttons.  When
 *  the user clicks on one of the buttons then text is searched and possibly
 *  replaced in the current document.
 */ 
public class SidebarSearchAndReplacePanel
	implements XToolPanel, XSidebarPanel
{
    private final static String DIALOG_PATH = "vnd.sun.star.extension://org.apache.openoffice.sidebar.SidebarSearchDemo/dialogs/search_and_replace.xdl";
    private final static String CONTROL_NAME_BUTTON_SEARCH = "button_search";
    private final static String CONTROL_NAME_BUTTON_REPLACE = "button_replace";
    private final static String CONTROL_NAME_FIELD_SEARCH = "field_search";
    private final static String CONTROL_NAME_FIELD_REPLACE = "field_replace";
    
    
    public SidebarSearchAndReplacePanel (
			final XComponentContext xContext,
			final XWindow xParentWindow,
			final XFrame xFrame)
	{
        mxController = xFrame.getController();
        mxModel = mxController.getModel();
        maLastSearchResult = null;

        XWindowPeer xParentPeer = (XWindowPeer) UnoRuntime.queryInterface(XWindowPeer.class, xParentWindow);
        if (xParentPeer == null)
        {
            Log.Instance().println("SidebarSearchAndReplacePanel: no window peer of parent window");
            return;
        }

        // Create the dialog window.
        XContainerWindowProvider xProvider = ContainerWindowProvider.create(xContext);
        if (xProvider == null)
        {
            Log.Instance().println("SidebarSearchAndReplacePanel: could not create XContainerWindowProvider");
            return;
        }

        try
        {
            mxWindow = xProvider.createContainerWindow(DIALOG_PATH, "", xParentPeer, this);
        }
        catch (Exception aException)
        {
            Log.Instance().println("SidebarSearchAndReplacePanel: could not create container window");
            Log.Instance().PrintStackTrace(aException);
            mxWindow = null;
        }
        if (mxWindow == null)
        {
            Log.Instance().println("SidebarSearchAndReplacePanel: could not create container window");
            return;
        }
        
        // Add a window listener to get informed about disposing and size changes.
        mxWindow.addWindowListener(
                new XWindowListener2()
                {
                    @Override public void disposing(EventObject arg0) { mxWindow = null; }
                    @Override public void windowShown(EventObject arg0) {}
                    @Override public void windowResized(WindowEvent arg0) { ProcessResize(); }
                    @Override public void windowMoved(WindowEvent arg0) {}
                    @Override public void windowHidden(EventObject arg0) {}
                    @Override public void windowEnabled(EventObject arg0) {}
                    @Override public void windowDisabled(EventObject arg0) {}
                }
            );
        mxWindow.setVisible(true);
        	    
        // Setup callbacks for the search and the replace buttons.
        ConnectToButtons();
	}


    
    
    /** Add an XActionListener each to the search and the replace button.
     */
    private void ConnectToButtons ()
    {
        final XControlContainer xControlContainer = (XControlContainer)UnoRuntime.queryInterface(XControlContainer.class, mxWindow);
        if (xControlContainer==null)
            ThrowRuntimeException("SearchAndReplacePanel can not get access to dialog");

        // Lookup the buttons.
        final XButton xSearchButton = (XButton)UnoRuntime.queryInterface(XButton.class, xControlContainer.getControl(CONTROL_NAME_BUTTON_SEARCH));
        final XButton xReplaceButton = (XButton)UnoRuntime.queryInterface(XButton.class, xControlContainer.getControl(CONTROL_NAME_BUTTON_REPLACE));
        if (xSearchButton==null || xReplaceButton==null)
            ThrowRuntimeException("SearchAndReplacePanel can not find Search and Replace buttons in dialog");
        
        // Add an action listener for the search button.
        xSearchButton.addActionListener(new XActionListener()
        {
            @Override public void disposing (final EventObject aEvent) {}
            @Override public void actionPerformed (final ActionEvent aEvent)
            {
                final String sSearchText = GetTextFromTextComponent(CONTROL_NAME_FIELD_SEARCH);
                if (sSearchText != null)
                ProcessSearchAndReplace(sSearchText, null);
            }
        });

        // Add an action listener for the replace button.
        xReplaceButton.addActionListener(new XActionListener()
        {
            @Override public void disposing (final EventObject aEvent) {}
            @Override public void actionPerformed (final ActionEvent aEvent)
            {
                final String sSearchText = GetTextFromTextComponent(CONTROL_NAME_FIELD_SEARCH);
                final String sReplacementText = GetTextFromTextComponent(CONTROL_NAME_FIELD_REPLACE);
                if (sSearchText!=null && sReplacementText!=null)
                    ProcessSearchAndReplace(sSearchText, sReplacementText);
            }
        });
    }
    
    
    
    
    /** Search for the next occurence of the search string.
     *  When sReplacement is given (i.e. not null) then also replace the found text.
     */
    private void ProcessSearchAndReplace (
            final String sSearchText,
            final String sReplacementText)
    {
        final XSearchable xSearchable = (XSearchable)UnoRuntime.queryInterface(
                XSearchable.class,
                mxModel);
        if (xSearchable == null)
            return;
            
        // Setup the search.
        final XSearchDescriptor xDescriptor = xSearchable.createSearchDescriptor();
        xDescriptor.setSearchString(sSearchText);
              
        // Start or continue the search.
        final Object aSearchResult;
        if (maLastSearchResult == null)
            aSearchResult = xSearchable.findFirst(xDescriptor);
        else
            aSearchResult = xSearchable.findNext(maLastSearchResult.getEnd(), xDescriptor);
        
        // Convert the result to XTextRange.  This is the document type specific part.
        // Only Writer is supported at the moment.
        if (aSearchResult != null)
            maLastSearchResult = (XTextRange)UnoRuntime.queryInterface(XTextRange.class, aSearchResult);
        else
            maLastSearchResult = null;
        
        // Do the replacement (when the search text was found and a replacement is given).
        if (maLastSearchResult!=null && sReplacementText!=null)
        {
            maLastSearchResult.setString(sReplacementText);
        }

        // Select the found (or replaced) text.
        final XSelectionSupplier xSelectionSupplier = (XSelectionSupplier)UnoRuntime.queryInterface(
                XSelectionSupplier.class,
                mxController);
        if (xSelectionSupplier != null)
        {
            try
            {
                xSelectionSupplier.select(maLastSearchResult);
            }
            catch (IllegalArgumentException aException)
            {
                Log.Instance().PrintStackTrace(aException);
            }
        }
    }
    
    
    

    /** Lookup the XControl object in the dialog for the given name.
     */
    private XControl GetControl (final String sControlName)
    {
        final XControlContainer xControlContainer = (XControlContainer)UnoRuntime.queryInterface(XControlContainer.class, mxWindow);
        if (xControlContainer == null)
        {
            Log.Instance().println("SearchAndReplacePanel can not get access to dialog");
            return null;
        }
        else
            return xControlContainer.getControl(sControlName);
    }
    
    
    

    /** Look up a named control in the dialog, cast it to XTextComponent and return its text content.
     */
    private String GetTextFromTextComponent (final String sControlName)
    {
        final XTextComponent xSearchField = (XTextComponent)UnoRuntime.queryInterface(
                XTextComponent.class, 
                GetControl(sControlName));
        if (xSearchField == null)
        {
            Log.Instance().println(String.format("can not find XTextComponent '%s' in dialog", sControlName));
            return null;
        }
        
        return xSearchField.getText();
    }


    
    
    /** Return the XWindow associated with the control that is specified by the given name.
     */
    private XWindow GetControlWindow (final String sControlName)
    {
        final XControl xControl = GetControl(sControlName);
        if (xControl == null)
            return null;
        else
            return (XWindow)UnoRuntime.queryInterface(
                    XWindow.class,
                    xControl.getPeer());
    }
    
    
    
    
    @Override
	public XAccessible createAccessible(XAccessible arg0)
	{
		return (XAccessible)UnoRuntime.queryInterface(XAccessible.class, getWindow());
	}

	
	
	
	@Override
	public XWindow getWindow()
	{
        if (mxWindow == null)
        {
            throw new DisposedException("DemoPanel disposed", this);
        }

        return mxWindow;
	}
	
	
	
	
	/** Handle size changes of the dialog window by adapting the size of some of its controls.
	 */
	private void ProcessResize ()
	{
	    final Rectangle aWindowBox = mxWindow.getPosSize();
	    Log.Instance().printf("new window size is %dx%d\n", aWindowBox.Width, aWindowBox.Height);

	    // Get access to the search controls.  Try to shorten the text field to the width
	    // of the dialog (with border a both sides) but don't make it shorter than the button.
        final XWindow aSearchButton = GetControlWindow(CONTROL_NAME_BUTTON_SEARCH);
        final XWindow aSearchField = GetControlWindow(CONTROL_NAME_FIELD_SEARCH);
        Log.Instance().printf("search controls are '%s' and '%s'\n", aSearchButton.toString(), aSearchField.toString());
        if (aSearchButton!=null && aSearchField!=null)
        {
            final Rectangle aButtonBox = aSearchButton.getPosSize();
            final Rectangle aFieldBox = aSearchField.getPosSize();
            final int nNewWidth = Math.max(
                    aButtonBox.Width,
                    aWindowBox.Width - 2*aFieldBox.X);
            aSearchField.setPosSize(0,0,nNewWidth,0, PosSize.WIDTH);
        }

        // Do the same for the replacement controls.
        final XWindow aReplaceButton = GetControlWindow(CONTROL_NAME_BUTTON_REPLACE);
        final XWindow aReplaceField = GetControlWindow(CONTROL_NAME_FIELD_REPLACE);
        if (aReplaceButton!=null && aReplaceField!=null)
        {
            final Rectangle aButtonBox = aReplaceButton.getPosSize();
            final Rectangle aFieldBox = aReplaceField.getPosSize();
            final int nNewWidth = Math.max(
                    aButtonBox.Width,
                    aWindowBox.Width - 2*aFieldBox.X);
            aReplaceField.setPosSize(0,0,nNewWidth,0, PosSize.WIDTH);
        }
	}
	
	
	
	
	private void ThrowRuntimeException (final String sMessage)
	{
        Log.Instance().println("ERROR: "+sMessage);
        throw new com.sun.star.uno.RuntimeException(sMessage);
	}
	
	


	/** The XSidebarPanel is an optional interface.  We do not really need it for this demo but is included
	 *  to show how it works.
	 */
    @Override
    public LayoutSize getHeightForWidth (final int nNewWidth)
    {
        return new LayoutSize(
                // The height of this dialog is fixed.  Just return the value that was set during its construction.
                mxWindow.getPosSize().Height,
                // No upper bound.  The dialog can be enlarged until the bottom of the sidebar.
                -1,
                // No preferred size.
                0);
    }

    
    
    
    private XWindow mxWindow;
	private XController mxController;
	private XModel mxModel;
	private XTextRange maLastSearchResult;
}
