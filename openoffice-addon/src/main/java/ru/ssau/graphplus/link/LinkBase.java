package ru.ssau.graphplus.link;

import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.drawing.TextHorizontalAdjust;
import com.sun.star.drawing.TextVerticalAdjust;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import ru.ssau.graphplus.api.Link;
import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.node.NodeBase;


public abstract class LinkBase implements ShapesProvider, Link {

    protected NodeBase node1, node2;

    public void setStartNode(Node node1) {
        if (node1 instanceof NodeBase) {
            this.node1 = (NodeBase) node1;
        }
    }

    //    @Override
    public void setEndNode(Node node2) {
        if (node2 instanceof NodeBase) {
            this.node2 = (NodeBase) node2;
        }
    }

    protected interface LinkStyle {
        void applyStyleForHalf1(XPropertySet xPropertySet) throws UnknownPropertyException, PropertyVetoException, WrappedTargetException, com.sun.star.lang.IllegalArgumentException;

        void applyStyleForHalf2(XPropertySet xPropertySet) throws UnknownPropertyException, PropertyVetoException, WrappedTargetException, IllegalArgumentException;

        void applyStyleForText(XPropertySet xPropertySet) throws UnknownPropertyException, PropertyVetoException, WrappedTargetException, IllegalArgumentException;
    }


    protected abstract class LinkStyleBase implements LinkStyle {
        @Override
        public void applyStyleForText(XPropertySet xPStext) throws UnknownPropertyException, PropertyVetoException, WrappedTargetException, com.sun.star.lang.IllegalArgumentException {

            xPStext.setPropertyValue("TextVerticalAdjust", TextVerticalAdjust.CENTER);
            xPStext.setPropertyValue("TextHorizontalAdjust", TextHorizontalAdjust.CENTER);
            xPStext.setPropertyValue("TextAutoGrowWidth", new Boolean(true));

        }
    }

    protected interface LinkApplyer {
        void apply(LinkStyle linkStyle, LinkBase linkBase);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LinkBase)) return false;

        LinkBase linkBase = (LinkBase) o;

        if (node1 != null ? !node1.equals(linkBase.node1) : linkBase.node1 != null) return false;
        if (node2 != null ? !node2.equals(linkBase.node2) : linkBase.node2 != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = node1 != null ? node1.hashCode() : 0;
        result = 31 * result + (node2 != null ? node2.hashCode() : 0);
        return result;
    }
}
