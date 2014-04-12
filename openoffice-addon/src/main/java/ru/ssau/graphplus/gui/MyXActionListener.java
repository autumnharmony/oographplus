/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.gui;

import com.sun.star.awt.ActionEvent;
import com.sun.star.awt.XActionListener;
import com.sun.star.awt.XControl;
import com.sun.star.drawing.XShape;

public class MyXActionListener implements XActionListener {
    protected XControl dialogControl;
    protected XShape xShape;

    public MyXActionListener(XShape xShape, XControl m_xDialogControl) {
        this.xShape = xShape;
        this.dialogControl = m_xDialogControl;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        System.out.println("act");
    }

    @Override
    public void disposing(com.sun.star.lang.EventObject eventObject) {

    }
}
