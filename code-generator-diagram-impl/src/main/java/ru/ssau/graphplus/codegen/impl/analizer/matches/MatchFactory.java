package ru.ssau.graphplus.codegen.impl.analizer.matches;

import ru.ssau.graphplus.api.DiagramType;
import ru.ssau.graphplus.codegen.CodeProvider;

import java.util.List;
import java.util.Map;

/**
 * Created by anton on 31.03.14.
 */
public interface MatchFactory {
    List<Match> createAll(DiagramType diagramType);
    Map<Match, CodeProvider> getMatchToCodeProvider(DiagramType diagramType);
}
