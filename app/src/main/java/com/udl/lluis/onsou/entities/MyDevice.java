package com.udl.lluis.onsou.entities;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Lluís on 19/03/2015.
 */
public class MyDevice {

    private static MyDevice instance;

    private long id;

    private String name;

    private LatLng position;

    private MyDevice(){
    }

    public static MyDevice getInstance(){
        if (instance == null) {
            instance = new MyDevice();
        }
        return instance;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LatLng getPosition() {
        return position;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }
}
