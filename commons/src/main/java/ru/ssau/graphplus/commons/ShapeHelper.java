/*************************************************************************
 *
 *  The Contents of this file are made available subject to the terms of
 *  the BSD license.
 *
 *  Copyright 2000, 2010 Oracle and/or its affiliates.
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *  1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *  3. Neither the name of Sun Microsystems, Inc. nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 *  FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 *  COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 *  BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 *  OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 *  TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 *  USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *************************************************************************/
package ru.ssau.graphplus.commons;
// __________ Imports __________

import com.sun.star.awt.Point;
import com.sun.star.awt.Size;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XEnumeration;
import com.sun.star.container.XEnumerationAccess;
import com.sun.star.drawing.XDrawPage;
import com.sun.star.drawing.XShape;
import com.sun.star.drawing.XShapes;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.text.*;
import com.sun.star.uno.UnoRuntime;

import java.util.ArrayList;
import java.util.Collection;


public class ShapeHelper {
    // __________ static helper methods __________
    //
    public static XPropertySet createAndInsertShape(XComponent xDrawDoc,
                                                    XShapes xShapes, Point aPos, Size aSize, String sShapeType)
            throws java.lang.Exception {
        XShape xShape = createShape(xDrawDoc, aPos, aSize, sShapeType);
        xShapes.add(xShape);
        XPropertySet xPropSet = (XPropertySet)
                UnoRuntime.queryInterface(XPropertySet.class, xShape);
        return xPropSet;
    }

    public static XShape createAndInsertShapeReturnXShape(XComponent xDrawDoc,
                                                          XShapes xShapes, Point aPos, Size aSize, String sShapeType)
            throws java.lang.Exception {
        XShape xShape = createShape(xDrawDoc, aPos, aSize, sShapeType);
        xShapes.add(xShape);
//        XPropertySet xPropSet = (XPropertySet)
//            UnoRuntime.queryInterface( XPropertySet.class, xShape );
        return xShape;
    }


    /**
     * create a Shape
     */
    public static XShape createShape(XComponent xDrawDoc,
                                     Point aPos, Size aSize, String sShapeType)
            throws java.lang.Exception {
        XShape xShape = null;
        XMultiServiceFactory xFactory =
                (XMultiServiceFactory) UnoRuntime.queryInterface(
                        XMultiServiceFactory.class, xDrawDoc);
        Object xObj = xFactory.createInstance(sShapeType);
        xShape = (XShape) UnoRuntime.queryInterface(
                XShape.class, xObj);
        xShape.setPosition(aPos);
        xShape.setSize(aSize);
        return xShape;
    }

    public static XShape createShape(XMultiServiceFactory xFactory,
                                     Point aPos, Size aSize, String sShapeType)
            throws java.lang.Exception {
        XShape xShape = null;

        Object xObj = xFactory.createInstance(sShapeType);
        xShape = (XShape) UnoRuntime.queryInterface(
                XShape.class, xObj);
        xShape.setPosition(aPos);
        xShape.setSize(aSize);
        return xShape;
    }

    /**
     * add text to a shape. the return value is the PropertySet
     * of the text range that has been added
     */
    public static XPropertySet addPortion(XShape xShape, String sText, boolean bNewParagraph)
            throws com.sun.star.lang.IllegalArgumentException {
        XText xText = (XText)
                UnoRuntime.queryInterface(XText.class, xShape);

        XTextCursor xTextCursor = xText.createTextCursor();
        //xTextCursor.gotoEnd( true );
        if (bNewParagraph == true) {
            xText.insertControlCharacter(xTextCursor, ControlCharacter.PARAGRAPH_BREAK, false);
            xTextCursor.gotoEnd(false);
        }
        XTextRange xTextRange = (XTextRange)
                UnoRuntime.queryInterface(XTextRange.class, xTextCursor);
        xTextRange.setString(sText);

        //xTextCursor.gotoEnd( true );
        XPropertySet xPropSet = (XPropertySet)
                UnoRuntime.queryInterface(XPropertySet.class, xTextRange);
        return xPropSet;
    }

