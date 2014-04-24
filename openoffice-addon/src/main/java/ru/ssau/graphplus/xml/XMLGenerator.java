
package ru.ssau.graphplus.xml;

import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XNamed;
import com.sun.star.drawing.XConnectorShape;
import com.sun.star.drawing.XDrawPage;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.uno.UnoRuntime;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import ru.ssau.graphplus.*;
import ru.ssau.graphplus.api.DiagramElement;
import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.commons.ConnectedShapes;
import ru.ssau.graphplus.commons.MiscHelper;
import ru.ssau.graphplus.commons.QI;
import ru.ssau.graphplus.link.LinkBase;
import ru.ssau.graphplus.node.NodeBase;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.io.*;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class XMLGenerator {

    private static final String DEFAULT_PATH = "/home/anton/doc.xml";
    private static final String GRAPH = "graph";
    private static final String LINK = "link";
    private static final String NODE = "node";

    private com.sun.star.lang.XComponent xDrawDoc;
    private com.sun.star.drawing.XDrawPage xDrawPage;


    public XMLGenerator(XComponent xDrawDoc, XDrawPage xDrawPage) {
        this.xDrawDoc = xDrawDoc;
        this.xDrawPage = xDrawPage;
    }

    public void generateXML() {
        generateXML(DEFAULT_PATH);
    }

    public void generateXML(String path) {
        generateXMLforDocument(xDrawDoc, xDrawPage, path);

    }

    public static void generateXMLforDocument(XComponent xDrawDoc, XDrawPage xDrawPage, String path) {

        try {
            DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
            Document doc = docBuilder.newDocument();

            Element root = doc.createElement(GRAPH);
            doc.appendChild(root);
            com.sun.star.drawing.XDrawPagesSupplier xDPS =
                    (com.sun.star.drawing.XDrawPagesSupplier) UnoRuntime.queryInterface(
                            com.sun.star.drawing.XDrawPagesSupplier.class, xDrawDoc);
            com.sun.star.drawing.XDrawPages xDPn = xDPS.getDrawPages();
            com.sun.star.container.XIndexAccess xDPi =
                    (com.sun.star.container.XIndexAccess) UnoRuntime.queryInterface(
                            com.sun.star.container.XIndexAccess.class, xDPn);
            xDrawPage = (com.sun.star.drawing.XDrawPage) UnoRuntime.queryInterface(
                    com.sun.star.drawing.XDrawPage.class, xDPi.getByIndex(0));


            for (int i = 0; i < xDrawPage.getCount(); i++) {
                System.out.println(i);
                XShape xSh = null;
                try {
                    xSh = (XShape) UnoRuntime.queryInterface(XShape.class, xDrawPage.getByIndex(i));
                } catch (com.sun.star.lang.IndexOutOfBoundsException ex) {
                    Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                } catch (WrappedTargetException ex) {
                    Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                }
                XNamed xN = (XNamed) UnoRuntime.queryInterface(XNamed.class, xSh);


                if (!MiscHelper.isNode(xSh)) {

                } else {

                    Element n = doc.createElement(NODE);

                    if (xN != null) {
                        n.setAttribute("id", xN.getName());
                        n.setIdAttribute("id", true);
                    }
                    root.appendChild(n);
                }

                if (!MiscHelper.isLink(xSh)) {
                    continue;
                } else {

                    Element l = doc.createElement(LINK);

                    XConnectorShape xConnSh = (XConnectorShape) UnoRuntime.queryInterface(XConnectorShape.class, xSh);


                    XPropertySet xShapeProps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xConnSh);
                    Object startShape;
                    Object endShape;
                    try {
                        startShape = xShapeProps.getPropertyValue("StartShape");
                        endShape = xShapeProps.getPropertyValue("EndShape");
                        XNamed xNConnSh = (XNamed) UnoRuntime.queryInterface(XNamed.class, xConnSh);
                        XShape xShStart = (XShape) UnoRuntime.queryInterface(XShape.class, startShape);
                        XNamed xNamedStart = (XNamed) UnoRuntime.queryInterface(XNamed.class, startShape);
                        XShape xShEnd = (XShape) UnoRuntime.queryInterface(XShape.class, endShape);
                        XNamed xNamedEnd = (XNamed) UnoRuntime.queryInterface(XNamed.class, endShape);
                        l.setAttribute("id", xNConnSh.getName());
                        l.setIdAttribute("id", true);
                        l.setAttribute("nodeid", xNamedEnd.getName());

                        Element w = doc.getElementById(xNamedStart.getName());
                        w.appendChild(l);

                    } catch (UnknownPropertyException ex) {
                        Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }


            TransformerFactory transfac = TransformerFactory.newInstance();
            Transformer trans = transfac.newTransformer();
            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            trans.setOutputProperty(OutputKeys.INDENT, "yes");


            StringWriter sw = new StringWriter();
            StreamResult result = new StreamResult(sw);
            DOMSource source = new DOMSource(doc);
            trans.transform(source, result);
            String xmlString = sw.toString();

            OutputStream outputStream = null;

            File f = null;
            FileWriter fw = null;


            f = new File(path);

            if (!f.exists()) {
                try {
                    f.createNewFile();


                } catch (IOException ex) {
                    Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
                }


            }
            try {
                outputStream = new FileOutputStream(f);
                System.out.println(f.getCanonicalPath());
                fw = new FileWriter(f);
                fw.append(xmlString);

            } catch (IOException ex) {
                Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    fw.close();
                } catch (java.lang.Exception ignore) {
                }


            }


        } catch (com.sun.star.lang.IndexOutOfBoundsException ex) {
            Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
        } catch (WrappedTargetException ex) {
            Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
        } finally {

        }

    }


    public static String generateXMLStringByDiagramModel(DiagramModel diagramModel) throws Exception {
        Collection<DiagramElement> diagramElements = diagramModel.getDiagramElements();


        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        Element root = doc.createElement(GRAPH);
        root.setAttribute("id", diagramModel.getName());
        doc.appendChild(root);

        for (DiagramElement diagramElement : diagramElements) {
            String name = diagramElement.getName();
            if (diagramElement instanceof Node) {
                Node node = (Node) diagramElement;
                Element element = doc.createElement("node");
                element.setAttribute("h", String.valueOf(node.getSize().Height));

                element.setAttribute("w", String.valueOf(node.getSize().Width));

                element.setAttribute("x", String.valueOf(node.getPosition().X));
                element.setAttribute("y", String.valueOf(node.getPosition().Y));

                element.setAttribute("id", node.getName());
                element.setAttribute("type", node.getType().toString());
                root.appendChild(element);


            } else if (diagramElement instanceof LinkBase) {
                LinkBase link = (LinkBase) diagramElement;

                Element element = doc.createElement("link");

                element.setAttribute("id", link.getName());


                Map<XConnectorShape, ConnectedShapes> connectedShapes = diagramModel.getConnectedShapes();
                ConnectedShapes connectedShapes1 = connectedShapes.get(QI.XConnectorShape(link.getConnShape1()));
                XShape fromShape = connectedShapes1.getStart();
                ConnectedShapes connectedShapes2 = connectedShapes.get(QI.XConnectorShape(link.getConnShape2()));
                XShape toShape = connectedShapes2.getEnd();
                Map<XShape, DiagramElement> shapeToDiagramElementMap = diagramModel.getShapeToDiagramElementMap();
                DiagramElement fromElement = shapeToDiagramElementMap.get(fromShape);
                DiagramElement toElement = shapeToDiagramElementMap.get(toShape);


                NodeBase fromNode = (NodeBase) fromElement;
                NodeBase toNode = (NodeBase) toElement;
//
                element.setAttribute("from", fromNode.getName());
                element.setAttribute("to", toNode.getName());
                root.appendChild(element);

            }
        }


        TransformerFactory transfac = TransformerFactory.newInstance();
        Transformer trans = transfac.newTransformer();
//        trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        trans.setOutputProperty(OutputKeys.INDENT, "yes");


        StringWriter sw = new StringWriter();
        StreamResult result = new StreamResult(sw);
        DOMSource source = new DOMSource(doc);
        trans.transform(source, result);
        String xmlString = sw.toString();


        return xmlString;

    }

    private static String joinPointsToString(Point[] firstPartPoints) {
        String firstPart = "";
        for (int i = 0; i < firstPartPoints.length; i++) {
            Point point = firstPartPoints[i];
            firstPart += point.x + "," + point.y + "; ";

        }
        return firstPart;
    }
}
