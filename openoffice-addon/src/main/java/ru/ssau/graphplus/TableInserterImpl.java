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
import com.sun.star.lang.*;
import com.sun.star.table.*;
import com.sun.star.uno.*;
import com.sun.star.util.XModifiable;
import com.sun.star.util.XModifyListener;
import ru.ssau.graphplus.commons.OOoUtils;
import ru.ssau.graphplus.commons.QI;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: anton
 * Date: 2/8/14
 * Time: 1:08 AM
 * To change this template use File | Settings | File Templates.
 */
public class TableInserterImpl implements TableInserter {

    final private XMultiServiceFactory xMSF;

    public TableInserterImpl(XMultiServiceFactory xMSF) {
        this.xMSF = xMSF;
    }

    public void insertTable(XComponent xDrawDoc) {
        try {
            Object instance = xMSF.createInstance("com.sun.star.drawing.TableShape");
            XShape oTableShape = QI.XShape(instance);
            XDrawPage oDrawPage = DrawHelper.getCurrentDrawPage(xDrawDoc);


            int borderBottom = OOoUtils.getIntProperty(oDrawPage, "BorderBottom");
            int borderLeft = OOoUtils.getIntProperty(oDrawPage, "BorderLeft");
            int borderRight = OOoUtils.getIntProperty(oDrawPage, "BorderRight");
            int width = OOoUtils.getIntProperty(oDrawPage, "Width");
            int height = OOoUtils.getIntProperty(oDrawPage, "Height");


            Size aSize = new Size(width - borderLeft - borderRight, 3000);
            oTableShape.setSize(aSize);
            OOoUtils.setBooleanProperty(oTableShape, "SizeProtect", true);
            Point aPos = new Point();

            aPos.X = width / 2 - aSize.Width / 2;
            aPos.Y = height - borderBottom - 3000;


            oTableShape.setPosition(aPos);


            oDrawPage.add(oTableShape);
            Object model = OOoUtils.getProperty(oTableShape, "Model");
            //            XModel xModel = QI.XModel(model);
            XTable xTable = UnoRuntime.queryInterface(XTable.class, model);

            XModifiable xModifiable = UnoRuntime.queryInterface(XModifiable.class, model);

            if (xModifiable != null) {
                xModifiable.addModifyListener(new XModifyListener() {
                    @Override
                    public void modified(EventObject eventObject) {
                        Map<String, String> keyValueMapFromXTable = getKeyValueMapFromXTable(UnoRuntime.queryInterface(XTable.class, eventObject.Source));


                    }

                    @Override
                    public void disposing(EventObject eventObject) {
                        //TODO implement
                    }
                });
            }

            XEventBroadcaster xEventBroadcaster = UnoRuntime.queryInterface(XEventBroadcaster.class, model);
            if (xEventBroadcaster != null) {

                xEventBroadcaster.addEventListener(new XEventListener() {
                    @Override
                    public void notifyEvent(com.sun.star.document.EventObject eventObject) {
                        OOGraph.LOGGER.info(eventObject.EventName);
                    }

                    @Override
                    public void disposing(EventObject eventObject) {
                        //TODO implement
                    }
                });
            }

            XTableColumns columns = xTable.getColumns();


            columns.insertByIndex(0, 1);


            XTableRows rows = xTable.getRows();
            rows.insertByIndex(0, 1);
            rows.insertByIndex(0, 1);


            for (int i = 0; i < columns.getCount() - 1; i++) {
                Object byIndex = columns.getByIndex(i);
                OOoUtils.setProperty(byIndex, "Width", aSize.Width / columns.getCount());
            }

            for (int i = 0; i < rows.getCount() - 1; i++) {
                Object byIndex = rows.getByIndex(i);
                OOoUtils.setProperty(byIndex, "Height", aSize.Height / rows.getCount());
            }

            oTableShape.setSize(aSize);

        } catch (com.sun.star.uno.Exception e) {
            e.printStackTrace();
        }

    }

    Map<String, String> getKeyValueMapFromXTable(XTable xTable) {

        Map<String, String> result = new HashMap();
        XTableRows rows = xTable.getRows();
        for (int i = 0; i < rows.getCount(); i++) {
            try {
                Object byIndex = rows.getByIndex(i);
                XCellRange xCellRange = UnoRuntime.queryInterface(XCellRange.class, byIndex);
                XCell cellByPosition = xCellRange.getCellByPosition(0, 0);
                String formula = cellByPosition.getFormula();
                double value = cellByPosition.getValue();

            } catch (com.sun.star.lang.IndexOutOfBoundsException e) {
                e.printStackTrace();
            } catch (WrappedTargetException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
