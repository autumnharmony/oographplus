/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ssau.graphplus.gui;

import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XIndexContainer;
import com.sun.star.container.XNameContainer;
import com.sun.star.drawing.XShape;
import com.sun.star.drawing.XShapes;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.*;
import com.sun.star.ui.ActionTriggerSeparatorType;
import com.sun.star.ui.ContextMenuExecuteEvent;
import com.sun.star.ui.ContextMenuInterceptorAction;
import com.sun.star.ui.XContextMenuInterceptor;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import ru.ssau.graphplus.DiagramController;
import ru.ssau.graphplus.DiagramElement;
import ru.ssau.graphplus.Misc;
import ru.ssau.graphplus.QI;
import ru.ssau.graphplus.link.Link;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ContextMenuInterceptor implements XContextMenuInterceptor {

    public static final String EDIT_LINK = "Edit link";
    private final DiagramController diagramController;
    XComponentContext context;
//    boolean tagAsLinkInserted;
//    boolean tagAsNodeInserted;
//    boolean editLinkInserted;


    public static final Collection<String> SLOTS_TO_REMOVE = new ArrayList<String>(); // ArrayList<String>(Arrays.asList("slot:27027", "slot:27033"));

    public ContextMenuInterceptor(XComponentContext context, DiagramController diagramController) {
        this.context = context;
        this.diagramController = diagramController;
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

        XIndexContainer menuItems = arg0.ActionTriggerContainer;
        com.sun.star.container.XIndexContainer xContextMenu = menuItems;
        com.sun.star.lang.XMultiServiceFactory xMenuElementFactory =
                (com.sun.star.lang.XMultiServiceFactory) UnoRuntime.queryInterface(
                        com.sun.star.lang.XMultiServiceFactory.class, xContextMenu);


        Object selection = arg0.Selection.getSelection();
        XShape xShape = null;
        XServiceInfo xServiceInfo = (XServiceInfo) UnoRuntime.queryInterface(
                XServiceInfo.class, selection);
        XShapes xSelection = (XShapes) UnoRuntime.queryInterface(
                XShapes.class, selection);


        if (xSelection.getCount() > 2) {
            return ContextMenuInterceptorAction.CONTINUE_MODIFIED;
        }

        if (xSelection.getCount() == 2) {

            insertFor2Selected(xContextMenu, xMenuElementFactory, xSelection);

        } else {

            //count == 1
            if (insertFor1Selected(menuItems, xContextMenu, xMenuElementFactory, xShape, xSelection))
                return ContextMenuInterceptorAction.EXECUTE_MODIFIED;
        }


        return ContextMenuInterceptorAction.CONTINUE_MODIFIED;
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    private boolean insertFor1Selected(XIndexContainer menuItems, XIndexContainer xContextMenu, XMultiServiceFactory xMenuElementFactory, XShape xShape, XShapes xSelection) {
        try {


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
                        XPropertySet xRootMenuEntry =
                                (XPropertySet) UnoRuntime.queryInterface(
                                        XPropertySet.class,
                                        xMenuElementFactory.createInstance("com.sun.star.ui.ActionTrigger"));

                        // create a line separator for our new help sub menu
                        XPropertySet xSeparator =
                                (XPropertySet) UnoRuntime.queryInterface(
                                        XPropertySet.class,
                                        xMenuElementFactory.createInstance("com.sun.star.ui.ActionTriggerSeparator"));

                        Short aSeparatorType = new Short(ActionTriggerSeparatorType.LINE);
                        xSeparator.setPropertyValue("SeparatorType", (Object) aSeparatorType);

                        // intialize root menu entry "Help"
                        //xRootMenuEntry.setPropertyValue("Text", Misc.toString(xShape));
                        xRootMenuEntry.setPropertyValue("Text", "ZXCZXC");
                        xRootMenuEntry.setPropertyValue("CommandURL", "ru.ssau.graphplus.oograph:Omg");
                        //                    xRootMenuEntry.setPropertyValue("", selection);


                        // add separator into the given context menu
                        xContextMenu.insertByIndex(0, xSeparator);

                        // add new sub menu into the given context menu
                        xContextMenu.insertByIndex(0, xRootMenuEntry);

                        // The controller should execute the modified context menu and stop notifying other
                        // interceptors.


                        return true;
                    }
                } catch (UnknownPropertyException ex) {
                    // do something useful
                    // we used a unknown property
                } catch (IndexOutOfBoundsException ex) {
                    // do something useful
                    // we used an invalid index for accessing a container
                } catch (Exception ex) {
                    // something strange has happend!
                } catch (Throwable ex) {
                    // catch java exceptions - do something useful
                }

            } else {


                boolean insertedTagAsNode = false;
                boolean insertedTagAsLink = false;
                boolean insertedEditLink = false;

                for (int i = 0; i < xContextMenu.getCount() && (!insertedTagAsLink || !insertedTagAsNode); i++) {
                    try {
                        XPropertySet xME = (XPropertySet) QI.XPropertySet(xContextMenu.getByIndex(i));
                        if (xME.getPropertyValue("Text").equals("Tag as node")) {
                            insertedTagAsNode = true;
                        }
                        if (xME.getPropertyValue("Text").equals("Tag as link")) {
                            insertedTagAsLink = true;
                        }

                        if (xME.getPropertyValue("Text").equals(EDIT_LINK)) {
                            insertedEditLink = true;
                        }
                    } catch (IndexOutOfBoundsException ex) {
                        Logger.getLogger(ContextMenuInterceptor.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (UnknownPropertyException ex) {
                        Logger.getLogger(ContextMenuInterceptor.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (WrappedTargetException ex) {
                        Logger.getLogger(ContextMenuInterceptor.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }


                if (Misc.isConnectorShape(xShape)) {
                    XPropertySet xMenuEntry;

                    try {

                        if (!insertedTagAsLink) {

                            xMenuEntry = (XPropertySet) UnoRuntime.queryInterface(
                                    XPropertySet.class,
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
//                                tagAsLinkInserted = true;
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

                    if (!insertedEditLink) {
                        DiagramElement diagramElement = diagramController.getDiagramModel().getShapeToDiagramElementMap().get(xShape);
                        if (diagramElement != null) {
                            if (diagramElement instanceof Link) {
                                try {
                                    xMenuEntry = (XPropertySet) UnoRuntime.queryInterface(
                                            XPropertySet.class,
                                            xMenuElementFactory.createInstance("com.sun.star.ui.ActionTrigger"));
                                    xMenuEntry.setPropertyValue("Text", EDIT_LINK);

                                    xMenuEntry.setPropertyValue("CommandURL", "ru.ssau.graphplus:EditLink");
                                    xContextMenu.insertByIndex(1, (Object) xMenuEntry);
                                    insertedEditLink = true;

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                } else {
                    //if (xShape.getShapeType().contains("Ellipse")) {
                    try {


                        if (!insertedTagAsNode) {

                            XPropertySet xMenuEntry;

                            xMenuEntry = (XPropertySet) UnoRuntime.queryInterface(
                                    XPropertySet.class,
                                    xMenuElementFactory.createInstance("com.sun.star.ui.ActionTrigger"));

                            //if (xContextMenu.)
                            //                    if (!tagAsNodeInserted) {
                            xMenuEntry.setPropertyValue("Text", "Tag as node");
                            xMenuEntry.setPropertyValue("CommandURL", "ru.ssau.graphplus:TagAsNode");
                            xMenuEntry.setPropertyValue("HelpURL", "5410");
                            xContextMenu.insertByIndex(0, (Object) xMenuEntry);
//                                tagAsNodeInserted = true;
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
        return false;
    }

    private void insertFor2Selected(XIndexContainer xContextMenu, XMultiServiceFactory xMenuElementFactory, XShapes xSelection) {
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
                    XPropertySet assocTextItem =
                            (XPropertySet) UnoRuntime.queryInterface(
                                    XPropertySet.class,
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
    }
}
