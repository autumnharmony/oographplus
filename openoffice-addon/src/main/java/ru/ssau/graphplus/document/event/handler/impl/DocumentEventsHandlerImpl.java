/*
 * Copyright (c) 2013. Anton Borisov
 */

package ru.ssau.graphplus.document.event.handler.impl;

import com.sun.star.document.DocumentEvent;
import ru.ssau.graphplus.OOGraph;
import ru.ssau.graphplus.document.event.handler.DocumentEventHandler;
import ru.ssau.graphplus.document.event.handler.DocumentEventsHandler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * User: anton
 * Date: 9/8/13
 * Time: 1:23 AM
 */
public class DocumentEventsHandlerImpl implements DocumentEventsHandler {
    private Map<String, Set<DocumentEventHandler>> eventHandlerListMap = new HashMap<String, Set<DocumentEventHandler>>() {
    };


    public DocumentEventsHandlerImpl() {
//        OOGraph.LOGGER.info("DocumentEventsHandlerImpl ctor");
    }

    @Override
    public void registerHandler(String event, DocumentEventHandler documentEventHandler) {

        if (!eventHandlerListMap.containsKey(event)) {
            eventHandlerListMap.put(event, new HashSet<DocumentEventHandler>());
        }
        eventHandlerListMap.get(event).add(new OnlySpecificEventHandler(event, documentEventHandler));
    }

    @Override
    public void registerHandler(Iterable<String> events, DocumentEventHandler documentEventHandler) {

        for (String event : events) {
            if (!eventHandlerListMap.containsKey(event)) {
                eventHandlerListMap.put(event, new HashSet<DocumentEventHandler>());
            }
            eventHandlerListMap.get(event).add(documentEventHandler);
        }
    }

    @Override
    public void unregisterHandler(String event, DocumentEventHandler documentEventHandler) {
        if (eventHandlerListMap.containsKey(event)) {
            eventHandlerListMap.get(event).remove(documentEventHandler);
        }
    }

    @Override
    public void documentEventOccured(DocumentEvent documentEvent) {
        Set<DocumentEventHandler> documentEventHandlers = eventHandlerListMap.get(documentEvent.EventName);
        if (documentEventHandlers == null){
            return;
        }
        for (DocumentEventHandler documentEventHandler : documentEventHandlers) {
            documentEventHandler.documentEventOccured(documentEvent);
        }

    }

    @Override
    public void documentEventOccured(String eventName) {

        Set<DocumentEventHandler> documentEventHandlers = eventHandlerListMap.get(eventName);
        if (documentEventHandlers == null){
            return;
        }

        for (DocumentEventHandler documentEventHandler : eventHandlerListMap.get(eventName)) {
            documentEventHandler.documentEventOccured(eventName);
        }
    }

    private class OnlySpecificEventHandler implements DocumentEventHandler {

        private final DocumentEventHandler documentEventHandler;
        private final String event;

        public OnlySpecificEventHandler(String event, DocumentEventHandler documentEventHandler) {
            this.event = event;
            this.documentEventHandler = documentEventHandler;
        }

        @Override
        public void documentEventOccured(DocumentEvent documentEvent) {
            if (documentEvent.EventName.equals(event)) {
                documentEventHandler.documentEventOccured(documentEvent);
            } else {
                // ignore ?
            }
        }

        @Override
        public void documentEventOccured(String eventName) {
//            OOGraph.LOGGER.info("IMPLEMENT");
        }


    }

    private abstract class MultiEventHandler implements DocumentEventHandler {
        public MultiEventHandler(Iterable<String> events, DocumentEventHandler documentEventHandler) {
        }

        @Override
        public void documentEventOccured(DocumentEvent documentEvent) {
            //TODO implement
        }
    }
}
