/* 
 * OOoUtils.java 
 * 
 * Created on February 22, 2003, 12:10 PM 
 * 
 * Copyright 2003 Danny Brewer 
 * Anyone may run this code. 
 * If you wish to modify or distribute this code, then 
 *  you are granted a license to do so only under the terms 
 *  of the Gnu Lesser General Public License. 
 * See:  http://www.gnu.org/licenses/lgpl.html 
 */

package ru.ssau.graphplus.commons;


//---------------------------------------------------------------------- 
//  OpenOffice.org imports 
//---------------------------------------------------------------------- 

import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;
import com.sun.star.bridge.XUnoUrlResolver;
import com.sun.star.container.XNamed;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.XInterface;

//import com.sun.star.view.*;
//import com.sun.star.table.*; 
//import com.sun.star.sheet.*; 
//import com.sun.star.drawing.*; 
//import com.sun.star.text.*; 
//import com.sun.star.util.*; 
//import com.sun.star.awt.*; 


/**
 * @author danny brewer
 */
public class OOoUtils {
    private OOoUtils() {
    }


    //---------------------------------------------------------------------- 
    //  Get remote service manager. 
    //   This is the first object you need to connect to a 
    //    running instance of OpenOffice.org. 
    //---------------------------------------------------------------------- 

    public final static String LOCAL_OO_SERVICE_MGR_URL = "uno:socket,host=localhost,port=8100;urp;StarOffice.ServiceManager";


    // Connect to OpenOffice's ServiceManager, port 8100, localhost. 
    public static XMultiServiceFactory getRemoteOOoServiceManager()
            throws java.lang.Exception,
            com.sun.star.uno.Exception,
            com.sun.star.connection.NoConnectException,
            com.sun.star.beans.UnknownPropertyException {
        return getRemoteOOoServiceManager(LOCAL_OO_SERVICE_MGR_URL);
    }


    // Connect to OpenOffice's ServiceManager, port 8100, localhost. 
    public static XMultiServiceFactory getRemoteOOoServiceManager(String host, String port)
            throws java.lang.Exception,
            com.sun.star.uno.Exception,
            com.sun.star.connection.NoConnectException,
            com.sun.star.beans.UnknownPropertyException {
        return getRemoteOOoServiceManager("uno:socket,host=" + host + ",port=" + port + ";urp;StarOffice.ServiceManager");
    }


    // Connect to a specified URL to get a remote service manager. 
    public static XMultiServiceFactory getRemoteOOoServiceManager(String unoRemoteServiceManagerUrl)
            throws java.lang.Exception,
            com.sun.star.uno.Exception,
            com.sun.star.connection.NoConnectException,
            com.sun.star.beans.UnknownPropertyException {
        // Create a local component context. 
        XComponentContext localContext = com.sun.star.comp.helper.Bootstrap.createInitialComponentContext(null);

        // Get the local service manager from the local component context. 
        XMultiComponentFactory localServiceManager = localContext.getServiceManager();

        // Ask local service manager for a UnoUrlResolver object with an XUnoUrlResolver interface. 
        Object unoUrlResolver = localServiceManager.createInstanceWithContext("com.sun.star.bridge.UnoUrlResolver", localContext);

        // Query the UnoUrlResolver object for an XUnoUrlResolver interface. 
        XUnoUrlResolver unoUrlResolver_XUnoUrlResolver = QI.XUnoUrlResolver(unoUrlResolver);
        // At this point, the variables 
        //   unoUrlResolver 
        //   unoUrlResolver_XUnoUrlResolver 
        //  point to the same service.  Just different interfaces of it. 

        // Use the unoUrlResolver to get a remote service manager, 
        //  by resolving the URL that was passed in as a parameter. 
        Object remoteOOoServiceManager = unoUrlResolver_XUnoUrlResolver.resolve(unoRemoteServiceManagerUrl);

        // Get a more convenient interface. 
        XMultiServiceFactory remoteOOoServiceManager_MultiServiceFactory = QI.XMultiServiceFactory(remoteOOoServiceManager);

        return remoteOOoServiceManager_MultiServiceFactory;
    }


