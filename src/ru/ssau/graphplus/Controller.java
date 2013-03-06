/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ssau.graphplus;

/**
 *
 * @author 1
 */
import com.sun.star.awt.Point;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.Locale;
import ru.ssau.graphplus.gui.Gui;
import ru.ssau.graphplus.link.Linker;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XNamed;
import com.sun.star.drawing.*;
import com.sun.star.frame.XController;
import com.sun.star.frame.XFrame;
import com.sun.star.lang.*;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.XModifyListener;
import com.sun.star.view.XSelectionChangeListener;
import com.sun.star.view.XSelectionSupplier;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.IndexOutOfBoundsException;
import ru.ssau.graphplus.node.Node;

/**
 *
 * @author anton
 */
public final class  Controller implements XSelectionChangeListener, XModifyListener {

    public enum State {
        Nothing,
        InputTwoShapes
    }
    State state;

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }


    private XComponentContext m_xContext = null;
    private XFrame m_xFrame = null;
    private XController m_xController = null;
    private XSelectionSupplier m_xSelectionSupplier = null;
    private ArrayList<XShape> nodes = null;
    private ArrayList<XShape> links = null;
    private Map<String, XShape> elements = null;
    private XMultiServiceFactory xMSF = null;
    private XMultiComponentFactory xMCF = null;
    private Map<XShape, DiagramElement> whichElementContainsShape = null;

    private List<String> historyOfActions;


    public List<String> getHistoryOfActions() {
        return historyOfActions;
    }

    private Map<Object, Point> positions = null;

    XDrawPage xDP = null;
    int count = 0;

    Controller(XComponentContext xContext, XFrame xFrame, XMultiServiceFactory xMSF_, XMultiComponentFactory xMCF_, XDrawPage xDP_) {

        m_xContext = xContext;
        m_xFrame = xFrame;
        m_xController = m_xFrame.getController();
        xMCF = xMCF_;
        xMSF = xMSF_;
        xDP = xDP_;

        nodes = new ArrayList<XShape>();
        links = new ArrayList<XShape>();
        elements = new HashMap<String, XShape>();
        whichElementContainsShape = new HashMap<XShape, DiagramElement>();
        positions = new HashMap<Object, Point>();
        addSelectionListener();
        historyOfActions = new ArrayList<String>();
    }

    public Map<String, XShape> getElements() {
        return elements;
    }

    public void addSelectionListener() {
        if (m_xSelectionSupplier == null) {
            m_xSelectionSupplier = (XSelectionSupplier) UnoRuntime.queryInterface(XSelectionSupplier.class, m_xController);
        }
        if (m_xSelectionSupplier != null) {
            m_xSelectionSupplier.addSelectionChangeListener(this);
        }
    }

    public void removeSelectionListener() {
        if (m_xSelectionSupplier != null) {
            m_xSelectionSupplier.removeSelectionChangeListener(this);
        }
    }

    public XDrawPage getCurrentPage() {
        XDrawView xDrawView = (XDrawView) UnoRuntime.queryInterface(XDrawView.class, m_xController);
        return xDrawView.getCurrentPage();
    }

    public Locale getLocation() {
        Locale locale = null;
        try {
            XMultiComponentFactory xMCF = m_xContext.getServiceManager();
            Object oConfigurationProvider = xMCF.createInstanceWithContext("com.sun.star.configuration.ConfigurationProvider", m_xContext);
            XLocalizable xLocalizable = (XLocalizable) UnoRuntime.queryInterface(XLocalizable.class, oConfigurationProvider);
            locale = xLocalizable.getLocale();
        } catch (Exception ex) {
            System.err.println(ex.getLocalizedMessage());
        }
        return locale;
    }

    public String getNumberStrOfShape(String name) {
        String s = "";
        char[] charName = name.toCharArray();
        int i = 0;
        while (i < name.length() && charName[i] != '-') {
            i++;
        }
        while (i < name.length() && (charName[i] < 48 || charName[i] > 57)) {
            i++;
        }
        while (i < name.length()) {
            s += charName[i++];
        }
        return s;
    }

    public int getNumberOfShape(String name) {
        return parseInt(getNumberStrOfShape(name));
    }

    public int parseInt(String s) {
        int n = -1;
        try {
            n = Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            System.err.println(ex.getLocalizedMessage());
        }
        return n;
    }

    // XSelectionChangeListener

    public void disposing(EventObject arg0) {
    }
