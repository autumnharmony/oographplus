package ru.ssau.graphplus.codegen.impl.analizer.matches;

import ru.ssau.graphplus.api.DiagramType;

import java.util.List;

/**
 * Created by anton on 31.03.14.
 */
public interface MatchFactory {
    List<Match> createAll(DiagramType diagramType);
}
