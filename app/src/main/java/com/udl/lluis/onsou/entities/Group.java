package com.udl.lluis.onsou.entities;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Lluís on 21/03/2015.
 */
public class Group {

    private Long id;
    private String name;
    private List<Device> devices = new ArrayList<>();

    private Color color;

    public Group(String name, Color color){
        this.name = name;
        this.color = color;

        // Set ID
        //this.id = getNewId();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List< Device> getDevices() {
        return devices;
    }

    public void addDevice(Device device){
        devices.add(device);
    }

    public Color getColor(){
        return color;
    }

    public void setColor(Color color){
        this.color = color;
    }
}
