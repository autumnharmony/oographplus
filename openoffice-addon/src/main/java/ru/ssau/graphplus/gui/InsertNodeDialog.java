/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus.gui;

import ru.ssau.graphplus.MyDispatch;

/**
 * Created with IntelliJ IDEA.
 * User: anton
 * Date: 2/15/14
 * Time: 12:04 AM
 * To change this template use File | Settings | File Templates.
 */
public class InsertNodeDialog {

    private final MyDispatch myDispatch;
    private InsertNodePanel insertNodePanel;

    public InsertNodeDialog(MyDispatch myDispatch1) {
        myDispatch = myDispatch1;
    }

    public void init(InsertNodePanel insertNodePanel) {
        this.insertNodePanel = insertNodePanel;
    }
}
