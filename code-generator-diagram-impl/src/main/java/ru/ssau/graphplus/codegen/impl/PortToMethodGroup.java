package ru.ssau.graphplus.codegen.impl;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.sun.star.drawing.XShape;
import ru.ssau.graphplus.codegen.impl.recognition.LinkTypeRecogniser;
import ru.ssau.graphplus.commons.ConnectedShapesComplex;
import ru.ssau.graphplus.commons.ShapeHelperWrapper;

import java.util.List;


public class PortToMethodGroup extends LinkCodeGroupBase<PortToMethodCode> implements GroupingCode<PortToMethodCode>, PortToMethodCode {



    public PortToMethodGroup(ConnectedShapesComplex connectedShapesComplex, LinkTypeRecogniser linkTypeRecogniser, ShapeHelperWrapper shapeHelper) {
        super(connectedShapesComplex, linkTypeRecogniser, shapeHelper);
    }

    @Override
    protected XShape from() {
        return ((LinkCodeBase)group.get(0)).from();
    }

    @Override
    public String getCode() {
        Optional<PortToMethodCode> found = Iterables.tryFind(group, new Predicate<PortToMethodCode>() {
            @Override
            public boolean apply(PortToMethodCode portToMethodCode) {
                return portToMethodCode instanceof PortToMethodDefaultCode;
            }
        });

        PortToMethodCode portToMethodCode1 = null;
        if (found.isPresent()){
            portToMethodCode1 = found.get();
        }
        else {
            throw new IllegalStateException();
        }


        final PortToMethodCode finalPortToMethodCode = portToMethodCode1;
        Iterable<PortToMethodCode> anothers = Iterables.filter(group, new Predicate<PortToMethodCode>() {
            @Override
            public boolean apply(PortToMethodCode portToMethodCode) {
                return !portToMethodCode.equals(finalPortToMethodCode);
            }
        });

        StringBuffer buffer = new StringBuffer();
        buffer.append(text(from()));
        buffer.append(getPortChar(from()));
        for (PortToMethodCode another : anothers){
            buffer.append(((LinkCodeBase) another).linkText());
            buffer.append("->");
            buffer.append(text(((LinkCodeBase) another).to()));
            buffer.append("|");
        }
        buffer.append("->");
        buffer.append(text(((LinkCodeBase) portToMethodCode1).to()));

        buffer.append(";");
        return buffer.toString();
    }


}
