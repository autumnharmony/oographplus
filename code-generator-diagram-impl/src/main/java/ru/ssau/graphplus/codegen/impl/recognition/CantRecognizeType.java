package ru.ssau.graphplus.codegen.impl.recognition;

import com.sun.star.drawing.XShape;
import ru.ssau.graphplus.codegen.CodegenException;

public class CantRecognizeType extends CodegenException{

    private final XShape shape;

    public CantRecognizeType(XShape p0) {
        shape = p0;
    }

    public XShape getShape() {
        return shape;
    }
}
