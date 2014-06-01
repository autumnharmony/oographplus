/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.sun.star.awt.XContainerWindowEventHandler;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XDialogEventHandler;
import com.sun.star.awt.XWindow;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.uno.*;
import com.sun.star.uno.RuntimeException;

import java.lang.Exception;
import java.util.HashMap;
import java.util.Map;

public class MyDialogHandler implements XDialogEventHandler, XContainerWindowEventHandler {

    Map<Event, EventHandler> eventHandlerMap = new HashMap<>();


    public static class Event {
        private String s;

        public Event(String s) {
            this.s = s;
        }

        public static Event event(String s) {
            return new Event(s);
        }

        public String getName() {
            return s;
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


    }


    @Override
    public boolean callHandlerMethod(XDialog xDialog, Object o, String s) throws WrappedTargetException {
        try {
            Event event = Event.event(s);
            EventHandler eventHandler = eventHandlerMap.get(event);
            return eventHandler.handle(xDialog, o, s);
        } catch (Exception ex) {
            throw new java.lang.RuntimeException(ex);
        }
    }

    @Override
    public boolean callHandlerMethod(XWindow xWindow, Object o, String s) throws WrappedTargetException {
        try {
            Event event = Event.event(s);
            EventHandler eventHandler = eventHandlerMap.get(event);
            return eventHandler.handle(null, o, s);
        } catch (Exception ex) {
            throw new RuntimeException("error", ex);
        }
    }

    @Override
    public String[] getSupportedMethodNames() {

        Iterable<String> transform = Iterables.transform(eventHandlerMap.keySet(), new Function<Event, String>() {
            @Override
            public String apply(Event event) {
                return event.getName();
            }
        });

        return Iterables.toArray(transform, String.class);
    }
}
