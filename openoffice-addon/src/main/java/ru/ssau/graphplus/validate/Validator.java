package ru.ssau.graphplus.validate;


import org.apache.xerces.impl.xs.XMLSchemaLoader;
import org.apache.xerces.jaxp.validation.XMLSchemaFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import ru.ssau.graphplus.api.DiagramType;

import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import java.io.IOException;
import java.io.StringReader;

/**
 * User: anton
 * Date: 5/11/13
 * Time: 9:27 PM
 */
public class Validator {

    String protocol = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\">\n" +
            "    <xs:element name=\"channel\">\n" +
            "        <xs:complexType>\n" +
            "            <xs:sequence>\n" +
            "                <xs:element maxOccurs=\"unbounded\" ref=\"state\"/>\n" +
            "            </xs:sequence>\n" +
            "            <xs:attribute name=\"entry\" use=\"required\" type=\"xs:NCName\"/>\n" +
            "            <xs:attribute name=\"id\" use=\"required\" type=\"xs:NCName\"/>\n" +
            "        </xs:complexType>\n" +
            "\n" +
            "        <xs:key name=\"stateKey\">\n" +
            "            <xs:selector xpath=\"state\"></xs:selector>\n" +
            "            <xs:field xpath=\"@id\"></xs:field>\n" +
            "        </xs:key>\n" +
            "        \n" +
            "        <xs:keyref name=\"messageState\" refer=\"stateKey\">\n" +
            "            <xs:selector xpath=\"state/message\"></xs:selector>\n" +
            "            <xs:field xpath=\"@state\"></xs:field>\n" +
            "        </xs:keyref>\n" +
            "\n" +
            "        <xs:keyref name=\"channelEntry\" refer=\"stateKey\">\n" +
            "            <xs:selector xpath=\".\"></xs:selector>\n" +
            "            <xs:field xpath=\"@entry\"></xs:field>\n" +
            "        </xs:keyref>\n" +
            "\n" +
            "\n" +
            "    </xs:element>\n" +
            "\n" +
            "    <xs:element name=\"state\">\n" +
            "        <xs:complexType>\n" +
            "            <xs:sequence>\n" +
            "                <xs:element minOccurs=\"0\" ref=\"message\"/>\n" +
            "            </xs:sequence>\n" +
            "            <xs:attribute name=\"id\" use=\"required\" type=\"xs:NCName\"/>\n" +
            "            <xs:attribute name=\"type\" use=\"required\" type=\"xs:NCName\"/>\n" +
            "        </xs:complexType>\n" +
            "\n" +
            "    </xs:element>\n" +
            "\n" +
            "\n" +
            "    <xs:element name=\"message\">\n" +
            "        <xs:complexType>\n" +
            "            <xs:attribute name=\"id\" use=\"required\" type=\"xs:NCName\"/>\n" +
            "            <xs:attribute name=\"state\" use=\"required\" type=\"xs:NCName\"/>\n" +
            "        </xs:complexType>\n" +
            "    </xs:element>\n" +
            "</xs:schema>\n";

    public boolean isValid(DiagramType type, String xml) {
        XMLSchemaLoader xmlSchemaLoader = new XMLSchemaLoader();
        XMLSchemaFactory xmlSchemaFactory = new XMLSchemaFactory();
        String filename = null;
        switch (type) {
//            case Composition:
//                break;
            case Process:
                filename = "processes.xsd";
                break;
            case Channel:
                filename = "protocol.xsd";
                break;

        }

        try {
//            InputStream resourceAsStream = this.getClass().getResourceAsStream(filename);
            Schema schema = xmlSchemaFactory.newSchema(new StreamSource(new StringReader(protocol)));
            javax.xml.validation.Validator validator = schema.newValidator();
            try {
                validator.validate(new SAXSource(new InputSource(new StringReader(xml))));

            } catch (IOException e) {
                e.printStackTrace();
            }


        } catch (SAXException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

}
