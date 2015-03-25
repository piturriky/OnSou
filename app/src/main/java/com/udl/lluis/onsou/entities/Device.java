package com.udl.lluis.onsou.entities;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

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

    public Device(long id, String name, LatLng position, boolean isFriend, boolean isOnline) {
        this.id = id;
        this.name = name;
        //this.position = position;
        this.latitude = position.latitude;
        this.longitude = position.longitude;
        this.isFriend = isFriend;
        this.isOnline = isOnline;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
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