    public static XPropertySet replacePortion(XShape xShape, String sText)
            throws com.sun.star.lang.IllegalArgumentException {
        XText xText = (XText)
                UnoRuntime.queryInterface(XText.class, xShape);

        XTextCursor xTextCursor = xText.createTextCursor();
        xTextCursor.gotoStart(true);
//        true);//gotoEnd( false );

        XTextRange xTextRange = (XTextRange)
                UnoRuntime.queryInterface(XTextRange.class, xTextCursor);
        xTextRange.setString(sText);
        xTextCursor.gotoEnd(true);
        XPropertySet xPropSet = (XPropertySet)
                UnoRuntime.queryInterface(XPropertySet.class, xTextRange);
        return xPropSet;
    }

    public static void setPropertyForLastParagraph(XShape xText, String sPropName,
                                                   Object aValue)
            throws com.sun.star.beans.UnknownPropertyException,
            com.sun.star.beans.PropertyVetoException,
            com.sun.star.lang.IllegalArgumentException,
            com.sun.star.lang.WrappedTargetException,
            com.sun.star.container.NoSuchElementException {
        XEnumerationAccess xEnumerationAccess = (XEnumerationAccess)
                UnoRuntime.queryInterface(XEnumerationAccess.class, xText);
        if (xEnumerationAccess.hasElements()) {
            XEnumeration xEnumeration = xEnumerationAccess.createEnumeration();
            while (xEnumeration.hasMoreElements()) {
                Object xObj = xEnumeration.nextElement();
                if (xEnumeration.hasMoreElements() == false) {
                    XTextContent xTextContent = (XTextContent) UnoRuntime.queryInterface(
                            XTextContent.class, xObj);
                    XPropertySet xParaPropSet = (XPropertySet)
                            UnoRuntime.queryInterface(XPropertySet.class, xTextContent);
                    xParaPropSet.setPropertyValue(sPropName, aValue);
                }
            }
        }
    }


    public static void insertShape(XShape xShape, XShapes xShapes) {
        try {
            xShapes.add(xShape);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }

    }

    public static void insertShape(XShape xShape, XShapes xShapes, Point point) {
        try {
            xShapes.add(xShape);
            xShape.setPosition(point);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }

    }

    public static void insertShape(XShape xShape, XDrawPage xDP) {
        XShapes xShapes = (XShapes) UnoRuntime.queryInterface(XShapes.class, xDP);
        insertShape(xShape, xShapes);
    }

    public static void insertShape(XShape xShape, XDrawPage xDP, Point point) {
        XShapes xShapes = (XShapes) UnoRuntime.queryInterface(XShapes.class, xDP);
        insertShape(xShape, xShapes, point);
    }

    public static void insertShape(XShape xShape, XDrawPage xDP, PostCreationAction postCreationAction) {
        XShapes xShapes = (XShapes) UnoRuntime.queryInterface(XShapes.class, xDP);
        insertShape(xShape, xShapes);
        postCreationAction.postCreate(xShape);
    }

//    private static Logger logger = Logger.getLogger(ShapeHelper.class.getName());

    public static boolean removeShape(XShape xShape, XDrawPage xDP) {

//        logger.entering(ShapeHelper.class.getCanonicalName(), "removeShape");

        for (int i = 0; i < xDP.getCount(); i++) {
            try {
                Object byIndex = xDP.getByIndex(i);
                XShape xShape1 = QI.XShape(byIndex);
                if (xShape.equals(xShape1)) {
                    xDP.remove(xShape);
                    return true;
                }

            } catch (com.sun.star.lang.IndexOutOfBoundsException | WrappedTargetException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

//        logger.exiting(ShapeHelper.class.getCanonicalName(), "removeShape");
        return false;
    }

    public static Collection<XShape> getShapes(XDrawPage xDrawPage) {
        int count = xDrawPage.getCount();

        Collection<XShape> xShapes = new ArrayList();
        for (int i = 0; i < count; i++) {
            try {
                Object byIndex = xDrawPage.getByIndex(i);
                XShape xShape = QI.XShape(byIndex);
                xShapes.add(xShape);
            } catch (com.sun.star.lang.IndexOutOfBoundsException e) {
                e.printStackTrace();
            } catch (WrappedTargetException e) {
                e.printStackTrace();
            }

        }
        return xShapes;
    }


    public static boolean isTextShape(XShape xShape1) {
        //TODO recode contains
        if (xShape1.getShapeType().contains("Text")) {
            return true;
        }
        return false;
    }

    public static boolean isConnectorShape(XShape xShape) {
        //TODO recode contains
        if (xShape.getShapeType().contains("Connector")) {
            return true;
        }
        return false;
    }
}
