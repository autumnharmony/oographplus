/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus;

import com.sun.star.awt.Point;
import com.sun.star.awt.Size;
import com.sun.star.document.XEventBroadcaster;
import com.sun.star.document.XEventListener;
import com.sun.star.drawing.XDrawPage;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.XComponent;
import com.sun.star.table.XTable;
import com.sun.star.table.XTableColumns;
import com.sun.star.table.XTableRows;
import com.sun.star.uno.*;
import com.sun.star.util.XModifiable;
import com.sun.star.util.XModifyListener;

import java.util.Map;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: anton
 * Date: 2/8/14
 * Time: 1:08 AM
 * To change this template use File | Settings | File Templates.
 */
public interface TableInserter {
    void insertTable(XComponent xDrawDoc);
}
