/* 
 * QI.java 
 * 
 * A module to sugar coat the UnoRuntime.queryInterface() procedure. 
 * 
 * Created on February 21, 2003, 5:13 PM 
 * 
 * Copyright 2003 Danny Brewer 
 * Anyone may run this code. 
 * If you wish to modify or distribute this code, then 
 *  you are granted a license to do so only under the terms 
 *  of the Gnu Lesser General Public License. 
 * See:  http://www.gnu.org/licenses/lgpl.html 
 */

package ru.ssau.graphplus;


//---------------------------------------------------------------------- 
//  OpenOffice.org imports 
//---------------------------------------------------------------------- 

import com.sun.star.awt.*;
import com.sun.star.beans.XPropertySet;
import com.sun.star.bridge.XUnoUrlResolver;
import com.sun.star.chart.XChartDocument;
import com.sun.star.chart.XDiagram;
import com.sun.star.container.*;
import com.sun.star.document.XEmbeddedObjectSupplier;
import com.sun.star.drawing.*;
import com.sun.star.frame.*;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.sheet.XCellRangeAddressable;
import com.sun.star.sheet.XSpreadsheet;
import com.sun.star.sheet.XSpreadsheetDocument;
import com.sun.star.table.XTableChart;
import com.sun.star.table.XTableCharts;
import com.sun.star.table.XTableChartsSupplier;
import com.sun.star.text.XText;
import com.sun.star.text.XTextField;
import com.sun.star.util.XCloseable;
import com.sun.star.util.XNumberFormatTypes;
import com.sun.star.util.XNumberFormatsSupplier;
import com.sun.star.view.XPrintable;
import com.sun.star.view.XSelectionSupplier;




public class QI {


    private QI() {
    }


    private static UnoRuntimeWrapper _unoRuntimeWrapper;

    {
        _unoRuntimeWrapper= new UnoRuntimeWrapperImpl();
    }

    // for test
    public static void setUnoRuntimeWrapper(UnoRuntimeWrapper unoRuntimeWrapper){
        _unoRuntimeWrapper = unoRuntimeWrapper;
    }


    // The following are syntax sugar for UnoRuntime.queryInterface(). 


    //-------------------------------------------------- 
    //  Beans                   com.sun.star.beans.* 
    //-------------------------------------------------- 

    static public XPropertySet XPropertySet(Object obj) {
        return (XPropertySet)
                _unoRuntimeWrapper.queryInterface(XPropertySet.class, obj);
    }

    //-------------------------------------------------- 
    //  Bridge                  com.sun.star.bridge.* 
    //-------------------------------------------------- 

    static public XUnoUrlResolver XUnoUrlResolver(Object obj) {
        return (XUnoUrlResolver) _unoRuntimeWrapper.queryInterface(XUnoUrlResolver.class, obj);
    }

    //-------------------------------------------------- 
    //  Chart                   com.sun.star.chart.* 
    //-------------------------------------------------- 

    static public XChartDocument XChartDocument(Object obj) {
        return (XChartDocument) _unoRuntimeWrapper.queryInterface(XChartDocument.class, obj);
    }

    static public XDiagram XDiagram(Object obj) {
        return (XDiagram) _unoRuntimeWrapper.queryInterface(XDiagram.class, obj);
    }

    //-------------------------------------------------- 
    //  Container               com.sun.star.container.* 
    //-------------------------------------------------- 

    static public XIndexAccess XIndexAccess(Object obj) {
        return (XIndexAccess) _unoRuntimeWrapper.queryInterface(XIndexAccess.class, obj);
    }

    static public XNameAccess XNameAccess(Object obj) {
        return (XNameAccess) _unoRuntimeWrapper.queryInterface(XNameAccess.class, obj);
    }

    static public XNameContainer XNameContainer(Object obj) {
        return (XNameContainer) _unoRuntimeWrapper.queryInterface(XNameContainer.class, obj);
    }

