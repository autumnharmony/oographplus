
package ru.ssau.graphplus.link;

import com.google.inject.Inject;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.XMultiServiceFactory;
import ru.ssau.graphplus.AbstractDiagramElementFactory;
import ru.ssau.graphplus.Settings;
import ru.ssau.graphplus.commons.ConnectedShapesComplex;
import ru.ssau.graphplus.commons.MiscHelper;
import ru.ssau.graphplus.api.Link;
import ru.ssau.graphplus.commons.QI;
import ru.ssau.graphplus.codegen.impl.recognition.LinkTypeRecogniser;
import ru.ssau.graphplus.codegen.impl.recognition.LinkTypeRecogniserImpl;

import java.util.HashMap;

/**
 * @author anton
 */
public class LinkFactory extends AbstractDiagramElementFactory {


    private static final String LINK_PREFIX = "link";


    @Inject
    public LinkFactory(XMultiServiceFactory xmsf) {
        super(xmsf);

    }

    public enum LinkConnectors {
        OneConnectorShape,
        TwoConnectorsShape
    }

    public Link create(Link.LinkType type, LinkConnectors linkConnectors) {
        Link link = null;

        if (linkConnectors.equals(LinkConnectors.OneConnectorShape)) {
            switch (type) {
                case ControlFlow:

                    link = new ControlLink.ControlLinkOneConnector(xmsf, LINK_PREFIX + getCount());

                    break;
                case MixedFlow:
                    link = new MixedLink(xmsf, LINK_PREFIX + getCount());
                    break;
                case DataFlow:
                    link = new DataLink.DataLinkOneConnector(xmsf, LINK_PREFIX + getCount());
                    break;
            }
        } else if (linkConnectors.equals(LinkConnectors.TwoConnectorsShape)) {

            switch (type) {
                case ControlFlow:

                    link = new ControlLink.ControlLinkTwoConnectorsAndText(xmsf, LINK_PREFIX + getCount());

                    break;
                case MixedFlow:
                    link = new MixedLink(xmsf, LINK_PREFIX + getCount());
                    break;
                case DataFlow:
                    link = new DataLink.DataLinkTwoConnectors(xmsf, LINK_PREFIX + getCount());
                    break;
            }


        }


        return link;
    }


    public Link create(Link.LinkType type) {
        Link link = null;
        if (Settings.getSettings().isAddTextToShapeToLink()) {
            switch (type) {
                case ControlFlow:


                    link = new ControlLink.ControlLinkTwoConnectorsAndText(xmsf, LINK_PREFIX + getCount());


                    break;
                case MixedFlow:
                    link = new MixedLink(xmsf, LINK_PREFIX + getCount());
                    break;
                case DataFlow:
                    link = new DataLink.DataLinkTwoConnectors(xmsf, LINK_PREFIX + getCount());
                    break;
            }

        } else {
            switch (type) {
                case ControlFlow:


                    link = new ControlLink.ControlLinkTwoConnectorsAndText(xmsf, LINK_PREFIX + getCount());


                    break;
                case MixedFlow:
                    link = new MixedLink(xmsf, LINK_PREFIX + getCount());
                    break;
                case DataFlow:
                    link = new DataLink.DataLinkOneConnector(xmsf, LINK_PREFIX + getCount());
                    break;
            }
        }


        return link;
    }


    public static void setId(XShape shape, Link link) {
        if (link instanceof LinkTwoConnectorsAndTextBase) {
            LinkTwoConnectorsAndTextBase link_ = (LinkTwoConnectorsAndTextBase) link;
            if (link_.getConnShape1().equals(shape)) {
                MiscHelper.setId(shape, link.getId() + "/conn1");
            }
            if (link_.getConnShape2().equals(shape)) {
                MiscHelper.setId(shape, link.getId() + "/conn2");
            }
            if (link_.getTextShape().equals(shape)) {
                MiscHelper.setId(shape, link.getId() + "/text");
            }
        }
    }


    public Link create(ConnectedShapesComplex connectedShapesComplex) {
        LinkTypeRecogniser linkTypeRecogniser = new LinkTypeRecogniserImpl();
        Link.LinkType type = linkTypeRecogniser.getType(connectedShapesComplex.connector1 != null ? connectedShapesComplex.connector1 : connectedShapesComplex.connector, connectedShapesComplex.textShape, connectedShapesComplex.connector2);
        HashMap map = new HashMap();
        LinkConnectors linkConnectors =Settings.getSettings().isAddTextToShapeToLink() ? LinkConnectors.TwoConnectorsShape : LinkConnectors.OneConnectorShape;

        Link link = create(type, linkConnectors);

        if (connectedShapesComplex.connector != null) {
            link.setName(QI.XText(connectedShapesComplex.connector).getString());
        } else {
            link.setName(QI.XText(connectedShapesComplex.textShape).getString());
        }

        return link;
    }


    public static void setId(Link link, XShape... shapes) {

        if (link instanceof LinkTwoConnectorsAndTextBase) {
            LinkTwoConnectorsAndTextBase link_ = (LinkTwoConnectorsAndTextBase) link;

            for (XShape xShape : shapes) {
                if (link_.getConnShape1().equals(xShape)) {
                    MiscHelper.setId(xShape, link.getId() + "/conn1");
                }

                if (link_.getConnShape2().equals(xShape)) {
                    MiscHelper.setId(xShape, link.getId() + "/conn2");
                }


                if (link_.getTextShape().equals(xShape)) {
                    MiscHelper.setId(xShape, link.getId() + "/text");
                }


            }
        }

    }


}
