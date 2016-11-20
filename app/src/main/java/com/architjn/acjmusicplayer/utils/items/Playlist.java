package com.architjn.acjmusicplayer.utils.items;

public class Playlist {

    int id;
    String name;

    public Playlist(int id, String name){
        this.id = id;
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
