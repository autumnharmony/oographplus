package ru.ssau.graphplus;

import com.sun.star.drawing.*;
import com.sun.star.frame.XController;
import com.sun.star.frame.XModel;
import com.sun.star.lang.*;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.UnoRuntime;
import ru.ssau.graphplus.node.Node;

import java.util.Collection;

public class DrawHelper {

    private DrawHelper() {
    }

    //---------------------------------------------------------------------- 
    //  Sugar coated access to pages on a drawing document. 
    //   The first page of a drawing is page zero. 
    //---------------------------------------------------------------------- 
    // How many pages are on a drawing? 
    public static int getNumDrawPages(Object drawDoc) {
        XDrawPages drawDoc_XDrawPages = drawDoc_getXDrawPages(drawDoc);
        return drawDoc_XDrawPages.getCount();
    }

    public static int getNumDrawPages(XDrawPages drawDoc_XDrawPages) {
        return drawDoc_XDrawPages.getCount();
    }

    // Obtain a page from a drawing. 
    // Return null if not successful. 
    public static XDrawPage getDrawPageByIndex(Object drawDoc, int pageIndex)
            throws com.sun.star.lang.IndexOutOfBoundsException,
            com.sun.star.lang.WrappedTargetException {
        XDrawPages drawDoc_XDrawPages = drawDoc_getXDrawPages(drawDoc);
        return getDrawPageByIndex(drawDoc_XDrawPages, pageIndex);
    }

    public static XDrawPage getDrawPageByIndex(XDrawPages drawDoc_XDrawPages, int pageIndex)
            throws com.sun.star.lang.IndexOutOfBoundsException,
            com.sun.star.lang.WrappedTargetException {
        // Now ask the XIndexAccess interface to the drawPages object to get a certian page. 
        Object drawPage = drawDoc_XDrawPages.getByIndex(pageIndex);

        // Get the right interface to the page. 
        XDrawPage drawPage_XDrawPage = QI.XDrawPage(drawPage);

        return drawPage_XDrawPage;
    }

    // Create a new page on a drawing. 
    // Return null if not successful. 
    public static XDrawPage insertNewPageByIndex(Object drawDoc, int pageIndex) {
        XDrawPages drawDoc_XDrawPages = drawDoc_getXDrawPages(drawDoc);
        XDrawPage xDrawPage = drawDoc_XDrawPages.insertNewByIndex(pageIndex);
        return xDrawPage;
    }

    public static XDrawPage insertNewPageByIndex(XDrawPages drawDoc_XDrawPages, int pageIndex) {
        XDrawPage xDrawPage = drawDoc_XDrawPages.insertNewByIndex(pageIndex);
        return xDrawPage;
    }

    // Remove a page from a drawing 
    public static void removePageFromDrawing(Object drawDoc, int pageIndex)
            throws com.sun.star.lang.IndexOutOfBoundsException,
            com.sun.star.lang.WrappedTargetException {
        XDrawPages drawDoc_XDrawPages = drawDoc_getXDrawPages(drawDoc);
        XDrawPage xDrawPage = getDrawPageByIndex(drawDoc_XDrawPages, pageIndex);
        drawDoc_XDrawPages.remove(xDrawPage);
    }

    public static void removePageFromDrawing(XDrawPages drawDoc_XDrawPages, int pageIndex)
            throws com.sun.star.lang.IndexOutOfBoundsException,
            com.sun.star.lang.WrappedTargetException {
        XDrawPage xDrawPage = getDrawPageByIndex(drawDoc_XDrawPages, pageIndex);
        drawDoc_XDrawPages.remove(xDrawPage);
    }

    public static void removePageFromDrawing(Object drawDoc, XDrawPage xDrawPage) {
        XDrawPages xDrawPages = drawDoc_getXDrawPages(drawDoc);
        xDrawPages.remove(xDrawPage);
    }

    public static void removePageFromDrawing(XDrawPages drawDoc_XDrawPages, XDrawPage xDrawPage) {
        drawDoc_XDrawPages.remove(xDrawPage);
    }

