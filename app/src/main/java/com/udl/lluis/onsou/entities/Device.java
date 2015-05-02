package com.udl.lluis.onsou.entities;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Lluís on 19/03/2015.
 */
public class Device implements Serializable {

    private final long id;

    private final String name;
    private boolean isFriend;
    private boolean isOnline;

    //private LatLng position;
    private double latitude;
    private double longitude;

    private transient Marker marker;

    public Device(long id, String name, LatLng position, boolean isFriend, boolean isOnline) {
        this.id = id;
        this.name = name;
        //this.position = position;
        this.latitude = position.latitude;
        this.longitude = position.longitude;
        this.isFriend = isFriend;
        this.isOnline = isOnline;
    }
    public Device(long id, String name, double latitude, double longitude, boolean isFriend, boolean isOnline) {
        this.id = id;
        this.name = name;
        //this.position = position;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isFriend = isFriend;
        this.isOnline = isOnline;
    }

    public LatLng getPosition(){
        return new LatLng(this.latitude,this.longitude);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isFriend() {
        return isFriend;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }


    public static HashMap<Long, Device> getFromServer(ArrayList<com.lluis.onsou.backend.registration.model.Device> devices){
        HashMap<Long,Device> res = new HashMap<>();
        for(com.lluis.onsou.backend.registration.model.Device device : devices){
            if(device.getId().compareTo(MyDevice.getInstance().getId())!=0){
                Device d = new Device(device.getId(),device.getUsername(),device.getLatitude(),device.getLongitude(),device.getIsFriend(),device.getOnline());
                res.put(d.getId(),d);
            }
        }
        return res;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Device)) return false;

        Device device = (Device) o;

        if (id != device.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
