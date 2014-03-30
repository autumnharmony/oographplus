/*
 * Copyright (c) 2013. Anton Borisov
 */

package ru.ssau.graphplus;

import ru.ssau.graphplus.store.Base64Coder;

import java.io.*;

/**
 * User: anton
 * Date: 8/26/13
 * Time: 1:59 AM
 */
public class StringSerializer {


    public static Object fromString(String s) throws IOException,
            ClassNotFoundException {
        byte [] data = Base64Coder.decode(s);
        ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(  data ) );
        Object o  = ois.readObject();
        ois.close();
        return o;
    }

    /** Write the object to a Base64 string. */
    public static String toString(Serializable o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( o );
        oos.close();
        return new String( Base64Coder.encode( baos.toByteArray() ) );
    }
}