    //---------------------------------------------------------------------- 
    //  Sugar coated access to layers of a drawing document. 
    //   The first layer of a drawing is page zero. 
    //---------------------------------------------------------------------- 
    public static int getNumDrawLayers(Object drawDoc) {
        XLayerManager xLayerManager = drawDoc_getXLayerManager(drawDoc);
        return xLayerManager.getCount();
    }

    //---------------------------------------------------------------------- 
    //  Get useful interfaces to a drawing document. 
    //---------------------------------------------------------------------- 
    // Get one of the useful interfaces from a drawing document. 
    // XDrawPages gives you... 
    //      XDrawPage insertNewByIndex( int pageIndex ) 
    //      void remove( XDrawPage drawPage ) 
    // Since XDrawPages includes XIndexAccess, you also get... 
    //      int getCount() 
    //      Object getByIndex( long index ) 
    // Since XIndexAccess includes XElementAccess, you also get... 
    //      type getElementType() 
    //      boolean hasElements() 
    public static XDrawPages drawDoc_getXDrawPages(Object drawDoc) {
        // Get a different interface to the drawDoc. 
        // The parameter passed in to us is the wrong interface to the drawDoc. 
        XDrawPagesSupplier drawDoc_XDrawPagesSupplier = QI.XDrawPagesSupplier(drawDoc);

        // Ask the drawing document to give us it's draw pages object. 
        Object drawPages = drawDoc_XDrawPagesSupplier.getDrawPages();

        // Get the XDrawPages interface to the object. 
        XDrawPages drawPages_XDrawPages = QI.XDrawPages(drawPages);

        return drawPages_XDrawPages;
    }

    // Get one of the useful interfaces from a drawing document. 
    // XLayerManager gives you... 
    //      XLayer insertNewByIndex( int layerIndex ) 
    //      void remove( XLayer layer ) 
    //      void attachShapeToLayer( XShape shape, XLayer layer ) 
    //      XLayer getLayerForShape( XShape shape ) 
    // Since XLayerManager includes XIndexAccess, you also get... 
    //      int getCount() 
    //      Object getByIndex( long index ) 
    // Since XIndexAccess includes XElementAccess, you also get... 
    //      type getElementType() 
    //      boolean hasElements() 
    // QueryInterface can also be used to get an XNameAccess from this object. 
    // XNameAccess gives you... 
    //      Object getByName( String name ) 
    //      String[] getElementNames() 
    //      boolean hasByName( String name ) 
    public static XLayerManager drawDoc_getXLayerManager(Object drawDoc) {
        // Get a different interface to the drawDoc. 
        // The parameter passed in to us is the wrong interface to the drawDoc. 
        XLayerSupplier drawDoc_XLayerSupplier = QI.XLayerSupplier(drawDoc);

        // Ask the drawing document to give us it's layer manager object. 
        Object layerManager = drawDoc_XLayerSupplier.getLayerManager();

        // Get the XLayerManager interface to the object. 
        XLayerManager layerManager_XLayerManager = QI.XLayerManager(layerManager);

        return layerManager_XLayerManager;
    }

    //---------------------------------------------------------------------- 
    //  Operations on Pages 
    //---------------------------------------------------------------------- 
    public static String getPageName(Object drawPage) {
        // Get a different interface to the drawDoc. 
        //XNamed drawPage_XNamed = QI.XNamed( drawPage );        
        //return drawPage_XNamed.getName(); 
        return OOoUtils.XNamed_getName(drawPage);
    }

    public static void setPageName(Object drawPage, String pageName) {
        // Get a different interface to the drawDoc. 
        //XNamed drawPage_XNamed = QI.XNamed( drawPage ); 
        //drawPage_XNamed.setName( pageName ); 
        OOoUtils.XNamed_setName(drawPage, pageName);
    }

