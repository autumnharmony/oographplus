/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.gui.dialogs;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.sun.star.awt.*;
import com.sun.star.awt.tree.XMutableTreeDataModel;
import com.sun.star.awt.tree.XMutableTreeNode;
import com.sun.star.awt.tree.XTreeNode;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.lang.*;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.uno.Any;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.view.SelectionType;
import com.sun.star.view.XSelectionChangeListener;
import com.sun.star.view.XSelectionSupplier;
import ru.ssau.graphplus.DiagramController;
import ru.ssau.graphplus.MyDialogHandler;
import ru.ssau.graphplus.api.DiagramElement;
import ru.ssau.graphplus.api.DiagramService;
import ru.ssau.graphplus.commons.QI;
import ru.ssau.graphplus.gui.MyDialog;
import ru.ssau.graphplus.validation.ValidationResult;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: anton
 * Date: 5/2/14
 * Time: 1:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class ValidationDialog implements MyDialog<GetCodeDialog> {

    public static final String VALIDATION_DIALOG_XDL = "vnd.sun.star.extension://ru.ssau.graphplus.oograph/dialogs/ValidationDialog.xdl";
    private XWindow window;
    private XMutableTreeDataModel mutableTreeDataModel;
    private DiagramService diagramService;

    @Override
    public MyDialogHandler getDialogHandler() {
        return new MyDialogHandler(ImmutableMap.<MyDialogHandler.Event, MyDialogHandler.EventHandler>builder().build());
    }

    private Map<XMutableTreeNode, DiagramElement> map = new HashMap<>();

    public void init(XWindow xWindow, ValidationResult validationResult, XMutableTreeDataModel xControlModel, final DiagramService diagramService) {
        this.diagramService = diagramService;
        this.window = xWindow;
        this.mutableTreeDataModel = xControlModel;
        XControlContainer xControlContainer = UnoRuntime.queryInterface(XControlContainer.class, xWindow);
        XControl treeControl = xControlContainer.getControl("treeControl");
        XSelectionSupplier xSelectionSupplier = QI.XSelectionSupplier(treeControl);
        xSelectionSupplier.addSelectionChangeListener(new XSelectionChangeListener() {
            @Override
            public void selectionChanged(EventObject eventObject) {
                System.out.println(eventObject.Source);

                Object selection = QI.XSelectionSupplier(eventObject.Source).getSelection();
                if (selection != null) {
                    XTreeNode xTreeNode = UnoRuntime.queryInterface(XTreeNode.class, selection);
                    XMutableTreeNode xMutableTreeNode = UnoRuntime.queryInterface(XMutableTreeNode.class, xTreeNode);
                    xMutableTreeNode.getDataValue();
                    xTreeNode.getDisplayValue();
                    DiagramElement diagramElement = map.get(xMutableTreeNode);
                    diagramService.select(diagramElement);

                }


            }

            @Override
            public void disposing(EventObject eventObject) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });

        XControlModel model = treeControl.getModel();
        try {
            XPropertySet xPropertySet = QI.XPropertySet(model);
            xPropertySet.setPropertyValue("DataModel", xControlModel);
            xPropertySet.setPropertyValue("SelectionType", SelectionType.SINGLE);
        } catch (UnknownPropertyException | PropertyVetoException | IllegalArgumentException | WrappedTargetException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        ;

        XMutableTreeNode node = mutableTreeDataModel.createNode("Validation result", false);
        try {
            mutableTreeDataModel.setRoot(node);
        } catch (com.sun.star.lang.IllegalArgumentException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        for (ValidationResult.Item item : validationResult.getItems()) {
            try {
                XMutableTreeNode node1 = mutableTreeDataModel.createNode(item.getFullDescription(), false);
                map.put(node1, item.getDiagramElement());
//                node1.getDataValue(item.getDiagramElement());
//                node1.
                node.appendChild(node1);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

    }
}
