package ru.ssau.graphplus.dispatch;


import com.sun.star.beans.NamedValue;
import com.sun.star.frame.XModel2;
import com.sun.star.frame.XModuleManager;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.task.XJob;
import com.sun.star.uno.*;

import java.util.ArrayList;

public final class DispatchInterceptorJob extends WeakBase
        implements XJob, XServiceInfo
{
    private final XComponentContext m_xContext;
    private XModel2 m_xModel;
    private XModuleManager m_xModuleManager;
    private static final String m_implementationName = DispatchInterceptorJob.class.getName();
    private static final String[] m_serviceNames = { "com.sun.star.task.Job" };

    private static final ArrayList<String> m_aSupportedModules = new ArrayList(1);

    public DispatchInterceptorJob(XComponentContext context)
    {
        this.m_xContext = context;
        this.m_xModel = null;
        this.m_xModuleManager = null;
        try {
            this.m_xModuleManager = ((XModuleManager)UnoRuntime.queryInterface(XModuleManager.class, this.m_xContext.getServiceManager().createInstanceWithContext("com.sun.star.frame.ModuleManager", this.m_xContext)));
        }
        catch (java.lang.Exception e)
        {
        }
    }

    public static XSingleComponentFactory __getComponentFactory(String sImplementationName)
    {
        XSingleComponentFactory xFactory = null;

        if (sImplementationName.equals(m_implementationName)) {
            xFactory = Factory.createComponentFactory(DispatchInterceptorJob.class, m_serviceNames);
        }
        return xFactory;
    }

    public static boolean __writeRegistryServiceInfo(XRegistryKey xRegistryKey) {
        return Factory.writeRegistryServiceInfo(m_implementationName, m_serviceNames, xRegistryKey);
    }

    public String getImplementationName()
    {
        return m_implementationName;
    }

    public boolean supportsService(String sService) {
        int len = m_serviceNames.length;

        for (int i = 0; i < len; i++) {
            if (sService.equals(m_serviceNames[i])) {
                return true;
            }
        }
        return false;
    }

    public String[] getSupportedServiceNames() {
        return m_serviceNames;
    }

    public Object execute(NamedValue[] aArguments) throws IllegalArgumentException, com.sun.star.uno.Exception
    {
        InterceptionController aController;
        try
        {
            NamedValue[] aEnvironment = null;
            for (int i = 0; i < aArguments.length; i++) {
                if (aArguments[i].Name.equals("Environment")) {
                    aEnvironment = (NamedValue[])AnyConverter.toArray(aArguments[i].Value);

                    break;
                }
            }

            if (aEnvironment == null) {
                throw new IllegalArgumentException("no environment");
            }

            String sEnvType = null;
            String sEventName = null;
            this.m_xModel = null;
            for (int i = 0; i < aEnvironment.length; i++) {
                String sName = aEnvironment[i].Name;
                if (sName.equals("EnvType")) {
                    sEnvType = AnyConverter.toString(aEnvironment[i].Value);
                }
                else if (sName.equals("EventName")) {
                    sEventName = AnyConverter.toString(aEnvironment[i].Value);
                }
                else if (sName.equals("Model")) {
                    this.m_xModel = ((XModel2)UnoRuntime.queryInterface(XModel2.class, aEnvironment[i].Value));
                }

            }

            if ((sEnvType == null) || (!sEnvType.equals("DOCUMENTEVENT"))) {
                throw new IllegalArgumentException("Invalid event type! This Job only works with document events.");
            }

            if ((sEventName == null) || (!sEventName.equals("onDocumentOpened"))) {
                throw new IllegalArgumentException("Invalid event! This Job only works with onDocumentOpened (OnLoad + OnNew) document event.");
            }

            if (this.m_xModel == null) {
                throw new IllegalArgumentException("The Job needs a XModel reference.");
            }

            String sModuleIdentifier = this.m_xModuleManager.identify(this.m_xModel);
            System.out.printf("css.frame.XJob.execute: Event: \"%s\" - Module : %s\n", new Object[] { sEventName, sModuleIdentifier });

            if (!m_aSupportedModules.contains(sModuleIdentifier)) {
                return new Any(Type.VOID, null);
            }

            aController = new InterceptionController(this.m_xContext, this.m_xModel, sModuleIdentifier);
        }
        catch (java.lang.Exception e)
        {
        }
        return new Any(Type.VOID, null);
    }

    static
    {
        m_aSupportedModules.add("com.sun.star.drawing.DrawingDocument");
    }
}