    //  Sugar Coated property manipulation. 
    public static void setHeight(Object obj, int height)
            throws com.sun.star.beans.UnknownPropertyException,
            com.sun.star.beans.PropertyVetoException,
            com.sun.star.lang.IllegalArgumentException,
            com.sun.star.lang.WrappedTargetException {
        OOoUtils.setIntProperty(obj, "Height", height);
    }

    public static int getHeight(Object obj)
            throws com.sun.star.beans.UnknownPropertyException,
            com.sun.star.lang.WrappedTargetException {
        return OOoUtils.getIntProperty(obj, "Height");
    }

    public static void setWidth(Object obj, int width)
            throws com.sun.star.beans.UnknownPropertyException,
            com.sun.star.beans.PropertyVetoException,
            com.sun.star.lang.IllegalArgumentException,
            com.sun.star.lang.WrappedTargetException {
        OOoUtils.setIntProperty(obj, "Width", width);
    }

    public static int getWidth(Object obj)
            throws com.sun.star.beans.UnknownPropertyException,
            com.sun.star.lang.WrappedTargetException {
        return OOoUtils.getIntProperty(obj, "Width");
    }
    //---------------------------------------------------------------------- 
    //  Operations on Shapes 
    //---------------------------------------------------------------------- 
    public static final String SHAPE_KIND_RECTANGLE = "com.sun.star.drawing.RectangleShape";
    public static final String SHAPE_KIND_ELLIPSE = "com.sun.star.drawing.EllipseShape";
    public static final String SHAPE_KIND_LINE = "com.sun.star.drawing.LineShape";
    public static final String SHAPE_KIND_TEXT = "com.sun.star.drawing.TextShape";

    public static XShape createRectangleShape(Object drawDoc, int x, int y, int width, int height)
            throws com.sun.star.uno.Exception,
            com.sun.star.beans.PropertyVetoException {
        return createShape(drawDoc, SHAPE_KIND_RECTANGLE, x, y, width, height);
    }

    public static XShape createEllipseShape(Object drawDoc, int x, int y, int width, int height)
            throws com.sun.star.uno.Exception,
            com.sun.star.beans.PropertyVetoException {
        return createShape(drawDoc, SHAPE_KIND_ELLIPSE, x, y, width, height);
    }

    public static XShape createLineShape(Object drawDoc, int x, int y, int width, int height)
            throws com.sun.star.uno.Exception,
            com.sun.star.beans.PropertyVetoException {
        return createShape(drawDoc, SHAPE_KIND_LINE, x, y, width, height);
    }

    public static XShape createTextShape(Object drawDoc, int x, int y, int width, int height)
            throws com.sun.star.uno.Exception,
            com.sun.star.beans.PropertyVetoException {
        return createShape(drawDoc, SHAPE_KIND_TEXT, x, y, width, height);
    }

    // Return null if not successful. 
    // Possible values for kind are: 
    //      com.sun.star.drawing.RectangleShape 
    //      com.sun.star.drawing.EllipseShape 
    //      com.sun.star.drawing.LineShape 
    //      com.sun.star.drawing.TextShape 
    public static XShape createShape(Object drawDoc, String kind)
            throws com.sun.star.uno.Exception {
        // We need the XMultiServiceFactory interface. 
        XMultiServiceFactory drawDoc_XMultiServiceFactory;
        if (drawDoc instanceof XMultiServiceFactory) {
            // If the right interface was passed in, just typecaset it. 
            drawDoc_XMultiServiceFactory = (XMultiServiceFactory) drawDoc;
        } else {
            // Get a different interface to the drawDoc. 
            // The parameter passed in to us is the wrong interface to the drawDoc. 
            drawDoc_XMultiServiceFactory = QI.XMultiServiceFactory(drawDoc);
        }

        // Ask MultiServiceFactory to create a shape. 
        // Yuck, it gives back an Object with no specific interface. 
        Object shape_noInterface = drawDoc_XMultiServiceFactory.createInstance(kind);

        // Get a more useful interface to the shape object. 
        XShape shape = QI.XShape(shape_noInterface);

        return shape;
    }

