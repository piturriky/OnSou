package com.Lluis.onSou.backend.model;

import java.util.HashMap;

/**
 * Created by Lluís on 25/04/2015.
 */
public class Result {
    private boolean status;
    private int errorType;
    private String msg;
    private Object obj;

    public static HashMap<Integer,String> errorTypes = new HashMap<Integer,String>(){{
        put(1,"Invalid username");
        put(2,"Invalid password");
        put(3,"Invalid username, already exist");
        put(4,"Invalid id, device not registered");
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
}
