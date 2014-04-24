package ru.ssau.graphplus.node;

import com.sun.star.awt.Point;
import com.sun.star.awt.Rectangle;
import com.sun.star.awt.Size;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.XMultiServiceFactory;
import ru.ssau.graphplus.*;
import ru.ssau.graphplus.api.*;
import ru.ssau.graphplus.api.DiagramElement;
import ru.ssau.graphplus.api.DiagramModel;
import ru.ssau.graphplus.commons.MiscHelper;
import ru.ssau.graphplus.commons.PostCreationAction;
import ru.ssau.graphplus.commons.QI;

import java.io.Serializable;


public abstract class NodeBase implements Node, ShapeBuilder, DiagramElement, Serializable, Refreshable<DiagramModel>, DeferredInitializable<XShape>, StringSerializable {


    private static final long serialVersionUID = 1L;
    protected transient PostCreationAction postCreationAction;
    protected transient XShape xShape;
    private String id;
    protected NodeType nodeType;

    // actually name = ((XNamed) shape).getName()
    // but copy as field needed for serialization purposes
    private String name;

    public void setName(String name) {
        this.name = name;
        if (xShape != null){
            QI.XNamed(xShape).setName(name);
        }
    }

    protected NodeBase(PostCreationAction postCreationAction) {
        this.postCreationAction = postCreationAction;
    }

    public NodeBase() {
    }

    public NodeBase(String id){
        this.id = id;
    }


    @Override
    public Rectangle getBound() {
        Point position = xShape.getPosition();
        Size size = xShape.getSize();
        return new Rectangle(position.X, position.Y, size.Width, size.Height);
    }

    @Override
    public String getName() {
        String string = null;

        if (xShape != null){
            string = QI.XText(xShape).getString();
        }

        if (string != null && !string.equals(name)){
            name = string;
        }

        return name;

    }

    @Override
    public Point getPosition() {
        return xShape.getPosition();
    }


    @Override
    public Size getSize() {
        return xShape.getSize();
    }

    @Override
    public void setProps(XShape... params) {
        // TODO
        if (params.length > 0) {
            this.xShape = params[0];
        }
    }

    @Override
    public void refresh(DiagramModel diagramModel) {
        //To change body of implemented methods use File | Settings | File Templates.
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
        if (xShape!=null){
            xShape.setPosition(new Point(x, y));
        }
    }

    @Override
    public void setPosition(Point position) {
        xShape.setPosition(position);
    }

    public abstract XShape buildShape(XMultiServiceFactory xMSF);

    public XShape getShape() {
        return xShape;
    }

    public void setShape(XShape xShape) {
        this.xShape = xShape;
    }

    public void setProps() {
        if (postCreationAction != null) {
            postCreationAction.postCreate(getShape());
        }
    }

    @Override
    public NodeType getType() {
        if (nodeType == null) {
            if (this instanceof ClientNode) {
                nodeType = NodeType.ClientPort;
            } else if (this instanceof ServerNode) {
                nodeType = NodeType.ServerPort;
            } else if (this instanceof ProcessNode) {
                nodeType = NodeType.StartMethodOfProcess;
            } else if (this instanceof ProcedureNode) {
                nodeType = NodeType.MethodOfProcess;
            }
        }
        return nodeType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()+ "{ name='"+getName()+" id='" + id + "' }";
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
            MiscHelper.tagShapeAsNode(shape);
        }
    }
}
