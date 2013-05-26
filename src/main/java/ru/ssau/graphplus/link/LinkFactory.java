/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ssau.graphplus.link;

import com.sun.star.drawing.XDrawPage;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;
import ru.ssau.graphplus.Misc;
import ru.ssau.graphplus.link.Link.LinkType;

/**
 * @author anton
 */
public class LinkFactory {

    public static final String CONTROL_LINK = "control_link";
    public static final String LINK_LINK = "link_link";
    public static final String MESSAGE_LINK = "message_link";
    private XMultiServiceFactory xmsf;

    private int linkCounter;
    private int controlCounter;
    private int messageCounter;


    public LinkFactory(XMultiServiceFactory xmsf) {
        this.xmsf = xmsf;
        linkCounter = 0;
        controlCounter = 0;
        messageCounter = 0;
    }

    public Link create(LinkType type, XComponent xComp, XDrawPage xDP) {
        Link link = null;
        switch (type) {
            case Control:
                link = new ControlLink(xmsf, xDP, xComp, CONTROL_LINK + controlCounter++);
                break;
            case Link:
                link = new LinkLink(xmsf, xDP, xComp, LINK_LINK + linkCounter++);
                break;
            case Message:
                link = new MessageLink(xmsf, xDP, xComp, MESSAGE_LINK + messageCounter++);
                break;
        }


        return link;
    }

    /**
     *
     * @param type
     * @return  Link object w/o shapes
     */
    public Link createPrototype(LinkType type){
        Link link = null;
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


    public Link create(LinkType type, XComponent xComp, XDrawPage xDP, XShape xShape1, XShape xShape2, Boolean separatedTextShape) {
        return create(type, xComp, xDP);
    }


    public static void setId(XShape shape, Link link) {
        if (link.getConnShape1().equals(shape)) {
            Misc.setId(shape, link.getId() + "/conn1");
        }
        if (link.getConnShape2().equals(shape)) {
            Misc.setId(shape, link.getId() + "/conn2");
        }
        if (link.getTextShape().equals(shape)) {
            Misc.setId(shape, link.getId() + "/text");
        }
    }
}