    static public XNameReplace XNameReplace(Object obj) {
        return (XNameReplace) _unoRuntimeWrapper.queryInterface(XNameReplace.class, obj);
    }

    static public XNamed XNamed(Object obj) {
        return (XNamed) _unoRuntimeWrapper.queryInterface(XNamed.class, obj);
    }

    //-------------------------------------------------- 
    //  Document                com.sun.star.document.* 
    //-------------------------------------------------- 

    static public XEmbeddedObjectSupplier XEmbeddedObjectSupplier(Object obj) {
        return (XEmbeddedObjectSupplier) _unoRuntimeWrapper.queryInterface(XEmbeddedObjectSupplier.class, obj);
    }

    //-------------------------------------------------- 
    //  Drawing                 com.sun.star.drawing.* 
    //-------------------------------------------------- 

    static public XDrawPage XDrawPage(Object obj) {
        return (XDrawPage) _unoRuntimeWrapper.queryInterface(XDrawPage.class, obj);
    }

    static public XDrawPageSupplier XDrawPageSupplier(Object obj) {
        return (XDrawPageSupplier) _unoRuntimeWrapper.queryInterface(XDrawPageSupplier.class, obj);
    }

    static public XDrawPages XDrawPages(Object obj) {
        return (XDrawPages) _unoRuntimeWrapper.queryInterface(XDrawPages.class, obj);
    }

    static public XDrawPagesSupplier XDrawPagesSupplier(Object obj) {
        return (XDrawPagesSupplier) _unoRuntimeWrapper.queryInterface(XDrawPagesSupplier.class, obj);
    }

    static public XLayerManager XLayerManager(Object obj) {
        return (XLayerManager) _unoRuntimeWrapper.queryInterface(XLayerManager.class, obj);
    }

    static public XLayerSupplier XLayerSupplier(Object obj) {
        return (XLayerSupplier) _unoRuntimeWrapper.queryInterface(XLayerSupplier.class, obj);
    }

    static public XShape XShape(Object obj) {
        return (XShape) _unoRuntimeWrapper.queryInterface(XShape.class, obj);
    }

    static public XShapes XShapes(Object obj) {
        return (XShapes) _unoRuntimeWrapper.queryInterface(XShapes.class, obj);
    }

    //-------------------------------------------------- 
    //  Frame                   com.sun.star.frame.* 
    //-------------------------------------------------- 

    static public XComponentLoader XComponentLoader(Object obj) {
        return (XComponentLoader) _unoRuntimeWrapper.queryInterface(XComponentLoader.class, obj);
    }

    static public XDispatchHelper XDispatchHelper(Object obj) {
        return (XDispatchHelper) _unoRuntimeWrapper.queryInterface(XDispatchHelper.class, obj);
    }

    static public XDispatchProvider XDispatchProvider(Object obj) {
        return (XDispatchProvider) _unoRuntimeWrapper.queryInterface(XDispatchProvider.class, obj);
    }

    static public XModel XModel(Object obj) {
        return (XModel) _unoRuntimeWrapper.queryInterface(XModel.class, obj);
    }

    static public XStorable XStorable(Object obj) {
        return (XStorable) _unoRuntimeWrapper.queryInterface(XStorable.class, obj);
    }

    //-------------------------------------------------- 
    //  Lang                    com.sun.star.lang.* 
    //-------------------------------------------------- 

    static public XMultiComponentFactory XMultiComponentFactory(Object obj) {
        return (XMultiComponentFactory) _unoRuntimeWrapper.queryInterface(XMultiComponentFactory.class, obj);
    }

    static public XMultiServiceFactory XMultiServiceFactory(Object obj) {
        return (XMultiServiceFactory) _unoRuntimeWrapper.queryInterface(XMultiServiceFactory.class, obj);
    }

    //-------------------------------------------------- 
    //  Sheet                   com.sun.star.sheet.* 
    //-------------------------------------------------- 

    static public XCellRangeAddressable XCellRangeAddressable(Object obj) {
        return (XCellRangeAddressable) _unoRuntimeWrapper.queryInterface(XCellRangeAddressable.class, obj);
    }

