package ru.ssau.graphplus;

import junit.framework.Assert;
import org.junit.Test;
import ru.ssau.graphplus.api.DiagramType;
import ru.ssau.graphplus.validate.Validator;

/**
 * User: anton
 * Date: 5/11/13
 * Time: 10:00 PM
 */
public class ValidatorTest {
    private Validator validator;

    public void setUp() throws Exception {


    }

    public void tearDown() throws Exception {

    }
    @Test
    public void testIsValid() throws Exception {

        validator = new Validator();
        String xml = "<channel id=\"Call\" entry=\"S0\">\n" +
                "<state id=\"S0\" type=\"cli\">\n" +
                "<message id=\"call\" state=\"S1\"/>\n" +
                "</state>\n" +
                "<state id=\"S1\" type=\"srv\">\n" +
                "<message id=\"ret\" state=\"S2\"/>\n" +
                "</state>\n" +
                "<state id=\"S2\" type=\"cli\">\n" +
                "</state>\n" +
                ""+
                "</channel>\n";
        boolean valid = validator.isValid(DiagramType.Channel, xml);
        Assert.assertTrue(valid);

    }

    @Test
    public void testIsValidNotValid() throws Exception {

        validator = new Validator();
        String xml = "<channel id=\"Call\" entry=\"S0\">\n" +
                "<state id=\"S0\" type=\"cli\">\n" +
                "<message id=\"call\" state=\"S1\"/>\n" +
                "</state>\n" +
                "<state id=\"S1\" type=\"srv\">\n" +
                "<message id=\"ret\" state=\"S2\"/>\n" +
                "</state>\n" +
                "<state id=\"S2\" type=\"cli\">\n" +
                "</state>\n" +
                "<omg></omg>"+
                "</channel>\n";
        boolean valid = validator.isValid(DiagramType.Channel, xml);
        Assert.assertFalse(valid);

    }
}
