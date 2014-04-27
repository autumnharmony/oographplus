package ru.ssau.graphplus.analizer.matches;

import ru.ssau.graphplus.analizer.matches.Match;
import ru.ssau.graphplus.api.DiagramType;

import java.util.List;

/**
 * Created by anton on 31.03.14.
 */
public interface MatchFactory {
    List<Match> createAll(DiagramType diagramType);
}
