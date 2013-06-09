package ru.ssau.graphplus.gui;

import com.sun.star.awt.Rectangle;
import com.sun.star.awt.XMessageBox;
import com.sun.star.awt.XMessageBoxFactory;
import com.sun.star.awt.XWindowPeer;
import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.*;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.XComponent;
import com.sun.star.lib.uno.helper.ComponentBase;
import com.sun.star.lib.uno.helper.InterfaceContainer;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.Type;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.URL;
import com.sun.star.util.XURLTransformer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class StatusBarInterceptor extends ComponentBase
        implements XDispatchProviderInterceptor, XInterceptorInfo, XDispatch, XStatusListener
{
    private final XComponentContext m_xContext;
    private final XFrame m_xFrame;
    private XDispatchProvider m_xMaster;
    private XDispatchProvider m_xSlave;
    private XMessageBoxFactory m_xMsgBoxFactory;
    private XDispatch m_xOriginalDispatch;
    private boolean m_bRegistered;
    private static final ArrayList<String> m_lCommands;

    static
    {
        m_lCommands = new ArrayList(2);
        m_lCommands.add(".uno:Context");
    }
    private static final String[] m_sCommands = (String[])m_lCommands.toArray(new String[m_lCommands.size()]);

    static final Type STATUS_LISTENER_TYPE = new Type(XStatusListener.class);
    private boolean m_bListening;
    private static URL m_aURL = null;
    private String m_sStatus = "";

    static List<WeakReference<StatusBarInterceptor>> weakReference = new ArrayList<>();

    public StatusBarInterceptor(XComponentContext context, XFrame xFrame) {

        weakReference.add(new WeakReference<>(this));

        Logger.getAnonymousLogger().info("StatusBarInterceptor ctor");

        this.m_xContext = context;
        this.m_xFrame = xFrame;
        this.m_xMaster = null;
        this.m_xSlave = null;
        this.m_xMsgBoxFactory = null;
        this.m_bRegistered = false;
        this.m_bListening = false;
        this.m_xOriginalDispatch = null;

        if (m_aURL == null) {
            m_aURL = new URL();
            m_aURL.Complete = ".uno:Context";
            try {
                URL[] aURLs = { m_aURL };
                XURLTransformer xTransformer = UnoRuntime.queryInterface(XURLTransformer.class, this.m_xContext.getServiceManager().createInstanceWithContext("com.sun.star.util.URLTransformer", this.m_xContext));

                xTransformer.parseStrict(aURLs);
                m_aURL = aURLs[0];
            } catch (java.lang.Exception ex) {
                Logger.getLogger(StatusBarInterceptor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {
            this.m_xMsgBoxFactory = UnoRuntime.queryInterface(XMessageBoxFactory.class, this.m_xContext.getServiceManager().createInstanceWithContext("com.sun.star.awt.Toolkit", this.m_xContext));
        }
        catch (com.sun.star.uno.Exception ex)
        {
            Logger.getLogger(StatusBarInterceptor.class.getName()).log(Level.SEVERE, null, ex);
        }

        XDispatchProviderInterception xInterception = UnoRuntime.queryInterface(XDispatchProviderInterception.class, xFrame);

        if (xInterception != null) {
            xInterception.registerDispatchProviderInterceptor(this);

            this.m_bRegistered = true;
        }

        XDispatchProvider xProvider = UnoRuntime.queryInterface(XDispatchProvider.class, this.m_xFrame);

        if (xProvider != null) {
            XDispatch xDispatch = xProvider.queryDispatch(m_aURL, "_self", 2);

            startListening(xDispatch);
        }
    }

    private void startListening(XDispatch xDispatch) {
        if (xDispatch == null) {
            return;
        }
        if (this.m_bListening) {
            if (this.m_xOriginalDispatch != null) {
                try {
                    this.m_xOriginalDispatch.removeStatusListener(this, m_aURL);
                    this.m_xOriginalDispatch = null;
                } catch (java.lang.Exception ex) {
                    Logger.getLogger(StatusBarInterceptor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            this.m_bListening = false;
        }
        try {
            xDispatch.addStatusListener(this, m_aURL);
            this.m_xOriginalDispatch = xDispatch;
            this.m_bListening = true;
        } catch (java.lang.Exception ex) {
            Logger.getLogger(StatusBarInterceptor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public synchronized XFrame getFrame() {
        return this.m_xFrame;
    }

    public synchronized boolean isIntercepting() {
        return this.m_bRegistered;
    }

    public synchronized void stopIntercepting() {
        if (!this.m_bRegistered)
            return;
        try
        {
            XDispatchProviderInterception xInterception = UnoRuntime.queryInterface(XDispatchProviderInterception.class, this.m_xFrame);

            if (xInterception != null) {
                xInterception.releaseDispatchProviderInterceptor(this);

                this.m_bRegistered = false;
            }
        }
        catch (java.lang.Exception e)
        {
        }
    }

    public XDispatch queryDispatch(URL URL, String TargetFrameName, int SearchFlags)
    {
        XDispatch xRet = null;
        synchronized (this) {
            if (m_lCommands.contains(URL.Complete)) {
                XDispatch xDispatch = this.m_xSlave.queryDispatch(URL, TargetFrameName, SearchFlags);

                if (xDispatch != this.m_xOriginalDispatch) {
                    startListening(xDispatch);
                }
                return this;
            }
            if (this.m_xSlave != null) {
                xRet = this.m_xSlave.queryDispatch(URL, TargetFrameName, SearchFlags);
            }
        }
        return xRet;
    }

    public XDispatch[] queryDispatches(DispatchDescriptor[] Requests)
    {
        int nCount = Requests.length;
        XDispatch[] lDispatcher = new XDispatch[nCount];

        for (int i = 0; i < nCount; i++) {
            lDispatcher[i] = queryDispatch(Requests[i].FeatureURL, Requests[i].FrameName, Requests[i].SearchFlags);
        }

        return lDispatcher;
    }

    public synchronized XDispatchProvider getSlaveDispatchProvider()
    {
        return this.m_xSlave;
    }

    public synchronized void setSlaveDispatchProvider(XDispatchProvider xDispatchProvider)
    {
        this.m_xSlave = xDispatchProvider;
    }

    public synchronized XDispatchProvider getMasterDispatchProvider() {
        return this.m_xMaster;
    }

    public synchronized void setMasterDispatchProvider(XDispatchProvider xDispatchProvider)
    {
        this.m_xMaster = xDispatchProvider;
    }

    public String[] getInterceptedURLs() {
        return m_sCommands;
    }

    public void dispatch(URL aURL, PropertyValue[] aPropertyValue) {
        boolean bDispatch = true;
        if (bDispatch)
            showMessageBox(aURL.Complete);
        else if (this.m_xOriginalDispatch != null)
            this.m_xOriginalDispatch.dispatch(aURL, aPropertyValue);
    }

    public void addStatusListener(XStatusListener xControl, URL aURL)
    {
        this.listenerContainer.addInterface(STATUS_LISTENER_TYPE, xControl);
    }

    public void removeStatusListener(XStatusListener xControl, URL aURL) {
        this.listenerContainer.removeInterface(STATUS_LISTENER_TYPE, xControl);
    }

    private void showMessageBox(String sCommand) {
        try {
            XWindowPeer xWindowPeer = UnoRuntime.queryInterface(XWindowPeer.class, this.m_xFrame.getContainerWindow());

            XMessageBox xMessageBox = this.m_xMsgBoxFactory.createMessageBox(xWindowPeer, new Rectangle(), "infobox", 1, "Dispatch Provider Interceptor Demo", sCommand + " has been intercepted");

            xMessageBox.execute();
            XComponent xComponent = UnoRuntime.queryInterface(XComponent.class, xMessageBox);

            if (xComponent != null)
                xComponent.dispose();
        }
        catch (java.lang.Exception e) {
        }
    }

    public void displayMessage(String s) {
        synchronized (this){
            this.m_sStatus = s;
        }
        fireStatusUpdate();
    }

    public void statusChanged(FeatureStateEvent aFeatureStateEvent) {
        String sStatus = "";
        if (aFeatureStateEvent.IsEnabled) {
            try {
                sStatus = AnyConverter.toString(aFeatureStateEvent.State);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(StatusBarInterceptor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        synchronized (this) {
            this.m_sStatus = "Original status string: ".concat(sStatus);
        }

        fireStatusUpdate();
    }

    public void disposing(EventObject aEventObject) {
    }

    private void fireStatusUpdate() {
        InterfaceContainer aListeners = this.listenerContainer.getContainer(STATUS_LISTENER_TYPE);
        Iterator aIter = aListeners.iterator();
        while (aIter.hasNext()) {
            XStatusListener xListener = (XStatusListener)aIter.next();
            if (xListener != null) {
                FeatureStateEvent aFeatureStateEvent = new FeatureStateEvent();
                aFeatureStateEvent.IsEnabled = (this.m_sStatus.length() > 0);
                aFeatureStateEvent.State = this.m_sStatus;
                aFeatureStateEvent.FeatureURL = m_aURL;
                xListener.statusChanged(aFeatureStateEvent);
            }
        }
    }


}