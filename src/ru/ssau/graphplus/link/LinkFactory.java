/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ssau.graphplus.link;

import ru.ssau.graphplus.link.Link.LinkType;
import com.sun.star.drawing.XDrawPage;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;

/**
 *
 * @author anton
 */
public class LinkFactory {
    
    private XMultiServiceFactory xmsf;


    public LinkFactory(XMultiServiceFactory xmsf) {
        this.xmsf = xmsf;
    }

    public Link create(LinkType type, XComponent xComp, XDrawPage xDP) {
        Link link = null;
        switch (type) {
            case Control:
                link = new ControlLink(xmsf, xDP, xComp);
                break;
            case Link:
                link = new LinkLink(xmsf, xDP, xComp);
                break;
            case Message:
                link  = new MessageLink(xmsf, xDP, xComp);
                break;
        }

        return link;
    }



    public Link create(LinkType type, XComponent xComp, XDrawPage xDP, XShape xShape1, XShape xShape2, Boolean separatedTextShape) {
        Link link = null;
        switch (type) {
            case Control:
                link = new ControlLink(xmsf, xDP, xComp);
                break;
            case Link:
                link = new LinkLink(xmsf, xDP, xComp);
                break;
            case Message:
                link  = new MessageLink(xmsf, xDP, xComp);
                break;
        }

        return link;
    }
}
