package com.udl.lluis.onsou.entities;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Lluís on 19/03/2015.
 */
public class MyDevice {

    private static MyDevice instance;

    private Long id = Long.MIN_VALUE;
    private String GCMId = "";
    private String name = "";
    private LatLng position;

    private boolean isOnline = false;

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

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public String getGCMId() {
        return GCMId;
    }

    public void setGCMId(String GCMId) {
        this.GCMId = GCMId;
    }

    public boolean isOnline() {
        return isOnline && id != null && GCMId !="";
    }

    public void setOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }
}
