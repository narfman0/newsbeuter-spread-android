package org.atlaslabs.newsbeuterspread.models;

public class Item {
    public String author, content, pub_date, title, url;
    public int id;

    @Override
    public String toString(){
        return "[Item title: " + title + " id: " + id + "]";
    }
}
