package ru.ssau.graphplus;

import com.sun.star.frame.*;
import com.sun.star.util.URL;

public class MyInterceptor implements XDispatchProviderInterceptor {

    private final XFrame frame;
    private XDispatchProvider slaveDispatchProvider;
   private XDispatchProvider masterDispatchProvider;

    public MyInterceptor(XFrame m_xFrame, XDispatchProvider slaveDispatchProvider, XDispatchProvider masterDispatchProvider) {
        this.slaveDispatchProvider = slaveDispatchProvider;
        this.masterDispatchProvider = masterDispatchProvider;
        this.frame = m_xFrame;
    }

    public XDispatchProvider getMasterDispatchProvider() {
      return masterDispatchProvider;
   }

   public XDispatchProvider getSlaveDispatchProvider() {
      return slaveDispatchProvider;
   }

   public void setMasterDispatchProvider(XDispatchProvider arg0) {
      masterDispatchProvider = arg0;

   }

   public void setSlaveDispatchProvider(XDispatchProvider arg0) {
      slaveDispatchProvider = arg0;

   }
   /**
    * intercepts the command URLs and dispatches to the user defined handlers.
    */
   public XDispatch queryDispatch(URL arg0, String arg1, int arg2) {
      // To intercept the .uno:Save
//      if(arg0.Complete.equals(".uno:Save")) {
//         // your command handler
//         return new MySave();
//      } else if(arg0.Complete.equals(".uno:Paste")) {
//         // return your paste command handler
//      }

      System.out.println(arg0.Name + arg0.Arguments+ arg0.Complete + arg0.Main + arg0.Mark+ arg0.Password+ arg0.Path + arg0.Server +" "+arg1+ " "+ arg2);

      return getMasterDispatchProvider().queryDispatch(arg0, arg1, arg2); // to get next command url.
   }

   public XDispatch[] queryDispatches(DispatchDescriptor[] arg0) {
      return getSlaveDispatchProvider().queryDispatches(arg0);
   }
}
