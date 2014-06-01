
package ru.ssau.graphplus;

import com.sun.star.frame.XFrame;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

/**
 * This class which objects are containers for diagram model,diagram controller, dispatch objects per frame
 */
public class FrameObject {

    private final XFrame xFrame;
    private final DiagramController diagramController;
    private final DiagramModel diagramModel;

    private final MyDispatch dispatch;



    public FrameObject(XFrame xFrame, DiagramController diagramController, DiagramModel diagramModel, MyDispatch myDispatch) {
        this.xFrame = xFrame;
        this.diagramController = diagramController;
        this.diagramModel = diagramModel;
        this.dispatch = myDispatch;
        frameObjects.put(xFrame, this);




//            String home = System.getProperty("user.home");
//            Path log = Paths.get(home + File.separator + ".graphplus" + File.separator + "oograph.log");
//
//
//
//            try {
//                Files.createDirectories(log.getParent());
//                File file1 = Files.createFile(log).toFile();
//                Logger graphplus = Logger.getLogger("graphplus");
//                graphplus.addHandler(new FileHandler(file1.getAbsolutePath()));
//                myDispatch.setLogger(graphplus);
//            } catch (FileAlreadyExistsException e) {
//                System.err.println("already exists: " + e.getMessage());
//            } catch (IOException e) {
//                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//            }

    }

    private static Map<XFrame, FrameObject> frameObjects = new WeakHashMap<>();

    public static Map<XFrame,FrameObject> getFrameObjects(){
        return Collections.unmodifiableMap(frameObjects);
    }

    public XFrame getFrame() {
        return xFrame;
    }

    public DiagramController getController() {
        return diagramController;
    }

    public MyDispatch getDispatch() {
        return dispatch;
    }
}
