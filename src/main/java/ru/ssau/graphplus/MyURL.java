package ru.ssau.graphplus;

import com.sun.star.util.URL;

/**
* User: anton
* Date: 5/18/13
* Time: 2:51 AM
*/
public class MyURL {
    URL url;

    MyURL(URL url) {
        this.url = url;
    }

    @Override
    public int hashCode() {
        return url.Complete.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MyURL myURL = (MyURL) o;

        if (!url.Complete.equals(myURL.url.Complete)) return false;

        return true;
    }
}