    public static XShape createShape(Object drawDoc, String kind, int x, int y, int width, int height)
            throws com.sun.star.uno.Exception,
            com.sun.star.beans.PropertyVetoException {
        XShape shape = createShape(drawDoc, kind);
        setShapePositionAndSize(shape, x, y, width, height);
        return shape;
    }

    public static void setShapePositionAndSize(XShape shape, int x, int y, int width, int height)
            throws com.sun.star.beans.PropertyVetoException {
        setShapePosition(shape, x, y);
        setShapeSize(shape, width, height);
    }

    public static void setShapePosition(XShape shape, int x, int y) {
        com.sun.star.awt.Point position = new com.sun.star.awt.Point(x, y);
        shape.setPosition(position);
    }

    public static void setShapeSize(XShape shape, int width, int height)
            throws com.sun.star.beans.PropertyVetoException {
        com.sun.star.awt.Size size = new com.sun.star.awt.Size(width, height);
        shape.setSize(size);
    }

    //  Sugar Coated property manipulation. 
    public static void setFillColor(Object obj, int rgbFillColor)
            throws com.sun.star.beans.UnknownPropertyException,
            com.sun.star.beans.PropertyVetoException,
            com.sun.star.lang.IllegalArgumentException,
            com.sun.star.lang.WrappedTargetException {
        OOoUtils.setIntProperty(obj, "FillColor", rgbFillColor);
    }

    public static int getFillColor(Object obj)
            throws com.sun.star.beans.UnknownPropertyException,
            com.sun.star.lang.WrappedTargetException {
        return OOoUtils.getIntProperty(obj, "FillColor");
    }

    public static XDrawPage getCurrentDrawPage(XComponent drawDoc) {
        XModel xModel = (XModel) UnoRuntime.queryInterface(XModel.class, drawDoc);
        XController xController = xModel.getCurrentController();
        XDrawView xDV = (XDrawView) UnoRuntime.queryInterface(XDrawView.class, xController);
        return xDV.getCurrentPage();
    }

    public static void insertShapeOnCurrentPage(XShape xShape, XComponent drawDoc) {
        XDrawPage drawPage = getCurrentDrawPage(drawDoc);
        XShapes xShapes = (XShapes) UnoRuntime.queryInterface(XShapes.class, drawPage);
        ShapeHelper.insertShape(xShape, drawPage);
    }

    public static void insertNodeOnCurrentPage(final Node node, XComponent drawDoc) {
        XDrawPage drawPage = getCurrentDrawPage(drawDoc);
//        XShapes xShapes = (XShapes) UnoRuntime.queryInterface(XShapes.class, drawPage);

        //ShapeHelper.insertShape(linkReplace.getShape(), xDP , postCreationAction);
        ShapeHelper.insertShape(node.getShape(), drawPage, new Node.DefaultPostCreationAction());
                //node.runPostCreation();
    }
    
    public static void insertShapesOnCurrentPage(Collection<XShape> shapes, XComponent drawDoc) {
        XDrawPage drawPage = getCurrentDrawPage(drawDoc);
        XShapes xShapes = (XShapes) UnoRuntime.queryInterface(XShapes.class, drawPage);
      
        for (XShape xShape : shapes)
        ShapeHelper.insertShape(xShape, drawPage);
    }

    public static boolean pageContainsShape(XDrawPage xDrawPage, XShape xShape){
        Object o = AnyConverter.toObject(Object.class, xShape);

        int count = xDrawPage.getCount();
        for (int i = 0; i < 0; i++){
            Object byIndex = null;
            try {
                byIndex = xDrawPage.getByIndex(i);

                XShape xShape1 = QI.XShape(byIndex);

                if (byIndex.equals(o)){
                    return true;
                }
            } catch (com.sun.star.lang.IndexOutOfBoundsException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (WrappedTargetException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        }

        return false;
    }
}
