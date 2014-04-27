
package ru.ssau.graphplus.link;

import com.google.inject.Inject;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;
import ru.ssau.graphplus.AbstractDiagramElementFactory;
import ru.ssau.graphplus.commons.ConnectedShapesComplex;
import ru.ssau.graphplus.commons.MiscHelper;
import ru.ssau.graphplus.api.Link;
import ru.ssau.graphplus.recognition.LinkTypeRecogniser;
import ru.ssau.graphplus.recognition.LinkTypeRecogniserImpl;

/**
 * @author anton
 */
public class LinkFactory extends AbstractDiagramElementFactory {


    private static final String LINK_PREFIX = "link";



    @Inject
    public LinkFactory(XMultiServiceFactory xmsf) {
        super(xmsf);

    }

    public Link create(Link.LinkType type) {
        LinkBase link = null;
        switch (type) {
            case ControlFlow:
                link = new ControlLink(xmsf, LINK_PREFIX + getCount());
                break;
            case MixedFlow:
                link = new MixedLink(xmsf, LINK_PREFIX + getCount());
                break;
            case DataFlow:
                link = new DataLink(xmsf, LINK_PREFIX + getCount());
                break;
        }


        return link;
    }


    public static void setId(XShape shape, Link link) {
        if (link instanceof LinkBase) {
            LinkBase link_ = (LinkBase) link;
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



    public Link create(ConnectedShapesComplex connectedShapesComplex){
        LinkTypeRecogniser linkTypeRecogniser = new LinkTypeRecogniserImpl();
        Link.LinkType type = linkTypeRecogniser.getType(connectedShapesComplex.connector1 != null ? connectedShapesComplex.connector1 : connectedShapesComplex.connector, connectedShapesComplex.textShape, connectedShapesComplex.connector2);
        return create(type);
    }



    public static void setId(Link link, XShape ... shapes) {

        if (link instanceof LinkBase) {
            LinkBase link_ = (LinkBase) link;

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
