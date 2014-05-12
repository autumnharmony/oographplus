/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.codegen.impl.analizer.matches;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import ru.ssau.graphplus.codegen.impl.CodeProviderAnnotation;
import ru.ssau.graphplus.api.DiagramType;
import ru.ssau.graphplus.codegen.CodeProvider;
import ru.ssau.graphplus.commons.ConnectedShapesComplex;
import ru.ssau.graphplus.commons.ShapeHelperWrapper;
import ru.ssau.graphplus.codegen.impl.recognition.LinkTypeRecogniser;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class MatchFactoryImpl implements MatchFactory {

    private final ShapeHelperWrapper shapeHelper;
    private final LinkTypeRecogniser linkTypeRecogniser;

    @Inject
    public MatchFactoryImpl(ShapeHelperWrapper shapeHelper, LinkTypeRecogniser linkTypeRecogniser) {
        this.shapeHelper = shapeHelper;
        this.linkTypeRecogniser = linkTypeRecogniser;
    }

    Set<Class<? extends Match>> processMatchSet = Sets.<Class<? extends Match>>newHashSet(DataAndControlMixedMatch.class, MethodToMethodOnFail.class, MethodToMethodOnSuccess.class, PortToMethodDefault.class, PortToMethodOnMessage.class, ReadMessageFromPort.class, SendMessageToPort.class);
    Set<Class<? extends Match>> channelMatchSet = Sets.<Class<? extends Match>>newHashSet(StateTransition.class);


    Map<DiagramType, Set<Class<? extends Match>>> matchesClasses = ImmutableMap.<DiagramType, Set<Class<? extends Match>>>builder()

            .put(DiagramType.Channel, channelMatchSet)
            .put(DiagramType.Process, processMatchSet)

            .build();

    public List<Match> createAll(DiagramType diagramType) {
        List<Match> matches = new ArrayList<>();
        for (Class<? extends Match> aClass1 : processMatchSet) {
            matches.add(create(aClass1));
        }
        return matches;
    }

    public Map<Match, CodeProvider> getMatchToCodeProvider(DiagramType diagramType) {

        Map<Match, CodeProvider> map = new HashMap<>();

        for (Class<? extends Match> aClass1 : matchesClasses.get(diagramType)) {

            Match match = create(aClass1);

            CodeProviderAnnotation annotation = match.getClass().getAnnotation(CodeProviderAnnotation.class);

            CodeProvider codeP = null;
            if (annotation != null) {
                Class<? extends CodeProvider> codeProvider = annotation.codeProvider();


                try {
                    Constructor<? extends CodeProvider> constructor = codeProvider.getConstructor(ConnectedShapesComplex.class, LinkTypeRecogniser.class, ShapeHelperWrapper.class);
                    CodeProvider codeProvider1 = constructor.newInstance(null, linkTypeRecogniser, shapeHelper);
                    codeP = codeProvider1;
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
            map.put(match, codeP);
        }

        return map;
    }




    private Match create(Class<? extends Match> aClass) {
        Constructor<? extends Match> constructor = null;
        try {
            constructor = aClass.getConstructor(ShapeHelperWrapper.class, LinkTypeRecogniser.class);
            return constructor.newInstance(shapeHelper, linkTypeRecogniser);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
