/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.gui.dialogs;

import com.google.common.collect.ImmutableMap;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XControlContainer;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XTextComponent;
import com.sun.star.datatransfer.DataFlavor;
import com.sun.star.datatransfer.UnsupportedFlavorException;
import com.sun.star.datatransfer.XTransferable;
import com.sun.star.datatransfer.clipboard.XClipboard;
import com.sun.star.datatransfer.clipboard.XClipboardOwner;
import com.sun.star.uno.Type;
import com.sun.star.uno.UnoRuntime;
import ru.ssau.graphplus.MyDialog;
import ru.ssau.graphplus.MyDialogHandler;
import ru.ssau.graphplus.QI;

public class GetCodeDialog implements MyDialog<GetCodeDialog> {

    public static final String GET_CODE_DIALOG_XDL = "vnd.sun.star.extension://ru.ssau.graphplus.oograph/dialogs/GetCodeDialog.xdl";

    public static final String COPY_TO_CLIPBOARD_EXECUTE = "copyToClipboardExecute";

    private final MyDialogHandler myDialogHandler;
    private final String code;
    private final Object oClipboard;
    //    private final DiagramModel diagramModel;
    private XDialog xDialog;
    private XTextComponent xTextComponent;

    public GetCodeDialog(final String code, final Object oClipboard) {
        this.oClipboard = oClipboard;

        this.code = code;
//        this.diagramModel = diagramModel;
        myDialogHandler = new MyDialogHandler(ImmutableMap.<MyDialogHandler.Event, MyDialogHandler.EventHandler>builder().put(MyDialogHandler.Event.event(COPY_TO_CLIPBOARD_EXECUTE), new MyDialogHandler.EventHandler() {
            @Override
            public boolean handle(XDialog xDialog, Object o, String s) {


                // query for the interface XClipboard
                XClipboard xClipboard = (XClipboard)UnoRuntime.queryInterface(XClipboard.class, oClipboard);
                //---------------------------------------------------
                // becoming a clipboard owner
                //---------------------------------------------------
                System.out.println("Becoming a clipboard owner...");
                System.out.println("");
                ClipboardOwner aClipOwner = new ClipboardOwner();

                xClipboard.setContents(new TextTransferable(xTextComponent.getText()), aClipOwner);
//                while (aClipOwner.isClipboardOwner())
//                {
//                    System.out.println("Still clipboard owner...");
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                    }
//                }

                return true;
            }
        }).build());


    }

    @Override
    public MyDialogHandler getDialogHandler() {
        return myDialogHandler;
    }

//    public DiagramModel getDiagramModel() {
//        return diagramModel;
//    }

    public void init(XDialog xDialog){
        this.xDialog = xDialog;
        XControlContainer xControlContainer = UnoRuntime.queryInterface(XControlContainer.class, xDialog);
        XControl codeTextField = xControlContainer.getControl("CodeTextField");
        xTextComponent = QI.XTextComponent(codeTextField);
        xTextComponent.setText(code);
    }



    //---------------------------------------
    // A simple transferable containing only
    // one format, unicode text
    //---------------------------------------
    public class TextTransferable implements XTransferable
    {
        public TextTransferable(String aText)
        {
            text = aText;
        }

        // XTransferable methods
        public Object getTransferData(DataFlavor aFlavor) throws UnsupportedFlavorException
        {
            if ( !aFlavor.MimeType.equalsIgnoreCase( UNICODE_CONTENT_TYPE ) )
                throw new UnsupportedFlavorException();
            return text;
        }
        public DataFlavor[] getTransferDataFlavors()
        {
            DataFlavor[] adf = new DataFlavor[1];
            DataFlavor uniflv = new DataFlavor(
                    UNICODE_CONTENT_TYPE,
                    "Unicode Text",
                    new Type(String.class) );
            adf[0] = uniflv;

            return adf;
        }
        public boolean isDataFlavorSupported(DataFlavor aFlavor)
        {
            return aFlavor.MimeType.equalsIgnoreCase(UNICODE_CONTENT_TYPE);
        }

        // members
        private final String text;
        private final String UNICODE_CONTENT_TYPE = "text/plain;charset=utf-16";
    }


    public class ClipboardOwner implements XClipboardOwner
    {
        public void lostOwnership(
                XClipboard xClipboard,
                XTransferable xTransferable )
        {
            System.out.println("");
            System.out.println( "Lost clipboard ownership..." );
            System.out.println("");

            isowner = false;
        }

        public boolean isClipboardOwner()
        {
            return isowner;
        }

        private boolean isowner = true;
    }
}
