package ru.ssau.graphplus;

import com.sun.star.container.XNamed;
import com.sun.star.drawing.XConnectorShape;
import com.sun.star.drawing.XShape;
import com.sun.star.drawing.XShapes;
import com.sun.star.lang.*;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.text.XText;
import com.sun.star.uno.UnoRuntime;
import ru.ssau.graphplus.link.Link;
import ru.ssau.graphplus.node.Node;

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

    public InputTwoShapesMode(DiagramController diagramController) {
        this();
        this.diagramController = diagramController;
    }
    public InputTwoShapesMode() {
        state = INPUT_TWO_SHAPES;
    }

    public void setDiagramController(DiagramController diagramController) {
        this.diagramController = diagramController;
    }

    private void setState(int state){
        this.state = state;
    }

    @Override
    public void onInput(EventObject eventObject, DiagramController diagramController) {
        System.out.println("selectionChanged");


        Object shapeObj = diagramController.getSelectedShape();
        Misc.printInfo(shapeObj);
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
                                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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
                if (diagramController.getDiagramModel().getShapeToDiagramElementMap().containsKey(selectedShape) && Misc.isTextShape(selectedShape)) {
//                    try {
//                        Object text = QI.XPropertySet(selectedShape).getPropertyValue("Text");
                    XText xText = QI.XText(selectedShape);

                    if (xText.getString().equals("") || xText == null) {

                    } else {
                        //setSelectedShape(null);
                    }
//                    } catch (UnknownPropertyException e) {
//                        e.printStackTrace();
//                    } catch (WrappedTargetException e) {
//                        e.printStackTrace();
//                    }
                }
            }
        }

        if (shapeObj != null) {
            XNamed xNamed = (XNamed) UnoRuntime.queryInterface(XNamed.class, diagramController.getSelectedShape());
            String selectedShapeName = xNamed.getName();
            // listen the diagrams
            //Misc.printInfo(shapeObj);

            if (state.equals(INPUT_TWO_SHAPES)) {
                if (diagramController.firstShape != null && diagramController.secondShape != null) {
                    diagramController.firstShape = null;
                    diagramController.secondShape = null;
                }

                XShape xShape = QI.XShape(shapeObj);
                boolean connectorShape = Misc.isConnectorShape(xShape);
                if (diagramController.firstShape == null) {

                    if (!connectorShape) {
                        diagramController.firstShape = xShape;
                    }

                } else {
                    if (diagramController.secondShape == null) {

                        if (!connectorShape) {
                            diagramController.secondShape = xShape;
                            // 2 shapes
                            if (diagramController.linker != null) {
                                diagramController.linker.link(diagramController.firstShape, diagramController.secondShape);
                                DiagramElement diagramElement = diagramController.getDiagramModel().getShapeToDiagramElementMap().get(diagramController.firstShape);
                                DiagramElement diagramElement1 = diagramController.getDiagramModel().getShapeToDiagramElementMap().get(diagramController.secondShape);

                                if (!(diagramElement instanceof Node) || !(diagramElement1 instanceof Node)) {

                                    diagramController.firstShape = null;
                                    diagramController.secondShape = null;
                                    state = INPUT_TWO_SHAPES;
                                } else {
                                    Node node1 = (Node) diagramElement;
                                    Node node2 = (Node) diagramElement1;


                                    //TODO awful
                                    Link link = (Link) diagramController.linker;

                                    link.setNode1(node1);
                                    link.setNode2(node2);

                                    diagramController.getDiagramModel().getConnectedShapes().put(QI.XConnectorShape(link.getConnShape1()), new ConnectedShapes(QI.XConnectorShape(link.getConnShape1())));
                                    diagramController.getDiagramModel().getConnectedShapes().put(QI.XConnectorShape(link.getConnShape2()), new ConnectedShapes(QI.XConnectorShape(link.getConnShape2())));

                                    setState(NOTHING);
//                        LinkAdjuster.adjustLink((Link)linker);
                                    diagramController.setSelectedShape(diagramController.linker.getTextShape());
                                    // TODO remove next
                                    Link linker1 = (Link) diagramController.linker;
                                    diagramController.resetInputMode();
                                    //QI.XPropertySet(linker1.getConnShape1()).addPropertyChangeListener();
                                }

                                diagramController.statusChangedEnable(DiagramController.getLastURL());
                            }
                        }

                    } else {

                    }

                }
            }
        }
    }





}
