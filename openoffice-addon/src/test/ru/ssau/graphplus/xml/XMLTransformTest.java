package ru.ssau.graphplus.xml;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * User: anton
 * Date: 5/11/13
 * Time: 10:30 PM
 */
public class XMLTransformTest {

//    private XMLTransform xmlTransform;

    @Before
    public void setUp() throws Exception {
//        xmlTransform = new XMLTransform();

    }

    @After
    public void tearDown() throws Exception {

    }
    @Ignore
    @Test
    public void testTransform() throws Exception {
//        System.out.println("test");
//
//        String s = "<graph>\n" +
//                "<node id=\"cl\" type=\"ClientPort\"/>\n" +
//                "<node id=\"ser\" type=\"ServerPort\"/>\n" +
//                "<link from=\"cl\" id=\"lnk\" to=\"ser\"/>\n" +
//                "</graph>";
//
//        String ss = "<xsl2:stylesheet version=\"1.0\" xmlns:xsl2=\"http://www.w3.org/1999/XSL/Transform\">\n" +
//                "\n" +
//                "    <xsl2:template match=\"/\">\n" +
//                "        <channel>\n" +
//                "        <xsl2:apply-templates />\n" +
//                "        </channel>\n" +
//                "    </xsl2:template>\n" +
//                "\n" +
//                "    <xsl2:template match=\"node\">\n" +
//                "        <xsl2:element name=\"state\">\n" +
//                "            <xsl2:element name=\"message\">\n" +
//                "                <xsl2:attribute name=\"state\">\n" +
//                "                    <xsl2:value-of select=\"//link[@from = self::node()/@id]/@to\"></xsl2:value-of>\n" +
//                "                </xsl2:attribute>\n" +
//                "            </xsl2:element>\n" +
//                "        </xsl2:element>\n" +
//                "    </xsl2:template>\n" +
//                "\n" +
//                "    <!--<xsl2:template match=\"message\">-->\n" +
//                "        <!--<xsl2:attribute name=\"state\">-->\n" +
//                "            <!--<xsl2:value-of select=\"/link[@from = ancestor::node()/@id]/@to\"/>-->\n" +
//                "        <!--</xsl2:attribute>-->\n" +
//                "    <!--</xsl2:template>-->\n" +
//                "\n" +
//                "\n" +
//                "    <!--<xsl2:template match=\"link\">-->\n" +
//                "\n" +
//                "    <!--</xsl2:template>-->\n" +
//                "\n" +
//                "</xsl2:stylesheet>";
//        String transform = xmlTransform.transform(s, ss);
//        System.out.println(transform);

    }
    @Ignore
    @Test
    public void testName() throws Exception {
        String s = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<graph>\n" +
                "    <node id=\"a\">\n" +
                "        <link id=\"link1\" to=\"b\"/>\n" +
                "    </node>\n" +
                "    <node id=\"b\">\n" +
                "        <link id=\"link2\" to=\"d\"/>\n" +
                "    </node>\n" +
                "    <node id=\"c\"/>\n" +
                "    <node id=\"c\"/>\n" +
                "    <node id=\"d\">\n" +
                "        <link id=\"link3\" to=\"c\"/>\n" +
                "    </node>\n" +
                "</graph>";

        String ss = "<xsl2:stylesheet version=\"2.0\" xmlns:xsl2=\"http://www.w3.org/1999/XSL/Transform\">\n" +
                "\n" +
                "    <xsl2:template match=\"/graph\">\n" +
                "        <xsl2:variable name=\"channel\">\n" +
                "            <xsl2:value-of select=\"substring-after(string(//link/@id), string(':'))\"></xsl2:value-of>\n" +
                "\n" +
                "        </xsl2:variable>\n" +
                "        <assemble>\n" +
                "\n" +
                "            <xsl2:attribute name=\"id\">\n" +
                "                <xsl2:value-of select=\"current()/@id\"></xsl2:value-of>\n" +
                "            </xsl2:attribute>\n" +
                "            <xsl2:apply-templates></xsl2:apply-templates>\n" +
                "            <xsl2:element name=\"channel\">\n" +
                "                <xsl2:attribute name=\"id\">\n" +
                "                    <xsl2:value-of select=\"$channel\"></xsl2:value-of>\n" +
                "                </xsl2:attribute>\n" +
                "            </xsl2:element>\n" +
                "        </assemble>\n" +
                "    </xsl2:template>\n" +
                "    <xsl2:template match=\"node\">\n" +
                "        <process>\n" +
                "            <xsl2:attribute name=\"id\">\n" +
                "                <xsl2:value-of select=\"current()/@id\"></xsl2:value-of>\n" +
                "            </xsl2:attribute>\n" +
                "            <xsl2:apply-templates></xsl2:apply-templates>\n" +
                "        </process>\n" +
                "    </xsl2:template>\n" +
                "\n" +
                "\n" +
                "</xsl2:stylesheet>";
//        String transform = xmlTransform.transform(s, ss);
//        System.out.println(transform);

    }
}

