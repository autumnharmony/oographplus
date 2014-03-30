package ru.ssau.graphplus.api;

public interface DiagramElement  {

    /**
     * User defined name of diagram element
     * @return
     */
    String getName();


    /**
     * Unique id for inner purposes
     * @return
     */
    String getId();


    void setId(String id);


    // no setName 'coz it's no need
}
