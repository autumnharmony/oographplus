package ru.ssau.graphplus.link;

import com.sun.star.container.XNamed;
import com.sun.star.drawing.XConnectorShape;
import com.sun.star.drawing.XShape;
import com.sun.star.drawing.XShapes;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.text.XText;
import com.sun.star.uno.UnoRuntime;
import ru.ssau.graphplus.*;
import ru.ssau.graphplus.api.DiagramElement;
import ru.ssau.graphplus.api.Link;
import ru.ssau.graphplus.commons.*;
import ru.ssau.graphplus.node.NodeBase;

/**
 * User: anton
 * Date: 5/18/13
 * Time: 6:09 PM
 */
public class InputTwoShapesMode implements InputMode {

    private static final int NOTHING = 0;
    private static final int INPUT_TWO_SHAPES = 1;
    private static final int FIRST_SHAPE_ENTERED = 2;
    private Integer state;
    private DiagramController diagramController;

    private Link link;

    XShape firstShape;
    XShape secondShape;

    public Link getLink() {
        return link;
    }

    public void setLink(Link link) {
        this.link = link;
    }

    public InputTwoShapesMode(DiagramController diagramController) {
        this();
        this.diagramController = diagramController;
    }

    public InputTwoShapesMode(DiagramController diagramController, Link link) {
        this();
        this.diagramController = diagramController;
        this.link = link;

    }

    public InputTwoShapesMode() {
        state = INPUT_TWO_SHAPES;
    }

    public void setDiagramController(DiagramController diagramController) {
        this.diagramController = diagramController;
    }

    private void setState(int state) {
        this.state = state;
    }

    @Override
    public void onInput(EventObject eventObject) {
        System.out.println("selectionChanged");


        Object shapeObj = diagramController.getSelectedShape();
//        Misc.printInfo(shapeObj);
        XShapes selectedShapes = diagramController.getSelectedShapes();

        if (selectedShapes != null) {
            if (selectedShapes.getCount() > 1) {

                // prevent selecting more than one shape from link

                System.out.println("more than one selected");
                for (int i = 0; i < selectedShapes.getCount(); i++) {

                    Object byIndex = null;
                    try {
                        byIndex = selectedShapes.getByIndex(i);
                        XShape xShape = QI.XShape(byIndex);
                        XConnectorShape xConnectorShape = QI.XConnectorShape(byIndex);
                        boolean text = xShape.getShapeType().contains("Text");
                        if (xConnectorShape != null || text) {
                            // connector

                            for (int j = 0; j < selectedShapes.getCount(); j++) {
                                if (i != j) {
                                    Object byIndex2 = null;
                                    try {
                                        byIndex2 = selectedShapes.getByIndex(j);
                                        XShape xShape2 = QI.XShape(byIndex2);
                                        XConnectorShape xConnectorShape2 = QI.XConnectorShape(byIndex2);
                                        boolean text2 = xShape2.getShapeType().contains("Text");
                                        if (xConnectorShape2 != null || text2) {


                                            // each pair of connectors
                                            if (diagramController.getDiagramModel().getShapeToDiagramElementMap().containsKey(xShape) &&
                                                    diagramController.getDiagramModel().getShapeToDiagramElementMap().containsKey(xShape2)
                                                    ) {
                                                if (diagramController.getDiagramModel().getShapeToDiagramElementMap().get(xShape).equals(diagramController.getDiagramModel().getShapeToDiagramElementMap().get(xShape2))) {
                                                    //xConnectorShape2
                                                    selectedShapes.remove(xShape2);
                                                    diagramController.getXSelectionSupplier().select(selectedShapes);
                                                }
                                            }
                                        }
                                    } catch (com.sun.star.lang.IndexOutOfBoundsException e) {
                                        e.printStackTrace();
                                    } catch (WrappedTargetException e) {
                                        e.printStackTrace();
                                    } catch (IllegalArgumentException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            // if selected two connectors which belongs to one link


                        }
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    } catch (WrappedTargetException e) {
                        e.printStackTrace();
                    }


                }
            } else {
                //1 selected shape
                XShape selectedShape = diagramController.getSelectedShape();
                if (diagramController.getDiagramModel().getShapeToDiagramElementMap().containsKey(selectedShape) && ShapeHelper.isTextShape(selectedShape)) {

                    XText xText = QI.XText(selectedShape);

                    if (xText.getString().equals("") || xText == null) {

                    } else {
                    }

                }
            }
        }

        if (shapeObj != null) {
            XNamed xNamed = (XNamed) UnoRuntime.queryInterface(XNamed.class, diagramController.getSelectedShape());
            String selectedShapeName = xNamed.getName();
            // listen the diagrams
            //Misc.printInfo(shapeObj);

            if (state.equals(INPUT_TWO_SHAPES)) {
                if (firstShape != null && secondShape != null) {
                    firstShape = null;
                    secondShape = null;
                }

                XShape xShape = QI.XShape(shapeObj);
                boolean connectorShape = ShapeHelper.isConnectorShape(xShape);
                if (firstShape == null) {

                    if (!connectorShape) {
                        firstShape = xShape;
                    }

                } else {
                    if (secondShape == null) {

                        if (!connectorShape) {
                            secondShape = xShape;
                            // 2 shapes
//                            if (diagramController.getLinker() != null) {
////                                diagramController.getLinker().link(firstShape, secondShape);
//                                DiagramElement diagramElement = diagramController.getDiagramModel().getDiagramElementByShape(firstShape);
//                                DiagramElement diagramElement1 = diagramController.getDiagramModel().getDiagramElementByShape(secondShape);
//
//                                if (!(diagramElement instanceof NodeBase) || !(diagramElement1 instanceof NodeBase)) {
//
//                                    firstShape = null;
//                                    secondShape = null;
//                                    state = INPUT_TWO_SHAPES;
//                                } else {
//                                    NodeBase node1 = (NodeBase) diagramElement;
//                                    NodeBase node2 = (NodeBase) diagramElement1;
//
//
//                                    //TODO awful
////                                    LinkBase link = (LinkBase) diagramController.getLinker();
//                                    LinkTwoConnectorsAndTextBase link = (LinkTwoConnectorsAndTextBase) getLink();
//
//
//                                    link.setStartNode(node1);
//                                    link.setEndNode(node2);
//
//                                    // TODO DI
//                                    diagramController.getDiagramModel().getConnectedShapes().put(QI.XConnectorShape(link.getConnShape1()), new ConnectedShapes(QI.XConnectorShape(link.getConnShape1()), new UnoRuntimeWrapperImpl(), new ShapeHelperWrapperImpl()));
//                                    diagramController.getDiagramModel().getConnectedShapes().put(QI.XConnectorShape(link.getConnShape2()), new ConnectedShapes(QI.XConnectorShape(link.getConnShape2()), new UnoRuntimeWrapperImpl(), new ShapeHelperWrapperImpl()));
//
//                                    setState(NOTHING);
////                        LinkAdjuster.adjustLink((LinkBase)linker);
//                                    diagramController.setSelectedShape(diagramController.getLinker().getTextShape());
//                                    // TODO remove next
//                                    diagramController.resetInputMode();
//                                    //QI.XPropertySet(linker1.getConnShape1()).addPropertyChangeListener();
//                                }
//
//                                diagramController.statusChangedEnable(DiagramController.getLastURL());
//                            }
                        }

                    } else {

                    }

                }
            }
        }
    }


}
