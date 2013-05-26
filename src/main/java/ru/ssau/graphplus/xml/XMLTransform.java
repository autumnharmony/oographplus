package ru.ssau.graphplus.xml;

import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;
import ru.ssau.graphplus.Constants;
import ru.ssau.graphplus.DiagramType;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


/**
 * User: anton
 * Date: 5/11/13
 * Time: 9:18 PM
 */
public class XMLTransform {


    public String transform(String xml, String transformFileName) {
        try {

            URL resource = getClass().getResource(transformFileName);
            InputStream resourceAsStream = getClass().getResourceAsStream(transformFileName);
            String s = resource.toExternalForm();
            File fileObject = new File(transformFileName);
            String systemID = resource.toExternalForm();

            Source xslSrc = new StreamSource(resourceAsStream);
            xslSrc.setSystemId(systemID);


            DocumentBuilderFactory factory = new DocumentBuilderFactoryImpl();
            DocumentBuilder builder =
                    factory.newDocumentBuilder();

            TransformerFactory transformerFactory =
                    javax.xml.transform.TransformerFactory.newInstance();
            Transformer transformer =
                    transformerFactory.newTransformer(xslSrc);
            StringWriter writer = new StringWriter();
            javax.xml.transform.Result result =
                    new javax.xml.transform.stream.StreamResult(writer);

            Source source = new StreamSource(new StringReader(xml));
            transformer.transform(source,result);
            return writer.getBuffer().toString();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return "";
    }

    Map<DiagramType, String> typeToTransformFileName = new HashMap<DiagramType, String>() {
        {
            put(DiagramType.Protocol, Constants.XSLT_PATH + "protocol.xsl");
            put(DiagramType.Processes, Constants.XSLT_PATH + "processes.xsl");
            put(DiagramType.Composition, Constants.XSLT_PATH + "composition.xsl");
        }
    };

    public String transform(String xml, DiagramType type) {
        String result = "";
        try {

            String s = typeToTransformFileName.get(type);


            String transform = transform(xml, s);
            result = transform;
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {

            return result;
        }
    }


    private String readFile(String file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = null;
        StringBuilder stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");

        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append(ls);
        }

        return stringBuilder.toString();
    }

    public XMLTransform() {
        Logger.getAnonymousLogger().info("XMLTransform");
    }
}
