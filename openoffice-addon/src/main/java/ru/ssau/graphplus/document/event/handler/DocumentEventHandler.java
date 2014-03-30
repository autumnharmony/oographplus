/*
 * Copyright (c) 2013. Anton Borisov
 */

package ru.ssau.graphplus.document.event.handler;

import com.sun.star.document.DocumentEvent;

public interface DocumentEventHandler {

    void documentEventOccured(DocumentEvent documentEvent);

    void documentEventOccured(String eventName);
}
