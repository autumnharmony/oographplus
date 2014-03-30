package ru.ssau.graphplus.temp.sidebar;

import com.sun.star.comp.loader.FactoryHelper;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.lang.XSingleServiceFactory;
import com.sun.star.registry.XRegistryKey;

/** The per-extension registration of the panel factory service.
 *  If you want to register more services (other factories or unrelated
 *  services) then extend the two methods accordingly.
 *  This class is referenced in the manifest file that is implicitly created
 *  by the build-oxt.xml Ant file when the jar file is created.
 */
 public class Component
{
    public static XSingleServiceFactory __getServiceFactory(
        final String sImplementationName,
        final XMultiServiceFactory xFactory,
        final XRegistryKey xKey)
    {
        XSingleServiceFactory xResult = null;
        Log.Instance().println("looking up service factory for "+sImplementationName);
        if (sImplementationName.equals(PanelFactory.class.getName()))
        {
            xResult = FactoryHelper.getServiceFactory(
            	PanelFactory.class,
                PanelFactory.__serviceName,
                xFactory,
                xKey);
        }
        Log.Instance().println("    returning "+xResult);
        
        return xResult;
    }
    
    
    
    
    public static boolean __writeRegistryServiceInfo(final XRegistryKey xKey)
    {
        boolean bResult = true;
        try
        {
            Log.Instance().println("writing registry service info for PanelFactory");

            bResult &= FactoryHelper.writeRegistryServiceInfo(
            	PanelFactory.class.getName(),
                PanelFactory.__serviceName,
                xKey);

            Log.Instance().println("    success");
        }
        catch (java.lang.Exception e)
        {
        	Log.Instance().PrintStackTrace(e);
        }
        
        return bResult;
    }
}