    static public XSpreadsheet XSpreadsheet(Object obj) {
        return (XSpreadsheet) _unoRuntimeWrapper.queryInterface(XSpreadsheet.class, obj);
    }

    static public XSpreadsheetDocument XSpreadsheetDocument(Object obj) {
        return (XSpreadsheetDocument) _unoRuntimeWrapper.queryInterface(XSpreadsheetDocument.class, obj);
    }

    //-------------------------------------------------- 
    //  Table                   com.sun.star.table.* 
    //-------------------------------------------------- 

    static public XTableChart XTableChart(Object obj) {
        return (XTableChart) _unoRuntimeWrapper.queryInterface(XTableChart.class, obj);
    }

    static public XTableCharts XTableCharts(Object obj) {
        return (XTableCharts) _unoRuntimeWrapper.queryInterface(XTableCharts.class, obj);
    }

    static public XTableChartsSupplier XTableChartsSupplier(Object obj) {
        return (XTableChartsSupplier) _unoRuntimeWrapper.queryInterface(XTableChartsSupplier.class, obj);
    }

    //-------------------------------------------------- 
    //  Text                    com.sun.star.text.* 
    //-------------------------------------------------- 

    static public XText XText(Object obj) {
        return (XText) _unoRuntimeWrapper.queryInterface(XText.class, obj);
    }

    //-------------------------------------------------- 
    //  Util                    com.sun.star.util.* 
    //-------------------------------------------------- 

    static public XCloseable XCloseable(Object obj) {
        return (XCloseable) _unoRuntimeWrapper.queryInterface(XCloseable.class, obj);
    }

    static public XNumberFormatsSupplier XNumberFormatsSupplier(Object obj) {
        return (XNumberFormatsSupplier) _unoRuntimeWrapper.queryInterface(XNumberFormatsSupplier.class, obj);
    }

    static public XNumberFormatTypes XNumberFormatTypes(Object obj) {
        return (XNumberFormatTypes) _unoRuntimeWrapper.queryInterface(XNumberFormatTypes.class, obj);
    }

    //-------------------------------------------------- 
    //  View                    com.sun.star.view.* 
    //-------------------------------------------------- 

    static public XPrintable XPrintable(Object obj) {
        return (XPrintable) _unoRuntimeWrapper.queryInterface(XPrintable.class, obj);
    }

    static public XSelectionSupplier XSelectionSupplier(Object obj) {
        return (XSelectionSupplier) _unoRuntimeWrapper.queryInterface(XSelectionSupplier.class, obj);
    }

    static public XDispatchProviderInterception XDispatchProviderInterception(Object object) {
        return (XDispatchProviderInterception) _unoRuntimeWrapper.queryInterface(XDispatchProviderInterception.class, object);
    }

    static public XComponent XComponent(Object o) {
        return (XComponent) _unoRuntimeWrapper.queryInterface(XComponent.class, o);
    }

    static public XConnectorShape XConnectorShape(Object o) {
        return (XConnectorShape) _unoRuntimeWrapper.queryInterface(XConnectorShape.class, o);
    }

    public static XTextField XTextField(Object o) {
        return (XTextField) _unoRuntimeWrapper.queryInterface(XTextField.class, o);
    }

    public static XTextComponent XTextComponent(Object o) {
        return (XTextComponent) _unoRuntimeWrapper.queryInterface(XTextComponent.class, o);
    }

    public static XItemList XItemList(Object o) {
       return _unoRuntimeWrapper.queryInterface(XItemList.class, o);
    }

    public static XComboBox XCombobox(Object o) {
        return _unoRuntimeWrapper.queryInterface(XComboBox.class, o);
    }

    public static XControl XControl(Object o) {
        return _unoRuntimeWrapper.queryInterface(XControl.class, o);
    }

    public static XButton XButton(Object o) {
        return _unoRuntimeWrapper.queryInterface(XButton.class, o);
    }
}