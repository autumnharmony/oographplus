/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sun.star.awt.*;
import com.sun.star.lang.WrappedTargetException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MyDialogHandler implements XDialogEventHandler, XContainerWindowEventHandler {

    Map<Event, EventHandler> eventHandlerMap = new HashMap<>();
    private final Iterable<Object> transform;

    public static class Event {
        String s;

        public Event(String s) {
            this.s = s;
        }

        public static Event event(String s){
             return new Event(s);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Event)) return false;

            Event event = (Event) o;

            if (!s.equals(event.s)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return s.hashCode();
        }
    }

    public interface EventHandler {
        boolean handle(XDialog xDialog, Object o, String s);
    }

    public MyDialogHandler(Map<Event, EventHandler> eventHandlerMap) {

        this.eventHandlerMap = eventHandlerMap;

        transform = Iterables.transform(eventHandlerMap.keySet(), new Function<Event, Object>() {
            @Override
            public Object apply(ru.ssau.graphplus.MyDialogHandler.Event input) {
                return input.s;
            }
        });

    }


    @Override
    public boolean callHandlerMethod(XDialog xDialog, Object o, String s) throws WrappedTargetException {
        try {
            Event event = Event.event(s);
            EventHandler eventHandler = eventHandlerMap.get(event);
            return eventHandler.handle(xDialog, o, s);
        }
        catch (Exception ex){
            throw new RuntimeException(ex);
            // TODO !!!
//            return false;
        }
    }

    @Override
    public boolean callHandlerMethod(XWindow xWindow, Object o, String s) throws WrappedTargetException {
        try {
            Event event = Event.event(s);
            EventHandler eventHandler = eventHandlerMap.get(event);
            return eventHandler.handle(null, o, s);
        }
        catch (Exception ex){
            throw new RuntimeException(ex);
            // TODO !!!
//            return false;
        }
    }

    @Override
    public String[] getSupportedMethodNames() {

        Object[] objects = Lists.newArrayList(transform).toArray();
        return (String[]) objects;
    }
}
