


/****************************************************************
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 *************************************************************/
package ru.ssau.graphplus;

import com.sun.star.document.*;
import com.sun.star.drawing.XConnectorShape;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import ru.ssau.graphplus.api.DiagramElement;
import ru.ssau.graphplus.commons.*;
import ru.ssau.graphplus.events.*;
import ru.ssau.graphplus.api.Link;
import ru.ssau.graphplus.link.LinkTwoConnectorsAndTextBase;

import java.util.*;

/**
 * @author ariel
 */
public class ConnectorShapeListener {

    boolean muteInsert;
    boolean muteModify;
    boolean muteRemove;

    public void setMuteInsert(boolean muteInsert) {
        this.muteInsert = muteInsert;
    }

    public void setMuteModify(boolean muteModify) {
        this.muteModify = muteModify;
    }

    public void setMuteRemove(boolean muteRemove) {
        this.muteRemove = muteRemove;
    }

    private final XComponentContext m_xContext;
    private final DiagramController diagramController;
    private DocumentListener maListener;
    private XDocumentEventBroadcaster m_xNewBroadcaster;
    private XEventBroadcaster m_xOldBroadcaster;
    private List<com.sun.star.document.EventObject> removeShapeEvents;

    public ConnectorShapeListener(XComponentContext xContext, XComponent xDoc, Map<XConnectorShape, ConnectedShapes> maConnectorsMap, DiagramController diagramController) {
        this.diagramController = diagramController;
        m_xContext = xContext;
        removeShapeEvents = new ArrayList();

        m_xNewBroadcaster = UnoRuntime.queryInterface(XDocumentEventBroadcaster.class, xDoc);
        m_xOldBroadcaster = UnoRuntime.queryInterface(XEventBroadcaster.class, xDoc);
        maListener = new DocumentListener(maConnectorsMap);

        if (m_xOldBroadcaster != null) {
            m_xOldBroadcaster.addEventListener(maListener);
        }

        if (m_xNewBroadcaster != null){
            m_xNewBroadcaster.addDocumentEventListener(maListener);
        }

        shapeEventMap = new HashMap<ShapeEventType, Set<ShapeEventListener>>();
    }

    public DocumentListener getDocumentListener() {
        return maListener;
    }

