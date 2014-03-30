package ru.ssau.graphplus.api;

import com.sun.star.awt.Point;
import com.sun.star.awt.Size;
import com.sun.star.drawing.XShape;

public interface Node extends DiagramElement{

    NodeType getType();

//    XShape getShape();

    Size getSize();

    Point getPosition();


    public enum NodeType {


        // Process Diagram Type

        /**
         ________
        |        \
        |         \
        |________/

         */
        ClientPort,

        /**
         ________
        |        /
        |       /
        |       \
        |________\
         */

        ServerPort,

        /**
         *   __________
         *  |          |
         *  |__________|
         *
         */
        StartMethodOfProcess,


        /**      ________
         *     /        \
         *    |          |
         *     \________/
         */
        MethodOfProcess,



        // Channel Diagram Type

        ClientPortState,
        ServerPortState,
        StartStateOfClient

    }

}
