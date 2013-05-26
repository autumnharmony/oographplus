package ru.ssau.graphplus.node;

import com.sun.star.awt.Point;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.XMultiServiceFactory;
import ru.ssau.graphplus.*;

import java.io.Serializable;
import java.util.logging.Logger;


public abstract class Node implements ShapeBuilder, DiagramElement, Serializable {

    private static final long serialVersionUID = 1L;
    protected transient PostCreationAction postCreationAction;
    protected transient XShape xShape;
    private String id;
    private NodeType nodeType;

    protected Node(PostCreationAction postCreationAction) {
        this.postCreationAction = postCreationAction;
    }

    public Node() {
    }

    @Override
    public void refresh(DiagramModel diagramModel) {
        Misc.setId(xShape, getName());
        if (!getName().equals(QI.XText(xShape).getString())) {

            Logger.getAnonymousLogger().info("Need to refresh node name");
        }
    }

    public int getX() {
        return xShape.getPosition().X;
    }

    public void setX(int x) {
        xShape.setPosition(new Point(x, xShape.getPosition().Y));

    }

    public int getY() {
        return xShape.getPosition().Y;
    }

    public void setY(int y) {
        xShape.setPosition(new Point(xShape.getPosition().X, y));
    }

    public void setPosition(int x, int y) {
        xShape.setPosition(new Point(x, y));
    }

    public abstract XShape buildShape(XMultiServiceFactory xMSF);

    public XShape getShape() {
        return xShape;
    }

    public void setShape(XShape xShape) {
        this.xShape = xShape;
    }

    public void runPostCreation() {
        if (postCreationAction != null) {
            postCreationAction.postCreate(getShape());
        }
    }

    public NodeType getType() {
        if (nodeType == null) {
            if (this instanceof ClientNode) {
                nodeType = NodeType.Client;
            } else if (this instanceof ServerNode) {
                nodeType = NodeType.Server;
            } else if (this instanceof ProcessNode) {
                nodeType = NodeType.Process;
            } else if (this instanceof ProcedureNode) {
                nodeType = NodeType.Procedure;
            }
        }
        return nodeType;
    }

    public String getName() {
        return QI.XText(xShape).getString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public enum NodeType {
        Client,
        Server,
        Process,
        Procedure
    }

    public static interface PostCreationAction {
        public abstract void postCreate(XShape shape);
    }

    public static class DefaultPostCreationAction implements PostCreationAction {

        boolean convert;

        public DefaultPostCreationAction(boolean convert) {
            this.convert = convert;
        }

        public DefaultPostCreationAction() {
        }

        @Override
        public void postCreate(XShape shape) {
            Misc.tagShapeAsNode(shape);
        }
    }

}
