
package ru.ssau.graphplus.link;

import com.sun.star.drawing.XDrawPage;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;
import ru.ssau.graphplus.DiagramElementFactory;
import ru.ssau.graphplus.MiscHelper;
import ru.ssau.graphplus.api.Link;

/**
 * @author anton
 */
public class LinkFactory extends DiagramElementFactory{


    private static final String LINK_PREFIX = "link";



    public LinkFactory(XMultiServiceFactory xmsf) {
        super(xmsf);

    }

    public Link create(Link.LinkType type, XComponent xComp) {
        LinkBase link = null;
        switch (type) {
            case Control:
                link = new ControlLink(xmsf,  xComp, LINK_PREFIX + getCount());
                break;
            case Link:
                link = new LinkLink(xmsf,  xComp, LINK_PREFIX + getCount());
                break;
            case Message:
                link = new MessageLink(xmsf, xComp, LINK_PREFIX + getCount());
                break;
        }


        return link;
    }

    /**
     *
     * @param type
     * @return  Link object w/o shapes
     */
    public Link createPrototype(Link.LinkType type){
        LinkBase link = null;
        switch (type) {
            case Control:
                link = new ControlLink();
                break;
            case Link:
                link = new LinkLink();
                break;
            case Message:
                link = new MessageLink();
                break;
        }
        return link;
    }


    public Link create(Link.LinkType type, XComponent xComp, XDrawPage xDP, XShape xShape1, XShape xShape2, Boolean separatedTextShape) {
        return create(type, xComp);
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