    //---------------------------------------------------------------------- 
    //  Given a remote service manager (or not), 
    //   return the Desktop object, conveniently in the form 
    //   of an XComponentLoader. 
    //---------------------------------------------------------------------- 

    public static XComponentLoader getDesktop()
            throws java.lang.Exception,
            com.sun.star.uno.Exception,
            com.sun.star.connection.NoConnectException,
            com.sun.star.beans.UnknownPropertyException {
        XMultiServiceFactory remoteOOoServiceManager = OOoUtils.getRemoteOOoServiceManager();
        return getDesktop(remoteOOoServiceManager);
    }

    public static XComponentLoader getDesktop(Object remoteOOoServiceManager)
            throws com.sun.star.uno.Exception {
        // Get the desired interface to the object.
        XMultiServiceFactory remoteOOoServiceManager_MultiServiceFactory = QI.XMultiServiceFactory(remoteOOoServiceManager);
        // At this point, the variables 
        //   oOORmtServiceMgr 
        //   oOORmtServiceMgr_MultiServiceFactory 
        //  point to the same service.  Just different interfaces of it. 

        return getDesktop(remoteOOoServiceManager_MultiServiceFactory);
    }

    public static XComponentLoader getDesktop(XMultiServiceFactory remoteOOoServiceManager)
            throws com.sun.star.uno.Exception {

        // Ask the MultiServiceFactory interface of the service manager for a Desktop. 
        // This might throw com.sun.star.uno.Exception. 
        // This gives us the desktop, via. it's XInterface interface. 
        XInterface desktop_XInterface =
                (XInterface) remoteOOoServiceManager.createInstance("com.sun.star.frame.Desktop");

        // Get the desired interface from the object. 
        // We don't need the XInterface interface, we need the XComponentLoader interface to the object. 
        XComponentLoader desktop_XComponentLoader = QI.XComponentLoader(desktop_XInterface);
        // At this point, the variables 
        //   desktop_XInterface 
        //   desktop_XComponentLoader 
        //  point to the same service.  Just different interfaces of it. 

        return desktop_XComponentLoader;
    }


    //---------------------------------------------------------------------- 
    //  Conveniences to make some structures 
    //---------------------------------------------------------------------- 

    public static PropertyValue makePropertyValue(String propertyName, int value) {
        return makePropertyValue(propertyName, new Integer(value));
    }

    public static PropertyValue makePropertyValue(String propertyName, boolean value) {
        return makePropertyValue(propertyName, new Boolean(value));
    }

    public static PropertyValue makePropertyValue(String propertyName, Object value) {
        PropertyValue propValue = new PropertyValue();
        propValue.Name = propertyName;
        propValue.Value = value;
        return propValue;
    }


    public static com.sun.star.awt.Point makePoint(int x, int y) {
        com.sun.star.awt.Point point = new com.sun.star.awt.Point();
        point.X = x;
        point.Y = y;
        return point;
    }

    public static com.sun.star.awt.Size makeSize(int width, int height) {
        com.sun.star.awt.Size size = new com.sun.star.awt.Size();
        size.Width = width;
        size.Height = height;
        return size;
    }

    public static com.sun.star.awt.Rectangle makeRectangle(int x, int y, int width, int height) {
        com.sun.star.awt.Rectangle rect = new com.sun.star.awt.Rectangle();
        rect.X = x;
        rect.Y = y;
        rect.Width = width;
        rect.Height = height;
        return rect;
    }


    //---------------------------------------------------------------------- 
    //  Property Manipulation 
    //---------------------------------------------------------------------- 

    public static void setIntProperty(Object obj, String propName, int value)
            throws com.sun.star.beans.UnknownPropertyException,
            com.sun.star.beans.PropertyVetoException,
            com.sun.star.lang.IllegalArgumentException,
            com.sun.star.lang.WrappedTargetException {
        setProperty(obj, propName, new Integer(value));
    }

    public static int getIntProperty(Object obj, String propName)
            throws com.sun.star.beans.UnknownPropertyException,
            com.sun.star.lang.WrappedTargetException {
        Object value = getProperty(obj, propName);
        if (value != null) {
            if (value instanceof Integer) {
                Integer intValue = (Integer) value;
                return intValue.intValue();
            }
        }
        return 0;
    }


