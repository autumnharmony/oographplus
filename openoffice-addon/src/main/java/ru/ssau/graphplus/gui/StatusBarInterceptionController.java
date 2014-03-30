package ru.ssau.graphplus.gui;

import com.sun.star.document.DocumentEvent;
import com.sun.star.document.XDocumentEventBroadcaster;
import com.sun.star.document.XDocumentEventListener;
import com.sun.star.frame.XController;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XModel2;
import com.sun.star.lang.EventObject;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

import java.util.*;

public class StatusBarInterceptionController
        implements XDocumentEventListener
{
    private final XComponentContext m_xContext;
    private final XModel2 m_xModel;
    private Map<XFrame, StatusBarInterceptor> m_aFrameInterceptorMap;
    private StatusBarInterceptor aInterceptor;

    public void displayMessage(String s){

        aInterceptor.displayMessage(s);
    }

    public StatusBarInterceptionController(XComponentContext xContext, XModel2 xModel, String sModuleIdentifier)
    {
        this.m_xContext = xContext;
        this.m_xModel = xModel;
        try {
            this.m_aFrameInterceptorMap = Collections.synchronizedMap(new HashMap());

            XController xController = xModel.getCurrentController();
            if (xController != null) {
                XFrame xFrame = xController.getFrame();
                if (xFrame != null) {
                    aInterceptor = new StatusBarInterceptor(xContext, xFrame);
                    this.m_aFrameInterceptorMap.put(xFrame, aInterceptor);
                    xFrame.addEventListener(this);
                }

            }

            XDocumentEventBroadcaster xBroadcaster = UnoRuntime.queryInterface(XDocumentEventBroadcaster.class, this.m_xModel);

            if (xBroadcaster != null)
                xBroadcaster.addDocumentEventListener(this);
        }
        catch (Exception e)
        {
        }
    }

    public void documentEventOccured(DocumentEvent aDocumentEvent)
    {
        if ((aDocumentEvent.EventName.equals("OnViewCreated")) &&
                (aDocumentEvent.ViewController != null)) {
            XFrame xFrame = aDocumentEvent.ViewController.getFrame();
            if (xFrame != null)
                synchronized (this) {
                    if (!this.m_aFrameInterceptorMap.containsKey(xFrame)) {
                        StatusBarInterceptor aInterceptor = new StatusBarInterceptor(this.m_xContext, xFrame);
                        this.m_aFrameInterceptorMap.put(xFrame, aInterceptor);
                        xFrame.addEventListener(this);
                    }
                }
        }
    }

    public void disposing(EventObject aEventObject)
    {
        XModel2 xModel = UnoRuntime.queryInterface(XModel2.class, aEventObject.Source);
        XFrame xFrame = UnoRuntime.queryInterface(XFrame.class, aEventObject.Source);
        if (xModel != null)
        {
            Collection aInterceptors = this.m_aFrameInterceptorMap.values();
            Iterator it = aInterceptors.iterator();
            while (it.hasNext()) {
                StatusBarInterceptor aInterceptor = (StatusBarInterceptor)it.next();
                aInterceptor.stopIntercepting();
                xFrame = aInterceptor.getFrame();
                if (xFrame != null) {
                    xFrame.removeEventListener(this);
                }
            }
            this.m_aFrameInterceptorMap.clear();
        } else if (xFrame != null)
        {
            if (this.m_aFrameInterceptorMap.containsKey(xFrame))
                this.m_aFrameInterceptorMap.remove(xFrame);
        }
    }
}