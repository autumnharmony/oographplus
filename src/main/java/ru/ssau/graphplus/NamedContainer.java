/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ssau.graphplus;

import com.sun.star.container.ElementExistException;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XNameContainer;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.uno.Type;

import java.util.HashMap;
import java.util.Map;

/**
 * @author anton
 */
public class NamedContainer implements XNameContainer {


    //    Proxy proxy = new NamedContainer().
    Map<String, Object> map;

    public NamedContainer() {
        map = new HashMap<String, Object>();
    }

    public void insertByName(String string, Object o) throws IllegalArgumentException, ElementExistException, WrappedTargetException {
        //throw new UnsupportedOperationException("Not supported yet.");
        if (map.containsKey(string)) {
            throw new ElementExistException();
        }
        map.put(string, o);
    }

    public void removeByName(String string) throws NoSuchElementException, WrappedTargetException {
        if (!map.containsKey(string)) {
            throw new NoSuchElementException();
        }
        map.remove(string);
        //        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void replaceByName(String string, Object o) throws IllegalArgumentException, NoSuchElementException, WrappedTargetException {
        //throw new UnsupportedOperationException("Not supported yet.");
        if (map.containsKey(string)) map.put(string, o);
        else throw new NoSuchElementException();
    }

    public Object getByName(String string) throws NoSuchElementException, WrappedTargetException {
        if (map.containsKey(string)) return map.get(string);
        throw new NoSuchElementException();
    }

    public String[] getElementNames() {
        return new String[]{};
    }

    public boolean hasByName(String string) {
        return map.containsKey(string);
    }

    public Type getElementType() {
        return Type.ANY;
    }

    public boolean hasElements() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
