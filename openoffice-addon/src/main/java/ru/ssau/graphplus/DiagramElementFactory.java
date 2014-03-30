/*
 * Copyright (c) 2014. Anton Borisov
 */

package ru.ssau.graphplus;

import com.sun.star.lang.XMultiServiceFactory;


public class DiagramElementFactory {

    protected int counter;

    protected XMultiServiceFactory xmsf;

    public int getCount() {
        return counter++;
    }

    public void setCount(int count) {
        this.counter = counter;
    }

    public DiagramElementFactory(XMultiServiceFactory xmsf) {
        counter  = 0;
        this.xmsf = xmsf;
    }



    public DiagramElementFactory() {
        counter = 0;
    }
}
