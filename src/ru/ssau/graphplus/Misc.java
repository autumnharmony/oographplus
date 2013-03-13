/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ssau.graphplus;

import com.sun.star.uno.AnyConverter;
import ru.ssau.graphplus.node.Node;
import com.sun.star.beans.*;
import com.sun.star.container.*;
import com.sun.star.document.XEventBroadcaster;
import com.sun.star.drawing.PolygonKind;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.style.XStyle;
import com.sun.star.uno.Any;
import com.sun.star.uno.Type;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.xml.AttributeData;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author anton
 */
public class Misc {

    public static final String GRAPH_ELEMENT_TYPE = "GraphElementType";
    public static final String NODE_TYPE_ATTR_NAME = "NodeType";
    public static final String USER_DEFINED_ATTRIBUTES = "UserDefinedAttributes";
    public static final String NODE = "Node";
    public static final String LINK = "Link";

    public static boolean isNode(XShape xShape) {
        return fieldEqualsValue(xShape, GRAPH_ELEMENT_TYPE, "Node");
    }

    public static boolean isLink(XShape xShape) {
        return fieldEqualsValue(xShape, GRAPH_ELEMENT_TYPE, "Link");
    }

    public static void printXNameContainer(XNameContainer xNC) {
        XNameAccess xNameAccess = (XNameAccess) UnoRuntime.queryInterface(XNameAccess.class, xNC);
        String[] names = xNameAccess.getElementNames();
        for (String name : names) {
            try {
                System.out.println(name + "=[" + xNC.getByName(name) + "]");
            } catch (NoSuchElementException ex) {
                Logger.getLogger(Misc.class.getName()).log(Level.SEVERE, null, ex);
            } catch (WrappedTargetException ex) {
                Logger.getLogger(Misc.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        XEnumerationAccess xEnumerationAccess = (XEnumerationAccess) UnoRuntime.queryInterface(
                XEnumerationAccess.class, xNC);

        // create an enumerator
        XEnumeration xEnum = xEnumerationAccess.createEnumeration();

        // iterate through the collection by name
        while (xEnum.hasMoreElements()) {
            // get the next element as a UNO Any
            Object aSheet = null;
            try {
                aSheet = xEnum.nextElement();
            } catch (NoSuchElementException ex) {
                Logger.getLogger(Misc.class.getName()).log(Level.SEVERE, null, ex);
            } catch (WrappedTargetException ex) {
                Logger.getLogger(Misc.class.getName()).log(Level.SEVERE, null, ex);
            }

            // get the name of the sheet from its XNamed interface
            String s = (String) aSheet;
//            XNamed xSheetNamed = (XNamed) UnoRuntime.queryInterface(XNamed.class, aSheet);
            System.out.println(s);
        }
    }

    private static boolean fieldEqualsValue(XShape xShape, String field, String value) {
        try {
            XPropertySet xPropSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xShape);
            XNameContainer container = (XNameContainer) UnoRuntime.queryInterface(XNameContainer.class,
                    xPropSet.getPropertyValue(USER_DEFINED_ATTRIBUTES));


            try {
                AttributeData a = (AttributeData) container.getByName(field);
                String myval = a.Value;

                if (myval.equals(value)) {
                    return true;
                }
            } catch (com.sun.star.container.NoSuchElementException ex) {
                Logger.getLogger(Misc.class.getName()).log(Level.SEVERE, null, ex);

            } catch (WrappedTargetException ex) {
                return false;
            }

            return false;
        } catch (UnknownPropertyException ex) {
            return false;
        } catch (WrappedTargetException ex) {
            return false;
        }
    }

    public static void setUserDefinedAttributeValue(XShape xShape, String field, String value) {
        try {
            XPropertySet xPropSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xShape);
            XNameContainer container = (XNameContainer) UnoRuntime.queryInterface(XNameContainer.class,
                    xPropSet.getPropertyValue(USER_DEFINED_ATTRIBUTES));


            try {
                AttributeData a = new AttributeData();

                a.Value = value;
                container.insertByName(field, a);



            } catch (IllegalArgumentException ex) {
                Logger.getLogger(Misc.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ElementExistException ex) {
                Logger.getLogger(Misc.class.getName()).log(Level.SEVERE, null, ex);
            } catch (WrappedTargetException ex) {
                ex.printStackTrace();
            }

            xPropSet.setPropertyValue(USER_DEFINED_ATTRIBUTES, container);

        } catch (PropertyVetoException ex) {
            Logger.getLogger(Misc.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(Misc.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownPropertyException ex) {
            ex.printStackTrace();
        } catch (WrappedTargetException ex) {
            ex.printStackTrace();
        }
    }

    public static String getUserDefinedAttributeValue(XShape xShape, String field) {
        try {
            XPropertySet xPropSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xShape);
            XNameContainer container = (XNameContainer) UnoRuntime.queryInterface(XNameContainer.class,
                    xPropSet.getPropertyValue(USER_DEFINED_ATTRIBUTES));


            try {
                AttributeData a = (AttributeData) container.getByName(field);

                return a.Value;




            } catch (NoSuchElementException ex) {
                ex.printStackTrace();
                return null;

            } catch (WrappedTargetException ex) {
                ex.printStackTrace();
                return null;
            }

        } catch (UnknownPropertyException ex) {
            ex.printStackTrace();
            return null;
        } catch (WrappedTargetException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static void tagShape(XShape xShape, String name, String value) {

        try {
            XPropertySet xPropSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xShape);

            Object userDefinedAttrs = xPropSet.getPropertyValue(USER_DEFINED_ATTRIBUTES);
            XNameContainer container = (XNameContainer) UnoRuntime.queryInterface(XNameContainer.class,
                    xPropSet.getPropertyValue(USER_DEFINED_ATTRIBUTES));

            Misc.printInfo(xShape);



            AttributeData data = new AttributeData();
            data.Type = name;
            data.Value = value;
            try {
                container.insertByName(data.Type, data);
            } catch (com.sun.star.container.ElementExistException ex) {
                try {
                    container.removeByName(data.Type);
                } catch (NoSuchElementException ex1) {
                    Logger.getLogger(Misc.class.getName()).log(Level.SEVERE, null, ex1);
                }
                container.insertByName(data.Type, data);
            }

            xPropSet.setPropertyValue(USER_DEFINED_ATTRIBUTES, container);


            XPropertySet updatedXPropSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xShape);
            Object updatedUserDefinedAttrs = xPropSet.getPropertyValue(USER_DEFINED_ATTRIBUTES);
            XNameContainer updatedContainer = (XNameContainer) UnoRuntime.queryInterface(XNameContainer.class,
                    xPropSet.getPropertyValue(USER_DEFINED_ATTRIBUTES));
            try {
                Object byName = updatedContainer.getByName(name);

            } catch (NoSuchElementException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }


            //xPropSet.setPropertyValue("ShapeIsNode", new com.sun.star.uno.Any(Type.BOOLEAN, Boolean.TRUE));
            //xPropSet.setPropertyValue("GraphElementType", new com.sun.star.uno.Any(Type.STRING, "Node"));
        } catch (ElementExistException ex) {
            Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownPropertyException ex) {
            Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
        } catch (WrappedTargetException ex) {
            Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void tagShapeAsNode(XShape xShape) {
        tagShape(xShape, GRAPH_ELEMENT_TYPE, NODE);
    }

    public static void tagShapeAsLink(XShape xShape) {
        tagShape(xShape, GRAPH_ELEMENT_TYPE, LINK);
    }

    public static void setNodeType(XShape xShape, Node.NodeType type) {
        setUserDefinedAttributeValue(xShape, NODE_TYPE_ATTR_NAME, type.toString());
    }

    public static String getNodeType(XShape xShape) {
        return getUserDefinedAttributeValue(xShape, NODE_TYPE_ATTR_NAME);
    }

    public static String toString(XShape xShape) {
        return getUserDefinedAttributeValue(xShape, NODE_TYPE_ATTR_NAME);
    }

    public static String getGraphElementType(XShape xShape) {
        String type = "NotGraphElement";
        try {

            XPropertySet xPropSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xShape);
            XNameContainer container = (XNameContainer) UnoRuntime.queryInterface(XNameContainer.class,
                    xPropSet.getPropertyValue(USER_DEFINED_ATTRIBUTES));

            try {
                AttributeData a = (AttributeData) container.getByName(GRAPH_ELEMENT_TYPE);

                type = a.Value;

            } catch (Exception ex) {
                Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (UnknownPropertyException ex) {
            Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
        } catch (WrappedTargetException ex) {
            Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
        }
        return type;
    }

    public static void printInfo(Object obj) {
        XPropertySet xPropSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, obj);
        XPropertySetInfo xPSI = xPropSet.getPropertySetInfo();
        Property[] props = xPSI.getProperties();
        for (Property prop : props) {
            System.out.println(prop.Name);
            System.out.println(prop.Type);
            try {
                System.out.println(xPropSet.getPropertyValue(prop.Name));
//                XStyle xStyle = null;
//                if (prop.Type.toString().contains("PolygonKind")) {
//                    PolygonKind pk = (PolygonKind) xPropSet.getPropertyValue(prop.Name);
//                    System.out.println(pk.toString());
//                    
//                }
//                if ((xStyle = UnoRuntime.queryInterface(XStyle.class, xPropSet.getPropertyValue(prop.Name))) != null) {
//                    xStyle.
//                }
//                System.out.println(xPropSet.getPropertyValue(prop.Name).toString());
            } catch (UnknownPropertyException ex) {
                Logger.getLogger(Misc.class.getName()).log(Level.SEVERE, null, ex);
            } catch (WrappedTargetException ex) {
                Logger.getLogger(Misc.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("=======");
        }
        System.out.println("===END====");
    }

    public static String getAttribute(XShape xShape) {
        return "";
    }

    public static void setAttribute(XShape xShape, String s) {
        //return "";  
    }

    public static void addUserDefinedAttributes(Object obj, XMultiServiceFactory xMSF) {
        try {
            XPropertySet xPS = QI.XPropertySet(obj);
            if (xPS != null) {
                Object udAttr = xPS.getPropertyValue(USER_DEFINED_ATTRIBUTES);
                Object sudAttr = xPS.getPropertyValue(USER_DEFINED_ATTRIBUTES);


                Object xUDA = AnyConverter.toObject(
                        new Type( XNameContainer.class ), xPS.getPropertyValue(
                        "UserDefinedAttributes" ) );



                XNameContainer xNC = (XNameContainer) UnoRuntime.queryInterface(XNameContainer.class, udAttr);
                if (xNC == null) {
//                    Object ncObj = xMSF.createInstance("com.sun.star.drawing.BitmapTable");

                    XNameContainer xNamedCont = new NamedContainer();



                    XContainer xCont = (XContainer) UnoRuntime.queryInterface(XContainer.class, xNamedCont);
//                    xCont.addContainerListener(new XContainerListener() {
//
//                        public void elementInserted(ContainerEvent ce) {
////                            throw new UnsupportedOperationException("Not supported yet.");
//                            System.out.println(ce.Element.toString());
//                        }
//
//                        public void elementRemoved(ContainerEvent ce) {
////                            throw new UnsupportedOperationException("Not supported yet.");
//                        }
//
//                        public void elementReplaced(ContainerEvent ce) {
////                            throw new UnsupportedOperationException("Not supported yet.");
//                        }
//
//                        public void disposing(EventObject eo) {
//                            throw new UnsupportedOperationException("Not supported yet.");
//                        }
//                    });
//                    XContainerListener xContListener = (XContainerListener) UnoRuntime.queryInterface(XContainerListener.class, xNamedCont);
//                    xContListener.elementInserted()
                    String s = new String("stupid openoffice");
                    xNamedCont.insertByName("omg", s);
//                    xNamedCont.insertByName(NODE_TYPE_ATTR_NAME, obj);
//                    printXNameContainer(xNamedCont);
//                    xNamedCont = (XNameContainer) QI.XNameContainer(ncObj);

//                    System.out.println(s);

//                    xPS.setPropertyValue(USER_DEFINED_ATTRIBUTES, ncObj);
//                    String ss = (String) xNamedCont.getByName("omg");
                    xPS.setPropertyValue(USER_DEFINED_ATTRIBUTES, xNamedCont);
                }

            }

        } catch (UnknownPropertyException ex) {
            Logger.getLogger(Misc.class.getName()).log(Level.SEVERE, null, ex);
        } catch (WrappedTargetException ex) {
            Logger.getLogger(Misc.class.getName()).log(Level.SEVERE, null, ex);
        } catch (com.sun.star.uno.Exception ex) {
            Logger.getLogger(Misc.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("qwe");



    }

    public static boolean isConnectorShape(XShape xShape) {
        //TODO recode contains
        if (xShape.getShapeType().contains("Connector")) {
            return true;
        }
        return false;
    }

    public static boolean isTextShape(XShape xShape1) {
        //TODO recode contains
        if (xShape1.getShapeType().contains("Text")) {
            return true;
        }
        return false;
    }
}
