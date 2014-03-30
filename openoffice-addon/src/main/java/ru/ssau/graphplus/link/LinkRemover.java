package ru.ssau.graphplus.link;

import com.sun.star.beans.NamedValue;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XEventListener;
import com.sun.star.task.XAsyncJob;
import com.sun.star.task.XJobListener;
import ru.ssau.graphplus.OOGraph;

import java.util.logging.Logger;

/**
 * User: anton
 * Date: 3/29/13
 * Time: 1:15 AM
 */
public class LinkRemover implements XComponent, XAsyncJob {
    @Override
    public void executeAsync(NamedValue[] namedValues, XJobListener xJobListener) throws com.sun.star.lang.IllegalArgumentException {
        OOGraph.LOGGER.info("LinkRemover executeAsync");
    }

    @Override
    public void dispose() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addEventListener(XEventListener xEventListener) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void removeEventListener(XEventListener xEventListener) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
