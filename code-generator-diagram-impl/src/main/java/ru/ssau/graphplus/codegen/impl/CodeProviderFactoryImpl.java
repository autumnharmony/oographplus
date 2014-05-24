package ru.ssau.graphplus.codegen.impl;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import ru.ssau.graphplus.api.Node;
import ru.ssau.graphplus.codegen.CodeProvider;
import ru.ssau.graphplus.codegen.impl.recognition.LinkTypeRecogniser;
import ru.ssau.graphplus.commons.ConnectedShapesComplex;
import ru.ssau.graphplus.commons.ShapeHelperWrapper;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;


public class CodeProviderFactoryImpl implements CodeProviderFactory {


    private final LinkTypeRecogniser linkTypeRecogniser;
    private final ShapeHelperWrapper shapeHelper;

    @Inject
    public CodeProviderFactoryImpl(LinkTypeRecogniser linkTypeRecogniser, ShapeHelperWrapper shapeHelper) {
        this.linkTypeRecogniser = linkTypeRecogniser;
        this.shapeHelper = shapeHelper;
    }

    class MultipleCauseException extends RuntimeException {

        List<Throwable> throwables;

        MultipleCauseException(List<Throwable> throwables) {
            this.throwables = throwables;
        }

        MultipleCauseException(String message, List<Throwable> throwables) {
            super(message);
            this.throwables = throwables;
        }

        @Override
        public void printStackTrace() {
            for (Throwable throwable : throwables) {
                throwable.printStackTrace();
            }
        }

        @Override
        public void printStackTrace(PrintStream s) {
            for (Throwable throwable : throwables) {
                throwable.printStackTrace(s);
            }
        }

        @Override
        public void printStackTrace(PrintWriter s) {
            for (Throwable throwable : throwables) {
                throwable.printStackTrace(s);
            }
        }
    }

    @Override
    public CodeProvider create(Class<? extends CodeProvider> codeProviderClass, Map<String, Object> params) {

        boolean fail;
        Exception exception1;
        Exception exception2;
        try {
            Constructor<? extends CodeProvider> constructor = codeProviderClass.getConstructor(ConnectedShapesComplex.class, LinkTypeRecogniser.class, ShapeHelperWrapper.class);
            CodeProvider codeProvider1 = constructor.newInstance(null, linkTypeRecogniser, shapeHelper);
            return codeProvider1;
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            fail = true;
            exception1 = e;
        }

        if (fail) {
            try {
                Constructor<? extends CodeProvider> constructor = codeProviderClass.getConstructor(Node.class, Map.class, Map.class);
                CodeProvider codeProvider = constructor.newInstance(params.get("node"), params.get("out"), params.get("in"));
                return codeProvider;
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                exception2 = e;
                throw new MultipleCauseException("can't create code provider", Lists.<Throwable>newArrayList(exception1, exception2));
            }
        } else {
            throw new IllegalArgumentException("can't create code provider");
        }


    }
}
