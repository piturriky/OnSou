package com.Lluis.onSou.backend.model;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.ArrayList;

/**
 * Created by Lluís on 19/03/2015.
 */
@Entity
public class Device{

    @Id
    Long id;

    @Index
    private String GCMId;
    @Index
    private String username;
    @Index
    private String pass;
    @Index
    private boolean isOnline;
    @Index
    private boolean onlyForFriends;
    @Index
    private double latitude;
    @Index
    private double longitude;
    @Index
    private ArrayList<Long> friendsList = new ArrayList<>();

    public Device(){}

    public Device(String username, String pass) {
        this.username = username;
        this.pass = pass;
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPass() {
        return pass;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public String getGCMId() {
        return GCMId;
    }

    public void setGCMId(String GCMId) {
        this.GCMId = GCMId;
    }

    public void setUsername(String usename) {
        this.username = username;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    public boolean isOnlyForFriends() {
        return onlyForFriends;
    }

    public void setOnlyForFriends(boolean onlyForFriends) {
        this.onlyForFriends = onlyForFriends;
    }

    public void addFriend(Long id){
        if(!friendsList.contains(id)){
            friendsList.add(id);
        }
    }

    public void removeFriend(Long id){
        if(friendsList.contains(id)){
            friendsList.remove(id);
        }
    }

    public boolean isMyFriend(Long id){
        return friendsList.contains(id);
    }

    public ArrayList<Long> getFriends(){
        return friendsList;
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