    public static void setBooleanProperty(Object obj, String propName, boolean value)
            throws com.sun.star.beans.UnknownPropertyException,
            com.sun.star.beans.PropertyVetoException,
            com.sun.star.lang.IllegalArgumentException,
            com.sun.star.lang.WrappedTargetException {
        setProperty(obj, propName, new Boolean(value));
    }

    public static boolean getBooleanProperty(Object obj, String propName)
            throws com.sun.star.beans.UnknownPropertyException,
            com.sun.star.lang.WrappedTargetException {
        Object value = getProperty(obj, propName);
        if (value != null) {
            if (value instanceof Boolean) {
                Boolean booleanValue = (Boolean) value;
                return booleanValue.booleanValue();
            }
        }
        return false;
    }


    public static void setStringProperty(Object obj, String propName, String value)
            throws com.sun.star.beans.UnknownPropertyException,
            com.sun.star.beans.PropertyVetoException,
            com.sun.star.lang.IllegalArgumentException,
            com.sun.star.lang.WrappedTargetException {
        setProperty(obj, propName, value);
    }

    public static String getStringProperty(Object obj, String propName)
            throws com.sun.star.beans.UnknownPropertyException,
            com.sun.star.lang.WrappedTargetException {
        Object value = getProperty(obj, propName);
        if (value != null) {
            if (value instanceof String) {
                String stringValue = (String) value;
                return stringValue;
            }
        }
        return "";
    }


    public static void setProperty(Object obj, String propName, Object value)
            throws com.sun.star.beans.UnknownPropertyException,
            com.sun.star.beans.PropertyVetoException,
            com.sun.star.lang.IllegalArgumentException,
            com.sun.star.lang.WrappedTargetException {
        // We need the XPropertySet interface. 
        XPropertySet obj_XPropertySet;
        if (obj instanceof XPropertySet) {
            // If the right interface was passed in, just typecaset it. 
            obj_XPropertySet = (XPropertySet) obj;
        } else {
            // Get a different interface to the drawDoc. 
            // The parameter passed in to us is the wrong interface to the object. 
            obj_XPropertySet = QI.XPropertySet(obj);
        }

        // Now just call our sibling using the correct interface. 
        setProperty(obj_XPropertySet, propName, value);
    }

    public static void setProperty(XPropertySet obj, String propName, Object value)
            throws com.sun.star.beans.UnknownPropertyException,
            com.sun.star.beans.PropertyVetoException,
            com.sun.star.lang.IllegalArgumentException,
            com.sun.star.lang.WrappedTargetException {
        obj.setPropertyValue(propName, value);
    }


    public static Object getProperty(Object obj, String propName)
            throws com.sun.star.beans.UnknownPropertyException,
            com.sun.star.lang.WrappedTargetException {
        // We need the XPropertySet interface. 
        XPropertySet obj_XPropertySet;
        if (obj instanceof XPropertySet) {
            // If the right interface was passed in, just typecaset it. 
            obj_XPropertySet = (XPropertySet) obj;
        } else {
            // Get a different interface to the drawDoc. 
            // The parameter passed in to us is the wrong interface to the object. 
            obj_XPropertySet = QI.XPropertySet(obj);
        }

        // Now just call our sibling using the correct interface. 
        return getProperty(obj_XPropertySet, propName);
    }

    public static Object getProperty(XPropertySet obj, String propName)
            throws com.sun.star.beans.UnknownPropertyException,
            com.sun.star.lang.WrappedTargetException {
        return obj.getPropertyValue(propName);
    }

    //---------------------------------------------------------------------- 
    //  Sugar coating for com.sun.star.container.XNamed interface. 
    //---------------------------------------------------------------------- 

    public static void XNamed_setName(Object obj, String name) {
        XNamed xNamed = QI.XNamed(obj);
        xNamed.setName(name);
    }

    public static String XNamed_getName(Object obj) {
        XNamed xNamed = QI.XNamed(obj);
        return xNamed.getName();
    }

} 