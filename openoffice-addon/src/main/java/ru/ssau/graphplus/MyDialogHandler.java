/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XDialogEventHandler;
import com.sun.star.lang.WrappedTargetException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: anton
 * Date: 2/9/14
 * Time: 5:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class MyDialogHandler implements XDialogEventHandler {

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
            return eventHandlerMap.get(MyDialogHandler.Event.event(s)).handle(xDialog, o, s);
        }
        catch (Exception ex){
            // TODO !!!
            return false;
        }
    }

    @Override
    public String[] getSupportedMethodNames() {

        return (String[]) Lists.newArrayList(transform).toArray();
    }
}
