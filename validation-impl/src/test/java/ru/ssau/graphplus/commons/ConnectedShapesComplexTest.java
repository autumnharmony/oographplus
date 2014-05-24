/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.commons;

import com.google.common.collect.ImmutableMap;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.drawing.XConnectorShape;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.WrappedTargetException;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.matchers.TypeSafeMatcher;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static ru.ssau.graphplus.commons.ConnectedShapesComplexTest.ConnectedShapesComplexMatcher.oneConnector;
import static ru.ssau.graphplus.commons.ConnectedShapesComplexTest.ConnectedShapesComplexMatcher.twoConnectors;

/**
 */
public class ConnectedShapesComplexTest {

    private UnoRuntimeWrapper unoRuntime;

    @Before
    public void setUp() throws Exception {
        unoRuntime = mock(UnoRuntimeWrapper.class);
    }

    @Test
    public void testOneConnectorStaysSame() throws Exception {
        XShape from = createMock(ImmutableMap.<String, Object>builder().put("End", "ASD").build());
        XShape to = createMock(ImmutableMap.<String, Object>builder().put("QWE", "QWE").build());
        XConnectorShape connector = createConnector(ImmutableMap.<String, Object>builder().put("QWE", "ASD").build());
        ConnectedShapesComplex connectedShapesComplex = new ConnectedShapesComplex(
                from,
                to,
                connector
        );

        connectedShapesComplex.normalize();

        assertThat(connectedShapesComplex, oneConnector(from, to, connector));

    }

    @Test
    public void testTwoConnectorInvertedAfterNormalize() throws Exception {
        XShape from = createMock(ImmutableMap.<String, Object>builder().put("End", "ASD").build());
        XShape to = createMock(ImmutableMap.<String, Object>builder().put("QWE", "QWE").build());
        XShape text = createMock(ImmutableMap.<String, Object>builder().put("QWE", "QWE").build());
        XConnectorShape connector1 = createConnector(ImmutableMap.<String, Object>builder().put("LineEndName", "Arrow").build());
        XConnectorShape connector2 = createConnector(ImmutableMap.<String, Object>builder().put("LineEndName", "Circle").build());
        ConnectedShapesComplex connectedShapesComplex = new ConnectedShapesComplex(
                from,
                to,
                connector1, connector2, text
        );

        connectedShapesComplex.normalize();

        assertThat(connectedShapesComplex, twoConnectors(to, from, connector2, connector1, text));
    }

    static class ConnectedShapesComplexMatcher extends TypeSafeMatcher<ConnectedShapesComplex> {


        private XShape from;
        private XShape to;
        private XConnectorShape connector;

        private XShape text;
        private XConnectorShape connector1;
        private XConnectorShape connector2;


        private ConnectedShapesComplexMatcher(XShape from, XShape to, XConnectorShape connector) {
            this.from = from;
            this.to = to;
            this.connector = connector;
        }

        private ConnectedShapesComplexMatcher(XShape from, XShape to, XShape text, XConnectorShape connector1, XConnectorShape connector2) {
            this.from = from;
            this.to = to;
            this.text = text;
            this.connector1 = connector1;
            this.connector2 = connector2;
        }

        static ConnectedShapesComplexMatcher oneConnector(XShape from, XShape to, XConnectorShape connector) {
            return new ConnectedShapesComplexMatcher(from, to, connector);
        }

        static ConnectedShapesComplexMatcher twoConnectors(XShape from, XShape to, XConnectorShape connector1, XConnectorShape connector2, XShape text) {
            return new ConnectedShapesComplexMatcher(from, to, text, connector1, connector2);
        }


        private boolean fromToMatches(ConnectedShapesComplex connectedShapesComplex) {
            return connectedShapesComplex.fromShape.equals(from) && connectedShapesComplex.toShape.equals(to);
        }

        @Override
        public boolean matchesSafely(ConnectedShapesComplex item) {
            if (connector == null && text != null) {
                return (item.connector1.equals(connector1) && item.connector2.equals(connector2) && item.textShape.equals(text) && fromToMatches(item));
            } else {
                return (item.connector.equals(connector) && fromToMatches(item));
            }
        }

        @Override
        public void describeTo(Description description) {

        }
    }


    XShape createMock(Map<String, Object> xps) {
        XShape xShape = mock(XShape.class);
        UnoRuntimeWrapper unoRuntime = mock(UnoRuntimeWrapper.class);
        QI.setUnoRuntimeWrapper(unoRuntime);
        xPS(xps, xShape);
        return xShape;
    }


    XConnectorShape createConnector(Map<String, Object> xps) {
        XConnectorShape xShape = mock(XConnectorShape.class);

        QI.setUnoRuntimeWrapper(unoRuntime);
        xPS(xps, xShape);
        return xShape;
    }

    private void xPS(Map<String, Object> xps, XShape xShape) {
        XPropertySet xPropertySet = mock(XPropertySet.class);
        when(unoRuntime.queryInterface(XPropertySet.class, xShape)).thenReturn(xPropertySet);
        for (Map.Entry<String, Object> entry : xps.entrySet()) {
            try {
                when(xPropertySet.getPropertyValue(entry.getKey())).thenReturn(entry.getValue());
            } catch (UnknownPropertyException | WrappedTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
