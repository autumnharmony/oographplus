package ru.ssau.graphplus.codegen.impl;

import ru.ssau.graphplus.api.DiagramType;
import ru.ssau.graphplus.codegen.CodeGenerator;



/**
 * Created by anton on 12.05.14.
 */
public interface CodeGeneratorFactory {
    CodeGenerator create(String name, DiagramType diagramType);
}
