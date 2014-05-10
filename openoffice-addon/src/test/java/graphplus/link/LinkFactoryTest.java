/*
 * Copyright (c) 2014. Anton Borisov
 */

package java.graphplus.link;

import com.sun.star.lang.XMultiServiceFactory;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.ssau.graphplus.api.Link;
import ru.ssau.graphplus.link.ControlLink;
import ru.ssau.graphplus.link.DataLink;
import ru.ssau.graphplus.link.LinkFactory;
import ru.ssau.graphplus.link.MixedLink;

import static org.mockito.Mockito.mock;

/**
 */
public class LinkFactoryTest {

    private LinkFactory linkFactory;

    @Before
    public void setUp() throws Exception {
        linkFactory = new LinkFactory(mock(XMultiServiceFactory.class));
    }

    @Test
    public void testCreate1() throws Exception {
        Link link = linkFactory.create(Link.LinkType.MixedFlow, LinkFactory.LinkConnectors.OneConnectorShape);
        Assert.assertTrue(link instanceof MixedLink);
    }

    @Test
    public void testCreate2() throws Exception {
        Link link = linkFactory.create(Link.LinkType.ControlFlow, LinkFactory.LinkConnectors.OneConnectorShape);
        Assert.assertTrue(link instanceof ControlLink);
    }



    @Test
    public void testCreate3() throws Exception {
        Link link = linkFactory.create(Link.LinkType.DataFlow, LinkFactory.LinkConnectors.OneConnectorShape);
        Assert.assertTrue(link instanceof DataLink);
    }
}
