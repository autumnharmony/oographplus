package ru.ssau.graphplus;

import com.sun.star.drawing.XShape;

/**
 * User: anton
 * Date: 3/8/13
 * Time: 6:16 PM
 */
public class AddingListMode {

    private int count;

    public boolean needToDetach() {
        return count == 3;
    }


    public void oneMoreTime(XShape xShape) {

        count++;
    }
}