    public class DocumentListener implements
            XDocumentEventListener,
            XEventListener {


        private final Map<XConnectorShape, ConnectedShapes> maConnectorsMap;
        private int passNextShapeRemovedEvents;

        public void setPassNextShapeRemovedEvents(int passNextShapeRemovedEvents) {
            OOGraph.LOGGER.info("setPassNextShapeRemovedEvents(" + passNextShapeRemovedEvents + ")");
            this.passNextShapeRemovedEvents = passNextShapeRemovedEvents;
        }

        public int getPassNextShapeRemovedEvents() {

            return passNextShapeRemovedEvents;
        }

        public DocumentListener(Map<XConnectorShape, ConnectedShapes> maConnectorsMap) {
            this.maConnectorsMap = maConnectorsMap;
        }


        public void notifyEvent(com.sun.star.document.EventObject aEvent) {
            System.out.printf("OLD Event - Event: %s", aEvent.EventName);
            XServiceInfo xInfo = UnoRuntime.queryInterface(XServiceInfo.class, aEvent.Source);
            if (xInfo != null) {
                System.out.printf(" - %s", xInfo.getImplementationName());
            }
            System.out.println();
            if (aEvent.EventName.equals("ShapeInserted")) {

                if (!muteInsert) {
                    XConnectorShape xConnectorShape = UnoRuntime.queryInterface(XConnectorShape.class, aEvent.Source);

                    XShape xShape = UnoRuntime.queryInterface(XShape.class, aEvent.Source);

                    if (xConnectorShape != null) {
                        if (!maConnectorsMap.containsKey(xConnectorShape)) {
                            //TODO DI
                            ConnectedShapes value = new ConnectedShapes(xConnectorShape, new UnoRuntimeWrapperImpl(), new ShapeHelperWrapperImpl());
                            maConnectorsMap.put(xConnectorShape, value);
                            System.out.println("Inserted connector shape");
                            fireEvent(new ShapeInsertedEvent(xConnectorShape, value));
                        }
                    } else {
                        fireEvent(new ShapeInsertedEvent(xShape));
                    }
                }

            } else if (aEvent.EventName.equals("ShapeRemoved")) {
                if (!muteRemove) {
                    XShape xShape = QI.XShape(aEvent.Source);
                    XConnectorShape xConnectorShape = UnoRuntime.queryInterface(XConnectorShape.class, aEvent.Source);
                    if (xShape != null) {

                        boolean connector = xConnectorShape != null;
                        if (connector) {
                            diagramController.getDiagramModel().getConnectedShapes().remove(xConnectorShape);
                        }
                    }

                    fireEvent(new ShapeRemovedEvent(xShape));
                    if (1 == 1) return;
                    //TODO!!


                    if (passNextShapeRemovedEvents > 0) {
                        OOGraph.LOGGER.info("passNextShapeRemovedEvents > 0 pass");
                        setPassNextShapeRemovedEvents(getPassNextShapeRemovedEvents() - 1);

                    } else {

                        OOGraph.LOGGER.info("shape removed event handling");

                        if (diagramController.getDiagramModel().diagramElementWithShapeExists(xShape)) {


                            boolean text = xShape.getShapeType().contains("Text");
                            boolean connector = !text && xConnectorShape != null;
                            if (connector || text) {

                                if (connector) {

                                    diagramController.getDiagramModel().getConnectedShapes().remove(xConnectorShape);

                                    if (maConnectorsMap.containsKey(xConnectorShape)) {
                                        maConnectorsMap.remove(xConnectorShape);
                                        System.out.println("Removed connector shape");
                                        DiagramElement diagramElement = diagramController.getDiagramModel().getShapeToDiagramElementMap().get(xShape);
                                        Link link = (Link) diagramElement;

                                        fireEvent(new ShapeRemovedEvent(xShape));
                                    }
                                } else {

                                    // text
                                    OOGraph.LOGGER.info("removed text shape");
//                            setPassNextShapeRemovedEvents(3);
                                    DiagramElement diagramElement = diagramController.getDiagramModel().getShapeToDiagramElementMap().get(xShape);
                                    LinkTwoConnectorsAndTextBase link = (LinkTwoConnectorsAndTextBase) diagramElement;
//                            fireEvent(new ShapeRemovedEvent(link.getTextShape()));
                                    fireEvent(new ShapeRemovedEvent(link.getTextShape()));
//                            fireEvent(new ShapeRemovedEvent(link.getConnShape2()));
                                    //fireEvent(new ShapeRemovedEvent(xShape));
                                }
                            }
                        }
                    }
                }
            } else if (aEvent.EventName.equals("ShapeModified")) {
                if (!muteModify) {
                    XConnectorShape xConnectorShape = UnoRuntime.queryInterface(XConnectorShape.class, aEvent.Source);
                    if (xConnectorShape != null) {
                        if (maConnectorsMap.containsKey(xConnectorShape)) {
                            ConnectedShapes aShapes = maConnectorsMap.get(xConnectorShape);
                            if (aShapes.hasChanged()) {
                                System.out.println("Connected shapes have changed");
//                            aShapes.update();
                                fireEvent(new ConnectedShapesChanged(xConnectorShape, aShapes));
                            }
                        }
//                    else {
//                        maConnectorsMap.put(xConnectorShape, new ConnectedShapes(xConnectorShape));
//                    }
                    } else {
                        XShape xShape = QI.XShape(aEvent.Source);
                        if (MiscHelper.isNode(xShape)) {

                        }

                    }
                    fireEvent(new ShapeModifiedEvent(QI.XShape(aEvent.Source)));
                    diagramController.getDiagramModel().getDiagramElementByShape(QI.XShape(aEvent.Source)).setId(QI.XText(aEvent.Source).getString());
                    QI.XNamed(aEvent.Source).setName(QI.XText(aEvent.Source).getString());
                }
            }
        }

        public void disposing(EventObject arg0) {
            System.out.println("disposing...");
        }

        @Override
        public void documentEventOccured(DocumentEvent documentEvent) {
            System.out.println(documentEvent.toString());
        }
    }

    Map<ShapeEventType, Set<ShapeEventListener>> shapeEventMap;

    public void addShapeModifiedListener(ShapeModifiedListener shapeModifiedListener) {

        addShapeEventListener(shapeModifiedListener, ShapeEventType.ShapeModified);
    }

    public void addShapeInsertedListener(ShapeInsertedListener shapeInsertedListener) {
        addShapeEventListener(shapeInsertedListener, ShapeEventType.ShapeInserted);
    }

    private void addShapeEventListener(ShapeEventListener shapeEventListener, ShapeEventType eventType) {
        if (shapeEventMap.containsKey(eventType)) {

        } else {
            shapeEventMap.put(eventType, new HashSet<ShapeEventListener>());
        }

        shapeEventMap.get(eventType).add(shapeEventListener);
    }

    public void addShapeEventListener(ShapeEventListener shapeEventListener) {
        addShapeEventListener(shapeEventListener, shapeEventListener.getEventType());
    }

    public void fireEvent(ShapeEvent shapeEvent) {

        Set<ShapeEventListener> shapeEventListeners = shapeEventMap.get(shapeEvent.getShapeEventType());
        if (shapeEventListeners != null) {
            for (ShapeEventListener shapeEventListener : shapeEventListeners) {
                shapeEventListener.onShapeEvent(shapeEvent);
            }
        }
    }


}
