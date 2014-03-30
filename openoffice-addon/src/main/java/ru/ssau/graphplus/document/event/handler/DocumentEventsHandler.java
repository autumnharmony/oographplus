/*
 * Copyright (c) 2013. Anton Borisov
 */

package ru.ssau.graphplus.document.event.handler;

import com.sun.star.document.DocumentEvent;
import ru.ssau.graphplus.document.event.handler.impl.DocumentEventsHandlerImpl;

public interface DocumentEventsHandler {
    void documentEventOccured(DocumentEvent documentEvent);
    void documentEventOccured(String eventName);

    void registerHandler(String event, DocumentEventHandler documentEventHandler);

    void registerHandler(Iterable<String> events, DocumentEventHandler documentEventHandler);

    void unregisterHandler(String event, DocumentEventHandler documentEventHandler);


}
