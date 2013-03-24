/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ssau.graphplus.gui;

import com.sun.star.util.XURLTransformer;
import ru.ssau.graphplus.Misc;
import ru.ssau.graphplus.QI;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XIndexContainer;
import com.sun.star.container.XNameContainer;
import com.sun.star.drawing.XShape;
import com.sun.star.drawing.XShapes;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.ui.ActionTriggerSeparatorType;
import com.sun.star.ui.ContextMenuExecuteEvent;
import com.sun.star.ui.ContextMenuInterceptorAction;
import com.sun.star.ui.XContextMenuInterceptor;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ContextMenuInterceptor implements XContextMenuInterceptor {

    XComponentContext context;
    boolean tagAsLinkInserted;
    boolean tagAsNodeInserted;


    public static final Collection<String> SLOTS_TO_REMOVE = new ArrayList<String>(); // ArrayList<String>(Arrays.asList("slot:27027", "slot:27033"));

    public ContextMenuInterceptor(XComponentContext context) {
        this.context = context;
    }

    public static class MenuElement {

        public static boolean IsMenuEntry(com.sun.star.beans.XPropertySet xMenuElement) {
            com.sun.star.lang.XServiceInfo xServiceInfo =
                    (com.sun.star.lang.XServiceInfo) UnoRuntime.queryInterface(
                    com.sun.star.lang.XServiceInfo.class, xMenuElement);

            return xServiceInfo.supportsService("com.sun.star.ui.ActionTrigger");
        }

        public static boolean IsMenuSeparator(com.sun.star.beans.XPropertySet xMenuElement) {
            com.sun.star.lang.XServiceInfo xServiceInfo =
                    (com.sun.star.lang.XServiceInfo) UnoRuntime.queryInterface(
                    com.sun.star.lang.XServiceInfo.class, xMenuElement);

            return xServiceInfo.supportsService("com.sun.star.ui.ActionTriggerSeparator");
        }
    }

    public ContextMenuInterceptorAction notifyContextMenuExecute(ContextMenuExecuteEvent arg0) {


        Object selection = arg0.Selection.getSelection();
        XShape xShape = null;// = (XShape) UnoRuntime.queryInterface(
//                XShape.class, selection);
        XServiceInfo xServiceInfo = (XServiceInfo) UnoRuntime.queryInterface(
                XServiceInfo.class, selection);
        XShapes xSelection = (XShapes) UnoRuntime.queryInterface(
                XShapes.class, selection);



        if (xSelection.getCount() > 2) {
            return ContextMenuInterceptorAction.CONTINUE_MODIFIED;
        }

        XIndexContainer menuItems = arg0.ActionTriggerContainer;
        com.sun.star.container.XIndexContainer xContextMenu = menuItems;
        com.sun.star.lang.XMultiServiceFactory xMenuElementFactory =
                (com.sun.star.lang.XMultiServiceFactory) UnoRuntime.queryInterface(
                com.sun.star.lang.XMultiServiceFactory.class, xContextMenu);

        if (xSelection.getCount() == 2) {
            try {
                XShape xShape1 = (XShape) UnoRuntime.queryInterface(
                        XShape.class, xSelection.getByIndex(0));
                XShape xShape2 = (XShape) UnoRuntime.queryInterface(
                        XShape.class, xSelection.getByIndex(1));

                XShape firstShape;
                XShape textShape;

                String t = xShape1.getShapeType() + xShape2.getShapeType();
                if ((t.contains("Ellipse") && t.contains("Text")) || (t.contains("Connector") && t.contains("Text"))) {
                    int ellPos = t.indexOf("Ellipse");
                    if (ellPos == -1) {
                        ellPos = t.indexOf("Connector");
                    }
                    int textPos = t.indexOf("Text");

                    if (ellPos < textPos) {
                        firstShape = xShape1;
                        textShape = xShape2;
                    } else {
                        firstShape = xShape2;
                        textShape = xShape1;
                    }
                    if (Misc.isNode(firstShape) || Misc.isLink(firstShape)) {
                        com.sun.star.beans.XPropertySet assocTextItem =
                                (XPropertySet) UnoRuntime.queryInterface(
                                com.sun.star.beans.XPropertySet.class,
                                xMenuElementFactory.createInstance("com.sun.star.ui.ActionTrigger"));
                        assocTextItem.setPropertyValue("Text", "Assoc");
                        assocTextItem.setPropertyValue("CommandURL", "ru.ssau.graphplus.oograph:Assoc");
                        xContextMenu.insertByIndex(0, (Object) assocTextItem);
                    }

                }

            } catch (UnknownPropertyException ex) {
                Logger.getLogger(ContextMenuInterceptor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (PropertyVetoException ex) {
                Logger.getLogger(ContextMenuInterceptor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(ContextMenuInterceptor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IndexOutOfBoundsException ex) {
                Logger.getLogger(ContextMenuInterceptor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (WrappedTargetException ex) {
                Logger.getLogger(ContextMenuInterceptor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(ContextMenuInterceptor.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                //count == 1


                try {
                    xShape = (XShape) UnoRuntime.queryInterface(
                            XShape.class, xSelection.getByIndex(0));

                    // Retrieve context menu container and query for service factory to
                    // create sub menus, menu entries and separators
                } catch (IndexOutOfBoundsException ex) {
                    Logger.getLogger(ContextMenuInterceptor.class.getName()).log(Level.SEVERE, null, ex);
                } catch (WrappedTargetException ex) {
                    Logger.getLogger(ContextMenuInterceptor.class.getName()).log(Level.SEVERE, null, ex);
                }


                XPropertySet xPropSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xShape);
                XNameContainer container = (XNameContainer) UnoRuntime.queryInterface(XNameContainer.class,
                        xPropSet.getPropertyValue("UserDefinedAttributes"));

//                Misc.printXNameContainer(container);

                boolean isNodeOrLink = Misc.isNode(xShape) || Misc.isLink(xShape);



                if (isNodeOrLink) {

                    for (int i = 0; i < menuItems.getCount(); i++) {
                        Object menuItem = null;
                        try {
                            menuItem = menuItems.getByIndex(i);
                        } catch (IndexOutOfBoundsException ex) {
                            Logger.getLogger(ContextMenuInterceptor.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (WrappedTargetException ex) {
                            Logger.getLogger(ContextMenuInterceptor.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        XPropertySet menuItemXPropSet = (XPropertySet) UnoRuntime.queryInterface(
                                XPropertySet.class, menuItem);




                        //                        ????????????????????????...
                        //                        slot:27033
                        //                        ????????????????????????...
                        //                        slot:27027



                        if (MenuElement.IsMenuEntry(menuItemXPropSet)) {
                            try {
                                System.out.println(menuItemXPropSet.getPropertyValue("Text"));
                                System.out.println(menuItemXPropSet.getPropertyValue("CommandURL"));

                                if (SLOTS_TO_REMOVE.contains(menuItemXPropSet.getPropertyValue("CommandURL"))) {
                                    try {
                                        System.out.println(menuItemXPropSet.getPropertyValue("Text") + " removed");
                                        menuItems.removeByIndex(i);
                                    } catch (IndexOutOfBoundsException ex) {
                                        Logger.getLogger(ContextMenuInterceptor.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            } catch (UnknownPropertyException ex) {
                                Logger.getLogger(ContextMenuInterceptor.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (WrappedTargetException ex) {
                                Logger.getLogger(ContextMenuInterceptor.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }


                    // our items





                    try {

                        if (xMenuElementFactory != null) {
                            // create root menu entry for sub menu and sub menu
                            com.sun.star.beans.XPropertySet xRootMenuEntry =
                                    (XPropertySet) UnoRuntime.queryInterface(
                                    com.sun.star.beans.XPropertySet.class,
                                    xMenuElementFactory.createInstance("com.sun.star.ui.ActionTrigger"));

                            // create a line separator for our new help sub menu
                            com.sun.star.beans.XPropertySet xSeparator =
                                    (com.sun.star.beans.XPropertySet) UnoRuntime.queryInterface(
                                    com.sun.star.beans.XPropertySet.class,
                                    xMenuElementFactory.createInstance("com.sun.star.ui.ActionTriggerSeparator"));

                            Short aSeparatorType = new Short(ActionTriggerSeparatorType.LINE);
                            xSeparator.setPropertyValue("SeparatorType", (Object) aSeparatorType);

                            // query sub menu for index container to get access
    //                    com.sun.star.container.XIndexContainer xSubMenuContainer =
    //                            (com.sun.star.container.XIndexContainer) UnoRuntime.queryInterface(
    //                            com.sun.star.container.XIndexContainer.class,
    //                            xMenuElementFactory.createInstance(
    //                            "com.sun.star.ui.ActionTriggerContainer"));

                            // intialize root menu entry "Help"
                            xRootMenuEntry.setPropertyValue("Text", Misc.toString(xShape));
                            xRootMenuEntry.setPropertyValue("CommandURL", "ru.ssau.graphplus.oograph:Omg");
    //                    xRootMenuEntry.setPropertyValue("", selection);

                            //xRootMenuEntry.setPropertyValue("HelpURL", "5410");
    //                    xRootMenuEntry.setPropertyValue("SubContainer", (Object) xSubMenuContainer);

                            // create menu entries for the new sub menu

                            // intialize help/content menu entry
                            // entry "Content"
    //                    XPropertySet xMenuEntry = (XPropertySet) UnoRuntime.queryInterface(
    //                            XPropertySet.class, xMenuElementFactory.createInstance(
    //                            "com.sun.star.ui.ActionTrigger"));
    //
    //                    xMenuEntry.setPropertyValue("Text", new String("Content"));
    //                    xMenuEntry.setPropertyValue("CommandURL", new String("slot:5401"));
    //                    xMenuEntry.setPropertyValue("HelpURL", new String("5401"));
    //
    //                    // insert menu entry to sub menu
    //                    xSubMenuContainer.insertByIndex(0, (Object) xMenuEntry);
    //
    //                    // intialize help/help agent
    //                    // entry "Help Agent"
    //                    xMenuEntry = (com.sun.star.beans.XPropertySet) UnoRuntime.queryInterface(
    //                            com.sun.star.beans.XPropertySet.class,
    //                            xMenuElementFactory.createInstance(
    //                            "com.sun.star.ui.ActionTrigger"));
    //                    xMenuEntry.setPropertyValue("Text", new String("Help Agent"));
    //                    xMenuEntry.setPropertyValue("CommandURL", new String("slot:5962"));
    //                    xMenuEntry.setPropertyValue("HelpURL", new String("5962"));
    //
    //                    // insert menu entry to sub menu
    //                    xSubMenuContainer.insertByIndex(1, (Object) xMenuEntry);

                            // intialize help/tips
                            // entry "Tips"
    //                    xMenuEntry = (com.sun.star.beans.XPropertySet) UnoRuntime.queryInterface(
    //                            com.sun.star.beans.XPropertySet.class,
    //                            xMenuElementFactory.createInstance(
    //                            "com.sun.star.ui.ActionTrigger"));
    //                    xMenuEntry.setPropertyValue("Text", new String("Tips"));
    //                    xMenuEntry.setPropertyValue("CommandURL", new String("slot:5404"));
    //                    xMenuEntry.setPropertyValue("HelpURL", new String("5404"));
    //
    //                    // insert menu entry to sub menu
    //                    xSubMenuContainer.insertByIndex(2, (Object) xMenuEntry);

                            // add separator into the given context menu
                            xContextMenu.insertByIndex(0, (Object) xSeparator);

                            // add new sub menu into the given context menu
                            xContextMenu.insertByIndex(0, (Object) xRootMenuEntry);

                            // The controller should execute the modified context menu and stop notifying other
                            // interceptors.






                            return com.sun.star.ui.ContextMenuInterceptorAction.EXECUTE_MODIFIED;
                        }
                    } catch (com.sun.star.beans.UnknownPropertyException ex) {
                        // do something useful
                        // we used a unknown property 
                    } catch (com.sun.star.lang.IndexOutOfBoundsException ex) {
                        // do something useful
                        // we used an invalid index for accessing a container
                    } catch (com.sun.star.uno.Exception ex) {
                        // something strange has happend!
                    } catch (java.lang.Throwable ex) {
                        // catch java exceptions - do something useful
                    }

                } else {


                    boolean insertedTagAsNode = false;
                    boolean insertedTagAsLink = false;

                    for (int i = 0; i < xContextMenu.getCount() && (!insertedTagAsLink || !insertedTagAsNode); i++) {
                        try {
                            XPropertySet xME = (XPropertySet) QI.XPropertySet(xContextMenu.getByIndex(i));
                            if (xME.getPropertyValue("Text").equals("Tag as node")) {
                                insertedTagAsNode = true;
                            }
                            if (xME.getPropertyValue("Text").equals("Tag as link")) {
                                insertedTagAsLink = true;
                            }
                        } catch (IndexOutOfBoundsException ex) {
                            Logger.getLogger(ContextMenuInterceptor.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (UnknownPropertyException ex) {
                            Logger.getLogger(ContextMenuInterceptor.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (WrappedTargetException ex) {
                            Logger.getLogger(ContextMenuInterceptor.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }


                    if (xShape.getShapeType().contains("ConnectorShape")) {
                        try {
                            com.sun.star.beans.XPropertySet xMenuEntry;

                            if (!insertedTagAsLink) {

                                xMenuEntry = (XPropertySet) UnoRuntime.queryInterface(
                                        com.sun.star.beans.XPropertySet.class,
                                        xMenuElementFactory.createInstance("com.sun.star.ui.ActionTrigger"));






                                // query sub menu for index container to get access
                                //                    com.sun.star.container.XIndexContainer xSubMenuContainer =
                                //                            (com.sun.star.container.XIndexContainer) UnoRuntime.queryInterface(
                                //                            com.sun.star.container.XIndexContainer.class,
                                //                            xMenuElementFactory.createInstance(
                                //                            "com.sun.star.ui.ActionTriggerContainer"));

                                // intialize root menu entry "Help"
    //                    if (!tagAsLinkInserted) {
                                xMenuEntry.setPropertyValue("Text", "Tag as link");

                                xMenuEntry.setPropertyValue("CommandURL", "ru.ssau.graphplus:TagAsLink");
                                xMenuEntry.setPropertyValue("HelpURL", "5410");
                                xContextMenu.insertByIndex(0, (Object) xMenuEntry);
                                tagAsLinkInserted = true;
                            }
    //                    }
                        } catch (UnknownPropertyException ex) {
                            Logger.getLogger(ContextMenuInterceptor.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (PropertyVetoException ex) {
                            Logger.getLogger(ContextMenuInterceptor.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IllegalArgumentException ex) {
                            Logger.getLogger(ContextMenuInterceptor.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (WrappedTargetException ex) {
                            Logger.getLogger(ContextMenuInterceptor.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (Exception ex) {
                            Logger.getLogger(ContextMenuInterceptor.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    else {
                    //if (xShape.getShapeType().contains("Ellipse")) {
                        try {






                            if (!insertedTagAsNode) {

                                com.sun.star.beans.XPropertySet xMenuEntry;

                                xMenuEntry = (XPropertySet) UnoRuntime.queryInterface(
                                        com.sun.star.beans.XPropertySet.class,
                                        xMenuElementFactory.createInstance("com.sun.star.ui.ActionTrigger"));

    //if (xContextMenu.)
    //                    if (!tagAsNodeInserted) {
                                xMenuEntry.setPropertyValue("Text", "Tag as node");
                                xMenuEntry.setPropertyValue("CommandURL", "ru.ssau.graphplus:TagAsNode");
                                xMenuEntry.setPropertyValue("HelpURL", "5410");
                                xContextMenu.insertByIndex(0, (Object) xMenuEntry);
                                tagAsNodeInserted = true;
    //                    }
                            }

                        } catch (UnknownPropertyException ex) {
                            Logger.getLogger(ContextMenuInterceptor.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (PropertyVetoException ex) {
                            Logger.getLogger(ContextMenuInterceptor.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IllegalArgumentException ex) {
                            Logger.getLogger(ContextMenuInterceptor.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (WrappedTargetException ex) {
                            Logger.getLogger(ContextMenuInterceptor.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (Exception ex) {
                            Logger.getLogger(ContextMenuInterceptor.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            } catch (UnknownPropertyException ex) {
                Logger.getLogger(ContextMenuInterceptor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (WrappedTargetException ex) {
                Logger.getLogger(ContextMenuInterceptor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }






        return ContextMenuInterceptorAction.CONTINUE_MODIFIED;
        //throw new UnsupportedOperationException("Not supported yet.");
    }
}