//    boolean fistSelected = false;
//    boolean secondSelected = false;
    XShape firstShape;
    XShape secondShape;
    Linker linker;

    public Linker getLinker() {
        return linker;
    }

    public void setLinker(Linker linker) {
        this.linker = linker;
    }

    // XSelectionChangeListener
    @Override
    public void selectionChanged(EventObject event) {
        System.out.println("selectionChanged");

        Object shapeObj = getSelectedShape();
        if (shapeObj != null) {
            XNamed xNamed = (XNamed) UnoRuntime.queryInterface(XNamed.class, getSelectedShape());
            String selectedShapeName = xNamed.getName();
            // listen the diagrams
            Misc.printInfo(shapeObj);

            if (state.equals(State.InputTwoShapes)) {
                if (firstShape != null && secondShape != null) {
                    firstShape = null;
                    secondShape = null;
                }


                if (firstShape == null) {
                    firstShape = QI.XShape(shapeObj);
                } else {
                    if (secondShape == null) {
                        secondShape = QI.XShape(shapeObj);
                        // 2 shapes 
                        if (linker != null) {
                            linker.link(firstShape, secondShape);

                            setState(State.Nothing);
//                        LinkAdjuster.adjustLink((Link)linker);
                            setSelectedShape(linker.getTextShape());
                        }
                    }

                }
            } else {
            }
        }

    }

    public void setSelectedShape(Object obj) {
        try {
            m_xSelectionSupplier.select(obj);
        } catch (IllegalArgumentException ex) {
            System.err.println(ex.getLocalizedMessage());
        }
    }

    public boolean isOnlySimpleItemIsSelected() {
        if (getSelectedShapes().getCount() == 1) {
//            XNamed xNamed = (XNamed) UnoRuntime.queryInterface(XNamed.class, getSelectedShape());
//            String selectedShapeName = xNamed.getName();
//            if ((selectedShapeName.startsWith("OrganizationDiagram") || selectedShapeName.startsWith("SimpleOrganizationDiagram") || selectedShapeName.startsWith("HorizontalOrganizationDiagram") || selectedShapeName.startsWith("TableHierarchyDiagram")) && selectedShapeName.contains("RectangleShape") && !selectedShapeName.endsWith("RectangleShape0")) {
            return true;
//            }
        }
        return false;
    }

    public XShapes getSelectedShapes() {
        return (XShapes) UnoRuntime.queryInterface(XShapes.class, m_xSelectionSupplier.getSelection());
    }

    public XShape getSelectedShape(int i) {
        try {
            XShapes xShapes = getSelectedShapes();
            if (xShapes != null) {
                return (XShape) UnoRuntime.queryInterface(XShape.class, xShapes.getByIndex(i));
            }
        } catch (IndexOutOfBoundsException ex) {
            System.err.println(ex.getLocalizedMessage());
        } catch (WrappedTargetException ex) {
            System.err.println(ex.getLocalizedMessage());
        }
        return null;
    }

    public XShape getSelectedShape() {
        return getSelectedShape(0);
    }

    public void modified(EventObject arg0) {

        System.out.println("modified");
        Misc.printInfo(arg0.Source);



        System.out.println(arg0.toString());


        if (xDP.getCount() > count) {
            count++;
            System.out.println("added new shape");

            Object obj = null;
            try {
                obj = xDP.getByIndex(xDP.getCount() - 1);
            } catch (com.sun.star.lang.IndexOutOfBoundsException ex) {
                Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
            } catch (WrappedTargetException ex) {
                Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
            }
            XShape xShape = (XShape) UnoRuntime.queryInterface(XShape.class, obj);
            positions.put(obj, xShape.getPosition());
            if (Misc.isNode(xShape) || Misc.isLink(xShape)){

            }
            else {
                Misc.addUserDefinedAttributes(xShape, xMSF);
            }
            System.out.println(xShape.getShapeType());
            //if (xShape.getShapeType().equals("com.sun.star.drawing.ConnectorShape")) {
            //if (Misc.isLink(xShape)) {
            if (xShape.getShapeType().contains("Text")) {
//                System.out.print("Text need to remove");
//                xDP.remove(xShape);
            }


            if (Status.isTagAllNewShapes() && xShape.getShapeType().equals("com.sun.star.drawing.ConnectorShape")) {

//                ShapeHelper.createAndInsertShape(m_xFrame, x, null, null, null)
                //xShape.getShapeType().equals("com.sun.star.drawing.ConnectorShape")) {

                links.add(xShape);


                System.out.println("ConnectorShape added");
                XConnectorShape xConnSh = (XConnectorShape) UnoRuntime.queryInterface(XConnectorShape.class, xShape);

                xMCF = m_xContext.getServiceManager();
                xMSF = (XMultiServiceFactory) UnoRuntime.queryInterface(XMultiServiceFactory.class, xMCF);

                try {


                    XPropertySet xShapeProps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xConnSh);
                    Object startShape = xShapeProps.getPropertyValue("StartShape");
                    Object endShape = xShapeProps.getPropertyValue("EndShape");
                    XShape xShStart = (XShape) UnoRuntime.queryInterface(XShape.class, startShape);
                    XShape xShEnd = (XShape) UnoRuntime.queryInterface(XShape.class, endShape);


                    XPropertySet xShStartProps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xShStart);


                    XPropertySet xShEndProps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xShEnd);

                    XNamed xNamedConnector = (XNamed) UnoRuntime.queryInterface(XNamed.class, xConnSh);
                    Misc.tagShapeAsLink(xShape);
                    Gui.createDialog(xNamedConnector, xConnSh, m_xContext, elements);
                    // TODO
                    elements.put(QI.XNamed(xShape).getName(), xShape);
                    XPropertySet xPS = QI.XPropertySet(xShape);
                    xPS.setPropertyValue("Text", QI.XNamed(xShape).getName());
                    //xConnSh.
                } catch (com.sun.star.uno.Exception ex) {
                    Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                if (Status.isTagAllNewShapes() && xShape.getShapeType().equals("com.sun.star.drawing.EllipseShape")) {
                    //if (xShape.getShapeType().equals("com.sun.star.drawing.EllipseShape")) {
                    //if (Misc.isNode(xShape)) {
                    nodes.add(xShape);


//                    count++;


                    System.out.println("Not Connector shape");
                    XPropertySet xShapeProps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xShape);




                    //xShapeProps.setPropertyValue("Name", lastEnteredName);
                    String s;
                    XNamed xNamed = (XNamed) UnoRuntime.queryInterface(
                            XNamed.class, xShape);
                    Misc.tagShapeAsNode(xShape);
                    try {
                        Gui.createDialog(xNamed, xShape, m_xContext, elements);
                    } catch (com.sun.star.uno.Exception ex) {
                        Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    // TODO
                    elements.put(QI.XNamed(xShape).getName(), xShape);




                    //System.out.println("lastEnteredName" + lastEnteredName);
                    ///xNamed.setName(lastEnteredName);


                    ///String name = (String) xNamed.getName();
                    //System.out.println(name);




                }
            }

        }
    }


    private Collection<Object> checkPositions(){



        for (int i = 0 ; i < xDP.getCount(); i++){
            try {
                Object byIndex = xDP.getByIndex(i);
                XShape xShape = QI.XShape(byIndex);
                if (!positions.get(byIndex).equals(xShape.getPosition())){
                    DiagramElement diagramElement = whichElementContainsShape.get(xShape);
                    if (diagramElement instanceof Node){
                        Node node = (Node) diagramElement;
                        //  TODO adjust all related links
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                break;
            } catch (WrappedTargetException e) {
                e.printStackTrace();
            }
        }

         return Collections.EMPTY_LIST;

    }

    public void addNode(String name) throws Exception {
    }

    public void addLink(String name) throws Exception {
    }
}