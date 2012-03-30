/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.ssau.graphplus;

import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.XNameContainer;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.xml.AttributeData;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author anton
 */
public class Misc {
    public static boolean isNode(XShape xShape) {
       return fieldEqualsValue(xShape, "GraphElementType", "Node");
    }

    public static boolean isLink(XShape xShape) {
        return fieldEqualsValue(xShape, "GraphElementType", "Link");
    }

    private static boolean fieldEqualsValue(XShape xShape, String field, String value){
        try {
            XPropertySet xPropSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xShape);
            XNameContainer container = (XNameContainer) UnoRuntime.queryInterface(XNameContainer.class,
                    xPropSet.getPropertyValue("UserDefinedAttributes"));


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



    private static void tagShape(XShape xShape, String name, String value){
         XPropertySet xPropSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xShape);
        try {
            //XPropertySet propertySet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xTextSection);
            XNameContainer container = (XNameContainer) UnoRuntime.queryInterface(XNameContainer.class,
                    xPropSet.getPropertyValue("UserDefinedAttributes"));

            AttributeData data = new AttributeData();
            data.Type = name;
            data.Value = value;
            container.insertByName(data.Type, data);

            //container.insertByName("ShapeIsNode")
            xPropSet.setPropertyValue("UserDefinedAttributes", container);
            //xPropSet.setPropertyValue("ShapeIsNode", new com.sun.star.uno.Any(Type.BOOLEAN, Boolean.TRUE));
            //xPropSet.setPropertyValue("GraphElementType", new com.sun.star.uno.Any(Type.STRING, "Node"));
        } catch (ElementExistException ex) {
            Logger.getLogger(AddOn4.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownPropertyException ex) {
            Logger.getLogger(AddOn4.class.getName()).log(Level.SEVERE, null, ex);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(AddOn4.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(AddOn4.class.getName()).log(Level.SEVERE, null, ex);
        } catch (WrappedTargetException ex) {
            Logger.getLogger(AddOn4.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void tagShapeAsNode(XShape xShape) {
        tagShape(xShape, "GraphElementType", "Node");
    }
    public static void tagShapeAsLink(XShape xShape) {
       tagShape(xShape, "GraphElementType", "Link");
    }



    public static String getGraphElementType(XShape xShape) {
        String type = "NotGraphElement";
        try {

            XPropertySet xPropSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xShape);
            XNameContainer container = (XNameContainer) UnoRuntime.queryInterface(XNameContainer.class,
                    xPropSet.getPropertyValue("UserDefinedAttributes"));

            try {
                AttributeData a = (AttributeData) container.getByName("GraphElementType");

                type = a.Value;

            } catch (Exception ex) {
                Logger.getLogger(AddOn4.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (UnknownPropertyException ex) {
            Logger.getLogger(AddOn4.class.getName()).log(Level.SEVERE, null, ex);
        } catch (WrappedTargetException ex) {
            Logger.getLogger(AddOn4.class.getName()).log(Level.SEVERE, null, ex);
        }
                    return type;
    }
}
