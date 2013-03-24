/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ssau.graphplus;

import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XNamed;
import com.sun.star.drawing.XConnectorShape;
import com.sun.star.drawing.XDrawPage;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.uno.UnoRuntime;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
    public static void generateXMLforDocument(XComponent xDrawDoc, XDrawPage xDrawPage, String path){

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


                    if (!Misc.isNode(xSh)){

                    }
                else {

                    Element n = doc.createElement(NODE);

                    if (xN != null) {
                        n.setAttribute("id", xN.getName());
                        n.setIdAttribute("id", true);
                    }
                    root.appendChild(n);
                }

                    if (!Misc.isLink(xSh)){
                        continue;
                    }
                else {

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
        } catch (Exception ex){
            Logger.getLogger(OOGraph.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
        }

    }
}
