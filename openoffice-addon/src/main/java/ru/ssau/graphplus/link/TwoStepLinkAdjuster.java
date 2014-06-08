package ru.ssau.graphplus.link;

import com.sun.star.awt.Point;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.lang.WrappedTargetException;
import ru.ssau.graphplus.api.DiagramService;
import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.commons.QI;
import ru.ssau.graphplus.node.NodeBase;

import java.awt.geom.Point2D;

/**
* Created by 1 on 03.06.14.
*/
public class TwoStepLinkAdjuster extends LinkAdjusterImpl {
    LinkAdjusterImpl linkAdjuster = new LinkAdjusterImpl();
    private LinkTwoConnectorsAndTextBase linkBase;

    private DiagramService diagramService;

    public TwoStepLinkAdjuster(DiagramService diagramService) {
        this.diagramService = diagramService;
    }

    @Override
    public void adjustLink(LinkBase linkBase, NodeBase from, NodeBase to) {
        this.linkBase = (LinkTwoConnectorsAndTextBase) linkBase;
        linkAdjuster.adjustLink(linkBase, from, to);

        diagramService.layoutLink(from, to, linkBase);
        super.adjustLink(linkBase, from, to);
    }
    @Override
    protected Point2D getNodeCoord(Node node) {
        if (node.equals(linkBase.getFrom())){
            Object startPosition = null;
            try {
                startPosition = QI.XPropertySet(linkBase.getConnShape1()).getPropertyValue("StartPosition");
                Point point = (Point) startPosition;
                return new Point2D.Double(point.X, point.Y);
            } catch (UnknownPropertyException | WrappedTargetException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

        };


        if (node.equals(linkBase.getTo())){
            Object startPosition = null;
            try {
                startPosition = QI.XPropertySet(linkBase.getConnShape1()).getPropertyValue("EndPosition");
                Point point = (Point) startPosition;
                return new Point2D.Double(point.X, point.Y);
            } catch (UnknownPropertyException | WrappedTargetException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

        };

        throw new IllegalArgumentException();
    }
}
