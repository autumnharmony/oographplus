/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.codegen.impl;

import com.google.common.base.Strings;
import ru.ssau.graphplus.commons.ConnectedShapesComplex;
import ru.ssau.graphplus.commons.ShapeHelper;
import ru.ssau.graphplus.commons.ShapeHelperWrapper;
import ru.ssau.graphplus.codegen.impl.recognition.LinkTypeRecogniser;

public class LinkCodeBaseImpl extends LinkCodeBase {


    public LinkCodeBaseImpl(ConnectedShapesComplex connectedShapesComplex, LinkTypeRecogniser linkTypeRecogniser, ShapeHelperWrapper shapeHelper) {
        super(connectedShapesComplex, linkTypeRecogniser, shapeHelper);
    }

    public LinkCodeBaseImpl(ConnectedShapesComplex connectedShapesComplex) {
        super(connectedShapesComplex, null, null);
    }

    @Override
    public String getCode() {
        return ""+ (Strings.isNullOrEmpty(ShapeHelper.getText(connectedShapesComplex.fromShape)) ? "unknown" : ShapeHelper.getText(connectedShapesComplex.fromShape))
        + (Strings.isNullOrEmpty(ShapeHelper.getText(connectedShapesComplex.toShape)) ? "unknown" : ShapeHelper.getText(connectedShapesComplex.toShape))

        + (Strings.isNullOrEmpty(ShapeHelper.getText(connectedShapesComplex.toShape)) ? "unknown" : ShapeHelper.getText(connectedShapesComplex.toShape));
    }
}
