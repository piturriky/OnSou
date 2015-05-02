package com.Lluis.onSou.backend.model;


import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by Lluís on 25/04/2015.
 */
public class Result {
    private boolean status;
    private int errorType;
    private String msg;
    private Object obj;
    private Device device;
    private ArrayList<Device> devices;


    public static HashMap<Integer,String> errorTypes = new HashMap<Integer,String>(){{
        put(1,"Invalid username");
        put(2,"Invalid password");
        put(3,"Invalid username, already exist");
        put(4,"Invalid id, device not registered");
        put(5,"Already Friends!!");
    }};

    public Result(){}

    public Result(boolean status){
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    public int getErrorType() {
        return errorType;
    }

    public void setErrorType(int errorType) {
        this.errorType = errorType;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public ArrayList<Device> getDevices() {
        return devices;
    }

    public void setDevices(ArrayList<Device> devices) {
        this.devices = devices;
    }
}

