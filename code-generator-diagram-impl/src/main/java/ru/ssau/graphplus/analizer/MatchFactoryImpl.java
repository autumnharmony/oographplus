package ru.ssau.graphplus.analizer;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import ru.ssau.graphplus.ShapeHelperWrapper;

import java.util.List;


public class MatchFactoryImpl implements MatchFactory {

    @Inject
    private ShapeHelperWrapper shapeHelperWrapper;

    @Override
    public List<Match> createAll() {
        return Lists.<Match>newArrayList(new MethodToMethodOnSuccess(shapeHelperWrapper), new MethodToMethodOnFail(shapeHelperWrapper), new PortToMethodOnMessage(shapeHelperWrapper), new PortToMethodOnMessage2(shapeHelperWrapper));
    }
}
