/*
 * Copyright (c) 2013. Anton Borisov
 */

package ru.ssau.graphplus.document.event.handler.impl;

import com.sun.star.document.DocumentEvent;
import junit.framework.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import ru.ssau.graphplus.document.event.handler.DocumentEventHandler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * User: anton
 * Date: 9/8/13
 * Time: 2:09 AM
 */
public class DocumentEventsHandlerImplTest {


    @Test
    public void testRegisterHandler() throws Exception {
        DocumentEventsHandlerImpl documentEventsHandler = new DocumentEventsHandlerImpl();
        DocumentEventHandler eventHandler = mock(DocumentEventHandler.class);
        documentEventsHandler.registerHandler("qwe", eventHandler);
        DocumentEvent documentEvent = new DocumentEvent();
        documentEvent.EventName = "qwe";
        documentEventsHandler.documentEventOccured(documentEvent);

        ArgumentCaptor<DocumentEvent> documentEventArgumentCaptor = ArgumentCaptor.forClass(DocumentEvent.class);
        verify(eventHandler).documentEventOccured(documentEventArgumentCaptor.capture());

        DocumentEvent value = documentEventArgumentCaptor.getValue();
        Assert.assertEquals(value, documentEvent);

    }

    @Test
    public void testUnregisterHandler() throws Exception {

    }
}
