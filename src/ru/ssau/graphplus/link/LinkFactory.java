/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ssau.graphplus.link;

import com.sun.star.beans.*;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.WrappedTargetException;
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

        try {

            link.xPS1.addVetoableChangeListener("StartShape", new XVetoableChangeListener() {
                @Override
                public void vetoableChange(PropertyChangeEvent propertyChangeEvent) throws PropertyVetoException {
                    System.out.println(propertyChangeEvent.OldValue);
                    System.out.println(propertyChangeEvent.NewValue);
                }

                @Override
                public void disposing(EventObject eventObject) {

                }
            });

            link.xPS1.addPropertyChangeListener("", new XPropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                    System.out.println("OMGOMGOMG");
                }

                @Override
                public void disposing(EventObject eventObject) {
                    //To change body of implemented methods use File | Settings | File Templates.
                }
            });


            link.xPS2.addPropertyChangeListener("", new XPropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                    System.out.println("OMGOMGOMG");
                }

                @Override
                public void disposing(EventObject eventObject) {
                    //To change body of implemented methods use File | Settings | File Templates.
                }
            });
        } catch (UnknownPropertyException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (WrappedTargetException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        try {
            link.xPS2.addPropertyChangeListener("EndShape", new XPropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                    System.out.println("OMGOMGOMG");
                }

                @Override
                public void disposing(EventObject eventObject) {
                    //To change body of implemented methods use File | Settings | File Templates.
                }
            });
        } catch (UnknownPropertyException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (WrappedTargetException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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
