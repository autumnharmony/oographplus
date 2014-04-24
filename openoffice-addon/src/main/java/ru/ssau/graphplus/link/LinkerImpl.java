package ru.ssau.graphplus.link;

import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.WrappedTargetException;
import ru.ssau.graphplus.commons.QI;
import ru.ssau.graphplus.api.Link;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: anton
 * Date: 3/3/13
 * Time: 4:49 AM
 */
public class LinkerImpl implements Linker {

    private final LinkBase linkReplace;
    //private final XShape newConnector;
    private XPropertySet xPS;


    public LinkerImpl(Link linkReplace, XShape newConnector) {
        this.linkReplace = (LinkBase) linkReplace;
    }

    @Override
    public void link(XShape sh1, XShape sh2) {
        try {


            QI.XPropertySet(linkReplace.getConnShape1()).setPropertyValue("StartShape", sh1);

            QI.XPropertySet(linkReplace.getConnShape2()).setPropertyValue("EndShape", sh2);

        } catch (UnknownPropertyException ex) {
            Logger.getLogger(ControlLink.class.getName()).log(Level.SEVERE, null, ex);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(ControlLink.class.getName()).log(Level.SEVERE, null, ex);
        } catch (com.sun.star.lang.IllegalArgumentException ex) {
            Logger.getLogger(ControlLink.class.getName()).log(Level.SEVERE, null, ex);
        } catch (WrappedTargetException ex) {
            Logger.getLogger(ControlLink.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void adjustLink(XShape sh1, XShape sh2) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public XShape getTextShape() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


}
